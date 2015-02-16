package net.deepthought.controls.importdata;

import net.deepthought.data.persistence.db.BaseEntity;

import java.lang.reflect.Field;
import java.util.Collection;

import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 10/01/15.
 */
public class ImportDataCollectionChildTreeItem extends TreeItem {

  protected Collection<? extends BaseEntity> collection;

  protected Field collectionChildField;


  public ImportDataCollectionChildTreeItem(Collection<? extends BaseEntity> collection, Field collectionChildField) {
    super(collectionChildField);

    this.collection = collection;
    this.collectionChildField = collectionChildField;

    addCollectionsChildren(collection);
  }

  protected void addCollectionsChildren(Collection<? extends BaseEntity> collection) {
    for(BaseEntity child : collection) {
      getChildren().add(new ImportDataTreeItem(child));
    }
  }

}
