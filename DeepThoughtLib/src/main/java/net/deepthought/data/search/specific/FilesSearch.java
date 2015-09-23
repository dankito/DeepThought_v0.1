package net.deepthought.data.search.specific;

import net.deepthought.data.model.FileLink;
import net.deepthought.data.search.Search;
import net.deepthought.data.search.SearchCompletedListener;

import java.util.Collection;

/**
 * Created by ganymed on 27/07/15.
 */
public class FilesSearch extends Search<FileLink> {


  protected boolean searchFileName;

  protected boolean searchFileUri;

  protected boolean searchFileDescription;


  public FilesSearch(String searchTerm, SearchCompletedListener<Collection<FileLink>> completedListener) {
    this(searchTerm, true, true, true, completedListener);
  }

  public FilesSearch(String searchTerm, boolean searchFileName, boolean searchFileUri, boolean searchFileDescription, SearchCompletedListener<Collection<FileLink>> completedListener) {
    super(searchTerm, completedListener);
    this.searchFileName = searchFileName;
    this.searchFileUri = searchFileUri;
    this.searchFileDescription = searchFileDescription;
  }


  public boolean searchFileName() {
    return searchFileName;
  }

  public boolean searchFileUri() {
    return searchFileUri;
  }

  public boolean searchFileDescription() {
    return searchFileDescription;
  }

}
