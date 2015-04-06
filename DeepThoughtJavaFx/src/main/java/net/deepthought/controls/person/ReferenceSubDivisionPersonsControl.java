package net.deepthought.controls.person;

import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceSubDivision;
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
public class ReferenceSubDivisionPersonsControl extends PersonsControl {


  protected ReferenceSubDivision subDivision = null;


  public ReferenceSubDivisionPersonsControl() {
    this(null);
  }

  public ReferenceSubDivisionPersonsControl(ReferenceSubDivision subDivision) {
    super();

    setSubDivision(subDivision);
  }

  public void setSubDivision(ReferenceSubDivision subDivision) {
    if(this.subDivision != null)
      this.subDivision.removeEntityListener(referenceSubDivisionListener);

    this.subDivision = subDivision;
    setDisable(subDivision == null);

    if(subDivision != null) {
      setEntityPersons(extractReferenceSubDivisionPersons(subDivision));
      subDivision.addEntityListener(referenceSubDivisionListener);
    }
    else
      setEntityPersons(new HashMap<PersonRole, Set<Person>>()); // TODO: or set to null to tell that editing is not enabled?

    for(PersonListCell cell : personListCells)
      ((ReferenceSubDivisionPersonListCell)cell).setSubDivision(subDivision);
  }

  protected Map<PersonRole, Set<Person>> extractReferenceSubDivisionPersons(ReferenceSubDivision reference) {
    Map<PersonRole, Set<Person>> persons = new HashMap<>();

    for(PersonRole role : reference.getPersonRoles()) {
      persons.put(role, reference.getPersonsForRole(role));
    }

    return persons;
  }

  @Override
  protected PersonListCell createPersonListCell() {
    return new ReferenceSubDivisionPersonListCell(this, subDivision);
  }

  @Override
  public void close() {
    subDivision.removeEntityListener(referenceSubDivisionListener);
  }



  protected EntityListener referenceSubDivisionListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == subDivision.getReferenceBasePersonAssociations())
        setEntityPersons(extractReferenceSubDivisionPersons(subDivision));
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == subDivision.getReferenceBasePersonAssociations())
        setEntityPersons(extractReferenceSubDivisionPersons(subDivision));
    }
  };

}
