package net.deepthought.data.model.listener;

import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;

import org.junit.Ignore;

/**
 * Created by ganymed on 09/11/14.
 */
@Ignore
public class DeepThoughtListenerTest extends DeepThoughtListenerTestBase {

  @Override
  protected IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }
}
