package com.j256.ormlite.table;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.inheritance.InheritanceHierarchy;
import com.j256.ormlite.misc.JavaxPersistenceConfigurer;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Database table configuration information either supplied by Spring or direct Java wiring or from a
 * {@link DatabaseTable} annotation.
 * 
 * @author graywatson
 */
public class DatabaseTableConfig<T> {

	private static JavaxPersistenceConfigurer javaxPersistenceConfigurer;

	private Class<T> dataClass;
	private String tableName;
	private List<DatabaseFieldConfig> fieldConfigs;
	private PropertyConfig[] propertyConfigs;
	private Constructor<T> constructor;

  protected InheritanceHierarchy inheritanceHierarchy = null;

	static {
		try {
			// see if we have this class at runtime
			Class.forName("javax.persistence.Entity");
			// if we do then get our JavaxPersistance class
			Class<?> clazz = Class.forName("com.j256.ormlite.misc.JavaxPersistenceImpl");
			javaxPersistenceConfigurer = (JavaxPersistenceConfigurer) clazz.getConstructor().newInstance();
		} catch (Exception e) {
			// no configurer
			javaxPersistenceConfigurer = null;
		}
	}

	public DatabaseTableConfig() {
		// for spring
	}

	/**
	 * Setup a table config associated with the dataClass and field configurations. The table-name will be extracted
	 * from the dataClass.
	 */
	public DatabaseTableConfig(Class<T> dataClass, List<DatabaseFieldConfig> fieldConfigs) {
		this(dataClass, extractTableName(dataClass), fieldConfigs);
	}

	/**
	 * Setup a table config associated with the dataClass, table-name, and field configurations.
	 */
	public DatabaseTableConfig(Class<T> dataClass, String tableName, List<DatabaseFieldConfig> fieldConfigs) {
		this.dataClass = dataClass;
		this.tableName = tableName;
		this.fieldConfigs = fieldConfigs;
	}

  private DatabaseTableConfig(Class<T> dataClass, String tableName, PropertyConfig[] propertyConfigs) {
		this.dataClass = dataClass;
		this.tableName = tableName;
		this.propertyConfigs = propertyConfigs;
	}

	/**
	 * Initialize the class if this is being called with Spring.
	 */
	public void initialize() {
		if (dataClass == null) {
			throw new IllegalStateException("dataClass was never set on " + getClass().getSimpleName());
		}
		if (tableName == null) {
			tableName = extractTableName(dataClass);
		}
	}

	public Class<T> getDataClass() {
		return dataClass;
	}

	// @Required
	public void setDataClass(Class<T> dataClass) {
		this.dataClass = dataClass;
	}

	public String getTableName() {
		return tableName;
	}

	/**
	 * Set the table name. If not specified then the name is gotten from the class name.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setFieldConfigs(List<DatabaseFieldConfig> fieldConfigs) {
		this.fieldConfigs = fieldConfigs;
	}

  /**
	 * Extract the field types from the fieldConfigs if they have not already been configured.
	 */
	public void extractFieldTypes(ConnectionSource connectionSource) throws SQLException {
		if (propertyConfigs == null) {
			if (fieldConfigs == null) {
				propertyConfigs = extractFieldTypes(connectionSource, dataClass, tableName);
			} else {
				propertyConfigs = convertFieldConfigs(connectionSource, tableName, fieldConfigs);
			}
		}
	}

	/**
	 * Return the field types associated with this configuration.
	 */
	public PropertyConfig[] getFieldTypes(DatabaseType databaseType) throws SQLException {
		if (propertyConfigs == null) {
			throw new SQLException("Field types have not been extracted in table config");
		}
		return propertyConfigs;
	}

	public List<DatabaseFieldConfig> getFieldConfigs() {
		return fieldConfigs;
	}

	/**
	 * Return the constructor for this class. If not constructor has been set on the class then it will be found on the
	 * class through reflection.
	 */
	public Constructor<T> getConstructor() {
		if (constructor == null) {
			constructor = findNoArgConstructor(dataClass);
		}
		return constructor;
	}

	// @NotRequired
	public void setConstructor(Constructor<T> constructor) {
		this.constructor = constructor;
	}

  public boolean hasInheritanceHierarchy() {
    return inheritanceHierarchy != null;
  }

  public InheritanceHierarchy getInheritanceHierarchy() {
    return inheritanceHierarchy;
  }

