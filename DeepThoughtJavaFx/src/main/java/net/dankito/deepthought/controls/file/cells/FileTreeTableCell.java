package net.dankito.deepthought.controls.file.cells;

import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.model.FileLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by ganymed on 27/12/14.
 */
public class FileTreeTableCell extends TreeTableCell<FileLink, String> {

  private final static Logger log = LoggerFactory.getLogger(FileTreeTableCell.class);


  protected FileLink file = null;
  protected net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<FileLink> editedFiles = null;

  protected HBox graphicPane = new HBox();

  protected CheckBox isOnEntityCheckBox = new CheckBox();

  protected Label fileNameLabel = new Label();


  protected HBox entryFileOptionsButtonsPane = new HBox();

  protected Button removeFileButton = new Button();
  protected Button editFileButton = new Button();
  protected Button saveFileAsButton = new Button();
  protected Button showFileInFileManagerButton = new Button();
  protected Button viewFileButton = new Button();


  public FileTreeTableCell(net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<FileLink> editedFiles) {
    this.editedFiles = editedFiles;

    setText(null);
    setupGraphic();

    tableRowProperty().addListener(new ChangeListener<TreeTableRow<FileLink>>() {
      @Override
      public void changed(ObservableValue<? extends TreeTableRow<FileLink>> observable, TreeTableRow<FileLink> oldValue, TreeTableRow<FileLink> newValue) {
        if(newValue != null) {
          itemChanged(newValue.getItem());

          newValue.itemProperty().addListener(new ChangeListener<FileLink>() {
            @Override
            public void changed(ObservableValue<? extends FileLink> observable, FileLink oldValue, FileLink newValue) {
              itemChanged(newValue);
            }
          });
        }
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));
  }

  protected void setupGraphic() {
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.BASELINE_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);

    HBox.setHgrow(fileNameLabel, Priority.ALWAYS);
    HBox.setMargin(fileNameLabel, new Insets(0, 6, 0, 6));
    HBox.setMargin(removeFileButton, new Insets(0, 6, 0, 0));
    HBox.setMargin(editFileButton, new Insets(0, 6, 0, 0));
    HBox.setMargin(saveFileAsButton, new Insets(0, 6, 0, 0));
    HBox.setMargin(showFileInFileManagerButton, new Insets(0, 6, 0, 0));

//    graphicPane.getChildren().add(isOnEntityCheckBox);

    fileNameLabel.setMaxWidth(FXUtils.SizeMaxValue);
    graphicPane.getChildren().add(fileNameLabel);

    entryFileOptionsButtonsPane.setAlignment(Pos.CENTER_LEFT);
    entryFileOptionsButtonsPane.managedProperty().bind(entryFileOptionsButtonsPane.visibleProperty());
    graphicPane.getChildren().add(entryFileOptionsButtonsPane);

    net.dankito.deepthought.util.localization.JavaFxLocalization.bindLabeledText(removeFileButton, "delete");
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(removeFileButton, "remove.file.from.entry.tool.tip");
    removeFileButton.setMinWidth(80);
    entryFileOptionsButtonsPane.getChildren().add(removeFileButton);
    removeFileButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleRemoveFileButtonAction();
      }
    });

    net.dankito.deepthought.util.localization.JavaFxLocalization.bindLabeledText(editFileButton, "edit");
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(editFileButton, "edit.file.tool.tip");
    editFileButton.setMinWidth(80);
    entryFileOptionsButtonsPane.getChildren().add(editFileButton);
    editFileButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleEditFileButtonAction();
      }
    });

    net.dankito.deepthought.util.localization.JavaFxLocalization.bindLabeledText(saveFileAsButton, "save.as...");
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(saveFileAsButton, "save.file.as.tool.tip");
    saveFileAsButton.setMinWidth(80);
    entryFileOptionsButtonsPane.getChildren().add(saveFileAsButton);
    saveFileAsButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleSaveFileAsButtonAction();
      }
    });
    saveFileAsButton.setDisable(true);

    net.dankito.deepthought.util.localization.JavaFxLocalization.bindLabeledText(showFileInFileManagerButton, "show.file.in.file.manager");
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(showFileInFileManagerButton, "show.file.in.file.manager.tool.tip");
    showFileInFileManagerButton.setMinWidth(80);
    graphicPane.getChildren().add(showFileInFileManagerButton);
    showFileInFileManagerButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleShowFileInFileManagerButtonAction();
      }
    });
    showFileInFileManagerButton.setDisable(true);

    net.dankito.deepthought.util.localization.JavaFxLocalization.bindLabeledText(viewFileButton, "view");
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(viewFileButton, "view.file.tool.tip");
    viewFileButton.setMinWidth(80);
    graphicPane.getChildren().add(viewFileButton);
    viewFileButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleViewFileButtonAction();
      }
    });
  }


  @Override
  protected void updateItem(String item, boolean empty) {
    Object fileCheck = getTreeTableRow().getItem();
    if(fileCheck != file && fileCheck instanceof FileLink)
      itemChanged((FileLink) fileCheck);

    super.updateItem(item, empty);

    if(empty) {
      setGraphic(null);
    }
    else {
      if(file != null)
        fileNameLabel.setText(file.getTextRepresentation());
      setGraphic(graphicPane);
    }
  }


  protected void mouseClicked(MouseEvent event) {
    if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
      if(file.isPersisted() == false) // a Folder sub file, not a file added to Entry
        viewFile();
      else
        net.dankito.deepthought.controller.Dialogs.showEditFileDialog(file);
    }
  }

  protected void selectCurrentCell() {
    getTreeTableView().getSelectionModel().select(getIndex());
  }

  protected void itemChanged(FileLink newValue) {
    this.file = newValue;

    if(newValue == null) {
      setGraphic(null);
    }
    else {
      fileNameLabel.setText(newValue.getTextRepresentation());
      entryFileOptionsButtonsPane.setVisible(newValue.isPersisted());
      setTooltip(new Tooltip(file.getUriString()));
    }
  }


  protected void handleRemoveFileButtonAction() {
    if(editedFiles != null) {
      editedFiles.removeEntityFromEntry(file);
    }
  }

  protected void handleEditFileButtonAction() {
    net.dankito.deepthought.controller.Dialogs.showEditFileDialog(file);
  }

  protected void handleSaveFileAsButtonAction() {

  }

  protected void handleShowFileInFileManagerButtonAction() {

  }

  protected void handleViewFileButtonAction() {
    viewFile();
  }

  protected void viewFile() {
    try {
//        HostServices hostServices = DeepThoughtFx.hostServices();
//        hostServices.showDocument(file.getUriString());

//        ProcessBuilder.Redirect redirect = ProcessBuilder.Redirect.to(new java.io.File(file.getUriString()));
      String program = file.getUriString();
      String arguments = "";
      java.io.File directory = null;

      String os = System.getProperties().getProperty("os.name");
      log.debug("Running on {}", os);
      if(os.toLowerCase().contains("linux")) {
        arguments = program;
        program = "xdg-open";
        directory = new java.io.File("/");
      }
      // TODO: how to start default program on Windows and MacOs?

      ProcessBuilder builder = new ProcessBuilder(program, arguments);
      builder.directory(directory);
      Process process = builder.start();
    } catch(Exception ex) {
      String debug2 = ex.getMessage();
    }
  }


}
