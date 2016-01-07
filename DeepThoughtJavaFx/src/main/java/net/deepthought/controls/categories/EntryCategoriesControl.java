package net.deepthought.controls.categories;

import net.deepthought.Application;
import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.Constants;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.controls.ICleanUp;
import net.deepthought.controls.event.EntryCategoriesEditedEvent;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;

import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryCategoriesControl extends CollapsiblePane implements IEditedEntitiesHolder<Category>, ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(EntryCategoriesControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected ObservableSet<Category> editedEntryCategories = FXCollections.observableSet();
  protected Set<Category> addedCategories = new HashSet<>();
  protected Set<Category> removedCategories = new HashSet<>();

  protected ObservableList<Tag> listViewAllTagsItems = null;
  protected FilteredList<Tag> filteredTags = null;

  protected List<EntryCategoryTreeCell> entryCategoryTreeCells = new ArrayList<>();

  protected EventHandler<EntryCategoriesEditedEvent> categoryAddedEventHandler = null;
  protected EventHandler<EntryCategoriesEditedEvent> categoryRemovedEventHandler = null;


  protected Label lblCategories;
  @FXML
  protected FlowPane pnSelectedCategoriesPreview;

  protected Button btnAddTopLevelCategory;

  @FXML
  protected VBox pnContent;
  @FXML
  protected HBox pnSearchCategories;
  @FXML
  protected TextField txtfldSearchCategories;
  @FXML
  protected Button btnCreateCategory;
  @FXML
  protected TreeView<Category> trvwCategories;


  public EntryCategoriesControl(Entry entry) {
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    setupControl();

    if(deepThought != null)
      deepThought.addEntityListener(deepThoughtListener);

    setEntry(entry);
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      EntryCategoriesControl.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };

  public void cleanUp() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    clearEntryCategoryLabels();

    ((TopLevelCategoryTreeItem)trvwCategories.getRoot()).cleanUp();
    trvwCategories.setRoot(null);

    for(EntryCategoryTreeCell cell : entryCategoryTreeCells)
      cell.cleanUp();

    categoryAddedEventHandler = null;
    categoryRemovedEventHandler = null;
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    listViewAllTagsItems.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
      listViewAllTagsItems.addAll(deepThought.getTags());
    }
  }

  protected void setupControl() {
    this.setExpanded(false);

    setupTitle();

    setupContent();

    showEntryCategories();
  }

  protected void setupTitle() {
    HBox titlePane = new HBox();
    titlePane.setAlignment(Pos.CENTER_LEFT);
    titlePane.setPrefWidth(USE_COMPUTED_SIZE);
    titlePane.setMaxWidth(FXUtils.SizeMaxValue);
//    titlePane.setPrefHeight(USE_COMPUTED_SIZE);
//    titlePane.setMinHeight(USE_PREF_SIZE);
    titlePane.setMaxHeight(FXUtils.SizeMaxValue);

    lblCategories = new Label();
    JavaFxLocalization.bindLabeledText(lblCategories, "categories");
    lblCategories.setMinHeight(22);
    lblCategories.setPrefWidth(USE_COMPUTED_SIZE);
    lblCategories.setMinWidth(USE_PREF_SIZE);
    lblCategories.setMaxWidth(USE_PREF_SIZE);
    titlePane.getChildren().add(lblCategories);
    HBox.setMargin(lblCategories, new Insets(0, 4, 0, 4));

    pnSelectedCategoriesPreview = new FlowPane();
    pnSelectedCategoriesPreview.setColumnHalignment(HPos.LEFT);
    pnSelectedCategoriesPreview.setRowValignment(VPos.CENTER);
    pnSelectedCategoriesPreview.setAlignment(Pos.CENTER_LEFT);
    pnSelectedCategoriesPreview.setVgap(2);
    titlePane.getChildren().add(pnSelectedCategoriesPreview);
    HBox.setHgrow(pnSelectedCategoriesPreview, Priority.ALWAYS);

    btnAddTopLevelCategory = new Button();
    btnAddTopLevelCategory.setMinHeight(24);
    btnAddTopLevelCategory.setMaxHeight(24);
    btnAddTopLevelCategory.setMinWidth(24);
    btnAddTopLevelCategory.setMaxWidth(24);
    btnAddTopLevelCategory.setFont(new Font(9.5));
    btnAddTopLevelCategory.setText("+");
    btnAddTopLevelCategory.setTextFill(Constants.AddEntityButtonTextColor);
    JavaFxLocalization.bindControlToolTip(btnAddTopLevelCategory, "create.new.top.level.category.tool.tip");
    titlePane.getChildren().add(btnAddTopLevelCategory);
    HBox.setMargin(btnAddTopLevelCategory, new Insets(0, 0, 0, 4));

    btnAddTopLevelCategory.setOnAction(event -> handleButtonAddTopLevelCategoryAction(event));

    setTitle(titlePane);
  }

  protected void setupContent() {
    pnContent = new VBox();
    pnContent.setMinHeight(230);
//    pnContent.setMinHeight(268); // as long as there's not search bar, reise TreeView's min height to fit with EntryTag's Control height

    setupPaneSearchCategories();

    trvwCategories = new TreeView<Category>(new TopLevelCategoryTreeItem());
    trvwCategories.setMinHeight(230);
//    trvwCategories.setMinHeight(268); // as long as there's not search bar, reise TreeView's min height to fit with EntryTag's Control height
    trvwCategories.setMaxHeight(FXUtils.SizeMaxValue);
    trvwCategories.setMaxWidth(FXUtils.SizeMaxValue);
    trvwCategories.setShowRoot(false);
    trvwCategories.setEditable(true);

    trvwCategories.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        toggleSelectedCategoriesAffiliation();
        event.consume();
      } else if (event.getCode() == KeyCode.DELETE) {
        deleteSelectedCategories();
        event.consume();
      }
    });

    trvwCategories.setCellFactory(treeView -> {
      EntryCategoryTreeCell cell = new EntryCategoryTreeCell(this);
      entryCategoryTreeCells.add(cell);
      return cell;
    });

    pnContent.getChildren().add(trvwCategories);
    VBox.setVgrow(trvwCategories, Priority.ALWAYS);
    VBox.setMargin(trvwCategories, new Insets(6, 0, 0, 0));

    setContent(pnContent);
  }

  protected void setupPaneSearchCategories() {
    pnSearchCategories = new HBox();
    pnSearchCategories.setAlignment(Pos.CENTER_LEFT);
    pnSearchCategories.setPrefHeight(40);
    pnSearchCategories.setVisible(false);
    pnSearchCategories.setManaged(false);

    txtfldSearchCategories = TextFields.createClearableTextField();
    txtfldSearchCategories.setId("txtfldSearchCategories");
    JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchCategories, "search.categories.prompt.text");
    pnSearchCategories.getChildren().add(txtfldSearchCategories);
    txtfldSearchCategories.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        txtfldSearchCategories.clear();
        event.consume();
      }
    });
    HBox.setHgrow(txtfldSearchCategories, Priority.ALWAYS);

    btnCreateCategory = new Button();
    btnCreateCategory.setId("btnCreateCategory");
    btnCreateCategory.setOnAction(event -> handleButtonCreateCategoryAction(event));
    pnSearchCategories.getChildren().add(btnCreateCategory);
    JavaFxLocalization.bindLabeledText(btnCreateCategory, "new...");

    pnContent.getChildren().add(pnSearchCategories);
    VBox.setMargin(pnSearchCategories, new Insets(6, 0, 0, 0));
  }

  protected void showEntryCategoriesThreadSafe() {
    if(Platform.isFxApplicationThread()) {
      showEntryCategories();
    }
    else {
      Platform.runLater(() -> showEntryCategories());
    }
  }

  protected void showEntryCategories() {
    clearEntryCategoryLabels();

    for (final Category category : editedEntryCategories) {
      pnSelectedCategoriesPreview.getChildren().add(new EntryCategoryLabel(category, event -> {
        removeEntityFromEntry(category);
      }));
    }
  }

  protected void clearEntryCategoryLabels() {
    FXUtils.cleanUpChildrenAndClearPane(pnSelectedCategoriesPreview);
  }


  protected void setControlsForEnteredTagsSearch(String newValue) {
    searchCategories(newValue);
    btnCreateCategory.setDisable(checkIfCategoryOfThatNameExists(newValue));
  }

  protected boolean checkIfCategoryOfThatNameExists(String tagName) {
    if(tagName == null || tagName.isEmpty())
      return true;

    if(checkIfSystemCategoryOfThatNameExists(tagName))
      return true;

    for(Tag tag : Application.getDeepThought().getTags()) {
      if(tagName.equals(tag.getName()))
        return true;
    }

    return false;
  }

  protected boolean checkIfSystemCategoryOfThatNameExists(String tagName) { // dankl, you're so dumb: We're in Categories, not Tags
    return Localization.getLocalizedString("system.tag.all.entries").equals(tagName) ||
        Localization.getLocalizedString("system.tag.entries.with.no.tags").equals(tagName);
  }

  protected void searchCategories(String searchTerm) {
    filteredTags.setPredicate((category) -> {
      // If filter text is empty, display all Tags.
      if (searchTerm == null || searchTerm.isEmpty()) {
        return true;
      }

      String lowerCaseSearchTerm = searchTerm.toLowerCase();

      if (category.getName().toLowerCase().contains(lowerCaseSearchTerm)) {
        return true; // Search Term matches Tag's name
      }
      return false; // Does not match.
    });
  }

  @Override
  public ObservableSet<Category> getEditedEntities() {
    return editedEntryCategories;
  }

  @Override
  public Set<Category> getAddedEntities() {
    return addedCategories;
  }

  @Override
  public Set<Category> getRemovedEntities() {
    return removedCategories;
  }

  protected void addNewCategoryToEntry() {
    String newCategoryName = txtfldSearchCategories.getText();
    Category newCategory = new Category(newCategoryName);
    Application.getDeepThought().addCategory(newCategory);

    addEntityToEntry(newCategory);

    btnCreateCategory.setDisable(true);
  }

  @Override
  public void addEntityToEntry(Category category) {
    if(removedCategories.contains(category))
      removedCategories.remove(category);
    else
      addedCategories.add(category);

    editedEntryCategories.add(category);

    showEntryCategories();
    fireCategoryAddedEvent(category);
  }

  @Override
  public void removeEntityFromEntry(Category category) {
    if(addedCategories.contains(category))
      addedCategories.remove(category);
    else
      removedCategories.add(category);

    editedEntryCategories.remove(category);

    showEntryCategories();
    fireCategoryRemovedEvent(category);
  }

  @Override
  public boolean containsEditedEntity(Category entity) {
    return editedEntryCategories.contains(entity);
  }


  protected void toggleCategoryAffiliation(Category category) {
    if(category == null)
      return;

    if(containsEditedEntity(category) == false)
      addEntityToEntry(category);
    else
      removeEntityFromEntry(category);
  }

  protected void toggleSelectedCategoriesAffiliation() {
    for(Category selectedCategory : getSelectedCategories()) {
      toggleCategoryAffiliation(selectedCategory);
    }
  }

  protected void deleteSelectedCategories() {
    for(Category selectedCategory : getSelectedCategories()) {
      if(Alerts.deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(deepThought, selectedCategory)) {
        if(containsEditedEntity(selectedCategory))
          removeEntityFromEntry(selectedCategory);
      }
    }
  }

  protected Collection<Category> getSelectedCategories() {
    List<Category> selectedCategories = new ArrayList<>(); // make a copy as when multiple Categories are selected after removing first one SelectionModel gets cleared
    for(TreeItem<Category> selectedItem : trvwCategories.getSelectionModel().getSelectedItems())
      selectedCategories.add(selectedItem.getValue());

    return selectedCategories;
  }


  public void setEntry(Entry entry) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = entry;

    clearEditedCategoriesSets();

    if(this.entry != null) {
      editedEntryCategories.addAll(entry.getCategories());
      this.entry.addEntityListener(entryListener);
    }

    setDisable(entry == null);
    txtfldSearchCategories.clear();
    showEntryCategories();
  }

  public void setEntryCategories(Collection<Category> categories) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = null;

    clearEditedCategoriesSets();

    editedEntryCategories.addAll(categories);

    setDisable(false);
    txtfldSearchCategories.clear();
    showEntryCategories();
  }

  protected void clearEditedCategoriesSets() {
    editedEntryCategories.clear();
    addedCategories.clear();
    removedCategories.clear();
  }


  @FXML
  public void handleButtonCreateCategoryAction(ActionEvent event) {
    addNewCategoryToEntry();
  }

  @FXML
  public void handleButtonAddTopLevelCategoryAction(ActionEvent event) {
    final Category newCategory = new Category();

    Dialogs.showEditCategoryDialog(newCategory, getScene().getWindow(), true, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {

      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok) {
          addEntityToEntry(newCategory);
          trvwCategories.getSelectionModel().clearSelection();
          trvwCategories.getSelectionModel().selectLast();
          scrollToSelectedItem();
        }
      }
    });
  }

  protected void scrollToSelectedItem() {
    trvwCategories.scrollTo(trvwCategories.getSelectionModel().getSelectedIndex());
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity instanceof Category) {
        handleCategoryAddedToEntryThreadSafe((Category) addedEntity);
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity instanceof Category) {
        handleCategoryRemovedFromEntryThreadSafe((Category) removedEntity);
      }
    }
  };

  protected void handleCategoryAddedToEntryThreadSafe(final Category addedEntity) {
    if(Platform.isFxApplicationThread()) {
      handleCategoryAddedToEntry(addedEntity);
    }
    else {
      Platform.runLater(() -> handleCategoryAddedToEntry(addedEntity));
    }
  }

  protected void handleCategoryAddedToEntry(Category addedEntity) {
    editedEntryCategories.add(addedEntity);
    showEntryCategories();
  }

  protected void handleCategoryRemovedFromEntryThreadSafe(final Category removedEntity) {
    if(Platform.isFxApplicationThread()) {
      handleCategoryRemovedFromEntry(removedEntity);
    }
    else {
      Platform.runLater(() -> handleCategoryRemovedFromEntry(removedEntity));
    }
  }

  protected void handleCategoryRemovedFromEntry(Category removedEntity) {
    editedEntryCategories.remove(removedEntity);
    showEntryCategories();
  }

  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
