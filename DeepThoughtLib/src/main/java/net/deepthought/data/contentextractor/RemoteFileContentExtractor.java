package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

public class RemoteFileContentExtractor implements IRemoteFileContentExtractor {


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
    return canDownloadFile(url);
  }

  @Override
  public ContentExtractOptions createExtractOptionsForUrl(String url) {
    return null;
  }

  @Override
  public void createEntryFromUrlAsync(String url, CreateEntryListener listener) {

  }

  // TODO: move this logic to a better place
//  @Override
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

}
