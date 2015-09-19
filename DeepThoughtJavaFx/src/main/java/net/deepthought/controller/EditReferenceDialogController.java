package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.Constants;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.controls.html.CollapsibleHtmlEditor;
import net.deepthought.controls.html.HtmlEditorListener;
import net.deepthought.controls.person.ReferencePersonsControl;
import net.deepthought.controls.person.ReferenceSubDivisionPersonsControl;
import net.deepthought.controls.person.SeriesTitlePersonsControl;
import net.deepthought.controls.reference.ISelectedReferenceHolder;
import net.deepthought.controls.reference.SearchAndSelectReferenceControl;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.search.specific.ReferenceBaseType;
import net.deepthought.util.Alerts;
import net.deepthought.util.DateConvertUtils;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
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

  protected int publishingDateFormat = DateFormat.MEDIUM;


  @FXML
  protected VBox pnContent;

  @FXML
  protected Pane paneSeriesTitle;

  @FXML
  protected Pane paneReference;

  @FXML
  protected Pane paneSeriesTitleHeader;
  @FXML
  protected ToggleButton btnShowHideSeriesTitlePane;
  @FXML
  protected ToggleButton btnShowHideSearchSeriesTitle;
  protected SearchAndSelectReferenceControl searchAndSelectSeriesTitleControl = null;

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

  @FXML
  protected TitledPane ttldpnSeriesTitleAbstract;
  @FXML
  protected TextArea txtarSeriesTitleAbstract;

  protected CollapsibleHtmlEditor htmledSeriesTitleTableOfContents;


  protected SeriesTitlePersonsControl seriesTitlePersonsControl;

  @FXML
  protected Pane paneSeriesTitleOnlineAddress;
  @FXML
  protected TextField txtfldSeriesTitleOnlineAddress;

  @FXML
  protected TitledPane ttldpnSeriesTitleNotes;
  @FXML
  protected TextArea txtarSeriesTitleNotes;


  @FXML
  protected TitledPane ttldpnSeriesTitleFiles;
  @FXML
  protected FlowPane flpnSeriesTitleFilesPreview;
  @FXML
  protected TreeTableView<FileLink> trtblvwSeriesTitleFiles;
  @FXML
  protected TreeTableColumn<FileLink, String> clmnSeriesTitleFile;


  @FXML
  protected Button btnChooseReferenceFieldsToShow;

  @FXML
  protected ToggleButton btnShowHideReferencePane;
  @FXML
  protected ToggleButton btnShowHideSearchReference;
  protected SearchAndSelectReferenceControl searchAndSelectReferenceControl = null;

  @FXML
  protected Pane paneReferenceTitle;
  @FXML
  protected TextField txtfldReferenceTitle;

  @FXML
  protected Pane paneReferenceSubTitle;
  @FXML
  protected TextField txtfldReferenceSubTitle;

  @FXML
  protected TitledPane ttldpnReferenceAbstract;
  @FXML
  protected TextArea txtarReferenceAbstract;

  protected CollapsibleHtmlEditor htmledReferenceTableOfContents;


  protected ReferencePersonsControl referencePersonsControl;

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

  @FXML
  protected TitledPane ttldpnReferenceNotes;
  @FXML
  protected TextArea txtarReferenceNotes;


  @FXML
  protected TitledPane ttldpnReferenceFiles;
  @FXML
  protected FlowPane flpnReferenceFilesPreview;
  @FXML
  protected TreeTableView<FileLink> trtblvwReferenceFiles;
  @FXML
  protected TreeTableColumn<FileLink, String> clmnReferenceFile;


  @FXML
  protected Pane paneReferenceSubDivision;
  @FXML
  protected ToggleButton btnShowHideReferenceSubDivisionPane;
  @FXML
  protected Button btnChooseReferenceSubDivisionFieldsToShow;

  @FXML
  protected Pane paneReferenceSubDivisionTitle;
  @FXML
  protected TextField txtfldReferenceSubDivisionTitle;

  @FXML
  protected Pane paneReferenceSubDivisionSubTitle;
  @FXML
  protected TextField txtfldReferenceSubDivisionSubTitle;

  @FXML
  protected TitledPane ttldpnReferenceSubDivisionAbstract;
  @FXML
  protected TextArea txtarReferenceSubDivisionAbstract;


  protected ReferenceSubDivisionPersonsControl referenceSubDivisionPersonsControl;

  @FXML
  protected Pane paneReferenceSubDivisionOnlineAddress;
  @FXML
  protected TextField txtfldReferenceSubDivisionOnlineAddress;

  @FXML
  protected TitledPane ttldpnReferenceSubDivisionNotes;
  @FXML
  protected TextArea txtarReferenceSubDivisionNotes;

  @FXML
  protected TitledPane ttldpnReferenceSubDivisionFiles;
  @FXML
  protected FlowPane flpnReferenceSubDivisionFilesPreview;
  @FXML
  protected TreeTableView<FileLink> trtblvwReferenceSubDivisionFiles;
  @FXML
  protected TreeTableColumn<FileLink, String> clmnReferenceSubDivisionFile;


  @Override
  protected Node getContent() {
    return pnContent;
  }

  @Override
  protected String getHelpTextResourceKeyPrefix() {
    return "context.help.series.title.";
  }

  @Override
  protected String getEntityType() {
    return "reference";
  }

  @Override
  protected ContextMenu createHiddenFieldsContextMenu() {
    return null;
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
  }

  protected void setupControls() {
    super.setupControls();
    setButtonChooseFieldsToShowVisiblity(false);

    setupSeriesTitleControls();
    setupReferenceControls();
    setupReferenceSubDivisionControls();
  }


  protected void setupSeriesTitleControls() {
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitle);
    paneSeriesTitle.visibleProperty().bind(btnShowHideSeriesTitlePane.selectedProperty());

    btnShowHideSeriesTitlePane.selectedProperty().addListener((observable, oldValue, newValue) -> setButtonShowHideSeriesTitlePaneText());
    setButtonShowHideSeriesTitlePaneText();

    btnShowHideSearchSeriesTitle.setGraphic(new ImageView(Constants.SearchIconPath));

    searchAndSelectSeriesTitleControl = new SearchAndSelectReferenceControl(ReferenceBaseType.SeriesTitle, new ISelectedReferenceHolder() {
      @Override
      public ReferenceBase getSelectedReferenceBase() {
        return seriesTitle;
      }

      @Override
      public void selectedReferenceBaseChanged(ReferenceBase newReferenceBase) {
        setSeriesTitle((SeriesTitle) newReferenceBase);
      }

      @Override
      public void addFieldChangedEvent(EventHandler<FieldChangedEvent> fieldChangedEvent) {

      }
    });

    searchAndSelectSeriesTitleControl.setVisible(false);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(searchAndSelectSeriesTitleControl);
    searchAndSelectSeriesTitleControl.visibleProperty().bind(btnShowHideSearchSeriesTitle.selectedProperty());
    searchAndSelectSeriesTitleControl.setMinHeight(190);
    searchAndSelectSeriesTitleControl.setMaxHeight(190);
    pnContent.getChildren().add(1, searchAndSelectSeriesTitleControl);

    txtfldSeriesTitleTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleTitle);
      if (editedReferenceBase instanceof SeriesTitle)
        updateWindowTitle();
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleSubTitle);
    txtfldSeriesTitleSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleAbstract);
    txtarSeriesTitleAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleAbstract));

    htmledSeriesTitleTableOfContents = new CollapsibleHtmlEditor("table.of.contents", seriesTitleTableOfContentsListener);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledSeriesTitleTableOfContents);
    paneSeriesTitle.getChildren().add(4, htmledSeriesTitleTableOfContents);

    seriesTitlePersonsControl = new SeriesTitlePersonsControl();
    seriesTitlePersonsControl.setExpanded(true);
    seriesTitlePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitlePersons));
    seriesTitlePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitlePersons));
    VBox.setMargin(seriesTitlePersonsControl, new Insets(6, 0, 0, 0));
    paneSeriesTitle.getChildren().add(6, seriesTitlePersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(seriesTitlePersonsControl);
    seriesTitlePersonsControl.setVisible(false);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleOnlineAddress);
    txtfldSeriesTitleOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleOnlineAddress));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleNotes);
    txtarSeriesTitleNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleNotes));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleFiles);
  }

  protected void setSeriesTitle(SeriesTitle newSeriesTitle) {
    if(hasSeriesTitleBeenEdited()) {
      if(Alerts.askUserIfEditedSeriesTitleShouldBeSaved(seriesTitle) == true) {
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
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReference);
    paneReference.visibleProperty().bind(btnShowHideReferencePane.selectedProperty());

    btnShowHideReferencePane.selectedProperty().addListener((observable, oldValue, newValue) -> setButtonShowHideReferencePaneText());
    setButtonShowHideReferencePaneText();

    btnShowHideSearchReference.setGraphic(new ImageView(Constants.SearchIconPath));

    searchAndSelectReferenceControl = new SearchAndSelectReferenceControl(ReferenceBaseType.Reference, new ISelectedReferenceHolder() {
      @Override
      public ReferenceBase getSelectedReferenceBase() {
        return reference;
      }

      @Override
      public void selectedReferenceBaseChanged(ReferenceBase newReferenceBase) {
        setReference((Reference) newReferenceBase);
      }

      @Override
      public void addFieldChangedEvent(EventHandler<FieldChangedEvent> fieldChangedEvent) {

      }
    });

    searchAndSelectReferenceControl.setVisible(false);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(searchAndSelectReferenceControl);
    searchAndSelectReferenceControl.visibleProperty().bind(btnShowHideSearchReference.selectedProperty());
    searchAndSelectReferenceControl.setMinHeight(190);
    searchAndSelectReferenceControl.setMaxHeight(190);
    pnContent.getChildren().add(5, searchAndSelectReferenceControl);

    txtfldReferenceTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceTitle);
      if (editedReferenceBase instanceof Reference)
        updateWindowTitle();
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubTitle);
    txtfldReferenceSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnReferenceAbstract);
    txtarReferenceAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceAbstract));

    htmledReferenceTableOfContents = new CollapsibleHtmlEditor("table.of.contents", referenceTableOfContentsListener);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledReferenceTableOfContents);
    paneReference.getChildren().add(5, htmledReferenceTableOfContents);

    referencePersonsControl = new ReferencePersonsControl();
    referencePersonsControl.setExpanded(false);
    referencePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferencePersons));
    referencePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferencePersons));
    VBox.setMargin(referencePersonsControl, new Insets(6, 0, 0, 0));
    paneReference.getChildren().add(6, referencePersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferencePublishingDate);
    txtfldReferenceIssueOrPublishingDate.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceIssueOrPublishingDate);
    });

