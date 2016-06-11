package net.dankito.deepthought.controls.reference;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.specific.ReferenceBasesSearch;
import net.dankito.deepthought.data.search.specific.ReferenceBaseType;
import net.dankito.deepthought.util.Notification;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public class SearchAndSelectReferenceControl extends VBox implements ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(SearchAndSelectReferenceControl.class);


  protected ReferenceBaseType type = null;

  protected ISelectedReferenceHolder selectedReferenceHolder = null;

  protected DeepThought deepThought = null;

  protected net.dankito.deepthought.controls.LazyLoadingObservableList<ReferenceBase> listViewReferenceBasesItems = null;

  protected ReferenceBasesSearch lastReferenceBasesSearch = null;

  protected Collection<EventHandler<net.dankito.deepthought.controls.event.FieldChangedEvent>> fieldChangedEvents = new HashSet<>();

  protected List<ReferenceBaseListCell> referenceBaseListCells = new ArrayList<>();


  @FXML
  protected Pane paneSearchForReference;
  @FXML
  protected CustomTextField txtfldSearchForReference;

  @FXML
  protected ListView<ReferenceBase> lstvwReferences;

  public SearchAndSelectReferenceControl(ReferenceBaseType type, ISelectedReferenceHolder selectedReferenceHolder) {
    this.type = type;
    this.selectedReferenceHolder = selectedReferenceHolder;
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    if(net.dankito.deepthought.controls.utils.FXUtils.loadControl(this, "SearchAndSelectReferenceControl")) {
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    }
  }


  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      SearchAndSelectReferenceControl.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };


  @Override
  public void cleanUp() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    selectedReferenceHolder = null;

    lastReferenceBasesSearch = null;

    for(ReferenceBaseListCell cell : referenceBaseListCells)
      cell.cleanUp();
    referenceBaseListCells.clear();
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    listViewReferenceBasesItems.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
      resetListViewReferenceBasesItems(deepThought);
    }
  }

  protected void setupControl() {
    // replace normal TextField txtfldSearchForFiles with a SearchTextField (with a cross to clear selection)
    paneSearchForReference.getChildren().remove(txtfldSearchForReference);
    txtfldSearchForReference = (CustomTextField) TextFields.createClearableTextField();
    txtfldSearchForReference.setId("txtfldSearchForReference");
    paneSearchForReference.getChildren().add(1, txtfldSearchForReference);
    HBox.setHgrow(txtfldSearchForReference, Priority.ALWAYS);
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchForReference, "search.reference.prompt.text");
    txtfldSearchForReference.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        searchReferenceBases();
      }
    });
    txtfldSearchForReference.setOnAction((event) -> handleTextFieldSearchForReferenceAction());
    txtfldSearchForReference.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        txtfldSearchForReference.clear();
        event.consume();
      }
    });

    lstvwReferences.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        selectedReferenceHolder.selectedReferenceBaseChanged(lstvwReferences.getSelectionModel().getSelectedItem());
        event.consume();
      } else if (event.getCode() == KeyCode.DELETE) {
        deleteSelectedReferenceBases();
        event.consume();
      }
    });

    lstvwReferences.setCellFactory((listView) -> {
      ReferenceBaseListCell cell = new ReferenceBaseListCell(selectedReferenceHolder);
      referenceBaseListCells.add(cell);
      return cell;
    });

    listViewReferenceBasesItems = new net.dankito.deepthought.controls.LazyLoadingObservableList<>();
