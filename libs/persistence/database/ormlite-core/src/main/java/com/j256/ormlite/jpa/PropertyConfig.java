package com.j256.ormlite.jpa;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.dao.cda.EntitiesCollection;
import com.j256.ormlite.dao.cda.LazyLoadingEntitiesCollection;
import com.j256.ormlite.dao.cda.ManyToManyEntitiesCollection;
import com.j256.ormlite.dao.cda.ManyToManyLazyLoadingEntitiesCollection;
import com.j256.ormlite.dao.cda.jointable.JoinTableConfig;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.VoidType;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.jpa.relationconfig.OneToManyConfig;
import com.j256.ormlite.jpa.relationconfig.OneToOneConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.stmt.mapped.MappedQueryForId;
import com.j256.ormlite.stmt.query.OrderBy;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.GenerationType;
import javax.persistence.InheritanceType;

/**
 * Per field information configured from the {@link com.j256.ormlite.field.DatabaseField} annotation and the associated {@link Field} in the
 * class. Use the {@link #createFieldType} static method to instantiate the class.
 * 
 * @author graywatson
 */
public class PropertyConfig {

	/** default suffix added to fields that are id fields of foreign objects */
	public static final String FOREIGN_ID_FIELD_SUFFIX = "_id";

  public static final int NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED = DatabaseField.DEFAULT_MAX_FOREIGN_AUTO_REFRESH_LEVEL;

  private final static Logger log = LoggerFactory.getLogger(PropertyConfig.class);


	protected ConnectionSource connectionSource;
  protected String tableName;
  protected DatabaseFieldConfig fieldConfig;
  protected Class<?> parentClass;
  protected boolean isPropertyOfParentClass;

  protected DataPersister dataPersister;
  protected Object defaultValue;
  protected Object dataTypeConfigObj;

  protected FieldConverter fieldConverter;
  protected PropertyConfig foreignIdField;
  protected EntityConfig<?, ?> foreignEntityConfig;
  protected PropertyConfig foreignPropertyConfig;
  protected Dao<?, ?> foreignDao; // cda: don't implement against implementation but interface (-> makes Dao customizable)
  protected MappedQueryForId<Object, Object> mappedQueryForId;

	private static final ThreadLocal<LevelCounters> threadLevelCounters = new ThreadLocal<LevelCounters>() {
		@Override
		protected LevelCounters initialValue() {
			return new LevelCounters();
		}
	};


  protected EntityConfig entityConfig;

  protected Field field;
  protected Method fieldGetMethod;
  protected Method fieldSetMethod;
  protected Class type;
  protected Class sqlType;

  protected DataType dataType;
  protected String fieldName;
  protected String columnName;

  protected String columnDefinition = null;
  protected int length = 255;

  protected boolean canBeNull = true;
  protected boolean unique = false;
  protected boolean insertable = true;
  protected boolean updatable = true;
  protected FetchType fetch = FetchType.EAGER;
  protected CascadeType[] cascade = new CascadeType[0];

  protected Boolean cascadePersist = null;
  protected Boolean cascadeRefresh = null;
  protected Boolean cascadeMerge = null; // TODO
  protected Boolean cascadeRemove = null;

  protected boolean isId;
  protected boolean isGeneratedId;
  protected GenerationType generatedIdType = GenerationType.AUTO;
  protected String generatedIdSequence;

  protected boolean isVersion = false;

  protected boolean isRelationshipProperty = false;
  protected boolean isOneCardinalityRelationshipProperty = false;
  protected boolean isManyCardinalityRelationshipProperty = false;
  protected boolean isOwningSide = false;
  protected boolean isInverseSide = false;
  protected boolean isJoinColumn = false;
  protected JoinTableConfig joinTable = null;
  protected boolean isBidirectional = false;
  protected boolean isForeignAutoCreate = false; // TODO: try to remove this value or implement other logic - but right now it's needed!

  protected Class targetEntityClass;
  protected EntityConfig targetEntityConfig;
  protected Property targetProperty;
  protected boolean typeIsACollection = false;
  protected PropertyConfig targetPropertyConfig = null;

  protected boolean isOneToOneField = false;
  protected OneToOneConfig oneToOneConfig;

  protected boolean isOneToManyField = false;
  protected boolean isManyToOneField = false;
  protected OneToManyConfig oneToManyConfig;

  protected boolean isManyToManyField = false;
  protected ManyToManyConfig manyToManyConfig;

  protected List<OrderBy> orderColumns = new ArrayList<>();


  public PropertyConfig(EntityConfig entityConfig, Property property) {
    this.entityConfig = entityConfig;
    this.connectionSource = entityConfig.getConnectionSource();
    this.field = property.getField();
    this.fieldGetMethod = property.getGetMethod();
    this.fieldSetMethod = property.getSetMethod();

    this.type = property.getType();
    this.typeIsACollection = Collection.class.isAssignableFrom(property.getType());

    this.fieldName = property.getFieldName();
    setColumnName(this.fieldName);
  }

  // for sub classes like DiscriminatorColumnConfig
  public PropertyConfig(EntityConfig entityConfig, String columnName) {
    this.entityConfig = entityConfig;

    this.fieldName = columnName;
    setColumnName(columnName);
  }

  /**
	 * You should use {@link com.j256.ormlite.instances.Instances#getFieldTypeCreator()} createFieldType} to instantiate one of these field if you have a {@link Field}.
	 */
	public PropertyConfig(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig,
                        Class<?> parentClass) throws SQLException {
		this.connectionSource = connectionSource;
		this.tableName = tableName;
		this.field = field;
    this.fieldConfig = fieldConfig;
		this.parentClass = parentClass;

    if(field != null) { // for Sub Classes like DiscriminatorColumnFieldType field can be null
      this.type = field.getType();
      if (fieldConfig.getForeignCollectionGenericType() != null)
        this.type = fieldConfig.getForeignCollectionGenericType();
    }
    this.fieldName = fieldConfig.getFieldName();
    setColumnName(fieldConfig.getColumnName());

    this.columnDefinition = fieldConfig.getColumnDefinition();
    this.length = fieldConfig.getWidth();

    this.canBeNull = fieldConfig.isCanBeNull();
    this.unique = fieldConfig.isUnique();
    this.insertable = fieldConfig.isInsertable();
    this.updatable = fieldConfig.isUpdatable();

    this.isId = fieldConfig.isId();
    this.isGeneratedId = fieldConfig.isGeneratedId();
    this.generatedIdSequence = fieldConfig.getGeneratedIdSequence();

    this.isVersion = fieldConfig.isVersion();

    this.isOneToOneField = fieldConfig.isOneToOneField();
    this.oneToOneConfig = fieldConfig.getOneToOneConfig();
    this.isOneToManyField = fieldConfig.isOneToManyField();
    this.oneToManyConfig = fieldConfig.getOneToManyConfig();
    this.isManyToManyField = fieldConfig.isManyToManyField();
    this.manyToManyConfig = fieldConfig.getManyToManyConfig();
    this.targetEntityClass = fieldConfig.getForeignCollectionGenericType();
    this.isForeignAutoCreate = fieldConfig.isForeignAutoCreate();

    setupFieldType(connectionSource, tableName, field, fieldConfig);
	}

