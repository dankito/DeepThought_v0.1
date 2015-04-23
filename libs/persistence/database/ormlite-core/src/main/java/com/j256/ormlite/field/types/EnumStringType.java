package com.j256.ormlite.field.types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Type that persists an enum as its string value. You can also use the {@link EnumIntegerType}.
 * 
 * @author graywatson
 */
public class EnumStringType extends BaseEnumType {

	public static int DEFAULT_WIDTH = 100;

	private static final EnumStringType singleTon = new EnumStringType();

	public static EnumStringType getSingleton() {
		return singleTon;
	}

	private EnumStringType() {
		super(SqlType.STRING, new Class<?>[] { Enum.class });
	}

	/**
	 * Here for others to subclass.
	 */
	protected EnumStringType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) throws SQLException {
		if (propertyConfig == null) {
			return sqlArg;
		}
		String value = (String) sqlArg;
		@SuppressWarnings("unchecked")
		Map<String, Enum<?>> enumStringMap = (Map<String, Enum<?>>) propertyConfig.getDataTypeConfigObj();
		if (enumStringMap == null) {
			return enumVal(propertyConfig, value, null, propertyConfig.getUnknownEnumVal());
		} else {
			return enumVal(propertyConfig, value, enumStringMap.get(value), propertyConfig.getUnknownEnumVal());
		}
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) {
		return defaultStr;
	}

	@Override
	public Object javaToSqlArg(PropertyConfig propertyConfig, Object obj) {
		Enum<?> enumVal = (Enum<?>) obj;
		return enumVal.name();
	}

	@Override
	public Object makeConfigObject(PropertyConfig propertyConfig) throws SQLException {
		Map<String, Enum<?>> enumStringMap = new HashMap<String, Enum<?>>();
		Enum<?>[] constants = (Enum<?>[]) propertyConfig.getType().getEnumConstants();
		if (constants == null) {
			throw new SQLException("Field " + propertyConfig + " improperly configured as type " + this);
		}
		for (Enum<?> enumVal : constants) {
			enumStringMap.put(enumVal.name(), enumVal);
		}
		return enumStringMap;
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	public Object resultStringToJava(PropertyConfig propertyConfig, String stringValue, int columnPos) throws SQLException {
		return sqlArgToJava(propertyConfig, stringValue, columnPos);
	}
}
