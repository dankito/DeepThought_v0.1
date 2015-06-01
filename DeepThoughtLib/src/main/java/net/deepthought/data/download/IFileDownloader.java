package net.deepthought.data.download;

/**
 * Created by ganymed on 26/04/15.
 */
public interface IFileDownloader {

  public boolean canDownloadUrl(String url);

  public void downloadAsync(final DownloadConfig config, final DownloadListener listener);

}
