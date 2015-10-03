package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

import java.io.File;

import javafx.scene.image.Image;

/**
 * Created by ganymed on 25/04/15.
 */
public class DefaultLocalFileContentExtractor implements ILocalFileContentExtractor {


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
    return Localization.getLocalizedString("local.file.content.extractor");
  }


  @Override
  public boolean canAttachFileToEntry(String url) {
    return FileUtils.isLocalFile(url);
  }

  @Override
  public boolean canSetFileAsEntryContent(String url) {
    return FileUtils.isLocalFile(url) && FileUtils.isImageFile(FileUtils.getMimeType(url));
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
    if(clipboardContent.hasImage()) {
      Image image = clipboardContent.getImage();
      return new ContentExtractOption(this, image, false, true, true);
    }

    if(clipboardContent.hasFiles()) {
      for(File file : clipboardContent.getFiles()) { // TODO: what about other files if one of the first files already succeed?
        String filePath = file.getAbsolutePath();
        if(canAttachFileToEntry(filePath) || canSetFileAsEntryContent(filePath) || canCreateEntryFromUrl(filePath)) {
          return new ContentExtractOption(this, filePath, canCreateEntryFromUrl(filePath), canAttachFileToEntry(filePath), canSetFileAsEntryContent(filePath));
        }
      }
    }
    else if(clipboardContent.hasUrl())
      return canExtractContentFromFilePath(clipboardContent.getUrl());
    else if(clipboardContent.hasString())
      return canExtractContentFromFilePath(clipboardContent.getString());

    return ContentExtractOption.CanNotExtractContent;
  }

  protected ContentExtractOption canExtractContentFromFilePath(String filePath) {
    if(canAttachFileToEntry(filePath) || canSetFileAsEntryContent(filePath) || canCreateEntryFromUrl(filePath)) {
      return new ContentExtractOption(this, filePath, canCreateEntryFromUrl(filePath), canAttachFileToEntry(filePath), canSetFileAsEntryContent(filePath));
    }

    return ContentExtractOption.CanNotExtractContent;
  }

  public FileLink createFileLink(ContentExtractOption contentExtractOption) {
    if(contentExtractOption.isUrl())
      return new FileLink(contentExtractOption.getUrl());
    else if(contentExtractOption.isImage()) {
      Image image = (Image)contentExtractOption.getSource();
    }

    return null;
  }

  @Override
  public void createEntryFromClipboardContentAsync(ContentExtractOption contentExtractOption, CreateEntryListener listener) {
    if(listener != null)
      listener.entryCreated(new EntryCreationResult(contentExtractOption, new Entry()));
  }


  @Override
  public String toString() {
    return "DefaultLocalFileContentExtractor";
  }

}
