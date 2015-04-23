package com.j256.ormlite.table;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.DiscriminatorColumnConfig;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.SingleTableEntityConfig;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.misc.TableInfoRegistry;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.InheritanceType;

/**
 * Couple utility methods for the creating, dropping, and maintenance of tables.
 * 
 * @author graywatson
 */
public class TableUtils {

	private static Logger logger = LoggerFactory.getLogger(TableUtils.class);
	private static final PropertyConfig[] NO_PROPERTY_CONFIGs = new PropertyConfig[0];

	/**
	 * For static methods only.
	 */
	private TableUtils() {
	}

	/**
	 * Issue the database statements to create the table associated with a class.
	 * 
	 * @param connectionSource
	 *            Associated connectionSource source.
	 * @param dataClass
	 *            The class for which a table will be created.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int createTable(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
		return createTable(connectionSource, dataClass, false);
	}

	/**
	 * Create a table if it does not already exist. This is not supported by all databases.
	 */
	public static <T> int createTableIfNotExists(ConnectionSource connectionSource, Class<T> dataClass)
			throws SQLException {
		return createTable(connectionSource, dataClass, true);
	}

	/**
	 * Issue the database statements to create the table associated with a table configuration.
	 * 
	 * @param connectionSource
	 *            connectionSource Associated connectionSource source.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int createTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig)
			throws SQLException {
		return createTable(connectionSource, tableConfig, false);
	}

	/**
	 * Create a table if it does not already exist. This is not supported by all databases.
	 */
	public static <T> int createTableIfNotExists(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig)
			throws SQLException {
		return createTable(connectionSource, tableConfig, true);
	}

	/**
	 * Return an ordered collection of SQL statements that need to be run to create a table. To do the work of creating,
	 * you should call {@link #createTable}.
	 * 
	 * @param connectionSource
	 *            Our connect source which is used to get the database type, not to apply the creates.
	 * @param dataClass
	 *            The class for which a table will be created.
	 * @return The collection of table create statements.
	 */
	public static <T, ID> List<String> getCreateTableStatements(ConnectionSource connectionSource, Class<T> dataClass)
			throws SQLException {
		Dao<T, ID> dao = Instances.getDaoManager().createDao(connectionSource, dataClass); // Instances.getDaoManager()
    dao.setObjectCache(true); // TODO: don't know how to solve it otherwise right now - implement global boolean cacheObjects flag?
		if (dao != null) {// cda
			return addCreateTableStatements(connectionSource, dao.getEntityConfig(), false);
		} else {
			EntityConfig<T, ID> entityConfig = TableInfoRegistry.getInstance().getAndMayCreateTableInfoForClass(dataClass, connectionSource);
			return addCreateTableStatements(connectionSource, entityConfig, false);
		}
	}

	/**
	 * Return an ordered collection of SQL statements that need to be run to create a table. To do the work of creating,
	 * you should call {@link #createTable}.
	 * 
	 * @param connectionSource
	 *            Our connect source which is used to get the database type, not to apply the creates.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @return The collection of table create statements.
	 */
	public static <T, ID> List<String> getCreateTableStatements(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		Dao<T, ID> dao = Instances.getDaoManager().createDao(connectionSource, tableConfig); // cda
		if (dao != null) { // cda
			return addCreateTableStatements(connectionSource, dao.getEntityConfig(), false);
		} else {
			tableConfig.extractFieldTypes(connectionSource);
			EntityConfig<T, ID> entityConfig = TableInfoRegistry.getInstance().getAndMayCreateTableInfoForClass(connectionSource.getDatabaseType(), tableConfig);
			return addCreateTableStatements(connectionSource, entityConfig, false);
		}
	}

