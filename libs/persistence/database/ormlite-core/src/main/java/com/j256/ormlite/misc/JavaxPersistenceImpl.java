package com.j256.ormlite.misc;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.inheritance.EntityInheritance;
import com.j256.ormlite.jpa.inheritance.InheritanceHierarchy;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.jpa.relationconfig.OneToManyConfig;
import com.j256.ormlite.jpa.relationconfig.OneToOneConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Class for isolating the detection of the javax.persistence annotations. This used to be a hard dependency but it has
 * become optinal/test since we use reflection here.
 * 
 * @author graywatson
 */
public class JavaxPersistenceImpl implements JavaxPersistenceConfigurer {

  private final static Logger log = LoggerFactory.getLogger(JavaxPersistenceImpl.class);


	public DatabaseFieldConfig createFieldConfig(DatabaseType databaseType, Field field) throws SQLException{
		Column columnAnnotation = field.getAnnotation(Column.class);
		Basic basicAnnotation = field.getAnnotation(Basic.class);
		Id idAnnotation = field.getAnnotation(Id.class);
		GeneratedValue generatedValueAnnotation = field.getAnnotation(GeneratedValue.class);
		OneToOne oneToOneAnnotation = field.getAnnotation(OneToOne.class);
    OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
		ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);
    ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);
    JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
    JoinTable joinTableAnnotation = field.getAnnotation(JoinTable.class);
		Enumerated enumeratedAnnotation = field.getAnnotation(Enumerated.class);
		Version versionAnnotation = field.getAnnotation(Version.class);
    OrderBy orderByAnnotation = field.getAnnotation(OrderBy.class);

		if (columnAnnotation == null && basicAnnotation == null && idAnnotation == null && oneToOneAnnotation == null
				&& oneToManyAnnotation == null && manyToOneAnnotation == null && manyToManyAnnotation == null &&
        enumeratedAnnotation == null && versionAnnotation == null) {
			return null;
		}

		DatabaseFieldConfig config = new DatabaseFieldConfig();
		String fieldName = field.getName();
		if (databaseType.isEntityNamesMustBeUpCase()) {
			fieldName = fieldName.toUpperCase();
		}
		config.setFieldName(fieldName);

		if (columnAnnotation != null) {
      config.setColumnName(extractColumnName(fieldName, columnAnnotation));

			if (stringNotEmpty(columnAnnotation.columnDefinition())) {
				config.setColumnDefinition(columnAnnotation.columnDefinition());
			}
			config.setWidth(columnAnnotation.length());
			config.setCanBeNull(columnAnnotation.nullable());
			config.setUnique(columnAnnotation.unique());
      config.setInsertable(columnAnnotation.insertable());
      config.setUpdatable(columnAnnotation.updatable());
		}
		if (basicAnnotation != null) {
			config.setCanBeNull(basicAnnotation.optional());
		}

		if (idAnnotation != null) {
			if (generatedValueAnnotation == null) {
				config.setId(true);
			} else {
				// generatedValue only works if it is also an id according to {@link GeneratedValue)
				config.setGeneratedId(true);
			}
		}

		if (oneToOneAnnotation != null || oneToManyAnnotation != null || manyToOneAnnotation != null || manyToManyAnnotation != null) {
			// if we have a collection then make it a foreign collection
			if (Collection.class.isAssignableFrom(field.getType())
					|| Instances.getFieldTypeCreator().foreignCollectionCanBeAssignedToField(field.getType())) {
				config.setForeignCollection(true);

				if (joinColumnAnnotation != null && stringNotEmpty(joinColumnAnnotation.name())) {
					config.setForeignCollectionColumnName(joinColumnAnnotation.name());
				}
        else
          config.setForeignCollectionColumnName(fieldName);

        Class otherSideClass = null;
				if (oneToManyAnnotation != null) {
          otherSideClass = configureOneToManyField(oneToManyAnnotation, config, field);
        }
        else if (manyToManyAnnotation != null) {
          otherSideClass = configureManyToManyField(manyToManyAnnotation, config, field);
        }

        if(orderByAnnotation != null)
          config.setOrderColumns(extractOrderColumns(orderByAnnotation, otherSideClass));
    }
      else {
				// otherwise it is a foreign field
				config.setForeign(true);
        config.setIsJoinColumn(true);

				if (joinColumnAnnotation != null) {

					if (stringNotEmpty(joinColumnAnnotation.name())) {
						config.setColumnName(joinColumnAnnotation.name());
					}
					config.setCanBeNull(joinColumnAnnotation.nullable());
					config.setUnique(joinColumnAnnotation.unique());
				}

        if (manyToOneAnnotation != null) {
          configureManyToOneField(manyToOneAnnotation, config, field);
        }
        else if(oneToOneAnnotation != null) {
          configureOneToOneField(oneToOneAnnotation, config, field);
        }
      }
		}

		if (enumeratedAnnotation != null) {
			EnumType enumType = enumeratedAnnotation.value();
			if (enumType != null && enumType == EnumType.STRING) {
				config.setDataType(DataType.ENUM_STRING);
			} else {
				config.setDataType(DataType.ENUM_INTEGER);
			}
		}

		if (versionAnnotation != null) {
			// just the presence of the version...
			config.setVersion(true);
		}

		if (config.getDataPersister() == null) {
			config.setDataPersister(DataPersisterManager.lookupForField(field));
		}

		config.setUseGetSet(DatabaseFieldConfig.findGetMethod(field, false) != null
				&& DatabaseFieldConfig.findSetMethod(field, false) != null);

		return config;
	}

  protected List<com.j256.ormlite.stmt.query.OrderBy> extractOrderColumns(OrderBy orderByAnnotation, Class otherSideClass) throws SQLException {
    List<com.j256.ormlite.stmt.query.OrderBy> orderBy = new ArrayList<>();

    for(String orderByString : orderByAnnotation.value().split(",")) {
      orderByString = orderByString.trim();
      boolean ascending = orderByString.endsWith("DESC") == false;

      String orderColumnName = getOrderColumnName(orderByString, otherSideClass);

      orderBy.add(new com.j256.ormlite.stmt.query.OrderBy(orderColumnName, ascending));
    }

    return orderBy;
  }

  protected String getOrderColumnName(String orderByString, Class otherSideClass) throws SQLException {
    String orderColumnFieldName = null;

    if(orderByString.contains("ASC"))
      orderColumnFieldName = orderByString.replace("ASC", "").trim();
    else if(orderByString.contains("DESC"))
      orderColumnFieldName = orderByString.replace("DESC", "").trim();
    else if(orderByString.length() > 0)
      orderColumnFieldName = orderByString.trim();

    if(orderColumnFieldName != null) {
      Field orderByField = findFieldByName(otherSideClass, orderColumnFieldName, true);
      if(orderByField != null)
        return extractColumnName(orderByField);
    }

    Field idField = findIdField(otherSideClass);
    if(idField != null)
      return extractColumnName(idField);

    throw new SQLException("Could not find column for OrderBy string '" + orderByString + "' mapped to class " + otherSideClass);
  }

  protected void configureOneToOneField(OneToOne oneToOneAnnotation, DatabaseFieldConfig config, Field field) throws SQLException {
    Class targetType = field.getType();
    if(oneToOneAnnotation.targetEntity() != void.class)
      targetType = oneToOneAnnotation.targetEntity();

    FetchType fetch = oneToOneAnnotation.fetch();
    CascadeType[] cascade = oneToOneAnnotation.cascade();

    String joinColumnName = targetType.getName().toLowerCase() + "_id";
    if(field.isAnnotationPresent(JoinColumn.class))
      joinColumnName = field.getAnnotation(JoinColumn.class).name();

    Field targetField = findOneToOneTargetField(field, oneToOneAnnotation.mappedBy(), targetType);

    if(targetField == null) { // unidirectional association
      config.setIsOneToOneField(true);
      config.setOneToOneConfig(new OneToOneConfig(field.getDeclaringClass(), field, targetType, joinColumnName, fetch, cascade));
    }
    else { // bidirectional @OneToOne association
      if(targetField.isAnnotationPresent(JoinColumn.class))
        joinColumnName = targetField.getAnnotation(JoinColumn.class).name();

      Field owningSide;
      Field ownedSide;

      if (oneToOneAnnotation.mappedBy().isEmpty() == false) {
        owningSide = targetField;
        ownedSide = field;
      } else if (targetField.getAnnotation(OneToOne.class).mappedBy().isEmpty() == false) {
        owningSide = field;
        ownedSide = targetField;
      } else
        throw new SQLException("On both @OneToOne sides no mappedBy value has been found, so Owner could not be determined for fields " + field.getDeclaringClass().getName() + "."
            + field.getName() + " and " + targetField.getDeclaringClass().getName() + "." + targetField.getName());

      config.setIsOneToOneField(true);
      config.setOneToOneConfig(new OneToOneConfig(owningSide.getDeclaringClass(), owningSide, ownedSide.getDeclaringClass(), ownedSide, joinColumnName, fetch, cascade));
    }

    config.setColumnName(joinColumnName);
  }

  protected Field findOneToOneTargetField(Field field, String mappedBy, Class targetType) throws SQLException {
    Class classContainingField = field.getDeclaringClass();

    for(Field targetField : getAllDeclaredFieldsInClassHierarchy(targetType)) {
      if(classContainingField.equals(targetField.getDeclaringClass())) {
        if (mappedBy.isEmpty() == false && mappedBy.equals(targetField.getName()))
          return targetField;
        else if (targetField.isAnnotationPresent(OneToOne.class)) {
          OneToOne oneToOneAnnotation = targetField.getAnnotation(OneToOne.class);
          if (oneToOneAnnotation.mappedBy().isEmpty() == false && oneToOneAnnotation.mappedBy().equals(field.getName()))
            return targetField;
        }
      }
    }

    return null; // an unidirectional OneToOne association
  }

  protected void configureManyToOneField(ManyToOne manyToOneAnnotation, DatabaseFieldConfig config, Field manySideField) throws SQLException {
    Class manySideClass = manySideField.getDeclaringClass();

    CascadeType[] cascade = manyToOneAnnotation.cascade();
    FetchType fetchType = manyToOneAnnotation.fetch();
    if (fetchType != null && fetchType == FetchType.EAGER) {
      config.setForeignCollectionEager(true); // TODO: remove
    }


    Class oneSideClass = getTargetEntityClass(manyToOneAnnotation, manySideField);
    Field oneSideField = findOneSideField(manySideField, oneSideClass);

    String joinColumnName = getManyToOneJoinColumnName(manySideField);

    config.setIsManyToOneField(true);
    config.setOneToManyConfig(new OneToManyConfig(oneSideClass, oneSideField, manySideClass, manySideField, joinColumnName, fetchType, cascade));
    config.setColumnName(joinColumnName);
  }

  protected Field findOneSideField(Field manySideField, Class oneSideClass) throws SQLException {
    for(Field oneSideField : getAllDeclaredFieldsInClassHierarchy(oneSideClass)) {
      if(oneSideField.isAnnotationPresent(OneToMany.class)) {
        OneToMany oneToManyAnnotation = oneSideField.getAnnotation(OneToMany.class);
        if(manySideField.getName().equals(oneToManyAnnotation.mappedBy())) {
          // now also check if it's the correct target entity type
          Class oneSideTargetEntity = getTargetEntityClass(oneToManyAnnotation, oneSideField);
          if(manySideField.getDeclaringClass().equals(oneSideTargetEntity))
            return oneSideField;
        }
      }
    }

//    throw new SQLException("Could not find @OneToMany field on class " + oneSideClass.getName() + " for @ManyToOne field " + manySideField.toString());
    return null; // TODO: what consequences does it have returning null?
  }

  protected Class configureOneToManyField(OneToMany oneToManyAnnotation, DatabaseFieldConfig config, Field oneSideField) throws SQLException {
    Class oneSideClass = oneSideField.getDeclaringClass();

    CascadeType[] cascade = oneToManyAnnotation.cascade();
    FetchType fetchType = oneToManyAnnotation.fetch();
    if (fetchType != null && fetchType == FetchType.EAGER) {
      config.setForeignCollectionEager(true); // TODO: remove
    }

    try {
      Class manySideClass = getTargetEntityClass(oneToManyAnnotation, oneSideField);
      config.setForeignCollectionGenericType(manySideClass); // TODO: remove

      if (oneToManyAnnotation.mappedBy() != null && oneToManyAnnotation.mappedBy().isEmpty() == false) {
        configureBidirectionalOneToManyField(oneToManyAnnotation, config, oneSideField, oneSideClass, fetchType, cascade, manySideClass);
      }
      else {
        // ok, this means relation is not bidirectional, @JoinColumn annotation has to be present
        configureUnidirectionalOneToManyField(config, oneSideField, oneSideClass, fetchType, cascade);
        // TODO: unidirectional means we have to create a Join Table, this case is not supported yet
        throw new SQLException("Sorry, but unidirectional @OneToMany associations are not supported yet by this implementation. Please add a @ManyToOne field on the many side.");
      }

      return manySideClass;
    } catch(Exception ex) {
      log.error("Could not configure OnToMany field for field " + oneSideField, ex);
      throw new SQLException(ex);
    }
  }

  protected void configureBidirectionalOneToManyField(OneToMany oneToManyAnnotation, DatabaseFieldConfig config, Field oneSideField, Class oneSideClass, FetchType fetchType, CascadeType[] cascade, Class manySideClass) throws NoSuchFieldException {
    Field manySideField = findFieldByName(manySideClass, oneToManyAnnotation.mappedBy(), true);

    String joinColumnName = getManyToOneJoinColumnName(manySideField);
    config.setIsOneToManyField(true);
    config.setOneToManyConfig(new OneToManyConfig(oneSideClass, oneSideField, manySideClass, manySideField, joinColumnName, fetchType, cascade));
  }

  protected void configureUnidirectionalOneToManyField(DatabaseFieldConfig config, Field oneSideField, Class oneSideClass, FetchType fetchType, CascadeType[] cascade) throws SQLException {
    if(oneSideField.isAnnotationPresent(JoinColumn.class) == false)
      throw new SQLException("For a @OneToMany annotation of field " + oneSideClass.getSimpleName() + "." + oneSideField.getName() + " either mappedBy has to be set (for " +
    "bidirectional association) or @JoinColumn annotation has to be specified (for unidirectional association).");
    else {
      String joinColumnName = getOneToManyJoinColumnName(oneSideField);
      config.setIsOneToManyField(true);
      config.setOneToManyConfig(new OneToManyConfig(oneSideClass, oneSideField, joinColumnName, fetchType, cascade));
    }
  }

  protected String getOneToManyJoinColumnName(Field oneSideField) throws SQLException {
    String joinColumnName = "";

    if(oneSideField.isAnnotationPresent(JoinColumn.class)) {
      JoinColumn joinColumn = oneSideField.getAnnotation(JoinColumn.class);
      if(joinColumn.name().isEmpty() == false)
        joinColumnName = joinColumn.name();
      // TODO:
//      log.warn("On a @JoinColumn annotation for a @ManyToOne field all other values name() will be ignored (are not implemented yet)");
    }
    else
      throw new SQLException("For a @OneToMany annotation of field " + oneSideField.getDeclaringClass().getSimpleName() + "." + oneSideField.getName() + " either mappedBy has to be set (for " +
          "bidirectional association) or @JoinColumn annotation has to be specified (for unidirectional association).");

    return joinColumnName;
  }

  protected String getManyToOneJoinColumnName(Field manySideField) {
    String joinColumnName = manySideField.getName().toLowerCase() + "_id";

    if(manySideField.isAnnotationPresent(JoinColumn.class)) {
      JoinColumn joinColumn = manySideField.getAnnotation(JoinColumn.class);
      if(joinColumn.name().isEmpty() == false)
        joinColumnName = joinColumn.name();
      // TODO:
//      log.warn("On a @JoinColumn annotation for a @ManyToOne field all other values name() will be ignored (are not implemented yet)");
    }

    return joinColumnName;
  }

  protected Class configureManyToManyField(ManyToMany manyToManyAnnotation, DatabaseFieldConfig config, Field field) throws SQLException {
    config.setIsManyToManyField(true);

    CascadeType[] cascade = manyToManyAnnotation.cascade();
    FetchType fetchType = manyToManyAnnotation.fetch();
    if (fetchType != null && fetchType == FetchType.EAGER) {
      config.setForeignCollectionEager(true);
    }

    try {
      Class targetEntity = getTargetEntityClass(manyToManyAnnotation, field);
      Field owningSideField;
      Field inverseSideField;

      if(manyToManyAnnotation.mappedBy() != null && manyToManyAnnotation.mappedBy().isEmpty() == false) {
        inverseSideField = field;
        owningSideField = findFieldByName(targetEntity, manyToManyAnnotation.mappedBy(), true);
      }
      else {
        owningSideField = field;
        inverseSideField = findInverseSideField(field, targetEntity);
      }

      config.setManyToManyConfig(new ManyToManyConfig(owningSideField, inverseSideField, fetchType, cascade));
      return targetEntity;
    } catch(Exception ex) { throw new SQLException(ex); }
  }

  protected Field findInverseSideField(Field field, Class targetEntity) throws SQLException{
    String fieldName = field.getName();
    Class classContainingField = field.getDeclaringClass();

    for(Field targetEntityField : getAllDeclaredFieldsInClassHierarchy(targetEntity)) {
      if(targetEntityField.isAnnotationPresent(ManyToMany.class)) {
        ManyToMany targetManyToMany = targetEntityField.getAnnotation(ManyToMany.class);
        if(classContainingField.equals(getTargetEntityClass(targetManyToMany, targetEntityField)) && fieldName.equals(targetManyToMany.mappedBy()))
          return targetEntityField;
      }
    }

    throw new SQLException("For ManyToMany field " + field + " in target class " + targetEntity + " no ManyToMany annotated field has been found that declares mappedBy=\"" + fieldName + "\"");
  }

  protected Class getTargetEntityClass(ManyToOne manyToOneAnnotation, Field field) throws SQLException{
    if(manyToOneAnnotation.targetEntity() != void.class)
      return manyToOneAnnotation.targetEntity();

    return field.getType();
  }

  protected Field findManySideField(Field field, Class oneSideClass, Class manySideClass) throws SQLException{
    String fieldName = field.getName();

    for(Field manySideEntityField : getAllDeclaredFieldsInClassHierarchy(manySideClass)) {
      if(manySideEntityField.isAnnotationPresent(ManyToOne.class)) {
        // TODO: also check if it's the correct target entity type
        ManyToOne targetManyToOne = manySideEntityField.getAnnotation(ManyToOne.class);
//        if(fieldName.equals(targetManyToOne.mappedBy()))
//          return manySideEntityField;
        if(manySideEntityField.getType().equals(oneSideClass))
          return manySideEntityField;
      }
    }

    throw new SQLException("For ManyToMany field " + field + " in target class " + manySideClass + " no ManyToMany annotated field has been found that declares mappedBy=\"" + fieldName + "\"");
  }

  protected Class getTargetEntityClass(OneToMany oneToManyAnnotation, Field field) throws SQLException{
    if(oneToManyAnnotation.targetEntity() != void.class)
      return oneToManyAnnotation.targetEntity();

    if(field.getGenericType() instanceof ParameterizedType == false)
      throw new SQLException("For a @OneToMany relation either Annotation's targetEntity value has to be set or Field's Datatype has to be a generic Collection / Set with " +
          "generic type set to target entity's type.");

    ParameterizedType genericType = (ParameterizedType)field.getGenericType();
    return  (Class<?>) genericType.getActualTypeArguments()[0];
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

  public String getEntityName(Class<?> clazz) {
    return getEntityTableName(clazz);
  }

  public static String getEntityTableName(Class<?> clazz) {
		Entity entityAnnotation = clazz.getAnnotation(Entity.class);
		Table tableAnnotation = clazz.getAnnotation(Table.class);

    if (tableAnnotation != null && stringNotEmpty(tableAnnotation.name())) {
      return tableAnnotation.name();
    }
		if (entityAnnotation != null && stringNotEmpty(entityAnnotation.name())) {
			return entityAnnotation.name();
		}

    // if the name isn't specified, it is the class name lowercased
		return clazz.getSimpleName().toLowerCase();
	}

	private static boolean stringNotEmpty(String value) {
		return (value != null && value.length() > 0);
	}


  public static InheritanceHierarchy getInheritanceHierarchyForClass(Class entity) {
    InheritanceHierarchy hierarchy = new InheritanceHierarchy();
    List<Class> currentHierarchyTypeSubclasses = new ArrayList<>();

    for (Class<?> classWalk = entity; classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(JavaxPersistenceImpl.classIsEntityOrMappedSuperclass(classWalk) == false)
        break;

//      currentHierarchyTypeSubclasses.add(classWalk);

      if(classWalk.isAnnotationPresent(Inheritance.class)) {
        Inheritance inheritanceAnnotation = classWalk.getAnnotation(Inheritance.class);
        InheritanceType inheritanceStrategy = inheritanceAnnotation.strategy();

        EntityInheritance entityInheritance = new EntityInheritance(classWalk, inheritanceStrategy, currentHierarchyTypeSubclasses);
        currentHierarchyTypeSubclasses = new ArrayList<>();

        if(classWalk.isAnnotationPresent(DiscriminatorColumn.class)) {
          DiscriminatorColumn discriminatorColumnAnnotation = classWalk.getAnnotation(DiscriminatorColumn.class);
          entityInheritance.setDiscriminatorColumn(discriminatorColumnAnnotation);
        }

        hierarchy.addEntityHierarchyAtTop(entityInheritance);
      }
      else
        currentHierarchyTypeSubclasses.add(classWalk);
    }

    return hierarchy;
  }


  public static boolean classIsEntity(Class dataClass) {
    return dataClass.isAnnotationPresent(Entity.class);
  }

  public static boolean classIsEntityOrMappedSuperclass(Class dataClass) {
    return classIsEntity(dataClass) || dataClass.isAnnotationPresent(MappedSuperclass.class);
  }

  public static boolean IsInheritanceLevelStartEntity(Class dataClass) {
    return dataClass.isAnnotationPresent(Inheritance.class);
  }

  public static String GetEntityDiscriminatorValue(Class<?> dataClass) {
    if(dataClass.isAnnotationPresent(DiscriminatorValue.class)) {
      DiscriminatorValue discriminatorValueAnnotation = dataClass.getAnnotation(DiscriminatorValue.class);
      return discriminatorValueAnnotation.value();
    }

    return dataClass.getSimpleName();
  }

  // TODO: move to other class, is a generic method, not only useful to JavaxPersistence
  public static Field findFieldByName(Class clazz, String fieldName) {
    return findFieldByName(clazz, fieldName, false);
  }

  public static Field findFieldByName(Class clazz, String fieldName, boolean alsoSearchParentClasses) {
    try {
      return clazz.getDeclaredField(fieldName);
    } catch(Exception ex) { } // class contains no direct field of that name

    if(alsoSearchParentClasses) {
      for (Class<?> classWalk = clazz.getSuperclass(); classWalk != null; classWalk = classWalk.getSuperclass()) {
        if(JavaxPersistenceImpl.classIsEntityOrMappedSuperclass(classWalk) == false) // mapped entities stop here
          break;

        try {
          return classWalk.getDeclaredField(fieldName);
        } catch(Exception ex) { } // classWalk contains no direct field of that name
      }
    }

    return null;
  }

  public static List<Field> getAllDeclaredFieldsInClassHierarchy(Class clazz) {
    List<Field> declaredFields = new ArrayList<>();

    for (Class<?> classWalk = clazz; classWalk != null; classWalk = classWalk.getSuperclass()) {
      if (JavaxPersistenceImpl.classIsEntityOrMappedSuperclass(classWalk) == false) // mapped entities stop here
        break;

      declaredFields.addAll(Arrays.asList(classWalk.getDeclaredFields()));
    }

    return declaredFields;
  }

  public static String extractColumnName(Field field) throws SQLException {
    if(field.isAnnotationPresent(Column.class) == false)
      throw new SQLException("Field " + field + " does not have a @Column annotation");

    Column column = field.getAnnotation(Column.class);

    String fieldName = field.getName();
    // TODO: how to check if column name has to be upper case?
//    if (databaseType.isEntityNamesMustBeUpCase()) {
//      fieldName = fieldName.toUpperCase();
//    }

    return extractColumnName(fieldName, column);
  }

  public static String extractColumnName(String fieldName, Column columnAnnotation) {
    if (stringNotEmpty(columnAnnotation.name())) {
      return columnAnnotation.name();
    }

    return fieldName;
  }

  public static Field findIdField(Class clazz) {
    for(Field field : getAllDeclaredFieldsInClassHierarchy(clazz)) {
      if(field.isAnnotationPresent(Id.class))
        return field;
    }

    return null;
  }
}
