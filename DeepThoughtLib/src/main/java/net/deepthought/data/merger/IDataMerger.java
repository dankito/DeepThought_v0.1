package net.deepthought.data.merger;

import net.deepthought.data.persistence.db.BaseEntity;

import java.util.List;

/**
 * Created by ganymed on 10/01/15.
 */
public interface IDataMerger {

  public boolean mergeWithCurrentData(List<BaseEntity> data, boolean mergeTheirSubEntitiesAsWell, MergeDataListener listener);
  public boolean addToCurrentData(List<BaseEntity> data, MergeDataListener listener);


}
