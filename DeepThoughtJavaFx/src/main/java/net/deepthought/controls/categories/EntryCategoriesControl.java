package net.deepthought.controls.categories;

import net.deepthought.Application;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.event.EntryCategoriesEditedEvent;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryCategoriesControl extends TitledPane implements ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(EntryCategoriesControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected Set<Category> editedEntryCategories = new HashSet<>();
  protected Set<Category> addedCategories = new HashSet<>();
  protected Set<Category> removedCategories = new HashSet<>();

  protected ObservableList<Tag> listViewAllTagsItems = null;
  protected FilteredList<Tag> filteredTags = null;
  protected SortedList<Tag> sortedFilteredTags = null;

  protected List<EntryCategoryTreeCell> entryCategoryTreeCells = new ArrayList<>();

  protected EventHandler<EntryCategoriesEditedEvent> categoryAddedEventHandler = null;
  protected EventHandler<EntryCategoriesEditedEvent> categoryRemovedEventHandler = null;


  @FXML
  protected Pane pnGraphicsPane;
  @FXML
  protected FlowPane pnSelectedCategoriesPreview;

  @FXML
  protected Pane pnContent;
  @FXML
  protected Pane pnFilterCategories;
  @FXML
  protected TextField txtfldFilterCategories;
  @FXML
  protected Button btnCreateCategory;
  @FXML
  protected TreeView<Category> trvwCategories;


  public EntryCategoriesControl() {
    this(null);
  }

  public EntryCategoriesControl(Entry entry) {
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/EntryCategoriesControl.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);

      setEntry(entry);
    } catch (IOException ex) {
      log.error("Could not load EntryCategoriesControl", ex);
    }
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

    pnFilterCategories.setVisible(false);
    pnFilterCategories.setManaged(false);
//    // replace normal TextField txtfldFilterCategories with a SearchTextField (with a cross to clear selection)
//    pnFilterCategories.getChildren().remove(txtfldFilterCategories);
//    txtfldFilterCategories = TextFields.createClearableTextField();
//    pnFilterCategories.getChildren().add(0, txtfldFilterCategories);
//    HBox.setHgrow(txtfldFilterCategories, Priority.ALWAYS);
//    txtfldFilterCategories.setPromptText("Find tags to add");
//
//    txtfldFilterCategories.textProperty().addListener(new ChangeListener<String>() {
//      @Override
//      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//        setControlsForEnteredTagsFilter(newValue);
//      }
//    });
//    txtfldFilterCategories.setOnAction(new EventHandler<ActionEvent>() {
//      @Override
//      public void handle(ActionEvent event) {
//        if (checkIfCategoryOfThatNameExists(txtfldFilterCategories.getText()) == false)
//          addNewCategoryToEntry();
//      }
//    });


    trvwCategories.setRoot(new TopLevelCategoryTreeItem(entry));

    trvwCategories.setCellFactory(treeView -> {
      EntryCategoryTreeCell cell = new EntryCategoryTreeCell(entry, this);
      entryCategoryTreeCells.add(cell);
      return cell;
    });

    showEntryCategories(entry);
  }

  protected void showEntryCategories(Entry entry) {
    clearEntryCategoryLabels();

    if(entry != null) {
//      for(Category category : entry.getCategories()) {
      for(final Category category : editedEntryCategories) {
        pnSelectedCategoriesPreview.getChildren().add(new EntryCategoryLabel(entry, category, event -> {
          removeCategoryFromEntry(entry, category);

          for(EntryCategoryTreeCell cell : entryCategoryTreeCells) {
            if(category.equals(cell.category)) {
              cell.setComboBoxToUnselected();
              break;
            }
          }
        }));
      }
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

  protected void addNewCategoryToEntry() {
    String newCategoryName = txtfldFilterCategories.getText();
    Category newCategory = new Category(newCategoryName);
    Application.getDeepThought().addCategory(newCategory);

//    newCategory.addEntry(entry);
    addCategoryToEntry(entry, newCategory);

    btnCreateCategory.setDisable(true);
  }

  protected void addCategoryToEntry(Entry entry, Category category) {
    if(removedCategories.contains(category))
      removedCategories.remove(category);
    else
      addedCategories.add(category);

    editedEntryCategories.add(category);

    showEntryCategories(entry);
    fireCategoryAddedEvent(entry, category);
  }

  protected void removeCategoryFromEntry(Entry entry, Category category) {
    if(addedCategories.contains(category))
      addedCategories.remove(category);
    else
      removedCategories.add(category);

    editedEntryCategories.remove(category);

    showEntryCategories(entry);
    fireCategoryRemovedEvent(entry, category);
  }


  public void setEntry(Entry entry) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = entry;

    editedEntryCategories.clear();
    addedCategories.clear();
    removedCategories.clear();

    if(this.entry != null) {
      editedEntryCategories = new HashSet<>(entry.getCategories());
      this.entry.addEntityListener(entryListener);
    }

    setDisable(entry == null);
    txtfldFilterCategories.clear();
    showEntryCategories(entry);

    for(EntryCategoryTreeCell cell : entryCategoryTreeCells)
      cell.setEntry(this.entry);
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
    addCategoryToEntry(entry, newCategory);
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity instanceof Category)
        editedEntryCategories.add((Category)addedEntity);
      showEntryCategories(entry);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity instanceof Category)
        editedEntryCategories.remove((Category)removedEntity);
      showEntryCategories(entry);
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
        showEntryCategories(entry);
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


  protected void fireCategoryAddedEvent(Entry entry, Category category) {
    if(categoryAddedEventHandler != null)
      categoryAddedEventHandler.handle(new EntryCategoriesEditedEvent(this, entry, category));
  }

  protected void fireCategoryRemovedEvent(Entry entry, Category category) {
    if(categoryRemovedEventHandler != null)
      categoryRemovedEventHandler.handle(new EntryCategoriesEditedEvent(this, entry, category));
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
