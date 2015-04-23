package com.j256.ormlite.stmt;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Default row mapper when you are using the {@link Dao#queryRaw(String, RawRowMapper, String...)}.
 * 
 * @author graywatson
 */
public class RawRowMapperImpl<T, ID> implements RawRowMapper<T> {

	private final EntityConfig<T, ID> entityConfig;

	public RawRowMapperImpl(EntityConfig<T, ID> entityConfig) {
		this.entityConfig = entityConfig;
	}

	public T mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
		// create our object
		T rowObj = entityConfig.createObject();
    Object id = null;

		for (int i = 0; i < columnNames.length; i++) {
			// sanity check, prolly will never happen but let's be careful out there
			if (i >= resultColumns.length) {
				continue;
			}
			// run through and convert each field
			PropertyConfig propertyConfig = entityConfig.getFieldTypeByColumnName(columnNames[i]);
			Object fieldObj = propertyConfig.convertStringToJavaField(resultColumns[i], i);
			// assign it to the row object
			propertyConfig.assignField(rowObj, fieldObj, false, null);

      if(propertyConfig.isId())
        id = fieldObj;
		}

    for(PropertyConfig foreignCollectionType : entityConfig.getForeignCollections()) {
      Object foreignObj = foreignCollectionType.extractRawJavaFieldValue(rowObj);
      if(foreignCollectionType.isForeignCollectionInstance(foreignObj) == false) {
        Collection collection = foreignCollectionType.buildForeignCollection(rowObj, id);
        foreignCollectionType.assignField(rowObj, collection, true, null);
      }
    }

    entityConfig.invokePostLoadLifeCycleMethod(rowObj);

		return rowObj;
	}
}
