package com.j256.ormlite.jpa;

import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;
import com.j256.ormlite.misc.TableInfoRegistry;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 17/11/14.
 */
public class SingleTableEntityConfig<T, ID> extends InheritanceEntityConfig<T, ID> {

  protected List<PropertyConfig> allTableFieldsList = null;
  protected PropertyConfig[] allTableFields = null;


  // TODO: change parameter to List<EntityConfig> subEntities
  public SingleTableEntityConfig(Class entityClass, ConnectionSource connectionSource, List<EntityConfig> subEntities) throws SQLException {
    super(entityClass, connectionSource, subEntities, InheritanceType.SINGLE_TABLE);
    allTableFieldsList = new ArrayList<>(Arrays.asList(getPropertyConfigs()));

//    if(subEntities.size() > 0) {
//      if(subEntities.get(0) instanceof Class) // TODO: remove
//        addSubClassesToTable(subEntities, connectionSource);
////      else
////        addInheritanceLevelSubEntities(subEntities, connectionSource);
//    }
  }

  // TODO: change parameter to List<EntityConfig> subclasses
  public void addSubClassesToTable(List<Class> subclasses, ConnectionSource connectionSource) throws SQLException {
    EntityConfig previousEntityConfig = null;

    for(int i = subclasses.size() - 1; i >= 0; i--) {
      Class subClass = subclasses.get(i);
      if(subEntities.contains(subClass))
        continue;

      allTableFieldsList.addAll(TableInfoRegistry.getClassFieldTypes(subClass, false, tableName, connectionSource));
      allTableFields = null;

      EntityConfig classEntityConfig = null;
      if (TableInfoRegistry.getInstance().hasTableInfoForClass(subClass) == false) {
        List<Field> classFields = TableInfoRegistry.getClassColumns(subClass, true);
        List<PropertyConfig> classColumns = TableInfoRegistry.createFieldTypesFromColumns(tableName, connectionSource, classFields);

        classEntityConfig = new EntityConfig(subClass, connectionSource, tableName, classColumns.toArray(new PropertyConfig[classColumns.size()]));
        TableInfoRegistry.getInstance().registerTableInfo(subClass, classEntityConfig);
      } else {
        classEntityConfig = TableInfoRegistry.getInstance().getTableInfoForClass(subClass);
        classEntityConfig.setTableName(tableName);
      }

      classEntityConfig.setInheritance(InheritanceType.SINGLE_TABLE);
      classEntityConfig.setInheritanceTopLevelEntityConfig(this);

      if(previousEntityConfig != null) {
        previousEntityConfig.setParentEntityConfig(classEntityConfig);
        classEntityConfig.addChildTableInfo(previousEntityConfig);
      }

      previousEntityConfig = classEntityConfig;
//      subEntities.add(subClass);
    }
  }


  public List<PropertyConfig> getAllTableFieldsList() {
    return allTableFieldsList;
  }

  public PropertyConfig[] getAllTableFields() {
    if(allTableFields == null)
      allTableFields = allTableFieldsList.toArray(new PropertyConfig[allTableFieldsList.size()]);
    return allTableFields;
  }
}
