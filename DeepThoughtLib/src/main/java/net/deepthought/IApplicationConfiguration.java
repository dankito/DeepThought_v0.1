package net.deepthought;

/**
 * Created by ganymed on 15/08/15.
 */
public interface IApplicationConfiguration {

  IDependencyResolver getDependencyResolver();

  Class[] getEntityClasses();

  String getDataFolder();

}
