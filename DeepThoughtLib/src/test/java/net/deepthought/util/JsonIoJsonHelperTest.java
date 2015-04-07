package net.deepthought.util;

import net.deepthought.data.helper.DataHelper;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.User;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Date;

/**
 * Created by ganymed on 01/01/15.
 */
public class JsonIoJsonHelperTest {

  private final static Logger log = LoggerFactory.getLogger(JsonIoJsonHelperTest.class);


  @Test
  public void serializeDeepThoughtApplicationToJson() {
    DeepThoughtApplication application = DataHelper.createTestApplication();
    Date startTime = new Date();

    SerializationResult serializationResult = JsonIoJsonHelper.generateJsonString(application, true);

    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
    log.debug("Serialization took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");

    Assert.assertTrue(serializationResult.successful());
    Assert.assertTrue(serializationResult.getSerializationResult() != null);
    Assert.assertFalse(serializationResult.getSerializationResult().isEmpty());
  }

  @Test
  public void deserializeDeepThoughtApplicationFromJson() throws IOException {
    FileInputStream jsonInputStream = new FileInputStream(DataHelper.getLatestDataModelVersionJsonFile());
    InputStreamReader inputStreamReader = new InputStreamReader(jsonInputStream);
    CharBuffer buffer = CharBuffer.allocate(jsonInputStream.available());
    inputStreamReader.read(buffer);
    char[] chars = buffer.array();
    String json = new String(chars);
    Date startTime = new Date();

    DeserializationResult<DeepThoughtApplication> deserializationResult = JsonIoJsonHelper.parseJsonString(json, DeepThoughtApplication.class);

    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
    log.debug("Deserialization took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");

    Assert.assertTrue(deserializationResult.successful());
    Assert.assertNotNull(deserializationResult.getResult());

    DeepThoughtApplication application = deserializationResult.getResult();
    Assert.assertNotNull(application.getLastLoggedOnUser());
    Assert.assertTrue(application.getUsers().size() > 0);

    for(User user : application.getUsers()) {
      Assert.assertTrue(user.getDeepThoughts().size() > 0);
      Assert.assertTrue(user.getGroups().size() > 0);
      for(DeepThought deepThought : user.getDeepThoughts())
        testDeepThought(deepThought);
    }
  }

  protected void testDeepThought(DeepThought deepThought) {
    Assert.assertNotNull(deepThought.getTopLevelCategory());
    Assert.assertTrue(deepThought.getCategories().size() > 0);
    Assert.assertTrue(deepThought.countEntries() > 0);
    Assert.assertTrue(deepThought.countTags() > 0);
    Assert.assertTrue(deepThought.countIndexTerms() > 0);
    Assert.assertTrue(deepThought.countPersons() > 0);
    Assert.assertTrue(deepThought.getNextEntryIndex() > 1);

    Assert.assertNotNull(deepThought.getSettings().getLastViewedCategory());
    Assert.assertNotNull(deepThought.getSettings().getLastViewedEntry());
    Assert.assertNotNull(deepThought.getSettings().getLastViewedTag());
    Assert.assertNotEquals(SelectedTab.Unknown, deepThought.getSettings().getLastSelectedTab());
    Assert.assertNotEquals(SelectedTab.Unknown, deepThought.getSettings().getLastSelectedAndroidTab());

    for(Entry entry : deepThought.getEntries()) {
      Assert.assertTrue(entry.getCategories().size() > 0);
      Assert.assertTrue(entry.hasTags());
      Assert.assertTrue(entry.hasIndexTerms());
      Assert.assertTrue(entry.getPersons().size() > 0);
      Assert.assertTrue(entry.getFiles().size() > 0);
      Assert.assertTrue(entry.getNotes().size() > 0);

      Assert.assertTrue(entry.getEntryIndex() > 0);
      Assert.assertNotNull(entry.getDeepThought());
      Assert.assertNotNull(entry.getPreviewImage());
      Assert.assertTrue(entry.hasSubEntries() || entry.getParentEntry() != null);

      // test data hasn't been inserted into database, so these fields cannot be set
//      Assert.assertNotNull(entry.getId());
//      Assert.assertTrue(entry.getVersion() > 0);
//      Assert.assertNotNull(entry.getCreatedOn());
//      Assert.assertNotNull(entry.getCreatedBy());
//      Assert.assertNotNull(entry.getModifiedOn());
//      Assert.assertNotNull(entry.getModifiedBy());
//      Assert.assertFalse(entry.isDeleted());
//      Assert.assertNull(entry.getDeletedBy());
//      Assert.assertNotNull(entry.getOwner());
    }
  }

  @Test
  public void serializeDeepThoughtToJson_TransientFieldsDontGetSerialized() {
    DeepThoughtApplication application = DataHelper.createTestApplication();

    SerializationResult result = JsonIoJsonHelper.generateJsonString(application, true);

    String json = result.getSerializationResult();

    checkDeepThoughtIgnoreFields(json);
    checkCategoryIgnoreFields(json);
    checkEntryIgnoreFields(json);
    checkTagIgnoreFields(json);
    checkKeywordIgnoreFields(json);
    checkPersonIgnoreFields(json);
  }

  protected void checkDeepThoughtIgnoreFields(String json) {
    Assert.assertFalse(json.contains("deepThoughtListeners"));
    Assert.assertFalse(json.contains("entityListeners"));
    Assert.assertFalse(json.contains("categoriesChangedListeners"));
    Assert.assertFalse(json.contains("entriesChangedListeners"));
    Assert.assertFalse(json.contains("tagsChangedListeners"));
    Assert.assertFalse(json.contains("keywordsChangedListeners"));
    Assert.assertFalse(json.contains("personsChangedListeners"));

    Assert.assertFalse(json.contains("categoryListener"));
    Assert.assertFalse(json.contains("entryListener"));
    Assert.assertFalse(json.contains("tagListener"));
    Assert.assertFalse(json.contains("keywordListener"));
    Assert.assertFalse(json.contains("personListener"));

    Assert.assertFalse(json.contains("sortedTags"));
    Assert.assertFalse(json.contains("\"favoriteEntryTemplates\"")); // as favoriteEntryTemplatesRelations get serialized we have to check for the exact key
  }

  protected void checkCategoryIgnoreFields(String json) {
    Assert.assertFalse(json.contains("\"defaultEntryTemplate\"")); // as defaultEntryTemplateKey get serialized we have to check for the exact key
//    Assert.assertFalse(json.contains("deepThought"));
    Assert.assertFalse(json.contains("categoryListeners"));
  }

  protected void checkEntryIgnoreFields(String json) {
    Assert.assertFalse(json.contains("entryListeners"));

    Assert.assertFalse(json.contains("getTagsSorted"));
//    Assert.assertFalse(json.contains("deepThought"));

    Assert.assertFalse(json.contains("\"template\"")); // as templateKey get serialized we have to check for the exact key
  }

  protected void checkTagIgnoreFields(String json) {
    Assert.assertFalse(json.contains("tagListeners"));
  }

  protected void checkKeywordIgnoreFields(String json) {
    Assert.assertFalse(json.contains("keywordListeners"));
  }

  protected void checkPersonIgnoreFields(String json) {
    Assert.assertFalse(json.contains("listeners"));
  }


}
