package com.j256.ormlite.jpa.configuration.reader.annotations.property.relationships;

import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.StringHelper;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by ganymed on 07/03/15.
 */
public class OneToOneAnnotationTest extends JpaConfigurationReaderTestBase {

  private final static String TestJoinColumnName = "inverse_side";
  private final static String TestJoinColumnDefinition = "smallint";


  @Entity
  static class OwningSideUnAnnotated {
    @Id protected Long id;

    protected OwningSideUnAnnotated inverseSide;
  }

  @Test(expected = SQLException.class)
  public void oneToOneAnnotationNotSet_ExceptionIsThrownTypeCannotBeSerialized() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OwningSideUnAnnotated.class });

    Assert.fail("Should never come to this as Exception should be thrown");
  }


  @Entity
  static class OwningSideUniDirectional {
    @Id protected Long id;

    @OneToOne
    protected InverseSideUniDirectional inverseSide;
  }

  @Entity
  static class InverseSideUniDirectional {
    @Id protected Long id;
  }

  @Test
  public void unidirectionalOneToOne_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ OwningSideUniDirectional.class, InverseSideUniDirectional.class });
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(OwningSideUniDirectional.class, "inverseSide");

    testUnidirectionalOneToOneRelationshipProperties(inverseSidePropertyConfig);
  }

  @Test
  public void unidirectionalOneToOneJoinColumnNotSet_JoinColumnSettingsAreSetToDefault() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OwningSideUniDirectional.class, InverseSideUniDirectional.class });
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(OwningSideUniDirectional.class, "inverseSide");

    Assert.assertTrue(inverseSidePropertyConfig.isJoinColumn());
    Assert.assertEquals((InverseSideUniDirectional.class.getSimpleName() + "_" + "id").toUpperCase(), inverseSidePropertyConfig.getColumnName());
    Assert.assertTrue(inverseSidePropertyConfig.canBeNull());
    Assert.assertFalse(inverseSidePropertyConfig.isUnique());
    Assert.assertTrue(inverseSidePropertyConfig.isInsertable());
    Assert.assertTrue(inverseSidePropertyConfig.isUpdatable());
    Assert.assertFalse(StringHelper.stringNotNullOrEmpty(inverseSidePropertyConfig.getColumnDefinition()));
  }

  @Test
  public void unidirectionalOneToOne_OneToOneDefaultAttributeValuesGetApplied() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OwningSideUniDirectional.class, InverseSideUniDirectional.class });
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(OwningSideUniDirectional.class, "inverseSide");

    Assert.assertEquals(InverseSideUniDirectional.class, inverseSidePropertyConfig.getTargetEntityClass());
    Assert.assertEquals(0, inverseSidePropertyConfig.getCascade().length);
    Assert.assertEquals(FetchType.EAGER, inverseSidePropertyConfig.getFetch());
    Assert.assertTrue(inverseSidePropertyConfig.canBeNull());

    Assert.assertFalse(inverseSidePropertyConfig.cascadePersist());
    Assert.assertFalse(inverseSidePropertyConfig.cascadeRefresh());
    Assert.assertFalse(inverseSidePropertyConfig.cascadeRemove());
  }


  @Entity
  static class OwningSideUniDirectionalJoinColumnSet {
    @Id protected Long id;

    @OneToOne
    @JoinColumn(name = TestJoinColumnName, nullable = false, unique = true, insertable = false, updatable = false, columnDefinition = TestJoinColumnDefinition)
    protected InverseSideUniDirectional inverseSide;
  }

  @Test
  public void unidirectionalOneToOneJoinColumnSet_JoinColumnSettingsAreApplied() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OwningSideUniDirectionalJoinColumnSet.class, InverseSideUniDirectional.class });
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(OwningSideUniDirectionalJoinColumnSet.class, "inverseSide");

    Assert.assertTrue(inverseSidePropertyConfig.isJoinColumn());
    Assert.assertEquals(TestJoinColumnName.toUpperCase(), inverseSidePropertyConfig.getColumnName());
    Assert.assertFalse(inverseSidePropertyConfig.canBeNull());
    Assert.assertTrue(inverseSidePropertyConfig.isUnique());
    Assert.assertFalse(inverseSidePropertyConfig.isInsertable());
    Assert.assertFalse(inverseSidePropertyConfig.isUpdatable());
    Assert.assertEquals(TestJoinColumnDefinition, inverseSidePropertyConfig.getColumnDefinition());
  }


  @Entity
  static class OwningSideUniDirectionalJoinColumnReferencedColumnNameSet {
    @Id protected Long id;

    @OneToOne
    @JoinColumn(referencedColumnName = "I will throw a not supported exception")
    protected InverseSideUniDirectional inverseSide;
  }

  @Test(expected = SQLException.class)
  public void unidirectionalOneToOneJoinColumnReferencedColumnNameSet_ThrowsNotSupportedException() throws SQLException, NoSuchFieldException {
    try {
      entityConfigurationReader.readConfiguration(new Class[]{ OwningSideUniDirectionalJoinColumnReferencedColumnNameSet.class, InverseSideUniDirectional.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception must be thrown");
  }


  @Entity
  static class OwningSideUniDirectionalJoinColumnTableSet {
    @Id protected Long id;

    @OneToOne
    @JoinColumn(table = "I will throw a not supported exception")
    protected InverseSideUniDirectional inverseSide;
  }

  @Test(expected = SQLException.class)
  public void unidirectionalOneToOneJoinColumnTableSet_ThrowsNotSupportedException() throws SQLException, NoSuchFieldException {
    try {
      entityConfigurationReader.readConfiguration(new Class[] { OwningSideUniDirectionalJoinColumnTableSet.class, InverseSideUniDirectional.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception must be thrown");
  }


  @Entity
  static class TwoUniDirectionalRelationshipsClassOne {
    @Id protected Long id;

    @OneToOne
    protected TwoUniDirectionalRelationshipsClassTwo two;
  }

  @Entity
  static class TwoUniDirectionalRelationshipsClassTwo {
    @Id protected Long id;

    @OneToOne
    protected TwoUniDirectionalRelationshipsClassOne one;
  }

  @Test
  public void twoUnidirectionalOneToOneClasses_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { TwoUniDirectionalRelationshipsClassOne.class, TwoUniDirectionalRelationshipsClassTwo.class });
    PropertyConfig twoPropertyConfig = getPropertyConfigurationForField(TwoUniDirectionalRelationshipsClassOne.class, "two");
    PropertyConfig onePropertyConfig = getPropertyConfigurationForField(TwoUniDirectionalRelationshipsClassTwo.class, "one");

    testUnidirectionalOneToOneRelationshipProperties(twoPropertyConfig);
    testUnidirectionalOneToOneRelationshipProperties(onePropertyConfig);
  }


  @Entity
  static class OwningSideBiDirectional {
    @Id protected Long id;

    @OneToOne
    protected InverseSideBiDirectional inverseSide;
  }

  @Entity
  static class InverseSideBiDirectional {
    @Id protected Long id;

    @OneToOne(mappedBy = "inverseSide")
    protected OwningSideBiDirectional owningSide;
  }

  @Test
  public void bidirectionalOneToOneClasses_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OwningSideBiDirectional.class, InverseSideBiDirectional.class });
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(OwningSideBiDirectional.class, "inverseSide");
    PropertyConfig owningSidePropertyConfig = getPropertyConfigurationForField(InverseSideBiDirectional.class, "owningSide");

    testBidirectionalOneToOneRelationshipProperties(inverseSidePropertyConfig);
    testBidirectionalOneToOneRelationshipProperties(owningSidePropertyConfig);

    Assert.assertTrue(inverseSidePropertyConfig.isOwningSide());
    Assert.assertFalse(inverseSidePropertyConfig.isInverseSide());
    Assert.assertFalse(owningSidePropertyConfig.isOwningSide());
    Assert.assertTrue(owningSidePropertyConfig.isInverseSide());
  }


  @Entity
  static class OwningSideOrphanRemovalSet {
    @Id protected Long id;

    @OneToOne(orphanRemoval = true)
    protected InverseSideUniDirectional inverseSide;
  }

  @Test(expected = SQLException.class)
  public void orphanRemovalSet_ThrowsNotSupportedException() throws SQLException, NoSuchFieldException {
    try {
      entityConfigurationReader.readConfiguration(new Class[] { OwningSideOrphanRemovalSet.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception must be thrown");
  }


  @Entity
  static class OwningSideSupportedOneToOneAttributesSet {
    @Id protected Long id;

    @OneToOne(targetEntity = InverseSideUniDirectional.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    protected InverseSideUniDirectional inverseSide;
  }

  @Test
  public void supportedOneToOneAttributesSet_AttributesAreApplied() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{OwningSideSupportedOneToOneAttributesSet.class, InverseSideUniDirectional.class});
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(OwningSideSupportedOneToOneAttributesSet.class, "inverseSide");

    Assert.assertEquals(InverseSideUniDirectional.class, inverseSidePropertyConfig.getTargetEntityClass());
    Assert.assertEquals(1, inverseSidePropertyConfig.getCascade().length);
    Assert.assertEquals(CascadeType.ALL, inverseSidePropertyConfig.getCascade()[0]);
    Assert.assertEquals(FetchType.LAZY, inverseSidePropertyConfig.getFetch());
    Assert.assertFalse(inverseSidePropertyConfig.canBeNull());

    Assert.assertTrue(inverseSidePropertyConfig.cascadePersist());
    Assert.assertTrue(inverseSidePropertyConfig.cascadeRefresh());
    Assert.assertTrue(inverseSidePropertyConfig.cascadeRemove());
  }

}
