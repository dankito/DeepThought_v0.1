package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.BaseEntityListCell;
import net.deepthought.controls.Constants;
import net.deepthought.controls.ContextHelpControl;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.NewOrEditButton;
import net.deepthought.controls.event.NewOrEditButtonMenuActionEvent;
import net.deepthought.controls.person.ReferencePersonsControl;
import net.deepthought.controls.person.ReferenceSubDivisionPersonsControl;
import net.deepthought.controls.person.SeriesTitlePersonsControl;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Publisher;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.enums.ReferenceCategory;
import net.deepthought.data.model.enums.ReferenceIndicationUnit;
import net.deepthought.data.model.enums.SeriesTitleCategory;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.DateConvertUtils;
import net.deepthought.util.Empty;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Created by ganymed on 21/12/14.
 */
public class EditReferenceDialogController extends ChildWindowsController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(EditReferenceDialogController.class);


  protected SeriesTitle seriesTitle = null;

  protected Reference reference = null;

  protected ReferenceSubDivision referenceSubDivision = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();


  @FXML
  protected BorderPane dialogPane;

  @FXML
  protected Button btnApplyChanges;

  @FXML
  protected Pane paneSeriesTitle;

  @FXML
  protected Pane contentPane;

  @FXML
  protected Pane paneSeriesTitleHeader;

  @FXML
  protected Pane paneSeriesTitleTitle;
  @FXML
  protected TextField txtfldSeriesTitleTitle;
  @FXML
  protected Pane paneSeriesTitleCategory;
  @FXML
  protected ComboBox<SeriesTitleCategory> cmbxSeriesTitleCategory;
  @FXML
  protected NewOrEditButton btnNewOrEditSeriesTitleCategory;
  @FXML
  protected Button btnChooseSeriesTitleFieldsToShow;

  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;

  protected ContextHelpControl contextHelpControl;

  @FXML
  protected Pane paneSeriesTitleSubTitle;
  @FXML
  protected TextField txtfldSeriesTitleSubTitle;

  @FXML
  protected Pane paneSeriesTitleTitleSupplement;
  @FXML
  protected TextField txtfldSeriesTitleTitleSupplement;

  @FXML
  protected TitledPane ttldpnSeriesTitleAbstract;
  @FXML
  protected TextArea txtarSeriesTitleAbstract;

  @FXML
  protected TitledPane ttldpnSeriesTitleTableOfContents;
  @FXML
  protected HTMLEditor htmledSeriesTitleTableOfContents;


  protected SeriesTitlePersonsControl seriesTitlePersonsControl;

  @FXML
  protected Pane paneSeriesTitleFirstAndLastDayOfPublication;
  @FXML
  protected DatePicker dtpckSeriesTitleFirstDayOfPublication;
  @FXML
  protected DatePicker dtpckSeriesTitleLastDayOfPublication;
  @FXML
  protected Pane paneSeriesTitlePublisher;
  @FXML
  protected ComboBox<Publisher> cmbxSeriesTitlePublisher;
  @FXML
  protected NewOrEditButton btnSeriesTitleNewOrEditPublisher;

  @FXML
  protected Pane paneSeriesTitleAbbreviation;
  @FXML
  protected TextField txtfldSeriesTitleStandardAbbreviation;
  @FXML
  protected TextField txtfldSeriesTitleUserAbbreviation1;
  @FXML
  protected TextField txtfldSeriesTitleUserAbbreviation2;

  @FXML
  protected Pane paneSeriesTitleOnlineAddress;
  @FXML
  protected TextField txtfldSeriesTitleOnlineAddress;
  @FXML
  protected DatePicker dtpckSeriesTitleLastAccess;

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
  protected Pane pnSeriesTitle;
  @FXML
  protected ComboBox<SeriesTitle> cmbxSeriesTitle;
  @FXML
  protected NewOrEditButton btnNewOrEditSeriesTitle;
  @FXML
  protected Button btnChooseFieldsToShow;

  @FXML
  protected Pane paneTitle;
  @FXML
  protected TextField txtfldTitle;
  @FXML
  protected Pane paneReferenceCategory;
  @FXML
  protected ComboBox<ReferenceCategory> cmbxReferenceCategory;
  @FXML
  protected NewOrEditButton btnNewOrEditReferenceCategory;

  @FXML
  protected Pane paneSubTitle;
  @FXML
  protected TextField txtfldSubTitle;

  @FXML
  protected Pane paneTitleSupplement;
  @FXML
  protected TextField txtfldTitleSupplement;

  @FXML
  protected TitledPane ttldpnAbstract;
  @FXML
  protected TextArea txtarAbstract;

  @FXML
  protected TitledPane ttldpnTableOfContents;
  @FXML
  protected HTMLEditor htmledTableOfContents;


  protected ReferencePersonsControl referencePersonsControl;

  @FXML
  protected Pane paneEditionAndVolume;
  @FXML
  protected TextField txtfldEdition;
  @FXML
  protected TextField txtfldVolume;
  @FXML
  protected Pane panePublishingDateAndPlaceOfPublication;
  @FXML
  protected DatePicker dtpckPublishingDate;
  @FXML
  protected TextField txtfldPlaceOfPublication;
  @FXML
  protected Pane panePublisher;
  @FXML
  protected ComboBox<Publisher> cmbxPublisher;
  @FXML
  protected NewOrEditButton btnNewOrEditPublisher;

  @FXML
  protected TextField txtfldIsbnOrIssn;
  @FXML
  protected Pane pnReferenceLength;
  @FXML
  protected TextField txtfldReferenceLength;
  @FXML
  protected ComboBox<ReferenceIndicationUnit> cmbxReferenceLengthUnit;
  @FXML
  protected NewOrEditButton btnNewOrEditReferenceLengthUnit;

  @FXML
  protected Pane paneIssue;
  @FXML
  protected TextField txtfldIssue;
  @FXML
  protected TextField txtfldYear;
  @FXML
  protected TextField txtfldDoi;

  @FXML
  protected TextField txtfldPrice;
  @FXML
  protected Pane panePriceAndLanguage;
  @FXML
  protected ComboBox<Language> cmbxLanguage;
  @FXML
  protected NewOrEditButton btnNewOrEditLanguage;
  @FXML
  protected Pane paneOnlineAddress;
  @FXML
  protected TextField txtfldOnlineAddress;
  @FXML
  protected DatePicker dtpckLastAccess;

  @FXML
  protected TitledPane ttldpnNotes;
  @FXML
  protected TextArea txtarNotes;


  @FXML
  protected TitledPane ttldpnFiles;
  @FXML
  protected FlowPane flpnFilesPreview;
  @FXML
  protected TreeTableView<FileLink> trtblvwFiles;
  @FXML
  protected TreeTableColumn<FileLink, String> clmnFile;


  @FXML
  protected Pane paneReferenceSubDivision;
  @FXML
  protected Button btnChooseFieldsToShowForReferenceSubDivision;

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
  protected DatePicker dtpckReferenceSubDivisionLastAccess;

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
  public void initialize(URL location, ResourceBundle resources) {
    btnApplyChanges.managedProperty().bind(btnApplyChanges.visibleProperty());

    fieldsWithUnsavedChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        btnApplyChanges.setDisable(fieldsWithUnsavedChanges.size() == 0);
      }
    });

    Application.getSettings().addSettingsChangedListener(new SettingsChangedListener() {
      @Override
      public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
        if (setting == Setting.UserDeviceDialogFieldsDisplay)
          dialogFieldsDisplayChanged((DialogsFieldsDisplay) newValue);
      }
    });

    Application.getDeepThought().addEntityListener(deepThoughtListener);
    // TODO: what to do when DeepThought changes -> close dialog
  }

  protected void setupControls() {
    setupSeriesTitleControls();
    setupReferenceControls();
    setupReferenceSubDivisionControls();
  }


  protected void setupSeriesTitleControls() {
    txtfldSeriesTitleTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleTitle);
      updateWindowTitle(newValue);
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleCategory);
    paneSeriesTitleCategory.setVisible(false);

    resetComboBoxSeriesTitleCategoryItems();
    cmbxSeriesTitleCategory.valueProperty().addListener(cmbxSeriesTitleCategoryValueChangeListener);

    cmbxSeriesTitleCategory.setConverter(new StringConverter<SeriesTitleCategory>() {
      @Override
      public String toString(SeriesTitleCategory seriesTitle) {
        return seriesTitle.getTextRepresentation();
      }

      @Override
      public SeriesTitleCategory fromString(String string) {
        return null;
      }
    });
    cmbxSeriesTitleCategory.setCellFactory(new Callback<ListView<SeriesTitleCategory>, ListCell<SeriesTitleCategory>>() {
      @Override
      public ListCell<SeriesTitleCategory> call(ListView<SeriesTitleCategory> param) {
        return new BaseEntityListCell<SeriesTitleCategory>();
      }
    });

    btnNewOrEditSeriesTitleCategory = new NewOrEditButton();
    btnNewOrEditSeriesTitleCategory.setOnAction(event -> handleButtonNewOrEditSeriesTitleCategoryAction(event));
    btnNewOrEditSeriesTitleCategory.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewSeriesTitleCategoryAction(event));
    btnNewOrEditSeriesTitleCategory.setDisable(true); // TODO: unset as soon as editing is possible
    paneSeriesTitleCategory.getChildren().add(btnNewOrEditSeriesTitleCategory);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleSubTitle);
    txtfldSeriesTitleSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleTitleSupplement);
    txtfldSeriesTitleTitleSupplement.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleTitleSupplement));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleAbstract);
    txtarSeriesTitleAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleAbstract));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleTableOfContents);
    FXUtils.addHtmlEditorTextChangedListener(htmledSeriesTitleTableOfContents, event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleTableOfContents));

    seriesTitlePersonsControl = new SeriesTitlePersonsControl();
    seriesTitlePersonsControl.setExpanded(true);
    seriesTitlePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitlePersons));
    seriesTitlePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitlePersons));
    paneSeriesTitle.getChildren().add(6, seriesTitlePersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(seriesTitlePersonsControl);
    seriesTitlePersonsControl.setVisible(false);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleFirstAndLastDayOfPublication);
    dtpckSeriesTitleFirstDayOfPublication.setConverter(localeDateStringConverter);
    dtpckSeriesTitleFirstDayOfPublication.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication));
    dtpckSeriesTitleLastDayOfPublication.setConverter(localeDateStringConverter);
    dtpckSeriesTitleLastDayOfPublication.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitlePublisher);
    resetComboBoxPublisherItems();
    cmbxSeriesTitlePublisher.valueProperty().addListener(cmbxPublisherValueChangeListener);

    cmbxSeriesTitlePublisher.setConverter(new StringConverter<Publisher>() {
      @Override
      public String toString(Publisher publisher) {
        return publisher.getTextRepresentation();
      }

      @Override
      public Publisher fromString(String string) {
        return null;
      }
    });
    cmbxSeriesTitlePublisher.setCellFactory(new Callback<ListView<Publisher>, ListCell<Publisher>>() {
      @Override
      public ListCell<Publisher> call(ListView<Publisher> param) {
        return new BaseEntityListCell<Publisher>();
      }
    });

    btnSeriesTitleNewOrEditPublisher = new NewOrEditButton();
    btnSeriesTitleNewOrEditPublisher.setOnAction(event -> handleButtonSeriesTitleNewOrEditPublisherAction(event));
    btnSeriesTitleNewOrEditPublisher.setOnNewMenuItemEventActionHandler(event -> handleMenuItemSeriesTitleNewPublisherAction(event));
    paneSeriesTitlePublisher.getChildren().add(btnSeriesTitleNewOrEditPublisher);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleAbbreviation);
    txtfldSeriesTitleStandardAbbreviation.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleStandardAbbreviation));
    txtfldSeriesTitleUserAbbreviation1.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation1));
    txtfldSeriesTitleUserAbbreviation2.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation2));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleOnlineAddress);
    txtfldSeriesTitleOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleOnlineAddress));
    dtpckSeriesTitleLastAccess.setConverter(localeDateStringConverter);
    dtpckSeriesTitleLastAccess.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleLastAccess));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleNotes);
    txtarSeriesTitleNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleNotes));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleFiles);

