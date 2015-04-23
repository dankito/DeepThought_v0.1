package com.j256.ormlite.jpa;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jpa.inheritance.EntityInheritance;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;
import com.j256.ormlite.jpa.inheritance.InheritanceHierarchy;
import com.j256.ormlite.misc.JavaxPersistenceImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 * Created by ganymed on 05/03/15.
 */
public class JpaEntityConfigurationReader {

  public final static String NotSupportedExceptionTrailMessage = "currently not supported and there are no plans to implement support for it";

  private final static Logger log = LoggerFactory.getLogger(JpaEntityConfigurationReader.class);


  protected List<Class> knownEntities = new ArrayList<>();

  protected JpaPropertyConfigurationReader propertyConfigurationReader = null;
  protected ConnectionSource connectionSource = null;


  public JpaEntityConfigurationReader(ConnectionSource connectionSource) {
    this(connectionSource, new JpaPropertyConfigurationReader());
  }

  public JpaEntityConfigurationReader(JpaPropertyConfigurationReader propertyConfigurationReader) {
    this(null, propertyConfigurationReader);
  }

  public JpaEntityConfigurationReader(ConnectionSource connectionSource, JpaPropertyConfigurationReader propertyConfigurationReader) {
    this.connectionSource = connectionSource;
    this.propertyConfigurationReader = propertyConfigurationReader;
  }


  public EntityConfig[] readConfiguration(Class... entityClasses) throws SQLException {
    this.knownEntities = Arrays.asList(entityClasses);

    List<EntityConfig> entityConfigs = new ArrayList<>();

    for(Class entityClass : entityClasses)
      entityConfigs.add(readEntityConfiguration(entityClass, connectionSource));

    return entityConfigs.toArray(new EntityConfig[entityConfigs.size()]);
  }

  public EntityConfig[] readConfigurationAndCreateTablesIfNotExists(Class... entityClasses) throws SQLException {
    EntityConfig[] entityConfigs = readConfiguration(entityClasses);

    for(EntityConfig entityConfig : entityConfigs) {
      entityConfig.setDao(createDao(entityConfig, connectionSource));
      TableUtils.createTableIfNotExists(connectionSource, entityConfig.getEntityClass());
    }

    return entityConfigs;
  }

  // TODO: create a new Dao only in one place in code
  protected Dao createDao(EntityConfig entityConfig, ConnectionSource connectionSource) throws SQLException {
    return new BaseDaoImpl(entityConfig, connectionSource) { };
  }

  protected EntityConfig readEntityConfiguration(Class<?> entityClass, ConnectionSource connectionSource) throws SQLException {
    log.info("Reading configuration for Entity " + entityClass + " ...");
    if(knownEntities.contains(entityClass) == false)
      throw new SQLException("Class " + entityClass + " is an unknown Entity. Add this Class to to Classes parameter of Method readConfiguration()");

    if(classIsEntity(entityClass) == false)
      throw new SQLException("Class " + entityClass + " is not an Entity as no @Entity annotation could be found");

    if(Registry.getEntityRegistry().hasEntityConfiguration(entityClass))
      return Registry.getEntityRegistry().getEntityConfiguration(entityClass);

    EntityConfig entityConfig = createEntityConfig(entityClass, connectionSource, new ArrayList<EntityConfig>());

    readEntityClassHierarchyConfiguration(entityConfig, connectionSource);

    if(entityConfig.getIdProperty() == null)
      throw new SQLException("Id not set on Entity " + entityConfig);

    return entityConfig;
  }

