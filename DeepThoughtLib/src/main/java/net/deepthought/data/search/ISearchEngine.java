package net.deepthought.data.search;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ganymed on 17/03/15.
 */
public interface ISearchEngine {

  public void indexEntity(UserDataEntity entity);
//
//  public void entryUpdated(Entry entry);

  public void updateIndexForEntity(UserDataEntity entity, String propertyName);


  public void filterTags(Search<Tag> search);

  public void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, Collection<Entry> entriesHavingFilteredTags, Set<Tag> tagsOnEntriesContainingFilteredTags);

  public void filterEntries(FilterEntriesSearch search);

  public void filterReferenceBases(Search<ReferenceBase> search);

  public void filterPersons(Search<Person> search);


  public void close();

}
