package net.deepthought.data.contentextractor;

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

  protected List<ITextContentExtractor> textContentExtractors = new CopyOnWriteArrayList<>();

  protected List<IOnlineArticleContentExtractor> onlineArticleContentExtractors = new CopyOnWriteArrayList<>();


  public DefaultContentExtractorManager() {
//    addContentExtractor(new DefaultLocalFileContentExtractor());
//    addContentExtractor(new RemoteFileContentExtractor());
  }


//  @Override
//  public Collection<IContentExtractor> getContentExtractors() {
//    return contentExtractors;
//  }

  @Override
  public boolean addContentExtractor(IContentExtractor contentExtractor) {
    if(contentExtractor instanceof ITextContentExtractor)
      return addTextContentExtractor((ITextContentExtractor)contentExtractor);
    if(contentExtractor instanceof IOnlineArticleContentExtractor)
      return addOnlineArticleContentExtractor((IOnlineArticleContentExtractor)contentExtractor);
//    return contentExtractors.add(contentExtractor);
    return false;
  }

  //@Override
  public boolean addTextContentExtractor(ITextContentExtractor contentExtractor) {
    return textContentExtractors.add(contentExtractor);
  }

  //@Override
  public boolean addOnlineArticleContentExtractor(IOnlineArticleContentExtractor contentExtractor) {
    return onlineArticleContentExtractors.add(contentExtractor);
  }

//  public List<IContentExtractor> getContentExtractorsForUrl(String url) {
//    List<IContentExtractor> contentExtractorsForUrl = new ArrayList<>();
//
//    for(IContentExtractor contentExtractor : getContentExtractors()) {
//      if(contentExtractor.canCreateEntryFromUrl(url))
//        contentExtractorsForUrl.add(contentExtractor);
//    }
//
//    return contentExtractorsForUrl;
//  }

  public ContentExtractOptions getContentExtractorOptionsForClipboardContent(ClipboardContent clipboardContent) {

    if(clipboardContent.hasImage()) {
      // TODO:
//      Image image = clipboardContent.getImage();
//      return new ContentExtractOption(this, image, false, true, true);
      return new ContentExtractOptions();
    }

    List<String> urls = new ArrayList<>();

    if(clipboardContent.hasFiles()) {
      for(File file : clipboardContent.getFiles()) {
        urls.add(file.getAbsolutePath());
//        String filePath = file.getAbsolutePath();
//        if(canAttachFileToEntry(filePath) || canSetFileAsEntryContent(filePath) || canCreateEntryFromUrl(filePath)) {
//          return new ContentExtractOption(this, filePath, canCreateEntryFromUrl(filePath), canAttachFileToEntry(filePath), canSetFileAsEntryContent(filePath));
//        }
      }
    }
    else if(clipboardContent.hasUrl())
      urls.add(clipboardContent.getUrl());
//      return canExtractContentFromFilePath(clipboardContent.getUrl());
    else if(clipboardContent.hasString()) {
      if(StringUtils.isNotNullOrEmpty(clipboardContent.getString()))
        urls.add(clipboardContent.getString());
//      return canExtractContentFromFilePath(clipboardContent.getString());
    }

    for(String url : urls) {
      if(isAttachableFile(url)) {
        ContentExtractOptions contentExtractOptions = new ContentExtractOptions(url, canSetFileAsEntryContent(url));
        for(ITextContentExtractor textContentExtractor : textContentExtractors) {
          if(textContentExtractor.canCreateEntryFromUrl(url))
            contentExtractOptions.addContentExtractOption(new ContentExtractOption(textContentExtractor, url, true));
        }

        return contentExtractOptions; // TODO: what about other files if one of the first files already succeed?
      }
      else {
        for(IOnlineArticleContentExtractor onlineArticleContentExtractor : onlineArticleContentExtractors) {
          if(onlineArticleContentExtractor.canCreateEntryFromUrl(url)) {
            ContentExtractOptions contentExtractOptions = new ContentExtractOptions(url, false);
            contentExtractOptions.addContentExtractOption(new ContentExtractOption(onlineArticleContentExtractor, url, true));
            return contentExtractOptions;
          }
        }
      }
    }

    ContentExtractOptions contentExtractOptions = new ContentExtractOptions();

//    for(IContentExtractor contentExtractor : getContentExtractors()) {
//      ContentExtractOption contentExtractOption = contentExtractor.canCreateEntryFromClipboardContent(clipboardContent);
//      if(contentExtractOption != ContentExtractOption.CanNotExtractContent)
//        contentExtractOptions.addContentExtractOption(contentExtractOption);
//    }

    return contentExtractOptions;
  }

  protected boolean isAttachableFile(String url) {
    return FileUtils.isLocalFile(url) || FileUtils.isRemoteFile(url);
  }

  protected boolean canSetFileAsEntryContent(String url) {
//    return FileUtils.isImageFile(FileUtils.getMimeType(url));
    String mimeType = FileUtils.getMimeType(url);
    return mimeType.endsWith("jpg") || mimeType.endsWith("jpeg") || mimeType.endsWith("jpe") || mimeType.endsWith("png") || mimeType.endsWith("gif");
  }

}
