package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.GeneratedKeyHolder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A mapped statement for creating a new instance of an object.
 * 
 * @author graywatson
 */
public class MappedCreate<T, ID> extends BaseMappedStatement<T, ID> {

	private final String queryNextSequenceStmt;
	private String dataClassName;
	private int versionFieldTypeIndex;

  protected List<Object> autoCreatingJoinColumnObjects = new ArrayList<>();


	private MappedCreate(EntityConfig<T, ID> entityConfig, String statement, PropertyConfig[] argPropertyConfigs,
			String queryNextSequenceStmt, int versionFieldTypeIndex) {
		super(entityConfig, statement, argPropertyConfigs);
		this.dataClassName = entityConfig.getEntityClass().getSimpleName();
		this.queryNextSequenceStmt = queryNextSequenceStmt;
		this.versionFieldTypeIndex = versionFieldTypeIndex;
	}

	/**
	 * Create an object in the database.
	 */
	public int insert(DatabaseType databaseType, DatabaseConnection databaseConnection, T data, ObjectCache objectCache)
			throws SQLException {
    if(autoCreatingJoinColumnObjects.contains(data))
      return -1;

    autoCreatingJoinColumnObjects.add(data);
		KeyHolder keyHolder = null;

		if (idField != null) {
			boolean assignId;
			if (idField.isAllowGeneratedIdInsert() && !idField.isObjectsFieldValueDefault(data)) {
				assignId = false;
			} else {
				assignId = true;
			}

			if (idField.isSelfGeneratedId() && idField.isGeneratedId()) {
				if (assignId) {
					idField.assignField(data, idField.generateId(), false, objectCache);
				}
			} else if (idField.isGeneratedIdSequence() && databaseType.isSelectSequenceBeforeInsert()) {
				if (assignId) {
					assignSequenceId(databaseConnection, data, objectCache);
				}
			} else if (idField.isGeneratedId()) {
				if (assignId) {
					// get the id back from the database
					keyHolder = new KeyHolder();
				}
			} else {
				// the id should have been set by the caller already
			}
		}

		try {
//			// implement {@link DatabaseField#foreignAutoCreate()}, need to do this _before_ getFieldObjects() below
//			if (entityConfig.isForeignAutoCreate()) {
//				for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
//					if (!propertyConfig.isForeignAutoCreate()) {
//						continue;
//					}
//					// get the field value
//					Object foreignObj = propertyConfig.extractRawJavaFieldValue(data);
//          if(foreignObj instanceof Collection) {
//            for(Object item : (Collection)foreignObj) {
//              try {
//                if (item != null && propertyConfig.getForeignIdField().isObjectsFieldValueDefault(item)) {
//                  propertyConfig.createWithForeignDao(item);
//                }
//              } catch (Exception ex) {
//                logger.error("Could not insert value for Property " + propertyConfig.getField());
//              }
//            }
//          }
//          else {
//            try {
//              if (foreignObj != null && propertyConfig.getForeignIdField().isObjectsFieldValueDefault(foreignObj)) {
//                propertyConfig.createWithForeignDao(foreignObj);
//              }
//            } catch (Exception ex) {
//              logger.error("Could not insert value for Property " + propertyConfig.getField());
//            }
//          }
//				}
//			}

//      createUncreatedOneToOneAndManyToOneRelatedProperties(data);

      Object[] args = getFieldObjects(data);
			Object versionDefaultValue = null;
			// implement {@link DatabaseField#version()}
			if (versionFieldTypeIndex >= 0 && args[versionFieldTypeIndex] == null) {
				// if the version is null then we need to initialize it before create
				PropertyConfig versionPropertyConfig = argPropertyConfigs[versionFieldTypeIndex];
				versionDefaultValue = versionPropertyConfig.moveToNextValue(null);
				args[versionFieldTypeIndex] = versionPropertyConfig.convertJavaFieldToSqlArgValue(versionDefaultValue);
			}

			int rowC;
			try {
				rowC = databaseConnection.insert(statement, args, argPropertyConfigs, keyHolder);
			} catch (SQLException e) {
				logger.debug("insert data with statement '{}' and {} args, threw exception: {}", statement, args.length, e);
				if (args.length > 0) {
					// need to do the (Object) cast to force args to be a single object
					logger.trace("insert arguments: {}", (Object) args);
				}

				throw e;
			}
      finally {
        autoCreatingJoinColumnObjects.remove(data);
      }
      logger.debug("insert data with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("insert arguments: {}", (Object) args);
			}

			if (rowC > 0) {
				if (versionDefaultValue != null) { // assign version
					argPropertyConfigs[versionFieldTypeIndex].assignField(data, versionDefaultValue, false, null);
				}

        assignCreatedId(data, objectCache, keyHolder);

				/*
				 * If we have a cache and if all of the foreign-collection fields have been assigned then add to cache.
				 * However, if one of the foreign collections has not be assigned then don't add it to the cache.
				 */
        Object id = idField.extractJavaFieldValue(data);
				if (objectCache != null /*&& foreignCollectionsAreAssigned(entityConfig.getForeignCollections(), data)*/) {
					objectCache.put(clazz, id, data);
				}

        // cda:
        assignEntityCollectionToForeignCollections(data, id);
        persistColumnsWithCascadePersist(data);
			}

			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run insert stmt on object " + data + ": " + statement, e);
		}
    finally {
      autoCreatingJoinColumnObjects.remove(data);
    }
	}