  protected void setupFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig) throws SQLException {
    DatabaseType databaseType = connectionSource.getDatabaseType();

    // post process our config settings
    fieldConfig.postProcess();

    Class<?> clazz = field.getType();

    DataPersister dataPersister = setupDataPersister(field, fieldConfig, clazz);

    checkFieldRelationConfiguration(field, fieldConfig, clazz, dataPersister);

    checkFieldIdConfiguration(tableName, field, fieldConfig, databaseType);

    checkFieldUseGetSetConfiguration(field, fieldConfig);

    assignDataType(databaseType, dataPersister);

//    if(fieldConfig.isManyToManyField()) {
//      LevelCounters levelCounters = threadLevelCounters.get();
//      levelCounters.foreignCollectionLevelMax = 2;
//      fieldConfig.setForeignCollectionMaxEagerLevel(2);
//    }
  }

  protected DataPersister setupDataPersister(Field field, DatabaseFieldConfig fieldConfig, Class<?> clazz) throws SQLException {
    DataPersister dataPersister;
    if (fieldConfig.getDataPersister() == null) {
      Class<? extends DataPersister> persisterClass = fieldConfig.getPersisterClass();
      if (persisterClass == null || persisterClass == VoidType.class) {
        dataPersister = DataPersisterManager.lookupForField(field);
      } else {
        Method method;
        try {
          method = persisterClass.getDeclaredMethod("getSingleton");
        } catch (Exception e) {
          throw SqlExceptionUtil.create("Could not find getSingleton static method on class "
              + persisterClass, e);
        }
        Object result;
        try {
          result = method.invoke(null);
        } catch (InvocationTargetException e) {
          throw SqlExceptionUtil.create("Could not run getSingleton method on class " + persisterClass,
              e.getTargetException());
        } catch (Exception e) {
          throw SqlExceptionUtil.create("Could not run getSingleton method on class " + persisterClass, e);
        }
        if (result == null) {
          throw new SQLException("Static getSingleton method should not return null on class "
              + persisterClass);
        }
        try {
          dataPersister = (DataPersister) result;
        } catch (Exception e) {
          throw SqlExceptionUtil.create(
              "Could not cast result of static getSingleton method to DataPersister from class "
                  + persisterClass, e);
        }
      }
    } else {
      dataPersister = fieldConfig.getDataPersister();
      if (!dataPersister.isValidForField(field)) {
        StringBuilder sb = new StringBuilder();
        sb.append("Field class ").append(clazz.getName());
        sb.append(" for field ").append(this);
        sb.append(" is not valid for type ").append(dataPersister);
        Class<?> primaryClass = dataPersister.getPrimaryClass();
        if (primaryClass != null) {
          sb.append(", maybe should be " + primaryClass);
        }
        throw new IllegalArgumentException(sb.toString());
      }
    }
    return dataPersister;
  }

  protected void checkFieldRelationConfiguration(Field field, DatabaseFieldConfig fieldConfig, Class<?> clazz, DataPersister dataPersister) throws SQLException {
    String foreignColumnName = fieldConfig.getForeignColumnName();
    String defaultFieldName = field.getName();

    if (fieldConfig.isForeign() || fieldConfig.isForeignAutoRefresh() || foreignColumnName != null) {
      if (dataPersister != null && dataPersister.isPrimitive()) {
        throw new IllegalArgumentException("Field " + this + " is a primitive class " + clazz
            + " but marked as foreign");
      }
      if (foreignColumnName == null) {
        defaultFieldName = defaultFieldName + FOREIGN_ID_FIELD_SUFFIX;
      } else {
        defaultFieldName = defaultFieldName + "_" + foreignColumnName;
      }
      if (Instances.getFieldTypeCreator().foreignCollectionCanBeAssignedToField(clazz)) {
        throw new SQLException("Field '" + field.getName() + "' in class " + clazz + "' should use the @"
            + ForeignCollectionField.class.getSimpleName() + " annotation not foreign=true");
      }
    }
    else if (fieldConfig.isForeignCollection()) {
      if (Collection.class.isAssignableFrom(clazz) == false && Instances.getFieldTypeCreator().foreignCollectionCanBeAssignedToField(clazz) == false) {
        throw new SQLException("Field class for '" + field.getName() + "' must be of class "
            + ForeignCollection.class.getSimpleName() + " or Collection.");
      }
      checkIfForeignCollectionTargetClassHasBeenFound(field, fieldConfig);
    } else if (dataPersister == null && (!fieldConfig.isForeignCollection())) {
      if (byte[].class.isAssignableFrom(clazz)) {
        throw new SQLException("ORMLite does not know how to store " + clazz + " for field '" + field.getName()
            + "'. byte[] fields must specify dataType=DataType.BYTE_ARRAY or SERIALIZABLE");
      } else if (Serializable.class.isAssignableFrom(clazz)) {
        throw new SQLException("ORMLite does not know how to store " + clazz + " for field '" + field.getName()
            + "'.  Use another class, custom persister, or to serialize it use "
            + "dataType=DataType.SERIALIZABLE");
      } else {
        throw new IllegalArgumentException("ORMLite does not know how to store " + clazz + " for field "
            + field.getName() + ". Use another class or a custom persister.");
      }
    }

    if (fieldConfig.getColumnName() == null) {
      setColumnName(defaultFieldName);
    } else {
      setColumnName(fieldConfig.getColumnName());
    }


    if (fieldConfig.isForeignAutoRefresh() && !fieldConfig.isForeign()) {
      throw new IllegalArgumentException("Field " + field.getName()
          + " must have foreign = true if foreignAutoRefresh = true");
    }
    if (fieldConfig.isForeignAutoCreate() && !fieldConfig.isForeign()) {
      throw new IllegalArgumentException("Field " + field.getName()
          + " must have foreign = true if foreignAutoCreate = true");
    }
    if (fieldConfig.getForeignColumnName() != null && !fieldConfig.isForeign()) {
      throw new IllegalArgumentException("Field " + field.getName()
          + " must have foreign = true if foreignColumnName is set");
    }
    if (fieldConfig.isVersion() && (dataPersister == null || !dataPersister.isValidForVersion())) {
      throw new IllegalArgumentException("Field " + field.getName()
          + " is not a valid type to be a version field");
    }
    if (fieldConfig.getMaxForeignAutoRefreshLevel() != DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED
        && !fieldConfig.isForeignAutoRefresh()) {
      throw new IllegalArgumentException("Field " + field.getName() + " has maxForeignAutoRefreshLevel set ("
          + fieldConfig.getMaxForeignAutoRefreshLevel() + ") but foreignAutoRefresh is false");
    }
  }

  protected void checkIfForeignCollectionTargetClassHasBeenFound(Field field, DatabaseFieldConfig fieldConfig) throws SQLException {
    if(fieldConfig.getForeignCollectionGenericType() != null)
      return;

    Type type = field.getGenericType();
    if (!(type instanceof ParameterizedType)) {
      throw new SQLException("Field class for '" + field.getName() + "' must be a parameterized Collection.");
    }
    Type[] genericArguments = ((ParameterizedType) type).getActualTypeArguments();
    if (genericArguments.length == 0) {
      // i doubt this will ever be reached
      throw new SQLException("Field class for '" + field.getName()
          + "' must be a parameterized Collection with at least 1 type.");
    }
  }

  protected void checkFieldIdConfiguration(String tableName, Field field, DatabaseFieldConfig fieldConfig, DatabaseType databaseType) {
    if (fieldConfig.isId()) {
      if (fieldConfig.isGeneratedId() || fieldConfig.getGeneratedIdSequence() != null) {
        throw new IllegalArgumentException("Must specify one of id, generatedId, and generatedIdSequence with "
            + field.getName());
      }
      this.isId = true;
      this.isGeneratedId = false;
      this.generatedIdSequence = null;
    } else if (fieldConfig.isGeneratedId()) {
      if (fieldConfig.getGeneratedIdSequence() != null) {
        throw new IllegalArgumentException("Must specify one of id, generatedId, and generatedIdSequence with "
            + field.getName());
      }
      this.isId = true;
      this.isGeneratedId = true;
      if (databaseType.isIdSequenceNeeded()) {
        this.generatedIdSequence = databaseType.generateIdSequenceName(tableName, this);
      } else {
        this.generatedIdSequence = null;
      }
    } else if (fieldConfig.getGeneratedIdSequence() != null) {
      this.isId = true;
      this.isGeneratedId = true;
      String seqName = fieldConfig.getGeneratedIdSequence();
      if (databaseType.isEntityNamesMustBeUpCase()) {
        seqName = seqName.toUpperCase();
      }
      this.generatedIdSequence = seqName;
    } else {
      this.isId = false;
      this.isGeneratedId = false;
      this.generatedIdSequence = null;
    }

    if (this.isId && (fieldConfig.isForeign() || fieldConfig.isForeignAutoRefresh())) {
      throw new IllegalArgumentException("Id field " + field.getName() + " cannot also be a foreign object");
    }
    if (fieldConfig.isAllowGeneratedIdInsert() && !fieldConfig.isGeneratedId()) {
      throw new IllegalArgumentException("Field " + field.getName()
          + " must be a generated-id if allowGeneratedIdInsert = true");
    }
  }

  protected void checkFieldUseGetSetConfiguration(Field field, DatabaseFieldConfig fieldConfig) {
    if (fieldConfig.isUseGetSet()) {
      this.fieldGetMethod = DatabaseFieldConfig.findGetMethod(field, true);
      this.fieldSetMethod = DatabaseFieldConfig.findSetMethod(field, true);
    } else {
      if (!field.isAccessible()) {
        try {
          this.field.setAccessible(true);
        } catch (SecurityException e) {
          throw new IllegalArgumentException("Could not open access to field " + field.getName()
              + ".  You may have to set useGetSet=true to fix.");
        }
      }
      this.fieldGetMethod = null;
      this.fieldSetMethod = null;
    }
  }

  /**
	 * Because we go recursive in a lot of situations if we construct DAOs inside of the FieldType constructor, we have
	 * to do this 2nd pass initialization so we can better use the DAO caches.
	 * 
	 * @see BaseDaoImpl#initialize()
	 */
	public void configDaoInformation(ConnectionSource connectionSource, Class<?> parentClass) throws SQLException {
		if(isJavaxPersistenceRelationOneToOneField()) {
			configOneToOneField();
		}
    else if(isOrmLiteRelationOneToOneField()) {
      configOneToOneField();
    }
    else if(isRelationManyToOneField()) {
			configRelationManyToOneField();
		}
    else if(isRelationOneToManyField()) {
			configRelationOneToManyField();
		}
    else if(fieldConfig.isManyToManyField())
      configManyToManyField();
    else {
			foreignEntityConfig = null;
			foreignIdField = null;
			foreignPropertyConfig = null;
			foreignDao = null;
			mappedQueryForId = null;
		}

		// we have to do this because if we have a foreign field then our id type might have gone to an _id primitive
		if (this.foreignIdField != null) {
			assignDataType(connectionSource.getDatabaseType(), this.foreignIdField.getDataPersister());
		}
	}

  protected boolean isJavaxPersistenceRelationOneToOneField() {
    return isOneToOneField();
  }

  protected boolean isOrmLiteRelationOneToOneField() {
    return (fieldConfig.isForeignAutoRefresh() || fieldConfig.getForeignColumnName() != null) && fieldConfig.isManyToOneField() == false && fieldConfig.isOneToOneField() == false;
  }

  public void configOneToOneField() throws SQLException {
    Class<?> fieldClass = field.getType();
    String foreignColumnName = fieldConfig.getForeignColumnName();

    DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
    if (tableConfig == null) {
      // NOTE: the cast is necessary for maven
      foreignDao = Instances.getDaoManager().createDao(connectionSource, fieldClass); // cda
      foreignEntityConfig = foreignDao.getEntityConfig();
    } else {
      tableConfig.extractFieldTypes(connectionSource);
      // NOTE: the cast is necessary for maven
      foreignDao = Instances.getDaoManager().createDao(connectionSource, tableConfig); // cda
      foreignEntityConfig = foreignDao.getEntityConfig();
    }
    foreignDao.setObjectCache(true); // TODO: don't know how to solve it otherwise right now - implement global boolean cacheObjects flag?

    if (foreignColumnName == null) {
      foreignIdField = foreignEntityConfig.getIdProperty();
      if (foreignIdField == null) {
        throw new IllegalArgumentException("Foreign field " + fieldClass + " does not have id field");
      }
    } else {
      foreignIdField = foreignEntityConfig.getFieldTypeByColumnName(foreignColumnName);
      if (foreignIdField == null) {
        throw new IllegalArgumentException("Foreign field " + fieldClass + " does not have field named '"
            + foreignColumnName + "'");
      }
    }
    @SuppressWarnings("unchecked")
    MappedQueryForId<Object, Object> castMappedQueryForId =
        (MappedQueryForId<Object, Object>) MappedQueryForId.build(connectionSource.getDatabaseType(), foreignEntityConfig,
            foreignIdField);
    mappedQueryForId = castMappedQueryForId;
    foreignPropertyConfig = null;
  }

  protected boolean isRelationManyToOneField() {
    return (isForeign() /*&& foreignDao != null*/) || isManyToOneField();
  }

  protected void configRelationManyToOneField() throws SQLException {
    Class<?> fieldClass = field.getType();

    // cda: i just don't know what to do with this check, as for a foreign relation foreign id has to be persisted - and that's an int, long, ...
//    if (this.dataPersister != null && this.dataPersister.isPrimitive()) {
//      throw new IllegalArgumentException("Field " + this + " is a primitive class " + fieldClass
//          + " but marked as foreign");
//    }
    DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();
    if (tableConfig != null) {
      tableConfig.extractFieldTypes(connectionSource);
      // NOTE: the cast is necessary for maven
      foreignDao = Instances.getDaoManager().createDao(connectionSource, tableConfig); // cda
    } else {
				/*
				 * Initially we were only doing this just for BaseDaoEnabled.class and isForeignAutoCreate(). But we
				 * need it also for foreign fields because the alternative was to use reflection. Chances are if it is
				 * foreign we're going to need the DAO in the future anyway so we might as well create it. This also
				 * allows us to make use of any table configs.
				 */
      // NOTE: the cast is necessary for maven
      foreignDao = Instances.getDaoManager().createDao(connectionSource, fieldClass); // cda
    }
    foreignDao.setObjectCache(true); // TODO: don't know how to solve it otherwise right now - implement global boolean cacheObjects flag?

    foreignEntityConfig = foreignDao.getEntityConfig();
    foreignIdField = foreignEntityConfig.getIdProperty();
    if (foreignIdField == null) {
      throw new IllegalArgumentException("Foreign field " + fieldClass + " does not have id field");
    }
    if (isForeignAutoCreate() && !foreignIdField.isGeneratedId()) {
      throw new IllegalArgumentException("Field " + field.getName()
          + ", if foreignAutoCreate = true then class " + fieldClass.getSimpleName()
          + " must have id field with generatedId = true");
    }

    if(getOneToManyConfig() != null)
      foreignPropertyConfig = findManyToOneForeignField(foreignDao, field, getOneToManyConfig());
    mappedQueryForId = null;
  }

  protected PropertyConfig findManyToOneForeignField(Dao<?, ?> foreignDao, Field field, OneToManyConfig oneToManyConfig) {
    for(PropertyConfig foreignPropertyConfig : foreignDao.getEntityConfig().getPropertyConfigs()) {
      if(foreignPropertyConfig.getField().equals(oneToManyConfig.getOneSideField()))
        return foreignPropertyConfig;
    }

    return null;
  }

  protected boolean isRelationOneToManyField() {
    return fieldConfig.isForeignCollection() && fieldConfig.isManyToManyField() == false;
  }

  protected void configRelationOneToManyField() throws SQLException {
    Class<?> fieldClass = field.getType();

    if (Collection.class.isAssignableFrom(fieldClass) == false && Instances.getFieldTypeCreator().foreignCollectionCanBeAssignedToField(fieldClass) == false) {
      throw new SQLException("Field class for '" + field.getName() + "' must be of class "
          + ForeignCollection.class.getSimpleName() + " or Collection.");
    }

    Class<?> collectionClazz = null;

    if(fieldConfig.getForeignCollectionGenericType() != null)
      collectionClazz = fieldConfig.getForeignCollectionGenericType();
    else {
      Type type = field.getGenericType();
      if (!(type instanceof ParameterizedType)) {
        throw new SQLException("Field class for '" + field.getName() + "' must be a parameterized Collection.");
      }

      Type[] genericArguments = ((ParameterizedType) type).getActualTypeArguments();
      if (genericArguments.length == 0) {
        // i doubt this will ever be reached
        throw new SQLException("Field class for '" + field.getName()
            + "' must be a parameterized Collection with at least 1 type.");
      }

      // If argument is a type variable we need to get arguments from superclass
      if (genericArguments[0] instanceof TypeVariable) {
        genericArguments = ((ParameterizedType) parentClass.getGenericSuperclass()).getActualTypeArguments();
      }

      if (!(genericArguments[0] instanceof Class)) {
        throw new SQLException("Field class for '" + field.getName()
            + "' must be a parameterized Collection whose generic argument is an entity class not: "
            + genericArguments[0]);
      }

      collectionClazz = (Class<?>) genericArguments[0];
    }

    DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();

    if (tableConfig == null) {
      foreignDao = Instances.getDaoManager().createDao(connectionSource, collectionClazz); // cda: don't implement against implementation but interface
    }
    else {
      foreignDao = Instances.getDaoManager().createDao(connectionSource, tableConfig); // cda: don't implement against implementation but interface
    }
    foreignDao.setObjectCache(true); // TODO: don't know how to solve it otherwise right now - implement global boolean cacheObjects flag?

    if(getOneToManyConfig() != null) {
      for (PropertyConfig propertyConfig : foreignDao.getEntityConfig().getJoinColumns()) {
        if(propertyConfig.getField().equals(getOneToManyConfig().getManySideField())) {
          foreignPropertyConfig = propertyConfig;
          break;
        }
      }
    }
    else
      foreignPropertyConfig = findForeignFieldType(collectionClazz, parentClass, foreignDao);
    foreignIdField = null;
    foreignEntityConfig = null;
    mappedQueryForId = null;
  }

  protected void configManyToManyField() throws SQLException {
    Class collectionClazz = fieldConfig.getManyToManyConfig().getOtherSideClass(field);
    DatabaseTableConfig<?> tableConfig = fieldConfig.getForeignTableConfig();

    if (tableConfig == null) {
      foreignDao = Instances.getDaoManager().createDao(connectionSource, collectionClazz); // cda: don't implement against implementation but interface
    }
    else {
      foreignDao = Instances.getDaoManager().createDao(connectionSource, tableConfig); // cda: don't implement against implementation but interface
    }
    foreignDao.setObjectCache(true); // TODO: don't know how to solve it otherwise right now - implement global boolean cacheObjects flag?

    if(getManyToManyConfig() != null) {
      Field otherSideField = getManyToManyConfig().getOtherSideField(getField());
      for (PropertyConfig propertyConfig : foreignDao.getEntityConfig().getJoinTableProperties()) {
        if(propertyConfig.getField().equals(otherSideField)) {
          foreignPropertyConfig = propertyConfig;
          break;
        }
      }
    }
    else
      foreignPropertyConfig = findForeignFieldType(collectionClazz, parentClass, foreignDao);
    foreignIdField = null;
    foreignEntityConfig = null;
    mappedQueryForId = null;
  }


  public EntityConfig getEntityConfig() {
    return entityConfig;
  }

  public Field getField() {
		return field;
	}

  public Class getType() {
    return type;
  }

  public boolean isTypeIsACollection() {
    return typeIsACollection;
  }

  // TODO: try to keep consistency with fieldConverter
  public SqlType getSqlTypeOfFieldConverter() {
    return getFieldConverter().getSqlType();
  }
  public Class getSqlType() {
    return sqlType;
  }

  public void setSqlType(Class sqlType) {
    this.sqlType = sqlType;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
    if(dataType != null) {
      setDataPersister(dataType.getDataPersister());
      this.fieldConverter = entityConfig.getConnectionSource().getDatabaseType().getFieldConverter(dataPersister, this);
    }
  }

  public DataPersister getDataPersister() {
    if(dataPersister == null && dataType != null)
      dataPersister = dataType.getDataPersister();
    if(dataPersister == null && getTargetEntityConfig() != null && getTargetEntityConfig().getIdProperty() != null) // TODO: remove last check as id field has always to be set
      dataPersister = getTargetEntityConfig().getIdProperty().getDataPersister();
//    if(dataPersister == null) // TODO: remove
//      dataPersister = DataPersisterManager.lookupForField(field);
    return dataPersister;
  }

  public void setDataPersister(DataPersister dataPersister) {
    this.dataPersister = dataPersister;

    try {
      this.dataTypeConfigObj = dataPersister.makeConfigObject(this);
    } catch(Exception ex) {
      log.error("Could not make Config Object for Property " + this + " from dataPersister " + dataPersister);
    }
  }

  public FieldConverter getFieldConverter() {
    if(fieldConverter == null) {
      if(getDataPersister() != null)
        this.fieldConverter = entityConfig.getConnectionSource().getDatabaseType().getFieldConverter(dataPersister, this);
      else if(fieldConverter == null && getTargetEntityConfig() != null && getTargetEntityConfig().getIdProperty() != null) // TODO: remove last check as id field has always to be set
        fieldConverter = getTargetEntityConfig().getIdProperty().getDataPersister();
      else
        this.fieldConverter = entityConfig.getConnectionSource().getDatabaseType().getFieldConverter(getDataPersister(), this);
    }
    return fieldConverter;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    if(entityConfig.getDatabaseType().isEntityNamesMustBeUpCase())
      this.columnName = columnName.toUpperCase();
    else
      this.columnName = columnName;
  }

  /**
   * Call through to {@link DatabaseFieldConfig#getColumnDefinition()}
   */
