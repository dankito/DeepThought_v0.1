package net.deepthought.controls.reference;

import net.deepthought.Application;
import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.NewOrEditButton;
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
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Empty;
import net.deepthought.util.Localization;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryReferenceControl extends VBox {

  private final static Logger log = LoggerFactory.getLogger(EntryReferenceControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();

  protected ObservableList<ReferenceBase> comboBoxSeriesTitleOrReferenceItems;

  protected EventHandler<FieldChangedEvent> fieldChangedEvent;


  @FXML
  protected Pane paneSeriesTitleOrReference;
  @FXML
  protected ComboBox<ReferenceBase> cmbxSeriesTitleOrReference;
  @FXML
  protected NewOrEditButton btnNewOrEditReference;

  @FXML
  protected TextField txtfldReferenceIndication;


  public EntryReferenceControl() {
    this(null);
  }

  public EntryReferenceControl(Entry entry, EventHandler<FieldChangedEvent> fieldChangedEvent) {
    this(entry);
    this.fieldChangedEvent = fieldChangedEvent;
  }

  public EntryReferenceControl(Entry entry) {
    this.entry = entry;
    if(entry != null)
      entry.addEntityListener(entryListener);

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
    } catch (IOException ex) {
      log.error("Could not load EntryReferenceControl", ex);
    }
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    comboBoxSeriesTitleOrReferenceItems.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
      resetComboBoxSeriesTitleOrReferencesItems(deepThought);
    }
  }

  protected void setupControl() {
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this);

    setupPaneReference();


//    final TextField txtfldReference = new TextField();
//    TextFields.bindAutoCompletion(txtfldReference, suggestionProvider -> {
//      return findReferencesToSuggestion(suggestionProvider);
//    }, new StringConverter<Reference>() {
//      @Override
//      public String toString(Reference reference) {
//        if(reference != null)
//          return reference.getStringRepresentation();
//        return txtfldReference.getText();
//      }
//
//      @Override
//      public Reference fromString(String string) {
//        Reference reference = Reference.findReferenceFromStringRepresentation(string);
//        if (reference != null)
//          return reference;
//
//        // TODO: maybe it helps returning a dummy object
////        return Reference.createReferenceFromStringRepresentation(string);
//        return null;
//      }
//    });
//
//    paneSeriesTitleReferenceAndSubDivisionSettings.getChildren().add(txtfldReference);

//    setFieldsVisibility(entry);
  }

  protected void setupPaneReference() {
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleOrReference);

    btnNewOrEditReference = new NewOrEditButton();// create btnNewOrEditReference before cmbxSeriesTitleOrReference's value gets set (otherwise cmbxSeriesTitleOrReferenceValueChangedListener causes a NullPointerException)
    btnNewOrEditReference.setOnAction(event -> handleButtonEditOrNewReferenceAction(event));
    btnNewOrEditReference.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewReferenceAction(event));
    paneSeriesTitleOrReference.getChildren().add(2, btnNewOrEditReference);

    btnNewOrEditReference.setMinWidth(100);
    btnNewOrEditReference.setPrefWidth(162);
