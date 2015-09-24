package net.deepthought.controls.file.cells;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.event.IMouseAndKeyEventReceiver;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.Alerts;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by ganymed on 27/12/14.
 */
public abstract class FileTreeTableCellBase extends TreeTableCell<FileLink, String> {

  private final static Logger log = LoggerFactory.getLogger(FileTreeTableCellBase.class);


  protected FileLink file = null;


  protected HBox graphicPane = new HBox();

  protected Label cellLabel = new Label();


  public FileTreeTableCellBase() {
    setText(null);
    setupGraphic();

    tableRowProperty().addListener(new ChangeListener<TreeTableRow<FileLink>>() {
      @Override
      public void changed(ObservableValue<? extends TreeTableRow<FileLink>> observable, TreeTableRow<FileLink> oldValue, TreeTableRow<FileLink> newValue) {
        if (newValue != null) {
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

    setOnContextMenuRequested(event -> showContextMenu(event));
  }


  protected void setupGraphic() {
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.BASELINE_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);
    cellLabel.setMaxWidth(Double.MAX_VALUE);

    graphicPane.getChildren().add(cellLabel);
    HBox.setHgrow(cellLabel, Priority.ALWAYS);
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
      cellLabel.setText(getCellTextRepresentation());
      setGraphic(graphicPane);
    }
  }

  protected abstract String getCellTextRepresentation();


  protected void showContextMenu(ContextMenuEvent event) {
    ContextMenu contextMenu = createContextMenu();

    contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem editFileItem = new MenuItem(Localization.getLocalizedString("edit"));
    editFileItem.setOnAction(event -> showEditFileDialog());
    contextMenu.getItems().add(editFileItem);

    MenuItem saveFileAsItem = new MenuItem(Localization.getLocalizedString("save.as..."));
    saveFileAsItem.setDisable(true);
    saveFileAsItem.setOnAction(event -> saveFileAs());
    contextMenu.getItems().add(saveFileAsItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem openInDefaultApplicationItem = new MenuItem(Localization.getLocalizedString("view.file"));
    openInDefaultApplicationItem.setOnAction(event -> openFileInDefaultApplication());
    contextMenu.getItems().add(openInDefaultApplicationItem);

    MenuItem showFileInFileManagerItem = new MenuItem(Localization.getLocalizedString("show.file.in.file.manager"));
    showFileInFileManagerItem.setDisable(true);
    showFileInFileManagerItem.setOnAction(event -> showFileInFileManager());
    contextMenu.getItems().add(showFileInFileManagerItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteFileItem = new MenuItem(Localization.getLocalizedString("delete.file"));
    deleteFileItem.setOnAction(event -> deleteFile());
    contextMenu.getItems().add(deleteFileItem);

    return contextMenu;
  }

  protected void showEditFileDialog() {
    Dialogs.showEditFileDialog(file);
  }

  protected void saveFileAs() {
    // TODO
  }

  protected void openFileInDefaultApplication() {
    FileUtils.openFileInOperatingSystemDefaultApplication(file);
  }

  protected void showFileInFileManager() {
    FileUtils.showFileInFileManager(file);
  }

  protected void deleteFile() {
    Alerts.deleteFileWithUserConfirmationIfIsSetOnEntriesOrReferenceBases(file);
  }

  protected void selectCurrentCell() {
    getTreeTableView().getSelectionModel().select(getIndex());
  }

  protected void itemChanged(FileLink newValue) {
    this.file = newValue;

    if(file == null) {
      setGraphic(null);
    }
    else {
      cellLabel.setText(getCellTextRepresentation());
      setTooltip(new Tooltip(file.getUriString() + " (" + file.getDescription() + ")"));
    }
  }


}
