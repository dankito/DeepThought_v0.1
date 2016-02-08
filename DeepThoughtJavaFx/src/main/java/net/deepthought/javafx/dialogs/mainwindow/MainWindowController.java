/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.deepthought.javafx.dialogs.mainwindow;

import net.deepthought.Application;
import net.deepthought.clipboard.ClipboardContentChangedListener;
import net.deepthought.clipboard.IClipboardWatcher;
import net.deepthought.clipboard.JavaFxClipboardWatcher;
import net.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.deepthought.communication.messages.request.DoOcrRequest;
import net.deepthought.communication.messages.request.ImportFilesRequest;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.deepthought.controller.Dialogs;
import net.deepthought.controls.Constants;
import net.deepthought.controls.clipboard.ContentExtractOptionForUi;
import net.deepthought.controls.clipboard.ContentExtractOptionForUiCreator;
import net.deepthought.controls.clipboard.CreateEntryFromClipboardContentPopup;
import net.deepthought.controls.connected_devices.ConnectedDevicesPanel;
import net.deepthought.controls.entries.EntriesOverviewControl;
import net.deepthought.data.model.settings.enums.ReferencesDisplay;
import net.deepthought.javafx.dialogs.mainwindow.tabs.categories.CategoryTreeCell;
import net.deepthought.javafx.dialogs.mainwindow.tabs.categories.CategoryTreeItem;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.TabTagsControl;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.contentextractor.ContentExtractOptions;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.DeepThoughtSettings;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.platform.JavaSeApplicationConfiguration;
import net.deepthought.plugin.IPlugin;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.localization.JavaFxLocalization;
import net.deepthought.util.localization.Localization;
import net.deepthought.util.Notification;
import net.deepthought.util.NotificationType;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
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

  protected IClipboardWatcher clipboardWatcher = null;

  protected CreateEntryFromClipboardContentPopup createEntryFromClipboardContentPopup;

  protected ContentExtractOptionForUiCreator contentExtractOptionForUiCreator = null;

  protected CategoryTreeItem selectedCategoryTreeItem = null;

  protected boolean hasTabCategoriesBeenLoadedYet = false;


  @FXML
  GridPane grdpnMainMenu;

  @FXML
  protected Menu mnitmMainMenuFile;
  @FXML
  protected Menu mnitmFileClipboard;
  @FXML
  protected MenuItem mnitmToolsBackups;
  @FXML
  protected Menu mnitmMainMenuWindow;

  @FXML
  protected CheckMenuItem chkmnitmViewDialogsFieldsDisplayShowImportantOnes;
  @FXML
  protected CheckMenuItem chkmnitmViewDialogsFieldsDisplayShowAll;
  @FXML
  protected CheckMenuItem chkmnitmViewReferencesDisplayShowOnlyReference;
  @FXML
  protected CheckMenuItem chkmnitmViewReferencesDisplayShowAll;
  @FXML
  protected CheckMenuItem chkmnitmViewShowCategories;
  @FXML
  protected CheckMenuItem chkmnitmViewShowQuickEditEntryPane;

  @FXML
  protected Menu mnitmToolsLanguage;


  @FXML
  protected MenuButton btnOnlineArticleExtractors;

  @FXML
  protected GridPane statusBar;
  @FXML
  protected Label statusLabel;
  @FXML
  protected Label statusLabelCountEntries;

  protected ConnectedDevicesPanel pnConnectedDevices;


  @FXML
  protected SplitPane contentPane;

  @FXML
  protected TabPane tbpnOverview;
  @FXML
  protected Tab tabCategories;
  @FXML
  protected Tab tabTags;


  @FXML
  protected HBox paneSearchCategories;
  @FXML
  protected CustomTextField txtfldSearchCategories;
  @FXML
  protected Button btnAddCategory;
  @FXML
  protected Button btnRemoveSelectedCategories;
  @FXML
  protected TreeView<Category> trvwCategories;


  protected TabTagsControl tabTagsControl;

  protected EntriesOverviewControl entriesOverviewControl;

  protected boolean hasContentPaneDividerBeenSetForTheFirstTime = false;



  @Override
  public void initialize(URL url, ResourceBundle rb) {
    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        deepThoughtChangedThreadSafe(deepThought);
      }

      @Override
      public void notification(Notification notification) {
        FXUtils.runOnUiThread(() -> notifyUser(notification));
      }
    });

    Application.instantiateAsync(new JavaSeApplicationConfiguration());

    setupControls();

    Dialogs.getOpenedChildWindows().addListener(new SetChangeListener<Stage>() {
      @Override
      public void onChanged(Change<? extends Stage> c) {
        if (c.wasAdded())
          addMenuItemToWindowMenu(c.getElementAdded());
        if (c.wasRemoved())
          removeMenuItemFromWindowMenu(c.getElementRemoved());

        mnitmMainMenuWindow.setDisable(Dialogs.getOpenedChildWindows().isEmpty());
      }
    });
  }

  protected void notifyUser(Notification notification) {
    if(notification instanceof DeepThoughtError)
      showErrorOccurredMessage((DeepThoughtError) notification);
    else if(notification.getType() == NotificationType.Info)
      showInfoMessage(notification);
    if(notification.getType() == NotificationType.ApplicationInstantiated)
      applicationInstantiated();
    else if(notification.getType() == NotificationType.PluginLoaded) {
      showInfoMessage(notification);
      if(notification.getParameter() instanceof IPlugin) {
        pluginLoaded((IPlugin)notification.getParameter());
      }
    }
    else if(notification.getType() == NotificationType.DeepThoughtsConnectorStarted) {
      pnConnectedDevices = new ConnectedDevicesPanel();
      statusBar.add(pnConnectedDevices, 2, 0);

      Application.getDeepThoughtsConnector().addImportFilesOrDoOcrListener(importFilesOrDoOcrListener);
    }
  }

  protected void showInfoMessage(Notification notification) {
    if(notification.hasNotificationMessageTitle())
      setStatusLabelText(notification.getNotificationMessageTitle() + ": " + notification.getNotificationMessage());
    else
      setStatusLabelText(notification.getNotificationMessage());
  }

  protected void showErrorOccurredMessage(DeepThoughtError error) {
    Alerts.showErrorMessage(stage, error);
  }

  protected void applicationInstantiated() {
    tabTagsControl.applicationInstantiated();
  }

  protected void pluginLoaded(IPlugin plugin) {
    if(plugin instanceof IOnlineArticleContentExtractor) {
      onlineArticleContentExtractorPluginLoaded((IOnlineArticleContentExtractor) plugin);
    }
  }

  protected void onlineArticleContentExtractorPluginLoaded(IOnlineArticleContentExtractor onlineArticleContentExtractor) {
    if(onlineArticleContentExtractor.hasArticlesOverview()) {
      MenuItem articleContentExtractorMenuItem = new MenuItem(onlineArticleContentExtractor.getSiteBaseUrl());
      articleContentExtractorMenuItem.setOnAction(event -> Dialogs.showArticlesOverviewDialog(onlineArticleContentExtractor));

      if(onlineArticleContentExtractor.getIconUrl() != IOnlineArticleContentExtractor.NoIcon)
        articleContentExtractorMenuItem.setGraphic(createOnlineArticleContentExtractorIcon(onlineArticleContentExtractor.getIconUrl()));

      btnOnlineArticleExtractors.getItems().add(articleContentExtractorMenuItem);
      btnOnlineArticleExtractors.setVisible(true);
    }
  }

  protected Node createOnlineArticleContentExtractorIcon(String iconUrl) {
    ImageView iconView = new ImageView(iconUrl);
    iconView.setPreserveRatio(true);
    iconView.setFitHeight(38);

    HBox graphicsPane = new HBox(iconView);
    graphicsPane.setPrefWidth(38);
    graphicsPane.setMaxWidth(38);
    graphicsPane.setMaxHeight(38);
    graphicsPane.setAlignment(Pos.CENTER);

    return graphicsPane;
  }

  protected void deepThoughtChangedThreadSafe(final DeepThought deepThought) {
    FXUtils.runOnUiThread(() -> deepThoughtChanged(deepThought));
  }

  protected void deepThoughtChanged(DeepThought deepThought) {
    log.debug("DeepThought changed from {} to {}", this.deepThought, deepThought);

    if(this.deepThought != null) {
      Application.getSettings().removeSettingsChangedListener(userDeviceSettingsChangedListener);
    }

    this.deepThought = deepThought;

    setControlsEnabledState(deepThought != null);

    clearAllData();

    if(deepThought != null) {
      applyNewDeepThoughtSettings(deepThought);
    }
  }

  protected void applyNewDeepThoughtSettings(DeepThought deepThought) {
    DeepThoughtSettings settings = deepThought.getSettings();

    selectedCategoryChanged(deepThought.getTopLevelCategory());

    tabTagsControl.deepThoughtChanged(deepThought);
    entriesOverviewControl.deepThoughtChanged(deepThought);

    FXUtils.applyWindowSettingsAndListenToChanges(stage, settings.getMainWindowSettings());
    setSelectedTab(settings.getLastSelectedTab());

    applyUserDeviceSettings(); // TODO: isn't this redundant with selecting Tab and current category?

//    if(deepThought.getLastViewedCategory() != null)
//      trvwCategories.getSelectionModel().(deepThought.getLastViewedCategory());

//      List<Entry> allEntries = new ArrayList<>(deepThought.getEntries());
//      for(int i = 0, length = deepThought.countEntries(); i < length; i++) {
//        Entry entry = allEntries.get(0);
//        entry.setEntryIndex(length - i);
//        ((DefaultDataManager)Application.dataManager).entityUpdated(entry);
//      }
  }

  protected void applyUserDeviceSettings() {
    Application.getSettings().addSettingsChangedListener(userDeviceSettingsChangedListener);

    UserDeviceSettings settings = Application.getSettings();

    showCategoriesChanged(settings.showCategories());
    entriesOverviewControl.showPaneQuickEditEntryChanged(settings.showEntryQuickEditPane());

    setMenuItemViewDialogsFieldsDisplayShowImportantOnesWithoutInvokingListener(settings.getDialogsFieldsDisplay());
    setMenuItemViewDialogsFieldsDisplayShowAllWithoutInvokingListener(settings.getDialogsFieldsDisplay());

    setMenuItemViewReferencesDisplayShowOnlyReferenceWithoutInvokingListener(settings.getReferencesDisplay());
    setMenuItemViewReferencesDisplayShowAllWithoutInvokingListener(settings.getReferencesDisplay());

    setMenuItemViewShowCategoriesWithoutInvokingListener(settings.showCategories());

    setMenuItemViewShowQuickEditEntryPaneWithoutInvokingListener(settings.showEntryQuickEditPane());
  }

  protected void setMenuItemViewDialogsFieldsDisplayShowImportantOnesWithoutInvokingListener(DialogsFieldsDisplay dialogsFieldsDisplay) {
    chkmnitmViewDialogsFieldsDisplayShowImportantOnes.selectedProperty().removeListener(checkMenuItemViewDialogsFieldsDisplayShowImportantOnesSelectedChangeListener);
    chkmnitmViewDialogsFieldsDisplayShowImportantOnes.setSelected(dialogsFieldsDisplay == DialogsFieldsDisplay.ImportantOnes);
    chkmnitmViewDialogsFieldsDisplayShowImportantOnes.selectedProperty().addListener(checkMenuItemViewDialogsFieldsDisplayShowImportantOnesSelectedChangeListener);
  }

  protected void setMenuItemViewDialogsFieldsDisplayShowAllWithoutInvokingListener(DialogsFieldsDisplay dialogsFieldsDisplay) {
    chkmnitmViewDialogsFieldsDisplayShowAll.selectedProperty().removeListener(checkMenuItemViewDialogsFieldsDisplayShowAllSelectedChangeListener);
    chkmnitmViewDialogsFieldsDisplayShowAll.setSelected(dialogsFieldsDisplay == DialogsFieldsDisplay.All);
    chkmnitmViewDialogsFieldsDisplayShowAll.selectedProperty().addListener(checkMenuItemViewDialogsFieldsDisplayShowAllSelectedChangeListener);
  }

  protected void setMenuItemViewReferencesDisplayShowOnlyReferenceWithoutInvokingListener(ReferencesDisplay referencesDisplay) {
    chkmnitmViewReferencesDisplayShowOnlyReference.selectedProperty().removeListener(checkMenuItemViewReferencesDisplayShowOnlyReferenceSelectedChangeListener);
    chkmnitmViewReferencesDisplayShowOnlyReference.setSelected(referencesDisplay == ReferencesDisplay.ShowOnlyReference);
    chkmnitmViewReferencesDisplayShowOnlyReference.selectedProperty().addListener(checkMenuItemViewReferencesDisplayShowOnlyReferenceSelectedChangeListener);
  }

  protected void setMenuItemViewReferencesDisplayShowAllWithoutInvokingListener(ReferencesDisplay referencesDisplay) {
    chkmnitmViewReferencesDisplayShowAll.selectedProperty().removeListener(checkMenuItemViewReferencesDisplayShowAllSelectedChangeListener);
    chkmnitmViewReferencesDisplayShowAll.setSelected(referencesDisplay == ReferencesDisplay.ShowAll);
    chkmnitmViewReferencesDisplayShowAll.selectedProperty().addListener(checkMenuItemViewReferencesDisplayShowAllSelectedChangeListener);
  }

  protected void setMenuItemViewShowCategoriesWithoutInvokingListener(boolean showCategories) {
    chkmnitmViewShowCategories.selectedProperty().removeListener(checkMenuItemViewShowCategoriesSelectedChangeListener);
    chkmnitmViewShowCategories.setSelected(showCategories);
    chkmnitmViewShowCategories.selectedProperty().addListener(checkMenuItemViewShowCategoriesSelectedChangeListener);
  }

  protected void setMenuItemViewShowQuickEditEntryPaneWithoutInvokingListener(boolean showEntryQuickEditPane) {
    chkmnitmViewShowQuickEditEntryPane.selectedProperty().removeListener(checkMenuItemViewShowQuickEditEntrySelectedChangeListener);
    chkmnitmViewShowQuickEditEntryPane.setSelected(showEntryQuickEditPane);
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
    setStatusLabelText(Localization.getLocalizedString("backing.up.data"));
    Application.shutdown();

    for(Stage openedWindow : Dialogs.getOpenedChildWindows()) {
      openedWindow.close();
    }

    cleanUp();
  }

  protected void cleanUp() {
    entriesOverviewControl.cleanUp();
    createEntryFromClipboardContentPopup.cleanUp();
    pnConnectedDevices.cleanUp();
    clipboardWatcher.removeClipboardContentChangedExternallyListener(clipboardContentChangedExternallyListener);
  }

  private void setStatusLabelText(final String statusText) {
    FXUtils.runOnUiThread(() -> statusLabel.setText(statusText));
  }

  protected void setupControls() {
    contentPane.setDisable(true); // don't enable controls as no DeepThought is received / deserialized yet

    setupMainMenu();

    setupTabPaneOverview();

    setupEntriesOverviewSection();
  }

  protected void setupMainMenu() {
    mnitmToolsLanguage.setOnShowing(event -> handleMenuToolsLanguageShowing(event));

    ImageView newspaperIcon = new ImageView(Constants.NewspaperIconPath);
    newspaperIcon.setPreserveRatio(true);
    newspaperIcon.setFitHeight(20); // TODO: make icon fill button
    btnOnlineArticleExtractors.setGraphic(newspaperIcon);
    btnOnlineArticleExtractors.getItems().clear(); // remove automatically added 'Article 1' and 'Article 2'

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnOnlineArticleExtractors);
//    grdpnMainMenu.getColumnConstraints().get(1).setPrefWidth(0);
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

      if(hasTabCategoriesBeenLoadedYet == false) {
        hasTabCategoriesBeenLoadedYet = true;
        // create Categories TreeItems at the first time TabCategories is selected as otherwise they may get created without ever being used
        trvwCategories.setRoot(new CategoryTreeItem(deepThought.getTopLevelCategory()));
      }
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
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneSearchCategories);
    paneSearchCategories.setVisible(false); // TODO: display again when you know how to search Categories

    // replace normal TextField txtfldSearchCategories with a SearchTextField (with a cross to clear selection)
    paneSearchCategories.getChildren().remove(txtfldSearchCategories);
    txtfldSearchCategories = (CustomTextField) TextFields.createClearableTextField();
    paneSearchCategories.getChildren().add(1, txtfldSearchCategories);
    HBox.setHgrow(txtfldSearchCategories, Priority.ALWAYS);
    JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchCategories, "search.categories.prompt.text");
    txtfldSearchCategories.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        txtfldSearchCategories.clear();
        event.consume();
      }
    });

    btnRemoveSelectedCategories.setTextFill(Constants.RemoveEntityButtonTextColor);
    btnAddCategory.setTextFill(Constants.AddEntityButtonTextColor);

    JavaFxLocalization.bindControlToolTip(btnRemoveSelectedCategories, "delete.selected.categories.tool.tip");
    JavaFxLocalization.bindControlToolTip(btnAddCategory, "create.new.top.level.category.tool.tip");

    trvwCategories.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        deleteSelectedCategories();
        event.consume();
      }
    });

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

    if(showCategories == false && deepThought.getSettings().getLastSelectedTab() == SelectedTab.Categories)
      tbpnOverview.getSelectionModel().select(tabTags);
  }

  protected void deleteSelectedCategories() {
    for(Category selectedCategory : getSelectedCategories()) {
      Alerts.deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(deepThought, selectedCategory);
    }
  }

  protected Collection<Category> getSelectedCategories() {
    List<Category> selectedCategories = new ArrayList<>(); // make a copy as when multiple Categories are selected after removing first one SelectionModel gets cleared
    for(TreeItem<Category> selectedItem : trvwCategories.getSelectionModel().getSelectedItems())
      selectedCategories.add(selectedItem.getValue());

    return selectedCategories;
  }

  protected void setupEntriesOverviewSection() {
    entriesOverviewControl = new EntriesOverviewControl(this);
    contentPane.getItems().add(entriesOverviewControl);

//    contentPane.setDividerPositions(0.26);

    contentPane.getDividers().get(0).positionProperty().addListener(((observableValue, oldValue, newValue) -> {
      contentPaneDividerPositionChanged(newValue);
    }));
  }

  protected void contentPaneDividerPositionChanged(Number newValue) {
    if (deepThought != null) {
      if(hasContentPaneDividerBeenSetForTheFirstTime == false) {
        hasContentPaneDividerBeenSetForTheFirstTime = true;

        double dividerPosition = deepThought.getSettings().getMainWindowTabsAndEntriesOverviewDividerPosition() / stage.getWidth();
        contentPane.setDividerPosition(0, dividerPosition);
      }
      else {
        double newTabsControlWidth = newValue.doubleValue() * stage.getWidth();
        deepThought.getSettings().setMainWindowTabsAndEntriesOverviewDividerPosition(newTabsControlWidth);
      }
    }
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

//    parent.removeSubCategory(category);
    deepThought.removeCategory(category);
  }

  @FXML
  public void handleMenuItemFileCloseAction(ActionEvent event) {
    stage.close();
  }

  protected ChangeListener<Boolean> checkMenuItemViewDialogsFieldsDisplayShowImportantOnesSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      Application.getSettings().setDialogsFieldsDisplay(DialogsFieldsDisplay.ImportantOnes);
    }
  };

  protected ChangeListener<Boolean> checkMenuItemViewDialogsFieldsDisplayShowAllSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      Application.getSettings().setDialogsFieldsDisplay(DialogsFieldsDisplay.All);
    }
  };

  protected ChangeListener<Boolean> checkMenuItemViewReferencesDisplayShowOnlyReferenceSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      Application.getSettings().setReferencesDisplay(ReferencesDisplay.ShowOnlyReference);
    }
  };

  protected ChangeListener<Boolean> checkMenuItemViewReferencesDisplayShowAllSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      Application.getSettings().setReferencesDisplay(ReferencesDisplay.ShowAll);
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
    Point2D buttonCoordinates = FXUtils.getNodeScreenCoordinates(btnAddCategory);

    final double centerX = buttonCoordinates.getX() + btnAddCategory.getWidth() / 2;
    final double y = buttonCoordinates.getY() + btnAddCategory.getHeight() + 6;

    Dialogs.showEditCategoryDialog(new Category(), centerX, y, stage, true);
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


  protected void setMenuFileClipboard(ContentExtractOptions contentExtractOptions) {
    mnitmFileClipboard.getItems().clear();
    mnitmFileClipboard.setDisable(contentExtractOptions.hasContentExtractOptions() == false);

    if (contentExtractOptions.hasContentExtractOptions()) {
      List<ContentExtractOptionForUi> options = contentExtractOptionForUiCreator.createOptions(contentExtractOptions);

      for (ContentExtractOptionForUi option : options) {
        addClipboardMenuItem(option);
      }
    }
  }

  protected void addClipboardMenuItem(final ContentExtractOptionForUi option) {
    final MenuItem optionMenu = new MenuItem(option.getDisplayName());
    if(option.getShortCut() != null) {
      optionMenu.setAccelerator(option.getShortCut());
    }

    mnitmFileClipboard.getItems().add(optionMenu);

    optionMenu.setOnAction(action -> option.runAction(null));
  }


  protected void handleMenuToolsLanguageShowing(Event event) { // TODO: why setting items on each showing, not once and then only react to language changes?
    mnitmToolsLanguage.getItems().clear();
    ApplicationLanguage currentLanguage = Application.getSettings().getLanguage();

    for(final ApplicationLanguage language : Application.getApplication().getApplicationLanguages()) {
      CheckMenuItem languageItem = new CheckMenuItem();
      JavaFxLocalization.bindMenuItemText(languageItem, language.getName());
      languageItem.setSelected(language.equals(currentLanguage));
      languageItem.setOnAction(menuEvent -> applicationLanguageChanged(language));

      mnitmToolsLanguage.getItems().add(languageItem);
    }
  }

  protected void applicationLanguageChanged(ApplicationLanguage language) {
    Application.getSettings().setLanguage(language);
  }

  @FXML
  public void handleMenuItemToolsDeviceRegistrationAction(Event event) {
    Dialogs.showRegisterUserDevicesDialog(stage);
  }

  @FXML
  public void handleMenuItemToolsBackupsAction(Event event) {
    Dialogs.showRestoreBackupDialog(stage);
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

    clipboardWatcher = new JavaFxClipboardWatcher(stage, clipboardContentChangedExternallyListener);
    this.createEntryFromClipboardContentPopup = new CreateEntryFromClipboardContentPopup(stage, clipboardWatcher);
    this.contentExtractOptionForUiCreator = new ContentExtractOptionForUiCreator(stage);

    stage.setOnHiding(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        windowClosing();
      }
    });
  }

  protected SettingsChangedListener userDeviceSettingsChangedListener = new SettingsChangedListener() {
    @Override
    public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
      FXUtils.runOnUiThread(() -> reactToUserDeviceSettingsChanged(setting, newValue));
    }
  };

  protected void reactToUserDeviceSettingsChanged(Setting setting, Object newValue) {
    if(setting == Setting.UserDeviceShowQuickEditEntryPane) {
      boolean showQuickEditEntryPane = (boolean)newValue;
      entriesOverviewControl.showPaneQuickEditEntryChanged(showQuickEditEntryPane);
      if(chkmnitmViewShowQuickEditEntryPane.isSelected() != showQuickEditEntryPane) {
        setMenuItemViewShowQuickEditEntryPaneWithoutInvokingListener(showQuickEditEntryPane);
      }
    }
    else if(setting == Setting.UserDeviceShowCategories) {
      boolean showCategories = (boolean)newValue;
      showCategoriesChanged(showCategories);
      if(chkmnitmViewShowCategories.isSelected() != showCategories) {
        setMenuItemViewShowCategoriesWithoutInvokingListener(showCategories);
      }
    }
    else if(setting == Setting.UserDeviceDialogFieldsDisplay) {
      DialogsFieldsDisplay dialogsFieldsDisplay = (DialogsFieldsDisplay)newValue;
      setMenuItemViewDialogsFieldsDisplayShowImportantOnesWithoutInvokingListener(dialogsFieldsDisplay);
      setMenuItemViewDialogsFieldsDisplayShowAllWithoutInvokingListener(dialogsFieldsDisplay);
    }
    else if(setting == Setting.UserDeviceReferencesDisplay) {
      ReferencesDisplay referencesDisplay = (ReferencesDisplay)newValue;
      setMenuItemViewReferencesDisplayShowOnlyReferenceWithoutInvokingListener(referencesDisplay);
      setMenuItemViewReferencesDisplayShowAllWithoutInvokingListener(referencesDisplay);
    }
  }

  protected ClipboardContentChangedListener clipboardContentChangedExternallyListener = new ClipboardContentChangedListener() {
    @Override
    public void clipboardContentChanged(final ContentExtractOptions contentExtractOptions) {
      FXUtils.runOnUiThread(() -> setMenuFileClipboard(contentExtractOptions));
    }
  };


  protected ImportFilesOrDoOcrListener importFilesOrDoOcrListener = new ImportFilesOrDoOcrListener() {

    @Override
    public void importFiles(ImportFilesRequest request) {
      // TODO
    }

    @Override
    public void doOcr(DoOcrRequest request) {
      // TODO
    }

    @Override
    public void scanBarcode(RequestWithAsynchronousResponse request) {
      // TODO
    }

    @Override
    public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {
      // TODO
    }
  };

}