//  public String getColumnDefinition() {
//    return fieldConfig.getColumnDefinition();
//  }

  public String getColumnDefinition() {
    return columnDefinition;
  }

  public void setColumnDefinition(String columnDefinition) {
    this.columnDefinition = columnDefinition;
  }

//  public int getWidth() {
//    return fieldConfig.getWidth();
//  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }


//  public boolean isCanBeNull() {
//    return fieldConfig.isCanBeNull();
//  }

  public boolean canBeNull() {
    return canBeNull;
  }

  public void setCanBeNull(boolean canBeNull) {
    this.canBeNull = canBeNull;
  }

//  public boolean isUnique() {
//    return fieldConfig.isUnique();
//  }
//
//  public boolean isInsertable() {
//    return fieldConfig.isInsertable();
//  }
//
//  public boolean isUpdatable() {
//    return fieldConfig.isUpdatable();
//  }


  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isInsertable() {
    return insertable;
  }

  public void setInsertable(boolean insertable) {
    this.insertable = insertable;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }


  /*      Relation properties configuration   */

  public boolean isRelationshipProperty() {
    return isRelationshipProperty;
  }

  public void setIsRelationshipProperty(boolean isRelationProperty) {
    this.isRelationshipProperty = isRelationProperty;
//    this.isForeignAutoCreate = true; // TODO: try to remove / implement better logic
  }

  public boolean isOneCardinalityRelationshipProperty() {
    return isOneCardinalityRelationshipProperty;
  }

  public void setIsOneCardinalityRelationshipProperty(boolean isOneCardinalityRelationshipProperty) {
    this.isOneCardinalityRelationshipProperty = isOneCardinalityRelationshipProperty;
  }

  public boolean isManyCardinalityRelationshipProperty() {
    return isManyCardinalityRelationshipProperty;
  }

  public void setIsManyCardinalityRelationshipProperty(boolean isManyCardinalityRelationshipProperty) {
    this.isManyCardinalityRelationshipProperty = isManyCardinalityRelationshipProperty;
  }

  /**
   * Owning Side is that side of a relationship that has the Join Column.
   * @return
   */
  public boolean isOwningSide() {
    return isOwningSide;
  }

  public void setIsOwningSide(boolean isOwningSide) {
    this.isOwningSide = isOwningSide;
  }

  public boolean isInverseSide() {
    return isInverseSide;
  }

  public void setIsInverseSide(boolean isInverseSide) {
    this.isInverseSide = isInverseSide;
  }

  public boolean isJoinColumn() {
    return isJoinColumn;
  }

  public void setIsJoinColumn(boolean isJoinColumn) {
    this.isJoinColumn = isJoinColumn;
    this.entityConfig.addJoinColumn(this);
  }

  public JoinTableConfig getJoinTable() {
    if(joinTable == null && isInverseSide && getTargetPropertyConfig() != null)
      this.joinTable = getTargetPropertyConfig().getJoinTable();
    return joinTable;
  }

  public void setJoinTable(JoinTableConfig joinTable) {
    this.joinTable = joinTable;
  }

  public boolean isBidirectional() {
    return isBidirectional;
  }

  public void setIsBidirectional(boolean isBidirectional) {
    this.isBidirectional = isBidirectional;
  }

  public Class getTargetEntityClass() {
    return targetEntityClass;
  }

  public void setTargetEntityClass(Class targetEntityClass) {
    this.targetEntityClass = targetEntityClass;
  }

  public EntityConfig getTargetEntityConfig() {
    if(targetEntityConfig == null && targetEntityClass != null)
      targetEntityConfig = Registry.getEntityRegistry().getEntityConfiguration(targetEntityClass);
    return targetEntityConfig;
  }

  public void setTargetEntityConfig(EntityConfig targetEntityConfig) {
    this.targetEntityConfig = targetEntityConfig;
  }

  public Property getTargetProperty() {
    return targetProperty;
  }

  public void setTargetProperty(Property targetProperty) {
    this.targetProperty = targetProperty;
  }

  public PropertyConfig getTargetPropertyConfig() {
    if(targetPropertyConfig == null && targetProperty != null)
      targetPropertyConfig = Registry.getPropertyRegistry().getPropertyConfiguration(targetEntityClass, targetProperty);
    return targetPropertyConfig;
  }

  public void setTargetPropertyConfig(PropertyConfig targetPropertyConfig) {
    this.targetPropertyConfig = targetPropertyConfig;
  }

  public boolean isOneToOneField() {
    return isOneToOneField;
  }

  public void setIsOneToOneField(boolean isOneToOneField) {
    this.isOneToOneField = isOneToOneField;
  }

  public OneToOneConfig getOneToOneConfig() {
    return oneToOneConfig;
  }

  public void setOneToOneConfig(OneToOneConfig oneToOneConfig) {
    this.oneToOneConfig = oneToOneConfig;
  }

  public boolean isOneToManyField() {
    return isOneToManyField;
  }

  public void setIsOneToManyField(boolean isOneToManyField) {
    this.isOneToManyField = isOneToManyField;
  }

  public boolean isManyToOneField() {
    return isManyToOneField;
  }

  public void setIsManyToOneField(boolean isManyToOneField) {
    this.isManyToOneField = isManyToOneField;
  }

  public OneToManyConfig getOneToManyConfig() {
    return oneToManyConfig;
  }

  public void setOneToManyConfig(OneToManyConfig oneToManyConfig) {
    this.oneToManyConfig = oneToManyConfig;
  }

  public boolean isManyToManyField() {
    return isManyToManyField;
  }

  public void setIsManyToManyField(boolean isManyToManyField) {
    this.isManyToManyField = isManyToManyField;
  }

  public ManyToManyConfig getManyToManyConfig() {
    return manyToManyConfig;
  }

  public void setManyToManyConfig(ManyToManyConfig manyToManyConfig) {
    this.manyToManyConfig = manyToManyConfig;
  }

  public boolean hasOrderColumns() {
    return getOrderColumns().size() > 0;
  }

  public List<OrderBy> getOrderColumns() {
    return orderColumns;
  }

  public FetchType getFetch() {
    return fetch;
  }

  public void setFetch(FetchType fetch) {
    this.fetch = fetch;
  }

  public CascadeType[] getCascade() {
    return cascade;
  }

  public void setCascade(CascadeType[] cascade) {
    this.cascade = cascade;
  }



  public String getTableName() {
		return tableName;
	}