//    btnNewOrEditReference.setNewText("reference.new");
//    btnNewOrEditReference.setEditText("reference.edit");

    setupComboBoxSeriesTitleOrReference();
  }

  protected void setupComboBoxSeriesTitleOrReference() {
    cmbxSeriesTitleOrReference.setEditable(false); // TODO: undo as soon searching / creating directly in ComboBox is possible again
    comboBoxSeriesTitleOrReferenceItems = cmbxSeriesTitleOrReference.getItems(); // make a copy as in autoCompleteComboBox a FilteredList gets layed over ComboBox's items and only operate on that list (as operating on
    // cmbxSeriesTitleOrReference.getItems() would then operate on FilteredList and that is prohibited)
    cmbxSeriesTitleOrReference.setItems(comboBoxSeriesTitleOrReferenceItems);
    resetComboBoxSeriesTitleOrReferencesItems(Application.getDeepThought());

    cmbxSeriesTitleOrReference.setCellFactory(new Callback<ListView<ReferenceBase>, ListCell<ReferenceBase>>() {
      @Override
      public ListCell<ReferenceBase> call(ListView<ReferenceBase> param) {
        return new ReferenceListCell();
      }
    });


    cmbxSeriesTitleOrReference.setConverter(new StringConverter<ReferenceBase>() {
      @Override
      public String toString(ReferenceBase reference) {
        log.debug("toString called for {}", reference);
        if (reference != null) {
          if(reference instanceof Reference)
            return ((Reference)reference).getPreview(); // Reference
          return reference.getTextRepresentation(); // SeriesTitle
        }
        return cmbxSeriesTitleOrReference.getEditor().getText();
      }

      @Override
      public ReferenceBase fromString(String string) {
        log.debug("fromString called for {}", string);
        // TODO: ComboBox contains SeriesTitles as well!
        Reference reference = Reference.findReferenceFromStringRepresentation(string);
        if (reference != null)
          return reference;

//        // TODO: maybe it helps returning a dummy object
//        return Reference.createReferenceFromStringRepresentation(string);
        return null;
      }
    });
//
//    cmbxSeriesTitleOrReference.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
//      if(event.getCode().equals(KeyCode.ENTER)) {
//        log.debug("Enter has been pressed, selected item is {}", cmbxSeriesTitleOrReference.getSelectionModel().getSelectedItem());
//        handleComboBoxReferenceEnterHasBeenPressed();
//      }
//
//      setButtonEditOrNewReferenceText();
//    });
//
    cmbxSeriesTitleOrReference.valueProperty().addListener(cmbxSeriesTitleOrReferenceValueChangedListener);

//    cmbxSeriesTitleOrReference.valueProperty().addListener(new ChangeListener<Reference>() {
//      @Override
//      public void changed(ObservableValue<? extends Reference> observable, Reference oldValue, Reference newValue) {
//        log.debug("cmbxSeriesTitleOrReference value changed to {}", newValue);
//        entry.setReference(newValue);
//
//        if(oldValue == null && newValue != null)
//          setFieldsVisibility(entry);
//      }
//    });

//    FXUtils.<Reference>autoCompleteComboBox(cmbxSeriesTitleOrReference, new Callback<FXUtils.DoesItemMatchSearchTermParam<Reference>, Boolean>() {
//      @Override
//      public Boolean call(FXUtils.DoesItemMatchSearchTermParam<Reference> param) {
//        return doesSearchTermMatchReference(param);
//      }
//    });

    if(entry.getReference() != null)
      cmbxSeriesTitleOrReference.setValue(entry.getReference());
    else if(entry.getSeries() != null)
      cmbxSeriesTitleOrReference.setValue(entry.getSeries());
    else
      cmbxSeriesTitleOrReference.setValue(Empty.Reference);
  }

//  protected void handleComboBoxReferenceEnterHasBeenPressed() {
//    Reference selectedReference = cmbxSeriesTitleOrReference.getSelectionModel().getSelectedItem();
//    if(selectedReference != null) {
//      if(selectedReference.getId() == null) {
//        Application.getDeepThought().addReference(selectedReference);
//        resetComboBoxSeriesTitleOrReferencesItems(Application.getDeepThought());
//      }
//      entry.setReference(cmbxSeriesTitleOrReference.getSelectionModel().getSelectedItem());
//    }
//    else {
//      String enteredText = cmbxSeriesTitleOrReference.getEditor().getText();
//      Reference foundReference = Reference.findReferenceFromStringRepresentation(enteredText);
//      log.debug("Enter has been pressed in ComboBox Reference, foundReference is {}", foundReference);
//      if (foundReference != null) {
//        entry.setReference(foundReference);
//      } else {
//        createNewReference();
//      }
//    }
//
//    cmbxSeriesTitleOrReference.getEditor().positionCaret(cmbxSeriesTitleOrReference.getEditor().getText().length());
//  }

