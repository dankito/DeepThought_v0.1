package net.dankito.deepthought.data.compare;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ganymed on 10/01/15.
 */
public class DataCompareResult {

  protected List<BaseEntity> listEntitiesWithCompareResult = new ArrayList<>();
  protected List<BaseEntity> listCurrentEntitiesWithCompareResult = new ArrayList<>();

  protected Map<BaseEntity, BaseEntity> entitiesThatDoNotMatch = new HashMap<>();

  protected Map<BaseEntity, BaseEntity> unchangedEntities = new HashMap<>();
  protected Map<BaseEntity, BaseEntity> entitiesNewerThanCurrent = new HashMap<>();
  protected Map<BaseEntity, BaseEntity> entitiesOlderThanCurrent = new HashMap<>();

  public Set<BaseEntity> createdEntities = new HashSet<>();
  public Set<BaseEntity> deletedEntities = new HashSet<>();

  public Set<BaseEntity> createdCurrentEntities = new HashSet<>();
  public Set<BaseEntity> deletedCurrentEntities = new HashSet<>();



  public void addEntitiesThatDoNotMatch(BaseEntity entity, BaseEntity currentEntity) {
    listEntitiesWithCompareResult.add(entity);
    listCurrentEntitiesWithCompareResult.add(currentEntity);

    entitiesThatDoNotMatch.put(entity, currentEntity);
  }

  public void addUnchangedEntities(BaseEntity entity, BaseEntity currentEntity) {
    listEntitiesWithCompareResult.add(entity);
    listCurrentEntitiesWithCompareResult.add(currentEntity);

    unchangedEntities.put(entity, currentEntity);
  }

  public void addEntitiesNewerThanCurrent(BaseEntity entity, BaseEntity currentEntity) {
    listEntitiesWithCompareResult.add(entity);
    listCurrentEntitiesWithCompareResult.add(currentEntity);

    entitiesNewerThanCurrent.put(entity, currentEntity);
  }

  public void addEntitiesOlderThanCurrent(BaseEntity entity, BaseEntity currentEntity) {
    listEntitiesWithCompareResult.add(entity);
    listCurrentEntitiesWithCompareResult.add(currentEntity);

    entitiesOlderThanCurrent.put(entity, currentEntity);
  }

  public boolean addCreatedEntity(BaseEntity entity) {
    listEntitiesWithCompareResult.add(entity);

    return createdEntities.add(entity);
  }

  public boolean addDeletedEntity(BaseEntity entity) {
    listEntitiesWithCompareResult.add(entity);

    return deletedEntities.add(entity);
  }

  public boolean addCreatedCurrentEntity(BaseEntity entity) {
    listEntitiesWithCompareResult.add(entity);

    return createdCurrentEntities.add(entity);
  }

  public boolean addDeletedCurrentEntity(BaseEntity entity) {
    listEntitiesWithCompareResult.add(entity);

    return deletedCurrentEntities.add(entity);
  }


  public boolean hasCompareResultForEntity(BaseEntity entity) {
    return listEntitiesWithCompareResult.contains(entity);
  }

  public boolean hasCompareResultForCurrentEntity(BaseEntity currentEntity) {
    return listCurrentEntitiesWithCompareResult.contains(currentEntity);
  }


  public CompareResult getCompareResultForEntity(BaseEntity entity) {
    if(unchangedEntities.containsKey(entity))
      return CompareResult.Unchanged;
    else if(createdEntities.contains(entity))
      return CompareResult.Created;
    else if(deletedEntities.contains(entity))
      return CompareResult.Deleted;
    else if(entitiesNewerThanCurrent.containsKey(entity))
      return CompareResult.Newer;
    else if(entitiesOlderThanCurrent.containsKey(entity))
      return CompareResult.Older;
    else if(entitiesThatDoNotMatch.containsKey(entity))
      return CompareResult.NoMatchingEntityFound;

    return CompareResult.Unknown;
  }

  public CompareResult getCompareResultForCurrentEntity(BaseEntity currentEntity) {
    if(unchangedEntities.containsValue(currentEntity))
      return CompareResult.Unchanged;
    else if(createdCurrentEntities.contains(currentEntity))
      return CompareResult.Created;
    else if(deletedCurrentEntities.contains(currentEntity))
      return CompareResult.Deleted;
    else if(entitiesOlderThanCurrent.containsValue(currentEntity))
      return CompareResult.Newer;
    else if(entitiesNewerThanCurrent.containsValue(currentEntity))
      return CompareResult.Older;
    else if(entitiesThatDoNotMatch.containsValue(currentEntity))
      return CompareResult.NoMatchingEntityFound;

    return CompareResult.Unknown;
  }

}
