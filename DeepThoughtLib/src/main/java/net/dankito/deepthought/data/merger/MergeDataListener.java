package net.dankito.deepthought.data.merger;

import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.DeepThoughtError;

import java.util.List;

/**
 * Created by ganymed on 07/01/15.
 */
public interface MergeDataListener {

  public void beginToMergeEntity(BaseEntity entity);

  public void mergeEntityResult(BaseEntity entity, boolean successful, DeepThoughtError error);

  public void addingEntitiesDone(boolean successful, List<BaseEntity> entitiesSucceededToInsert, List<BaseEntity> entitiesFailedToInsert);

}
