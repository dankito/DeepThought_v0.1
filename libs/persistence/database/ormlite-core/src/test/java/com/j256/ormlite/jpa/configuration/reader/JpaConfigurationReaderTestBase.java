package com.j256.ormlite.jpa.configuration.reader;

import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.Registry;
import com.j256.ormlite.jpa.configuration.reader.mock.MockConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Created by ganymed on 07/03/15.
 */
public class JpaConfigurationReaderTestBase {

  protected JpaEntityConfigurationReader entityConfigurationReader = null;

  protected ConnectionSource connectionSource = null;


  @Before
  public void setup() {
    Registry.getEntityRegistry().clear();
    Registry.getPropertyRegistry().clear();

    this.connectionSource = new MockConnectionSource();
    entityConfigurationReader = new JpaEntityConfigurationReader(this.connectionSource);
  }

  @After
  public void tearDown() {

  }


  protected PropertyConfig getPropertyConfigurationForField(Class<?> entityClass, String fieldName) throws NoSuchFieldException {
    return Registry.getPropertyRegistry().getPropertyConfiguration(entityClass.getDeclaredField(fieldName));
  }


  protected void testUnidirectionalOneToOneRelationshipProperties(PropertyConfig propertyConfig) {
    testOneToOneRelationshipProperties(propertyConfig);

    Assert.assertFalse(propertyConfig.isBidirectional());
    Assert.assertFalse(propertyConfig.getOneToOneConfig().isBidirectional());
    Assert.assertTrue(propertyConfig.isOwningSide());
    Assert.assertNull(propertyConfig.getTargetProperty());
  }

  protected void testBidirectionalOneToOneRelationshipProperties(PropertyConfig propertyConfig) {
    testOneToOneRelationshipProperties(propertyConfig);

    Assert.assertTrue(propertyConfig.isBidirectional());
    Assert.assertTrue(propertyConfig.getOneToOneConfig().isBidirectional());
    Assert.assertNotNull(propertyConfig.getTargetProperty());
  }

  protected void testOneToOneRelationshipProperties(PropertyConfig propertyConfig) {
    Assert.assertTrue(propertyConfig.isRelationshipProperty());
    Assert.assertTrue(propertyConfig.isOneCardinalityRelationshipProperty());
    Assert.assertFalse(propertyConfig.isManyCardinalityRelationshipProperty());
    Assert.assertTrue(propertyConfig.isOneToOneField());
    Assert.assertNotNull(propertyConfig.getOneToOneConfig());
    Assert.assertNotNull(propertyConfig.getTargetEntityClass());
  }


  protected void testUnidirectionalManyToOneRelationshipProperties(PropertyConfig propertyConfig) {
    testManyToOneRelationshipProperties(propertyConfig);

    Assert.assertFalse(propertyConfig.isBidirectional());
    Assert.assertFalse(propertyConfig.getOneToManyConfig().isBidirectional());
    Assert.assertTrue(propertyConfig.isOwningSide());
    Assert.assertNull(propertyConfig.getTargetProperty());
  }

  protected void testBidirectionalManyToOneRelationshipProperties(PropertyConfig propertyConfig) {
    testManyToOneRelationshipProperties(propertyConfig);

    Assert.assertTrue(propertyConfig.isBidirectional());
    Assert.assertTrue(propertyConfig.getOneToManyConfig().isBidirectional());
    Assert.assertNotNull(propertyConfig.getTargetProperty());
  }

  protected void testManyToOneRelationshipProperties(PropertyConfig propertyConfig) {
    Assert.assertTrue(propertyConfig.isRelationshipProperty());
    Assert.assertTrue(propertyConfig.isManyCardinalityRelationshipProperty());
    Assert.assertFalse(propertyConfig.isOneCardinalityRelationshipProperty());
    Assert.assertTrue(propertyConfig.isManyToOneField());
    Assert.assertNotNull(propertyConfig.getOneToManyConfig());
    Assert.assertNotNull(propertyConfig.getTargetEntityClass());
  }


