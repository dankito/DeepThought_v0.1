package net.dankito.deepthought.data.download;

/**
 * Created by ganymed on 26/04/15.
 */
public interface IFileDownloader {

  boolean canDownloadUrl(String url);

  void downloadAsync(final DownloadConfig config, final DownloadListener listener);

}
