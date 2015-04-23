package com.j256.ormlite.jpa.configuration.reader.annotations.property.relationships;

import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 07/03/15.
 */
public class OneToManyAnnotationTest extends JpaConfigurationReaderTestBase {

  @Entity
  static class OneSideUnAnnotated {
    @Id protected Long id;

    protected Collection<OneSideUnAnnotated> manys;
  }

  @Test(expected = SQLException.class)
  public void oneToManyAnnotationNotSet_ExceptionIsThrownTypeCannotBeSerialized() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OneSideUnAnnotated.class });

    Assert.fail("Should never come to this as Exception should be thrown");
  }


  @Entity
  static class OneSideUniDirectional {
    @Id protected Long id;

    @OneToMany
    protected Collection<ManySideUniDirectional> manys;
  }

  @Entity
  static class ManySideUniDirectional {
    @Id protected Long id;
  }

  @Test(expected = SQLException.class) // TODO: remove again as soon as unidirectional OneToMany relationships are implemented
  public void unidirectionalOneToMany_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ OneSideUniDirectional.class, ManySideUniDirectional.class });
    PropertyConfig manysPropertyConfig = getPropertyConfigurationForField(OneSideUniDirectional.class, "manys");

    testUnidirectionalOneToManyRelationshipProperties(manysPropertyConfig);
  }

  @Test(expected = SQLException.class) // TODO: remove again as soon as unidirectional OneToMany relationships are implemented
  public void unidirectionalOneToMany_OneToManyDefaultAttributeValuesGetApplied() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OneSideUniDirectional.class, ManySideUniDirectional.class });
    PropertyConfig manysPropertyConfig = getPropertyConfigurationForField(OneSideUniDirectional.class, "manys");

    Assert.assertEquals(ManySideUniDirectional.class, manysPropertyConfig.getTargetEntityClass());
    Assert.assertEquals(0, manysPropertyConfig.getCascade().length);
    Assert.assertEquals(FetchType.LAZY, manysPropertyConfig.getFetch());

    Assert.assertFalse(manysPropertyConfig.cascadePersist());
    Assert.assertFalse(manysPropertyConfig.cascadeRefresh());
    Assert.assertFalse(manysPropertyConfig.cascadeRemove());
  }


  @Entity
  static class TwoUniDirectionalRelationshipsClassOne {
    @Id protected Long id;

    @OneToMany
    protected Collection<TwoUniDirectionalRelationshipsClassTwo> twos;
  }

  @Entity
  static class TwoUniDirectionalRelationshipsClassTwo {
    @Id protected Long id;

    @ManyToOne
    protected TwoUniDirectionalRelationshipsClassOne one;
  }

  @Test(expected = SQLException.class) // TODO: remove again as soon as unidirectional OneToMany relationships are implemented
  public void twoUnidirectionalOneToManyAndManyToOneClasses_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { TwoUniDirectionalRelationshipsClassOne.class, TwoUniDirectionalRelationshipsClassTwo.class });
    PropertyConfig twosPropertyConfig = getPropertyConfigurationForField(TwoUniDirectionalRelationshipsClassOne.class, "twos");
    PropertyConfig onePropertyConfig = getPropertyConfigurationForField(TwoUniDirectionalRelationshipsClassTwo.class, "one");

    testUnidirectionalManyToOneRelationshipProperties(onePropertyConfig);
    testUnidirectionalOneToManyRelationshipProperties(twosPropertyConfig);
  }


  @Entity
  static class OneSideBiDirectional {
    @Id protected Long id;

    @OneToMany(mappedBy = "one")
    protected Collection<ManySideBiDirectional> manys;
  }

  @Entity
  static class ManySideBiDirectional {
    @Id protected Long id;

    @ManyToOne
    protected OneSideBiDirectional one;
  }

  @Test
  public void bidirectionalOneToManyAndManyToOneClasses_RelationShipPropertiesGetSet() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { OneSideBiDirectional.class, ManySideBiDirectional.class });
    PropertyConfig manysPropertyConfig = getPropertyConfigurationForField(OneSideBiDirectional.class, "manys");
    PropertyConfig onePropertyConfig = getPropertyConfigurationForField(ManySideBiDirectional.class, "one");

    testBidirectionalOneToManyRelationshipProperties(manysPropertyConfig);
    testBidirectionalManyToOneRelationshipProperties(onePropertyConfig);

    Assert.assertFalse(manysPropertyConfig.isOwningSide());
    Assert.assertTrue(manysPropertyConfig.isInverseSide());
    Assert.assertTrue(onePropertyConfig.isOwningSide());
    Assert.assertFalse(onePropertyConfig.isInverseSide());
  }


  @Entity
  static class OrphanRemovalSet {
    @Id protected Long id;

    @OneToMany(orphanRemoval = true)
    protected Collection<ManySideUniDirectional> manys;
  }

  @Test(expected = SQLException.class)
  public void orphanRemovalSet_ThrowsNotSupportedException() throws SQLException, NoSuchFieldException {
    try {
      entityConfigurationReader.readConfiguration(new Class[] { OrphanRemovalSet.class, ManySideUniDirectional.class });
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come to this as Exception must be thrown");
  }

}
