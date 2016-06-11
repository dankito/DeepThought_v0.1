package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.model.ReferenceSubDivisionTestBase;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;

/**
 * Created by ganymed on 10/11/14.
 */
public class ReferenceSubDivisionTest extends ReferenceSubDivisionTestBase {

  @Override
  protected IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }

}
