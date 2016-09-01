package net.dankito.deepthought.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 01/09/16.
 */
public abstract class ApplicationLifeCycleServiceBase implements IApplicationLifeCycleService {

  private static final Logger log = LoggerFactory.getLogger(ApplicationLifeCycleServiceBase.class);


  protected boolean isApplicationInForeground = false;

  protected List<ApplicationForegroundBackgroundListener> applicationForegroundBackgroundListeners = new ArrayList<>();

  protected List<ApplicationTerminatesListener> applicationTerminatesListeners = new ArrayList<>();


  @Override
  public boolean addApplicationForegroundBackgroundListener(ApplicationForegroundBackgroundListener listener) {
    return applicationForegroundBackgroundListeners.add(listener);
  }

  @Override
  public boolean removeApplicationForegroundBackgroundListener(ApplicationForegroundBackgroundListener listener) {
    return applicationForegroundBackgroundListeners.remove(listener);
  }

  @Override
  public boolean addApplicationTerminatesListenerListener(ApplicationTerminatesListener listener) {
    return applicationTerminatesListeners.add(listener);
  }

  @Override
  public boolean removeApplicationTerminatesListenerListener(ApplicationTerminatesListener listener) {
    return applicationTerminatesListeners.remove(listener);
  }


  protected void applicationCameToForeground() {
    isApplicationInForeground = true;

    log.info("Application came to Foreground");

    callApplicationCameToForegroundListeners();
  }

  protected void applicationWentToBackground() {
    isApplicationInForeground = false;

    log.info("Application went to Background");

    callApplicationWentToBackgroundListeners();
  }

  protected void applicationIsGoingToTerminate() {
    log.info("Application is going to terminate");

    callApplicationIsGoingToTerminateListeners();
  }


  protected void callApplicationCameToForegroundListeners() {
    for(ApplicationForegroundBackgroundListener listener : applicationForegroundBackgroundListeners) {
      listener.applicationCameToForeground();
    }
  }

  protected void callApplicationWentToBackgroundListeners() {
    for(ApplicationForegroundBackgroundListener listener : applicationForegroundBackgroundListeners) {
      listener.applicationWentToBackground();
    }
  }

  protected void callApplicationIsGoingToTerminateListeners() {
    for(ApplicationTerminatesListener listener : applicationTerminatesListeners) {
      listener.applicationIsGoingToTerminate();
    }
  }


  @Override
  public boolean isApplicationInForeground() {
    return isApplicationInForeground;
  }

}
