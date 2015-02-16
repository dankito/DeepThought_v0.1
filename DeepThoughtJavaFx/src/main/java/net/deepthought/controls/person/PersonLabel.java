package net.deepthought.controls.person;

import net.deepthought.controls.CollectionItemLabel;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 01/02/15.
 */
public class PersonLabel extends CollectionItemLabel {

  protected PersonRole role;

  protected Person person;


  public PersonLabel(PersonRole role, Person person) {
    this.role = role;
    this.person = person;

    person.addEntityListener(personListener);

    setUserData(person);
    itemDisplayNameUpdated();
  }

  @Override
  protected String getItemDisplayName() {
    if(person != null)
      return person.getNameRepresentation();
    return "";
  }


  EntityListener personListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      itemDisplayNameUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

}
