package com.j256.ormlite.jpa;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.misc.JavaxPersistenceImpl;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.ObjectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AccessType;
import javax.persistence.Index;
import javax.persistence.InheritanceType;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.UniqueConstraint;

/**
 * Information about a database table including the associated joinTableName, class, constructor, and the included fields.
 * 
 * @param <T>
 *            The class that the code will be operating on.
 * @param <ID>
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class EntityConfig<T, ID> {

  private final static Logger log = LoggerFactory.getLogger(EntityConfig.class);

	private static final PropertyConfig[] NO_FOREIGN_COLLECTIONS = new PropertyConfig[0];
  private static final PropertyConfig[] NO_MANY_TO_MANY_FIELDS = new PropertyConfig[0];
  private static final PropertyConfig[] NO_JOIN_COLUMNS = new PropertyConfig[0];

	protected Dao<T, ID> dao; // cda: don't implement against implementation but interface (-> makes Dao customizable)
  protected ConnectionSource connectionSource = null;

	protected Class<T> entityClass;
	protected String tableName;
  protected String entityName; // only used for JPQL, so not of any use in our case

  protected Constructor<T> constructor;
  protected AccessType access = null;
	protected List<PropertyConfig> propertyConfigs = new ArrayList<>();
  protected Map<String, PropertyConfig> propertyConfigsColumnNames = new HashMap<>(); // TODO: merge with propertyConfigs

	protected List<PropertyConfig> foreignCollections = null;
  protected List<PropertyConfig> joinColumns = null;
  protected List<PropertyConfig> joinTableProperties = null;
	protected PropertyConfig idProperty;

	protected boolean foreignAutoCreate;
	private Map<String, PropertyConfig> fieldNameMap;

  // @Table Annotation settings
  protected String catalogName = "";
  protected String schemaName = "";
  protected UniqueConstraint[] uniqueConstraints = new UniqueConstraint[0];
  protected Index[] indexes = new Index[0];

  // inheritance
  protected EntityConfig inheritanceTopLevelEntityConfig = null;
  protected EntityConfig parentEntityConfig = null;
  protected List<EntityConfig> topDownInheritanceHierarchy = null;
  protected List<EntityConfig> childEntityConfigs = new ArrayList<>();
  protected InheritanceType inheritance = null;

  // Life Cycle Events
  // TODO: but it's possible that it has more than one method per event, isn't it?
  protected Method prePersistLifeCycleMethod = null;
  protected Method postPersistLifeCycleMethod = null;
  protected Method postLoadLifeCycleMethod = null;
  protected Method preUpdateLifeCycleMethod = null;
  protected Method postUpdateLifeCycleMethod = null;
  protected Method preRemoveLifeCycleMethod = null;
  protected Method postRemoveLifeCycleMethod = null;


	/**
	 * Creates a holder of information about a table/class.
	 * 
	 * @param connectionSource
	 *            Source of our database connections.
	 * @param dao
	 *            Associated BaseDaoImpl.
	 * @param entityClass
	 *            Class that we are holding information about.
	 */
	public EntityConfig(ConnectionSource connectionSource, Dao<T, ID> dao, Class<T> entityClass)
			throws SQLException { // cda: don't implement against implementation but interface
		this(connectionSource.getDatabaseType(), dao,
				DatabaseTableConfig.fromClass(connectionSource, entityClass));
    this.connectionSource = connectionSource;
	}

	/**
	 * Creates a holder of information about a table/class.
	 * 
	 * @param databaseType
	 *            Database type we are storing the class in.
	 * @param dao
	 *            Associated BaseDaoImpl.
	 * @param tableConfig
	 *            Configuration for our table.
	 */
	public EntityConfig(DatabaseType databaseType, Dao<T, ID> dao, DatabaseTableConfig<T> tableConfig)
			throws SQLException { // cda: don't implement against implementation but interface
		this.dao = dao;
		this.entityClass = tableConfig.getDataClass();
    setTableName(tableConfig.getTableName());
		setPropertyConfigs(tableConfig.getFieldTypes(databaseType));
    this.constructor = tableConfig.getConstructor();
    
    findSpecialColumns();
    findLifeCycleEvents(getEntityClass());
	}

  // cda: my default constructor
  public EntityConfig(Class entityClass, ConnectionSource connectionSource) throws SQLException {
    this.connectionSource = connectionSource;
    this.dao = null;
    this.entityClass = entityClass;
    setEntityName(entityClass.getSimpleName());
    setTableName(entityName);
    this.constructor = ReflectionHelper.findNoArgConstructor(entityClass);

//    this.foreignAutoCreate = true; // TODO: right now this value seems neccessary, try to remove / implement other logic. See MappedCreate.insert()
//    findSpecialColumns(); // TODO: to be done in JpaEntityConfigurationReader
//    findLifeCycleEvents(getEntityClass()); // TODO: to be done in JpaEntityConfigurationReader
  }

  // cda: For Inheritance tables
  public EntityConfig(Class entityClass, ConnectionSource connectionSource, String tableName, PropertyConfig[] propertyConfigs) throws SQLException {
    this(entityClass, connectionSource);

    this.setPropertyConfigs(propertyConfigs);
  }

  // cda: for JoinTables
  protected EntityConfig(ConnectionSource connectionSource, String tableName/*, PropertyConfig owningSideProperty*/) throws SQLException {
    entityClass = null;
    this.connectionSource = connectionSource;
    setEntityName(tableName);
    setTableName(tableName);

//    addProperty(owningSideProperty);

    constructor = null;
    foreignAutoCreate = false; // TODO: remove
  }

  // TODO: remove
  // cda: for my JoinTableInfo sub class
  protected EntityConfig(String tableName, PropertyConfig[] propertyConfigs, PropertyConfig idProperty) throws SQLException {
    dao = null;
    entityClass = null;
    setTableName(tableName);
    setPropertyConfigs(propertyConfigs);
//    foreignCollections = NO_FOREIGN_COLLECTIONS;
//    joinColumns = NO_JOIN_COLUMNS;
//    joinTableProperties = NO_MANY_TO_MANY_FIELDS;
    this.idProperty = idProperty;
    constructor = null;
    foreignAutoCreate = false;
  }

  protected void findSpecialColumns() throws SQLException {
    // find the id field
    PropertyConfig findIdPropertyConfig = null;
    boolean foreignAutoCreate = false;
//    List<PropertyConfig> joinColumns = new ArrayList<>();
//    int foreignCollectionCount = 0;
    int manyToManyFieldsCount = 0;

    for (PropertyConfig propertyConfig : propertyConfigs) {
      if (propertyConfig.isId() || propertyConfig.isGeneratedId() || propertyConfig.isGeneratedIdSequence()) {
        if (findIdPropertyConfig != null) {
          throw new SQLException("More than 1 idProperty configured for class " + entityClass + " ("
              + findIdPropertyConfig + "," + propertyConfig + ")");
        }
        findIdPropertyConfig = propertyConfig;
      }
      if (propertyConfig.isForeignAutoCreate() || (propertyConfig.isOneToManyField() && propertyConfig.getOneToManyConfig().cascadePersist()) ||
          (propertyConfig.isManyToManyField() && propertyConfig.getManyToManyConfig().cascadePersist())) {
        foreignAutoCreate = true;
      }
      if (propertyConfig.isJoinColumn())
//        joinColumns.add(propertyConfig);
        addJoinColumn(propertyConfig);
      if (propertyConfig.isForeignCollection()) {
//        foreignCollectionCount++;
        addForeignCollection(propertyConfig);
      }
      if (propertyConfig.isManyToManyField())
//        manyToManyFieldsCount++;
        addJoinTableProperty(propertyConfig);
    }

    // can be null if there is no id field
    this.idProperty = findIdPropertyConfig;
    this.foreignAutoCreate = foreignAutoCreate;
//    this.joinColumns = joinColumns.toArray(new PropertyConfig[joinColumns.size()]);

//    if (foreignCollectionCount == 0) {
//      this.foreignCollections = NO_FOREIGN_COLLECTIONS;
//      this.joinTableProperties = NO_MANY_TO_MANY_FIELDS;
//    } else {
//      this.foreignCollections = new PropertyConfig[foreignCollectionCount];
//      this.joinTableProperties = new PropertyConfig[manyToManyFieldsCount];
//      foreignCollectionCount = 0;
//      manyToManyFieldsCount = 0;

//      for (PropertyConfig propertyConfig : propertyConfigs) {
//        if (propertyConfig.isForeignCollection()) {
//          this.foreignCollections[foreignCollectionCount] = propertyConfig;
//          foreignCollectionCount++;
//        }
//        if (propertyConfig.isManyToManyField()) {
//          this.joinTableProperties[manyToManyFieldsCount] = propertyConfig;
//          manyToManyFieldsCount++;
//        }
//      }
//    }
  }

  public Dao<T, ID> getDao() throws SQLException {
    if(dao == null && connectionSource != null) {
//      dao = Instances.getDaoManager().createDao(connectionSource, entityClass);
      dao = new BaseDaoImpl<T, ID>(this, connectionSource) { }; // TODO: find an universal way to create Dao
      dao.setObjectCache(true); // TODO: don't know how to solve it otherwise right now - implement global boolean cacheObjects flag?
    }
    return dao;
  }

  public void setDao(Dao dao) {
    this.dao = dao;
  }

  public ConnectionSource getConnectionSource() {
    return connectionSource;
  }

  public DatabaseType getDatabaseType() {
    if(connectionSource != null)
      return connectionSource.getDatabaseType();
    return null;
  }

  /**
	 * Return the class associated with this object-info.
	 */
	public Class<T> getEntityClass() {
		return entityClass;
	}

	/**
	 * Return the name of the table associated with the object.
	 */
	public String getTableName() {
		return tableName;
	}

  public void setTableName(String tableName) {
    if(getDatabaseType().isEntityNamesMustBeUpCase())
      tableName = tableName.toUpperCase();
    this.tableName = tableName;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public UniqueConstraint[] getUniqueConstraints() {
    return uniqueConstraints;
  }

  public void setUniqueConstraints(UniqueConstraint[] uniqueConstraints) {
    this.uniqueConstraints = uniqueConstraints;
  }

  public Index[] getIndexes() {
    return indexes;
  }

  public void setIndexes(Index[] indexes) {
    this.indexes = indexes;
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    if(getDatabaseType().isEntityNamesMustBeUpCase())
      entityName = entityName.toUpperCase();
    this.entityName = entityName;
  }

  /**
	 * Return the array of field types associated with the object.
	 */
	public PropertyConfig[] getPropertyConfigs() {
		return propertyConfigs.toArray(new PropertyConfig[0]);
	}

  public List<PropertyConfig> getProperties() {
    return propertyConfigs;
  }

  public PropertyConfig[] getFieldTypesWithoutForeignCollections() {
    List<PropertyConfig> fieldTypesWithoutForeignCollections = new ArrayList<>();
    for(PropertyConfig propertyConfig : getPropertyConfigs()) {
      if(propertyConfig.isForeignCollection() == false)
        fieldTypesWithoutForeignCollections.add(propertyConfig);
    }
    PropertyConfig[] result = new PropertyConfig[fieldTypesWithoutForeignCollections.size()];
    result = fieldTypesWithoutForeignCollections.toArray(result);
    fieldTypesWithoutForeignCollections = null;

    return result;
  }

	/**
	 * Return the {@link PropertyConfig} associated with the columnName.
	 */
	public PropertyConfig getFieldTypeByColumnName(String columnName) {
    columnName = columnName.toUpperCase();
    if(propertyConfigsColumnNames.containsKey(columnName))
      return propertyConfigsColumnNames.get(columnName);
    else if(parentEntityConfig != null) {
      PropertyConfig propertyFromParentEntity = parentEntityConfig.getFieldTypeByColumnName(columnName);
      if(propertyFromParentEntity != null)
        return propertyFromParentEntity;
    }

		if (fieldNameMap == null) {
			// build our alias map if we need it
			Map<String, PropertyConfig> map = new HashMap<String, PropertyConfig>();
			for (PropertyConfig propertyConfig : propertyConfigs) {
				map.put(propertyConfig.getColumnName().toLowerCase(), propertyConfig);
			}
			fieldNameMap = map;
		}
		PropertyConfig propertyConfig = fieldNameMap.get(columnName.toLowerCase());
		// if column name is found, return it
		if (propertyConfig != null) {
			return propertyConfig;
		}
		// look to see if someone is using the field-name instead of column-name
		for (PropertyConfig propertyConfig2 : propertyConfigs) {
			if (propertyConfig2.getFieldName().equals(columnName)) {
				throw new IllegalArgumentException("You should use columnName '" + propertyConfig2.getColumnName()
						+ "' for table " + tableName + " instead of fieldName '" + propertyConfig2.getFieldName() + "'");
			}
		}
		throw new IllegalArgumentException("Unknown column name '" + columnName + "' in table " + tableName);
	}

  /**
   * <p>
   *   Not such a good design.<br/>
   *   Only needed for Joined inheritance tables as by default all super class fields get added to TableInfo,
   *   but Joined tables only contain columns from their own fields.
   * </p>
   * @param propertyConfigs
   * @throws SQLException
   */
  protected void setPropertyConfigs(PropertyConfig[] propertyConfigs) throws SQLException {
    this.propertyConfigs.clear();
    this.propertyConfigsColumnNames.clear();

    for(PropertyConfig propertyConfig : propertyConfigs)
      addProperty(propertyConfig);

    try { findSpecialColumns(); } catch(Exception ex) { }  // TODO: remove
//    idProperty = null; // TODO: was this ever valid code?
  }

  public boolean addProperty(PropertyConfig propertyConfig) throws SQLException {
    if(propertyConfigsColumnNames.containsKey(propertyConfig.getColumnName().toUpperCase()) == false) {
      propertyConfigsColumnNames.put(propertyConfig.getColumnName().toUpperCase(), propertyConfig);
      this.propertyConfigs.add(propertyConfig);

      if (propertyConfig.isId())
        setIdProperty(propertyConfig);

      return true;
    }
    else
      tryToMergeProperties(propertyConfigsColumnNames.get(propertyConfig.getColumnName().toUpperCase()), propertyConfig);

    return false;
  }

  protected void tryToMergeProperties(PropertyConfig currentProperty, PropertyConfig propertyWithSameColumnName) {
    if(currentProperty.getField() == null && propertyWithSameColumnName.getField() != null)
      currentProperty.field = propertyWithSameColumnName.getField();
    if(currentProperty.fieldGetMethod == null && propertyWithSameColumnName.fieldGetMethod != null)
      currentProperty.fieldGetMethod = propertyWithSameColumnName.fieldGetMethod;
    if(currentProperty.fieldSetMethod == null && propertyWithSameColumnName.fieldSetMethod != null)
      currentProperty.fieldSetMethod = propertyWithSameColumnName.fieldSetMethod;
  }

  /**
	 * Return the id-field associated with the object.
	 */
	public PropertyConfig getIdProperty() {
    if(idProperty == null) {
      if(getParentEntityConfig() != null)
        idProperty = getParentEntityConfig().getIdProperty();
    }
		return idProperty;
	}

  public void setIdProperty(PropertyConfig idProperty) throws SQLException {
    if(this.idProperty != null && idProperty != null && this.idProperty.equals(idProperty) == false)
      throw new SQLException("There is more than one ID property defined for Entity " + this + ". Found (at least) " + this.idProperty + " and " + idProperty);
    this.idProperty = idProperty;
  }

  public Constructor<T> getConstructor() {
		return constructor;
	}

  public AccessType getAccess() {
    return access;
  }

  public void setAccess(AccessType access) {
    this.access = access;
  }

  /**
	 * Return a string representation of the object.
	 */
	public String objectToString(T object) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(object.getClass().getSimpleName());
		for (PropertyConfig propertyConfig : propertyConfigs) {
			sb.append(' ').append(propertyConfig.getColumnName()).append("=");
			try {
				sb.append(propertyConfig.extractJavaFieldValue(object));
			} catch (Exception e) {
				throw new IllegalStateException("Could not generate toString of field " + propertyConfig, e);
			}
		}
		return sb.toString();
	}

	/**
	 * Create and return an object of this type using our reflection constructor.
	 */
	public T createObject() throws SQLException {
		try {
			T instance;
			ObjectFactory<T> factory = null;
			if (dao != null) {
				factory = dao.getObjectFactory();
			}
			if (factory == null) {
				instance = constructor.newInstance();
			} else {
				instance = factory.createObject(constructor, dao.getDataClass());
			}
			wireNewInstance(dao, instance);
			return instance;
		} catch (Exception e) {
			throw SqlExceptionUtil.create("Could not create object for " + constructor.getDeclaringClass(), e);
		}
	}

	/**
	 * Return true if we can update this object via its ID.
	 */
	public boolean isUpdatable() {
		// to update we must have an id field and there must be more than just the id field
		return (idProperty != null && propertyConfigs.size() > 1);
	}

	/**
	 * Return true if one of the fields has {@link DatabaseField#foreignAutoCreate()} enabled.
	 */
	public boolean isForeignAutoCreate() {
		return foreignAutoCreate;
	}

  public boolean hasForeignCollections() {
    return foreignCollections != null && foreignCollections.size() > 0;
  }

	/**
	 * Return an array with the fields that are {@link ForeignCollection}s or a blank array if none.
	 */
	public List<PropertyConfig> getForeignCollections() {
    if(foreignCollections == null)
      foreignCollections = new ArrayList<>();
		return foreignCollections;
	}

  public boolean addForeignCollection(PropertyConfig foreignCollectionProperty) throws SQLException {
    addProperty(foreignCollectionProperty);
    return getForeignCollections().add(foreignCollectionProperty);
  }

  public boolean hasJoinColumns() {
    return joinColumns != null && joinColumns.size() > 0;
  }

  public List<PropertyConfig> getJoinColumns() {
    if(joinColumns == null)
      joinColumns = new ArrayList<>();
    return joinColumns;
  }

  public boolean addJoinColumn(PropertyConfig joinColumnProperty) {
    if(getJoinColumns().contains(joinColumnProperty) == false)
      return getJoinColumns().add(joinColumnProperty);
    return false;
  }

  public boolean hasJoinTableProperties() {
    return joinTableProperties != null && joinTableProperties.size() > 0;
  }

  public List<PropertyConfig> getJoinTableProperties() {
    if(joinTableProperties == null)
      joinTableProperties = new ArrayList<>();
    return joinTableProperties;
  }

  public boolean addJoinTableProperty(PropertyConfig joinTableProperty) {
    return getJoinTableProperties().add(joinTableProperty);
  }

  public EntityConfig getInheritanceTopLevelEntityConfig() {
    return inheritanceTopLevelEntityConfig;
  }

  public void setInheritanceTopLevelEntityConfig(EntityConfig inheritanceTopLevelEntityConfig) {
    this.inheritanceTopLevelEntityConfig = inheritanceTopLevelEntityConfig;
  }

  public EntityConfig getParentEntityConfig() {
    return parentEntityConfig;
  }

  public void setParentEntityConfig(EntityConfig parentEntityConfig) {
    this.parentEntityConfig = parentEntityConfig;
    this.topDownInheritanceHierarchy = null;
  }

  public List<EntityConfig> getTopDownInheritanceHierarchy() {
    if(topDownInheritanceHierarchy == null) {
      topDownInheritanceHierarchy = new ArrayList<>();
      for(EntityConfig parentEntityConfig = this; parentEntityConfig != null; parentEntityConfig = parentEntityConfig.getParentEntityConfig()) {
        topDownInheritanceHierarchy.add(0, parentEntityConfig);
      }
    }

    return topDownInheritanceHierarchy;
  }

  public List<EntityConfig> getChildEntityConfigs() {
    return childEntityConfigs;
  }

  public boolean addChildTableInfo(EntityConfig childEntityConfig) {
    if(childEntityConfigs.contains(childEntityConfig) == false) {
      childEntityConfig.setParentEntityConfig(this);
      return childEntityConfigs.add(childEntityConfig);
    }
    return false;
  }

  public InheritanceType getInheritance() {
    return inheritance;
  }

  public void setInheritance(InheritanceType inheritance) {
    this.inheritance = inheritance;
  }

  /**
	 * Return true if this table information has a field with this columnName as set by
	 * {@link DatabaseField#columnName()} or the field name if not set.
	 */
	public boolean hasColumnName(String columnName) {
    return propertyConfigsColumnNames.containsKey(columnName);

//		for (PropertyConfig propertyConfig : propertyConfigs) {
//			if (propertyConfig.getColumnName().equals(columnName)) {
//				return true;
//			}
//		}
//		return false;
	}

	private static <T, ID> void wireNewInstance(Dao<T, ID> dao, T instance) {  // cda: don't implement against implementation but interface
		if (instance instanceof BaseDaoEnabled) {
			@SuppressWarnings("unchecked")
			BaseDaoEnabled<T, ID> daoEnabled = (BaseDaoEnabled<T, ID>) instance;
			daoEnabled.setDao(dao);
		}
	}

  protected void findLifeCycleEvents(Class<T> dataClass) {
    for (Class<?> classWalk = dataClass; classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(JavaxPersistenceImpl.classIsEntityOrMappedSuperclass(dataClass)) {
        for (Method method : classWalk.getDeclaredMethods()) {
          checkMethodForLifeCycleEvents(method);
        }
      }
    }
  }

  protected void checkMethodForLifeCycleEvents(Method method) {
    // TODO: i don't know what the specifications says but i implemented it this way that superclass life cycle events don't overwrite that ones from child classes
    // (or should both be called?)
    if(method.isAnnotationPresent(PrePersist.class) && prePersistLifeCycleMethod == null)
      prePersistLifeCycleMethod = method;
    if(method.isAnnotationPresent(PostPersist.class) && postPersistLifeCycleMethod == null)
      postPersistLifeCycleMethod = method;
    if(method.isAnnotationPresent(PostLoad.class) && postLoadLifeCycleMethod == null)
      postLoadLifeCycleMethod = method;
    if(method.isAnnotationPresent(PreUpdate.class) && preUpdateLifeCycleMethod == null)
      preUpdateLifeCycleMethod = method;
    if(method.isAnnotationPresent(PostUpdate.class) && postUpdateLifeCycleMethod == null)
      postUpdateLifeCycleMethod = method;
    if(method.isAnnotationPresent(PreRemove.class) && preRemoveLifeCycleMethod == null)
      preRemoveLifeCycleMethod = method;
    if(method.isAnnotationPresent(PostRemove.class) && postRemoveLifeCycleMethod == null)
      postRemoveLifeCycleMethod = method;
  }

  public Method prePersistLifeCycleMethod() {
    return prePersistLifeCycleMethod;
  }

  public void addPrePersistLifeCycleMethod(Method method) {
    if(prePersistLifeCycleMethod == null)
    prePersistLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method postPersistLifeCycleMethod() {
    return postPersistLifeCycleMethod;
  }

  public void addPostPersistLifeCycleMethod(Method method) {
    if(postPersistLifeCycleMethod == null)
      postPersistLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method postLoadLifeCycleMethod() {
    return postLoadLifeCycleMethod;
  }

  public void addPostLoadLifeCycleMethod(Method method) {
    if(postLoadLifeCycleMethod == null)
      postLoadLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method preUpdateLifeCycleMethod() {
    return preUpdateLifeCycleMethod;
  }

  public void addPreUpdateLifeCycleMethod(Method method) {
    if(preUpdateLifeCycleMethod == null)
      preUpdateLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method postUpdateLifeCycleMethod() {
    return postUpdateLifeCycleMethod;
  }

  public void addPostUpdateLifeCycleMethod(Method method) {
    if(postUpdateLifeCycleMethod == null)
      postUpdateLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method preRemoveLifeCycleMethod() {
    return preRemoveLifeCycleMethod;
  }

  public void addPreRemoveLifeCycleMethod(Method method) {
    if(preRemoveLifeCycleMethod == null)
      preRemoveLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public Method postRemoveLifeCycleMethod() {
    return postRemoveLifeCycleMethod;
  }

  public void addPostRemoveLifeCycleMethod(Method method) {
    if(postRemoveLifeCycleMethod == null)
      postRemoveLifeCycleMethod = method; // TODO: change to list to be able add multiple life cycle methods
  }

  public void invokePrePersistLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(prePersistLifeCycleMethod, data);
  }

  public void invokePostPersistLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(postPersistLifeCycleMethod, data);
  }

  public void invokePostLoadLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(postLoadLifeCycleMethod, data);
  }

  public void invokePreUpdateLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(preUpdateLifeCycleMethod, data);
  }

  public void invokePostUpdateLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(postUpdateLifeCycleMethod, data);
  }

  public void invokePreRemoveLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(preRemoveLifeCycleMethod, data);
  }

  public void invokePostRemoveLifeCycleMethod(Object data) {
    invokeMethodIfNotNull(postRemoveLifeCycleMethod, data);
  }

  protected void invokeMethodIfNotNull(Method method, Object methodOwner) {
    if(method == null)
      return;

    try {
      boolean isAccessible = method.isAccessible();
      if(isAccessible == false)
        method.setAccessible(true);

      method.invoke(methodOwner);

      if(isAccessible == false)
        method.setAccessible(false);
    } catch(Exception ex) { log.error("Could not invoke method " + method.getName(), ex); }
  }


  @Override
  public String toString() {
    return entityName;
  }
}
