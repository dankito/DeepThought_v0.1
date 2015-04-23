package com.j256.ormlite.misc;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.JoinedEntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.SingleTableEntityConfig;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 17/11/14.
 */
public class TableInfoRegistry {

  protected static TableInfoRegistry instance = null;

  public static TableInfoRegistry getInstance() {
    if(instance == null)
      instance = new TableInfoRegistry();
    return instance;
  }

  public static void setTableInfoRegistry(TableInfoRegistry registry) {
    instance = registry;
  }


  protected Map<Class, EntityConfig> tableInfoMap = new HashMap<>();

  protected TableInfoRegistry() {

  }


  public void clear() {
    tableInfoMap.clear();
  }

  public void registerTableInfo(Class tableClass, EntityConfig entityConfig) {
    tableInfoMap.put(tableClass, entityConfig);
  }

  public boolean hasTableInfoForClass(Class tableClass) {
    return tableInfoMap.containsKey(tableClass);
  }

  public EntityConfig getTableInfoForClass(Class tableClass){
    return tableInfoMap.get(tableClass);
  }

  public <T, ID> EntityConfig<T, ID> getAndMayCreateTableInfoForClass(Class tableClass, ConnectionSource connectionSource) throws SQLException {
    return getAndMayCreateTableInfoForClass(tableClass, connectionSource, null);
  }

  public <T, ID> EntityConfig<T, ID> getAndMayCreateTableInfoForClass(Class tableClass, ConnectionSource connectionSource, Dao dao) throws SQLException {
    if(hasTableInfoForClass(tableClass) == false) {
      registerTableInfo(tableClass, createTableInfo(tableClass, connectionSource, dao));
    }

    return getTableInfoForClass(tableClass);
  }

  public <T, ID> EntityConfig<T, ID> getAndMayCreateTableInfoForClass(DatabaseType databaseType, DatabaseTableConfig tableConfig) throws SQLException {
    return getAndMayCreateTableInfoForClass(databaseType, tableConfig, null);
  }

  public <T, ID> EntityConfig<T, ID> getAndMayCreateTableInfoForClass(DatabaseType databaseType, DatabaseTableConfig tableConfig, Dao dao) throws SQLException {
    if(hasTableInfoForClass(tableConfig.getDataClass()) == false) {
      registerTableInfo(tableConfig.getDataClass(), createTableInfo(databaseType, tableConfig, dao));
    }

    return getTableInfoForClass(tableConfig.getDataClass());
  }

  protected EntityConfig getAndMayCreateInheritanceTableInfoForClass(Class<?> dataClass, ConnectionSource connectionSource, List<Class> currentInheritanceTypeSubclasses) throws SQLException {
    EntityConfig inheritanceEntityConfig = getTableInfoForClass(dataClass);

    if(hasTableInfoForClass(dataClass) == false || inheritanceEntityConfig instanceof InheritanceEntityConfig == false) {
      inheritanceEntityConfig = createInheritanceTableInfo(dataClass, connectionSource, currentInheritanceTypeSubclasses);
      registerTableInfo(dataClass, inheritanceEntityConfig);
    }
    else {
      ((InheritanceEntityConfig) inheritanceEntityConfig).addSubClassesToTable(currentInheritanceTypeSubclasses, connectionSource);
    }

    return getTableInfoForClass(dataClass);
  }


//  public TableInfo[] createTableInfos(ConnectionSource connectionSource, Class[] entityClasses) throws SQLException {
//    return createTableInfos(connectionSource, Arrays.asList(entityClasses));
//  }

  public EntityConfig[] createTableInfos(ConnectionSource connectionSource, Class... entityClasses) throws SQLException {
    List<EntityConfig> entityConfigs = new ArrayList<>();

    for(Class entityClass : entityClasses)
      entityConfigs.add(createTableInfoHierarchy(connectionSource, entityClass));

    return entityConfigs.toArray(new EntityConfig[entityConfigs.size()]);
  }

  public EntityConfig createTableInfoHierarchy(ConnectionSource connectionSource, Class<?> entityClass) throws SQLException {
    EntityConfig entityClassEntityConfig = null;
    List<Class> currentInheritanceTypeSubclasses = new ArrayList<>();
    EntityConfig currentEntityConfig = null, previousEntityConfig = null;

    for (Class<?> classWalk = entityClass; classWalk != null; classWalk = classWalk.getSuperclass()) {
      if (JavaxPersistenceImpl.classIsEntityOrMappedSuperclass(classWalk) == false)
        break;

//      currentInheritanceTypeSubclasses.add(classWalk);

      if (JavaxPersistenceImpl.IsInheritanceLevelStartEntity(classWalk)) {
        currentEntityConfig = getAndMayCreateInheritanceTableInfoForClass(classWalk, connectionSource, currentInheritanceTypeSubclasses);
      }
      else if(classWalk.isAnnotationPresent(Entity.class)) {
        currentInheritanceTypeSubclasses.add(classWalk);
        currentEntityConfig = getAndMayCreateTableInfoForClass(classWalk, connectionSource);
      }
      else {
        // TODO: get fields from Mapped super class
        if(previousEntityConfig != null) {
//          previousTableInfo.fie
        }
        continue; // don't create TableInfo for MappedSuperclass
      }

      if(classWalk.equals(entityClass))
        entityClassEntityConfig = currentEntityConfig;

      if(previousEntityConfig != null) {
        previousEntityConfig.setParentEntityConfig(currentEntityConfig);
        currentEntityConfig.addChildTableInfo(previousEntityConfig);
      }

      previousEntityConfig = currentEntityConfig;
    }

    return entityClassEntityConfig;
  }

