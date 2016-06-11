package net.dankito.deepthought.data.compare;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 10/01/15.
 */
public class DefaultDataComparer implements IDataComparer {

  public DataCompareResult compareDataToCurrent(BaseEntity data) {
    DataCompareResult result = new DataCompareResult();

    compareEntityToCurrent(data, result);

    return result;
  }

  protected void compareEntityToCurrent(BaseEntity entity, DataCompareResult result) {
    if(result.hasCompareResultForEntity(entity) == true) // cyclic dependency detected
      return;

    BaseEntity currentEntity = Application.getEntityManager().getEntityById(entity.getClass(), entity.getId());

    if(currentEntity == null) {
      if(entity.isDeleted() == false)
        result.addCreatedEntity(entity);
    }
    else if(entity.getCreatedOn().equals(currentEntity.getCreatedOn()) == false) {
      result.addEntitiesThatDoNotMatch(entity, currentEntity);
    }
    else if(entity.isDeleted() == true && currentEntity.isDeleted() == false) {
      result.addDeletedEntity(entity);
    }
    else if(entity.isDeleted() == false && currentEntity.isDeleted() == true) {
      result.addDeletedCurrentEntity(entity);
    }
    else if(entity.getModifiedOn().equals(currentEntity.getModifiedOn())) {
      result.addUnchangedEntities(entity, currentEntity);
    }
    else if(entity.getModifiedOn().compareTo(currentEntity.getModifiedOn()) > 0) {
      result.addEntitiesNewerThanCurrent(entity, currentEntity);
    }
    else if(entity.getModifiedOn().compareTo(currentEntity.getModifiedOn()) < 0) {
      result.addEntitiesOlderThanCurrent(entity, currentEntity);
    }

    compareEntityCollectionFields(entity, currentEntity, result);
  }

  protected void compareEntityCollectionFields(BaseEntity entity, BaseEntity currentEntity, DataCompareResult result) {
    for(Field collectionField : ReflectionHelper.findCollectionsChildrenFields(entity.getClass())) {
      Collection<BaseEntity> entityCollection = ReflectionHelper.getCollectionFieldValue(entity, collectionField);
      Collection<BaseEntity> currentEntityCollection = ReflectionHelper.getCollectionFieldValue(currentEntity, collectionField);

      findCreatedCurrentItems(result, entityCollection, currentEntityCollection);

      for(BaseEntity entityCollectionItem : entityCollection)
        compareEntityToCurrent(entityCollectionItem, result);
    }
  }

  protected void findCreatedCurrentItems(DataCompareResult result, Collection<BaseEntity> entityCollection, Collection<BaseEntity> currentEntityCollection) {
    Map<Long, BaseEntity> entityCollectionMappedById = getBaseEntityCollectionMappedById(entityCollection);
    for(BaseEntity currentEntityCollectionItem : currentEntityCollection) {
      if(entityCollectionMappedById.containsKey(currentEntityCollectionItem.getId()) == false)
        result.addCreatedCurrentEntity(currentEntityCollectionItem);
    }
  }

  protected Map<Long, BaseEntity> getBaseEntityCollectionMappedById(Collection<BaseEntity> entityCollection) {
    Map<Long, BaseEntity> entitiesMappedById = new HashMap<>();

    for(BaseEntity entity : entityCollection)
      entitiesMappedById.put(entity.getId(), entity);

    return entitiesMappedById;
  }
}
