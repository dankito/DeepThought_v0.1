package net.deepthought.data.helper;

import net.deepthought.Application;
import net.deepthought.data.ApplicationConfiguration;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 11/04/15.
 */
public class MockEntityManager implements IEntityManager {

  protected static Field idField;
  protected static Field versionField;
  protected static Field createdByField;
  protected static Field modifiedByField;
  protected static Field ownerField;

  protected ApplicationConfiguration configuration = new TestApplicationConfiguration();

  protected Map<Class, Long> lastEntityIndices = new HashMap<>();

  protected Map<Class, Map<Long, BaseEntity>> mapPersistedEntities = new HashMap<>();


  public MockEntityManager() {
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


  @Override
  public String getDatabasePath() {
    return configuration.getDataFolder();
  }

  @Override
  public boolean persistEntity(BaseEntity entity) {
    Class entityClass = entity.getClass();
    if(mapPersistedEntities.containsKey(entityClass) == false)
      mapPersistedEntities.put(entityClass, new HashMap<Long, BaseEntity>());
    if(lastEntityIndices.containsKey(entityClass) == false)
      lastEntityIndices.put(entityClass, 0L);

    try {
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
    } catch(Exception ex) {
      return false;
    }
    return true;
  }

  @Override
  public boolean updateEntity(BaseEntity entity) {
    return true;
  }

  @Override
  public boolean updateEntities(List<BaseEntity> entities) {
    return true;
  }

  @Override
  public boolean deleteEntity(BaseEntity entity) {
    Class entityClass = entity.getClass();
    if(mapPersistedEntities.containsKey(entityClass) && mapPersistedEntities.get(entityClass).containsKey(entity.getId())) {
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
}
