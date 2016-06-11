package net.dankito.deepthought.controls.file;

import net.dankito.deepthought.data.model.FileLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 27/12/14.
 */
public class FileTreeItem extends TreeItem<FileLink> {

  private final static Logger log = LoggerFactory.getLogger(FileTreeItem.class);


  protected FileLink file;

  protected boolean haveChildrenBeenLoaded = false;


  public FileTreeItem(FileLink file) {
    super(file);
    this.file = file;
  }


  @Override
  public boolean isLeaf() {
    return file.isFolder() == false;
  }

  @Override
  public ObservableList<TreeItem<FileLink>> getChildren() {
    if(haveChildrenBeenLoaded == false) {
      haveChildrenBeenLoaded = true;
      super.getChildren().addAll(loadChildren());
    }

    return super.getChildren();
  }

  protected Collection<TreeItem<FileLink>> loadChildren() {
    List<TreeItem<FileLink>> children = new ArrayList<>();

    try {
      File folder = new File(file.getUriString());
      for(File folderFile : folder.listFiles())
        children.add(new FileTreeItem(new FileLink(folderFile.getAbsolutePath(), folderFile.getName(), folderFile.isDirectory())));
    } catch(Exception ex) {
      log.warn("Could not add files to TableTreeView for folder " + file, ex);
    }

    return children;
  }
}
