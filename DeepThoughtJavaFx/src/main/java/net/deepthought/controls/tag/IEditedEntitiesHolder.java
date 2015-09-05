package net.deepthought.controls.tag;

import net.deepthought.data.persistence.db.UserDataEntity;

import javafx.collections.ObservableSet;

/**
 * Created by ganymed on 29/07/15.
 */
public interface IEditedEntitiesHolder<T extends UserDataEntity> {

  ObservableSet<T> getEditedEntities();

  void addEntityToEntry(T entity);

  void removeEntityFromEntry(T entity);

  boolean containsEditedEntity(T entity);
}
