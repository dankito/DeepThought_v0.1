package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.ReferenceSubDivisionCategoryTableName)
public class ReferenceSubDivisionCategory extends ExtensibleEnumeration {

  private static final long serialVersionUID = -6047771597427468906L;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
  protected Set<ReferenceSubDivision> subDivisions = new HashSet<>();


  public ReferenceSubDivisionCategory() {

  }

  public ReferenceSubDivisionCategory(String name) {
    super(name);
  }

  public ReferenceSubDivisionCategory(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }


  public Set<ReferenceSubDivision> getSubDivisions() {
    return subDivisions;
  }

  public boolean addReferenceSubDivision(ReferenceSubDivision subDivision) {
    if(subDivisions.add(subDivision)) {
      callEntityAddedListeners(subDivisions, subDivision);
      return true;
    }

    return false;
  }

  public boolean removeReferenceSubDivision(ReferenceSubDivision subDivision) {
    if(subDivisions.remove(subDivision)) {
      callEntityRemovedListeners(subDivisions, subDivision);
      return true;
    }

    return false;
  }


  @Override
  public String toString() {
    return "ReferenceSubDivisionCategory " + getTextRepresentation();
  }


  protected static ReferenceSubDivisionCategory newsPaperArticleCategory = null;

  protected static ReferenceSubDivisionCategory magazineArticleCategory = null;

  protected static ReferenceSubDivisionCategory articleCategory = null;

  public static ReferenceSubDivisionCategory getNewsPaperArticleCategory() {
    if(newsPaperArticleCategory == null)
      newsPaperArticleCategory = findByNameResourceKey("reference.sub.division.category.newspaper.article");
    return newsPaperArticleCategory;
  }

  public static ReferenceSubDivisionCategory getMagazineArticleCategory() {
    if(magazineArticleCategory == null)
      magazineArticleCategory = findByNameResourceKey("reference.sub.division.category.magazine.article");
    return magazineArticleCategory;
  }

  public static ReferenceSubDivisionCategory getArticleCategory() {
    if(magazineArticleCategory == null)
      magazineArticleCategory = findByNameResourceKey("reference.sub.division.category.article");
    return magazineArticleCategory;
  }


  public final static ReferenceSubDivisionCategory CategoryWithThatNameNotFound = new ReferenceSubDivisionCategory("Category with that name not found");

  public static ReferenceSubDivisionCategory findByName(String name) {
    for(ReferenceSubDivisionCategory category : Application.getDeepThought().getReferenceSubDivisionCategories()) {
      if(category.getName().equals(name))
        return category;
    }

    return CategoryWithThatNameNotFound;
  }

  public static ReferenceSubDivisionCategory findByNameResourceKey(String nameResourceKey) {
    if(Application.getDeepThought() != null) {
      for (ReferenceSubDivisionCategory category : Application.getDeepThought().getReferenceSubDivisionCategories()) {
        if (nameResourceKey.equals(category.nameResourceKey))
          return category;
      }
    }

    return CategoryWithThatNameNotFound;
  }

}
