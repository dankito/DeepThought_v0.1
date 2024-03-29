package net.dankito.deepthought.controls.file;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controller.ChildWindowsController;
import net.dankito.deepthought.controller.ChildWindowsControllerListener;
import net.dankito.deepthought.controller.Dialogs;
import net.dankito.deepthought.controller.enums.DialogResult;
import net.dankito.deepthought.controls.CollapsiblePane;
import net.dankito.deepthought.controls.Constants;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.file.cells.FileNameTreeTableCell;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.controls.utils.IEditedEntitiesHolder;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.file.FileUtils;
import net.dankito.deepthought.util.localization.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Created by ganymed on 01/02/15.
 */
public class FilesControl extends CollapsiblePane implements ICleanUp {

  protected final static Logger log = LoggerFactory.getLogger(FilesControl.class);
  public static final int ContentPaneMinHeight = 150;


  protected DeepThought deepThought = null;

  protected IEditedEntitiesHolder<FileLink> editedFiles = null;


  protected FlowPane pnSelectedFilesPreview;

  protected ToggleButton btnShowHideSearchPane;

  protected Button btnAddFile;


  protected VBox contentPane;

  @FXML
  protected TreeTableView<FileLink> trtblvwFiles;
  @FXML
  protected TreeTableColumn<FileLink, String> clmFileName;
  @FXML
  protected TreeTableColumn<FileLink, String> clmFileUri;


  protected SearchAndSelectFilesControl searchAndSelectFilesControl;


  public FilesControl(IEditedEntitiesHolder<FileLink> editedFiles) {
    setEditedFiles(editedFiles);
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    setupControl();

    if(deepThought != null)
      deepThought.addEntityListener(deepThoughtListener);
  }


  public void setEditedFiles(IEditedEntitiesHolder<FileLink> editedFiles) {
    if(this.editedFiles != null)
      this.editedFiles.getEditedEntities().removeListener(editedFilesChangedListener);

    this.editedFiles = editedFiles;

    if(editedFiles != null)
      editedFiles.getEditedEntities().addListener(editedFilesChangedListener);

    if(pnSelectedFilesPreview != null) // on setup / calling setEditedFiles() from constructor pnSelectedFilesPreview is still null
      updateFilesSetOnEntityPreview();
  }


  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      FilesControl.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };


  @Override
  public void cleanUp() {
    Application.removeApplicationListener(applicationListener);

    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    searchAndSelectFilesControl.cleanUp();

    clearSelectedFilesPreview();

    ((FileRootTreeItem)trtblvwFiles.getRoot()).cleanUp();

    if(editedFiles != null) {
      editedFiles.getEditedEntities().removeListener(editedFilesChangedListener);
      editedFiles.cleanUp();
    }
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
    }
  }

  protected void setupControl() {
    setMinHeight(22);

    setupTitle();
    this.setExpanded(false);

    contentPane = new VBox();
    contentPane.setMinHeight(ContentPaneMinHeight);
    contentPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
    contentPane.setMaxHeight(Region.USE_PREF_SIZE);
    contentPane.setMaxWidth(FXUtils.SizeMaxValue);
    contentPane.setPadding(new Insets(6, 0, 0, 0));

    clmFileName = new TreeTableColumn<>();
    JavaFxLocalization.bindTableColumnBaseText(clmFileName, "name");
    clmFileName.setMinWidth(Region.USE_PREF_SIZE);
    clmFileName.setPrefWidth(280);
    clmFileName.setMaxWidth(FXUtils.SizeMaxValue);

//    clmFileName.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileLink, String> p) ->
//        new ReadOnlyStringWrapper(p.getValue().getValue().getName()));
    clmFileName.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
      @Override
      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
        return new FileNameTreeTableCell(editedFiles);
      }
    });

    clmFileUri = new TreeTableColumn<>();
    JavaFxLocalization.bindTableColumnBaseText(clmFileUri, "uri");
    clmFileUri.setMinWidth(Region.USE_PREF_SIZE);
    clmFileUri.setPrefWidth(600);
    clmFileUri.setMaxWidth(FXUtils.SizeMaxValue);

