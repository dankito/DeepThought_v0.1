package net.deepthought.controls.person;

import net.deepthought.controls.tag.IEditedEntitiesHolder;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 27/12/14.
 */
public class ReferenceSubDivisionPersonListCell extends PersonListCell {

  protected ReferenceSubDivision subDivision = null;


  public ReferenceSubDivisionPersonListCell(IEditedEntitiesHolder<Person> editedPersonsHolder, ReferenceSubDivision subDivision) {
    super(editedPersonsHolder);

    setSubDivision(subDivision);
  }


  @Override
  public void cleanUpControl() {
    super.cleanUpControl();

    if(this.subDivision != null) {
      this.subDivision.removeEntityListener(referenceSubDivisionListener);
      this.subDivision = null;
    }
  }

  public void setSubDivision(ReferenceSubDivision subDivision) {
    if(this.subDivision != null) {
      this.subDivision.removeEntityListener(referenceSubDivisionListener);
    }

    this.subDivision = subDivision;

    if(subDivision != null) {
      subDivision.addEntityListener(referenceSubDivisionListener);
    }
  }


  protected EntityListener referenceSubDivisionListener = new EntityListener() {
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
