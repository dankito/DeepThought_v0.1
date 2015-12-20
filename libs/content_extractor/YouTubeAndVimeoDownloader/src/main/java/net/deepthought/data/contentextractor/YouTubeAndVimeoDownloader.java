package net.deepthought.data.contentextractor;

import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;
import com.github.axet.vget.vhs.VimeoInfo;
import com.github.axet.vget.vhs.VimeoParser;
import com.github.axet.vget.vhs.YouTubeParser;
import com.github.axet.vget.vhs.YoutubeInfo;
import com.github.axet.wget.info.DownloadInfo;

import net.deepthought.data.contentextractor.model.AudioQuality;
import net.deepthought.data.contentextractor.model.AvailableFormat;
import net.deepthought.data.contentextractor.model.AvailableFormats;
import net.deepthought.data.contentextractor.model.Container;
import net.deepthought.data.contentextractor.model.Encoding;
import net.deepthought.data.contentextractor.model.VideoQuality;
import net.deepthought.data.download.DownloadConfig;
import net.deepthought.data.download.DownloadListener;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.ThreadHelper;

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


  public void downloadAsync(final DownloadConfig config, final DownloadListener listener) {
    ThreadHelper.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        download(config, listener);
      }
    });
  }

  protected void download(DownloadConfig config, DownloadListener listener) {
    try {
//      VideoInfo info = getVideoInfo(config.getUrl());
      URL url = new URL(config.getUrl());
      VGetParser parser = VGet.parser(url);
      VideoInfo info = parser.info(url);
//      VideoInfo info = new VideoInfo(url);

      VGet vget = new VGet(info, new File(config.getDestinationFileName()));

      Runnable notify = new DownloadThread(vget, info, config, listener);

//      // [OPTIONAL] call v.extract() only if you d like to get video title
//      // or download url link
//      // before start download. or just skip it.
//      vget.extract(parser, config.getStop(), notify);
//
//      log.debug("Title: " + info.getTitle());
//      log.debug("Download URL: " + info.getInfo().getSource());

      vget.download(parser, config.getStop(), notify);

//      Application.getDownloader().downloadAsync(config, listener);
    } catch (RuntimeException e) {
      listener.downloadCompleted(config, false, new DeepThoughtError(Localization.getLocalizedString("error.could.not.download.file", config.getUrl()), e));
    } catch (Exception e) {
      listener.downloadCompleted(config, false, new DeepThoughtError(Localization.getLocalizedString("error.could.not.download.file", config.getUrl()), e));
    }
  }

  protected class DownloadThread implements Runnable {

    protected VGet vget;

    protected VideoInfo info;

    protected DownloadConfig downloadConfig;

    protected DownloadListener listener;

    protected long last;


    public DownloadThread(VGet vget, VideoInfo info, DownloadConfig config, DownloadListener listener) {
      this.vget = vget;
      this.info = info;
      this.downloadConfig = config;
      this.listener = listener;
    }


    @Override
    public void run() {
      VideoInfo videoInfo = info;
      DownloadInfo downloadInfo = videoInfo.getInfo();

      // notify app or save download state
      // you can extract information from DownloadInfo info;
      switch (videoInfo.getState()) {
        case EXTRACTING:
        case EXTRACTING_DONE:
          if (videoInfo instanceof YoutubeInfo) {
            YoutubeInfo i = (YoutubeInfo) videoInfo;
            log.debug(videoInfo.getState() + " " + i.getVideoQuality());
          } else if (videoInfo instanceof VimeoInfo) {
            VimeoInfo i = (VimeoInfo) videoInfo;
            log.debug(videoInfo.getState() + " " + i.getVideoQuality());
          } else {
            log.debug("downloading unknown quality");
          }
          break;
        case RETRYING:
          log.debug(videoInfo.getState() + " " + videoInfo.getDelay());
          if(videoInfo.getDelay() == 0) {
            downloadConfig.stopDownload();
            listener.downloadCompleted(downloadConfig, false,
                new DeepThoughtError(Localization.getLocalizedString("error.could.not.download.file", info.getWeb().toExternalForm())));
          }
          break;
        case DOWNLOADING:
          long now = System.currentTimeMillis();
          if (now - 1000 > last) {
            last = now;

            String parts = "";

            List<DownloadInfo.Part> pp = downloadInfo.getParts();
            if (pp != null) {
              // multipart download
              for (DownloadInfo.Part p : pp) {
                if (p.getState().equals(DownloadInfo.Part.States.DOWNLOADING)) {
                  parts += String.format("Part#%d(%.2f) ", p.getNumber(), p.getCount()
                      / (float) p.getLength());
                }
              }
            }

            log.debug(String.format("%s %.2f %s", videoInfo.getState(),
                downloadInfo.getCount() / (float) downloadInfo.getLength(), parts));
          }
          break;
        case DONE:
          // TODO: can be removed (as well as vget field) as soon as we set the destination filename and not VGet
          downloadConfig.setDestinationFileName(vget.getTarget().getAbsolutePath());
          listener.downloadCompleted(downloadConfig, true, null);
          break;
        case ERROR:
          listener.downloadCompleted(downloadConfig, false,
              new DeepThoughtError(Localization.getLocalizedString("error.could.not.download.file", info.getWeb().toExternalForm())));
          break;
        default:
          break;
      }
    }
  }
}
