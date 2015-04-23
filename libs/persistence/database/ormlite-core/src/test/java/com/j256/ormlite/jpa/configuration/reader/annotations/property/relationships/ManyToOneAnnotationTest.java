package com.j256.ormlite.jpa.configuration.reader.annotations.property.relationships;

import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.StringHelper;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 07/03/15.
 */
public class ManyToOneAnnotationTest extends JpaConfigurationReaderTestBase {

  private final static String TestJoinColumnName = "inverse_side";
  private final static String TestJoinColumnDefinition = "smallint";


  @Entity
  static class WithoutManyToOneAnnotation {
    @Id protected Long id;

    protected WithoutManyToOneAnnotation parent;
    protected Collection<WithoutManyToOneAnnotation> children;
  }

  @Test(expected = SQLException.class)
  public void withoutManyToOneAnnotation_ExceptionIsThrownTypeCannotBeSerialized() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { WithoutManyToOneAnnotation.class });

    Assert.fail("Should never come to this as Exception should be thrown");
  }


  @Entity
  static class ManyToOneUniDirectional {
    @Id protected Long id;

    @ManyToOne
    protected InverseSideUniDirectional one;
  }

  @Entity
  static class InverseSideUniDirectional {
    @Id protected Long id;
    protected Collection<ManyToOneUniDirectional> manys;
  }

  @Test
  public void unidirectionalManyToOne_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ ManyToOneUniDirectional.class, InverseSideUniDirectional.class });
    PropertyConfig onePropertyConfig = getPropertyConfigurationForField(ManyToOneUniDirectional.class, "one");

    testUnidirectionalManyToOneRelationshipProperties(onePropertyConfig);
  }

  @Test
  public void unidirectionalManyToOneJoinColumnNotSet_JoinColumnSettingsAreSetToDefault() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { ManyToOneUniDirectional.class, InverseSideUniDirectional.class });
    PropertyConfig onePropertyConfig = getPropertyConfigurationForField(ManyToOneUniDirectional.class, "one");

    Assert.assertTrue(onePropertyConfig.isJoinColumn());
    Assert.assertEquals((InverseSideUniDirectional.class.getSimpleName() + "_" + "id").toUpperCase(), onePropertyConfig.getColumnName());
    Assert.assertTrue(onePropertyConfig.canBeNull());
    Assert.assertFalse(onePropertyConfig.isUnique());
    Assert.assertTrue(onePropertyConfig.isInsertable());
    Assert.assertTrue(onePropertyConfig.isUpdatable());
    Assert.assertFalse(StringHelper.stringNotNullOrEmpty(onePropertyConfig.getColumnDefinition()));
  }

  @Test
  public void unidirectionalManyToOne_ManyToOneDefaultAttributeValuesGetApplied() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { ManyToOneUniDirectional.class, InverseSideUniDirectional.class });
    PropertyConfig onePropertyConfig = getPropertyConfigurationForField(ManyToOneUniDirectional.class, "one");

    Assert.assertEquals(InverseSideUniDirectional.class, onePropertyConfig.getTargetEntityClass());
    Assert.assertEquals(0, onePropertyConfig.getCascade().length);
    Assert.assertEquals(FetchType.EAGER, onePropertyConfig.getFetch());
    Assert.assertTrue(onePropertyConfig.canBeNull());

    Assert.assertFalse(onePropertyConfig.cascadePersist());
    Assert.assertFalse(onePropertyConfig.cascadeRefresh());
    Assert.assertFalse(onePropertyConfig.cascadeRemove());
  }


  @Entity
  static class UniDirectionalJoinColumnSet {
    @Id protected Long id;

    @ManyToOne
    @JoinColumn(name = TestJoinColumnName, nullable = false, unique = true, insertable = false, updatable = false, columnDefinition = TestJoinColumnDefinition)
    protected InverseSideUniDirectional inverseSide;
  }

  @Test
  public void unidirectionalOneToOneJoinColumnSet_JoinColumnSettingsAreApplied() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { UniDirectionalJoinColumnSet.class });
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(UniDirectionalJoinColumnSet.class, "inverseSide");

    Assert.assertTrue(inverseSidePropertyConfig.isJoinColumn());
    Assert.assertEquals(TestJoinColumnName.toUpperCase(), inverseSidePropertyConfig.getColumnName());
    Assert.assertFalse(inverseSidePropertyConfig.canBeNull());
    Assert.assertTrue(inverseSidePropertyConfig.isUnique());
    Assert.assertFalse(inverseSidePropertyConfig.isInsertable());
    Assert.assertFalse(inverseSidePropertyConfig.isUpdatable());
    Assert.assertEquals(TestJoinColumnDefinition, inverseSidePropertyConfig.getColumnDefinition());
  }


  @Entity
  static class UniDirectionalJoinColumnReferencedColumnNameSet {
    @Id protected Long id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "I will throw a not supported exception")
    protected InverseSideUniDirectional inverseSide;
  }

  @Test(expected = SQLException.class)
  public void unidirectionalOneToOneJoinColumnReferencedColumnNameSet_ThrowsNotSupportedException() throws SQLException, NoSuchFieldException {
    try {
      entityConfigurationReader.readConfiguration(new Class[]{ UniDirectionalJoinColumnReferencedColumnNameSet.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception must be thrown");
  }


  @Entity
  static class UniDirectionalJoinColumnTableSet {
    @Id protected Long id;

    @ManyToOne
    @JoinColumn(table = "I will throw a not supported exception")
    protected InverseSideUniDirectional inverseSide;
  }

  @Test(expected = SQLException.class)
  public void unidirectionalOneToOneJoinColumnTableSet_ThrowsNotSupportedException() throws SQLException, NoSuchFieldException {
    try {
      entityConfigurationReader.readConfiguration(new Class[] { UniDirectionalJoinColumnTableSet.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception must be thrown");
  }


  @Entity
  static class TwoUniDirectionalRelationshipsClassOne {
    @Id protected Long id;

    @ManyToOne
    protected TwoUniDirectionalRelationshipsClassTwo two;
  }

  @Entity
  static class TwoUniDirectionalRelationshipsClassTwo {
    @Id protected Long id;

    @ManyToOne
    protected TwoUniDirectionalRelationshipsClassOne one;
  }

  @Test
  public void twoUnidirectionalManyToOneClasses_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { TwoUniDirectionalRelationshipsClassOne.class, TwoUniDirectionalRelationshipsClassTwo.class });
    PropertyConfig twoPropertyConfig = getPropertyConfigurationForField(TwoUniDirectionalRelationshipsClassOne.class, "two");
    PropertyConfig onePropertyConfig = getPropertyConfigurationForField(TwoUniDirectionalRelationshipsClassTwo.class, "one");

    testUnidirectionalManyToOneRelationshipProperties(twoPropertyConfig);
    testUnidirectionalManyToOneRelationshipProperties(onePropertyConfig);
  }


  @Entity
  static class OwningSideBiDirectional {
    @Id protected Long id;

    @ManyToOne
    protected InverseSideBiDirectional inverseSide;
  }

  @Entity
  static class InverseSideBiDirectional {
    @Id protected Long id;

    @OneToMany(mappedBy = "inverseSide")
    protected Collection<OwningSideBiDirectional> owningSide;
  }

  @Test
  public void bidirectionalOneToOneClasses_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OwningSideBiDirectional.class, InverseSideBiDirectional.class });
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(OwningSideBiDirectional.class, "inverseSide");
    PropertyConfig owningSidePropertyConfig = getPropertyConfigurationForField(InverseSideBiDirectional.class, "owningSide");

    testBidirectionalManyToOneRelationshipProperties(inverseSidePropertyConfig);
    testBidirectionalOneToManyRelationshipProperties(owningSidePropertyConfig);

    Assert.assertTrue(inverseSidePropertyConfig.isOwningSide());
    Assert.assertFalse(inverseSidePropertyConfig.isInverseSide());
    Assert.assertFalse(owningSidePropertyConfig.isOwningSide());
    Assert.assertTrue(owningSidePropertyConfig.isInverseSide());
  }


  @Entity
  static class SupportedManyToOneAttributesSet {
    @Id protected Long id;

    @ManyToOne(targetEntity = InverseSideUniDirectional.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    protected InverseSideUniDirectional inverseSide;
  }

  @Test
  public void supportedOneToOneAttributesSet_AttributesAreApplied() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{SupportedManyToOneAttributesSet.class});
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(SupportedManyToOneAttributesSet.class, "inverseSide");

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
