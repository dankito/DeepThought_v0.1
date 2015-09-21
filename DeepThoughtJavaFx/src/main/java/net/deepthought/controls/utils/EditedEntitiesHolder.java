package net.deepthought.controls.utils;

import net.deepthought.controls.ICleanUp;
import net.deepthought.data.persistence.db.UserDataEntity;

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

  protected EventHandler<EntityCollectionEditedEvent> entityAddedEventHandler = null;
  protected EventHandler<EntityCollectionEditedEvent> entityRemovedEventHandler = null;


  public EditedEntitiesHolder() {

  }

  public EditedEntitiesHolder(Collection<T> currentEntities) {
    setCurrentEntities(currentEntities);
  }

  public EditedEntitiesHolder(Collection<T> currentEntities, EventHandler<EntityCollectionEditedEvent> entityAddedEventHandler, EventHandler<EntityCollectionEditedEvent> entityRemovedEventHandler) {
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
    else {
      addedEntities.add(entity);
    }

    editedEntities.add(entity);

    firePersonAddedEvent(entity);

  }

  @Override
  public void removeEntityFromEntry(T entity) {
    if(addedEntities.contains(entity)) {
      addedEntities.remove(entity);
    }
    else {
      removedEntities.add(entity);
    }

    editedEntities.remove(entity);

    firePersonRemovedEvent(entity);
  }



  protected void firePersonAddedEvent(T entity) {
    if(entityAddedEventHandler != null)
      entityAddedEventHandler.handle(new EntityCollectionEditedEvent(editedEntities, entity));
  }

  protected void firePersonRemovedEvent(T entity) {
    if(entityRemovedEventHandler != null)
      entityRemovedEventHandler.handle(new EntityCollectionEditedEvent(editedEntities, entity));
  }

  public EventHandler<EntityCollectionEditedEvent> getEntityAddedEventHandler() {
    return entityAddedEventHandler;
  }

  public void setEntityAddedEventHandler(EventHandler<EntityCollectionEditedEvent> entityAddedEventHandler) {
    this.entityAddedEventHandler = entityAddedEventHandler;
  }

  public EventHandler<EntityCollectionEditedEvent> getEntityRemovedEventHandler() {
    return entityRemovedEventHandler;
  }

  public void setEntityRemovedEventHandler(EventHandler<EntityCollectionEditedEvent> entityRemovedEventHandler) {
    this.entityRemovedEventHandler = entityRemovedEventHandler;
  }
}
