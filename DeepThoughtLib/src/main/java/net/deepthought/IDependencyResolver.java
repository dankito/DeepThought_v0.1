package net.deepthought;

import net.deepthought.data.IDataManager;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.compare.IDataComparer;
import net.deepthought.data.merger.IDataMerger;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;

/**
 * Created by ganymed on 05/01/15.
 */
public interface IDependencyResolver {

  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception;

  public IDataManager createDataManager(IEntityManager entityManager);

  public IBackupManager createBackupManager();

  public IDataComparer createDataComparer();

  public IDataMerger createDataMerger();

}
