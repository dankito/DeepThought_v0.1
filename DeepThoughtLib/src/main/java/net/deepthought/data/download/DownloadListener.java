package net.deepthought.data.download;

import net.deepthought.util.DeepThoughtError;

/**
 * Created by ganymed on 26/04/15.
 */
public interface DownloadListener {

  void progress(DownloadConfig download, float percentage);

  void downloadCompleted(DownloadConfig download, boolean successful, DeepThoughtError error);

}
