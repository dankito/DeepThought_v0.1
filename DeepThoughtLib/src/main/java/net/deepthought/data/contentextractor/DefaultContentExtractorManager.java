package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.ocr.IOcrContentExtractor;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 24/04/15.
 */
public class DefaultContentExtractorManager implements IContentExtractorManager {

  protected List<IContentExtractor> contentExtractors = new CopyOnWriteArrayList<>();

  protected List<IOcrContentExtractor> ocrContentExtractors = new CopyOnWriteArrayList<>();

  protected List<IOnlineArticleContentExtractor> onlineArticleContentExtractors = new CopyOnWriteArrayList<>();

  protected List<IOnlineArticleContentExtractor> onlineArticleContentExtractorsWithArticleOverview = new CopyOnWriteArrayList<>();


  public DefaultContentExtractorManager() {
    contentExtractors.add(new BasicWebPageContentExtractor());
    contentExtractors.add(new LocalFileContentExtractor());
//    addContentExtractor(new RemoteFileContentExtractor());
  }



  @Override
  public boolean addContentExtractor(IContentExtractor contentExtractor) {
    if(contentExtractor instanceof IOcrContentExtractor)
      return addOcrContentExtractor((IOcrContentExtractor) contentExtractor); // TODO: in this way a class implementing multiple IContentExtractor interfaces only gets added to ocrContentExtractors
    if(contentExtractor instanceof IOnlineArticleContentExtractor)
      return addOnlineArticleContentExtractor((IOnlineArticleContentExtractor) contentExtractor);

    if(contentExtractor instanceof IOcrContentExtractor == false && contentExtractor instanceof IOnlineArticleContentExtractor == false)
      return contentExtractors.add(contentExtractor);

    return false;
  }

  //@Override
  public boolean addOcrContentExtractor(IOcrContentExtractor contentExtractor) {
    return ocrContentExtractors.add(contentExtractor);
  }

  //@Override
  public boolean addOnlineArticleContentExtractor(IOnlineArticleContentExtractor contentExtractor) {
    if(contentExtractor.hasArticlesOverview())
      onlineArticleContentExtractorsWithArticleOverview.add(contentExtractor);
    return onlineArticleContentExtractors.add(contentExtractor);
  }


  public ContentExtractOptions getContentExtractorOptionsForClipboardContent(ClipboardContent clipboardContent) {
    if(clipboardContent.hasImage()) {
      // TODO:
//      Image image = clipboardContent.getImage();
//      return new ContentExtractOption(this, image, false, true, true);
      return new ContentExtractOptions();
    }

    List<String> urls = getUrlsFromClipboardContent(clipboardContent);

    if(urls.size() > 0) {
      return createContentExtractOptionsFromUrls(urls);
    }

    ContentExtractOptions contentExtractOptions = new ContentExtractOptions();

    return contentExtractOptions;
  }

  protected List<String> getUrlsFromClipboardContent(ClipboardContent clipboardContent) {
    List<String> urls = new ArrayList<>();

    if(clipboardContent.hasFiles()) {
      for(File file : clipboardContent.getFiles()) {
        urls.add(file.getAbsolutePath());
      }
    }
    else if(clipboardContent.hasUrl()) {
      urls.add(clipboardContent.getUrl());
    }
    else if(clipboardContent.hasString()) {
      if(StringUtils.isNotNullOrEmpty(clipboardContent.getString()))
        urls.add(clipboardContent.getString());
    }
    return urls;
  }

  protected ContentExtractOptions createContentExtractOptionsFromUrls(List<String> urls) {
    for(String url : urls) {
      // TODO: what about the remaining urls if a previous one succeeds?
      for(IOnlineArticleContentExtractor onlineArticleContentExtractor : onlineArticleContentExtractors) {
        if(onlineArticleContentExtractor.canCreateEntryFromUrl(url)) {
          return onlineArticleContentExtractor.createExtractOptionsForUrl(url);
        }
      }

      for(IContentExtractor contentExtractor : contentExtractors) {
        if(contentExtractor.canCreateEntryFromUrl(url)) {
          return contentExtractor.createExtractOptionsForUrl(url);
        }
      }
    }

    return new ContentExtractOptions();
  }

  protected boolean isAttachableFile(String url) {
    return FileUtils.isLocalFile(url) || FileUtils.isRemoteFile(url);
  }

  /**
   * Checks if CKEditor can display this file type.
   */
  protected boolean canSetFileAsEntryContent(String url) {
//    return FileUtils.isImageFile(FileUtils.getMimeType(url));
    String mimeType = FileUtils.getMimeType(url);
    return mimeType.endsWith("jpg") || mimeType.endsWith("jpeg") || mimeType.endsWith("jpe") || mimeType.endsWith("png") || mimeType.endsWith("gif");
  }


  public boolean hasOcrContentExtractors() {
    return ocrContentExtractors.size() > 0;
  }

  public IOcrContentExtractor getPreferredOcrContentExtractor() {
    if(hasOcrContentExtractors()) {
      return ocrContentExtractors.get(0); // TODO: if there are multiple ones available may judge which fits best
    }

    return null;
  }

  public boolean hasOnlineArticleContentExtractors() {
    return onlineArticleContentExtractors.size() > 0;
  }

  public List<IOnlineArticleContentExtractor> getOnlineArticleContentExtractors() {
    return onlineArticleContentExtractors;
  }

  public boolean hasOnlineArticleContentExtractorsWithArticleOverview() {
    return getOnlineArticleContentExtractorsWithArticleOverview().size() > 0;
  }

  public List<IOnlineArticleContentExtractor> getOnlineArticleContentExtractorsWithArticleOverview() {
    return onlineArticleContentExtractorsWithArticleOverview;
  }

}
