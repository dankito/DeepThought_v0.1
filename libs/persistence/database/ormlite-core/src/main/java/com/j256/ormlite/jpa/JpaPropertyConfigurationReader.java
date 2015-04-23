package com.j256.ormlite.jpa;

import com.j256.ormlite.dao.cda.jointable.JoinTableConfig;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.jpa.relationconfig.OneToManyConfig;
import com.j256.ormlite.jpa.relationconfig.OneToOneConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.Version;

/**
 * Created by ganymed on 05/03/15.
 */
public class JpaPropertyConfigurationReader {

  private final static Logger log = LoggerFactory.getLogger(JpaPropertyConfigurationReader.class);


  public <T, ID> void readEntityPropertiesConfiguration(EntityConfig<T, ID> entityConfig) throws SQLException {
    // TODO: either remove or implement configureIdProperty
//    Property idProperty = findIdProperty(entityConfig); // advantages of this method is it finds the Id property also in parent @Entity classes - but do i need this?
//    if(idProperty == null)
//      throw new SQLException("@Id not set on any Field or Method of Entity " + entityConfig.getEntityClass() + " or one of its @MappedSuperClass or @Entity super classes");

    // TODO: configure Id property
//    configureIdProperty()

    for(Property entityProperty : ReflectionHelper.getEntityPersistableProperties(entityConfig.getEntityClass())) {
      if(Registry.getPropertyRegistry().hasPropertyConfiguration(entityConfig.getEntityClass(), entityProperty) == true) // potentially dangerous as Properties on Parent classes
      // can be on multiple Entities, but its EntityConfig value is only set to first Entity's config
        entityConfig.addProperty(Registry.getPropertyRegistry().getPropertyConfiguration(entityConfig.getEntityClass(), entityProperty));
      else
        entityConfig.addProperty(readPropertyConfiguration(entityConfig, entityProperty));
    }
  }

  public Property findIdProperty(EntityConfig entityConfig) throws SQLException {
    Property foundIdProperty = null;

    for(Property property : ReflectionHelper.getEntityPersistableProperties(entityConfig.getEntityClass())) {
        if(foundIdProperty != null)
          throw new SQLException("@Id Annotation already found on " + foundIdProperty + ", but @Id is also set on property " + property + ". " +
              "Only for one Field or Method per Class Hierarchy @Id Annotation can be set.");

      foundIdProperty = property;
      if(entityConfig.getAccess() == null) { // only set Access if not already set explicitly through @Access annotation
        if(property.whereIsAnnotationPlaced(Id.class) == Property.AnnotationPlacement.GetMethod)
          entityConfig.setAccess(AccessType.PROPERTY);
        else
          entityConfig.setAccess(AccessType.FIELD);
      }

//      Method fieldGetMethod = ReflectionHelper.findGetMethod(property);
//
//      if(property.isAnnotationPresent(Id.class) && isAnnotatedWithTransient(property) == false) {
//        if(foundIdProperty != null)
//          throw new SQLException("@Id Annotation already found on " + foundIdProperty + ", but @Id is also set on property " + property + ". " +
//              "Only for one Field or Method per Class Hierarchy @Id Annotation can be set.");
//        if(fieldGetMethod != null && isAnnotatedWithTransient(fieldGetMethod))
//          continue;
//
//        foundIdProperty = new Property(property, fieldGetMethod, ReflectionHelper.findSetMethod(property));
//        if(entityConfig.getAccess() == null) // only set Access if not already set explicitly through @Access annotation
//          entityConfig.setAccess(AccessType.FIELD);
//      }
//      else if(fieldGetMethod != null && fieldGetMethod.isAnnotationPresent(Id.class) && isAnnotatedWithTransient(fieldGetMethod) == false) {
//        if(foundIdProperty != null)
//          throw new SQLException("@Id Annotation already found on " + foundIdProperty + ", but @Id is also set on method " + fieldGetMethod + ". " +
//              "Only for one Field or Method per Class Hierarchy @Id Annotation can be set.");
//        if(isAnnotatedWithTransient(property))
//          continue;
//
//        foundIdProperty = new Property(property, fieldGetMethod, ReflectionHelper.findSetMethod(property));
//        if(entityConfig.getAccess() == null) // only set Access if not already set explicitly through @Access annotation
//          entityConfig.setAccess(AccessType.PROPERTY);
//      }
    }

    return foundIdProperty;
  }

  protected PropertyConfig readPropertyConfiguration(EntityConfig entityConfig, Property property) throws SQLException {
    PropertyConfig propertyConfig = new PropertyConfig(entityConfig, property);

    Registry.getPropertyRegistry().registerPropertyConfiguration(entityConfig.getEntityClass(), property, propertyConfig);

    setSqlType(property, propertyConfig);
    readIdConfiguration(property, propertyConfig, entityConfig);
    readVersionConfiguration(property, propertyConfig);
    readBasicAnnotation(property, propertyConfig);
    readColumnAnnotation(property, propertyConfig);

    readRelationConfiguration(property, propertyConfig);

    return propertyConfig;
  }

