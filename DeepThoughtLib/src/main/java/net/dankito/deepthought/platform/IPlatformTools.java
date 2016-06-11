package net.dankito.deepthought.platform;

/**
 * Created by ganymed on 29/09/15.
 */
public interface IPlatformTools {

  String base64EncodeByteArray(byte[] data);

  byte[] base64DecodeByteArray(String encodedData);

}
