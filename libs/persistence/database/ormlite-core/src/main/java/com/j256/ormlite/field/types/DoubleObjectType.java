package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

/**
 * Type that persists a Double object.
 * 
 * @author graywatson
 */
public class DoubleObjectType extends BaseDataType {

	private static final DoubleObjectType singleTon = new DoubleObjectType();

	public static DoubleObjectType getSingleton() {
		return singleTon;
	}

	private DoubleObjectType() {
		super(SqlType.DOUBLE, new Class<?>[] { Double.class });
	}

	protected DoubleObjectType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) {
		return Double.parseDouble(defaultStr);
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		return (Double) results.getDouble(columnPos);
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}
}
