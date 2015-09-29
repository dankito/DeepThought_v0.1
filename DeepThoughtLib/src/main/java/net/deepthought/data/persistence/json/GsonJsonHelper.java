package net.deepthought.data.persistence.json;

import com.google.gson.Gson;

import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GsonJsonHelper {

  private static final Logger log = LoggerFactory.getLogger(GsonJsonHelper.class);

  private GsonJsonHelper() {

  }

  public static <T> DeserializationResult<T> parseJsonString(String json, Class<T> deserializedObjectClass) {
    if (json == null) {
      return new DeserializationResult<T>(new IllegalArgumentException("A JSON String must be defined"));
    }

    try {
      Gson gson = new Gson();
      T result = (T)gson.fromJson(json, deserializedObjectClass);
      return new DeserializationResult<T>(result);
    } catch (Exception e) {
      log.error("Other exception occurred during json deserialization", e);
      return new DeserializationResult<T>(e);
    }
  }

  public static SerializationResult generateJsonString(Object object) {
    try {
      Gson gson = new Gson();
      String json = gson.toJson(object);

      return new SerializationResult(json);
    } catch (Exception e) {
      log.error("Error generating json", e);
      return new SerializationResult(e);
    }
  }

}
