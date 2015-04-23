package com.j256.ormlite.jpa.relationconfig;

import com.j256.ormlite.jpa.Property;
import com.j256.ormlite.jpa.PropertyConfig;

import java.lang.reflect.Field;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;

/**
 * Created by ganymed on 01/11/14.
 */
public class OneToOneConfig extends AssociationConfig {

  protected Class owningSideClass;
  protected Field owningSideField; // TODO: remove
  protected Property owningSideProperty;

  protected Class inverseSideClass;
  protected Field inverseSideField; // TODO: remove
  protected Property inverseSideProperty;

  protected String joinColumnName;


  public OneToOneConfig(Class owningSideClass, Field owningSideField, Class inverseSideClass, String joinColumnName, FetchType fetch, CascadeType[] cascade) {
    super(fetch, cascade, false);

    this.owningSideClass = owningSideClass;
    this.owningSideField = owningSideField;
    this.inverseSideClass = inverseSideClass;
    this.joinColumnName = joinColumnName;
  }

  public OneToOneConfig(Class owningSideClass, Field owningSideField, Class inverseSideClass, Field inverseSideField, String joinColumnName, FetchType fetch, CascadeType[] cascade) {
    this(owningSideClass, owningSideField, inverseSideClass, joinColumnName, fetch, cascade);

    isBidirectional = true;
    this.inverseSideField = inverseSideField;
  }

  public OneToOneConfig(Property owningSideProperty, Class inverseSideClass, String joinColumnName, FetchType fetch, CascadeType[] cascade) {
    super(fetch, cascade, false);

    this.owningSideClass = owningSideProperty.getDeclaringClass();
    this.owningSideProperty = owningSideProperty;
    this.inverseSideClass = inverseSideClass;
    this.joinColumnName = joinColumnName;
  }

  public OneToOneConfig(Property owningSideProperty, Property inverseSideProperty, String joinColumnName, FetchType fetch, CascadeType[] cascade) {
    this(owningSideProperty, inverseSideProperty.getDeclaringClass(), joinColumnName, fetch, cascade);

    this.inverseSideProperty = inverseSideProperty;
    isBidirectional = true;
  }

  public Field getOtherSideField(PropertyConfig propertyConfig) {
    if(owningSideField.equals(propertyConfig.getField()))
      return inverseSideField;
    return owningSideField;
  }


  public boolean isBidirectional() {
    return isBidirectional;
  }

  public Class getOwningSideClass() {
    return owningSideClass;
  }

  public Field getOwningSideField() {
    return owningSideField;
  }

  public Class getInverseSideClass() {
    return inverseSideClass;
  }

  public Field getInverseSideField() {
    return inverseSideField;
  }

  public String getJoinColumnName() {
    return joinColumnName;
  }

  public FetchType getFetch() {
    return fetch;
  }

  public CascadeType[] getCascade() {
    return cascade;
  }


  @Override
  public String toString() {
    if(isBidirectional == false)
      return "Unidirectional: " + owningSideClass.getSimpleName() + "." + owningSideField.getName();
    else
      return "Bidirectional: " + owningSideClass.getSimpleName() + "." + owningSideField.getName() + " <-> " + inverseSideClass.getSimpleName() + "." + inverseSideField.getName();
  }
}