//    clmnSeriesTitleFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
//      @Override
//      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
//        return new FileTreeTableCell(seriesTitle);
//      }
//    });

    contextHelpControl = new ContextHelpControl("context.help.series.title.");
    dialogPane.setRight(contextHelpControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(contextHelpControl);
    contextHelpControl.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());

    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowHideContextHelp.setGraphic(new ImageView(Constants.ContextHelpIconPath));
  }

  protected void setupReferenceControls() {
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(pnSeriesTitle);
    resetComboBoxSeriesTitleItems();
//    cmbxSeriesTitle.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanged.ReferenceSeriesTitle));
    cmbxSeriesTitle.valueProperty().addListener(cmbxSeriesTitleValueChangeListener);

    cmbxSeriesTitle.setConverter(new StringConverter<SeriesTitle>() {
      @Override
      public String toString(SeriesTitle seriesTitle) {
        return seriesTitle.getTextRepresentation();
      }

      @Override
      public SeriesTitle fromString(String string) {
        // TODO
        return null;
      }
    });
    cmbxSeriesTitle.setCellFactory(new Callback<ListView<SeriesTitle>, ListCell<SeriesTitle>>() {
      @Override
      public ListCell<SeriesTitle> call(ListView<SeriesTitle> param) {
        return new BaseEntityListCell<SeriesTitle>();
      }
    });

    btnNewOrEditSeriesTitle = new NewOrEditButton();
    btnNewOrEditSeriesTitle.setOnAction(event -> handleButtonNewOrEditSeriesTitleAction(event));
    btnNewOrEditSeriesTitle.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewSeriesTitleAction(event));
    pnSeriesTitle.getChildren().add(2, btnNewOrEditSeriesTitle);


    txtfldTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceTitle);
      updateWindowTitle(newValue);
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceCategory);
    paneReferenceCategory.setVisible(false);

    resetComboBoxReferenceCategoryItems();
//    cmbxReferenceCategory.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanged.ReferenceReferenceCategory));
    cmbxReferenceCategory.valueProperty().addListener(cmbxReferenceCategoryValueChangeListener);

    cmbxReferenceCategory.setConverter(new StringConverter<ReferenceCategory>() {
      @Override
      public String toString(ReferenceCategory referenceCategory) {
        return referenceCategory.getTextRepresentation();
      }

      @Override
      public ReferenceCategory fromString(String string) {
        return null;
      }
    });
    cmbxReferenceCategory.setCellFactory(new Callback<ListView<ReferenceCategory>, ListCell<ReferenceCategory>>() {
      @Override
      public ListCell<ReferenceCategory> call(ListView<ReferenceCategory> param) {
        return new BaseEntityListCell<ReferenceCategory>();
      }
    });

    btnNewOrEditReferenceCategory = new NewOrEditButton();
    btnNewOrEditReferenceCategory.setOnAction(event -> handleButtonNewOrEditReferenceCategoryAction(event));
    btnNewOrEditReferenceCategory.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewReferenceCategoryAction(event));
    btnNewOrEditReferenceCategory.setDisable(true); // TODO: unset as soon as editing is possible
    paneReferenceCategory.getChildren().add(btnNewOrEditReferenceCategory);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSubTitle);
    txtfldSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneTitleSupplement);
    txtfldTitleSupplement.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceTitleSupplement));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnAbstract);
    txtarAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceAbstract));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnTableOfContents);
    FXUtils.addHtmlEditorTextChangedListener(htmledTableOfContents, event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceTableOfContents));

    referencePersonsControl = new ReferencePersonsControl();
    referencePersonsControl.setExpanded(true);
    referencePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePersons));
    referencePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePersons));
    contentPane.getChildren().add(9, referencePersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneEditionAndVolume);
    txtfldEdition.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceEdition));
    txtfldVolume.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceVolume));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(panePublishingDateAndPlaceOfPublication);
    dtpckPublishingDate.setConverter(localeDateStringConverter);
    dtpckPublishingDate.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePublishingDate));
    txtfldPlaceOfPublication.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePlaceOfPublication));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(panePublisher);
    resetComboBoxPublisherItems();
//    cmbxPublisher.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanged.ReferencePublisher));
    cmbxPublisher.valueProperty().addListener(cmbxPublisherValueChangeListener);

    cmbxPublisher.setConverter(new StringConverter<Publisher>() {
      @Override
      public String toString(Publisher publisher) {
        return publisher.getTextRepresentation();
      }

      @Override
      public Publisher fromString(String string) {
        return null;
      }
    });
    cmbxPublisher.setCellFactory(new Callback<ListView<Publisher>, ListCell<Publisher>>() {
      @Override
      public ListCell<Publisher> call(ListView<Publisher> param) {
        return new BaseEntityListCell<Publisher>();
      }
    });

    btnNewOrEditPublisher = new NewOrEditButton();
//    btnNewOrEditPublisher.setPrefHeight(35);
//    btnNewOrEditPublisher.setPrefWidth(115);
    btnNewOrEditPublisher.setOnAction(event -> handleButtonNewOrEditPublisherAction(event));
    btnNewOrEditPublisher.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewPublisherAction(event));
    panePublisher.getChildren().add(btnNewOrEditPublisher);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(pnReferenceLength);
    txtfldIsbnOrIssn.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceIsbnOrIssn));
    txtfldReferenceLength.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceReferenceLength));

    resetComboBoxReferenceLengthUnitItems();
//    cmbxReferenceLengthUnit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanged.ReferenceReferenceLengthUnit));
    cmbxReferenceLengthUnit.valueProperty().addListener(cmbxReferenceLengthUnitValueChangeListener);
    cmbxReferenceLengthUnit.setConverter(new StringConverter<ReferenceIndicationUnit>() {
      @Override
      public String toString(ReferenceIndicationUnit indicationUnit) {
        return indicationUnit.getTextRepresentation();
      }

      @Override
      public ReferenceIndicationUnit fromString(String string) {
        return null;
      }
    });
    cmbxReferenceLengthUnit.setCellFactory(new Callback<ListView<ReferenceIndicationUnit>, ListCell<ReferenceIndicationUnit>>() {
      @Override
      public ListCell<ReferenceIndicationUnit> call(ListView<ReferenceIndicationUnit> param) {
        return new BaseEntityListCell<ReferenceIndicationUnit>();
      }
    });

    btnNewOrEditReferenceLengthUnit = new NewOrEditButton();
    btnNewOrEditReferenceLengthUnit.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
    btnNewOrEditReferenceLengthUnit.setShowNewMenuItem(true);
    btnNewOrEditReferenceLengthUnit.setOnAction(event -> handleButtonNewOrEditReferenceIndicationUnitAction(event));
    btnNewOrEditReferenceLengthUnit.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewReferenceIndicationUnitAction(event));
    btnNewOrEditReferenceLengthUnit.setDisable(true); // TODO: unset as soon as editing is possible
    pnReferenceLength.getChildren().add(btnNewOrEditReferenceLengthUnit);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneIssue);
    txtfldIssue.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceIssue));
    txtfldYear.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceYear));
    txtfldDoi.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceDoi));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(panePriceAndLanguage);
    txtfldPrice.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePrice));
    resetComboBoxLanguageItems();
