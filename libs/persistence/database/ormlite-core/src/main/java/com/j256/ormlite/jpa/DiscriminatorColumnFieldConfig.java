package com.j256.ormlite.jpa;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.types.CharType;
import com.j256.ormlite.field.types.IntType;
import com.j256.ormlite.field.types.StringType;

import javax.persistence.DiscriminatorType;

/**
 * Created by ganymed on 17/11/14.
 */
public class DiscriminatorColumnFieldConfig extends DatabaseFieldConfig {

  public DiscriminatorColumnFieldConfig(String discriminatorColumnName, DiscriminatorType type, int length) {
    this.fieldName = discriminatorColumnName;
    this.columnName = discriminatorColumnName;

    this.width = length;
    this.columnDefinition = null;

    if(type == DiscriminatorType.STRING) {
      this.dataPersister = StringType.getSingleton();
      this.defaultValue = this.columnName;
    }
    else if(type == DiscriminatorType.CHAR) {
      this.dataPersister = CharType.getSingleton();
      this.defaultValue = "a"; // TODO: ??
    }
    else if(type == DiscriminatorType.INTEGER) {
      this.dataPersister = IntType.getSingleton();
      this.defaultValue = "1";
    }

    setDefaultValuesForDiscriminatorColumnField();
  }

  public DiscriminatorColumnFieldConfig(String discriminatorColumnName, DiscriminatorType type, int length, String columnDefinition) {
    this(discriminatorColumnName, type, length);
    this.columnDefinition = columnDefinition;
  }

  protected void setDefaultValuesForDiscriminatorColumnField() {
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
