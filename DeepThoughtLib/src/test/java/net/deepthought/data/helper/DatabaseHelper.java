package net.deepthought.data.helper;

import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;

import java.util.List;

/**
 * Created by ganymed on 18/01/15.
 */
public class DatabaseHelper {


  public static Object getValueFromTable(IEntityManager entityManager, String tableName, String columnName, Long entityId) {
    return getValueFromTable(entityManager, tableName, columnName, entityId, TableConfig.BaseEntityIdColumnName);
  }

  public static Object getValueFromTable(IEntityManager entityManager, String tableName, String columnName, Long entityId, String idColumnName) {
    List queryResult = entityManager.doNativeQuery("SELECT " + columnName + " FROM " + tableName + " WHERE " + idColumnName + "=" + entityId);
    Assert.assertEquals(1, queryResult.size()); // only one row fetched
    if(queryResult.get(0) instanceof List) {
      queryResult = (List) queryResult.get(0);
      Assert.assertEquals(1, queryResult.size()); // only one field fetched
    }
    return queryResult.get(0);
  }
}
