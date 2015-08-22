package net.deepthought.data.helper;

import net.deepthought.DefaultDependencyResolver;
import net.deepthought.communication.listener.DeepThoughtsConnectorListener;
import net.deepthought.data.IDataManager;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.persistence.IEntityManager;

/**
 * Created by ganymed on 07/01/15.
 */
public class TestDependencyResolver extends DefaultDependencyResolver {

  public TestDependencyResolver() {
    super();
  }

  public TestDependencyResolver(IEntityManager entityManager) {
    super(entityManager);
  }

  public TestDependencyResolver(DeepThoughtsConnectorListener connectorListener) {
    super(connectorListener);
  }

  public TestDependencyResolver(IEntityManager entityManager, DeepThoughtsConnectorListener connectorListener) {
    super(entityManager, connectorListener);
  }

  public TestDependencyResolver(IEntityManager entityManager, IBackupManager backupManager) {
    super(entityManager, backupManager);
  }

  @Override
  public IDataManager createDataManager(IEntityManager entityManager) {
    return new TestDataManager(entityManager);
  }

  @Override
  public IBackupManager createBackupManager() {
    if(backupManager != null)
      return backupManager;
    return new NoOperationBackupManager();
  }
}
