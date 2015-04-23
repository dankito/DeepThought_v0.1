package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Persists an Enum Java class as its ordinal integer value. You can also specify the {@link EnumStringType} as the
 * type.
 * 
 * @author graywatson
 */
public class EnumIntegerType extends BaseEnumType {

	private static final EnumIntegerType singleTon = new EnumIntegerType();

	public static EnumIntegerType getSingleton() {
		return singleTon;
	}

	private EnumIntegerType() {
		super(SqlType.INTEGER, new Class<?>[0]);
	}

	/**
	 * Here for others to subclass.
	 */
	protected EnumIntegerType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) {
		return Integer.parseInt(defaultStr);
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		return results.getInt(columnPos);
	}

	@Override
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) throws SQLException {
		if (propertyConfig == null) {
			return sqlArg;
		}
		// do this once
		Integer valInteger = (Integer) sqlArg;
		@SuppressWarnings("unchecked")
		Map<Integer, Enum<?>> enumIntMap = (Map<Integer, Enum<?>>) propertyConfig.getDataTypeConfigObj();
		if (enumIntMap == null) {
			return enumVal(propertyConfig, valInteger, null, propertyConfig.getUnknownEnumVal());
		} else {
			return enumVal(propertyConfig, valInteger, enumIntMap.get(valInteger), propertyConfig.getUnknownEnumVal());
		}
	}

	@Override
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object obj) {
		Enum<?> enumVal = (Enum<?>) obj;
		return (Integer) enumVal.ordinal();
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}

	@Override
	public Object makeConfigObject(PropertyConfig propertyConfig) throws SQLException {
		Map<Integer, Enum<?>> enumIntMap = new HashMap<Integer, Enum<?>>();
		Enum<?>[] constants = (Enum<?>[]) propertyConfig.getType().getEnumConstants();
		if (constants == null) {
			throw new SQLException("Field " + propertyConfig + " improperly configured as type " + this);
		}
		for (Enum<?> enumVal : constants) {
			enumIntMap.put(enumVal.ordinal(), enumVal);
		}
		return enumIntMap;
	}

	@Override
	public Object resultStringToJava(PropertyConfig propertyConfig, String stringValue, int columnPos) throws SQLException {
		return sqlArgToJava(propertyConfig, Integer.parseInt(stringValue), columnPos);
	}

	@Override
	public Class<?> getPrimaryClass() {
		return int.class;
	}
}
