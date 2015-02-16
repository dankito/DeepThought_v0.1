package net.deepthought.android.db.helper;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

/**
 * Created by ganymed on 07/01/15.
 */
public class ResourceHelper {

  public static String readTextFileFromRawResource(Context context, int resId) throws IOException {
    InputStream inputStream = context.getResources().openRawResource(resId);
    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

    CharBuffer buffer = CharBuffer.allocate(inputStream.available());
    inputStreamReader.read(buffer);
    char[] chars = buffer.array();

    return new String(chars);
  }

}
