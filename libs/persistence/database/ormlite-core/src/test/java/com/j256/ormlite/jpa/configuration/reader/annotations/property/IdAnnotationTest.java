package com.j256.ormlite.jpa.configuration.reader.annotations.property;

import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

/**
 * Created by ganymed on 07/03/15.
 */
public class IdAnnotationTest extends JpaConfigurationReaderTestBase {


  @Entity
  static class EntityWithoutIdAnnotation {
    protected Long id;
  }

  @Test(expected = SQLException.class)
  public void idAnnotationNotSet_SQLExceptionIdIsMissingIsThrown() throws SQLException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithoutIdAnnotation.class });
  }


  @Entity
  static class EntityWithIdAnnotation {
    @Id protected Long id;
  }

  @Test
  public void idAnnotationSet_IsIdIsSetToTrue() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithIdAnnotation.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithIdAnnotation.class, "id");

    Assert.assertTrue(propertyConfig.isId());
    Assert.assertFalse(propertyConfig.isGeneratedId());
  }


  @Entity
  static class EntityWithGeneratedValueAnnotation {
    @Id
    @GeneratedValue
    protected Long id;
  }

  @Test
  public void generatedValueAnnotationNotSet_IsGeneratedIdIsSetToTrue() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithGeneratedValueAnnotation.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithGeneratedValueAnnotation.class, "id");

    Assert.assertTrue(propertyConfig.isId());
    Assert.assertTrue(propertyConfig.isGeneratedId());
  }


  @Entity
  static class EntityWithGeneratedValueAnnotationIsSetButNotIdAnnotation {
    @GeneratedValue
    protected Long id;
  }

  @Test(expected = SQLException.class)
  public void generatedValueAnnotationNotSetButNotIdAnnotation_SQLExceptionIdIsMissingIsThrown() throws SQLException {
    entityConfigurationReader.readConfiguration(new Class[]{EntityWithGeneratedValueAnnotationIsSetButNotIdAnnotation.class});
  }


  @Entity
  static class EntityWithSequenceGeneratorAnnotationSet {
    @Id @SequenceGenerator(name = "test")
    protected Long id;
  }

  @Test(expected = SQLException.class)
  public void sequenceGeneratorAnnotationSet_SQLExceptionNotSupportedThrown() throws SQLException {
    try {
      entityConfigurationReader.readConfiguration(new Class[]{EntityWithSequenceGeneratorAnnotationSet.class});
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception gets thrown");
  }


  @Entity
  static class EntityWithTableGeneratorAnnotationSet {
    @Id @TableGenerator(name = "test")
    protected Long id;
  }

  @Test(expected = SQLException.class)
  public void tableGeneratorAnnotationSet_SQLExceptionNotSupportedThrown() throws SQLException {
    try {
      entityConfigurationReader.readConfiguration(new Class[] { EntityWithTableGeneratorAnnotationSet.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception gets thrown");
  }

}
