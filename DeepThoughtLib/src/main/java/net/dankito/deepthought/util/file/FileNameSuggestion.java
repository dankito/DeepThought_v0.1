package net.dankito.deepthought.util.file;

/**
 * Created by ganymed on 01/01/15.
 */
public class FileNameSuggestion {

  protected String fileNameSuggestion = "";

  public FileNameSuggestion() {

  }

  public FileNameSuggestion(String fileNameSuggestion) {
    this.fileNameSuggestion = fileNameSuggestion;
  }


  public String getFileNameSuggestion() {
    return fileNameSuggestion;
  }

  public void setFileNameSuggestion(String fileNameSuggestion) {
    this.fileNameSuggestion = fileNameSuggestion;
  }


  @Override
  public String toString() {
    return fileNameSuggestion;
  }

}
