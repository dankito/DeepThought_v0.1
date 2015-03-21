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
  protected Pane paneTitle;
  @FXML
  protected TextField txtfldTitle;
  @FXML
  protected ComboBox<SeriesTitleCategory> cmbxSeriesTitleCategory;
  @FXML
  protected NewOrEditButton btnNewOrEditSeriesTitleCategory;
  @FXML
  protected Button btnChooseFieldsToShow;

  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;

  protected ContextHelpControl contextHelpControl;

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


  protected SeriesTitlePersonsControl seriesTitlePersonsControl;

  @FXML
  protected Pane paneFirstAndLastDayOfPublication;
  @FXML
  protected DatePicker dtpckFirstDayOfPublication;
  @FXML
  protected DatePicker dtpckLastDayOfPublication;
  @FXML
  protected Pane panePublisher;
  @FXML
  protected ComboBox<Publisher> cmbxPublisher;
  @FXML
  protected NewOrEditButton btnNewOrEditPublisher;

  @FXML
  protected Pane paneAbbreviation;
  @FXML
  protected TextField txtfldStandardAbbreviation;
  @FXML
  protected TextField txtfldUserAbbreviation1;
  @FXML
  protected TextField txtfldUserAbbreviation2;

  @FXML
  protected Pane paneOnlineAddress;
  @FXML
  protected TextField txtfldOnlineAddress;
  @FXML
  protected DatePicker dtpckLastAccess;

  @FXML
  protected TitledPane ttldpnSerialParts;

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
    txtfldTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseTitle);
      updateWindowTitle(newValue);
    });

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
    paneTitle.getChildren().add(4, btnNewOrEditSeriesTitleCategory);

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

    seriesTitlePersonsControl = new SeriesTitlePersonsControl();
    seriesTitlePersonsControl.setExpanded(true);
    seriesTitlePersonsControl.setPersonAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBasePersons));
    seriesTitlePersonsControl.setPersonRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBasePersons));
    contentPane.getChildren().add(5, seriesTitlePersonsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneFirstAndLastDayOfPublication);
    dtpckFirstDayOfPublication.setConverter(localeDateStringConverter);
    dtpckFirstDayOfPublication.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication));
    dtpckLastDayOfPublication.setConverter(localeDateStringConverter);
    dtpckLastDayOfPublication.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(panePublisher);
    resetComboBoxPublisherItems();
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
    btnNewOrEditPublisher.setOnAction(event -> handleButtonNewOrEditPublisherAction(event));
    btnNewOrEditPublisher.setOnNewMenuItemEventActionHandler(event -> handleMenuItemNewPublisherAction(event));
    panePublisher.getChildren().add(btnNewOrEditPublisher);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneAbbreviation);
    txtfldStandardAbbreviation.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleStandardAbbreviation));
    txtfldUserAbbreviation1.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation1));
    txtfldUserAbbreviation2.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation2));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneOnlineAddress);
    txtfldOnlineAddress.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseOnlineAddress));
    dtpckLastAccess.setConverter(localeDateStringConverter);
    dtpckLastAccess.valueProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseLastAccess));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnNotes);
    txtarNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.ReferenceBaseNotes));

