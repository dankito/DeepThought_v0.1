package net.deepthought.data.persistence.serializer;

import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;

/**
 * Created by ganymed on 07/01/15.
 */
public class JsonIoDataSerializer implements IDataSerializer {

  @Override
  public <T extends BaseEntity> SerializationResult serializeData(BaseEntity data) {
    return JsonIoJsonHelper.generateJsonString(data);
  }

}