//	public String getFieldName() {
//		return field.getName();
//	}

	/**
	 * Return the class of the field associated with this field type.
	 */
//	public Class<?> getType() {
//		return field.getType();
//	}

	public Object getDataTypeConfigObj() {
    if(dataTypeConfigObj == null && getDataPersister() != null)
      try { this.dataTypeConfigObj = getDataPersister().makeConfigObject(this); } catch(Exception ex) { log.error("Could not create dataTypeConfigObj", ex); }
		return dataTypeConfigObj;
	}

	/**
	 * Return the default value as parsed from the {@link DatabaseFieldConfig#getDefaultValue()}.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Return whether the field is an id field. It is an id if {@link com.j256.ormlite.field.DatabaseField#id},
	 * {@link com.j256.ormlite.field.DatabaseField#generatedId}, OR {@link com.j256.ormlite.field.DatabaseField#generatedIdSequence} are enabled.
	 */
	public boolean isId() {
		return isId;
	}

  public void setIsId(boolean isId) {
    this.isId = isId;
  }

  /**
	 * Return whether the field is a generated-id field. This is true if {@link com.j256.ormlite.field.DatabaseField#generatedId} OR
	 * {@link com.j256.ormlite.field.DatabaseField#generatedIdSequence} are enabled.
	 */
	public boolean isGeneratedId() {
		return isGeneratedId;
	}

  public void setIsGeneratedId(boolean isGeneratedId) {
    this.isGeneratedId = isGeneratedId;
  }

  public GenerationType getGeneratedIdType() {
    return generatedIdType;
  }

  public void setGeneratedIdType(GenerationType generatedIdType) {
    this.generatedIdType = generatedIdType;
  }

  /**
	 * Return whether the field is a generated-id-sequence field. This is true if
	 * {@link com.j256.ormlite.field.DatabaseField#generatedIdSequence} is specified OR if {@link com.j256.ormlite.field.DatabaseField#generatedId} is enabled and the
	 * {@link DatabaseType#isIdSequenceNeeded} is enabled. If the latter is true then the sequence name will be
	 * auto-generated.
	 */
	public boolean isGeneratedIdSequence() {
		return generatedIdSequence != null;
	}

  /**
   * Call through to {@link DataPersister#isSelfGeneratedId()}
   */
  public boolean isSelfGeneratedId() {
    return getDataPersister().isSelfGeneratedId();
  }

  /**
   * Call through to {@link DatabaseFieldConfig#isAllowGeneratedIdInsert()}
   */
  public boolean isAllowGeneratedIdInsert() {
    return fieldConfig != null && fieldConfig.isAllowGeneratedIdInsert();
  }

  /**
   * Call through to {@link DataPersister#generateId()}
   */
  public Object generateId() {
    return dataPersister.generateId();
  }

	/**
	 * Return the generated-id-sequence associated with the field or null if {@link #isGeneratedIdSequence} is false.
	 */
	public String getGeneratedIdSequence() {
		return generatedIdSequence;
	}

  /**
   * Call through to {@link DatabaseFieldConfig#isVersion()}
   */