//    clmnFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
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
    btnChooseFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

    paneSubTitle.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getSubTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneTitleSupplement.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getTitleSupplement()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnAbstract.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getAbstract()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnTableOfContents.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getTableOfContents()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneFirstAndLastDayOfPublication.setVisible(seriesTitle.getFirstDayOfPublication() != null || seriesTitle.getLastDayOfPublication() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    panePublisher.setVisible(seriesTitle.getPublisher() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    paneAbbreviation.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getStandardAbbreviation()) || StringUtils.isNotNullOrEmpty(seriesTitle.getUserAbbreviation1()) ||
        StringUtils.isNotNullOrEmpty(seriesTitle.getUserAbbreviation2()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    paneOnlineAddress.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getOnlineAddress()) || seriesTitle.getLastAccessDate() != null || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    ttldpnNotes.setVisible(StringUtils.isNotNullOrEmpty(seriesTitle.getNotes()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnFiles.setVisible(seriesTitle.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
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

  protected ChangeListener<Publisher> cmbxPublisherValueChangeListener = new ChangeListener<Publisher>() {
    @Override
    public void changed(ObservableValue<? extends Publisher> observable, Publisher oldValue, Publisher newValue) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.SeriesTitlePublisher);

      if(newValue == null || newValue == Empty.Publisher) {
        btnNewOrEditPublisher.setButtonFunction(NewOrEditButton.ButtonFunction.New);
        btnNewOrEditPublisher.setShowNewMenuItem(false);
      }
      else {
        btnNewOrEditPublisher.setButtonFunction(NewOrEditButton.ButtonFunction.Edit);
        btnNewOrEditPublisher.setShowNewMenuItem(true);
      }
    }
  };

  protected void resetComboBoxSeriesTitleCategoryItems() {
    cmbxSeriesTitleCategory.getItems().clear();
    cmbxSeriesTitleCategory.getItems().add(Empty.SeriesCategory);
    cmbxSeriesTitleCategory.getItems().addAll(Application.getDeepThought().getSeriesTitleCategories());
  }

  protected void resetComboBoxPublisherItems() {
    cmbxPublisher.getItems().clear();
    cmbxPublisher.getItems().add(Empty.Publisher);
    cmbxPublisher.getItems().addAll(new TreeSet<Publisher>(Application.getDeepThought().getPublishers()));
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

    if(seriesTitle.isPersisted() == false) { // a new SeriesTitle
      Application.getDeepThought().addSeriesTitle(seriesTitle);
    }

    saveEditedFieldsOnEntry();
    closeDialog();
  }

  @Override
  protected void closeDialog() {
    seriesTitle.removeEntityListener(seriesTitleListener);
    Application.getDeepThought().removeEntityListener(deepThoughtListener);

    super.closeDialog();
  }

  protected void saveEditedFieldsOnEntry() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseTitle)) {
      seriesTitle.setTitle(txtfldTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleSeriesTitleCategory)) {
      if(Empty.SeriesCategory.equals(cmbxSeriesTitleCategory.getValue()))
        seriesTitle.setCategory(null);
      else
        seriesTitle.setCategory(cmbxSeriesTitleCategory.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSeriesTitleCategory);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseSubTitle)) {
      seriesTitle.setSubTitle(txtfldSubTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseSubTitle);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceTitleSupplement)) {
      seriesTitle.setTitleSupplement(txtfldTitleSupplement.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTitleSupplement);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseAbstract)) {
      seriesTitle.setAbstract(txtarAbstract.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseAbstract);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceTableOfContents) || htmledTableOfContents.getHtmlText().equals(seriesTitle.getTableOfContents()) == false) {
      seriesTitle.setTableOfContents(htmledTableOfContents.getHtmlText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceTableOfContents);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBasePersons)) {
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

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBasePersons);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication)) {
      seriesTitle.setFirstDayOfPublication(DateConvertUtils.asUtilDate(dtpckFirstDayOfPublication.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication)) {
      seriesTitle.setLastDayOfPublication(DateConvertUtils.asUtilDate(dtpckLastDayOfPublication.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitlePublisher)) {
      if(Empty.Publisher.equals(cmbxPublisher.getValue()))
        seriesTitle.setPublisher(null);
      else
        seriesTitle.setPublisher(cmbxPublisher.getValue());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitlePublisher);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleStandardAbbreviation)) {
      seriesTitle.setStandardAbbreviation(txtfldStandardAbbreviation.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleStandardAbbreviation);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation1)) {
      seriesTitle.setUserAbbreviation1(txtfldUserAbbreviation1.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation1);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation2)) {
      seriesTitle.setUserAbbreviation2(txtfldUserAbbreviation2.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation2);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseOnlineAddress)) {
      seriesTitle.setOnlineAddress(txtfldOnlineAddress.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseOnlineAddress);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseLastAccess)) {
      seriesTitle.setLastAccessDate(DateConvertUtils.asUtilDate(dtpckLastAccess.getValue()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseLastAccess);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.SeriesTitleSerialParts)) {

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSerialParts);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseNotes)) {
      seriesTitle.setNotes(txtarNotes.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseNotes);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.ReferenceBaseFiles)) {

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseFiles);
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
        saveEditedFieldsOnEntry();
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

    if(paneSubTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneSubTitle, "subtitle");
    if(paneTitleSupplement.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneTitleSupplement, "title.supplement");

    if(ttldpnAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnAbstract, "abstract");
    if(ttldpnTableOfContents.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnTableOfContents, "table.of.contents");

    if(paneFirstAndLastDayOfPublication.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneFirstAndLastDayOfPublication, "first.day.of.publication");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneFirstAndLastDayOfPublication, "last.day.of.publication");
    }
    if(panePublisher.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, panePublisher, "publisher");

    if(paneAbbreviation.isVisible() == false) {
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneAbbreviation, "standard.abbreviation");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneAbbreviation, "user.abbreviation1");
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneAbbreviation, "user.abbreviation2");
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
    if(btnNewOrEditPublisher.getButtonFunction() == NewOrEditButton.ButtonFunction.Edit)
      net.deepthought.controller.Dialogs.showEditPublisherDialog(cmbxPublisher.getValue());
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
    txtfldTitle.requestFocus();

    setSeriesTitleValues(seriesTitle);
    seriesTitle.addEntityListener(seriesTitleListener);
  }

  protected void setSeriesTitleValues(final SeriesTitle seriesTitle) {
    btnApplyChanges.setVisible(seriesTitle.isPersisted());

    txtfldTitle.setText(seriesTitle.getTitle());
    cmbxSeriesTitleCategory.setValue(seriesTitle.getCategory());
    txtfldSubTitle.setText(seriesTitle.getSubTitle());
    txtfldTitleSupplement.setText(seriesTitle.getTitleSupplement());

    txtarAbstract.setText(seriesTitle.getAbstract());
    htmledTableOfContents.setHtmlText(seriesTitle.getTableOfContents());

    seriesTitlePersonsControl.setSeries(seriesTitle);

    dtpckFirstDayOfPublication.setValue(DateConvertUtils.asLocalDate(seriesTitle.getFirstDayOfPublication()));
    dtpckLastDayOfPublication.setValue(DateConvertUtils.asLocalDate(seriesTitle.getLastDayOfPublication()));

    txtfldStandardAbbreviation.setText(seriesTitle.getStandardAbbreviation());
    txtfldUserAbbreviation1.setText(seriesTitle.getUserAbbreviation1());
    txtfldUserAbbreviation2.setText(seriesTitle.getUserAbbreviation2());

    txtfldOnlineAddress.setText(seriesTitle.getOnlineAddress());
    dtpckLastAccess.setValue(DateConvertUtils.asLocalDate(seriesTitle.getLastAccessDate()));

    txtarNotes.setText(seriesTitle.getNotes());

//    trtblvwFiles.setRoot(new FileRootTreeItem(seriesTitle));

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
        titleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleCategoryJoinColumnName))
      seriesTitleceCategoryChanged((SeriesTitleCategory) previousValue, (SeriesTitleCategory) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseSubTitleColumnName))
        subTitleChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleTitleSupplementColumnName))
        titleSupplementChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.ReferenceBaseAbstractColumnName))
        abstractChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleTableOfContentsColumnName))
        tableOfContentsChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleFirstDayOfPublicationColumnName))
        firstDayOfPublicationChanged((Date) previousValue, (Date) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleLastDayOfPublicationColumnName))
        lastDayOfPublicationChanged((Date) previousValue, (Date) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitlePublisherJoinColumnName))
        publisherChanged((Publisher) previousValue, (Publisher) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleStandardAbbreviationColumnName))
        standardAbbreviationChanged((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleUserAbbreviation1ColumnName))
        userAbbreviation1Changed((String) previousValue, (String) newValue);
      else if(propertyName.equals(TableConfig.SeriesTitleUserAbbreviation2ColumnName))
        userAbbreviation2Changed((String) previousValue, (String) newValue);
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


  protected void titleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseTitle);
  }

  protected void seriesTitleceCategoryChanged(SeriesTitleCategory previousValue, SeriesTitleCategory newValue) {
    // TODO: if current value != previousValue, ask User what to do?
//    cmbxSeriesTitleCategory.valueProperty().removeListener(cmbxSeriesTitleCategoryValueChangeListener);
    if(newValue != null)
      cmbxSeriesTitleCategory.setValue(newValue);
    else
      cmbxSeriesTitleCategory.setValue(Empty.SeriesCategory);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleSeriesTitleCategory);
