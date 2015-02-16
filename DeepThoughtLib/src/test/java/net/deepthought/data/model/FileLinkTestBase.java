package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

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

    entry.addFile(file);

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

    entry.addFile(file);

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

    entry.addFile(file);

    boolean newValue = !file.isFolder();
    file.setIsFolder(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.FileLinkTableName, TableConfig.FileLinkIsFolderColumnName, file.getId()));
  }

  @Test
  public void updateNotes_UpdatedValueGetsPersistedInDb() throws Exception {
    FileLink file = new FileLink("test");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addFile(file);

    String newValue = "New value";
    file.setNotes(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.FileLinkTableName, TableConfig.FileLinkNotesColumnName, file.getId()));
  }

}
