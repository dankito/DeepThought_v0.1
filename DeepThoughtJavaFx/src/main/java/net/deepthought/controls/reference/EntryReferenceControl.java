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
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.search.Search;
import net.deepthought.data.search.specific.ReferenceBaseType;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryReferenceControl extends TitledPane implements ISelectedReferenceHolder {

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
  protected Label lblReference;
  @FXML
  protected Pane paneSelectedReferenceBase;
  @FXML
  protected NewOrEditButton btnNewOrEditReference;

  @FXML
  protected Pane paneReferenceIndicationSettings;
  @FXML
  protected TextField txtfldReferenceIndication;

  protected SearchAndSelectReferenceControl searchAndSelectReferenceControl = null;


  public EntryReferenceControl() {
    this(null);
  }

  public EntryReferenceControl(Entry entry, EventHandler<FieldChangedEvent> fieldChangedEvent) {
    this(entry);
    addFieldChangedEvent(fieldChangedEvent);
  }

  public EntryReferenceControl(Entry entry) {
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        EntryReferenceControl.this.deepThoughtChanged(deepThought);
      }

      @Override
      public void notification(Notification notification) {

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

      setEntry(entry);
    } catch (IOException ex) {
      log.error("Could not load EntryReferenceControl", ex);
    }
  }

  public void setEntry(Entry entry) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = entry;

    if(entry != null) {
      entry.addEntityListener(entryListener);

      txtfldReferenceIndication.setText(entry.getIndication());

      selectedReferenceBase = null;
      if(entry.getReferenceSubDivision() != null)
        selectedReferenceBase = entry.getReferenceSubDivision();
      else if(entry.getReference() != null)
        selectedReferenceBase = entry.getReference();
      else if(entry.getSeries() != null)
        selectedReferenceBase = entry.getSeries();

      selectedReferenceBaseChanged(selectedReferenceBase);
    }
    else {
      selectedReferenceBaseChanged(null);
    }

    setDisable(entry == null);
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    listViewReferenceBasesItems.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
    }
  }

  protected void setupControl() {
    btnNewOrEditReference = new NewOrEditButton();// create btnNewOrEditReference before cmbxSeriesTitleOrReference's value gets set (otherwise cmbxSeriesTitleOrReferenceValueChangedListener causes a NullPointerException)
    btnNewOrEditReference.setOnAction(event -> handleButtonEditOrNewReferenceAction(event));
    btnNewOrEditReference.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewReferenceAction(event));
    paneSeriesTitleOrReference.getChildren().add(2, btnNewOrEditReference);

    btnNewOrEditReference.setMinWidth(100);
    btnNewOrEditReference.setPrefWidth(162);
    btnNewOrEditReference.setMaxHeight(28);
    HBox.setMargin(btnNewOrEditReference, new Insets(0, 0, 0, 6));

    txtfldReferenceIndication.textProperty().addListener((observable, oldValue, newValue) ->
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReferenceIndication, txtfldReferenceIndication.getText()));

    searchAndSelectReferenceControl = new SearchAndSelectReferenceControl(ReferenceBaseType.All, this);
    setContent(searchAndSelectReferenceControl);
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
          selectedReferenceBaseChanged(((EditReferenceDialogController) controller).getEditedReferenceBase());
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  public void selectedReferenceBaseChanged(final ReferenceBase newReferenceBase) {
    if(Platform.isFxApplicationThread())
      selectedReferenceBaseChangedOnUiThread(newReferenceBase);
    else
      Platform.runLater(() -> selectedReferenceBaseChangedOnUiThread(newReferenceBase));
  }

  protected void selectedReferenceBaseChangedOnUiThread(ReferenceBase newReferenceBase) {
    if(currentWidthListener != null) {
      this.widthProperty().removeListener(currentWidthListener);
      paneReferenceIndicationSettings.widthProperty().removeListener(currentWidthListener);
    }
    ReferenceBase previousReferenceBase = this.selectedReferenceBase;
    this.selectedReferenceBase = newReferenceBase;

    if(paneSelectedReferenceBase.getChildren().size() > 0)
      ((EntryReferenceBaseLabel)paneSelectedReferenceBase.getChildren().get(0)).setOnButtonRemoveItemFromCollectionEventHandler(null);
    paneSelectedReferenceBase.getChildren().clear();

    if (selectedReferenceBase != null)
      createEntryReferenceBaseLabel(newReferenceBase);

    if (selectedReferenceBase == null) {
      btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.New);
      btnNewOrEditReference.setShowNewMenuItem(false);
    } else {
      btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
      btnNewOrEditReference.setShowNewMenuItem(true);
    }

    fireFieldChangedEvent(newReferenceBase, previousReferenceBase);
  }

  protected ChangeListener<? super Number> currentWidthListener = null;

  protected void createEntryReferenceBaseLabel(ReferenceBase newReferenceBase) {
    final EntryReferenceBaseLabel label = new EntryReferenceBaseLabel(newReferenceBase, onButtonRemoveItemFromCollectionEventHandler);

    // laborious but works: Before if EntryReferenceBaseLabel was to broad to be fully displayed it broadened the whole EntryReferenceControl and there moved the Splitter of SplitPane
    currentWidthListener = (observable, oldValue, newValue) -> setEntryReferenceBaseLabelMaxWidth(label);
    this.widthProperty().addListener(currentWidthListener);
    paneReferenceIndicationSettings.widthProperty().addListener(currentWidthListener);
    setEntryReferenceBaseLabelMaxWidth(label);

    paneSelectedReferenceBase.getChildren().add(label);
  }

  protected void setEntryReferenceBaseLabelMaxWidth(EntryReferenceBaseLabel label) {
    if(paneReferenceIndicationSettings.getWidth() > 0) // on the first call this.getWidth() == 0 -> maxWidth would be less than zero -> less than zero this means 'MAX_VALUE'
      label.setMaxWidth(this.getWidth() - lblReference.getWidth() - btnNewOrEditReference.getWidth() - paneReferenceIndicationSettings.getWidth() - 60);
    else
      label.setMaxWidth(this.getMinWidth() - 60);
  }

  protected EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler = new EventHandler<CollectionItemLabelEvent>() {
    @Override
    public void handle(CollectionItemLabelEvent event) {
      selectedReferenceBaseChanged(null);
    }
  };


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.EntrySeriesTitleJoinColumnName)) {
        selectedReferenceBaseChanged();
      }
      else if(propertyName.equals(TableConfig.EntryReferenceJoinColumnName)) {
        selectedReferenceBaseChanged();
      }
      else if(propertyName.equals(TableConfig.EntryReferenceSubDivisionJoinColumnName)) {
        selectedReferenceBaseChanged();
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

  protected void selectedReferenceBaseChanged() {
    if(entry.getReferenceSubDivision() != null)
      selectedReferenceBaseChanged(entry.getReferenceSubDivision());
    else if(entry.getReference() != null)
      selectedReferenceBaseChanged(entry.getReference());
    else if(entry.getSeries() != null)
      selectedReferenceBaseChanged(entry.getSeries());
    else
      selectedReferenceBaseChanged(null);
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

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      // TODO: this is not working this way
      if(updatedEntity.equals(entry.getSeries()) || updatedEntity.equals(entry.getReference()) || updatedEntity.equals(entry.getReferenceSubDivision())) {
        if(entry.getReferenceSubDivision() != null)
          selectedReferenceBaseChanged(entry.getReferenceSubDivision());
        else if(entry.getReference() != null)
          selectedReferenceBaseChanged(entry.getReference());
        else if(entry.getSeries() != null)
          selectedReferenceBaseChanged(entry.getSeries());
        else
          selectedReferenceBaseChanged(null);
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };



  protected void fireFieldChangedEvent(ReferenceBase newReferenceBase, ReferenceBase previousReferenceBase) {
    if(fieldChangedEvents == null)
      return;

    if(newReferenceBase instanceof SeriesTitle)
      fireFieldChangedEvent(FieldWithUnsavedChanges.EntrySeriesTitle, newReferenceBase);
    else if(newReferenceBase instanceof Reference)
      fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReference, newReferenceBase);
    else if(newReferenceBase instanceof ReferenceSubDivision)
      fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReferenceSubDivision, newReferenceBase);
    else if(previousReferenceBase instanceof SeriesTitle) // if newReferenceBase == null then get changed field by previousReferenceBase
      fireFieldChangedEvent(FieldWithUnsavedChanges.EntrySeriesTitle, newReferenceBase);
    else if(previousReferenceBase instanceof Reference)
      fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReference, newReferenceBase);
    else if(previousReferenceBase instanceof ReferenceSubDivision)
      fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReferenceSubDivision, newReferenceBase);
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

}
