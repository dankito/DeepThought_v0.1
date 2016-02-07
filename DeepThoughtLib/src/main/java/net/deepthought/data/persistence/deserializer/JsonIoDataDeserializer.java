package net.deepthought.data.persistence.deserializer;

import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;

/**
 * Created by ganymed on 07/01/15.
 */
public class JsonIoDataDeserializer implements IDataDeserializer {

  @Override
  public <T extends BaseEntity> DeserializationResult<T> deserializeFile(String serializedData, Class<? extends BaseEntity> dataClass) {
    return (DeserializationResult<T>)JsonIoJsonHelper.parseJsonString(serializedData, dataClass);
  }

}
