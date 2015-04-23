package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Type that persists a {@link BigInteger} object as a NUMERIC SQL database field.
 * 
 * @author graywatson
 */
public class BigDecimalNumericType extends BaseDataType {

	private static final BigDecimalNumericType singleTon = new BigDecimalNumericType();

	public static BigDecimalNumericType getSingleton() {
		return singleTon;
	}

	private BigDecimalNumericType() {
		// this has no classes because {@link BigDecimalString} is the default
		super(SqlType.BIG_DECIMAL, new Class<?>[0]);
	}

	/**
	 * Here for others to subclass.
	 */
	protected BigDecimalNumericType(SqlType sqlType, Class<?>[] classes) {
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
		return results.getBigDecimal(columnPos);
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}

	@Override
	public Class<?> getPrimaryClass() {
		return BigDecimal.class;
	}
}
