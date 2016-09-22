package net.dankito.deepthought.controls.entries;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.StringUtils;

import java.util.Collection;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

/**
 * Created by ganymed on 28/11/14.
 */
public abstract class EntryTableCell extends TableCell<Entry, String> {


  protected Entry entry;
  protected String textRepresentation = "";


  public EntryTableCell() {
    Application.getEntityChangesService().addAllEntitiesListener(allEntitiesListener);

    tableRowProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue != null)
        newValue.itemProperty().addListener((observable1, oldValue1, newValue1) -> entryChanged((Entry) newValue1));
    });
  }

  protected void entryChanged(Entry entry) {
    this.entry = entry;

    entryUpdated(entry);
  }

  protected void entryUpdated(final Entry entry) {
    FXUtils.runOnUiThread(() -> entryUpdatedOnUiThread(entry));
  }

  protected void entryUpdatedOnUiThread(Entry entry) {
    this.textRepresentation = getTextRepresentationForCell(entry);

    setItem(textRepresentation);
    updateItem(textRepresentation, StringUtils.isNullOrEmpty(textRepresentation));
  }

  protected abstract String getTextRepresentationForCell(Entry entry);

  protected String getItemTextRepresentation() {
    return textRepresentation;
  }


  @Override
  public void updateItem(String item, boolean empty) {
    Object entryCheck = ((TableRow<Entry>)getTableRow()).getItem();
    if(entryCheck != entry && entryCheck instanceof Entry)
      entryChanged((Entry)entryCheck);

    super.updateItem(item, empty);

    if (empty) {
      setText(null);
      setGraphic(null);
    }
    else {
      setGraphic(null);
      setText(getItemTextRepresentation());
    }
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {

    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(entry != null) {
        EntryTableCell.this.entityUpdated(entity, propertyName);
      }
    }

    @Override
    public void entityDeleted(BaseEntity entity) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(entry == collectionHolder) {
        entryCollectionChanged(collection, addedEntity);
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(entry == collectionHolder) {
        entryCollectionChanged(collection, removedEntity);
      }
    }
  };

  protected void entityUpdated(BaseEntity entity, String propertyName) {
    if(entity == entry) {
      entryUpdated((Entry)entity);
    }
  }

  protected void entryCollectionChanged(Collection<? extends BaseEntity> collection, BaseEntity addedOrRemovedEntity) {

  }

}

