package net.dankito.deepthought.controller;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controller.enums.FileLinkOptions;
import net.dankito.deepthought.controls.Constants;
import net.dankito.deepthought.controls.file.SearchAndSelectFilesControl;
import net.dankito.deepthought.controls.html.HtmlEditor;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.controls.utils.IEditedEntitiesHolder;
import net.dankito.deepthought.data.html.ImageElementData;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.util.Alerts;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.file.FileNameSuggestion;
import net.dankito.deepthought.util.file.FileUtils;
import net.dankito.deepthought.util.file.enums.ExistingFileHandling;
import net.dankito.deepthought.util.file.listener.FileOperationListener;
import net.dankito.deepthought.util.localization.JavaFxLocalization;
import net.dankito.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.function.UnaryOperator;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

/**
 * Created by ganymed on 31/12/14.
 */
public class EditEmbeddedFileDialogController extends EntityDialogFrameController implements Initializable {

  protected final static ImageView LockClosedIcon = new ImageView(Constants.LockClosedIconPath);

  protected final static ImageView LockOpenedIcon = new ImageView(Constants.LockOpenedIconPath);

  public static Background DefaultBackground;

  public static String DefaultTextFieldStyle;


  private final static Logger log = LoggerFactory.getLogger(EditEmbeddedFileDialogController.class);


  protected FileLink file;

  protected HtmlEditor editor;

  protected IEditedEntitiesHolder<FileLink> editedFiles;

  protected ImageElementData imgElement;

  protected String previousUriString = null;

  protected int originalImageWidth = ImageElementData.DefaultImageWidth;
  protected int originalImageHeight = ImageElementData.DefaultImageHeight;
  protected double ratio = (double)ImageElementData.DefaultImageWidth / ImageElementData.DefaultImageHeight;


  @FXML
  protected Pane pnFileSettings;
  @FXML
  protected VBox upperPane;
  @FXML
  protected TextField txtfldFileLocation;
  @FXML
  protected ToggleButton tglbtnShowSearchPane;

  protected SearchAndSelectFilesControl searchAndSelectFilesControl;

  @FXML
  protected ComboBox<FileLinkOptions> cmbxLocalFileLinkOptions;
  @FXML
  protected Label lblHtmlIncompatibleImageTypeSelected;

  @FXML
  protected HBox pnImageSize;
  @FXML
  protected Spinner<Integer> spnImageWidth;
  @FXML
  protected ToggleButton tglbtnPreserveImageRatio;
  @FXML
  protected Spinner<Integer> spnImageHeight;
  @FXML
  protected CheckBox chkbxAlsoAttachToEntity;

  @FXML
  protected TextField txtfldFileName;

  @FXML
  protected TextArea txtarDescription;


  @Override
  protected String getEntityType() {
    return "file";
  }


