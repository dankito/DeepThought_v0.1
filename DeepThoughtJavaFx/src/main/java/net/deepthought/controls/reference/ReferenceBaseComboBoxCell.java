package net.deepthought.controls.reference;

import net.deepthought.controls.BaseEntityListCell;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;

/**
 * Created by ganymed on 10/02/15.
 */
public class ReferenceBaseComboBoxCell<T extends ReferenceBase> extends BaseEntityListCell<T> {

  public ReferenceBaseComboBoxCell() {

  }

  protected String getItemTextRepresentation(T item) {
    if(item instanceof Reference)
      return ((Reference)item).getPreview(); // Reference

    return super.getItemTextRepresentation(item); // SeriesTitle
  }

}
