package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controller.enums.FileLinkOptions;
import net.deepthought.controls.html.HtmlEditor;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileNameSuggestion;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.file.enums.ExistingFileHandling;
import net.deepthought.util.file.listener.FileOperationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

/**
 * Created by ganymed on 31/12/14.
 */
public class EditEmbeddedFileDialogController extends EntityDialogFrameController implements Initializable {

  public final static Background WrongFileTypeEnteredBackground = new Background(new BackgroundFill(Color.RED, new CornerRadii(4), new Insets(0)));

  public static Background DefaultBackground;

  public static String DefaultTextFieldStyle;


  private final static Logger log = LoggerFactory.getLogger(EditEmbeddedFileDialogController.class);


  protected FileLink file;

  protected HtmlEditor editor;

  protected ImageElementData imgElement;

  protected String previousUriString = null;


  @FXML
  protected Pane pnFileSettings;
  @FXML
  protected TextField txtfldFileLocation;
  @FXML
  protected ComboBox<FileLinkOptions> cmbxLocalFileLinkOptions;
  @FXML
  protected Label lblHtmlIncompatibleImageTypeSelected;

  @FXML
  protected Spinner<Integer> spnImageWidth;
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

    cmbxLocalFileLinkOptions.setItems(FXCollections.observableArrayList(FileLinkOptions.values()));
    cmbxLocalFileLinkOptions.getSelectionModel().select(FileLinkOptions.Link);

    JavaFxLocalization.bindControlToolTip(lblHtmlIncompatibleImageTypeSelected, "incompatible.html.image");

    // TODO: prevent that in txtfldImageWidth and txtfldImageWidth other symbols than figures can be entered

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
    spnImageWidth.getEditor().textProperty().addListener((observable, oldValue, newValue) -> spnImageWidth.getValueFactory().setValue(Integer.parseInt(newValue)));
    spnImageWidth.valueProperty().addListener((observable, oldValue, newValue) -> {
      if(imgElement != null)
        updateImageElement();
    });

    spnImageHeight.setEditable(true);
    spnImageHeight.getEditor().setAlignment(Pos.CENTER_RIGHT);
    spnImageHeight.getEditor().setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), ImageElementData.DefaultImageHeight, filter));
    spnImageHeight.getEditor().textProperty().addListener((observable, oldValue, newValue) -> spnImageHeight.getValueFactory().setValue(Integer.parseInt(newValue)));
    spnImageHeight.valueProperty().addListener((observable, oldValue, newValue) -> {
      if(imgElement != null)
        updateImageElement();
    });

    txtfldFileName.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileName);
      updateWindowTitle();
    });

    txtarDescription.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileNotes));
  }

  protected void fileLocationChanged() {
    fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileFileLocation);

    String uri = txtfldFileLocation.getText();

    if(FileUtils.isFileEmbeddableInHtml(uri)) {
      btnApplyChanges.setDisable(false);
      btnOk.setDisable(false);
      lblHtmlIncompatibleImageTypeSelected.setVisible(false);
      txtfldFileLocation.setStyle(DefaultTextFieldStyle);

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

  protected void updateFileName(String uriString) {
    if(uriString == null)
      return;

    try {
        if(previousUriString == null || previousUriString.endsWith(txtfldFileName.getText())) {
          File file = new File(uriString);
          txtfldFileName.setText(file.getName());
        }
        else if(previousUriString != null && previousUriString.endsWith("/") &&
            (txtfldFileName.getText() == null || txtfldFileName.getText().isEmpty() || previousUriString.endsWith(txtfldFileName.getText() + "/"))) {
          File file = new File(uriString);
          txtfldFileName.setText(file.getName());
        }
    } catch(Exception ex) {
      log.debug("Could not extract file's name from uriString " + uriString, ex);
    }

    previousUriString = uriString;
  }

  public void setEditFile(Stage dialogStage, HtmlEditor editor, FileLink file, ImageElementData imgElement) {
    this.file = file;
    this.editor = editor;
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
    else {
      spnImageWidth.getValueFactory().setValue(ImageElementData.DefaultImageWidth);
      spnImageHeight.getValueFactory().setValue(ImageElementData.DefaultImageHeight);
    }

    txtarDescription.setText(file.getDescription());
  }


  @FXML
  public void handleButtonSelectFileAction(ActionEvent event) {
    // TODO: set file filter to Html compatible image types
    FileChooser fileChooser = new FileChooser();

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

    updateImageElement();
  }

  protected void updateImageElement() {
    if(file.isPersisted() == false) // File is not persisted and therefore not embedded yet in Html
      return;

    ImageElementData newImgElement = createImageElement(file);
    if(imgElement == null)
      editor.insertHtml(newImgElement.getHtmlCode());
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
