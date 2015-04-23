package com.j256.ormlite.dao.cda.jointable;

import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 01/11/14.
 */
public class JoinTableDaoRegistry {

  private static JoinTableDaoRegistry instance = null;

  public static JoinTableDaoRegistry getInstance() {
    if(instance == null)
      instance = new JoinTableDaoRegistry();
    return instance;
  }

  public static void setJoinTableRegistry(JoinTableDaoRegistry registry) {
    instance = registry;
  }


  protected JoinTableDaoRegistry() {

  }

  protected Map<String, JoinTableDao> createdJoinTableDaos = new HashMap<>();


  public JoinTableDao getJoinTableDaoForManyToManyRelation(ManyToManyConfig config, ConnectionSource connectionSource) throws SQLException {
    if(createdJoinTableDaos.containsKey(config.getJoinTableName()) == false) {
      createdJoinTableDaos.put(config.getJoinTableName(), new JoinTableDao(config, connectionSource));
    }

    return createdJoinTableDaos.get(config.getJoinTableName());
  }

//  protected JoinTableDao createJoinTableDao(ManyToManyConfig config) throws SQLException {
////    TableUtils.doCreateTable(config.getConnectionSource(), config.getTableInfo(), true);
//    return new JoinTableDao(config);
//  }

}
