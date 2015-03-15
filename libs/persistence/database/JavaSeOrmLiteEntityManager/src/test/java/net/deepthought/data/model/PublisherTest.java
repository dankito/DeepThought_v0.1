package net.deepthought.data.model;

import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;

/**
 * Created by ganymed on 10/11/14.
 */
public class PublisherTest extends PublisherTestBase {

  @Override
  protected IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }

}
