package net.deepthought.data.model.enums;

import net.deepthought.data.persistence.db.TableConfig;

import javax.persistence.Entity;

/**
 * Created by ganymed on 15/02/15.
 */
@Entity(name = TableConfig.BackupFileServiceTypeTableName)
public class BackupFileServiceType extends ExtensibleEnumeration {

  private static final long serialVersionUID = -314599552379705785L;


  public BackupFileServiceType() {

  }

  public BackupFileServiceType(String name) {
    super(name);
  }

  public BackupFileServiceType(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }


  @Override
  public String toString() {
    return "BackupFileServiceType " + getTextRepresentation();
  }
}
