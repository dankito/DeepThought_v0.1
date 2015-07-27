package net.deepthought.data.search;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.Tag;

import java.util.Collection;

/**
 * Created by ganymed on 17/03/15.
 */
public interface ISearchEngine {

  public void getEntriesWithoutTags(SearchCompletedListener<Collection<Entry>> listener);

  public void filterTags(FilterTagsSearch search);

  public void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);

  public void filterEntries(FilterEntriesSearch search);

  public void filterReferenceBases(Search<ReferenceBase> search);

  public void filterPersons(Search<Person> search);


  public void close();

}
