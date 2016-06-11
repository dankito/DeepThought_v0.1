package net.dankito.deepthought;

import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.platform.IPlatformConfiguration;
import net.dankito.deepthought.platform.IPreferencesStore;
import net.dankito.deepthought.plugin.IPlugin;

import java.util.Collection;

/**
 * Created by ganymed on 15/08/15.
 */
public interface IApplicationConfiguration<THtmlEditor> extends IDependencyResolver<THtmlEditor> {

  IPlatformConfiguration getPlatformConfiguration();

  IPreferencesStore getPreferencesStore();

  EntityManagerConfiguration getEntityManagerConfiguration();

  Collection<IPlugin> getStaticallyLinkedPlugins();

}
