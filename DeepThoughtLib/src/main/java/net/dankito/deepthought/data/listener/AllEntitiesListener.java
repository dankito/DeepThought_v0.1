package net.dankito.deepthought.data.listener;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 31/01/15.
 */
public interface AllEntitiesListener {

  void entityCreated(BaseEntity entity);

  void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue);

  void entityDeleted(BaseEntity entity);

  void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity);

  void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity);

}
