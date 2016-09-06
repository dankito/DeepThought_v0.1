package net.dankito.deepthought.controller;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.controls.html.DeepThoughtFxHtmlEditorListener;
import net.dankito.deepthought.controls.html.IHtmlEditorListener;
import net.dankito.deepthought.controls.person.SeriesTitlePersonsControl;
import net.dankito.deepthought.controls.reference.SearchAndSelectReferenceControl;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.model.listener.SettingsChangedListener;
import net.dankito.deepthought.data.model.settings.UserDeviceSettings;
import net.dankito.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.dankito.deepthought.data.model.settings.enums.ReferencesDisplay;
import net.dankito.deepthought.data.model.settings.enums.Setting;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.search.specific.ReferenceBaseType;
import net.dankito.deepthought.util.localization.LanguageChangedListener;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.time.chrono.Chronology;
import java.util.Collection;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by ganymed on 21/12/14.
 */
public class EditReferenceDialogController extends EntityDialogFrameController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(EditReferenceDialogController.class);


  protected ReferenceBase editedReferenceBase = null;

  protected ReferenceBase persistedParentReferenceBase;

  protected SeriesTitle seriesTitle = null;

  protected Reference reference = null;

  protected ReferenceSubDivision referenceSubDivision = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedSeriesTitleChanges = FXCollections.observableSet();
  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedReferenceChanges = FXCollections.observableSet();
  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedReferenceSubDivisionChanges = FXCollections.observableSet();


  protected net.dankito.deepthought.controls.utils.EditedEntitiesHolder<FileLink> editedSeriesTitleAttachedFiles = null;
  protected net.dankito.deepthought.controls.utils.EditedEntitiesHolder<FileLink> editedSeriesTitleEmbeddedFiles = null;

  protected IHtmlEditorListener seriesTitleTableOfContentsListener = null;

  protected IHtmlEditorListener seriesTitleAbstractListener = null;

  protected IHtmlEditorListener seriesTitleNotesListener = null;


  protected net.dankito.deepthought.controls.utils.EditedEntitiesHolder<FileLink> editedReferenceAttachedFiles = null;
  protected net.dankito.deepthought.controls.utils.EditedEntitiesHolder<FileLink> editedReferenceEmbeddedFiles = null;

  protected IHtmlEditorListener referenceAbstractListener = null;

  protected IHtmlEditorListener referenceTableOfContentsListener = null;

  protected IHtmlEditorListener referenceNotesListener = null;


  protected net.dankito.deepthought.controls.utils.EditedEntitiesHolder<FileLink> editedReferenceSubDivisionAttachedFiles = null;
  protected net.dankito.deepthought.controls.utils.EditedEntitiesHolder<FileLink> editedReferenceSubDivisionEmbeddedFiles = null;

  protected IHtmlEditorListener referenceSubDivisionAbstractListener = null;

  protected IHtmlEditorListener referenceSubDivisionNotesListener = null;


  @FXML
  protected VBox pnContent;


  @FXML
  protected Pane paneSeriesTitle;

  @FXML
  protected ToggleButton btnShowHideSeriesTitlePane;
  @FXML
  protected ToggleButton btnShowHideSearchSeriesTitle;
  protected SearchAndSelectReferenceControl searchAndSelectSeriesTitleControl = null;

  @FXML
  protected Pane paneSeriesTitleFields;

  @FXML
  protected Pane paneSeriesTitleValues;

  @FXML
  protected ImageView imgvwSeriesTitlePreviewImage;

  @FXML
  protected Pane paneSeriesTitleHeader;

  @FXML
  protected Pane paneSeriesTitleTitle;
  @FXML
  protected TextField txtfldSeriesTitleTitle;
  @FXML
  protected Button btnChooseSeriesTitleFieldsToShow;

  @FXML
  protected Pane paneSeriesTitleSubTitle;
  @FXML
  protected TextField txtfldSeriesTitleSubTitle;

  protected net.dankito.deepthought.controls.html.CollapsibleHtmlEditor htmledSeriesTitleAbstract;

  protected net.dankito.deepthought.controls.html.CollapsibleHtmlEditor htmledSeriesTitleTableOfContents;


  protected SeriesTitlePersonsControl seriesTitlePersonsControl;

  @FXML
  protected Pane paneSeriesTitleOnlineAddress;
  @FXML
  protected TextField txtfldSeriesTitleOnlineAddress;

  protected net.dankito.deepthought.controls.html.CollapsibleHtmlEditor htmledSeriesTitleNotes;

  protected net.dankito.deepthought.controls.file.FilesControl seriesTitleFilesControl;


  @FXML
  protected Pane paneReference;

  @FXML
  protected Button btnChooseReferenceFieldsToShow;

  @FXML
  protected ToggleButton btnShowHideReferencePane;
  @FXML
  protected ToggleButton btnShowHideSearchReference;
  protected SearchAndSelectReferenceControl searchAndSelectReferenceControl = null;

  @FXML
  protected Label lblReferenceHintText;

  @FXML
  protected Pane paneReferenceFields;

  @FXML
  protected ImageView imgvwReferencePreviewImage;

  @FXML
  protected Pane paneReferenceValues;

  @FXML
  protected Pane paneReferenceTitle;
  @FXML
  protected TextField txtfldReferenceTitle;

  @FXML
  protected Pane paneReferenceSubTitle;
  @FXML
  protected TextField txtfldReferenceSubTitle;

  @FXML
  protected Pane paneReferencePublishingDate;
  @FXML
  protected TextField txtfldReferenceIssueOrPublishingDate;
  @FXML
  protected DatePicker dtpckReferencePublishingDate;

  @FXML
  protected Pane paneReferenceOnlineAddress;
  @FXML
  protected TextField txtfldReferenceOnlineAddress;

  protected net.dankito.deepthought.controls.html.CollapsibleHtmlEditor htmledReferenceAbstract;

  protected net.dankito.deepthought.controls.html.CollapsibleHtmlEditor htmledReferenceTableOfContents;


  protected net.dankito.deepthought.controls.person.ReferencePersonsControl referencePersonsControl;

  protected net.dankito.deepthought.controls.html.CollapsibleHtmlEditor htmledReferenceNotes;

  protected net.dankito.deepthought.controls.file.FilesControl referenceFilesControl;


  @FXML
  protected Pane paneReferenceSubDivision;

  @FXML
  protected ToggleButton btnShowHideReferenceSubDivisionPane;
  @FXML
  protected Button btnChooseReferenceSubDivisionFieldsToShow;

  @FXML
  protected Pane paneReferenceSubDivisionFields;

  @FXML
  protected ImageView imgvwReferenceSubDivisionPreviewImage;

  @FXML
  protected Pane paneReferenceSubDivisionValues;

  @FXML
  protected Pane paneReferenceSubDivisionTitle;
  @FXML
  protected TextField txtfldReferenceSubDivisionTitle;

  @FXML
  protected Pane paneReferenceSubDivisionSubTitle;
  @FXML
  protected TextField txtfldReferenceSubDivisionSubTitle;

  @FXML
  protected Pane paneReferenceSubDivisionOnlineAddress;
  @FXML
  protected TextField txtfldReferenceSubDivisionOnlineAddress;

  protected net.dankito.deepthought.controls.html.CollapsibleHtmlEditor htmledReferenceSubDivisionAbstract;


  protected net.dankito.deepthought.controls.person.ReferenceSubDivisionPersonsControl referenceSubDivisionPersonsControl;

  protected net.dankito.deepthought.controls.html.CollapsibleHtmlEditor htmledReferenceSubDivisionNotes;

  protected net.dankito.deepthought.controls.file.FilesControl referenceSubDivisionFilesControl;


  @Override
  protected String getEntityType() {
    return "reference";
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    fieldsWithUnsavedSeriesTitleChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        btnApplyChanges.setDisable(hasUnsavedChanges() == false);
      }
    });
    fieldsWithUnsavedReferenceChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        btnApplyChanges.setDisable(hasUnsavedChanges() == false);
      }
    });
    fieldsWithUnsavedReferenceSubDivisionChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        btnApplyChanges.setDisable(hasUnsavedChanges() == false);
      }
    });

    Localization.addLanguageChangedListener(languageChangedListener);
  }

  protected void setupControls() {
    super.setupControls();
    setButtonChooseFieldsToShowVisibility(false);

    setupSeriesTitleControls();
    setupReferenceControls();
    setupReferenceSubDivisionControls();
  }


  protected void setupSeriesTitleControls() {
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitle);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(imgvwSeriesTitlePreviewImage);

    editedSeriesTitleAttachedFiles = new net.dankito.deepthought.controls.utils.EditedEntitiesHolder<>(seriesTitle.getAttachedFiles(), event -> fieldsWithUnsavedSeriesTitleChanges.add
        (FieldWithUnsavedChanges.SeriesTitleAttachedFiles), event -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleAttachedFiles));
    editedSeriesTitleEmbeddedFiles = new net.dankito.deepthought.controls.utils.EditedEntitiesHolder<>(seriesTitle.getEmbeddedFiles());

    seriesTitleTableOfContentsListener = new DeepThoughtFxHtmlEditorListener(editedSeriesTitleEmbeddedFiles, fieldsWithUnsavedSeriesTitleChanges, FieldWithUnsavedChanges.SeriesTitleTableOfContents);
    seriesTitleAbstractListener = new DeepThoughtFxHtmlEditorListener(editedSeriesTitleEmbeddedFiles, fieldsWithUnsavedSeriesTitleChanges, FieldWithUnsavedChanges.SeriesTitleAbstract);
    seriesTitleNotesListener = new DeepThoughtFxHtmlEditorListener(editedSeriesTitleEmbeddedFiles, fieldsWithUnsavedSeriesTitleChanges, FieldWithUnsavedChanges.SeriesTitleNotes);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleFields);
    paneSeriesTitleFields.visibleProperty().bind(btnShowHideSeriesTitlePane.selectedProperty());

    btnShowHideSeriesTitlePane.selectedProperty().addListener((observable, oldValue, newValue) -> setButtonShowHideSeriesTitlePaneText());
    setButtonShowHideSeriesTitlePaneText();

    btnShowHideSearchSeriesTitle.setGraphic(new ImageView(net.dankito.deepthought.controls.Constants.SearchIconPath));

    searchAndSelectSeriesTitleControl = new SearchAndSelectReferenceControl(ReferenceBaseType.SeriesTitle, new net.dankito.deepthought.controls.reference.ISelectedReferenceHolder() {
      @Override
      public ReferenceBase getSelectedReferenceBase() {
        return seriesTitle;
      }

      @Override
      public void selectedReferenceBaseChanged(ReferenceBase newReferenceBase) {
        setSeriesTitle((SeriesTitle) newReferenceBase);
      }

      @Override
      public void addFieldChangedEvent(EventHandler<net.dankito.deepthought.controls.event.FieldChangedEvent> fieldChangedEvent) {

      }
    });

    searchAndSelectSeriesTitleControl.setVisible(false);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(searchAndSelectSeriesTitleControl);
    searchAndSelectSeriesTitleControl.visibleProperty().bind(btnShowHideSearchSeriesTitle.selectedProperty());
    searchAndSelectSeriesTitleControl.setMinHeight(190);
    searchAndSelectSeriesTitleControl.setMaxHeight(190);
    pnContent.getChildren().add(1, searchAndSelectSeriesTitleControl);

    txtfldSeriesTitleTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleTitle);
      if (editedReferenceBase instanceof SeriesTitle)
        updateWindowTitle();
    });

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleSubTitle);
    txtfldSeriesTitleSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleSubTitle));

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleOnlineAddress);
    txtfldSeriesTitleOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleOnlineAddress));

    htmledSeriesTitleAbstract = new net.dankito.deepthought.controls.html.CollapsibleHtmlEditor("abstract", seriesTitleAbstractListener);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledSeriesTitleAbstract);
    paneSeriesTitleValues.getChildren().add(3, htmledSeriesTitleAbstract);
    VBox.setMargin(htmledSeriesTitleAbstract, new Insets(6, 0, 0, 0));
    htmledSeriesTitleAbstract.setExpanded(false);

    htmledSeriesTitleTableOfContents = new net.dankito.deepthought.controls.html.CollapsibleHtmlEditor("table.of.contents", seriesTitleTableOfContentsListener);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledSeriesTitleTableOfContents);
    paneSeriesTitleValues.getChildren().add(4, htmledSeriesTitleTableOfContents);
    VBox.setMargin(htmledSeriesTitleTableOfContents, new Insets(6, 0, 0, 0));
    htmledSeriesTitleTableOfContents.setExpanded(false);

    seriesTitlePersonsControl = new SeriesTitlePersonsControl();
    seriesTitlePersonsControl.setExpanded(true);
    seriesTitlePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitlePersons));
    seriesTitlePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitlePersons));
    VBox.setMargin(seriesTitlePersonsControl, new Insets(6, 0, 0, 0));
    paneSeriesTitleValues.getChildren().add(5, seriesTitlePersonsControl);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(seriesTitlePersonsControl);
    seriesTitlePersonsControl.setVisible(false);

    htmledSeriesTitleNotes = new net.dankito.deepthought.controls.html.CollapsibleHtmlEditor("notes", seriesTitleNotesListener);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledSeriesTitleNotes);
    paneSeriesTitleValues.getChildren().add(6, htmledSeriesTitleNotes);
    VBox.setMargin(htmledSeriesTitleNotes, new Insets(6, 0, 0, 0));
    htmledSeriesTitleNotes.setExpanded(false);

    seriesTitleFilesControl = new net.dankito.deepthought.controls.file.FilesControl(editedSeriesTitleAttachedFiles);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(seriesTitleFilesControl);
    seriesTitleFilesControl.setMinHeight(Region.USE_PREF_SIZE);
    seriesTitleFilesControl.setPrefHeight(Region.USE_COMPUTED_SIZE);
    seriesTitleFilesControl.setMaxHeight(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);
    paneSeriesTitleValues.getChildren().add(7, seriesTitleFilesControl);
    VBox.setMargin(seriesTitleFilesControl, new Insets(6, 0, 0, 0));
  }

  protected void setSeriesTitle(SeriesTitle newSeriesTitle) {
    if(hasSeriesTitleBeenEdited()) {
      if(net.dankito.deepthought.util.Alerts.askUserIfEditedSeriesTitleShouldBeSaved(seriesTitle) == true) {
        if (seriesTitle.isPersisted() == false)
          Application.getDeepThought().addSeriesTitle(seriesTitle);
        saveEditedFieldsOnSeriesTitle();
      }
    }

    seriesTitle = newSeriesTitle;
    if(seriesTitle == null)
      setToNewSeries();

    setSeriesTitleValues(seriesTitle);
  }

  protected void setupReferenceControls() {
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(imgvwReferencePreviewImage);

    editedReferenceAttachedFiles = new net.dankito.deepthought.controls.utils.EditedEntitiesHolder<>(reference.getAttachedFiles(), event -> fieldsWithUnsavedReferenceChanges.add
        (FieldWithUnsavedChanges.ReferenceAttachedFiles), event -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceAttachedFiles));
    editedReferenceEmbeddedFiles = new net.dankito.deepthought.controls.utils.EditedEntitiesHolder<>(reference.getEmbeddedFiles());

    referenceAbstractListener = new DeepThoughtFxHtmlEditorListener(editedReferenceEmbeddedFiles, fieldsWithUnsavedReferenceChanges, FieldWithUnsavedChanges.ReferenceAbstract);
    referenceTableOfContentsListener = new DeepThoughtFxHtmlEditorListener(editedReferenceEmbeddedFiles, fieldsWithUnsavedReferenceChanges, FieldWithUnsavedChanges.ReferenceTableOfContents);
    referenceNotesListener = new DeepThoughtFxHtmlEditorListener(editedReferenceEmbeddedFiles, fieldsWithUnsavedReferenceChanges, FieldWithUnsavedChanges.ReferenceNotes);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnShowHideReferencePane);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblReferenceHintText);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceFields);
    paneReferenceFields.visibleProperty().bind(btnShowHideReferencePane.selectedProperty());

    btnShowHideReferencePane.selectedProperty().addListener((observable, oldValue, newValue) -> setButtonShowHideReferencePaneText());
    setButtonShowHideReferencePaneText();

    btnShowHideSearchReference.setGraphic(new ImageView(net.dankito.deepthought.controls.Constants.SearchIconPath));

    searchAndSelectReferenceControl = new SearchAndSelectReferenceControl(ReferenceBaseType.Reference, new net.dankito.deepthought.controls.reference.ISelectedReferenceHolder() {
      @Override
      public ReferenceBase getSelectedReferenceBase() {
        return reference;
      }

      @Override
      public void selectedReferenceBaseChanged(ReferenceBase newReferenceBase) {
        setReference((Reference) newReferenceBase);
      }

      @Override
      public void addFieldChangedEvent(EventHandler<net.dankito.deepthought.controls.event.FieldChangedEvent> fieldChangedEvent) {

      }
    });

    searchAndSelectReferenceControl.setVisible(false);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(searchAndSelectReferenceControl);
    searchAndSelectReferenceControl.visibleProperty().bind(btnShowHideSearchReference.selectedProperty());
    searchAndSelectReferenceControl.setMinHeight(190);
    searchAndSelectReferenceControl.setMaxHeight(190);
    pnContent.getChildren().add(4, searchAndSelectReferenceControl);

    txtfldReferenceTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceTitle);
      if (editedReferenceBase instanceof Reference)
        updateWindowTitle();
    });

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubTitle);
    txtfldReferenceSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceSubTitle));

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferencePublishingDate);
    txtfldReferenceIssueOrPublishingDate.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceIssueOrPublishingDate);
    });

    dtpckReferencePublishingDate.valueProperty().addListener((observable, oldValue, newValue) -> setReferenceIssueTextFieldToDateSelectedInDatePicker());

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceOnlineAddress);
    txtfldReferenceOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceOnlineAddress));

    htmledReferenceAbstract = new net.dankito.deepthought.controls.html.CollapsibleHtmlEditor("abstract", referenceAbstractListener);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledReferenceAbstract);
    paneReferenceValues.getChildren().add(4, htmledReferenceAbstract);
    VBox.setMargin(htmledReferenceAbstract, new Insets(6, 0, 0, 0));
    htmledReferenceAbstract.setExpanded(false);

    htmledReferenceTableOfContents = new net.dankito.deepthought.controls.html.CollapsibleHtmlEditor("table.of.contents", referenceTableOfContentsListener);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledReferenceTableOfContents);
    paneReferenceValues.getChildren().add(5, htmledReferenceTableOfContents);
    VBox.setMargin(htmledReferenceTableOfContents, new Insets(6, 0, 0, 0));
    htmledReferenceTableOfContents.setExpanded(false);

    referencePersonsControl = new net.dankito.deepthought.controls.person.ReferencePersonsControl();
    referencePersonsControl.setExpanded(false);
    referencePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferencePersons));
    referencePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferencePersons));
    VBox.setMargin(referencePersonsControl, new Insets(6, 0, 0, 0));
    paneReferenceValues.getChildren().add(6, referencePersonsControl);

    htmledReferenceNotes = new net.dankito.deepthought.controls.html.CollapsibleHtmlEditor("notes", referenceNotesListener);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledReferenceNotes);
    paneReferenceValues.getChildren().add(7, htmledReferenceNotes);
    VBox.setMargin(htmledReferenceNotes, new Insets(6, 0, 0, 0));
    htmledReferenceNotes.setExpanded(false);

    referenceFilesControl = new net.dankito.deepthought.controls.file.FilesControl(editedReferenceAttachedFiles);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(referenceFilesControl);
    referenceFilesControl.setMinHeight(Region.USE_PREF_SIZE);
    referenceFilesControl.setPrefHeight(Region.USE_COMPUTED_SIZE);
    referenceFilesControl.setMaxHeight(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);
    paneReferenceValues.getChildren().add(8, referenceFilesControl);
    VBox.setMargin(referenceFilesControl, new Insets(6, 0, 0, 0));
  }

  protected void setReferenceIssueTextFieldToDateSelectedInDatePicker() {
    if(dtpckReferencePublishingDate.getValue() != null) {
      Date date = net.dankito.deepthought.util.DateConvertUtils.asUtilDate(dtpckReferencePublishingDate.getValue());
      txtfldReferenceIssueOrPublishingDate.setText(DateFormat.getDateInstance(Reference.PublishingDateFormat, Localization.getLanguageLocale()).format(date));
    }
  }

  protected void setReference(Reference newReference) {
    if(hasReferenceBeenEdited()) {
      if(net.dankito.deepthought.util.Alerts.askUserIfEditedReferenceShouldBeSaved(reference) == true) {
        if (reference.isPersisted() == false)
          Application.getDeepThought().addReference(reference);
        saveEditedFieldsOnReference();
      }
    }

    reference = newReference;
    setReferenceValues(reference);

    if(newReference.getSeries() != seriesTitle)
      setSeriesTitle(newReference.getSeries());
  }

  protected void setupReferenceSubDivisionControls() {
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivision);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(imgvwReferenceSubDivisionPreviewImage);

    editedReferenceSubDivisionAttachedFiles = new net.dankito.deepthought.controls.utils.EditedEntitiesHolder<>(referenceSubDivision.getAttachedFiles(), event -> fieldsWithUnsavedReferenceSubDivisionChanges.add
        (FieldWithUnsavedChanges.ReferenceSubDivisionAttachedFiles), event -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionAttachedFiles));
    editedReferenceSubDivisionEmbeddedFiles = new net.dankito.deepthought.controls.utils.EditedEntitiesHolder<>(referenceSubDivision.getEmbeddedFiles());

    referenceSubDivisionAbstractListener = new DeepThoughtFxHtmlEditorListener(editedReferenceSubDivisionEmbeddedFiles, fieldsWithUnsavedReferenceSubDivisionChanges, FieldWithUnsavedChanges.ReferenceSubDivisionAbstract);
    referenceSubDivisionNotesListener = new DeepThoughtFxHtmlEditorListener(editedReferenceSubDivisionEmbeddedFiles, fieldsWithUnsavedReferenceSubDivisionChanges, FieldWithUnsavedChanges.ReferenceSubDivisionNotes);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivisionFields);
    paneReferenceSubDivisionFields.visibleProperty().bind(btnShowHideReferenceSubDivisionPane.selectedProperty());

    btnShowHideReferenceSubDivisionPane.selectedProperty().addListener((observable, oldValue, newValue) -> setButtonShowHideReferenceSubDivisionPaneText());
    setButtonShowHideReferenceSubDivisionPaneText();

    txtfldReferenceSubDivisionTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionTitle);
      if (editedReferenceBase instanceof ReferenceSubDivision)
        updateWindowTitle();
    });

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivisionSubTitle);
    txtfldReferenceSubDivisionSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle));

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivisionOnlineAddress);
    txtfldReferenceSubDivisionOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress));

    htmledReferenceSubDivisionAbstract = new net.dankito.deepthought.controls.html.CollapsibleHtmlEditor("abstract", referenceSubDivisionAbstractListener);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledReferenceSubDivisionAbstract);
    paneReferenceSubDivisionValues.getChildren().add(3, htmledReferenceSubDivisionAbstract);
    VBox.setMargin(htmledReferenceSubDivisionAbstract, new Insets(6, 0, 0, 0));
    htmledReferenceSubDivisionAbstract.setExpanded(false);

    referenceSubDivisionPersonsControl = new net.dankito.deepthought.controls.person.ReferenceSubDivisionPersonsControl();
    referenceSubDivisionPersonsControl.setExpanded(true);
    referenceSubDivisionPersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionPersons));
    referenceSubDivisionPersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionPersons));
    VBox.setMargin(referenceSubDivisionPersonsControl, new Insets(6, 0, 0, 0));
    paneReferenceSubDivisionValues.getChildren().add(4, referenceSubDivisionPersonsControl);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(referenceSubDivisionPersonsControl);

    htmledReferenceSubDivisionNotes = new net.dankito.deepthought.controls.html.CollapsibleHtmlEditor("notes", referenceSubDivisionNotesListener);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledReferenceSubDivisionNotes);
    paneReferenceSubDivisionValues.getChildren().add(5, htmledReferenceSubDivisionNotes);
    VBox.setMargin(htmledReferenceSubDivisionNotes, new Insets(6, 0, 0, 0));
    htmledReferenceSubDivisionNotes.setExpanded(false);

    referenceSubDivisionFilesControl = new net.dankito.deepthought.controls.file.FilesControl(editedReferenceSubDivisionAttachedFiles);
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(referenceSubDivisionFilesControl);
    referenceSubDivisionFilesControl.setMinHeight(Region.USE_PREF_SIZE);
    referenceSubDivisionFilesControl.setPrefHeight(Region.USE_COMPUTED_SIZE);
    referenceSubDivisionFilesControl.setMaxHeight(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);
    paneReferenceSubDivisionValues.getChildren().add(6, referenceSubDivisionFilesControl);
    VBox.setMargin(referenceSubDivisionFilesControl, new Insets(6, 0, 0, 0));
  }


  protected void referencesDisplayChanged(ReferencesDisplay referencesDisplay) {
    boolean showAllReferenceTypes = referencesDisplay == ReferencesDisplay.ShowAll;

    paneSeriesTitle.setVisible(showAllReferenceTypes);

    btnShowHideReferencePane.setVisible(showAllReferenceTypes);
    lblReferenceHintText.setVisible(showAllReferenceTypes);

    paneReferenceSubDivision.setVisible(showAllReferenceTypes);
  }

  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
    btnChooseSeriesTitleFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.All);

    paneSeriesTitleSubTitle.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    htmledSeriesTitleAbstract.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);
    htmledSeriesTitleTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    seriesTitlePersonsControl.setVisible(seriesTitle.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    paneSeriesTitleOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    htmledSeriesTitleNotes.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);
    seriesTitleFilesControl.setVisible(seriesTitle.hasAttachedFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.All);


    btnChooseReferenceFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.All);

    paneReferenceSubTitle.setVisible(StringUtils.isNotNullOrEmpty(reference.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    htmledReferenceAbstract.setVisible(StringUtils.isNotNullOrEmpty(reference.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);
    htmledReferenceTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(reference.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    paneReferenceOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(reference.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    htmledReferenceNotes.setVisible(StringUtils.isNotNullOrEmpty(reference.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);
    referenceFilesControl.setVisible(reference.hasAttachedFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    btnChooseReferenceSubDivisionFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.All);

    paneReferenceSubDivisionSubTitle.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    htmledReferenceSubDivisionAbstract.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    referenceSubDivisionPersonsControl.setVisible(referenceSubDivision.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    paneReferenceSubDivisionOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);

    htmledReferenceSubDivisionNotes.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.All);
    referenceSubDivisionFilesControl.setVisible(referenceSubDivision.hasAttachedFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.All);
  }


  public ReferenceBase getEditedReferenceBase() {
    return editedReferenceBase;
  }

  @Override
  protected void saveEntity() {
    // TODO: also check if a previously set Entity now has been unset

    // TODO: save async while closing the dialog? Would make Dialog closing faster
    if(hasSeriesTitleBeenEdited()) {
      saveEditedFieldsOnSeriesTitle();

      if (seriesTitle.isPersisted() == false) { // a new SeriesTitle
        Application.getDeepThought().addSeriesTitle(seriesTitle);
      }

      if(editedReferenceBase == null)
        editedReferenceBase = seriesTitle;
    }

    if(hasReferenceBeenEdited()) {
      saveEditedFieldsOnReference();

      if (reference.isPersisted() == false) { // a new Reference
        Application.getDeepThought().addReference(reference);
//        if (persistedParentReferenceBase instanceof SeriesTitle) // is this really needed or are the two lines below sufficient
//          ((SeriesTitle) persistedParentReferenceBase).addSerialPart(reference);
      }

      if(seriesTitle.isPersisted())
        reference.setSeries(seriesTitle);

      if(editedReferenceBase == null)
        editedReferenceBase = reference;
    }

    if(hasReferenceSubDivisionBeenEdited()) {
      saveEditedFieldsOnReferenceSubDivision();

      if (referenceSubDivision.isPersisted() == false) { // a new ReferenceSubDivision
        Application.getDeepThought().addReferenceSubDivision(referenceSubDivision);
      }

      if(reference.isPersisted())
        reference.addSubDivision(referenceSubDivision);

      if(editedReferenceBase == null)
        editedReferenceBase = referenceSubDivision;
    }

    if(reference.isPersisted()) {
      if(seriesTitle.isPersisted() == false && reference.getSeries() != null)
        reference.setSeries(null);
      else if(seriesTitle.isPersisted() == true && reference.getSeries() != seriesTitle)
        reference.setSeries(seriesTitle);
    }

    if(referenceSubDivision.isPersisted()) {
      if(reference.isPersisted() == false && referenceSubDivision.getReference() != null)
        referenceSubDivision.setReference(reference);
      else if(reference.isPersisted() == true && referenceSubDivision.getReference() != reference)
        referenceSubDivision.setReference(reference);
    }
  }

  @Override
  protected boolean hasUnsavedChanges() {
    return hasSeriesTitleBeenEdited() || hasReferenceBeenEdited() || hasReferenceSubDivisionBeenEdited();
  }

  protected boolean hasSeriesTitleBeenEdited() {
    return fieldsWithUnsavedSeriesTitleChanges.size() > 0;
  }

  protected boolean hasReferenceBeenEdited() {
    return fieldsWithUnsavedReferenceChanges.size() > 0;
  }

  protected boolean hasReferenceSubDivisionBeenEdited() {
    return fieldsWithUnsavedReferenceSubDivisionChanges.size() > 0;
  }

  @Override
  protected void closeDialog() {
    seriesTitle.removeEntityListener(seriesTitleListener);
    reference.removeEntityListener(referenceListener);
    referenceSubDivision.removeEntityListener(referenceSubDivisionListener);

    cleanUpControls();

    if(Application.getSettings() != null) {
      Application.getSettings().removeSettingsChangedListener(userSettingsChanged);
    }

    super.closeDialog();
  }

  protected void cleanUpControls() {
    // i don't get it: referencePersonsControl never gets removed from Memory, all others in approximately 50 % of all cases
    // TODO: find Memory leaks
    searchAndSelectSeriesTitleControl.cleanUp();
    paneSeriesTitleValues.getChildren().remove(searchAndSelectSeriesTitleControl);
    searchAndSelectSeriesTitleControl = null;
    htmledSeriesTitleTableOfContents.cleanUp();
    htmledSeriesTitleAbstract.cleanUp();
    seriesTitlePersonsControl.cleanUp();
    paneSeriesTitleValues.getChildren().remove(seriesTitlePersonsControl);
    seriesTitlePersonsControl = null;
    htmledSeriesTitleNotes.cleanUp();
    seriesTitleFilesControl.cleanUp();

    searchAndSelectReferenceControl.cleanUp();
    htmledReferenceAbstract.cleanUp();
    htmledReferenceTableOfContents.cleanUp();
    paneReferenceValues.getChildren().remove(searchAndSelectReferenceControl);
    searchAndSelectReferenceControl = null;
    referencePersonsControl.cleanUp();
    paneReferenceValues.getChildren().remove(referencePersonsControl);
    referencePersonsControl = null;
    htmledReferenceNotes.cleanUp();
    referenceFilesControl.cleanUp();

    htmledReferenceSubDivisionAbstract.cleanUp();
    referenceSubDivisionPersonsControl.cleanUp();
    paneReferenceSubDivisionValues.getChildren().remove(referenceSubDivisionPersonsControl);
    referenceSubDivisionPersonsControl = null;
    htmledReferenceSubDivisionNotes.cleanUp();
    referenceSubDivisionFilesControl.cleanUp();

    Localization.removeLanguageChangedListener(languageChangedListener);
  }

  protected void saveEditedFieldsOnSeriesTitle() {
    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleTitle)) {
      seriesTitle.setTitle(txtfldSeriesTitleTitle.getText());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleTitle);
    }
    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleSubTitle)) {
      seriesTitle.setSubTitle(txtfldSeriesTitleSubTitle.getText());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleSubTitle);
    }

    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleAbstract)) {
      seriesTitle.setAbstract(htmledSeriesTitleAbstract.getHtml());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleAbstract);
    }
    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleTableOfContents) || htmledSeriesTitleTableOfContents.getHtml().equals(seriesTitle.getTableOfContents()) == false) {
      seriesTitle.setTableOfContents(htmledSeriesTitleTableOfContents.getHtml());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleTableOfContents);
    }

    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitlePersons)) {
      for(Person removedPerson : seriesTitlePersonsControl.getCopyOfRemovedPersonsAndClear())
        seriesTitle.removePerson(removedPerson);

      for(Person addedPerson : seriesTitlePersonsControl.getCopyOfAddedPersonsAndClear())
        seriesTitle.addPerson(addedPerson);

      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitlePersons);
    }

    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleOnlineAddress)) {
      seriesTitle.setOnlineAddress(txtfldSeriesTitleOnlineAddress.getText());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleOnlineAddress);
    }

    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleSerialParts)) {

      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleSerialParts);
    }

    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleNotes)) {
      seriesTitle.setNotes(htmledSeriesTitleNotes.getHtml());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleNotes);
    }

    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleAttachedFiles)) {
      for(FileLink removedFile : editedSeriesTitleAttachedFiles.getRemovedEntities())
        seriesTitle.removeAttachedFile(removedFile);
      editedSeriesTitleAttachedFiles.getRemovedEntities().clear();

      for(FileLink addedFile : editedSeriesTitleAttachedFiles.getAddedEntities())
        seriesTitle.addAttachedFile(addedFile);
      editedSeriesTitleAttachedFiles.getAddedEntities().clear();

      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleAttachedFiles);
    }

    for(FileLink removedEmbeddedFile : editedSeriesTitleEmbeddedFiles.getRemovedEntities())
      seriesTitle.removeEmbeddedFile(removedEmbeddedFile);
    editedSeriesTitleEmbeddedFiles.getRemovedEntities().clear();

    for(FileLink addedEmbeddedFile : editedSeriesTitleEmbeddedFiles.getAddedEntities())
      seriesTitle.addEmbeddedFile(addedEmbeddedFile);
    editedSeriesTitleEmbeddedFiles.getAddedEntities().clear();
  }

  protected void saveEditedFieldsOnReference() {
    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceSeriesTitle)) {
      //TODO
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceSeriesTitle);
    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceTitle)) {
      reference.setTitle(txtfldReferenceTitle.getText());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.ReferenceTitle);
    }
    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceSubTitle)) {
      reference.setSubTitle(txtfldReferenceSubTitle.getText());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceSubTitle);
    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceIssueOrPublishingDate)) {
      reference.setIssueOrPublishingDate(txtfldReferenceIssueOrPublishingDate.getText());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceIssueOrPublishingDate);
    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceOnlineAddress)) {
      reference.setOnlineAddress(txtfldReferenceOnlineAddress.getText());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceOnlineAddress);
    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceAbstract)) {
      reference.setAbstract(htmledReferenceAbstract.getHtml());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceAbstract);
    }
    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceTableOfContents) || htmledReferenceTableOfContents.getHtml().equals(reference.getTableOfContents()) == false) {
      if(net.dankito.deepthought.controls.utils.FXUtils.HtmlEditorDefaultText.equals(htmledReferenceTableOfContents.getHtml())) {
        if(StringUtils.isNotNullOrEmpty(reference.getTableOfContents()))
          reference.setTableOfContents("");
      }
      else
        reference.setTableOfContents(htmledReferenceTableOfContents.getHtml());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceTableOfContents);
    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferencePersons)) {
      for(Person removedPerson : referencePersonsControl.getCopyOfRemovedPersonsAndClear())
        reference.removePerson(removedPerson);

      for(Person addedPerson : referencePersonsControl.getCopyOfAddedPersonsAndClear())
        reference.addPerson(addedPerson);

      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferencePersons);
    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceNotes)) {
      reference.setNotes(htmledReferenceNotes.getHtml());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceNotes);
    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceAttachedFiles)) {
      for(FileLink removedFile : editedReferenceAttachedFiles.getRemovedEntities())
        reference.removeAttachedFile(removedFile);
      editedReferenceAttachedFiles.getRemovedEntities().clear();

      for(FileLink addedFile : editedReferenceAttachedFiles.getAddedEntities())
        reference.addAttachedFile(addedFile);
      editedReferenceAttachedFiles.getAddedEntities().clear();

      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceAttachedFiles);
    }

    for(FileLink removedEmbeddedFile : editedReferenceEmbeddedFiles.getRemovedEntities())
      reference.removeEmbeddedFile(removedEmbeddedFile);
    editedReferenceEmbeddedFiles.getRemovedEntities().clear();

    for(FileLink addedEmbeddedFile : editedReferenceEmbeddedFiles.getAddedEntities())
      reference.addEmbeddedFile(addedEmbeddedFile);
    editedReferenceEmbeddedFiles.getAddedEntities().clear();
  }

  protected void saveEditedFieldsOnReferenceSubDivision() {
    if(fieldsWithUnsavedReferenceSubDivisionChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionTitle)) {
      referenceSubDivision.setTitle(txtfldReferenceSubDivisionTitle.getText());
      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionTitle);
    }
    if(fieldsWithUnsavedReferenceSubDivisionChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle)) {
      referenceSubDivision.setSubTitle(txtfldReferenceSubDivisionSubTitle.getText());
      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle);
    }

    if(fieldsWithUnsavedReferenceSubDivisionChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract)) {
      referenceSubDivision.setAbstract(htmledReferenceSubDivisionAbstract.getHtml());
      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract);
    }

    if(fieldsWithUnsavedReferenceSubDivisionChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionPersons)) {
      for(Person removedPerson : referenceSubDivisionPersonsControl.getCopyOfRemovedPersonsAndClear())
        referenceSubDivision.removePerson(removedPerson);

      for(Person addedPerson : referenceSubDivisionPersonsControl.getCopyOfAddedPersonsAndClear())
        referenceSubDivision.addPerson(addedPerson);

      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionPersons);
    }

    if(fieldsWithUnsavedReferenceSubDivisionChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress)) {
      referenceSubDivision.setOnlineAddress(txtfldReferenceSubDivisionOnlineAddress.getText());
      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress);
    }

    if(fieldsWithUnsavedReferenceSubDivisionChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionNotes)) {
      referenceSubDivision.setNotes(htmledReferenceSubDivisionNotes.getHtml());
      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionNotes);
    }

    if(fieldsWithUnsavedReferenceSubDivisionChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionAttachedFiles)) {
      for(FileLink removedFile : editedReferenceSubDivisionAttachedFiles.getRemovedEntities())
        referenceSubDivision.removeAttachedFile(removedFile);
      editedReferenceSubDivisionAttachedFiles.getRemovedEntities().clear();

      for(FileLink addedFile : editedReferenceSubDivisionAttachedFiles.getAddedEntities())
        referenceSubDivision.addAttachedFile(addedFile);
      editedReferenceSubDivisionAttachedFiles.getAddedEntities().clear();

      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionAttachedFiles);
    }

    for(FileLink removedEmbeddedFile : editedReferenceSubDivisionEmbeddedFiles.getRemovedEntities())
      referenceSubDivision.removeEmbeddedFile(removedEmbeddedFile);
    editedReferenceSubDivisionEmbeddedFiles.getRemovedEntities().clear();

    for(FileLink addedEmbeddedFile : editedReferenceSubDivisionEmbeddedFiles.getAddedEntities())
      referenceSubDivision.addEmbeddedFile(addedEmbeddedFile);
    editedReferenceSubDivisionEmbeddedFiles.getAddedEntities().clear();
  }


  protected void setSeriesTitleValues(final SeriesTitle seriesTitle) {
    imgvwSeriesTitlePreviewImage.setVisible(seriesTitle.hasPreviewImage());
    if(seriesTitle.hasPreviewImage()) {
      imgvwSeriesTitlePreviewImage.setImage(new Image(seriesTitle.getPreviewImage().getUriString()));
    }

    txtfldSeriesTitleTitle.setText(seriesTitle.getTitle());
    txtfldSeriesTitleSubTitle.setText(seriesTitle.getSubTitle());

    htmledSeriesTitleAbstract.setHtml(seriesTitle.getAbstract());
    htmledSeriesTitleTableOfContents.setHtml(seriesTitle.getTableOfContents());

    txtfldSeriesTitleOnlineAddress.setText(seriesTitle.getOnlineAddress());

    htmledSeriesTitleNotes.setHtml(seriesTitle.getNotes());

    seriesTitlePersonsControl.setSeries(seriesTitle);

    fieldsWithUnsavedSeriesTitleChanges.clear();

    seriesTitle.addEntityListener(seriesTitleListener);
  }

  protected void setReferenceValues(final Reference reference) {
    imgvwReferencePreviewImage.setVisible(reference.hasPreviewImage());
    if(reference.hasPreviewImage()) {
      imgvwReferencePreviewImage.setImage(new Image(reference.getPreviewImage().getUriString()));
    }

    txtfldReferenceTitle.setText(reference.getTitle());
    txtfldReferenceSubTitle.setText(reference.getSubTitle());

    htmledReferenceAbstract.setHtml(reference.getAbstract());
    htmledReferenceTableOfContents.setHtml(reference.getTableOfContents());

    referencePersonsControl.setReference(reference);

    txtfldReferenceIssueOrPublishingDate.setText(reference.getIssueOrPublishingDate());
    dtpckReferencePublishingDate.setValue(net.dankito.deepthought.util.DateConvertUtils.asLocalDate(reference.getPublishingDate()));

    txtfldReferenceOnlineAddress.setText(reference.getOnlineAddress());

    htmledReferenceNotes.setHtml(reference.getNotes());

//    trtblvwFiles.setRoot(new FileRootTreeItem(reference));

    fieldsWithUnsavedReferenceChanges.clear();

    reference.addEntityListener(referenceListener);
  }

  protected void setReferenceSubDivisionValues(final ReferenceSubDivision subDivision) {
    imgvwReferenceSubDivisionPreviewImage.setVisible(subDivision.hasPreviewImage());
    if(subDivision.hasPreviewImage()) {
      imgvwReferenceSubDivisionPreviewImage.setImage(new Image(subDivision.getPreviewImage().getUriString()));
    }

    txtfldReferenceSubDivisionTitle.setText(subDivision.getTitle());
    txtfldReferenceSubDivisionSubTitle.setText(subDivision.getSubTitle());

    htmledReferenceSubDivisionAbstract.setHtml(subDivision.getAbstract());

    referenceSubDivisionPersonsControl.setSubDivision(subDivision);

    txtfldReferenceSubDivisionOnlineAddress.setText(subDivision.getOnlineAddress());

    htmledReferenceSubDivisionNotes.setHtml(subDivision.getNotes());

//    trtblvwReferenceSubDivisionFiles.setRoot(new FileRootTreeItem(subDivision));

    fieldsWithUnsavedReferenceSubDivisionChanges.clear();

    subDivision.addEntityListener(referenceSubDivisionListener);
  }



  @FXML
  public void setButtonShowHideSeriesTitlePaneText() {
    if(btnShowHideSeriesTitlePane.isSelected())
      btnShowHideSeriesTitlePane.setText(net.dankito.deepthought.controls.CollapsiblePane.ExpandedText);
    else
      btnShowHideSeriesTitlePane.setText(net.dankito.deepthought.controls.CollapsiblePane.CollapsedText);
  }

  public void handleButtonChooseSeriesTitleFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneSeriesTitleSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleSubTitle, "subtitle");

    if(htmledSeriesTitleAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledSeriesTitleAbstract, "abstract");
    if(htmledSeriesTitleTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledSeriesTitleTableOfContents, "table.of.contents");

    if(seriesTitlePersonsControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, seriesTitlePersonsControl, "persons");

    if(paneSeriesTitleOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleOnlineAddress, "online.address");
    }

    if(htmledSeriesTitleNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledSeriesTitleNotes, "notes");
    if(seriesTitleFilesControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, seriesTitleFilesControl, "files");

    hiddenFieldsMenu.show(btnChooseSeriesTitleFieldsToShow, Side.BOTTOM, 0, 0);
  }


  @FXML
  public void setButtonShowHideReferencePaneText() {
    if(btnShowHideReferencePane.isSelected())
      btnShowHideReferencePane.setText(net.dankito.deepthought.controls.CollapsiblePane.ExpandedText);
    else
      btnShowHideReferencePane.setText(net.dankito.deepthought.controls.CollapsiblePane.CollapsedText);
  }

  public void handleButtonChooseReferenceFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneReferenceSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceSubTitle, "subtitle");

    if(paneReferenceOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceOnlineAddress, "online.address");

    if(htmledReferenceAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledReferenceAbstract, "abstract");
    if(htmledReferenceTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledReferenceTableOfContents, "table.of.contents");
    }

    if(htmledReferenceNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledReferenceNotes, "notes");
    if(referenceFilesControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, referenceFilesControl, "files");

    hiddenFieldsMenu.show(btnChooseReferenceFieldsToShow, Side.BOTTOM, 0, 0);
  }


  @FXML
  public void setButtonShowHideReferenceSubDivisionPaneText() {
    if(btnShowHideReferenceSubDivisionPane.isSelected())
      btnShowHideReferenceSubDivisionPane.setText(net.dankito.deepthought.controls.CollapsiblePane.ExpandedText);
    else
      btnShowHideReferenceSubDivisionPane.setText(net.dankito.deepthought.controls.CollapsiblePane.CollapsedText);
  }

  public void handleButtonChooseReferenceSubDivisionFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneReferenceSubDivisionSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceSubDivisionSubTitle, "subtitle");

    if(htmledReferenceSubDivisionAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledReferenceSubDivisionAbstract, "abstract");

    if(referenceSubDivisionPersonsControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, referenceSubDivisionPersonsControl, "persons");

    if(paneReferenceSubDivisionOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceSubDivisionOnlineAddress, "online.address");
    }

    if(htmledReferenceSubDivisionNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledReferenceSubDivisionNotes, "notes");
    if(referenceSubDivisionFilesControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, referenceSubDivisionFilesControl, "files");

    hiddenFieldsMenu.show(btnChooseReferenceSubDivisionFieldsToShow, Side.BOTTOM, 0, 0);
  }


  public void setWindowStageAndReferenceBase(Stage windowStage, ReferenceBase referenceBase, ReferenceBase persistedParentReferenceBase) {
    // TODO: if referenceBase != null disallow editing of Entities below referenceBase's Hierarchy (e.g. referenceBase instanceof Reference -> don't allow ReferenceSubDivision editing
    editedReferenceBase = referenceBase;

    Node nodeToFocus = setReferenceBases(referenceBase, persistedParentReferenceBase);

    if(persistedParentReferenceBase != null) {
      nodeToFocus = setPersistedParentReferenceBase(persistedParentReferenceBase, nodeToFocus);
    }

    super.setWindowStage(windowStage, referenceBase);

    setupDialog(nodeToFocus);
  }

  public void setWindowStageAndReferenceBase(Stage windowStage, EntryCreationResult creationResult) {
    Node nodeToFocus = setReferenceBaseInstances(creationResult);
    super.setWindowStage(windowStage, editedReferenceBase);

    setupDialog(nodeToFocus);

    if(creationResult.getSeriesTitle() != null && seriesTitle.isPersisted() == false)
      fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleTitle);
    if(creationResult.getReference() != null && reference.isPersisted() == false)
      fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceTitle);
    if(creationResult.getReferenceSubDivision() != null && referenceSubDivision.isPersisted() == false)
      fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubTitle);
  }

  protected Node setReferenceBaseInstances(EntryCreationResult creationResult) {
    Node nodeToFocus = txtfldReferenceTitle;

    if(creationResult.getSeriesTitle() != null) {
      seriesTitle = creationResult.getSeriesTitle();
      editedReferenceBase = seriesTitle;
      nodeToFocus = txtfldSeriesTitleTitle;
      btnShowHideSeriesTitlePane.setSelected(true);
    }
    else
      setToNewSeries();

    if(creationResult.getReference() != null) {
      reference = creationResult.getReference();
      editedReferenceBase = reference;
      nodeToFocus = txtfldReferenceTitle;
      btnShowHideReferencePane.setSelected(true);
    }
    else
      setToNewReference();

    if(creationResult.getReferenceSubDivision() != null) {
      referenceSubDivision = creationResult.getReferenceSubDivision();
      editedReferenceBase = referenceSubDivision;
      nodeToFocus = txtfldReferenceSubDivisionTitle;
      btnShowHideReferenceSubDivisionPane.setSelected(true);
    }
    else
      setToNewReferenceSubDivision();

    return nodeToFocus;
  }

  protected void setToNewSeries() {
    seriesTitle = new SeriesTitle();
    btnShowHideSeriesTitlePane.setSelected(false);
  }

  protected void setToNewReference() {
    reference = new Reference();
  }

  protected void setToNewReferenceSubDivision() {
    referenceSubDivision = new ReferenceSubDivision();
    btnShowHideReferenceSubDivisionPane.setSelected(false);
  }

  protected void setupDialog(Node nodeToFocus) {
    setSeriesTitleValues(seriesTitle);
    setReferenceValues(reference);
    setReferenceSubDivisionValues(referenceSubDivision);

    applyUserSettings();

    if(seriesTitle.isPersisted())
      btnShowHideSeriesTitlePane.setSelected(true);
    if(reference.isPersisted())
      btnShowHideReferencePane.setSelected(true);
    if(referenceSubDivision.isPersisted())
      btnShowHideReferenceSubDivisionPane.setSelected(true);

    net.dankito.deepthought.controls.utils.FXUtils.focusNode(nodeToFocus);
  }

  protected void applyUserSettings() {
    UserDeviceSettings settings = Application.getSettings();

    referencesDisplayChanged(settings.getReferencesDisplay());
    dialogFieldsDisplayChanged(settings.getDialogsFieldsDisplay());

    settings.addSettingsChangedListener(userSettingsChanged);
  }

  protected Node setReferenceBases(ReferenceBase referenceBase, ReferenceBase persistedParentReferenceBase) {
    Node nodeToFocus = txtfldReferenceSubDivisionTitle;

    if(referenceBase instanceof ReferenceSubDivision) {
      this.referenceSubDivision = (ReferenceSubDivision)referenceBase;
      this.reference = referenceSubDivision.getReference();
      if(reference == null && persistedParentReferenceBase instanceof Reference) // it should never be the case that referenceSubDivision.getReference() == null and
      // persistedParentReferenceBase is not a Reference
        this.reference = (Reference)persistedParentReferenceBase;

      if(reference == null)
        setToNewReference();
      else
        this.seriesTitle = reference.getSeries();
      if(this.seriesTitle == null)
        setToNewSeries();
    }
    else {
      setToNewReferenceSubDivision();

      if(referenceBase instanceof Reference) {
        this.reference = (Reference) referenceBase;
        this.seriesTitle = reference.getSeries();
        if(this.seriesTitle == null)
          setToNewSeries();
        nodeToFocus = txtfldReferenceTitle;
        paneReferenceSubDivisionFields.setVisible(false);
      }
      else {
        setToNewReference();
        nodeToFocus = txtfldReferenceTitle;

        if(referenceBase instanceof SeriesTitle) {
          this.seriesTitle = (SeriesTitle) referenceBase;
          paneReferenceSubDivisionFields.setVisible(false);
          paneReferenceFields.setVisible(false);
          nodeToFocus = txtfldSeriesTitleTitle;
        }
        else // a new Reference should be created
          setToNewSeries();
      }
    }
    return nodeToFocus;
  }

  protected Node setPersistedParentReferenceBase(ReferenceBase persistedParentReferenceBase, Node nodeToFocus) {
    this.persistedParentReferenceBase = persistedParentReferenceBase;

    if(persistedParentReferenceBase instanceof SeriesTitle) {
      this.seriesTitle = (SeriesTitle)persistedParentReferenceBase;
      nodeToFocus = txtfldReferenceTitle;
    }
    else if(persistedParentReferenceBase instanceof Reference) {
      this.reference = (Reference)persistedParentReferenceBase;
      this.seriesTitle = reference.getSeries();
      if(seriesTitle == null)
        setToNewSeries();
      nodeToFocus = txtfldReferenceSubDivisionTitle;
    }
    return nodeToFocus;
  }


  protected EntityListener seriesTitleListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.ReferenceBaseTitleColumnName))
        seriesTitleTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
        seriesTitleSubTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
        seriesTitleAbstractChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleTableOfContentsColumnName))
        seriesTitleTableOfContentsChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseOnlineAddressColumnName))
        seriesTitleOnlineAddressChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseNotesColumnName))
        seriesTitleNotesChanged((String) previousValue, (String) newValue);
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


  protected void seriesTitleTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleTitle.setText(newValue);
    fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleTitle);
  }

  protected void seriesTitleSubTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleSubTitle.setText(newValue);
    fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleSubTitle);
  }

  protected void seriesTitleAbstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledSeriesTitleAbstract.setHtml(newValue);
    fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleAbstract);
  }

  protected void seriesTitleTableOfContentsChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledSeriesTitleTableOfContents.setHtml(newValue);
    fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleTableOfContents);
  }

  protected void seriesTitleOnlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleOnlineAddress.setText(newValue);
    fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleOnlineAddress);
  }

  protected void seriesTitleNotesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledSeriesTitleNotes.setHtml(newValue);
    fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleNotes);
  }


  protected EntityListener referenceListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.ReferenceBaseTitleColumnName))
        referenceTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
        referenceSubTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceIssueOrPublishingDateColumnName))
        referenceIssueOrPublishingDateChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseOnlineAddressColumnName))
        referenceOnlineAddressChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
        referenceAbstractChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceTableOfContentsColumnName))
        referenceTableOfContentsChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseNotesColumnName))
        referenceNotesChanged((String) previousValue, (String) newValue);
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

  protected void referenceTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceTitle.setText(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceTitle);
  }

  protected void referenceSubTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceSubTitle.setText(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceSubTitle);
  }

  protected void referenceIssueOrPublishingDateChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceIssueOrPublishingDate.setText(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceIssueOrPublishingDate);
  }

  protected void referenceOnlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceOnlineAddress.setText(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceOnlineAddress);
  }

  protected void referenceAbstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledReferenceAbstract.setHtml(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceAbstract);
  }

  protected void referenceTableOfContentsChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledReferenceTableOfContents.setHtml(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceTableOfContents);
  }

  protected void referenceNotesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledReferenceNotes.setHtml(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceNotes);
  }



  protected EntityListener referenceSubDivisionListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.ReferenceBaseTitleColumnName))
        referenceSubDivisionTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
        referenceSubDivisionSubTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
        referenceSubDivisionAbstractChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseOnlineAddressColumnName))
        referenceSubDivisionOnlineAddressChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseNotesColumnName))
        referenceSubDivisionNotesChanged((String) previousValue, (String) newValue);
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


  protected void referenceSubDivisionTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceSubDivisionTitle.setText(newValue);
    fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionTitle);
  }

  protected void referenceSubDivisionSubTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceSubDivisionSubTitle.setText(newValue);
    fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle);
  }

  protected void referenceSubDivisionAbstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledReferenceSubDivisionAbstract.setHtml(newValue);
    fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract);
  }

  protected void referenceSubDivisionOnlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceSubDivisionOnlineAddress.setText(newValue);
    fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress);
  }

  protected void referenceSubDivisionNotesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledReferenceSubDivisionNotes.setHtml(newValue);
    fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionNotes);
  }


  protected LanguageChangedListener languageChangedListener = new LanguageChangedListener() {
    @Override
    public void languageChanged(ApplicationLanguage newLanguage) {
      dtpckReferencePublishingDate.setChronology(Chronology.ofLocale(Localization.getLanguageLocale()));
      setReferenceIssueTextFieldToDateSelectedInDatePicker();
    }
  };

  protected SettingsChangedListener userSettingsChanged = new SettingsChangedListener() {
    @Override
    public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
      if(setting == Setting.UserDeviceDialogFieldsDisplay && newValue instanceof DialogsFieldsDisplay) {
        dialogFieldsDisplayChanged((DialogsFieldsDisplay)newValue);
      }
      else if(setting == Setting.UserDeviceReferencesDisplay && newValue instanceof ReferencesDisplay) {
        referencesDisplayChanged((ReferencesDisplay)newValue);
      }
    }
  };

}
