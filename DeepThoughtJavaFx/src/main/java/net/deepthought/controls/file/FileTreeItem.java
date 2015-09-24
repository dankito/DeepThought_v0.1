package net.deepthought.controls.file;

import net.deepthought.data.model.FileLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 27/12/14.
 */
public class FileTreeItem extends TreeItem<FileLink> {

  private final static Logger log = LoggerFactory.getLogger(FileTreeItem.class);


  protected FileLink file;


  public FileTreeItem(FileLink file) {
    super(file);
    this.file = file;

    if(file.isFolder()) { // TODO: implement lazy loading
      try {
        File folder = new File(file.getUriString());
        for(File folderFile : folder.listFiles())
          getChildren().add(new FileTreeItem(new FileLink(folderFile.getAbsolutePath(), folderFile.getName(), folderFile.isDirectory())));
      } catch(Exception ex) {
        log.warn("Could not add files to TableTreeView for folder " + file, ex);
      }
    }
  }

}
