package com.j256.ormlite.jpa.inheritance;

import com.j256.ormlite.jpa.DiscriminatorColumnConfig;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.InheritanceSubTableIdPropertyConfig;
import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 17/11/14.
 */
public abstract class InheritanceEntityConfig<T, ID> extends EntityConfig<T, ID> {

  public final static InheritanceType DefaultInheritanceType = InheritanceType.SINGLE_TABLE;
  public final static DiscriminatorType DefaultDiscriminatorColumnType = DiscriminatorType.STRING; // JPA default values
  public final static String DefaultDiscriminatorColumnName = "DTYPE";
  public final static int DefaultDiscriminatorColumnLength = 31;
  public final static String DefaultDiscriminatorColumnDefinition = null;


  protected DiscriminatorType discriminatorType = DefaultDiscriminatorColumnType;
  protected PropertyConfig discriminatorPropertyConfig = null;

  protected List<EntityConfig> subEntities = new ArrayList<>();
  protected Map<String, EntityConfig> mapDiscriminatorValuesToSubEntities = new HashMap<>();
  protected Map<EntityConfig, String> mapSubEntitiesToDiscriminatorValues = new HashMap<>();


  public InheritanceEntityConfig(Class<T> entityClass, ConnectionSource connectionSource, List<EntityConfig> subEntities, InheritanceType inheritance) throws SQLException {
    super(entityClass, connectionSource);

    this.inheritance = inheritance;
    this.inheritanceTopLevelEntityConfig = this;

    setTableName(JpaEntityConfigurationReader.getEntityTableName(entityClass));

    this.discriminatorPropertyConfig = createDiscriminatorColumn(entityClass);
    addProperty(this.discriminatorPropertyConfig);

    findAndStoreDiscriminatorValueForEntity(this);

    addInheritanceLevelSubEntities(subEntities);
  }


  // TODO: try to remove
  public abstract void addSubClassesToTable(List<Class> subclasses, ConnectionSource connectionSource) throws SQLException;



  public void addInheritanceLevelSubEntities(List<EntityConfig> subEntitiesToAdd) throws SQLException {
    for(EntityConfig subEntity : subEntitiesToAdd) {
      if(this.subEntities.contains(subEntity) == false) {
        findAndStoreDiscriminatorValueForEntity(subEntity);
        subEntity.setInheritance(this.inheritance);
        subEntity.setInheritanceTopLevelEntityConfig(this);

        this.subEntities.add(subEntity);

        if(inheritance == InheritanceType.JOINED)
//          addProperty(new InheritanceSubTableIdFieldType(subClass, classTableName, connectionSource, getIdProperty()));
          subEntity.addProperty(new InheritanceSubTableIdPropertyConfig(subEntity, this));
      }
    }
  }

  protected void findAndStoreDiscriminatorValueForEntity(EntityConfig entity) throws SQLException {
    String discriminatorValue = determineEntityDiscriminatorValue(entity);
    mapDiscriminatorValuesToSubEntities.put(discriminatorValue, entity);
    mapSubEntitiesToDiscriminatorValues.put(entity, discriminatorValue);
  }

  protected String determineEntityDiscriminatorValue(EntityConfig entity) throws SQLException {
    Class<?> entityClass = entity.getEntityClass();
    if(entityClass.isAnnotationPresent(DiscriminatorValue.class)) {
      DiscriminatorValue discriminatorValueAnnotation = entityClass.getAnnotation(DiscriminatorValue.class);
      if(discriminatorValueAnnotation.value().length() > this.discriminatorPropertyConfig.getLength())
        throw new SQLException("DiscriminatorValue " + discriminatorValueAnnotation.value() + " for Entity " + entity + " is too long for configured (default) column length of " +
                               discriminatorPropertyConfig.getLength() + " (default length = " + DefaultDiscriminatorColumnLength + ")");
      return discriminatorValueAnnotation.value();
    }

    if(discriminatorType == DiscriminatorType.INTEGER)
      return Integer.toString(mapDiscriminatorValuesToSubEntities.size() + 1);
    else if(discriminatorType == DiscriminatorType.CHAR)
      return new String(new char[] { (char)(65 + mapDiscriminatorValuesToSubEntities.size())});

    String entityName = entity.getTableName(); // default value for STRING is Entity's name
    if(entityName.length() > discriminatorPropertyConfig.getLength())
      entityName = entityName.substring(0, discriminatorPropertyConfig.getLength()); // TODO: check if this name is unique
    return entityName;
  }

  public EntityConfig getEntityForDiscriminatorValue(String discriminatorValue) {
    return mapDiscriminatorValuesToSubEntities.get(discriminatorValue);
  }

  public String getDiscriminatorValueForEntity(EntityConfig entity) {
    return mapSubEntitiesToDiscriminatorValues.get(entity);
  }

  protected PropertyConfig createDiscriminatorColumn(Class<?> tableClass) throws SQLException {
    if(tableClass.isAnnotationPresent(DiscriminatorColumn.class) == false) // if DiscriminatorColumn is not specified explicitly, return PropertyConfig with default values
      return new DiscriminatorColumnConfig(this, DefaultDiscriminatorColumnName, DefaultDiscriminatorColumnType, DefaultDiscriminatorColumnLength, DefaultDiscriminatorColumnDefinition);
    else {
      DiscriminatorColumn discriminatorColumnAnnotation = tableClass.getAnnotation(DiscriminatorColumn.class);
      this.discriminatorType = discriminatorColumnAnnotation.discriminatorType();
      return new DiscriminatorColumnConfig(this, discriminatorColumnAnnotation.name(), discriminatorColumnAnnotation.discriminatorType(), discriminatorColumnAnnotation.length(),
          discriminatorColumnAnnotation.columnDefinition().isEmpty() ? DefaultDiscriminatorColumnDefinition : discriminatorColumnAnnotation.columnDefinition());
    }
  }


  public DiscriminatorType getDiscriminatorType() {
    return discriminatorType;
  }

  public PropertyConfig getDiscriminatorPropertyConfig() {
    return discriminatorPropertyConfig;
  }
}