//  protected void createNewReference() {
//    log.debug("Creating a new Reference from string {}", cmbxSeriesTitleOrReference.getEditor().getText());
//    Reference newReference = Reference.createReferenceFromStringRepresentation(cmbxSeriesTitleOrReference.getEditor().getText());
//    Application.getDeepThought().addReference(newReference);
//    resetComboBoxSeriesTitleOrReferencesItems(Application.getDeepThought());
//    entry.setReference(newReference);
////    updateComboBoxSeriesTitleOrReferenceSelectedItem(newReference);
//  }

  protected Collection<Reference> findReferencesToSuggestion(AutoCompletionBinding.ISuggestionRequest suggestionProvider) {
    List<Reference> suggestions = new ArrayList<>();

    for(Reference reference : Application.getDeepThought().getReferencesSorted()) {
      if(suggestionProvider.isCancelled())
        break;

      if(reference.getTitle().toLowerCase().contains(suggestionProvider.getUserText().toLowerCase()))
        suggestions.add(reference);
    }

    return suggestions;
  }

//  protected void setFieldsVisibility(Entry entry) {
//    EntryTemplate template = entry.getTemplate();
//
//    paneSeriesTitle.setVisible(template.showSeriesTitle() || entry.getReferenceBase() != null || entry.getSeries() != null);
//    paneSeriesTitleOrReference.setVisible(template.showReference() || entry.getReferenceBase() != null);
//    paneReferenceSubDivision.setVisible(template.showReferenceSubDivision() || entry.getReferenceBase() != null || entry.getReferenceSubDivision() != null);
//
//    paneReferenceIndicationSettings.setVisible(paneSeriesTitleOrReference.isVisible());
//
//    paneReferenceIndicationStartSettings.setVisible(template.showReferenceStart() || entry.getReferenceBase() != null);
//    paneReferenceIndicationEndSettings.setVisible(template.showReferenceStart() || StringUtils.isNotNullOrEmpty(entry.getReferenceIndication()));
//
//    this.setVisible(paneSeriesTitleOrReference.isVisible());
//  }

  // newer version

