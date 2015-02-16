//package net.deepthought.android.json;
//
//import android.test.AndroidTestCase;
//
//import net.deepthought.android.R;
//import net.deepthought.android.db.helper.DataHelper;
//import net.deepthought.android.db.helper.ResourceHelper;
//import net.deepthought.data.model.DeepThoughtApplication;
//import net.deepthought.data.persistence.deserializer.DeserializationResult;
//import net.deepthought.data.persistence.json.JacksonJsonHelper;
//import net.deepthought.data.persistence.serializer.SerializationResult;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.Date;
//
///**
// * Created by ganymed on 06/01/15.
// */
//public class JacksonJsonHelperTest extends AndroidTestCase {
//
//  private final static Logger log = LoggerFactory.getLogger(JacksonJsonHelperTest.class);
//
//
//  public void testSerializeDeepThoughtApplicationToJson() {
//    DeepThoughtApplication application = DataHelper.createTestApplication();
//    Date startTime = new Date();
//
//    SerializationResult serializationResult = JacksonJsonHelper.generateJsonString(application);
//
//    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
//    log.debug("Serialization took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");
//
//    assertTrue(serializationResult.successful());
//    assertTrue(serializationResult.getSerializationResult() != null);
//    assertFalse(serializationResult.getSerializationResult().isEmpty());
//  }
//
//  public void testDeserializeDeepThoughtApplicationFromJson() throws IOException {
//    String json = ResourceHelper.readTextFileFromRawResource(getContext(), R.raw.jackson_test);
//    Date startTime = new Date();
//
//    DeserializationResult<DeepThoughtApplication> deserializationResult = JacksonJsonHelper.parseJsonString(json, DeepThoughtApplication.class);
//
//    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
//    log.debug("Deserialization took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");
//
//    assertTrue(deserializationResult.successful());
//    assertTrue(deserializationResult.getResult() != null);
//  }
//
//}
