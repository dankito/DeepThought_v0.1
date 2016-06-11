package net.dankito.deepthought.controls.reference;

import net.dankito.deepthought.controls.event.FieldChangedEvent;
import net.dankito.deepthought.data.model.ReferenceBase;

import javafx.event.EventHandler;

/**
 * Created by ganymed on 29/07/15.
 */
public interface ISelectedReferenceHolder {

  public ReferenceBase getSelectedReferenceBase();

  public void selectedReferenceBaseChanged(final ReferenceBase newReferenceBase);

  public void addFieldChangedEvent(EventHandler<FieldChangedEvent> fieldChangedEvent);

}
