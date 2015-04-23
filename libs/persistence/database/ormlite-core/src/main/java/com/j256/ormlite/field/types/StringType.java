package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

/**
 * Type that persists a String object.
 * 
 * @author graywatson
 */
public class StringType extends BaseDataType {

	public static int DEFAULT_WIDTH = 255;

	private static final StringType singleTon = new StringType();

	public static StringType getSingleton() {
		return singleTon;
	}

	private StringType() {
		super(SqlType.STRING, new Class<?>[] { String.class });
	}

	protected StringType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) {
		return defaultStr;
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}
}