  @Override
  protected void setupControls() {
    super.setupControls();

    DefaultBackground = txtfldFileLocation.getBackground();
    DefaultTextFieldStyle = txtfldFileLocation.getStyle();
    txtfldFileLocation.textProperty().addListener((observable, oldValue, newValue) -> {
      fileLocationChanged();
    });

    searchAndSelectFilesControl = new SearchAndSelectFilesControl(true, SelectionMode.SINGLE, event -> setSelectedFile(event.getSelectedEntity()));
    searchAndSelectFilesControl.setVisible(false);
    searchAndSelectFilesControl.setMaxHeight(250);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(searchAndSelectFilesControl);
    upperPane.getChildren().add(1, searchAndSelectFilesControl);
    VBox.setMargin(searchAndSelectFilesControl, new Insets(6, 0, 6, 0));

    searchAndSelectFilesControl.visibleProperty().bind(tglbtnShowSearchPane.selectedProperty());

    tglbtnShowSearchPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowSearchPane.setGraphic(new ImageView(Constants.SearchIconPath));

    cmbxLocalFileLinkOptions.setItems(FXCollections.observableArrayList(FileLinkOptions.values()));
    cmbxLocalFileLinkOptions.getSelectionModel().select(FileLinkOptions.Link);

    JavaFxLocalization.bindControlToolTip(lblHtmlIncompatibleImageTypeSelected, "incompatible.html.image");

    setupPaneImageSize();

    txtfldFileName.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileName);
      updateWindowTitle();
    });

    txtarDescription.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileNotes));
  }

  protected void setupPaneImageSize() {
    spnImageWidth = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000));
    spnImageWidth.setMinWidth(80);
    spnImageWidth.setMaxWidth(80);
    spnImageWidth.setMinHeight(30);
    spnImageWidth.setMaxHeight(30);
    pnImageSize.getChildren().add(1, spnImageWidth);
    HBox.setMargin(spnImageWidth, new Insets(0, 0, 0, 4));

    tglbtnPreserveImageRatio.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnPreserveImageRatio.setGraphic(LockClosedIcon);
    tglbtnPreserveImageRatio.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == true)
        tglbtnPreserveImageRatio.setGraphic(LockClosedIcon);
      else
        tglbtnPreserveImageRatio.setGraphic(LockOpenedIcon);
    });

    spnImageHeight = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000));
    spnImageHeight.setMinWidth(80);
    spnImageHeight.setMaxWidth(80);
    spnImageHeight.setMinHeight(30);
    spnImageHeight.setMaxHeight(30);
    pnImageSize.getChildren().add(4, spnImageHeight);
    HBox.setMargin(spnImageHeight, new Insets(0, 30, 0, 4));

    NumberFormat format = NumberFormat.getIntegerInstance();
    UnaryOperator<TextFormatter.Change> filter = c -> {
      if (c.isContentChange()) {
        ParsePosition parsePosition = new ParsePosition(0);
        // NumberFormat evaluates the beginning of the text
        format.parse(c.getControlNewText(), parsePosition);
        if (parsePosition.getIndex() == 0 ||
            parsePosition.getIndex() < c.getControlNewText().length()) {
          // reject parsing the complete text failed
          return null;
        }
      }
      return c;
    };

    // There seems to be a bug in Spinner implementation as sometimes typing text into TextField causes an Exception.
    // You don't see it on the surface, but the (uncatchable) Exceptions are being logged
    spnImageWidth.setEditable(true);
    spnImageWidth.getEditor().setAlignment(Pos.CENTER_RIGHT);
    spnImageWidth.getEditor().setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), ImageElementData.DefaultImageWidth, filter));
    spnImageWidth.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (imgElement != null)
        updateImageElement();
    });
    spnImageWidth.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue == false && tglbtnPreserveImageRatio.isSelected())
        spnImageHeight.getValueFactory().setValue((int) (spnImageWidth.getValue() / ratio));
    });
    spnImageWidth.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      spnImageWidth.getValueFactory().setValue(Integer.parseInt(newValue));
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileImageWidth);
    });

    spnImageHeight.setEditable(true);
    spnImageHeight.getEditor().setAlignment(Pos.CENTER_RIGHT);
    spnImageHeight.getEditor().setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), ImageElementData.DefaultImageHeight, filter));
    spnImageHeight.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (imgElement != null)
        updateImageElement();
    });
    spnImageHeight.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue == false && tglbtnPreserveImageRatio.isSelected())
        spnImageWidth.getValueFactory().setValue((int) (spnImageHeight.getValue() * ratio));
    });
    spnImageHeight.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      spnImageHeight.getValueFactory().setValue(Integer.parseInt(newValue));
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileImageHeight);
    });
  }

  protected void setSelectedFile(FileLink selectedFile) {
    this.file = selectedFile;
    fileToEditSet(file, null);
  }

  protected void fileLocationChanged() {
    fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileFileLocation);

    String uri = txtfldFileLocation.getText();

    if(FileUtils.isFileEmbeddableInHtml(uri)) {
      btnApplyChanges.setDisable(false);
      btnOk.setDisable(false);
      lblHtmlIncompatibleImageTypeSelected.setVisible(false);
      txtfldFileLocation.setStyle(DefaultTextFieldStyle);

      tryToReadImageHeightAndWidth(uri);

      updateWindowTitle();
      updateFileName(uri);

      try {
        String scheme = new URI(uri).getScheme();
        cmbxLocalFileLinkOptions.setDisable(scheme != null && "file".equals(scheme) == false);
      } catch (Exception ex) {
      }
    }
    else {
      btnApplyChanges.setDisable(true);
      btnOk.setDisable(true);
      lblHtmlIncompatibleImageTypeSelected.setVisible(true);
      FXUtils.addStyleToCurrentStyle(txtfldFileLocation, "-fx-text-fill: #FF0000;");
    }
  }

  protected void tryToReadImageHeightAndWidth(String uri) {
    try {
      if(uri.contains("://") == false) // TODO: is it absolutely certain that it's then a local file?
        uri = "file://" + uri;
      BufferedImage image = ImageIO.read(new URL(uri));

      originalImageWidth = image.getWidth();
      spnImageWidth.getValueFactory().setValue(originalImageWidth);

      originalImageHeight = image.getHeight();
      spnImageHeight.getValueFactory().setValue(originalImageHeight);
    } catch(Exception ex) {
      log.warn("Could not load image from uri " + uri, ex);
    }

    ratio = (double)originalImageWidth / originalImageHeight;
  }

  protected void updateFileName(String uriString) {
    if(uriString == null)
      return;

    try {
        if(previousUriString == null || previousUriString.endsWith(txtfldFileName.getText())) {
          File file = new File(uriString);
          txtfldFileName.setText(file.getName());
        }
        else if(previousUriString != null && previousUriString.endsWith("/") &&
            (StringUtils.isNullOrEmpty(txtfldFileName.getText()) || previousUriString.endsWith(txtfldFileName.getText() + "/"))) {
          File file = new File(uriString);
          txtfldFileName.setText(file.getName());
        }
    } catch(Exception ex) {
      log.debug("Could not extract file's name from uriString " + uriString, ex);
    }

    previousUriString = uriString;
  }

  public void setEditFile(Stage dialogStage, HtmlEditor editor, IEditedEntitiesHolder<FileLink> editedFiles, FileLink file, ImageElementData imgElement) {
    this.file = file;
    this.editor = editor;
    this.editedFiles = editedFiles;
    this.imgElement = imgElement;

    setWindowStage(dialogStage, file);

    fileToEditSet(file, imgElement);
    fieldsWithUnsavedChanges.clear();
  }

  protected void fileToEditSet(FileLink file, ImageElementData imgElement) {
    txtfldFileLocation.setText(file.getUriString());
    txtfldFileLocation.selectAll();
    txtfldFileLocation.requestFocus();

    if(imgElement != null) {
      spnImageWidth.getValueFactory().setValue(imgElement.getWidth());
      spnImageHeight.getValueFactory().setValue(imgElement.getHeight());
    }
    else if(StringUtils.isNotNullOrEmpty(file.getUriString())) {
      tryToReadImageHeightAndWidth(file.getUriString());
    }

    txtarDescription.setText(file.getDescription());
  }


  @FXML
  public void handleButtonSelectFileAction(ActionEvent event) {
    // TODO: set file filter to Html compatible image types
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(Localization.getLocalizedString("image.files") + " (*.jpg, *.png, *.gif, *.bmp, *.svg, *.ico)",
        "*.jpg", "*.png", "*.gif", "*.bmp", "*.svg", "*.ico"));

    if(txtfldFileLocation.getText() != null) {
      try {
        File currentFile = new File(txtfldFileLocation.getText());
        if(currentFile.exists())
          fileChooser.setInitialFileName(currentFile.getName());
        if(currentFile.getParentFile() != null && currentFile.getParentFile().exists())
          fileChooser.setInitialDirectory(currentFile.getParentFile());
      } catch (Exception ex) {
        log.debug("Could not extract file name and directory from " + txtfldFileLocation.getText(), ex);
      }
    }

    File selectedFile = fileChooser.showOpenDialog(windowStage);
    if(selectedFile != null) {
      txtfldFileLocation.setText(selectedFile.getAbsolutePath());
      txtfldFileLocation.positionCaret(txtfldFileLocation.getText().length());
      txtfldFileLocation.requestFocus();
    }
  }

  @Override
  protected void closeDialog() {
//    if(person != null)
//      person.removePersonListener(personListener);

    super.closeDialog();
  }

  @Override
  protected void saveEntity() {
    saveEditedFields();

    if(file.isPersisted() == false)
      Application.getDeepThought().addFile(file);
    if(editedFiles.containsEditedEntity(file) == false)
      editedFiles.addEntityToEntry(file);

    updateImageElement();
  }

  protected void updateImageElement() {
    if(file.isPersisted() == false) // File is not persisted and therefore not embedded yet in Html
      return;

    ImageElementData newImgElement = createImageElement(file);
    if(imgElement == null)
      editor.insertHtml(newImgElement.createHtmlCode());
    else {
      String currentHtml = editor.getHtml();
      ImageElementData currentImgElement = findImageElementInHtml(currentHtml, file, imgElement.getEmbeddingId());
      if(currentImgElement == null) // should actually never be the case
        return;

      newImgElement.setEmbeddingId(currentImgElement.getEmbeddingId());
      editor.replaceImageElement(currentImgElement, newImgElement);
    }

    this.imgElement = newImgElement;
  }

  protected void saveEditedFields() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.FileFileLocation)) {
      file.setUriString(txtfldFileLocation.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileFileLocation);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.FileName)) {
      if(txtfldFileName.getText().equals(file.getName()) == false)
        file.setName(txtfldFileName.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileName);
    }

    if(cmbxLocalFileLinkOptions.isDisabled() == false) {
      FileLinkOptions fileLinkOption = cmbxLocalFileLinkOptions.getSelectionModel().getSelectedItem();
      if(fileLinkOption == FileLinkOptions.CopyToDataFolder)
        copyFileToDataFolder();
      else if(fileLinkOption == FileLinkOptions.MoveToDataFolder)
        moveFileToDataFolder();
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.FileNotes)) {
      file.setDescription(txtarDescription.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileNotes);
    }

    file.setFileType(FileUtils.getFileType(file));

    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileImageWidth);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileImageHeight);
  }

  protected ImageElementData createImageElement(FileLink file) {
    int width = spnImageWidth.getValue();
    int height = spnImageHeight.getValue();

    return createImageElement(file, width, height);
  }

  protected ImageElementData createImageElement(FileLink file, int width, int height) {
//    return "<img src=\"" + file.getUriString() + "\" imageid=\"" + file.getId() + "\" width=\"" + width + "\" height=\"" + height + "\" alt=\"" + file.getDescription() + "\" />";
    // CKEditor sorts attributes alphabetically
//    return "<img alt=\"" + file.getDescription() + "\" height=\"" + height + "\" imageid=\"" + file.getId() + "\" src=\"" + file.getUriString() + "\" width=\"" + width + "\" />";

    return new ImageElementData(file, width, height);
  }

  protected ImageElementData findImageElementInHtml(String html, FileLink file, Long embeddingId) {
    // TODO: isn't it overhead to parse whole HTML code each time?
    List<ImageElementData> imgElements = Application.getHtmlHelper().extractAllImageElementsFromHtml(html);

    for(ImageElementData imgElement : imgElements) {
      if(file.getId().equals(imgElement.getFileId()) && embeddingId.equals(imgElement.getEmbeddingId()))
        return imgElement;
    }

    return null;
  }


  protected void copyFileToDataFolder() {
    FileUtils.copyFileToDataFolder(file, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        // TODO: ask user what to do
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(final DeepThoughtError error) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            Alerts.showErrorMessage(windowStage, Localization.getLocalizedString("error.could.not.copy.file.to.destination"), error.getNotificationMessage());
          }
        });
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        if (successful)
          file = new FileLink(destinationFile.getPath());
      }
    });
  }

  protected void moveFileToDataFolder() {
    FileUtils.moveFileToDataFolder(file, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        // TODO: ask user what to do
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(final DeepThoughtError error) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            Alerts.showErrorMessage(windowStage, Localization.getLocalizedString("error.could.not.copy.file.to.destination"), error.getNotificationMessage());
          }
        });
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        if (successful)
          file = new FileLink(destinationFile.getPath());
      }
    });
  }

}
