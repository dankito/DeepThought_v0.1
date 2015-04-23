package com.j256.ormlite.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

/**
 * Created by ganymed on 05/03/15.
 */
public class ReflectionHelper {

  private final static Logger log = LoggerFactory.getLogger(ReflectionHelper.class);

  protected static Map<Class, List<Property>> mapExtractedEntityProperties = new HashMap<>();

  protected static Map<Class, Map<String, Property>> mapFoundPropertiesByName = new HashMap<>();
  protected static Map<Field, Method> mapFoundFieldGetMethods = new HashMap<>();
  protected static Map<Field, Method> mapFoundFieldSetMethods = new HashMap<>();


//  public static Field findFieldByName(Class clazz, String fieldName) {
//    for(Field field : getEntityFields(clazz)) {
//      if(fieldName.equals(field.getName()))
//        return field;
//    }
//
//    return null;
//  }

  public static Property findPropertyByName(Class clazz, String propertyName) {
    if(mapFoundPropertiesByName.containsKey(clazz) && mapFoundPropertiesByName.get(clazz).containsKey(propertyName))
      return mapFoundPropertiesByName.get(clazz).get(propertyName);

    for(Property property : getEntityPersistableProperties(clazz)) {
      if(propertyName.equals(property.getFieldName())) {
        foundProperty(clazz, propertyName, property);
        return property;
      }
    }

    return null;
  }

  protected static void foundProperty(Class clazz, String propertyName, Property property) {
    if(mapFoundPropertiesByName.containsKey(clazz) == false)
      mapFoundPropertiesByName.put(clazz, new HashMap<String, Property>());

    mapFoundPropertiesByName.get(clazz).put(propertyName, property);
  }


//  public static Field findIdField(Class clazz) {
//    for(Field field : getAllDeclaredFieldsInClassHierarchy(clazz)) {
//      if(field.isAnnotationPresent(Id.class))
//        return field;
//    }
//
//    return null;
//  }

  /**
   * Gets all Entity's persistable fields including its MappedSuperClass super classes fields
   * @param entityClass
   * @return
   */
  public static List<Field> getEntityFields(Class entityClass) {
    List<Field> declaredFields = new ArrayList<>();
    extractClassFields(declaredFields, entityClass);

    for (Class<?> classWalk = entityClass.getSuperclass(); classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(JpaEntityConfigurationReader.classIsMappedSuperClass(classWalk) == false) // mapped entities stop here
        break;

      extractClassFields(declaredFields, classWalk);
    }

    return declaredFields;
  }

  protected static void extractClassFields(List<Field> declaredFields, Class<?> classWalk) {
    Field[] fields = classWalk.getDeclaredFields();

    for(Field field : fields) {
//      if(isNonFinalNonStaticNonTransientField(field))
        declaredFields.add(field);
    }
  }

  protected static Map<String, Method> getEntityGetMethods(Class entityClass) {
    Map<String, Method> persistableGetMethods = new HashMap<>();
    extractClassGetMethods(persistableGetMethods, entityClass);

    for (Class<?> classWalk = entityClass.getSuperclass(); classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(JpaEntityConfigurationReader.classIsMappedSuperClass(classWalk) == false) // mapped entities stop here
        break;

      extractClassGetMethods(persistableGetMethods, classWalk);
    }

    return persistableGetMethods;
  }

  protected static void extractClassGetMethods(Map<String, Method> persistableGetMethods, Class<?> classWalk) {
    Method[] methods = classWalk.getDeclaredMethods();

    for(Method method : methods) {
      if(isGetMethod(method)) { // is a get method
//        if(isNonFinalNonStaticNonAbstractMethod(method)) // is persistable
          persistableGetMethods.put(method.getName(), method); // TODO: this hides overwritten methods from sub classes
      }
    }
  }

  /**
   * Gets all Entity's persistable properties including its MappedSuperClass super classes properties
   * @param entityClass
   * @return
   */
  public static List<Property> getEntityPersistableProperties(Class entityClass) {
    if(mapExtractedEntityProperties.containsKey(entityClass))
      return mapExtractedEntityProperties.get(entityClass);

    List<Property> persistableProperties = new ArrayList<>();

    List<Field> persistableFields = getEntityFields(entityClass);
    Map<String, Method> persistableGetMethods = getEntityGetMethods(entityClass);

    for(Field persistableField : persistableFields) {
      Method getMethod = findGetMethod(persistableField, persistableGetMethods);
      if(getMethod != null)
        persistableGetMethods.remove(getMethod.getName());

      if(isNonFinalNonStaticNonTransientField(persistableField) && (getMethod == null || isNonFinalNonStaticNonAbstractMethod(getMethod))) {
        Property property = findPropertyForField(persistableField, getMethod);
        extractPropertyAnnotations(property);
        persistableProperties.add(property);
        foundProperty(entityClass, property.getFieldName(), property);
      }
    }

    // TODO: also respect 'get' Methods without a field?
//    for(Method getMethod : persistableGetMethods.values()) { // TODO: apply same mechanism as above (extractAnnotations, foundProperty)
//      if(isNonFinalNonStaticNonAbstractMethod(getMethod)) {
//        persistableProperties.add(new Property(null, getMethod, ReflectionHelper.findSetMethod(getMethod))); // TODO: filter out getMethods to not persistable fields (like
//        // ReferenceBase.personRoles)
//      }
//    }

    mapExtractedEntityProperties.put(entityClass, persistableProperties);
    return persistableProperties;
  }

//  /**
//   * Gets all persistable fields in Class hierarchy until a super class is not annotated with @Entity or @MappedSuperClass
//   * @param clazz
//   * @return
//   */
//  public static List<Field> getAllDeclaredFieldsInClassHierarchy(Class clazz) {
//    List<Field> declaredFields = new ArrayList<>();
//
//    for (Class<?> classWalk = clazz; classWalk != null; classWalk = classWalk.getSuperclass()) {
//      if(JpaEntityConfigurationReader.classIsEntityOrMappedSuperclass(classWalk) == false) // mapped entities stop here
//        break;
//
//      extractClassFields(declaredFields, classWalk);
//    }
//
//    return declaredFields;
//  }


