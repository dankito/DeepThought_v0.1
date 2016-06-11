package net.dankito.deepthought.controls.event;

import java.util.Collection;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by ganymed on 30/11/14.
 */
public class EntityCollectionEditedEvent<T> extends Event {

  protected Collection<T> editedEntities;

  protected T addedOrRemovedEntity;


  public EntityCollectionEditedEvent(Collection<T> editedEntities, T addedOrRemovedEntity) {
    super(EventType.ROOT);

    this.editedEntities = editedEntities;
    this.addedOrRemovedEntity = addedOrRemovedEntity;
  }


  public Collection<T> getEditedEntities() {
    return editedEntities;
  }

  public T getAddedOrRemovedEntity() {
    return addedOrRemovedEntity;
  }

}
