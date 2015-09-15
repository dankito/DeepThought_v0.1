package net.deepthought.controls.person;

import net.deepthought.data.model.Person;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by ganymed on 01/02/15.
 */
public class SeriesTitlePersonsControl extends PersonsControl {


  protected SeriesTitle series = null;


  public SeriesTitlePersonsControl() {
    this(null);
  }

  public SeriesTitlePersonsControl(SeriesTitle series) {
    super();

    setSeries(series);
  }

  public void setSeries(SeriesTitle series) {
    if(this.series != null)
      this.series.removeEntityListener(seriesTitleListener);

    this.series = series;
    setDisable(series == null);

    if(series != null) {
      setEntityPersons(series.getPersons());
      series.addEntityListener(seriesTitleListener);
    }
    else
      setEntityPersons(new HashSet<Person>()); // TODO: or set to null to tell that editing is not enabled?
  }

  @Override
  public void cleanUpControl() {
    super.cleanUpControl();

    if(this.series != null)
      series.removeEntityListener(seriesTitleListener);
  }



  protected EntityListener seriesTitleListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == series.getReferenceBasePersonAssociations())
        setEntityPersons(series.getPersons());
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == series.getReferenceBasePersonAssociations())
        setEntityPersons(series.getPersons());
    }
  };

}
