package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;

/**
 * Mapped statement for refreshing the fields in an object.
 * 
 * @author graywatson
 */
public class MappedRefresh<T, ID> extends MappedQueryForId<T, ID> {

	private MappedRefresh(EntityConfig<T, ID> entityConfig, String statement, PropertyConfig[] argPropertyConfigs,
			PropertyConfig[] resultPropertyConfigs) {
		super(entityConfig, statement, argPropertyConfigs, resultPropertyConfigs, "refresh");
	}

	/**
	 * Execute our refresh query statement and then update all of the fields in data with the fields from the result.
	 * 
	 * @return 1 if we found the object in the table by id or 0 if not.
	 */
	public int executeRefresh(DatabaseConnection databaseConnection, T data, ObjectCache objectCache)
			throws SQLException {
		@SuppressWarnings("unchecked")
		ID id = (ID) idField.extractJavaFieldValue(data);
		// we don't care about the cache here
		T result = super.execute(databaseConnection, id, null);
		if (result == null) {
			return 0;
		}
		// copy each field from the result into the passed in object
		for (PropertyConfig propertyConfig : resultsPropertyConfigs) {
			if (propertyConfig != idField) {
				propertyConfig.assignField(data, propertyConfig.extractJavaFieldValue(result), false, objectCache);
			}
		}
		return 1;
	}

	public static <T, ID> MappedRefresh<T, ID> build(DatabaseType databaseType, EntityConfig<T, ID> entityConfig)
			throws SQLException {
		PropertyConfig idField = entityConfig.getIdProperty();
		if (idField == null) {
			throw new SQLException("Cannot refresh " + entityConfig.getEntityClass()
					+ " because it doesn't have an id field");
		}
		String statement = buildStatement(databaseType, entityConfig, idField);
		return new MappedRefresh<T, ID>(entityConfig, statement, new PropertyConfig[] { entityConfig.getIdProperty() },
				entityConfig.getPropertyConfigs());
	}
}