//    cmbxSeriesTitleCategory.valueProperty().addListener(cmbxSeriesTitleCategoryValueChangeListener);
  }

  protected void subTitleChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldSubTitle.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseSubTitle);
  }

  protected void titleSupplementChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldTitleSupplement.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleTitleSupplement);
  }

  protected void abstractChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtarAbstract.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.ReferenceBaseAbstract);
  }

  protected void tableOfContentsChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    htmledTableOfContents.setHtmlText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleTableOfContents);
  }

  protected void firstDayOfPublicationChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckFirstDayOfPublication.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleFirstDayOfPublication);
  }

  protected void lastDayOfPublicationChanged(Date previousValue, Date newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    dtpckLastDayOfPublication.setValue(DateConvertUtils.asLocalDate(newValue));
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleLastDayOfPublication);
  }

  protected void publisherChanged(Publisher previousValue, Publisher newValue) {
    // TODO: if current value != previousValue, ask User what to do?
//    cmbxPublisher.valueProperty().removeListener(cmbxPublisherValueChangeListener);
    if(newValue != null)
      cmbxPublisher.setValue(newValue);
    else
      cmbxPublisher.setValue(Empty.Publisher);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitlePublisher);
//    cmbxPublisher.valueProperty().addListener(cmbxPublisherValueChangeListener);
  }

  protected void standardAbbreviationChanged(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldStandardAbbreviation.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleStandardAbbreviation);
  }

  protected void userAbbreviation1Changed(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldUserAbbreviation1.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation1);
  }

  protected void userAbbreviation2Changed(String previousValue, String newValue) {
    // TODO: if current value != previousValue, ask User what to do?
    txtfldUserAbbreviation2.setText(newValue);
    fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.SeriesTitleUserAbbreviation2);
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
      if(collection == Application.getDeepThought().getSeriesTitleCategories()) {
        resetComboBoxSeriesTitleCategoryItems();
      }
      else if(collection == Application.getDeepThought().getPublishers()) {
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
      else if(updatedEntity instanceof Publisher && updatedEntity.equals(cmbxPublisher.getValue())) {
        cmbxPublisher.valueProperty().removeListener(cmbxPublisherValueChangeListener);
        cmbxPublisher.setValue(Empty.Publisher);
        cmbxPublisher.setValue((Publisher) updatedEntity);
        cmbxPublisher.valueProperty().addListener(cmbxPublisherValueChangeListener);
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == Application.getDeepThought().getSeriesTitleCategories()) {
        resetComboBoxSeriesTitleCategoryItems();
      }
      else if(collection == Application.getDeepThought().getPublishers()) {
        resetComboBoxPublisherItems();
      }
    }
  };

}
