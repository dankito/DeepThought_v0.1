package net.deepthought.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by ganymed on 17/08/15.
 */
public class AndroidPluginManager implements IPluginManager {

  public final static String TextContentExtractorPluginIntentName = "net.deepthought.plugin.TextContentExtractor";


  private final static Logger log = LoggerFactory.getLogger(AndroidPluginManager.class);


  protected Context context;


  public AndroidPluginManager(Context context) {
    this.context = context;
  }


  @Override
  public File getPluginsFolderFile() {
    return null; // not needed in Android
  }

  @Override
  public String getPluginsFolderPath() {
    return null; // not needed in Android
  }

  @Override
  public void loadPluginsAsync() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        loadPlugins();
      }
    }).start();
  }

  protected void loadPlugins() {
    PackageManager packageManager = context.getPackageManager();
    Intent textContentExtractorsIntent = new Intent(TextContentExtractorPluginIntentName);
    List<ResolveInfo> textContentExtractors = packageManager.queryIntentActivities(textContentExtractorsIntent, 0);

    for(ResolveInfo resolveInfo : textContentExtractors) {
      CharSequence label = resolveInfo.loadLabel(packageManager);
      if(label != null) {
//        if(plugin instanceof IContentExtractor)
//          Application.getContentExtractorManager().addContentExtractor((IContentExtractor)plugin);
//
//        Application.notifyUser(new Notification(NotificationType.PluginLoaded, Localization.getLocalizedString("plugin.loaded", plugin.getName()), plugin)); }
      }
    }
  }
}
