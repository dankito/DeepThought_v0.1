package net.deepthought.util.listener;

import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.FileNameSuggestion;
import net.deepthought.util.enums.ExistingFileHandling;

import java.io.File;

/**
 * Created by ganymed on 01/01/15.
 */
public interface FileOperationListener {

  public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion);

  public void errorOccurred(DeepThoughtError error);

  public void fileOperationDone(boolean successful, File destinationFile);

}
