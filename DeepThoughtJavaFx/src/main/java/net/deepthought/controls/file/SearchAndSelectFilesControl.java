package net.deepthought.controls.file;

import net.deepthought.Application;
import net.deepthought.controller.Dialogs;
import net.deepthought.controls.ICleanUp;
import net.deepthought.controls.LazyLoadingObservableList;
import net.deepthought.controls.person.PersonListCell;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.specific.FilesSearch;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Notification;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public class SearchAndSelectFilesControl extends VBox implements ICleanUp {

  protected final static Logger log = LoggerFactory.getLogger(SearchAndSelectFilesControl.class);


  protected IEditedEntitiesHolder<FileLink> editedFilesHolder = null;

  protected DeepThought deepThought = null;

  protected LazyLoadingObservableList<Person> listViewPersonsItems;

  protected FilesSearch lastFilesSearch = null;

  protected List<PersonListCell> personListCells = new ArrayList<>();


  @FXML
  protected TreeTableView<FileLink> trtblvwFiles;
  @FXML
  protected TreeTableColumn<FileLink, String> clmFileName;
  @FXML
  protected TreeTableColumn<FileLink, String> clmFileUri;

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


  public SearchAndSelectFilesControl(IEditedEntitiesHolder<FileLink> editedFilesHolder) {
    this.editedFilesHolder = editedFilesHolder;

    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    if(FXUtils.loadControl(this, "SearchAndSelectFilesControl")) {
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    }
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

    for(PersonListCell cell : personListCells)
      cell.cleanUp();
    personListCells.clear();

    ((FileRootTreeItem)trtblvwFiles.getRoot()).cleanUp();

    ((FileRootTreeItem)trtblvwSearchResults.getRoot()).cleanUp();

    editedFilesHolder = null;
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
    clmFileName.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileLink, String> p) ->
        new ReadOnlyStringWrapper(p.getValue().getValue().getName()));
//    clmFileName.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
//      @Override
//      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
//        return new FileTreeTableCell(editedFilesHolder);
//      }
//    });
    clmFileUri.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileLink, String> p) ->
        new ReadOnlyStringWrapper(p.getValue().getValue().getUriString()));

    trtblvwFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    trtblvwFiles.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        deleteSelectedFiles();
        event.consume();
      }
    });

    txtfldSearchForFiles = (CustomTextField) TextFields.createClearableTextField();
    paneSearchBar.getChildren().add(1, txtfldSearchForFiles);
    HBox.setHgrow(txtfldSearchForFiles, Priority.ALWAYS);
    JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchForFiles, "search.files.prompt.text");
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

    trtblvwFiles.setRoot(new FileRootTreeItem(editedFilesHolder));


    clmSearchResultFileName.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileLink, String> p) ->
        new ReadOnlyStringWrapper(p.getValue().getValue().getName()));

    clmSearchResultFileUri.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileLink, String> p) ->
        new ReadOnlyStringWrapper(p.getValue().getValue().getUriString()));

    trtblvwSearchResults.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    trtblvwSearchResults.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        toggleSelectedFilesAffiliation();
        event.consume();
      }
    });

    searchResultsRootTreeItem = new FileSearchResultsRootTreeItem(editedFilesHolder);
    trtblvwSearchResults.setRoot(searchResultsRootTreeItem);

    setSearchPaneVisibility(false);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSearchFiles);

    resetListViewSearchResultsItems();
  }

  public void setSearchPaneVisibility(boolean isVisible) {
    paneSearchFiles.setVisible(isVisible);
  }

  protected void toggleFileAffiliation(FileLink file) {
    if(editedFilesHolder.containsEditedEntity(file))
      editedFilesHolder.removeEntityFromEntry(file);
    else
      editedFilesHolder.addEntityToEntry(file);
  }

  protected void toggleSelectedFilesAffiliation() {
    for(FileLink selectedFile : getSelectedFiles()) {
      toggleFileAffiliation(selectedFile);
    }
  }

  protected void deleteSelectedFiles() {
    for(FileLink selectedFile : getSelectedFiles()) {
      if(Alerts.deleteFileWithUserConfirmationIfIsSetOnEntriesOrReferenceBases(deepThought, selectedFile)) {
        if(editedFilesHolder != null && editedFilesHolder.containsEditedEntity(selectedFile))
          editedFilesHolder.removeEntityFromEntry(selectedFile);
      }
    }
  }

  protected Collection<FileLink> getSelectedFiles() {
    Collection<FileLink> selectedFiles = new ArrayList<>(); // make a copy as when multiple Persons are selected after removing first one SelectionModel gets cleared

    for(TreeItem<FileLink> item : trtblvwFiles.getSelectionModel().getSelectedItems()) {
      selectedFiles.add(item.getValue());
    }

    return selectedFiles;
  }


  protected void searchFiles() {
    if(lastFilesSearch != null && lastFilesSearch.isCompleted() == false)
      lastFilesSearch.interrupt();

    lastFilesSearch = new FilesSearch(txtfldSearchForFiles.getText(), (results) -> {
      searchResultsRootTreeItem.setSearchResults(results);
    });
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

    Dialogs.showEditPersonDialog(newPerson, null);
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


}
