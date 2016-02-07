package net.deepthought.util;

import net.deepthought.data.helper.AssertSetToFalse;
import net.deepthought.data.helper.AssertSetToTrue;
import net.deepthought.data.helper.ErrorOccurred;
import net.deepthought.util.file.FileNameSuggestion;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.file.enums.ExistingFileHandling;
import net.deepthought.util.file.listener.FileOperationListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 01/01/15.
 */
public class FileUtilsTest {

  public final static String TestFilesFolderName = "data/tests/";
  public final static File TestFilesFolder = new File(TestFilesFolderName);

  private final static Logger log = LoggerFactory.getLogger(FileUtilsTest.class);


  @After
  public void tearDown() {
    clearTestFilesFolder();
  }



  @Test
  public void copyFile_DestinationFileDoesNotExist_FileGetsCopied() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File destinationFile = createTestFile("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();
    final AssertSetToFalse destinationFileAlreadyExists = new AssertSetToFalse(false);

    FileUtils.copyFile(sourceFile, destinationFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        destinationFileAlreadyExists.setValue(true);
        return ExistingFileHandling.KeepExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(destinationFileAlreadyExists.getValue());
    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder + 1, TestFilesFolder.list().length); // assert a file has been added
  }

  @Test
  public void copyFile_DestinationFileExists_KeepExistingFile_NoFileGetsChanged() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.copyFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.KeepExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertFalse(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder, TestFilesFolder.list().length); // assert no file has been added
  }

  @Test
  public void copyFile_DestinationFileExists_ReplaceExistingFile_ExistingFileGetsReplaced() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.copyFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.ReplaceExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder, TestFilesFolder.list().length); // assert the amount of files stays the same
  }

  @Test
  public void copyFile_DestinationFileExists_RenameNewFile_ExistingFileWillBeKeptNewFileRenamed() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;
    final List<File> newFileContainer = new ArrayList<>();

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.copyFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        newFileContainer.add(destinationFile);
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder + 1, TestFilesFolder.list().length); // assert a file has been added
    Assert.assertEquals(1, newFileContainer.size());
    Assert.assertNotEquals(existingFile.getAbsolutePath(), newFileContainer.get(0).getAbsolutePath());
  }

  @Test
  public void copyFile_DestinationFileExists_RenameNewFileToASuggestedValue_ExistingFileWillBeKeptNewFileRenamedToSuggestion() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;
    final List<File> newFileContainer = new ArrayList<>();
    final String newFileNameSuggestion = "super_duper_destination_file.txt";

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.copyFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        suggestion.setFileNameSuggestion(newFileNameSuggestion);
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        newFileContainer.add(destinationFile);
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder + 1, TestFilesFolder.list().length); // assert a file has been added

    Assert.assertEquals(1, newFileContainer.size());
    File newFile = newFileContainer.get(0);
    Assert.assertNotEquals(existingFile.getAbsolutePath(), newFile.getAbsolutePath());
    Assert.assertEquals(newFileNameSuggestion, newFile.getName());
  }

  @Test
  public void copyFile_DestinationFileExists_RenameExistingFile_ExistingFileRenamedNewFileCopied() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.copyFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.RenameExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder + 1, TestFilesFolder.list().length); // assert a file has been added
  }


  /*          Move file             */


  @Test
  public void moveFile_DestinationFileDoesNotExist_FileGetsMoved() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File destinationFile = createTestFile("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();
    final AssertSetToFalse destinationFileAlreadyExists = new AssertSetToFalse(false);

    FileUtils.moveFile(sourceFile, destinationFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        destinationFileAlreadyExists.setValue(true);
        return ExistingFileHandling.KeepExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(destinationFileAlreadyExists.getValue());
    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());

    Assert.assertEquals(countFilesInTestFolder, TestFilesFolder.list().length); // assert no file has been added
    Assert.assertFalse(sourceFile.exists()); // assert file has been moved
    Assert.assertTrue(destinationFile.exists());
  }

  @Test
  public void moveFile_DestinationFileExists_KeepExistingFile_NoFileGetsChanged() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.moveFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.KeepExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertFalse(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder, TestFilesFolder.list().length); // assert no file has been added / moved
  }

  @Test
  public void moveFile_DestinationFileExists_ReplaceExistingFile_ExistingFileGetsReplaced() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.moveFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.ReplaceExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder - 1, TestFilesFolder.list().length); // assert the amount of files has been reduced
  }

  @Test
  public void moveFile_DestinationFileExists_RenameNewFile_ExistingFileWillBeKeptNewFileRenamed() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;
    final List<File> newFileContainer = new ArrayList<>();

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.moveFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        newFileContainer.add(destinationFile);
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());

    Assert.assertEquals(countFilesInTestFolder, TestFilesFolder.list().length); // assert the amount of files stays the same
    Assert.assertFalse(sourceFile.exists());
    Assert.assertEquals(1, newFileContainer.size());
    Assert.assertNotEquals(existingFile.getAbsolutePath(), newFileContainer.get(0).getAbsolutePath());
  }

  @Test
  public void moveFile_DestinationFileExists_RenameNewFileToASuggestedValue_ExistingFileWillBeKeptNewFileRenamedToSuggestion() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;
    final List<File> newFileContainer = new ArrayList<>();
    final String newFileNameSuggestion = "super_duper_destination_file.txt";

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.moveFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        suggestion.setFileNameSuggestion(newFileNameSuggestion);
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        newFileContainer.add(destinationFile);
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());
    Assert.assertEquals(countFilesInTestFolder, TestFilesFolder.list().length); // assert the amount of files stays the same
    Assert.assertFalse(sourceFile.exists());

    Assert.assertEquals(1, newFileContainer.size());
    File newFile = newFileContainer.get(0);
    Assert.assertNotEquals(existingFile.getAbsolutePath(), newFile.getAbsolutePath());
    Assert.assertEquals(newFileNameSuggestion, newFile.getName());
  }

  @Test
  public void moveFile_DestinationFileExists_RenameExistingFile_ExistingFileRenamedNewFileMoved() {
    File sourceFile = ensureTestFileExists("source_file.txt");
    File existingFile = ensureTestFileExists("destination_file.txt");

    int countFilesInTestFolder = TestFilesFolder.list().length;

    final ErrorOccurred errorOccurred = new ErrorOccurred();
    final AssertSetToTrue fileOperationSucceeded = new AssertSetToTrue();

    FileUtils.moveFile(sourceFile, existingFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.RenameExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        errorOccurred.setErrorOccurred(true);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        fileOperationSucceeded.setValue(successful);
      }
    });

    Assert.assertFalse(errorOccurred.hasErrorOccurred());
    Assert.assertTrue(fileOperationSucceeded.isSetToTrue());

    Assert.assertEquals(countFilesInTestFolder, TestFilesFolder.list().length); // assert the amount of files stays the same
    Assert.assertFalse(sourceFile.exists());
  }


  protected File createTestFile(String testFileName) {
    return new File(TestFilesFolderName, testFileName);
  }

  protected File ensureTestFileExists(String testFileName) {
    File testFile = createTestFile(testFileName);
    try {
      if(testFile.exists() == false) {
        testFile.getParentFile().mkdirs();
        testFile.createNewFile();
      }
    } catch(Exception ex) {
      log.error("Could not create test file " + testFile.getAbsolutePath(), ex);
    }

    return testFile;
  }

  protected void clearTestFilesFolder() {
    File testFileFolder = new File(TestFilesFolderName);
    for(File testFile : testFileFolder.listFiles()) {
      try {
        testFile.delete();
      } catch (Exception ex) {
        log.error("Could not delete test file " + testFileFolder.getAbsolutePath(), ex);
      }
    }
  }
}
