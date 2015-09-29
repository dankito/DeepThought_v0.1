package net.deepthought.data.helper;

import net.deepthought.util.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by ganymed on 29/09/15.
 */
public class FileHelper {

  public static byte[] loadTestImage() throws IOException {
    URL imageUrl = FileHelper.class.getClassLoader().getResource("test_image.jpg");
    String imagePath = imageUrl.toExternalForm().replace("file:", "");
    return FileUtils.readFile(new File(imagePath));
  }

}
