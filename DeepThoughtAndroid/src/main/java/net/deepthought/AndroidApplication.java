package net.deepthought;

import net.deepthought.platform.AndroidApplicationConfiguration;

/**
 * Created by ganymed on 18/08/15.
 */
public class AndroidApplication extends android.app.Application {


  // instantiate and shutdown DeepThought application here and android.app.Application is created and destroyed only once while MainActivity (may) multiple times

  @Override
  public void onCreate() {
    super.onCreate();

    Application.instantiateAsync(new AndroidApplicationConfiguration(this));
  }


  @Override
  public void onTerminate() {
    Application.shutdown();
    super.onTerminate();
  }

}
