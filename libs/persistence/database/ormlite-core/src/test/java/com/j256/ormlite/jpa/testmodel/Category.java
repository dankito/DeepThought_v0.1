package com.j256.ormlite.jpa.testmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PostPersist;

/**
 * Created by ganymed on 10/11/14.
 */
@Entity(name = TableConfig.CategoryTableName)
public class Category extends UserDataEntity {

  private static final long serialVersionUID = -4554383040629348423L;

  private final static Logger log = LoggerFactory.getLogger(Category.class);


  @Column(name = TableConfig.CategoryNameColumnName)
  protected String name = "";

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

//  @ManyToMany(fetch = FetchType.LAZY/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/ )
//  @JoinTable(
//      name = TableConfig.CategoryEntryJoinTableName,
//      joinColumns = { @JoinColumn(name = TableConfig.CategoryEntryJoinTableCategoryIdColumnName, referencedColumnName = "id") },
//      inverseJoinColumns = { @JoinColumn(name = TableConfig.CategoryEntryJoinTableEntryIdColumnName, referencedColumnName = "id") }
//  )
//  @OrderBy("entryIndex DESC")
//  protected Collection<Entry> entries = new ArrayList<>();
//
////  @JsonIgnore
//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.CategoryDeepThoughtJoinColumnName)
////  @Transient
//  protected DeepThought deepThought;


  protected Category() {

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
  }

  public boolean isExpanded() {
    return isExpanded;
  }

  public void setIsExpanded(boolean isExpanded) {
    boolean previousIsExpanded = this.isExpanded;
    this.isExpanded = isExpanded;
  }

  public int getCategoryOrder() {
    return categoryOrder;
  }

  public void setCategoryOrder(int categoryOrder) {
    int previousCategoryIndex = this.categoryOrder;
    this.categoryOrder = categoryOrder;
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

    boolean result = subCategories.add(subCategory);

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
    }

    return result;
  }

  public boolean containsSubCategory(Category subCategory) {
    return subCategories.contains(subCategory);
  }

//  public boolean hasEntries() {
//    return getEntries().size() > 0;
//  }
//
//  public Collection<Entry> getEntries() {
//    return entries;
//  }
//
//  public boolean addEntry(Entry entry) {
//    if(entries.contains(entry) == false) {
//      if (entries.add(entry)) {
//        entry.addCategory(this);
//        Collections.sort((List) entries, entriesByIndexComparator);
//
//        if (entry.deepThought == null)
//          deepThought.addEntry(entry);
//
//        callEntityAddedListeners(entries, entry);
//        return true;
//      }
//    }
//
//    return false;
//  }
//
//  public boolean removeEntry(Entry entry) {
//    boolean result = entries.remove(entry);
//    if(result) {
//      entry.removeCategory(this);
//      callEntityRemovedListeners(entries, entry);
//    }
//
//    return result;
//  }
//
//  public boolean containsEntry(Entry entry) {
//    return entries.contains(entry);
//  }
//
//  public DeepThought getDeepThought() {
//    return deepThought;
//  }


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


<<<<<<< HEAD:src/test/java/com/j256/ormlite/jpa/testmodel/Category.java
//  public static Category createTopLevelCategory() {
//    Category topLevelCategory = new Category(Localization.getLocalizedStringForResourceKey("i.know.me.nothing.knowing"));
//
//    return topLevelCategory;
//  }
=======
  @PostPersist
  protected void postPersist() {
    if(Application.getSearchEngine() != null)
      Application.getSearchEngine().indexEntity(this);
  }

  @Override
  protected void callPropertyChangedListeners(String propertyName, Object previousValue, Object newValue) {
    super.callPropertyChangedListeners(propertyName, previousValue, newValue);

    if(Application.getSearchEngine() != null)
      Application.getSearchEngine().updateIndexForEntity(this, propertyName);
  }


  public static Category createTopLevelCategory() {
    Category topLevelCategory = new Category(Localization.getLocalizedStringForResourceKey("i.know.me.nothing.knowing"));

    return topLevelCategory;
  }
>>>>>>> 3bed7c67baed58edad591a0868ec5f516d3ad1e6:DeepThoughtLib/src/main/java/net/deepthought/data/model/Category.java
}