	/**
	 * Issue the database statements to drop the table associated with a class.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 * 
	 * @param connectionSource
	 *            Associated connectionSource source.
	 * @param dataClass
	 *            The class for which a table will be dropped.
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T, ID> int dropTable(ConnectionSource connectionSource, Class<T> dataClass, boolean ignoreErrors)
			throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		Dao<T, ID> dao = Instances.getDaoManager().createDao(connectionSource, dataClass); // cda
    dao.setObjectCache(true); // TODO: don't know how to solve it otherwise right now - implement global boolean cacheObjects flag?
		if (dao != null) { // cda
			return doDropTable(databaseType, connectionSource, dao.getEntityConfig(), ignoreErrors);
		} else {
			EntityConfig<T, ID> entityConfig = TableInfoRegistry.getInstance().getAndMayCreateTableInfoForClass(dataClass, connectionSource);
			return doDropTable(databaseType, connectionSource, entityConfig, ignoreErrors);
		}
	}

	/**
	 * Issue the database statements to drop the table associated with a table configuration.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 * 
	 * @param connectionSource
	 *            Associated connectionSource source.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T, ID> int dropTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig,
			boolean ignoreErrors) throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		Dao<T, ID> dao = Instances.getDaoManager().createDao(connectionSource, tableConfig); // cda
		if (dao != null) { // cda
			return doDropTable(databaseType, connectionSource, dao.getEntityConfig(), ignoreErrors);
		} else {
			tableConfig.extractFieldTypes(connectionSource);
			EntityConfig<T, ID> entityConfig = TableInfoRegistry.getInstance().getAndMayCreateTableInfoForClass(databaseType, tableConfig);
			return doDropTable(databaseType, connectionSource, entityConfig, ignoreErrors);
		}
	}

	/**
	 * Clear all data out of the table. For certain database types and with large sized tables, which may take a long
	 * time. In some configurations, it may be faster to drop and re-create the table.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 */
	public static <T> int clearTable(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
		String tableName = DatabaseTableConfig.extractTableName(dataClass);
		if (connectionSource.getDatabaseType().isEntityNamesMustBeUpCase()) {
			tableName = tableName.toUpperCase();
		}
		return clearTable(connectionSource, tableName);
	}

