package net.dankito.deepthought.listener;

import net.dankito.deepthought.data.persistence.db.UserDataEntity;

/**
 * Created by ganymed on 16/09/15.
 */
public interface EntityEditedListener {

  void editingDone(boolean cancelled, UserDataEntity editedEntity);

}
