package net.dankito.deepthought.data.merger;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.ReflectionHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 10/01/15.
 */
public class DefaultDataMerger implements IDataMerger {

  private final static Logger log = LoggerFactory.getLogger(DefaultDataMerger.class);


  @Override
  public boolean mergeWithCurrentData(List<BaseEntity> data, boolean mergeTheirSubEntitiesAsWell, MergeDataListener listener) {
    List<BaseEntity> entitiesSucceededToInsert = new ArrayList<>();
    List<BaseEntity> entitiesFailedToInsert = new ArrayList<>();
    Set<BaseEntity> mergedEntities = new HashSet<>();

    mergeEntityAndItsSubEntitiesWithDataCollection(data, mergeTheirSubEntitiesAsWell, mergedEntities, entitiesSucceededToInsert, entitiesFailedToInsert, listener);

    if(listener != null)
      listener.addingEntitiesDone(entitiesFailedToInsert.size() == 0, entitiesSucceededToInsert, entitiesFailedToInsert);

    Application.getDataManager().retrieveDeepThoughtApplication();

    return entitiesFailedToInsert.size() == 0;
  }

  protected void mergeEntityAndItsSubEntitiesWithDataCollection(Collection<BaseEntity> data, boolean mergeTheirSubEntitiesAsWell, Set<BaseEntity> mergedEntities, List<BaseEntity> entitiesSucceededToInsert, List<BaseEntity> entitiesFailedToInsert, MergeDataListener listener) {
    for(BaseEntity entity : data) {
      if(mergedEntities.contains(entity) == true)
        break;

      mergedEntities.add(entity);

      if(mergeEntityWithDataCollection(entity, listener))
        entitiesSucceededToInsert.add(entity);
      else
        entitiesFailedToInsert.add(entity);

      if(mergeTheirSubEntitiesAsWell == true) {
        mergeEntityAndItsSubEntitiesWithDataCollection(ReflectionHelper.getBaseEntityChildren(entity), mergeTheirSubEntitiesAsWell, mergedEntities, entitiesSucceededToInsert, entitiesFailedToInsert, listener);

        for (Collection<BaseEntity> collectionChild : ReflectionHelper.getCollectionsChildren(entity))
          mergeEntityAndItsSubEntitiesWithDataCollection(collectionChild, mergeTheirSubEntitiesAsWell, mergedEntities, entitiesSucceededToInsert, entitiesFailedToInsert, listener);
      }
    }
  }

  protected boolean mergeEntityWithDataCollection(BaseEntity entity, MergeDataListener listener) {
    if(listener != null)
      listener.beginToMergeEntity(entity);

    IEntityManager entityManager = Application.getEntityManager();

    if(entity.getId() == null) { // Entity never has been persisted -> try to add to Data collection
      return addEntityToDataCollection(entity, listener);
    }

    BaseEntity persistedCounterpart = entityManager.getEntityById(entity.getClass(), entity.getId());

    if(persistedCounterpart == null || entity.getCreatedOn().equals(persistedCounterpart.getCreatedOn()) == false) { // Entity either not persisted or a different Entity has
      // been persisted with that ID
      resetEntityAndSubEntitiesIds(entity);
      return addEntityToDataCollection(entity, listener);
    }
    else {
      // usually Entity Managers don't accept Entities not created in their persistence context -> take the Entity from their persistence context and copy entity's values to it
      ReflectionHelper.copyObjectFields(persistedCounterpart, entity);
      if (entityManager.updateEntity(persistedCounterpart) == false) {
        log.debug("addEntityToDataCollection(): Could not update Entity {}", entity);
        if (listener != null)
          listener.mergeEntityResult(entity, false, DeepThoughtError.errorFromLocalizationKey("error.could.not.add.entity.to.data.collection", entity.toString()));
        return false;
      }
    }


    // TODO: find out for which Entities i have to call tryToInsertEntity()
//    tryToInsertEntity(application, entitiesSucceededToInsert, entitiesFailedToInsert, backup, listener);
//    tryToInsertEntity(application.getSettings(), entitiesSucceededToInsert, entitiesFailedToInsert, backup, listener);
//
//    for(User user : application.getUsers()) {
//      tryToInsertEntity(user, entitiesSucceededToInsert, entitiesFailedToInsert, backup, listener);
//      for (DeepThought deepThought : user.getDeepThoughts()) {
//        tryToInsertEntity(deepThought, entitiesSucceededToInsert, entitiesFailedToInsert, backup, listener);
//      }
//    }

    if(listener != null)
      listener.mergeEntityResult(entity, true, DeepThoughtError.Success);
    return true;
  }

