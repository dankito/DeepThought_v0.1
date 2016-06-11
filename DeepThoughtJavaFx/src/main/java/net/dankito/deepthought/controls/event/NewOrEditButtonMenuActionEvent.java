package net.dankito.deepthought.controls.event;

import net.dankito.deepthought.controls.NewOrEditButton;

import javafx.event.ActionEvent;
import javafx.event.Event;

/**
 * Created by ganymed on 10/02/15.
 */
public class NewOrEditButtonMenuActionEvent extends Event {

  protected NewOrEditButton button;


  public NewOrEditButtonMenuActionEvent(ActionEvent event, NewOrEditButton button) {
    super(event.getSource(), event.getTarget(), event.getEventType());
    this.button = button;
  }

}
