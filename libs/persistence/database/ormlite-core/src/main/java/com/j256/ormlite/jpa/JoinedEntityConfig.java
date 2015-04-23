package com.j256.ormlite.jpa;

import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;
import com.j256.ormlite.misc.JavaxPersistenceImpl;
import com.j256.ormlite.misc.TableInfoRegistry;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 17/11/14.
 */
public class JoinedEntityConfig<T, ID> extends InheritanceEntityConfig<T, ID> {

  protected Map<String, List<PropertyConfig>> subClassesFieldTypes = new HashMap<>();


  // TODO: change parameter to List<EntityConfig> subEntities
  public JoinedEntityConfig(Class entityClass, ConnectionSource connectionSource, List<EntityConfig> subEntities) throws SQLException {
    super(entityClass, connectionSource, subEntities, InheritanceType.JOINED);

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
      Class<?> subClass = subclasses.get(i);
      if(subEntities.contains(subClass))
        continue;

      EntityConfig classEntityConfig = null;
      String classTableName = JavaxPersistenceImpl.getEntityTableName(subClass);
      List<PropertyConfig> classColumnsList = TableInfoRegistry.getClassFieldTypes(subClass, false, classTableName, connectionSource);
      classColumnsList.add(0, new InheritanceSubTableIdPropertyConfig(subClass, classTableName, connectionSource, getIdProperty()));
      PropertyConfig[] classColumns = classColumnsList.toArray(new PropertyConfig[classColumnsList.size()]);

      if (TableInfoRegistry.getInstance().hasTableInfoForClass(subClass) == false) {
        classEntityConfig = new EntityConfig(subClass, connectionSource, classTableName, classColumns);
        TableInfoRegistry.getInstance().registerTableInfo(subClass, classEntityConfig);
      } else {
        classEntityConfig = TableInfoRegistry.getInstance().getTableInfoForClass(subClass);
        classEntityConfig.setPropertyConfigs(classColumns);
      }

      classEntityConfig.setInheritance(InheritanceType.JOINED);
      classEntityConfig.setInheritanceTopLevelEntityConfig(this);

      mapDiscriminatorValuesToSubEntities.put(JavaxPersistenceImpl.GetEntityDiscriminatorValue(subClass), classEntityConfig);

      if(previousEntityConfig != null) {
        previousEntityConfig.setParentEntityConfig(classEntityConfig);
        classEntityConfig.addChildTableInfo(previousEntityConfig);
      }

      previousEntityConfig = classEntityConfig;
//      subEntities.add(subClass);
    }
  }


  public List<PropertyConfig> getSubClassFieldTypes(String discriminatorValue) {
    if(subClassesFieldTypes.containsKey(discriminatorValue) == false)
      subClassesFieldTypes.put(discriminatorValue, findSubClassFieldTypes(discriminatorValue));
    return subClassesFieldTypes.get(discriminatorValue);
  }

  protected List<PropertyConfig> findSubClassFieldTypes(String discriminatorValue) {
    List<PropertyConfig> subClassPropertyConfigs = new ArrayList<>();

    for(EntityConfig subClassEntityConfig : (List<EntityConfig>) getEntityForDiscriminatorValue(discriminatorValue).getTopDownInheritanceHierarchy()) { // why is the cast needed?
      subClassPropertyConfigs.addAll(Arrays.asList(subClassEntityConfig.getPropertyConfigs()));
    }

    return subClassPropertyConfigs;
  }
}