//    cmbxLanguage.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanged.ReferenceLanguage));
    cmbxLanguage.valueProperty().addListener(cmbxLanguageValueChangeListener);

    cmbxLanguage.setConverter(new StringConverter<Language>() {
      @Override
      public String toString(Language language) {
        return language.getTextRepresentation();
      }

      @Override
      public Language fromString(String string) {
        return null;
      }
    });
    cmbxLanguage.setCellFactory(new Callback<ListView<Language>, ListCell<Language>>() {
      @Override
      public ListCell<Language> call(ListView<Language> param) {
        return new BaseEntityListCell<Language>();
      }
    });

    btnNewOrEditLanguage = new NewOrEditButton();
    btnNewOrEditLanguage.setOnAction(event -> handleButtonNewOrEditLanguageAction(event));
    btnNewOrEditLanguage.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewLanguageAction(event));
    btnNewOrEditLanguage.setDisable(true); // TODO: unset as soon as editing is possible
    panePriceAndLanguage.getChildren().add(btnNewOrEditLanguage);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneOnlineAddress);
    txtfldOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceOnlineAddress));
    dtpckLastAccess.setConverter(localeDateStringConverter);
    dtpckLastAccess.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceLastAccess));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnNotes);
    txtarNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceNotes));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnFiles);
