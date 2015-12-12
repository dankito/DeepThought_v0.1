package net.deepthought.controls;

import net.deepthought.Application;
import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.contentextractor.ClipboardContent;
import net.deepthought.data.contentextractor.ContentExtractOption;
import net.deepthought.data.contentextractor.ContentExtractOptions;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.contentextractor.ocr.IOcrContentExtractor;
import net.deepthought.data.contentextractor.JavaFxClipboardContent;
import net.deepthought.data.contentextractor.OptionInvokedListener;
import net.deepthought.data.download.DownloadConfig;
import net.deepthought.data.download.DownloadListener;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.InputManager;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileNameSuggestion;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.file.enums.ExistingFileHandling;
import net.deepthought.util.file.listener.FileOperationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Created by ganymed on 25/04/15.
 */
public class CreateEntryFromClipboardContentPopup extends PopupControl {

  private final static Logger log = LoggerFactory.getLogger(CreateEntryFromClipboardContentPopup.class);


  protected Stage stageToShowIn = null;

  protected VBox contentPane;

  protected VBox optionsPane;

  protected Label headerLabel;

  protected Button hidePopupButton;

  protected Object sourceOfLastShownPopup = null;


  public CreateEntryFromClipboardContentPopup(Stage stageToShowIn) {
    this.stageToShowIn = stageToShowIn;

    createPopupFrame();

    stageToShowIn.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue == true) { // Window got focused
          checkIfEntryCanBeCreatedFromClipboardContent();
        }
      }
    });
  }


  protected void createPopupFrame() {
    setAutoHide(true);
//    setAutoFix(true);

    setMinWidth(150);
    setMaxWidth(stageToShowIn.getWidth() - 12);

    contentPane = new VBox(0);
//    Color backgroundColor = Color.LIGHTSKYBLUE.deriveColor(0, 1.0, 1.0, 0.95);
//    Color backgroundColor = Color.GOLDENROD.deriveColor(0, 1.0, 1.0, 0.3);
//    Color backgroundColor = Color.LAWNGREEN.deriveColor(0, 1.0, 1.0, 0.15);
//    Color backgroundColor = Color.DARKGREEN.deriveColor(0, 1.0, 1.0, 0.95);
//    Color backgroundColor = Color.LIMEGREEN.deriveColor(0, 1.0, 1.0, 0.95);
    contentPane.setBackground(Constants.ClipboardContentPopupBackground);
    getScene().setRoot(contentPane);

    hidePopupButton = new Button("x");
    AnchorPane.setTopAnchor(hidePopupButton, 0d);
    AnchorPane.setRightAnchor(hidePopupButton, 0d);
    AnchorPane.setBottomAnchor(hidePopupButton, 4d);
    hidePopupButton.setOnAction(action -> hideThreadSafe());

    headerLabel = new Label();
    AnchorPane.setTopAnchor(headerLabel, 8d);
    AnchorPane.setLeftAnchor(headerLabel, 10d);
    AnchorPane.setBottomAnchor(headerLabel, 4d);
    AnchorPane.setRightAnchor(headerLabel, hidePopupButton.getWidth() + 26 + 6); // 26 = hidePopupButton width

    contentPane.getChildren().add(new AnchorPane(headerLabel, hidePopupButton));

    optionsPane = new VBox();
    contentPane.getChildren().add(optionsPane);
  }

  protected void checkIfEntryCanBeCreatedFromClipboardContent() {
    ClipboardContent clipboardContent = new JavaFxClipboardContent(Clipboard.getSystemClipboard());
    ContentExtractOptions contentExtractOptions = Application.getContentExtractorManager().getContentExtractorOptionsForClipboardContent(clipboardContent);
    if(contentExtractOptions.hasContentExtractOptions()) {
      if(contentExtractOptions.getSource().equals(sourceOfLastShownPopup) == false) {
        sourceOfLastShownPopup = contentExtractOptions.getSource();
        showCreateEntryFromClipboardContentPopup(contentExtractOptions);
      }
    }
  }

  protected void showCreateEntryFromClipboardContentPopup(ContentExtractOptions contentExtractOptions) {
    optionsPane.getChildren().clear();

    if(contentExtractOptions.isOnlineArticleContentExtractor())
      showCreateEntryFromOnlineArticleContentExtractorPopup(contentExtractOptions);
    else if(contentExtractOptions.isUrl())
      showCreateEntryFromFilePopup(contentExtractOptions);
  }

  protected void showCreateEntryFromOnlineArticleContentExtractorPopup(final ContentExtractOptions contentExtractOptions) {
    if(isShowing())
      hideThreadSafe();

    createCreateEntryFromOnlineArticleOptions(contentExtractOptions);

    showInStage();
  }

  protected void showCreateEntryFromFilePopup(final ContentExtractOptions contentExtractOptions) {
    if(isShowing())
      hideThreadSafe();

    createCreateEntryFromFileOptions(contentExtractOptions);

    showInStage();
  }

  protected void showInStage() {
    contentPane.setDisable(false); // TODO: this is not fully correctly implemented: if an option button is pressed and the process is still working, while the window gets
    // moved, than all options get enabled again

    show(stageToShowIn.getScene().getRoot(), 0, 0);
    // ok, PopupWindow is too stupid to set its position to right bottom window corner by itself, so i have to do it on my own
    setX(stageToShowIn.getX() + stageToShowIn.getWidth() - getWidth() - 6);
    setY(stageToShowIn.getY() + stageToShowIn.getHeight() - getHeight() - getAdjustmentForStatusBar() - 6);
  }

  protected double getAdjustmentForStatusBar() {
    Parent root = stageToShowIn.getScene().getRoot();
    if(root instanceof BorderPane) {
      BorderPane rootPane = (BorderPane)root;
      if(rootPane.getBottom() instanceof Region)
        return ((Region)rootPane.getBottom()).getHeight();
    }

    return 0;
  }

  protected void hideThreadSafe() {
    if(Platform.isFxApplicationThread())
      hide();
    else
      Platform.runLater(() -> hide());
  }

  protected void createCreateEntryFromOnlineArticleOptions(ContentExtractOptions contentExtractOptions) {
    final ContentExtractOption contentExtractOption = contentExtractOptions.getContentExtractOptions().get(0);
    final IOnlineArticleContentExtractor contentExtractor = (IOnlineArticleContentExtractor)contentExtractOption.getContentExtractor();

    headerLabel.setText(Localization.getLocalizedString("ask.create.entry.from.online.article",
        ((IOnlineArticleContentExtractor) contentExtractOptions.getContentExtractOptions().get(0).getContentExtractor()).getSiteBaseUrl())); // TODO: is it always true that  OnlineArticleContentExtractors contain only one IContentExtractor?

    addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, "create.entry.from.online.article.option.directly.add.entry", InputManager.getInstance().getCreateEntryFromClipboardDirectlyAddEntryKeyCombination(),
        options -> directlyAddEntryFromOnlineArticle(contentExtractOption, contentExtractor));

    Control viewNewEntryFirstOption = addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, "create.entry.from.online.article.option.view.new.entry.first", InputManager.getInstance().getCreateEntryFromClipboardViewNewEntryFirstKeyCombination(),
        options -> createEntryFromOnlineArticleButViewFirst(contentExtractOption, contentExtractor));

    VBox.setMargin(viewNewEntryFirstOption, new Insets(0, 10, 7, 18));
  }

  public void createEntryFromOnlineArticleButViewFirst(ContentExtractOption contentExtractOption, IOnlineArticleContentExtractor contentExtractor) {
    contentExtractor.createEntryFromClipboardContentAsync(contentExtractOption, result -> {
      if (result.successful())
        Dialogs.showEditEntryDialog(result);
      else
        showCouldNotCreateEntryError(result);
      hideThreadSafe();
    });
  }

  public void directlyAddEntryFromOnlineArticle(ContentExtractOption contentExtractOption, IOnlineArticleContentExtractor contentExtractor) {
    contentExtractor.createEntryFromClipboardContentAsync(contentExtractOption, result -> {
      if (result.successful())
        result.saveCreatedEntities();
      else
        showCouldNotCreateEntryError(result);

      hideThreadSafe();
    });
  }

  protected void createCreateEntryFromFileOptions(final ContentExtractOptions contentExtractOptions) {
    headerLabel.setText(Localization.getLocalizedString("ask.create.entry.from.local.file",
        FileUtils.getFileNameIncludingExtension((String) contentExtractOptions.getSource())));

    Control lastOption = null;

    if(contentExtractOptions.canSetFileAsEntryContent()) {
      lastOption = addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, "create.entry.from.local.file.option.set.as.entry.content",
          InputManager.getInstance().getCreateEntryFromClipboardSetAsEntryContentKeyCombination(), options -> copyFileToDataFolderAndSetAsEntryContent(options));
    }

    if(contentExtractOptions.canAttachFileToEntry()) {
      lastOption = addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, "create.entry.from.local.file.option.add.as.file.attachment",
          InputManager.getInstance().getCreateEntryFromClipboardAddAsFileAttachmentKeyCombination(), options -> attachFileToEntry(options));
    }

    if(contentExtractOptions.canExtractText()) {
      lastOption = addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, "create.entry.from.local.file.option.try.to.extract.text.from.it",
          InputManager.getInstance().getCreateEntryFromClipboardTryToExtractTextKeyCombination(), options -> tryToExtractText(options));
    }

    if(contentExtractOptions.canAttachFileToEntry() && contentExtractOptions.canExtractText()) {
      lastOption = addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, "create.entry.from.local.file.option.add.as.file.attachment.and.try.to.extract.text.from.it",
          InputManager.getInstance().getCreateEntryFromClipboardAddAsFileAttachmentAndTryToExtractTextKeyCombination(), options -> attachFileToEntryAndTryToExtractText(options));
    }

    if(lastOption != null)
      VBox.setMargin(lastOption, new Insets(0, 10, 7, 18));
  }

  public void setFileAsEntryContent(ContentExtractOptions options) {
//    final ContentExtractOption setFileAsEntryContentOption = options.getSetFileAsEntryContentOption();
//    FileLink newFile = ((ILocalFileContentExtractor)setFileAsEntryContentOption.getContentExtractor()).createFileLink(setFileAsEntryContentOption);
    final FileLink newFile = new FileLink(options.getUrl());

    Entry newEntry = new Entry();
    newEntry.setContent("<html dir=\"ltr\"><head></head><body contenteditable=\"true\"><p><img src=\"file://" + newFile.getUriString() + "\" " +
        "/></p><p></p></body></html>");
    Dialogs.showEditEntryDialog(newEntry);
  }

  public void copyFileToDataFolderAndSetAsEntryContent(final ContentExtractOptions options) {
//    final ContentExtractOption setFileAsEntryContentOption = options.getSetFileAsEntryContentOption();
//    final FileLink newFile = ((ILocalFileContentExtractor)setFileAsEntryContentOption.getContentExtractor()).createFileLink(setFileAsEntryContentOption);
    final FileLink newFile = new FileLink(options.getUrl());
    final String fileUrl = newFile.getUriString();

    if(FileUtils.isRemoteFile(fileUrl) && Application.getDownloader().canDownloadUrl(fileUrl)) {
      downloadToTempFile(fileUrl, (successful, tempFile) -> {
        if(successful) {
          String fileName = newFile.getName();
          newFile.setUriString(tempFile.getAbsolutePath());
          newFile.setName(fileName);
          copyFileToDataFolderAndSetAsEntryContent(options, newFile);
        }
      });
    }
    else
      copyFileToDataFolderAndSetAsEntryContent(options, newFile);
  }

  protected void copyFileToDataFolderAndSetAsEntryContent(final ContentExtractOptions options, FileLink newFile) {
    FileUtils.copyFileToDataFolder(newFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        showCouldNotCreateEntryError(options.getSource(), error);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        if (successful) {
          Entry newEntry = new Entry();
          newEntry.setContent("<html dir=\"ltr\"><head></head><body contenteditable=\"true\"><p><img src=\"file://" + destinationFile.getAbsolutePath() + "\" " +
              "/></p><p></p></body></html>");
          Dialogs.showEditEntryDialog(newEntry);
        }
      }
    });
  }

  public void attachFileToEntry(ContentExtractOptions options) {
//    ContentExtractOption attachFileOption = options.getAttachFileToEntryOption();
//    final FileLink newFile = ((ILocalFileContentExtractor)attachFileOption.getContentExtractor()).createFileLink(attachFileOption);
    final FileLink newFile = new FileLink(options.getUrl());
    Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok) {
          Entry newEntry = new Entry();
          newEntry.addAttachedFile(newFile);
          Dialogs.showEditEntryDialog(newEntry);
        }
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  public void tryToExtractText(ContentExtractOptions options) {
    final ContentExtractOption extractTextOption = options.getExtractTextOption();
    final IOcrContentExtractor textContentExtractor = (IOcrContentExtractor)extractTextOption.getContentExtractor();

    if(options.isRemoteFile()) {
      downloadToTempFile(extractTextOption.getUrl(), (successful, tempFile) -> {
        if(successful) {
          extractTextOption.setSource(tempFile.getAbsolutePath());
          tryToExtractText(extractTextOption, textContentExtractor);
        }
      });
    }
    else {
      tryToExtractText(extractTextOption, textContentExtractor);
    }
  }

  protected void tryToExtractText(ContentExtractOption extractTextOption, IOcrContentExtractor textContentExtractor) {
    textContentExtractor.createEntryFromClipboardContentAsync(extractTextOption, result -> {
      if (result.successful())
        Dialogs.showEditEntryDialog(result);
      else
        showCouldNotCreateEntryError(result);
    });
  }

  public void attachFileToEntryAndTryToExtractText(final ContentExtractOptions options) {
//    ContentExtractOption attachFileOption = options.getAttachFileToEntryOption();
//    final FileLink newFile = ((ILocalFileContentExtractor)attachFileOption.getContentExtractor()).createFileLink(attachFileOption);
    final FileLink newFile = new FileLink(options.getUrl());
    Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok) {
          ContentExtractOption extractTextOption = options.getExtractTextOption();
          IOcrContentExtractor textContentExtractor = (IOcrContentExtractor)extractTextOption.getContentExtractor();
          textContentExtractor.createEntryFromUrlAsync(newFile.getUriString(), result -> {
            if (result.successful()) {
              Entry newEntry = result.getCreatedEntry();
              newEntry.addAttachedFile(newFile);
              Dialogs.showEditEntryDialog(result);
            } else
              showCouldNotCreateEntryError(result);
          });
        }
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  protected void addOptionToCreateEntryFromContentExtractorPopup(ContentExtractOptions contentExtractOptions, String optionNameResourceKey, OptionInvokedListener listener) {
    addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, optionNameResourceKey, null, listener);
  }

  protected Control addOptionToCreateEntryFromContentExtractorPopup(final ContentExtractOptions contentExtractOptions,
                                                                    String optionNameResourceKey, KeyCombination optionKeyCombination, final OptionInvokedListener listener) {
    String optionText = Localization.getLocalizedString(optionNameResourceKey);
    if(optionKeyCombination != null)
      optionText += " (" + optionKeyCombination.getDisplayText() + ")";

//    final Hyperlink optionLink = new Hyperlink(optionText);
    final Button optionLink = new Button(optionText);
    optionLink.setUnderline(true);
    optionLink.setTextFill(Color.BLACK);
    optionLink.setBackground(Background.EMPTY);
    optionLink.setCursor(Cursor.HAND);
//    optionLink.setBorder(new Border(new BorderStroke(Color.DARKSLATEGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2))));
    optionLink.setPrefHeight(24);
    optionsPane.getChildren().add(optionLink);
    VBox.setMargin(optionLink, new Insets(0, 10, 6, 18));

    if(optionKeyCombination != null)
      optionLink.getScene().getAccelerators().put(optionKeyCombination, () -> optionLink.fire());

    optionLink.setOnAction(action -> {
      CreateEntryFromClipboardContentPopup.this.contentPane.setDisable(true);
      if(listener != null)
        listener.optionInvoked(contentExtractOptions);
    });

    optionLink.setOnMouseEntered(event -> optionLink.setBackground(Constants.ClipboardContentPopupOptionMouseOverBackground));
    optionLink.setOnMouseExited(event -> optionLink.setBackground(Background.EMPTY));

    return optionLink;
  }


  protected interface DownloadToTempFileResult {
    public void completed(boolean successful, File tempFile);
  }

  protected void downloadToTempFile(final String url, final DownloadToTempFileResult result) {
    try {
      final File tempFile = File.createTempFile("DeepThoughtDownload_" + FileUtils.getFileName(url), FileUtils.getFileExtension(url));
      Application.getDownloader().downloadAsync(new DownloadConfig(url, tempFile.getAbsolutePath()), new DownloadListener() {
        @Override
        public void progress(DownloadConfig download, float percentage) {

        }

        @Override
        public void downloadCompleted(DownloadConfig download, boolean successful, DeepThoughtError error) {
          if(successful == false) {
            log.error("Could not download file " + url + ": " + error);
            // TODO: notify user
          }

          if(result != null)
            result.completed(successful, tempFile);
        }
      });
    } catch(Exception ex) {
      log.error("Could not download file " + url, ex);
      // TODO: notify user

      if(result != null)
        result.completed(false, null);
    }
  }

  protected void showCouldNotCreateEntryError(EntryCreationResult result) {
    showCouldNotCreateEntryError(result.getSource(), result.getError());
  }

  protected void showCouldNotCreateEntryError(Object source, DeepThoughtError error) {
    log.error("Could not create Entry from Source " + source, error.getException());
    Alerts.showErrorMessage(stageToShowIn, error.getNotificationMessage(), Localization.getLocalizedString("can.not.create.entry.from", source), error.getException());
  }
}