  protected void setSqlType(Property property, PropertyConfig propertyConfig) throws SQLException {
    if(property.getType() == Date.class || property.getType() == Calendar.class)
      setDateOrCalenderSqlType(property, propertyConfig);
    else if(property.getType().isEnum())
      setEnumSqlType(property, propertyConfig);
//    else if(String.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.STRING);
//    else if(Long.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.LONG_OBJ);
//    else if(Integer.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.INTEGER);
//    else if(Boolean.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.BOOLEAN_OBJ);
//    else if(boolean.class.equals(property.getType()))
//      propertyConfig.setDataType(DataType.BOOLEAN);
      // TODO: configure Lob field; set settings according to p. 39/40
    else if(isAnnotationPresent(property, Lob.class)) {
//      DatabaseType databaseType = propertyConfig.getEntityConfig().getDatabaseType();
//      if()
      if(String.class.isAssignableFrom(property.getType()) || char[].class.isAssignableFrom(property.getType()) || Character[].class.isAssignableFrom(property.getType())) {
        propertyConfig.setDataType(DataType.STRING);
        propertyConfig.setColumnDefinition("longvarchar");
      }
      else {
        propertyConfig.setDataType(DataType.BYTE_ARRAY);
        propertyConfig.setColumnDefinition("longvarbinary");
      }
    }
    else {
      for(DataType dataType : DataType.values()) {
        if(property.getType().equals(dataType.getType())) {
          propertyConfig.setDataType(dataType);
          break;
        }
      }
    }

    if(propertyConfig.getDataType() == null && Collection.class.isAssignableFrom(property.getType()) == false) {
      if(isAnnotationPresent(property, OneToOne.class) == false && isAnnotationPresent(property, ManyToOne.class) == false &&
          isAnnotationPresent(property, OneToMany.class) == false && isAnnotationPresent(property, ManyToMany.class) == false)
        throw new SQLException("Don't know how to serialize Type of Property " + property + ". If it's a relationship, did you forget to set appropriate Annotation (@OneToOne, " +
            "@OneToMany, ...) on its field or get method?");
    }

    // TODO: also set other data type's SQL type
  }

  protected void setDateOrCalenderSqlType(Property property, PropertyConfig propertyConfig) {
    if(isAnnotationPresent(property, Temporal.class) == false) {
      log.warn("@Temporal not set on field " + property + ". According to JPA specification for data types java.util.Date and java.util.Calender @Temporal annotation " +
          "has to be set. Ignoring this java.sql.Timestamp is assumed for " + property.getFieldName());
//      propertyConfig.setSqlType(java.sql.Timestamp.class);
      propertyConfig.setDataType(DataType.DATE_LONG);
    }
    else {
      Temporal temporalAnnotation = getPropertyAnnotation(property, Temporal.class);
      switch(temporalAnnotation.value()) {
        case DATE:
//          propertyConfig.setSqlType(java.sql.Date.class);
          propertyConfig.setDataType(DataType.DATE);
          break;
        case TIME:
//          propertyConfig.setSqlType(java.sql.Time.class);
          propertyConfig.setDataType(DataType.DATE_LONG);
          break;
        default:
//          propertyConfig.setSqlType(java.sql.Timestamp.class);
          propertyConfig.setDataType(DataType.DATE_LONG);
          break;
      }
    }
  }

  protected void setEnumSqlType(Property property, PropertyConfig propertyConfig) {
    if(isAnnotationPresent(property, Enumerated.class)) {
      Enumerated enumeratedAnnotation = getPropertyAnnotation(property, Enumerated.class);
      if(enumeratedAnnotation.value() == EnumType.STRING) {
//        propertyConfig.setSqlType(String.class);
        propertyConfig.setDataType(DataType.ENUM_STRING);
        return;
      }
    }

//    propertyConfig.setSqlType(Integer.class);
    propertyConfig.setDataType(DataType.ENUM_INTEGER);
  }

  protected void readIdConfiguration(Property property, PropertyConfig propertyConfig, EntityConfig entityConfig) throws SQLException {
    if(isAnnotationPresent(property, Id.class)) {
      propertyConfig.setIsId(true);
      entityConfig.setIdProperty(propertyConfig);

      if(entityConfig.getAccess() == null) { // if access != null than it has been set by @AccessAnnotation
        if (property.whereIsAnnotationPlaced(Id.class) == Property.AnnotationPlacement.GetMethod) // otherwise access is determined where @Id Annotation is placed, on field or get method
          entityConfig.setAccess(AccessType.PROPERTY);
        else
          entityConfig.setAccess(AccessType.FIELD);
      }

      if(isAnnotationPresent(property, GeneratedValue.class)) {
        propertyConfig.setIsGeneratedId(true);
        GeneratedValue generatedValueAnnotation = getPropertyAnnotation(property, GeneratedValue.class);
        propertyConfig.setGeneratedIdType(generatedValueAnnotation.strategy());

        if(StringHelper.stringNotNullOrEmpty(generatedValueAnnotation.generator()))
          log.warn("Attribute generator of Annotation GeneratedValue (as used in " + property + ") is " + JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage);
      }

      if(isAnnotationPresent(property, SequenceGenerator.class))
        throwAnnotationNotSupportedException("SequenceGenerator", property );
      if(isAnnotationPresent(property, TableGenerator.class))
        throwAnnotationNotSupportedException("TableGenerator", property);
    }
  }

  protected void readVersionConfiguration(Property property, PropertyConfig propertyConfig) {
    if(isAnnotationPresent(property, Version.class)) {
      propertyConfig.setIsVersion(true);
    }
  }

  protected void readBasicAnnotation(Property property, PropertyConfig propertyConfig) {
    if(isAnnotationPresent(property, Basic.class)) {
      Basic basicAnnotation = getPropertyAnnotation(property, Basic.class);
      propertyConfig.setFetch(basicAnnotation.fetch());
      propertyConfig.setCanBeNull(basicAnnotation.optional());
    }
    // no Annotations neither on Field nor on Get-Method - then per default property gets treated as if
    // @Basic(fetch = FetchType.EAGER, optional = true)
    // would be set
    else if((property.getField() == null || property.getField().getAnnotations().length == 0) &&
        (property.getGetMethod() == null || property.getGetMethod().getAnnotations().length == 0)) {
      propertyConfig.setFetch(FetchType.EAGER);
      propertyConfig.setCanBeNull(true);
    }
  }