//    clmnFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
//      @Override
//      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
//        return new FileTreeTableCell(reference);
//      }
//    });
  }

  protected void setupReferenceSubDivisionControls() {
    txtfldReferenceSubDivisionTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionTitle);
      updateWindowTitle(newValue);
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivisionSubTitle);
    txtfldReferenceSubDivisionSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnReferenceSubDivisionAbstract);
    txtarReferenceSubDivisionAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract));

    referenceSubDivisionPersonsControl = new ReferenceSubDivisionPersonsControl();
    referenceSubDivisionPersonsControl.setExpanded(true);
    referenceSubDivisionPersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionPersons));
    referenceSubDivisionPersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionPersons));
    paneReferenceSubDivision.getChildren().add(4, referenceSubDivisionPersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(referenceSubDivisionPersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivisionOnlineAddress);
    txtfldReferenceSubDivisionOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress));
    dtpckReferenceSubDivisionLastAccess.setConverter(localeDateStringConverter);
    dtpckReferenceSubDivisionLastAccess.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionLastAccess));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnReferenceSubDivisionNotes);
    txtarReferenceSubDivisionNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionNotes));

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

    paneSeriesTitleCategory.setVisible(seriesTitle.getCategory() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneSeriesTitleSubTitle.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneSeriesTitleTitleSupplement.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getTitleSupplement()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnSeriesTitleAbstract.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnSeriesTitleTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    seriesTitlePersonsControl.setVisible(seriesTitle.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneSeriesTitleFirstAndLastDayOfPublication.setVisible(seriesTitle.getFirstDayOfPublication() != null || seriesTitle.getLastDayOfPublication() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneSeriesTitlePublisher.setVisible(seriesTitle.getPublisher() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneSeriesTitleAbbreviation.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getStandardAbbreviation()) || StringUtils.isNotNullOrEmpty(seriesTitle.getUserAbbreviation1()) ||
        StringUtils.isNotNullOrEmpty(seriesTitle.getUserAbbreviation2()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneSeriesTitleOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getOnlineAddress()) || seriesTitle.getLastAccessDate() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnSeriesTitleNotes.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnSeriesTitleFiles.setVisible(seriesTitle.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);


    btnChooseFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneReferenceCategory.setVisible(reference.getCategory() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneSubTitle.setVisible(StringUtils.isNotNullOrEmpty(reference.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneTitleSupplement.setVisible(StringUtils.isNotNullOrEmpty(reference.getTitleSupplement()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnAbstract.setVisible(StringUtils.isNotNullOrEmpty(reference.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(reference.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneEditionAndVolume.setVisible(StringUtils.isNotNullOrEmpty(reference.getEdition()) || StringUtils.isNotNullOrEmpty(reference.getVolume()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
//    panePublishingDateAndPlaceOfPublication.setVisible(reference.getPublishingDate() != null || StringUtils.isNotNullOrEmpty(reference.getPlaceOfPublication()) ||
//        dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    panePublisher.setVisible(reference.getPublisher() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    pnReferenceLength.setVisible(StringUtils.isNotNullOrEmpty(reference.getIsbnOrIssn()) || StringUtils.isNotNullOrEmpty(reference.getLength()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
//    paneIssue.setVisible(StringUtils.isNotNullOrEmpty(reference.getIssue()) || reference.getYear() != null || StringUtils.isNotNullOrEmpty(reference.getDoi()) ||
//        dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    panePriceAndLanguage.setVisible(StringUtils.isNotNullOrEmpty(reference.getPrice()) || reference.getLanguage() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(reference.getOnlineAddress()) || reference.getLastAccessDate() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnNotes.setVisible(StringUtils.isNotNullOrEmpty(reference.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnFiles.setVisible(reference.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);


    btnChooseFieldsToShowForReferenceSubDivision.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneReferenceSubDivisionSubTitle.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnReferenceSubDivisionAbstract.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    referenceSubDivisionPersonsControl.setVisible(referenceSubDivision.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneReferenceSubDivisionOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getOnlineAddress()) || referenceSubDivision.getLastAccessDate() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnReferenceSubDivisionNotes.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnReferenceSubDivisionFiles.setVisible(referenceSubDivision.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
  }



  protected ChangeListener<SeriesTitleCategory> cmbxSeriesTitleCategoryValueChangeListener = new ChangeListener<SeriesTitleCategory>() {
    @Override
    public void changed(ObservableValue<? extends SeriesTitleCategory> observable, SeriesTitleCategory oldValue, SeriesTitleCategory newValue) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleSeriesTitleCategory);

      if(newValue == null || newValue == Empty.SeriesCategory) {
        btnNewOrEditSeriesTitleCategory.setButtonFunction(NewOrEditButton.ButtonFunction.New);
        btnNewOrEditSeriesTitleCategory.setShowNewMenuItem(false);
      }
      else {
        btnNewOrEditSeriesTitleCategory.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
        btnNewOrEditSeriesTitleCategory.setShowNewMenuItem(true);
      }
    }
  };

  protected ChangeListener<Publisher> cmbxSeriesTitlePublisherValueChangeListener = new ChangeListener<Publisher>() {
    @Override
    public void changed(ObservableValue<? extends Publisher> observable, Publisher oldValue, Publisher newValue) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitlePublisher);

      if(newValue == null || newValue == Empty.Publisher) {
        btnSeriesTitleNewOrEditPublisher.setButtonFunction(NewOrEditButton.ButtonFunction.New);
        btnSeriesTitleNewOrEditPublisher.setShowNewMenuItem(false);
      }
      else {
        btnSeriesTitleNewOrEditPublisher.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
        btnSeriesTitleNewOrEditPublisher.setShowNewMenuItem(true);
      }
    }
  };

  protected void resetComboBoxSeriesTitleCategoryItems() {
    cmbxSeriesTitleCategory.getItems().clear();
    cmbxSeriesTitleCategory.getItems().add(Empty.SeriesCategory);
    cmbxSeriesTitleCategory.getItems().addAll(Application.getDeepThought().getSeriesTitleCategories());
  }

  protected void resetComboBoxSeriesTitlePublisherItems() {
    cmbxSeriesTitlePublisher.getItems().clear();
    cmbxSeriesTitlePublisher.getItems().add(Empty.Publisher);
    cmbxSeriesTitlePublisher.getItems().addAll(new TreeSet<Publisher>(Application.getDeepThought().getPublishers()));
  }


  protected ChangeListener<SeriesTitle> cmbxSeriesTitleValueChangeListener = new ChangeListener<SeriesTitle>() {
    @Override
    public void changed(ObservableValue<? extends SeriesTitle> observable, SeriesTitle oldValue, SeriesTitle newValue) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSeriesTitle);

      if(newValue == null || Empty.Series.equals(newValue)) {
        btnNewOrEditSeriesTitle.setButtonFunction(NewOrEditButton.ButtonFunction.New);
        btnNewOrEditSeriesTitle.setShowNewMenuItem(false);
      }
      else {
        btnNewOrEditSeriesTitle.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
        btnNewOrEditSeriesTitle.setShowNewMenuItem(true);
      }
    }
  };

  protected ChangeListener<ReferenceCategory> cmbxReferenceCategoryValueChangeListener = new ChangeListener<ReferenceCategory>() {
    @Override
    public void changed(ObservableValue<? extends ReferenceCategory> observable, ReferenceCategory oldValue, ReferenceCategory newValue) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceReferenceCategory);

      if(newValue == null || Empty.ReferenceCategory.equals(newValue)) {
        btnNewOrEditReferenceCategory.setButtonFunction(NewOrEditButton.ButtonFunction.New);
        btnNewOrEditReferenceCategory.setShowNewMenuItem(false);
      }
      else {
        btnNewOrEditReferenceCategory.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
        btnNewOrEditReferenceCategory.setShowNewMenuItem(true);
      }
    }
  };

  protected ChangeListener<Publisher> cmbxPublisherValueChangeListener = new ChangeListener<Publisher>() {
    @Override
    public void changed(ObservableValue<? extends Publisher> observable, Publisher oldValue, Publisher newValue) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePublisher);

      if(newValue == null || Empty.Publisher.equals(newValue)) {
        btnNewOrEditPublisher.setButtonFunction(NewOrEditButton.ButtonFunction.New);
        btnNewOrEditPublisher.setShowNewMenuItem(false);
      }
      else {
        btnNewOrEditPublisher.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
        btnNewOrEditPublisher.setShowNewMenuItem(true);
      }
    }
  };

  protected ChangeListener<ReferenceIndicationUnit> cmbxReferenceLengthUnitValueChangeListener = new ChangeListener<ReferenceIndicationUnit>() {
    @Override
    public void changed(ObservableValue<? extends ReferenceIndicationUnit> observable, ReferenceIndicationUnit oldValue, ReferenceIndicationUnit newValue) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceReferenceLengthUnit);
    }
  };

  protected ChangeListener<Language> cmbxLanguageValueChangeListener = new ChangeListener<Language>() {
    @Override
    public void changed(ObservableValue<? extends Language> observable, Language oldValue, Language newValue) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceLanguage);

      if(newValue == null || Empty.Language.equals(newValue)) {
        btnNewOrEditLanguage.setButtonFunction(NewOrEditButton.ButtonFunction.New);
        btnNewOrEditLanguage.setShowNewMenuItem(false);
      }
      else {
        btnNewOrEditLanguage.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
        btnNewOrEditLanguage.setShowNewMenuItem(true);
      }
    }
  };

  protected void resetComboBoxSeriesTitleItems() {
    cmbxSeriesTitle.getItems().clear();
    cmbxSeriesTitle.getItems().add(Empty.Series);
    cmbxSeriesTitle.getItems().addAll(Application.getDeepThought().getSeriesTitles());
  }

  protected void resetComboBoxReferenceCategoryItems() {
    cmbxReferenceCategory.getItems().clear();
    cmbxReferenceCategory.getItems().add(Empty.ReferenceCategory);
    cmbxReferenceCategory.getItems().addAll(Application.getDeepThought().getReferenceCategories());
  }

  protected void resetComboBoxPublisherItems() {
    cmbxPublisher.getItems().clear();
    cmbxPublisher.getItems().add(Empty.Publisher);
    cmbxPublisher.getItems().addAll(new TreeSet<Publisher>(Application.getDeepThought().getPublishers()));
  }

  protected void resetComboBoxReferenceLengthUnitItems() {
    cmbxReferenceLengthUnit.getItems().clear();
    cmbxReferenceLengthUnit.getItems().addAll(Application.getDeepThought().getReferenceIndicationUnits());
  }

  protected void resetComboBoxLanguageItems() {
    cmbxLanguage.getItems().clear();
    cmbxLanguage.getItems().add(Empty.Language);
    cmbxLanguage.getItems().addAll(Application.getDeepThought().getLanguages());
  }


  @FXML
  public void handleButtonApplyAction(ActionEvent actionEvent) {
    saveEditedFieldsOnReference();
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Cancel);
    closeDialog();
  }

  @FXML
  public void handleButtonOkAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Ok);

    // TODO: save async while closing the dialog? Would make Dialog closing faster
    if(reference.isPersisted() == false) // a new Reference
      Application.getDeepThought().addReference(reference);

    if(seriesTitle.isPersisted() == false) { // a new SeriesTitle
      if(StringUtils.isNotNullOrEmpty(txtfldSeriesTitleTitle.getText())) {
        Application.getDeepThought().addSeriesTitle(seriesTitle);
        reference.setSeries(seriesTitle);
      }
    }

    if(referenceSubDivision.isPersisted() == false) { // a new ReferenceSubDivision
      if(StringUtils.isNotNullOrEmpty(txtfldReferenceSubDivisionTitle.getText())) {
        reference.addSubDivision(referenceSubDivision);
      }
    }

    saveEditedFieldsOnSeriesTitle();
    saveEditedFieldsOnReference();
    saveEditedFieldsOnReferenceSubDivision();

    closeDialog();
  }

  @Override
  protected void closeDialog() {
    seriesTitle.removeEntityListener(seriesTitleListener);
    reference.removeEntityListener(referenceListener);
    referenceSubDivision.removeEntityListener(referenceSubDivisionListener);
    Application.getDeepThought().removeEntityListener(deepThoughtListener);

    super.closeDialog();
  }

  protected void saveEditedFieldsOnSeriesTitle() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleTitle)) {
      seriesTitle.setTitle(txtfldSeriesTitleTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleSeriesTitleCategory)) {
      if(Empty.SeriesCategory.equals(cmbxSeriesTitleCategory.getValue()))
        seriesTitle.setCategory(null);
      else
        seriesTitle.setCategory(cmbxSeriesTitleCategory.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSeriesTitleCategory);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleSubTitle)) {
      seriesTitle.setSubTitle(txtfldSeriesTitleSubTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSubTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleTitleSupplement)) {
      seriesTitle.setTitleSupplement(txtfldSeriesTitleTitleSupplement.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitleSupplement);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleAbstract)) {
      seriesTitle.setAbstract(txtarSeriesTitleAbstract.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleAbstract);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleTableOfContents) || htmledSeriesTitleTableOfContents.getHtmlText().equals(seriesTitle.getTableOfContents()) == false) {
      if(FXUtils.HtmlEditorDefaultText.equals(htmledSeriesTitleTableOfContents.getHtmlText())) {
        if(StringUtils.isNotNullOrEmpty(seriesTitle.getTableOfContents()))
          seriesTitle.setTableOfContents("");
      }
      else
        seriesTitle.setTableOfContents(htmledSeriesTitleTableOfContents.getHtmlText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleTableOfContents);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitlePersons)) {
      Map<PersonRole, Set<Person>> removedPersons = new HashMap<>(seriesTitlePersonsControl.getRemovedPersons());
      for(PersonRole removedPersonsInRole : removedPersons.keySet()) {
        for(Person removedPerson : removedPersons.get(removedPersonsInRole))
          seriesTitle.removePerson(removedPerson, removedPersonsInRole);
      }

      Map<PersonRole, Set<Person>> addedPersons = new HashMap<>(seriesTitlePersonsControl.getAddedPersons());
      for(PersonRole addedPersonsInRole : addedPersons.keySet()) {
        for(Person addedPerson : addedPersons.get(addedPersonsInRole))
          seriesTitle.addPerson(addedPerson, addedPersonsInRole);
      }

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitlePersons);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication)) {
      seriesTitle.setFirstDayOfPublication(DateConvertUtils.asUtilDate(dtpckSeriesTitleFirstDayOfPublication.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication)) {
      seriesTitle.setLastDayOfPublication(DateConvertUtils.asUtilDate(dtpckSeriesTitleLastDayOfPublication.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitlePublisher)) {
      if(Empty.Publisher.equals(cmbxSeriesTitlePublisher.getValue()))
        seriesTitle.setPublisher(null);
      else
        seriesTitle.setPublisher(cmbxSeriesTitlePublisher.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitlePublisher);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleStandardAbbreviation)) {
      seriesTitle.setStandardAbbreviation(txtfldSeriesTitleStandardAbbreviation.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleStandardAbbreviation);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation1)) {
      seriesTitle.setUserAbbreviation1(txtfldSeriesTitleUserAbbreviation1.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation1);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation2)) {
      seriesTitle.setUserAbbreviation2(txtfldSeriesTitleUserAbbreviation2.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation2);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleOnlineAddress)) {
      seriesTitle.setOnlineAddress(txtfldSeriesTitleOnlineAddress.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleOnlineAddress);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleLastAccess)) {
      seriesTitle.setLastAccessDate(DateConvertUtils.asUtilDate(dtpckSeriesTitleLastAccess.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleLastAccess);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleSerialParts)) {

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSerialParts);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleNotes)) {
      seriesTitle.setNotes(txtarSeriesTitleNotes.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleNotes);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleFiles)) {

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleFiles);
    }
  }

  protected void saveEditedFieldsOnReference() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSeriesTitle)) {
      if(Empty.Series.equals(cmbxSeriesTitle.getValue()))
        reference.setSeries(null);
      else
        reference.setSeries(cmbxSeriesTitle.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSeriesTitle);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceTitle)) {
      reference.setTitle(txtfldTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceReferenceCategory)) {
      if(Empty.ReferenceCategory.equals(cmbxReferenceCategory.getValue()))
        reference.setCategory(null);
      else
        reference.setCategory(cmbxReferenceCategory.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceReferenceCategory);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubTitle)) {
      reference.setSubTitle(txtfldSubTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceTitleSupplement)) {
      reference.setTitleSupplement(txtfldTitleSupplement.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitleSupplement);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceAbstract)) {
      reference.setAbstract(txtarAbstract.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceAbstract);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceTableOfContents) || htmledTableOfContents.getHtmlText().equals(reference.getTableOfContents()) == false) {
      if(FXUtils.HtmlEditorDefaultText.equals(htmledTableOfContents.getHtmlText())) {
        if(StringUtils.isNotNullOrEmpty(reference.getTableOfContents()))
          reference.setTableOfContents("");
      }
      else
        reference.setTableOfContents(htmledTableOfContents.getHtmlText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTableOfContents);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferencePersons)) {
      Map<PersonRole, Set<Person>> removedPersons = new HashMap<>(referencePersonsControl.getRemovedPersons());
      for(PersonRole removedPersonsInRole : removedPersons.keySet()) {
        for(Person removedPerson : removedPersons.get(removedPersonsInRole))
          reference.removePerson(removedPerson, removedPersonsInRole);
      }

      Map<PersonRole, Set<Person>> addedPersons = new HashMap<>(referencePersonsControl.getAddedPersons());
      for(PersonRole addedPersonsInRole : addedPersons.keySet()) {
        for(Person addedPerson : addedPersons.get(addedPersonsInRole))
          reference.addPerson(addedPerson, addedPersonsInRole);
      }

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePersons);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceEdition)) {
      reference.setEdition(txtfldEdition.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceEdition);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceVolume)) {
      reference.setVolume(txtfldVolume.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceVolume);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferencePublishingDate)) {
      reference.setPublishingDate(DateConvertUtils.asUtilDate(dtpckPublishingDate.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePublishingDate);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferencePlaceOfPublication)) {
      reference.setPlaceOfPublication(txtfldPlaceOfPublication.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePlaceOfPublication);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferencePublisher)) {
      if(Empty.Publisher.equals(cmbxPublisher.getValue()))
        reference.setPublisher(null);
      else
        reference.setPublisher(cmbxPublisher.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePublisher);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceIsbnOrIssn)) {
      reference.setIsbnOrIssn(txtfldIsbnOrIssn.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceIsbnOrIssn);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceReferenceLength)) {
      reference.setLength(txtfldReferenceLength.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceReferenceLength);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceReferenceLengthUnit)) {
      reference.setLengthUnit(cmbxReferenceLengthUnit.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceReferenceLengthUnit);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceIssue)) {
      reference.setIssue(txtfldIssue.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceIssue);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceYear)) {
      if(StringUtils.isNullOrEmpty(txtfldYear.getText()))
        reference.setYear(null);
      else
        reference.setYear(Integer.parseInt(txtfldYear.getText()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceYear);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceDoi)) {
      reference.setDoi(txtfldDoi.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceDoi);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferencePrice)) {
      reference.setPrice(txtfldPrice.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePrice);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceLanguage)) {
      if(Empty.Language.equals(cmbxLanguage.getValue()))
        reference.setLanguage(null);
      else
        reference.setLanguage(cmbxLanguage.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceLanguage);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceOnlineAddress)) {
      reference.setOnlineAddress(txtfldOnlineAddress.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceOnlineAddress);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceLastAccess)) {
      reference.setLastAccessDate(DateConvertUtils.asUtilDate(dtpckLastAccess.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceLastAccess);
    }

//    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanged.ReferenceSubDivisions)) {
//
//      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanged.ReferenceSubDivisions);
//    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceNotes)) {
      reference.setNotes(txtarNotes.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceNotes);
    }

