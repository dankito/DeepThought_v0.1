package com.j256.ormlite.dao.cda;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.cda.testmodel.ManyToManyEntities;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by ganymed on 05/11/14.
 */
public class ManyToManyAnnotationConfigurationTest extends EntitiesCollectionTestBase {

  @Test()
  public void NonGenericSet_TargetEntityIsSetInAnnotation_Succeeds() throws Exception {
    // the actual test is here, if dao and therefore JoinTable can be created without errors
    Dao<ManyToManyEntities.NonGenericSetOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.NonGenericSetOwningSide.class, true, true);
    Dao<ManyToManyEntities.NonGenericSetInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.NonGenericSetInverseSide.class, true, true);
  }

  @Test()
  public void NonGenericSet_TargetEntityInAnnotationMissing_ThrowsException() throws Exception {
    Exception exception = null;

    try {
      Dao<ManyToManyEntities.NonGenericSetTargetEntityInAnnotationMissingOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.NonGenericSetTargetEntityInAnnotationMissingOwningSide.class, true, true);
      Dao<ManyToManyEntities.NonGenericSetTargetEntityInAnnotationMissingInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.NonGenericSetTargetEntityInAnnotationMissingInverseSide.class, true, true);
    } catch(Exception ex) { exception = ex; }

    Assert.assertNotNull(exception);
    Assert.assertTrue(exception instanceof SQLException);
    Assert.assertTrue(exception.getMessage().equals("java.sql.SQLException: For a @ManyToMany relation either Annotation's targetEntity value has to be set or Field's Datatype " +
        "has to be a generic Collection / Set with generic type set to target entity's type."));
  }

  @Test()
  public void GenericCollection_MappedByInAnnotationMissing_ThrowsException() throws Exception {
    Exception exception = null;

    try {
      Dao<ManyToManyEntities.GenericCollectionMappedByInAnnotationMissingOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.GenericCollectionMappedByInAnnotationMissingOwningSide.class, true, true);
      Dao<ManyToManyEntities.GenericCollectionMappedByInAnnotationMissingInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.GenericCollectionMappedByInAnnotationMissingInverseSide.class, true, true);
    } catch(Exception ex) { exception = ex; }

    Assert.assertNotNull(exception);
    Assert.assertTrue(exception instanceof SQLException);
    Assert.assertTrue(exception.getMessage().contains(" no ManyToMany annotated field has been found that declares mappedBy="));
  }

  @Test()
  public void NonGenericSet_MappedByInAnnotationMissing_ThrowsException() throws Exception {
    Exception exception = null;

    try {
      Dao<ManyToManyEntities.NonGenericSetMappedByInAnnotationMissingOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.NonGenericSetMappedByInAnnotationMissingOwningSide.class, true, true);
      Dao<ManyToManyEntities.NonGenericSetMappedByInAnnotationMissingInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.NonGenericSetMappedByInAnnotationMissingInverseSide.class, true, true);
    } catch(Exception ex) { exception = ex; }

    Assert.assertNotNull(exception);
    Assert.assertTrue(exception instanceof SQLException);
    Assert.assertTrue(exception.getMessage().contains(" no ManyToMany annotated field has been found that declares mappedBy="));
  }
}