//    dtpckReferencePublishingDate.setConverter(localeDateStringConverter);
    dtpckReferencePublishingDate.valueProperty().addListener((observable, oldValue, newValue) -> {
      txtfldReferenceIssueOrPublishingDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(DateConvertUtils.asUtilDate(newValue)));
        });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceOnlineAddress);
    txtfldReferenceOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceOnlineAddress));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnReferenceNotes);
    txtarReferenceNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceNotes));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnReferenceFiles);
//    clmnFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
//      @Override
//      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
//        return new FileTreeTableCell(reference);
//      }
//    });
  }

  protected void setReference(Reference newReference) {
    if(hasReferenceBeenEdited()) {
      if(Alerts.askUserIfEditedReferenceShouldBeSaved(reference) == true) {
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
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivision);
    paneReferenceSubDivision.visibleProperty().bind(btnShowHideReferenceSubDivisionPane.selectedProperty());

    btnShowHideReferenceSubDivisionPane.selectedProperty().addListener((observable, oldValue, newValue) -> setButtonShowHideReferenceSubDivisionPaneText());
    setButtonShowHideReferenceSubDivisionPaneText();

    txtfldReferenceSubDivisionTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionTitle);
      if (editedReferenceBase instanceof ReferenceSubDivision)
        updateWindowTitle();
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivisionSubTitle);
    txtfldReferenceSubDivisionSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnReferenceSubDivisionAbstract);
    txtarReferenceSubDivisionAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract));

    referenceSubDivisionPersonsControl = new ReferenceSubDivisionPersonsControl();
    referenceSubDivisionPersonsControl.setExpanded(true);
    referenceSubDivisionPersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionPersons));
    referenceSubDivisionPersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionPersons));
    VBox.setMargin(referenceSubDivisionPersonsControl, new Insets(6, 0, 0, 0));
    paneReferenceSubDivision.getChildren().add(4, referenceSubDivisionPersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(referenceSubDivisionPersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivisionOnlineAddress);
    txtfldReferenceSubDivisionOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnReferenceSubDivisionNotes);
    txtarReferenceSubDivisionNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedReferenceSubDivisionChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionNotes));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnReferenceSubDivisionFiles);

//    clmnReferenceSubDivisionFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
//      @Override
//      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
//        return new FileTreeTableCell(referenceSubDivision);
//      }
//    });
  }


  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
    btnChooseSeriesTitleFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneSeriesTitleSubTitle.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnSeriesTitleAbstract.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    htmledSeriesTitleTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    seriesTitlePersonsControl.setVisible(seriesTitle.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneSeriesTitleOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnSeriesTitleNotes.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnSeriesTitleFiles.setVisible(seriesTitle.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);


    btnChooseReferenceFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneReferenceSubTitle.setVisible(StringUtils.isNotNullOrEmpty(reference.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnReferenceAbstract.setVisible(StringUtils.isNotNullOrEmpty(reference.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    htmledReferenceTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(reference.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneReferenceOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(reference.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnReferenceNotes.setVisible(StringUtils.isNotNullOrEmpty(reference.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnReferenceFiles.setVisible(reference.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);


    btnChooseReferenceSubDivisionFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneReferenceSubDivisionSubTitle.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnReferenceSubDivisionAbstract.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    referenceSubDivisionPersonsControl.setVisible(referenceSubDivision.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneReferenceSubDivisionOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnReferenceSubDivisionNotes.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnReferenceSubDivisionFiles.setVisible(referenceSubDivision.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
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

    super.closeDialog();
  }

  protected void cleanUpControls() {
    // i don't get it: referencePersonsControl never gets removed from Memory, all others in approximately 50 % of all cases
    // TODO: find Memory leaks
    searchAndSelectSeriesTitleControl.cleanUpControl();
    paneSeriesTitle.getChildren().remove(searchAndSelectSeriesTitleControl);
    searchAndSelectSeriesTitleControl = null;
    htmledSeriesTitleTableOfContents.cleanUpControl();
    seriesTitlePersonsControl.cleanUpControl();
    paneSeriesTitle.getChildren().remove(seriesTitlePersonsControl);
    seriesTitlePersonsControl = null;

    searchAndSelectReferenceControl.cleanUpControl();
    paneReference.getChildren().remove(searchAndSelectReferenceControl);
    searchAndSelectReferenceControl = null;
    htmledReferenceTableOfContents.cleanUpControl();
    referencePersonsControl.cleanUpControl();
    paneReference.getChildren().remove(referencePersonsControl);
    referencePersonsControl = null;

    referenceSubDivisionPersonsControl.cleanUpControl();
    paneReferenceSubDivision.getChildren().remove(referenceSubDivisionPersonsControl);
    referenceSubDivisionPersonsControl = null;
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
      seriesTitle.setAbstract(txtarSeriesTitleAbstract.getText());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleAbstract);
    }
    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleTableOfContents) || htmledSeriesTitleTableOfContents.getHtml().equals(seriesTitle.getTableOfContents()) == false) {
      if(FXUtils.HtmlEditorDefaultText.equals(htmledSeriesTitleTableOfContents.getHtml())) {
        if(StringUtils.isNotNullOrEmpty(seriesTitle.getTableOfContents()))
          seriesTitle.setTableOfContents("");
      }
      else
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
      seriesTitle.setNotes(txtarSeriesTitleNotes.getText());
      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleNotes);
    }

    if(fieldsWithUnsavedSeriesTitleChanges.contains(FieldWithUnsavedChanges.SeriesTitleFiles)) {

      fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleFiles);
    }
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

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceAbstract)) {
      reference.setAbstract(txtarReferenceAbstract.getText());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceAbstract);
    }
    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceTableOfContents) || htmledReferenceTableOfContents.getHtml().equals(reference.getTableOfContents()) == false) {
      if(FXUtils.HtmlEditorDefaultText.equals(htmledReferenceTableOfContents.getHtml())) {
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

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceIssueOrPublishingDate)) {
      reference.setIssueOrPublishingDate(txtfldReferenceIssueOrPublishingDate.getText());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceIssueOrPublishingDate);
    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceOnlineAddress)) {
      reference.setOnlineAddress(txtfldReferenceOnlineAddress.getText());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceOnlineAddress);
    }

