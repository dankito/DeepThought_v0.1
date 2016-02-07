package net.deepthought.controls.event;

import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.data.model.Category;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by ganymed on 30/11/14.
 */
public class EntryCategoriesEditedEvent extends Event {

  protected EntryCategoriesControl control;

  protected Category category;

  public EntryCategoriesEditedEvent(EntryCategoriesControl control, Category category) {
    super(control, control, EventType.ROOT);

    this.control = control;

    this.category = category;
  }


  public EntryCategoriesControl getControl() {
    return control;
  }

  public Category getCategory() {
    return category;
  }

}
