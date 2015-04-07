package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.Constants;
import net.deepthought.controls.ContextHelpControl;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.event.NewOrEditButtonMenuActionEvent;
import net.deepthought.controls.person.ReferencePersonsControl;
import net.deepthought.controls.person.ReferenceSubDivisionPersonsControl;
import net.deepthought.controls.person.SeriesTitlePersonsControl;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.DateConvertUtils;
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
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
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
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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

  protected int publishingDateFormat = DateFormat.MEDIUM;


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
  protected Button btnChooseSeriesTitleFieldsToShow;

  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;

  protected ContextHelpControl contextHelpControl;

  @FXML
  protected Pane paneSeriesTitleSubTitle;
  @FXML
  protected TextField txtfldSeriesTitleSubTitle;

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
  protected Button btnChooseFieldsToShow;

  @FXML
  protected Pane paneTitle;
  @FXML
  protected TextField txtfldTitle;

  @FXML
  protected Pane paneSubTitle;
  @FXML
  protected TextField txtfldSubTitle;

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
  protected Pane panePublishingDate;
  @FXML
  protected DatePicker dtpckPublishingDate;

  @FXML
  protected TextField txtfldIsbnOrIssn;
  @FXML
  protected Pane paneIsbnOrIssn;

  @FXML
  protected Pane paneOnlineAddress;
  @FXML
  protected TextField txtfldOnlineAddress;

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

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleSubTitle);
    txtfldSeriesTitleSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleAbstract);
    txtarSeriesTitleAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleAbstract));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnSeriesTitleTableOfContents);
    FXUtils.addHtmlEditorTextChangedListener(htmledSeriesTitleTableOfContents, event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleTableOfContents));

    seriesTitlePersonsControl = new SeriesTitlePersonsControl();
    seriesTitlePersonsControl.setExpanded(true);
    seriesTitlePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitlePersons));
    seriesTitlePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitlePersons));
    VBox.setMargin(seriesTitlePersonsControl, new Insets(6, 0, 0, 0));
    paneSeriesTitle.getChildren().add(6, seriesTitlePersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(seriesTitlePersonsControl);
    seriesTitlePersonsControl.setVisible(false);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleOnlineAddress);
    txtfldSeriesTitleOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleOnlineAddress));

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
    txtfldTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceTitle);
      updateWindowTitle(newValue);
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSubTitle);
    txtfldSubTitle.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubTitle));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnAbstract);
    txtarAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceAbstract));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnTableOfContents);
    FXUtils.addHtmlEditorTextChangedListener(htmledTableOfContents, event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceTableOfContents));

    referencePersonsControl = new ReferencePersonsControl();
    referencePersonsControl.setExpanded(true);
    referencePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePersons));
    referencePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePersons));
    VBox.setMargin(referencePersonsControl, new Insets(6, 0, 0, 0));
    contentPane.getChildren().add(6, referencePersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(panePublishingDate);
    dtpckPublishingDate.setConverter(localeDateStringConverter);
    dtpckPublishingDate.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferencePublishingDate));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneIsbnOrIssn);
    txtfldIsbnOrIssn.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceIsbnOrIssn));
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneOnlineAddress);
    txtfldOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceOnlineAddress));

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
    VBox.setMargin(referenceSubDivisionPersonsControl, new Insets(6, 0, 0, 0));
    paneReferenceSubDivision.getChildren().add(4, referenceSubDivisionPersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(referenceSubDivisionPersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneReferenceSubDivisionOnlineAddress);
    txtfldReferenceSubDivisionOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress));

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

    paneSeriesTitleSubTitle.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnSeriesTitleAbstract.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnSeriesTitleTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    seriesTitlePersonsControl.setVisible(seriesTitle.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneSeriesTitleOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnSeriesTitleNotes.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnSeriesTitleFiles.setVisible(seriesTitle.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);


    btnChooseFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneSubTitle.setVisible(StringUtils.isNotNullOrEmpty(reference.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnAbstract.setVisible(StringUtils.isNotNullOrEmpty(reference.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(reference.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneIsbnOrIssn.setVisible(StringUtils.isNotNullOrEmpty(reference.getIsbnOrIssn()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(reference.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnNotes.setVisible(StringUtils.isNotNullOrEmpty(reference.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnFiles.setVisible(reference.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);


    btnChooseReferenceSubDivisionFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneReferenceSubDivisionSubTitle.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnReferenceSubDivisionAbstract.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    referenceSubDivisionPersonsControl.setVisible(referenceSubDivision.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneReferenceSubDivisionOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getOnlineAddress()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnReferenceSubDivisionNotes.setVisible(StringUtils.isNotNullOrEmpty(referenceSubDivision.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnReferenceSubDivisionFiles.setVisible(referenceSubDivision.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
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
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleSubTitle)) {
      seriesTitle.setSubTitle(txtfldSeriesTitleSubTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSubTitle);
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
      for(Person removedPerson : seriesTitlePersonsControl.getRemovedPersons())
        seriesTitle.removePerson(removedPerson);

      for(Person addedPerson : seriesTitlePersonsControl.getAddedPersons())
        seriesTitle.addPerson(addedPerson);

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitlePersons);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleOnlineAddress)) {
      seriesTitle.setOnlineAddress(txtfldSeriesTitleOnlineAddress.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleOnlineAddress);
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
      //TODO
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSeriesTitle);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceTitle)) {
      reference.setTitle(txtfldTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubTitle)) {
      reference.setSubTitle(txtfldSubTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubTitle);
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
      for(Person removedPerson : referencePersonsControl.getRemovedPersons())
        reference.removePerson(removedPerson);

      for(Person addedPerson : referencePersonsControl.getAddedPersons())
        reference.addPerson(addedPerson);

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePersons);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferencePublishingDate)) {
      reference.setPublishingDate(DateConvertUtils.asUtilDate(dtpckPublishingDate.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferencePublishingDate);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceIsbnOrIssn)) {
      reference.setIsbnOrIssn(txtfldIsbnOrIssn.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceIsbnOrIssn);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceOnlineAddress)) {
      reference.setOnlineAddress(txtfldOnlineAddress.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceOnlineAddress);
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
      for(Person removedPerson : referenceSubDivisionPersonsControl.getRemovedPersons())
        referenceSubDivision.removePerson(removedPerson);

      for(Person addedPerson : referenceSubDivisionPersonsControl.getAddedPersons())
        referenceSubDivision.addPerson(addedPerson);

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionPersons);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress)) {
      referenceSubDivision.setOnlineAddress(txtfldReferenceSubDivisionOnlineAddress.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubDivisionOnlineAddress);
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
    txtfldSeriesTitleSubTitle.setText(seriesTitle.getSubTitle());

    txtarSeriesTitleAbstract.setText(seriesTitle.getAbstract());
    htmledSeriesTitleTableOfContents.setHtmlText(seriesTitle.getTableOfContents());

    seriesTitlePersonsControl.setSeries(seriesTitle);

    txtfldSeriesTitleOnlineAddress.setText(seriesTitle.getOnlineAddress());

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

    if(paneSeriesTitleSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSeriesTitleSubTitle, "subtitle");

    if(ttldpnSeriesTitleAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleAbstract, "abstract");
    if(ttldpnSeriesTitleTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnSeriesTitleTableOfContents, "table.of.contents");

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

  public void handleButtonNewOrEditSeriesTitleAction(ActionEvent event) {

  }

  public void handleButtonChooseFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSubTitle, "subtitle");

    if(paneIsbnOrIssn.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneIsbnOrIssn, "isbn.or.issn");

    if(ttldpnAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnAbstract, "abstract");
    if(ttldpnTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnTableOfContents, "table.of.contents");

    if(paneOnlineAddress.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneOnlineAddress, "online.address");
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
    }

    if(ttldpnReferenceSubDivisionNotes.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceSubDivisionNotes, "notes");
    if(ttldpnReferenceSubDivisionFiles.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnReferenceSubDivisionFiles, "files");

    hiddenFieldsMenu.show(btnChooseReferenceSubDivisionFieldsToShow, Side.BOTTOM, 0, 0);
  }

  protected void createHiddenFieldMenuItem(ContextMenu hiddenFieldsMenu, Node nodeToShowOnClick, String menuItemText) {
    MenuItem titleMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(titleMenuItem, menuItemText);
    hiddenFieldsMenu.getItems().add(titleMenuItem);
    titleMenuItem.setOnAction(event -> nodeToShowOnClick.setVisible(true));
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

    txtfldTitle.setText(reference.getTitle());
    txtfldSubTitle.setText(reference.getSubTitle());

    txtarAbstract.setText(reference.getAbstract());
    htmledTableOfContents.setHtmlText(reference.getTableOfContents());

    referencePersonsControl.setReference(reference);

    dtpckPublishingDate.setValue(DateConvertUtils.asLocalDate(reference.getPublishingDate()));

    txtfldIsbnOrIssn.setText(reference.getIsbnOrIssn());

    txtfldOnlineAddress.setText(reference.getOnlineAddress());

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
    public String toString(LocalDate date) {
      log.debug("publishingDateFormat: " + publishingDateFormat);
      return DateFormat.getDateInstance(DateFormat.YEAR_FIELD, Localization.getLanguageLocale()).format(DateConvertUtils.asUtilDate(date));
    }

    @Override
    public LocalDate fromString(String string) {
      try {
        Date parsedDate = null;
        try {
          parsedDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).parse(string);
          publishingDateFormat = DateFormat.MEDIUM;
        } catch(Exception ex) {
          if(string.length() == 4) {
            try {
              int year = Integer.parseInt(string) - 1900;
              log.debug("Parsed year: " + year);
              parsedDate = new Date(year, 0, 1);
              publishingDateFormat = DateFormat.YEAR_FIELD;
            } catch(Exception ex2) { }
          }
          else if(string.length() == 7) {
            try {
              int month = Integer.parseInt(string.substring(0, 2)) - 1;
              int year = Integer.parseInt(string.substring(3, 7)) - 1900;
              parsedDate = new Date(year, month, 1);
              publishingDateFormat = DateFormat.SHORT;
            } catch(Exception ex2) { }
          }
          else {
            try {
              parsedDate = DateFormat.getDateInstance(DateFormat.LONG, Localization.getLanguageLocale()).parse(string);
              publishingDateFormat = DateFormat.LONG;
            } catch(Exception ex2) { }
          }
        }

        if(parsedDate == null)
          log.warn("Could not parse string {} to java.util.date for Locale {}", string, Localization.getLanguageLocale());
        else {
          log.debug("Parsed date: " + parsedDate);
          return DateConvertUtils.asLocalDate(parsedDate);
        }
      } catch(Exception ex) { log.warn("Could not parse string {} to java.util.date for Locale {}", string, Localization.getLanguageLocale()); }

      return LocalDate.now();
    }
  };


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
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleTitle);
  }

  protected void seriesTitleSubTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleSubTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSubTitle);
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

  protected void seriesTitleOnlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSeriesTitleOnlineAddress.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleOnlineAddress);
  }

  protected void seriesTitleNotesChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarSeriesTitleNotes.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleNotes);
  }


  protected EntityListener referenceListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.ReferenceBaseTitleColumnName))
        titleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
        subTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
        abstractChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceTableOfContentsColumnName))
        tableOfContentsChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceIsbnOrIssnColumnName))
        isbnOrIssnChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseOnlineAddressColumnName))
        onlineAddressChanged((String) previousValue, (String) newValue);
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

  protected void titleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitle);
  }

  protected void subTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSubTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceSubTitle);
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

  protected void isbnOrIssnChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldIsbnOrIssn.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceIsbnOrIssn);
  }

  protected void onlineAddressChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldOnlineAddress.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceOnlineAddress);
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

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

}