  protected void readEntityClassHierarchyConfiguration(EntityConfig entityConfig, ConnectionSource connectionSource) throws SQLException {
    List<EntityConfig> currentInheritanceTypeSubEntities = new ArrayList<>();
    currentInheritanceTypeSubEntities.add(entityConfig);

    EntityConfig currentEntityConfig = null, previousEntityConfig = entityConfig;

    for (Class<?> classWalk = entityConfig.getEntityClass().getSuperclass(); classWalk != null; classWalk = classWalk.getSuperclass()) {
      if (classIsEntityOrMappedSuperclass(classWalk) == false)
        break; // top of inheritance hierarchy reached

      if(classIsEntity(classWalk)) {
        currentEntityConfig = getCachedOrCreateNewEntityConfig(classWalk, connectionSource, currentInheritanceTypeSubEntities);
        currentInheritanceTypeSubEntities.add(currentEntityConfig);

        if(previousEntityConfig != null)
          currentEntityConfig.addChildTableInfo(previousEntityConfig);

        previousEntityConfig = currentEntityConfig;
      }
    }
  }

  protected <T, ID> EntityConfig<T, ID> getCachedOrCreateNewEntityConfig(Class entityClass, ConnectionSource connectionSource, List<EntityConfig> currentInheritanceTypeSubEntities) throws SQLException {
    if(Registry.getEntityRegistry().hasEntityConfiguration(entityClass)) {
      EntityConfig entityConfig = Registry.getEntityRegistry().getEntityConfiguration(entityClass);
      if(entityConfig instanceof InheritanceEntityConfig) {
        ((InheritanceEntityConfig) entityConfig).addInheritanceLevelSubEntities(currentInheritanceTypeSubEntities);
        currentInheritanceTypeSubEntities.clear();
      }
      return entityConfig;
    }

    return createEntityConfig(entityClass, connectionSource, currentInheritanceTypeSubEntities);
  }

  protected <T, ID> EntityConfig<T, ID> createEntityConfig(Class entityClass, ConnectionSource connectionSource, List<EntityConfig> currentInheritanceTypeSubEntities) throws SQLException {
    EntityConfig<T, ID> entityConfig = null;
    InheritanceType inheritanceStrategy = getInheritanceStrategyIfEntityIsInheritanceStartEntity(entityClass);

    if(inheritanceStrategy == null)
      entityConfig = new EntityConfig<T, ID>(entityClass, connectionSource);
    else
      entityConfig = createInheritanceEntityConfig(entityClass, inheritanceStrategy, connectionSource, currentInheritanceTypeSubEntities);

    Registry.getEntityRegistry().registerEntityConfiguration(entityClass, entityConfig);

    readEntityAnnotations(entityClass, entityConfig);
    findLifeCycleEvents(entityClass, entityConfig);
    propertyConfigurationReader.readEntityPropertiesConfiguration(entityConfig);

    return entityConfig;
  }

//  protected <T, ID> TableInfo<T, ID> createTableInfo(DatabaseType databaseType, DatabaseTableConfig tableConfig, Dao dao) throws SQLException {
//    return new TableInfo<T, ID>(databaseType, dao, tableConfig); // TODO: check for inheritance type
//  }

  protected InheritanceEntityConfig createInheritanceEntityConfig(Class<?> dataClass, InheritanceType inheritanceStrategy, ConnectionSource connectionSource, List<EntityConfig> currentInheritanceTypeSubclasses) throws SQLException {


    switch (inheritanceStrategy) {
      case SINGLE_TABLE:
        return createSingleTableTableInfoForClass(dataClass, connectionSource, currentInheritanceTypeSubclasses);
      case JOINED:
        return createJoinedTableInfoForClass(dataClass, connectionSource, currentInheritanceTypeSubclasses);
      // TODO: implement TABLE_PER_CLASS (or throw at least an Exception that it's currently not supported
    }

//    return getAndMayCreateTableInfoForClass(dataClass, connectionSource); // produces a Stack Overflow
    return null;
  }

  public SingleTableEntityConfig createSingleTableTableInfoForClass(Class tableClass, ConnectionSource connectionSource, List<EntityConfig> subclasses) throws SQLException {
    SingleTableEntityConfig singleTableEntityConfig = new SingleTableEntityConfig(tableClass, connectionSource, subclasses);

    return singleTableEntityConfig;
  }

