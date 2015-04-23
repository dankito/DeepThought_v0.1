package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Mapped statement for updating an object.
 * 
 * @author graywatson
 */
public class MappedUpdate<T, ID> extends BaseMappedStatement<T, ID> {

	private final PropertyConfig versionPropertyConfig;
	private final int versionFieldTypeIndex;

	private MappedUpdate(EntityConfig<T, ID> entityConfig, String statement, PropertyConfig[] argPropertyConfigs,
			PropertyConfig versionPropertyConfig, int versionFieldTypeIndex) {
		super(entityConfig, statement, argPropertyConfigs);
		this.versionPropertyConfig = versionPropertyConfig;
		this.versionFieldTypeIndex = versionFieldTypeIndex;
	}

	public static <T, ID> MappedUpdate<T, ID> build(DatabaseType databaseType, EntityConfig<T, ID> entityConfig)
			throws SQLException {
		PropertyConfig idField = entityConfig.getIdProperty();
		if (idField == null) {
			throw new SQLException("Cannot update " + entityConfig.getEntityClass() + " because it doesn't have an id field");
		}

		StringBuilder sb = new StringBuilder(64);
		appendTableName(databaseType, sb, "UPDATE ", entityConfig.getTableName());
		boolean first = true;
		int argFieldC = 0;
		PropertyConfig versionPropertyConfig = null;
		int versionFieldTypeIndex = -1;
		// first we count up how many arguments we are going to have
		for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
			if (isFieldUpdatable(propertyConfig, idField)) {
				if (propertyConfig.isVersion()) {
					versionPropertyConfig = propertyConfig;
					versionFieldTypeIndex = argFieldC;
				}
				argFieldC++;
			}
		}

		// one more for where id = ?
		argFieldC++;
		if (versionPropertyConfig != null) {
			// one more for the AND version = ?
			argFieldC++;
		}

		PropertyConfig[] argPropertyConfigs = new PropertyConfig[argFieldC];
		argFieldC = 0;
		for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
			if (!isFieldUpdatable(propertyConfig, idField)) {
				continue;
			}
			if (first) {
				sb.append("SET ");
				first = false;
			} else {
				sb.append(", ");
			}
			appendFieldColumnName(databaseType, sb, propertyConfig, null);
			argPropertyConfigs[argFieldC++] = propertyConfig;
			sb.append("= ?");
		}

		sb.append(' ');
		appendWhereFieldEq(databaseType, idField, sb, null);

		argPropertyConfigs[argFieldC++] = idField;
		if (versionPropertyConfig != null) {
			sb.append(" AND ");
			appendFieldColumnName(databaseType, sb, versionPropertyConfig, null);
			sb.append("= ?");
			argPropertyConfigs[argFieldC++] = versionPropertyConfig;
		}

		return new MappedUpdate<T, ID>(entityConfig, sb.toString(), argPropertyConfigs, versionPropertyConfig, versionFieldTypeIndex);
	}

	/**
	 * Update the object in the database.
	 */
	public int update(DatabaseConnection databaseConnection, Object data, ObjectCache objectCache) throws SQLException {
		try {
			// there is always and id field as an argument so just return 0 lines updated
			if (argPropertyConfigs.length <= 1) {
				return 0;
			}
			Object[] args = getFieldObjects(data);
			Object newVersion = null;
			if (versionPropertyConfig != null) {
				newVersion = versionPropertyConfig.extractJavaFieldValue(data);
				newVersion = versionPropertyConfig.moveToNextValue(newVersion);
				args[versionFieldTypeIndex] = versionPropertyConfig.convertJavaFieldToSqlArgValue(newVersion);
			}

			int rowC = databaseConnection.update(statement, args, argPropertyConfigs);
			if (rowC > 0) {
				if (newVersion != null) {
					// if we have updated a row then update the version field in our object to the new value
					versionPropertyConfig.assignField(data, newVersion, false, null);
				}
				if (objectCache != null) {
					// if we've changed something then see if we need to update our cache
					Object id = idField.extractJavaFieldValue(data);
					T cachedData = objectCache.get(clazz, id);
					if (cachedData != null && cachedData != data) {
						// copy each field from the updated data into the cached object
						for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
							if (propertyConfig != idField) {
								propertyConfig.assignField(cachedData, propertyConfig.extractJavaFieldValue(data), false,
										objectCache);
							}
						}
					}
				}

        // cda:
//        persistColumnsWithCascadeMerge((T) data);
			}

			logger.debug("update data with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("update arguments: {}", (Object) args);
			}

			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run update stmt on object " + data + ": " + statement, e);
		}
	}

  protected void persistColumnsWithCascadeMerge(T data) throws SQLException {
    persistForeignCollectionsWithCascadeMerge(data);

    persistJoinColumnsWithCascadeMerge(data);
  }

  protected void persistForeignCollectionsWithCascadeMerge(T data) throws SQLException {
    for(PropertyConfig foreignCollectionField : entityConfig.getForeignCollections()) {
      if(foreignCollectionField.cascadeMerge()) {
      Object foreignObj = foreignCollectionField.extractRawJavaFieldValue(data);
      if(foreignObj instanceof Collection) {
        for(Object item : (Collection<?>)foreignObj) {
          if (entityConfig.getIdProperty().isObjectsFieldValueDefault(item))
            foreignCollectionField.createWithForeignDao(item);

          if((foreignCollectionField.isOneToManyField() && foreignCollectionField.getOneToManyConfig().isBidirectional()) ||
              foreignCollectionField.isManyToManyField() && foreignCollectionField.getManyToManyConfig().isBidirectional())
            foreignCollectionField.getForeignDao().update(item);
        }
      }
      }
    }
  }

  protected void persistJoinColumnsWithCascadeMerge(T data) throws SQLException {
    boolean entityNeedsUpdate = false;

    for(PropertyConfig joinColumn : entityConfig.getJoinColumns()) {
      if(joinColumn.cascadeMerge()) {
      Object foreignObj = joinColumn.extractRawJavaFieldValue(data);
      // TODO: check for cyclic dependencies
      if (foreignObj != null) {
        if(joinColumn.getForeignIdField().isObjectsFieldValueDefault(foreignObj)) {
          joinColumn.createWithForeignDao(foreignObj);

            entityNeedsUpdate = true;
        }
      }
      }
    }

    if(entityNeedsUpdate)
      entityConfig.getDao().update(data);
  }

  private static boolean isFieldUpdatable(PropertyConfig propertyConfig, PropertyConfig idField) {
		if (propertyConfig == idField || propertyConfig.isForeignCollection() || propertyConfig.isReadOnly() || propertyConfig.isUpdatable() == false) {
			return false;
		} else {
			return true;
		}
	}
}
