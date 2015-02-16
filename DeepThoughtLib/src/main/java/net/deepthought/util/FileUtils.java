package net.deepthought.util;

import net.deepthought.Application;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.enums.ExistingFileHandling;
import net.deepthought.util.listener.FileOperationListener;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 01/01/15.
 */
public class FileUtils {
  
  private final static Logger log = LoggerFactory.getLogger(FileUtils.class);
  

  public final static String CouldNotGetDataFolderForFile = "Could not get data folder for File";

  public final static String UsersFolderName = "users";

  public final static String FilesFolderName = "files";
  public final static String DocumentsFilesFolderName = "documents";
  public final static String ImagesFilesFolderName = "images";
  public final static String AudioFilesFolderName = "audio";
  public final static String VideoFilesFolderName = "video";
  public final static String OtherFilesFolderName = "other";



  public static void copyFileToDataFolder(final FileLink file, final FileOperationListener listener) {
    String dataFolder = FileUtils.getUserDataFolderForFile(file);
    if(dataFolder != FileUtils.CouldNotGetDataFolderForFile) {
      File fileToCopy = null, destinationFile = null;
      try {
        fileToCopy = new File(file.getUriString());
        destinationFile = new File(dataFolder, fileToCopy.getName());
        copyFileAsync(fileToCopy, destinationFile, new FileOperationListener() {
          @Override
          public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
            if (listener != null)
              return listener.destinationFileAlreadyExists(existingFile, newFile, suggestion);
            else
              return ExistingFileHandling.KeepExistingFile;
          }

          @Override
          public void errorOccurred(DeepThoughtError error) {
            if (listener != null)
              listener.errorOccurred(error);
          }

          @Override
          public void fileOperationDone(boolean successful, File destinationFile) {
            if (successful) {
              file.setUriString(destinationFile.getPath());
            }

            if (listener != null)
              listener.fileOperationDone(successful, destinationFile);
          }
        });
      } catch(Exception ex) {
        log.error("Could not copyFile file " + file.getUriString() + " to data folder " + dataFolder, ex);
        if(listener != null) {
          listener.errorOccurred(DeepThoughtError.errorFromLocalizationKey(ex, "error.could.not.copy.file.to.destination", fileToCopy.getAbsolutePath(),
              destinationFile.getAbsolutePath(), ex.getLocalizedMessage()));
          listener.fileOperationDone(false, new File(file.getUriString()));
        }
      }
    }
    else {
      if(listener != null) {
        listener.errorOccurred(DeepThoughtError.errorFromLocalizationKey("error.could.not.create.file.data.folder", file.getUriString()));
        listener.fileOperationDone(false, new File(file.getUriString()));
      }
    }
  }

  public static void moveFileToDataFolder(FileLink file, final FileOperationListener listener) {
    final File sourceFile = new File(file.getUriString());

    copyFileToDataFolder(file, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        if(listener != null)
          return listener.destinationFileAlreadyExists(existingFile, newFile, suggestion);
        else
          return ExistingFileHandling.KeepExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        if(listener != null)
          listener.errorOccurred(error);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        boolean overallSuccessful = successful;
        if(successful) {
          overallSuccessful &= sourceFile.delete();
        }

        if(listener != null)
          listener.fileOperationDone(overallSuccessful, destinationFile);
      }
    });
  }

  public static void moveFile(File sourceFile, File destinationFile) {
    moveFile(sourceFile, destinationFile, null);
  }

  public static void moveFile(final File sourceFile, File destinationFile, final FileOperationListener listener) {
    copyFile(sourceFile, destinationFile, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        if (listener != null)
          return listener.destinationFileAlreadyExists(existingFile, newFile, suggestion);
        else
          return ExistingFileHandling.KeepExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        if (listener != null)
          listener.errorOccurred(error);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        boolean overallSuccessful = successful;
        if (successful) {
          overallSuccessful &= FileUtils.deleteFile(sourceFile);
        }

        if (listener != null)
          listener.fileOperationDone(overallSuccessful, destinationFile);
      }
    });
  }

  public static void copyFileAsync(File sourceFile, File destinationFile) {
    copyFileAsync(sourceFile, destinationFile, null);
  }

  public static void copyFileAsync(final File sourceFile, final File destinationFile, final FileOperationListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        copyFile(sourceFile, destinationFile, listener);
      }
    }).start();
  }

  public static boolean copyFile(File sourceFile, File destinationFile) {
    return copyFile(sourceFile, destinationFile, null);
  }

  public static boolean copyFile(File sourceFile, File destinationFile, FileOperationListener listener) {
    boolean success = copyFileRecursively(sourceFile, destinationFile, listener);

    if (listener != null)
      listener.fileOperationDone(success, destinationFile);

    return success;
  }

  protected static boolean copyFileRecursively(File sourceFile, File destinationFile, FileOperationListener listener) {
    List<File> destinationFileContainer = new ArrayList<>(); // Java doesn't know ref Parameters so i needed to find another way to return a changed File
    destinationFileContainer.add(destinationFile);
    if (isDestinationFileOk(sourceFile, destinationFileContainer, listener) == false)
      return false;

    destinationFile = destinationFileContainer.get(0);
    boolean success;

    if (sourceFile.isDirectory())
      success = copyFolder(sourceFile, destinationFile, listener);
    else
      success = copySingleFile(sourceFile, destinationFile, listener);

    return success;
  }

  protected static boolean copySingleFile(File sourceFile, File destinationFile, FileOperationListener listener) {
    try {
      FileChannel source = null;
      FileChannel destination = null;

      try {
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destinationFile).getChannel();
        destination.transferFrom(source, 0, source.size());
        destinationFile.setLastModified(System.currentTimeMillis());

        return true;
      } catch (Exception ex) {
        log.error("Could not copyFile " + sourceFile + " to " + destinationFile, ex);
        if (listener != null) {
          listener.errorOccurred(DeepThoughtError.errorFromLocalizationKey(ex, "error.could.not.copy.file.to.destination", sourceFile.getAbsolutePath(),
              destinationFile.getAbsolutePath(), ex.getLocalizedMessage()));
          listener.fileOperationDone(false, destinationFile);
        }
      } finally {
        if (source != null) {
          source.close();
        }
        if (destination != null) {
          destination.close();
        }
      }
    } catch(Exception ex){
      log.error("An error occurred while trying to copyFile " + sourceFile.getAbsolutePath() + " to " + destinationFile.getAbsolutePath(), ex);
    }

    return false;
  }

  protected static boolean copyFolder(File sourceFolder, File destinationFolder, FileOperationListener listener) {
    boolean success = true;

    if(destinationFolder.exists() == false){
      boolean result = destinationFolder.mkdirs();
      log.debug("Creating Directory {} was {}successful", destinationFolder.getAbsolutePath(), result ? "" : "not ");
    }
    if(destinationFolder.isDirectory() == false) {
      boolean result = destinationFolder.mkdir();
      log.debug("Creating Directory {} was {}successful", destinationFolder.getAbsolutePath(), result ? "" : "not ");
    }

    for (String file : sourceFolder.list()) {
      File sourceFile = new File(sourceFolder, file);
      File destinationFile = new File(destinationFolder, file);
      success &= copyFileRecursively(sourceFile, destinationFile, listener);
    }

    return success;
  }

  public static String readTextFile(File file) throws IOException {
    return new String(readFile(file), "UTF-8");
  }

  public static byte[] readFile(File file) throws IOException {
    FileChannel source = new FileInputStream(file).getChannel();
    ByteBuffer buffer = ByteBuffer.allocate((int) source.size());
    int bytesRead = source.read(buffer);
    return buffer.array();
  }

  protected static boolean isDestinationFileOk(File sourceFile, List<File> destinationFileContainer, FileOperationListener listener) {
    File destinationFile = destinationFileContainer.get(0);

    try {
      if (destinationFile.exists() == false) {
        if(destinationFile.getParentFile() != null && destinationFile.getParentFile().exists() == false)
          destinationFile.getParentFile().mkdirs();
        if(sourceFile.isDirectory())
          sourceFile.mkdir();
        else
          destinationFile.createNewFile();
      }
      else {
        if(listener == null) // cannot ask user what to do -> abort
          return false;
        else {
          FileNameSuggestion suggestion = new FileNameSuggestion(findUniqueFileName(destinationFile));
          ExistingFileHandling existingFileHandling = listener.destinationFileAlreadyExists(destinationFile, sourceFile, suggestion);
          if(existingFileHandling == ExistingFileHandling.KeepExistingFile) {
            if(listener != null)
              listener.fileOperationDone(false, destinationFile);
            return false;
          }
          else if(existingFileHandling == ExistingFileHandling.ReplaceExistingFile) // nothing to do, file will simply be overwritten
            return true;
          else if(existingFileHandling == ExistingFileHandling.RenameNewFile) {
            File newFileName = new File(destinationFile.getParent(), suggestion.getFileNameSuggestion());
            destinationFileContainer.clear();
            destinationFileContainer.add(newFileName);
          }
          else if(existingFileHandling == ExistingFileHandling.RenameExistingFile) {
            File newFileName = new File(destinationFile.getParent(), suggestion.getFileNameSuggestion());
            destinationFile.renameTo(newFileName);
            newFileName.setLastModified(System.currentTimeMillis());
          }
        }
      }
    } catch(Exception ex) {
      log.error("Could not create destination file " + destinationFile + " to copyFile " + sourceFile + " to it", ex);
      if(listener != null) {
        listener.errorOccurred(DeepThoughtError.errorFromLocalizationKey(ex, "error.could.not.create.destination.file", destinationFile.getAbsolutePath(), ex.getLocalizedMessage()));
        listener.fileOperationDone(false, destinationFile);
      }
      return false;
    }

    return true;
  }

  public static boolean isFileUnique(File file, ExistingFileHandling existingFileHandling) {
    try {
      if (file.exists() == false) {
        if(file.getParentFile() != null && file.getParentFile().exists() == false)
          file.getParentFile().mkdirs();
        file.createNewFile();
      }
      else {
        if(existingFileHandling == ExistingFileHandling.KeepExistingFile) {
          return false;
        }
        else if(existingFileHandling == ExistingFileHandling.ReplaceExistingFile) {
          file.delete();
          file.createNewFile();
          return true;
        }
        else if (existingFileHandling == ExistingFileHandling.RenameNewFile) {
          // TODO: i cannot do anything in this method as file parameter is copied (not passed by reference)
          return false;
        }
        else if (existingFileHandling == ExistingFileHandling.RenameExistingFile) {
          File newFileName = new File(file.getParent(), findUniqueFileName(file));
          file.renameTo(newFileName);
          return true;
        }
      }
    } catch(Exception ex) {
      log.error("Could not determine if " + file.getAbsolutePath() + " is unique", ex);
      return false;
    }

    return true;
  }

  public static String findUniqueFileName(File existingFile) {
    return findUniqueFileNameForPath(existingFile).getName();
  }

  public static File findUniqueFileNameForPath(File existingFile) {
    int counter = 2;
    File tmp = null;
    String fileName = getFileName(existingFile.getAbsolutePath());
    String extension = getFileExtension(existingFile.getAbsolutePath());

    do {
      tmp = new File(existingFile.getParent(), fileName + "(" + counter++ + ")" + "." + extension);
    } while(tmp.exists());

    return tmp;
  }


  public static String getUserDataFolderForFile(FileLink file) {
    String fileDataFolder = CouldNotGetDataFolderForFile;

    String dataFolder = Application.getDataFolderPath();
    if(dataFolder != Application.CouldNotGetDataFolderPath) {
      try {
        File tmp = new File(dataFolder, UsersFolderName);
        tmp = new File(tmp, Application.getLoggedOnUser().getUserName());
        tmp = new File(tmp, FilesFolderName);
        tmp = new File(tmp, getFileUserDataSubFolder(file));

        if (tmp.exists() == false)
          tmp.mkdirs();

        fileDataFolder = tmp.getPath();
      } catch(Exception ex) {
        log.error("Could not get or create user's files data folder", ex);
      }
    }

    return fileDataFolder;
  }

  public static String getFileUserDataSubFolder(FileLink file) {
    String mimeType = getMimeType(file);
    
    if(isDocumentFile(mimeType))
      return DocumentsFilesFolderName;
    else if(isImageFile(mimeType))
      return ImagesFilesFolderName;
    else if(isAudioFile(mimeType))
      return AudioFilesFolderName;
    else if(isVideoFile(mimeType))
      return VideoFilesFolderName;

    return OtherFilesFolderName;
  }

  public static boolean isDocumentFile(FileLink file) {
    return isDocumentFile(getMimeType(file));
  }

  public static boolean isDocumentFile(String mimeType) {
    return mimeType.startsWith("text") || mimeType.contains("opendocument") || mimeType.contains("officedocument") || mimeType.contains("pdf") || mimeType.contains("msword") || mimeType.contains("ms-excel") ||
        mimeType.contains("ms-powerpoint") || mimeType.endsWith("rtf");
  }

  public static boolean isImageFile(FileLink file) {
    return isImageFile(getMimeType(file));
  }

  public static boolean isImageFile(String mimeType) {
    return mimeType.startsWith("image");
  }

  public static boolean isAudioFile(FileLink file) {
    return isAudioFile(getMimeType(file));
  }

  public static boolean isAudioFile(String mimeType) {
    return mimeType.startsWith("audio");
  }

  public static boolean isVideoFile(FileLink file) {
    return isVideoFile(getMimeType(file));
  }

  public static boolean isVideoFile(String mimeType) {
    return mimeType.startsWith("video") || mimeType.equals("model/vnd.mts");
  }


  public static String getFileName(String path) {
    String file = path;
    if(path.contains("/")) // if it's only the file name, path doesn't contain '/'
      file = path.substring(path.lastIndexOf("/") + 1);

    if(file.contains(".") == false) // a file without an extension
      return file;

    file = file.replace(getFileExtension(file), "");
    if(file.endsWith(".")) // getFileExtension() doesn't return '.' before file extension
      file = file.substring(0, file.length() - 1);
    return file;
  }

  public static String getFileExtension(File file) {
    return getFileExtension(file.getAbsolutePath());
  }

  public static String getFileExtension(String path) {
    String file = path;
    if(path.contains("/")) // if it's only the file name, path doesn't contain '/'
      file = path.substring(path.lastIndexOf("/") + 1);

    if(file.contains(".") == false) // a file without an extension
      return "";
    return file.substring(file.lastIndexOf(".") + 1);
  }

  // TODO: make compatible with Android
  protected static Tika tika = new Tika();

  public static String getMimeType(FileLink file) {
//    MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
//    return mimeTypesMap.getContentType(file.getUriString()).toLowerCase();

    return tika.detect(file.getUriString());
  }

  public static void writeToFile(String fileContent, File destinationFile) throws Exception {
    // TODO: what to do if file already exists?
    ensureFileExists(destinationFile);

    BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFile));
    writer.write(fileContent);
    writer.close();
  }

  protected static void ensureFileExists(File destinationFile) throws IOException {
    if(destinationFile.getParentFile() != null && destinationFile.getParentFile().exists() == false)
      destinationFile.getParentFile().mkdirs();

    if(destinationFile.exists() == false)
      destinationFile.createNewFile();
  }

  public static boolean deleteFile(String path) {
    return deleteFile(new File(path));
  }

  public static boolean deleteFile(File file) {
    if(file.isDirectory() == false)
      return file.delete();
    else {
      File[] files = file.listFiles();
      boolean result = true;

      if(files != null) { //some JVMs return null for empty dirs
        for(File f: files) {
          if(f.isDirectory()) {
            result &= deleteFile(f);
          } else {
            result &= f.delete();
          }
        }
      }

      result &= file.delete();

      return result;
    }
  }
}
