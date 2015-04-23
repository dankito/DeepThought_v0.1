package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Type that persists a {@link java.util.Date} object as a String.
 * 
 * @author graywatson
 */
public class DateStringType extends BaseDateType {

	public static int DEFAULT_WIDTH = 50;

	private static final DateStringType singleTon = new DateStringType();

	public static DateStringType getSingleton() {
		return singleTon;
	}

	private DateStringType() {
		super(SqlType.STRING, new Class<?>[0]);
	}

	/**
	 * Here for others to subclass.
	 */
	protected DateStringType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) throws SQLException {
		DateStringFormatConfig formatConfig = convertDateStringConfig(propertyConfig, defaultDateFormatConfig);
		try {
			// we parse to make sure it works and then format it again
			return normalizeDateString(formatConfig, defaultStr);
		} catch (ParseException e) {
			throw SqlExceptionUtil.create("Problems with field " + propertyConfig + " parsing default date-string '"
					+ defaultStr + "' using '" + formatConfig + "'", e);
		}
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) throws SQLException {
		String value = (String) sqlArg;
		DateStringFormatConfig formatConfig = convertDateStringConfig(propertyConfig, defaultDateFormatConfig);
		try {
			return parseDateString(formatConfig, value);
		} catch (ParseException e) {
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing date-string '" + value
					+ "' using '" + formatConfig + "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object obj) {
		DateFormat dateFormat = convertDateStringConfig(propertyConfig, defaultDateFormatConfig).getDateFormat();
		return dateFormat.format((Date) obj);
	}

	@Override
	public Object makeConfigObject(PropertyConfig propertyConfig) {
		String format = propertyConfig.getFormat();
		if (format == null) {
			return defaultDateFormatConfig;
		} else {
			return new DateStringFormatConfig(format);
		}
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	public Object resultStringToJava(PropertyConfig propertyConfig, String stringValue, int columnPos) throws SQLException {
		return sqlArgToJava(propertyConfig, stringValue, columnPos);
	}

	@Override
	public Class<?> getPrimaryClass() {
		return byte[].class;
	}
}
