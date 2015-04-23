package com.j256.ormlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.h2.H2ConnectionSource;
import com.j256.ormlite.h2.H2DatabaseType;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import org.junit.After;
import org.junit.Before;

import java.sql.SQLException;

public abstract class BaseCoreTest {

	protected DatabaseType databaseType;
	protected WrappedConnectionSource connectionSource;

	@Before
	public void before() throws Exception {
    Instances.getDaoManager().clearCache(); // cda
		connectionSource = createConnection();
		databaseType = connectionSource.getDatabaseType();
	}

  protected static WrappedConnectionSource createConnection() throws SQLException {
    return new WrappedConnectionSource(new H2ConnectionSource());
  }

  @After
	public void after() throws Exception {
		connectionSource.close();
		connectionSource = null;
    Instances.getDaoManager().clearCache(); // cda
	}

	protected static class LimitAfterSelectDatabaseType extends H2DatabaseType {
		public LimitAfterSelectDatabaseType() throws SQLException {
			super();
		}
		@Override
		public boolean isLimitAfterSelect() {
			return true;
		}
	}

	protected static class Foo {
		public static final String ID_COLUMN_NAME = "id";
		public static final String VAL_COLUMN_NAME = "val";
		public static final String EQUAL_COLUMN_NAME = "equal";
		public static final String STRING_COLUMN_NAME = "string";
		@DatabaseField(generatedId = true, columnName = ID_COLUMN_NAME)
		public int id;
		@DatabaseField(columnName = VAL_COLUMN_NAME)
		public int val;
		@DatabaseField(columnName = EQUAL_COLUMN_NAME)
		public int equal;
		@DatabaseField(columnName = STRING_COLUMN_NAME)
		public String stringField;
		public Foo() {
		}
		@Override
		public String toString() {
			return "Foo:" + id;
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id == ((Foo) other).id;
		}
		@Override
		public int hashCode() {
			return id;
		}
	}

	protected static class Foreign {
		public static final String FOO_COLUMN_NAME = "foo_id";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, columnName = FOO_COLUMN_NAME)
		public Foo foo;
		public Foreign() {
		}
	}

	protected <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		@SuppressWarnings("unchecked")
		Dao<T, ID> dao = Instances.getDaoManager().createDao(connectionSource, clazz); // cda: don't implement against implementation but interface
		return configDao(dao, createTable);
	}

  protected <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable, boolean cacheObjects) throws Exception {
    Dao<T, ID> dao = createDao(clazz, createTable);
    dao.setObjectCache(cacheObjects);
    return dao;
  }

	protected <T, ID> Dao<T, ID> createDao(DatabaseTableConfig<T> tableConfig, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		@SuppressWarnings("unchecked")
		Dao<T, ID> dao = Instances.getDaoManager().createDao(connectionSource, tableConfig); // cda: don't implement against implementation but interface
		return configDao(dao, createTable);
	}

	protected <T> void createTable(Class<T> clazz, boolean dropAtEnd) throws Exception {
		createTable(DatabaseTableConfig.fromClass(connectionSource, clazz), dropAtEnd);
	}

	protected <T> void createTable(DatabaseTableConfig<T> tableConfig, boolean dropAtEnd) throws Exception {
		try {
			// first we drop it in case it existed before
			dropTable(tableConfig, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(connectionSource, tableConfig);
	}

	protected <T> void dropTable(Class<T> clazz, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, clazz, ignoreErrors);
	}

	protected <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, tableConfig, ignoreErrors);
	}

	private <T, ID> Dao<T, ID> configDao(Dao<T, ID> dao, boolean createTable) throws Exception { // cda: don't implement against implementation but interface
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		if (createTable) {
			DatabaseTableConfig<T> tableConfig = dao.getTableConfig();
			if (tableConfig == null) {
				tableConfig = DatabaseTableConfig.fromClass(connectionSource, dao.getDataClass());
			}
			createTable(tableConfig, true);
		}
		return dao;
	}
}
