package net.dankito.deepthought.controls.utils;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;

/**
 * Created by ganymed on 21/09/15.
 */
public class EditedEntitiesHolder<T extends UserDataEntity> implements IEditedEntitiesHolder<T>, ICleanUp {

  protected ObservableSet<T> editedEntities = FXCollections.observableSet();
  protected Set<T> addedEntities = new HashSet<>();
  protected Set<T> removedEntities = new HashSet<>();

  protected EventHandler<net.dankito.deepthought.controls.event.EntityCollectionEditedEvent> entityAddedEventHandler = null;
  protected EventHandler<net.dankito.deepthought.controls.event.EntityCollectionEditedEvent> entityRemovedEventHandler = null;


  public EditedEntitiesHolder() {

  }

  public EditedEntitiesHolder(Collection<T> currentEntities) {
    setCurrentEntities(currentEntities);
  }

  public EditedEntitiesHolder(Collection<T> currentEntities, EventHandler<net.dankito.deepthought.controls.event.EntityCollectionEditedEvent> entityAddedEventHandler, EventHandler<net.dankito.deepthought.controls.event.EntityCollectionEditedEvent> entityRemovedEventHandler) {
    this(currentEntities);
    this.entityAddedEventHandler = entityAddedEventHandler;
    this.entityRemovedEventHandler = entityRemovedEventHandler;
  }


  @Override
  public void cleanUp() {
    entityAddedEventHandler = null;
    entityRemovedEventHandler = null;

    editedEntities.clear();
    editedEntities = null;

    addedEntities.clear();
    addedEntities = null;

    removedEntities.clear();
    removedEntities = null;
  }


  protected void setCurrentEntities(Collection<T> entities) {
    if(entities instanceof ObservableSet)
      editedEntities = (ObservableSet<T>)entities;
    else
      editedEntities = FXCollections.observableSet(new HashSet<T>(entities));

    addedEntities.clear();
    removedEntities.clear();
  }


  @Override
  public ObservableSet<T> getEditedEntities() {
    return editedEntities;
  }

  @Override
  public Set<T> getAddedEntities() {
    return addedEntities;
  }

  @Override
  public Set<T> getRemovedEntities() {
    return removedEntities;
  }

  @Override
  public boolean containsEditedEntity(T entity) {
    return editedEntities.contains(entity);
  }

  @Override
  public void addEntityToEntry(T entity) {
    if(removedEntities.contains(entity)) {
      removedEntities.remove(entity);
    }

    addedEntities.add(entity);

    editedEntities.add(entity);

    fireEntityAddedEvent(entity);

  }

  @Override
  public void removeEntityFromEntry(T entity) {
    if(addedEntities.contains(entity)) {
      addedEntities.remove(entity);
    }

    removedEntities.add(entity);

    editedEntities.remove(entity);

    fireEntityRemovedEvent(entity);
  }



  protected void fireEntityAddedEvent(T entity) {
    if(entityAddedEventHandler != null)
      entityAddedEventHandler.handle(new net.dankito.deepthought.controls.event.EntityCollectionEditedEvent(editedEntities, entity));
  }

  protected void fireEntityRemovedEvent(T entity) {
    if(entityRemovedEventHandler != null)
      entityRemovedEventHandler.handle(new net.dankito.deepthought.controls.event.EntityCollectionEditedEvent(editedEntities, entity));
  }

  public EventHandler<net.dankito.deepthought.controls.event.EntityCollectionEditedEvent> getEntityAddedEventHandler() {
    return entityAddedEventHandler;
  }

  public void setEntityAddedEventHandler(EventHandler<net.dankito.deepthought.controls.event.EntityCollectionEditedEvent> entityAddedEventHandler) {
    this.entityAddedEventHandler = entityAddedEventHandler;
  }

  public EventHandler<net.dankito.deepthought.controls.event.EntityCollectionEditedEvent> getEntityRemovedEventHandler() {
    return entityRemovedEventHandler;
  }

  public void setEntityRemovedEventHandler(EventHandler<net.dankito.deepthought.controls.event.EntityCollectionEditedEvent> entityRemovedEventHandler) {
    this.entityRemovedEventHandler = entityRemovedEventHandler;
  }
}
