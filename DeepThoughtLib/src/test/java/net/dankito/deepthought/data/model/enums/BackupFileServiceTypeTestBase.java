package net.dankito.deepthought.data.model.enums;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class BackupFileServiceTypeTestBase extends EditableExtensibleEnumerationTestBase<BackupFileServiceType> {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<BackupFileServiceType> backupFileServiceTypes = new ArrayList<>(deepThought.getBackupFileServiceTypes());

    return backupFileServiceTypes.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.BackupFileServiceTypeTableName;
  }


  @Override
  protected BackupFileServiceType createNewEnumValue() {
    return new BackupFileServiceType("Love");
  }

  @Override
  protected void addToEnumeration(BackupFileServiceType enumValue) {
    Application.getDeepThought().addBackupFileServiceType(enumValue);
  }

  @Override
  protected void removeFromEnumeration(BackupFileServiceType enumValue) {
    Application.getDeepThought().removeBackupFileServiceType(enumValue);
  }

  @Override
  protected Collection<BackupFileServiceType> getEnumeration() {
    return Application.getDeepThought().getBackupFileServiceTypes();
  }

}
