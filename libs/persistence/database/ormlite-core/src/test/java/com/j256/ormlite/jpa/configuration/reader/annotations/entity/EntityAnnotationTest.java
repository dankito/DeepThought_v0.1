package com.j256.ormlite.jpa.configuration.reader.annotations.entity;

import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by ganymed on 07/03/15.
 */
public class EntityAnnotationTest extends JpaConfigurationReaderTestBase {

  private final static String TestEntityName = "test_entity";
  private final static String TestTableName = "test_table";


  static class NoEntityAnnotation { @Id protected Long id; }

  @Test(expected = SQLException.class)
  public void noEntityAnnotation_ThrowsAnSQLException() throws SQLException {
    try {
      entityConfigurationReader.readConfiguration(new Class[] { NoEntityAnnotation.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().toLowerCase().contains("not an entity"));
      throw ex;
    }

    Assert.fail("Should never come here as Exception must be thrown");
  }


  @Entity
  static class EntityWithoutNameAttributeSet { @Id protected Long id; }

  @Test
  public void nameNotSet_PerDefaultTableNameEqualsEntityName() throws SQLException {
    EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { EntityWithoutNameAttributeSet.class });

    Assert.assertEquals(EntityWithoutNameAttributeSet.class.getSimpleName().toLowerCase().toUpperCase(), configs[0].getTableName());
    Assert.assertEquals(EntityWithoutNameAttributeSet.class.getSimpleName().toLowerCase().toUpperCase(), configs[0].getEntityName());
  }


  @Entity(name = TestEntityName)
  static class EntityWithNameAttributeSet { @Id protected Long id; }

  @Test
  public void nameIsSet_TableNameEqualsAnnotationNameAttribute() throws SQLException {
    EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { EntityWithNameAttributeSet.class });

    Assert.assertEquals(TestEntityName.toUpperCase(), configs[0].getTableName());
    Assert.assertEquals(TestEntityName.toUpperCase(), configs[0].getEntityName());
  }


  @Entity(name = TestEntityName)
  @Table(name = TestTableName)
  static class EntityWithTableAnnotationSetAsWell { @Id protected Long id; }

  @Test
  public void tableAnnotationIsSetAsWell_TableAndEntityNameDiffer() throws SQLException {
    EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { EntityWithTableAnnotationSetAsWell.class });

    Assert.assertEquals(TestTableName.toUpperCase(), configs[0].getTableName());
    Assert.assertEquals(TestEntityName.toUpperCase(), configs[0].getEntityName());
  }


  @Entity
  static class EntityWithoutId {  }

  @Test(expected = SQLException.class)
  public void idNotSet_ThrowsAnSQLException() throws SQLException {
    try {
     entityConfigurationReader.readConfiguration(new Class[] { EntityWithoutId.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().toLowerCase().contains("id not set"));
      throw ex;
    }

    Assert.fail("Should never come here as Exception must be thrown");
  }


  @Entity
  static class EntityReferencingUnknownEntity {
    @Id protected Long id;
    @OneToOne UnknownEntity unknownEntity;
  }

  static class UnknownEntity { }

  @Test(expected = SQLException.class)
  public void entityReferencesUnknownEntity_ThrowsAnSQLException() throws SQLException {
    try {
      entityConfigurationReader.readConfiguration(new Class[] { EntityReferencingUnknownEntity.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().toLowerCase().contains("unknown entity"));
      throw ex;
    }

    Assert.fail("Should never come here as Exception must be thrown");
  }

}
