package net.dankito.deepthought.data.persistence.serializer;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

/**
 * Created by ganymed on 07/01/15.
 */
public interface IDataSerializer {

  public <T extends BaseEntity> SerializationResult serializeData(BaseEntity data);

}
