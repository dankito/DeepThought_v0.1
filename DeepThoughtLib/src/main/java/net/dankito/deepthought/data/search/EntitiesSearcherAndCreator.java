package net.dankito.deepthought.data.search;

import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.search.specific.CategoriesSearch;
import net.dankito.deepthought.data.search.specific.ReferenceBasesSearch;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.search.specific.ReferenceBaseType;
import net.dankito.deepthought.util.ObjectHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 16/05/16.
 */
public class EntitiesSearcherAndCreator implements IEntitiesSearcherAndCreator {


  protected transient Map<String, Tag> cachedTags = new HashMap<>();

  @Override
  public Tag findOrCreateTagForName(String name) {
    synchronized(cachedTags) {
      Tag existingTag = findTagForName(name);
      if(existingTag != null)
        return existingTag;

      Tag newTag = new Tag(name);
      cachedTags.put(name, newTag);

      return newTag;
    }
  }

  protected Tag findTagForName(String name) {
    if(cachedTags.containsKey(name))
      return cachedTags.get(name);

    final ObjectHolder<TagsSearchResults> searchResults = new ObjectHolder<>();
    final CountDownLatch waitForSearchResultsLatch = new CountDownLatch(1);

    Application.getSearchEngine().searchTags(new TagsSearch(name, new SearchCompletedListener<TagsSearchResults>() {
      @Override
      public void completed(TagsSearchResults results) {
        searchResults.set(results);
        waitForSearchResultsLatch.countDown();
      }
    }));

    try { waitForSearchResultsLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

    TagsSearchResults results = searchResults.get();
    if(results != null && results.hasLastResultExactMatch()) {
      return results.getExactMatchesOfLastResult();
    }

    return null;
  }


  @Override
  public Category findOrCreateTopLevelCategoryForName(String name) {
    Category existingCategory = findTopLevelCategoryForName(name);
    if(existingCategory != null)
      return existingCategory;

    Category newCategory = new Category(name);

    return newCategory;
  }

  protected Category findTopLevelCategoryForName(String name) {
    final ObjectHolder<Collection<Category>> searchResults = new ObjectHolder<>();
    final CountDownLatch waitForSearchResultsLatch = new CountDownLatch(1);

    Application.getSearchEngine().searchCategories(new CategoriesSearch(name, true, new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> results) {
        searchResults.set(results);
        waitForSearchResultsLatch.countDown();
      }
    }));

    try { waitForSearchResultsLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

    Collection<Category> results = searchResults.get();
    if(results != null && results.size() == 1) { // TODO: what to do if size() is greater one?
      return new ArrayList<Category>(results).get(0);
    }

    return null;
  }

  @Override
  public Category findOrCreateSubCategoryForName(Category parentCategory, String subCategoryName) {
    Category existingCategory = findSubCategoryForName(parentCategory, subCategoryName);
    if(existingCategory != null)
      return existingCategory;

    Category newCategory = new Category(subCategoryName);
    parentCategory.addSubCategory(newCategory);

    return newCategory;
  }

