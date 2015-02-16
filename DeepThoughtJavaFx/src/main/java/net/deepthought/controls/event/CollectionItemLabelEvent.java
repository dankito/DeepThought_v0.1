package net.deepthought.controls.event;

import net.deepthought.controls.CollectionItemLabel;

import javafx.event.Event;

/**
 * Created by ganymed on 30/11/14.
 */
public class CollectionItemLabelEvent extends Event {

  protected CollectionItemLabel label;

  public CollectionItemLabelEvent(Event event, CollectionItemLabel label) {
    super(event.getSource(), event.getTarget(), event.getEventType());

    this.label = label;
  }


  public CollectionItemLabel getLabel() {
    return label;
  }

}
