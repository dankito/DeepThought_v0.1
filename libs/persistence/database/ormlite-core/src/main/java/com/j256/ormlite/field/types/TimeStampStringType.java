package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Type that persists a {@link java.sql.Timestamp} object as a String.
 * 
 * @author graywatson
 */
public class TimeStampStringType extends DateStringType {

	private static final TimeStampStringType singleTon = new TimeStampStringType();

	public static TimeStampStringType getSingleton() {
		return singleTon;
	}

	private TimeStampStringType() {
		super(SqlType.STRING, new Class<?>[0]);
	}

	/**
	 * Here for others to subclass.
	 */
	protected TimeStampStringType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) throws SQLException {
		Date date = (Date) super.sqlArgToJava(propertyConfig, sqlArg, columnPos);
		return new Timestamp(date.getTime());
	}

	@Override
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object javaObject) {
		Timestamp timeStamp = (Timestamp) javaObject;
		return super.javaToSqlArg(propertyConfig, new Date(timeStamp.getTime()));
	}

	@Override
	public boolean isValidForField(Field field) {
		return (field.getType() == java.sql.Timestamp.class);
	}

	@Override
	public Object moveToNextValue(Object currentValue) {
		long newVal = System.currentTimeMillis();
		if (currentValue == null) {
			return new java.sql.Timestamp(newVal);
		} else if (newVal == ((java.sql.Timestamp) currentValue).getTime()) {
			return new java.sql.Timestamp(newVal + 1L);
		} else {
			return new java.sql.Timestamp(newVal);
		}
	}
}