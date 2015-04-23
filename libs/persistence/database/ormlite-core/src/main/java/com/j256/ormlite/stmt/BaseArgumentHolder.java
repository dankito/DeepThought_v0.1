package com.j256.ormlite.stmt;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;

import java.sql.SQLException;

/**
 * Base class for other select argument classes.
 * 
 * @author graywatson
 */
public abstract class BaseArgumentHolder implements ArgumentHolder {

	private String columnName = null;
	private PropertyConfig propertyConfig = null;
	private SqlType sqlType = null;

	public BaseArgumentHolder() {
		// no args
	}

	public BaseArgumentHolder(String columName) {
		this.columnName = columName;
	}

	public BaseArgumentHolder(SqlType sqlType) {
		this.sqlType = sqlType;
	}

	/**
	 * Return the stored value.
	 */
	protected abstract Object getValue();

	public abstract void setValue(Object value);

	/**
	 * Return true if the value is set.
	 */
	protected abstract boolean isValueSet();

	public String getColumnName() {
		return columnName;
	}

	public void setMetaInfo(String columnName) {
		if (this.columnName == null) {
			// not set yet
		} else if (this.columnName.equals(columnName)) {
			// set to the same value as before
		} else {
			throw new IllegalArgumentException("Column name cannot be set twice from " + this.columnName + " to "
					+ columnName + ".  Using a SelectArg twice in query with different columns?");
		}
		this.columnName = columnName;
	}

	public void setMetaInfo(PropertyConfig propertyConfig) {
		if (this.propertyConfig == null) {
			// not set yet
		} else if (this.propertyConfig == propertyConfig) {
			// set to the same value as before
		} else {
			throw new IllegalArgumentException("FieldType name cannot be set twice from " + this.propertyConfig + " to "
					+ propertyConfig + ".  Using a SelectArg twice in query with different columns?");
		}
		this.propertyConfig = propertyConfig;
	}

	public void setMetaInfo(String columnName, PropertyConfig propertyConfig) {
		setMetaInfo(columnName);
		setMetaInfo(propertyConfig);
	}

	public Object getSqlArgValue() throws SQLException {
		if (!isValueSet()) {
			throw new SQLException("Column value has not been set for " + columnName);
		}
		Object value = getValue();
		if (value == null) {
			return null;
		} else if (propertyConfig == null) {
			return value;
		} else if (propertyConfig.isForeign() && propertyConfig.getType() == value.getClass()) {
			PropertyConfig idPropertyConfig = propertyConfig.getForeignIdField();
			return idPropertyConfig.extractJavaFieldValue(value);
		} else {
			return propertyConfig.convertJavaFieldToSqlArgValue(value);
		}
	}

	public PropertyConfig getPropertyConfig() {
		return propertyConfig;
	}

	public SqlType getSqlType() {
		return sqlType;
	}

	@Override
	public String toString() {
		if (!isValueSet()) {
			return "[unset]";
		}
		Object val;
		try {
			val = getSqlArgValue();
			if (val == null) {
				return "[null]";
			} else {
				return val.toString();
			}
		} catch (SQLException e) {
			return "[could not get value: " + e + "]";
		}
	}
}
