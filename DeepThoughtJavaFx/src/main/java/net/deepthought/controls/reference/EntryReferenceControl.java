package net.deepthought.controls.reference;

import net.deepthought.Application;
import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.EditReferenceDialogController;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.LazyLoadingObservableList;
import net.deepthought.controls.NewOrEditButton;
import net.deepthought.controls.event.CollectionItemLabelEvent;
import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.controls.event.NewOrEditButtonMenuActionEvent;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.CombinedLazyLoadingList;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.search.Search;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Empty;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryReferenceControl extends TitledPane {

  private final static Logger log = LoggerFactory.getLogger(EntryReferenceControl.class);


  protected Entry entry = null;

  protected ReferenceBase selectedReferenceBase = null;

  protected DeepThought deepThought = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();

  protected LazyLoadingObservableList<ReferenceBase> listViewReferenceBasesItems = null;
//  protected CombinedLazyLoadingObservableList<ReferenceBase> listViewReferenceBasesItems = null;
//  protected ObservableList<ReferenceBase> listViewReferenceBasesItems = null;
//  protected FilteredList<ReferenceBase> filteredReferenceBases = null;
//  protected SortedList<ReferenceBase> sortedFilteredReferenceBases = null;

  protected Search<ReferenceBase> filterReferenceBasesSearch = null;

  protected Collection<EventHandler<FieldChangedEvent>> fieldChangedEvents = new HashSet<>();


  @FXML
  protected Pane paneSeriesTitleOrReference;
  @FXML
  protected Pane paneSelectedReferenceBase;
  @FXML
  protected NewOrEditButton btnNewOrEditReference;

  @FXML
  protected TextField txtfldReferenceIndication;

  @FXML
  protected Pane paneSearchForReference;
  @FXML
  protected CustomTextField txtfldSearchForReference;

  @FXML
  protected ListView<ReferenceBase> lstvwReferences;


  public EntryReferenceControl() {
    this(null);
  }

  public EntryReferenceControl(Entry entry, EventHandler<FieldChangedEvent> fieldChangedEvent) {
    this(entry);
    addFieldChangedEvent(fieldChangedEvent);
  }

  public EntryReferenceControl(Entry entry) {
    this.entry = entry;
    if(entry != null) {
      entry.addEntityListener(entryListener);

      if(entry.getReferenceSubDivision() != null)
        selectedReferenceBase = entry.getReferenceSubDivision();
      else if(entry.getReference() != null)
        selectedReferenceBase = entry.getReference();
      else if(entry.getSeries() != null)
        selectedReferenceBase = entry.getSeries();
    }

    setDisable(entry == null);
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        EntryReferenceControl.this.deepThoughtChanged(deepThought);
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {

      }
    });

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/EntryReferenceControl.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);

      if(entry != null) // TODO: was fuer ein Quatsch dies an dieser Stelle zu machen, entbehrt jeder Logik
        txtfldReferenceIndication.setText(entry.getIndication());

      selectedReferenceBaseChanged(selectedReferenceBase);
    } catch (IOException ex) {
      log.error("Could not load EntryReferenceControl", ex);
    }
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
    btnNewOrEditReference = new NewOrEditButton();// create btnNewOrEditReference before cmbxSeriesTitleOrReference's value gets set (otherwise cmbxSeriesTitleOrReferenceValueChangedListener causes a NullPointerException)
    btnNewOrEditReference.setOnAction(event -> handleButtonEditOrNewReferenceAction(event));
    btnNewOrEditReference.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewReferenceAction(event));
    paneSeriesTitleOrReference.getChildren().add(2, btnNewOrEditReference);

    btnNewOrEditReference.setMinWidth(100);
    btnNewOrEditReference.setPrefWidth(162);

    txtfldReferenceIndication.textProperty().addListener((observable, oldValue, newValue) ->
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReferenceIndication, txtfldReferenceIndication.getText()));

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

    lstvwReferences.setCellFactory((listView) -> new ReferenceBaseListCell(this));

//    listViewReferenceBasesItems = FXCollections.observableList(lstvwReferences.getItems());
    listViewReferenceBasesItems = new LazyLoadingObservableList<>();
//    listViewReferenceBasesItems = new CombinedLazyLoadingObservableList<>();
    lstvwReferences.setItems(listViewReferenceBasesItems);

