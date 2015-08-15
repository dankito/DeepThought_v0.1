package net.deepthought.controls.reference;

import net.deepthought.Application;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.LazyLoadingObservableList;
import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.specific.FilterReferenceBasesSearch;
import net.deepthought.data.search.specific.ReferenceBaseType;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public class SearchAndSelectReferenceControl extends VBox implements ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(SearchAndSelectReferenceControl.class);


  protected ReferenceBaseType type = null;

  protected ISelectedReferenceHolder selectedReferenceHolder = null;

  protected DeepThought deepThought = null;

  protected LazyLoadingObservableList<ReferenceBase> listViewReferenceBasesItems = null;

  protected FilterReferenceBasesSearch filterReferenceBasesSearch = null;

  protected Collection<EventHandler<FieldChangedEvent>> fieldChangedEvents = new HashSet<>();

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

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/SearchAndSelectReferenceControl.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    } catch (IOException ex) {
      log.error("Could not load SearchAndSelectReferenceControl", ex);
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
  public void cleanUpControl() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    for(ReferenceBaseListCell cell : referenceBaseListCells)
      cell.cleanUpControl();
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
    // replace normal TextField txtfldSearchForPerson with a SearchTextField (with a cross to clear selection)
    paneSearchForReference.getChildren().remove(txtfldSearchForReference);
    txtfldSearchForReference = (CustomTextField) TextFields.createClearableTextField();
    txtfldSearchForReference.setId("txtfldSearchForReference");
    paneSearchForReference.getChildren().add(1, txtfldSearchForReference);
    HBox.setHgrow(txtfldSearchForReference, Priority.ALWAYS);
    JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchForReference, "search.for.reference");
    txtfldSearchForReference.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        filterReferenceBases();
      }
    });
    txtfldSearchForReference.setOnAction((event) -> handleTextFieldSearchForReferenceAction());

    // TODO: set cell
    lstvwReferences.setCellFactory((listView) -> {
      ReferenceBaseListCell cell = new ReferenceBaseListCell(selectedReferenceHolder);
      referenceBaseListCells.add(cell);
      return cell;
    });

    listViewReferenceBasesItems = new LazyLoadingObservableList<>();
//    listViewReferenceBasesItems = new CombinedLazyLoadingObservableList<>();
    lstvwReferences.setItems(listViewReferenceBasesItems);

    // TODO: - check if DeepThough != null  - react on changes to SeriesTitles etc. - Make ListView items searchable
    if(Application.getDeepThought() != null)
      resetListViewReferenceBasesItems(Application.getDeepThought());
  }

  protected void filterReferenceBases() {
    filterReferenceBases(txtfldSearchForReference.getText());
  }

  protected void filterReferenceBases(String filter) {
    if(filterReferenceBasesSearch != null)
      filterReferenceBasesSearch.interrupt();

    filterReferenceBasesSearch = new FilterReferenceBasesSearch(filter, type, (results) -> {
      listViewReferenceBasesItems.setUnderlyingCollection(results);
    });
    Application.getSearchEngine().filterReferenceBases(filterReferenceBasesSearch);
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
    filterReferenceBases("");
  }



  protected void fireFieldChangedEvent(ReferenceBase newReferenceBase, ReferenceBase previousReferenceBase) {
    if(fieldChangedEvents == null)
      return;

    if(fieldChangedEvents != null) {
      if(newReferenceBase instanceof SeriesTitle)
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntrySeriesTitle, newReferenceBase);
      else if(newReferenceBase instanceof Reference)
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReference, newReferenceBase);
      else if(newReferenceBase instanceof ReferenceSubDivision)
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReferenceSubDivision, newReferenceBase);
    }
    else {
      if(previousReferenceBase instanceof SeriesTitle)
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntrySeriesTitle, previousReferenceBase);
      else if(previousReferenceBase instanceof Reference)
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReference, previousReferenceBase);
      else if(previousReferenceBase instanceof ReferenceSubDivision)
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReferenceSubDivision, previousReferenceBase);
    }
  }

  protected void fireFieldChangedEvent(FieldWithUnsavedChanges changedField, Object newValue) {
    for(EventHandler<FieldChangedEvent> fieldChangedEvent : fieldChangedEvents)
      fieldChangedEvent.handle(new FieldChangedEvent(this, changedField, newValue));
  }

  public void addFieldChangedEvent(EventHandler<FieldChangedEvent> fieldChangedEvent) {
    this.fieldChangedEvents.add(fieldChangedEvent);
  }

}
