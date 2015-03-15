/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.deepthought;

import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.entries.EntryCreatedTableCell;
import net.deepthought.controls.entries.EntryModifiedTableCell;
import net.deepthought.controls.entries.EntryPreviewTableCell;
import net.deepthought.controls.entries.EntryTagsTableCell;
import net.deepthought.controls.tabcategories.CategoryTreeCell;
import net.deepthought.controls.tabcategories.CategoryTreeItem;
import net.deepthought.controls.tabtags.TagFilterTableCell;
import net.deepthought.controls.tabtags.TagNameTableCell;
import net.deepthought.controls.tag.EntryTagsControl;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;
import net.deepthought.model.AllEntriesSystemTag;
import net.deepthought.model.EntriesWithoutTagsSystemTag;
import net.deepthought.model.SystemTag;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 *
 * @author cdankl
 */
public class MainWindowController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(MainWindowController.class);


  protected Stage _stage = null;

  protected DeepThought deepThought = null;

  protected CategoryTreeItem selectedCategoryTreeItem = null;

  protected ObservableList<Tag> tableViewTagsItems = null;
  protected FilteredList<Tag> filteredTags = null;
  protected SortedList<Tag> sortedFilteredTags = null;
  protected ObservableList<TagFilterTableCell> tagFilterTableCells = FXCollections.observableArrayList();

  protected ObservableSet<Tag> tagsToFilterFor = FXCollections.observableSet();
  protected ObservableSet<Entry> entriesWithFilteredTags = FXCollections.observableSet();

  protected ObservableList<Entry> tableViewEntriesItems = null;
  protected FilteredList<Entry> filteredEntries = null;
  protected SortedList<Entry> sortedFilteredEntries = null;
