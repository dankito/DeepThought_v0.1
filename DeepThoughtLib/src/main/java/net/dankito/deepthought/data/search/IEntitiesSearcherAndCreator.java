package net.dankito.deepthought.data.search;

import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.Reference;

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
