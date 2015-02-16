package net.deepthought.data.merger;

import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.persistence.JpaEntityManager;

/**
 * Created by ganymed on 18/01/15.
 */
public class DefaultDataMergerTest extends DefaultDataMergerTestBase {

  @Override
  protected IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new JpaEntityManager(configuration);
  }

}
