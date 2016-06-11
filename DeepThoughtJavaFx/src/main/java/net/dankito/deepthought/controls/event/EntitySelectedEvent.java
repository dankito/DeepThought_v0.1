package net.dankito.deepthought.controls.event;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by ganymed on 30/11/14.
 */
public class EntitySelectedEvent<T> extends Event {

  protected T selectedEntity;


  public EntitySelectedEvent(T selectedEntity) {
    super(EventType.ROOT);

    this.selectedEntity = selectedEntity;
  }


  public T getSelectedEntity() {
    return selectedEntity;
  }

}
