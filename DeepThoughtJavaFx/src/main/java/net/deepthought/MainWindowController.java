/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.deepthought;

import net.deepthought.communication.listener.CaptureImageOrDoOcrListener;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.messages.CaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.StopCaptureImageOrDoOcrRequest;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.controller.Dialogs;
import net.deepthought.controls.Constants;
import net.deepthought.controls.CreateEntryFromClipboardContentPopup;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.entries.EntriesOverviewControl;
import net.deepthought.controls.tabcategories.CategoryTreeCell;
import net.deepthought.controls.tabcategories.CategoryTreeItem;
import net.deepthought.controls.tabtags.TabTagsControl;
import net.deepthought.data.contentextractor.ClipboardContent;
import net.deepthought.data.contentextractor.ContentExtractOption;
import net.deepthought.data.contentextractor.ContentExtractOptions;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.contentextractor.JavaFxClipboardContent;
import net.deepthought.data.contentextractor.OptionInvokedListener;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Device;
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
import net.deepthought.util.IconManager;
import net.deepthought.util.InputManager;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;
import net.deepthought.util.NotificationType;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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

  protected CreateEntryFromClipboardContentPopup createEntryFromClipboardContentPopup;

  protected CategoryTreeItem selectedCategoryTreeItem = null;


  @FXML
  GridPane grdpnMainMenu;

  @FXML
  protected Menu mnitmFile;
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
  protected CheckMenuItem chkmnitmViewShowCategories;
  @FXML
  protected CheckMenuItem chkmnitmViewShowQuickEditEntryPane;

  @FXML
  protected Menu mnitmToolsLanguage;


  @FXML
  protected MenuButton btnOnlineArticleExtractors;

  @FXML
  protected Pane statusBar;
  @FXML
  protected Label statusLabel;
  @FXML
  protected Label statusLabelCountEntries;
  @FXML
  protected Pane pnConnectedDevices;


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



  @Override
  public void initialize(URL url, ResourceBundle rb) {
    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        deepThoughtChangedThreadSafe(deepThought);
      }

      @Override
      public void notification(Notification notification) {
        if(notification.getType() == NotificationType.ApplicationInstantiated)
          tabTagsControl.applicationInstantiated();
        notifyUserThreadSafe(notification);
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

  protected void notifyUserThreadSafe(Notification notification) {
    if(Platform.isFxApplicationThread())
      notifyUser(notification);
    else
      Platform.runLater(() -> notifyUser(notification));
  }

  protected void notifyUser(Notification notification) {
    if(notification instanceof DeepThoughtError)
      showErrorOccurredMessage((DeepThoughtError) notification);
    else if(notification.getType() == NotificationType.Info)
      showInfoMessage(notification);
    else if(notification.getType() == NotificationType.PluginLoaded) {
      showInfoMessage(notification);
      if(notification.getParameter() instanceof IPlugin) {
        pluginLoaded((IPlugin)notification.getParameter());
      }
    }
    else if(notification.getType() == NotificationType.DeepThoughtsConnectorStarted) {
      Application.getDeepThoughtsConnector().addConnectedDevicesListener(connectedDevicesListener);
      Application.getDeepThoughtsConnector().addCaptureImageOrDoOcrListener(captureImageOrDoOcrListener);
      for(ConnectedDevice connectedDevice : Application.getDeepThoughtsConnector().getConnectedDevicesManager().getConnectedDevices())
        addConnectedDeviceIcon(connectedDevice);
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

  protected void pluginLoaded(IPlugin plugin) {
    if(plugin instanceof IOnlineArticleContentExtractor) {
      onlineArticleContentExtractorPluginLoaded((IOnlineArticleContentExtractor) plugin);
    }
  }

  protected void onlineArticleContentExtractorPluginLoaded(IOnlineArticleContentExtractor onlineArticleContentExtractor) {
    if(onlineArticleContentExtractor.hasArticlesOverview()) {
      MenuItem articleContentExtractorMenuItem = new MenuItem(onlineArticleContentExtractor.getSiteBaseUrl());
      articleContentExtractorMenuItem.setOnAction(event -> Dialogs.showArticlesOverviewDialog(onlineArticleContentExtractor));

      if(onlineArticleContentExtractor.getIconUrl() != IOnlineArticleContentExtractor.NoIcon) {
        HBox graphicsPane = new HBox(new ImageView(onlineArticleContentExtractor.getIconUrl()));
        graphicsPane.setPrefWidth(38);
        graphicsPane.setMaxWidth(38);
        graphicsPane.setAlignment(Pos.CENTER);
        articleContentExtractorMenuItem.setGraphic(graphicsPane);
      }

      btnOnlineArticleExtractors.getItems().add(articleContentExtractorMenuItem);
//        btnOnlineArticleExtractors.setDisable(false);
      btnOnlineArticleExtractors.setVisible(true);
//        grdpnMainMenu.getColumnConstraints().get(1).setPrefWidth(btnOnlineArticleExtractors.getPrefWidth());
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
      Application.getSettings().removeSettingsChangedListener(userDeviceSettingsChangedListener);
    }

    this.deepThought = deepThought;

    setControlsEnabledState(deepThought != null);

    clearAllData();

    if(deepThought != null) {
      DeepThoughtSettings settings = deepThought.getSettings();

      trvwCategories.setRoot(new CategoryTreeItem(deepThought.getTopLevelCategory()));
      selectedCategoryChanged(deepThought.getTopLevelCategory());

      tabTagsControl.deepThoughtChanged(deepThought);
      entriesOverviewControl.deepThoughtChanged(deepThought);

      FXUtils.applyWindowSettingsAndListenToChanges(stage, settings.getMainWindowSettings());
      setSelectedTab(settings.getLastSelectedTab());

      userDeviceSettingsChanged(); // TODO: isn't this redundant with selecting Tab and current category?

//    if(deepThought.getLastViewedCategory() != null)
//      trvwCategories.getSelectionModel().(deepThought.getLastViewedCategory());

//      List<Entry> allEntries = new ArrayList<>(deepThought.getEntries());
//      for(int i = 0, length = deepThought.countEntries(); i < length; i++) {
//        Entry entry = allEntries.get(0);
//        entry.setEntryIndex(length - i);
//        ((DefaultDataManager)Application.dataManager).entityUpdated(entry);
//      }
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
    setStatusLabelText(Localization.getLocalizedString("backing.up.data"));
    Application.shutdown();

    for(Stage openedWindow : Dialogs.getOpenedChildWindows()) {
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

    if(contentPane.getDividers().size() > 0) {
      contentPane.getDividers().get(0).positionProperty().addListener(((observableValue, oldValue, newValue) -> {
        if (deepThought != null) {
          double newTabsControlWidth = newValue.doubleValue() * stage.getWidth();
          deepThought.getSettings().setMainWindowTabsAndEntriesOverviewDividerPosition(newTabsControlWidth);
        }
      }));
    }
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

    trvwCategories.setOnContextMenuRequested(event -> showTreeViewCategoriesContextMenu(event));

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

    if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Categories)
      tbpnOverview.getSelectionModel().select(tabTags);
  }

  protected void showTreeViewCategoriesContextMenu(ContextMenuEvent event) {
    ContextMenu contextMenu = createTreeViewCategoriesContextMenu();

    contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
  }

  protected ContextMenu createTreeViewCategoriesContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem addCategoryMenuItem = new MenuItem("Add Category");
    contextMenu.getItems().add(addCategoryMenuItem);

    addCategoryMenuItem.setOnAction(event -> {
      deepThought.addCategory(new Category());
    });

    return contextMenu;
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


  @FXML
  public void handleMainMenuFileShowing(Event event) {
    ClipboardContent clipboardContent = new JavaFxClipboardContent(Clipboard.getSystemClipboard());
    ContentExtractOptions contentExtractOptions = Application.getContentExtractorManager().getContentExtractorOptionsForClipboardContent(clipboardContent);

    mnitmFileClipboard.getItems().clear();
    mnitmFileClipboard.setDisable(contentExtractOptions.hasContentExtractOptions() == false);

    if(contentExtractOptions.hasContentExtractOptions()) {
      setMenuFileClipboard(contentExtractOptions);
    }
  }

  protected void setMenuFileClipboard(ContentExtractOptions contentExtractOptions) {
    if(contentExtractOptions.isOnlineArticleContentExtractor())
      createOnlineArticleClipboardMenuItems(contentExtractOptions);
//    else if(contentExtractOptions.isRemoteFileContentExtractor())
//      createRemoteFileClipboardMenuItems(contentExtractOptions);
//    else if(contentExtractOptions.isLocalFileContentExtractor())
//      createLocalFileClipboardMenuItems(contentExtractOptions);
    else if(contentExtractOptions.isUrl())
      createLocalFileClipboardMenuItems(contentExtractOptions);
  }

  protected void createOnlineArticleClipboardMenuItems(ContentExtractOptions contentExtractOptions) {
    final ContentExtractOption contentExtractOption = contentExtractOptions.getContentExtractOptions().get(0);
    final IOnlineArticleContentExtractor contentExtractor = (IOnlineArticleContentExtractor)contentExtractOption.getContentExtractor();

    addClipboardMenuItem(contentExtractOptions, "create.entry.from.online.article.option.directly.add.entry", InputManager.getInstance().getCreateEntryFromClipboardDirectlyAddEntryKeyCombination(),
        options -> createEntryFromClipboardContentPopup.directlyAddEntryFromOnlineArticle(contentExtractOption, contentExtractor));
    addClipboardMenuItem(contentExtractOptions, "create.entry.from.online.article.option.view.new.entry.first", InputManager.getInstance().getCreateEntryFromClipboardViewNewEntryFirstKeyCombination(),
        options -> createEntryFromClipboardContentPopup.createEntryFromOnlineArticleButViewFirst(contentExtractOption, contentExtractor));
  }

  protected void createRemoteFileClipboardMenuItems(ContentExtractOptions contentExtractOptions) {

  }

  protected void createLocalFileClipboardMenuItems(ContentExtractOptions contentExtractOptions) {
    if(contentExtractOptions.canSetFileAsEntryContent())
      addClipboardMenuItem(contentExtractOptions, "create.entry.from.local.file.option.set.as.entry.content",
        InputManager.getInstance().getCreateEntryFromClipboardSetAsEntryContentKeyCombination(), options -> createEntryFromClipboardContentPopup.copyFileToDataFolderAndSetAsEntryContent(options));

    if(contentExtractOptions.canAttachFileToEntry())
      addClipboardMenuItem(contentExtractOptions, "create.entry.from.local.file.option.add.as.file.attachment",
        InputManager.getInstance().getCreateEntryFromClipboardAddAsFileAttachmentKeyCombination(), options -> createEntryFromClipboardContentPopup.attachFileToEntry(options));

    if(contentExtractOptions.canExtractText())
      addClipboardMenuItem(contentExtractOptions, "create.entry.from.local.file.option.try.to.extract.text.from.it",
        InputManager.getInstance().getCreateEntryFromClipboardTryToExtractTextKeyCombination(), options -> createEntryFromClipboardContentPopup.tryToExtractText(options));

    if(contentExtractOptions.canAttachFileToEntry() && contentExtractOptions.canExtractText())
      addClipboardMenuItem(contentExtractOptions, "create.entry.from.local.file.option.add.as.file.attachment.and.try.to.extract.text.from.it",
        InputManager.getInstance().getCreateEntryFromClipboardAddAsFileAttachmentAndTryToExtractTextKeyCombination(), options -> createEntryFromClipboardContentPopup.attachFileToEntryAndTryToExtractText(options));
  }

  protected void addClipboardMenuItem(final ContentExtractOptions contentExtractOptions, String optionNameResourceKey, KeyCombination optionKeyCombination,
                                      final OptionInvokedListener listener) {
    final MenuItem optionMenu = new MenuItem(Localization.getLocalizedString(optionNameResourceKey));
//    if(optionKeyCombination != null)
    optionMenu.setAccelerator(optionKeyCombination);
    mnitmFileClipboard.getItems().add(optionMenu);

    optionMenu.setOnAction(action -> {
      if (listener != null)
        listener.optionInvoked(contentExtractOptions);
    });
  }


  protected void handleMenuToolsLanguageShowing(Event event) {
    mnitmToolsLanguage.getItems().clear();
    ApplicationLanguage currentLanguage = Application.getLoggedOnUser().getSettings().getLanguage();

    for(final ApplicationLanguage language : Application.getApplication().getApplicationLanguages()) {
      CheckMenuItem languageItem = new CheckMenuItem();
      JavaFxLocalization.bindMenuItemText(languageItem, language.getName());
      languageItem.setSelected(language.equals(currentLanguage));
      languageItem.setOnAction(menuEvent -> applicationLanguageChanged(language));

      mnitmToolsLanguage.getItems().add(languageItem);
    }
  }

  protected void applicationLanguageChanged(ApplicationLanguage language) {
    Application.getLoggedOnUser().getSettings().setLanguage(language);
    JavaFxLocalization.setLocaleForLanguage(language);
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

    this.createEntryFromClipboardContentPopup = new CreateEntryFromClipboardContentPopup(stage);

    stage.setOnHiding(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        windowClosing();
      }
    });

    stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (deepThought != null) {
          double dividerPosition = deepThought.getSettings().getMainWindowTabsAndEntriesOverviewDividerPosition() / newValue.doubleValue();
          contentPane.setDividerPosition(0, dividerPosition);
        }
      }
    });

    stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        mnitmFileClipboard.getItems().clear();
      }
    });
  }

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


  protected ConnectedDevicesListener connectedDevicesListener = new ConnectedDevicesListener() {

    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      Platform.runLater(() -> addConnectedDeviceIcon(device));
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      Platform.runLater(() -> removeConnectedDeviceIcon(device));
    }
  };

  protected Map<String, Node> connectedDeviceIcons = new HashMap<>();

  protected void addConnectedDeviceIcon(final ConnectedDevice connectedDevice) {
    if(connectedDeviceIcons.containsKey(connectedDevice.getUniqueDeviceId()))
      return;

    Device device = connectedDevice.getDevice();

    ImageView icon = new ImageView(IconManager.getInstance().getIconForOperatingSystem(device.getPlatform(), device.getOsVersion(), device.getPlatformArchitecture()));
    icon.setPreserveRatio(true);
    icon.setFitHeight(24);
    icon.maxHeight(24);
//    icon.setUserData(connectedDevice);

//    pnConnectedDevices.getChildren().add(icon);
//    HBox.setMargin(icon, new Insets(0, 4, 0, 0));

    Label label = new Label(null, icon);
    label.setUserData(connectedDevice);
    JavaFxLocalization.bindControlToolTip(label, "connected.device.tool.tip", connectedDevice.getDevice().getPlatform(), connectedDevice.getDevice().getOsVersion(),
        connectedDevice.getAddress(), connectedDevice.hasCaptureDevice(), connectedDevice.canDoOcr());

    pnConnectedDevices.getChildren().add(label);
    HBox.setMargin(label, new Insets(0, 4, 0, 0));
    connectedDeviceIcons.put(connectedDevice.getUniqueDeviceId(), label);
  }

  protected void removeConnectedDeviceIcon(ConnectedDevice device) {
    if(connectedDeviceIcons.containsKey(device.getUniqueDeviceId())) {
      Node icon = connectedDeviceIcons.remove(device.getUniqueDeviceId());
      pnConnectedDevices.getChildren().remove(icon);
    }
  }


  protected CaptureImageOrDoOcrListener captureImageOrDoOcrListener = new CaptureImageOrDoOcrListener() {
    @Override
    public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
      // TODO
    }

    @Override
    public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {
      // TODO
    }
  };

}
