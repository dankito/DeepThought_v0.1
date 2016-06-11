package net.dankito.deepthought.controls.person;

import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

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
      setEntityPersons(subDivision.getPersons());
      subDivision.addEntityListener(referenceSubDivisionListener);
    }
    else
      setEntityPersons(new HashSet<Person>()); // TODO: or set to null to tell that editing is not enabled?
  }

  @Override
  public void cleanUp() {
    super.cleanUp();

    if(this.subDivision != null)
      subDivision.removeEntityListener(referenceSubDivisionListener);
  }



  protected EntityListener referenceSubDivisionListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == subDivision.getReferenceBasePersonAssociations()) {
        net.dankito.deepthought.controls.utils.FXUtils.runOnUiThread(() -> setEntityPersons(subDivision.getPersons()));
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == subDivision.getReferenceBasePersonAssociations()) {
        net.dankito.deepthought.controls.utils.FXUtils.runOnUiThread(() -> setEntityPersons(subDivision.getPersons()));
      }
    }
  };

}
