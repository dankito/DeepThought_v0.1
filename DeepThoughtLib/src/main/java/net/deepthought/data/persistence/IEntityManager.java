package net.deepthought.data.persistence;

import net.deepthought.data.persistence.db.BaseEntity;

import java.util.List;

/**
 * Created by ganymed on 02/01/15.
 */
public interface IEntityManager {

  public String getDatabasePath();

  public boolean persistEntity(BaseEntity entity);

  public boolean updateEntity(BaseEntity entity);
  public boolean updateEntities(List<BaseEntity> entities);

  public boolean deleteEntity(BaseEntity entity);

  public <T extends BaseEntity> T getEntityById(Class<T> type, Long id);
  public <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type);

  /**
   * <p>
   *   Lazy initialization requires that the Database connection is still open.
   *   But if you plan to close Database connection and still like to continue working with an Entity containing lazy loaded fields,
   *   you have to retrieve all lazy field data from database before.
   * </p>
   * @param entity
   * @throws Exception
   */
  public void resolveAllLazyRelations(BaseEntity entity) throws Exception;

  public List doNativeQuery(String query);

  public void close();

}
