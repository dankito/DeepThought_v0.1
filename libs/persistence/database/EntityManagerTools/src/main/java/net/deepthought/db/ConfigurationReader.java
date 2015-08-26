package net.deepthought.db;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.j256.ormlite.dao.cda.jointable.JoinTableConfig;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.ConnectionSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by ganymed on 24/08/15.
 */
public class ConfigurationReader extends JsonReader {

  public final static String DeclaringClassFieldName = "declaringClass";

  public final static String FieldNameFieldName = "fieldName";

  public final static String MethodNameFieldName = "methodName";

  public final static String ParametersFieldName = "parameters";

  public final static String ParameterFieldName = "parameter";


  private final static Logger log = LoggerFactory.getLogger(ConfigurationReader.class);


  public static Object jsonToJava(String json, ConnectionSource connectionSource) throws IOException
  {
    ByteArrayInputStream ba = new ByteArrayInputStream(json.getBytes("UTF-8"));
    JsonReader jr = new ConfigurationReader(connectionSource, ba, false);
    Object obj = jr.readObject();
    jr.close();
    return obj;
  }


  protected ConnectionSource connectionSource = null;


  public ConfigurationReader(ConnectionSource connectionSource) {
    this.connectionSource = connectionSource;
  }

  public ConfigurationReader(ConnectionSource connectionSource, InputStream in) {
    super(in);
    this.connectionSource = connectionSource;
  }

  public ConfigurationReader(ConnectionSource connectionSource, InputStream in, boolean noObjects) {
    super(in, noObjects);
    this.connectionSource = connectionSource;
  }

  @Override
  protected Object convertMapsToObjects(JsonObject<String, Object> root) throws IOException {
    return super.convertMapsToObjects(root);
  }

  @Override
  protected Object convertParsedMapsToJava(JsonObject root) throws IOException {
    return super.convertParsedMapsToJava(root);
  }

  @Override
  protected Object createJavaObjectInstance(Class clazz, JsonObject jsonObj) throws IOException {
    if(EntityConfig.class.equals(clazz)) {
      return createEntityConfig(clazz, jsonObj);
    }
    else if(JoinTableConfig.class.equals(clazz))
      return createJoinTableConfig(clazz, jsonObj);
    else if(PropertyConfig.class.equals(clazz))
      return createPropertyConfig(clazz, jsonObj);
    else if(Field.class.equals(clazz))
      return createField(jsonObj);
    else if(Method.class.equals(clazz))
      return createMethod(jsonObj);
    else if(Constructor.class.equals(clazz))
      return createConstructor(jsonObj);
    else
      return super.createJavaObjectInstance(clazz, jsonObj);
  }

  protected Object createPropertyConfig(Class clazz, JsonObject jsonObj) throws IOException {
    return super.createJavaObjectInstance(clazz, jsonObj);
  }

  protected Object createEntityConfig(Class clazz, JsonObject jsonObj) throws IOException {
    EntityConfig entityConfig = (EntityConfig)super.createJavaObjectInstance(clazz, jsonObj);
    entityConfig.setConnectionSource(connectionSource);
    return entityConfig;
  }

  protected Object createJoinTableConfig(Class clazz, JsonObject jsonObj) throws IOException {
    JoinTableConfig joinTableConfig = (JoinTableConfig)super.createJavaObjectInstance(clazz, jsonObj);
    joinTableConfig.setConnectionSource(connectionSource);
    return joinTableConfig;
  }

  protected Object createField(JsonObject jsonObj) {
    try {
      String declaringClassName = (String)jsonObj.get(DeclaringClassFieldName);
      String fieldName = (String)jsonObj.get(FieldNameFieldName);

      Class declaringClass = Class.forName(declaringClassName);
      Field field = declaringClass.getDeclaredField(fieldName);
      jsonObj.setTarget(field);

      try {
        if(field.isAccessible() == false)
          field.setAccessible(true);
      } catch(Exception ex) { }

      return field;
    } catch(Exception ex) {
      log.error("Could not deserialize java.lang.reflection.Field " + jsonObj, ex);
    }

    return null;
  }

  protected Object createMethod(JsonObject jsonObj) {
    try {
      String declaringClassName = (String)jsonObj.get(DeclaringClassFieldName);
      String methodName = (String)jsonObj.get(MethodNameFieldName);

      Class[] parameters = extractParameters(jsonObj);

      Class declaringClass = Class.forName(declaringClassName);
      Method method = declaringClass.getDeclaredMethod(methodName, parameters);
      jsonObj.setTarget(method);

      try {
        if(method.isAccessible() == false)
          method.setAccessible(true);
      } catch(Exception ex) { }

      return method;
    } catch(Exception ex) {
      log.error("Could not deserialize java.lang.reflection.Method " + jsonObj, ex);
    }

    return null;
  }

  protected Object createConstructor(JsonObject jsonObj) {
    try {
      String declaringClassName = (String)jsonObj.get(DeclaringClassFieldName);

      Class[] parameters = extractParameters(jsonObj);

      Class declaringClass = Class.forName(declaringClassName);
      Constructor constructor = declaringClass.getDeclaredConstructor(parameters);
      jsonObj.setTarget(constructor);

      try {
        if(constructor.isAccessible() == false)
          constructor.setAccessible(true);
      } catch(Exception ex) { }

      return constructor;
    } catch(Exception ex) {
      log.error("Could not deserialize java.lang.reflection.Method " + jsonObj, ex);
    }

    return null;
  }

  protected Class[] extractParameters(JsonObject jsonObj) {
    Object[] parameterClasses = (Object[])jsonObj.get(ParametersFieldName);
    Class[] parameters = new Class[parameterClasses.length];
    for(int i = 0; i < parameterClasses.length; i++) {
      try {
        String className = (String)((JsonObject)parameterClasses[i]).get(ParameterFieldName);
        parameters[i] = getClassForClassName(className);
      } catch(Exception ex) { log.error("Could not get class for Parameter " + parameterClasses[i], ex); }
    }
    return parameters;
  }

  protected Class<?> getClassForClassName(String className) throws ClassNotFoundException {
    if("int".equals(className))
      return int.class;
    if("boolean".equals(className))
      return boolean.class;
    if("char".equals(className))
      return char.class;
    if("byte".equals(className))
      return byte.class;
    if("short".equals(className))
      return short.class;
    if("long".equals(className))
      return long.class;
    if("float".equals(className))
      return float.class;
    if("double".equals(className))
      return double.class;

    return Class.forName(className);
  }

  @Override
  public Object jsonObjectsToJava(JsonObject root) throws IOException {
    return super.jsonObjectsToJava(root);
  }

  @Override
  public Object readObject() throws IOException {
    return super.readObject();
  }
}
