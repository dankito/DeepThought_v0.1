package net.dankito.deepthought.controls.event;

import javafx.event.Event;

/**
 * Created by ganymed on 30/11/14.
 */
public class CollectionItemLabelEvent extends Event {

  protected net.dankito.deepthought.controls.CollectionItemLabel label;

  public CollectionItemLabelEvent(Event event, net.dankito.deepthought.controls.CollectionItemLabel label) {
    super(event.getSource(), event.getTarget(), event.getEventType());

    this.label = label;
  }


  public net.dankito.deepthought.controls.CollectionItemLabel getLabel() {
    return label;
  }

}
