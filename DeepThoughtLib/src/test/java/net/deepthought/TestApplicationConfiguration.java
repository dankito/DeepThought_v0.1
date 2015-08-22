package net.deepthought;

import net.deepthought.communication.listener.DeepThoughtsConnectorListener;
import net.deepthought.data.IDataManager;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.NoOperationBackupManager;
import net.deepthought.data.helper.TestDataManager;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;

/**
 * Created by ganymed on 22/08/15.
 */
public class TestApplicationConfiguration extends DefaultDependencyResolver implements IApplicationConfiguration {


  protected EntityManagerConfiguration entityManagerConfiguration;


  public TestApplicationConfiguration() {

  }

  public TestApplicationConfiguration(String dataFolder) {
    this.entityManagerConfiguration = new TestEntityManagerConfiguration(dataFolder);
  }

  public TestApplicationConfiguration(IEntityManager entityManager) {
    super(entityManager);
  }

  public TestApplicationConfiguration(DeepThoughtsConnectorListener connectorListener) {
    super(connectorListener);
  }

  public TestApplicationConfiguration(IEntityManager entityManager, DeepThoughtsConnectorListener connectorListener) {
    super(entityManager, connectorListener);
  }

  public TestApplicationConfiguration(IEntityManager entityManager, IBackupManager backupManager) {
    super(entityManager, backupManager);
  }


  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
    if(entityManager != null)
      return entityManager;
    return new MockEntityManager();
  }

  @Override
  public EntityManagerConfiguration getEntityManagerConfiguration() {
    if(entityManagerConfiguration == null)
      entityManagerConfiguration = new TestEntityManagerConfiguration();
    return entityManagerConfiguration;
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