//    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanged.ReferenceBaseFiles)) {
//
//      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanged.ReferenceBaseFiles);
//    }
  }

  protected void saveEditedFieldsOnReferenceSubDivision() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionTitle)) {
      referenceSubDivision.setTitle(txtfldSubTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle)) {
      referenceSubDivision.setSubTitle(txtfldReferenceSubDivisionSubTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract)) {
      referenceSubDivision.setAbstract(txtarReferenceSubDivisionAbstract.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionPersons)) {
      Map<PersonRole, Set<Person>> removedPersons = new HashMap<>(referenceSubDivisionPersonsControl.getRemovedPersons());
      for(PersonRole removedPersonsInRole : removedPersons.keySet()) {
        for(Person removedPerson : removedPersons.get(removedPersonsInRole))
          referenceSubDivision.removePerson(removedPerson, removedPersonsInRole);
      }

      Map<PersonRole, Set<Person>> addedPersons = new HashMap<>(referenceSubDivisionPersonsControl.getAddedPersons());
      for(PersonRole addedPersonsInRole : addedPersons.keySet()) {
        for(Person addedPerson : addedPersons.get(addedPersonsInRole))
          referenceSubDivision.addPerson(addedPerson, addedPersonsInRole);
      }

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionPersons);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress)) {
      referenceSubDivision.setOnlineAddress(txtfldReferenceSubDivisionOnlineAddress.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionLastAccess)) {
      referenceSubDivision.setLastAccessDate(DateConvertUtils.asUtilDate(dtpckReferenceSubDivisionLastAccess.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionLastAccess);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionNotes)) {
      referenceSubDivision.setNotes(txtarReferenceSubDivisionNotes.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionNotes);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionFiles)) {

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionFiles);
    }
  }

  @Override
  public void setWindowStage(Stage windowStage) {
    super.setWindowStage(windowStage);

    windowStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        askIfStageShouldBeClosed(event);
      }
    });

    windowStage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

      }
    });
  }

  protected void askIfStageShouldBeClosed(WindowEvent event) {
    if(hasUnsavedChanges()) {
      Action response = Dialogs.create()
          .owner(windowStage)
          .title("Entry contains unsaved changes")
          .message("Entry contains unsaved changes. Do you like to save changes now?")
          .actions(Dialog.ACTION_CANCEL, Dialog.ACTION_NO, Dialog.ACTION_YES)
          .showConfirm();

      if(response.equals(Dialog.ACTION_CANCEL))
        event.consume(); // consume event so that stage doesn't get closed
      else if(response.equals(Dialog.ACTION_YES)) {
        saveEditedFieldsOnReference();
        closeDialog();
      }
      else
        closeDialog();
    }
  }


  protected void handleMenuItemNewSeriesTitleCategoryAction(NewOrEditButtonMenuActionEvent event) {
    createNewSeriesTitleCategory();
  }

  protected void createNewSeriesTitleCategory() {
    final SeriesTitleCategory newSeriesTitleCategory = new SeriesTitleCategory();

//    net.deepthought.controller.Dialogs.showEditSeriesTitleCategoryDialog(newSeriesTitleCategory, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//        if(controller.getDialogResult() == DialogResult.Ok)
//          seriesTitle.setCategory(newSeriesTitleCategory);
//      }
//    });
  }

  @FXML
  public void handleButtonSeriesTitleNewOrEditPublisherAction(ActionEvent event) {

  }

  protected void handleMenuItemSeriesTitleNewPublisherAction(NewOrEditButtonMenuActionEvent event) {

  }

  @FXML
  public void handleButtonSeriesTitleAddFileAction(ActionEvent event) {
//    final FileLink newFile = new FileLink();
//
//    net.deepthought.controller.Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//        if (controller.getDialogResult() == DialogResult.Ok) {
//          seriesTitle.addFile(newFile);
//        }
//      }
//    });
  }


  public SeriesTitle getSeriesTitle() {
    return seriesTitle;
  }

  protected void setSeriesTitleValues(final SeriesTitle seriesTitle) {
    txtfldSeriesTitleTitle.setText(seriesTitle.getTitle());
    cmbxSeriesTitleCategory.setValue(seriesTitle.getCategory());
    txtfldSeriesTitleSubTitle.setText(seriesTitle.getSubTitle());
    txtfldSeriesTitleTitleSupplement.setText(seriesTitle.getTitleSupplement());

    txtarSeriesTitleAbstract.setText(seriesTitle.getAbstract());
    htmledSeriesTitleTableOfContents.setHtmlText(seriesTitle.getTableOfContents());

    seriesTitlePersonsControl.setSeries(seriesTitle);

    dtpckSeriesTitleFirstDayOfPublication.setValue(DateConvertUtils.asLocalDate(seriesTitle.getFirstDayOfPublication()));
    dtpckSeriesTitleLastDayOfPublication.setValue(DateConvertUtils.asLocalDate(seriesTitle.getLastDayOfPublication()));

    txtfldSeriesTitleStandardAbbreviation.setText(seriesTitle.getStandardAbbreviation());
    txtfldSeriesTitleUserAbbreviation1.setText(seriesTitle.getUserAbbreviation1());
    txtfldSeriesTitleUserAbbreviation2.setText(seriesTitle.getUserAbbreviation2());

    txtfldSeriesTitleOnlineAddress.setText(seriesTitle.getOnlineAddress());
    dtpckSeriesTitleLastAccess.setValue(DateConvertUtils.asLocalDate(seriesTitle.getLastAccessDate()));

    txtarSeriesTitleNotes.setText(seriesTitle.getNotes());

//    trtblvwSeriesTitleFiles.setRoot(new FileRootTreeItem(seriesTitle));

    fieldsWithUnsavedChanges.clear();

    seriesTitle.addEntityListener(seriesTitleListener);
    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }

  protected void setReferenceSubDivisionValues(final ReferenceSubDivision subDivision) {
    txtfldReferenceSubDivisionTitle.setText(subDivision.getTitle());
    txtfldReferenceSubDivisionSubTitle.setText(subDivision.getSubTitle());

    txtarReferenceSubDivisionAbstract.setText(subDivision.getAbstract());

    referenceSubDivisionPersonsControl.setSubDivision(subDivision);

    txtfldReferenceSubDivisionOnlineAddress.setText(subDivision.getOnlineAddress());
    dtpckReferenceSubDivisionLastAccess.setValue(DateConvertUtils.asLocalDate(subDivision.getLastAccessDate()));

    txtarReferenceSubDivisionNotes.setText(subDivision.getNotes());

//    trtblvwReferenceSubDivisionFiles.setRoot(new FileRootTreeItem(subDivision));

    fieldsWithUnsavedChanges.clear();

    subDivision.addEntityListener(referenceSubDivisionListener);
    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }



  public void handleButtonNewOrEditSeriesTitleCategoryAction(ActionEvent event) {
//    if(btnNewOrEditSeriesTitleCategory.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
//      net.deepthought.controller.Dialogs.showEditSeriesTitleCategoryDialog(cmbxSeriesTitleCategory.getValue());
//    else
//      createNewSeriesTitleCategory();
  }

  public void handleButtonChooseSeriesTitleFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneSeriesTitleCategory.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleCategory, "series.title.category");

    if(paneSeriesTitleSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleSubTitle, "subtitle");
    if(paneSeriesTitleTitleSupplement.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleTitleSupplement, "title.supplement");

    if(ttldpnSeriesTitleAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleAbstract, "abstract");
    if(ttldpnSeriesTitleTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleTableOfContents, "table.of.contents");

    if(seriesTitlePersonsControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, seriesTitlePersonsControl, "persons");

    if(paneSeriesTitleFirstAndLastDayOfPublication.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleFirstAndLastDayOfPublication, "first.day.of.publication");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleFirstAndLastDayOfPublication, "last.day.of.publication");
    }
    if(paneSeriesTitlePublisher.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitlePublisher, "publisher");

    if(paneSeriesTitleAbbreviation.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleAbbreviation, "standard.abbreviation");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleAbbreviation, "user.abbreviation1");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleAbbreviation, "user.abbreviation2");
    }
    if(paneSeriesTitleOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleOnlineAddress, "online.address");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleOnlineAddress, "last.access");
    }

    if(ttldpnSeriesTitleNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleNotes, "notes");
    if(ttldpnSeriesTitleFiles.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleFiles, "files");

    hiddenFieldsMenu.show(btnChooseSeriesTitleFieldsToShow, Side.BOTTOM, 0, 0);
  }

  public void handleButtonNewOrEditSeriesTitleAction(ActionEvent event) {

  }

  public void handleButtonChooseFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneReferenceCategory.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceCategory, "reference.category");

    if(paneSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSubTitle, "subtitle");
    if(paneTitleSupplement.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneTitleSupplement, "title.supplement");

    if(ttldpnAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnAbstract, "abstract");
    if(ttldpnTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnTableOfContents, "table.of.contents");

    if(paneEditionAndVolume.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneEditionAndVolume, "edition");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneEditionAndVolume, "volume");
    }
    if(panePublishingDateAndPlaceOfPublication.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, panePublishingDateAndPlaceOfPublication, "publishing.date");
      createHiddenFieldMenuItem(hiddenFieldsMenu, panePublishingDateAndPlaceOfPublication, "place.of.publication");
    }
    if(panePublisher.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, panePublisher, "publisher");

    if(pnReferenceLength.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, pnReferenceLength, "isbn.or.issn");
      createHiddenFieldMenuItem(hiddenFieldsMenu, pnReferenceLength, "length");
    }
    if(paneIssue.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneIssue, "issue");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneIssue, "year");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneIssue, "doi");
    }
    if(panePriceAndLanguage.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, panePriceAndLanguage, "price");
      createHiddenFieldMenuItem(hiddenFieldsMenu, panePriceAndLanguage, "language");
    }
    if(paneOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneOnlineAddress, "online.address");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneOnlineAddress, "last.access");
    }

    if(ttldpnNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnNotes, "notes");
    if(ttldpnFiles.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnFiles, "files");

    hiddenFieldsMenu.show(btnChooseFieldsToShow, Side.BOTTOM, 0, 0);
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
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneReferenceSubDivisionOnlineAddress, "last.access");
    }

    if(ttldpnReferenceSubDivisionNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceSubDivisionNotes, "notes");
    if(ttldpnReferenceSubDivisionFiles.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceSubDivisionFiles, "files");

    hiddenFieldsMenu.show(btnChooseFieldsToShowForReferenceSubDivision, Side.BOTTOM, 0, 0);
  }

  protected void createHiddenFieldMenuItem(ContextMenu hiddenFieldsMenu, Node nodeToShowOnClick, String menuItemText) {
    MenuItem titleMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(titleMenuItem, menuItemText);
    hiddenFieldsMenu.getItems().add(titleMenuItem);
    titleMenuItem.setOnAction(event -> nodeToShowOnClick.setVisible(true));
  }

  protected void handleMenuItemNewSeriesTitleAction(NewOrEditButtonMenuActionEvent event) {

  }

  public void handleButtonNewOrEditReferenceCategoryAction(ActionEvent event) {
//    if(btnNewOrEditReferenceCategory.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
//      net.deepthought.controller.Dialogs.showEditReferenceCategoryDialog(cmbxReferenceCategory.getValue());
//    else
//      createNewSeriesTitleCategory();
  }

  protected void handleMenuItemNewReferenceCategoryAction(NewOrEditButtonMenuActionEvent event) {
    createNewReferenceCategory();
  }

  protected void createNewReferenceCategory() {
    final ReferenceCategory newReferenceCategory = new ReferenceCategory();

//    net.deepthought.controller.Dialogs.showEditReferenceCategoryDialog(newReferenceCategory, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//        if(controller.getDialogResult() == DialogResult.Ok)
//          reference.setCategory(newReferenceCategory);
//      }
//    });
  }

  @FXML
  public void handleButtonNewOrEditPublisherAction(ActionEvent event) {

  }

  protected void handleMenuItemNewPublisherAction(NewOrEditButtonMenuActionEvent event) {

  }

  public void handleButtonNewOrEditReferenceIndicationUnitAction(ActionEvent event) {
//    if(btnNewOrEditReferenceLengthUnit.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
//      net.deepthought.controller.Dialogs.showEditReferenceIndicationUnitDialog(cmbxReferenceLengthUnit.getValue());
//    else
//      createNewReferenceIndicationUnit();
  }

  protected void handleMenuItemNewReferenceIndicationUnitAction(NewOrEditButtonMenuActionEvent event) {
    createNewReferenceIndicationUnit();
  }

  protected void createNewReferenceIndicationUnit() {
    final ReferenceIndicationUnit newReferenceIndicationUnit = new ReferenceIndicationUnit();

//    net.deepthought.controller.Dialogs.showEditReferenceIndicationUnitDialog(newReferenceIndicationUnit, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//        if(controller.getDialogResult() == DialogResult.Ok)
//          reference.setLengthUnit(newReferenceIndicationUnit);
//      }
//    });
  }

  public void handleButtonNewOrEditLanguageAction(ActionEvent event) {
//    if(btnNewOrEditLanguage.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
//      net.deepthought.controller.Dialogs.showEditLanguageDialog(cmbxLanguage.getValue());
//    else
//      createNewLanguage();
  }

  protected void handleMenuItemNewLanguageAction(NewOrEditButtonMenuActionEvent event) {
    createNewLanguage();
  }

  protected void createNewLanguage() {
    final Language newLanguage = new Language();

//    net.deepthought.controller.Dialogs.showEditLanguageDialog(newLanguage, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//        if(controller.getDialogResult() == DialogResult.Ok)
//          reference.setLanguage(newLanguage);
//      }
//    });
  }

  @FXML
  public void handleButtonAddFileAction(ActionEvent event) {
//    final FileLink newFile = new FileLink();
//
//    net.deepthought.controller.Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//        if (controller.getDialogResult() == DialogResult.Ok) {
//          reference.addFile(newFile);
//        }
//      }
//    });
  }

  @FXML
  public void handleButtonReferenceSubDivisionAddFileAction(ActionEvent event) {
//    final FileLink newFile = new FileLink();
//
//    net.deepthought.controller.Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
//      @Override
//      public void windowClosing(Stage stage, ChildWindowsController controller) {
//
//      }
//
//      @Override
//      public void windowClosed(Stage stage, ChildWindowsController controller) {
//        if (controller.getDialogResult() == DialogResult.Ok) {
//          reference.addFile(newFile);
//        }
//      }
//    });
  }


  public Reference getReference() {
    return reference;
  }

  public void setWindowStageAndReference(Stage windowStage, Reference reference) {
    setWindowStageAndReferenceSubDivision(windowStage, reference, new ReferenceSubDivision());
  }

  public void setWindowStageAndReferenceSubDivision(Stage windowStage, Reference reference, ReferenceSubDivision subDivision) {
    this.reference = reference;
    this.referenceSubDivision = subDivision;

    this.seriesTitle = reference.getSeries();
    if(seriesTitle == null)
      seriesTitle = new SeriesTitle();

    super.setWindowStage(windowStage);

    updateWindowTitle(reference.getTitle());
    windowStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        askIfStageShouldBeClosed(event);
      }
    });

    setupControls();
    txtfldTitle.requestFocus();

    setSeriesTitleValues(seriesTitle);
    setReferenceValues(reference);
    setReferenceSubDivisionValues(subDivision);
  }

  protected void setReferenceValues(final Reference reference) {
    btnApplyChanges.setVisible(reference.isPersisted());

    if(reference.getSeries() == null)
      cmbxSeriesTitle.setValue(Empty.Series);
    else
      cmbxSeriesTitle.setValue(reference.getSeries());

    txtfldTitle.setText(reference.getTitle());
    if(reference.getCategory() == null)
      cmbxReferenceCategory.setValue(Empty.ReferenceCategory);
    else
      cmbxReferenceCategory.setValue(reference.getCategory());
    txtfldSubTitle.setText(reference.getSubTitle());
    txtfldTitleSupplement.setText(reference.getTitleSupplement());

    txtarAbstract.setText(reference.getAbstract());
    htmledTableOfContents.setHtmlText(reference.getTableOfContents());

    referencePersonsControl.setReference(reference);

    txtfldEdition.setText(reference.getEdition());
    txtfldVolume.setText(reference.getVolume());

    dtpckPublishingDate.setValue(DateConvertUtils.asLocalDate(reference.getPublishingDate()));
    txtfldPlaceOfPublication.setText(reference.getPlaceOfPublication());
    if(reference.getPublisher() == null)
      cmbxPublisher.setValue(Empty.Publisher);
    else
      cmbxPublisher.setValue(reference.getPublisher());

    txtfldIsbnOrIssn.setText(reference.getIsbnOrIssn());
    txtfldReferenceLength.setText(reference.getLength());
    cmbxReferenceLengthUnit.setValue(reference.getLengthUnit());

    txtfldIssue.setText(reference.getIssue());
    if(reference.getYear() != null)
      txtfldYear.setText(Integer.toString(reference.getYear()));
    txtfldDoi.setText(reference.getDoi());

    txtfldPrice.setText(reference.getPrice());
    if(reference.getLanguage() == null)
      cmbxLanguage.setValue(Empty.Language);
    else
      cmbxLanguage.setValue(reference.getLanguage());

    txtfldOnlineAddress.setText(reference.getOnlineAddress());
    dtpckLastAccess.setValue(DateConvertUtils.asLocalDate(reference.getLastAccessDate()));

    txtarNotes.setText(reference.getNotes());

//    trtblvwFiles.setRoot(new FileRootTreeItem(reference));

    fieldsWithUnsavedChanges.clear();

    reference.addEntityListener(referenceListener);
    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }

  public boolean hasUnsavedChanges() {
    return fieldsWithUnsavedChanges.size() > 0;
  }

  protected void updateWindowTitle(String referenceTitle) {
    if(this.reference.isPersisted() == false)
      windowStage.setTitle(Localization.getLocalizedStringForResourceKey("create.reference", referenceTitle));
    else
      windowStage.setTitle(Localization.getLocalizedStringForResourceKey("edit.reference", referenceTitle));
  }


  protected StringConverter<LocalDate> localeDateStringConverter = new StringConverter<LocalDate>() {
    @Override
    public String toString(LocalDate object) {
      return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(DateConvertUtils.asUtilDate(object));
    }

    @Override
    public LocalDate fromString(String string) {
      try {
        return DateConvertUtils.asLocalDate(DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).parse(string));
      } catch(Exception ex) { log.warn("Could not parse string {} to java.util.date for Locale {}", string, Localization.getLanguageLocale()); }

      return LocalDate.now();
    }
  };


  protected EntityListener seriesTitleListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.ReferenceBaseTitleColumnName))
        seriesTitleTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleCategoryJoinColumnName))
        seriesTitleCategoryChanged((SeriesTitleCategory) previousValue, (SeriesTitleCategory) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
        seriesTitleSubTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleTitleSupplementColumnName))
        seriesTitleTitleSupplementChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
        seriesTitleAbstractChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleTableOfContentsColumnName))
        seriesTitleTableOfContentsChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleFirstDayOfPublicationColumnName))
        seriesTitleFirstDayOfPublicationChanged((Date) previousValue, (Date) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleLastDayOfPublicationColumnName))
        seriesTitleLastDayOfPublicationChanged((Date) previousValue, (Date) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitlePublisherJoinColumnName))
        seriesTitlePublisherChanged((Publisher) previousValue, (Publisher) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleStandardAbbreviationColumnName))
        seriesTitleStandardAbbreviationChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleUserAbbreviation1ColumnName))
        seriesTitleUserAbbreviation1Changed((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleUserAbbreviation2ColumnName))
        seriesTitleUserAbbreviation2Changed((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseOnlineAddressColumnName))
        seriesTitleOnlineAddressChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseLastAccessDateColumnName))
        seriesTitleLastAccessChanged((Date) previousValue, (Date) newValue);
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
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleTitle);
  }

  protected void seriesTitleCategoryChanged(SeriesTitleCategory previousValue, SeriesTitleCategory newValue) {
    // TODO: if current value != previousValue, ask User what to do?
//    cmbxSeriesTitleCategory.valueProperty().removeListener(cmbxSeriesTitleCategoryValueChangeListener);
    if(newValue != null)
      cmbxSeriesTitleCategory.setValue(newValue);
    else
      cmbxSeriesTitleCategory.setValue(Empty.SeriesCategory);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSeriesTitleCategory);
