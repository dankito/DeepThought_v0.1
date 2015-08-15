package net.deepthought.controls.person;

import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 27/12/14.
 */
public class SeriesTitlePersonListCell extends PersonListCell {

  protected SeriesTitle series = null;


  public SeriesTitlePersonListCell(PersonsControl personsControl, SeriesTitle series) {
    super(personsControl);

    this.series = series;
    if(series != null) {
      series.addEntityListener(seriesTitleListener);
    }
  }


  @Override
  public void cleanUpControl() {
    super.cleanUpControl();

    if(this.series != null) {
      this.series.removeEntityListener(seriesTitleListener);
    }
  }

  public void setSeries(SeriesTitle series) {
    if(this.series != null) {
      this.series.removeEntityListener(seriesTitleListener);
    }

    this.series = series;

    if(series != null) {
      series.addEntityListener(seriesTitleListener);
    }
  }


  protected EntityListener seriesTitleListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity.equals(getItem()))
        itemChanged(getItem());
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity.equals(getItem()))
        itemChanged(getItem());
    }
  };

}
