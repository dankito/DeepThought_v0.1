package com.j256.ormlite.dao.cda.inheritance.helper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.cda.testmodel.InheritanceModel;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 22/11/14.
 */
public class TableHelper {

  private final static Logger log = LoggerFactory.getLogger(TableHelper.class);


  public static <T, ID> Dao<T, ID> createDao(ConnectionSource connectionSource, Class<T> clazz) throws SQLException {
    if (connectionSource == null) {
      throw new SQLException("Connection source is null");
    }

    @SuppressWarnings("unchecked")
    Dao<T, ID> dao = Instances.getDaoManager().createDao(connectionSource, clazz);

    DatabaseTableConfig<T> tableConfig = dao.getTableConfig();
    if (tableConfig == null) {
      tableConfig = DatabaseTableConfig.fromClass(connectionSource, dao.getDataClass());
    }

    try {
      // first we drop it in case it existed before
      dropTable(connectionSource, tableConfig, true);
    } catch (SQLException ignored) {
      // ignore any errors about missing tables
    }

    TableUtils.createTable(connectionSource, tableConfig);

    return dao;
  }

  public static <T> void dropTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws SQLException {
    // drop the table and ignore any errors along the way
    TableUtils.dropTable(connectionSource, tableConfig, ignoreErrors);
  }

  public static void deleteAllTables(ConnectionSource connectionSource) throws SQLException {
    Dao<InheritanceModel.JoinedTableInheritanceBaseEntity, Long> baseEntityDao = createDao(connectionSource, InheritanceModel.JoinedTableInheritanceBaseEntity.class);

    List<String> tableNames = getAllTableNames(baseEntityDao);

    for(String tableName : tableNames) {
      try { baseEntityDao.executeRawNoArgs("DROP TABLE \"" + tableName + "\""); } catch (Exception ex) {
        String message = ex.getMessage();
      }
    }
  }

  public static List<String> getAllTableNames(Dao<?, ?> dao) throws SQLException {
    GenericRawResults<String[]> rawResults = dao.queryRaw("SELECT TABLE_NAME FROM information_schema.tables WHERE TABLE_TYPE='TABLE' AND TABLE_SCHEMA='PUBLIC'");

    List<String> tableNames = new ArrayList<>();
    for(String[] row : rawResults.getResults()) {
      tableNames.add(row[0]);
    }

    return tableNames;
  }
}
