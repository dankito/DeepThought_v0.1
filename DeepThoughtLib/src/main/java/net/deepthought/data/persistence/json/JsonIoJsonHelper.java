package net.deepthought.data.persistence.json;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonIoJsonHelper {

  private static final Logger log = LoggerFactory.getLogger(JsonIoJsonHelper.class);

  private JsonIoJsonHelper() {

  }

  public static <T> DeserializationResult<T> parseJsonString(String json, Class<T> deserializedObjectClass) {
    if (json == null) {
      return new DeserializationResult<T>(new IllegalArgumentException("A JSON String must be defined"));
    }

    try {
      T result = (T)JsonReader.jsonToJava(json);
      return new DeserializationResult<T>(result);
    } catch (IOException e) {
      log.error("Error io json ", e);
      return new DeserializationResult<T>(e);
    } catch (Exception e) {
      log.error("Other exception occurred during json deserialization", e);
      return new DeserializationResult<T>(e);
    }
  }

  public static SerializationResult generateJsonString(Object object) {
    return generateJsonString(object, false);
  }

  public static SerializationResult generateJsonString(Object object, boolean formatOutput) {
    try {
      Map<String, Object> arguments = new HashMap<>();
//      arguments.put(JsonWriter.FIELD_SPECIFIERS, getFieldsToIncludeMap());
      if(formatOutput)
        arguments.put(JsonWriter.PRETTY_PRINT, true);

      String json = JsonWriter.objectToJson(object, arguments);
      return new SerializationResult(json);
    } catch (Exception e) {
      log.error("Error generate json", e);
      return new SerializationResult(e);
    }
  }

  protected static Map<Class, List<String>> fieldsToIncludeMap = null;

  private static Map<Class, List<String>> getFieldsToIncludeMap() {
    if(fieldsToIncludeMap == null)
      fieldsToIncludeMap = createFieldsToIncludeMap();

    return fieldsToIncludeMap;
  }

  protected static Map<Class, List<String>> createFieldsToIncludeMap() {
    Map<Class, List<String>> fieldsIgnoreMap = new HashMap<>();

    fieldsIgnoreMap.put(BaseEntity.class, getBaseEntityIncludeFields());
    fieldsIgnoreMap.put(UserDataEntity.class, getUserDataEntityIncludeFields());
    fieldsIgnoreMap.put(DeepThought.class, getDeepThoughtIncludeFields());
    fieldsIgnoreMap.put(Category.class, getCategoryIncludeFields());
    fieldsIgnoreMap.put(Entry.class, getEntryIncludeFields());
    fieldsIgnoreMap.put(Tag.class, getTagIncludeFields());
    fieldsIgnoreMap.put(Person.class, getPersonIncludeFields());
    fieldsIgnoreMap.put(Device.class, getDeviceIncludeFields());

    return fieldsIgnoreMap;
  }

  protected static List<String> getBaseEntityIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("id");
//    includeFields.add("createdOn");
//    includeFields.add("modifiedOn");
//    includeFields.add("version");
//    includeFields.add("deleted");

    return includeFields;
  }

  protected static List<String> getUserDataEntityIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("createdBy");
//    includeFields.add("modifiedBy");
//    includeFields.add("deletedBy");
//    includeFields.add("owner");

    return includeFields;
  }

  protected static List<String> getDeepThoughtIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("deepThoughtOwner");

    return includeFields;
  }

  protected static List<String> getCategoryIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("deepThought");

    return includeFields;
  }

  protected static List<String> getEntryIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("getTagsSorted");
//    includeFields.add("deepThought");

    return includeFields;
  }

  protected static List<String> getTagIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("deepThought");

    return includeFields;
  }

  protected static List<String> getKeywordIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("deepThought");

    return includeFields;
  }

  protected static List<String> getPersonIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("deepThought");

    return includeFields;
  }

  protected static List<String> getDeviceIncludeFields() {
    List<String> includeFields = new ArrayList<>();

//    includeFields.add("owner");

    return includeFields;
  }

}
