package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 10/11/14.
 */
@Entity(name = TableConfig.CategoryTableName)
public class Category extends UserDataEntity {

  private static final long serialVersionUID = -4554383040629348423L;

  private final static Logger log = LoggerFactory.getLogger(Category.class);


  @Column(name = TableConfig.CategoryNameColumnName)
  protected String name = "";

  @Column(name = TableConfig.CategoryDescriptionColumnName)
  protected String description = "";

  @Column(name = TableConfig.CategoryIsExpandedColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
  protected boolean isExpanded = false;

  @Column(name = TableConfig.CategoryCategoryOrderColumnName)
  protected int categoryOrder;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.CategoryParentCategoryJoinColumnName)
  protected Category parentCategory;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentCategory"/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/)
  @OrderBy("categoryOrder ASC") // TODO: EclipseLink Error: The order by value [category_index], specified on the element [subCategories] from entity [class Category], is invalid. No property or field with that name exists on the target entity [class Category].
  protected Collection<Category> subCategories = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
  @OrderBy("entryIndex DESC")
  protected Collection<Entry> entries = new ArrayList<>();

//  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.CategoryDeepThoughtJoinColumnName)
//  @Transient
  protected DeepThought deepThought;


  public Category() {
    this(Localization.getLocalizedString("new.category.default.name"));
  }

  public Category(String name) {
    this.name = name;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    String previousName = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.CategoryNameColumnName, previousName, name);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    String previousDescription = this.description;
    this.description = description;
    callPropertyChangedListeners(TableConfig.CategoryDescriptionColumnName, previousDescription, description);
  }

  public boolean isExpanded() {
    return isExpanded;
  }

  public void setIsExpanded(boolean isExpanded) {
    boolean previousIsExpanded = this.isExpanded;
    this.isExpanded = isExpanded;
    callPropertyChangedListeners(TableConfig.CategoryIsExpandedColumnName, previousIsExpanded, isExpanded);
  }

  public int getCategoryOrder() {
    return categoryOrder;
  }

  public void setCategoryOrder(int categoryOrder) {
    int previousCategoryIndex = this.categoryOrder;
    this.categoryOrder = categoryOrder;
    callPropertyChangedListeners(TableConfig.CategoryCategoryOrderColumnName, previousCategoryIndex, categoryOrder);
  }

  public boolean isTopLevelCategory() {
    return deepThought != null && getParentCategory() == deepThought.getTopLevelCategory();
  }

  public Category getParentCategory() {
    return parentCategory;
  }

  public boolean hasSubCategories() {
    return getSubCategories().size() > 0;
  }

  public Collection<Category> getSubCategories() {
    return subCategories;
  }

  public boolean addSubCategory(Category subCategory) {
    subCategory.parentCategory = this;
    subCategory.categoryOrder = subCategories.size();
    if(subCategory.deepThought == null)
      Application.getDeepThought().addCategory(subCategory);

    boolean result = subCategories.add(subCategory);
    if(result) {
      callEntityAddedListeners(subCategories, subCategory);
    }

    return result;
  }

  public boolean removeSubCategory(Category subCategory) {
    int removeSubCategoryIndex = subCategory.getCategoryOrder();

    boolean result = subCategories.remove(subCategory);
    if(result) {
      subCategory.parentCategory = null;

      for(Category subCategoryEnum : subCategories) {
        if(subCategoryEnum.getCategoryOrder() >= removeSubCategoryIndex)
          subCategoryEnum.setCategoryOrder(subCategoryEnum.getCategoryOrder() - 1);
      }

//      if(deepThought != null /*&& this.equals(subCategory.getParentCategory())*/) // TODO: what is this.equals(subCategory.getParentCategory()) good for?
//        deepThought.removeCategory(subCategory);

      callEntityRemovedListeners(subCategories, subCategory);
    }

    return result;
  }

  public boolean containsSubCategory(Category subCategory) {
    return subCategories.contains(subCategory);
  }

  public boolean hasEntries() {
    return getEntries().size() > 0;
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntry(Entry entry) {
    boolean result = entries.add(entry);
    callEntityAddedListeners(entries, entry);
    return result;
  }

  protected boolean removeEntry(Entry entry) {
    boolean result = entries.remove(entry);
    callEntityRemovedListeners(entries, entry);
    return result;
  }


  public DeepThought getDeepThought() {
    return deepThought;
  }

  protected void setDeepThought(DeepThought deepThought) {
    Object previousValue = this.deepThought;
    this.deepThought = deepThought;
    callPropertyChangedListeners(TableConfig.CategoryDeepThoughtJoinColumnName, previousValue, deepThought);
  }


  protected transient Comparator<Entry> entriesByIndexComparator = new Comparator<Entry>() {
    @Override
    public int compare(Entry o1, Entry o2) {
      return ((Integer)o1.getEntryIndex()).compareTo(o2.getEntryIndex());
    }
  };


  @Override
  public String getTextRepresentation() {
    return "Category " + getName();
  }

  @Override
  public String toString() {
    String description = "Category " + name;

    if(parentCategory != null)
      description += ", child of " + parentCategory.getName();

    if(getSubCategories().size() > 0)
      description += ", " + getSubCategories().size() + " subcategories";

    return description;
  }


  public static Category createTopLevelCategory() {
    Category topLevelCategory = new Category(Localization.getLocalizedString("i.know.me.nothing.knowing"));

    return topLevelCategory;
  }
}