  protected void assignCreatedId(T data, ObjectCache objectCache, KeyHolder keyHolder) throws SQLException {
    if (keyHolder != null) {
      // assign the key returned by the database to the object's id field after it was inserted
      Number key = keyHolder.getKey();
      if (key == null) {
        // may never happen but let's be careful out there
        throw new SQLException(
            "generated-id key was not set by the update call, maybe a schema mismatch between entity and database table?");
      }
      if (key.longValue() == 0L) {
        // sanity check because the generated-key returned is 0 by default, may never happen
        throw new SQLException(
            "generated-id key must not be 0 value, maybe a schema mismatch between entity and database table?");
      }
      assignIdValue(data, key, "keyholder", objectCache);
    }
  }

  protected void createUncreatedOneToOneAndManyToOneRelatedProperties(T data) {
    for(PropertyConfig joinColumnProperty : entityConfig.getJoinColumns()) { // TODO: right now i don't care if cascade is set to persist or not
      try {
        Object foreignObj = joinColumnProperty.extractRawJavaFieldValue(data);

        if (foreignObj != null && joinColumnProperty.getForeignIdField().isObjectsFieldValueDefault(foreignObj)) {
          if(autoCreatingJoinColumnObjects.contains(foreignObj) == false) {
            autoCreatingJoinColumnObjects.add(foreignObj); // to avoid cyclic redundancies
            joinColumnProperty.createWithForeignDao(foreignObj);
            autoCreatingJoinColumnObjects.remove(foreignObj);
          }
        }
      } catch (Exception ex) {
        logger.error("Could not insert value for Property " + joinColumnProperty);
      }
    }
  }

  protected void assignEntityCollectionToForeignCollections(T data, Object id) {
    for(PropertyConfig foreignCollectionField : entityConfig.getForeignCollections()) {
      try {
        Object foreignObj = foreignCollectionField.extractRawJavaFieldValue(data);
        if(foreignCollectionField.isForeignCollectionInstance(foreignObj) == false) {
          Collection entityCollection = foreignCollectionField.buildForeignCollection(data, id, false);
          foreignCollectionField.assignField(data, entityCollection, true, null);

          for (Object item : (Collection<?>) foreignObj) {
            entityCollection.add(item);
          }
        }
      } catch(Exception ex) {
        logger.error("Could not assign an EntityCollection to foreign collection field " + foreignCollectionField, ex);
      }
    }
  }

  protected void persistColumnsWithCascadePersist(T data) throws SQLException {
    persistForeignCollectionsWithCascadePersist(data);

    persistJoinColumnsWithCascadePersist(data);
  }

  protected void persistForeignCollectionsWithCascadePersist(T data) throws SQLException {
    for(PropertyConfig foreignCollectionField : entityConfig.getForeignCollections()) {
//      if(foreignCollectionField.cascadePersist()) {
        Object foreignObj = foreignCollectionField.extractRawJavaFieldValue(data);
        if(foreignObj instanceof Collection) {
          for(Object item : (Collection<?>)foreignObj) {
//            if (foreignCollectionField.getForeignPropertyConfig().getForeignIdField() != null &&
//                foreignCollectionField.getForeignPropertyConfig().getForeignIdField().isObjectsFieldValueDefault(item))
            if (foreignCollectionField.getTargetEntityConfig().getIdProperty().isObjectsFieldValueDefault(item))
              foreignCollectionField.createWithForeignDao(item);

            if((foreignCollectionField.isOneToManyField() && foreignCollectionField.getOneToManyConfig().isBidirectional()) ||
                foreignCollectionField.isManyToManyField() && foreignCollectionField.getManyToManyConfig().isBidirectional())
            foreignCollectionField.getForeignDao().update(item);
          }
        }
//      }
    }
  }

  protected void persistJoinColumnsWithCascadePersist(T data) throws SQLException {
//    boolean entityNeedsUpdate = false;

    for(PropertyConfig joinColumn : entityConfig.getJoinColumns()) {
//      if(joinColumn.cascadePersist()) {
        Object foreignObj = joinColumn.extractRawJavaFieldValue(data);
        // TODO: check for cyclic dependencies
        if (foreignObj != null) {
          if(joinColumn.getForeignIdField().isObjectsFieldValueDefault(foreignObj)) {
            joinColumn.createWithForeignDao(foreignObj);

//            entityNeedsUpdate = true;
          }
        }
//      }
    }

    if(entityConfig.getJoinColumns().size() > 0)
      entityConfig.getDao().update(data);
  }