  protected void testUnidirectionalOneToManyRelationshipProperties(PropertyConfig propertyConfig) {
    testOneToManyRelationshipProperties(propertyConfig);

    Assert.assertFalse(propertyConfig.isBidirectional());
    Assert.assertFalse(propertyConfig.getOneToManyConfig().isBidirectional());
    Assert.assertTrue(propertyConfig.isOwningSide());
    Assert.assertNull(propertyConfig.getTargetProperty());
  }

  protected void testBidirectionalOneToManyRelationshipProperties(PropertyConfig propertyConfig) {
    testOneToManyRelationshipProperties(propertyConfig);

    Assert.assertTrue(propertyConfig.isBidirectional());
    Assert.assertTrue(propertyConfig.getOneToManyConfig().isBidirectional());
    Assert.assertNotNull(propertyConfig.getTargetProperty());
  }

  protected void testOneToManyRelationshipProperties(PropertyConfig propertyConfig) {
    Assert.assertTrue(propertyConfig.isRelationshipProperty());
    Assert.assertTrue(propertyConfig.isOneCardinalityRelationshipProperty());
    Assert.assertFalse(propertyConfig.isManyCardinalityRelationshipProperty());
    Assert.assertTrue(propertyConfig.isOneToManyField());
    Assert.assertNotNull(propertyConfig.getOneToManyConfig());
    Assert.assertNotNull(propertyConfig.getTargetEntityClass());
  }


  protected void testUnidirectionalManyToManyRelationshipProperties(PropertyConfig propertyConfig) {
    testManyToManyRelationshipProperties(propertyConfig);

    Assert.assertFalse(propertyConfig.isBidirectional());
    Assert.assertFalse(propertyConfig.getManyToManyConfig().isBidirectional());
    Assert.assertNull(propertyConfig.getTargetProperty());

    testManyToManyRelationshipOwningSideProperties(propertyConfig);
  }

  protected void testBidirectionalManyToManyRelationshipOwningSideProperties(PropertyConfig propertyConfig) {
    testBidirectionalManyToManyRelationshipProperties(propertyConfig);
    testManyToManyRelationshipOwningSideProperties(propertyConfig);
  }

  protected void testBidirectionalManyToManyRelationshipProperties(PropertyConfig propertyConfig) {
    testManyToManyRelationshipProperties(propertyConfig);

    Assert.assertTrue(propertyConfig.isBidirectional());
    Assert.assertTrue(propertyConfig.getManyToManyConfig().isBidirectional());
    Assert.assertNotNull(propertyConfig.getTargetProperty());
  }

  protected void testManyToManyRelationshipOwningSideProperties(PropertyConfig propertyConfig) {
    Assert.assertTrue(propertyConfig.isOwningSide());
    Assert.assertFalse(propertyConfig.isInverseSide());

    Assert.assertNotNull(propertyConfig.getJoinTable());
    Assert.assertNotNull(propertyConfig.getJoinTable().getTableName());
    Assert.assertNotNull(propertyConfig.getJoinTable().getInverseSideJoinColumn());
    Assert.assertNotNull(propertyConfig.getJoinTable().getInverseSideJoinColumn().getDataPersister());
    Assert.assertNotNull(propertyConfig.getJoinTable().getInverseSideJoinColumn().getFieldConverter());
  }

  protected void testManyToManyRelationshipProperties(PropertyConfig propertyConfig) {
    Assert.assertTrue(propertyConfig.isRelationshipProperty());
    Assert.assertTrue(propertyConfig.isManyCardinalityRelationshipProperty());
    Assert.assertFalse(propertyConfig.isOneCardinalityRelationshipProperty());
    Assert.assertTrue(propertyConfig.isManyToManyField());
    Assert.assertNotNull(propertyConfig.getManyToManyConfig());
    Assert.assertNotNull(propertyConfig.getTargetEntityClass());
  }

}
