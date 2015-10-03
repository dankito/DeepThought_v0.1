package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

public class RemoteFileContentExtractor implements IRemoteFileContentExtractor {


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
    return Localization.getLocalizedString("remote.file.content.extractor");
  }


  @Override
  public boolean canDownloadFile(String url) {
    return Application.getDownloader().canDownloadUrl(url);
  }

  @Override
  public boolean canAttachFileToEntry(String url) {
    return canDownloadFile(url);
  }

  @Override
  public boolean canSetFileAsEntryContent(String url) {
    return FileUtils.isRemoteFile(url) && FileUtils.isImageFile(FileUtils.getMimeType(url));
  }

  @Override
  public FileLink createFileLink(ContentExtractOption contentExtractOption) {
    if(contentExtractOption.isUrl())
      return new FileLink(contentExtractOption.getUrl());

    return null;
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return false;
  }

  @Override
  public void createEntryFromUrlAsync(String url, CreateEntryListener listener) {

  }

  @Override
  public ContentExtractOption canCreateEntryFromClipboardContent(ClipboardContent clipboardContent) {
    if(clipboardContent.hasUrl()) {
      if(canDownloadFile(clipboardContent.getUrl()))
        return new ContentExtractOption(this, clipboardContent.getUrl(), false, true, canSetFileAsEntryContent(clipboardContent.getUrl()));
    }
    else if(clipboardContent.hasString()) {
      if(canDownloadFile(clipboardContent.getString()))
        return new ContentExtractOption(this, clipboardContent.getString(), false, true, canSetFileAsEntryContent(clipboardContent.getString()));
    }

    return ContentExtractOption.CanNotExtractContent;
  }

  @Override
  public void createEntryFromClipboardContentAsync(ContentExtractOption contentExtractOption, CreateEntryListener listener) {

  }
}
