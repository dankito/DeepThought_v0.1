package com.j256.ormlite.support;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.h2.H2ConnectionSource;
import com.j256.ormlite.jpa.PropertyConfig;

import org.junit.AfterClass;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This test class is also a demonstration of how the {@link DatabaseConnectionProxyFactory} and
 * {@link DatabaseConnectionProxy} system works.
 * 
 * @author graywatson
 */
public class DatabaseConnectionProxyFactoryTest extends BaseCoreTest {

	private static final int TEST_CHANGE_FROM = 131231;
	private static final int TEST_CHANGE_TO = TEST_CHANGE_FROM + 1;

	static {
		/*
		 * First we set our connectionSource proxy using a static initializer. Here we use an anonymous class which constructs
		 * instances of our connectionSource proxy.
		 */
		H2ConnectionSource.setDatabaseConnectionProxyFactory(new DatabaseConnectionProxyFactory() {
			public DatabaseConnection createProxy(DatabaseConnection realConnection) {
				return new ConnectionProxy(realConnection);
			}
		});
	}

	/**
	 * For testing purposes we have to un-wire our proxy after the tests run.
	 */
	@AfterClass
	public static void afterClass() {
		H2ConnectionSource.setDatabaseConnectionProxyFactory(null);
	}

	/**
	 * Here we create an instance of {@link Foo} and then look to see if the connectionSource proxy was able to store the
	 * 'val' field value.
	 */
	@Test
	public void testBasic() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 100;

		ConnectionProxy.lastValue = 0;
		assertEquals(1, dao.create(foo));
		/*
		 * After we create an instance of foo, we check to see that our proxy was able to intercept the val argument.
		 */
		assertEquals(foo.val, ConnectionProxy.lastValue);
	}

	/**
	 * Here we inserting a particular val value and the connectionSource-proxy should alter it _before_ it is inserted.
	 */
	@Test
	public void testChangeInsertValue() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = TEST_CHANGE_FROM;

		ConnectionProxy.lastValue = 0;
		assertEquals(1, dao.create(foo));
		/*
		 * After we create an instance of foo, we check to see that our proxy was able to intercept the val argument.
		 */
		assertEquals(foo.val, ConnectionProxy.lastValue);

		Foo result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(TEST_CHANGE_TO, result.val);
		assertTrue(result.val != TEST_CHANGE_FROM);
	}

	/**
	 * Sample connectionSource proxy which overrides the {@link DatabaseConnectionProxy} so we can do something interesting
	 * specifically with the {@link DatabaseConnection#insert(String, Object[], com.j256.ormlite.jpa.PropertyConfig[], GeneratedKeyHolder)}
	 * method. In this case we are just recording the argument to insert which is the setting of the {@code val} field
	 * in {@link Foo}.
	 */
	private static class ConnectionProxy extends DatabaseConnectionProxy {
		static int lastValue;
		public ConnectionProxy(DatabaseConnection conn) {
			super(conn);
		}
		@Override
		public int insert(String statement, Object[] args, PropertyConfig[] argfieldTypes, GeneratedKeyHolder keyHolder)
				throws SQLException {
			// just record the first argument to the insert which for Foo should be the 'val' field
			lastValue = (Integer) args[0];
			if (lastValue == TEST_CHANGE_FROM) {
				args[0] = TEST_CHANGE_TO;
			}
			return super.insert(statement, args, argfieldTypes, keyHolder);
		}
	}
}