//  protected Entry selectedEntryInTableViewEntries = null;


  @FXML
  protected MenuItem mnitmToolsBackups;
  @FXML
  protected Menu mnitmMainMenuWindow;

  @FXML
  protected Label statusLabel;
  @FXML
  protected Label statusLabelCountEntries;


  @FXML
  protected CheckMenuItem chkmnitmViewDialogsFieldsDisplayShowImportantOnes;
  @FXML
  protected CheckMenuItem chkmnitmViewDialogsFieldsDisplayShowAll;
  @FXML
  protected CheckMenuItem chkmnitmViewShowCategories;
  @FXML
  protected CheckMenuItem chkmnitmViewShowQuickEditEntryPane;


  @FXML
  protected Region contentPane;

  @FXML
  protected TabPane tbpnOverview;
  @FXML
  protected Tab tabCategories;
  @FXML
  protected Tab tabTags;


  @FXML
  protected HBox hboxCategoriesBar;
  @FXML
  protected CustomTextField txtfldCategoriesQuickFilter;
  @FXML
  protected Button btnRemoveSelectedCategories;
  @FXML
  protected TreeView<Category> trvwCategories;


  @FXML
  protected HBox hboxTagsBar;
  @FXML
  protected TextField txtfldTagsQuickFilter;
  @FXML
  protected Button btnRemoveTagsFilter;
  @FXML
  protected Button btnRemoveSelectedTag;
  @FXML
  protected TableView<Tag> tblvwTags;
  @FXML
  protected TableColumn<Tag, String> clmnTagName;
  @FXML
  protected TableColumn<Tag, Boolean> clmnTagFilter;


  @FXML
  protected SplitPane splpnEntries;

  @FXML
  protected HBox hboxEntriesBar;
  @FXML
  protected CustomTextField txtfldEntriesQuickFilter;
  @FXML
  ToggleButton tglbtnEntriesQuickFilterTitle;
  @FXML
  ToggleButton tglbtnEntriesQuickFilterContent;
  @FXML
  protected Button btnRemoveSelectedEntries;

  @FXML
  protected TableView<Entry> tblvwEntries;
  @FXML
  protected TableColumn<Entry, Long> clmnId;
  @FXML
  protected TableColumn<Entry, String> clmnEntryPreview;
  @FXML
  protected TableColumn<Entry, String> clmnTags;
  @FXML
  protected TableColumn<Entry, String> clmnCreated;
  @FXML
  protected TableColumn<Entry, String> clmnModified;


  @FXML
  protected Pane pnQuickEditEntry;
  @FXML
  protected TextField txtfldEntryAbstract;

  protected EntryTagsControl currentEditedEntryTagsControl = null;
  @FXML
  protected TextArea txtarEntryContent;


  @Override
  public void initialize(URL url, ResourceBundle rb) {
    setupControls();

    net.deepthought.controller.Dialogs.getOpenedChildWindows().addListener(new SetChangeListener<Stage>() {
      @Override
      public void onChanged(Change<? extends Stage> c) {
        if (c.wasAdded())
          addMenuItemToWindowMenu(c.getElementAdded());
        if (c.wasRemoved())
          removeMenuItemFromWindowMenu(c.getElementRemoved());
      }
    });

    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        deepThoughtChangedThreadSafe(deepThought);
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        showErrorOccurredMessageThreadSafe(error);
      }
    });

    Application.instantiateAsync(new DefaultDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
//        return new JpaEntityManager(configuration);
        return new OrmLiteJavaSeEntityManager(configuration);
      }
    });
  }

  protected void showErrorOccurredMessageThreadSafe(DeepThoughtError error) {
    if(Platform.isFxApplicationThread())
      showErrorOccurredMessage(error);
    else
      Platform.runLater(() -> showErrorOccurredMessage(error));
  }

  protected void showErrorOccurredMessage(DeepThoughtError error) {
    if(error.getErrorMessageTitle() != null) {
      Action response = Dialogs.create()
          .owner(_stage)
          .title(error.getErrorMessageTitle())
          .message(error.getErrorMessage())
          .actions(Dialog.ACTION_OK)
          .showException(error.getException());
    }
    else if(error.isSevere() == true) {
      Action response = Dialogs.create()
          .owner(_stage)
          .title(Localization.getLocalizedStringForResourceKey("alert.message.title.severe.error.occurred"))
          .message(Localization.getLocalizedStringForResourceKey("alert.message.message.severe.error.occurred", error.getErrorMessage()))
          .actions(Dialog.ACTION_OK)
          .showException(error.getException());
    }
    else {
      Dialogs dialog = Dialogs.create()
          .owner(_stage)
          .title(Localization.getLocalizedStringForResourceKey("alert.message.title.error.occurred"))
          .message(error.getErrorMessage())
          .actions(Dialog.ACTION_OK);

      Action response = null;
      if(error.getException() != null)
        response = dialog.showException(error.getException());
      else
        response = dialog.showError();
    }
  }

  protected void deepThoughtChangedThreadSafe(final DeepThought deepThought) {
    if(Platform.isFxApplicationThread())
      deepThoughtChanged(deepThought);
    else {
      Platform.runLater(() -> deepThoughtChanged(deepThought));
    }
  }

  protected void deepThoughtChanged(DeepThought deepThought) {
    log.debug("DeepThought changed from {} to {}", this.deepThought, deepThought);

    if(this.deepThought != null) {
      this.deepThought.removeEntityListener(deepThoughtListener);
      Application.getSettings().removeSettingsChangedListener(userDeviceSettingsChangedListener);
    }

    this.deepThought = deepThought;

    setControlsEnabledState(deepThought != null);

    clearAllData();

    if(deepThought != null) {
      deepThought.addEntityListener(deepThoughtListener);

      userDeviceSettingsChanged();

      trvwCategories.setRoot(new CategoryTreeItem(deepThought.getTopLevelCategory()));
      selectedCategoryChanged(deepThought.getTopLevelCategory());

      showAllTagsInListViewTags(deepThought);

      setSelectedTab(deepThought.getSettings().getLastSelectedTab());

//    if(deepThought.getLastViewedCategory() != null)
//      trvwCategories.getSelectionModel().(deepThought.getLastViewedCategory());

      if (deepThought.getSettings().getLastViewedTag() != null) {
        tblvwTags.getSelectionModel().select(deepThought.getSettings().getLastViewedTag());
      } else {
        tblvwTags.getSelectionModel().select(0);
      }

      if (deepThought.getSettings().getLastViewedEntry() != null)
        tblvwEntries.getSelectionModel().select(deepThought.getSettings().getLastViewedEntry());
    }
  }

  protected void userDeviceSettingsChanged() {
    Application.getSettings().addSettingsChangedListener(userDeviceSettingsChangedListener);

    UserDeviceSettings settings = Application.getSettings();

    showCategoriesChanged(settings.showCategories());
    showPaneQuickEditEntryChanged(settings.showEntryQuickEditPane());

    chkmnitmViewDialogsFieldsDisplayShowImportantOnes.selectedProperty().removeListener(checkMenuItemViewDialogsFieldsDisplayShowImportantOnesSelectedChangeListener);
    chkmnitmViewDialogsFieldsDisplayShowImportantOnes.setSelected(settings.getDialogsFieldsDisplay() == DialogsFieldsDisplay.ShowImportantOnes);
    chkmnitmViewDialogsFieldsDisplayShowImportantOnes.selectedProperty().addListener(checkMenuItemViewDialogsFieldsDisplayShowImportantOnesSelectedChangeListener);

    chkmnitmViewDialogsFieldsDisplayShowAll.selectedProperty().removeListener(checkMenuItemViewDialogsFieldsDisplayShowAllSelectedChangeListener);
    chkmnitmViewDialogsFieldsDisplayShowAll.setSelected(settings.getDialogsFieldsDisplay() == DialogsFieldsDisplay.ShowAll);
    chkmnitmViewDialogsFieldsDisplayShowAll.selectedProperty().addListener(checkMenuItemViewDialogsFieldsDisplayShowAllSelectedChangeListener);

    chkmnitmViewShowCategories.selectedProperty().removeListener(checkMenuItemViewShowCategoriesSelectedChangeListener);
    chkmnitmViewShowCategories.setSelected(settings.showCategories());
    chkmnitmViewShowCategories.selectedProperty().addListener(checkMenuItemViewShowCategoriesSelectedChangeListener);

    chkmnitmViewShowQuickEditEntryPane.selectedProperty().removeListener(checkMenuItemViewShowQuickEditEntrySelectedChangeListener);
    chkmnitmViewShowQuickEditEntryPane.setSelected(settings.showEntryQuickEditPane());
    chkmnitmViewShowQuickEditEntryPane.selectedProperty().addListener(checkMenuItemViewShowQuickEditEntrySelectedChangeListener);
  }

  protected void setControlsEnabledState(boolean enabled) {
    mnitmToolsBackups.setDisable(enabled == false);
    contentPane.setDisable(enabled == false);
  }

  protected void clearAllData() {
    trvwCategories.setRoot(null);
    tableViewTagsItems.clear();
    tableViewEntriesItems.clear();
    currentEditedEntryTagsControl.setEntry(null);
  }

  protected void showAllTagsInListViewTags(DeepThought deepThought) {
    tableViewTagsItems.clear();

    tableViewTagsItems.add(new AllEntriesSystemTag(deepThought));
    tableViewTagsItems.add(new EntriesWithoutTagsSystemTag(deepThought));
    tableViewTagsItems.addAll(deepThought.getTags());

    tblvwTags.getSelectionModel().select(deepThought.getSettings().getLastViewedTag());
  }

  protected void windowClosing() {
    setStatusLabelText(Localization.getLocalizedStringForResourceKey("backing.up.data"));
    Application.shutdown();

    for(Stage openedWindow : net.deepthought.controller.Dialogs.getOpenedChildWindows()) {
      openedWindow.close();
    }
  }

  private void setStatusLabelText(final String statusText) {
    if(Platform.isFxApplicationThread())
      statusLabel.setText(statusText);
    else {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          statusLabel.setText(statusText);
        }
      });
    }
  }

  protected void setupControls() {
    contentPane.setDisable(true); // don't enable controls as no DeepThought is received / deserialized

    setupMainMenu();

    setupTabPaneOverview();

    setupEntriesOverviewSection();

    setupQuickEditEntrySection();
  }

  protected void setupMainMenu() {

  }

  private void setupTabPaneOverview() {
    tbpnOverview.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> tabpaneOverviewSelectedTabChanged(tbpnOverview.getSelectionModel()
        .getSelectedItem()));

    setupTagsTab();

    setupCategoriesTab();
  }

  protected void tabpaneOverviewSelectedTabChanged(Tab selectedTab) {
    if(selectedTab == tabTags) {
      deepThought.getSettings().setLastSelectedTab(SelectedTab.Tags);
      selectedTagChanged(deepThought.getSettings().getLastViewedTag());
    }
    else if(selectedTab == tabCategories) {
      deepThought.getSettings().setLastSelectedTab(SelectedTab.Categories);
      selectedCategoryChanged(deepThought.getSettings().getLastViewedCategory());
    }


  }

  protected void setSelectedTab(SelectedTab selectedTab) {
    if(selectedTab == SelectedTab.Tags) {
      tbpnOverview.getSelectionModel().select(tabTags);
    }
    else if(selectedTab == SelectedTab.Categories) {
      tbpnOverview.getSelectionModel().select(tabCategories);
    }
  }

  protected void setupTagsTab() {
    // replace normal TextField txtfldTagsQuickFilter with a SearchTextField (with a cross to clear selection)
    hboxTagsBar.getChildren().remove(txtfldTagsQuickFilter);
    txtfldTagsQuickFilter = (CustomTextField) TextFields.createClearableTextField();
    hboxTagsBar.getChildren().add(1, txtfldTagsQuickFilter);
    HBox.setHgrow(txtfldTagsQuickFilter, Priority.ALWAYS);
    txtfldTagsQuickFilter.setPromptText("Quickly filter Tags");
    txtfldTagsQuickFilter.setPrefWidth(80);
    txtfldTagsQuickFilter.textProperty().addListener((observable, oldValue, newValue) -> quickFilterTags());
    txtfldTagsQuickFilter.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if (event.getCode() == KeyCode.ESCAPE)
        txtfldTagsQuickFilter.clear();
    });

    btnRemoveTagsFilter.setGraphic(new ImageView("icons/filter_delete_16x16.gif"));
    JavaFxLocalization.bindControlToolTip(btnRemoveTagsFilter, "button.remove.tags.filter.tool.tip");

    tblvwTags.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      log.debug("tblvwTags selected tag changed from {} to {}, isAddingTag = {}", oldValue, newValue, isAddingTag);
      if(isAddingTag == false) {
        selectedTagChanged(newValue);
      }
    });

    tableViewTagsItems = FXCollections.observableList(tblvwTags.getItems());
    filteredTags = new FilteredList<>(tableViewTagsItems, tag -> true);
    sortedFilteredTags = new SortedList<>(filteredTags, tagComparator);

    tblvwTags.setItems(sortedFilteredTags);

    clmnTagName.setCellFactory(new Callback<TableColumn<Tag, String>, TableCell<Tag, String>>() {
       @Override
       public TableCell<Tag, String> call(TableColumn<Tag, String> param) {
         return new TagNameTableCell();
       }
     });

    clmnTagFilter.setCellFactory(new Callback<TableColumn<Tag, Boolean>, TableCell<Tag, Boolean>>() {
      @Override
      public TableCell<Tag, Boolean> call(TableColumn<Tag, Boolean> param) {
        final TagFilterTableCell cell = new TagFilterTableCell(tagsToFilterFor);
        tagFilterTableCells.add(cell);

        cell.isFilteredProperty().addListener(new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue == true)
              addTagToTagFilter(cell.getTag());
            else
              removeTagFromTagFilter(cell.getTag());

            btnRemoveTagsFilter.setDisable(tagsToFilterFor.size() == 0);
          }
        });

        return cell;
      }
    });
  }

  protected void selectedTagChanged(Tag tag) {
    log.debug("Selected Tag changed to {}", tag);

    if(deepThought.getSettings().getLastViewedTag() != null)
      deepThought.getSettings().getLastViewedTag().removeEntityListener(selectedTagListener);

    if(tag instanceof SystemTag)
      deepThought.getSettings().setLastViewedTag(null); // TODO: i think setting to null is a bad solution
    else
      deepThought.getSettings().setLastViewedTag(tag);

    Tag selectedTag = deepThought.getSettings().getLastViewedTag();
    btnRemoveSelectedTag.setDisable(selectedTag == null || selectedTag instanceof SystemTag);

    if(tag != null)
      tag.addEntityListener(selectedTagListener);

    showEntriesForSelectedTag(tag);
  }

  protected void showEntriesForSelectedTag(Tag tag) {
    if(tagsToFilterFor.size() > 0) {
      Set<Entry> filteredEntriesWithThisTag = new TreeSet<>();
      for(Entry filteredEntry : entriesWithFilteredTags) {
        if(filteredEntry.hasTag(tag))
          filteredEntriesWithThisTag.add(filteredEntry);
      }

      showEntries(filteredEntriesWithThisTag);
    }
    else if(tag != null)
      showEntries(tag.getEntries());
    else
      showEntries(new HashSet<>());
  }

  protected void setupCategoriesTab() {
    // replace normal TextField txtfldCategoriesQuickFilter with a SearchTextField (with a cross to clear selection)
    hboxCategoriesBar.getChildren().remove(txtfldCategoriesQuickFilter);
    txtfldCategoriesQuickFilter = (CustomTextField) TextFields.createClearableTextField();
    hboxCategoriesBar.getChildren().add(1, txtfldCategoriesQuickFilter);
    HBox.setHgrow(txtfldCategoriesQuickFilter, Priority.ALWAYS);
    txtfldCategoriesQuickFilter.setPromptText("Quickly filter Categories");
    txtfldCategoriesQuickFilter.setPromptText("(disabled)");

    trvwCategories.setContextMenu(createTreeViewCategoriesContextMenu());

    trvwCategories.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Category>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<Category>> observable, TreeItem<Category> oldValue, TreeItem<Category> newValue) {
        selectedCategoryTreeItem = (CategoryTreeItem) newValue;

        if (newValue == null)
          selectedCategoryChanged(deepThought.getTopLevelCategory());
        else
          selectedCategoryChanged(newValue.getValue());
      }
    });

    trvwCategories.setCellFactory(new Callback<TreeView<Category>, TreeCell<Category>>() {
      @Override
      public TreeCell<Category> call(TreeView<Category> param) {
        return new CategoryTreeCell();
      }
    });
  }

  protected void showCategoriesChanged(boolean showCategories) {
    if (showCategories == false)
      tbpnOverview.getTabs().remove(tabCategories);
    else tbpnOverview.getTabs().add(0, tabCategories);

    if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Categories)
      tbpnOverview.getSelectionModel().select(tabTags);
  }

  private ContextMenu createTreeViewCategoriesContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem addCategoryMenuItem = new MenuItem("Add Category");
    contextMenu.getItems().add(addCategoryMenuItem);

    addCategoryMenuItem.setOnAction(event -> {
      deepThought.addCategory(new Category());
    });

    return contextMenu;
  }

  protected void setupEntriesOverviewSection() {
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

    tableViewEntriesItems = tblvwEntries.getItems();
    filteredEntries = new FilteredList<>(tableViewEntriesItems, entry -> true);
    sortedFilteredEntries = new SortedList<Entry>(filteredEntries, entriesComparator);
    tblvwEntries.setItems(filteredEntries);

    tblvwEntries.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tblvwEntries.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Entry>() {
      @Override
      public void changed(ObservableValue<? extends Entry> observable, Entry oldValue, Entry newValue) {
        if(oldValue != null)
          oldValue.removeEntityListener(currentlyEditedEntryListener);

        selectedEntryChanged(newValue);
      }
    });

    clmnId.setCellValueFactory(new PropertyValueFactory<Entry, Long>("entryIndex"));
    clmnEntryPreview.setCellFactory((param) -> {
      return new EntryPreviewTableCell();
    });
    clmnTags.setCellFactory((param) -> {
      return new EntryTagsTableCell();
    });
    clmnCreated.setCellFactory((param) -> {
      return new EntryCreatedTableCell();
    });
    clmnModified.setCellFactory((param) -> {
      return new EntryModifiedTableCell();
    });
  }

  protected void setupQuickEditEntrySection() {
    pnQuickEditEntry.managedProperty().bind(pnQuickEditEntry.visibleProperty());

    txtfldEntryAbstract.textProperty().addListener((observable, oldValue, newValue) -> {
      Entry selectedEntry = tblvwEntries.getSelectionModel().getSelectedItem();
      if(selectedEntry != null)
        selectedEntry.setAbstract(txtfldEntryAbstract.getText());
    });

    txtarEntryContent.textProperty().addListener((observable, oldValue, newValue) -> {
      Entry selectedEntry = tblvwEntries.getSelectionModel().getSelectedItem();
      if (selectedEntry != null)
        selectedEntry.setContent(txtarEntryContent.getText());
    });

    currentEditedEntryTagsControl = new EntryTagsControl();
    currentEditedEntryTagsControl.setTagAddedEventHandler(event -> event.getEntry().addTag(event.getTag()));
    currentEditedEntryTagsControl.setTagRemovedEventHandler(event -> event.getEntry().removeTag(event.getTag()));
    VBox.setMargin(currentEditedEntryTagsControl, new Insets(6, 0, 6, 0));
    pnQuickEditEntry.getChildren().add(1, currentEditedEntryTagsControl);
  }

  protected void showPaneQuickEditEntryChanged(boolean showPaneQuickEditEntry) {
    pnQuickEditEntry.setVisible(showPaneQuickEditEntry);

    if(showPaneQuickEditEntry) {
      if(splpnEntries.getItems().contains(pnQuickEditEntry) == false) {
//      pnQuickEditEntry.setVisible(true);
        splpnEntries.getItems().add(pnQuickEditEntry);
        splpnEntries.setDividerPositions(0.5);
      }
    }
    else {
//      pnQuickEditEntry.setVisible(false);
      splpnEntries.getItems().remove(pnQuickEditEntry);
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

  protected EntityListener currentlyEditedEntryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.EntryTitleColumnName)) {
        txtfldEntryAbstract.setText(((Entry) entity).getAbstract());
      }
      else if(propertyName.equals(TableConfig.EntryContentColumnName)) {
        txtarEntryContent.setText(((Entry)entity).getContent());
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

  protected EntityListener selectedTagListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Tags && deepThought.getSettings().getLastViewedTag() != null &&
          collection == deepThought.getSettings().getLastViewedTag().getEntries()) {
        if(filteredTags.size() > 0)
          reapplyTagsFilter();
        showEntriesForSelectedTag((Tag)collectionHolder);
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Tags && deepThought.getSettings().getLastViewedTag() != null &&
          collection == deepThought.getSettings().getLastViewedTag().getEntries()) {
        if(filteredTags.size() > 0)
          reapplyTagsFilter();
        showEntriesForSelectedTag((Tag)collectionHolder);
      }
    }
  };


  protected ListChangeListener<Tag> entryTagSuggestionListChangeListener = new ListChangeListener<Tag>() {
    @Override
    public void onChanged(Change<? extends Tag> c) {
      Entry currentlyEditedEntry = tblvwEntries.getSelectionModel().getSelectedItem();
      if(currentlyEditedEntry == null || c.next() == false)
        return;

      for(Tag addedTag : c.getAddedSubList()) {
        addTagToEntry(currentlyEditedEntry, addedTag);
        if(c.next() == false)
          return;
      }

      for(Tag removedTag : c.getRemoved()) {
        removeTagFromEntry(currentlyEditedEntry, removedTag);
        if(c.next() == false)
          return;
      }
    }
  };

  protected SetChangeListener<Tag> entryTagSuggestionSetChangeListener = new SetChangeListener<Tag>() {
    @Override
    public void onChanged(Change<? extends Tag> c) {
      Entry currentlyEditedEntry = tblvwEntries.getSelectionModel().getSelectedItem();

      if(c.wasAdded()) {
        addTagToEntry(currentlyEditedEntry, c.getElementAdded());
      }
      else if(c.wasRemoved()) {
        removeTagFromEntry(currentlyEditedEntry, c.getElementRemoved());
      }

//      if(currentlyEditedEntry == null || c.next() == false)
//        return;
//
//      for(Tag addedTag : c.getElementAdded()) {
//        addTagToEntry(currentlyEditedEntry, addedTag);
//        if(c.next() == false)
//          return;
//      }
//
//      for(Tag removedTag : c.getRemoved()) {
//        removeTagFromEntry(currentlyEditedEntry, removedTag);
//        if(c.next() == false)
//          return;
//      }
    }
  };

  protected void addTagToEntry(Entry entry, Tag addedTag) {
    entry.addTag(addedTag);
  }

  protected void removeTagFromCurrentlyEditedEntry(Tag removedTag) {
    Entry currentlyEditedEntry = tblvwEntries.getSelectionModel().getSelectedItem();
    if(currentlyEditedEntry != null)
      removeTagFromEntry(currentlyEditedEntry, removedTag);
  }

  protected void removeTagFromEntry(Entry entry, Tag removedTag) {
    entry.removeTag(removedTag);
  }

  protected void quickFilterTags() {
    Tag selectedTag = deepThought.getSettings().getLastViewedTag();

    String filter = txtfldTagsQuickFilter.getText();

    filteredTags.setPredicate((tag) -> {
      // If filter text is empty, display all Tags.
      if (filter == null || filter.isEmpty()) {
        return true;
      }

      String lowerCaseFilter = filter.toLowerCase();
      String[] parts = lowerCaseFilter.split(",");
      String lowerCaseTagName = tag.getName().toLowerCase();

      for(String part : parts) {
        if (lowerCaseTagName.contains(part.trim())) {
          return true; // Filter matches Tag's name
        }
      }

      return false; // Does not match.
    });

    if(filteredTags.contains(selectedTag))
      tblvwTags.getSelectionModel().select(selectedTag);
  }

  protected void addTagToTagFilter(Tag tag) {
    tagsToFilterFor.add(tag);
    reapplyTagsFilter();

    tblvwTags.getSelectionModel().select(tag);
  }

  protected void removeTagFromTagFilter(Tag tag) {
    tagsToFilterFor.remove(tag);
    reapplyTagsFilter();

    tblvwTags.getSelectionModel().select(tag);
  }

  protected void reapplyTagsFilter() {
    entriesWithFilteredTags.clear();

    if(tagsToFilterFor.size() == 0) {
      showAllTagsInListViewTags(deepThought);
    }
    else {
//      final Set<Entry> entriesContainingFilteredTags = new HashSet<>();
      final Set<Tag> tagsWithEntriesContainingFilteredTags = new HashSet<>();
      tagsWithEntriesContainingFilteredTags.addAll(tagsToFilterFor);

      for (Tag filteredTag : tagsToFilterFor) {
        for (Entry entry : filteredTag.getEntries()) {
          if (entry.hasTags(tagsToFilterFor)) {
//            entriesContainingFilteredTags.add(entry);
            entriesWithFilteredTags.add(entry);
            tagsWithEntriesContainingFilteredTags.addAll(entry.getTags());
          }
        }
      }

//    filteredTags.setPredicate((tagToCheck) -> {
//      return tagsWithEntriesContainingFilteredTags.contains(tagToCheck);
//    });

//      List<Entry> sortedEntriesContainingFilteredTags = new ArrayList<>(entriesContainingFilteredTags);
//      Collections.sort(sortedEntriesContainingFilteredTags, new Comparator<Entry>() {
//        @Override
//        public int compare(Entry o1, Entry o2) {
//          return ((Integer)o2.getEntryIndex()).compareTo(o1.getEntryIndex());
//        }
//      });
//      showEntries(sortedEntriesContainingFilteredTags);

      tableViewTagsItems.clear();
      tableViewTagsItems.addAll(tagsWithEntriesContainingFilteredTags);
    }

    quickFilterTags();
  }

  protected void filterEntries() {
    String filter = txtfldEntriesQuickFilter.getText();

    filteredEntries.setPredicate((entry) -> {
      // If filter text is empty, display all Entries.
      if (filter == null || filter.isEmpty()) {
        return true;
      }

      String lowerCaseFilter = filter.toLowerCase();

      if(tglbtnEntriesQuickFilterTitle.isSelected() && entry.getTitle().toLowerCase().contains(lowerCaseFilter)) {
        return true; // Filter matches title
      }
      else if (tglbtnEntriesQuickFilterContent.isSelected() && entry.getContent().toLowerCase().contains(lowerCaseFilter)) {
        return true; // Filter matches content
      }
      return false; // Does not match.
    });
  }

  @FXML
  protected void handleButtonRemoveSelectedCategoryAction(ActionEvent event) {
    Category selectedCategory = deepThought.getSettings().getLastViewedCategory();
    if(deepThought.getTopLevelCategory().equals(selectedCategory) == false) {
      if(selectedCategory.hasSubCategories() || selectedCategory.hasEntries()) {
        Boolean deleteCategory = Alerts.showConfirmDeleteCategoryWithSubCategoriesOrEntries(selectedCategory);
        if(deleteCategory) {
          removeCategory(selectedCategory);
        }
      }
      else {
        removeCategory(selectedCategory);
      }
    }
  }

  protected void removeCategory(Category category) {
    Category parent = category.getParentCategory();

    if(selectedCategoryTreeItem != null) {
      TreeItem parentTreeItem = selectedCategoryTreeItem.getParent();
      TreeItem previousSibling = selectedCategoryTreeItem.previousSibling();

      parentTreeItem.getChildren().remove(selectedCategoryTreeItem);

      if(previousSibling != null)
        trvwCategories.getSelectionModel().select(previousSibling);
      else
        trvwCategories.getSelectionModel().select(parentTreeItem);
    }

    parent.removeSubCategory(category);
  }

  @FXML
  public void handleMenuItemFileCloseAction(ActionEvent event) {
    _stage.close();
  }

  protected ChangeListener<Boolean> checkMenuItemViewDialogsFieldsDisplayShowImportantOnesSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      Application.getSettings().setDialogsFieldsDisplay(DialogsFieldsDisplay.ShowImportantOnes);
    }
  };

  protected ChangeListener<Boolean> checkMenuItemViewDialogsFieldsDisplayShowAllSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      Application.getSettings().setDialogsFieldsDisplay(DialogsFieldsDisplay.ShowAll);
    }
  };

  protected ChangeListener<Boolean> checkMenuItemViewShowCategoriesSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      Application.getSettings().setShowCategories(newValue);
    }
  };

  protected ChangeListener<Boolean> checkMenuItemViewShowQuickEditEntrySelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      Application.getSettings().setShowQuickEditEntryPane(newValue);
    }
  };

  @FXML
  protected void handleButtonAddCategoryAction(ActionEvent event) {
//    if(selectedCategoryTreeItem != null)
//      selectedCategoryTreeItem.getChildren().addListener(categoriesTreeItemsChanged);

    Category selectedCategory = deepThought.getSettings().getLastViewedCategory();
    Category newCategory = new Category();
//    deepThought.addCategory(newCategory);
    selectedCategory.addSubCategory(newCategory);

//    if(selectedCategoryTreeItem != null)
//      selectedCategoryTreeItem.getChildren().removeListener(categoriesTreeItemsChanged);
  }

  ListChangeListener<TreeItem<Category>> categoriesTreeItemsChanged = new ListChangeListener<TreeItem<Category>>() {
    @Override
    public void onChanged(Change<? extends TreeItem<Category>> c) {
      if(c.next()) {
        log.debug("onChanged() called with a AddedSubList size of {}", c.getAddedSubList().size());
        if (c.getAddedSize() == 1) {
          CategoryTreeItem newItem = (CategoryTreeItem)c.getAddedSubList().get(0);
          newItem.getParent().setExpanded(true);
          trvwCategories.getSelectionModel().select(newItem);
          trvwCategories.edit(newItem);
          Node graphic = newItem.getGraphic();
          log.debug("CategoryTreeItem {}'s graphic is {}", newItem, graphic);
        }
      }
    }
  };

  protected void selectedCategoryChanged(Category category) {
    deepThought.getSettings().setLastViewedCategory(category);

    btnRemoveSelectedCategories.setDisable(category.equals(deepThought.getTopLevelCategory()));

    showEntries(category.getEntries());
  }

  protected void showEntries(Collection<Entry> entries) {
    tableViewEntriesItems.clear();
    tableViewEntriesItems.addAll(entries);
  }

  protected void selectedEntryChanged(Entry selectedEntry) {
    log.debug("Selected Entry changed to {}", selectedEntry);

    if(deepThought.getSettings().getLastViewedEntry() != null)
      deepThought.getSettings().getLastViewedEntry().removeEntityListener(currentlyEditedEntryListener);

    deepThought.getSettings().setLastViewedEntry(selectedEntry);

    btnRemoveSelectedEntries.setDisable(selectedEntry == null);
    pnQuickEditEntry.setDisable(selectedEntry == null);
    currentEditedEntryTagsControl.setEntry(selectedEntry);

    if(selectedEntry != null) {
      selectedEntry.addEntityListener(currentlyEditedEntryListener);
      txtfldEntryAbstract.setText(selectedEntry.getAbstract());
      txtarEntryContent.setText(selectedEntry.getContent());

      txtfldEntryAbstract.requestFocus();
      txtfldEntryAbstract.selectAll();
    }
    else {
      txtfldEntryAbstract.setText("");
      txtarEntryContent.setText("");
    }
  }


  @FXML
  public void handleMenuItemToolsBackupsAction(Event event) {
    net.deepthought.controller.Dialogs.showRestoreBackupDialog(_stage);
  }

  @FXML
  public void handleMainMenuWindowShowing(Event event) {
//    if(mnitmMainMenuWindow.getItems().size() > 0)
//      mnitmMainMenuWindow.getItems().clear();
//
//    for(final Stage openedWindow : openedChildWindows) {
//      MenuItem openedWindowItem = new MenuItem(openedWindow.getTitle());
//      openedWindowItem.setOnAction(new EventHandler<ActionEvent>() {
//        @Override
//        public void handle(ActionEvent event) {
//          openedWindow.show();
//          openedWindow.requestFocus();
//        }
//      });
//
//      mnitmMainMenuWindow.getItems().add(openedWindowItem);
//    }
  }

  @FXML
  protected void handleButtonRemoveTagsFilterAction(ActionEvent event) {
    for(TagFilterTableCell cell : tagFilterTableCells) {
      cell.uncheck();
    }

    tagsToFilterFor.clear();

    showAllTagsInListViewTags(deepThought);

    quickFilterTags();
  }

  @FXML
  protected void handleButtonRemoveSelectedTagsAction(ActionEvent event) {
    Tag selectedTag = tblvwTags.getSelectionModel().getSelectedItem();
    if(selectedTag == null)
      return;

    Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(deepThought, selectedTag);
  }

  @FXML
  protected void handleButtonAddTagAction(ActionEvent event) {
    addNewTag("New tag");
  }

  protected Tag addNewTag(String name) {
    Tag newTag = new Tag(name);
    deepThought.addTag(newTag);

    return newTag;
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

  protected void addAndSelectEntry(Entry newEntry) {
    tableViewEntriesItems.add(0, newEntry);
    selectEntry(newEntry);
  }

  private void selectEntry(Entry entry) {
    tblvwEntries.getSelectionModel().select(entry);
    deepThought.getSettings().setLastViewedEntry(entry); // TODO: can this ever be not already set to this Entry?

    txtfldEntryAbstract.requestFocus();
  }

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
////      windowStage.initOwner(_stage);
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

  protected void addMenuItemToWindowMenu(final Stage childWindow) {
    final MenuItem childWindowItem = new MenuItem(childWindow.getTitle());
    childWindowItem.setUserData(childWindow);

    childWindow.titleProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        childWindowItem.setText(newValue);
      }
    });

    childWindowItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        childWindow.show();
        childWindow.requestFocus();
      }
    });

    mnitmMainMenuWindow.getItems().add(childWindowItem);
  }

  protected void removeMenuItemFromWindowMenu(Stage childWindow) {
    for(MenuItem menuItem : mnitmMainMenuWindow.getItems()) {
      if(childWindow.equals(menuItem.getUserData())) {
        mnitmMainMenuWindow.getItems().remove(menuItem);
        break;
      }
    }
  }

  public void setStage(Stage stage) {
    _stage = stage;

    stage.setOnHiding(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        windowClosing();
      }
    });

    stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//        int padding = 50;
