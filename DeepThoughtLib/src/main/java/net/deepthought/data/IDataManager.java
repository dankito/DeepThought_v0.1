package net.deepthought.data;

import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.User;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;

/**
 * Created by ganymed on 03/01/15.
 */
public interface IDataManager {

  public DeepThoughtApplication getApplication();
  public UserDeviceSettings getSettings();
  public User getLoggedOnUser();
  public DeepThought getDeepThought();
  public String getDataCollectionSavePath();
  public String getDataFolderPath();

  public IEntityManager getEntityManager();

  public boolean addApplicationListener(ApplicationListener listener);
  public boolean removeApplicationListener(ApplicationListener listener);

  public void deleteExistingDataCollection();
  public void replaceExistingDataCollectionWithData(DeepThoughtApplication data);

  public DeepThought retrieveDeepThoughtApplication();
  public void recreateEntityManagerAndRetrieveDeepThoughtApplication();

  public void ensureAllLazyLoadingDataIsLoaded(BaseEntity entity);

  public void close();

}
