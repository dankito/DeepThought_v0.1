package net.deepthought.controls;

import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;

/**
 * Created by ganymed on 10/02/15.
 */
public class BaseEntityListCell<T extends BaseEntity> extends ListCell<T> {

  public BaseEntityListCell() {
    setText(null);
    setGraphic(null);

    itemProperty().addListener(new ChangeListener<T>() {
      @Override
      public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        itemChanged(newValue);
      }
    });
  }


  @Override
  protected void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);

    if(empty || item == null) {
      setText(null);
    }
    else {
      setText(item.getTextRepresentation());
    }
  }

  protected void itemChanged(T newValue) {
    if(getItem() != null) {
      getItem().removeEntityListener(baseEntityListener);
    }

    if(newValue != null) {
      newValue.addEntityListener(baseEntityListener);
    }

    updateItem(newValue, newValue == null);
  }

  protected EntityListener baseEntityListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(entity == getItem())
        itemChanged((T)entity);
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
