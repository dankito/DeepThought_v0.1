package net.deepthought.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ganymed on 26/09/15.
 */
public class IconManager {

  private final static Logger log = LoggerFactory.getLogger(IconManager.class);


  protected static IconManager instance = null;

  public static IconManager getInstance() {
    if(instance == null)
      instance = new IconManager();
    return instance;
  }


  public void setImageViewToImageFromUrl(ImageView imageView, String url) {
    imageView.setImageBitmap(null);
    imageView.setTag(url);

    if(StringUtils.isNotNullOrEmpty(url))
      new DownloadImagesTask(imageView, url).execute();
  }

  public Bitmap getImageFromUrl(String urlString) {
    try {
      URL url = new URL(urlString);
      URLConnection connection = url.openConnection();
      connection.connect();

      InputStream inputStream = connection.getInputStream();
      return getImageFromStream(inputStream);
    } catch (IOException ex) {
      log.error("Could not get Bitmap from Url " + urlString, ex);
    }

    return null;
  }

  public Bitmap getImageFromStream(InputStream is) {
    Bitmap bm = null;
    try {
      BufferedInputStream bis = new BufferedInputStream(is);
      bm = BitmapFactory.decodeStream(bis);
      bis.close();
      is.close();
    } catch (IOException ex) {
      log.error("Could not get Bitmap from InputStream", ex);
    }
    return bm;
  }

  public class DownloadImagesTask extends AsyncTask<Void, Void, Bitmap> {

    protected ImageView imageView = null;

    protected String url;


    public DownloadImagesTask(ImageView imageView, String url) {
      this.imageView = imageView;
      this.url = url;
    }


    @Override
    protected Bitmap doInBackground(Void... params) {
      return IconManager.getInstance().getImageFromUrl(url);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      if(url.equals(imageView.getTag())) // if imageView's Tag doesn't equal url anymore, imageView has been set to another Url in the meantime
        imageView.setImageBitmap(result);
    }

  }

}