  protected void readColumnAnnotation(Property property, PropertyConfig propertyConfig) throws SQLException {
    if(isAnnotationPresent(property, Column.class)) {
      Column columnAnnotation = getPropertyAnnotation(property, Column.class);

      if(StringHelper.stringNotNullOrEmpty(columnAnnotation.name()))
        propertyConfig.setColumnName(columnAnnotation.name());
      if(StringHelper.stringNotNullOrEmpty(columnAnnotation.columnDefinition()))
        propertyConfig.setColumnDefinition(columnAnnotation.columnDefinition());

      propertyConfig.setUnique(columnAnnotation.unique());
      propertyConfig.setCanBeNull(columnAnnotation.nullable());
      propertyConfig.setInsertable(columnAnnotation.insertable());
      propertyConfig.setUpdatable(columnAnnotation.updatable());
      propertyConfig.setLength(columnAnnotation.length());

      if(StringHelper.stringNotNullOrEmpty(columnAnnotation.table()))
        throwAttributeNotSupportedException("table", "Column", property);

      if(columnAnnotation.precision() > 0 || columnAnnotation.scale() > 0)
        throwAttributeNotSupportedException("precision", "Column", property);
    }
  }


  protected void readRelationConfiguration(Property property, PropertyConfig propertyConfig) throws SQLException {
    if(isAnnotationPresent(property, OneToOne.class))
      readOneToOneConfiguration(property, propertyConfig, getPropertyAnnotation(property, OneToOne.class));
    if(isAnnotationPresent(property, ManyToOne.class))
      readManyToOneConfiguration(property, propertyConfig, getPropertyAnnotation(property, ManyToOne.class));
    if(isAnnotationPresent(property, OneToMany.class))
      readOneToManyConfiguration(property, propertyConfig, getPropertyAnnotation(property, OneToMany.class));
    if(isAnnotationPresent(property, ManyToMany.class))
      readManyToManyConfiguration(property, propertyConfig, getPropertyAnnotation(property, ManyToMany.class));

//    // if we have a collection then make it a foreign collection
//    if (Collection.class.isAssignableFrom(field.getType())
//        || Instances.getFieldTypeCreator().foreignCollectionCanBeAssignedToField(field.getType())) {
//      config.setForeignCollection(true);
//
//      if (joinColumnAnnotation != null && stringNotEmpty(joinColumnAnnotation.name())) {
//        config.setForeignCollectionColumnName(joinColumnAnnotation.name());
//      }
//      else
//        config.setForeignCollectionColumnName(fieldName);
//
//      Class otherSideClass = null;
//      if (oneToManyAnnotation != null) {
//        otherSideClass = configureOneToManyField(oneToManyAnnotation, config, field);
//      }
//      else if (manyToManyAnnotation != null) {
//        otherSideClass = configureManyToManyField(manyToManyAnnotation, config, field);
//      }
//
//      if(orderByAnnotation != null)
//        config.setOrderColumns(extractOrderColumns(orderByAnnotation, otherSideClass));
//    }
//    else {
////      otherwise it is a foreign field
//      config.setForeign(true);
//      config.setIsJoinColumn(true);
//
//      if (joinColumnAnnotation != null) {
//
//        if (stringNotEmpty(joinColumnAnnotation.name())) {
//          config.setColumnName(joinColumnAnnotation.name());
//        }
//        config.setCanBeNull(joinColumnAnnotation.nullable());
//        config.setUnique(joinColumnAnnotation.unique());
//      }
//
//      if (manyToOneAnnotation != null) {
//        configureManyToOneField(manyToOneAnnotation, config, field);
//      }
//      else if(oneToOneAnnotation != null) {
//        configureOneToOneField(oneToOneAnnotation, config, field);
//      }
//    }

//    Class otherSideClass = null;
//    readOrderByAnnotation(property, propertyConfig, otherSideClass);
  }

  protected void readOneToOneConfiguration(Property property, PropertyConfig propertyConfig, OneToOne oneToOneAnnotation) throws SQLException {
    propertyConfig.setIsOneCardinalityRelationshipProperty(true);
    propertyConfig.setIsOneToOneField(true);
    readJoinColumnConfiguration(property, propertyConfig);

    Class targetEntityClass = property.getType();
    if(oneToOneAnnotation.targetEntity() != void.class)
      targetEntityClass = oneToOneAnnotation.targetEntity();
    propertyConfig.setTargetEntityClass(targetEntityClass);

    FetchType fetch = oneToOneAnnotation.fetch();
    CascadeType[] cascade = oneToOneAnnotation.cascade();

    propertyConfig.setCascade(cascade);
    propertyConfig.setFetch(fetch);
    if(oneToOneAnnotation.optional() == false) // don't overwrite a may previously set value by JoinColumn
      propertyConfig.setCanBeNull(oneToOneAnnotation.optional()); // TODO: what's the difference between JoinColumn.nullable() and OneToOne.optional() ?
    if(fetch == FetchType.LAZY)
      log.warn("FetchType.LAZY as on property " + property + " is not supported for @OneToOne relationships as this would require Proxy Generation or Byte code manipulation " +
          "like with JavaAssist,  which is not supported on Android. As LAZY is per JPA specification only a hint, it will be in this case silently ignored and Fetch set to  EAGER.");

    if(oneToOneAnnotation.orphanRemoval() == true)
      throwAttributeNotSupportedException("orphanRemoval", "OneToOne", property);

    String joinColumnName = getJoinColumnName(property, targetEntityClass);

    configureOneToOneTargetProperty(property, propertyConfig, oneToOneAnnotation, targetEntityClass, fetch, cascade, joinColumnName);
  }

