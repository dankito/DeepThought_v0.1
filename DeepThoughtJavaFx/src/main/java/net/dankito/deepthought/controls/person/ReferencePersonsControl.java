package net.dankito.deepthought.controls.person;

import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

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
      setEntityPersons(reference.getPersons());
      reference.addEntityListener(referenceListener);
    }
    else
      setEntityPersons(new HashSet<Person>()); // TODO: or set to null to tell that editing is not enabled?
  }

  @Override
  public void cleanUp() {
    super.cleanUp();

    if(this.reference != null)
      reference.removeEntityListener(referenceListener);
  }



  protected EntityListener referenceListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == reference.getReferenceBasePersonAssociations()) {
        FXUtils.runOnUiThread(() -> setEntityPersons(reference.getPersons()));
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == reference.getReferenceBasePersonAssociations()) {
        FXUtils.runOnUiThread(() -> setEntityPersons(reference.getPersons()));
      }
    }
  };

}
