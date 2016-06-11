package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.model.DeepThoughtApplicationTestBase;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;

/**
 * Created by ganymed on 29/01/15.
 */
public class DeepThoughtApplicationTest extends DeepThoughtApplicationTestBase {

  @Override
  protected IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }

}
