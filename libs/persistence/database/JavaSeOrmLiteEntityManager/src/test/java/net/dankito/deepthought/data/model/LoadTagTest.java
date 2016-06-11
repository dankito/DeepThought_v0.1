package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.model.LoadTagTestBase;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;

/**
 * Created by ganymed on 19/04/15.
 */
public class LoadTagTest extends LoadTagTestBase {

  @Override
  protected IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }

}
