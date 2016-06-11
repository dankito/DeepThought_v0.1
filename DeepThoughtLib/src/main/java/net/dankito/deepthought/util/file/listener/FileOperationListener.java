package net.dankito.deepthought.util.file.listener;

import net.dankito.deepthought.util.file.enums.ExistingFileHandling;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.file.FileNameSuggestion;

import java.io.File;

/**
 * Created by ganymed on 01/01/15.
 */
public interface FileOperationListener {

  public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion);

  public void errorOccurred(DeepThoughtError error);

  public void fileOperationDone(boolean successful, File destinationFile);

}
