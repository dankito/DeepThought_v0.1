package net.deepthought.controls.person;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.listener.EntryPersonListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryPersonsControl extends PersonsControl {


  protected Entry entry = null;


  public EntryPersonsControl() {
    this(null);
  }

  public EntryPersonsControl(Entry entry) {
    super();

    setEntry(entry);
  }

  protected PersonListCell createPersonListCell() {
    return new EntryPersonListCell(this, entry);
  }

  public void setEntry(Entry entry) {
    if(this.entry != null)
      this.entry.removeEntryPersonListener(entryPersonListener);

    this.entry = entry;
    setDisable(entry == null);

    if(entry != null) {
      setEntityPersons(extractReferencePersons(entry));
      entry.addEntryPersonListener(entryPersonListener);
    }
    else
      setEntityPersons(new HashMap<PersonRole, Set<Person>>()); // TODO: or set to null to tell that editing is not enabled?

    for(PersonListCell cell : personListCells)
      ((EntryPersonListCell)cell).setEntry(entry);
  }

  protected Map<PersonRole, Set<Person>> extractReferencePersons(Entry entry) {
    Map<PersonRole, Set<Person>> persons = new HashMap<>();

    for(PersonRole role : entry.getPersonRoles()) {
      persons.put(role, entry.getPersonsForRole(role));
    }

    return persons;
  }

  @Override
  public void close() {
    entry.removeEntryPersonListener(entryPersonListener);
  }


  protected EntryPersonListener entryPersonListener = new EntryPersonListener() {
    @Override
    public void personAdded(Entry entry, PersonRole role, Person addedPerson) {
      if(entry.equals(EntryPersonsControl.this.entry) == false)
        log.warn("This should never be the case, EntryPersonListener's entry parameter {} doesn't equal member variable entry {}", entry, EntryPersonsControl.this.entry);
      setEntityPersons(extractReferencePersons(entry));
    }

    @Override
    public void personRemoved(Entry entry, PersonRole role, Person removedPerson) {
      if(entry.equals(EntryPersonsControl.this.entry) == false)
        log.warn("This should never be the case, EntryPersonListener's entry parameter {} doesn't equal member variable entry {}", entry, EntryPersonsControl.this.entry);
      setEntityPersons(extractReferencePersons(entry));

    }
  };

}
