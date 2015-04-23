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
 * A mapped statement for deleting objects that correspond to a collection of IDs.
 * 
 * @author graywatson
 */
public class MappedDeleteCollection<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedDeleteCollection(EntityConfig<T, ID> entityConfig, String statement, PropertyConfig[] argPropertyConfigs) {
		super(entityConfig, statement, argPropertyConfigs);
	}

	/**
	 * Delete all of the objects in the collection. This builds a {@link MappedDeleteCollection} on the fly because the
	 * datas could be variable sized.
	 */
	public static <T, ID> int deleteObjects(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			DatabaseConnection databaseConnection, Collection<T> datas, ObjectCache objectCache) throws SQLException {
		MappedDeleteCollection<T, ID> deleteCollection =
				MappedDeleteCollection.build(databaseType, entityConfig, datas.size());
		Object[] fieldObjects = new Object[datas.size()];
		PropertyConfig idField = entityConfig.getIdProperty();
		int objC = 0;
		for (T data : datas) {
			fieldObjects[objC] = idField.extractJavaFieldToSqlArgValue(data);
			objC++;
		}
		return updateRows(databaseConnection, entityConfig.getEntityClass(), deleteCollection, fieldObjects, objectCache);
	}

	/**
	 * Delete all of the objects in the collection. This builds a {@link MappedDeleteCollection} on the fly because the
	 * ids could be variable sized.
	 */
	public static <T, ID> int deleteIds(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			DatabaseConnection databaseConnection, Collection<ID> ids, ObjectCache objectCache) throws SQLException {
		MappedDeleteCollection<T, ID> deleteCollection =
				MappedDeleteCollection.build(databaseType, entityConfig, ids.size());
		Object[] fieldObjects = new Object[ids.size()];
		PropertyConfig idField = entityConfig.getIdProperty();
		int objC = 0;
		for (ID id : ids) {
			fieldObjects[objC] = idField.convertJavaFieldToSqlArgValue(id);
			objC++;
		}
		return updateRows(databaseConnection, entityConfig.getEntityClass(), deleteCollection, fieldObjects, objectCache);
	}

	/**
	 * This is private because the execute is the only method that should be called here.
	 */
	private static <T, ID> MappedDeleteCollection<T, ID> build(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			int dataSize) throws SQLException {
		PropertyConfig idField = entityConfig.getIdProperty();
		if (idField == null) {
			throw new SQLException("Cannot delete " + entityConfig.getEntityClass()
					+ " because it doesn't have an id field defined");
		}
		StringBuilder sb = new StringBuilder(128);
		appendTableName(databaseType, sb, "DELETE FROM ", entityConfig.getTableName());
		PropertyConfig[] argPropertyConfigs = new PropertyConfig[dataSize];
		appendWhereIds(databaseType, idField, sb, dataSize, argPropertyConfigs);
		return new MappedDeleteCollection<T, ID>(entityConfig, sb.toString(), argPropertyConfigs);
	}

	private static <T, ID> int updateRows(DatabaseConnection databaseConnection, Class<T> clazz,
			MappedDeleteCollection<T, ID> deleteCollection, Object[] args, ObjectCache objectCache) throws SQLException {
		try {
			int rowC = databaseConnection.delete(deleteCollection.statement, args, deleteCollection.argPropertyConfigs);
			if (rowC > 0 && objectCache != null) {
				for (Object id : args) {
					objectCache.remove(clazz, id);
				}
			}
			logger.debug("delete-collection with statement '{}' and {} args, changed {} rows",
					deleteCollection.statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("delete-collection arguments: {}", (Object) args);
			}
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run delete collection stmt: " + deleteCollection.statement, e);
		}
	}

	private static void appendWhereIds(DatabaseType databaseType, PropertyConfig idField, StringBuilder sb, int numDatas,
			PropertyConfig[] propertyConfigs) {
		sb.append("WHERE ");
		databaseType.appendEscapedEntityName(sb, idField.getColumnName());
		sb.append(" IN (");
		boolean first = true;
		for (int i = 0; i < numDatas; i++) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			sb.append('?');
			if (propertyConfigs != null) {
				propertyConfigs[i] = idField;
			}
		}
		sb.append(") ");
	}
}
