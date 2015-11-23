package net.deepthought.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ganymed on 23/11/15.
 */
public class StreamHelper {

  public static byte[] readBytesFromInputStream(InputStream input) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = input.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    return buffer.toByteArray();
  }

}
