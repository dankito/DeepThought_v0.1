package net.deepthought.data.contentextractor;

import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;
import com.github.axet.vget.vhs.VimeoInfo;
import com.github.axet.vget.vhs.YoutubeInfo;
import com.github.axet.wget.info.DownloadInfo;

import net.deepthought.Application;
import net.deepthought.data.download.DownloadConfig;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Created by ganymed on 26/04/15.
 */
public class YouTubeAndVimeoDownloader {

  protected VideoInfo info;


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
      URL url = new URL(config.getUrl());

      // [OPTIONAL] limit maximum quality, or do not call this function if
      // you wish maximum quality available.
      //
      // if youtube does not have video with requested quality, program
      // will raise en exception.
      VGetParser parser = null;

      // create proper html parser depends on url
      parser = VGet.parser(url);

      // download maximum video quality from youtube
      // parser = new YouTubeQParser(YoutubeQuality.p480);

      // download mp4 format only, fail if non exist
      // parser = new YouTubeMPGParser();

      // create proper videoinfo to keep specific video information
      info = parser.info(url);

      VGet v = new VGet(info, new File(config.getDestinationFileName()));

      Runnable notify = new DownloadThread(info);

      // [OPTIONAL] call v.extract() only if you d like to get video title
      // or download url link
      // before start download. or just skip it.
      v.extract(parser, config.getStop(), notify);

      System.out.println("Title: " + info.getTitle());
      System.out.println("Download URL: " + info.getInfo().getSource());

      v.download(parser, config.getStop(), notify);
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
            System.out.println(i1.getState() + " " + i.getVideoQuality());
          } else if (i1 instanceof VimeoInfo) {
            VimeoInfo i = (VimeoInfo) i1;
            System.out.println(i1.getState() + " " + i.getVideoQuality());
          } else {
            System.out.println("downloading unknown quality");
          }
          break;
        case RETRYING:
          System.out.println(i1.getState() + " " + i1.getDelay());
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

            System.out.println(String.format("%s %.2f %s", i1.getState(),
                i2.getCount() / (float) i2.getLength(), parts));
          }
          break;
        default:
          break;
      }
    }
  }
}