//        if(newValue.doubleValue() < oldValue.doubleValue())
//          padding += (oldValue.doubleValue() - newValue.doubleValue());
//        pnTitledPaneEditEntryTagsGraphic.setPrefWidth(pnQuickEditEntry.getWidth() - padding);
      }
    });
  }

  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getTags()){
        if(Platform.isFxApplicationThread()) {
          tagHasBeenAdded((Tag)addedEntity);
        }
        else {
          Platform.runLater(() -> {
            tagHasBeenAdded((Tag)addedEntity);
          });
        }
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getTags()) {
//        FXCollections.sort(tableViewTagsItems);
        quickFilterTags();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getTags()) {
        if(Platform.isFxApplicationThread()) {
          tagHasBeenRemoved((Tag)removedEntity);
        }
        else {
          Platform.runLater(() -> {
            tagHasBeenRemoved((Tag)removedEntity);
          });
        }
      }
    }
  };

  protected SettingsChangedListener userDeviceSettingsChangedListener = new SettingsChangedListener() {
    @Override
    public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
      if(setting == Setting.UserDeviceShowQuickEditEntryPane)
        showPaneQuickEditEntryChanged((boolean) newValue);
      else if(setting == Setting.UserDeviceShowCategories) {
        showCategoriesChanged((boolean) newValue);
      }
    }
  };


  protected boolean isAddingTag = false;

  protected void tagHasBeenAdded(Tag tag) {
    if(tagsToFilterFor.size() == 0) {
      isAddingTag = true;
      tableViewTagsItems.add(tag);
      isAddingTag = false;
    }

    sortTags();
  }

  protected void tagHasBeenRemoved(Tag tag) {
    tableViewTagsItems.remove(tag);

    sortTags();
  }

  protected void sortTags() {
    log.debug("Going to sort sortedFilteredTags containing {} items", sortedFilteredTags.size());

    try {
      FXCollections.sort(tableViewTagsItems, tagComparator);
    } catch(Exception ex) {
      log.error("Could not sort Tags", ex);
    }
  }

  protected Comparator<Tag> tagComparator = new Comparator<Tag>() {
    @Override
    public int compare(Tag tag1, Tag tag2) {
      if(tag1 == null || tag2 == null) {
        log.debug("This should actually never be the case, both tag's name are null");
        return 0;
      }
      if(tag1 == null || tag1.getName() == null) {
        log.debug("tag1 {} or its name is null", tag1);
        return -1;
      }
      if(tag2 == null || tag2.getName() == null) {
        log.debug("tag2 {} or its name is null", tag2);
        return 1;
      }

      return tag1.getName().compareTo(tag2.getName());
    }
  };

  protected Comparator<Entry> entriesComparator = new Comparator<Entry>() {
    @Override
    public int compare(Entry entry1, Entry entry2) {
      if(entry1 == null && entry2 == null)
        return 0;
      else if(entry1 == null && entry2 != null)
        return -1;
      else if(entry1 != null && entry2 == null)
        return 1;

      return ((Integer)entry2.getEntryIndex()).compareTo(entry1.getEntryIndex());
    }
  };

}
