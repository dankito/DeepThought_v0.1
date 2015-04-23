package com.j256.ormlite.dao.cda;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.cda.testmodel.CascadeEntities;
import com.j256.ormlite.dao.cda.testmodel.helper.TestRelationFieldTypeCreator;
import com.j256.ormlite.instances.Instances;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 16/11/14.
 */
public class CascadePersistTest extends BaseCoreTest {

  private final static Logger log = LoggerFactory.getLogger(CascadePersistTest.class);


  @Override
  public void before() throws Exception {
    Instances.setFieldTypeCreator(new TestRelationFieldTypeCreator());
    super.before();
  }

  @Override
  public void after() throws Exception {
    super.after();
    Instances.setFieldTypeCreator(null);
  }


  @Test
  public void addEntityToEagerOneToManyRelation_CascadePersist_EntityGetsSavedToDatabase() {

  }

  @Test
  public void addEntityToLazyOneToManyRelation_CascadePersist_EntityGetsSavedToDatabase() {

  }

  @Test
  public void addEntityToEagerManyToManyRelation_CascadePersist_EntityGetsSavedToDatabase() {

  }

  @Test
  public void addEntityToLazyManyToManyRelation_CascadePersist_EntityGetsSavedToDatabase() {

  }

  @Test
  public void addEntityToEagerOneToManyRelation_CascadeAll_EntityGetsSavedToDatabase() throws Exception {
    CascadeEntities.CascadeAllEagerOneSide oneSide = new CascadeEntities.CascadeAllEagerOneSide("one");
    Dao<CascadeEntities.CascadeAllEagerOneSide, Long> dao = createDao(CascadeEntities.CascadeAllEagerOneSide.class, true, true);

    CascadeEntities.CascadeAllEagerManySide manySide = new CascadeEntities.CascadeAllEagerManySide("many");
    oneSide.getManySides().add(manySide);
  }

  @Test
  public void addEntityToLazyOneToManyRelation_CascadeAll_EntityGetsSavedToDatabase() {

  }

  @Test
  public void addEntityToEagerManyToManyRelation_CascadeAll_EntityGetsSavedToDatabase() {

  }

  @Test
  public void addEntityToLazyManyToManyRelation_CascadeAll_EntityGetsSavedToDatabase() {

  }

  @Test
  public void addEntity_DoNotCascadePersist_EntityDoesNotGetSavedToDatabase() {

  }
}
