package com.j256.ormlite.jpa.configuration.reader.annotations.property.relationships;

import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * Created by ganymed on 07/03/15.
 */
public class ManyToManyAnnotationTest extends JpaConfigurationReaderTestBase {

  @Entity
  static class OwningSideUnAnnotated {
    @Id protected Long id;

    protected Collection<OwningSideUnAnnotated> inverseSides;
  }

  @Test(expected = SQLException.class)
  public void manyToManyAnnotationNotSet_ExceptionIsThrownTypeCannotBeSerialized() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OwningSideUnAnnotated.class });

    Assert.fail("Should never come to this as Exception should be thrown");
  }


  @Entity
  static class OwningSideUniDirectional {
    @Id protected Long id;

    @ManyToMany
    protected Collection<InverseSideUniDirectional> inverseSides;
  }

  @Entity
  static class InverseSideUniDirectional {
    @Id protected Long id;
  }

  @Test
  public void unidirectionalManyToMany_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ OwningSideUniDirectional.class, InverseSideUniDirectional.class });
    PropertyConfig inverseSidesPropertyConfig = getPropertyConfigurationForField(OwningSideUniDirectional.class, "inverseSides");

    testUnidirectionalManyToManyRelationshipProperties(inverseSidesPropertyConfig);
  }

  @Test
  public void unidirectionalOneToMany_OneToManyDefaultAttributeValuesGetApplied() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OwningSideUniDirectional.class, InverseSideUniDirectional.class });
    PropertyConfig inverseSidePropertyConfig = getPropertyConfigurationForField(OwningSideUniDirectional.class, "inverseSides");

    Assert.assertEquals(InverseSideUniDirectional.class, inverseSidePropertyConfig.getTargetEntityClass());
    Assert.assertEquals(0, inverseSidePropertyConfig.getCascade().length);
    Assert.assertEquals(FetchType.LAZY, inverseSidePropertyConfig.getFetch());

    Assert.assertFalse(inverseSidePropertyConfig.cascadePersist());
    Assert.assertFalse(inverseSidePropertyConfig.cascadeRefresh());
    Assert.assertFalse(inverseSidePropertyConfig.cascadeRemove());
  }


  @Entity
  static class TwoUniDirectionalRelationshipsClassOne {
    @Id protected Long id;

    @ManyToMany
    protected Collection<TwoUniDirectionalRelationshipsClassTwo> twos;
  }

  @Entity
  static class TwoUniDirectionalRelationshipsClassTwo {
    @Id protected Long id;

    @ManyToMany
    protected Collection<TwoUniDirectionalRelationshipsClassOne> ones;
  }

  @Test
  public void twoUnidirectionalManyToManyClasses_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { TwoUniDirectionalRelationshipsClassOne.class, TwoUniDirectionalRelationshipsClassTwo.class });
    PropertyConfig twosPropertyConfig = getPropertyConfigurationForField(TwoUniDirectionalRelationshipsClassOne.class, "twos");
    PropertyConfig onesPropertyConfig = getPropertyConfigurationForField(TwoUniDirectionalRelationshipsClassTwo.class, "ones");

    testUnidirectionalManyToManyRelationshipProperties(twosPropertyConfig);
    testUnidirectionalManyToManyRelationshipProperties(onesPropertyConfig);
  }


  @Entity
  static class OwningSideBiDirectional {
    @Id protected Long id;

    @ManyToMany
    protected Collection<InverseSideBiDirectional> inverseSides;
  }

  @Entity
  static class InverseSideBiDirectional {
    @Id protected Long id;

    @ManyToMany(mappedBy = "inverseSides")
    protected Collection<OwningSideBiDirectional> owningSides;
  }

  @Test
  public void bidirectionalOneToManyAndManyToOneClasses_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{OwningSideBiDirectional.class, InverseSideBiDirectional.class});
    PropertyConfig inverseSidesPropertyConfig = getPropertyConfigurationForField(OwningSideBiDirectional.class, "inverseSides");
    PropertyConfig owningSidesPropertyConfig = getPropertyConfigurationForField(InverseSideBiDirectional.class, "owningSides");

    testBidirectionalManyToManyRelationshipOwningSideProperties(inverseSidesPropertyConfig);
    testBidirectionalManyToManyRelationshipProperties(owningSidesPropertyConfig);

    Assert.assertTrue(inverseSidesPropertyConfig.isOwningSide());
    Assert.assertFalse(inverseSidesPropertyConfig.isInverseSide());
    Assert.assertFalse(owningSidesPropertyConfig.isOwningSide());
    Assert.assertTrue(owningSidesPropertyConfig.isInverseSide());
  }

}
