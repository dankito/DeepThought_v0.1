package net.deepthought.controls.person;

import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.listener.EntryPersonListener;

import java.util.HashSet;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryPersonsControl extends PersonsControl {


  protected Entry entry = null;


  public EntryPersonsControl(Entry entry) {
    super();

    setEntry(entry);
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
  }

  @Override
  public void cleanUp() {
    super.cleanUp();

    if(this.entry != null)
      this.entry.removeEntryPersonListener(entryPersonListener);
  }

  protected EntryPersonListener entryPersonListener = new EntryPersonListener() {
    @Override
    public void personAdded(Entry entry, Person addedPerson) {
      FXUtils.runOnUiThread(() -> setEntityPersons(entry.getPersons()));
    }

    @Override
    public void personRemoved(Entry entry, Person removedPerson) {
      FXUtils.runOnUiThread(() -> setEntityPersons(entry.getPersons()));

    }
  };

}
