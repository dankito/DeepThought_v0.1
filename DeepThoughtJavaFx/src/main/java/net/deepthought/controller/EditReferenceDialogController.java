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
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Publisher;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.enums.ReferenceCategory;
import net.deepthought.data.model.enums.ReferenceIndicationUnit;
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
import javafx.scene.input.KeyEvent;
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


  protected Reference reference = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();


  @FXML
  protected BorderPane dialogPane;

  @FXML
  protected Button btnApplyChanges;

  @FXML
  protected Pane contentPane;

  @FXML
  protected Pane pnSeriesTitle;
  @FXML
  protected ComboBox<SeriesTitle> cmbxSeriesTitle;
  @FXML
  protected NewOrEditButton btnNewOrEditSeriesTitle;
  @FXML
  protected Button btnChooseFieldsToShow;
  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;

  protected ContextHelpControl contextHelpControl;

  @FXML
  protected Pane paneTitle;
  @FXML
  protected TextField txtfldTitle;
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
  protected TitledPane ttldpnReferenceSubDivisions;

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
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseTitle);
      updateWindowTitle(newValue);
    });

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
    paneTitle.getChildren().add(btnNewOrEditReferenceCategory);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSubTitle);
    txtfldSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneTitleSupplement);
    txtfldTitleSupplement.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceTitleSupplement));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnAbstract);
    txtarAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseAbstract));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnTableOfContents);
    // TODO: how to set HtmlEditor text changed listener? (https://stackoverflow.com/questions/22128153/javafx-htmleditor-text-change-listener)
    htmledTableOfContents.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceTableOfContents);
      }
    });

    referencePersonsControl = new ReferencePersonsControl();
    referencePersonsControl.setExpanded(true);
    referencePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBasePersons));
    referencePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBasePersons));
    contentPane.getChildren().add(6, referencePersonsControl);

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
    txtfldOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseOnlineAddress));
    dtpckLastAccess.setConverter(localeDateStringConverter);
    dtpckLastAccess.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseLastAccess));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnNotes);
    txtarNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseNotes));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnFiles);
