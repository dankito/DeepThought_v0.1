package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.TestApplicationConfiguration;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.ObjectHolder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 20/12/15.
 */
public class YouTubeAndVimeoContentExtractorTest {

  protected YouTubeAndVimeoContentExtractor contentExtractor;


  @Before
  public void setup() {
    Application.instantiate(new TestApplicationConfiguration());

    contentExtractor = new YouTubeAndVimeoContentExtractor();
  }


  @Test
  public void passYouTubeHomePageUrl_canCreateEntryFromUrlReturnsFalse() {
    Assert.assertFalse(contentExtractor.canCreateEntryFromUrl("https://www.youtube.com/"));
  }

  @Test
  public void passYouTubeWatchVideoUrl_canCreateEntryFromUrlReturnsTrue() {
    Assert.assertTrue(contentExtractor.canCreateEntryFromUrl("https://www.youtube.com/watch?v=lwlogyj7nFE"));
  }

  @Test
  public void passYouTubeVUrlWithoutVideoId_canCreateEntryFromUrlReturnsFalse() {
    Assert.assertFalse(contentExtractor.canCreateEntryFromUrl("https://www.youtube.com/v/"));
  }

  @Test
  public void passYouTubeVVideoUrl_canCreateEntryFromUrlReturnsTrue() {
    Assert.assertTrue(contentExtractor.canCreateEntryFromUrl("https://www.youtube.com/v/lwlogyj7nFE"));
  }

  @Test
  public void passYouTu_BeHomePage_canCreateEntryFromUrlReturnsFalse() {
    Assert.assertFalse(contentExtractor.canCreateEntryFromUrl("https://www.youtu.be/"));
  }

  @Test
  public void passYouTu_BeVideoUrl_canCreateEntryFromUrlReturnsTrue() {
    Assert.assertTrue(contentExtractor.canCreateEntryFromUrl("https://www.youtu.be/lwlogyj7nFE"));
  }

  @Test
  public void passVimeoVideoUrl_canCreateEntryFromUrlReturnsFalseYet() {
    Assert.assertFalse(contentExtractor.canCreateEntryFromUrl("https://vimeo.com/32863936"));
  }


  @Test
  public void createExtractOptionsForUrlForWatchUrl() {
    String url = "https://www.youtube.com/watch?v=lwlogyj7nFE";
    ContentExtractOptions options = contentExtractor.createExtractOptionsForUrl(url);

    Assert.assertEquals(url, options.getUrl());
    Assert.assertTrue(options.getContentExtractOptionsSize() == 4 || options.getContentExtractOptionsSize() == 5);
  }

  @Test
  public void createExtractOptionsForUrlForVUrl() {
    String url = "https://www.youtube.com/v/lwlogyj7nFE";
    ContentExtractOptions options = contentExtractor.createExtractOptionsForUrl(url);

    Assert.assertEquals(url, options.getUrl());
    Assert.assertTrue(options.getContentExtractOptionsSize() == 4 || options.getContentExtractOptionsSize() == 5);
  }

  @Test
  public void createExtractOptionsForUrlForYouTu_BeUrl() {
    String url = "https://www.youtu.be/lwlogyj7nFE";
    ContentExtractOptions options = contentExtractor.createExtractOptionsForUrl(url);

    Assert.assertEquals(url, options.getUrl());
    Assert.assertTrue(options.getContentExtractOptionsSize() == 4 || options.getContentExtractOptionsSize() == 5);
  }


  @Test
  public void downloadBestOption_DownloadedFileSizeIsCorrect() {
    ContentExtractOptions options = contentExtractor.createExtractOptionsForUrl("https://www.youtube.com/watch?v=uoq6_2xnQeY");

    final ObjectHolder<EntryCreationResult> createdEntryHolder = new ObjectHolder<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

    ContentExtractOption bestOption = options.getContentExtractOptions().get(0);
    bestOption.runAction(new ExtractContentActionResultListener() {
      @Override
      public void extractingContentDone(EntryCreationResult result) {
        createdEntryHolder.set(result);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(30, TimeUnit.SECONDS); } catch(Exception ex) { }

    EntryCreationResult result = createdEntryHolder.get();
    Assert.assertNotNull(result);
    Assert.assertTrue(result.successful());
    Assert.assertEquals(1, result.getAttachedFiles().size());

    FileLink downloadedFile = result.getAttachedFiles().get(0);
    File downloadedFileOnHardDisk = new File(downloadedFile.getUriString());
    long size = downloadedFileOnHardDisk.length();
    downloadedFileOnHardDisk.delete();
    Assert.assertEquals(14709963, size);
  }


  @Test
  public void downloadFileWhichCannotBeDownloadedDueToProhibitedException_DownloadedFileSizeIsCorrect() {
    // TODO: don't know why but Red Hot Chili Peppers - Under the Bridge cannot be downloaded
    ContentExtractOptions options = contentExtractor.createExtractOptionsForUrl("https://www.youtube.com/watch?v=lwlogyj7nFE");

    final ObjectHolder<EntryCreationResult> createdEntryHolder = new ObjectHolder<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

    ContentExtractOption bestOption = options.getContentExtractOptions().get(0);
    bestOption.runAction(new ExtractContentActionResultListener() {
      @Override
      public void extractingContentDone(EntryCreationResult result) {
        createdEntryHolder.set(result);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(20, TimeUnit.SECONDS); } catch(Exception ex) { }

    EntryCreationResult result = createdEntryHolder.get();
    Assert.assertNotNull(result);
    Assert.assertFalse(result.successful());
  }

}
