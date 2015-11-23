package net.deepthought.data.helper;

import net.deepthought.util.StreamHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ganymed on 29/09/15.
 */
public class FileHelper {

  public static byte[] loadTestImage() throws IOException {
    InputStream stream = FileHelper.class.getClassLoader().getResourceAsStream("test_image.jpg");
    return StreamHelper.readBytesFromInputStream(stream);
  }

}
