package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Type that persists a {@link BigInteger} object.
 * 
 * @author graywatson
 */
public class BigDecimalStringType extends BaseDataType {

	public static int DEFAULT_WIDTH = 255;

	private static final BigDecimalStringType singleTon = new BigDecimalStringType();

	public static BigDecimalStringType getSingleton() {
		return singleTon;
	}

	private BigDecimalStringType() {
		super(SqlType.STRING, new Class<?>[] { BigDecimal.class });
	}

	/**
	 * Here for others to subclass.
	 */
	protected BigDecimalStringType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) throws SQLException {
		try {
			return new BigDecimal(defaultStr);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with field " + propertyConfig + " parsing default BigDecimal string '"
					+ defaultStr + "'", e);
		}
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) throws SQLException {
		try {
			return new BigDecimal((String) sqlArg);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing BigDecimal string '" + sqlArg
					+ "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object obj) {
		BigDecimal bigInteger = (BigDecimal) obj;
		return bigInteger.toString();
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}
}