  protected void configureOneToOneTargetProperty(Property property, PropertyConfig propertyConfig, OneToOne oneToOneAnnotation, Class targetEntityClass, FetchType fetch, CascadeType[] cascade, String joinColumnName) throws SQLException {
    Property targetProperty = findOneToOneTargetProperty(property, oneToOneAnnotation.mappedBy(), targetEntityClass);

    if(targetProperty == null) { // unidirectional association
      propertyConfig.setIsJoinColumn(true);
      propertyConfig.setIsOwningSide(true);
      propertyConfig.setColumnName(joinColumnName);
      propertyConfig.setOneToOneConfig(new OneToOneConfig(property, targetEntityClass, joinColumnName, fetch, cascade)); // TODO: remove
    }
    else { // bidirectional @OneToOne association
      if(isAnnotationPresent(targetProperty, JoinColumn.class))
        joinColumnName = getPropertyAnnotation(targetProperty, JoinColumn.class).name(); // TODO: this should be false as if otherside has the JoinColumn i don't need to set it here

      Property owningSide;
      Property inverseSide;

      if (oneToOneAnnotation.mappedBy().isEmpty() == false) {
        propertyConfig.setIsInverseSide(true);
        propertyConfig.setIsBidirectional(true);
        propertyConfig.setTargetProperty(targetProperty);

        owningSide = targetProperty; // TODO: remove
        inverseSide = property;
        propertyConfig.setOneToOneConfig(new OneToOneConfig(owningSide, inverseSide, joinColumnName, fetch, cascade));
      }
      else if (getPropertyAnnotation(targetProperty, OneToOne.class).mappedBy().isEmpty() == false) {
        propertyConfig.setIsOwningSide(true);
        propertyConfig.setIsJoinColumn(true);
        propertyConfig.setIsBidirectional(true);
        propertyConfig.setColumnName(joinColumnName);
        propertyConfig.setTargetProperty(targetProperty);

        owningSide = property; // TODO: remove
        inverseSide = targetProperty;
        propertyConfig.setOneToOneConfig(new OneToOneConfig(owningSide, inverseSide, joinColumnName, fetch, cascade));
      }
      else { // if on both side mappedBy is not set we have two unidirectional relationships instead of one bidirectional
        log.warn("Just for case that this was not on purpose: On both @OneToOne sides no mappedBy value has been found, so two unidirectional relationships instead of one " +
            "bidirectional one will be created for properties " + property + " and " + targetProperty);
        propertyConfig.setIsOwningSide(true);
        propertyConfig.setIsJoinColumn(true);
      }
    }

    propertyConfig.setColumnName(joinColumnName); // TODO: really set Column Name even if it's not a Join Column?
  }

  protected Property findOneToOneTargetProperty(Property property, String mappedBy, Class targetEntityType) {
    Class classContainingField = property.getDeclaringClass();

    for(Property targetProperty : ReflectionHelper.getEntityPersistableProperties(targetEntityType)) { // TODO: search for properties, not for fields
      if(classContainingField.equals(targetProperty.getType())) {
        if (mappedBy.isEmpty() == false && mappedBy.equals(targetProperty.getFieldName()))
          return targetProperty;
        else if (isAnnotationPresent(targetProperty, OneToOne.class)) {
          OneToOne oneToOneAnnotation = getPropertyAnnotation(targetProperty, OneToOne.class);
          if (oneToOneAnnotation.mappedBy().isEmpty() == false && oneToOneAnnotation.mappedBy().equals(property.getFieldName()))
            return targetProperty;
        }
      }
    }

    return null; // an unidirectional OneToOne association
  }

  protected Property findOneToManyTargetProperty(Property property, String mappedBy, Class targetEntityType) {
    Class classContainingField = property.getDeclaringClass();

    for(Property targetProperty : ReflectionHelper.getEntityPersistableProperties(targetEntityType)) {
      if(classContainingField.equals(targetProperty.getType())) {
        if (mappedBy.isEmpty() == false && mappedBy.equals(targetProperty.getFieldName()))
          return targetProperty;
      }
    }

    return null; // an unidirectional OneToMany association
  }

  protected Property findManyToOneTargetProperty(Property property, Class oneSideClass) throws SQLException {
    for(Property targetClassProperty : ReflectionHelper.getEntityPersistableProperties(oneSideClass)) {
      if(isAnnotationPresent(targetClassProperty, OneToMany.class)) {
        OneToMany oneToManyAnnotation = getPropertyAnnotation(targetClassProperty, OneToMany.class);
        if(property.getFieldName().equals(oneToManyAnnotation.mappedBy())) {
          // now also check if it's the correct target entity type
          Class oneSideTargetEntity = getTargetEntityClass(oneToManyAnnotation, targetClassProperty);
          if(property.getDeclaringClass().equals(oneSideTargetEntity))
            return targetClassProperty;
        }
      }
    }

//    throw new SQLException("Could not find @OneToMany field on class " + oneSideClass.getName() + " for @ManyToOne field " + property.toString());
    return null; // TODO: what consequences does it have returning null?
  }

  protected Property findManyToManyTargetProperty(Property property, String mappedBy, Class targetEntityClass) {
    for(Property targetProperty : ReflectionHelper.getEntityPersistableProperties(targetEntityClass)) {
      if(isAnnotationPresent(targetProperty, ManyToMany.class)) {
        ManyToMany manyToManyAnnotation = getPropertyAnnotation(targetProperty, ManyToMany.class);
        if (manyToManyAnnotation.mappedBy().isEmpty() == false && manyToManyAnnotation.mappedBy().equals(property.getFieldName())) {
          if(manyToManyAnnotation.targetEntity() != void.class) {
            if(manyToManyAnnotation.targetEntity().isAssignableFrom(property.getDeclaringClass()))
              return targetProperty;
          }
          else if(targetProperty.isGenericType() && targetProperty.getGenericType().equals(property.getDeclaringClass()))
            return targetProperty;
        }
      }
      if (mappedBy.isEmpty() == false && mappedBy.equals(targetProperty.getFieldName()) && Collection.class.isAssignableFrom(targetProperty.getType())) {
        if(targetProperty.isGenericType()) {
          if(targetProperty.getGenericType().equals(property.getDeclaringClass()))
            return targetProperty;
        }
        else if(targetEntityClass.equals(targetProperty.getDeclaringClass()))
          return targetProperty;
      }
    }

    return null; // an unidirectional ManyToMany association
  }

