package net.dankito.deepthought.controls.reference;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controller.ChildWindowsController;
import net.dankito.deepthought.controller.ChildWindowsControllerListener;
import net.dankito.deepthought.controller.EditReferenceDialogController;
import net.dankito.deepthought.controller.enums.DialogResult;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.controls.CollapsiblePane;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.event.FieldChangedEvent;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.search.specific.ReferenceBaseType;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.isbn.IsbnResolvingListener;
import net.dankito.deepthought.util.isbn.ResolveIsbnResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryReferenceControl extends CollapsiblePane implements ISelectedReferenceHolder, ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(EntryReferenceControl.class);


  protected Entry entry = null;

  protected EntryCreationResult creationResult = null;

  protected ReferenceBase selectedReferenceBase = null;

  protected DeepThought deepThought = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();

  protected Collection<EventHandler<FieldChangedEvent>> fieldChangedEvents = new HashSet<>();

  protected net.dankito.deepthought.controls.reference.EntryReferenceBaseLabel currentReferenceLabel = null;


  @FXML
  protected HBox paneSelectedReferenceBase;
  @FXML
  protected net.dankito.deepthought.controls.NewOrEditButton btnNewOrEditReference;

  protected net.dankito.deepthought.controls.connected_devices.ScanIsbnConnectedDevicesPanel connectedDevicesPanel;

  @FXML
  protected TextField txtfldReferenceIndication;

  protected net.dankito.deepthought.controls.reference.SearchAndSelectReferenceControl searchAndSelectReferenceControl = null;


  public EntryReferenceControl() {
    this(null);
  }

  public EntryReferenceControl(Entry entry, EventHandler<FieldChangedEvent> fieldChangedEvent) {
    this(entry);
    addFieldChangedEvent(fieldChangedEvent);
  }

  public EntryReferenceControl(Entry entry) {
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    setupControl();

    if(deepThought != null)
      deepThought.addEntityListener(deepThoughtListener);

    setEntry(entry);
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      EntryReferenceControl.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };

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

  public void setEntryCreationResult(EntryCreationResult creationResult) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.creationResult = creationResult;
    this.entry = null;

    if(creationResult != null) {
      selectedReferenceBase = null;
      if(creationResult.getReferenceSubDivision() != null)
        selectedReferenceBase = creationResult.getReferenceSubDivision();
      else if(creationResult.getReference() != null)
        selectedReferenceBase = creationResult.getReference();
      else if(creationResult.getSeriesTitle() != null)
        selectedReferenceBase = creationResult.getSeriesTitle();

      selectedReferenceBaseChanged(selectedReferenceBase);
    }
    else {
      selectedReferenceBaseChanged(null);
    }

    setDisable(creationResult == null);
  }

  public void cleanUp() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    searchAndSelectReferenceControl.cleanUp();

    fieldChangedEvents.clear();
    clearCurrentReferenceLabel();

    connectedDevicesPanel.cleanUp();

    this.entry = null;
    this.creationResult = null;
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
    setupTitle();

    searchAndSelectReferenceControl = new net.dankito.deepthought.controls.reference.SearchAndSelectReferenceControl(ReferenceBaseType.All, this);
    searchAndSelectReferenceControl.setMaxHeight(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);
    setContent(searchAndSelectReferenceControl);
  }

  protected void setupTitle() {
    HBox titlePane = new HBox();
    titlePane.setAlignment(Pos.CENTER_LEFT);
    titlePane.setMinHeight(32);
    titlePane.setMaxHeight(32);
    titlePane.setMaxWidth(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);

    Label lblReference = new Label();
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindLabeledText(lblReference, "reference");
    lblReference.setPrefWidth(USE_COMPUTED_SIZE);
    lblReference.setMinWidth(USE_PREF_SIZE);
    lblReference.setMaxWidth(USE_PREF_SIZE);
    titlePane.getChildren().add(lblReference);
    HBox.setMargin(lblReference, new Insets(0, 12, 0, 0));

    paneSelectedReferenceBase = new HBox();
    paneSelectedReferenceBase.setAlignment(Pos.CENTER_LEFT);
    paneSelectedReferenceBase.setMinWidth(100);
    paneSelectedReferenceBase.setMaxWidth(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);
    titlePane.getChildren().add(paneSelectedReferenceBase);

    btnNewOrEditReference = new net.dankito.deepthought.controls.NewOrEditButton();
    btnNewOrEditReference.setId("btnNewOrEditReference");
    btnNewOrEditReference.setOnAction(event -> handleButtonEditOrNewReferenceAction(event));
    btnNewOrEditReference.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewReferenceAction(event));
    titlePane.getChildren().add(2, btnNewOrEditReference);

    btnNewOrEditReference.setMinWidth(100);
    btnNewOrEditReference.setPrefWidth(162);
    btnNewOrEditReference.setMaxHeight(28);
    HBox.setMargin(btnNewOrEditReference, new Insets(0, 0, 0, 6));

    MenuItem newReferenceFromIsbnNumberItem = new MenuItem(Localization.getLocalizedString("new.reference.from.isbn.number"));
    newReferenceFromIsbnNumberItem.setOnAction(event -> handleNewReferenceFromIsbnNumberItemClicked(event));
    btnNewOrEditReference.getItems().add(newReferenceFromIsbnNumberItem);

    connectedDevicesPanel = new net.dankito.deepthought.controls.connected_devices.ScanIsbnConnectedDevicesPanel(isbnResolvingListener);
    titlePane.getChildren().add(3, connectedDevicesPanel);
    HBox.setMargin(connectedDevicesPanel, new Insets(0, 0, 0, 6));

    Label lblIndication = new Label();
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindLabeledText(lblIndication, "indication");
    lblIndication.setPrefWidth(USE_COMPUTED_SIZE);
    lblIndication.setMinWidth(USE_PREF_SIZE);
    lblIndication.setMaxWidth(USE_PREF_SIZE);
    titlePane.getChildren().add(lblIndication);
    HBox.setMargin(lblIndication, new Insets(0, 6, 0, 12));

    txtfldReferenceIndication = new TextField();
    txtfldReferenceIndication.setMinWidth(60);
    txtfldReferenceIndication.setPrefWidth(90);
    txtfldReferenceIndication.textProperty().addListener((observable, oldValue, newValue) ->
        fireFieldChangedEvent(FieldWithUnsavedChanges.EntryReferenceIndication, txtfldReferenceIndication.getText()));
    titlePane.getChildren().add(txtfldReferenceIndication);

    setTitle(titlePane);
  }


  public void handleButtonEditOrNewReferenceAction(ActionEvent event) {
    if(btnNewOrEditReference.getButtonFunction() == net.dankito.deepthought.controls.NewOrEditButton.ButtonFunction.New)
      createNewReferenceBase();
    else {
      if(creationResult == null)
        net.dankito.deepthought.controller.Dialogs.showEditReferenceDialog(selectedReferenceBase);
      else
        net.dankito.deepthought.controller.Dialogs.showEditReferenceDialog(creationResult);
    }
  }

  public void handleMenuItemNewReferenceAction(net.dankito.deepthought.controls.event.NewOrEditButtonMenuActionEvent event) {
    createNewReferenceBase();
  }

  protected void createNewReferenceBase() {
    showEditReferenceDialog();
  }

  protected void showEditReferenceDialog() {
    showEditReferenceDialog(null);
  }

  protected void showEditReferenceDialog(ReferenceBase reference) {
    net.dankito.deepthought.controller.Dialogs.showEditReferenceDialog(reference, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok)
          selectedReferenceBaseChanged(((EditReferenceDialogController) controller).getEditedReferenceBase());
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  protected void handleNewReferenceFromIsbnNumberItemClicked(ActionEvent event) {
    new net.dankito.deepthought.dialogs.AddReferenceFromIsbnDialog().showAsync(getScene().getWindow(), new IsbnResolvingListener() {
      @Override
      public void isbnResolvingDone(ResolveIsbnResult result) {
        if(result.isSuccessful()) {
          selectedReferenceBaseChanged(result.getResolvedReference());
        }
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
    ReferenceBase previousReferenceBase = this.selectedReferenceBase;
    this.selectedReferenceBase = newReferenceBase;

    clearCurrentReferenceLabel();

    if (selectedReferenceBase != null)
      createEntryReferenceBaseLabel(newReferenceBase);

    if (selectedReferenceBase == null) {
      btnNewOrEditReference.setButtonFunction(net.dankito.deepthought.controls.NewOrEditButton.ButtonFunction.New);
      btnNewOrEditReference.setShowNewMenuItem(false);
    } else {
      btnNewOrEditReference.setButtonFunction(net.dankito.deepthought.controls.NewOrEditButton.ButtonFunction.Edit);
      btnNewOrEditReference.setShowNewMenuItem(true);
    }

    fireFieldChangedEvent(newReferenceBase, previousReferenceBase);
  }

  protected void createEntryReferenceBaseLabel(ReferenceBase newReferenceBase) {
    clearCurrentReferenceLabel();

    currentReferenceLabel = new net.dankito.deepthought.controls.reference.EntryReferenceBaseLabel(newReferenceBase, creationResult, onButtonRemoveItemFromCollectionEventHandler);

    paneSelectedReferenceBase.getChildren().add(currentReferenceLabel);
  }

  protected void clearCurrentReferenceLabel() {
    if(currentReferenceLabel != null) {
      currentReferenceLabel.cleanUp();
      currentReferenceLabel = null;
    }

    paneSelectedReferenceBase.getChildren().clear();
  }

  protected EventHandler<net.dankito.deepthought.controls.event.CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler = new EventHandler<net.dankito.deepthought.controls.event.CollectionItemLabelEvent>() {
    @Override
    public void handle(net.dankito.deepthought.controls.event.CollectionItemLabelEvent event) {
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
      if(entry != null) {
        // TODO: this is not working this way
        if (updatedEntity.equals(entry.getSeries()) || updatedEntity.equals(entry.getReference()) || updatedEntity.equals(entry.getReferenceSubDivision())) {
          if (entry.getReferenceSubDivision() != null)
            selectedReferenceBaseChanged(entry.getReferenceSubDivision());
          else if (entry.getReference() != null)
            selectedReferenceBaseChanged(entry.getReference());
          else if (entry.getSeries() != null)
            selectedReferenceBaseChanged(entry.getSeries());
          else
            selectedReferenceBaseChanged(null);
        }
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };


  protected IsbnResolvingListener isbnResolvingListener = new IsbnResolvingListener() {
    @Override
    public void isbnResolvingDone(ResolveIsbnResult result) {
      if(result.isSuccessful()) {
        selectedReferenceBaseChanged(result.getResolvedReference());
      }
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
