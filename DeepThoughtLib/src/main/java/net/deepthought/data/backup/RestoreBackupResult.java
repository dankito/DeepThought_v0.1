package net.deepthought.data.backup;

import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.DeepThoughtError;

import java.util.List;

/**
 * Created by ganymed on 03/01/15.
 */
public class RestoreBackupResult {

  protected BackupFile backup;

  protected List<BaseEntity> entitiesSucceededToInsert;
  protected List<BaseEntity> entitiesFailedToInsert;

  protected DeepThoughtError error;


  public RestoreBackupResult(BackupFile backup, DeepThoughtError error) {
    this.backup = backup;
    this.error = error;
  }

  public RestoreBackupResult(BackupFile backup, List<BaseEntity> entitiesSucceededToInsert, List<BaseEntity> entitiesFailedToInsert) {
    this.backup = backup;
    this.entitiesSucceededToInsert = entitiesSucceededToInsert;
    this.entitiesFailedToInsert = entitiesFailedToInsert;

    this.error = createError(entitiesSucceededToInsert, entitiesFailedToInsert);
  }

  protected DeepThoughtError createError(List<BaseEntity> entitiesSucceededToInsert, List<BaseEntity> entitiesFailedToInsert) {
    if(entitiesFailedToInsert.size() == 0)
      return DeepThoughtError.Success;
    else if(entitiesSucceededToInsert.size() == 0) {
      return DeepThoughtError.errorFromLocalizationKey("error.could.not.insert.any.backed.up.entity");
    }
    else {
      return DeepThoughtError.errorFromLocalizationKey("error.could.insert.x.of.y.backed.up.entities", entitiesSucceededToInsert.size(),
          entitiesSucceededToInsert.size() + entitiesFailedToInsert.size());
    }
  }


  public BackupFile getBackup() {
    return backup;
  }

  public List<BaseEntity> getEntitiesSucceededToInsert() {
    return entitiesSucceededToInsert;
  }

  public List<BaseEntity> getEntitiesFailedToInsert() {
    return entitiesFailedToInsert;
  }

  public DeepThoughtError getError() {
    return error;
  }

}
