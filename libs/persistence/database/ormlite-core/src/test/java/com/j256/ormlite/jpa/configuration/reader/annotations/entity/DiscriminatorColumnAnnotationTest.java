package com.j256.ormlite.jpa.configuration.reader.annotations.entity;

import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 10/03/15.
 */
public class DiscriminatorColumnAnnotationTest extends JpaConfigurationReaderTestBase {

  private final static String TestDiscriminatorColumnName = "DISCRIM";


  @Entity
  @Inheritance
  static class WithoutDiscriminatorColumnAnnotation {
    @Id Long id;
  }

  @Test
  public void withoutDiscriminatorColumnAnnotation_DefaultValuesAreSet() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(WithoutDiscriminatorColumnAnnotation.class);

    InheritanceEntityConfig entity = (InheritanceEntityConfig)entities[0];

    Assert.assertEquals(InheritanceEntityConfig.DefaultDiscriminatorColumnType, entity.getDiscriminatorType());
    Assert.assertNotNull(entity.getDiscriminatorPropertyConfig());
    Assert.assertEquals(InheritanceEntityConfig.DefaultDiscriminatorColumnName, entity.getDiscriminatorPropertyConfig().getColumnName());
    Assert.assertEquals(InheritanceEntityConfig.DefaultDiscriminatorColumnLength, entity.getDiscriminatorPropertyConfig().getLength());
    Assert.assertEquals(null, entity.getDiscriminatorPropertyConfig().getColumnDefinition());
  }


  @Entity
  @Inheritance
  @DiscriminatorColumn(name = TestDiscriminatorColumnName, discriminatorType = DiscriminatorType.INTEGER, columnDefinition = "int", length = 8) // length is not senseful in this example
  static class DiscriminatorColumnValuesSetToInteger {
    @Id Long id;
  }

  @Test
  public void readSingleTableInheritance() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(DiscriminatorColumnValuesSetToInteger.class );

    InheritanceEntityConfig entity = (InheritanceEntityConfig)entities[0];

    Assert.assertEquals(DiscriminatorType.INTEGER, entity.getDiscriminatorType());
    Assert.assertNotNull(entity.getDiscriminatorPropertyConfig());
    Assert.assertEquals(TestDiscriminatorColumnName, entity.getDiscriminatorPropertyConfig().getColumnName());
    Assert.assertEquals(8, entity.getDiscriminatorPropertyConfig().getLength());
    Assert.assertEquals("int", entity.getDiscriminatorPropertyConfig().getColumnDefinition());
  }


  @Entity
  @Inheritance(strategy = InheritanceType.JOINED)
  @DiscriminatorColumn(name = TestDiscriminatorColumnName, discriminatorType = DiscriminatorType.CHAR, columnDefinition = "char", length = 1) // length is not senseful in this example
  static class DiscriminatorColumnValuesSetToChar {
    @Id Long id;
  }

  @Test
  public void readJoinedInheritance() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(DiscriminatorColumnValuesSetToChar.class);

    InheritanceEntityConfig entity = (InheritanceEntityConfig)entities[0];

    Assert.assertEquals(DiscriminatorType.CHAR, entity.getDiscriminatorType());
    Assert.assertNotNull(entity.getDiscriminatorPropertyConfig());
    Assert.assertEquals(TestDiscriminatorColumnName, entity.getDiscriminatorPropertyConfig().getColumnName());
    Assert.assertEquals(1, entity.getDiscriminatorPropertyConfig().getLength());
    Assert.assertEquals("char", entity.getDiscriminatorPropertyConfig().getColumnDefinition());
  }

}
