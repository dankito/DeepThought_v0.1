package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.model.AvailableFormats;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ganymed on 26/04/15.
 */
public class YouTubeAndVimeoDownloaderTest {

  protected YouTubeAndVimeoDownloader downloader;

  @Before
  public void setup() {
    downloader = new YouTubeAndVimeoDownloader();
  }


  @Test
  public void getContentExtractOptionsForRedHotChiliPeppers_UnderTheBridge() {
    String url = "https://www.youtube.com/watch?v=lwlogyj7nFE";
    AvailableFormats formats = downloader.getAvailableFormats(url);

    Assert.assertEquals(url, formats.getUrl());
    Assert.assertEquals("Red Hot Chili Peppers - Under The Bridge (Official Music Video)", formats.getTitle());
    Assert.assertEquals("https://i.ytimg.com/vi/lwlogyj7nFE/maxresdefault.jpg", formats.getPreviewImageUrl());
    Assert.assertTrue(formats.getFormats().size() == 4 || formats.getFormats().size() == 5); // sometimes it's 4, sometimes it's 5
  }

  @Test
  public void getContentExtractOptionsForEdwardSharpeAndTheMagneticZeros_Home() {
    // does not work as in VimeoParser.extractLinks(final VideoInfo info, final AtomicBoolean stop, final Runnable notify)
    // from Parameter url Variable clip gets set which is wrong (clip url does not exist anymore)
    String url = "https://vimeo.com/32863936";
    AvailableFormats formats = downloader.getAvailableFormats(url);

    Assert.assertEquals(url, formats.getUrl());
    Assert.assertEquals(2, formats.getFormats().size());
  }

}
