package net.dankito.deepthought.platform;

import android.util.Base64;

import net.dankito.deepthought.platform.IPlatformTools;

/**
 * Created by ganymed on 29/09/15.
 */
public class AndroidPlatformTools implements IPlatformTools {

  @Override
  public String base64EncodeByteArray(byte[] data) {
    return Base64.encodeToString(data, Base64.NO_WRAP);
  }

  @Override
  public byte[] base64DecodeByteArray(String encodedData) {
    return Base64.decode(encodedData, Base64.DEFAULT);
  }

}
