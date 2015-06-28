package com.j256.ormlite.support;

import java.sql.SQLException;

/**
 * Defines a class that creates connectionSource proxies. This can be set on the {@code JdbcConnectionSource} or
 * {@code  AndroidConnectionSource} using the {@code setDatabaseConnectionProxyFactory(...)} static method on each class.
 * 
 * <p>
 * Typically you create a subclass of {@link DatabaseConnectionProxy} and override the methods that you want to wrap for
 * logging, monitoring, or other reason. Something like:
 * </p>
 * 
 * <p>
 * 
 * <pre>
 * private static class MyConnectionProxy extends DatabaseConnectionProxy {
 * 	public ConnectionProxy(DatabaseConnection conn) {
 * 		super(conn);
 * 	}
 * 	&#064;Override
 * 	public int insert(String statement, Object[] args, FieldType[] argfieldTypes, GeneratedKeyHolder keyHolder)
 * 			throws SQLException {
 * 		// do something here to the arguments or write to a log or something
 * 		return super.insert(statement, args, argfieldTypes, keyHolder);
 * 	}
 * }
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Then define your own factory which constructs instances of your proxy object. For example:
 * </p>
 * 
 * <p>
 * 
 * <pre>
 * JdbcConnectionSource.setDatabaseConnectionProxyFactory(new DatabaseConnectionProxyFactory() {
 * 	public DatabaseConnection createProxy(DatabaseConnection realConnection) {
 * 		return new MyConnectionProxy(realConnection);
 * 	}
 * });
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * You can also use the {@link ReflectionDatabaseConnectionProxyFactory} which takes a class and constructs your proxy
 * subclass using reflection.
 * </p>
 * 
 * <p>
 * To see a working example of the connectionSource proxy, see the {@link DatabaseConnectionProxyFactoryTest}.
 * </p>
 * 
 * @author graywatson
 */
public interface DatabaseConnectionProxyFactory {

	/**
	 * Create a proxy database connectionSource that may extend {@link DatabaseConnectionProxy}. This method should
	 * instantiate the proxy and set the real-connectionSource on it.
	 */
	public DatabaseConnection createProxy(DatabaseConnection realConnection) throws SQLException;
}