//    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanged.ReferenceSubDivisions)) {
//
//      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanged.ReferenceSubDivisions);
//    }

    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanges.ReferenceNotes)) {
      reference.setNotes(txtarReferenceNotes.getText());
      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceNotes);
    }

//    if(fieldsWithUnsavedReferenceChanges.contains(FieldWithUnsavedChanged.ReferenceBaseFiles)) {
//
//      fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanged.ReferenceBaseFiles);
//    }
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
      referenceSubDivision.setAbstract(txtarReferenceSubDivisionAbstract.getText());
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
      referenceSubDivision.setNotes(txtarReferenceSubDivisionNotes.getText());
      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionNotes);
    }

    if(fieldsWithUnsavedReferenceSubDivisionChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionFiles)) {

      fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionFiles);
    }
  }


  protected void setSeriesTitleValues(final SeriesTitle seriesTitle) {
    if(seriesTitle != null) {
      txtfldSeriesTitleTitle.setText(seriesTitle.getTitle());
      txtfldSeriesTitleSubTitle.setText(seriesTitle.getSubTitle());

      txtarSeriesTitleAbstract.setText(seriesTitle.getAbstract());
      htmledSeriesTitleTableOfContents.setHtml(seriesTitle.getTableOfContents());

      txtfldSeriesTitleOnlineAddress.setText(seriesTitle.getOnlineAddress());

      txtarSeriesTitleNotes.setText(seriesTitle.getNotes());

      seriesTitlePersonsControl.setSeries(seriesTitle);
    }
    else {
      txtfldSeriesTitleTitle.setText("");
      txtfldSeriesTitleSubTitle.setText(seriesTitle.getSubTitle());

      txtarSeriesTitleAbstract.setText(seriesTitle.getAbstract());
      htmledSeriesTitleTableOfContents.setHtml(seriesTitle.getTableOfContents());

      txtfldSeriesTitleOnlineAddress.setText(seriesTitle.getOnlineAddress());

      txtarSeriesTitleNotes.setText(seriesTitle.getNotes());

      seriesTitlePersonsControl.setSeries(seriesTitle);
    }

//    trtblvwSeriesTitleFiles.setRoot(new FileRootTreeItem(seriesTitle));

    fieldsWithUnsavedSeriesTitleChanges.clear();

    seriesTitle.addEntityListener(seriesTitleListener);
    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }

  protected void setReferenceSubDivisionValues(final ReferenceSubDivision subDivision) {
    txtfldReferenceSubDivisionTitle.setText(subDivision.getTitle());
    txtfldReferenceSubDivisionSubTitle.setText(subDivision.getSubTitle());

    txtarReferenceSubDivisionAbstract.setText(subDivision.getAbstract());

    referenceSubDivisionPersonsControl.setSubDivision(subDivision);

    txtfldReferenceSubDivisionOnlineAddress.setText(subDivision.getOnlineAddress());

    txtarReferenceSubDivisionNotes.setText(subDivision.getNotes());

//    trtblvwReferenceSubDivisionFiles.setRoot(new FileRootTreeItem(subDivision));

    fieldsWithUnsavedReferenceSubDivisionChanges.clear();

    subDivision.addEntityListener(referenceSubDivisionListener);
    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }



  @FXML
  public void setButtonShowHideSeriesTitlePaneText() {
    if(btnShowHideSeriesTitlePane.isSelected())
      btnShowHideSeriesTitlePane.setText(CollapsiblePane.ExpandedText);
    else
      btnShowHideSeriesTitlePane.setText(CollapsiblePane.CollapsedText);
  }

  public void handleButtonChooseSeriesTitleFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneSeriesTitleSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleSubTitle, "subtitle");

    if(ttldpnSeriesTitleAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleAbstract, "abstract");
    if(htmledSeriesTitleTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledSeriesTitleTableOfContents, "table.of.contents");

    if(seriesTitlePersonsControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, seriesTitlePersonsControl, "persons");

    if(paneSeriesTitleOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleOnlineAddress, "online.address");
    }

    if(ttldpnSeriesTitleNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleNotes, "notes");
    if(ttldpnSeriesTitleFiles.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleFiles, "files");

    hiddenFieldsMenu.show(btnChooseSeriesTitleFieldsToShow, Side.BOTTOM, 0, 0);
  }


  @FXML
  public void setButtonShowHideReferencePaneText() {
    if(btnShowHideReferencePane.isSelected())
      btnShowHideReferencePane.setText(CollapsiblePane.ExpandedText);
    else
      btnShowHideReferencePane.setText(CollapsiblePane.CollapsedText);
  }

  public void handleButtonChooseReferenceFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneReferenceSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceSubTitle, "subtitle");

    if(ttldpnReferenceAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceAbstract, "abstract");
    if(htmledReferenceTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledReferenceTableOfContents, "table.of.contents");

    if(paneReferenceOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceOnlineAddress, "online.address");
    }

    if(ttldpnReferenceNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceNotes, "notes");
    if(ttldpnReferenceFiles.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceFiles, "files");

    hiddenFieldsMenu.show(btnChooseReferenceFieldsToShow, Side.BOTTOM, 0, 0);
  }


  @FXML
  public void setButtonShowHideReferenceSubDivisionPaneText() {
    if(btnShowHideReferenceSubDivisionPane.isSelected())
      btnShowHideReferenceSubDivisionPane.setText(CollapsiblePane.ExpandedText);
    else
      btnShowHideReferenceSubDivisionPane.setText(CollapsiblePane.CollapsedText);
  }

  public void handleButtonChooseReferenceSubDivisionFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneReferenceSubDivisionSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceSubDivisionSubTitle, "subtitle");

    if(ttldpnReferenceSubDivisionAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceSubDivisionAbstract, "abstract");

    if(referenceSubDivisionPersonsControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, referenceSubDivisionPersonsControl, "persons");

    if(paneReferenceSubDivisionOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceSubDivisionOnlineAddress, "online.address");
    }

    if(ttldpnReferenceSubDivisionNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceSubDivisionNotes, "notes");
    if(ttldpnReferenceSubDivisionFiles.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceSubDivisionFiles, "files");

    hiddenFieldsMenu.show(btnChooseReferenceSubDivisionFieldsToShow, Side.BOTTOM, 0, 0);
  }


  public void setWindowStageAndReferenceBase(Stage windowStage, ReferenceBase referenceBase, ReferenceBase persistedParentReferenceBase) {
    // TODO: if referenceBase != null disallow editing of Entities below referenceBase's Hierarchy (e.g. referenceBase instanceof Reference -> don't allow ReferenceSubDivision editing
    editedReferenceBase = referenceBase;
    super.setWindowStage(windowStage, referenceBase);

    Node nodeToFocus = setReferenceBases(referenceBase, persistedParentReferenceBase);

    if(persistedParentReferenceBase != null) {
      nodeToFocus = setPersistedParentReferenceBase(persistedParentReferenceBase, nodeToFocus);
    }

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

    if(seriesTitle.isPersisted())
      btnShowHideSeriesTitlePane.setSelected(true);
    if(reference.isPersisted())
      btnShowHideReferencePane.setSelected(true);
    if(referenceSubDivision.isPersisted())
      btnShowHideReferenceSubDivisionPane.setSelected(true);

    FXUtils.focusNode(nodeToFocus);
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
        paneReferenceSubDivision.setVisible(false);
      }
      else {
        setToNewReference();
        nodeToFocus = txtfldReferenceTitle;

        if(referenceBase instanceof SeriesTitle) {
          this.seriesTitle = (SeriesTitle) referenceBase;
          paneReferenceSubDivision.setVisible(false);
          paneReference.setVisible(false);
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

  protected void setReferenceValues(final Reference reference) {
    txtfldReferenceTitle.setText(reference.getTitle());
    txtfldReferenceSubTitle.setText(reference.getSubTitle());

    txtarReferenceAbstract.setText(reference.getAbstract());
    htmledReferenceTableOfContents.setHtml(reference.getTableOfContents());

    referencePersonsControl.setReference(reference);

    txtfldReferenceIssueOrPublishingDate.setText(reference.getIssueOrPublishingDate());
    dtpckReferencePublishingDate.setValue(DateConvertUtils.asLocalDate(reference.getPublishingDate()));

    txtfldReferenceOnlineAddress.setText(reference.getOnlineAddress());

    txtarReferenceNotes.setText(reference.getNotes());

//    trtblvwFiles.setRoot(new FileRootTreeItem(reference));

    fieldsWithUnsavedReferenceChanges.clear();

    reference.addEntityListener(referenceListener);
    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
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
    txtarSeriesTitleAbstract.setText(newValue);
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
    txtarSeriesTitleNotes.setText(newValue);
    fieldsWithUnsavedSeriesTitleChanges.remove(FieldWithUnsavedChanges.SeriesTitleNotes);
  }


  protected EntityListener referenceListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.ReferenceBaseTitleColumnName))
        referenceTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
        referenceSubTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
        referenceAbstractChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceTableOfContentsColumnName))
        referenceTableOfContentsChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseOnlineAddressColumnName))
        referenceOnlineAddressChanged((String) previousValue, (String) newValue);
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

  protected void referenceAbstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarReferenceAbstract.setText(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceAbstract);
  }

  protected void referenceTableOfContentsChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledReferenceTableOfContents.setHtml(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceTableOfContents);
  }

  protected void referenceOnlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceOnlineAddress.setText(newValue);
    fieldsWithUnsavedReferenceChanges.remove(FieldWithUnsavedChanges.ReferenceOnlineAddress);
  }

  protected void referenceNotesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarReferenceNotes.setText(newValue);
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
    txtarReferenceSubDivisionAbstract.setText(newValue);
    fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract);
  }

  protected void referenceSubDivisionOnlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceSubDivisionOnlineAddress.setText(newValue);
    fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress);
  }

  protected void referenceSubDivisionNotesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarReferenceSubDivisionNotes.setText(newValue);
    fieldsWithUnsavedReferenceSubDivisionChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionNotes);
  }


  protected HtmlEditorListener seriesTitleTableOfContentsListener = new HtmlEditorListener() {
    @Override
    public void htmlCodeUpdated(String newHtmlCode) {
      fieldsWithUnsavedSeriesTitleChanges.add(FieldWithUnsavedChanges.SeriesTitleTableOfContents);
    }
  };

  protected HtmlEditorListener referenceTableOfContentsListener = new HtmlEditorListener() {
    @Override
    public void htmlCodeUpdated(String newHtmlCode) {
      fieldsWithUnsavedReferenceChanges.add(FieldWithUnsavedChanges.ReferenceTableOfContents);
    }
  };

}
