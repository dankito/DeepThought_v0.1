package net.dankito.deepthought.data.listener;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 20/09/16.
 */
public interface IExternalCallableEntityChangesService extends IEntityChangesService {


  void informEntityCreatedListeners(BaseEntity entity);

  void informEntityUpdatedListeners(BaseEntity entity, String propertyName, Object previousValue, Object newValue);

  void informEntityDeletedListeners(BaseEntity entity);

  void informEntityAddedToCollectionListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity);

  void informEntityRemovedFromCollectionListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity);

}
