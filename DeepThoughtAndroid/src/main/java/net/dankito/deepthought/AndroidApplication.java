package net.dankito.deepthought;

import net.dankito.deepthought.application.AndroidApplicationLifeCycleService;
import net.dankito.deepthought.controls.html.AndroidHtmlEditorPool;
import net.dankito.deepthought.platform.AndroidApplicationConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 18/08/15.
 */
public class AndroidApplication extends android.app.Application {

  private static final Logger log = LoggerFactory.getLogger(AndroidApplication.class);


  // instantiate and shutdown DeepThought application here and android.app.Application is created and destroyed only once while MainActivity (may) multiple times

  @Override
  public void onCreate() {
    super.onCreate();

    AndroidApplicationLifeCycleService lifeCycleService = new AndroidApplicationLifeCycleService();
    registerActivityLifecycleCallbacks(lifeCycleService);

    Application.instantiateAsync(new AndroidApplicationConfiguration(this, lifeCycleService));
  }


  @Override
  public void onTerminate() {
    Application.shutdown();
    AndroidHtmlEditorPool.getInstance().cleanUp();

    super.onTerminate();
  }

  @Override
  public void onLowMemory() {
    log.error("onLowMemory has been called");

    super.onLowMemory();
  }

  @Override
  public void onTrimMemory(int level) {
    log.error("onTrimMemory() called with level " + level + " (Complete = " + TRIM_MEMORY_COMPLETE + ", Background = " + TRIM_MEMORY_BACKGROUND + ", UI Hidden = " +
        TRIM_MEMORY_UI_HIDDEN + ")");

    super.onTrimMemory(level);
  }

}
