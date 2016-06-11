package net.dankito.deepthought.data.persistence.json;

import com.google.gson.Gson;

import net.dankito.deepthought.data.helper.FileHelper;
import net.dankito.deepthought.data.persistence.serializer.SerializationResult;
import net.dankito.deepthought.util.LogHelper;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * Created by ganymed on 29/09/15.
 */
public class JsonSerializerCompareTest {

  private final static Logger log = LoggerFactory.getLogger(JsonSerializerCompareTest.class);


  @Test
  public void compare_SerializeBinaryData() throws IOException {
    byte[] binaryData = FileHelper.loadTestImage();

    Date jsonIoStartTime = new Date();
    SerializationResult jsonIoResult = JsonIoJsonHelper.generateJsonString(binaryData);
    long jsonIoTimeElapsed = new Date().getTime() - jsonIoStartTime.getTime();
    log.debug("JsonIo took " + LogHelper.createTimeElapsedString(jsonIoTimeElapsed) + " to serialize binary data. Result has a length of " + jsonIoResult.getSerializationResult().length());

    Gson gson = new Gson();
    Date gsonStartTime = new Date();
    String gsonResult = gson.toJson(binaryData);
    long gsonTimeElapsed = new Date().getTime() - gsonStartTime.getTime();
    log.debug("Gson took " + LogHelper.createTimeElapsedString(gsonTimeElapsed) + " to serialize binary data. Result has a length of " + gsonResult.length());

    if(jsonIoTimeElapsed < gsonTimeElapsed)
      log.debug("JsonIo was faster");
    else
      log.debug("Gson was faster");
  }
}
