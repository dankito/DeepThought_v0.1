package net.deepthought.controls.file;

import net.deepthought.controls.ICleanUp;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.model.FileLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.SetChangeListener;
import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 27/12/14.
 */
public class FileSearchResultsRootTreeItem extends TreeItem<FileLink> implements ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(FileSearchResultsRootTreeItem.class);


  protected IEditedEntitiesHolder<FileLink> editedFiles;

  protected Map<FileLink, FileTreeItem> mapFileToItem = new HashMap<>();


  public FileSearchResultsRootTreeItem(IEditedEntitiesHolder<FileLink> editedFiles) {
    this.editedFiles = editedFiles;

    if(editedFiles != null)
      editedFiles.getEditedEntities().addListener(entryListener);

    setExpanded(true);
  }

  public void setSearchResults(Collection<FileLink> searchResults) {
    getChildren().clear();

    for(FileLink searchResult : searchResults)
      addFileToChildren(searchResult);
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
      TreeItem.childrenModificationEvent(); // TODO: update only if TreeItem represents modified FileLink
    }
  };

}