  @Override
  public boolean addToCurrentData(List<BaseEntity> data, MergeDataListener listener) {
    List<BaseEntity> entitiesSucceededToInsert = new ArrayList<>();
    List<BaseEntity> entitiesFailedToInsert = new ArrayList<>();

    for(BaseEntity entity : data) {
      if(entity.getId() != null) // Entity has already an ID of another Collection. Remove it so it can be inserted to this Data Collection
        resetEntityAndSubEntitiesIds(entity);

      if(addEntityToDataCollection(entity, listener))
        entitiesSucceededToInsert.add(entity);
      else
        entitiesFailedToInsert.add(entity);
    }

    if(listener != null)
      listener.addingEntitiesDone(entitiesFailedToInsert.size() == 0, entitiesSucceededToInsert, entitiesFailedToInsert);

    Application.getDataManager().retrieveDeepThoughtApplication();

    return entitiesFailedToInsert.size() == 0;
  }

  private void resetEntityAndSubEntitiesIds(BaseEntity entity) {
    entity.resetId();

    for(BaseEntity baseEntityChild : ReflectionHelper.getBaseEntityChildren(entity)) {
      if(baseEntityChild.isPersisted() == true)
        resetEntityAndSubEntitiesIds(baseEntityChild);
    }

    for(Collection<BaseEntity> childCollection : ReflectionHelper.getCollectionsChildren(entity)) {
      for(BaseEntity childCollectionItem : childCollection) {
        if(childCollectionItem.isPersisted() == true)
          resetEntityAndSubEntitiesIds(childCollectionItem);
      }
    }

//    if(entity instanceof DeepThoughtApplication) {
//      DeepThoughtApplication application = (DeepThoughtApplication)entity;
//      resetEntityAndSubEntitiesIds(application.getSettings());
//      for(User user : application.getUsers())
//        resetEntityAndSubEntitiesIds(user);
//      for(Group group : application.getGroups())
//        resetEntityAndSubEntitiesIds(group);
//    }
//
//    if(entity instanceof User) {
//      User user = (User)entity;
//      for(DeepThought deepThought : user.getDeepThoughts())
//        resetEntityAndSubEntitiesIds(deepThought);
//    }
//
//    if(entity instanceof DeepThought) {
//      DeepThought deepThought = (DeepThought)entity;
//      resetEntityAndSubEntitiesIds(deepThought.getTopLevelCategory());
//      for(Category category : deepThought.getCategories())
//        resetEntityAndSubEntitiesIds(category);
//      for(Entry entry : deepThought.getEntries())
//        resetEntityAndSubEntitiesIds(entry);
//      for(Tag tag : deepThought.getTags())
//        resetEntityAndSubEntitiesIds(tag);
//      for(Keyword keyword : deepThought.getKeywords())
//        resetEntityAndSubEntitiesIds(keyword);
//      for(Person person : deepThought.getPersons())
//        resetEntityAndSubEntitiesIds(person);
//    }
//
//    if(entity instanceof Entry) {
//      Entry entry = (Entry)entity;
//      for(FileLink file : entry.getAttachedFiles())
//        resetEntityAndSubEntitiesIds(file);
//      for(Note note : entry.getNotes())
//        resetEntityAndSubEntitiesIds(note);
//      // TODO: has this to be done are is this already covered by deepThought.getEntries() ?
//      for(Entry subEntry : entry.getSubEntries())
//        resetEntityAndSubEntitiesIds(subEntry);
//    }
//
//    // TODO: which Entities are there to be also resetted?
  }

  protected boolean addEntityToDataCollection(BaseEntity entity, MergeDataListener listener) {
    if(listener != null)
      listener.beginToMergeEntity(entity);

    IEntityManager entityManager = Application.getEntityManager();

    if(entity.isPersisted() == false) {
      if (entityManager.persistEntity(entity) == true) {
        if(listener != null)
          listener.mergeEntityResult(entity, true, DeepThoughtError.Success);
        return true;
      }

      log.debug("addEntityToDataCollection(): Could not persist Entity {}, let's see if it can be added ...", entity);
    }

    if(entityManager.updateEntity(entity) == false) {
      log.debug("addEntityToDataCollection(): Could not update Entity {}", entity);
      if (listener != null)
        listener.mergeEntityResult(entity, false, DeepThoughtError.errorFromLocalizationKey("error.could.not.add.entity.to.data.collection", entity.toString()));
      return false;
    }


    // TODO: find out for which Entities i have to call tryToInsertEntity()
//    tryToInsertEntity(application, entitiesSucceededToInsert, entitiesFailedToInsert, backup, listener);
//    tryToInsertEntity(application.getSettings(), entitiesSucceededToInsert, entitiesFailedToInsert, backup, listener);
//
//    for(User user : application.getUsers()) {
//      tryToInsertEntity(user, entitiesSucceededToInsert, entitiesFailedToInsert, backup, listener);
//      for (DeepThought deepThought : user.getDeepThoughts()) {
//        tryToInsertEntity(deepThought, entitiesSucceededToInsert, entitiesFailedToInsert, backup, listener);
//      }
//    }

    if(listener != null)
      listener.mergeEntityResult(entity, true, DeepThoughtError.Success);
    return true;
  }

}
