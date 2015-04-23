package com.j256.ormlite.jpa.configuration.reader.annotations.entity;

import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;

/**
 * Created by ganymed on 10/03/15.
 */
public class DiscriminatorValueAnnotationTest extends JpaConfigurationReaderTestBase {


  @Entity
  @Inheritance
  @DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, length = 100)
  static class StringDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {
    @Id Long id;
  }

  @Entity
  static class StringDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass1 extends StringDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {

  }

  @Entity
  static class StringDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass2 extends StringDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {

  }

  @Test
  public void stringDiscriminatorWithoutDiscriminatorValueAnnotationSet_DiscriminatorValuesGetSetToItsDefaults() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(StringDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass.class, StringDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass1.class,
        StringDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass2.class);

    InheritanceEntityConfig baseClassConfig = (InheritanceEntityConfig)entities[0];
    Assert.assertEquals(StringDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass.class.getSimpleName().toUpperCase(), baseClassConfig.getDiscriminatorValueForEntity(baseClassConfig));

    EntityConfig subClass1Config = entities[1];
    Assert.assertEquals(StringDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass1.class.getSimpleName().toUpperCase(), baseClassConfig.getDiscriminatorValueForEntity(subClass1Config));

    EntityConfig subClass2Config = entities[2];
    Assert.assertEquals(StringDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass2.class.getSimpleName().toUpperCase(), baseClassConfig.getDiscriminatorValueForEntity(subClass2Config));
  }


  @Entity
  @Inheritance
  @DiscriminatorColumn(discriminatorType = DiscriminatorType.CHAR)
  static class CharDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {
    @Id Long id;
  }

  @Entity
  static class CharDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass1 extends CharDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {

  }

  @Entity
  static class CharDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass2 extends CharDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {

  }

  @Test
  public void charDiscriminatorWithoutDiscriminatorValueAnnotationSet_DiscriminatorValuesGetSetToItsDefaults() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(CharDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass.class, CharDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass1.class,
        CharDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass2.class);

    InheritanceEntityConfig baseClassConfig = (InheritanceEntityConfig)entities[0];
    Assert.assertEquals("A", baseClassConfig.getDiscriminatorValueForEntity(baseClassConfig));

    EntityConfig subClass1Config = entities[1];
    Assert.assertEquals("B", baseClassConfig.getDiscriminatorValueForEntity(subClass1Config));

    EntityConfig subClass2Config = entities[2];
    Assert.assertEquals("C", baseClassConfig.getDiscriminatorValueForEntity(subClass2Config));
  }


  @Entity
  @Inheritance
  @DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER)
  static class IntegerDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {
    @Id Long id;
  }

  @Entity
  static class IntegerDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass1 extends IntegerDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {

  }

  @Entity
  static class IntegerDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass2 extends IntegerDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass {

  }

  @Test
  public void integerDiscriminatorWithoutDiscriminatorValueAnnotationSet_DiscriminatorValuesGetSetToItsDefaults() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(IntegerDiscriminatorWithoutDiscriminatorValueAnnotationSetBaseClass.class, IntegerDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass1.class,
        IntegerDiscriminatorWithoutDiscriminatorValueAnnotationSetSubClass2.class);

    InheritanceEntityConfig baseClassConfig = (InheritanceEntityConfig)entities[0];
    Assert.assertEquals("1", baseClassConfig.getDiscriminatorValueForEntity(baseClassConfig));

    EntityConfig subClass1Config = entities[1];
    Assert.assertEquals("2", baseClassConfig.getDiscriminatorValueForEntity(subClass1Config));

    EntityConfig subClass2Config = entities[2];
    Assert.assertEquals("3", baseClassConfig.getDiscriminatorValueForEntity(subClass2Config));
  }


  @Entity
  @Inheritance
  @DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, length = 2)
  @DiscriminatorValue("iAmTooLong")
  static class StringDiscriminatorValueExceedingColumnLength {
    @Id Long id;
  }

  @Test(expected = SQLException.class)
  public void stringDiscriminatorValueExceedingColumnLength_ExceptionGetsThrown() throws SQLException {
    try {
      entityConfigurationReader.readConfiguration(StringDiscriminatorValueExceedingColumnLength.class);
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().contains("too long"));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception gets thrown");
  }


}
