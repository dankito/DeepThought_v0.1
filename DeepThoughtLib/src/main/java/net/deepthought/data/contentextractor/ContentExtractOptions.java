package net.deepthought.data.contentextractor;

import net.deepthought.util.file.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 25/04/15.
 */
public class ContentExtractOptions {

  protected List<ContentExtractOption> contentExtractOptions = new ArrayList<>();

  protected Object source = null;

  protected boolean isUrl = false;

  protected boolean canSetFileAsEntryContent = false;


  public ContentExtractOptions() {

  }

  public ContentExtractOptions(String url) {
    this.source = url;
    this.isUrl = true;
  }

  public ContentExtractOptions(String url, boolean canSetFileAsEntryContent) {
    this(url);
    this.canSetFileAsEntryContent = canSetFileAsEntryContent;
  }


  public boolean addContentExtractOption(ContentExtractOption contentExtractOption) {
    return contentExtractOptions.add(contentExtractOption);
  }

  public boolean hasContentExtractOptions() {
    return source != null || contentExtractOptions.size() > 0;
  }

  public int getContentExtractOptionsSize() {
    return contentExtractOptions.size();
  }

  public List<ContentExtractOption> getContentExtractOptions() {
    return contentExtractOptions;
  }


  public boolean isLocalFile() {
    return isUrl() && FileUtils.isLocalFile(getUrl());
  }

  public boolean isRemoteFile() {
    return isUrl() && FileUtils.isRemoteFile(getUrl());
  }

  public boolean isFile() {
    return isLocalFile() || isRemoteFile();
  }

  public boolean isLocalFileContentExtractor() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.getContentExtractor() instanceof ILocalFileContentExtractor &&
          contentExtractOption.getContentExtractor() instanceof IRemoteFileContentExtractor == false)
        return true;
    }

    return false;
  }

  public ILocalFileContentExtractor getLocalFileContentExtractor() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.getContentExtractor() instanceof ILocalFileContentExtractor &&
          contentExtractOption.getContentExtractor() instanceof IRemoteFileContentExtractor == false)
        return (ILocalFileContentExtractor)contentExtractOption.getContentExtractor();
    }

    return null;
  }

  public boolean isRemoteFileContentExtractor() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.getContentExtractor() instanceof IRemoteFileContentExtractor)
        return true;
    }

    return false;
  }

  public IRemoteFileContentExtractor getRemoteFileContentExtractor() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.getContentExtractor() instanceof IRemoteFileContentExtractor)
        return (IRemoteFileContentExtractor)contentExtractOption.getContentExtractor();
    }

    return null;
  }

  public boolean isOnlineArticleContentExtractor() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.getContentExtractor() instanceof IOnlineArticleContentExtractor)
        return true;
    }

    return false;
  }

  public boolean isTextContentExtractor() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.getContentExtractor() instanceof ITextContentExtractor)
        return true;
    }

    return false;
  }

  public ITextContentExtractor getTextContentExtractor() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.getContentExtractor() instanceof ITextContentExtractor)
        return (ITextContentExtractor)contentExtractOption.getContentExtractor();
    }

    return null;
  }


  public boolean canExtractText() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.canExtractText())
        return true;
    }

    return false;
  }

  public ContentExtractOption getExtractTextOption() {
    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
      if(contentExtractOption.canExtractText())
        return contentExtractOption;
    }

    return null;
  }

  public boolean canAttachFileToEntry() {
//    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
//      if(contentExtractOption.canAttachFileToEntry())
//        return true;
//    }
//
//    return false;

    return isUrl();
  }

//  public ContentExtractOption getAttachFileToEntryOption() {
//    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
//      if(contentExtractOption.canAttachFileToEntry())
//        return contentExtractOption;
//    }
//
//    return null;
//  }

  public boolean canSetFileAsEntryContent() {
//    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
//      if(contentExtractOption.canSetFileAsEntryContent())
//        return true;
//    }
//
//    return false;

    return canSetFileAsEntryContent;
  }

//  public ContentExtractOption getSetFileAsEntryContentOption() {
//    for(ContentExtractOption contentExtractOption : contentExtractOptions) {
//      if(contentExtractOption.canSetFileAsEntryContent())
//        return contentExtractOption;
//    }
//
//    return null;
//  }

  public Object getSource() {
//    if(contentExtractOptions.size() > 0)
//      return contentExtractOptions.get(0).getSource();
//    return null;
    return source;
  }

  public boolean isUrl() {
    return isUrl;
  }

  public String getUrl() {
    if(isUrl()) {
      String url = (String) source;
      if(url.startsWith("file:"))
        url = url.substring("file:".length());
      return url;
    }
    return "";
  }
}
