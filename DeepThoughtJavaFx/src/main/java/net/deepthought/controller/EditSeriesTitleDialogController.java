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
import net.deepthought.controls.person.SeriesTitlePersonsControl;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Publisher;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.enums.PersonRole;
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
public class EditSeriesTitleDialogController extends ChildWindowsController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(EditSeriesTitleDialogController.class);


  protected SeriesTitle seriesTitle = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();


  @FXML
  protected BorderPane dialogPane;

  @FXML
  protected Button btnApplyChanges;

  @FXML
  protected Pane contentPane;

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
  protected TitledPane ttldpnSerialParts;

  @FXML
  protected TitledPane ttldpnSeriesTitleNotes;
  @FXML
  protected TextArea txtarSeriesTitleNotes;


  @FXML
  protected TitledPane ttldpnSeriesTitleFiles;
  @FXML
  protected FlowPane flpnFilesSeriesTitlePreview;
  @FXML
  protected TreeTableView<FileLink> trtblvwSeriesTitleFiles;
  @FXML
  protected TreeTableColumn<FileLink, String> clmnSeriesTitleFile;


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
    contentPane.getChildren().add(5, seriesTitlePersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(seriesTitlePersonsControl);
    seriesTitlePersonsControl.setVisible(false);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitleFirstAndLastDayOfPublication);
    dtpckSeriesTitleFirstDayOfPublication.setConverter(localeDateStringConverter);
    dtpckSeriesTitleFirstDayOfPublication.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication));
    dtpckSeriesTitleLastDayOfPublication.setConverter(localeDateStringConverter);
    dtpckSeriesTitleLastDayOfPublication.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSeriesTitlePublisher);
    resetComboBoxSeriesTitlePublisherItems();
    cmbxSeriesTitlePublisher.valueProperty().addListener(cmbxSeriesTitlePublisherValueChangeListener);

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
    btnSeriesTitleNewOrEditPublisher.setOnAction(event -> handleButtonNewOrEditPublisherAction(event));
    btnSeriesTitleNewOrEditPublisher.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewPublisherAction(event));
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

//    clmnSeriesTitleFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
//      @Override
//      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
//        return new FileTreeTableCell(seriesTitle);
//      }
//    });

    contextHelpControl = new ContextHelpControl("context.help.series.title.");
    dialogPane.setRight(contextHelpControl);
//    contextHelpControl.showContextHelpForResourceKey("default");

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(contextHelpControl);
    contextHelpControl.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());

    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowHideContextHelp.setGraphic(new ImageView(Constants.ContextHelpIconPath));
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


  @FXML
  public void handleButtonApplyAction(ActionEvent actionEvent) {
    saveEditedFieldsOnSeriesTitle();
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Cancel);
    closeDialog();
  }

  @FXML
  public void handleButtonOkAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Ok);

    if(seriesTitle.isPersisted() == false) { // a new SeriesTitle
      Application.getDeepThought().addSeriesTitle(seriesTitle);
    }

    saveEditedFieldsOnSeriesTitle();
    closeDialog();
  }

  @Override
  protected void closeDialog() {
    seriesTitle.removeEntityListener(seriesTitleListener);
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
        saveEditedFieldsOnSeriesTitle();
        closeDialog();
      }
      else
        closeDialog();
    }
  }


  public void handleButtonNewOrEditSeriesTitleCategoryAction(ActionEvent event) {
//    if(btnNewOrEditSeriesTitleCategory.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
//      net.deepthought.controller.Dialogs.showEditSeriesTitleCategoryDialog(cmbxSeriesTitleCategory.getValue());
//    else
//      createNewSeriesTitleCategory();
  }

  public void handleButtonChooseFieldsToShowAction(ActionEvent event) {
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

  protected void createHiddenFieldMenuItem(ContextMenu hiddenFieldsMenu, Node nodeToShowOnClick, String menuItemText) {
    MenuItem titleMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(titleMenuItem, menuItemText);
    hiddenFieldsMenu.getItems().add(titleMenuItem);
    titleMenuItem.setOnAction(event -> nodeToShowOnClick.setVisible(true));
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

  protected void createNewPublisher() {
    final Publisher newPublisher = new Publisher();

    net.deepthought.controller.Dialogs.showEditPublisherDialog(newPublisher, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {

      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {
        if(controller.getDialogResult() == DialogResult.Ok)
          seriesTitle.setPublisher(newPublisher);
      }
    });
  }

  @FXML
  public void handleButtonNewOrEditPublisherAction(ActionEvent event) {
    if(btnSeriesTitleNewOrEditPublisher.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
      net.deepthought.controller.Dialogs.showEditPublisherDialog(cmbxSeriesTitlePublisher.getValue());
    else
      createNewPublisher();
  }

  protected void handleMenuItemNewPublisherAction(NewOrEditButtonMenuActionEvent event) {
    createNewPublisher();
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
//          seriesTitle.addFile(newFile);
//        }
//      }
//    });
  }


  public SeriesTitle getSeriesTitle() {
    return seriesTitle;
  }

  public void setWindowStageAndSeriesTitle(Stage windowStage, SeriesTitle seriesTitle) {
    this.seriesTitle = seriesTitle;
    super.setWindowStage(windowStage);

    updateWindowTitle(seriesTitle.getTitle());
    windowStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        askIfStageShouldBeClosed(event);
      }
    });

    setupControls();
    txtfldSeriesTitleTitle.requestFocus();

    setSeriesTitleValues(seriesTitle);
    seriesTitle.addEntityListener(seriesTitleListener);
  }

  protected void setSeriesTitleValues(final SeriesTitle seriesTitle) {
    btnApplyChanges.setVisible(seriesTitle.isPersisted());

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

    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }

  public boolean hasUnsavedChanges() {
    return fieldsWithUnsavedChanges.size() > 0;
  }

  protected void updateWindowTitle(String seriesTitle) {
    if(this.seriesTitle.isPersisted() == false)
      windowStage.setTitle(Localization.getLocalizedStringForResourceKey("create.series.title", seriesTitle));
    else
      windowStage.setTitle(Localization.getLocalizedStringForResourceKey("edit.series.title", seriesTitle));
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
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == Application.getDeepThought().getSeriesTitleCategories()) {
        resetComboBoxSeriesTitleCategoryItems();
      }
      else if(collection == Application.getDeepThought().getPublishers()) {
        resetComboBoxSeriesTitlePublisherItems();
      }
    }
  };

}