  public JoinedEntityConfig createJoinedTableInfoForClass(Class tableClass, ConnectionSource connectionSource, List<EntityConfig> subclasses) throws SQLException {
    JoinedEntityConfig joinedEntityConfig = new JoinedEntityConfig(tableClass, connectionSource, subclasses);

    return joinedEntityConfig;
  }

  protected void readEntityAnnotations(Class<?> entityClass, EntityConfig entityConfig) throws SQLException {
    entityConfig.setTableName(getEntityTableName(entityClass));

    readEntityAnnotation(entityClass, entityConfig);
    readTableAnnotation(entityClass, entityConfig);
    readAccessAnnotation(entityClass, entityConfig);
  }

  protected void readEntityAnnotation(Class<?> entityClass, EntityConfig entityConfig) throws SQLException {
    if(entityClass.isAnnotationPresent(Entity.class) == false)
      throw new SQLException("@Entity annotation is not set on Entity " + entityConfig);

    Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
    if(StringHelper.stringNotNullOrEmpty(entityAnnotation.name()))
      entityConfig.setEntityName(entityAnnotation.name());
  }

  protected void readTableAnnotation(Class<?> entityClass, EntityConfig entityConfig) throws SQLException {
    if(entityClass.isAnnotationPresent(Table.class)) {
      Table tableAnnotation = entityClass.getAnnotation(Table.class);

      entityConfig.setCatalogName(tableAnnotation.catalog());
      if(StringHelper.stringNotNullOrEmpty(tableAnnotation.catalog())) // TODO: remove as soon as Catalog is respected at table creation
        throw new SQLException("Catalog (as on class " + entityClass + ") is " + NotSupportedExceptionTrailMessage);

      entityConfig.setSchemaName(tableAnnotation.schema());
      if(StringHelper.stringNotNullOrEmpty(tableAnnotation.schema())) // TODO: remove as soon as Schema is respected at table creation
        throw new SQLException("Schema (as on class " + entityClass + ") is " + NotSupportedExceptionTrailMessage);

      entityConfig.setUniqueConstraints(tableAnnotation.uniqueConstraints());
      if(tableAnnotation.uniqueConstraints().length > 0) // TODO: remove as soon as Unique constraints are respected at table creation
        throw new SQLException("Unique Contraints (as on class " + entityClass + ") are " + NotSupportedExceptionTrailMessage);

      entityConfig.setIndexes(tableAnnotation.indexes());
      if(tableAnnotation.indexes().length > 0) // TODO: remove as soon as Indexes are respected at table creation
        throw new SQLException("Indexes (as on class " + entityClass + ") are " + NotSupportedExceptionTrailMessage);
    }
  }

  protected void readAccessAnnotation(Class<?> entityClass, EntityConfig entityConfig) throws SQLException {
    if(entityClass.isAnnotationPresent(Access.class)) {
      Access accessAnnotation = entityClass.getAnnotation(Access.class);
      entityConfig.setAccess(accessAnnotation.value());
    }
  }


  protected void findLifeCycleEvents(Class dataClass, EntityConfig entityConfig) {
    for (Class<?> classWalk = dataClass; classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(classIsEntityOrMappedSuperclass(dataClass)) {
        for (Method method : classWalk.getDeclaredMethods()) {
          checkMethodForLifeCycleEvents(method, entityConfig);
        }
      }
    }
  }

  protected void checkMethodForLifeCycleEvents(Method method, EntityConfig entityConfig) {
//    List<Annotation> methodAnnotations = Arrays.asList(method.getAnnotations());

    // TODO: i don't know what the specifications says but i implemented it this way that superclass life cycle events don't overwrite that ones from child classes
    // (or should both be called?)
    if(method.isAnnotationPresent(PrePersist.class))
      entityConfig.addPrePersistLifeCycleMethod(method);
    if(method.isAnnotationPresent(PostPersist.class))
      entityConfig.addPostPersistLifeCycleMethod(method);
    if(method.isAnnotationPresent(PostLoad.class))
      entityConfig.addPostLoadLifeCycleMethod(method);
    if(method.isAnnotationPresent(PreUpdate.class))
      entityConfig.addPreUpdateLifeCycleMethod(method);
    if(method.isAnnotationPresent(PostUpdate.class))
      entityConfig.addPostUpdateLifeCycleMethod(method);
    if(method.isAnnotationPresent(PreRemove.class))
      entityConfig.addPreRemoveLifeCycleMethod(method);
    if(method.isAnnotationPresent(PostRemove.class))
      entityConfig.addPostRemoveLifeCycleMethod(method);
  }

