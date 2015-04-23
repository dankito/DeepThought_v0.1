package com.j256.ormlite.dao.cda.jointable;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jpa.Property;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.Registry;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.stmt.query.OrderBy;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ganymed on 01/11/14.
 */
public class JoinTableDao {

  protected ManyToManyConfig config;
  protected ConnectionSource connectionSource;

  protected StatementExecutor statementExecutor;

  // a little hack: has both ManyToManyCollections try to delete Relation and both depend on removal success, if last delete equals Ids equals current one, simply return lastDeletionResult
  protected Object lastDeletedOwningSideId = null;
  protected Object lastDeletedInverseSideId = null;
  protected boolean lastDeletionResult = false;

  protected JoinTableConfig joinTable;
  protected PropertyConfig owningSideConfig;
  protected Class inverseSideClass;
  protected Property inverseSideProperty;
  protected PropertyConfig inverseSideConfig;


  // cda: my constructor
  public JoinTableDao(JoinTableConfig joinTable, PropertyConfig owningSideConfig, Class inverseSideClass, Property inverseSideProperty) {
    this.joinTable = joinTable;
    this.owningSideConfig = owningSideConfig;
    this.inverseSideClass = inverseSideClass;
    this.inverseSideProperty = inverseSideProperty;

    this.connectionSource = joinTable.getConnectionSource();
    statementExecutor = new StatementExecutor(connectionSource.getDatabaseType(), joinTable, null);
  }

  // TODO: try to remove
  public JoinTableDao(ManyToManyConfig config, ConnectionSource connectionSource) throws SQLException {
    this.config = config;
    this.connectionSource = connectionSource;
    statementExecutor = new StatementExecutor(connectionSource.getDatabaseType(), config.getEntityConfig(), null);
  }


  public String[] getJoinedEntitiesIds(Object selfId, boolean isOwningSide) throws SQLException {
    String selfColumnName = getSelfColumnName(isOwningSide);
    String otherSideColumnName = getOtherSideColumnName(isOwningSide);
    boolean orderIds = false;
    PropertyConfig otherSidePropertyConfig = null;
    try { otherSidePropertyConfig = getOtherSideField(isOwningSide); orderIds = otherSidePropertyConfig.getOrderColumns().size() > 0; } catch(Exception ex) { }

//    String query = "SELECT " + otherSideColumnName + " FROM " + config.getJoinTableName() + " WHERE " + selfColumnName + "=?";
    String query = "SELECT " + otherSideColumnName + " FROM " + joinTable.getTableName() + " WHERE " + selfColumnName + "=?";

    String debugQuery = "SELECT * FROM " + joinTable.getTableName();
    GenericRawResults<String[]> debugRawResults = statementExecutor.queryRaw(connectionSource, debugQuery, new String[0], null);
    List<String[]> debugResults = debugRawResults.getResults();

    return retrieveJoinedEntitiesIds(selfId, query);
  }

  public String[] getJoinedEntitiesIdsOrdered(Object selfId, boolean isOwningSide, String orderTableName, String orderTableIdColumnName, List<OrderBy> orderByList) throws SQLException {
    String selfColumnName = getSelfColumnName(isOwningSide);
    String otherSideColumnName = getOtherSideColumnName(isOwningSide);

//    String query = "SELECT " + otherSideColumnName + " FROM " + config.getJoinTableName() +  " JOIN " + orderTableName + " ON " +
//        config.getJoinTableName() + "." + otherSideColumnName + "=" + orderTableName + "." + orderTableIdColumnName + " WHERE " + selfColumnName + "=? ORDER BY";
    String query = "SELECT " + otherSideColumnName + " FROM " + joinTable.getTableName() +  " JOIN " + orderTableName + " ON " +
        joinTable.getTableName() + "." + otherSideColumnName + "=" + orderTableName + "." + orderTableIdColumnName + " WHERE " + selfColumnName + "=? ORDER BY";

      for(OrderBy orderBy : orderByList) {
        query += " " + orderTableName + "." + orderBy.getColumnName();
        if (orderBy.isAscending() == false)
          query += " DESC";
        query += ",";
      }
      query = query.substring(0, query.length() - 1);

    return retrieveJoinedEntitiesIds(selfId, query);
  }

  protected String[] retrieveJoinedEntitiesIds(Object selfId, String query) throws SQLException {
    GenericRawResults<String[]> relationRawResult = statementExecutor.queryRaw(connectionSource, query, new String[]{selfId.toString()}, null);

    List<String[]> results = relationRawResult.getResults();
    String[] joinedEntitiesIds = new String[results.size()];
    for(int i = 0; i < results.size(); i++) {
      String[] result = results.get(i);
      joinedEntitiesIds[i] = result[0];
    }

//    List<String[]> debug = statementExecutor.queryRaw(connectionSource, "SELECT * FROM " + config.getJoinTableName(), new String[0], null).getResults();

    return joinedEntitiesIds;
  }

