package com.j256.ormlite.dao.cda;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.util.ArrayList;

/**
 * Created by ganymed on 01/11/14.
 */
public class DatabaseJoinTableConfig extends DatabaseTableConfig {

  protected ManyToManyConfig manyToManyConfig;


  public DatabaseJoinTableConfig(ManyToManyConfig manyToManyConfig) {
    super(null, manyToManyConfig.getJoinTableName(), new ArrayList<DatabaseFieldConfig>());

    this.manyToManyConfig = manyToManyConfig;
    createFieldTypes();
  }

  protected void createFieldTypes() {
//    Field field = new Field(null, manyToManyConfig.getJoinTableIdColumnName(), Long.class, Modifier.fieldModifiers(), 0, "", new byte[0]);
//    Instances.getFieldTypeCreator().createFieldType(manyToManyConfig.getConnectionSource(), manyToManyConfig.getJoinTableName(),)
  }
}