//    clmnFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
//      @Override
//      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
//        return new FileTreeTableCell(reference);
//      }
//    });

    contextHelpControl = new ContextHelpControl("context.help.series.title.");
    dialogPane.setRight(contextHelpControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(contextHelpControl);
    contextHelpControl.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());
    tglbtnShowHideContextHelp.setGraphic(new ImageView(Constants.ContextHelpIconPath));
    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
  }

  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
    btnChooseFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneSubTitle.setVisible(StringUtils.isNotNullOrEmpty(reference.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneTitleSupplement.setVisible(StringUtils.isNotNullOrEmpty(reference.getTitleSupplement()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnAbstract.setVisible(StringUtils.isNotNullOrEmpty(reference.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(reference.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneEditionAndVolume.setVisible(StringUtils.isNotNullOrEmpty(reference.getEdition()) || StringUtils.isNotNullOrEmpty(reference.getVolume()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    panePublishingDateAndPlaceOfPublication.setVisible(reference.getPublishingDate() != null || StringUtils.isNotNullOrEmpty(reference.getPlaceOfPublication()) ||
        dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    panePublisher.setVisible(reference.getPublisher() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    pnReferenceLength.setVisible(StringUtils.isNotNullOrEmpty(reference.getIsbnOrIssn()) || StringUtils.isNotNullOrEmpty(reference.getLength()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneIssue.setVisible(StringUtils.isNotNullOrEmpty(reference.getIssue()) || reference.getYear() != null || StringUtils.isNotNullOrEmpty(reference.getDoi()) ||
        dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    panePriceAndLanguage.setVisible(StringUtils.isNotNullOrEmpty(reference.getPrice()) || reference.getLanguage() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(reference.getOnlineAddress()) || reference.getLastAccessDate() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnNotes.setVisible(StringUtils.isNotNullOrEmpty(reference.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnFiles.setVisible(reference.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
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
    saveEditedFieldsOnEntry();
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Cancel);
    closeDialog();
  }

  @FXML
  public void handleButtonOkAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Ok);

    if(reference.getId() == null) { // a new Reference
      Application.getDeepThought().addReference(reference);
    }

    saveEditedFieldsOnEntry();
    closeDialog();
  }

  @Override
  protected void closeDialog() {
    reference.removeEntityListener(referenceListener);
    Application.getDeepThought().removeEntityListener(deepThoughtListener);

    super.closeDialog();
  }

  protected void saveEditedFieldsOnEntry() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSeriesTitle)) {
      if(Empty.Series.equals(cmbxSeriesTitle.getValue()))
        reference.setSeries(null);
      else
        reference.setSeries(cmbxSeriesTitle.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSeriesTitle);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseTitle)) {
      reference.setTitle(txtfldTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceReferenceCategory)) {
      if(Empty.ReferenceCategory.equals(cmbxReferenceCategory.getValue()))
        reference.setCategory(null);
      else
        reference.setCategory(cmbxReferenceCategory.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceReferenceCategory);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseSubTitle)) {
      reference.setSubTitle(txtfldSubTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseSubTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceTitleSupplement)) {
      reference.setTitleSupplement(txtfldTitleSupplement.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitleSupplement);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseAbstract)) {
      reference.setAbstract(txtarAbstract.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseAbstract);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceTableOfContents) || htmledTableOfContents.getHtmlText().equals(reference.getTableOfContents()) == false) {
      reference.setTableOfContents(htmledTableOfContents.getHtmlText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTableOfContents);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBasePersons)) {
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

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBasePersons);
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

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseOnlineAddress)) {
      reference.setOnlineAddress(txtfldOnlineAddress.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseOnlineAddress);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseLastAccess)) {
      reference.setLastAccessDate(DateConvertUtils.asUtilDate(dtpckLastAccess.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseLastAccess);
    }

//    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanged.ReferenceSubDivisions)) {
//
//      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanged.ReferenceSubDivisions);
//    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseNotes)) {
      reference.setNotes(txtarNotes.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseNotes);
    }

//    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanged.ReferenceBaseFiles)) {
//
//      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanged.ReferenceBaseFiles);
//    }
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
        saveEditedFieldsOnEntry();
        closeDialog();
      }
      else
        closeDialog();
    }
  }


  public void handleButtonNewOrEditSeriesTitleAction(ActionEvent event) {
    if(btnNewOrEditSeriesTitle.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
      net.deepthought.controller.Dialogs.showEditSeriesTitleDialog(cmbxSeriesTitle.getValue());
    else
      createNewSeriesTitle();
  }

  public void handleButtonChooseFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

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

  protected void createHiddenFieldMenuItem(ContextMenu hiddenFieldsMenu, Node nodeToShowOnClick, String menuItemText) {
    MenuItem titleMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(titleMenuItem, menuItemText);
    hiddenFieldsMenu.getItems().add(titleMenuItem);
    titleMenuItem.setOnAction(event -> nodeToShowOnClick.setVisible(true));
  }

  protected void handleMenuItemNewSeriesTitleAction(NewOrEditButtonMenuActionEvent event) {
    createNewSeriesTitle();
  }

  protected void createNewSeriesTitle() {
    final SeriesTitle newSeriesTitle = new SeriesTitle();

    net.deepthought.controller.Dialogs.showEditSeriesTitleDialog(newSeriesTitle, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {

      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {
        if(controller.getDialogResult() == DialogResult.Ok)
          reference.setSeries(newSeriesTitle);
      }
    });
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

  protected void createNewPublisher() {
    final Publisher newPublisher = new Publisher();

    net.deepthought.controller.Dialogs.showEditPublisherDialog(newPublisher, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {

      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {
        if(controller.getDialogResult() == DialogResult.Ok)
          reference.setPublisher(newPublisher);
      }
    });
  }

  @FXML
  public void handleButtonNewOrEditPublisherAction(ActionEvent event) {
    if(btnNewOrEditPublisher.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
      net.deepthought.controller.Dialogs.showEditPublisherDialog(cmbxPublisher.getValue());
    else
      createNewPublisher();
  }

  protected void handleMenuItemNewPublisherAction(NewOrEditButtonMenuActionEvent event) {
    createNewPublisher();
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


  public Reference getReference() {
    return reference;
  }

  public void setWindowStageAndReference(Stage windowStage, Reference reference) {
    this.reference = reference;
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

    setReferenceValues(reference);
    reference.addEntityListener(referenceListener);
  }

  protected void setReferenceValues(final Reference reference) {
    btnApplyChanges.setVisible(reference.getId() != null);

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

    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }

  public boolean hasUnsavedChanges() {
    return fieldsWithUnsavedChanges.size() > 0;
  }

  protected void updateWindowTitle(String referenceTitle) {
    if(this.reference.getId() == null)
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
        yearChanged((String) previousValue, (String) newValue);
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
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseTitle);
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
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseSubTitle);
  }

  protected void titleSupplementChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldTitleSupplement.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitleSupplement);
  }

  protected void abstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarAbstract.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseAbstract);
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
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseOnlineAddress);
  }

  protected void lastAccessChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckLastAccess.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseLastAccess);
  }

  protected void notesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarNotes.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseNotes);
  }


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == Application.getDeepThought().getPublishers()) {
        resetComboBoxPublisherItems();
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
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
      if(collection == Application.getDeepThought().getPublishers()) {
        resetComboBoxPublisherItems();
      }
    }
  };

}
