package net.dankito.deepthought.data.helper;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.IApplicationConfiguration;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Group;
import net.dankito.deepthought.data.model.Note;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.model.enums.BackupFileServiceType;
import net.dankito.deepthought.data.model.enums.FileType;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.enums.NoteType;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 11/04/15.
 */
public class MockEntityManager implements IEntityManager {

  protected IApplicationConfiguration configuration = new TestApplicationConfiguration();

  protected Map<Class, Long> lastEntityIndices = new HashMap<>();

  protected Map<Class, Map<Long, BaseEntity>> mapPersistedEntities = new HashMap<>();


  public MockEntityManager() {
    findPersistenceFields();

    findLifeCycleMethods();
  }


  @Override
  public String getDatabasePath() {
    return new File(configuration.getEntityManagerConfiguration().getDataFolder(), "Mock.db").getAbsolutePath();
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

      if(entity instanceof DeepThoughtApplication)
        persistDeepThoughtApplication((DeepThoughtApplication) entity);
      else if(entity instanceof DeepThought)
        persistDeepThought((DeepThought)entity);
      if(entity instanceof UserDataEntity) {
        createdByField.set(entity, Application.getLoggedOnUser());
        modifiedByField.set(entity, Application.getLoggedOnUser());
        ownerField.set(entity, Application.getLoggedOnUser());
      }

      callPostPersistLifeCycleMethod(entity);
    } catch(Exception ex) {
      return false;
    }
    return true;
  }

  private void persistDeepThoughtApplication(DeepThoughtApplication application) {
    for(User entity : application.getUsers())
      persistEntity(entity);
    for(Group entity : application.getGroups())
      persistEntity(entity);
    for(Device entity : application.getDevices())
      persistEntity(entity);
    for(ApplicationLanguage entity : application.getApplicationLanguages())
      persistEntity(entity);
  }

  protected void persistDeepThought(DeepThought deepThought) {
    persistEntity(deepThought.getTopLevelCategory());
    for(Category category : deepThought.getCategories())
      persistEntity(category);

    persistEntity(deepThought.getTopLevelEntry());
    for(Entry entry : deepThought.getEntries())
      persistEntity(entry);

    for(Tag tag : deepThought.getTags())
      persistEntity(tag);
    for(Person person : deepThought.getPersons())
      persistEntity(person);

    for(SeriesTitle entity : deepThought.getSeriesTitles())
      persistEntity(entity);
    for(Reference entity : deepThought.getReferences())
      persistEntity(entity);
    for(ReferenceSubDivision entity : deepThought.getReferenceSubDivisions())
      persistEntity(entity);

    for(NoteType entity : deepThought.getNoteTypes())
      persistEntity(entity);
    for(Note entity : deepThought.getNotes())
      persistEntity(entity);

    for(FileType entity : deepThought.getFileTypes())
      persistEntity(entity);
    for(FileLink entity : deepThought.getFiles())
      persistEntity(entity);
    for(BackupFileServiceType entity : deepThought.getBackupFileServiceTypes())
      persistEntity(entity);
    for(Language entity : deepThought.getLanguages())
      persistEntity(entity);
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
  public <T extends BaseEntity> List<T> getEntitiesById(Class<T> type, Collection<Long> ids, boolean keepOrderingOfIds) {
    List<T> result = new ArrayList<>();

    for(Long id : ids)
      result.add(getEntityById(type, id));

    return result;
  }

  @Override
  public <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type) {
//    if(type == DeepThoughtApplication.class)
//      return new ArrayList<T>() {{ add((T)DataHelper.createTestApplication()); }} ;
    return new ArrayList<>();
  }

  @Override
  public <T> Collection<T> sortReferenceBaseIds(Collection<T> referenceBaseIds) {
    return new ArrayList<>();
  }

  @Override
  public void resolveAllLazyRelations(BaseEntity entity) throws Exception {

  }

  @Override
  public <T extends BaseEntity> List<T> queryEntities(Class<T> entityClass, String whereStatement) throws SQLException {
    return new ArrayList<>();
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
  protected Method baseEntityPostPersistLifeCycleMethod = null;
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

      baseEntityPostPersistLifeCycleMethod = BaseEntity.class.getDeclaredMethod("postPersist");
      baseEntityPostPersistLifeCycleMethod.setAccessible(true);

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
      else
        baseEntityPostPersistLifeCycleMethod.invoke(entity);
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
