package com.j256.ormlite.jpa.inheritance;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 16/11/14.
 */
public class EntityInheritance {

  protected Class entity = null;

  protected InheritanceType inheritanceType = null;

  protected List<Class> inheritanceLevelSubclasses = new ArrayList<>();

  protected DiscriminatorColumn discriminatorColumn = null;

  protected EntityInheritance previousEntityInheritance = null;
  protected EntityInheritance nextEntityInheritance = null;


  public EntityInheritance(Class entity, InheritanceType inheritanceType, List<Class> inheritanceLevelSubclasses) {
    this.entity = entity;
    this.inheritanceType = inheritanceType;
    this.inheritanceLevelSubclasses = inheritanceLevelSubclasses;
  }


//  public TableInfo getTableInfo() {
//    if(TableInfoRegistry.getInstance().hasTableInfoForClass(getEntity()) == false) {
//      TableInfoRegistry.getInstance().registerTableInfo(getEntity(), new TableInfo());
//    }
//
//    return TableInfoRegistry.getInstance().getTableInfoForClass(getEntity());
//  }

  public Class getEntity() {
    return entity;
  }

  public InheritanceType getInheritanceType() {
    return inheritanceType;
  }

  public List<Class> getInheritanceLevelSubclasses() {
    return inheritanceLevelSubclasses;
  }

  public boolean hasDiscriminatorColumn() {
    return discriminatorColumn != null;
  }

  public DiscriminatorColumn getDiscriminatorColumn() {
    return discriminatorColumn;
  }

  public void setDiscriminatorColumn(DiscriminatorColumn discriminatorColumn) {
    this.discriminatorColumn = discriminatorColumn;
  }

  public EntityInheritance getPreviousEntityInheritance() {
    return previousEntityInheritance;
  }

  public void setPreviousEntityInheritance(EntityInheritance previousEntityInheritance) {
    this.previousEntityInheritance = previousEntityInheritance;
  }

  public EntityInheritance getNextEntityInheritance() {
    return nextEntityInheritance;
  }

  public void setNextEntityInheritance(EntityInheritance nextEntityInheritance) {
    this.nextEntityInheritance = nextEntityInheritance;
  }


  @Override
  public String toString() {
    return entity.getSimpleName() + ": " + inheritanceType + " inheritance";
  }

}
