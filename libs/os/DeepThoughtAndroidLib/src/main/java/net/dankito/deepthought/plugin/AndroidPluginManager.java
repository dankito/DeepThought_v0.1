package net.dankito.deepthought.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.plugin.ocr.Constants;
import net.dankito.deepthought.plugin.ocr.OcrContentExtractorAndroid;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Created by ganymed on 17/08/15.
 */
public class AndroidPluginManager extends DefaultPluginManager {


  private final static Logger log = LoggerFactory.getLogger(AndroidPluginManager.class);


  protected Context context;


  public AndroidPluginManager(Context context) {
    this.context = context;
  }


  @Override
  protected String getPluginsFileExtension() {
    return "apk";
  }

  @Override
  protected ClassLoader createClassLoaderForPluginFile(URL url) {
    try {
      DexFile dexFile = new DexFile(new File(url.toURI()));
//      return new DexClassLoader()
      return new dalvik.system.PathClassLoader(
          new File(url.toURI()).getAbsolutePath(),
          ClassLoader.getSystemClassLoader());
    } catch(Exception ex) { log.error("Could not create ClassLoader for file " + url, ex); }

    return null;
  }

  protected void loadPlugins(Collection<IPlugin> staticallyLinkedPlugins) {
    checkIfOcrContentExtractorPluginIsInstalled();
    super.loadPlugins(staticallyLinkedPlugins);
  }

  @Override
  protected void loadPluginsFromPluginsFolder() {
    // don't try to load Plugins from plugins folder in Android
    // does not work (yet) as Android cannot load .class files and no .dex version is implemented yet
  }

  protected void checkIfOcrContentExtractorPluginIsInstalled() {
    try {
      List<ResolveInfo> textContentExtractors = findTextFairyApp();

      for (ResolveInfo resolveInfo : textContentExtractors) {
        if(resolveInfo.activityInfo != null) {
          OcrContentExtractorAndroid plugin = new OcrContentExtractorAndroid(context, resolveInfo);
          Application.getContentExtractorManager().addContentExtractor(plugin);

          Application.notifyUser(new Notification(NotificationType.PluginLoaded, Localization.getLocalizedString("plugin.loaded", plugin.getName()), plugin));
        }
      }
    } catch(Exception ex) { log.error("Could not load OcrContentExtractor plugins", ex); }
  }

  protected List<ResolveInfo> findTextFairyApp() {
    PackageManager packageManager = context.getPackageManager();
    Intent textContentExtractorsIntent = new Intent(Constants.TEXT_CONTENT_EXTRACTOR_PLUGIN_INTENT_NAME);

    List<ResolveInfo> textContentExtractors = packageManager.queryIntentActivities(textContentExtractorsIntent, 0);
    log.info("Found " + textContentExtractors.size() + " OcrContentExtractor plugins");

    return textContentExtractors;
  }
}
