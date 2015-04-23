package com.j256.ormlite.dao.cda.jointable;

import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.Property;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;

import java.sql.SQLException;

/**
 * Created by ganymed on 02/11/14.
 */
public class JoinTableConfig extends EntityConfig {

  protected JoinTableDao joinTableDao;

  protected PropertyConfig owningSideJoinColumn;
  protected PropertyConfig inverseSideJoinColumn;

  protected boolean isBidirectional = false;
  protected Property inverseSideProperty;

  protected ManyToManyConfig manyToManyConfig; // TODO: remove

  public JoinTableConfig(ManyToManyConfig manyToManyConfig) throws SQLException {
    super(manyToManyConfig.getJoinTableName(), manyToManyConfig.getFieldTypes(), manyToManyConfig.getIdFieldType());
    this.manyToManyConfig = manyToManyConfig;
  }

  public JoinTableConfig(String joinTableName, PropertyConfig owningSideProperty, Class inverseSideClass, String inverseSideJoinColumnName, Property inverseSideProperty) throws SQLException {
    super(owningSideProperty.getEntityConfig().getConnectionSource(), joinTableName/*, owningSideProperty*/);

    this.owningSideJoinColumn = new PropertyConfig(this, owningSideProperty.getColumnName());
    owningSideJoinColumn.setTargetEntityClass(owningSideProperty.getTargetEntityClass());
    owningSideJoinColumn.setTargetPropertyConfig(owningSideProperty);
    this.addProperty(owningSideJoinColumn);

    this.inverseSideJoinColumn = new PropertyConfig(this, inverseSideJoinColumnName);
    inverseSideJoinColumn.setTargetEntityClass(inverseSideClass);
    inverseSideJoinColumn.setTargetProperty(inverseSideProperty);
    this.addProperty(inverseSideJoinColumn);

    this.inverseSideProperty = inverseSideProperty;
    this.isBidirectional = inverseSideProperty != null;

    this.joinTableDao = new JoinTableDao(this, owningSideProperty, inverseSideClass, inverseSideProperty);
  }


  public JoinTableDao getJoinTableDao() {
    return joinTableDao;
  }

  public PropertyConfig getInverseSideJoinColumn() {
    return inverseSideJoinColumn;
  }

  public boolean isBidirectional() {
    return isBidirectional;
  }

  public String getOwningSideJoinColumnName() {
    return owningSideJoinColumn.getColumnName();
  }

  public String getInverseSideJoinColumnName() {
    return inverseSideJoinColumn.getColumnName();
  }

}
