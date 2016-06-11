package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.util.file.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 25/04/15.
 */
public class ContentExtractOptions {

  protected List<ContentExtractOption> contentExtractOptions = new ArrayList<>();

  protected Object source = null;

  protected boolean isUrl = false;

  protected String sourceShortName = null;

  protected boolean canSetFileAsEntryContent = false;


  public ContentExtractOptions() {
    this("");
  }

  public ContentExtractOptions(String url) {
    this(url, false);
  }

  public ContentExtractOptions(String url, String sourceShortName) {
    this(url);
    this.sourceShortName = sourceShortName;
  }

  public ContentExtractOptions(String url, boolean canSetFileAsEntryContent) {
    this.source = url;
    this.isUrl = true;
    this.canSetFileAsEntryContent = canSetFileAsEntryContent;
  }


  public boolean addContentExtractOption(ContentExtractOption contentExtractOption) {
    return contentExtractOptions.add(contentExtractOption);
  }

  public boolean hasContentExtractOptions() {
    return contentExtractOptions.size() > 0;
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

  public Object getSource() {
//    if(contentExtractOptions.size() > 0)
//      return contentExtractOptions.get(0).getSource();
//    return null;
    return source;
  }

  public String getSourceShortName() {
    if(sourceShortName == null) {
      return source.toString();
    }

    return sourceShortName;
  }

  public void setSourceShortName(String sourceShortName) {
    this.sourceShortName = sourceShortName;
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


  @Override
  public String toString() {
    return getUrl() + " (" + getContentExtractOptionsSize() + " ContentExtractOptions)";
  }

}
