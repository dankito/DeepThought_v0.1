package net.deepthought;

import net.deepthought.data.DefaultDataManager;
import net.deepthought.data.IDataManager;
import net.deepthought.data.backup.DefaultBackupManager;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.compare.DefaultDataComparer;
import net.deepthought.data.compare.IDataComparer;
import net.deepthought.data.merger.DefaultDataMerger;
import net.deepthought.data.merger.IDataMerger;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;

/**
 * Created by ganymed on 05/01/15.
 */
public class DefaultDependencyResolver implements IDependencyResolver {

  protected IEntityManager entityManager;

  protected IBackupManager backupManager = null;

  protected IDataComparer dataComparer = null;

  protected IDataMerger dataMerger = null;


  public DefaultDependencyResolver() {
    this(null);
  }

  public DefaultDependencyResolver(IEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public DefaultDependencyResolver(IEntityManager entityManager, IBackupManager backupManager) {
    this.entityManager = entityManager;
    this.backupManager = backupManager;
  }


  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return entityManager;
  }

  @Override
  public IDataManager createDataManager(IEntityManager entityManager) {
    return new DefaultDataManager(entityManager);
  }

  @Override
  public IBackupManager createBackupManager() {
    if(backupManager != null)
      return backupManager;
    return new DefaultBackupManager();
  }

  @Override
  public IDataComparer createDataComparer() {
    if(dataComparer != null)
      return dataComparer;
    return new DefaultDataComparer();
  }

  @Override
  public IDataMerger createDataMerger() {
    if(dataMerger != null)
      return dataMerger;
    return new DefaultDataMerger();
  }

}
