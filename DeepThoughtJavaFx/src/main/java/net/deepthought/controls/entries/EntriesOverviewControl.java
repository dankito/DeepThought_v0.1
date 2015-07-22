package net.deepthought.controls.entries;

import net.deepthought.Application;
import net.deepthought.MainWindowController;
import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.IMainWindowControl;
import net.deepthought.controls.LazyLoadingObservableList;
import net.deepthought.controls.person.PersonLabel;
import net.deepthought.controls.reference.EntryReferenceBaseLabel;
import net.deepthought.controls.reference.EntryReferenceControl;
import net.deepthought.controls.tag.EntryTagsControl;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.model.ui.EntriesWithoutTagsSystemTag;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.search.FilterEntriesSearch;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.StringUtils;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntriesOverviewControl extends SplitPane implements IMainWindowControl {

  private final static Logger log = LoggerFactory.getLogger(EntriesOverviewControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected Collection<Entry> unfilteredCurrentEntriesToShow = new ArrayList<>();

//  protected ObservableList<Entry> tableViewEntriesItems = null;
  protected LazyLoadingObservableList<Entry> tableViewEntriesItems = null;
  protected FilteredList<Entry> filteredEntries = null;
  protected SortedList<Entry> sortedFilteredEntries = null;
//  protected Entry selectedEntryInTableViewEntries = null;

  protected FilterEntriesSearch filterEntriesSearch = null;


  protected MainWindowController mainWindowController;


  @FXML
  protected SplitPane splpnEntries;

  @FXML
  protected HBox hboxEntriesBar;
  @FXML
  protected CustomTextField txtfldEntriesQuickFilter;
  @FXML
  ToggleButton tglbtnEntriesQuickFilterAbstract;
  @FXML
  ToggleButton tglbtnEntriesQuickFilterContent;
  @FXML
  protected Button btnRemoveSelectedEntries;

  @FXML
  protected TableView<Entry> tblvwEntries;
  @FXML
  protected TableColumn<Entry, Long> clmnId;
  @FXML
  protected TableColumn<Entry, String> clmnReferencePreview;
  @FXML
  protected TableColumn<Entry, String> clmnEntryPreview;
  @FXML
  protected TableColumn<Entry, String> clmnTags;
  @FXML
  protected TableColumn<Entry, String> clmnCreated;
  @FXML
  protected TableColumn<Entry, String> clmnModified;


  @FXML
  protected ScrollPane pnQuickEditEntryScrollPane;
  @FXML
  protected Pane pnQuickEditEntry;
  @FXML
  protected TextField txtfldEntryAbstract;

  protected EntryTagsControl currentEditedEntryTagsControl = null;

  @FXML
  ScrollPane pnReferenceAndPersonsScrollPane;
  @FXML
  Pane pnReferenceAndPersons;
  @FXML
  Pane pnReference;
  @FXML
  Label lblReference;
  @FXML
  Pane pnSelectedReference;
  @FXML
  TextField txtfldReferenceIndication;
//  @FXML
//  ScrollPane pnPersonsScrollPane;
  @FXML
  Pane pnPersons;
  @FXML
  Label lblPersons;
  @FXML
  Pane pnSelectedPersons;

  @FXML
  protected HTMLEditor htmledEntryContent;



  public EntriesOverviewControl(MainWindowController mainWindowController) {
    this.mainWindowController = mainWindowController;
    deepThought = Application.getDeepThought();

    if(FXUtils.loadControl(this, "EntriesOverviewControl"))
      setupControl();

//    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/EntriesOverviewControl.fxml"));
//    fxmlLoader.setRoot(this);
//    fxmlLoader.setController(this);
//    fxmlLoader.setResources(Localization.getStringsResourceBundle());
//
//    try {
//      fxmlLoader.load();
//      setupControl();
//
//      if(deepThought != null)
//        deepThought.addEntityListener(deepThoughtListener);
//    } catch (IOException ex) {
//      log.error("Could not load EntriesOverviewControl", ex);
//    }
  }

  public void deepThoughtChanged(DeepThought newDeepThought) {
    this.deepThought = newDeepThought;

    if(newDeepThought != null) {
      if (deepThought.getSettings().getLastViewedEntry() != null) {
        // no, don't set Entry, as TableView then iterates through all Entries in Table to find selected Entry -> would eventually load a lot of Entries from Database
//        tblvwEntries.getSelectionModel().select(deepThought.getSettings().getLastViewedEntry());
        selectedEntryChanged(deepThought.getSettings().getLastViewedEntry());
      }
    }
  }

  public void clearData() {
    tableViewEntriesItems.clear();
    currentEditedEntryTagsControl.setEntry(null);
  }

  protected void setupControl() {
    // replace normal TextField txtfldEntriesQuickFilter with a SearchTextField (with a cross to clear selection)
    hboxEntriesBar.getChildren().remove(txtfldEntriesQuickFilter);
    txtfldEntriesQuickFilter = (CustomTextField) TextFields.createClearableTextField();
    hboxEntriesBar.getChildren().add(1, txtfldEntriesQuickFilter);
    HBox.setHgrow(txtfldEntriesQuickFilter, Priority.ALWAYS);
    JavaFxLocalization.bindTextInputControlPromptText(txtfldEntriesQuickFilter, "quickly.filter.entries");
    txtfldEntriesQuickFilter.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        filterEntries();
      }
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(tglbtnEntriesQuickFilterAbstract);
    JavaFxLocalization.bindControlToolTip(tglbtnEntriesQuickFilterAbstract, "quickly.filter.entries.abstract.tool.tip");
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(tglbtnEntriesQuickFilterContent);
    JavaFxLocalization.bindControlToolTip(tglbtnEntriesQuickFilterContent, "quickly.filter.entries.content.tool.tip");

    tableViewEntriesItems = new LazyLoadingObservableList<>();
//    filteredEntries = new FilteredList<>(tblvwEntries.getItems(), entry -> true);
//    sortedFilteredEntries = new SortedList<Entry>(tblvwEntries.getItems(), entriesComparator);
    tblvwEntries.setItems(tableViewEntriesItems);

//    tableViewEntriesItems = tblvwEntries.getItems();
//    filteredEntries = new FilteredList<>(tableViewEntriesItems, entry -> true);
//    sortedFilteredEntries = new SortedList<Entry>(filteredEntries, entriesComparator);
//    tblvwEntries.setItems(sortedFilteredEntries);

//    sortedFilteredEntries.comparatorProperty().bind(tblvwEntries.comparatorProperty());

    tblvwEntries.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tblvwEntries.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Entry>() {
      @Override
      public void changed(ObservableValue<? extends Entry> observable, Entry oldValue, Entry newValue) {
        if (oldValue != null)
          oldValue.removeEntityListener(currentlyEditedEntryListener);

        selectedEntryChanged(newValue);
      }
    });

    clmnId.setCellValueFactory(new PropertyValueFactory<Entry, Long>("entryIndex"));
    clmnReferencePreview.setCellFactory((param) -> {
      return new EntryReferencePreviewTableCell();
    });
    clmnEntryPreview.setCellFactory((param) -> {
      return new EntryPreviewTableCell();
    });
//    clmnEntryPreview.setCellValueFactory(new PropertyValueFactory<Entry, String>("preview"));
    clmnTags.setCellFactory((param) -> {
      return new EntryTagsTableCell();
    });
//    clmnTags.setCellValueFactory(new PropertyValueFactory<Entry, String>("tagsPreview"));
    clmnCreated.setCellFactory((param) -> {
      return new EntryCreatedTableCell();
    });
    clmnModified.setCellFactory((param) -> {
      return new EntryModifiedTableCell();
    });

    setupQuickEditEntrySection();
  }

  protected void setupQuickEditEntrySection() {
//    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(pnQuickEditEntry);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(pnQuickEditEntryScrollPane);

    txtfldEntryAbstract.textProperty().addListener((observable, oldValue, newValue) -> {
      Entry selectedEntry = tblvwEntries.getSelectionModel().getSelectedItem();
      if (selectedEntry != null)
        selectedEntry.setAbstract(txtfldEntryAbstract.getText());
    });

    FXUtils.addHtmlEditorTextChangedListener(htmledEntryContent, event -> {
      Entry selectedEntry = tblvwEntries.getSelectionModel().getSelectedItem();
      if (selectedEntry != null)
        selectedEntry.setContent(htmledEntryContent.getHtmlText());
    });

    currentEditedEntryTagsControl = new EntryTagsControl();
    currentEditedEntryTagsControl.setTagAddedEventHandler(event -> event.getEntry().addTag(event.getTag()));
    currentEditedEntryTagsControl.setTagRemovedEventHandler(event -> event.getEntry().removeTag(event.getTag()));
    VBox.setMargin(currentEditedEntryTagsControl, new Insets(6, 0, 6, 0));
    pnQuickEditEntry.getChildren().add(1, currentEditedEntryTagsControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(pnReferenceAndPersonsScrollPane);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(pnReference);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(pnPersons);

    this.widthProperty().addListener((observable, oldValue, newValue) -> pnReferenceAndPersonsScrollPane.setPrefWidth(this.getWidth()));

    final ChangeListener<? super Number> fixControlsWidthListener = (observable, oldValue, newValue) -> {
      if(observable instanceof ReadOnlyProperty) { // set control's width to a fixed value. Otherwise dynamic layout would shrink them
        Object bean = ((ReadOnlyProperty)observable).getBean();
        if(bean instanceof Region) {
          ((Region)bean).setMinWidth(newValue.doubleValue());
          ((Region)bean).setMaxWidth(newValue.doubleValue());
        }
      }
    };
    lblReference.widthProperty().addListener(fixControlsWidthListener);
    txtfldReferenceIndication.widthProperty().addListener(fixControlsWidthListener);
    lblPersons.widthProperty().addListener(fixControlsWidthListener);
  }

  public void showPaneQuickEditEntryChanged(boolean showPaneQuickEditEntry) {
//    pnQuickEditEntry.setVisible(showPaneQuickEditEntry);
    pnQuickEditEntryScrollPane.setVisible(showPaneQuickEditEntry);

    if(showPaneQuickEditEntry) {
//      if(splpnEntries.getItems().contains(pnQuickEditEntry) == false) {
      if(splpnEntries.getItems().contains(pnQuickEditEntryScrollPane) == false) {
//        splpnEntries.getItems().add(pnQuickEditEntry);
        splpnEntries.getItems().add(pnQuickEditEntryScrollPane);
        splpnEntries.setDividerPositions(0.5);
      }
    }
    else {
//      splpnEntries.getItems().remove(pnQuickEditEntry);
      splpnEntries.getItems().remove(pnQuickEditEntryScrollPane);
      splpnEntries.setDividerPosition(0, 1);
      try {
        FXUtils.showSplitPaneDividers(splpnEntries, false);
        splpnEntries.getDividers().remove(0);
//        splpnEntries.getDividers().clear();
//        splpnEntries.getDividers().removeAll(splpnEntries.getDividers());
//        for(SplitPane.Divider divider : new ArrayList<>(splpnEntries.getDividers())) {
//          splpnEntries.getDividers().remove(divider);
//        }
      } catch(Exception ex) { } // throws an exception but does exactly what i want
    }
  }


  public void showEntries(Collection<Entry> entries) {
    this.unfilteredCurrentEntriesToShow = entries;

//    selectedEntryChanged(null);
    tblvwEntries.getSelectionModel().clearSelection();

//    tableViewEntriesItems.clear();
//    tableViewEntriesItems.addAll(entries);
    tableViewEntriesItems.setUnderlyingCollection(entries);
  }

  protected void selectedEntryChanged(final Entry selectedEntry) {
    if(Platform.isFxApplicationThread())
      selectedEntryChangedOnUiThread(selectedEntry);
    else
      Platform.runLater(() -> selectedEntryChangedOnUiThread(selectedEntry));
  }

  protected void selectedEntryChangedOnUiThread(Entry selectedEntry) {
    log.debug("Selected Entry changed to {}", selectedEntry);

    if(deepThought.getSettings().getLastViewedEntry() != null)
      deepThought.getSettings().getLastViewedEntry().removeEntityListener(currentlyEditedEntryListener);

    deepThought.getSettings().setLastViewedEntry(selectedEntry);

    btnRemoveSelectedEntries.setDisable(selectedEntry == null);
    pnQuickEditEntry.setDisable(selectedEntry == null);
    currentEditedEntryTagsControl.setEntry(selectedEntry);

    if(selectedEntry != null) {
      selectedEntry.addEntityListener(currentlyEditedEntryListener);
      txtfldEntryAbstract.setText(selectedEntry.getAbstractAsPlainText());
      htmledEntryContent.setHtmlText(selectedEntry.getContent());
      txtfldEntryAbstract.selectAll();

      setPaneReferenceAndPersons(selectedEntry);
    }
    else {
      txtfldEntryAbstract.setText("");
      htmledEntryContent.setHtmlText("");
      pnReferenceAndPersonsScrollPane.setVisible(false);
    }
  }

  protected void setPaneReferenceAndPersons(Entry selectedEntry) {
    pnReferenceAndPersonsScrollPane.setVisible(selectedEntry.hasPersonsOrIsAReferenceSet());
    pnReference.setVisible(selectedEntry.isAReferenceSet());
    pnPersons.setVisible(selectedEntry.hasPersons());

    pnSelectedReference.getChildren().clear();
    txtfldReferenceIndication.setText("");
    pnSelectedPersons.getChildren().clear();

    if(selectedEntry.isAReferenceSet()) {
      if(selectedEntry.hasPersons())
        pnReference.setMaxWidth(this.getWidth() * 2 / 3);
      else
        pnReference.setMaxWidth(this.getWidth() - 30);
      pnSelectedReference.getChildren().add(new EntryReferenceBaseLabel(selectedEntry.getLowestReferenceBase(), null));
    }
    txtfldReferenceIndication.setText(selectedEntry.getIndication());

    if(selectedEntry.hasPersons()) {
      pnReferenceAndPersonsScrollPane.setMinHeight(55);
      for(Person person : selectedEntry.getPersons()) {
        final PersonLabel label = new PersonLabel(person);
        pnSelectedPersons.getChildren().add(label);
        HBox.setMargin(label, new Insets(0, 6, 0, 0));
      }
    }
    else {
      pnReferenceAndPersonsScrollPane.setMinHeight(40);
    }
  }

  @FXML
  public void handleToggleButtonEntriesQuickFilterOptionsAction(ActionEvent actionEvent) {
    filterEntries();
  }

  protected void addEntryToSelectedCategory(Entry newEntry) {
    if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Categories) {
      Category selectedCategory = deepThought.getSettings().getLastViewedCategory();
      if(selectedCategory != null && selectedCategory != deepThought.getTopLevelCategory()) {
        selectedCategory.addEntry(newEntry);
      }
    }
  }

