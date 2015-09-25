package net.deepthought;

import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.platform.IPlatformConfiguration;
import net.deepthought.platform.IPreferencesStore;
import net.deepthought.plugin.IPlugin;

import java.util.Collection;

/**
 * Created by ganymed on 15/08/15.
 */
public interface IApplicationConfiguration extends IDependencyResolver {

  IPlatformConfiguration getPlatformConfiguration();

  IPreferencesStore getPreferencesStore();

  EntityManagerConfiguration getEntityManagerConfiguration();

  Collection<IPlugin> getStaticallyLinkedPlugins();

}
