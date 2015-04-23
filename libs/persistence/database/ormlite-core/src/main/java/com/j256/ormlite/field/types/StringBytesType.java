package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

/**
 * Type that persists a String as a byte array.
 * 
 * @author graywatson
 */
public class StringBytesType extends BaseDataType {

	private static final String DEFAULT_STRING_BYTES_CHARSET_NAME = "Unicode";

	private static final StringBytesType singleTon = new StringBytesType();

	public static StringBytesType getSingleton() {
		return singleTon;
	}

	private StringBytesType() {
		super(SqlType.BYTE_ARRAY, new Class<?>[0]);
	}

	/**
	 * Here for others to subclass.
	 */
	protected StringBytesType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) throws SQLException {
		throw new SQLException("String-bytes type cannot have default values");
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		return results.getBytes(columnPos);
	}

	@Override
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) throws SQLException {
		byte[] bytes = (byte[]) sqlArg;
		String charsetName = getCharsetName(propertyConfig);
		try {
			// NOTE: I can't use new String(bytes, Charset) because it was introduced in 1.6.
			return new String(bytes, charsetName);
		} catch (UnsupportedEncodingException e) {
			throw SqlExceptionUtil.create("Could not convert string with charset name: " + charsetName, e);
		}
	}

	@Override
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object javaObject) throws SQLException {
		String string = (String) javaObject;
		String charsetName = getCharsetName(propertyConfig);
		try {
			// NOTE: I can't use string.getBytes(Charset) because it was introduced in 1.6.
			return string.getBytes(charsetName);
		} catch (UnsupportedEncodingException e) {
			throw SqlExceptionUtil.create("Could not convert string with charset name: " + charsetName, e);
		}
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public boolean isArgumentHolderRequired() {
		return true;
	}

	@Override
	public Object resultStringToJava(PropertyConfig propertyConfig, String stringValue, int columnPos) throws SQLException {
		throw new SQLException("String-bytes type cannot be converted from string to Java");
	}

	@Override
	public Class<?> getPrimaryClass() {
		return String.class;
	}

	private String getCharsetName(PropertyConfig propertyConfig) {
		if (propertyConfig == null || propertyConfig.getFormat() == null) {
			return DEFAULT_STRING_BYTES_CHARSET_NAME;
		} else {
			return propertyConfig.getFormat();
		}
	}
}