//  protected void addAndSelectEntry(Entry newEntry) {
//    tableViewEntriesItems.add(0, newEntry);
//    selectEntry(newEntry);
//  }
//
//  private void selectEntry(Entry entry) {
//    tblvwEntries.getSelectionModel().select(entry);
//    deepThought.getSettings().setLastViewedEntry(entry); // TODO: can this ever be not already set to this Entry?
//
//    txtfldEntryAbstract.requestFocus();
//  }

  @FXML
  public void handleButtonAddEntryAction(ActionEvent actionEvent) {
    showEditEntryDialog(new Entry());
  }

  @FXML
  public void handleButtonRemoveSelectedEntriesAction(ActionEvent actionEvent) {
    removeSelectedEntries();
  }

  protected void removeSelectedEntries() {
    ObservableList<Entry> selectedItems = tblvwEntries.getSelectionModel().getSelectedItems();
    for(Entry selectedEntry : selectedItems) {
      if(selectedEntry instanceof Entry)
        deepThought.removeEntry((Entry)selectedEntry);
    }

    tableViewEntriesItems.removeAll(selectedItems);
  }

  @FXML
  public void handleTableViewEntriesOverviewMouseClickedAction(MouseEvent mouseEvent) {
    if(mouseEvent.getClickCount() == 2 && mouseEvent.getButton() == MouseButton.PRIMARY) {
      editCurrentlySelectedEntry();
    }
  }

  @FXML
  public void handleEditCurrentSelectedEntryAction(ActionEvent actionEvent) {
    editCurrentlySelectedEntry();
  }

  protected void editCurrentlySelectedEntry() {
    if(deepThought.getSettings().getLastViewedEntry() instanceof Entry)
      showEditEntryDialog(deepThought.getSettings().getLastViewedEntry());
  }

  protected void showEditEntryDialog(final Entry entry) {
    net.deepthought.controller.Dialogs.showEditEntryDialog(entry, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        if(controller.getDialogResult() == DialogResult.Ok) {
//          if (tableViewEntriesItems.contains(entry) == false) { // a new Entry
//            addEntryToSelectedCategory(entry);
//            addAndSelectEntry(entry);
//          } else {
//            selectEntry(entry);
//          }
        }
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });

//    try {
//      FXMLLoader loader = new FXMLLoader();
//      loader.setResources(Localization.getStringsResourceBundle());
//      loader.setLocation(getClass().getClassLoader().getResource("dialogs/EditEntryDialog.fxml"));
//      Parent parent = loader.load();
//
//      // Create the dialog Stage.
//      Stage dialogStage = new Stage();
////      dialogStage.setTitle(Localization.getLocalizedStringForResourceKey("edit.entry"));
//      dialogStage.initModality(Modality.NONE);
////      windowStage.initOwner(stage);
//      Scene scene = new Scene(parent);
//      dialogStage.setScene(scene);
//
//      // Set the person into the controller.
//      EditEntryDialogController controller = loader.getController();
//      controller.setWindowStage(dialogStage);
//      controller.setEntry(entry);
//
//      controller.setListener(new ChildWindowsControllerListener() {
//        @Override
//        public void windowClosing(Stage stage, ChildWindowsController controller) {
//          if(controller.getDialogResult() == DialogResult.Ok) {
//            if(entry.getId() == null) { // a new Entry
//              deepThought.addEntry(entry);
//              addEntryToSelectedCategory(entry);
//              addAndSelectEntry(entry);
//            }
//            else {
//              selectEntry(entry);
//            }
//          }
//        }
//
//        @Override
//        public void windowClosed(Stage stage, ChildWindowsController controller) {
//          removeClosedChildWindow(stage);
//        }
//      });
//
//      addOpenedChildWindow(dialogStage);
//      dialogStage.show();
//    } catch(Exception ex) {
//      log.error("Could not load / show dialog", ex);
//    }
  }


  protected EntityListener currentlyEditedEntryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.EntryTitleColumnName)) {
        txtfldEntryAbstract.setText(((Entry) entity).getAbstract());
      }
      else if(propertyName.equals(TableConfig.EntryContentColumnName)) {
        htmledEntryContent.setHtmlText(((Entry) entity).getContent());
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


  protected void filterEntries() {
    if(filterEntriesSearch != null && filterEntriesSearch.isCompleted() == false)
      filterEntriesSearch.interrupt();

    String filterTerm = txtfldEntriesQuickFilter.getText();
    Tag selectedTag = deepThought.getSettings().getLastViewedTag();

    if(StringUtils.isNullOrEmpty(filterTerm)) // no filter applied -> show all entries
      tableViewEntriesItems.setUnderlyingCollection(unfilteredCurrentEntriesToShow);
    else {
      filterEntriesSearch = new FilterEntriesSearch(txtfldEntriesQuickFilter.getText(), tglbtnEntriesQuickFilterContent.isSelected(), tglbtnEntriesQuickFilterAbstract.isSelected(),
          /*unfilteredCurrentEntriesToShow,*/ (results) -> {
        Platform.runLater(() -> {
          tblvwEntries.getSelectionModel().clearSelection();
          tableViewEntriesItems.setUnderlyingCollection(results);
        });
      });

      if(selectedTag instanceof EntriesWithoutTagsSystemTag)
        filterEntriesSearch.setFilterOnlyEntriesWithoutTags(true);
      else if(selectedTag instanceof SystemTag == false) {
        filterEntriesSearch.setEntriesToFilter(unfilteredCurrentEntriesToShow);
      }

      Application.getSearchEngine().filterEntries(filterEntriesSearch);
    }
  }


}
