package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.clipboard.ClipboardContent;
import net.dankito.deepthought.data.contentextractor.file.LocalFileContentExtractor;
import net.dankito.deepthought.data.contentextractor.file.RemoteFileContentExtractor;
import net.dankito.deepthought.data.contentextractor.ocr.IOcrContentExtractor;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.file.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 24/04/15.
 */
public class ContentExtractorManager implements IContentExtractorManager {

  protected static final int CountDefaultContentExtractors = 3;


  protected List<IContentExtractor> contentExtractors = new CopyOnWriteArrayList<>();

  protected List<IOcrContentExtractor> ocrContentExtractors = new CopyOnWriteArrayList<>();

  protected List<IOnlineArticleContentExtractor> onlineArticleContentExtractors = new CopyOnWriteArrayList<>();

  protected List<IOnlineArticleContentExtractor> onlineArticleContentExtractorsWithArticleOverview = new CopyOnWriteArrayList<>();


  public ContentExtractorManager() {
    contentExtractors.add(new BasicWebPageContentExtractor());
    contentExtractors.add(new LocalFileContentExtractor());
    contentExtractors.add(new RemoteFileContentExtractor());
  }



  @Override
  public boolean addContentExtractor(IContentExtractor contentExtractor) {
    if(contentExtractor instanceof IOcrContentExtractor)
      return addOcrContentExtractor((IOcrContentExtractor) contentExtractor); // TODO: in this way a class implementing multiple IContentExtractor interfaces only gets added to ocrContentExtractors
    if(contentExtractor instanceof IOnlineArticleContentExtractor)
      return addOnlineArticleContentExtractor((IOnlineArticleContentExtractor) contentExtractor);

    if(contentExtractor instanceof IOcrContentExtractor == false && contentExtractor instanceof IOnlineArticleContentExtractor == false) {
      // add before default ContentExtractors
      // TODO: implement Priority property in IContentExtractor
      contentExtractors.add(contentExtractors.size() - CountDefaultContentExtractors, contentExtractor);
      return true;
    }

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


  public void getContentExtractorOptionsForClipboardContentAsync(ClipboardContent clipboardContent, GetContentExtractorOptionsListener listener) {
    if(clipboardContent.hasImage()) {
      // TODO:
//      Image image = clipboardContent.getImage();
//      return new ContentExtractOption(this, image, false, true, true);
      dispatchCreatedContentExtractOptions(new ContentExtractOptions(), listener);
      return;
    }

    List<String> urls = getUrlsFromClipboardContent(clipboardContent);

    if(urls.size() > 0) {
      createContentExtractOptionsFromUrls(urls, listener);
      return;
    }

    ContentExtractOptions contentExtractOptions = new ContentExtractOptions();

   dispatchCreatedContentExtractOptions(contentExtractOptions, listener);
  }

  protected void dispatchCreatedContentExtractOptions(ContentExtractOptions options, GetContentExtractorOptionsListener listener) {
    listener.contentExtractorOptionsRetrieved(options);
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

  protected void createContentExtractOptionsFromUrls(List<String> urls, GetContentExtractorOptionsListener listener) {
    for(String url : urls) {
      IContentExtractor foundContentExtractor = getContentExtractorForUrl(url);
      if(foundContentExtractor != null) {
        dispatchCreatedContentExtractOptions(foundContentExtractor.createExtractOptionsForUrl(url), listener);
        return; // TODO: what about the remaining urls if a previous one succeeds?
      }
    }

    dispatchCreatedContentExtractOptions(new ContentExtractOptions(), listener); // no ContentExtractor found for Clipboard Content
  }

  @Override
  public IContentExtractor getContentExtractorForUrl(String url) {
    for(IOnlineArticleContentExtractor onlineArticleContentExtractor : onlineArticleContentExtractors) {
      if(onlineArticleContentExtractor.canCreateEntryFromUrl(url)) {
        return onlineArticleContentExtractor;
      }
    }

    for(IContentExtractor contentExtractor : contentExtractors) {
      if(contentExtractor.canCreateEntryFromUrl(url)) {
        return contentExtractor;
      }
    }

    return null;
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