  public static Property findPropertyForField(Field persistableField) {
    return findPropertyForField(persistableField, ReflectionHelper.findGetMethod(persistableField));
  }

  public static Property findPropertyForField(Field persistableField, Method getMethod) {
    return new Property(persistableField, getMethod, ReflectionHelper.findSetMethod(persistableField));
  }


  protected static Method findGetMethod(Field field, Map<String, Method> fieldGetMethods) {
    String methodName = createMethodNameFromField(field, "get"); // first try to find method with get-Prefix
    if(fieldGetMethods.containsKey(methodName))
      return fieldGetMethods.get(methodName);

    methodName = createMethodNameFromField(field, "is"); // then with is-Prefix (for boolean fields)
    if(fieldGetMethods.containsKey(methodName))
      return fieldGetMethods.get(methodName);

    methodName = createMethodNameFromField(field, "has"); // and finally with has-Prefix (for boolean fields)
    if(fieldGetMethods.containsKey(methodName))
      return fieldGetMethods.get(methodName);

    return findGetMethod(field);
  }

  /**
   * Find and return the appropriate getter method for field.
   *
   * @return Get method or null if none found.
   */
  public static Method findGetMethod(Field field) {
    return findGetMethod(field, false);
  }

  /**
   * Find and return the appropriate getter method for field.
   *
   * @return Get method or null if none found.
   */
  public static Method findGetMethod(Field field, boolean throwExceptions) {
    if(mapFoundFieldGetMethods.containsKey(field))
      return mapFoundFieldGetMethods.get(field);

    Method fieldGetMethod = null;

    String methodName = createMethodNameFromField(field, "get"); // first try to find method with get-Prefix
    try {
      fieldGetMethod = field.getDeclaringClass().getMethod(methodName);
      if(methodHasNoParameters(fieldGetMethod) == false) // not a Getter
        fieldGetMethod = null;
    } catch (Exception e) { }

    if(fieldGetMethod == null) {
      methodName = createMethodNameFromField(field, "is"); // then with is-Prefix (for boolean fields)
      try {
        fieldGetMethod = field.getDeclaringClass().getMethod(methodName);
        if(methodHasNoParameters(fieldGetMethod) == false) // not a Getter
          fieldGetMethod = null;
      } catch (Exception e) { }
    }

    if(fieldGetMethod == null) {
      methodName = createMethodNameFromField(field, "has"); // and finally with has-Prefix (for boolean fields)
      try {
        fieldGetMethod = field.getDeclaringClass().getMethod(methodName);
        if(methodHasNoParameters(fieldGetMethod) == false) // not a Getter
          fieldGetMethod = null;
      } catch (Exception e) {
        if (throwExceptions) {
          throw new IllegalArgumentException("Could not find appropriate get method for " + field);
        } else {
          return null;
        }
      }
    }

    if (fieldGetMethod.getReturnType() != field.getType() && field.getType().isAssignableFrom(fieldGetMethod.getReturnType())) {
      if (throwExceptions) {
        throw new IllegalArgumentException("Return type of get method " + methodName + " does not return " + field.getType());
      } else {
        return null;
      }
    }

    mapFoundFieldGetMethods.put(field, fieldGetMethod);
    return fieldGetMethod;
  }

  /**
   * Find and return the appropriate setter method for field.
   *
   * @return Set method or null if none found.
   */
  public static Method findSetMethod(Field field) {
    return findSetMethod(field, false);
  }

  /**
   * Find and return the appropriate setter method for field.
   *
   * @return Set method or null if none found.
   */
  public static Method findSetMethod(Field field, boolean throwExceptions) {
    if(mapFoundFieldSetMethods.containsKey(field))
      return mapFoundFieldSetMethods.get(field);

    Method fieldSetMethod;

    String methodName = createMethodNameFromField(field, "set");
    try {
      fieldSetMethod = field.getDeclaringClass().getMethod(methodName, field.getType());
    } catch (Exception e) {
      if (throwExceptions) {
        throw new IllegalArgumentException("Could not find appropriate set method for " + field);
      } else {
        return null;
      }
    }
    if (fieldSetMethod.getReturnType() != void.class) {
      if (throwExceptions) {
        throw new IllegalArgumentException("Return type of set method " + methodName + " returns "
            + fieldSetMethod.getReturnType() + " instead of void");
      } else {
        return null;
      }
    }

    mapFoundFieldSetMethods.put(field, fieldSetMethod);
    return fieldSetMethod;
  }

