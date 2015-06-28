package com.j256.ormlite.field.types;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class BaseTypeTest extends BaseCoreTest {

	protected static final String TABLE_NAME = "foo";
	protected static final PropertyConfig[] NO_PROPERTY_CONFIGs = new PropertyConfig[0];

	protected <T, ID> void testType(Dao<T, ID> dao, T foo, Class<T> clazz, Object javaVal, Object defaultSqlVal,
			Object sqlArg, String defaultValStr, DataType dataType, String columnName, boolean isValidGeneratedType,
			boolean isAppropriateId, boolean isEscapedValue, boolean isPrimitive, boolean isSelectArgRequired,
			boolean isStreamType, boolean isComparable, boolean isConvertableId) throws Exception {
		DataPersister dataPersister = dataType.getDataPersister();
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt =
					conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, NO_PROPERTY_CONFIGs,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			int colNum = results.findColumn(columnName);
			Field field = clazz.getDeclaredField(columnName);
			PropertyConfig propertyConfig = Instances.getFieldTypeCreator().createFieldType(connectionSource, TABLE_NAME, field, clazz);
			Class<?>[] classes = propertyConfig.getDataPersister().getAssociatedClasses();
			if (classes.length > 0) {
				assertTrue(classes[0].isAssignableFrom(propertyConfig.getType()));
			}
			assertTrue(propertyConfig.getDataPersister().isValidForField(field));
			if (javaVal instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) javaVal,
						(byte[]) dataPersister.resultToJava(propertyConfig, results, colNum)));
			} else {
				Map<String, Integer> colMap = new HashMap<String, Integer>();
				colMap.put(columnName, colNum);
				Object result = propertyConfig.resultToJava(results, colMap);
				assertEquals(javaVal, result);
			}
			if (dataType == DataType.STRING_BYTES || dataType == DataType.BYTE_ARRAY
					|| dataType == DataType.SERIALIZABLE) {
				try {
					dataPersister.parseDefaultString(propertyConfig, "");
					fail("parseDefaultString should have thrown for " + dataType);
				} catch (SQLException e) {
					// expected
				}
			} else if (defaultValStr != null) {
				assertEquals(defaultSqlVal, dataPersister.parseDefaultString(propertyConfig, defaultValStr));
			}
			if (sqlArg == null) {
				// noop
			} else if (sqlArg instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) sqlArg, (byte[]) dataPersister.javaToSqlArg(propertyConfig, javaVal)));
			} else {
				assertEquals(sqlArg, dataPersister.javaToSqlArg(propertyConfig, javaVal));
			}
			assertEquals(isValidGeneratedType, dataPersister.isValidGeneratedType());
			assertEquals(isAppropriateId, dataPersister.isAppropriateId());
			assertEquals(isEscapedValue, dataPersister.isEscapedValue());
			assertEquals(isEscapedValue, dataPersister.isEscapedDefaultValue());
			assertEquals(isPrimitive, dataPersister.isPrimitive());
			assertEquals(isSelectArgRequired, dataPersister.isArgumentHolderRequired());
			assertEquals(isStreamType, dataPersister.isStreamType());
			assertEquals(isComparable, dataPersister.isComparable());
			if (isConvertableId) {
				assertNotNull(dataPersister.convertIdNumber(10));
			} else {
				assertNull(dataPersister.convertIdNumber(10));
			}
			List<T> list = dao.queryForAll();
			assertEquals(1, list.size());
			assertTrue(dao.objectsEqual(foo, list.get(0)));
			// if we have a value then look for it, floats don't find any results because of rounding issues
			if (javaVal != null && dataPersister.isComparable() && dataType != DataType.FLOAT
					&& dataType != DataType.FLOAT_OBJ) {
				// test for inline arguments
				list = dao.queryForMatching(foo);
				assertEquals(1, list.size());
				assertTrue(dao.objectsEqual(foo, list.get(0)));
				// test for SelectArg arguments
				list = dao.queryForMatchingArgs(foo);
				assertEquals(1, list.size());
				assertTrue(dao.objectsEqual(foo, list.get(0)));
			}
			if (dataType == DataType.STRING_BYTES || dataType == DataType.BYTE_ARRAY
					|| dataType == DataType.SERIALIZABLE) {
				// no converting from string to value
			} else {
				// test string conversion
				String stringVal = results.getString(colNum);
				Object convertedJavaVal = propertyConfig.convertStringToJavaField(stringVal, 0);
				assertEquals(javaVal, convertedJavaVal);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

}