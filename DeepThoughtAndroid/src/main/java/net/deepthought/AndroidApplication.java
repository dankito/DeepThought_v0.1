package net.deepthought;

import net.deepthought.platform.AndroidApplicationConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 18/08/15.
 */
public class AndroidApplication extends android.app.Application {

  private final static Logger log = LoggerFactory.getLogger(AndroidApplication.class);


  @Override
  public void onCreate() {
    super.onCreate();

    instantiateDeepThought();
  }

  protected void instantiateDeepThought() {
    Application.instantiateAsync(new AndroidApplicationConfiguration(this));
  }

  @Override
  public void onTerminate() {
    Application.shutdown();
    super.onTerminate();
  }
}