  /**
   * Find and return the appropriate setter method for method.
   *
   * @return Set method or null if none found.
   */
  public static Method findSetMethod(Method getMethod) {
    return findSetMethod(getMethod, false);
  }

  /**
   * Find and return the appropriate setter method for method.
   *
   * @return Set method or null if none found.
   */
  public static Method findSetMethod(Method getMethod, boolean throwExceptions) {
    Method fieldSetMethod;

    String getMethodName = getMethod.getName();
    String setMethodName = null;
    if(getMethodName.startsWith("get"))
      setMethodName = "s" + getMethodName.substring(1);
    else if(getMethodName.startsWith("is"))
      setMethodName = "set" + getMethodName.substring(2);
    else if(getMethodName.startsWith("has"))
      setMethodName = "set" + getMethodName.substring(3);

    try {
      fieldSetMethod = getMethod.getDeclaringClass().getMethod(setMethodName, getMethod.getReturnType());
    } catch (Exception e) {
      if (throwExceptions) {
        throw new IllegalArgumentException("Could not find appropriate set method for " + getMethod);
      } else {
        return null;
      }
    }
    if (fieldSetMethod.getReturnType() != void.class) {
      if (throwExceptions) {
        throw new IllegalArgumentException("Return type of set method " + setMethodName + " returns "
            + fieldSetMethod.getReturnType() + " instead of void");
      } else {
        return null;
      }
    }
    return fieldSetMethod;
  }

  private static String createMethodNameFromField(Field field, String prefix) {
    return prefix + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
  }

  private static void extractPropertyAnnotations(Property property) {
    if(property.getField() != null) {
      for(Annotation annotation : property.getField().getDeclaredAnnotations())
        property.annotationFound(annotation.getClass(), annotation, Property.AnnotationPlacement.Field);
    }

    if(property.getGetMethod() != null) {
      for(Annotation annotation : property.getGetMethod().getDeclaredAnnotations())
        property.annotationFound(annotation.getClass(), annotation, Property.AnnotationPlacement.GetMethod);
    }
  }

  /**
   * Locate the no arg constructor for the class.
   */
  public static <T> Constructor<T> findNoArgConstructor(Class<T> dataClass) {
    Constructor<T>[] constructors;
    try {
      @SuppressWarnings("unchecked")
      Constructor<T>[] consts = (Constructor<T>[]) dataClass.getDeclaredConstructors();
      // i do this [grossness] to be able to move the Suppress inside the method
      constructors = consts;
    } catch (Exception e) {
      throw new IllegalArgumentException("Can't lookup declared constructors for " + dataClass, e);
    }
    for (Constructor<T> con : constructors) {
      if (con.getParameterTypes().length == 0) {
        if (!con.isAccessible()) {
          try {
            con.setAccessible(true);
          } catch (SecurityException e) {
            throw new IllegalArgumentException("Could not open access to constructor for " + dataClass);
          }
        }
        return con;
      }
    }
    if (dataClass.getEnclosingClass() == null) {
      throw new IllegalArgumentException("Can't find a no-arg constructor for " + dataClass);
    } else {
      throw new IllegalArgumentException("Can't find a no-arg constructor for " + dataClass
          + ".  Missing static on inner class?");
    }
  }

  public static boolean isNonFinalNonStaticNonTransientField(Field field) {
    return isFinalField(field) == false && isStaticField(field) == false && isTransientField(field) == false && field.isAnnotationPresent(Transient.class) == false;
  }

  public static boolean isFinalField(Field field) {
    return Modifier.isFinal(field.getModifiers());
  }

  public static boolean isStaticField(Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  public static boolean isTransientField(Field field) {
    return Modifier.isTransient(field.getModifiers());
  }

  public static boolean isNonFinalNonStaticNonAbstractMethod(Method method) {
    return isFinalMethod(method) == false && isStaticMethod(method) == false && isAbstractMethod(method) == false && method.isAnnotationPresent(Transient.class) == false;
  }

  public static boolean isFinalMethod(Method method) {
    return Modifier.isFinal(method.getModifiers());
  }

  public static boolean isStaticMethod(Method method) {
    return Modifier.isStatic(method.getModifiers());
  }

  public static boolean isAbstractMethod(Method method) {
    return Modifier.isAbstract(method.getModifiers());
  }

  public static boolean isGetMethod(Method method) {
    String methodName = method.getName();
    return methodName.startsWith("get") /*|| methodName.startsWith("is") || methodName.startsWith("has")*/ && methodHasNoParameters(method);
  }

  public static boolean methodHasNoParameters(Method method) {
    return method.getParameterTypes().length == 0;
  }

}
