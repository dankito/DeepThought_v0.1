package net.deepthought.persistence;

import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.persistence.db.TableConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

/**
 * Created by ganymed on 02/01/15.
 */
public class DatabaseUpdateManager {

  private final static Logger log = LoggerFactory.getLogger(DatabaseUpdateManager.class);


  public static void updateDatabaseToCurrentVersion(EntityManager entityManager, DeepThoughtApplication application) {
    if(application.getDataModelVersion() == 0) {

      EntityTransaction transaction = entityManager.getTransaction();
      transaction.begin();

      try {
//        Query query = entityManager.createNativeQuery("ALTER TABLE " + TableConfig.AppSettingsTableName + "  ADD " + TableConfig.AppSettingsDataModelVersionColumnName + " " + "NUMBER");
        Query query = entityManager.createNativeQuery("ALTER TABLE " + TableConfig.AppSettingsTableName + " RENAME COLUMN database_version to  " + TableConfig.AppSettingsDataModelVersionColumnName);
        query.executeUpdate();

        transaction.commit();

        application.setDataModelVersion(1);
      } catch(Exception ex) {
        log.error("Could not update Database to Version 1", ex);

        if(transaction.isActive()) {
          try {
            transaction.rollback();
          } catch(Exception ex2) {
            log.error("Could not rollback transaction", ex2);
          }
        }
      }
      finally {
        transaction = null;
      }
    }

    /*
    SQLite supports a limited subset of ALTER TABLE. The ALTER TABLE command in SQLite allows the user to rename a table or to add a new column to an existing table. It is not possible to rename a column, remove a column, or add or remove constraints from a table.

You can:
1. create new table as the one you are trying to change,
2. copyFile all data,
3. drop old table,
4. rename the new one
     */

    /*
    Add multiple columns in table

ALTER TABLE table_name
    ADD (column_1 column-definition,
       column_2 column-definition,
       ...
       column_n column_definition);

       Modify multiple columns in table

       ALTER TABLE table_name
  MODIFY (column_1 column_type,
          column_2 column_type,
          ...
          column_n column_type);

          Drop column in table

          ALTER TABLE table_name
  DROP COLUMN column_name;

  Rename column in table

  ALTER TABLE table_name
  RENAME COLUMN old_name to new_name;

  Rename table

  ALTER TABLE table_name
  RENAME TO new_table_name;
     */
  }
}
