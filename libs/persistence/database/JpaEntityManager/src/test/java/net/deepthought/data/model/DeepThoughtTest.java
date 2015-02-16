package net.deepthought.data.model;

import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.persistence.JpaEntityManager;

/**
 * Created by ganymed on 09/11/14.
 */
public class DeepThoughtTest extends DeepThoughtTestBase {

  @Override
  protected IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new JpaEntityManager(configuration);
  }

}
