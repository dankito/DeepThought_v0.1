package net.dankito.deepthought.data.model;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class FileLinkTestBase extends DataModelTestBase {

  @Test
  public void updateUri_UpdatedValueGetsPersistedInDb() throws Exception {
    FileLink file = new FileLink("test");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addFile(file);

    entry.addAttachedFile(file);

    String newValue = "/tmp";
    file.setUriString(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.FileLinkTableName, TableConfig.FileLinkUriColumnName, file.getId()));
  }

  @Test
  public void updateName_UpdatedValueGetsPersistedInDb() throws Exception {
    FileLink file = new FileLink("test");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addFile(file);

    entry.addAttachedFile(file);

    String newName = "New name";
    file.setName(newName);

    Assert.assertEquals(newName, getValueFromTable(TableConfig.FileLinkTableName, TableConfig.FileLinkNameColumnName, file.getId()));
  }

  @Test
  public void updateIsFolder_UpdatedValueGetsPersistedInDb() throws Exception {
    FileLink file = new FileLink("test");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addFile(file);

    entry.addAttachedFile(file);

    boolean newValue = !file.isFolder();
    file.setIsFolder(newValue);

    Object storedValue = getValueFromTable(TableConfig.FileLinkTableName, TableConfig.FileLinkIsFolderColumnName, file.getId());
    compareBoolValue(newValue, storedValue);
  }

  @Test
  public void updateNotes_UpdatedValueGetsPersistedInDb() throws Exception {
    FileLink file = new FileLink("test");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addFile(file);

    entry.addAttachedFile(file);

    String newValue = "New value";
    file.setDescription(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.FileLinkTableName, TableConfig.FileLinkDescriptionColumnName, file.getId()));
  }

}
