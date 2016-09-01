package net.dankito.deepthought.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import net.dankito.deepthought.application.ApplicationLifeCycleServiceBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thanks to Cornstalks' Stackoverflow Answer from which the main idea of this Code is taken from:
 * https://stackoverflow.com/a/13809991
 *
 * Created by ganymed on 01/09/16.
 */
public class AndroidApplicationLifeCycleService extends ApplicationLifeCycleServiceBase implements Application.ActivityLifecycleCallbacks {

  private static final Logger log = LoggerFactory.getLogger(AndroidApplicationLifeCycleService.class);


  protected int resumed = 0;
  protected int paused = 0;
  protected int started = 0;
  protected int stopped = 0;


  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    log.info("Activity " + activity.getLocalClassName() + " has been created");
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
    // onDestroyed is not 100 % reliable:
    // "When user close the app, the process is terminated with no notice. onDestroy is not guaranteed to be called. only when you explicitly call finish()."
    // (from: https://stackoverflow.com/questions/23534046/app-closing-event-in-android)
    log.info("Activity " + activity.getLocalClassName() + " has been destroyed");
  }

  @Override
  public void onActivityResumed(Activity activity) {
    ++resumed;

    log.info("Activity " + activity.getLocalClassName() + " has been resumed");
  }

  @Override
  public void onActivityPaused(Activity activity) {
    ++paused;

    log.info("Activity " + activity.getLocalClassName() + " has been paused");
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override
  public void onActivityStarted(Activity activity) {
    ++started;

    log.info("Activity " + activity.getLocalClassName() + " has been started");

    if(isApplicationInForeground() == false && started > stopped) {
      applicationCameToForeground();
    }
  }

  @Override
  public void onActivityStopped(Activity activity) {
    ++stopped;

    log.info("Activity " + activity.getLocalClassName() + " has been stopped");

    if(isApplicationInForeground() && started <= stopped) {
      applicationWentToBackground();
    }
  }

  // sometimes MainActivity's onDestroy() method is called but not ActivityLifecycleCallbacks' one
  public void mainActivityIsGoingToBeDestroyed() {
    applicationIsGoingToTerminate();
  }

}
