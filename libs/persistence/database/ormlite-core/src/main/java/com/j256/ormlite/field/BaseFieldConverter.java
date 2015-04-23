package com.j256.ormlite.field;

import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

/**
 * Base class for field-converters.
 * 
 * @author graywatson
 */
public abstract class BaseFieldConverter implements FieldConverter {

	/**
	 * @throws SQLException
	 *             If there are problems with the conversion.
	 */
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object javaObject) throws SQLException {
		// noop pass-thru
		return javaObject;
	}

	public Object resultToJava(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		Object value = resultToSqlArg(propertyConfig, results, columnPos);
		if (value == null) {
			return null;
		} else {
			return sqlArgToJava(propertyConfig, value, columnPos);
		}
	}

	/**
	 * @throws SQLException
	 *             If there are problems with the conversion.
	 */
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) throws SQLException {
		// noop pass-thru
		return sqlArg;
	}

	public boolean isStreamType() {
		return false;
	}
}