//      checkIfCategoriesHaveBeenUpdated(collectionHolder, addedEntity);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
//      checkIfCategoriesHaveBeenUpdated(collectionHolder, updatedEntity);

      if(updatedEntity instanceof Category && entry != null && ((Category)updatedEntity).getEntries().contains(entry)) {
        showEntryCategoriesThreadSafe();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
//      checkIfCategoriesHaveBeenUpdated(collectionHolder, removedEntity);
    }
  };

//  protected void checkIfCategoriesHaveBeenUpdated(BaseEntity collectionHolder, BaseEntity entity) {
//    if(collectionHolder instanceof DeepThought && entity instanceof Category) {
//      DeepThought deepThought = (DeepThought)collectionHolder;
//      resetListViewAllTagsItems(deepThought);
//    }
//  }
//
//  protected void resetListViewAllTagsItems(DeepThought deepThought) {
//    listViewAllTagsItems.clear();
//    listViewAllTagsItems.addAll(deepThought.getTags());
//  }


  protected void fireCategoryAddedEvent(Category category) {
    if(categoryAddedEventHandler != null)
      categoryAddedEventHandler.handle(new EntryCategoriesEditedEvent(this, category));
  }

  protected void fireCategoryRemovedEvent(Category category) {
    if(categoryRemovedEventHandler != null)
      categoryRemovedEventHandler.handle(new EntryCategoriesEditedEvent(this, category));
  }

  public EventHandler<EntryCategoriesEditedEvent> getCategoryAddedEventHandler() {
    return categoryAddedEventHandler;
  }

  public void setCategoryAddedEventHandler(EventHandler<EntryCategoriesEditedEvent> categoryAddedEventHandler) {
    this.categoryAddedEventHandler = categoryAddedEventHandler;
  }

  public EventHandler<EntryCategoriesEditedEvent> getCategoryRemovedEventHandler() {
    return categoryRemovedEventHandler;
  }

  public void setCategoryRemovedEventHandler(EventHandler<EntryCategoriesEditedEvent> categoryRemovedEventHandler) {
    this.categoryRemovedEventHandler = categoryRemovedEventHandler;
  }


  public Set<Category> getEditedEntryCategories() {
    return editedEntryCategories;
  }

  public Set<Category> getAddedCategories() {
    return addedCategories;
  }

  public Set<Category> getRemovedCategories() {
    return removedCategories;
  }
}