  protected void readManyToOneConfiguration(Property property, PropertyConfig propertyConfig, ManyToOne manyToOneAnnotation) throws SQLException {
    propertyConfig.setIsManyToOneField(true);
    propertyConfig.setIsManyCardinalityRelationshipProperty(true);
    readJoinColumnConfiguration(property, propertyConfig);

    Class targetEntityClass = property.getType();
    if(manyToOneAnnotation.targetEntity() != void.class)
      targetEntityClass = manyToOneAnnotation.targetEntity();
    propertyConfig.setTargetEntityClass(targetEntityClass);

    CascadeType[] cascade = manyToOneAnnotation.cascade();
    FetchType fetch = manyToOneAnnotation.fetch();
    propertyConfig.setFetch(fetch);
    propertyConfig.setCascade(cascade);

    if(manyToOneAnnotation.optional() == false) // don't overwrite a may previously set value by JoinColumn
      propertyConfig.setCanBeNull(manyToOneAnnotation.optional()); // TODO: what's the difference between JoinColumn.nullable() and OneToOne.optional() ?
    if(fetch == FetchType.LAZY)
      log.warn("FetchType.LAZY as on property " + property + " is not supported for @ManyToOne relationships as this would require Proxy Generation or Byte code manipulation " +
          "like with JavaAssist,  which is not supported on Android. As LAZY is per JPA specification only a hint, it will be in this case silently ignored and Fetch set to  EAGER.");

    propertyConfig.setIsOwningSide(true);
    propertyConfig.setIsJoinColumn(true);
    String joinColumnName = getJoinColumnName(property, targetEntityClass);
    propertyConfig.setColumnName(joinColumnName);

    Property targetProperty = findManyToOneTargetProperty(property, targetEntityClass);
    if(targetProperty != null) {
      propertyConfig.setIsBidirectional(true);
      propertyConfig.setTargetProperty(targetProperty);
      propertyConfig.setOneToManyConfig(new OneToManyConfig(targetProperty, property, joinColumnName, fetch, cascade)); // TODO: remove
    }
    else
      propertyConfig.setOneToManyConfig(new OneToManyConfig(property, joinColumnName, fetch, cascade)); // TODO: remove
  }

  protected String getJoinColumnName(Property property, Class targetEntityClass) {
    String joinColumnName = targetEntityClass.getSimpleName().toLowerCase() + "_id"; // TODO: this is wrong! Don't assume other side's identity column to automatically called 'id', get real column name

    if(isAnnotationPresent(property, JoinColumn.class)) {
      JoinColumn joinColumn = getPropertyAnnotation(property, JoinColumn.class);
      if(joinColumn.name().isEmpty() == false)
        joinColumnName = joinColumn.name();
    }

    return joinColumnName;
  }

  protected Class getTargetEntityClass(OneToMany oneToManyAnnotation, Property property) throws SQLException{
    if(oneToManyAnnotation.targetEntity() != void.class)
      return oneToManyAnnotation.targetEntity();

    if(property.isGenericType() == false)
      throw new SQLException("For @OneToMany property " + property + " either Annotation's targetEntity value has to be set or Field's Datatype has to be a generic " +
          "Collection / Set with  generic type set to target entity's type.");

    return property.getGenericType();
  }

  protected Class getTargetEntityClass(ManyToMany manyToManyAnnotation, Field field) throws SQLException{
    if(manyToManyAnnotation.targetEntity() != void.class)
      return manyToManyAnnotation.targetEntity();

    if(field.getGenericType() instanceof ParameterizedType == false)
      throw new SQLException("For a @ManyToMany relation either Annotation's targetEntity value has to be set or Field's Datatype has to be a generic Collection / Set with " +
          "generic type set to target entity's type.");

    ParameterizedType genericType = (ParameterizedType)field.getGenericType();
    return  (Class<?>) genericType.getActualTypeArguments()[0];
  }

  protected void readJoinColumnConfiguration(Property property, PropertyConfig propertyConfig) throws SQLException {
    propertyConfig.setIsRelationshipProperty(true);

    if(isAnnotationPresent(property, JoinColumn.class)) {
      propertyConfig.setIsJoinColumn(true);

      JoinColumn joinColumnAnnotation = getPropertyAnnotation(property, JoinColumn.class);
      readJoinColumnConfiguration(property, propertyConfig, joinColumnAnnotation);
    }
  }

  protected void readJoinColumnConfiguration(Property property, PropertyConfig propertyConfig, JoinColumn joinColumnAnnotation) throws SQLException {
    if (StringHelper.stringNotNullOrEmpty(joinColumnAnnotation.name()))
      propertyConfig.setColumnName(joinColumnAnnotation.name());
    if(StringHelper.stringNotNullOrEmpty(joinColumnAnnotation.columnDefinition()))
      propertyConfig.setColumnDefinition(joinColumnAnnotation.columnDefinition());

    propertyConfig.setCanBeNull(joinColumnAnnotation.nullable());
    propertyConfig.setUnique(joinColumnAnnotation.unique());
    propertyConfig.setInsertable(joinColumnAnnotation.insertable());
    propertyConfig.setUpdatable(joinColumnAnnotation.updatable());

    if(StringHelper.stringNotNullOrEmpty(joinColumnAnnotation.referencedColumnName()))
      throwAttributeNotSupportedException("referencedColumnName", "JoinColumn", property);
    if(StringHelper.stringNotNullOrEmpty(joinColumnAnnotation.table()))
      throwAttributeNotSupportedException("table", "JoinColumn", property);
  }

