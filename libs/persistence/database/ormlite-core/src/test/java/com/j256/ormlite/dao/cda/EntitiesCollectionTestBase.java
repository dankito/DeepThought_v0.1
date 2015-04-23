package com.j256.ormlite.dao.cda;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.cda.testmodel.RelationEntities;
import com.j256.ormlite.dao.cda.testmodel.helper.AnnotationInvocationHandler;
import com.j256.ormlite.dao.cda.testmodel.helper.TestRelationFieldTypeCreator;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.misc.TableInfoRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 05/11/14.
 */
public abstract class EntitiesCollectionTestBase extends BaseCoreTest {

  private final static Logger log = LoggerFactory.getLogger(EntitiesCollectionTestBase.class);


  @Override
  public void before() throws Exception {
    Instances.setFieldTypeCreator(new TestRelationFieldTypeCreator());
    super.before();
  }

  @Override
  public void after() throws Exception {
    super.after();
    Instances.setFieldTypeCreator(null);
    TableInfoRegistry.getInstance().clear();
  }

  protected void setFetchTypeForCollections(FetchType fetchType) {
    setFetchAndCascadeTypeForCollections(fetchType, null);
  }

  protected void setFetchAndCascadeTypeForCollections(FetchType fetchType, CascadeType[] cascadeType) {
    Class relationClass = RelationEntities.LazyOneSide.class;
    try {
      Field field = relationClass.getDeclaredField("manySides");
      setFetchAndCascadeTypeForField(field, fetchType, cascadeType);
    } catch (Exception ex) {
      log.error("Could not set FetchType on @ManyToMany annotation for class " + relationClass, ex);
    }
  }

  protected void setFetchAndCascadeTypeForField(Field field, FetchType fetchType, CascadeType[] cascadeType) throws Exception {
    OneToMany oneToManyAnnotation = field.getDeclaredAnnotation(OneToMany.class);

    if(fetchType != null) {
      OneToMany oneToManyAfterSettingFetchType = (OneToMany) setAttrValue(oneToManyAnnotation, OneToMany.class, "fetch", fetchType);
      FetchType fetchSetTest = oneToManyAfterSettingFetchType.fetch();
      OneToMany debug = field.getDeclaredAnnotation(OneToMany.class);
      FetchType debug2 = debug.fetch();
      if (fetchSetTest != fetchType)
        log.error("Could not set field " + field + " fetch type to " + fetchType);
    }

    if (cascadeType != null) {
      OneToMany oneToManyAfterSettingCascade = (OneToMany) setAttrValue(oneToManyAnnotation, OneToMany.class, "cascade", cascadeType);
      CascadeType[] cascadeSetTest = oneToManyAfterSettingCascade.cascade();
      if (cascadeSetTest != cascadeType)
        log.error("Could not set field " + field + " cascade type to " + cascadeType);
    }
  }

  public static Annotation setAttrValue(Annotation anno, Class<? extends Annotation> type, String attrName, Object newValue) throws Exception {
    InvocationHandler handler = new AnnotationInvocationHandler(anno, attrName, newValue);
    Annotation proxy = (Annotation) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
    return proxy;
  }
}
