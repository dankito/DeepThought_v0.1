package net.dankito.deepthought.data.model.listener;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 31/01/15.
 */
public interface EntityListener {

  public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue);

  public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity);

  public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity);

  public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity);

}
