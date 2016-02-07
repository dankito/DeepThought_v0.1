package net.deepthought.data.search;

import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.search.specific.EntriesSearch;
import net.deepthought.data.search.specific.FilesSearch;
import net.deepthought.data.search.specific.ReferenceBasesSearch;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.deepthought.data.search.specific.TagsSearch;

import java.util.Collection;

/**
 * Created by ganymed on 17/03/15.
 */
public interface ISearchEngine {

  public void getEntriesWithoutTags(SearchCompletedListener<Collection<Entry>> listener);

  public void searchTags(TagsSearch search);

  public void searchCategories(Search<Category> search);

  public void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);
  public void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, String searchTerm, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);

  public void searchEntries(EntriesSearch search);

  public void searchReferenceBases(ReferenceBasesSearch search);

  public void searchPersons(Search<Person> search);

  public void searchFiles(FilesSearch search);


  public void close();

}
