package net.dankito.deepthought.controls.utils;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;

import java.util.Set;

import javafx.collections.ObservableSet;

/**
 * Created by ganymed on 29/07/15.
 */
public interface IEditedEntitiesHolder<T extends UserDataEntity> extends ICleanUp {

  ObservableSet<T> getEditedEntities();

  Set<T> getAddedEntities();

  Set<T> getRemovedEntities();

  void addEntityToEntry(T entity);

  void removeEntityFromEntry(T entity);

  boolean containsEditedEntity(T entity);

}
