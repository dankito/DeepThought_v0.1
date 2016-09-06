package net.dankito.deepthought.listener;

import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;

/**
 * Created by ganymed on 06/09/16.
 */
public interface EditEntityListener {

  void entityEdited(BaseEntity entity, FieldWithUnsavedChanges changedField, Object newFieldValue);

}