  public void setInheritanceHierarchy(InheritanceHierarchy inheritanceHierarchy) {
    this.inheritanceHierarchy = inheritanceHierarchy;
  }

  /**
	 * Extract the DatabaseTableConfig for a particular class by looking for class and field annotations. This is used
	 * by internal classes to configure a class.
	 */
	public static <T> DatabaseTableConfig<T> fromClass(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		String tableName = extractTableName(clazz);
		if (connectionSource.getDatabaseType().isEntityNamesMustBeUpCase()) {
			tableName = tableName.toUpperCase();
		}
		return new DatabaseTableConfig<T>(clazz, tableName, extractFieldTypes(connectionSource, clazz, tableName));
	}

	/**
	 * Extract and return the table name for a class.
	 */
	public static <T> String extractTableName(Class<T> clazz) {
		DatabaseTable databaseTable = clazz.getAnnotation(DatabaseTable.class);
		String name = null;
		if (databaseTable != null && databaseTable.tableName() != null && databaseTable.tableName().length() > 0) {
			name = databaseTable.tableName();
		}
		if (name == null && javaxPersistenceConfigurer != null) {
			name = javaxPersistenceConfigurer.getEntityName(clazz);
		}
		if (name == null) {
			// if the name isn't specified, it is the class name lowercased
			name = clazz.getSimpleName().toLowerCase();
		}
		return name;
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

	private static <T> PropertyConfig[] extractFieldTypes(ConnectionSource connectionSource, Class<T> clazz, String tableName)
			throws SQLException {
		List<PropertyConfig> propertyConfigs = new ArrayList<PropertyConfig>();
    boolean isPropertyOfParentClass = false;

		for (Class<?> classWalk = clazz; classWalk != null; classWalk = classWalk.getSuperclass()) {
      if(isPropertyOfParentClass == false && classWalk.isAnnotationPresent(Entity.class) && classWalk.equals(clazz) == false)
        isPropertyOfParentClass = true;

			for (Field field : getClassColumns(classWalk)) {
        PropertyConfig propertyConfig = Instances.getFieldTypeCreator().createFieldType(connectionSource, tableName, field, clazz); // cda: to make FieldType creation configurable
        if (propertyConfig != null) {
          propertyConfig.setIsPropertyOfParentClass(isPropertyOfParentClass);
          propertyConfigs.add(propertyConfig);
        }
			}
		}

		if (propertyConfigs.isEmpty()) {
			throw new IllegalArgumentException("No fields have a " + DatabaseField.class.getSimpleName()
					+ " annotation in " + clazz);
		}

		return propertyConfigs.toArray(new PropertyConfig[propertyConfigs.size()]);
	}

  public static List<Field> getClassColumns(Class dataClass) {
    List<Field> classColumns = new ArrayList<>();

    for (Field field : dataClass.getDeclaredFields()) {
      if(field.isAnnotationPresent(Transient.class) == false) { // TODO: also check get-Method
        classColumns.add(field);
      }
    }

    return classColumns;
  }

	private PropertyConfig[] convertFieldConfigs(ConnectionSource connectionSource, String tableName,
			List<DatabaseFieldConfig> fieldConfigs) throws SQLException {
		List<PropertyConfig> propertyConfigs = new ArrayList<PropertyConfig>();
		for (DatabaseFieldConfig fieldConfig : fieldConfigs) {
			PropertyConfig propertyConfig = null;
			// walk up the classes until we find the field
			for (Class<?> classWalk = dataClass; classWalk != null; classWalk = classWalk.getSuperclass()) {
				Field field;
				try {
					field = classWalk.getDeclaredField(fieldConfig.getFieldName());
				} catch (NoSuchFieldException e) {
					// we ignore this and just loop hopefully finding it in a upper class
					continue;
				}
				if (field != null) {
					propertyConfig = Instances.getFieldTypeCreator().createFieldType(connectionSource, tableName, field, fieldConfig, dataClass);
					break;
				}
			}

			if (propertyConfig == null) {
				throw new SQLException("Could not find declared field with name '" + fieldConfig.getFieldName()
						+ "' for " + dataClass);
			}
			propertyConfigs.add(propertyConfig);
		}
		if (propertyConfigs.isEmpty()) {
			throw new SQLException("No fields were configured for class " + dataClass);
		}
		return propertyConfigs.toArray(new PropertyConfig[propertyConfigs.size()]);
	}
}