//  public boolean isVersion() {
//    return fieldConfig.isVersion();
//  }


  public boolean isVersion() {
    return isVersion;
  }

  public void setIsVersion(boolean isVersion) {
    this.isVersion = isVersion;
  }

  public void setOrderColumns(List<OrderBy> orderColumns) {
    this.orderColumns = orderColumns;
  }

  public boolean isForeign() {
		return isOneToOneField || isManyToOneField || (fieldConfig != null && fieldConfig.isForeign());
	}

	/**
	 * Assign to the data object the val corresponding to the fieldType.
	 */
	public void assignField(Object data, Object val, boolean parentObject, ObjectCache objectCache) throws SQLException {
		// if this is a foreign object then val is the foreign object's id val
//    if (getForeignIdField() != null && val != null) {
    // TODO: is this good to sort out null values?
		if (val != null && getForeignIdField() != null && typeIsACollection == false) { // TODO: find a better way to determine target side is a Collection or not
			// get the current field value which is the foreign-id
			Object foreignId = extractJavaFieldValue(data);
			/*
			 * See if we don't need to create a new foreign object. If we are refreshing and the id field has not
			 * changed then there is no need to create a new foreign object and maybe lose previously refreshed field
			 * information.
			 */
			if (foreignId != null && foreignId.equals(val)) {
				return;
			}
			// awhitlock: raised as OrmLite issue: bug #122
			Object cachedVal;
			ObjectCache foreignCache = getForeignDao().getObjectCache();
			if (foreignCache == null) {
				cachedVal = null;
			} else {
				cachedVal = foreignCache.get(getType(), val);
			}
			if (cachedVal != null) {
				val = cachedVal;
			} else if (!parentObject) {
				Object foreignObject;
				LevelCounters levelCounters = threadLevelCounters.get();
				// we record the current auto-refresh level which will be used along the way
				if (levelCounters.autoRefreshLevel == 0) {
//					levelCounters.autoRefreshLevelMax = fieldConfig.getMaxForeignAutoRefreshLevel();
          levelCounters.autoRefreshLevelMax = NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED;
				}
				// if we have recursed the proper number of times, return a shell with just the id set
				if (levelCounters.autoRefreshLevel >= levelCounters.autoRefreshLevelMax) {
					// create a shell and assign its id field
					foreignObject = getForeignDao().getEntityConfig().createObject();
					foreignIdField.assignField(foreignObject, val, false, objectCache);
				} else {
					/*
					 * We may not have a mapped query for id because we aren't auto-refreshing ourselves. But a parent
					 * class may be auto-refreshing us with a level > 1 so we may need to build out query-for-id
					 * optimization on the fly here.
					 */
					if (mappedQueryForId == null) {
            if(val != null && isRelationshipProperty && getForeignDao().getEntityConfig().getInheritance() != null) {
              createMappedQueryForIdForInheritanceTables((InheritanceEntityConfig)getForeignDao().getEntityConfig().getInheritanceTopLevelEntityConfig(), val, objectCache);
            }
            else {
              @SuppressWarnings("unchecked")
              MappedQueryForId<Object, Object> castMappedQueryForId =
                  (MappedQueryForId<Object, Object>) MappedQueryForId.build(
                      connectionSource.getDatabaseType(),
                      getForeignDao().getEntityConfig(), foreignIdField);
              mappedQueryForId = castMappedQueryForId;
            }
					}
					levelCounters.autoRefreshLevel++;

					try {
						DatabaseConnection databaseConnection = connectionSource.getReadOnlyConnection();
						try {
							// recurse and get the sub-object
							foreignObject = mappedQueryForId.execute(databaseConnection, val, objectCache);
						} finally {
							connectionSource.releaseConnection(databaseConnection);
						}
					} finally {
						levelCounters.autoRefreshLevel--;
						if (levelCounters.autoRefreshLevel <= 0) {
							threadLevelCounters.remove();
						}
					}
				}
				// the value we are to assign to our field is now the foreign object itself
				val = foreignObject;
			}
		}

		if (getEntityConfig().getAccess() == AccessType.PROPERTY && fieldSetMethod != null) {
      try {
        fieldSetMethod.invoke(data, val);
      } catch (Exception e) {
        log.error("Could not set field value of Property " + this + " to " + val);
        throw SqlExceptionUtil.create("Could not call " + fieldSetMethod + " on object with '" + val + "' for "
            + this, e);
      }
		} else {
      try {
        field.set(data, val);
      } catch (IllegalArgumentException e) {
//				throw SqlExceptionUtil.create("Could not assign object '" + val + "' of type " + val.getClass()
//						+ " to field " + this, e);
//        log.error("Could not assign object '" + val + "' of type " + val.getClass()
//            + " to field " + this, e);
      } catch (IllegalAccessException e) {
        log.error("Could not set field value of Property " + this + " to " + val);
        throw SqlExceptionUtil.create("Could not assign object '" + val + "' of type " + val.getClass()
            + "' to field " + this, e);
      }
		}
	}

  protected void createMappedQueryForIdForInheritanceTables(InheritanceEntityConfig inheritanceConfig, Object val, ObjectCache objectCache) throws SQLException {
    String query = "SELECT " + inheritanceConfig.getDiscriminatorPropertyConfig().getColumnName() +
        " FROM " + inheritanceConfig.getTableName() + " WHERE " + inheritanceConfig.getIdProperty().getColumnName() + "=?";
    StatementExecutor executor = new StatementExecutor(connectionSource.getDatabaseType(), inheritanceConfig, inheritanceConfig.getDao());

    String discriminatorValue = null;
    GenericRawResults<String[]> rawResults = executor.queryRaw(connectionSource, query, new String[]{val.toString()}, objectCache);
    String[] debug = rawResults.getColumnNames();
    List<String[]> results = rawResults.getResults();
    if(results.size() > 0) { // should actually contain only one row // TODO: check
      String[] row = results.get(0);
      if (row.length > 0) { // should contain actually only one column // TODO: check
        discriminatorValue = row[0];
      }
    }

    if(discriminatorValue != null) { // TODO: throw Exception if not found
      EntityConfig leafEntityConfig = inheritanceConfig.getEntityForDiscriminatorValue(discriminatorValue);

      MappedQueryForId<Object, Object> castMappedQueryForId =
          (MappedQueryForId<Object, Object>) MappedQueryForId.build(
              connectionSource.getDatabaseType(),
              leafEntityConfig, foreignIdField);
      mappedQueryForId = castMappedQueryForId;
    }
  }

  /**
	 * Assign an ID value to this field.
	 */
	public Object assignIdValue(Object data, Number val, ObjectCache objectCache) throws SQLException {
		Object idVal = dataPersister.convertIdNumber(val);
		if (idVal == null) {
			throw new SQLException("Invalid class " + dataPersister + " for sequence-id " + this);
		} else {
			assignField(data, idVal, false, objectCache);
			return idVal;
		}
	}

	/**
	 * Return the value from the field in the object that is defined by this FieldType.
	 */
	public <FV> FV extractRawJavaFieldValue(Object object) throws SQLException {
		Object val;
		if (entityConfig.getAccess() == AccessType.PROPERTY && fieldGetMethod != null) {
      try {
        val = fieldGetMethod.invoke(object);
      } catch (Exception e) {
        log.error("Could not extract field value for Property " + this + " on Object " + object);
        throw SqlExceptionUtil.create("Could not call " + fieldGetMethod + " for " + this, e);
      }
		} else {
      try {
        // field object may not be a T yet
        val = field.get(object);
      } catch (Exception e) {
        log.error("Could not extract field value for Property " + this + " on Object " + object, e);
        throw SqlExceptionUtil.create("Could not get field value for " + this, e);
      }
		}

//    if(val != null && this.isRelationshipProperty && typeIsACollection == false) {
//      if(getTargetEntityConfig() != null && getTargetEntityConfig().getIdProperty() != null)
//        return getTargetEntityConfig().getIdProperty().extractRawJavaFieldValue(val);
//    }

		@SuppressWarnings("unchecked")
		FV converted = (FV) val;
		return converted;
	}

	/**
	 * Return the value from the field in the object that is defined by this FieldType. If the field is a foreign object
	 * then the ID of the field is returned instead.
	 */
	public Object extractJavaFieldValue(Object object) throws SQLException {

		Object val = extractRawJavaFieldValue(object);

		// if this is a foreign object then we want its id field
//    if (foreignIdField != null && val != null) {
//		if (val != null && isRelationshipProperty == true && typeIsACollection == false) {
//			val = getForeignIdField().extractRawJavaFieldValue(val);
//		}
    if(val != null && this.isRelationshipProperty && typeIsACollection == false) {
      if(getTargetEntityConfig() != null && getTargetEntityConfig().getIdProperty() != null)
        return getTargetEntityConfig().getIdProperty().extractRawJavaFieldValue(val);
    }

		return val;
	}

	/**
	 * Extract a field from an object and convert to something suitable to be passed to SQL as an argument.
	 */
	public Object extractJavaFieldToSqlArgValue(Object object) throws SQLException {
		return convertJavaFieldToSqlArgValue(extractJavaFieldValue(object));
	}

	/**
	 * Convert a field value to something suitable to be stored in the database.
	 */
	public Object convertJavaFieldToSqlArgValue(Object fieldVal) throws SQLException {
		if (fieldVal == null) {
			return null;
		} else {
			return getFieldConverter().javaToSqlArg(this, fieldVal);
		}
	}

	/**
	 * Convert a string value into the appropriate Java field value.
	 */
	public Object convertStringToJavaField(String value, int columnPos) throws SQLException {
		if (value == null) {
			return null;
		} else {
			return getFieldConverter().resultStringToJava(this, value, columnPos);
		}
	}

	/**
	 * Move the SQL value to the next one for version processing.
	 */
	public Object moveToNextValue(Object val) {
		if (dataPersister == null) {
			return null;
		} else {
			return dataPersister.moveToNextValue(val);
		}
	}

	/**
	 * Return the id field associated with the foreign object or null if none.
	 */
	public PropertyConfig getForeignIdField() {
    if(foreignIdField == null) {
      if(Registry.getEntityRegistry().hasEntityConfiguration(targetEntityClass))
        foreignIdField = Registry.getEntityRegistry().getEntityConfiguration(targetEntityClass).getIdProperty();
    }
		return foreignIdField;
	}

  public PropertyConfig getForeignPropertyConfig() {
    if(foreignPropertyConfig == null) {
      if(Registry.getPropertyRegistry().hasPropertyConfiguration(targetEntityClass, targetProperty))
        foreignPropertyConfig = Registry.getPropertyRegistry().getPropertyConfiguration(targetEntityClass, targetProperty);
    }
    return foreignPropertyConfig;
  }

  public Dao<?, ?> getForeignDao() throws SQLException {
    if(foreignDao == null) {
      if(getTargetEntityConfig() != null)
        this.foreignDao = getTargetEntityConfig().getDao();
      if(getTargetEntityClass() != null && Registry.getEntityRegistry().hasEntityConfiguration(getTargetEntityClass()))
        this.foreignDao = Registry.getEntityRegistry().getEntityConfiguration(getTargetEntityClass()).getDao();
    }
    return foreignDao;
  }

  /**
	 * Call through to {@link DataPersister#isEscapedValue()}
	 */
	public boolean isEscapedValue() {
		return dataPersister.isEscapedValue();
	}

	public Enum<?> getUnknownEnumVal() {
    if(fieldConfig != null)
		  return fieldConfig.getUnknownEnumValue();
    return null;
	}

	/**
	 * Return the format of the field.
	 */
	public String getFormat() {
    if(fieldConfig != null)
		return fieldConfig.getFormat();
    return "";
	}

  // TODO: what is this for?
	public boolean isUniqueCombo() {
		return fieldConfig != null && fieldConfig.isUniqueCombo();
	}

  // TODO: what is this for?
	public String getIndexName() {
    if(fieldConfig != null)
		return fieldConfig.getIndexName(tableName);
    return null;
	}

	public String getUniqueIndexName() {
    if(fieldConfig != null)
		return fieldConfig.getUniqueIndexName(tableName);
    return null;
	}

	/**
	 * Call through to {@link DataPersister#isEscapedDefaultValue()}
	 */
	public boolean isEscapedDefaultValue() {
		return dataPersister.isEscapedDefaultValue();
	}

	/**
	 * Call through to {@link DataPersister#isComparable()}
	 */
	public boolean isComparable() throws SQLException {
		if (typeIsACollection || (fieldConfig != null && fieldConfig.isForeignCollection())) {
			return false;
		}
		/*
		 * We've seen dataPersister being null here in some strange cases. Why? It may because someone is searching on
		 * an improper field. Or maybe a table-config does not match the Java object?
		 */
		if (getDataPersister() == null) {
			throw new SQLException("Internal error.  Data-persister is not configured for field.  "
					+ "Please post _full_ exception with associated data objects to mailing list: " + this);
		} else {
			return getDataPersister().isComparable();
		}
	}

	/**
	 * Call through to {@link DataPersister#isArgumentHolderRequired()}
	 */
	public boolean isArgumentHolderRequired() {
		return dataPersister.isArgumentHolderRequired();
	}

	/**
	 * Call through to {@link DatabaseFieldConfig#isForeignCollection()}
	 */
	public boolean isForeignCollection() {
    // TODO: is this correctly implemented with isManyCardinalityRelationshipProperty? Also see isForeign()
		return (isOneToManyField || isManyToManyField) ||
        (fieldConfig != null && fieldConfig.isForeignCollection()); // TODO: try to remove fieldConfig.isForeignCollection()
	}

