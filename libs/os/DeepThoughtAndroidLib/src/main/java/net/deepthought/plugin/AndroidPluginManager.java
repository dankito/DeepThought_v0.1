package net.deepthought.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import net.deepthought.Application;
import net.deepthought.plugin.ocr.OcrContentExtractorAndroid;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;
import net.deepthought.util.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 17/08/15.
 */
public class AndroidPluginManager extends DefaultPluginManager {

  public final static String TextContentExtractorPluginIntentName = "com.renard.plugin.TextFairyPlugin";


  private final static Logger log = LoggerFactory.getLogger(AndroidPluginManager.class);


  protected Context context;


  public AndroidPluginManager(Context context) {
    this.context = context;
  }


  protected void loadPlugins(Collection<IPlugin> staticallyLinkedPlugins) {
    checkIfOcrContentExtractorPluginIsInstalled();
    super.loadPlugins(staticallyLinkedPlugins);
  }

  @Override
  protected void loadPluginsFromPluginsFolder() {
    // simply don't do it, ServiceLoader crashes Android Application
  }

  protected void checkIfOcrContentExtractorPluginIsInstalled() {
    try {
      PackageManager packageManager = context.getPackageManager();
      Intent textContentExtractorsIntent = new Intent(TextContentExtractorPluginIntentName);
      List<ResolveInfo> textContentExtractors = packageManager.queryIntentActivities(textContentExtractorsIntent, 0);
      log.info("Found " + textContentExtractors.size() + " OcrContentExtractor plugins");

      for (ResolveInfo resolveInfo : textContentExtractors) {
        if(resolveInfo.activityInfo != null) {
          OcrContentExtractorAndroid plugin = new OcrContentExtractorAndroid(context, resolveInfo);
          Application.getContentExtractorManager().addContentExtractor(plugin);

          Application.notifyUser(new Notification(NotificationType.PluginLoaded, Localization.getLocalizedString("plugin.loaded", plugin.getName()), plugin));
        }
      }
    } catch(Exception ex) { log.error("Could not load OcrContentExtractor plugins", ex); }
  }
}
