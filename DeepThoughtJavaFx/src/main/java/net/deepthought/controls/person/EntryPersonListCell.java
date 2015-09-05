package net.deepthought.controls.person;

import net.deepthought.controls.tag.IEditedEntitiesHolder;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.listener.EntryPersonListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 27/12/14.
 */
public class EntryPersonListCell extends PersonListCell {

  protected Entry entry = null;


  public EntryPersonListCell(IEditedEntitiesHolder<Person> editedPersonsHolder, Entry entry) {
    super(editedPersonsHolder);

    setEntry(entry);
  }

  @Override
  public void cleanUpControl() {
    super.cleanUpControl();

    removeListeners();
  }

  public void setEntry(Entry entry) {
    removeListeners();

    this.entry = entry;

    if(entry != null) {
      entry.addEntityListener(entryListener);
      entry.addEntryPersonListener(entryPersonListener);
    }
  }

  protected void removeListeners() {
    if(this.entry != null) {
      this.entry.removeEntityListener(entryListener);
      this.entry.removeEntryPersonListener(entryPersonListener);
    }
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity.equals(getItem()))
        personChanged(getItem());
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity.equals(getItem()))
        personChanged(getItem());
    }
  };

  protected EntryPersonListener entryPersonListener = new EntryPersonListener() {
    @Override
    public void personAdded(Entry entry, Person addedPerson) {
      if(addedPerson.equals(getItem()))
        updateItem(addedPerson, addedPerson == null);
    }

    @Override
    public void personRemoved(Entry entry, Person removedPerson) {
      if(removedPerson.equals(getItem()))
        updateItem(removedPerson, removedPerson == null);
    }
  };

}
