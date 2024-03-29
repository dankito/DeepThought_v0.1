package net.dankito.deepthought.controls.file;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.specific.FilesSearch;
import net.dankito.deepthought.util.Notification;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Created by ganymed on 01/02/15.
 */
public class SearchAndSelectFilesControl extends VBox implements net.dankito.deepthought.controls.event.IMouseAndKeyEventReceiver, ICleanUp {

  protected final static Logger log = LoggerFactory.getLogger(SearchAndSelectFilesControl.class);


  protected DeepThought deepThought = null;

  protected boolean searchInHtmlEmbeddableFilesOnly = false;

  protected FilesSearch lastFilesSearch = null;

  protected net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<FileLink> editedFilesHolder = null;

  protected EventHandler<net.dankito.deepthought.controls.event.EntitySelectedEvent<FileLink>> entitySelectedEventHandler = null;


  @FXML
  protected VBox paneSearchFiles;
  @FXML
  protected HBox paneSearchBar;

  protected TextField txtfldSearchForFiles;
  @FXML
  protected TreeTableView<FileLink> trtblvwSearchResults;

  protected FileSearchResultsRootTreeItem searchResultsRootTreeItem;
  @FXML
  protected TreeTableColumn<FileLink, String> clmSearchResultFileName;
  @FXML
  protected TreeTableColumn<FileLink, String> clmSearchResultFileUri;


  public SearchAndSelectFilesControl(net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<FileLink> editedFilesHolder) {
    this(editedFilesHolder, false);
  }

  public SearchAndSelectFilesControl(net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<FileLink> editedFilesHolder, boolean searchInHtmlEmbeddableFilesOnly) {
    this.editedFilesHolder = editedFilesHolder;
    this.searchInHtmlEmbeddableFilesOnly = searchInHtmlEmbeddableFilesOnly;

    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    if(FXUtils.loadControl(this, "SearchAndSelectFilesControl")) {
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    }
  }

  public SearchAndSelectFilesControl(boolean searchInHtmlEmbeddableFilesOnly, SelectionMode selectionMode, EventHandler<net.dankito.deepthought.controls.event.EntitySelectedEvent<FileLink>> entitySelectedEventHandler) {
    this(null, searchInHtmlEmbeddableFilesOnly);

    this.entitySelectedEventHandler = entitySelectedEventHandler;
    trtblvwSearchResults.getSelectionModel().setSelectionMode(selectionMode);
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      SearchAndSelectFilesControl.this.deepThoughtChanged(deepThought);
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

    ((FileSearchResultsRootTreeItem)trtblvwSearchResults.getRoot()).cleanUp();

    editedFilesHolder = null;
    entitySelectedEventHandler = null;
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
      resetListViewSearchResultsItems();
    }
  }

  protected void setupControl() {
    txtfldSearchForFiles = (CustomTextField) TextFields.createClearableTextField();
    txtfldSearchForFiles.setId("txtfldSearchForFiles");
    paneSearchBar.getChildren().add(1, txtfldSearchForFiles);
    HBox.setHgrow(txtfldSearchForFiles, Priority.ALWAYS);
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchForFiles, "search.files.prompt.text");
    txtfldSearchForFiles.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        searchFiles();
      }
    });
    txtfldSearchForFiles.setOnAction((event) -> handleTextFieldSearchFilesAction());
    txtfldSearchForFiles.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        txtfldSearchForFiles.clear();
        event.consume();
      }
    });


//    clmSearchResultFileName.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileLink, String> p) ->
//        new ReadOnlyStringWrapper(p.getValue().getValue().getName()));
    clmSearchResultFileName.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
      @Override
      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
        return new net.dankito.deepthought.controls.file.cells.FileSearchResultFileNameTreeTableCell();
      }
    });

