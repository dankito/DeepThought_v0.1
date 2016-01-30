package net.deepthought.data.contentextractor.file;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.ContentExtractOption;
import net.deepthought.data.download.DownloadConfig;
import net.deepthought.data.download.DownloadListener;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.localization.Localization;
import net.deepthought.util.ObjectHolder;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RemoteFileContentExtractor extends FileContentExtractorBase {

  private static final Logger log = LoggerFactory.getLogger(RemoteFileContentExtractor.class);


  public String getName() {
    return Localization.getLocalizedString("remote.file.content.extractor");
  }


  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return Application.getDownloader().canDownloadUrl(url);
  }

  @Override
  protected String getAndMayAdjustUrlFromOption(ContentExtractOption option) {
    String remoteUrl = option.getUrl();
    File destinationFile = FileUtils.findUniqueFileNameInUserDataFolderForUrl(remoteUrl);

    final ObjectHolder<Boolean> downloadSuccessHolder = new ObjectHolder<>(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    downloadFile(remoteUrl, destinationFile, new DownloadFileResult() {
      @Override
      public void completed(boolean successful, File downloadedFile) {
        downloadSuccessHolder.set(successful);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(40, TimeUnit.SECONDS); } catch(Exception ex) { }

    if(downloadSuccessHolder.get() == true) {
      remoteUrl = destinationFile.getAbsolutePath();
    }

    return remoteUrl;
  }

  protected interface DownloadFileResult {
    void completed(boolean successful, File downloadedFile);
  }

  protected void downloadFile(final String url, final File destinationFile, final DownloadFileResult result) {
    Application.getDownloader().downloadAsync(new DownloadConfig(url, destinationFile.getAbsolutePath()), new DownloadListener() {
      @Override
      public void progress(DownloadConfig download, float percentage) {

      }

      @Override
      public void downloadCompleted(DownloadConfig download, boolean successful, DeepThoughtError error) {
        if(successful == false) {
          log.error("Could not download file " + url + ": " + error);
          // TODO: notify user
        }

        if(result != null)
          result.completed(successful, destinationFile);
      }
    });
  }

}
