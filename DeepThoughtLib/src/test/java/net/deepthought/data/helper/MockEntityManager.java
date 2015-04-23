package net.deepthought.data.helper;

import net.deepthought.Application;
import net.deepthought.data.ApplicationConfiguration;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 11/04/15.
 */
public class MockEntityManager implements IEntityManager {

  protected ApplicationConfiguration configuration = new TestApplicationConfiguration();

  protected Map<Class, Long> lastEntityIndices = new HashMap<>();

  protected Map<Class, Map<Long, BaseEntity>> mapPersistedEntities = new HashMap<>();


  public MockEntityManager() {
    findPersistenceFields();

    findLifeCycleMethods();
  }


  @Override
  public String getDatabasePath() {
    return configuration.getDataFolder();
  }

  @Override
  public boolean persistEntity(BaseEntity entity) {
    Class entityClass = entity.getClass();
    if(entity instanceof ReferenceBase)
      entityClass = ReferenceBase.class;

    if(mapPersistedEntities.containsKey(entityClass) == false)
      mapPersistedEntities.put(entityClass, new HashMap<Long, BaseEntity>());
    if(lastEntityIndices.containsKey(entityClass) == false)
      lastEntityIndices.put(entityClass, 0L);

    try {
      callPrePersistLifeCycleMethod(entity);

      Long id = lastEntityIndices.get(entityClass);
      id++;
      lastEntityIndices.put(entityClass, id);
      idField.set(entity, id);
      versionField.set(entity, 1L);

      mapPersistedEntities.get(entityClass).put(id, entity);

      if(entity instanceof UserDataEntity) {
        createdByField.set(entity, Application.getLoggedOnUser());
        modifiedByField.set(entity, Application.getLoggedOnUser());
        ownerField.set(entity, Application.getLoggedOnUser());
      }
      else if(entity instanceof DeepThoughtApplication) {
        persistEntity(((DeepThoughtApplication)entity).getLastLoggedOnUser());
      }

      callPostPersistLifeCycleMethod(entity);
    } catch(Exception ex) {
      return false;
    }
    return true;
  }

  @Override
  public boolean updateEntity(BaseEntity entity) {
    callPreUpdateLifeCycleMethod(entity);
    return true;
  }

  @Override
  public boolean updateEntities(List<BaseEntity> entities) {
    for(BaseEntity entity : entities)
      updateEntity(entity);
    return true;
  }

  @Override
  public boolean deleteEntity(BaseEntity entity) {
    Class entityClass = entity.getClass();
    if(entity instanceof ReferenceBase)
      entityClass = ReferenceBase.class;

    if(mapPersistedEntities.containsKey(entityClass) && mapPersistedEntities.get(entityClass).containsKey(entity.getId())) {
      callPreRemoveLifeCycleMethod(entity);
      mapPersistedEntities.get(entityClass).remove(entity.getId());
      return true;
    }

    return false;
  }

  @Override
  public <T extends BaseEntity> T getEntityById(Class<T> type, Long id) {
    if(mapPersistedEntities.containsKey(type) && mapPersistedEntities.get(type).containsKey(id))
      return (T)mapPersistedEntities.get(type).get(id);

    return null;
  }

