package net.deepthought;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;

import net.deepthought.data.model.FileLink;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by ganymed on 26/09/15.
 */
public class AndroidHelper {

  private final static Logger log = LoggerFactory.getLogger(AndroidHelper.class);


  public static boolean isRunningOnUiThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

  public static boolean hasPermission(Context context, String featureNameFromPackageManagerClass) {
    try {
      PackageManager pm = context.getPackageManager();
      return pm.hasSystemFeature(featureNameFromPackageManagerClass);
    } catch(Exception ex) { }

    return false;
  }


  public static FileLink takePhoto(Activity activity, int requestCode) {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
      // Create the File where the photo should go
      FileLink photoFile = FileUtils.createCapturedImagesTempFile();

      if (photoFile != null) {
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoFile.getUriString())));
        activity.startActivityForResult(takePictureIntent, requestCode);
        return photoFile;
      }
    }

    return null;
  }

}
