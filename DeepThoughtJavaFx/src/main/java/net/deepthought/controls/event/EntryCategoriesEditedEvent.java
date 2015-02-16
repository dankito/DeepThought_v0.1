package net.deepthought.controls.event;

import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by ganymed on 30/11/14.
 */
public class EntryCategoriesEditedEvent extends Event {

  protected EntryCategoriesControl control;

  protected Entry entry;
  protected Category category;

  public EntryCategoriesEditedEvent(EntryCategoriesControl control, Entry entry, Category category) {
    super(control, control, EventType.ROOT);

    this.control = control;

    this.entry = entry;
    this.category = category;
  }


  public EntryCategoriesControl getControl() {
    return control;
  }

  public Entry getEntry() {
    return entry;
  }

  public Category getCategory() {
    return category;
  }

}