	/**
	 * Clear all data out of the table. For certain database types and with large sized tables, which may take a long
	 * time. In some configurations, it may be faster to drop and re-create the table.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and is unrecoverable.
	 * </p>
	 */
	public static <T> int clearTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig)
			throws SQLException {
		return clearTable(connectionSource, tableConfig.getTableName());
	}

	private static <T, ID> int createTable(ConnectionSource connectionSource, Class<T> dataClass, boolean ifNotExists)
			throws SQLException {
    Dao<T, ID> dao = Instances.getDaoManager().lookupDao(connectionSource, dataClass); // cda
    if(dao == null)
      dao = Instances.getDaoManager().createDao(connectionSource, dataClass); // cda
//    dao.setObjectCache(true); // TODO: don't know how to solve it otherwise right now - implement global boolean cacheObjects flag?

		if (dao != null) { // cda
			return doCreateTable(connectionSource, dao.getEntityConfig(), ifNotExists);
		} else {
			EntityConfig<T, ID> entityConfig = TableInfoRegistry.getInstance().getAndMayCreateTableInfoForClass(dataClass, connectionSource);
			return doCreateTable(connectionSource, entityConfig, ifNotExists);
		}
	}

	private static <T, ID> int createTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig,
			boolean ifNotExists) throws SQLException {
		Dao<T, ID> dao = Instances.getDaoManager().createDao(connectionSource, tableConfig); // cda
		if (dao != null) { // cda
			return doCreateTable(connectionSource, dao.getEntityConfig(), ifNotExists);
		} else {
			tableConfig.extractFieldTypes(connectionSource);
			EntityConfig<T, ID> entityConfig = TableInfoRegistry.getInstance().getAndMayCreateTableInfoForClass(connectionSource.getDatabaseType(), tableConfig);
			return doCreateTable(connectionSource, entityConfig, ifNotExists);
		}
	}

  // TODO: on clearTable JoinTables don't get cleared
	private static <T> int clearTable(ConnectionSource connectionSource, String tableName) throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		StringBuilder sb = new StringBuilder(48);
		if (databaseType.isTruncateSupported()) {
			sb.append("TRUNCATE TABLE ");
		} else {
			sb.append("DELETE FROM ");
		}
		databaseType.appendEscapedEntityName(sb, tableName);
		String statement = sb.toString();
		logger.info("clearing table '{}' with '{}", tableName, statement);
		CompiledStatement compiledStmt = null;
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			compiledStmt =
					connection.compileStatement(statement, StatementType.EXECUTE, NO_PROPERTY_CONFIGs,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			return compiledStmt.runExecute();
		} finally {
			IOUtils.closeThrowSqlException(compiledStmt, "compiled statement");
			connectionSource.releaseConnection(connection);
		}
	}

	private static <T, ID> int doDropTable(DatabaseType databaseType, ConnectionSource connectionSource,
			EntityConfig<T, ID> entityConfig, boolean ignoreErrors) throws SQLException {
		logger.info("dropping table '{}'", entityConfig.getTableName());
		List<String> statements = new ArrayList<String>();
		addDropIndexStatements(databaseType, entityConfig, statements);
		addDropTableStatements(databaseType, entityConfig, statements);
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			return doStatements(connection, "drop", statements, ignoreErrors,
					databaseType.isCreateTableReturnsNegative(), false);
		} finally {
      if(entityConfig.hasJoinTableProperties())
        dropJoinTables(databaseType, connectionSource, entityConfig);
			connectionSource.releaseConnection(connection);
		}
	}

  private static <T, ID> void dropJoinTables(DatabaseType databaseType, ConnectionSource connection, EntityConfig<T, ID> entityConfig) throws SQLException {
    for(PropertyConfig joinedProperty : entityConfig.getJoinTableProperties()) {
      if(joinedProperty.isOwningSide()) { // only owning sides create Join Tables
        doDropTable(databaseType, connection, joinedProperty.getJoinTable(), true);
      }
    }
  }

  private static <T, ID> void addDropIndexStatements(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			List<String> statements) {
		// run through and look for index annotations
		Set<String> indexSet = new HashSet<String>();
		for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
			String indexName = propertyConfig.getIndexName();
			if (indexName != null) {
				indexSet.add(indexName);
			}
			String uniqueIndexName = propertyConfig.getUniqueIndexName();
			if (uniqueIndexName != null) {
				indexSet.add(uniqueIndexName);
			}
		}

		StringBuilder sb = new StringBuilder(48);
		for (String indexName : indexSet) {
			logger.info("dropping index '{}' for table '{}", indexName, entityConfig.getTableName());
			sb.append("DROP INDEX ");
			databaseType.appendEscapedEntityName(sb, indexName);
			statements.add(sb.toString());
			sb.setLength(0);
		}
	}

	/**
	 * Generate and return the list of statements to create a database table and any associated features.
	 */
	private static <T, ID> void addCreateTableStatements(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			List<String> statements, List<String> queriesAfter, boolean ifNotExists) throws SQLException {
    if(entityConfig.getInheritance() != InheritanceType.JOINED)
      addCreateTableStatementsForSingleTable(databaseType, entityConfig, statements, queriesAfter, ifNotExists);
    else {
      for(EntityConfig singleTable = entityConfig; singleTable != null; singleTable = singleTable.getParentEntityConfig()) {
        addCreateTableStatementsForSingleTable(databaseType, singleTable, statements, queriesAfter, ifNotExists);
      }
    }
	}

  protected static <T, ID> void addCreateTableStatementsForSingleTable(DatabaseType databaseType, EntityConfig<T, ID> entityConfig, List<String> statements, List<String> queriesAfter, boolean ifNotExists) throws SQLException {
    StringBuilder sb = new StringBuilder(256);
    sb.append("CREATE TABLE ");

    if ((ifNotExists || entityConfig.getInheritance() == InheritanceType.SINGLE_TABLE || entityConfig.getInheritance() == InheritanceType.JOINED)
        && databaseType.isCreateIfNotExistsSupported()) {
      sb.append("IF NOT EXISTS ");
    }

    databaseType.appendEscapedEntityName(sb, entityConfig.getTableName());
    sb.append(" (");

    List<String> additionalArgs = new ArrayList<String>();
    List<String> statementsBefore = new ArrayList<String>();
    List<String> statementsAfter = new ArrayList<String>();

    PropertyConfig[] propertyConfigs = entityConfig.getPropertyConfigs();
    if(entityConfig.getInheritance() == InheritanceType.SINGLE_TABLE)
      propertyConfigs = ((SingleTableEntityConfig) entityConfig.getInheritanceTopLevelEntityConfig()).getAllTableFields();
//    else if(tableInfo.getInheritance() == TableInheritance.Joined && tableInfo.equals(tableInfo.getInheritanceTopLevelTableInfo()) == false) {
//      FieldType[] currentFieldTypes = fieldTypes;
//      fieldTypes = new FieldType[currentFieldTypes.length + 1];
//      System.arraycopy(currentFieldTypes, 0, fieldTypes, 0, currentFieldTypes.length);
//      fieldTypes[currentFieldTypes.length] = tableInfo.getInheritanceTopLevelTableInfo().getIdProperty();
//    }

    if(entityConfig instanceof InheritanceEntityConfig) {
      appendColumn(databaseType, entityConfig, ((InheritanceEntityConfig)entityConfig).getDiscriminatorPropertyConfig(), sb, additionalArgs, queriesAfter, statementsBefore, statementsAfter);
    }

    if(entityConfig.getIdProperty() != null) { // Join Tables don't have an id column
      appendColumn(databaseType, entityConfig, entityConfig.getIdProperty(), sb, additionalArgs, queriesAfter, statementsBefore, statementsAfter);
    }

    // our statement will be set here later
    for (PropertyConfig propertyConfig : propertyConfigs) {
      // skip foreign collections
      if (propertyConfig.isForeignCollection() || propertyConfig.isId() || propertyConfig instanceof DiscriminatorColumnConfig)
        continue;

      appendColumn(databaseType, entityConfig, propertyConfig, sb, additionalArgs, queriesAfter, statementsBefore, statementsAfter);
    }

    sb.delete(sb.length() - 2, sb.length()); // delete last ", "

    // add any sql that sets any primary key fields
    databaseType.addPrimaryKeySql(entityConfig.getPropertyConfigs(), additionalArgs, statementsBefore, statementsAfter,
        queriesAfter);
    // add any sql that sets any unique fields
    databaseType.addUniqueComboSql(entityConfig.getPropertyConfigs(), additionalArgs, statementsBefore, statementsAfter,
        queriesAfter);
    for (String arg : additionalArgs) {
      // we will have spat out one argument already so we don't have to do the first dance
      sb.append(", ").append(arg);
    }
    sb.append(") ");
    databaseType.appendCreateTableSuffix(sb);
    statements.addAll(statementsBefore);
    statements.add(sb.toString());
    statements.addAll(statementsAfter);
    addCreateIndexStatements(databaseType, entityConfig, statements, ifNotExists, false);
    addCreateIndexStatements(databaseType, entityConfig, statements, ifNotExists, true);
  }

  protected static <T, ID> void appendColumn(DatabaseType databaseType, EntityConfig<T, ID> entityConfig, PropertyConfig propertyConfig, StringBuilder sb, List<String> additionalArgs, List<String> queriesAfter, List<String> statementsBefore, List<String> statementsAfter) throws SQLException {
    String columnDefinition = propertyConfig.getColumnDefinition();
    if (columnDefinition == null) {
      // we have to call back to the database type for the specific create syntax
      databaseType.appendColumnArg(entityConfig.getTableName(), sb, propertyConfig, additionalArgs, statementsBefore,
          statementsAfter, queriesAfter);
    } else {
      // hand defined field
      databaseType.appendEscapedEntityName(sb, propertyConfig.getColumnName());
      sb.append(' ').append(columnDefinition).append(' ');
    }

    sb.append(", ");
  }

  private static <T, ID> void addCreateIndexStatements(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			List<String> statements, boolean ifNotExists, boolean unique) {
		// run through and look for index annotations
		Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
		for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
			String indexName;
			if (unique) {
				indexName = propertyConfig.getUniqueIndexName();
			} else {
				indexName = propertyConfig.getIndexName();
			}
			if (indexName == null) {
				continue;
			}

			List<String> columnList = indexMap.get(indexName);
			if (columnList == null) {
				columnList = new ArrayList<String>();
				indexMap.put(indexName, columnList);
			}
			columnList.add(propertyConfig.getColumnName());
		}

		StringBuilder sb = new StringBuilder(128);
		for (Map.Entry<String, List<String>> indexEntry : indexMap.entrySet()) {
			logger.info("creating index '{}' for table '{}", indexEntry.getKey(), entityConfig.getTableName());
			sb.append("CREATE ");
			if (unique) {
				sb.append("UNIQUE ");
			}
			sb.append("INDEX ");
			if (ifNotExists && databaseType.isCreateIndexIfNotExistsSupported()) {
				sb.append("IF NOT EXISTS ");
			}
			databaseType.appendEscapedEntityName(sb, indexEntry.getKey());
			sb.append(" ON ");
			databaseType.appendEscapedEntityName(sb, entityConfig.getTableName());
			sb.append(" ( ");
			boolean first = true;
			for (String columnName : indexEntry.getValue()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				databaseType.appendEscapedEntityName(sb, columnName);
			}
			sb.append(" )");
			statements.add(sb.toString());
			sb.setLength(0);
		}
	}

	/**
	 * Generate and return the list of statements to drop a database table.
	 */
	private static <T, ID> void addDropTableStatements(DatabaseType databaseType, EntityConfig<T, ID> entityConfig,
			List<String> statements) {
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();
		for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
			databaseType.dropColumnArg(propertyConfig, statementsBefore, statementsAfter);
		}
		StringBuilder sb = new StringBuilder(64);
		sb.append("DROP TABLE ");
		databaseType.appendEscapedEntityName(sb, entityConfig.getTableName());
		sb.append(' ');
		statements.addAll(statementsBefore);
		statements.add(sb.toString());
		statements.addAll(statementsAfter);
	}

	private static <T, ID> int doCreateTable(ConnectionSource connectionSource, EntityConfig<T, ID> entityConfig,
			boolean ifNotExists) throws SQLException {
		logger.info("creating table '{}'", entityConfig.getTableName());

    DatabaseType databaseType = connectionSource.getDatabaseType();
		List<String> statements = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		addCreateTableStatements(databaseType, entityConfig, statements, queriesAfter, ifNotExists);

		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			int stmtC =
					doStatements(connection, "create", statements, false, databaseType.isCreateTableReturnsNegative(),
							databaseType.isCreateTableReturnsZero());
			stmtC += doCreateTestQueries(connection, databaseType, queriesAfter);
			return stmtC;
		} finally {
      if(entityConfig.hasJoinTableProperties())
        createJoinTables(connectionSource, entityConfig);
			connectionSource.releaseConnection(connection);
		}
	}

  private static <T, ID> void createJoinTables(ConnectionSource connection, EntityConfig<T, ID> entityConfig) throws SQLException {
    for(PropertyConfig joinedProperty : entityConfig.getJoinTableProperties()) {
      if(joinedProperty.isOwningSide()) { // only owning sides create Join Tables
        doCreateTable(connection, joinedProperty.getJoinTable(), true);
      }
    }
  }

  private static int doStatements(DatabaseConnection connection, String label, Collection<String> statements,
			boolean ignoreErrors, boolean returnsNegative, boolean expectingZero) throws SQLException {
		int stmtC = 0;
		for (String statement : statements) {
			int rowC = 0;
			CompiledStatement compiledStmt = null;
			try {
				compiledStmt =
						connection.compileStatement(statement, StatementType.EXECUTE, NO_PROPERTY_CONFIGs,
								DatabaseConnection.DEFAULT_RESULT_FLAGS);
				rowC = compiledStmt.runExecute();
				logger.info("executed {} table statement changed {} rows: {}", label, rowC, statement);
			} catch (SQLException e) {
				if (ignoreErrors) {
					logger.info("ignoring {} error '{}' for statement: {}", label, e, statement);
				} else {
					throw SqlExceptionUtil.create("SQL statement failed: " + statement, e);
				}
			} finally {
				IOUtils.closeThrowSqlException(compiledStmt, "compiled statement");
			}
			// sanity check
			if (rowC < 0) {
				if (!returnsNegative) {
					throw new SQLException("SQL statement " + statement + " updated " + rowC
							+ " rows, we were expecting >= 0");
				}
			} else if (rowC > 0 && expectingZero) {
				throw new SQLException("SQL statement updated " + rowC + " rows, we were expecting == 0: " + statement);
			}
			stmtC++;
		}
		return stmtC;
	}

	private static int doCreateTestQueries(DatabaseConnection connection, DatabaseType databaseType,
			List<String> queriesAfter) throws SQLException {
		int stmtC = 0;
		// now execute any test queries which test the newly created table
		for (String query : queriesAfter) {
			CompiledStatement compiledStmt = null;
			try {
				compiledStmt =
						connection.compileStatement(query, StatementType.SELECT, NO_PROPERTY_CONFIGs,
								DatabaseConnection.DEFAULT_RESULT_FLAGS);
				// we don't care about an object cache here
				DatabaseResults results = compiledStmt.runQuery(null);
				int rowC = 0;
				// count the results
				for (boolean isThereMore = results.first(); isThereMore; isThereMore = results.next()) {
					rowC++;
				}
				logger.info("executing create table after-query got {} results: {}", rowC, query);
			} catch (SQLException e) {
				// we do this to make sure that the statement is in the exception
				throw SqlExceptionUtil.create("executing create table after-query failed: " + query, e);
			} finally {
				// result set is closed by the statement being closed
				IOUtils.closeThrowSqlException(compiledStmt, "compiled statement");
			}
			stmtC++;
		}
		return stmtC;
	}

	private static <T, ID> List<String> addCreateTableStatements(ConnectionSource connectionSource,
			EntityConfig<T, ID> entityConfig, boolean ifNotExists) throws SQLException {
		List<String> statements = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		addCreateTableStatements(connectionSource.getDatabaseType(), entityConfig, statements, queriesAfter, ifNotExists);
		return statements;
	}
}