//  public boolean isJoinColumn() {
//    return fieldConfig.isJoinColumn();
//  }
//
//  public boolean isOneToOneField() {
//    return fieldConfig.isOneToOneField();
//  }
//
//  public OneToOneConfig getOneToOneConfig() {
//    return fieldConfig.getOneToOneConfig();
//  }
//
//  public boolean isOneToManyField() {
//    return fieldConfig.isOneToManyField();
//  }
//
//  public boolean isManyToOneField() {
//    return fieldConfig.isManyToOneField();
//  }
//
//  public OneToManyConfig getOneToManyConfig() {
//    return fieldConfig.getOneToManyConfig();
//  }
//
//  public boolean isManyToManyField() {
//    return fieldConfig.isManyToManyField();
//  }
//
//  public ManyToManyConfig getManyToManyConfig() {
//    return fieldConfig.getManyToManyConfig();
//  }

//  public boolean cascadePersist() {
//    if(getOneToOneConfig() != null)
//      return getOneToOneConfig().cascadePersist();
//    else if(getOneToManyConfig() != null)
//      return getOneToManyConfig().cascadePersist();
//    else if(getManyToManyConfig() != null)
//      return getManyToManyConfig().cascadePersist();
//
//    return false;
//  }


  public boolean cascadePersist() {
    if(cascadePersist == null) {
      cascadePersist = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.PERSIST.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadePersist = true;
          break;
        }
      }
    }

    return cascadePersist;
  }

  public boolean cascadeMerge() {
    if(cascadeMerge == null) {
      cascadeMerge = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.MERGE.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadeMerge = true;
          break;
        }
      }
    }

    return cascadeMerge;
  }

  public boolean cascadeRefresh() {
    if(cascadeRefresh == null) {
      cascadeRefresh = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.REFRESH.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadeRefresh = true;
          break;
        }
      }
    }

    return cascadeRefresh;
  }

  public boolean cascadeRemove() {
    if(cascadeRemove == null) {
      cascadeRemove = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.REMOVE.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadeRemove = true;
          break;
        }
      }
    }

    return cascadeRemove;
  }

	/**
	 * Build and return a foreign collection based on the field settings that matches the id argument. This can return
	 * null in certain circumstances.
	 * 
	 * @param parent
	 *            The parent object that we will set on each item in the collection.
	 * @param id
	 *            The id of the foreign object we will look for. This can be null if we are creating an empty
	 *            collection.
	 */
	public <FT, FID> Collection<FT> buildForeignCollection(Object parent, FID id) throws SQLException {
    return buildForeignCollection(parent, id, true);
  }

  /**
   * Build and return a foreign collection based on the field settings that matches the id argument. This can return
   * null in certain circumstances.
   *
   * @param parent
   *            The parent object that we will set on each item in the collection.
   * @param id
   *            The id of the foreign object we will look for. This can be null if we are creating an empty
   *            collection.
   */
  public <FT, FID> Collection<FT> buildForeignCollection(Object parent, FID id, boolean queryForExistingCollectionItems) throws SQLException {
		// this can happen if we have a foreign-auto-refresh scenario
		if (getTargetPropertyConfig() == null) {
			return null; // TODO: i think it's a bad idea to return null, why not at least return a new HashSet so that application can continue?
		}
		@SuppressWarnings("unchecked")
		Dao<FT, FID> castDao = (Dao<FT, FID>) getForeignDao();
//		if (!fieldConfig.isForeignCollectionEager()) {
    if(fetch == FetchType.LAZY) {
			// we know this won't go recursive so no need for the counters
			return createLazyLoadingCollection(parent, id, castDao, queryForExistingCollectionItems);
		}

		LevelCounters levelCounters = threadLevelCounters.get();
		if (levelCounters.foreignCollectionLevel == 0) {
//			levelCounters.foreignCollectionLevelMax = fieldConfig.getForeignCollectionMaxEagerLevel();
      levelCounters.foreignCollectionLevelMax = NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED; // TODO
		}
		// are we over our level limit?
		if (levelCounters.foreignCollectionLevel >= levelCounters.foreignCollectionLevelMax) {
			// then return a lazy collection instead
			return createLazyLoadingCollection(parent, id, castDao, queryForExistingCollectionItems);
		}
		levelCounters.foreignCollectionLevel++;
		try {
			return createEagerLoadingCollection(parent, id, castDao, queryForExistingCollectionItems);
		} finally {
			levelCounters.foreignCollectionLevel--;
		}
	}

  protected <FT, FID> Collection<FT> createLazyLoadingCollection(Object parent, FID id, Dao<FT, FID> dao, boolean queryForExistingCollectionItems) throws SQLException {
    if(isManyToManyField())
      return new ManyToManyLazyLoadingEntitiesCollection<FT>(this, id, parent, queryForExistingCollectionItems);
    return new LazyLoadingEntitiesCollection<FT>(this, id, parent, getOneToManyConfig(), queryForExistingCollectionItems);
//    if(isManyToManyField())
//      return new ManyToManyLazyLoadingEntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, getManyToManyConfig());
//    return new LazyLoadingEntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, getOneToManyConfig());

//    return new LazyForeignCollection<FT, FID>(dao, parent, id, foreignPropertyConfig,
//        fieldConfig.getForeignCollectionOrderColumnName(), fieldConfig.isForeignCollectionOrderAscending());
  }

  protected <FT, FID> Collection<FT> createEagerLoadingCollection(Object parent, FID id, Dao<FT, FID> dao, boolean queryForExistingCollectionItems) throws SQLException {
    if(isManyToManyField())
      return new ManyToManyEntitiesCollection<FT>(this, id, parent, queryForExistingCollectionItems);
    return new EntitiesCollection<FT>(this, id, parent, getOneToManyConfig(), queryForExistingCollectionItems);
//    if(isManyToManyField())
//      return new ManyToManyEntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, getManyToManyConfig());
//    return new EntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, getOneToManyConfig());

//    return new EagerForeignCollection<FT, FID>(dao, parent, id, foreignPropertyConfig,
//        fieldConfig.getForeignCollectionOrderColumnName(), fieldConfig.isForeignCollectionOrderAscending());
  }

  //  public boolean isForeignCollectionInstance(Object fieldInstance) {
