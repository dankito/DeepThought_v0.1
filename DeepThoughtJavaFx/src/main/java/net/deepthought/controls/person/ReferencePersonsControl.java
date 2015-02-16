package net.deepthought.controls.person;

import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ganymed on 01/02/15.
 */
public class ReferencePersonsControl extends PersonsControl {


  protected Reference reference = null;


  public ReferencePersonsControl() {
    this(null);
  }

  public ReferencePersonsControl(Reference reference) {
    super();

    setReference(reference);
  }

  public void setReference(Reference reference) {
    if(this.reference != null)
      this.reference.removeEntityListener(referenceListener);

    this.reference = reference;
    setDisable(reference == null);

    if(reference != null) {
      setEntityPersons(extractReferencePersons(reference));
      reference.addEntityListener(referenceListener);
    }
    else
      setEntityPersons(new HashMap<PersonRole, Set<Person>>()); // TODO: or set to null to tell that editing is not enabled?

    for(PersonListCell cell : personListCells)
      ((ReferencePersonListCell)cell).setReference(reference);
  }

  protected Map<PersonRole, Set<Person>> extractReferencePersons(Reference reference) {
    Map<PersonRole, Set<Person>> persons = new HashMap<>();

    for(PersonRole role : reference.getPersonRoles()) {
      persons.put(role, reference.getPersonsForRole(role));
    }

    return persons;
  }

  @Override
  protected PersonListCell createPersonListCell() {
    return new ReferencePersonListCell(this, reference);
  }

  @Override
  public void close() {
    reference.removeEntityListener(referenceListener);
  }



  protected EntityListener referenceListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == reference.getReferenceBasePersonAssociations())
        setEntityPersons(extractReferencePersons(reference));
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == reference.getReferenceBasePersonAssociations())
        setEntityPersons(extractReferencePersons(reference));
    }
  };

}
