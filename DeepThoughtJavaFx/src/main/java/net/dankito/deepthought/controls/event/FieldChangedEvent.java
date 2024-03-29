package net.dankito.deepthought.controls.event;

import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;

/**
 * Created by ganymed on 30/11/14.
 */
public class FieldChangedEvent extends Event {

  protected FieldWithUnsavedChanges fieldWithUnsavedChanges;

  protected Object newValue = null;

  public FieldChangedEvent(Node node, FieldWithUnsavedChanges fieldWithUnsavedChanges, Object newValue) {
    super(node, node, EventType.ROOT);

    this.fieldWithUnsavedChanges = fieldWithUnsavedChanges;
    this.newValue = newValue;
  }


  public FieldWithUnsavedChanges getFieldWithUnsavedChanges() {
    return fieldWithUnsavedChanges;
  }

  public Object getNewValue() {
    return newValue;
  }

}
