package net.deepthought.controls.event;

import net.deepthought.controls.tag.EntryTagsControl;
import net.deepthought.data.model.Tag;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by ganymed on 30/11/14.
 */
public class EntryTagsEditedEvent extends Event {

  protected EntryTagsControl control;

  protected Tag tag;

  public EntryTagsEditedEvent(EntryTagsControl control, Tag tag) {
    super(control, control, EventType.ROOT);

    this.control = control;

    this.tag = tag;
  }


  public EntryTagsControl getControl() {
    return control;
  }

  public Tag getTag() {
    return tag;
  }

}
