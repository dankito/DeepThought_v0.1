package com.j256.ormlite.jpa.configuration.reader.annotations.property;

import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;

/**
 * Created by ganymed on 07/03/15.
 */
public class BasicAnnotationTest extends JpaConfigurationReaderTestBase {


  @Entity
  static class EntityWithoutBasicAnnotation {
    @Id protected Long id;

    protected String lastName;
  }

  @Test
  public void basicAnnotationNotSet_FetchIsSetToEager_CanBeNullIsSetToTrue() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithoutBasicAnnotation.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithoutBasicAnnotation.class, "lastName");

    Assert.assertEquals(FetchType.EAGER, propertyConfig.getFetch());
    Assert.assertTrue(propertyConfig.canBeNull());
  }


  @Entity
  static class EntityWithBasicAnnotationSetToLazyAndOptional {
    @Id protected Long id;

    @Basic(fetch = FetchType.LAZY, optional = true)
    protected String lastName;
  }

  @Test
  public void basicAnnotationSet_FetchIsSetToLazy_CanBeNullIsSetToTrue() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithBasicAnnotationSetToLazyAndOptional.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithBasicAnnotationSetToLazyAndOptional.class, "lastName");

    Assert.assertEquals(FetchType.LAZY, propertyConfig.getFetch());
    Assert.assertTrue(propertyConfig.canBeNull());
  }


  @Entity
  static class EntityWithBasicAnnotationSetToEagerAndNonOptional {
    @Id protected Long id;

    @Basic(fetch = FetchType.EAGER, optional = false)
    protected String lastName;
  }

  @Test
  public void basicAnnotationSet_FetchIsSetToEager_CanBeNullIsSetToFalse() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithBasicAnnotationSetToEagerAndNonOptional.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithBasicAnnotationSetToEagerAndNonOptional.class, "lastName");

    Assert.assertEquals(FetchType.EAGER, propertyConfig.getFetch());
    Assert.assertFalse(propertyConfig.canBeNull());
  }

}
