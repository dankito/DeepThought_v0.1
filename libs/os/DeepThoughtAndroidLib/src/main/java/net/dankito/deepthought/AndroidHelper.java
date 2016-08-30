package net.dankito.deepthought;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;

import net.dankito.deepthought.android.lib.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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


  public static Uri takePhoto(Activity activity, int requestCode) {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
      // Create the File where the photo should go
      Uri tempPhotoUri = createCapturedImageTempFile();

      if (tempPhotoUri != null) {
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);
        activity.startActivityForResult(takePictureIntent, requestCode);
        return tempPhotoUri;
      }
    }

    return null;
  }

  protected static Uri createCapturedImageTempFile() {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";

    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    try {
      if (!storageDir.exists()) {
        storageDir.mkdirs();
      }

      File image = new File(storageDir, imageFileName + ".jpg");
      if (image.exists()) {
        image.createNewFile();
      }

      return Uri.fromFile(image);
    } catch (Exception ex) { // TODO: pass this error back to caller application
      log.error("Could not create a temp file for capturing an Image", ex);
    }

    return null;
  }


  public static void selectImagesFromGallery(Activity context, int requestCode) {
    Intent i = new Intent(Intent.ACTION_GET_CONTENT, null);

    // TODO: set Files types either to Html compatible types or that ones in request parameter
    if (Build.VERSION.SDK_INT >= 19) {
      i.setType("image/*");
      i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/png", "image/jpg", "image/jpeg"});
    } else {
      i.setType("image/png,image/jpg, image/jpeg");
    }

    Intent chooser = Intent.createChooser(i, context.getString(R.string.image_source));
    context.startActivityForResult(chooser, requestCode);
  }

  public static byte[] getImageBytesFromIntent(Context context, Intent data) throws Exception {
    String uri = data.getDataString();

    InputStream selectedFileStream = context.getContentResolver().openInputStream(data.getData());
    return readDataFromInputStream(selectedFileStream);
  }

  protected static byte[] readDataFromInputStream(InputStream inputStream) throws Exception{
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    return buffer.toByteArray();
  }

}
