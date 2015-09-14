package net.deepthought.controls.categories;

import net.deepthought.Application;
import net.deepthought.controller.Dialogs;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.Constants;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.event.EntryCategoriesEditedEvent;
import net.deepthought.controls.tag.IEditedEntitiesHolder;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryCategoriesControl extends CollapsiblePane implements IEditedEntitiesHolder<Category>, ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(EntryCategoriesControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected ObservableSet<Category> editedEntryCategories = FXCollections.observableSet();
  protected Set<Category> addedCategories = new HashSet<>();
  protected Set<Category> removedCategories = new HashSet<>();

  protected ObservableList<Tag> listViewAllTagsItems = null;
  protected FilteredList<Tag> filteredTags = null;
  protected SortedList<Tag> sortedFilteredTags = null;

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
  protected HBox pnFilterCategories;
  @FXML
  protected TextField txtfldFilterCategories;
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

  public void cleanUpControl() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    clearEntryCategoryLabels();

    ((TopLevelCategoryTreeItem)trvwCategories.getRoot()).cleanUpControl();
    trvwCategories.setRoot(null);

    for(EntryCategoryTreeCell cell : entryCategoryTreeCells)
      cell.cleanUpControl();

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

    pnContent = new VBox();

    setupPaneFilterCategories();

    trvwCategories = new TreeView<Category>(new TopLevelCategoryTreeItem());
    trvwCategories.setMinHeight(230);
    trvwCategories.setMaxHeight(Double.MAX_VALUE);
    trvwCategories.setMaxWidth(Double.MAX_VALUE);
    trvwCategories.setShowRoot(false);
    trvwCategories.setEditable(true);

    trvwCategories.setCellFactory(treeView -> {
      EntryCategoryTreeCell cell = new EntryCategoryTreeCell(this);
      entryCategoryTreeCells.add(cell);
      return cell;
    });

    pnContent.getChildren().add(trvwCategories);
    VBox.setVgrow(trvwCategories, Priority.ALWAYS);
    VBox.setMargin(trvwCategories, new Insets(6, 0, 0, 0));

    setContent(pnContent);

    showEntryCategories();
  }

  protected void setupPaneFilterCategories() {
    pnFilterCategories = new HBox();
    pnFilterCategories.setAlignment(Pos.CENTER_LEFT);
    pnFilterCategories.setPrefHeight(40);
    pnFilterCategories.setVisible(false);
    pnFilterCategories.setManaged(false);

    txtfldFilterCategories = new TextField();
    pnFilterCategories.getChildren().add(txtfldFilterCategories);
    HBox.setHgrow(txtfldFilterCategories, Priority.ALWAYS);

    btnCreateCategory = new Button();
    btnCreateCategory.setOnAction(event -> handleButtonCreateCategoryAction(event));
    pnFilterCategories.getChildren().add(btnCreateCategory);
    JavaFxLocalization.bindLabeledText(btnCreateCategory, "new...");

    pnContent.getChildren().add(pnFilterCategories);
    VBox.setMargin(pnFilterCategories, new Insets(6, 0, 0, 0));
  }

  protected void setupTitle() {
    HBox titlePane = new HBox();
    titlePane.setAlignment(Pos.CENTER_LEFT);
    titlePane.setPrefWidth(USE_COMPUTED_SIZE);
    titlePane.setMaxWidth(Double.MAX_VALUE);
//    titlePane.setPrefHeight(USE_COMPUTED_SIZE);
//    titlePane.setMinHeight(USE_PREF_SIZE);
    titlePane.setMaxHeight(Double.MAX_VALUE);

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
    titlePane.getChildren().add(btnAddTopLevelCategory);
    HBox.setMargin(btnAddTopLevelCategory, new Insets(0, 0, 0, 4));

    btnAddTopLevelCategory.setOnAction(event -> Dialogs.showEditCategoryDialog(new Category(), getScene().getWindow(), true));

    setTitle(titlePane);
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


  protected void setControlsForEnteredTagsFilter(String newValue) {
    filterCategories(newValue);
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

  protected void filterCategories(String filterConstraint) {
    filteredTags.setPredicate((category) -> {
      // If filter text is empty, display all Tags.
      if (filterConstraint == null || filterConstraint.isEmpty()) {
        return true;
      }

      String lowerCaseFilterConstraint = filterConstraint.toLowerCase();

      if (category.getName().toLowerCase().contains(lowerCaseFilterConstraint)) {
        return true; // Filter matches Tag's name
      }
      return false; // Does not match.
    });
  }

  @Override
  public ObservableSet<Category> getEditedEntities() {
    return editedEntryCategories;
  }

  protected void addNewCategoryToEntry() {
    String newCategoryName = txtfldFilterCategories.getText();
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
    txtfldFilterCategories.clear();
    showEntryCategories();
  }

  public void setEntryCategories(Collection<Category> categories) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = null;

    clearEditedCategoriesSets();

    editedEntryCategories.addAll(categories);

    setDisable(false);
    txtfldFilterCategories.clear();
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
  public void handleButtonAddCategoryAction(ActionEvent event) {
    Category newCategory = new Category();
    Application.getDeepThought().addCategory(newCategory);

//    newCategory.addEntry(entry);
    addEntityToEntry(newCategory);
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity instanceof Category)
        editedEntryCategories.add((Category)addedEntity);
      showEntryCategories();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity instanceof Category)
        editedEntryCategories.remove((Category)removedEntity);
      showEntryCategories();
    }
  };

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

      if(updatedEntity instanceof Category && entry != null && ((Category)updatedEntity).getEntries().contains(entry))
        showEntryCategories();
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
