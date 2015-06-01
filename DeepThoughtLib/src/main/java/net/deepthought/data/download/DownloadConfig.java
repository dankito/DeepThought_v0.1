package net.deepthought.data.download;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 26/04/15.
 */
public class DownloadConfig {

  protected String url;

  protected String destinationFileName;

  protected AtomicBoolean stop = new AtomicBoolean(false);


  public DownloadConfig(String url, String destinationFileName) {
    this.url = url;
    this.destinationFileName = destinationFileName;
  }


  public String getUrl() {
    return url;
  }

  public String getDestinationFileName() {
    return destinationFileName;
  }

  public AtomicBoolean getStop() {
    return stop;
  }

  public boolean isStopped() {
    return stop.get();
  }

  public void stopDownload() {
    stop.set(true);
  }


  @Override
  public String toString() {
    return "Downloading " + url + " to " + destinationFileName;
  }

}
