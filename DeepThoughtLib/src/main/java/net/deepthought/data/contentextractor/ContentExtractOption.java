package net.deepthought.data.contentextractor;

import java.io.File;

import javafx.scene.image.Image;

/**
 * Created by ganymed on 25/04/15.
 */
public class ContentExtractOption {

  public final static ContentExtractOption CanNotExtractContent = new ContentExtractOption();


  protected IContentExtractor contentExtractor;

  protected Object source;

  protected boolean canCreateEntryFromUrl;

  protected boolean canAttachFileToEntry;

  protected boolean canSetFileAsEntryContent;

  protected boolean isUrl = false;
  protected boolean isImage = false;


  protected ContentExtractOption() {
    this(null, "", false, false, false);
  }

  public ContentExtractOption(IContentExtractor contentExtractor, Object source, boolean canCreateEntryFromUrl) {
    this(contentExtractor, source, canCreateEntryFromUrl, false, false);
  }

  public ContentExtractOption(IContentExtractor contentExtractor, Object source, boolean canCreateEntryFromUrl, boolean canAttachFileToEntry, boolean canSetFileAsEntryContent) {
    this.contentExtractor = contentExtractor;
    setSource(source);

    this.canCreateEntryFromUrl = canCreateEntryFromUrl;
    this.canAttachFileToEntry = canAttachFileToEntry;
    this.canSetFileAsEntryContent = canSetFileAsEntryContent;
  }


  public IContentExtractor getContentExtractor() {
    return contentExtractor;
  }

  public Object getSource() {
    return source;
  }

  public void setSource(Object source) {
    this.source = source;

    if(source instanceof Image)
      isImage = true;
    else if(source instanceof String) {
      try {
        new File((String)source).toURI(); // check if source can be parsed to an URI
        isUrl = true;
      } catch(Exception ex) { }
    }
  }

  public boolean canExtractText() {
    return canCreateEntryFromUrl;
  }

  public boolean canAttachFileToEntry() {
    return canAttachFileToEntry;
  }

  public boolean canSetFileAsEntryContent() {
    return canSetFileAsEntryContent;
  }

  public boolean isUrl() {
    return isUrl;
  }

  public String getUrl() {
    if(isUrl())
      return (String)source;
    return null;
  }

  public boolean isImage() {
    return isImage;
  }


  @Override
  public String toString() {
    return contentExtractor + " for source " + source;
  }

}
