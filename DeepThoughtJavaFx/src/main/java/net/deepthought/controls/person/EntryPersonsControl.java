package net.deepthought.controls.person;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.listener.EntryPersonListener;

import java.util.HashSet;

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
      setEntityPersons(entry.getPersons());
      entry.addEntryPersonListener(entryPersonListener);
    }
    else
      setEntityPersons(new HashSet<Person>()); // TODO: or set to null to tell that editing is not enabled?

    for(PersonListCell cell : personListCells)
      ((EntryPersonListCell)cell).setEntry(entry);
  }

  @Override
  public void cleanUpControl() {
    super.cleanUpControl();

    if(this.entry != null)
      this.entry.removeEntryPersonListener(entryPersonListener);
  }

  protected EntryPersonListener entryPersonListener = new EntryPersonListener() {
    @Override
    public void personAdded(Entry entry, Person addedPerson) {
      if(entry.equals(EntryPersonsControl.this.entry) == false)
        log.warn("This should never be the case, EntryPersonListener's entry parameter {} doesn't equal member variable entry {}", entry, EntryPersonsControl.this.entry);
      setEntityPersons(entry.getPersons());
    }

    @Override
    public void personRemoved(Entry entry, Person removedPerson) {
      if(entry.equals(EntryPersonsControl.this.entry) == false)
        log.warn("This should never be the case, EntryPersonListener's entry parameter {} doesn't equal member variable entry {}", entry, EntryPersonsControl.this.entry);
      setEntityPersons(entry.getPersons());

    }
  };

}
