package net.deepthought.controls.file;

import net.deepthought.controls.ICleanUp;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.model.FileLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.SetChangeListener;
import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 27/12/14.
 */
public class FileRootTreeItem extends TreeItem<FileLink> implements ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(FileRootTreeItem.class);


  protected IEditedEntitiesHolder<FileLink> editedFiles;

  protected Map<FileLink, FileTreeItem> mapFileToItem = new HashMap<>();


  public FileRootTreeItem(IEditedEntitiesHolder<FileLink> editedFiles) {
    this.editedFiles = editedFiles;
    editedFiles.getEditedEntities().addListener(entryListener);

    for(FileLink file : editedFiles.getEditedEntities()) {
      addFileToChildren(file);
    }

    setExpanded(true);
  }

  @Override
  public void cleanUp() {
    if(editedFiles != null) {
      editedFiles.getEditedEntities().removeListener(entryListener);
      editedFiles = null;
    }
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


  protected SetChangeListener<FileLink> entryListener = new SetChangeListener<FileLink>() {
    @Override
    public void onChanged(Change<? extends FileLink> change) {
      if(change.wasRemoved()) {
        removeFileFromChildren(change.getElementRemoved());
        TreeItem.childrenModificationEvent();
      }

      if(change.wasAdded()) {
        addFileToChildren(change.getElementAdded());
        TreeItem.childrenModificationEvent();
      }
    }
  };

}