  // TODO: try to remove static modifiers


  public static String getEntityTableName(Class<?> entityClass) {
    if(entityClass.isAnnotationPresent(Table.class)) {
      Table tableAnnotation = entityClass.getAnnotation(Table.class);
      if(StringHelper.stringNotNullOrEmpty(tableAnnotation.name())) {
        return tableAnnotation.name();
      }
    }

    if(entityClass.isAnnotationPresent(Entity.class)) {
      Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
      if(StringHelper.stringNotNullOrEmpty(entityAnnotation.name())) {
        return entityAnnotation.name();
      }
    }

    return entityClass.getSimpleName().toLowerCase(); // if table name isn't specified otherwise, it is the class name lowercased
  }


  public static InheritanceHierarchy getInheritanceHierarchyForClass(Class entity) {
    InheritanceHierarchy hierarchy = new InheritanceHierarchy();
    List<Class> currentHierarchyTypeSubclasses = new ArrayList<>();

    for (Class<?> classWalk = entity; classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(JavaxPersistenceImpl.classIsEntityOrMappedSuperclass(classWalk) == false)
        break;

//      currentHierarchyTypeSubclasses.add(classWalk);

      if(classWalk.isAnnotationPresent(Inheritance.class)) {
        Inheritance inheritanceAnnotation = classWalk.getAnnotation(Inheritance.class);
        InheritanceType inheritanceStrategy = inheritanceAnnotation.strategy();

        EntityInheritance entityInheritance = new EntityInheritance(classWalk, inheritanceStrategy, currentHierarchyTypeSubclasses);
        currentHierarchyTypeSubclasses = new ArrayList<>();

        if(classWalk.isAnnotationPresent(DiscriminatorColumn.class)) {
          DiscriminatorColumn discriminatorColumnAnnotation = classWalk.getAnnotation(DiscriminatorColumn.class);
          entityInheritance.setDiscriminatorColumn(discriminatorColumnAnnotation);
        }

        hierarchy.addEntityHierarchyAtTop(entityInheritance);
      }
      else
        currentHierarchyTypeSubclasses.add(classWalk);
    }

    return hierarchy;
  }


  public static boolean classIsEntity(Class dataClass) {
    return dataClass.isAnnotationPresent(Entity.class);
  }

  public static boolean classIsMappedSuperClass(Class dataClass) {
    return dataClass.isAnnotationPresent(MappedSuperclass.class);
  }

  public static boolean classIsEntityOrMappedSuperclass(Class dataClass) {
    return classIsEntity(dataClass) || classIsMappedSuperClass(dataClass);
  }

  protected InheritanceType getInheritanceStrategyIfEntityIsInheritanceStartEntity(Class entityClass) {
    if(isInheritanceClass(entityClass))
      return getInheritanceStrategy(entityClass);

    for (Class<?> classWalk = entityClass.getSuperclass(); classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(classIsMappedSuperClass(classWalk) == false) // only check super classes belonging directly to this Entity (MappedSuperClasses), not other Entities
        break;

      if(isInheritanceClass(classWalk))
        return getInheritanceStrategy(classWalk);
    }

    return null;
  }

  public boolean isInheritanceClass(Class dataClass) {
    return dataClass.isAnnotationPresent(Inheritance.class);
  }

  public InheritanceType getInheritanceStrategy(Class dataClass) {
    Inheritance inheritanceAnnotation = (Inheritance)dataClass.getAnnotation(Inheritance.class);
    return inheritanceAnnotation.strategy();
  }

}