//    return fieldInstance instanceof ForeignCollection;
//  }
  public boolean isForeignCollectionInstance(Object fieldInstance) {
    return fieldInstance instanceof EntitiesCollection;
  }

  /**
	 * Get the result object from the results. A call through to {@link FieldConverter#resultToJava}.
	 */
	public <T> T resultToJava(DatabaseResults results, Map<String, Integer> columnPositions) throws SQLException {
    String columnNameUpperCase = getColumnName().toUpperCase();
		Integer dbColumnPos = columnPositions.get(columnNameUpperCase);

		if (dbColumnPos == null) {
      if(isId() && entityConfig.getInheritance() == InheritanceType.JOINED) { // in Joined inheritance tables id column name exists at least twice, so it's ambiguous for results.findColumn(getColumnName())
        String[] columnNames = results.getColumnNames();
        for(int i = 0; i < results.getColumnCount(); i++) {
          if(columnNameUpperCase.equals(columnNames[i].toUpperCase())) {
            dbColumnPos = i;
            break;
          }
        }
      }
      else
  			dbColumnPos = results.findColumn(getColumnName());
      if(dbColumnPos < 0)
        return null;

			columnPositions.put(columnNameUpperCase, dbColumnPos);
		}

    try {
      @SuppressWarnings("unchecked")
      T converted = (T) getFieldConverter().resultToJava(this, results, dbColumnPos);
      if (isForeign()) {
			/*
			 * Subtle problem here. If your foreign field is a primitive and the value was null then this would return 0
			 * from getInt(). We have to specifically test to see if we have a foreign field so if it is null we return
			 * a null value to not create the sub-object.
			 */
        if (results.wasNull(dbColumnPos)) {
          return null;
        }
      } else if (getDataPersister().isPrimitive()) {
        if (fieldConfig != null && fieldConfig.isThrowIfNull() && results.wasNull(dbColumnPos)) { // TODO
          throw new SQLException("Results value for primitive field '" + field.getName()
              + "' was an invalid null value");
        }
      } else if (!getFieldConverter().isStreamType() && results.wasNull(dbColumnPos)) {
        // we can't check if we have a null if this is a stream type
        return null;
      }
      return converted;
    } catch(Exception ex) {
      log.error("Could not convert results " + results.getColumnNames() + " for property " + field, ex);
    }

    return null;
	}

  /**
   * Call through to {@link DatabaseFieldConfig#isForeignAutoCreate()}
   */
  public boolean isForeignAutoCreate() {
    return isForeignAutoCreate;
  }

	/**
	 * Call through to {@link DatabaseFieldConfig#isReadOnly()}
	 */
	public boolean isReadOnly() {
		return fieldConfig != null && fieldConfig.isReadOnly();
	}

	/**
	 * Return the value of field in the data argument if it is not the default value for the class. If it is the default
	 * then null is returned.
	 */
	public <FV> FV getFieldValueIfNotDefault(Object object) throws SQLException {
		@SuppressWarnings("unchecked")
		FV fieldValue = (FV) extractJavaFieldValue(object);
		if (isFieldValueDefault(fieldValue)) {
			return null;
		} else {
			return fieldValue;
		}
	}

	/**
	 * Return whether or not the data object has a default value passed for this field of this type.
	 */
	public boolean isObjectsFieldValueDefault(Object object) throws SQLException {
		Object fieldValue = extractJavaFieldValue(object);
		return isFieldValueDefault(fieldValue);
	}


  // TODO: try to remove (or at least move to another class)
  /*
   * Default values.
   *
   * NOTE: These don't get any values so the compiler assigns them to the default values for the type. Ahhhh. Smart.
   */
  private static boolean DEFAULT_VALUE_BOOLEAN;
  private static byte DEFAULT_VALUE_BYTE;
  private static char DEFAULT_VALUE_CHAR;
  private static short DEFAULT_VALUE_SHORT;
  private static int DEFAULT_VALUE_INT;
  private static long DEFAULT_VALUE_LONG;
  private static float DEFAULT_VALUE_FLOAT;
  private static double DEFAULT_VALUE_DOUBLE;

	/**
	 * Return whether or not the field value passed in is the default value for the type of the field. Null will return
	 * true.
	 */
	public Object getJavaDefaultValueDefault() {
		if (field.getType() == boolean.class) {
			return DEFAULT_VALUE_BOOLEAN;
		} else if (field.getType() == byte.class || field.getType() == Byte.class) {
			return DEFAULT_VALUE_BYTE;
		} else if (field.getType() == char.class || field.getType() == Character.class) {
			return DEFAULT_VALUE_CHAR;
		} else if (field.getType() == short.class || field.getType() == Short.class) {
			return DEFAULT_VALUE_SHORT;
		} else if (field.getType() == int.class || field.getType() == Integer.class) {
			return DEFAULT_VALUE_INT;
		} else if (field.getType() == long.class || field.getType() == Long.class) {
			return DEFAULT_VALUE_LONG;
		} else if (field.getType() == float.class || field.getType() == Float.class) {
			return DEFAULT_VALUE_FLOAT;
		} else if (field.getType() == double.class || field.getType() == Double.class) {
			return DEFAULT_VALUE_DOUBLE;
		} else {
			return null;
		}
	}

	/**
	 * Pass the foreign data argument to the foreign {@link Dao#create(Object)} method.
	 */
	public <T> int createWithForeignDao(T foreignData) throws SQLException {
		@SuppressWarnings("unchecked")
		Dao<T, ?> castDao = (Dao<T, ?>) getForeignDao();
		return castDao.create(foreignData);
	}

	@Override
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != this.getClass()) {
			return false;
		}
		PropertyConfig other = (PropertyConfig) arg;
		return field.equals(other.field)
				&& (parentClass == null ? other.parentClass == null : parentClass.equals(other.parentClass));
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public String toString() {
		return "Property " + getFieldName() + " on Entity " + entityConfig;
	}

	/**
	 * Return whether or not the field value passed in is the default value for the type of the field. Null will return
	 * true.
	 */
	private boolean isFieldValueDefault(Object fieldValue) {
		if (fieldValue == null) {
			return true;
		} else {
			return fieldValue.equals(getJavaDefaultValueDefault());
		}
	}

	/**
	 * If we have a class Foo with a collection of Bar's then we go through Bar's DAO looking for a Foo field. We need
	 * this field to build the query that is able to find all Bar's that have foo_id that matches our id.
	 */
	private PropertyConfig findForeignFieldType(Class<?> clazz, Class<?> foreignClass, Dao<?, ?> foreignDao)
			throws SQLException { // cda: don't implement against implementation but interface
    if(targetProperty != null && Registry.getPropertyRegistry().hasPropertyConfiguration(foreignClass, targetProperty))
      return Registry.getPropertyRegistry().getPropertyConfiguration(foreignClass, targetProperty);
    EntityConfig targetEntityConfig = Registry.getEntityRegistry().getEntityConfiguration(foreignClass); // TODO: replace with targetClass
    if(targetEntityConfig != null && targetEntityConfig.getIdProperty() != null)
      return targetEntityConfig.getIdProperty();

		String foreignColumnName = fieldConfig.getForeignCollectionForeignFieldName(); // TODO: try to remove
    if(foreignColumnName == null)
      foreignColumnName = columnName; // TODO: is this correct? Is Column name always set to targetClass' JoinColumn name?
		for (PropertyConfig propertyConfig : foreignDao.getEntityConfig().getPropertyConfigs()) {
			if (propertyConfig.getType() == foreignClass
					&& (foreignColumnName == null || propertyConfig.getField().getName().equals(foreignColumnName))) {
				if (!propertyConfig.fieldConfig.isForeign() && !propertyConfig.fieldConfig.isForeignAutoRefresh()) {
					// this may never be reached
					throw new SQLException("Foreign collection object " + clazz + " for field '" + field.getName()
							+ "' contains a field of class " + foreignClass + " but it's not foreign");
				}
				return propertyConfig;
			}
      else if(propertyConfig.fieldConfig.isManyToManyField() && propertyConfig.fieldConfig.getManyToManyConfig() != null) {
        return propertyConfig;
      }
		}
		// build our complex error message
		StringBuilder sb = new StringBuilder();
		sb.append("Foreign collection class ").append(clazz.getName());
		sb.append(" for field '").append(field.getName()).append("' column-name does not contain a foreign field");
		if (foreignColumnName != null) {
			sb.append(" named '").append(foreignColumnName).append('\'');
		}
		sb.append(" of class ").append(foreignClass.getName());
		throw new SQLException(sb.toString());
	}

	/**
	 * Configure our data persister and any dependent fields. We have to do this here because both the constructor and
	 * {@link #configDaoInformation} method can set the data-type.
	 */
	protected void assignDataType(DatabaseType databaseType, DataPersister dataPersister) throws SQLException {
		dataPersister = databaseType.getDataPersister(dataPersister, this);
		this.dataPersister = dataPersister;
		if (dataPersister == null) {
			if (!isForeign() && !isForeignCollection()) {
				// may never happen but let's be careful out there
				throw new SQLException("Data persister for field " + this
						+ " is null but the field is not a foreign or foreignCollection");
			}
			return;
		}

		this.fieldConverter = databaseType.getFieldConverter(dataPersister, this);
		if (this.isGeneratedId && !dataPersister.isValidGeneratedType()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Generated-id field '").append(field.getName());
			sb.append("' in ").append(field.getDeclaringClass().getSimpleName());
			sb.append(" can't be type ").append(dataPersister.getSqlType());
			sb.append(".  Must be one of: ");
			for (DataType dataType : DataType.values()) {
				DataPersister persister = dataType.getDataPersister();
				if (persister != null && persister.isValidGeneratedType()) {
					sb.append(dataType).append(' ');
				}
			}
			throw new IllegalArgumentException(sb.toString());
		}

		if (fieldConfig.isThrowIfNull() && !dataPersister.isPrimitive()) {
			throw new SQLException("Field " + field.getName() + " must be a primitive if set with throwIfNull");
		}
		if (this.isId && !dataPersister.isAppropriateId()) {
			throw new SQLException("Field '" + field.getName() + "' is of data type " + dataPersister
					+ " which cannot be the ID field");
		}
		this.dataTypeConfigObj = dataPersister.makeConfigObject(this);
		String defaultStr = fieldConfig.getDefaultValue();
		if (defaultStr == null) {
			this.defaultValue = null;
		} else if (this.isGeneratedId) {
			throw new SQLException("Field '" + field.getName() + "' cannot be a generatedId and have a default value '"
					+ defaultStr + "'");
		} else {
			this.defaultValue = getFieldConverter().parseDefaultString(this, defaultStr);
		}
	}

  public boolean isPropertyOfParentClass() {
    return isPropertyOfParentClass;
  }

  public void setIsPropertyOfParentClass(boolean isPropertyOfParentClass) {
    this.isPropertyOfParentClass = isPropertyOfParentClass;
  }

  private static class LevelCounters {
		// current auto-refresh recursion level
		int autoRefreshLevel;
		// maximum auto-refresh recursion level
		int autoRefreshLevelMax;

		// current foreign-collection recursion level
		int foreignCollectionLevel;
		// maximum foreign-collection recursion level
		int foreignCollectionLevelMax;
	}
}
