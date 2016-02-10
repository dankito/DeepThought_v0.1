package net.deepthought.util.file;

import net.deepthought.Application;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.enums.FileType;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.OsHelper;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.enums.ExistingFileHandling;
import net.deepthought.util.file.listener.FileOperationListener;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by ganymed on 01/01/15.
 */
public class FileUtils {
  
  private final static Logger log = LoggerFactory.getLogger(FileUtils.class);


//  public final static String CouldNotDetectMimeType = "Could not detect Mime Type";
  public final static String CouldNotDetectMimeType = "application/octet-stream";

  public final static String CouldNotGetDataFolderForFile = "Could not get data folder for File";

  public final static String UsersFolderName = "users";

  public final static String FilesFolderName = "files";
  public final static String DocumentsFilesFolderName = "documents";
  public final static String ImagesFilesFolderName = "images";
  public final static String AudioFilesFolderName = "audio";
  public final static String VideoFilesFolderName = "video";
  public final static String OtherFilesFolderName = "other";

  public final static String CapturedImagesFolderName = "captured";
  public final static String CapturedImagesTempFolderName = "temp";

  public final static File FileOperationFailed = new File("Failed");



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
          overallSuccessful &= sourceFile.delete();
        }

        if (listener != null)
          listener.fileOperationDone(overallSuccessful, destinationFile);
      }
    });
  }

  public static FileLink moveFileToCapturedImagesFolder(String filePath) {
    return moveFileToCapturedImagesFolder(new FileLink(filePath));
  }

  public static FileLink moveFileToCapturedImagesFolder(FileLink file) {
    String destinationFilename = new File(getCapturedImagesFolder(), file.getName()).getAbsolutePath();

    if(moveFile(file, destinationFilename)) {
      if(isFileInCapturedImagesTempFolder(file)) {
        boolean debug = deleteFile(file.getUriString());
        if(debug) { }
      }

      file.setUriString(destinationFilename);
    }

    return file;
  }

  public static boolean moveFile(final FileLink sourceFile, String destinationFileName) {
    return moveFile(sourceFile, destinationFileName, null);
  }

  public static boolean moveFile(final FileLink sourceFile, String destinationFileName, final FileOperationListener listener) {
    final AtomicBoolean result = new AtomicBoolean(false);

    moveFile(new File(sourceFile.getUriString()), new File(destinationFileName), new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        if(listener != null)
          return listener.destinationFileAlreadyExists(existingFile, newFile, suggestion);
        return ExistingFileHandling.ReplaceExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        if (listener != null)
          listener.errorOccurred(error);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        result.set(successful);

        if (listener != null)
          listener.fileOperationDone(successful, destinationFile);
      }
    });

    return result.get();
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
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        copyFile(sourceFile, destinationFile, listener);
      }
    });
  }

  public static boolean copyFile(File sourceFile, File destinationFile) {
    return copyFile(sourceFile, destinationFile, null);
  }

  public static boolean copyFile(File sourceFile, File destinationFile, FileOperationListener listener) {
    File copiedFile = copyFileRecursively(sourceFile, destinationFile, listener);
    boolean success = copiedFile != FileOperationFailed;

    if (listener != null)
      listener.fileOperationDone(success, copiedFile);

    return success;
  }

  protected static File copyFileRecursively(File sourceFile, File destinationFile, FileOperationListener listener) {
    List<File> destinationFileContainer = new ArrayList<>(); // Java doesn't know ref Parameters so i needed to find another way to return a changed File
    destinationFileContainer.add(destinationFile);
    if (isDestinationFileOk(sourceFile, destinationFileContainer, listener) == false)
      return FileOperationFailed;

    destinationFile = destinationFileContainer.get(0); // TODO: in this way destinationFile is not getting passed back to caller!
    boolean success;

    if (sourceFile.isDirectory())
      success = copyFolder(sourceFile, destinationFile, listener);
    else
      success = copySingleFile(sourceFile, destinationFile, listener);

    return success ? destinationFile : FileOperationFailed;
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
      success &= copyFileRecursively(sourceFile, destinationFile, listener) != FileOperationFailed;
    }

    return success;
  }

  public static String readTextFile(File file) throws IOException {
    return new String(readFile(file), "UTF-8");
  }

  public static byte[] readFile(File file) throws IOException {
    return readFile(new FileInputStream(file));
  }

  public static byte[] readFile(FileInputStream fileInputStream) throws IOException {
    FileChannel source = fileInputStream.getChannel();
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

  public static File findUniqueFileNameInUserDataFolderForUrl(String url) {
    return findUniqueFileNameInUserDataFolderForFile(new File(url));
  }

  public static File findUniqueFileNameInUserDataFolderForFile(File file) {
    String fileName = getFileName(file.getAbsolutePath());
    String extension = getFileExtension(file.getAbsolutePath());

    String directory = getUserDataFolderForFile(file);

    int counter = 2;
    File tmp = new File(directory, fileName + "." + extension);

    while(tmp.exists()) {
      tmp = new File(directory, fileName + "(" + counter++ + ")" + "." + extension);
    }

    return tmp;
  }


  public static String getUserDataFolderForFile(File file) {
    return getUserDataFolderForFile(new FileLink(file.getAbsolutePath()));
  }

  public static String getUserDataFolderForFile(FileLink file) {
    return getUserDataFolderForFile(file.getFileType());
  }

  public static String getUserDataFolderForFile(FileType fileType) {
    String fileDataFolder = getUserDataFolder();

    if(fileDataFolder != Application.CouldNotGetDataFolderPath) {
      try {
        File tmp = new File(fileDataFolder, getUserDataSubFolderForFile(fileType));

        if (tmp.exists() == false)
          tmp.mkdirs();

        fileDataFolder = tmp.getPath();
      } catch(Exception ex) {
        log.error("Could not get or create user's files data folder", ex);
      }
    }

    return fileDataFolder;
  }

  public static String getUserDataFolder() {
    String userDataFolder = Application.getDataFolderPath();

    if(userDataFolder != Application.CouldNotGetDataFolderPath) {
      try {
        File tmp = new File(userDataFolder, UsersFolderName);
        tmp = new File(tmp, String.format("%02d", Application.getLoggedOnUser().getId()));
        tmp = new File(tmp, FilesFolderName);

        if (tmp.exists() == false)
          tmp.mkdirs();

        userDataFolder = tmp.getPath();
      } catch(Exception ex) {
        log.error("Could not get or create user's data folder", ex);
      }
    }

    return userDataFolder;
  }

  public static String getUserDataSubFolderForFile(FileType fileType) {
    String resourceKey = fileType.getNameResourceKey();

    if("file.type.document".equals(resourceKey))
      return DocumentsFilesFolderName;
    else if("file.type.image".equals(resourceKey))
      return ImagesFilesFolderName;
    else if("file.type.audio".equals(resourceKey))
      return AudioFilesFolderName;
    else if("file.type.video".equals(resourceKey))
      return VideoFilesFolderName;

    return OtherFilesFolderName;
  }

  public static String getCapturedImagesFolder() {
    File imagesFolder = new File(getUserDataFolderForFile(FileType.getImageFileType()), CapturedImagesFolderName);
    ensureFolderExists(imagesFolder);

    return imagesFolder.getAbsolutePath();
  }

  public static FileLink createCapturedImageFile() {
    String folder = getCapturedImagesFolder();
    String imageFileName = createCapturedImagesTempFileName();

    File imageFile = new File(folder, imageFileName);

    return new FileLink(imageFile.getAbsolutePath());
  }

  public static String getCapturedImagesTempFolder() {
    File imagesTempFolder = new File(getCapturedImagesFolder(), CapturedImagesTempFolderName);
    ensureFolderExists(imagesTempFolder);

    return imagesTempFolder.getAbsolutePath();
  }

  public static FileLink createCapturedImagesTempFile() {
    String folder = getCapturedImagesTempFolder();
    String imageFileName = createCapturedImagesTempFileName();

    File imageFile = new File(folder, imageFileName);
    try { imageFile.createNewFile(); } catch(Exception ex) { log.error("Could not create temp file for captured image " + imageFileName, ex); }

    return new FileLink(imageFile.getAbsolutePath());
  }

  public static String createCapturedImagesTempFileName() {
    String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss").format(new Date());
    return "captured_" + timeStamp + ".jpg";
  }

  public static boolean isFileInCapturedImagesTempFolder(FileLink file) {
    return true;
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

  public static boolean isImageFileSuitableForHtmlEditor(String mimeType) {
    return isImageFile(mimeType);
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

  public static String getFileNameIncludingExtension(String path) {
    String file = path;
    if(path.contains("/")) // if it's only the file name, path doesn't contain '/'
      file = path.substring(path.lastIndexOf("/") + 1);

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

  protected static MimeTypes mimeTypeDetector = null;

  protected static MimeTypes getMimeTypeDetector() {
    if(mimeTypeDetector == null) {
      if(OsHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(9)) // TODO: make compatible with lower Android versions
        mimeTypeDetector = MimeTypes.getDefaultMimeTypes();
    }

    return mimeTypeDetector;
  }

  public static String getMimeType(FileLink file) {
    return getMimeType(file.getUriString());
  }

  public static String getMimeType(String file) {
//    MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
//    return mimeTypesMap.getContentType(file.).toLowerCase();

    try {
      if(getMimeTypeDetector() != null) {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, file);
        MediaType mediaType = getMimeTypeDetector().detect(null, metadata);
        if (mediaType != null)
          return mediaType.toString();
      }
    } catch(Exception ex) {
      log.warn("Could not detect Mime Type for file " + file, ex);
    }

    return CouldNotDetectMimeType;
  }

  public static FileType getFileType(FileLink file) {
    return getFileType(file.getUriString());
  }

  public static FileType getFileType(String file) {
    String mimeType = getMimeType(file);

    if(mimeType != CouldNotDetectMimeType) {
      if(isDocumentFile(mimeType))
        return FileType.getForResourceKey("file.type.document");
      else if(isImageFile(mimeType))
        return FileType.getForResourceKey("file.type.image");
      else if(isAudioFile(mimeType))
        return FileType.getForResourceKey("file.type.audio");
      else if(isVideoFile(mimeType))
        return FileType.getForResourceKey("file.type.video");
      // TODO: what about if plugins add new file types?
    }

    return FileType.getDefaultFileType();
  }

  public static boolean isFileEmbeddableInHtml(FileLink file) {
    return file != null && isFileEmbeddableInHtml(file.getUriString());
  }

  public static boolean isFileEmbeddableInHtml(String uri) {
    if(getFileType(uri) == FileType.getImageFileType()) {
      String fileExtension = getFileExtension(uri);
      if(fileExtension != null)
        fileExtension = fileExtension.toLowerCase();

      return "jpeg".equals(fileExtension) || "png".equals(fileExtension) || "svg".equals(fileExtension) || "gif".equals(fileExtension) || "bmp".equals(fileExtension)
          || "ico".equals(fileExtension) || "apng".equals(fileExtension) || "jpg".equals(fileExtension) || "jpe".equals(fileExtension);
    }

    return false;
  }

  public static JarFile getDeepThoughtLibJarFile() {
    try {
      return new JarFile(getDeepThoughtLibJarFilePath());
    } catch(Exception ex) {
      log.error("Could not retrieve DeepThoughtLib's Jar file path", ex);
    }

    return null;
  }

  public static String getDeepThoughtLibJarFilePath() {
    try {
      String pathOfAnyResource = "Strings.properties"; // take the name of any Resource that's for save there
      URL url = Application.class.getClassLoader().getResource(pathOfAnyResource);

      String jarPathString = url.toExternalForm();
      jarPathString = jarPathString.replace("!/" + pathOfAnyResource, ""); // remove path of resource from path and last '!/' as well (file ends with '.jar!/'

      if(jarPathString.startsWith("jar:")) // remove leading jar:
        jarPathString = jarPathString.substring(4);
      if(jarPathString.startsWith("file:")) // remove leading file:
        jarPathString = jarPathString.substring(5);

      return jarPathString;
    } catch(Exception ex) {
      log.error("Could not retrieve DeepThoughtLib's Jar file path", ex);
    }

    return null;
  }

  public static File extractJarFileEntry(JarFile jar, JarEntry entry, String destinationDir) throws IOException {
    return extractJarFileEntry(jar, entry, new File(destinationDir));
  }

  public static File extractJarFileEntry(JarFile jar, JarEntry entry, File destinationDir) throws IOException {
    File destinationFile = new File(destinationDir, entry.getName());
    if(destinationDir != null && StringUtils.isNullOrEmpty(destinationDir.getPath()))
      destinationFile = new File(entry.getName());

    try {
      destinationFile.getParentFile().mkdirs();
      try { destinationFile.createNewFile(); } catch(Exception ex) { log.error("Could not create file " + destinationFile.getAbsolutePath(), ex); }

      InputStream inputStream = jar.getInputStream(entry); // get the input stream
      writeToFile(inputStream, destinationFile);

      return destinationFile;
    } catch(Exception ex) {
      log.error("Could not write Jar entry " + entry.getName() + " to temp file " + destinationFile, ex);
    }

    return null;
  }


  public static void writeToFile(String fileContent, File destinationFile) throws Exception {
    // TODO: what to do if file already exists?
    ensureFileExists(destinationFile);

    BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFile));
    writer.write(fileContent);
    writer.close();
  }

  public static void writeToFile(byte[] data, FileLink destinationFile) throws Exception {
    writeToFile(new ByteArrayInputStream(data), new File(destinationFile.getUriString()));
  }

  public static void writeToFile(InputStream inputStream, File destinationFile) throws Exception {
    // TODO: what to do if file already exists?
    ensureFileExists(destinationFile);

    OutputStream outputStream = null;

    try {
      outputStream = new FileOutputStream(destinationFile);

      int read = 0;
      byte[] bytes = new byte[1024];

      while ((read = inputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }

    } catch (IOException ex) {
      log.error("Could not write InputStream to file " + destinationFile.getAbsolutePath(), ex);
      throw ex;
    } finally {
      if (outputStream != null) {
        try {
          // outputStream.flush();
          outputStream.close();
        } catch (IOException e) { }
      }
    }
  }

  public static boolean isLocalFile(String url) {
    try {
      if(url.startsWith("file:"))
        url = url.substring("file:".length());
      File testFile = new File(url);
      return testFile.exists() && "file".equals(testFile.toURI().toURL().getProtocol());
    } catch(Exception ex) { }

    return false;
  }

  public static boolean isOnlineUrl(String url) {
    try {
      URL test = new URL(url);
      String protocol = test.getProtocol();
      return protocol.startsWith("http") || "ftp".equals(protocol);
    } catch(Exception ex) { }

    return false;
  }

  public static boolean isOnlineWebPage(String url) {
    return isOnlineUrl(url) && isOnlineUrlAWebPage(url);
  }

  private static boolean isOnlineUrlAWebPage(String url) {
    // if a http address has less than three '/', no relative path is given at it's the home page
    if(url.startsWith("http") && StringUtils.getNumberOfOccurrences("/", url) < 3) {
      return true;
    }

    String extension = getFileExtension(url).toLowerCase();

    // TODO: for what else extensions to check?
    return "".equals(extension) || "html".equals(extension) || "htm".equals(extension) || "php".equals(extension) || "asp".equals(extension);
  }

  public static boolean isRemoteFile(String url) {
    return isOnlineUrl(url) && isOnlineUrlAWebPage(url) == false;
  }

  public static boolean doesFileExist(String file) {
    return doesFileExist(new File(file));
  }

  public static boolean doesFileExist(File file) {
    return file != null && file.exists();
  }

  protected static void ensureFileExists(File destinationFile) throws IOException {
    if(destinationFile.getParentFile() != null && destinationFile.getParentFile().exists() == false)
      destinationFile.getParentFile().mkdirs();

    if(destinationFile.exists() == false)
      destinationFile.createNewFile();
  }

  public static void ensureFolderExists(String folderPath) {
    ensureFolderExists(new File(folderPath));
  }

  public static void ensureFolderExists(File folder) {
    if(folder.exists() == false)
      folder.mkdirs();
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

  public static String contactPathElements(String pathElement1, String pathElement2) {
    String concatenatedPath = pathElement1;
    if(pathElement1.endsWith("/") == false) {
      concatenatedPath += "/";
    }

    if(pathElement2.startsWith("/")) {
      pathElement2 = pathElement2.substring(1);
    }

    return concatenatedPath + pathElement2;
  }

  public static boolean isAbsoluteUrl(String url) {
    return url.contains("://"); // new File(url).isAbsolute() does not work as it says Urls starting with '/' are absolute
  }

  public static boolean isRelativeUrl(String url) {
    return !isAbsoluteUrl(url);
  }


  public static void openFileInOperatingSystemDefaultApplication(FileLink file) {
    try {
//        HostServices hostServices = DeepThoughtFx.hostServices();
//        hostServices.showDocument(file.getUriString());

//        ProcessBuilder.Redirect redirect = ProcessBuilder.Redirect.to(new java.io.File(file.getUriString()));
      String program = file.getUriString();
      String arguments = "";
      File directory = null;

      String os = System.getProperties().getProperty("os.name");
      log.debug("Running on {}", os);
      if(os.toLowerCase().contains("linux")) {
        arguments = program;
        program = "xdg-open";
        directory = new File("/");
      }
      // TODO: how to start default program on Windows and MacOs?

      ProcessBuilder builder = new ProcessBuilder(program, arguments);
      builder.directory(directory);
      Process process = builder.start();
    } catch(Exception ex) {
      log.error("Could not show file " + file + " in Operating System's default Application", ex);
    }
  }

  public static void showFileInFileManager(FileLink file) {
    // TODO
  }

  public static File createTempFile() {
    return createTempFile(".tmp");
  }

  public static File createTempFile(String fileExtension) {
    String tempDir = getTempDir();

    File tempFile = new File(tempDir, "temp_" + System.currentTimeMillis() + fileExtension);
    tempFile.deleteOnExit();

    return tempFile;
  }

  public static String getTempDir() {
    if(Application.getPlatformConfiguration() != null) {
      return Application.getPlatformConfiguration().getTempDir();
    }

    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    if (!tempDir.exists()) {
      tempDir.mkdirs();
    }
    return tempDir.getAbsolutePath();
  }
}
