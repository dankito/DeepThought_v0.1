package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.util.Date;

/**
 * Persists the {@link java.util.Date} Java class as long milliseconds since epoch.
 * 
 * <p>
 * NOTE: This is <i>not</i> the same as the {@link java.sql.Date} class.
 * </p>
 * 
 * @author graywatson
 */
public class DateLongType extends BaseDateType {

	private static final DateLongType singleTon = new DateLongType();

	public static DateLongType getSingleton() {
		return singleTon;
	}

	private DateLongType() {
		super(SqlType.LONG, new Class<?>[0]);
	}

	/**
	 * Here for others to subclass.
	 */
	protected DateLongType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) throws SQLException {
		try {
			return Long.parseLong(defaultStr);
		} catch (NumberFormatException e) {
			throw SqlExceptionUtil.create("Problems with field " + propertyConfig + " parsing default date-long value: "
					+ defaultStr, e);
		}
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		return results.getLong(columnPos);
	}

	@Override
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) {
		return new Date((Long) sqlArg);
	}

	@Override
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object obj) {
		Date date = (Date) obj;
		return (Long) date.getTime();
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}

	@Override
	public Object resultStringToJava(PropertyConfig propertyConfig, String stringValue, int columnPos) {
		return sqlArgToJava(propertyConfig, Long.parseLong(stringValue), columnPos);
	}

	@Override
	public Class<?> getPrimaryClass() {
		return Date.class;
	}
}
