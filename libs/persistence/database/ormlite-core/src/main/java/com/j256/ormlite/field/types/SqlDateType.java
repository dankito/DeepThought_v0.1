package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;

import java.lang.reflect.Field;
import java.sql.Timestamp;

/**
 * Type that persists a {@link java.sql.Date} object.
 * 
 * <p>
 * NOTE: This is <i>not</i> the same as the {@link java.util.Date} class handled with {@link DateType}. If it
 * recommended that you use the other Date class which is more standard to Java programs.
 * </p>
 * 
 * @author graywatson
 */
public class SqlDateType extends DateType {

	private static final SqlDateType singleTon = new SqlDateType();
	private static final DateStringFormatConfig sqlDateFormatConfig = new DateStringFormatConfig("yyyy-MM-dd");

	public static SqlDateType getSingleton() {
		return singleTon;
	}

	private SqlDateType() {
		super(SqlType.DATE, new Class<?>[] { java.sql.Date.class });
	}

	/**
	 * Here for others to subclass.
	 */
	protected SqlDateType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) {
		Timestamp value = (Timestamp) sqlArg;
		return new java.sql.Date(value.getTime());
	}

	@Override
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object javaObject) {
		java.sql.Date date = (java.sql.Date) javaObject;
		return new Timestamp(date.getTime());
	}

	@Override
	protected DateStringFormatConfig getDefaultDateFormatConfig() {
		return sqlDateFormatConfig;
	}

	@Override
	public Object resultStringToJava(PropertyConfig propertyConfig, String stringValue, int columnPos) {
		return sqlArgToJava(propertyConfig, Timestamp.valueOf(stringValue), columnPos);
	}

	@Override
	public boolean isValidForField(Field field) {
		return (field.getType() == java.sql.Date.class);
	}
}
