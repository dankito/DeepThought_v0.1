package com.j256.ormlite.dao.cda.jointable;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.types.LongType;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;

/**
 * Created by ganymed on 02/11/14.
 */
public class JoinTableDatabaseFieldConfig extends DatabaseFieldConfig {
  
  public JoinTableDatabaseFieldConfig(ManyToManyConfig manyToManyConfig, String fieldName, Class dataType) {
    this.fieldName = fieldName;
    this.columnName = fieldName;
    
    setDefaultValuesForJoinTableField();
  }

  public JoinTableDatabaseFieldConfig(ManyToManyConfig manyToManyConfig, String fieldName, Class dataType, boolean isIdColumn, boolean autogenerateIds) {
    this(manyToManyConfig, fieldName, dataType);

    this.id = isIdColumn;
    this.generatedId = autogenerateIds;
  }

  protected void setDefaultValuesForJoinTableField() {
    this.dataPersister = LongType.getSingleton(); // TODO: find DataPersister for dataType
    this.defaultValue = null;  // TODO: depend on dataType
    this.width = 0;  // TODO: depend on dataType
    this.id = false;
    this.generatedId = false;
    this.generatedIdSequence = null;
    this.foreign = false;
    this.foreignTableConfig = null;
    this.useGetSet = false;
    this.unknownEnumValue = null;
    this.throwIfNull = false;
    this.format = null;
    this.unique = false;
    this.uniqueCombo = false;
    this.index = false;
    this.indexName = null;
    this.uniqueIndex = false;
    this.uniqueIndexName = null;
    this.foreignAutoRefresh = false;
    this.allowGeneratedIdInsert = false;
    this.columnDefinition = null;
    this.foreignAutoCreate = false;
    this.foreignColumnName = "";
    this.readOnly = false;
    // foreign collection field information
    this.foreignCollection = false;
    this.foreignCollectionEager = false;
    this.foreignCollectionColumnName = "";
    this.foreignCollectionOrderColumnName = "";
    this.foreignCollectionForeignFieldName = "";
  }

}
