package net.dankito.deepthought.data.persistence;

import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 02/01/15.
 */
public interface IEntityManager {

  String getDatabasePath();

  boolean persistEntity(BaseEntity entity);

  boolean updateEntity(BaseEntity entity);
  boolean updateEntities(List<BaseEntity> entities);

  boolean deleteEntity(BaseEntity entity);

  <T extends BaseEntity> T getEntityById(Class<T> type, String id);
  <T extends BaseEntity> List<T> getEntitiesById(Class<T> type, Collection<String> ids, boolean keepOrderingOfIds);
  <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type);

  <T> Collection<T> sortReferenceBaseIds(Collection<T> referenceBaseIds);

  Collection<Entry> findEntriesHavingTheseTags(Collection<Tag> tags);

  /**
   * <p>
   *   Lazy initialization requires that the Database connection is still open.
   *   But if you plan to close Database connection and still like to continue working with an Entity containing lazy loaded fields,
   *   you have to retrieve all lazy field data from database before.
   * </p>
   * @param entity
   * @throws Exception
   */
  void resolveAllLazyRelations(BaseEntity entity) throws Exception;

  List doNativeQuery(String query) throws SQLException;

  void close();

}