//    listViewReferenceBasesItems = new CombinedLazyLoadingObservableList<>();
    lstvwReferences.setItems(listViewReferenceBasesItems);

    // TODO: - check if DeepThough != null  - react on changes to SeriesTitles etc. - Make ListView items searchable
    if(Application.getDeepThought() != null)
      resetListViewReferenceBasesItems(Application.getDeepThought());
  }

  protected void deleteSelectedReferenceBases() {
    for(ReferenceBase selectedReferenceBase : getSelectedReferenceBases()) {
      if(net.dankito.deepthought.util.Alerts.deleteReferenceBaseWithUserConfirmationIfIsSetOnEntries(deepThought, selectedReferenceBase)) {
        if(selectedReferenceHolder != null && selectedReferenceBase.equals(selectedReferenceHolder.getSelectedReferenceBase()))
          selectedReferenceHolder.selectedReferenceBaseChanged(null);
      }
    }
  }

  protected Collection<ReferenceBase> getSelectedReferenceBases() {
    return new ArrayList<>(lstvwReferences.getSelectionModel().getSelectedItems()); // make a copy as when multiple ReferenceBases are selected after removing first one SelectionModel gets cleared
  }


  protected void searchReferenceBases() {
    searchReferenceBases(txtfldSearchForReference.getText());
  }

  protected void searchReferenceBases(String searchTerm) {
    if(lastReferenceBasesSearch != null)
      lastReferenceBasesSearch.interrupt();

    lastReferenceBasesSearch = new ReferenceBasesSearch(searchTerm, type, (results) -> {
      listViewReferenceBasesItems.setUnderlyingCollection(results);
    });
    Application.getSearchEngine().searchReferenceBases(lastReferenceBasesSearch);
  }

  protected void handleTextFieldSearchForReferenceAction() {

  }


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      checkIfReferencesHaveBeenUpdated(collectionHolder, addedEntity);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfReferencesHaveBeenUpdated(collectionHolder, removedEntity);
    }
  };

  protected void checkIfReferencesHaveBeenUpdated(BaseEntity collectionHolder, BaseEntity entity) {
    if(collectionHolder instanceof DeepThought) {
      DeepThought deepThought = (DeepThought)collectionHolder;
      if(entity instanceof SeriesTitle || entity instanceof Reference || entity instanceof ReferenceSubDivision) {
        resetListViewReferenceBasesItems(deepThought); // TODO: actually only SeriesTitles or References have to be updated
      }
    }
  }

  protected void resetListViewReferenceBasesItems(DeepThought deepThought) {
    searchReferenceBases("");
  }



  protected void fireFieldChangedEvent(ReferenceBase newReferenceBase, ReferenceBase previousReferenceBase) {
    if(fieldChangedEvents == null)
      return;

    if(fieldChangedEvents != null) {
      if(newReferenceBase instanceof SeriesTitle)
        fireFieldChangedEvent(net.dankito.deepthought.controller.enums.FieldWithUnsavedChanges.EntrySeriesTitle, newReferenceBase);
      else if(newReferenceBase instanceof Reference)
        fireFieldChangedEvent(net.dankito.deepthought.controller.enums.FieldWithUnsavedChanges.EntryReference, newReferenceBase);
      else if(newReferenceBase instanceof ReferenceSubDivision)
        fireFieldChangedEvent(net.dankito.deepthought.controller.enums.FieldWithUnsavedChanges.EntryReferenceSubDivision, newReferenceBase);
    }
    else {
      if(previousReferenceBase instanceof SeriesTitle)
        fireFieldChangedEvent(net.dankito.deepthought.controller.enums.FieldWithUnsavedChanges.EntrySeriesTitle, previousReferenceBase);
      else if(previousReferenceBase instanceof Reference)
        fireFieldChangedEvent(net.dankito.deepthought.controller.enums.FieldWithUnsavedChanges.EntryReference, previousReferenceBase);
      else if(previousReferenceBase instanceof ReferenceSubDivision)
        fireFieldChangedEvent(net.dankito.deepthought.controller.enums.FieldWithUnsavedChanges.EntryReferenceSubDivision, previousReferenceBase);
    }
  }

  protected void fireFieldChangedEvent(net.dankito.deepthought.controller.enums.FieldWithUnsavedChanges changedField, Object newValue) {
    for(EventHandler<net.dankito.deepthought.controls.event.FieldChangedEvent> fieldChangedEvent : fieldChangedEvents)
      fieldChangedEvent.handle(new net.dankito.deepthought.controls.event.FieldChangedEvent(this, changedField, newValue));
  }

  public void addFieldChangedEvent(EventHandler<net.dankito.deepthought.controls.event.FieldChangedEvent> fieldChangedEvent) {
    this.fieldChangedEvents.add(fieldChangedEvent);
  }

}
