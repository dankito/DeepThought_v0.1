package net.dankito.deepthought.data;

import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.model.settings.UserDeviceSettings;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

/**
 * Created by ganymed on 03/01/15.
 */
public interface IDataManager {

  DeepThoughtApplication getApplication();
  UserDeviceSettings getSettings();
  User getLoggedOnUser();
  DeepThought getDeepThought();
  String getDataCollectionSavePath();
  String getDataFolderPath();

  IEntityManager getEntityManager();

  boolean addApplicationListener(ApplicationListener listener);
  boolean removeApplicationListener(ApplicationListener listener);

  boolean addAllEntitiesListener(AllEntitiesListener listener);
  boolean removeAllEntitiesListener(AllEntitiesListener listener);

  void deleteExistingDataCollection();
  void replaceExistingDataCollectionWithData(DeepThoughtApplication data);

  DeepThought retrieveDeepThoughtApplication();
  void recreateEntityManagerAndRetrieveDeepThoughtApplication();

  void ensureAllLazyLoadingDataIsLoaded(BaseEntity entity);

  void lazyLoadedEntityMapped(BaseEntity entity);

  void close();

}