  protected Category findSubCategoryForName(Category parentCategory, String subCategoryName) {
    final ObjectHolder<Collection<Category>> searchResults = new ObjectHolder<>();
    final CountDownLatch waitForSearchResultsLatch = new CountDownLatch(1);

    Application.getSearchEngine().searchCategories(new CategoriesSearch(subCategoryName, parentCategory, new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> results) {
        searchResults.set(results);
        waitForSearchResultsLatch.countDown();
      }
    }));

    try { waitForSearchResultsLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

    Collection<Category> results = searchResults.get();
    if(results != null && results.size() == 1) { // TODO: what to do if size() is greater one?
      return new ArrayList<Category>(results).get(0);
    }

    return null;
  }


  @Override
  public Person findOrCreatePerson(String lastName, String firstName) {
    Person existingPerson = findPerson(lastName, firstName);
    if(existingPerson != null)
      return existingPerson;

    Person newPerson = new Person(firstName, lastName);

    return newPerson;
  }

  protected Person findPerson(String lastName, String firstName) {
    final ObjectHolder<Collection<Person>> searchResults = new ObjectHolder<>();
    final CountDownLatch waitForSearchResultsLatch = new CountDownLatch(1);

    Application.getSearchEngine().searchPersons(new Search<Person>(lastName + ", " + firstName, new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> results) {
        searchResults.set(results);
        waitForSearchResultsLatch.countDown();
      }
    }));

    try { waitForSearchResultsLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

    Collection<Person> results = searchResults.get();
    if(results != null && results.size() == 1) { // TODO: what to do if size() is greater one?
      return new ArrayList<Person>(results).get(0);
    }

    return null;
  }


  protected transient Map<String, SeriesTitle> cachedSeriesTitles = new HashMap<>();

  @Override
  public SeriesTitle findOrCreateSeriesTitleForTitle(String title) {
    synchronized(cachedSeriesTitles) {
      SeriesTitle existingSeriesTitle = findSeriesTitleForTitle(title);
      if(existingSeriesTitle != null)
        return existingSeriesTitle;

      SeriesTitle newSeriesTitle = new SeriesTitle(title);
      cachedSeriesTitles.put(title, newSeriesTitle);

      return newSeriesTitle;
    }
  }

  protected SeriesTitle findSeriesTitleForTitle(String title) {
    if(cachedSeriesTitles.containsKey(title))
      return cachedSeriesTitles.get(title);

    final ObjectHolder<Collection<ReferenceBase>> searchResults = new ObjectHolder<>();
    final CountDownLatch waitForSearchResultsLatch = new CountDownLatch(1);

    Application.getSearchEngine().searchReferenceBases(new ReferenceBasesSearch(title, ReferenceBaseType.SeriesTitle, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> results) {
        searchResults.set(results);
        waitForSearchResultsLatch.countDown();
      }
    }));

    try { waitForSearchResultsLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

    Collection<ReferenceBase> results = searchResults.get();
    if(results != null && results.size() == 1) { // TODO: what to do if size() is greater one?
      SeriesTitle seriesTitle = (SeriesTitle)new ArrayList<ReferenceBase>(results).get(0);
      cachedSeriesTitles.put(title, seriesTitle);
      return seriesTitle;
    }

    return null;
  }


  protected transient Map<String, Reference> cachedReferences = new HashMap<>();

  @Override
  public Reference findOrCreateReferenceForTitle(String title) {
    synchronized(cachedReferences) {
      Reference existingReference = findReferenceForTitle(title);
      if(existingReference != null)
        return existingReference;

      Reference newReference = new Reference(title);
      cachedReferences.put(title, newReference);

      return newReference;
    }
  }

  protected Reference findReferenceForTitle(String title) {
    if(cachedReferences.containsKey(title))
      return cachedReferences.get(title);

    final ObjectHolder<Collection<ReferenceBase>> searchResults = new ObjectHolder<>();
    final CountDownLatch waitForSearchResultsLatch = new CountDownLatch(1);

    Application.getSearchEngine().searchReferenceBases(new ReferenceBasesSearch(title, ReferenceBaseType.Reference, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> results) {
        searchResults.set(results);
        waitForSearchResultsLatch.countDown();
      }
    }));

    try { waitForSearchResultsLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

    Collection<ReferenceBase> results = searchResults.get();
    if(results != null && results.size() == 1) { // TODO: what to do if size() is greater one?
      Reference reference = (Reference)new ArrayList<ReferenceBase>(results).get(0);
      cachedReferences.put(title, reference);
      return reference;
    }

    return null;
  }


  protected transient Map<String, Map<String, Reference>> cachedReferencesForDate = new HashMap<>();

  @Override
  public Reference findOrCreateReferenceForDate(SeriesTitle series, String articleDate) {
    synchronized(cachedReferencesForDate) {
      Reference existingReference = findReferenceForDate(series, articleDate);
      if(existingReference != null) {
        return existingReference;
      }

      Reference newReference = new Reference();
      newReference.setIssueOrPublishingDate(articleDate);

      String seriesTitleTitle = series == null ? null : series.getTitle();
      addReferenceForDateToCache(articleDate, seriesTitleTitle, newReference);

      return newReference;
    }
  }

  protected Reference findReferenceForDate(SeriesTitle series, String articleDate) {
    String seriesTitleTitle = series == null ? null : series.getTitle();
    if(cachedReferencesForDate.containsKey(seriesTitleTitle) && cachedReferencesForDate.get(seriesTitleTitle).containsKey(articleDate)) {
      return cachedReferencesForDate.get(seriesTitleTitle).get(articleDate);
    }


    final ObjectHolder<Collection<Reference>> searchResults = new ObjectHolder<>();
    final CountDownLatch waitForSearchResultsLatch = new CountDownLatch(1);

    Application.getSearchEngine().searchForReferenceOfDate(seriesTitleTitle, new Search<Reference>(articleDate, new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> results) {
        searchResults.set(results);
        waitForSearchResultsLatch.countDown();
      }
    }));

    try { waitForSearchResultsLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

    Collection<Reference> results = searchResults.get();
    if(results != null && results.size() == 1) { // TODO: what to do if size() is greater one?
      Reference reference = (Reference)new ArrayList<Reference>(results).get(0);

      addReferenceForDateToCache(articleDate, seriesTitleTitle, reference);

      return reference;
    }

    return null;
  }

  protected void addReferenceForDateToCache(String articleDate, String seriesTitleTitle, Reference reference) {
    if(cachedReferencesForDate.containsKey(seriesTitleTitle) == false) {
      cachedReferencesForDate.put(seriesTitleTitle, new HashMap<String, Reference>());
    }
    cachedReferencesForDate.get(seriesTitleTitle).put(articleDate, reference);
  }

}