//    filteredReferenceBases = new FilteredList<>(listViewReferenceBasesItems, referenceBase -> true);
//    sortedFilteredReferenceBases = new SortedList<>(filteredReferenceBases, referenceBaseComparator);
//    lstvwReferences.setItems(sortedFilteredReferenceBases);

    // TODO: - check if DeepThough != null  - react on changes to SeriesTitles etc. - Make ListView items searchable
    if(Application.getDeepThought() != null)
      resetListViewReferenceBasesItems(Application.getDeepThought());
  }

  protected void filterReferenceBases() {
    if(filterReferenceBasesSearch != null && filterReferenceBasesSearch.isCompleted() == false)
      filterReferenceBasesSearch.interrupt();

    Date startTime = new Date();
    filterReferenceBasesSearch = new Search<>(txtfldSearchForReference.getText(), (results) -> {
//      filteredReferenceBases.setPredicate((referenceBase) -> results.contains(referenceBase));
      listViewReferenceBasesItems.setUnderlyingCollection(results);
    });
    Application.getSearchEngine().filterReferenceBases(filterReferenceBasesSearch);
  }

  protected void handleTextFieldSearchForReferenceAction() {

  }


  public void handleButtonEditOrNewReferenceAction(ActionEvent event) {
    if(btnNewOrEditReference.getButtonFunction() == NewOrEditButton.ButtonFunction.New)
      createNewReferenceBase();
    else {
      Dialogs.showEditReferenceDialog(selectedReferenceBase);
    }
  }

  public void handleMenuItemNewReferenceAction(NewOrEditButtonMenuActionEvent event) {
    createNewReferenceBase();
  }

  protected void createNewReferenceBase() {
    //      Reference newReference = Reference.createReferenceFromStringRepresentation(cmbxSeriesTitleOrReference.getEditor().getText()); // TODO: use as soon as typing directly in ComboBox is
    // possible again
    Dialogs.showEditReferenceDialog(null, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok)
//          entry.setReference(newReference);
          selectedReferenceBaseChanged(((EditReferenceDialogController)controller).getEditedReferenceBase());
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  protected void selectedReferenceBaseChanged(ReferenceBase newReferenceBase) {
//    if(newReferenceBase == null)
//      cmbxSeriesTitleOrReference.setValue(Empty.Reference);
//    else
//      cmbxSeriesTitleOrReference.setValue(newReferenceBase);

    ReferenceBase previousReferenceBase = this.selectedReferenceBase;
    this.selectedReferenceBase = newReferenceBase;

    paneSelectedReferenceBase.getChildren().clear();

    if (selectedReferenceBase != null)
      paneSelectedReferenceBase.getChildren().add(new EntryReferenceBaseLabel(newReferenceBase, onButtonRemoveItemFromCollectionEventHandler));

    if (selectedReferenceBase == null) {
      btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.New);
      btnNewOrEditReference.setShowNewMenuItem(false);
    } else {
      btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
      btnNewOrEditReference.setShowNewMenuItem(true);
    }

    fireFieldChangedEvent(newReferenceBase, previousReferenceBase);
  }

  protected EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler = new EventHandler<CollectionItemLabelEvent>() {
    @Override
    public void handle(CollectionItemLabelEvent event) {
      selectedReferenceBaseChanged(null);
    }
  };


  protected ChangeListener<ReferenceBase> cmbxSeriesTitleOrReferenceValueChangedListener = new ChangeListener<ReferenceBase>() {
    @Override
    public void changed(ObservableValue<? extends ReferenceBase> observable, ReferenceBase oldValue, ReferenceBase newValue) {
      log.debug("Selected reference changed to {}", newValue);
      if(newValue instanceof Reference) {
        if(newValue != entry.getReference())
//        entry.setReference(newValue);
          fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReference, newValue);
        if(((Reference)newValue).getSeries() != entry.getSeries())
          fireFieldChangedEvent(FieldWithUnsavedChanges.EntrySeriesTitle, newValue);
      }
      else if(newValue instanceof SeriesTitle) {
        if(newValue != entry.getSeries())
          fireFieldChangedEvent(FieldWithUnsavedChanges.EntrySeriesTitle, newValue);
        if(entry.getReference() != null)
          fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReference, newValue);
      }

      if(newValue == null || Empty.Reference.equals(newValue)) {
        btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.New);
        btnNewOrEditReference.setShowNewMenuItem(false);
      }
      else {
        if(newValue instanceof SeriesTitle) {
          btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
          btnNewOrEditReference.setShowNewMenuItem(true);
        }
        else {
          btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
          btnNewOrEditReference.setShowNewMenuItem(true);
        }
      }
    }
  };


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.EntrySeriesTitleJoinColumnName)) {
        updateComboBoxSeriesTitleOrReferenceSelectedItem();
      }
      else if(propertyName.equals(TableConfig.EntryReferenceJoinColumnName)) {
        updateComboBoxSeriesTitleOrReferenceSelectedItem();
      }
      else if(propertyName.equals(TableConfig.EntryReferenceSubDivisionJoinColumnName)) {
        updateComboBoxSeriesTitleOrReferenceSelectedItem();
      }
      else if(propertyName.equals(TableConfig.EntryIndicationColumnName)) {
        referenceIndicationUpdated();
      }
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

  protected void updateComboBoxSeriesTitleOrReferenceSelectedItem() {
//    cmbxSeriesTitleOrReference.valueProperty().removeListener(cmbxSeriesTitleOrReferenceValueChangedListener);

    if(entry.getReference() != null)
      selectedReferenceBaseChanged(entry.getReference());
    else if(entry.getSeries() != null)
      selectedReferenceBaseChanged(entry.getSeries());
    else
      selectedReferenceBaseChanged(null);

//    cmbxSeriesTitleOrReference.valueProperty().addListener(cmbxSeriesTitleOrReferenceValueChangedListener);
  }

  protected void referenceIndicationUpdated() {
    txtfldReferenceIndication.setText(entry.getIndication());
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryReferenceIndication);
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
      // TODO: this is not working this way
      if(updatedEntity.equals(entry.getSeries()) || updatedEntity.equals(entry.getReference())) {
//        cmbxSeriesTitleOrReference.valueProperty().removeListener(cmbxSeriesTitleOrReferenceValueChangedListener);

        if(entry.getReferenceSubDivision() != null)
          selectedReferenceBaseChanged(entry.getReferenceSubDivision());
        else if(entry.getReference() != null)
          selectedReferenceBaseChanged(entry.getReference());
        else if(entry.getSeries() != null)
          selectedReferenceBaseChanged(entry.getSeries());
        else
          selectedReferenceBaseChanged(null);

//        cmbxSeriesTitleOrReference.valueProperty().addListener(cmbxSeriesTitleOrReferenceValueChangedListener);
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfReferencesHaveBeenUpdated(collectionHolder, removedEntity);
    }
  };

  protected void checkIfReferencesHaveBeenUpdated(BaseEntity collectionHolder, BaseEntity entity) {
    if(collectionHolder instanceof DeepThought) {
      DeepThought deepThought = (DeepThought)collectionHolder;
      if(entity instanceof SeriesTitle || entity instanceof Reference) {
//        resetComboBoxSeriesTitleOrReferencesItems(deepThought);
        resetListViewReferenceBasesItems(deepThought); // TODO: actually only SeriesTitles or References have to be updated
      }
    }
    else if(collectionHolder instanceof Reference && entity instanceof ReferenceSubDivision) {
      if(deepThought != null)
        resetListViewReferenceBasesItems(deepThought); // TODO: actually only ReferenceSubDivisions have to be updated
    }
  }

  protected void resetListViewReferenceBasesItems(DeepThought deepThought) {
//    listViewReferenceBasesItems.clear();
//
//    listViewReferenceBasesItems.addAll(deepThought.getSeriesTitles());
//    listViewReferenceBasesItems.addAll(deepThought.getReferences());
////    for(Reference reference : deepThought.getReferences()) {
////      listViewReferenceBasesItems.add(reference);
////      listViewReferenceBasesItems.addAll(reference.getSubDivisions());
////    }
//    listViewReferenceBasesItems.addAll(deepThought.getReferenceSubDivisions());

    listViewReferenceBasesItems.setUnderlyingCollection(new CombinedLazyLoadingList(Application.getDeepThought().getSeriesTitles(), Application.getDeepThought().getReferences(),
        Application.getDeepThought().getReferenceSubDivisions()));

    filterReferenceBases();
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


  public ReferenceBase getSelectedReferenceBase() {
    return selectedReferenceBase;
  }

  public String getReferenceIndication() {
    return txtfldReferenceIndication.getText();
  }


  protected Comparator<ReferenceBase> referenceBaseComparator = new Comparator<ReferenceBase>() {
    @Override
    public int compare(ReferenceBase o1, ReferenceBase o2) {
      if(o1 == null && o2 == null)
        return 0;
      else if(o1 != null && o2 == null)
        return -1;
      if(o1 == null && o2 != null)
        return 1;

      String preview1 = o1.getTextRepresentation();
      String preview2 = o2.getTextRepresentation();

      return preview1.compareToIgnoreCase(preview2);
    }
  };

}
