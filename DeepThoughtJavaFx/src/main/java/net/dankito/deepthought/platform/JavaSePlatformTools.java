package net.dankito.deepthought.platform;

import net.dankito.deepthought.platform.IPlatformTools;

import java.util.Base64;

/**
 * Created by ganymed on 29/09/15.
 */
public class JavaSePlatformTools implements IPlatformTools {

  @Override
  public String base64EncodeByteArray(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }

  @Override
  public byte[] base64DecodeByteArray(String encodedData) {
    return Base64.getDecoder().decode(encodedData);
  }

}
