package net.dankito.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.model.AvailableFormat;
import net.deepthought.data.contentextractor.model.AvailableFormats;
import net.deepthought.data.download.DownloadConfig;
import net.deepthought.data.download.DownloadListener;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.enums.FileType;
import net.deepthought.plugin.IPlugin;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.file.FileUtils;

public class YouTubeAndVimeoContentExtractor implements IContentExtractor, IPlugin {

  protected YouTubeAndVimeoDownloader downloader = new YouTubeAndVimeoDownloader();


  @Override
  public int getSupportedPluginSystemVersion() {
    return 1;
  }

  @Override
  public String getPluginVersion() {
    return "0.1";
  }

  @Override
  public String getName() {
    return "YouTubeAndVimeoContentExtractor"; // TODO
  }


  @Override
  public boolean canCreateEntryFromUrl(String url) {
    try {
//      URL parsedUrl = new URL(url);
//      return YouTubeParser.probe(parsedUrl) /*|| VimeoParser.probe(parsedUrl)*/; // VimeoParser does not work right now
      return url.contains("youtube.com/watch?v") || ((url.contains("youtube.com/v/") || url.contains("youtu.be/")) && url.substring(url.lastIndexOf('/')).length() > 1);
    } catch(Exception ex) { }

    return false;
  }

  public ContentExtractOptions createExtractOptionsForUrl(String url) {
    String adjustedUrl = mayAdjustUrl(url);
    AvailableFormats formats = downloader.getAvailableFormats(adjustedUrl);
    formats.setUrl(url);

    return getContentExtractOptionsFromAvailableFormats(formats);
  }

  protected String mayAdjustUrl(String url) {
    String adjustedUrl = url;

    if(adjustedUrl.contains("youtube.com/v/")) {
      adjustedUrl = adjustedUrl.replace("youtube.com/v/", "youtube.com/watch?v=");
    }

    if(adjustedUrl.contains("youtu.be/")) {
      adjustedUrl = adjustedUrl.replace("youtu.be/", "youtube.com/watch?v=");
    }

    return adjustedUrl;
  }

  protected ContentExtractOptions getContentExtractOptionsFromAvailableFormats(final AvailableFormats formats) {
    ContentExtractOptions options = new ContentExtractOptions(formats.getUrl(), "YouTube"); // TODO: decide if its from YouTube or Vimeo

    for(final AvailableFormat format : formats.getFormats()) {
      options.addContentExtractOption(new ContentExtractOption(this, format, new ExtractContentAction() {
        @Override
        public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
          downloadFile(option, format, formats, listener);
        }
      }));
    }

    return options;
  }

  protected void downloadFile(ContentExtractOption option, final AvailableFormat format, final AvailableFormats formats, final ExtractContentActionResultListener listener) {
    // TODO: make VGet respect download file name, not using their own
//    DownloadConfig config = new DownloadConfig(option.getUrl(), getDownloadDestinationFilename(formats));
    // TODO: this is wrong, in this way YouTube video urls get newly extracted and always the best option is downloaded, but that's how VGet currently works
    // -> really download selected option
    DownloadConfig config = new DownloadConfig(formats.getUrl(), getDownloadDestinationFolder(formats, format));

    downloader.downloadAsync(config, new DownloadListener() {
      @Override
      public void progress(DownloadConfig download, float percentage) {

      }

      @Override
      public void downloadCompleted(DownloadConfig download, boolean successful, DeepThoughtError error) {
        dispatchDownloadResult(download, format, successful, error, formats, listener);
      }
    });
  }

  protected String getDownloadDestinationFilename(AvailableFormats formats) {
    return FileUtils.findUniqueFileNameInUserDataFolderForUrl(formats.getUrl()).getAbsolutePath();
  }

  protected String getDownloadDestinationFolder(AvailableFormats formats, AvailableFormat format) {
    return FileUtils.getUserDataFolderForFile(getFileTypeForFormat(format));
  }

  protected FileType getFileTypeForFormat(AvailableFormat format) {
    if(format.getVideoEncoding() == null) {
      return FileType.getAudioFileType();
    }

    return FileType.getVideoFileType();
  }

  protected void dispatchDownloadResult(DownloadConfig download, AvailableFormat format, boolean successful, DeepThoughtError error, AvailableFormats formats, ExtractContentActionResultListener listener) {
    if(successful == false) {
      listener.extractingContentDone(new EntryCreationResult(download.getUrl(), error));
    }
    else {
      listener.extractingContentDone(createEntryCreationResult(download, format, formats));
    }
  }

  protected EntryCreationResult createEntryCreationResult(DownloadConfig download, AvailableFormat format, AvailableFormats formats) {
    Entry entry = new Entry("", formats.getTitle());
    FileLink previewImage = new FileLink(formats.getPreviewImageUrl(), formats.getTitle());
    entry.setPreviewImage(previewImage);

    FileLink downloadedFile = new FileLink(download.getDestinationFileName(), formats.getTitle());
    downloadedFile.setSourceUriString(formats.getUrl());
    downloadedFile.setFileType(getFileTypeForFormat(format));

    EntryCreationResult result = new EntryCreationResult(download.getUrl(), entry);
    result.addAttachedFile(downloadedFile);

    return result;
  }
}
