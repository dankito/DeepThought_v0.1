package net.deepthought.data.search;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.search.specific.FilterReferenceBasesSearch;
import net.deepthought.data.search.specific.FilterTagsSearch;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;

import java.util.Collection;

/**
 * Created by ganymed on 17/03/15.
 */
public interface ISearchEngine {

  public void getEntriesWithoutTags(SearchCompletedListener<Collection<Entry>> listener);

  public void filterTags(FilterTagsSearch search);

  public void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);

  public void filterEntries(net.deepthought.data.search.specific.FilterEntriesSearch search);

  public void filterReferenceBases(FilterReferenceBasesSearch search);

  public void filterPersons(Search<Person> search);


  public void close();

}
