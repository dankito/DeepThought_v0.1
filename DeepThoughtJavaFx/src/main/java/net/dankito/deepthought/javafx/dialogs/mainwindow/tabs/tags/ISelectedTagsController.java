package net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags;

import net.dankito.deepthought.data.model.Tag;

/**
 * Created by ganymed on 03/01/16.
 */
public interface ISelectedTagsController {

  void selectedTagChanged(Tag selectedTag);

  void removeSelectedTags();

}
