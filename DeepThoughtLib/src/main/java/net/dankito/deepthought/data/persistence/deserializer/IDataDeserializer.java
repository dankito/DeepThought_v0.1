package net.dankito.deepthought.data.persistence.deserializer;

import net.dankito.deepthought.data.persistence.db.BaseEntity;

/**
 * Created by ganymed on 07/01/15.
 */
public interface IDataDeserializer {

  public <T extends BaseEntity> DeserializationResult<T> deserializeFile(String serializedData, Class<? extends BaseEntity> dataClass);

}
