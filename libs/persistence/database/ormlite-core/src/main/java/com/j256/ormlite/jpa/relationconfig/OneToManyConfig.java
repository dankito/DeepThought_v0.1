package com.j256.ormlite.jpa.relationconfig;

import com.j256.ormlite.jpa.Property;

import java.lang.reflect.Field;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;

/**
 * Created by ganymed on 01/11/14.
 */
public class OneToManyConfig extends AssociationConfig {

  protected Class oneSideClass;
  protected Field oneSideField; // TODO: remove
  protected Property oneSideProperty;

  protected Class manySideClass;
  protected Field manySideField; // TODO: remove
  protected Property manySideProperty;

  protected String joinColumnName;


  public OneToManyConfig(Class oneSideClass, Field oneSideField, String joinColumnName, FetchType fetch, CascadeType[] cascade) {
    super(fetch, cascade, false);

    this.oneSideClass = oneSideClass;
    this.oneSideField = oneSideField;
    this.joinColumnName = joinColumnName;
  }

  public OneToManyConfig(Class oneSideClass, Field oneSideField, Class manySideClass, Field manySideField, String joinColumnName, FetchType fetch, CascadeType[] cascade) {
    this(oneSideClass, oneSideField, joinColumnName, fetch, cascade);

    isBidirectional = true;
    this.manySideClass = manySideClass;
    this.manySideField = manySideField;
  }

  public OneToManyConfig(Property oneSideProperty, String joinColumnName, FetchType fetch, CascadeType[] cascade) {
    super(fetch, cascade, false);

    this.oneSideClass = oneSideProperty.getDeclaringClass();
    this.oneSideProperty = oneSideProperty;
    this.joinColumnName = joinColumnName;
  }

  public OneToManyConfig(Property oneSideProperty, Property manySideProperty, String joinColumnName, FetchType fetch, CascadeType[] cascade) {
    this(oneSideProperty, joinColumnName, fetch, cascade);

    isBidirectional = true;
    this.manySideClass = manySideProperty.getDeclaringClass();
    this.manySideProperty = manySideProperty;
  }

  public Class getOneSideClass() {
    return oneSideClass;
  }

  public Field getOneSideField() {
    if(oneSideProperty != null)
      return oneSideProperty.getField();
    return oneSideField;
  }

  public Class getManySideClass() {
    return manySideClass;
  }

  public Field getManySideField() {
    if(manySideProperty != null)
      return manySideProperty.getField();
    return manySideField;
  }

  public String getJoinColumnName() {
    return joinColumnName;
  }


  @Override
  public String toString() {
    if(isBidirectional == false)
      return "Unidirectional: " + oneSideClass.getSimpleName() + "." + getOneSideField().getName();
    else
      return "Bidirectional: " + oneSideClass.getSimpleName() + "." + getOneSideField().getName() + " <-> " + manySideClass.getSimpleName() + "." + getManySideField().getName();
  }
}
