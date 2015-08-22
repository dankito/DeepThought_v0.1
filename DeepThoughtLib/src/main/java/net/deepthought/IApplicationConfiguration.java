package net.deepthought;

import net.deepthought.data.persistence.EntityManagerConfiguration;

/**
 * Created by ganymed on 15/08/15.
 */
public interface IApplicationConfiguration extends IDependencyResolver {

  EntityManagerConfiguration getEntityManagerConfiguration();

}
