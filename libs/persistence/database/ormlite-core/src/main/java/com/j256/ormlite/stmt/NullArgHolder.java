package com.j256.ormlite.stmt;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;

/**
 * An argument to a select SQL statement for null arguments. This overrides the protections around multiple columns
 * since it will always have a null value.
 * 
 * @author graywatson
 */
public class NullArgHolder implements ArgumentHolder {

	public NullArgHolder() {
		// typical that matches all columns/types
	}

	public String getColumnName() {
		return "null-holder";
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException("Cannot set null on " + getClass());
	}

	public void setMetaInfo(String columnName) {
		// noop
	}

	public void setMetaInfo(PropertyConfig propertyConfig) {
		// noop
	}

	public void setMetaInfo(String columnName, PropertyConfig propertyConfig) {
		// noop
	}

	public Object getSqlArgValue() {
		return null;
	}

	public SqlType getSqlType() {
		// we use this as our default because it should work with all SQL engines
		return SqlType.STRING;
	}

	public PropertyConfig getPropertyConfig() {
		return null;
	}
}
