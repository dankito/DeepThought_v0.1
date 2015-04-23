package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.JoinedEntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.InheritanceType;

/**
 * Abstract mapped statement for queries which handle the creating of a new object and the row mapping functionality.
 * 
 * @author graywatson
 */
public abstract class BaseMappedQuery<T, ID> extends BaseMappedStatement<T, ID> implements GenericRowMapper<T> {

	protected final PropertyConfig[] resultsPropertyConfigs;
	// cache of column names to results position
	private Map<String, Integer> columnPositions = null;
	private Object parent = null;
	private Object parentId = null;

	protected BaseMappedQuery(EntityConfig<T, ID> entityConfig, String statement, PropertyConfig[] argPropertyConfigs,
			PropertyConfig[] resultsPropertyConfigs) {
		super(entityConfig, statement, argPropertyConfigs);
		this.resultsPropertyConfigs = resultsPropertyConfigs;
	}

	public T mapRow(DatabaseResults results) throws SQLException {
		Map<String, Integer> colPosMap;
		if (columnPositions == null) {
			colPosMap = new HashMap<String, Integer>();
		} else {
			colPosMap = columnPositions;
		}

		ObjectCache objectCache = results.getObjectCache();
    Object id = null;
		if (objectCache != null) {
			id = idField.resultToJava(results, colPosMap);
			T cachedInstance = objectCache.get(clazz, id);
			if (cachedInstance != null) {
				// if we have a cached instance for this id then return it
				return cachedInstance;
			}
		}

		// create our instance
    List<PropertyConfig> instancePropertyConfigs = new ArrayList<>();
		T instance = createInstanceForResults(entityConfig, results, colPosMap, instancePropertyConfigs, resultsPropertyConfigs);


    if (objectCache != null && id != null) {
      // why not adding instance already here to cache? Avoids multiple retrievals on (deep) recursive dependencies on its fields
      objectCache.put(clazz, id, instance);
    }

		// populate its fields
		Boolean foreignCollections = false;

    for (PropertyConfig propertyConfig : instancePropertyConfigs) {
      if (propertyConfig.isForeignCollection()) {
        foreignCollections = true;
      } else {
        Object val = propertyConfig.resultToJava(results, colPosMap);
				/*
				 * This is pretty subtle. We introduced multiple foreign fields to the same type which use the {@link
				 * ForeignCollectionField} foreignColumnName field. The bug that was created was that all the fields
				 * were then set with the parent class. Only the fields that have a matching id value should be set to
				 * the parent. We had to add the val.equals logic.
				 */
        if (val != null && parent != null && propertyConfig.getType() == parent.getClass()
            && val.equals(parentId)) {
          propertyConfig.assignField(instance, parent, true, objectCache);
        } else {
          propertyConfig.assignField(instance, val, false, objectCache);
        }
        if (propertyConfig == idField) {
          id = val;
        }
      }
    }

    // if we have a cache and we have an id then add it to the cache
//    if (objectCache != null && id != null) { // for ManyToMany collections it is of great importance that instance will be cached already here, before ForeignCollections will be built
//      objectCache.put(clazz, id, instance);
//    }

		if (foreignCollections) {
			// go back and initialize any foreign collections
			for (PropertyConfig propertyConfig : instancePropertyConfigs) {
				if (propertyConfig.isForeignCollection()) {
					Collection collection = propertyConfig.buildForeignCollection(instance, id); // cda: implement against interfaces, not implementations
					if (collection != null) {
						propertyConfig.assignField(instance, collection, false, objectCache);
					}
				}
			}
		}

		if (columnPositions == null) {
			columnPositions = colPosMap;
		}

    entityConfig.invokePostLoadLifeCycleMethod(instance);

		return instance;
	}

  protected T createInstanceForResults(EntityConfig<T, ID> entityConfig, DatabaseResults results, Map<String, Integer> columnPositions, List<PropertyConfig> instancePropertyConfigs, PropertyConfig[] resultsPropertyConfigs) throws SQLException {
    if(entityConfig.getInheritance() != InheritanceType.JOINED) {
      instancePropertyConfigs.addAll(Arrays.asList(resultsPropertyConfigs));
      return this.entityConfig.createObject();
    }
    else {
//      if(tableInfo.getChildTableInfos().size() == 0) { // class lowest in inheritance hierarchy
//        return tableInfo.createObject();
//      }

      JoinedEntityConfig joinedTableInfo = (JoinedEntityConfig) entityConfig.getInheritanceTopLevelEntityConfig();
      String discriminatorColumnName = joinedTableInfo.getDiscriminatorPropertyConfig().getColumnName();

      Integer discriminatorColumnIndex = columnPositions.get(discriminatorColumnName);
      if (discriminatorColumnIndex == null) {
        discriminatorColumnIndex = results.findColumn(discriminatorColumnName);
        if(discriminatorColumnIndex >= 0)
          columnPositions.put(discriminatorColumnName, discriminatorColumnIndex);
        else {
          discriminatorColumnIndex = null;
          // TODO: is this correct?
          return entityConfig.createObject();
        }
      }

      String discriminatorValue = results.getString(discriminatorColumnIndex); // TODO: check for Discriminator type first
      EntityConfig resultEntityEntityConfig = joinedTableInfo.getEntityForDiscriminatorValue(discriminatorValue);

      instancePropertyConfigs.addAll(joinedTableInfo.getSubClassFieldTypes(discriminatorValue));

      return (T) resultEntityEntityConfig.createObject();
    }
  }

  protected void assignInstanceValuesFromResult(T instance, List<PropertyConfig> instancePropertyConfigs, DatabaseResults results, Map<String, Integer> colPosMap, Object id, Boolean foreignCollections, ObjectCache objectCache) throws SQLException {
    for (PropertyConfig propertyConfig : instancePropertyConfigs) {
      if (propertyConfig.isForeignCollection()) {
        foreignCollections = true;
      } else {
        Object val = propertyConfig.resultToJava(results, colPosMap);
				/*
				 * This is pretty subtle. We introduced multiple foreign fields to the same type which use the {@link
				 * ForeignCollectionField} foreignColumnName field. The bug that was created was that all the fields
				 * were then set with the parent class. Only the fields that have a matching id value should be set to
				 * the parent. We had to add the val.equals logic.
				 */
        if (val != null && parent != null && propertyConfig.getField().getType() == parent.getClass()
            && val.equals(parentId)) {
          propertyConfig.assignField(instance, parent, true, objectCache);
        } else {
          propertyConfig.assignField(instance, val, false, objectCache);
        }
        if (propertyConfig == idField) {
          id = val;
        }
      }
    }
  }

	/**
	 * If we have a foreign collection object then this sets the value on the foreign object in the class.
	 */
	public void setParentInformation(Object parent, Object parentId) {
		this.parent = parent;
		this.parentId = parentId;
	}
}
