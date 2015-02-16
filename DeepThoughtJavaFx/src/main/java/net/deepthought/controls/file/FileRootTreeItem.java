package net.deepthought.controls.file;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 27/12/14.
 */
public class FileRootTreeItem extends TreeItem<FileLink> {

  private final static Logger log = LoggerFactory.getLogger(FileRootTreeItem.class);


  protected Entry entry;

  protected Map<FileLink, FileTreeItem> mapFileToItem = new HashMap<>();


  public FileRootTreeItem(Entry entry) {
    this.entry = entry;
    entry.addEntityListener(entryListener);

    for(FileLink file : entry.getFiles()) {
      addFileToChildren(file);
    }

    setExpanded(true);
  }

  protected void addFileToChildren(FileLink file) {
    FileTreeItem item = new FileTreeItem(file);
    mapFileToItem.put(file, item);
    getChildren().add(item);
  }

  protected void removeFileFromChildren(FileLink file) {
    if(mapFileToItem.containsKey(file))
      getChildren().remove(mapFileToItem.get(file));
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, final BaseEntity addedEntity) {
      if(collection == ((Entry)collectionHolder).getFiles()) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            addFileToChildren((FileLink)addedEntity);
            TreeItem.childrenModificationEvent();
          }
        });
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, final BaseEntity removedEntity) {
      if(collection == ((Entry)collectionHolder).getFiles()) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            removeFileFromChildren((FileLink)removedEntity);
            TreeItem.childrenModificationEvent();
          }
        });
      }
    }
  };

}
