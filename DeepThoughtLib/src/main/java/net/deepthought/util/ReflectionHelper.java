package net.deepthought.util;

import net.deepthought.data.model.Entry;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.UserDataEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 10/01/15.
 */
public class ReflectionHelper {

  private final static Logger log = LoggerFactory.getLogger(ReflectionHelper.class);


  public static List<Collection<BaseEntity>> getCollectionsChildren(BaseEntity entity) {
    List<Collection<BaseEntity>> childCollections = new ArrayList<>();

    for(Field collectionField : findCollectionsChildrenFields(entity.getClass())) {
      childCollections.add(getCollectionFieldValue(entity, collectionField));
    }

    return childCollections;
  }

  public static List<Field> findCollectionsChildrenFields(Class clazz) {
    List<Field> collectionsChildren = new ArrayList<>();

    if((clazz == UserDataEntity.class || clazz == BaseEntity.class) == false) {
      collectionsChildren.addAll(findCollectionsChildrenFields(clazz.getSuperclass()));

      for (Field field : clazz.getDeclaredFields()) {
        if(Collection.class.isAssignableFrom(field.getType()) && Modifier.isTransient(field.getModifiers()) == false) {
          collectionsChildren.add(field);
        }
      }
    }

    return collectionsChildren;
  }


  public static List<BaseEntity> getBaseEntityChildren(BaseEntity entity) {
    List<BaseEntity> baseEntityChildren = new ArrayList<>();

    for(Field baseEntityField : findBaseEntityChildrenFields(entity.getClass())) {
      Object fieldValue = getFieldValue(entity, baseEntityField);
      if(fieldValue != null)
        baseEntityChildren.add((BaseEntity)fieldValue);
    }

    return baseEntityChildren;
  }

  public static List<Field> findBaseEntityChildrenFields(Class clazz) {
    List<Field> baseEntityChildren = new ArrayList<>();

    if((clazz == Object.class || clazz == null) == false) {
      baseEntityChildren.addAll(findBaseEntityChildrenFields(clazz.getSuperclass()));

      for (Field field : clazz.getDeclaredFields()) {
        if(BaseEntity.class.isAssignableFrom(field.getType()) && Modifier.isTransient(field.getModifiers()) == false) {
          baseEntityChildren.add(field);
        }
      }
    }

    return baseEntityChildren;
  }

  public static List<Field> findEntityProperties(Class clazz) {
    List<Field> propertyFields = new ArrayList<>();

//    if((clazz == UserDataEntity.class || clazz == BaseEntity.class) == false) {
    if(clazz != null) {
      propertyFields.addAll(findEntityProperties(clazz.getSuperclass()));

      for (Field field : clazz.getDeclaredFields()) {
        if(Collection.class.isAssignableFrom(field.getType()) == false && hasTransientStaticOrFinalModifier(field) == false) {
          propertyFields.add(field);
        }
      }
    }

    return propertyFields;
  }

  public static List<Field> findAllFieldsInClassHierarchy(Class clazz) {
    List<Field> fields = new ArrayList<>();

    if(clazz != null) {
      fields.addAll(findEntityProperties(clazz.getSuperclass()));

      for (Field field : clazz.getDeclaredFields()) {
        if(hasTransientStaticOrFinalModifier(field) == false)
          fields.add(field);
      }
    }

    return fields;
  }

  protected static boolean hasTransientStaticOrFinalModifier(Field field) {
    int modifiers = field.getModifiers();
    return Modifier.isTransient(modifiers) == true || Modifier.isStatic(modifiers) == true || Modifier.isFinal(modifiers) == true;
  }

  public static Object copyObject(Object object) {
    try {
      Object copy = object.getClass().newInstance();
      copyObjectFields(copy, object);

      return copy;
    } catch(Exception ex) {
      log.error("Could not copy Object " + object, ex);
    }

    return null;
  }

  public static void copyObjectFields(Object destination, Object copySource) {
    for(Field field : ReflectionHelper.findAllFieldsInClassHierarchy(copySource.getClass())) {
      ReflectionHelper.setFieldValue(destination, field, ReflectionHelper.getFieldValue(copySource, field));
    }
  }

  public static Object getFieldValue(Object object, Field field) {
    try {
      boolean wasAccessible = true;
      if(field.isAccessible() == false) {
        wasAccessible = false;
        field.setAccessible(true);
      }

      Object value = field.get(object);

      if(wasAccessible == false)
        field.setAccessible(false);

      return value;
    } catch(Exception ex) {
      log.error("Could not get Value from Object " + object + " for field " + field);
    }

    return null;
  }

  public static boolean setFieldValue(Object destinationEntity, Field field, Object value) {
    try {
      boolean wasAccessible = true;
      if(field.isAccessible() == false) {
        wasAccessible = false;
        field.setAccessible(true);
      }

      field.set(destinationEntity, value);

      if(wasAccessible == false)
        field.setAccessible(false);

      return true;
    } catch(Exception ex) {
      log.error("Could not set Value " + value + " on Object " + destinationEntity + " for field " + field);
    }

    return false;
  }

  public static Collection<BaseEntity> getCollectionFieldValue(BaseEntity entity, Field collectionField) {
    return (Collection<BaseEntity>)getFieldValue(entity, collectionField);
  }

  public static String getFieldName(Field field) {
    String fieldName = field.getName();

    // special fields
    if("abstractString".equals(fieldName))
      return "Abstract";

    char firstCharacter = Character.toUpperCase(fieldName.charAt(0));
    fieldName = firstCharacter + fieldName.substring(1);

    return fieldName;
  }

  public static Method findPropertyGetMethod(BaseEntity entity, Field propertyField) {
    String fieldName = propertyField.getName().toLowerCase();
    log.debug("Trying to find Getter for " + fieldName + " on Entity " + entity);

    // special fields
    if(entity instanceof Entry) {
      if(fieldName.equals("abstractstring")) {
        try { return entity.getClass().getMethod("getAbstract"); } catch(Exception ex) { }
      }
    }

    // first try to find getter manually
    try {
      String prefix = Boolean.class.equals(propertyField.getType()) ? "is" : "get";
      Method getter = entity.getClass().getMethod(prefix + getFieldName(propertyField));
      if(getter != null) return getter;
    } catch(Exception ex) {

    }

    for(Method method : entity.getClass().getMethods()) { // TODO: search private and protected Methods as well?
      if(method.getName().toLowerCase().endsWith(fieldName) && method.getName().length() <= fieldName.length() + 3 && method.getReturnType().equals(propertyField.getType())) {
//        log.debug("Found getter for field " + propertyField + ": " + method);
        return method;
      }
    }

    log.warn("Could not find getter for field " + propertyField);
    return null;
  }

}
