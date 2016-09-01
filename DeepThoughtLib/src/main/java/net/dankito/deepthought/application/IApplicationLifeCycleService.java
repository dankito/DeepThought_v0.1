package net.dankito.deepthought.application;

/**
 * Created by ganymed on 01/09/16.
 */
public interface IApplicationLifeCycleService {

  boolean addApplicationTerminatesListenerListener(ApplicationTerminatesListener listener);

  boolean removeApplicationTerminatesListenerListener(ApplicationTerminatesListener listener);

  boolean isApplicationInForeground();

  boolean addApplicationForegroundBackgroundListener(ApplicationForegroundBackgroundListener listener);

  boolean removeApplicationForegroundBackgroundListener(ApplicationForegroundBackgroundListener listener);

}
