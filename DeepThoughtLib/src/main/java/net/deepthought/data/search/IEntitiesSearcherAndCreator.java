package net.deepthought.data.search;

import net.deepthought.data.model.Category;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;

/**
 * Created by ganymed on 16/05/16.
 */
public interface IEntitiesSearcherAndCreator {

  Tag findOrCreateTagForName(String name);

  Category findOrCreateTopLevelCategoryForName(String name);

  Category findOrCreateSubCategoryForName(Category parentCategory, String subCategoryName);

  Person findOrCreatePerson(String lastName, String firstName);

  SeriesTitle findOrCreateSeriesTitleForTitle(String title);

  Reference findOrCreateReferenceForTitle(String title);

  Reference findOrCreateReferenceForDate(SeriesTitle series, String articleDate);

}
