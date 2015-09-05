package net.deepthought.controls.person;

import net.deepthought.controls.tag.IEditedEntitiesHolder;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 27/12/14.
 */
public class ReferencePersonListCell extends PersonListCell {

  protected Reference reference = null;


  public ReferencePersonListCell(IEditedEntitiesHolder<Person> editedPersonsHolder, Reference reference) {
    super(editedPersonsHolder);

    setReference(reference);
  }


  @Override
  public void cleanUpControl() {
    super.cleanUpControl();

    if(this.reference != null) {
      this.reference.removeEntityListener(referenceListener);
      this.reference = null;
    }
  }

  public void setReference(Reference reference) {
    if(this.reference != null) {
      this.reference.removeEntityListener(referenceListener);
    }

    this.reference = reference;

    if(reference != null) {
      reference.addEntityListener(referenceListener);
    }
  }


  protected EntityListener referenceListener = new EntityListener() {
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

}
