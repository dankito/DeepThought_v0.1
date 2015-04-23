package com.j256.ormlite.jpa.configuration.reader.model;

import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.StringHelper;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;
import com.j256.ormlite.jpa.testmodel.Category;
import com.j256.ormlite.jpa.testmodel.Reference;
import com.j256.ormlite.jpa.testmodel.ReferenceSubDivision;
import com.j256.ormlite.jpa.testmodel.SeriesTitle;
import com.j256.ormlite.jpa.testmodel.User;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ganymed on 10/03/15.
 */
public class ReadDataModelConfigurationIntegrationTest extends JpaConfigurationReaderTestBase {

  protected static Class[] testModelClasses = new Class[] { User.class, Category.class, SeriesTitle.class, Reference.class, ReferenceSubDivision.class};

  @BeforeClass
  public static void suiteSetup() {

  }


  @Test
  public void readDataModel_CorrectNumberOfEntitiesGetRead() throws SQLException {
    EntityConfig[] readEntities = entityConfigurationReader.readConfiguration(testModelClasses);
    Assert.assertEquals(testModelClasses.length, readEntities.length);
  }

  @Test
  public void readDataModel_CorrectNumberOfPropertiesGetRead() throws SQLException {
    EntityConfig[] readEntities = entityConfigurationReader.readConfiguration(testModelClasses);

    Assert.assertEquals(1 + 5, readEntities[0].getPropertyConfigs().length); // User
    Assert.assertEquals(5 + 4 + 5, readEntities[1].getPropertyConfigs().length); // Category
    Assert.assertEquals(1 + 2, readEntities[2].getPropertyConfigs().length); // SeriesTitle
    Assert.assertEquals(1 + 4 + 4 + 5, readEntities[2].getParentEntityConfig().getPropertyConfigs().length); // ReferenceBase: +1 for DiscriminatorColumn
    Assert.assertEquals(1 + 4 , readEntities[3].getPropertyConfigs().length); // Reference
    Assert.assertEquals(1 + 4 + 4 + 5, readEntities[2].getParentEntityConfig().getPropertyConfigs().length); // ReferenceBase: +1 for DiscriminatorColumn
    Assert.assertEquals(1 + 4, readEntities[4].getPropertyConfigs().length); // ReferenceSubDivision
    Assert.assertEquals(1 + 4 + 4 + 5, readEntities[2].getParentEntityConfig().getPropertyConfigs().length); // ReferenceBase: +1 for DiscriminatorColumn
  }

  @Test
  public void readDataModel_PropertiesGetReadCorrectly() throws SQLException {
    EntityConfig[] readEntities = entityConfigurationReader.readConfiguration(testModelClasses);

    for(EntityConfig entity : readEntities) {
      Assert.assertNotNull("id field of entity " + entity + " may not be null", entity.getIdProperty());

      List<PropertyConfig> configs = entity.getProperties();
      for(PropertyConfig property : configs) {
        Assert.assertTrue(StringHelper.stringNotNullOrEmpty(property.getColumnName()));
        Assert.assertNotNull(property.getEntityConfig());
        Assert.assertNotNull("dataPersister for property " + property + " may not be null", property.getDataPersister());
        Assert.assertNotNull("fieldConverter for property " + property + " may not be null", property.getFieldConverter());
        Assert.assertTrue("dataType except for Relationship properties may not be null as for property " + property,
            property.getDataType() != null || property.isRelationshipProperty() == true);
      }
    }
  }
}