//    clmSearchResultFileUri.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileLink, String> p) ->
//        new ReadOnlyStringWrapper(p.getValue().getValue().getUriString()));
    clmSearchResultFileUri.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
      @Override
      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
        return new net.dankito.deepthought.controls.file.cells.FileSearchResultUriTreeTableCell();
      }
    });

    trtblvwSearchResults.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    trtblvwSearchResults.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        enterPressedOrFilesDoubleClicked(event);
      } else if (event.getCode() == KeyCode.DELETE) {
        deleteSelectedFiles();
        event.consume();
      }
    });
    trtblvwSearchResults.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
        enterPressedOrFilesDoubleClicked(event);
      }
    });

    searchResultsRootTreeItem = new FileSearchResultsRootTreeItem(editedFilesHolder);
    trtblvwSearchResults.setRoot(searchResultsRootTreeItem);

    setSearchPaneVisibility(false);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSearchFiles);
  }

  public void setSearchPaneVisibility(boolean isVisible) {
    if(isVisible && lastFilesSearch == null) { // paneSearchFiles gets shown for the first time -> load / search files
      searchFiles();
    }

    paneSearchFiles.setVisible(isVisible);
  }


  protected void enterPressedOrFilesDoubleClicked(Event event) {
    fireEntitySelectedEvent();
    toggleSelectedFilesAffiliation();
    event.consume();
  }

  protected void toggleFileAffiliation(FileLink file) {
    if(editedFilesHolder != null) {
      if (editedFilesHolder.containsEditedEntity(file))
        editedFilesHolder.removeEntityFromEntry(file);
      else
        editedFilesHolder.addEntityToEntry(file);
    }
  }

  protected void toggleSelectedFilesAffiliation() {
    for(FileLink selectedFile : getSelectedFiles()) {
      toggleFileAffiliation(selectedFile);
    }
  }

  protected void deleteSelectedFiles() {
    for(FileLink selectedFile : getSelectedFiles()) {
      deleteFile(selectedFile);
    }
  }

  protected void deleteFile(FileLink selectedFile) {
    if(net.dankito.deepthought.util.Alerts.deleteFileWithUserConfirmationIfIsSetOnEntriesOrReferenceBases(deepThought, selectedFile)) {
      if(editedFilesHolder != null && editedFilesHolder.containsEditedEntity(selectedFile))
        editedFilesHolder.removeEntityFromEntry(selectedFile);
    }
  }

  protected List<FileLink> getSelectedFiles() {
    List<FileLink> selectedFiles = new ArrayList<>(); // make a copy as when multiple Persons are selected after removing first one SelectionModel gets cleared

    for(TreeItem<FileLink> item : trtblvwSearchResults.getSelectionModel().getSelectedItems()) {
      if(item != null) // clearly a bug, how can a 'selected' tree item ever be null?
        selectedFiles.add(item.getValue());
    }

    return selectedFiles;
  }

  protected void fireEntitySelectedEvent() {
    if(entitySelectedEventHandler != null) {
      List<FileLink> selectedFiles = getSelectedFiles();
      if(selectedFiles.size() > 0)
        entitySelectedEventHandler.handle(new net.dankito.deepthought.controls.event.EntitySelectedEvent<FileLink>(selectedFiles.get(0)));
    }
  }


  protected void searchFiles() {
    if(lastFilesSearch != null && lastFilesSearch.isCompleted() == false)
      lastFilesSearch.interrupt();

    lastFilesSearch = new FilesSearch(txtfldSearchForFiles.getText(), (results) -> {
      searchResultsRootTreeItem.setSearchResults(results);
    });
    lastFilesSearch.setInHtmlEmbeddableFilesOnly(searchInHtmlEmbeddableFilesOnly);
    Application.getSearchEngine().searchFiles(lastFilesSearch);
  }


  protected void handleTextFieldSearchFilesAction() {
//    // TODO: check if person of that Name exists and if so don't create a new one but add existing one
//    final Person newPerson = Person.createPersonFromStringRepresentation(txtfldSearchForFiles.getText());
//
//    if(deepThought != null)
//      deepThought.addPerson(newPerson);
//
//    toggleFileAffiliation(newPerson);
  }

  @FXML
  public void handleButtonNewPersonAction(ActionEvent event) {
    final Person newPerson = Person.createPersonFromStringRepresentation(txtfldSearchForFiles.getText());

    net.dankito.deepthought.controller.Dialogs.showEditPersonDialog(newPerson, null);
  }


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getFiles()) {
        resetListViewSearchResultsItems();
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getFiles()) {
        resetListViewSearchResultsItems();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getFiles()) {
        resetListViewSearchResultsItems();
      }
    }
  };

  protected void resetListViewSearchResultsItems() {
    searchFiles();
  }


  @Override
  public void onMouseEvent(MouseEvent event) {

  }

  @Override
  public void onKeyEvent(KeyEvent event) {

  }
}
