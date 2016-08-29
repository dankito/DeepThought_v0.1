package net.dankito.deepthought.data.helper;

import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ganymed on 18/01/15.
 */
public class DatabaseHelper {


  public static Object getValueFromTable(IEntityManager entityManager, String tableName, String columnName, String entityId) throws SQLException {
    return getValueFromTable(entityManager, tableName, columnName, entityId, TableConfig.BaseEntityIdColumnName);
  }

  public static Object getValueFromTable(IEntityManager entityManager, String tableName, String columnName, String entityId, String idColumnName) throws SQLException {
    List queryResult = entityManager.doNativeQuery("SELECT " + columnName + " FROM " + tableName + " WHERE " + idColumnName + "=" + entityId);
    Assert.assertEquals(1, queryResult.size()); // only one row fetched

    if(queryResult.get(0) instanceof List) {
      queryResult = (List) queryResult.get(0);
      Assert.assertEquals(1, queryResult.size()); // only one field fetched
    }
    else if(queryResult.get(0) instanceof String[]) {
      String[] castResult = (String[]) queryResult.get(0);
      Assert.assertEquals(1, castResult.length); // only one field fetched
      String value = castResult[0];
      try { return Integer.parseInt(value); } catch(Exception ex) { } // often an Integer is returned - try to parse
      try { return Long.parseLong(value); } catch(Exception ex) { } // Ids, Timestamps, ... are persisted as Long - try to parse
      return value;
    }

    return queryResult.get(0);
  }
}