  public static <T, ID> MappedCreate<T, ID> build(DatabaseType databaseType, EntityConfig<T, ID> entityConfig) {
		StringBuilder sb = new StringBuilder(128);
		appendTableName(databaseType, sb, "INSERT INTO ", entityConfig.getTableName());
		int argFieldC = 0;
		int versionFieldTypeIndex = -1;
		// first we count up how many arguments we are going to have
		for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
			if (isFieldCreatable(databaseType, propertyConfig)) {
				if (propertyConfig.isVersion()) {
					versionFieldTypeIndex = argFieldC;
				}
				argFieldC++;
			}
		}
		PropertyConfig[] argPropertyConfigs = new PropertyConfig[argFieldC];
		if (argFieldC == 0) {
			databaseType.appendInsertNoColumns(sb);
		} else {
			argFieldC = 0;
			boolean first = true;
			sb.append('(');
			for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
				if (!isFieldCreatable(databaseType, propertyConfig)) {
					continue;
				}
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				appendFieldColumnName(databaseType, sb, propertyConfig, null);
				argPropertyConfigs[argFieldC++] = propertyConfig;
			}
			sb.append(") VALUES (");
			first = true;
			for (PropertyConfig propertyConfig : entityConfig.getPropertyConfigs()) {
				if (!isFieldCreatable(databaseType, propertyConfig)) {
					continue;
				}
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append("?");
			}
			sb.append(")");
		}
		PropertyConfig idField = entityConfig.getIdProperty();
		String queryNext = buildQueryNextSequence(databaseType, idField);
		return new MappedCreate<T, ID>(entityConfig, sb.toString(), argPropertyConfigs, queryNext, versionFieldTypeIndex);
	}

	private boolean foreignCollectionsAreAssigned(List<PropertyConfig> foreignCollections, Object data) throws SQLException {
		for (PropertyConfig propertyConfig : foreignCollections) {
			if (propertyConfig.extractJavaFieldValue(data) == null) {
				return false;
			}
		}
		return true;
	}

	private static boolean isFieldCreatable(DatabaseType databaseType, PropertyConfig propertyConfig) {
		// we don't insert anything if it is a collection
		if (propertyConfig.isForeignCollection()) {
			// skip foreign collections
			return false;
		} else if (propertyConfig.isReadOnly() || propertyConfig.isInsertable() == false) {
			// ignore read-only fields
			return false;
		} else if (databaseType.isIdSequenceNeeded() && databaseType.isSelectSequenceBeforeInsert()) {
			// we need to query for the next value from the sequence and the idProperty is inserted afterwards
			return true;
		} else if (propertyConfig.isGeneratedId() && !propertyConfig.isSelfGeneratedId() && !propertyConfig.isAllowGeneratedIdInsert()) {
			// skip generated-id fields because they will be auto-inserted
			return false;
		} else {
			return true;
		}
	}

	private static String buildQueryNextSequence(DatabaseType databaseType, PropertyConfig idField) {
		if (idField == null) {
			return null;
		}
		String seqName = idField.getGeneratedIdSequence();
		if (seqName == null) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder(64);
			databaseType.appendSelectNextValFromSequence(sb, seqName);
			return sb.toString();
		}
	}

	private void assignSequenceId(DatabaseConnection databaseConnection, T data, ObjectCache objectCache)
			throws SQLException {
		// call the query-next-sequence stmt to increment the sequence
		long seqVal = databaseConnection.queryForLong(queryNextSequenceStmt);
		logger.debug("queried for sequence {} using stmt: {}", seqVal, queryNextSequenceStmt);
		if (seqVal == 0) {
			// sanity check that it is working
			throw new SQLException("Should not have returned 0 for stmt: " + queryNextSequenceStmt);
		}
		assignIdValue(data, seqVal, "sequence", objectCache);
	}

	private void assignIdValue(T data, Number val, String label, ObjectCache objectCache) throws SQLException {
		// better to do this in one please with consistent logging
		idField.assignIdValue(data, val, objectCache);
		if (logger.isDebugEnabled()) {
			logger.debug("assigned id '{}' from {} to '{}' in {} object",
					new Object[] { val, label, idField.getFieldName(), dataClassName });
		}
	}

	private static class KeyHolder implements GeneratedKeyHolder {
		Number key;

		public Number getKey() {
			return key;
		}

		public void addKey(Number key) throws SQLException {
			if (this.key == null) {
				this.key = key;
			} else {
				throw new SQLException("generated key has already been set to " + this.key + ", now set to " + key);
			}
		}
	}
}