  public boolean doesJoinTableEntryExist(Object selfId, boolean isOwningSide, Object targetEntityId) throws SQLException {
    Object owningSideId = isOwningSide ? selfId : targetEntityId;
    Object inverseSideId = isOwningSide ? targetEntityId : selfId;

//    connectionSource.getReadOnlyConnection().compileStatement(query, StatementBuilder.StatementType.SELECT, );
//    GenericRawResults<String[]> relationRawResult = statementExecutor.queryRaw(connectionSource, "SELECT * FROM " + config.getJoinTableName() + " WHERE " + config
//        .getJoinTableOwningSideColumnName() + "=? AND " + config.getJoinTableInverseSideColumnName() + "=?", new String[] { owningSideId.toString(), inverseSideId.toString() }, null);
    GenericRawResults<String[]> relationRawResult = statementExecutor.queryRaw(connectionSource, "SELECT * FROM " + joinTable.getTableName() + " WHERE " + joinTable.getOwningSideJoinColumnName() + "=? AND "
        + joinTable.getInverseSideJoinColumnName() + "=?", new String[] { owningSideId.toString(), inverseSideId.toString() }, null);
    return relationRawResult.getResults().size() > 0;
  }

  public boolean insertJoinEntry(Object selfId, boolean isOwningSide, Object targetEntityId) throws SQLException {
    Object owningSideId = isOwningSide ? selfId : targetEntityId;
    Object inverseSideId = isOwningSide ? targetEntityId : selfId;

//    String query = "INSERT INTO " + config.getJoinTableName() + " (" + config.getJoinTableOwningSideColumnName() + ", " + config.getJoinTableInverseSideColumnName()
//        + ") VALUES (?, ?)";
    String query = "INSERT INTO " + joinTable.getTableName() + " (" + joinTable.getOwningSideJoinColumnName() + ", " + joinTable.getInverseSideJoinColumnName()
        + ") VALUES (?, ?)";
    DatabaseConnection connection = connectionSource.getReadWriteConnection();
    int numberOfRowsAffected = statementExecutor.executeRaw(connection, query, new String[] { owningSideId.toString(), inverseSideId.toString() });
    connectionSource.releaseConnection(connection);

    return numberOfRowsAffected == 1;
  }

  public boolean deleteJoinEntry(Object selfId, boolean isOwningSide, Object targetEntityId) throws SQLException {
    Object owningSideId = isOwningSide ? selfId : targetEntityId;
    Object inverseSideId = isOwningSide ? targetEntityId : selfId;

    // TODO: this is a little hack as both sides ManyToManyCollection try to remove JoinTable relation and depend on successful removal in order to proceed correctly
    if(owningSideId.equals(lastDeletedOwningSideId) && inverseSideId.equals(lastDeletedInverseSideId))
      return lastDeletionResult;

//    String query = "DELETE FROM " + config.getJoinTableName() + " WHERE " + config.getJoinTableOwningSideColumnName() + "=? AND " + config.getJoinTableInverseSideColumnName() + "=?";
    String query = "DELETE FROM " + joinTable.getTableName() + " WHERE " + joinTable.getOwningSideJoinColumnName() + "=? AND " + joinTable.getInverseSideJoinColumnName() + "=?";
    DatabaseConnection connection = connectionSource.getReadWriteConnection();
    int numberOfRowsAffected = statementExecutor.executeRaw(connection, query, new String[] { owningSideId.toString(), inverseSideId.toString() });
    connectionSource.releaseConnection(connection);

    lastDeletionResult = numberOfRowsAffected == 1;
    lastDeletedOwningSideId = owningSideId;
    lastDeletedInverseSideId = inverseSideId;

    return numberOfRowsAffected == 1;
  }

  protected String getSelfColumnName(boolean isOwningSide) {
//    return isOwningSide ? config.getJoinTableOwningSideColumnName() : config.getJoinTableInverseSideColumnName();
    return isOwningSide ? joinTable.getOwningSideJoinColumnName() : joinTable.getInverseSideJoinColumnName();
  }

  protected String getOtherSideColumnName(boolean isOwningSide) {
//    return isOwningSide ? config.getJoinTableInverseSideColumnName() : config.getJoinTableOwningSideColumnName();
    return isOwningSide ? joinTable.getInverseSideJoinColumnName() : joinTable.getOwningSideJoinColumnName();
  }

  protected PropertyConfig getOtherSideField(boolean isOwningSide) throws SQLException {
//    return isOwningSide ? config.getInverseSideFieldType() : config.getOwningSideFieldType();
    return isOwningSide ? owningSideConfig.getTargetPropertyConfig() : owningSideConfig;
  }

  public boolean dropTable() throws SQLException {
//    int numberOfRowsAffected = statementExecutor.executeRaw(connectionSource.getReadWriteConnection(), "DROP TABLE " + config.getJoinTableName(), new String[0]);
    int numberOfRowsAffected = statementExecutor.executeRaw(connectionSource.getReadWriteConnection(), "DROP TABLE " + joinTable.getTableName(), new String[0]);
    return numberOfRowsAffected > 0;
  }


  public ManyToManyConfig getConfig() {
    return config;
  }

  public PropertyConfig getInverseSideConfig() {
    if(inverseSideConfig == null && inverseSideProperty != null)
      inverseSideConfig = Registry.getPropertyRegistry().getPropertyConfiguration(inverseSideClass, inverseSideProperty);
    return inverseSideConfig;
  }
}
