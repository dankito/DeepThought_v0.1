package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Abstract mapped statement which has common statements used by the subclasses.
 * 
 * @author graywatson
 */
public abstract class BaseMappedStatement<T, ID> {

  protected static Logger logger = LoggerFactory.getLogger(BaseMappedStatement.class);

	protected final EntityConfig<T, ID> entityConfig;
	protected final Class<T> clazz;
	protected final PropertyConfig idField;
	protected final String statement;
	protected final PropertyConfig[] argPropertyConfigs;

	protected BaseMappedStatement(EntityConfig<T, ID> entityConfig, String statement, PropertyConfig[] argPropertyConfigs) {
		this.entityConfig = entityConfig;
		this.clazz = entityConfig.getEntityClass();
		this.idField = entityConfig.getIdProperty();
		this.statement = statement;
		this.argPropertyConfigs = argPropertyConfigs;
	}

	/**
	 * Return the array of field objects pulled from the data object.
	 */
	protected Object[] getFieldObjects(Object data) throws SQLException {
		Object[] objects = new Object[argPropertyConfigs.length];
		for (int i = 0; i < argPropertyConfigs.length; i++) {
			PropertyConfig propertyConfig = argPropertyConfigs[i];
			if (propertyConfig.isAllowGeneratedIdInsert()) {
				objects[i] = propertyConfig.getFieldValueIfNotDefault(data);
			} else {
				objects[i] = propertyConfig.extractJavaFieldToSqlArgValue(data);
			}
			if (objects[i] == null && propertyConfig.getDefaultValue() != null) {
				objects[i] = propertyConfig.getDefaultValue();
			}
		}
		return objects;
	}

	/**
	 * Return a field object converted from an id.
	 */
	protected Object convertIdToFieldObject(ID id) throws SQLException {
		return idField.convertJavaFieldToSqlArgValue(id);
	}

	static void appendWhereFieldEq(DatabaseType databaseType, PropertyConfig propertyConfig, StringBuilder sb,
			List<PropertyConfig> propertyConfigList) {
		sb.append("WHERE ");
		appendFieldColumnName(databaseType, sb, propertyConfig, propertyConfigList);
		sb.append("= ?");
	}

	static void appendTableName(DatabaseType databaseType, StringBuilder sb, String prefix, String tableName) {
		if (prefix != null) {
			sb.append(prefix);
		}
		databaseType.appendEscapedEntityName(sb, tableName);
		sb.append(' ');
	}

	static void appendFieldColumnName(DatabaseType databaseType, StringBuilder sb, PropertyConfig propertyConfig,
			List<PropertyConfig> propertyConfigList) {
		databaseType.appendEscapedEntityName(sb, propertyConfig.getColumnName());
		if (propertyConfigList != null) {
			propertyConfigList.add(propertyConfig);
		}
		sb.append(' ');
	}

	@Override
	public String toString() {
		return "MappedStatement: " + statement;
	}
}
