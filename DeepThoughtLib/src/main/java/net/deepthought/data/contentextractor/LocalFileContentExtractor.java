package net.deepthought.data.contentextractor;

import net.deepthought.data.model.FileLink;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

import java.io.File;

import javafx.scene.image.Image;

/**
 * Created by ganymed on 25/04/15.
 */
public class LocalFileContentExtractor implements ILocalFileContentExtractor {


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
    return canAttachFileToEntry(url) || canSetFileAsEntryContent(url);
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


//  public void setFileAsEntryContent(ContentExtractOptions options) {
////    final ContentExtractOption setFileAsEntryContentOption = options.getSetFileAsEntryContentOption();
////    FileLink newFile = ((ILocalFileContentExtractor)setFileAsEntryContentOption.getContentExtractor()).createFileLink(setFileAsEntryContentOption);
//    final FileLink newFile = new FileLink(options.getUrl());
//
//    Entry newEntry = new Entry();
//    newEntry.setContent("<html dir=\"ltr\"><head></head><body contenteditable=\"true\"><p><img src=\"file://" + newFile.getUriString() + "\" " +
//        "/></p><p></p></body></html>");
//    Dialogs.showEditEntryDialog(newEntry);
//  }
//
//  public void copyFileToDataFolderAndSetAsEntryContent(final ContentExtractOption options) {
//    final FileLink newFile = new FileLink(options.getUrl());
//    final String fileUrl = newFile.getUriString();
//
//    if(FileUtils.isRemoteFile(fileUrl) && Application.getDownloader().canDownloadUrl(fileUrl)) {
//      downloadToTempFile(fileUrl, (successful, tempFile) -> {
//        if(successful) {
//          String fileName = newFile.getName();
//          newFile.setUriString(tempFile.getAbsolutePath());
//          newFile.setName(fileName);
//          copyFileToDataFolderAndSetAsEntryContent(options, newFile);
//        }
//      });
//    }
//    else
//      copyFileToDataFolderAndSetAsEntryContent(options, newFile);
//  }
//
//  protected void copyFileToDataFolderAndSetAsEntryContent(final ContentExtractOption option, FileLink newFile) {
//    FileUtils.copyFileToDataFolder(newFile, new FileOperationListener() {
//      @Override
//      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
//        return ExistingFileHandling.RenameNewFile;
//      }
//
//      @Override
//      public void errorOccurred(DeepThoughtError error) {
//        showCouldNotCreateEntryError(option.getSource(), error);
//      }
//
//      @Override
//      public void fileOperationDone(boolean successful, File destinationFile) {
//        if (successful) {
//          Entry newEntry = new Entry();
//          newEntry.setContent("<html dir=\"ltr\"><head></head><body contenteditable=\"true\"><p><img src=\"file://" + destinationFile.getAbsolutePath() + "\" " +
//              "/></p><p></p></body></html>");
//          Dialogs.showEditEntryDialog(newEntry);
//        }
//      }
//    });
//  }
//
//  public void attachFileToEntry(ContentExtractOption option) {
//    final FileLink newFile = new FileLink(option.getUrl());
//    Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//        if (controller.getDialogResult() == DialogResult.Ok) {
//          Entry newEntry = new Entry();
//          newEntry.addAttachedFile(newFile);
//          Dialogs.showEditEntryDialog(newEntry);
//        }
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//
//      }
//    });
//  }
//
//  public void tryToExtractText(final ContentExtractOption extractTextOption) {
//    final IOcrContentExtractor textContentExtractor = (IOcrContentExtractor)extractTextOption.getContentExtractor();
//
//    if(extractTextOption.isRemoteFile()) {
//      downloadToTempFile(extractTextOption.getUrl(), (successful, tempFile) -> {
//        if(successful) {
//          extractTextOption.setSource(tempFile.getAbsolutePath());
//          tryToExtractText(extractTextOption, textContentExtractor);
//        }
//      });
//    }
//    else {
//      tryToExtractText(extractTextOption, textContentExtractor);
//    }
//  }
//
//  protected void tryToExtractText(ContentExtractOption extractTextOption, IOcrContentExtractor textContentExtractor) {
//    textContentExtractor.createEntryFromClipboardContentAsync(extractTextOption, result -> {
//      if (result.successful())
//        Dialogs.showEditEntryDialog(result);
//      else
//        showCouldNotCreateEntryError(result);
//    });
//  }
//
//  public void attachFileToEntryAndTryToExtractText(final ContentExtractOption option) {
//    final FileLink newFile = new FileLink(option.getUrl());
//    Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//        if (controller.getDialogResult() == DialogResult.Ok) {
//          IOcrContentExtractor textContentExtractor = (IOcrContentExtractor) option.getContentExtractor();
//          textContentExtractor.createEntryFromUrlAsync(newFile.getUriString(), result -> {
//            if (result.successful()) {
//              Entry newEntry = result.getCreatedEntry();
//              newEntry.addAttachedFile(newFile);
//              Dialogs.showEditEntryDialog(result);
//            } else
//              showCouldNotCreateEntryError(result);
//          });
//        }
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//
//      }
//    });
//  }
//
//
//  protected interface DownloadToTempFileResult {
//    public void completed(boolean successful, File tempFile);
//  }
//
//  protected void downloadToTempFile(final String url, final DownloadToTempFileResult result) {
//    try {
//      final File tempFile = File.createTempFile("DeepThoughtDownload_" + FileUtils.getFileName(url), FileUtils.getFileExtension(url));
//      Application.getDownloader().downloadAsync(new DownloadConfig(url, tempFile.getAbsolutePath()), new DownloadListener() {
//        @Override
//        public void progress(DownloadConfig download, float percentage) {
//
//        }
//
//        @Override
//        public void downloadCompleted(DownloadConfig download, boolean successful, DeepThoughtError error) {
//          if(successful == false) {
//            log.error("Could not download file " + url + ": " + error);
//            // TODO: notify user
//          }
//
//          if(result != null)
//            result.completed(successful, tempFile);
//        }
//      });
//    } catch(Exception ex) {
//      log.error("Could not download file " + url, ex);
//      // TODO: notify user
//
//      if(result != null)
//        result.completed(false, null);
//    }
//  }


  @Override
  public String toString() {
    return getName();
  }

}
