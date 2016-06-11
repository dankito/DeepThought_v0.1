package net.dankito.deepthought.data.persistence.serializer;

import net.dankito.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

/**
 * Created by ganymed on 07/01/15.
 */
public class JsonIoDataSerializer implements IDataSerializer {

  @Override
  public <T extends BaseEntity> SerializationResult serializeData(BaseEntity data) {
    return JsonIoJsonHelper.generateJsonString(data);
  }

}
