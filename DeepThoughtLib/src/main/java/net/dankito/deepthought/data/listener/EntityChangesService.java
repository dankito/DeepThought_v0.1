package net.dankito.deepthought.data.listener;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by ganymed on 20/09/16.
 */
public class EntityChangesService implements IExternalCallableEntityChangesService {

  protected Set<AllEntitiesListener> allEntitiesListeners = new CopyOnWriteArraySet<>();


  @Override
  public boolean addAllEntitiesListener(AllEntitiesListener listener) {
    return allEntitiesListeners.add(listener);
  }

  @Override
  public boolean removeAllEntitiesListener(AllEntitiesListener listener) {
    return allEntitiesListeners.remove(listener);
  }


  @Override
  public void informEntityCreatedListeners(BaseEntity entity) {
    callEntityCreatedListeners(entity);
  }

  @Override
  public void informEntityUpdatedListeners(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
    callEntityUpdatedListeners(entity, propertyName, previousValue, newValue);
  }

  @Override
  public void informEntityDeletedListeners(BaseEntity entity) {
    callEntityDeletedListeners(entity);
  }

  @Override
  public void informEntityAddedToCollectionListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
    callEntityAddedToCollectionListeners(collectionHolder, collection, addedEntity);
  }

  @Override
  public void informEntityRemovedFromCollectionListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
    callEntityRemovedFromCollectionListeners(collectionHolder, collection, addedEntity);
  }


  protected void callEntityCreatedListeners(BaseEntity entity) {
    for(AllEntitiesListener listener : allEntitiesListeners) {
      listener.entityCreated(entity);
    }
  }

  protected void callEntityUpdatedListeners(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
    for(AllEntitiesListener listener : allEntitiesListeners) {
      listener.entityUpdated(entity, propertyName, previousValue, newValue);
    }
  }

  protected void callEntityDeletedListeners(BaseEntity entity) {
    for(AllEntitiesListener listener : allEntitiesListeners) {
      listener.entityDeleted(entity);
    }
  }

  protected void callEntityAddedToCollectionListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
    for(AllEntitiesListener listener : allEntitiesListeners) {
      listener.entityAddedToCollection(collectionHolder, collection, addedEntity);
    }
  }

  protected void callEntityRemovedFromCollectionListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
    for(AllEntitiesListener listener : allEntitiesListeners) {
      listener.entityRemovedFromCollection(collectionHolder, collection, addedEntity);
    }
  }

}
