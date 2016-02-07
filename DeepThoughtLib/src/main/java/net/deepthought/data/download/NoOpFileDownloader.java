package net.deepthought.data.download;

/**
 * Created by ganymed on 26/04/15.
 */
public class NoOpFileDownloader implements IFileDownloader {

  @Override
  public boolean canDownloadUrl(String url) {
    return false;
  }

  @Override
  public void downloadAsync(DownloadConfig config, DownloadListener listener) {

  }

}
