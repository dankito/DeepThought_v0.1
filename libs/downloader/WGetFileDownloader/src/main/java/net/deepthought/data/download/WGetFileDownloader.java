package net.deepthought.data.download;

import com.github.axet.wget.WGet;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.DownloadInfo.Part;
import com.github.axet.wget.info.ex.DownloadMultipartError;

import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * Created by ganymed on 26/04/15.
 */
public class WGetFileDownloader implements IFileDownloader {

  private final static Logger log = LoggerFactory.getLogger(WGetFileDownloader.class);


  @Override
  public boolean canDownloadUrl(String url) {
    return FileUtils.isRemoteFile(url);
  }


  public void downloadAsync(final DownloadConfig config, final DownloadListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        download(config, listener);
      }
    }).start();
  }

  protected void download(DownloadConfig config, DownloadListener listener) {
    try {
      // choise file
      URL url = new URL(config.getUrl());
      // initialize url information object
      DownloadInfo info = new DownloadInfo(url);

      Runnable notify = new DownloadThread(info);
      // extract infromation from the web
      info.extract(config.getStop(), notify);
      // enable multipart donwload
      try {
        if(info.getRange())
          info.enableMultipart();
      } catch(Exception ex) { }
      // Choise target file
      File target = new File(config.getDestinationFileName());
      // create wget downloader
      WGet w = new WGet(info, target);
      // will blocks until download finishes
      w.download(config.getStop(), notify);

      if(listener != null)
        listener.downloadCompleted(config, true, null);
    } catch (DownloadMultipartError e) {
      log.error("Could not download " + config.getUrl(), e);
      if(e.getInfo().getParts() != null) {
        for (Part p : e.getInfo().getParts()) {
          Throwable ee = p.getException();
          if (ee != null)
            ee.printStackTrace();
        }
      }
    } catch (RuntimeException e) {
      log.error("Could not download " + config.getUrl(), e);
      throw e;
    } catch (Exception e) {
      log.error("Could not download " + config.getUrl(), e);
      throw new RuntimeException(e);
    }
  }

  protected class DownloadThread implements Runnable {

    protected DownloadInfo info;

    protected long last;


    public DownloadThread(DownloadInfo info) {
      this.info = info;
    }


    @Override
    public void run() {
      // notify app or save download state
      // you can extract information from DownloadInfo info;
      switch (info.getState()) {
        case EXTRACTING:
        case EXTRACTING_DONE:
        case DONE:
          log.debug("" + info.getState());
          break;
        case RETRYING:
          log.debug(info.getState() + " " + info.getDelay());
          break;
        case DOWNLOADING:
          long now = System.currentTimeMillis();
          if (now - 1000 > last) {
            last = now;

            String parts = "";

            if(info.getParts() != null) {
              for (Part p : info.getParts()) {
                if (p.getState().equals(Part.States.DOWNLOADING)) {
                  parts += String.format("Part#%d(%.2f) ", p.getNumber(),
                      p.getCount() / (float) p.getLength());
                }
              }
            }

            log.debug(String.format("%.2f %s", info.getCount() / (float) info.getLength(), parts));
          }
          break;
        default:
          break;
      }
    }
  }
}