  @Override
  public <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type) {
//    if(type == DeepThoughtApplication.class)
//      return new ArrayList<T>() {{ add((T)DataHelper.createTestApplication()); }} ;
    return new ArrayList<>();
  }

  @Override
  public void resolveAllLazyRelations(BaseEntity entity) throws Exception {

  }

  @Override
  public List doNativeQuery(String query) throws SQLException {
    return new ArrayList();
  }

  @Override
  public void close() {

  }


  protected static Field idField;
  protected static Field versionField;
  protected static Field createdByField;
  protected static Field modifiedByField;
  protected static Field ownerField;

  protected void findPersistenceFields() {
    try {
      idField = BaseEntity.class.getDeclaredField("id");
      idField.setAccessible(true);
      versionField = BaseEntity.class.getDeclaredField("version");
      versionField.setAccessible(true);

      createdByField = UserDataEntity.class.getDeclaredField("createdBy");
      createdByField.setAccessible(true);
      modifiedByField = UserDataEntity.class.getDeclaredField("modifiedBy");
      modifiedByField.setAccessible(true);
      ownerField = UserDataEntity.class.getDeclaredField("owner");
      ownerField.setAccessible(true);
    } catch(Exception ex) { }
  }


  protected Method userDataEntityPrePersistLifeCycleMethod = null;
  protected Method userDataEntityPreUpdateLifeCycleMethod = null;

  protected Method baseEntityPostLoadLifeCycleMethod = null;
  protected Method baseEntityPreRemoveLifeCycleMethod = null;

  protected Method entryPostPersistLifeCycleMethod = null;
  protected Method tagPostPersistLifeCycleMethod = null;
  protected Method categoryPostPersistLifeCycleMethod = null;
  protected Method personPostPersistLifeCycleMethod = null;
  protected Method referenceBasePostPersistLifeCycleMethod = null;

  protected void findLifeCycleMethods() {
    try {
      userDataEntityPrePersistLifeCycleMethod = UserDataEntity.class.getDeclaredMethod("prePersist");
      userDataEntityPrePersistLifeCycleMethod.setAccessible(true);

      userDataEntityPreUpdateLifeCycleMethod = UserDataEntity.class.getDeclaredMethod("preUpdate");
      userDataEntityPreUpdateLifeCycleMethod.setAccessible(true);

      baseEntityPostLoadLifeCycleMethod = BaseEntity.class.getDeclaredMethod("postLoad");
      baseEntityPostLoadLifeCycleMethod.setAccessible(true);

      baseEntityPreRemoveLifeCycleMethod = BaseEntity.class.getDeclaredMethod("preRemove");
      baseEntityPreRemoveLifeCycleMethod.setAccessible(true);

      entryPostPersistLifeCycleMethod = Entry.class.getDeclaredMethod("postPersist");
      entryPostPersistLifeCycleMethod.setAccessible(true);

      tagPostPersistLifeCycleMethod = Tag.class.getDeclaredMethod("postPersist");
      tagPostPersistLifeCycleMethod.setAccessible(true);

      categoryPostPersistLifeCycleMethod = Category.class.getDeclaredMethod("postPersist");
      categoryPostPersistLifeCycleMethod.setAccessible(true);

      personPostPersistLifeCycleMethod = Person.class.getDeclaredMethod("postPersist");
      personPostPersistLifeCycleMethod.setAccessible(true);

      referenceBasePostPersistLifeCycleMethod = ReferenceBase.class.getDeclaredMethod("postPersist");
      referenceBasePostPersistLifeCycleMethod.setAccessible(true);
    } catch(Exception ex) { }
  }

  protected void callPrePersistLifeCycleMethod(BaseEntity entity) {
    try {
      userDataEntityPrePersistLifeCycleMethod.invoke(entity);
    } catch(Exception ex) { }
  }

  protected void callPostPersistLifeCycleMethod(BaseEntity entity) {
    try {
      if(entity instanceof Entry)
        entryPostPersistLifeCycleMethod.invoke(entity);
      else if(entity instanceof Tag)
        tagPostPersistLifeCycleMethod.invoke(entity);
      else if(entity instanceof Category)
        categoryPostPersistLifeCycleMethod.invoke(entity);
      else if(entity instanceof Person)
        personPostPersistLifeCycleMethod.invoke(entity);
      else if(entity instanceof ReferenceBase)
        referenceBasePostPersistLifeCycleMethod.invoke(entity);
    } catch(Exception ex) { }
  }

  protected void callPreUpdateLifeCycleMethod(BaseEntity entity) {
    try {
      userDataEntityPreUpdateLifeCycleMethod.invoke(entity);
    } catch(Exception ex) { }
  }

  protected void callPostLoadLifeCycleMethod(BaseEntity entity) {
    try {
      baseEntityPostLoadLifeCycleMethod.invoke(entity);
    } catch(Exception ex) { }
  }

  protected void callPreRemoveLifeCycleMethod(BaseEntity entity) {
    try {
      baseEntityPreRemoveLifeCycleMethod.invoke(entity);
    } catch(Exception ex) { }
  }
}
