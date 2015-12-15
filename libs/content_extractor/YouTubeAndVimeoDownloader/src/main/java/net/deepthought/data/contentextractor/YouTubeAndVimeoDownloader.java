package net.deepthought.data.contentextractor;

import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;
import com.github.axet.vget.vhs.VimeoInfo;
import com.github.axet.vget.vhs.VimeoParser;
import com.github.axet.vget.vhs.YouTubeParser;
import com.github.axet.vget.vhs.YoutubeInfo;
import com.github.axet.wget.info.DownloadInfo;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.model.AudioQuality;
import net.deepthought.data.contentextractor.model.AvailableFormat;
import net.deepthought.data.contentextractor.model.AvailableFormats;
import net.deepthought.data.contentextractor.model.Container;
import net.deepthought.data.contentextractor.model.Encoding;
import net.deepthought.data.contentextractor.model.VideoQuality;
import net.deepthought.data.download.DownloadConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 26/04/15.
 */
public class YouTubeAndVimeoDownloader {

  public static final AvailableFormats CouldNotRetrieveAvailableFormats = new AvailableFormats();


  private static final Logger log = LoggerFactory.getLogger(YouTubeAndVimeoDownloader.class);



  public AvailableFormats getAvailableFormats(String urlString) {
    try {
      URL url = new URL(urlString);

      // [OPTIONAL] limit maximum quality, or do not call this function if
      // you wish maximum quality available.
      //
      // if youtube does not have video with requested quality, program
      // will raise en exception.
      VGetParser parser = VGet.parser(url);

      // download maximum video quality from youtube
      // parser = new YouTubeQParser(YoutubeQuality.p480);

      // download mp4 format only, fail if non exist
      // parser = new YouTubeMPGParser();

      // create proper videoinfo to keep specific video information
//    return parser.info(url);

      VideoInfo info = parser.info(url);

      return getAvailableFormatsFromParser(parser, info);
    } catch(Exception ex) {
      log.error("Could not retrieved available video formats for url " + urlString, ex);
    }

    return CouldNotRetrieveAvailableFormats;
  }

  protected AvailableFormats getAvailableFormatsFromParser(VGetParser parser, VideoInfo info) {
    if(parser instanceof YouTubeParser) {
      return getAvailableFormatsFromYouTubeParser((YouTubeParser) parser, info);
    }
    else if(parser instanceof VimeoParser) {
      return getAvailableFormatsFromVimeoParser((VimeoParser) parser, info);
    }

    return CouldNotRetrieveAvailableFormats;
  }

  protected AvailableFormats getAvailableFormatsFromYouTubeParser(YouTubeParser parser, VideoInfo info) {
    // code copied from YouTubeParser.extract(VideoInfo vinfo, AtomicBoolean stop, Runnable notify)
    List<YouTubeParser.VideoDownload> urls = parser.extractLinks(info);

    AvailableFormats availableFormats = new AvailableFormats(info.getWeb().toExternalForm(), info.getTitle(), info.getIcon().toExternalForm());

    addAvailableFormatsFromYouTubeVideoDownloadUrls(availableFormats, info, urls);

    return availableFormats;
  }

  protected void addAvailableFormatsFromYouTubeVideoDownloadUrls(AvailableFormats availableFormats, VideoInfo info, List<YouTubeParser.VideoDownload> urls) {
    reduceAndSortYouTubeVideoDownloadUrls(urls);

    for (int i = 0; i < urls.size(); i++) {
      YouTubeParser.VideoDownload download = urls.get(i);

      YoutubeInfo.StreamCombined stream = (YoutubeInfo.StreamCombined)download.stream;
      availableFormats.addAvailableFormat(createAvailableFormatFromYouTubeStream(download.url.toExternalForm(), stream));

//      YoutubeInfo yinfo = (YoutubeInfo) info;
//      yinfo.setStreamInfo(download.stream);
//      DownloadInfo downloadInfo = new DownloadInfo(download.url);
//      info.setInfo(downloadInfo);
    }
  }

  protected AvailableFormat createAvailableFormatFromYouTubeStream(String url, YoutubeInfo.StreamCombined stream) {
    return new AvailableFormat(url, Container.valueOf(stream.c.toString()), Encoding.valueOf(stream.video.toString()), Encoding.valueOf(stream.audio.toString()),
        VideoQuality.valueOf(stream.vq.toString()), AudioQuality.valueOf(stream.aq.toString()));
  }