//    clmFileUri.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileLink, String> p) ->
//        new ReadOnlyStringWrapper(p.getValue().getValue().getUriString()));
    clmFileUri.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
      @Override
      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
        return new net.dankito.deepthought.controls.file.cells.FileUriTreeTableCell(editedFiles);
      }
    });

    trtblvwFiles = new TreeTableView<>();
    trtblvwFiles.setShowRoot(false);
    trtblvwFiles.setRoot(new FileRootTreeItem(editedFiles));
    trtblvwFiles.getColumns().add(clmFileName);
    trtblvwFiles.getColumns().add(clmFileUri);
    trtblvwFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    trtblvwFiles.setMinHeight(150);
    trtblvwFiles.setMaxHeight(FXUtils.SizeMaxValue);

    trtblvwFiles.setOnKeyPressed(event -> {
      if(event.getCode() == KeyCode.ENTER) {
        editOrViewSelectedFile(event);
      }
      else if (event.getCode() == KeyCode.DELETE) {
        removeSelectedFiles();
        event.consume();
      }
    });
    trtblvwFiles.setOnMouseClicked(event -> {
      if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
        editOrViewSelectedFile(event);
      }
    });

    contentPane.getChildren().add(trtblvwFiles);

    searchAndSelectFilesControl = new SearchAndSelectFilesControl(editedFiles);
