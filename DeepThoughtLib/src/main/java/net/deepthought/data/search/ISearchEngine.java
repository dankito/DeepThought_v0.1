package net.deepthought.data.search;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.Tag;
import net.deepthought.data.search.specific.CategoriesSearch;
import net.deepthought.data.search.specific.EntriesSearch;
import net.deepthought.data.search.specific.FilesSearch;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.deepthought.data.search.specific.ReferenceBasesSearch;
import net.deepthought.data.search.specific.TagsSearch;

import java.util.Collection;

/**
 * Created by ganymed on 17/03/15.
 */
public interface ISearchEngine {

  void getEntriesWithoutTags(SearchCompletedListener<Collection<Entry>> listener);

  void searchTags(TagsSearch search);

  void searchCategories(CategoriesSearch search);

  void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);
  void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, String searchTerm, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);

  void searchEntries(EntriesSearch search);

  void searchReferenceBases(ReferenceBasesSearch search);
  void searchForReferenceOfDate(String optionalSeriesTitleTitle, Search<Reference> search);

  void searchPersons(Search<Person> search);

  void searchFiles(FilesSearch search);


  void close();

}