  protected void readOneToManyConfiguration(Property property, PropertyConfig propertyConfig, OneToMany oneToManyAnnotation) throws SQLException {
    propertyConfig.setIsRelationshipProperty(true);
    propertyConfig.setIsOneCardinalityRelationshipProperty(true);
    propertyConfig.setIsOneToManyField(true);
    propertyConfig.getEntityConfig().addForeignCollection(propertyConfig);

    Class targetEntityClass = null;
    if (Collection.class.isAssignableFrom(property.getType()) == false) // property type has to be assignable to Collection
      throw new SQLException("Type of @OneToMany property " + property + " has to be assignable to a java.util.Collection");
    else if (oneToManyAnnotation.targetEntity() != void.class) // and either targetEntity value has to be set on @OneToMany Annotation
      targetEntityClass = oneToManyAnnotation.targetEntity();
    else if (property.isGenericType() == false) // or generic type has to be set on Collection
      throw new SQLException("Target Type of @OneToMany property " + property + " has be set on targetEntity attribute of @OneToMany Annotation or by specifying generic type of " +
          "java.util.Collection derived property data type (e.g. Collection<String>, Set<MyEntity>, ...)");
    else
      targetEntityClass = property.getGenericType();
    propertyConfig.setTargetEntityClass(targetEntityClass);

    CascadeType[] cascade = oneToManyAnnotation.cascade();
    FetchType fetch = oneToManyAnnotation.fetch();
    propertyConfig.setCascade(cascade);
    propertyConfig.setFetch(fetch);

    if (oneToManyAnnotation.orphanRemoval() == true)
      throwAttributeNotSupportedException("orphanRemoval", "OneToOne", property);

    if (oneToManyAnnotation.mappedBy() != null && oneToManyAnnotation.mappedBy().isEmpty() == false) {
      try {
        configureBidirectionalOneToManyField(property, propertyConfig, oneToManyAnnotation, fetch, cascade, targetEntityClass);
      } catch (Exception ex) {
        propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig);
        log.error("Could not configure bidirectional OneToMany field for property " + property, ex);
        throw new SQLException(ex);
      }
    }
    else { // ok, this means relation is not bidirectional
      configureUnidirectionalOneToManyField(property, propertyConfig, targetEntityClass, fetch, cascade);
      // TODO: unidirectional means we have to create a Join Table, this case is not supported yet
      throw new SQLException("Sorry, but unidirectional @OneToMany associations as for property " + property + " are not supported yet by this implementation. Please add a @ManyToOne field on the many side.");
    }

