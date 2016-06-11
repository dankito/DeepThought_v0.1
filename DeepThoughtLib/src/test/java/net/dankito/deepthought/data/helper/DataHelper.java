package net.dankito.deepthought.data.helper;

import net.dankito.deepthought.data.backup.DatabaseBackupFileService;
import net.dankito.deepthought.data.backup.IBackupFileService;
import net.dankito.deepthought.data.backup.JsonIoBackupFileService;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Note;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.model.settings.enums.SelectedAndroidTab;
import net.dankito.deepthought.data.model.settings.enums.SelectedTab;
import net.dankito.deepthought.data.persistence.json.JsonIoJsonHelper;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;

/**
 * Created by ganymed on 01/01/15.
 */
public class DataHelper {

  public static DeepThoughtApplication createTestApplication() {
//    User loggedOnUser = User.createNewLocalUser();
//    DeepThought deepThought = createTestDeepThought();
//    loggedOnUser.addDeepThought(deepThought);
//    loggedOnUser.setLastViewedDeepThought(deepThought);
//
//    AppSettings appSettings = new AppSettings(Application.CurrentDataModelVersion, loggedOnUser, true);
//    return new DeepThoughtApplication(appSettings);

    DeepThoughtApplication application = DeepThoughtApplication.createApplication();
    User user = application.getLastLoggedOnUser();
    user.removeDeepThought(user.getLastViewedDeepThought());

    DeepThought deepThought = createTestDeepThought();
    user.addDeepThought(deepThought);
    user.setLastViewedDeepThought(deepThought);

    return application;
  }

  public static DeepThought createTestDeepThought() {
    DeepThoughtApplication application = DeepThoughtApplication.createApplication();
    DeepThought deepThought = application.getLastLoggedOnUser().getLastViewedDeepThought();

    Category booksCategory = new Category("Books");
    deepThought.addCategory(booksCategory);
    Category bookDigestsCategory = new Category("Digests");
    deepThought.addCategory(bookDigestsCategory);
    Category periodicalsCategory = new Category("Periodicals");
    deepThought.addCategory(periodicalsCategory);
    Category periodicalsPrintCategory = new Category("Print");
    deepThought.addCategory(periodicalsPrintCategory);
    Category periodicalsOnlineCategory = new Category("Online");
    deepThought.addCategory(periodicalsOnlineCategory);

    Entry entry1 = new Entry("Entry 1", "Contentless");
    Entry entry2 = new Entry("Entry 2", "Contentless");
    Entry entry3 = new Entry("Entry 3", "Contentless");
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);
    deepThought.addEntry(entry3);

    entry1.addSubEntry(entry2);
    entry1.addSubEntry(entry3);

    entry1.addCategory(bookDigestsCategory);
    entry2.addCategory(periodicalsPrintCategory);
    entry3.addCategory(periodicalsPrintCategory);
    entry3.addCategory(periodicalsOnlineCategory);

    Tag tag1 = new Tag("Peace");
    Tag tag2 = new Tag("Gandhi");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);

    entry1.addTag(tag1);
    entry1.addTag(tag2);
    entry2.addTag(tag1);
    entry2.addTag(tag2);
    entry3.addTag(tag1);

    Person welzer = new Person("Harald", "Welzer");
    Person paech = new Person("Niko", "Paech");
    Person graeber = new Person("David", "Graeber");
    deepThought.addPerson(welzer);
    deepThought.addPerson(paech);
    deepThought.addPerson(graeber);

    entry1.addPerson(welzer);
    entry1.addPerson(paech);
    entry2.addPerson(welzer);
    entry2.addPerson(welzer);
    entry3.addPerson(paech);

    entry1.addPerson(graeber);
    entry2.addPerson(graeber);
    entry2.addPerson(paech);
    entry3.addPerson(graeber);

    FileLink file1 = new FileLink("dummy1");
    FileLink file2 = new FileLink("dummy2");
    FileLink file3 = new FileLink("dummy3");
    FileLink file4 = new FileLink("dummy4");
    deepThought.addFile(file1);
    deepThought.addFile(file2);
    deepThought.addFile(file3);
    deepThought.addFile(file4);

    entry1.addAttachedFile(file1);
    entry2.addAttachedFile(file2);
    entry3.addAttachedFile(file3);

    entry1.setPreviewImage(file4);
    entry2.setPreviewImage(file1);
    entry3.setPreviewImage(file2);

    Note note1 = new Note("dummy1");
    Note note2 = new Note("dummy2");
    Note note3 = new Note("dummy3");
    deepThought.addNote(note1);
    deepThought.addNote(note2);
    deepThought.addNote(note3);
    entry1.addNote(note1);
    entry2.addNote(note2);
    entry3.addNote(note3);

    deepThought.getSettings().setLastViewedCategory(booksCategory);
    deepThought.getSettings().setLastViewedEntry(entry3);
    deepThought.getSettings().setLastViewedTag(tag2);
    deepThought.getSettings().setLastSelectedTab(SelectedTab.Tags);
    deepThought.getSettings().setLastSelectedAndroidTab(SelectedAndroidTab.EntriesOverview);

    return deepThought;
  }

  public static String createTestDeepThoughtJson() {
    return JsonIoJsonHelper.generateJsonString(createTestDeepThought(), true).getSerializationResult();
  }


  public static String getLatestDataModelVersionJsonFileContent() throws IOException {
    return new String(FileHelper.loadResourceFile("DataModelVersion01.json"));
  }

  public static File getLatestDataModelVersionJsonFile() {
    return getDataModelVersion01JsonFile();
  }

  public static File getLatestDataModelVersionDatabaseFile() {
    return getDataModelVersion01DatabaseFile();
  }

  public static File getLatestDataModelVersionFileForFileType(IBackupFileService backupFileService) {
    return getLatestDataModelVersionFileForFileType(backupFileService.getFileTypeFileExtension());
  }

  public static File getLatestDataModelVersionFileForFileType(String fileExtension) {
    if(fileExtension.toLowerCase().equals(JsonIoBackupFileService.JsonBackupFileExtension))
      return getLatestDataModelVersionJsonFile();
    else if(fileExtension.toLowerCase().equals(DatabaseBackupFileService.DatabaseBackupFileExtension))
      return getLatestDataModelVersionDatabaseFile();

    Assert.fail("Cannot find Data Model File for file extension " + fileExtension);
    return null;
  }


  public static File getDataModelVersion01JsonFile() {
    return new File(getExampleDataFolder(), "DataModelVersion01.json");
  }

  public static File getInvalidJsonFile() {
    return new File(getExampleDataFolder(), "invalid.json");
  }

  public static File getInvalidDatabaseFile() {
    return new File(getExampleDataFolder(), "invalid.db");
  }

  public static File getDataModelVersion01DatabaseFile() {
//    return new File(getExampleDataFolder(), "DataModelVersion01_SQLite.db");
//    return new File(getExampleDataFolder(), "DeepThoughtDb_Derby_Version01");
    return new File(getExampleDataFolder(), "DataModelVersion01_H2.mv.db");
  }

  public static File getExampleDataFolder() {
//    return new File(DeepThoughtProperties.getDataFolder(), "testdata");
    return new File("data/tests/example_data");
  }
}
