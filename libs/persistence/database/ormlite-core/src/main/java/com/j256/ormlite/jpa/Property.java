package com.j256.ormlite.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 07/03/15.
 */
public class Property {

  public enum AnnotationPlacement { Field, GetMethod }


  protected Field field;
  protected Method getMethod;
  protected Method setMethod;

  protected Map<Class<? extends Annotation>, AnnotationPlacement> mapFoundAnnotations = new HashMap<>();
  protected List<Class<? extends Annotation>> listNotAvailableAnnotations = new ArrayList<>();
  protected Map<Class<? extends Annotation>, Annotation> mapExtractedAnnotationInstances = new HashMap<>();


  public Property(Field field) {
    this(field, null);
  }

  public Property(Field field, Method getMethod) {
    this(field, getMethod, null);
  }

  public Property(Field field, Method getMethod, Method setMethod) {
    this.field = field;
    this.getMethod = getMethod;
    this.setMethod = setMethod;

    setAccess();
  }

  private void setAccess() {
    if(field != null && field.isAccessible() == false)
      field.setAccessible(true);

    if(getMethod != null && getMethod.isAccessible() == false)
      getMethod.setAccessible(true);

    if(setMethod != null && setMethod.isAccessible() == false)
      setMethod.setAccessible(true);
  }


  public Field getField() {
    return field;
  }

  public Method getGetMethod() {
    return getMethod;
  }

  public Method getSetMethod() {
    return setMethod;
  }


  public Class getType() {
    if(field != null)
      return field.getType();

    if(getMethod != null) {
      return getMethod.getReturnType();
    }

    if(setMethod != null && setMethod.getParameterTypes().length == 1)
      return setMethod.getParameterTypes()[0];

    return null; // should never come to this
  }

  public Class getDeclaringClass() {
    if(field != null)
      return field.getDeclaringClass();

    if(getMethod != null) {
      return getMethod.getDeclaringClass();
    }

    if(setMethod != null)
      return setMethod.getDeclaringClass();

    return null; // should never come to this
  }

  public String getFieldName() {
    if(field != null)
      return field.getName();

    if(getMethod != null) {
      String methodName = getMethod.getName();
      if(methodName.startsWith("get"))
        return extractFieldNameFromMethodName(methodName, 3);
      else if(methodName.startsWith("is"))
        return extractFieldNameFromMethodName(methodName, 2);
      else if(methodName.startsWith("has"))
        return extractFieldNameFromMethodName(methodName, 3);
    }

    if(setMethod != null)
      return extractFieldNameFromMethodName(setMethod.getName(), 3);

    return ""; // should never come to this
  }

  protected String extractFieldNameFromMethodName(String methodName, int countLettersToRemove) {
    String fieldName = methodName.substring(countLettersToRemove);
    String firstChar = new String(new char[] { fieldName.charAt(0) });

    return firstChar.toLowerCase() + fieldName.substring(1);
  }

  public boolean isGenericType() {
    if(field != null)
      return field.getGenericType() instanceof ParameterizedType;

    if(getMethod != null) {
      return getMethod.getGenericReturnType() instanceof ParameterizedType;
    }

    if(setMethod != null && setMethod.getParameterTypes().length == 1)
      return setMethod.getGenericParameterTypes()[0] instanceof ParameterizedType;

    return false; // should never come to this
  }

  public Class getGenericType() {
    if(field != null && field.getGenericType() instanceof ParameterizedType)
      return getGenericTypeClassFromType(field.getGenericType());

    if(getMethod != null && getMethod.getGenericReturnType() instanceof ParameterizedType) {
      return getGenericTypeClassFromType(getMethod.getGenericReturnType());
    }

    if(setMethod != null && setMethod.getParameterTypes().length == 1 && setMethod.getGenericParameterTypes()[0] instanceof ParameterizedType)
      return getGenericTypeClassFromType(setMethod.getGenericParameterTypes()[0]);

    return null; // should never come to this
  }

  protected Class getGenericTypeClassFromType(Type genericType) {
    return (Class)((ParameterizedType) genericType).getActualTypeArguments()[0];
  }


  public void annotationFound(Class<? extends Annotation> annotation, AnnotationPlacement placement) {
    mapFoundAnnotations.put(annotation, placement);
  }

  public void annotationFound(Class<? extends Annotation> annotationClass, Annotation instance, AnnotationPlacement placement) {
    annotationFound(annotationClass, placement);
    annotatedInstanceExtracted(annotationClass, instance);
  }

  public void annotationNotAvailable(Class<? extends Annotation> annotation) {
    listNotAvailableAnnotations.add(annotation);
  }

  public boolean hasAnnotationExistenceAlreadyBeenDecided(Class<? extends Annotation> annotation) {
    return listNotAvailableAnnotations.contains(annotation) || mapFoundAnnotations.containsKey(annotation);
  }

  public boolean isAnnotatedWithAnnotation(Class<? extends Annotation> annotation) {
    return mapFoundAnnotations.containsKey(annotation);
  }

  public AnnotationPlacement whereIsAnnotationPlaced(Class<? extends Annotation> annotation) {
    return mapFoundAnnotations.get(annotation);
  }

  public boolean hasAnnotatedInstanceBeenRetrieved(Class<? extends Annotation> annotationClass) {
    return mapExtractedAnnotationInstances.containsKey(annotationClass);
  }

  public void annotatedInstanceExtracted(Class<? extends Annotation> annotationClass, Annotation instance) {
    mapExtractedAnnotationInstances.put(annotationClass, instance);
  }

  public Annotation getAnnotatedInstance(Class<? extends Annotation> annotationClass) {
    return mapExtractedAnnotationInstances.get(annotationClass);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Property)) return false;

    Property property = (Property) o;

    if (field != null ? !field.equals(property.field) : property.field != null) return false;
    if (getMethod != null ? !getMethod.equals(property.getMethod) : property.getMethod != null) return false;
    if (setMethod != null ? !setMethod.equals(property.setMethod) : property.setMethod != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = field != null ? field.hashCode() : 0;
    result = 31 * result + (getMethod != null ? getMethod.hashCode() : 0);
    result = 31 * result + (setMethod != null ? setMethod.hashCode() : 0);
    return result;
  }


  @Override
  public String toString() {
    if(field != null)
      return "Field " + field.getName() + " on Class " + field.getDeclaringClass().getName();
    else if(getMethod != null)
      return "Field is null, using Get-Method " + getMethod.getName() + " on Class " + getMethod.getDeclaringClass().getName();
    return "Field and Get-Method are null, this should never happen!";
  }
}