//    cmbxSeriesTitleCategory.valueProperty().addListener(cmbxSeriesTitleCategoryValueChangeListener);
  }

  protected void seriesTitleSubTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleSubTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSubTitle);
  }

  protected void seriesTitleTitleSupplementChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleTitleSupplement.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleTitleSupplement);
  }

  protected void seriesTitleAbstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarSeriesTitleAbstract.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleAbstract);
  }

  protected void seriesTitleTableOfContentsChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledSeriesTitleTableOfContents.setHtmlText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleTableOfContents);
  }

  protected void seriesTitleFirstDayOfPublicationChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckSeriesTitleFirstDayOfPublication.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication);
  }

  protected void seriesTitleLastDayOfPublicationChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckSeriesTitleLastDayOfPublication.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication);
  }

  protected void seriesTitlePublisherChanged(Publisher previousValue, Publisher newValue) {
    // TODO: if current value != previousValue, ask User what to do?
//    cmbxSeriesTitlePublisher.valueProperty().removeListener(cmbxSeriesTitlePublisherValueChangeListener);
    if(newValue != null)
      cmbxSeriesTitlePublisher.setValue(newValue);
    else
      cmbxSeriesTitlePublisher.setValue(Empty.Publisher);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitlePublisher);
