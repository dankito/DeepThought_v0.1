package net.dankito.deepthought.controls.entries;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controller.Dialogs;
import net.dankito.deepthought.controls.Constants;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.IMainWindowControl;
import net.dankito.deepthought.controls.LazyLoadingObservableList;
import net.dankito.deepthought.controls.html.DeepThoughtFxHtmlEditor;
import net.dankito.deepthought.controls.html.EntryContentHtmlEditorListener;
import net.dankito.deepthought.controls.person.PersonLabel;
import net.dankito.deepthought.controls.reference.EntryReferenceBaseLabel;
import net.dankito.deepthought.controls.tag.EntryTagsControl;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.EntryPersonAssociation;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.model.settings.DeepThoughtSettings;
import net.dankito.deepthought.data.model.settings.enums.SelectedTab;
import net.dankito.deepthought.data.model.ui.EntriesWithoutTagsSystemTag;
import net.dankito.deepthought.data.model.ui.SystemTag;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.search.specific.EntriesSearch;
import net.dankito.deepthought.data.search.ui.EntriesForTag;
import net.dankito.deepthought.data.search.ui.EntriesForTagRetrievedListener;
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.JavaFxLocalization;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntriesOverviewControl extends SplitPane implements IMainWindowControl, ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(EntriesOverviewControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected Collection<Entry> unfilteredCurrentEntriesToShow = new ArrayList<>();

  protected LazyLoadingObservableList<Entry> tableViewEntriesItems = null;

  protected EntriesSearch lastEntriesSearch = null;


  protected MainWindowController mainWindowController;

  protected EntriesForTag entriesForTag;


  @FXML
  protected SplitPane splpnEntries;

  @FXML
  protected HBox hboxEntriesBar;
  @FXML
  protected CustomTextField txtfldSearchEntries;
  @FXML
  ToggleButton tglbtnSearchEntriesAbstract;
  @FXML
  ToggleButton tglbtnSearchEntriesContent;
  @FXML
  protected Button btnRemoveSelectedEntries;
  @FXML
  protected Button btnAddEntry;

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

  protected DeepThoughtFxHtmlEditor htmledEntryContent;



  public EntriesOverviewControl(MainWindowController mainWindowController, EntriesForTag entriesForTag) {
    this.mainWindowController = mainWindowController;
    this.entriesForTag = entriesForTag;
    entriesForTag.addEntriesForTagRetrievedListener(entriesForTagRetrievedListener);

    deepThought = Application.getDeepThought();

    if(FXUtils.loadControl(this, "EntriesOverviewControl")) {
      setupControl();
    }
  }

  public void deepThoughtChanged(DeepThought newDeepThought) {
    this.deepThought = newDeepThought;

    if(newDeepThought != null) {
      DeepThoughtSettings settings = deepThought.getSettings();

      FXUtils.applyColumnSettingsAndListenToChanges(clmnId, settings.getEntriesOverviewIdColumnSettings());
      FXUtils.applyColumnSettingsAndListenToChanges(clmnReferencePreview, settings.getEntriesOverviewReferenceColumnSettings());
      FXUtils.applyColumnSettingsAndListenToChanges(clmnEntryPreview, settings.getEntriesOverviewEntryPreviewColumnSettings());
      FXUtils.applyColumnSettingsAndListenToChanges(clmnTags, settings.getEntriesOverviewTagsColumnSettings());
      FXUtils.applyColumnSettingsAndListenToChanges(clmnCreated, settings.getEntriesOverviewCreatedColumnSettings());
      FXUtils.applyColumnSettingsAndListenToChanges(clmnModified, settings.getEntriesOverviewModifiedColumnSettings());

      if (settings.getLastViewedEntry() != null) {
        // no, don't set Entry, as TableView then iterates through all Entries in Table to find selected Entry -> would eventually load a lot of Entries from Database
//        tblvwEntries.getSelectionModel().select(deepThought.getSettings().getLastViewedEntry());
        selectedEntryChanged(settings.getLastViewedEntry());
      }
    }
  }

  public void clearData() {
    tableViewEntriesItems.clear();
    currentEditedEntryTagsControl.setEntry(null);
  }

  protected void setupControl() {
    // replace normal TextField txtfldSearchEntries with a SearchTextField (with a cross to clear selection)
    hboxEntriesBar.getChildren().remove(txtfldSearchEntries);
    txtfldSearchEntries = (CustomTextField) TextFields.createClearableTextField();
    txtfldSearchEntries.setId("txtfldSearchEntries");
    JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchEntries, "search.entries.prompt.text");
    hboxEntriesBar.getChildren().add(1, txtfldSearchEntries);
    HBox.setHgrow(txtfldSearchEntries, Priority.ALWAYS);
    txtfldSearchEntries.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        searchEntries();
      }
    });
    txtfldSearchEntries.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE)
        txtfldSearchEntries.clear();
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(tglbtnSearchEntriesAbstract);
    JavaFxLocalization.bindControlToolTip(tglbtnSearchEntriesAbstract, "search.entries.abstract.tool.tip");
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(tglbtnSearchEntriesContent);
    JavaFxLocalization.bindControlToolTip(tglbtnSearchEntriesContent, "search.entries.content.tool.tip");

    btnRemoveSelectedEntries.setTextFill(Constants.RemoveEntityButtonTextColor);
    JavaFxLocalization.bindControlToolTip(btnRemoveSelectedEntries, "delete.selected.entries.tool.tip");
    btnAddEntry.setTextFill(Constants.AddEntityButtonTextColor);
    JavaFxLocalization.bindControlToolTip(btnAddEntry, "add.new.entry.tool.tip");

    tableViewEntriesItems = new LazyLoadingObservableList<>();
    tblvwEntries.setItems(tableViewEntriesItems);

    tblvwEntries.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tblvwEntries.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Entry>() {
      @Override
      public void changed(ObservableValue<? extends Entry> observable, Entry oldValue, Entry newValue) {
        if (oldValue != null)
          oldValue.removeEntityListener(currentlyEditedEntryListener);

        selectedEntryChanged(newValue);
      }
    });

    tblvwEntries.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.DELETE /*&& FXUtils.isNoModifierPressed(event)*/) {
        removeSelectedEntries();
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
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(pnQuickEditEntryScrollPane);

    txtfldEntryAbstract.textProperty().addListener(txtfldEntryAbstractChangeListener);

    htmledEntryContent = new DeepThoughtFxHtmlEditor(entryContentListener);
    htmledEntryContent.setMinHeight(250);
    htmledEntryContent.setMaxHeight(FXUtils.SizeMaxValue);
    VBox.setVgrow(htmledEntryContent, Priority.ALWAYS);

    pnQuickEditEntry.getChildren().remove(pnQuickEditEntry.getChildren().size() - 1); // remove HtmlEditor set in JavaFX Scene Builder
    pnQuickEditEntry.getChildren().add(htmledEntryContent);

    currentEditedEntryTagsControl = new EntryTagsControl();
    currentEditedEntryTagsControl.setTagAddedEventHandler(event -> {
      Entry selectedEntry = deepThought.getSettings().getLastViewedEntry();
      if (selectedEntry != null)
        selectedEntry.addTag(event.getTag());
    });
    currentEditedEntryTagsControl.setTagRemovedEventHandler(event -> {
      Entry selectedEntry = deepThought.getSettings().getLastViewedEntry();
      if (selectedEntry != null)
        selectedEntry.removeTag(event.getTag());
    });
    pnQuickEditEntry.getChildren().add(1, currentEditedEntryTagsControl);
    currentEditedEntryTagsControl.setSearchAndSelectTagsControlHeight(190);

    txtfldReferenceIndication.textProperty().addListener(txtfldReferenceIndicationChangeListener);

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

  protected void contentUpdated(String newContent) {
    Entry selectedEntry = deepThought.getSettings().getLastViewedEntry();
    if (selectedEntry != null) // TODO: avoid setting Content just because viewed Entry has changed
      selectedEntry.setContent(newContent);
  }

  public void showPaneQuickEditEntryChanged(boolean showPaneQuickEditEntry) {
    pnQuickEditEntryScrollPane.setVisible(showPaneQuickEditEntry);

    if(showPaneQuickEditEntry) {
      if(splpnEntries.getItems().contains(pnQuickEditEntryScrollPane) == false) {
        splpnEntries.getItems().add(pnQuickEditEntryScrollPane);
      }

      splpnEntries.setDividerPosition(0, deepThought.getSettings().getEntriesOverviewDividerPosition());

      if(splpnEntries.getDividers().size() > 0) {
        splpnEntries.getDividers().get(0).positionProperty().addListener(((observableValue, oldValue, newValue) -> {
          if (deepThought != null) {
            deepThought.getSettings().setEntriesOverviewDividerPosition((double)newValue);
          }
        }));
      }
    }
    else {
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
    FXUtils.runOnUiThread(() -> selectedEntryChangedOnUiThread(selectedEntry));
  }

  protected void selectedEntryChangedOnUiThread(Entry selectedEntry) {
    log.debug("Selected Entry changed to {}", selectedEntry);

    if(deepThought.getSettings().getLastViewedEntry() != null)
      deepThought.getSettings().getLastViewedEntry().removeEntityListener(currentlyEditedEntryListener);

    deepThought.getSettings().setLastViewedEntry(selectedEntry);

    btnRemoveSelectedEntries.setDisable(selectedEntry == null);
    pnQuickEditEntry.setDisable(selectedEntry == null);
    currentEditedEntryTagsControl.setEntry(selectedEntry);
    entryContentListener.setEntry(selectedEntry);

    if(selectedEntry != null) {
      selectedEntry.addEntityListener(currentlyEditedEntryListener);
      showAbstract(selectedEntry);
      txtfldEntryAbstract.positionCaret(0);
      htmledEntryContent.setHtml(selectedEntry.getContent(), true);
      txtfldEntryAbstract.selectAll();

      setPaneReferenceAndPersons(selectedEntry);
    }
    else {
      txtfldEntryAbstract.setText("");
      htmledEntryContent.setHtml("", true);
      pnReferenceAndPersonsScrollPane.setVisible(false);
    }
  }

  protected void showAbstract(Entry entry) {
    txtfldEntryAbstract.textProperty().removeListener(txtfldEntryAbstractChangeListener);

    txtfldEntryAbstract.setText(entry.getAbstractAsPlainText());

    txtfldEntryAbstract.textProperty().addListener(txtfldEntryAbstractChangeListener);
  }

  protected void showReferenceIndication(Entry entry) {
    txtfldReferenceIndication.textProperty().removeListener(txtfldReferenceIndicationChangeListener);

    txtfldReferenceIndication.setText(entry.getIndication());

    txtfldReferenceIndication.textProperty().addListener(txtfldReferenceIndicationChangeListener);
  }

  protected void setPaneReferenceAndPersons(final Entry selectedEntry) {
    pnReferenceAndPersonsScrollPane.setVisible(selectedEntry.hasPersonsOrIsAReferenceSet());
    pnReference.setVisible(selectedEntry.isAReferenceSet());
    pnPersons.setVisible(selectedEntry.hasPersons());

    FXUtils.cleanUpChildrenAndClearPane(pnSelectedReference);
    //txtfldReferenceIndication.setText("");
    FXUtils.cleanUpChildrenAndClearPane(pnSelectedPersons);

    if(selectedEntry.isAReferenceSet()) {
      if(selectedEntry.hasPersons())
        pnReference.setMaxWidth(this.getWidth() * 2 / 3);
      else
        pnReference.setMaxWidth(this.getWidth() - 30);
      pnSelectedReference.getChildren().add(new EntryReferenceBaseLabel(selectedEntry.getLowestReferenceBase(), event -> selectedEntry.clearReferenceBases()));
    }
    showReferenceIndication(selectedEntry);

    if(selectedEntry.hasPersons()) {
      pnReferenceAndPersonsScrollPane.setMinHeight(46);
      for(final Person person : new TreeSet<>(selectedEntry.getPersons())) {
        PersonLabel label = new PersonLabel(person);
        pnSelectedPersons.getChildren().add(label);
        HBox.setMargin(label, new Insets(0, 6, 0, 0));

        label.setOnButtonRemoveItemFromCollectionEventHandler(event -> {
//          pnSelectedPersons.getChildren().remove(label);
          selectedEntry.removePerson(person);
        });
      }
    }
    else {
      pnReferenceAndPersonsScrollPane.setMinHeight(30);
    }
  }

  @FXML
  public void handleToggleButtonSearchEntriesOptionsAction(ActionEvent actionEvent) {
    searchEntries();
  }

  protected void addEntryToSelectedCategory(Entry newEntry) {
    if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Categories) {
      Category selectedCategory = deepThought.getSettings().getLastViewedCategory();
      if(selectedCategory != null && selectedCategory != deepThought.getTopLevelCategory()) {
        newEntry.addCategory(selectedCategory);
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
    List<Entry> selectedItems = new ArrayList<>(tblvwEntries.getSelectionModel().getSelectedItems()); // make a copy as when multiple Entries are selected after removing the first one SelectionModel gets cleared
    for(Entry selectedEntry : selectedItems) {
      deepThought.removeEntry(selectedEntry);
    }
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
    Dialogs.showEditEntryDialog(entry);
  }


  protected EntityListener currentlyEditedEntryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.EntryAbstractColumnName)) {
        showAbstract((Entry)entity);
      }
      else if(propertyName.equals(TableConfig.EntryIndicationColumnName)) {
        if(txtfldReferenceIndication.getText().equals(((Entry) entity).getIndication()) == false) // don't update txtfldReferenceIndication if change has been committed by it
          showReferenceIndication((Entry) entity);
      }
      else if(propertyName.equals(TableConfig.EntryContentColumnName)) {
        if(htmledEntryContent.getHtml().equals(((Entry) entity).getContent()) == false) // don't update Html Control if change has been committed by it
          htmledEntryContent.setHtml(((Entry) entity).getContent(), false);
      }
      else if(propertyName.equals(TableConfig.EntrySeriesTitleJoinColumnName) || propertyName.equals(TableConfig.EntryReferenceJoinColumnName) ||
          propertyName.equals(TableConfig.EntryReferenceSubDivisionJoinColumnName)) {
        setPaneReferenceAndPersons((Entry)entity);
      }
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity instanceof EntryPersonAssociation) {
        setPaneReferenceAndPersons((Entry)collectionHolder);
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity instanceof EntryPersonAssociation) {
        setPaneReferenceAndPersons((Entry)collectionHolder);
      }
    }
  };


  protected void searchEntries() {
    if(lastEntriesSearch != null && lastEntriesSearch.isCompleted() == false)
      lastEntriesSearch.interrupt();

    String searchTerm = txtfldSearchEntries.getText();
    Tag selectedTag = deepThought.getSettings().getLastViewedTag();

    if(StringUtils.isNullOrEmpty(searchTerm)) // TODO: remove this, get all (and sorted) Entries by SearchEngine
      tableViewEntriesItems.setUnderlyingCollection(unfilteredCurrentEntriesToShow);
    else {
      lastEntriesSearch = new EntriesSearch(txtfldSearchEntries.getText(), tglbtnSearchEntriesContent.isSelected(), tglbtnSearchEntriesAbstract.isSelected(), (results) -> {
        FXUtils.runOnUiThread(() -> {
          tblvwEntries.getSelectionModel().clearSelection();
          tableViewEntriesItems.setUnderlyingCollection(results);
        });
      });

      if(selectedTag instanceof EntriesWithoutTagsSystemTag)
        lastEntriesSearch.setSearchOnlyEntriesWithoutTags(true);
      else if(selectedTag instanceof SystemTag == false) {
        lastEntriesSearch.addTagEntriesMustHave(deepThought.getSettings().getLastViewedTag());
      }

      Application.getSearchEngine().searchEntries(lastEntriesSearch);
    }
  }


  protected EntryContentHtmlEditorListener entryContentListener = new EntryContentHtmlEditorListener();

  protected ChangeListener<String> txtfldEntryAbstractChangeListener = new ChangeListener<String>() {
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
      Entry selectedEntry = getSelectedEntry();
      if(selectedEntry != null) {
        selectedEntry.setAbstract(txtfldEntryAbstract.getText());
      }
    }
  };

  protected ChangeListener txtfldReferenceIndicationChangeListener = new ChangeListener() {
    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
      Entry selectedEntry = getSelectedEntry();
      if(selectedEntry != null) {
        selectedEntry.setIndication(txtfldReferenceIndication.getText());
      }
    }
  };

  protected Entry getSelectedEntry() {
    return tblvwEntries.getSelectionModel().getSelectedItem();
//    return deepThought.getSettings().getLastViewedEntry();
  }


  @Override
  public void cleanUp() {
    htmledEntryContent.cleanUp();
    currentEditedEntryTagsControl.cleanUp();
  }


  protected EntriesForTagRetrievedListener entriesForTagRetrievedListener = new EntriesForTagRetrievedListener() {
    @Override
    public void retrievedEntriesForTag(List<Entry> entries) {
      showEntries(entries);
    }
  };

}
