package net.deepthought.controls.reference;

import net.deepthought.Application;
import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.EditReferenceDialogController;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.ICleanUp;
import net.deepthought.controls.NewOrEditButton;
import net.deepthought.controls.event.CollectionItemLabelEvent;
import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.controls.event.NewOrEditButtonMenuActionEvent;
import net.deepthought.data.contentextractor.EntryCreationResult;
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
import net.deepthought.data.search.specific.ReferenceBaseType;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;
import net.deepthought.util.isbn.IsbnResolvingListener;
import net.deepthought.util.isbn.ResolveIsbnResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

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
import javafx.scene.control.TextInputDialog;
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

  protected EntryReferenceBaseLabel currentReferenceLabel = null;


  @FXML
  protected HBox paneSelectedReferenceBase;
  @FXML
  protected NewOrEditButton btnNewOrEditReference;

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

    searchAndSelectReferenceControl = new SearchAndSelectReferenceControl(ReferenceBaseType.All, this);
    searchAndSelectReferenceControl.setMaxHeight(Double.MAX_VALUE);
    setContent(searchAndSelectReferenceControl);
  }

  protected void setupTitle() {
    HBox titlePane = new HBox();
    titlePane.setAlignment(Pos.CENTER_LEFT);
    titlePane.setMinHeight(32);
    titlePane.setMaxHeight(32);
    titlePane.setMaxWidth(Double.MAX_VALUE);

    Label lblReference = new Label();
    JavaFxLocalization.bindLabeledText(lblReference, "reference");
    lblReference.setPrefWidth(USE_COMPUTED_SIZE);
    lblReference.setMinWidth(USE_PREF_SIZE);
    lblReference.setMaxWidth(USE_PREF_SIZE);
    titlePane.getChildren().add(lblReference);
    HBox.setMargin(lblReference, new Insets(0, 12, 0, 0));

    paneSelectedReferenceBase = new HBox();
    paneSelectedReferenceBase.setAlignment(Pos.CENTER_LEFT);
    paneSelectedReferenceBase.setMinWidth(100);
    paneSelectedReferenceBase.setMaxWidth(Double.MAX_VALUE);
    titlePane.getChildren().add(paneSelectedReferenceBase);

    btnNewOrEditReference = new NewOrEditButton();
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

    Label lblIndication = new Label();
    JavaFxLocalization.bindLabeledText(lblIndication, "indication");
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
    if(btnNewOrEditReference.getButtonFunction() == NewOrEditButton.ButtonFunction.New)
      createNewReferenceBase();
    else {
      if(creationResult == null)
        Dialogs.showEditReferenceDialog(selectedReferenceBase);
      else
        Dialogs.showEditReferenceDialog(creationResult);
    }
  }

  public void handleMenuItemNewReferenceAction(NewOrEditButtonMenuActionEvent event) {
    createNewReferenceBase();
  }

  protected void createNewReferenceBase() {
    showEditReferenceDialog();
  }

  protected void showEditReferenceDialog() {
    showEditReferenceDialog(null);
  }

  protected void showEditReferenceDialog(ReferenceBase reference) {
    Dialogs.showEditReferenceDialog(reference, new ChildWindowsControllerListener() {
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
    showEnterIsbnDialog(null, null);
  }

  protected void showEnterIsbnDialog(final String lastEnteredIsbn, final String lastEnteredIsbnErrorText) {
    if(Platform.isFxApplicationThread())
      showEnterIsbnDialogOnUiThread(lastEnteredIsbn, lastEnteredIsbnErrorText);
    else
      Platform.runLater(() -> showEnterIsbnDialogOnUiThread(lastEnteredIsbn, lastEnteredIsbnErrorText));
  }

  protected void showEnterIsbnDialogOnUiThread(String lastEnteredIsbn, String lastEnteredIsbnErrorText) {
    TextInputDialog dialog = new TextInputDialog(lastEnteredIsbn);
    dialog.initOwner(getScene().getWindow());
    dialog.setHeaderText(lastEnteredIsbnErrorText);
    dialog.setTitle(Localization.getLocalizedString("enter.isbn.dialog.title"));
    dialog.setContentText(Localization.getLocalizedString("enter.isbn"));

    waitForAndHandleUserIsbnInput(dialog);
  }

  protected void waitForAndHandleUserIsbnInput(TextInputDialog dialog) {
    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()){
      getReferenceForIsbn(result.get(), dialog);
    }
  }

  protected void getReferenceForIsbn(final String enteredIsbn, final TextInputDialog askForIsbnDialog) {
    Application.getIsbnResolver().resolveIsbnAsync(enteredIsbn, new IsbnResolvingListener() {
      @Override
      public void isbnResolvingDone(ResolveIsbnResult result) {
        if(result.isSuccessful()) {
          showEditReferenceDialog(result.getResolvedReference());
        }
        else {
          showEnterIsbnDialog(enteredIsbn, Localization.getLocalizedString("could.not.resolve.isbn", enteredIsbn));
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
      btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.New);
      btnNewOrEditReference.setShowNewMenuItem(false);
    } else {
      btnNewOrEditReference.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
      btnNewOrEditReference.setShowNewMenuItem(true);
    }

    fireFieldChangedEvent(newReferenceBase, previousReferenceBase);
  }

  protected void createEntryReferenceBaseLabel(ReferenceBase newReferenceBase) {
    clearCurrentReferenceLabel();

    currentReferenceLabel = new EntryReferenceBaseLabel(newReferenceBase, creationResult, onButtonRemoveItemFromCollectionEventHandler);

    paneSelectedReferenceBase.getChildren().add(currentReferenceLabel);
  }

  protected void clearCurrentReferenceLabel() {
    if(currentReferenceLabel != null) {
      currentReferenceLabel.cleanUp();
      currentReferenceLabel = null;
    }

    paneSelectedReferenceBase.getChildren().clear();
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