  protected <T, ID> EntityConfig<T, ID> createTableInfo(Class tableClass, ConnectionSource connectionSource, Dao dao) throws SQLException {
    if(JavaxPersistenceImpl.IsInheritanceLevelStartEntity(tableClass))
      return createInheritanceTableInfo(tableClass, connectionSource, new ArrayList<Class>()); // TODO: what to do with dao?

    return new EntityConfig<T, ID>(connectionSource, dao, tableClass);
  }

  protected <T, ID> EntityConfig<T, ID> createTableInfo(DatabaseType databaseType, DatabaseTableConfig tableConfig, Dao dao) throws SQLException {
    return new EntityConfig<T, ID>(databaseType, dao, tableConfig); // TODO: check for inheritance type
  }

  protected InheritanceEntityConfig createInheritanceTableInfo(Class<?> dataClass, ConnectionSource connectionSource, List<Class> currentInheritanceTypeSubclasses) throws SQLException {
    Inheritance inheritanceAnnotation = dataClass.getAnnotation(Inheritance.class);
    InheritanceType inheritanceStrategy = inheritanceAnnotation.strategy();

    switch (inheritanceStrategy) {
      case SINGLE_TABLE:
        return createSingleTableTableInfoForClass(dataClass, currentInheritanceTypeSubclasses, connectionSource);
      case JOINED:
        return createJoinedTableInfoForClass(dataClass, currentInheritanceTypeSubclasses, connectionSource);
      // TODO: implement TABLE_PER_CLASS (or throw at least an Exception that it's currently not supported
    }

//    return getAndMayCreateTableInfoForClass(dataClass, connectionSource); // produces a Stack Overflow
    return null;
  }

  public SingleTableEntityConfig createSingleTableTableInfoForClass(Class tableClass, List<Class> subclasses, ConnectionSource connectionSource) throws SQLException {
    SingleTableEntityConfig singleTableTableInfo = null;

    if(hasTableInfoForClass(tableClass) == false || getTableInfoForClass(tableClass) instanceof SingleTableEntityConfig == false) {
      singleTableTableInfo = new SingleTableEntityConfig(tableClass, connectionSource, subclasses);
      registerTableInfo(tableClass, singleTableTableInfo);
    }
    else {
      singleTableTableInfo = (SingleTableEntityConfig)getTableInfoForClass(tableClass);
      singleTableTableInfo.addSubClassesToTable(subclasses, connectionSource);
    }

    return singleTableTableInfo;
  }

  public JoinedEntityConfig createJoinedTableInfoForClass(Class tableClass, List<Class> subclasses, ConnectionSource connectionSource) throws SQLException {
    JoinedEntityConfig joinedTableInfo = null;

    if(hasTableInfoForClass(tableClass) == false || getTableInfoForClass(tableClass) instanceof JoinedEntityConfig == false) {
      joinedTableInfo = new JoinedEntityConfig(tableClass, connectionSource, subclasses);
      registerTableInfo(tableClass, joinedTableInfo);
    }
    else {
      joinedTableInfo = (JoinedEntityConfig)getTableInfoForClass(tableClass);
      joinedTableInfo.addSubClassesToTable(subclasses, connectionSource);
    }

    return joinedTableInfo;
  }

  public static List<Field> getClassColumns(Class dataClass, boolean includeParentClassesColumns) {
    List<Field> columns = new ArrayList<>();

    columns.addAll(DatabaseTableConfig.getClassColumns(dataClass));

    if(includeParentClassesColumns) {
      for (Class<?> classWalk = dataClass.getSuperclass(); classWalk != null; classWalk = classWalk.getSuperclass()) {
        if(JavaxPersistenceImpl.classIsEntityOrMappedSuperclass(classWalk) == false) // mapped entities stop here
          break;

        columns.addAll(DatabaseTableConfig.getClassColumns(classWalk));

        if(classWalk.isAnnotationPresent(Inheritance.class) == true) // a new inheritance level starts here -> don't go further in adding columns
          break;
      }
    }

    return columns;
  }

  public static List<PropertyConfig> createFieldTypesFromColumns(String tableName, ConnectionSource connectionSource, List<Field> classFields) throws SQLException {
    List<PropertyConfig> propertyConfigs = new ArrayList<>();

    for(Field column : classFields) {
      PropertyConfig propertyConfig = Instances.getFieldTypeCreator().createFieldType(connectionSource, tableName, column, column.getDeclaringClass());
      if(propertyConfig != null)
        propertyConfigs.add(propertyConfig);
    }

    return propertyConfigs;
  }

  public static List<PropertyConfig> getClassFieldTypes(Class dataClass, boolean includeParentClassesColumns, String tableName, ConnectionSource connectionSource) throws SQLException {
    return createFieldTypesFromColumns(tableName, connectionSource, getClassColumns(dataClass, includeParentClassesColumns));
  }
}