    readOrderByAnnotation(property, propertyConfig, targetEntityClass);
  }

  protected void configureBidirectionalOneToManyField(Property property, PropertyConfig propertyConfig, OneToMany oneToManyAnnotation, FetchType fetchType, CascadeType[] cascade, Class targetEntityClass) throws NoSuchFieldException {
    Property targetProperty = findOneToManyTargetProperty(property, oneToManyAnnotation.mappedBy(), targetEntityClass);
    if(targetProperty != null) {
      propertyConfig.setTargetProperty(targetProperty);
      propertyConfig.setIsBidirectional(true);
      propertyConfig.setIsInverseSide(true);
    }

    String joinColumnName = getJoinColumnName(targetProperty, targetEntityClass);
    propertyConfig.setOneToManyConfig(new OneToManyConfig(property, targetProperty, joinColumnName, fetchType, cascade)); // TODO: try to remove
  }

  protected void configureUnidirectionalOneToManyField(Property property, PropertyConfig propertyConfig, Class targetEntityClass, FetchType fetchType, CascadeType[] cascade) throws SQLException {
    propertyConfig.setIsBidirectional(false);
    propertyConfig.setIsOwningSide(true); // TODO: is this correct?

//    String joinColumnName = getOneToManyJoinColumnName(oneSideField);
//    propertyConfig.setOneToManyConfig(new OneToManyConfig(property, joinColumnName, fetchType, cascade));
  }

  protected void readManyToManyConfiguration(Property property, PropertyConfig propertyConfig, ManyToMany manyToManyAnnotation) throws SQLException {
    propertyConfig.setIsRelationshipProperty(true);
    propertyConfig.setIsManyCardinalityRelationshipProperty(true);
    propertyConfig.setIsManyToManyField(true);
    propertyConfig.getEntityConfig().addForeignCollection(propertyConfig);

    Class targetEntityClass = null;
    if (Collection.class.isAssignableFrom(property.getType()) == false) // property type has to be assignable to Collection
      throw new SQLException("Type of @ManyToMany property " + property + " has to be assignable to a java.util.Collection");
    else if (manyToManyAnnotation.targetEntity() != void.class) // and either targetEntity value has to be set on @ManyToMany Annotation
      targetEntityClass = manyToManyAnnotation.targetEntity();
    else if (property.isGenericType() == false) // or generic type has to be set on Collection
      throw new SQLException("Target Type of @ManyToMany property " + property + " has be set on targetEntity attribute of @ManyToMany Annotation or by specifying generic type of " +
          "java.util.Collection derived property data type (e.g. Collection<String>, Set<MyEntity>, ...)");
    else
      targetEntityClass = property.getGenericType();
    propertyConfig.setTargetEntityClass(targetEntityClass);

    CascadeType[] cascade = manyToManyAnnotation.cascade();
    FetchType fetch = manyToManyAnnotation.fetch();
    propertyConfig.setCascade(cascade);
    propertyConfig.setFetch(fetch);

    Property targetProperty = findManyToManyTargetProperty(property, manyToManyAnnotation.mappedBy(), targetEntityClass);

    if(targetProperty == null) {
      propertyConfig.setIsOwningSide(true);
      propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig);
      readJoinTableAnnotation(property, propertyConfig, targetEntityClass, null);
      propertyConfig.setManyToManyConfig(new ManyToManyConfig(property, targetEntityClass, fetch, cascade)); // TODO: try to remove
    }
    else {
      propertyConfig.setIsBidirectional(true);
      propertyConfig.setTargetProperty(targetProperty);

      Property owningSideProperty;
      Property inverseSideProperty;

      if(manyToManyAnnotation.mappedBy() != null && manyToManyAnnotation.mappedBy().isEmpty() == false) {
        propertyConfig.setIsInverseSide(true);
        inverseSideProperty = property;
        owningSideProperty = targetProperty;
      }
      else {
        propertyConfig.setIsOwningSide(true);
        propertyConfig.getEntityConfig().addJoinTableProperty(propertyConfig);
        readJoinTableAnnotation(property, propertyConfig, targetEntityClass, targetProperty);
        owningSideProperty = property;
        inverseSideProperty = targetProperty;
      }

      propertyConfig.setManyToManyConfig(new ManyToManyConfig(owningSideProperty, inverseSideProperty, fetch, cascade)); // TODO: try to remove
    }

    readOrderByAnnotation(property, propertyConfig, targetEntityClass);
  }

  protected JoinTableConfig readJoinTableAnnotation(Property owningSideProperty, PropertyConfig owningSidePropertyConfig, Class targetEntityClass, Property inverseSideProperty) throws SQLException {
    String owningSideEntityName = owningSidePropertyConfig.getEntityConfig().getTableName();
    String inverseSideEntityName = JpaEntityConfigurationReader.getEntityTableName(targetEntityClass);
    String joinTableName = owningSideEntityName + "_" + inverseSideEntityName; // TODO: check if table name is unique
    String owningSideJoinColumnNameStub = owningSidePropertyConfig.getEntityConfig().getTableName() + "_"; // if applied id column name has to be appended by calling (expensive) getIdColumnName(owningSidePropertyConfig.getEntityConfig())
    String inverseSideJoinColumnNameStub = owningSidePropertyConfig.getColumnName() + "_"; // if applied id column name has to be appended by calling (expensive) getIdColumnName(targetEntityClass)

    if(isAnnotationPresent(owningSideProperty, JoinTable.class) == false) {
      owningSidePropertyConfig.setColumnName(owningSideJoinColumnNameStub + getIdColumnName(owningSidePropertyConfig.getEntityConfig()));
      inverseSideJoinColumnNameStub +=  getIdColumnName(targetEntityClass);
      JoinTableConfig joinTable = new JoinTableConfig(joinTableName, owningSidePropertyConfig, targetEntityClass, inverseSideJoinColumnNameStub, inverseSideProperty);
      owningSidePropertyConfig.setJoinTable(joinTable);
      return joinTable;
    }
    else {
      JoinTable joinTableAnnotation = getPropertyAnnotation(owningSideProperty, JoinTable.class);
      if(StringHelper.stringNotNullOrEmpty(joinTableAnnotation.name()))
        joinTableName = joinTableAnnotation.name();

      if(joinTableAnnotation.joinColumns().length > 1)
        throw new SQLException("Sorry for the inconvenience, but @JoinTable with more than one @JoinColumn value as on property " + owningSideProperty + " is not supported");
      else if(joinTableAnnotation.joinColumns().length == 1)
        readJoinColumnConfiguration(owningSideProperty, owningSidePropertyConfig, joinTableAnnotation.joinColumns()[0]);
      else
        owningSidePropertyConfig.setColumnName(getJoinColumnName(owningSideProperty, owningSidePropertyConfig.getTargetEntityClass()));

      if(joinTableAnnotation.inverseJoinColumns().length > 1)
        throw new SQLException("Sorry for the inconvenience, but @JoinTable with more than one @InverseJoinColumn value as on property " + owningSideProperty + " is not supported");
      // TODO:
      else if(joinTableAnnotation.inverseJoinColumns().length == 1)
        inverseSideJoinColumnNameStub += joinTableAnnotation.inverseJoinColumns()[0].name();
      else
        inverseSideJoinColumnNameStub += getIdColumnName(targetEntityClass);

      JoinTableConfig joinTable = new JoinTableConfig(joinTableName, owningSidePropertyConfig, targetEntityClass, inverseSideJoinColumnNameStub, inverseSideProperty);
      owningSidePropertyConfig.setJoinTable(joinTable);

      if(joinTableAnnotation.inverseJoinColumns().length == 1)
        readJoinColumnConfiguration(inverseSideProperty, joinTable.getInverseSideJoinColumn(), joinTableAnnotation.inverseJoinColumns()[0]);

      // TODO: read other JoinTable settings

      return joinTable;
    }
  }

  protected String getIdColumnName(EntityConfig entity) {
    if(entity.getIdProperty() != null)
      return entity.getIdProperty().getColumnName();

    return "id"; // TODO: search for Id column
  }

  protected String getIdColumnName(Class entityClass) {
    return "id"; // TODO: check if Registry contains config for entity class. If so call getIdColumn(EntityConfig). If not search for Id column
  }

  protected void readOrderByAnnotation(Property property, PropertyConfig propertyConfig, Class targetEntityClass) throws SQLException {
    if(isAnnotationPresent(property, OrderBy.class))
      propertyConfig.setOrderColumns(extractOrderColumns(getPropertyAnnotation(property, OrderBy.class), targetEntityClass));
  }

  protected List<com.j256.ormlite.stmt.query.OrderBy> extractOrderColumns(OrderBy orderByAnnotation, Class targetEntityClass) throws SQLException {
    List<com.j256.ormlite.stmt.query.OrderBy> orderBy = new ArrayList<>();

    for(String orderByString : orderByAnnotation.value().split(",")) {
      orderByString = orderByString.trim();
      orderBy.add(extractOrderColumn(orderByString, targetEntityClass));
    }

    return orderBy;
  }

  protected com.j256.ormlite.stmt.query.OrderBy extractOrderColumn(String orderByString, Class targetEntityClass) throws SQLException {
    boolean ascending = orderByString.endsWith("DESC") == false;
    String orderColumnFieldName = null;

    if(orderByString.contains("ASC"))
      orderColumnFieldName = orderByString.replace("ASC", "").trim();
    else if(orderByString.contains("DESC"))
      orderColumnFieldName = orderByString.replace("DESC", "").trim();
    else if(orderByString.length() > 0)
      orderColumnFieldName = orderByString.trim();

    if(orderColumnFieldName != null) {
      Property orderByProperty = ReflectionHelper.findPropertyByName(targetEntityClass, orderColumnFieldName);
      if(orderByProperty != null) {
        if(Registry.getPropertyRegistry().hasPropertyConfiguration(targetEntityClass, orderByProperty)) {
          String columnName = Registry.getPropertyRegistry().getPropertyConfiguration(targetEntityClass, orderByProperty).getColumnName(); // TODO: this is not completely correct as Database may affords Upper Case column names
          return new com.j256.ormlite.stmt.query.OrderBy(columnName, ascending);
        }
        else // column not yet configured (that means its Entity configuration hasn't been read yet)
          return new com.j256.ormlite.stmt.query.OrderBy(targetEntityClass, orderByProperty, ascending); // -> save Property for later column name retrieval // TODO: dito
      }
    }

    Property idProperty = findIdProperty(targetEntityClass); // if column name for OrderBy is not set, entities get per default sorted by their Ids
    if(idProperty != null) { // actually this should never be the case for an entity
      if(Registry.getPropertyRegistry().hasPropertyConfiguration(targetEntityClass, idProperty)) {
        String columnName = Registry.getPropertyRegistry().getPropertyConfiguration(targetEntityClass, idProperty).getColumnName();
        return new com.j256.ormlite.stmt.query.OrderBy(columnName, ascending);
      }
      else
        return new com.j256.ormlite.stmt.query.OrderBy(targetEntityClass, idProperty, ascending);
    }

    throw new SQLException("Could not find column for OrderBy string '" + orderByString + "' mapped to class " + targetEntityClass);
  }

  public static String getColumnNameForField(Field field) throws SQLException {
    if(Registry.getPropertyRegistry().hasPropertyForField(field))
      return Registry.getPropertyRegistry().getPropertyConfiguration(field).getColumnName();

    String columnName = null;

    throw new SQLException("TODO: no way found yet to get OrderBy column for a not yet configured field");
//    if(isAnnotationPresent(property, Column.class)) {
//      Column columnAnnotation = (Column)getPropertyAnnotation()
//    }
//
//    Column column = field.getAnnotation(Column.class);
//
//    columnName = field.getName();

    // if not already registered, it's almost impossible to get database type for otherside's entity
    // -> create a Defered loading OrderBy class which gets field's column name on access, not now (ergo on configuration)?
//    if(entityConfig.getDatabaseType().isEntityNamesMustBeUpCase())
//      columnName = columnName.toUpperCase();

//     return columnName;
  }

  protected Property findIdProperty(Class clazz) {
    for(Property property : ReflectionHelper.getEntityPersistableProperties(clazz)) {
      if(isAnnotationPresent(property, Id.class))
        return property;
    }

    return null;
  }

  protected boolean isAnnotationPresent(Property property, Class<? extends Annotation> annotationClass) {
    if(property.hasAnnotationExistenceAlreadyBeenDecided(annotationClass))
      return property.isAnnotatedWithAnnotation(annotationClass);

    if(property.getField() != null && property.getField().isAnnotationPresent(annotationClass)) {
      property.annotationFound(annotationClass, Property.AnnotationPlacement.Field);
      return true;
    }
    else if(property.getGetMethod() != null && property.getGetMethod().isAnnotationPresent(annotationClass)) {
      property.annotationFound(annotationClass, Property.AnnotationPlacement.GetMethod);
      return true;
    }
    else {
      property.annotationNotAvailable(annotationClass);
      return false;
    }
  }

  protected <T extends Annotation> T getPropertyAnnotation(Property property, Class<T> annotationClass) {
    if(property.hasAnnotatedInstanceBeenRetrieved(annotationClass))
      return (T)property.getAnnotatedInstance(annotationClass);

    if(property.getField() != null && property.getField().isAnnotationPresent(annotationClass)) {
      T annotationInstance = property.getField().getAnnotation(annotationClass);
      property.annotatedInstanceExtracted(annotationClass, annotationInstance);
      return annotationInstance;
    }
     else if(property.getGetMethod() != null && property.getGetMethod().isAnnotationPresent(annotationClass)) {
      T annotationInstance = property.getGetMethod().getAnnotation(annotationClass);
      property.annotatedInstanceExtracted(annotationClass, annotationInstance);
      return annotationInstance;
    }

    return null;
  }


  protected void throwAnnotationNotSupportedException(String annotationName, Property property) throws SQLException {
    throw new SQLException("Annotation @" + annotationName + " (as used in " + property + ") is " + JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage);
  }

  protected void throwAttributeNotSupportedException(String attributeName, String annotationName, Property property) throws SQLException {
    throw new SQLException("Attribute " + attributeName + " of Annotation @" + annotationName + " (as used in " + property + ") is " + JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage);
  }

}
