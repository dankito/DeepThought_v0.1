package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

/**
 * Created by ganymed on 05/09/16.
 */
public interface ISynchronizationListener {

  void entitySynchronized(BaseEntity entity);

}
