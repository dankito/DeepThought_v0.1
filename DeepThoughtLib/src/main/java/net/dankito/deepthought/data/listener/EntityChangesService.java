package net.dankito.deepthought.data.listener;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by ganymed on 20/09/16.
 */
public class EntityChangesService implements IExternalCallableEntityChangesService {

  protected Set<AllEntitiesListener> allEntitiesListeners = new CopyOnWriteArraySet<>();

  protected DeepThought currentDeepThought = null;


  public EntityChangesService() {
    Application.addApplicationListener(applicationListener);
  }


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


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {
      callEntityCreatedListeners(entity);
    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      callEntityUpdatedListeners(entity, propertyName, previousValue, newValue);
    }

    @Override
    public void entityDeleted(BaseEntity entity) {
      callEntityDeletedListeners(entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      callEntityAddedToCollectionListeners(collectionHolder, collection, addedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      callEntityRemovedFromCollectionListeners(collectionHolder, collection, removedEntity);
    }
  };


  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      if(currentDeepThought != null) {
        currentDeepThought.AllEntriesSystemTag().removeEntityListener(systemTagsEntityListener);
        currentDeepThought.EntriesWithoutTagsSystemTag().removeEntityListener(systemTagsEntityListener);
      }

      currentDeepThought = deepThought;

      if(deepThought != null) {
        deepThought.AllEntriesSystemTag().addEntityListener(systemTagsEntityListener);
        deepThought.EntriesWithoutTagsSystemTag().addEntityListener(systemTagsEntityListener);
      }
    }

    @Override
    public void notification(Notification notification) {
      if(notification.getType() == NotificationType.ApplicationInstantiated) {
        Application.getSyncManager().addSynchronizationListener(allEntitiesListener);
        Application.getDataManager().addAllEntitiesListener(allEntitiesListener);
      }
    }
  };


  protected EntityListener systemTagsEntityListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      callEntityUpdatedListeners(entity, propertyName, previousValue, newValue);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      callEntityAddedToCollectionListeners(collectionHolder, collection, addedEntity);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      callEntityRemovedFromCollectionListeners(collectionHolder, collection, removedEntity);
    }
  };

}
