package net.deepthought.data.persistence.json;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;

import net.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 02/05/15.
 */
public class DeepThoughtJsonReader extends JsonReader {

  private final static Logger log = LoggerFactory.getLogger(DeepThoughtJsonReader.class);


  /**
   * Convert the passed in JSON string into a Java object graph.
   *
   * @param json String JSON input
   * @return Java object graph matching JSON input
   * @throws java.io.IOException If an I/O error occurs
   */
  public static Object jsonToJava(String json) throws IOException
  {
    ByteArrayInputStream ba = new ByteArrayInputStream(json.getBytes("UTF-8"));
    JsonReader jr = new DeepThoughtJsonReader(ba, false);
    Object obj = jr.readObject();
    jr.close();
    return obj;
  }


  protected Map<Class, Map<Object, BaseEntity>> mapCreatedEntities = new HashMap<>();


  public DeepThoughtJsonReader(InputStream in, boolean noObjects) {
    super(in, noObjects);
  }

  @Override
  protected Object createJavaObjectInstance(Class clazz, JsonObject jsonObj) throws IOException {
    clazz = getRealClass(clazz, jsonObj);

    if(BaseEntity.class.isAssignableFrom(clazz)) {
      Object id = jsonObj.get("id");
      if(id == null)
        id = jsonObj.getId();

      if(mapCreatedEntities.containsKey(clazz) && mapCreatedEntities.get(clazz).containsKey(id)) {
        Object target = mapCreatedEntities.get(clazz).get(id);
        jsonObj.setTarget(target);
        return target;
      }

      Object instance = super.createJavaObjectInstance(clazz, jsonObj);

      if(instance instanceof BaseEntity) {
        if (mapCreatedEntities.containsKey(clazz) == false)
          mapCreatedEntities.put(clazz, new HashMap<Object, BaseEntity>());

        mapCreatedEntities.get(clazz).put(id, (BaseEntity)instance);
      }

      return instance;
    }

    return super.createJavaObjectInstance(clazz, jsonObj);
  }

  private Class getRealClass(Class clazz, JsonObject jsonObj) {
    if(Object.class.equals(clazz) && isDeepThoughtDataModelType(jsonObj)) {
      try {
        return Class.forName(jsonObj.getType());
      } catch(Exception ex) {
        log.warn("Could not get Class for Type " + jsonObj.getType(), ex);
      }
    }

    return clazz;
  }

  protected boolean isDeepThoughtDataModelType(JsonObject jsonObj) {
    return jsonObj.getType().startsWith("net.deepthought.data.model.");
  }
}