  protected void reduceAndSortYouTubeVideoDownloadUrls(List<YouTubeParser.VideoDownload> urls) {
    for (int i = urls.size() - 1; i > 0; i--) {
      if (!(urls.get(i).stream instanceof YoutubeInfo.StreamCombined)) {
        urls.remove(i);
      }
    }

    Collections.sort(urls, new YouTubeParser.VideoContentFirst());
  }

  protected AvailableFormats getAvailableFormatsFromVimeoParser(VimeoParser parser, VideoInfo info) {
    List<VimeoParser.VideoDownload> urls = getSortedVimeoVideoDownloadUrls(parser, info);

    AvailableFormats availableFormats = new AvailableFormats(info.getWeb().toExternalForm(), info.getTitle(), info.getIcon().toExternalForm());

    addAvailableFormatsFromVimeoVideoDownloadUrls(availableFormats, info, urls);

    return availableFormats;
  }

  protected void addAvailableFormatsFromVimeoVideoDownloadUrls(AvailableFormats availableFormats, VideoInfo info, List<VimeoParser.VideoDownload> urls) {
    for (int i = 0; i < urls.size(); i++) {
      VimeoParser.VideoDownload download = urls.get(i);

      availableFormats.addAvailableFormat(new AvailableFormat(download.url.toExternalForm(), download.vq.toString().replace("p", "")));

//      VimeoInfo yinfo = (VimeoInfo) info;
//      yinfo.setVideoQuality(download.vq);
//      DownloadInfo downloadInfo = new DownloadInfo(download.url);
//      info.setInfo(downloadInfo);
//      return downloadInfo;
    }
  }

  protected List<VimeoParser.VideoDownload> getSortedVimeoVideoDownloadUrls(VimeoParser parser, VideoInfo info) {
    // code copied from VimeoParser.extract(VideoInfo vinfo, AtomicBoolean stop, Runnable notify)
    List<VimeoParser.VideoDownload> urls = parser.extractLinks(info, new AtomicBoolean(), new Runnable() {
      @Override
      public void run() {

      }
    });

    Collections.sort(urls, new VimeoParser.VideoContentFirst());
    return urls;
  }


  public void downloadAsync(final DownloadConfig config, final Object listener) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        download(config, listener);
      }
    });
  }

  protected void download(DownloadConfig config, Object listener) {
    try {
//      VideoInfo info = getVideoInfo(config.getUrl());
      URL url = new URL(config.getUrl());
      VGetParser parser = VGet.parser(url);
      VideoInfo info = parser.info(url);

      VGet vget = new VGet(info, new File(config.getDestinationFileName()));

      Runnable notify = new DownloadThread(info);

      // [OPTIONAL] call v.extract() only if you d like to get video title
      // or download url link
      // before start download. or just skip it.
      vget.extract(parser, config.getStop(), notify);

      log.debug("Title: " + info.getTitle());
      log.debug("Download URL: " + info.getInfo().getSource());

      vget.download(parser, config.getStop(), notify);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected class DownloadThread implements Runnable {

    protected VideoInfo info;

    protected long last;


    public DownloadThread(VideoInfo info) {
      this.info = info;
    }


    @Override
    public void run() {
      VideoInfo i1 = info;
      DownloadInfo i2 = i1.getInfo();

      // notify app or save download state
      // you can extract information from DownloadInfo info;
      switch (i1.getState()) {
        case EXTRACTING:
        case EXTRACTING_DONE:
        case DONE:
          if (i1 instanceof YoutubeInfo) {
            YoutubeInfo i = (YoutubeInfo) i1;
            log.debug(i1.getState() + " " + i.getVideoQuality());
          } else if (i1 instanceof VimeoInfo) {
            VimeoInfo i = (VimeoInfo) i1;
            log.debug(i1.getState() + " " + i.getVideoQuality());
          } else {
            log.debug("downloading unknown quality");
          }
          break;
        case RETRYING:
          log.debug(i1.getState() + " " + i1.getDelay());
          break;
        case DOWNLOADING:
          long now = System.currentTimeMillis();
          if (now - 1000 > last) {
            last = now;

            String parts = "";

            List<DownloadInfo.Part> pp = i2.getParts();
            if (pp != null) {
              // multipart download
              for (DownloadInfo.Part p : pp) {
                if (p.getState().equals(DownloadInfo.Part.States.DOWNLOADING)) {
                  parts += String.format("Part#%d(%.2f) ", p.getNumber(), p.getCount()
                      / (float) p.getLength());
                }
              }
            }

            log.debug(String.format("%s %.2f %s", i1.getState(),
                i2.getCount() / (float) i2.getLength(), parts));
          }
          break;
        default:
          break;
      }
    }
  }
}