//    cmbxSeriesTitlePublisher.valueProperty().addListener(cmbxSeriesTitlePublisherValueChangeListener);
  }

  protected void seriesTitleStandardAbbreviationChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleStandardAbbreviation.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleStandardAbbreviation);
  }

  protected void seriesTitleUserAbbreviation1Changed(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleUserAbbreviation1.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation1);
  }

  protected void seriesTitleUserAbbreviation2Changed(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleUserAbbreviation2.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation2);
  }

  protected void seriesTitleOnlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleOnlineAddress.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleOnlineAddress);
  }

  protected void seriesTitleLastAccessChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckSeriesTitleLastAccess.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleLastAccess);
  }

  protected void seriesTitleNotesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarSeriesTitleNotes.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleNotes);
  }


  protected EntityListener referenceListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.ReferenceSeriesTitleJoinColumnName))
        seriesTitleChanged((SeriesTitle) previousValue, (SeriesTitle) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseTitleColumnName))
        titleChanged((String)previousValue, (String)newValue);
      else if(propertyName.equals(TableConfig.ReferenceCategoryJoinColumnName))
        referenceCategoryChanged((ReferenceCategory)previousValue, (ReferenceCategory)newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
        subTitleChanged((String)previousValue, (String)newValue);
      else if(propertyName.equals(TableConfig.ReferenceTitleSupplementColumnName))
        titleSupplementChanged((String)previousValue, (String)newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
        abstractChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceTableOfContentsColumnName))
        tableOfContentsChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceEditionColumnName))
        editionChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceVolumeColumnName))
        volumeChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferencePublishingDateColumnName))
        publishingDateChanged((Date) previousValue, (Date) newValue);
      else if(propertyName.equals(TableConfig.ReferencePlaceOfPublicationColumnName))
        placeOfPublicationChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferencePublisherJoinColumnName))
        publisherChanged((Publisher) previousValue, (Publisher) newValue);
      else if(propertyName.equals(TableConfig.ReferenceIsbnOrIssnColumnName))
        isbnOrIssnChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceLengthColumnName))
        referenceLengthChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceLengthUnitJoinColumnName))
        referenceLengthUnitChanged((ReferenceIndicationUnit) previousValue, (ReferenceIndicationUnit) newValue);
      else if(propertyName.equals(TableConfig.ReferenceIssueColumnName))
        issueChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceYearColumnName))
        yearChanged(previousValue == null ? "" : previousValue.toString(), newValue.toString());
      else if(propertyName.equals(TableConfig.ReferenceDoiColumnName))
        doiChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferencePriceColumnName))
        priceChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceLanguageJoinColumnName))
        languageChanged((Language) previousValue, (Language) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseOnlineAddressColumnName))
        onlineAddressChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseLastAccessDateColumnName))
        lastAccessChanged((Date) previousValue, (Date) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseNotesColumnName))
        notesChanged((String) previousValue, (String) newValue);
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

  protected void seriesTitleChanged(SeriesTitle previousValue, SeriesTitle newValue) {
    // TODO: if current value != previousValue, ask User what to do?
//    cmbxSeriesTitle.valueProperty().removeListener(cmbxSeriesTitleValueChangeListener);
    if(newValue == null)
      cmbxSeriesTitle.setValue(Empty.Series);
    else
      cmbxSeriesTitle.setValue(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSeriesTitle);
//    cmbxSeriesTitle.valueProperty().addListener(cmbxSeriesTitleValueChangeListener);
  }

  protected void titleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitle);
  }

  protected void referenceCategoryChanged(ReferenceCategory previousValue, ReferenceCategory newValue) {
    // TODO: if current value != previousValue, ask User what to do?
//    cmbxReferenceCategory.valueProperty().removeListener(cmbxReferenceCategoryValueChangeListener);
    if(newValue == null)
      cmbxReferenceCategory.setValue(Empty.ReferenceCategory);
    else
      cmbxReferenceCategory.setValue(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceReferenceCategory);
//    cmbxReferenceCategory.valueProperty().addListener(cmbxReferenceCategoryValueChangeListener);
  }

  protected void subTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSubTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubTitle);
  }

  protected void titleSupplementChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldTitleSupplement.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitleSupplement);
  }

  protected void abstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarAbstract.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceAbstract);
  }

  protected void tableOfContentsChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledTableOfContents.setHtmlText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTableOfContents);
  }

  protected void editionChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldEdition.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceEdition);
  }

  protected void volumeChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldVolume.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceVolume);
  }

  protected void publishingDateChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckPublishingDate.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePublishingDate);
  }

  protected void placeOfPublicationChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldPlaceOfPublication.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePlaceOfPublication);
  }

  protected void publisherChanged(Publisher previousValue, Publisher newValue) {
    // TODO: if current value != previousValue, ask User what to do?
//    cmbxPublisher.valueProperty().removeListener(cmbxPublisherValueChangeListener);
    if(newValue == null)
      cmbxPublisher.setValue(Empty.Publisher);
    else
      cmbxPublisher.setValue(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePublisher);
//    cmbxPublisher.valueProperty().addListener(cmbxPublisherValueChangeListener);
  }

  protected void isbnOrIssnChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldIsbnOrIssn.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceIsbnOrIssn);
  }

  protected void referenceLengthChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceLength.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceReferenceLength);
  }

  protected void referenceLengthUnitChanged(ReferenceIndicationUnit previousValue, ReferenceIndicationUnit newValue) {
    // TODO: if current value != previousValue, ask User what to do?
//    cmbxReferenceLengthUnit.valueProperty().removeListener(cmbxReferenceLengthUnitValueChangeListener);
    cmbxReferenceLengthUnit.setValue(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceReferenceLengthUnit);
//    cmbxReferenceLengthUnit.valueProperty().addListener(cmbxReferenceLengthUnitValueChangeListener);
  }

  protected void issueChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldIssue.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceIssue);
  }

  protected void yearChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldYear.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceYear);
  }

  protected void doiChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldDoi.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceDoi);
  }

  protected void priceChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldPrice.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePrice);
  }

  protected void languageChanged(Language previousValue, Language newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    cmbxLanguage.valueProperty().removeListener(cmbxLanguageValueChangeListener);
    if(newValue == null)
      cmbxLanguage.setValue(Empty.Language);
    else
      cmbxLanguage.setValue(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceLanguage);
    cmbxLanguage.valueProperty().addListener(cmbxLanguageValueChangeListener);
  }

  protected void onlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldOnlineAddress.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceOnlineAddress);
  }

  protected void lastAccessChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckLastAccess.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceLastAccess);
  }

  protected void notesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarNotes.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceNotes);
  }



  protected EntityListener referenceSubDivisionListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.ReferenceBaseTitleColumnName))
        referenceSubDivisionTitleChanged((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitleCategoryJoinColumnName))
