package com.j256.ormlite.jpa.configuration.reader.annotations.entity;

import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.InheritanceSubTableIdPropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 10/03/15.
 */
public class InheritanceAnnotationTest extends JpaConfigurationReaderTestBase {


  @Entity
  @Inheritance
  static class InheritanceBaseClassWithDefaultSettings {
    @Id Long id;
  }

  @Entity
  static class InheritanceSubClass1WithDefaultSettings extends InheritanceBaseClassWithDefaultSettings {
    String firstName;
  }

  @Entity
  static class InheritanceSubClass2WithDefaultSettings extends InheritanceBaseClassWithDefaultSettings {
    String lastName;
  }

  @Test
  public void readInheritanceWithDefaultSettings() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(InheritanceBaseClassWithDefaultSettings.class, InheritanceSubClass1WithDefaultSettings.class,
        InheritanceSubClass2WithDefaultSettings.class);

    EntityConfig baseClassConfig = entities[0];
    Assert.assertNull(baseClassConfig.getParentEntityConfig());
    Assert.assertNotNull(baseClassConfig.getTableName());
    Assert.assertEquals(2, baseClassConfig.getPropertyConfigs().length); // id + DiscriminatorColumn
    Assert.assertTrue(baseClassConfig instanceof InheritanceEntityConfig);
    Assert.assertEquals(baseClassConfig, baseClassConfig.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(2, baseClassConfig.getChildEntityConfigs().size());

    EntityConfig subClass1Config = entities[1];
    Assert.assertEquals(baseClassConfig, subClass1Config.getParentEntityConfig());
    Assert.assertNotNull(subClass1Config.getTableName());
    Assert.assertEquals(1, subClass1Config.getPropertyConfigs().length);
    Assert.assertEquals(baseClassConfig.getIdProperty(), subClass1Config.getIdProperty());
    Assert.assertEquals(baseClassConfig, subClass1Config.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(0, subClass1Config.getChildEntityConfigs().size());

    EntityConfig subClass2Config = entities[2];
    Assert.assertEquals(baseClassConfig, subClass2Config.getParentEntityConfig());
    Assert.assertNotNull(subClass2Config.getTableName());
    Assert.assertEquals(1, subClass2Config.getPropertyConfigs().length);
    Assert.assertEquals(baseClassConfig.getIdProperty(), subClass2Config.getIdProperty());
    Assert.assertEquals(baseClassConfig, subClass2Config.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(0, subClass2Config.getChildEntityConfigs().size());
  }


  @Entity
  @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
  static class SingleTableInheritanceBase {
    @Id Long id;
  }

  @Entity
  static class SingleTableInheritanceSubClass1 extends SingleTableInheritanceBase {
    String firstName;
  }

  @Entity
  static class SingleTableInheritanceSubClass2 extends SingleTableInheritanceBase {
    String lastName;
  }

  @Test
  public void readSingleTableInheritance() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(SingleTableInheritanceBase.class, SingleTableInheritanceSubClass1.class, SingleTableInheritanceSubClass2.class);

    EntityConfig baseClassConfig = entities[0];
    Assert.assertNull(baseClassConfig.getParentEntityConfig());
    Assert.assertNotNull(baseClassConfig.getTableName());
    Assert.assertEquals(2, baseClassConfig.getPropertyConfigs().length); // id + DiscriminatorColumn
    Assert.assertTrue(baseClassConfig instanceof InheritanceEntityConfig);
    Assert.assertEquals(baseClassConfig, baseClassConfig.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(2, baseClassConfig.getChildEntityConfigs().size());

    EntityConfig subClass1Config = entities[1];
    Assert.assertEquals(baseClassConfig, subClass1Config.getParentEntityConfig());
    Assert.assertNotNull(subClass1Config.getTableName());
    Assert.assertEquals(1, subClass1Config.getPropertyConfigs().length);
    Assert.assertEquals(baseClassConfig.getIdProperty(), subClass1Config.getIdProperty());
    Assert.assertEquals(baseClassConfig, subClass1Config.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(0, subClass1Config.getChildEntityConfigs().size());

    EntityConfig subClass2Config = entities[2];
    Assert.assertEquals(baseClassConfig, subClass2Config.getParentEntityConfig());
    Assert.assertNotNull(subClass2Config.getTableName());
    Assert.assertEquals(1, subClass2Config.getPropertyConfigs().length);
    Assert.assertEquals(baseClassConfig.getIdProperty(), subClass2Config.getIdProperty());
    Assert.assertEquals(baseClassConfig, subClass2Config.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(0, subClass2Config.getChildEntityConfigs().size());
  }


  @Entity
  @Inheritance(strategy = InheritanceType.JOINED)
  static class JoinedInheritanceBase {
    @Id Long id;
  }

  @Entity
  static class JoinedInheritanceSubClass1 extends JoinedInheritanceBase {
    String firstName;
  }

  @Entity
  static class JoinedInheritanceSubClass2 extends JoinedInheritanceBase {
    String lastName;
  }

  @Test
  public void readJoinedInheritance() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfiguration(JoinedInheritanceBase.class, JoinedInheritanceSubClass1.class, JoinedInheritanceSubClass2.class);

    EntityConfig baseClassConfig = entities[0];
    Assert.assertNull(baseClassConfig.getParentEntityConfig());
    Assert.assertEquals(2, baseClassConfig.getPropertyConfigs().length); // id + DiscriminatorColumn
    Assert.assertTrue(baseClassConfig instanceof InheritanceEntityConfig);
    Assert.assertEquals(baseClassConfig, baseClassConfig.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(2, baseClassConfig.getChildEntityConfigs().size());

    EntityConfig subClass1Config = entities[1];
    Assert.assertEquals(baseClassConfig, subClass1Config.getParentEntityConfig());
    Assert.assertEquals(2, subClass1Config.getPropertyConfigs().length); // 1 + 1 for inherited Id column
    Assert.assertTrue(subClass1Config.getIdProperty() instanceof InheritanceSubTableIdPropertyConfig);
    Assert.assertEquals(baseClassConfig.getIdProperty(), ((InheritanceSubTableIdPropertyConfig)subClass1Config.getIdProperty()).getInheritanceHierarchyTopLevelIdProperty());
    Assert.assertEquals(baseClassConfig, subClass1Config.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(0, subClass1Config.getChildEntityConfigs().size());

    EntityConfig subClass2Config = entities[2];
    Assert.assertEquals(baseClassConfig, subClass2Config.getParentEntityConfig());
    Assert.assertEquals(2, subClass2Config.getPropertyConfigs().length); // 1 + 1 for inherited Id column
    Assert.assertTrue(subClass2Config.getIdProperty() instanceof InheritanceSubTableIdPropertyConfig);
    Assert.assertEquals(baseClassConfig.getIdProperty(), ((InheritanceSubTableIdPropertyConfig)subClass2Config.getIdProperty()).getInheritanceHierarchyTopLevelIdProperty());
    Assert.assertEquals(baseClassConfig, subClass2Config.getInheritanceTopLevelEntityConfig());
    Assert.assertEquals(0, subClass2Config.getChildEntityConfigs().size());
  }

}
