package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseConnection;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A mapped statement for deleting an object.
 * 
 * @author graywatson
 */
public class MappedDelete<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedDelete(EntityConfig<T, ID> entityConfig, String statement, PropertyConfig[] argPropertyConfigs) {
		super(entityConfig, statement, argPropertyConfigs);
	}

	public static <T, ID> MappedDelete<T, ID> build(DatabaseType databaseType, EntityConfig<T, ID> entityConfig)
			throws SQLException {
		PropertyConfig idField = entityConfig.getIdProperty();
		if (idField == null) {
			throw new SQLException("Cannot delete from " + entityConfig.getEntityClass()
					+ " because it doesn't have an id field");
		}
		StringBuilder sb = new StringBuilder(64);
		appendTableName(databaseType, sb, "DELETE FROM ", entityConfig.getTableName());
		appendWhereFieldEq(databaseType, idField, sb, null);
		return new MappedDelete<T, ID>(entityConfig, sb.toString(), new PropertyConfig[] { idField });
	}

	/**
	 * Delete the object from the database.
	 */
	public int delete(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		try {
			Object[] args = getFieldObjects(data);
			int rowC = databaseConnection.delete(statement, args, argPropertyConfigs);
			logger.debug("delete data with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("delete arguments: {}", (Object) args);
			}

      if(rowC > 0) { // removing has been successful
//        unsetEntitysRelationships(data);
        setIdToDefaultAndRemoveEntityFromCache(data, objectCache);
      }

			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run delete stmt on object " + data + ": " + statement, e);
		}
	}

  protected void setIdToDefaultAndRemoveEntityFromCache(T data, ObjectCache objectCache) throws SQLException {
    Object id = idField.extractJavaFieldToSqlArgValue(data);
    Field idFieldType = idField.getField();

    try {
      if (idFieldType.getType().isPrimitive())
        idField.getField().set(data, 0);
      else
        idField.getField().set(data, null);
    } catch(Exception ex) { logger.warn("Could not set entity " + data + " id to default value after entity has been deleted from database", ex); }

    if (objectCache != null) {
      objectCache.remove(clazz, id);
    }
  }

  protected void unsetEntitysRelationships(T data) {
    for(PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
      if(propertyConfig.isForeign()) {
        removeForeignFieldAssociation(data, propertyConfig);
      }
      else if(propertyConfig.isForeignCollection()) {
        removeForeignCollectionAssociation(data, propertyConfig);
      }
    }
  }

  protected void removeForeignFieldAssociation(T data, PropertyConfig propertyConfig) {
    try {
      Object foreignInstance = propertyConfig.extractRawJavaFieldValue(data);
      PropertyConfig foreignPropertyConfig = propertyConfig.getTargetPropertyConfig();
      try {
        if(foreignPropertyConfig.isForeign()) // a OneToOne relation -> set foreign relation as well to null
          foreignPropertyConfig.assignField(foreignInstance, null, false, null);
        else if(foreignPropertyConfig.isForeignCollection()) { // a ManyToOne relation -> remove data from one side collection
          Collection associationCollection = (Collection) foreignPropertyConfig.extractRawJavaFieldValue(foreignInstance);
          associationCollection.remove(data);
        }

        boolean cascadeRemove = false;
        if(propertyConfig.isOneToOneField()) {
          cascadeRemove = propertyConfig.getOneToOneConfig().cascadeRemove();
        }
        else if(propertyConfig.isManyToOneField()) {
          cascadeRemove = propertyConfig.getOneToManyConfig().cascadeRemove();
        }

        if(cascadeRemove) {
          propertyConfig.getForeignDao().delete(foreignInstance, foreignInstance.getClass());
        }

        propertyConfig.assignField(data, null, false, null); // set this side relation to null
      } catch (Exception ex) {
        logger.error("Could not assign foreign field " + propertyConfig, ex);
      }
//        if(fieldType.isOneToOneField() && fieldType.getOneToOneConfig().cascadeRemove())
//          try { fieldType.getOneToOneConfig().getOtherSideField(fieldType).
    } catch(Exception ex) { logger.error("Could not unset foreign field", ex); }
  }

  protected void removeForeignCollectionAssociation(T data, PropertyConfig propertyConfig) {
    try {
      Object associationCollection = propertyConfig.extractRawJavaFieldValue(data);
      PropertyConfig foreignPropertyConfig = propertyConfig.getTargetPropertyConfig();

      if(associationCollection instanceof Collection) {
        Collection<?> collection = (Collection<?>)associationCollection;
        List backup = new ArrayList<>(collection);

        for (Object foreignAssociation : backup) {
          if(propertyConfig.isOneToManyField())
            foreignPropertyConfig.assignField(foreignAssociation, null, true, null);
          else if(propertyConfig.isManyToManyField()) {
            Object foreignCollection = foreignPropertyConfig.extractJavaFieldValue(foreignAssociation);
//            if(foreignAssociation instanceof Collection)
              ((Collection)foreignCollection).remove(data);
          }
        }

        boolean cascadeRemove = false;
        if(propertyConfig.isOneToManyField()) {
          cascadeRemove = propertyConfig.getOneToManyConfig().cascadeRemove();
        }
        else if(propertyConfig.isManyToManyField()) {
          cascadeRemove = propertyConfig.getManyToManyConfig().cascadeRemove();
        }

        for(Object foreignAssociation : backup)
          collection.remove(foreignAssociation);

        if(cascadeRemove) {
          for(Object foreignAssociation : backup) {
            propertyConfig.getForeignDao().delete(foreignAssociation, foreignAssociation.getClass());
          }
        }
      }
    } catch(Exception ex) { logger.error("Could not unset foreign field", ex); }
  }

  /**
	 * Delete the object from the database.
	 */
	public int deleteById(DatabaseConnection databaseConnection, ID id, ObjectCache objectCache) throws SQLException {
		try {
			Object[] args = new Object[] { convertIdToFieldObject(id) };
			int rowC = databaseConnection.delete(statement, args, argPropertyConfigs);
			logger.debug("delete data with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("delete arguments: {}", (Object) args);
			}

      if(rowC > 0 && objectCache != null) { // removing has been successful
        T data = objectCache.get(clazz, id);
        if(data != null) {
          setIdToDefaultAndRemoveEntityFromCache(data, objectCache);
          unsetEntitysRelationships(data);
        }
        else
          objectCache.remove(clazz, id);
      }
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run deleteById stmt on id " + id + ": " + statement, e);
		}
	}
}
