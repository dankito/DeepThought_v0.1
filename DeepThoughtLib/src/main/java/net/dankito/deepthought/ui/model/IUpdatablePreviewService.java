package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

/**
 * Created by ganymed on 22/09/16.
 */
public interface IUpdatablePreviewService {

  void entityUpdated(BaseEntity entity, String propertyName);

  void entityDeleted(BaseEntity entity);

  void collectionOfEntityUpdated(BaseEntity collectionHolder, BaseEntity addedOrRemovedEntity);

  void languageChanged();

}
