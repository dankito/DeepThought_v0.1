package net.deepthought.data.model;

import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;

/**
 * Created by ganymed on 29/01/15.
 */
public class CreateDeepThoughtTest extends CreateDeepThoughtTestBase {

  @Override
  protected IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }

}
