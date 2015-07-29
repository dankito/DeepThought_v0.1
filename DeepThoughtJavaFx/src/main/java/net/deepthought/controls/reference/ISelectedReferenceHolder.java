package net.deepthought.controls.reference;

import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.data.model.ReferenceBase;

import javafx.event.EventHandler;

/**
 * Created by ganymed on 29/07/15.
 */
public interface ISelectedReferenceHolder {

  public ReferenceBase getSelectedReferenceBase();

  public void selectedReferenceBaseChanged(final ReferenceBase newReferenceBase);

  public void addFieldChangedEvent(EventHandler<FieldChangedEvent> fieldChangedEvent);

}
