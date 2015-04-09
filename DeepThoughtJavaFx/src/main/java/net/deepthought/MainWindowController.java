/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.deepthought;

import net.deepthought.controls.FXUtils;
import net.deepthought.controls.entries.EntriesOverviewControl;
import net.deepthought.controls.tabcategories.CategoryTreeCell;
import net.deepthought.controls.tabcategories.CategoryTreeItem;
import net.deepthought.controls.tabtags.TabTagsControl;
import net.deepthought.data.DeepThoughtFxApplicationConfiguration;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
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
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 *
 * @author cdankl
 */
public class MainWindowController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(MainWindowController.class);


  protected Stage stage = null;

  protected DeepThought deepThought = null;

  protected CategoryTreeItem selectedCategoryTreeItem = null;


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
  protected SplitPane contentPane;

  @FXML
  protected TabPane tbpnOverview;
  @FXML
  protected Tab tabCategories;
  @FXML
  protected Tab tabTags;


  @FXML
  protected HBox paneCategoriesQuickFilter;
  @FXML
  protected CustomTextField txtfldCategoriesQuickFilter;
  @FXML
  protected Button btnRemoveSelectedCategories;
  @FXML
  protected TreeView<Category> trvwCategories;


  protected TabTagsControl tabTagsControl;

  protected EntriesOverviewControl entriesOverviewControl;



  @Override
  public void initialize(URL url, ResourceBundle rb) {
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

    Application.instantiateAsync(new DeepThoughtFxApplicationConfiguration(), new DefaultDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
//        return new JpaEntityManager(configuration);
        return new OrmLiteJavaSeEntityManager(configuration);
      }
    });

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
          .owner(stage)
          .title(error.getErrorMessageTitle())
          .message(error.getErrorMessage())
          .actions(Dialog.ACTION_OK)
          .showException(error.getException());
    }
    else if(error.isSevere() == true) {
      Action response = Dialogs.create()
          .owner(stage)
          .title(Localization.getLocalizedStringForResourceKey("alert.message.title.severe.error.occurred"))
          .message(Localization.getLocalizedStringForResourceKey("alert.message.message.severe.error.occurred", error.getErrorMessage()))
          .actions(Dialog.ACTION_OK)
          .showException(error.getException());
    }
    else {
      Dialogs dialog = Dialogs.create()
          .owner(stage)
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

      trvwCategories.setRoot(new CategoryTreeItem(deepThought.getTopLevelCategory()));
      selectedCategoryChanged(deepThought.getTopLevelCategory());

      tabTagsControl.deepThoughtChanged(deepThought);
      entriesOverviewControl.deepThoughtChanged(deepThought);

      setSelectedTab(deepThought.getSettings().getLastSelectedTab());

      contentPane.setDividerPositions(deepThought.getSettings().getMainWindowTabsAndEntriesOverviewDividerPosition());

      userDeviceSettingsChanged(); // TODO: isn't this redundant with selecting Tab and current category?

//    if(deepThought.getLastViewedCategory() != null)
//      trvwCategories.getSelectionModel().(deepThought.getLastViewedCategory());
    }
  }

  protected void userDeviceSettingsChanged() {
    Application.getSettings().addSettingsChangedListener(userDeviceSettingsChangedListener);

    UserDeviceSettings settings = Application.getSettings();

    showCategoriesChanged(settings.showCategories());
    entriesOverviewControl.showPaneQuickEditEntryChanged(settings.showEntryQuickEditPane());

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
    tabTagsControl.clearData();
    entriesOverviewControl.clearData();
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
    contentPane.setDisable(true); // don't enable controls as no DeepThought is received / deserialized yet

    setupMainMenu();

    setupTabPaneOverview();

    setupEntriesOverviewSection();
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
      if(deepThought.getSettings().getLastViewedTag() != null)
        tabTagsControl.selectedTagChanged(deepThought.getSettings().getLastViewedTag());
      else
        tabTagsControl.setSelectedTagToAllEntriesSystemTag();
    }
    else if(selectedTab == tabCategories) {
      deepThought.getSettings().setLastSelectedTab(SelectedTab.Categories);
      selectedCategoryChanged(deepThought.getSettings().getLastViewedCategory());
    }


  }

  protected void setSelectedTab(SelectedTab selectedTab) {
    if(selectedTab == SelectedTab.Tags) {
      tbpnOverview.getSelectionModel().select(tabTags);
//      if (deepThought.getSettings().getLastViewedEntry() != null)
//        tblvwEntries.getSelectionModel().select(deepThought.getSettings().getLastViewedEntry());
    }
    else if(selectedTab == SelectedTab.Categories) {
//      tbpnOverview.getSelectionModel().select(tabCategories);
//      selectedCategoryChanged(deepThought.getTopLevelCategory());
    }
  }

  protected void setupTagsTab() {
    tabTagsControl = new TabTagsControl(this);
    tabTags.setContent(tabTagsControl);
  }

  protected void setupCategoriesTab() {
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneCategoriesQuickFilter);
    paneCategoriesQuickFilter.setVisible(false); // TODO: display again when you know how to filter Categories

    // replace normal TextField txtfldCategoriesQuickFilter with a SearchTextField (with a cross to clear selection)
    paneCategoriesQuickFilter.getChildren().remove(txtfldCategoriesQuickFilter);
    txtfldCategoriesQuickFilter = (CustomTextField) TextFields.createClearableTextField();
    paneCategoriesQuickFilter.getChildren().add(1, txtfldCategoriesQuickFilter);
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
    entriesOverviewControl = new EntriesOverviewControl(this);
    contentPane.getItems().add(entriesOverviewControl);

    contentPane.setDividerPositions(0.3);
    contentPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
      if(deepThought != null)
        deepThought.getSettings().setMainWindowTabsAndEntriesOverviewDividerPosition(newValue.doubleValue());
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
    stage.close();
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

  public void showEntries(Collection<Entry> entries) {
    entriesOverviewControl.showEntries(entries);
  }


  @FXML
  public void handleMenuItemToolsBackupsAction(Event event) {
    net.deepthought.controller.Dialogs.showRestoreBackupDialog(stage);
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
    this.stage = stage;

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

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

  protected SettingsChangedListener userDeviceSettingsChangedListener = new SettingsChangedListener() {
    @Override
    public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
      if(setting == Setting.UserDeviceShowQuickEditEntryPane)
        entriesOverviewControl.showPaneQuickEditEntryChanged((boolean) newValue);
      else if(setting == Setting.UserDeviceShowCategories) {
        showCategoriesChanged((boolean) newValue);
      }
      else if(setting == Setting.UserDeviceDialogFieldsDisplay) {
        chkmnitmViewDialogsFieldsDisplayShowImportantOnes.selectedProperty().removeListener(checkMenuItemViewDialogsFieldsDisplayShowImportantOnesSelectedChangeListener);
        chkmnitmViewDialogsFieldsDisplayShowAll.selectedProperty().removeListener(checkMenuItemViewDialogsFieldsDisplayShowAllSelectedChangeListener);

        chkmnitmViewDialogsFieldsDisplayShowImportantOnes.setSelected(Application.getLoggedOnUser().getSettings().getDialogsFieldsDisplay() == DialogsFieldsDisplay.ShowImportantOnes);
        chkmnitmViewDialogsFieldsDisplayShowAll.setSelected(Application.getLoggedOnUser().getSettings().getDialogsFieldsDisplay() == DialogsFieldsDisplay.ShowAll);

        chkmnitmViewDialogsFieldsDisplayShowImportantOnes.selectedProperty().addListener(checkMenuItemViewDialogsFieldsDisplayShowImportantOnesSelectedChangeListener);
        chkmnitmViewDialogsFieldsDisplayShowAll.selectedProperty().addListener(checkMenuItemViewDialogsFieldsDisplayShowAllSelectedChangeListener);
      }
    }
  };

}
