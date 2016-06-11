package net.dankito.deepthought.data.compare;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

/**
 * Created by ganymed on 10/01/15.
 */
public interface IDataComparer {

  public DataCompareResult compareDataToCurrent(BaseEntity data);

}
