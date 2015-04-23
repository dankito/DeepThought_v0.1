package com.j256.ormlite.table;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jpa.EntityConfig;

import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TableInfoTest extends BaseCoreTest {

	private final static String TABLE_NAME = "tablename";
	private final static String COLUMN_NAME = "column2";

	@Test(expected = IllegalArgumentException.class)
	public void testTableInfo() throws SQLException {
		new EntityConfig<NoFieldAnnotations, Void>(connectionSource, null, NoFieldAnnotations.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoNoArgConstructor() throws SQLException {
		new EntityConfig<NoNoArgConstructor, Void>(connectionSource, null, NoNoArgConstructor.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectNoFields() throws SQLException {
		new EntityConfig<NoFields, Void>(connectionSource, null, NoFields.class);
	}

	@Test(expected = SQLException.class)
	public void testObjectDoubleId() throws SQLException {
		new EntityConfig<DoubleId, String>(connectionSource, null, DoubleId.class);
	}

	@Test
	public void testBasic() throws SQLException {
		EntityConfig<Foo, String> entityConfig = new EntityConfig<Foo, String>(connectionSource, null, Foo.class);
		assertEquals(Foo.class, entityConfig.getEntityClass());
		assertEquals(TABLE_NAME, entityConfig.getTableName());
		assertEquals(COLUMN_NAME, entityConfig.getIdProperty().getColumnName());
		assertEquals(1, entityConfig.getPropertyConfigs().length);
		assertSame(entityConfig.getIdProperty(), entityConfig.getPropertyConfigs()[0]);
		assertEquals(COLUMN_NAME, entityConfig.getFieldTypeByColumnName(COLUMN_NAME).getColumnName());
	}

	@Test
	public void testObjectToString() throws Exception {
		String id = "f11232oo";
		Foo foo = new Foo();
		foo.id = id;
		assertEquals(id, foo.id);
		EntityConfig<Foo, String> entityConfig = new EntityConfig<Foo, String>(connectionSource, null, Foo.class);
		assertTrue(entityConfig.objectToString(foo).contains(id));
	}

	@Test
	public void testNoTableNameInAnnotation() throws Exception {
		EntityConfig<NoTableNameAnnotation, Void> entityConfig =
				new EntityConfig<NoTableNameAnnotation, Void>(connectionSource, null, NoTableNameAnnotation.class);
		assertEquals(NoTableNameAnnotation.class.getSimpleName().toLowerCase(), entityConfig.getTableName());
	}

	@Test(expected = SQLException.class)
	public void testZeroFieldConfigsSpecified() throws Exception {
		DatabaseTableConfig<NoTableNameAnnotation> tableConfig =
				new DatabaseTableConfig<NoTableNameAnnotation>(NoTableNameAnnotation.class,
						new ArrayList<DatabaseFieldConfig>());
		tableConfig.extractFieldTypes(connectionSource);
		new EntityConfig<NoTableNameAnnotation, Void>(databaseType, null, tableConfig);
	}

	@Test
	public void testConstruct() throws Exception {
		EntityConfig<Foo, String> entityConfig = new EntityConfig<Foo, String>(connectionSource, null, Foo.class);
		Foo foo = entityConfig.createObject();
		assertNotNull(foo);
	}

	@Test
	public void testUnknownForeignField() throws Exception {
		EntityConfig<Foreign, Void> entityConfig = new EntityConfig<Foreign, Void>(connectionSource, null, Foreign.class);
		try {
			entityConfig.getFieldTypeByColumnName("foo");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("'" + Foreign.FOREIGN_FIELD_NAME + "'"));
			assertTrue(e.getMessage().contains("'foo'"));
		}
	}

	/**
	 * Test to make sure that we can call a private constructor
	 */
	@Test
	public void testPrivateConstructor() throws Exception {
		Dao<PrivateConstructor, Object> packConstDao = createDao(PrivateConstructor.class, true);
		int id = 12312321;
		PrivateConstructor pack1 = PrivateConstructor.makeOne(id);
		assertEquals(id, pack1.id);
		packConstDao.create(pack1);
		// we should be able to look it up
		PrivateConstructor pack2 = packConstDao.queryForId(id);
		// and the id should match
		assertEquals(id, pack2.id);
	}

	@Test
	public void testHasColumnName() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		EntityConfig<Foo, String> entityConfig = ((BaseDaoImpl<Foo, String>) dao).getEntityConfig();
		assertTrue(entityConfig.hasColumnName(COLUMN_NAME));
		assertFalse(entityConfig.hasColumnName("not this name"));
	}

	/* ================================================================================================================ */

	protected static class NoFieldAnnotations {
		String id;
	}

	private static class NoFields {
	}

	protected static class NoNoArgConstructor {
		public NoNoArgConstructor(String arg) {
		}
	}

	protected static class DoubleId {
		@DatabaseField(id = true)
		String id1;
		@DatabaseField(id = true)
		String id2;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	private static class Foo {
		@DatabaseField(id = true, columnName = COLUMN_NAME)
		// private to test access levels
		private String id;
	}

	@DatabaseTable
	protected static class NoTableNameAnnotation {
		@DatabaseField
		String id;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class Foreign {
		public static final String FOREIGN_FIELD_NAME = "fooblah";
		@DatabaseField(foreign = true, columnName = FOREIGN_FIELD_NAME)
		public Foo foo;
		public Foreign() {
		}
	}

	private static class PrivateConstructor {
		@DatabaseField(id = true)
		int id;
		private PrivateConstructor() {
			// make it private
		}
		public static PrivateConstructor makeOne(int id) {
			PrivateConstructor pack = new PrivateConstructor();
			pack.id = id;
			return pack;
		}
	}
}