//  protected void setFieldsVisibility(Entry entry) {
//    SeriesTitle seriesTitle = cmbxSeriesTitle.getValue();
//    Reference reference = cmbxSeriesTitleOrReference.getValue();
//    ReferenceSubDivision subDivision = null; // TODO
//
////    paneSeriesTitleOrReference.setVisible(entry.getReferenceBase() != null);
//
//    paneReferenceSubDivision.setVisible(reference != Empty.Reference || subDivision != null);
//
//    paneReferenceIndicationSettings.setVisible(paneSeriesTitleOrReference.isVisible());
//
//    paneReferenceIndicationStartSettings.setVisible(reference != Empty.Reference);
//    paneReferenceIndicationEndSettings.setVisible(StringUtils.isNotNullOrEmpty(txtfldReferenceIndication.getText()));
//
//    this.setVisible(paneSeriesTitleOrReference.isVisible());
//  }

  protected Boolean doesSearchTermMatchReference(FXUtils.DoesItemMatchSearchTermParam<Reference> param) {
    return param.getItem().getTitle().toLowerCase().contains(param.getSearchTerm().toLowerCase());
  }


  public void handleButtonEditOrNewSeriesTitleAction(ActionEvent event) {

  }

  public void handleMenuItemNewSeriesTitleAction(NewOrEditButtonMenuActionEvent event) {

  }


  public void handleButtonEditOrNewReferenceAction(ActionEvent event) {
    if(btnNewOrEditReference.getButtonFunction() == NewOrEditButton.ButtonFunction.New)
      createNewReference();
    else {
      Dialogs.showEditReferenceDialog((Reference)cmbxSeriesTitleOrReference.getValue());
    }
  }

  public void handleMenuItemNewReferenceAction(NewOrEditButtonMenuActionEvent event) {
    createNewReference();
  }

  protected void createNewReference() {
    //      Reference newReference = Reference.createReferenceFromStringRepresentation(cmbxSeriesTitleOrReference.getEditor().getText()); // TODO: use as soon as typing directly in ComboBox is
    // possible again
    final Reference newReference = new Reference();
    Dialogs.showEditReferenceDialog(newReference, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok)
//          entry.setReference(newReference);
        cmbxSeriesTitleOrReference.setValue(newReference);
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }


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
      cmbxSeriesTitleOrReference.setValue(entry.getReference());
    else if(entry.getSeries() != null)
      cmbxSeriesTitleOrReference.setValue(entry.getSeries());
    else
      cmbxSeriesTitleOrReference.setValue(Empty.Reference);

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
      if(updatedEntity.equals(entry.getSeries()) || updatedEntity.equals(entry.getReference())) {
        cmbxSeriesTitleOrReference.valueProperty().removeListener(cmbxSeriesTitleOrReferenceValueChangedListener);
        cmbxSeriesTitleOrReference.setValue(Empty.Reference);
        if(entry.getReference() != null)
          cmbxSeriesTitleOrReference.setValue(entry.getReference());
        else
          cmbxSeriesTitleOrReference.setValue(entry.getSeries());
        cmbxSeriesTitleOrReference.valueProperty().addListener(cmbxSeriesTitleOrReferenceValueChangedListener);
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfReferencesHaveBeenUpdated(collectionHolder, removedEntity);
    }
  };

  protected void checkIfReferencesHaveBeenUpdated(BaseEntity collectionHolder, BaseEntity entity) {
    if(collectionHolder instanceof DeepThought) {
      if(entity instanceof SeriesTitle || entity instanceof Reference) {
        DeepThought deepThought = (DeepThought)collectionHolder;
        resetComboBoxSeriesTitleOrReferencesItems(deepThought);
      }
    };
  }

  protected void resetComboBoxSeriesTitleOrReferencesItems(DeepThought deepThought) {
    log.debug("Selected Reference before {}", cmbxSeriesTitleOrReference.getValue());

    comboBoxSeriesTitleOrReferenceItems.clear();

    comboBoxSeriesTitleOrReferenceItems.add(Empty.Reference);
    comboBoxSeriesTitleOrReferenceItems.addAll(deepThought.getReferencesSorted());
    comboBoxSeriesTitleOrReferenceItems.addAll(deepThought.getSeriesTitlesSorted());

    cmbxSeriesTitleOrReference.setVisibleRowCount(10);

    log.debug("Selected Reference after {}", cmbxSeriesTitleOrReference.getValue());
  }



  protected void fireFieldChangedEvent(FieldWithUnsavedChanges changedField, Object newValue) {
    if(fieldChangedEvent != null)
      fieldChangedEvent.handle(new FieldChangedEvent(this, changedField, newValue));
  }

  public EventHandler<FieldChangedEvent> getFieldChangedEvent() {
    return fieldChangedEvent;
  }

  public void setFieldChangedEvent(EventHandler<FieldChangedEvent> fieldChangedEvent) {
    this.fieldChangedEvent = fieldChangedEvent;
  }


  public ReferenceBase getReferenceBase() {
    if(cmbxSeriesTitleOrReference.getValue() == Empty.Reference)
      return null;
    return cmbxSeriesTitleOrReference.getValue();
  }

  public ReferenceSubDivision getReferenceSubDivision() {
    if(cmbxSeriesTitleOrReference.getValue() instanceof ReferenceSubDivision)
      return (ReferenceSubDivision)cmbxSeriesTitleOrReference.getValue();
    return null;
  }

  public String getReferenceIndication() {
    return txtfldReferenceIndication.getText();
  }

}
