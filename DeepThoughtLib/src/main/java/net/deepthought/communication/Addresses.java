package net.deepthought.communication;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ganymed on 20/08/15.
 */
public class Addresses {

  public final static String HttpPrefix = "http://";

  public final static String DeepThoughtUriPart = "/DeepThought/";

  public final static String AskForDeviceRegistrationMethodName = "AskForDeviceRegistration";

  public final static String SendAskForDeviceRegistrationResponseMethodName = "AskForDeviceRegistrationResponse";

  public final static String StartCaptureImageAndDoOcrMethodName = "StartCaptureImageAndDoOcr";
  public final static String OcrResultMethodName = "OcrResult";
  public final static String StopCaptureImageAndDoOcrMethodName = "StopCaptureImageAndDoOcr";

  protected final static List<String> MethodNames;


  static {
    MethodNames = Arrays.asList(AskForDeviceRegistrationMethodName, SendAskForDeviceRegistrationResponseMethodName, StartCaptureImageAndDoOcrMethodName, OcrResultMethodName,
                                StopCaptureImageAndDoOcrMethodName);
  }


  public static boolean isValidMethod(String methodName) {
    return MethodNames.contains(methodName);
  }


  public static String getAskForDeviceRegistrationAddress(String host, int port) {
    return createAddress(host, port, AskForDeviceRegistrationMethodName);
  }

  public static String getSendAskForDeviceRegistrationResponseAddress(String host, int port) {
    return createAddress(host, port, SendAskForDeviceRegistrationResponseMethodName);
  }

  public static String getStartCaptureImageAndDoOcrAddress(String host, int port) {
    return createAddress(host, port, StartCaptureImageAndDoOcrMethodName);
  }

  public static String getOcrResultAddress(String host, int port) {
    return createAddress(host, port, OcrResultMethodName);
  }

  public static String getStopCaptureImageAndDoOcrAddress(String host, int port) {
    return createAddress(host, port, StopCaptureImageAndDoOcrMethodName);
  }

  protected static String createAddress(String host, int port, String methodName) {
    String address = HttpPrefix + host;

    if(port > 0)
      address += ":" + port; // TODO: check if host contains a domain like .com -> remove domain, add port and then re-add domain

    address += DeepThoughtUriPart;
    address += methodName;

    return address;
  }
}
