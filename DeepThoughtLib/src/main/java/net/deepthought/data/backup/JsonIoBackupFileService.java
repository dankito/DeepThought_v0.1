package net.deepthought.data.backup;

import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.deserializer.JsonIoDataDeserializer;
import net.deepthought.data.persistence.serializer.JsonIoDataSerializer;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by ganymed on 05/01/15.
 */
public class JsonIoBackupFileService extends SerializeToFileBackupFileService {

//  public final static String BackupFileTypeKey = "Json";

  public final static String JsonBackupFileNameStart = "DeepThought.JsonBackup";
  public final static String JsonBackupFileExtension = "json";


  private final static Logger log = LoggerFactory.getLogger(JsonIoBackupFileService.class);


  protected JsonIoDataSerializer jsonIoDataSerializer = new JsonIoDataSerializer();
  protected JsonIoDataDeserializer jsonIoDataDeserializer = new JsonIoDataDeserializer();


  public JsonIoBackupFileService() {
    super("backup.file.service.type.json");
  }


//  @Override
//  public String getFileTypeKey() {
//    return BackupFileTypeKey;
//  }

  @Override
  public String getFileTypeFileExtension() {
    return JsonBackupFileExtension;
  }

  protected String createBackupFileName(CreateBackupParams params) {
    return JsonBackupFileNameStart + "_" + BackupDateFormat.format(new Date()) + "." + JsonBackupFileExtension;
  }

  @Override
  protected SerializationResult serializeEntity(BaseEntity entity) {
    return jsonIoDataSerializer.serializeData(entity);
  }

  @Override
  protected <T extends BaseEntity> DeserializationResult<T> deserializeEntity(String backupFileContent, Class<T> entityClass) {
    return jsonIoDataDeserializer.deserializeFile(backupFileContent, entityClass);
  }
}