//        seriesTitleCategoryChanged((SeriesTitleCategory) previousValue, (SeriesTitleCategory) newValue);
//      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
//        seriesTitleSubTitleChanged((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitleTitleSupplementColumnName))
//        seriesTitleTitleSupplementChanged((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
//        seriesTitleAbstractChanged((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitleTableOfContentsColumnName))
//        seriesTitleTableOfContentsChanged((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitleFirstDayOfPublicationColumnName))
//        seriesTitleFirstDayOfPublicationChanged((Date) previousValue, (Date) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitleLastDayOfPublicationColumnName))
//        seriesTitleLastDayOfPublicationChanged((Date) previousValue, (Date) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitlePublisherJoinColumnName))
//        seriesTitlePublisherChanged((Publisher) previousValue, (Publisher) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitleStandardAbbreviationColumnName))
//        seriesTitleStandardAbbreviationChanged((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitleUserAbbreviation1ColumnName))
//        seriesTitleUserAbbreviation1Changed((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.SeriesTitleUserAbbreviation2ColumnName))
//        seriesTitleUserAbbreviation2Changed((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.ReferenceBaseOnlineAddressColumnName))
//        seriesTitleOnlineAddressChanged((String) previousValue, (String) newValue);
//      else if(propertyName.equals(TableConfig.ReferenceBaseLastAccessDateColumnName))
//        seriesTitleLastAccessChanged((Date) previousValue, (Date) newValue);
//      else if(propertyName.equals(TableConfig.ReferenceBaseNotesColumnName))
//        seriesTitleNotesChanged((String) previousValue, (String) newValue);
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
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionTitle);
  }

  protected void referenceSubDivisionSubTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceSubDivisionSubTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionSubTitle);
  }

  protected void referenceSubDivisionAbstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarReferenceSubDivisionAbstract.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionAbstract);
  }

  protected void referenceSubDivisionOnlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldReferenceSubDivisionOnlineAddress.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress);
  }

  protected void referenceSubDivisionLastAccessChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckReferenceSubDivisionLastAccess.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionLastAccess);
  }

  protected void referenceSubDivisionNotesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarReferenceSubDivisionNotes.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionNotes);
  }


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == Application.getDeepThought().getSeriesTitleCategories()) {
        resetComboBoxSeriesTitleCategoryItems();
      }
      else if(collection == Application.getDeepThought().getPublishers()) {
        resetComboBoxSeriesTitlePublisherItems();
      }

      if(collection == Application.getDeepThought().getPublishers()) {
        resetComboBoxPublisherItems();
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(updatedEntity instanceof SeriesTitleCategory && updatedEntity.equals(cmbxSeriesTitleCategory.getValue())) {
        cmbxSeriesTitleCategory.valueProperty().removeListener(cmbxSeriesTitleCategoryValueChangeListener);
        cmbxSeriesTitleCategory.setValue(Empty.SeriesCategory); // don't know any other way to get ComboBox's current item updated
        cmbxSeriesTitleCategory.setValue((SeriesTitleCategory) updatedEntity);
        cmbxSeriesTitleCategory.valueProperty().addListener(cmbxSeriesTitleCategoryValueChangeListener);
      }
      else if(updatedEntity instanceof Publisher && updatedEntity.equals(cmbxSeriesTitlePublisher.getValue())) {
        cmbxSeriesTitlePublisher.valueProperty().removeListener(cmbxSeriesTitlePublisherValueChangeListener);
        cmbxSeriesTitlePublisher.setValue(Empty.Publisher);
        cmbxSeriesTitlePublisher.setValue((Publisher) updatedEntity);
        cmbxSeriesTitlePublisher.valueProperty().addListener(cmbxSeriesTitlePublisherValueChangeListener);
      }

      if(updatedEntity instanceof SeriesTitle && updatedEntity.equals(cmbxSeriesTitle.getValue())) {
        cmbxSeriesTitle.valueProperty().removeListener(cmbxSeriesTitleValueChangeListener);
        cmbxSeriesTitle.setValue(Empty.Series); // don't know any other way to get ComboBox's current item updated
        cmbxSeriesTitle.setValue((SeriesTitle) updatedEntity);
        cmbxSeriesTitle.valueProperty().addListener(cmbxSeriesTitleValueChangeListener);
      }
      else if(updatedEntity instanceof ReferenceCategory && updatedEntity.equals(cmbxReferenceCategory.getValue())) {
        cmbxReferenceCategory.valueProperty().removeListener(cmbxReferenceCategoryValueChangeListener);
        cmbxReferenceCategory.setValue(Empty.ReferenceCategory);
        cmbxReferenceCategory.setValue((ReferenceCategory) updatedEntity);
        cmbxReferenceCategory.valueProperty().addListener(cmbxReferenceCategoryValueChangeListener);
      }
      else if(updatedEntity instanceof Publisher && updatedEntity.equals(cmbxPublisher.getValue())) {
        cmbxPublisher.valueProperty().removeListener(cmbxPublisherValueChangeListener);
        cmbxPublisher.setValue(Empty.Publisher);
        cmbxPublisher.setValue((Publisher) updatedEntity);
        cmbxPublisher.valueProperty().addListener(cmbxPublisherValueChangeListener);
      }
      else if(updatedEntity instanceof ReferenceIndicationUnit && updatedEntity.equals(cmbxReferenceLengthUnit.getValue())) {
        cmbxReferenceLengthUnit.valueProperty().removeListener(cmbxReferenceLengthUnitValueChangeListener);
        cmbxReferenceLengthUnit.setValue(Empty.ReferenceIndicationUnit);
        cmbxReferenceLengthUnit.setValue((ReferenceIndicationUnit) updatedEntity);
        cmbxReferenceLengthUnit.valueProperty().addListener(cmbxReferenceLengthUnitValueChangeListener);
      }
      else if(updatedEntity instanceof Language && updatedEntity.equals(cmbxLanguage.getValue())) {
        cmbxLanguage.valueProperty().removeListener(cmbxLanguageValueChangeListener);
        cmbxLanguage.setValue(Empty.Language);
        cmbxLanguage.setValue((Language) updatedEntity);
        cmbxLanguage.valueProperty().addListener(cmbxLanguageValueChangeListener);
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == Application.getDeepThought().getSeriesTitleCategories()) {
        resetComboBoxSeriesTitleCategoryItems();
      }
      else if(collection == Application.getDeepThought().getPublishers()) {
        resetComboBoxSeriesTitlePublisherItems();
      }

      if(collection == Application.getDeepThought().getPublishers()) {
        resetComboBoxPublisherItems();
      }
    }
  };

}
