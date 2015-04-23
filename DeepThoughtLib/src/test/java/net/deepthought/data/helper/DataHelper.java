package net.deepthought.data.helper;

import net.deepthought.data.backup.DatabaseBackupFileService;
import net.deepthought.data.backup.IBackupFileService;
import net.deepthought.data.backup.JsonIoBackupFileService;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Note;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.User;
import net.deepthought.data.model.settings.enums.SelectedAndroidTab;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;

import org.junit.Assert;

import java.io.File;

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

    bookDigestsCategory.addEntry(entry1);
    periodicalsPrintCategory.addEntry(entry2);
    periodicalsPrintCategory.addEntry(entry3);
    periodicalsOnlineCategory.addEntry(entry3);

    Tag tag1 = new Tag("Peace");
    Tag tag2 = new Tag("Gandhi");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);

    entry1.addTag(tag1);
    entry1.addTag(tag2);
    entry2.addTag(tag1);
    entry2.addTag(tag2);
    entry3.addTag(tag1);

//    entry1.addTag(new Tag("Entry 1 Tag"));
//    entry2.addTag(new Tag("Entry 2 Tag"));
//    entry3.addTag(new Tag("Entry 3 Tag"));

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

    entry1.addFile(new FileLink("dummy"));
    entry2.addFile(new FileLink("dummy"));
    entry3.addFile(new FileLink("dummy"));

    entry1.addNote(new Note("dummy"));
    entry2.addNote(new Note("dummy"));
    entry3.addNote(new Note("dummy"));

    entry1.setPreviewImage(new FileLink("dummy"));
    entry2.setPreviewImage(new FileLink("dummy"));
    entry3.setPreviewImage(new FileLink("dummy"));

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
