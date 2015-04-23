package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;

import javax.persistence.InheritanceType;

/**
 * Mapped statement for querying for an object by its ID.
 * 
 * @author graywatson
 */
public class MappedQueryForId<T, ID> extends BaseMappedQuery<T, ID> {

	private final String label;

	protected MappedQueryForId(EntityConfig<T, ID> entityConfig, String statement, PropertyConfig[] argPropertyConfigs,
			PropertyConfig[] resultsPropertyConfigs, String label) {
		super(entityConfig, statement, argPropertyConfigs, resultsPropertyConfigs);
		this.label = label;
	}

	/**
	 * Query for an object in the database which matches the id argument.
	 */
	public T execute(DatabaseConnection databaseConnection, ID id, ObjectCache objectCache) throws SQLException {
		if (objectCache != null) {
			T result = objectCache.get(clazz, id);
			if (result != null) {
				return result;
			}
		}
		Object[] args = new Object[] { convertIdToFieldObject(id) };
		// @SuppressWarnings("unchecked")
		Object result = databaseConnection.queryForOne(statement, args, argPropertyConfigs, this, objectCache);
		if (result == null) {
			logger.debug("{} using '{}' and {} args, got no results", label, statement, args.length);
		} else if (result == DatabaseConnection.MORE_THAN_ONE) {
			logger.error("{} using '{}' and {} args, got >1 results", label, statement, args.length);
			logArgs(args);
			throw new SQLException(label + " got more than 1 result: " + statement);
		} else {
			logger.debug("{} using '{}' and {} args, got 1 result", label, statement, args.length);
		}
		logArgs(args);
		@SuppressWarnings("unchecked")
		T castResult = (T) result;
//    entityConfig.invokePostLoadLifeCycleMethod(castResult);
		return castResult;
	}

	public static <T, ID> MappedQueryForId<T, ID> build(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			PropertyConfig idPropertyConfig) throws SQLException {
		if (idPropertyConfig == null) {
			idPropertyConfig = entityConfig.getIdProperty();
			if (idPropertyConfig == null) {
				throw new SQLException("Cannot query-for-id with " + entityConfig.getEntityClass()
						+ " because it doesn't have an id field");
			}
		}
		String statement = buildStatement(databaseType, entityConfig, idPropertyConfig);
		return new MappedQueryForId<T, ID>(entityConfig, statement, new PropertyConfig[] {idPropertyConfig},
				entityConfig.getPropertyConfigs(), "query-for-id");
	}

	protected static <T, ID> String buildStatement(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			PropertyConfig idPropertyConfig) {
		// build the select statement by hand
		StringBuilder sb = new StringBuilder(64);
		appendTableName(databaseType, sb, "SELECT * FROM ", entityConfig.getTableName());

    if(entityConfig.getInheritance() == InheritanceType.JOINED)
      appendJoinForJoinedInheritanceTable(databaseType, entityConfig, sb);
    else
  		appendWhereFieldEq(databaseType, idPropertyConfig, sb, null);

		return sb.toString();
	}

  protected static <T, ID> void appendJoinForJoinedInheritanceTable(DatabaseType databaseType, EntityConfig<T, ID> entityConfig, StringBuilder sb) {
    int inheritanceLevel = 0;

    sb.append("t" + inheritanceLevel + " ");

    for(; inheritanceLevel < entityConfig.getTopDownInheritanceHierarchy().size() - 1; inheritanceLevel++) {
      EntityConfig inheritanceInfo = entityConfig.getTopDownInheritanceHierarchy().get(inheritanceLevel);
      sb.append(" LEFT OUTER JOIN ");
      if(inheritanceLevel < entityConfig.getTopDownInheritanceHierarchy().size() - 2)
        sb.append("(");
      appendTableName(databaseType, sb, "", entityConfig.getTopDownInheritanceHierarchy().get(inheritanceLevel).getTableName());
      sb.append(" t" + (inheritanceLevel + 1));
    }

    String idColumnName = entityConfig.getIdProperty().getColumnName();
    for(int i = entityConfig.getTopDownInheritanceHierarchy().size() - 2; i >= 0; i--) {
      sb.append(" ON (t" + (i + 1) + ".");
      databaseType.appendEscapedEntityName(sb, idColumnName);
      sb.append(" = t" + i + ".");
      databaseType.appendEscapedEntityName(sb, idColumnName);
      sb.append(")");

      if(i > 0)
        sb.append(")");
      else
        sb.append(" ");
    }

    sb.append("WHERE t0." );
    databaseType.appendEscapedEntityName(sb, idColumnName);
    sb.append(" = ?");
  }

  private void logArgs(Object[] args) {
		if (args.length > 0) {
			// need to do the (Object) cast to force args to be a single object and not an array
			logger.trace("{} arguments: {}", label, (Object) args);
		}
	}
}