//    searchAndSelectFilesControl.setPrefHeight(250);
//    searchAndSelectFilesControl.setMaxHeight(200);
    searchAndSelectFilesControl.setMaxHeight(FXUtils.SizeMaxValue);

    btnShowHideSearchPane.selectedProperty().addListener((observable, oldValue, newValue) -> searchAndSelectFilesControl.setSearchPaneVisibility(newValue));

    contentPane.getChildren().add(searchAndSelectFilesControl);
    VBox.setMargin(searchAndSelectFilesControl, new Insets(8, 0, 0, 0));

    this.setContent(contentPane);
  }

  protected void setupTitle() {
    HBox titlePane = new HBox();
    titlePane.setAlignment(Pos.CENTER_LEFT);
//    titlePane.setMinHeight(22);
    titlePane.setMaxHeight(FXUtils.SizeMaxValue);
    titlePane.setMaxWidth(FXUtils.SizeMaxValue);

    Label lblFiles = new Label();
    JavaFxLocalization.bindLabeledText(lblFiles, "files");
    lblFiles.setPrefWidth(USE_COMPUTED_SIZE);
    lblFiles.setMinWidth(USE_PREF_SIZE);
    lblFiles.setMaxWidth(USE_PREF_SIZE);
    titlePane.getChildren().add(lblFiles);
    HBox.setMargin(lblFiles, new Insets(0, 6, 0, 0));

    pnSelectedFilesPreview = new FlowPane();
    pnSelectedFilesPreview.setMaxWidth(FXUtils.SizeMaxValue);
    pnSelectedFilesPreview.setVgap(2);
    pnSelectedFilesPreview.setAlignment(Pos.CENTER_LEFT);
    titlePane.getChildren().add(pnSelectedFilesPreview);
    HBox.setHgrow(pnSelectedFilesPreview, Priority.ALWAYS);

    btnAddFile = new Button();
    btnAddFile.setMinHeight(24);
    btnAddFile.setMaxHeight(24);
    btnAddFile.setMinWidth(24);
    btnAddFile.setMaxWidth(24);
    btnAddFile.setFont(new Font(9.5));
    btnAddFile.setText("+");
    btnAddFile.setTextFill(Constants.AddEntityButtonTextColor);
    JavaFxLocalization.bindControlToolTip(btnAddFile, "create.file.tool.tip");
    titlePane.getChildren().add(btnAddFile);
    HBox.setMargin(btnAddFile, new Insets(0, 0, 0, 4));

    btnAddFile.setOnAction(event -> handleButtonAddFileAction(event));

    btnShowHideSearchPane = new ToggleButton();
    btnShowHideSearchPane.setId("btnShowHideSearchPane");
    btnShowHideSearchPane.setMinHeight(24);
    btnShowHideSearchPane.setMaxHeight(24);
    btnShowHideSearchPane.setMinWidth(24);
    btnShowHideSearchPane.setMaxWidth(24);
    btnShowHideSearchPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    btnShowHideSearchPane.setGraphic(new ImageView(Constants.SearchIconPath));
    JavaFxLocalization.bindControlToolTip(btnShowHideSearchPane, "search.files.tool.tip");
    titlePane.getChildren().add(btnShowHideSearchPane);
    HBox.setMargin(btnShowHideSearchPane, new Insets(0, 0, 0, 4));

    btnShowHideSearchPane.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == true && isExpanded() == false)
        setExpanded(true);

      double searchPaneHeight = searchAndSelectFilesControl.getHeight() > searchAndSelectFilesControl.getMinHeight() ?
          searchAndSelectFilesControl.getHeight() : searchAndSelectFilesControl.getMinHeight();
      double newHeight = newValue == true ? ContentPaneMinHeight + searchPaneHeight : ContentPaneMinHeight;
      contentPane.setMinHeight(newHeight);
      contentPane.setMaxHeight(newHeight);
    });

    setTitle(titlePane);

    if(editedFiles != null)
      updateFilesSetOnEntityPreview();
  }


  protected void editOrViewSelectedFile(Event event) {
    if(trtblvwFiles.getSelectionModel().getSelectedItem() != null) {
      FileLink selectedFile = trtblvwFiles.getSelectionModel().getSelectedItem().getValue();
      if(selectedFile != null) {
        if (selectedFile.isPersisted() == false) // a Folder sub file, not a file added to Entry
          viewFile(selectedFile);
        else
          showEditFileDialog(selectedFile);
        event.consume();
      }
    }
  }

  protected void viewFile(FileLink file) {
    FileUtils.openFileInOperatingSystemDefaultApplication(file);
  }

  protected void showEditFileDialog(FileLink file) {
    Dialogs.showEditFileDialog(file);
  }

  protected void removeSelectedFiles() {
    for(FileLink selectedFile : getSelectedFiles()) {
      if (editedFiles != null && editedFiles.containsEditedEntity(selectedFile))
        editedFiles.removeEntityFromEntry(selectedFile);
    }
  }

  protected Collection<FileLink> getSelectedFiles() {
    Collection<FileLink> selectedFiles = new ArrayList<>(); // make a copy as when multiple Persons are selected after removing first one SelectionModel gets cleared

    for(TreeItem<FileLink> item : trtblvwFiles.getSelectionModel().getSelectedItems()) {
      selectedFiles.add(item.getValue());
    }

    return selectedFiles;
  }

  protected void updateFilesSetOnEntityPreview() {
    clearSelectedFilesPreview();

    for(FileLink file : new TreeSet<>(editedFiles.getEditedEntities())) {
      pnSelectedFilesPreview.getChildren().add(createFilePreviewLabel(file));
    }
  }

  protected void clearSelectedFilesPreview() {
    FXUtils.cleanUpChildrenAndClearPane(pnSelectedFilesPreview);
  }

  protected void handleButtonAddFileAction(ActionEvent event) {
    final FileLink newFile = new FileLink();

    Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {

      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok) {
          addEntityToEntry(newFile);
//          trvwCategories.getSelectionModel().clearSelection();
//          trvwCategories.getSelectionModel().selectLast();
//          scrollToSelectedItem();
        }
      }
    });
  }


  protected net.dankito.deepthought.controls.file.FileLabel createFilePreviewLabel(FileLink file) {
    net.dankito.deepthought.controls.file.FileLabel label = new net.dankito.deepthought.controls.file.FileLabel(file);
    label.setOnButtonRemoveItemFromCollectionEventHandler((event) -> removeEntityFromEntry(file));
    return label;
  }

  public boolean containsEditedEntity(FileLink file) {
    return editedFiles != null && editedFiles.containsEditedEntity(file);
  }

  public void addEntityToEntry(FileLink file) {
    if(editedFiles != null)
      editedFiles.addEntityToEntry(file);
  }

  public void removeEntityFromEntry(FileLink file) {
    if(editedFiles != null)
      editedFiles.removeEntityFromEntry(file);
  }

  public IEditedEntitiesHolder<FileLink> getEditedFiles() {
    return editedFiles;
  }



  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getFiles()) {
        updateFilesSetOnEntityPreview();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };


  protected SetChangeListener<FileLink> editedFilesChangedListener = new SetChangeListener<FileLink>() {
    @Override
    public void onChanged(Change<? extends FileLink> change) {
      updateFilesSetOnEntityPreview();
    }
  };

}
