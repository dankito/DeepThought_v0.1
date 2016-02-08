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

  public final static String AskForDeviceRegistrationResponseMethodName = "AskForDeviceRegistrationResponse";

  public final static String NotifyRemoteWeHaveConnectedMethodName = "DeviceConnected";
  public final static String HeartbeatMethodName = "Heartbeat";

  public final static String StartImportFilesMethodName = "StartImportFile";
  public final static String ImportFilesResultMethodName = "ImportFilesResult";
  public final static String StopImportFilesMethodName = "StopImportFiles";

  public final static String DoOcrOnImageMethodName = "DoOcrOnImage";
  public final static String OcrResultMethodName = "OcrResult";
  public final static String StopDoOcrOnImageMethodName = "StopDoOcrOnImage";

  public final static String StartScanBarcodeMethodName = "StartScanBarcode";
  public final static String ScanBarcodeResultMethodName = "ScanBarcodeResult";
  public final static String StopScanBarcodeMethodName = "StopScanBarcode";

  public final static List<String> MethodNames;


  static {
    MethodNames = Arrays.asList(AskForDeviceRegistrationMethodName, AskForDeviceRegistrationResponseMethodName, NotifyRemoteWeHaveConnectedMethodName, HeartbeatMethodName,
        StartImportFilesMethodName, StopImportFilesMethodName, ImportFilesResultMethodName,
        DoOcrOnImageMethodName, OcrResultMethodName, StopDoOcrOnImageMethodName);
  }


  public static boolean isValidMethod(String methodName) {
    return MethodNames.contains(methodName);
  }


  public static String getAskForDeviceRegistrationAddress(String host, int port) {
    return createAddress(host, port, AskForDeviceRegistrationMethodName);
  }

  public static String getAskForDeviceRegistrationResponseAddress(String host, int port) {
    return createAddress(host, port, AskForDeviceRegistrationResponseMethodName);
  }

  public static String getNotifyRemoteWeHaveConnectedAddress(String host, int port) {
    return createAddress(host, port, NotifyRemoteWeHaveConnectedMethodName);
  }

  public static String getHeartbeatAddress(String host, int port) {
    return createAddress(host, port, HeartbeatMethodName);
  }


  public static String getStartImportFilesAddress(String host, int port) {
    return createAddress(host, port, StartImportFilesMethodName);
  }

  public static String getImportFilesResultAddress(String host, int port) {
    return createAddress(host, port, ImportFilesResultMethodName);
  }

  public static String getStopImportFilesAddress(String host, int port) {
    return createAddress(host, port, StopImportFilesMethodName);
  }


  public static String getDoOcrOnImageAddress(String host, int port) {
    return createAddress(host, port, DoOcrOnImageMethodName);
  }

  public static String getOcrResultAddress(String host, int port) {
    return createAddress(host, port, OcrResultMethodName);
  }

  public static String getStopDoOcrOnImageMethodNameAddress(String host, int port) {
    return createAddress(host, port, StopDoOcrOnImageMethodName);
  }



  public static String getStartScanBarcodeAddress(String host, int port) {
    return createAddress(host, port, StartScanBarcodeMethodName);
  }

  public static String getScanBarcodeResultAddress(String host, int port) {
    return createAddress(host, port, ScanBarcodeResultMethodName);
  }

  public static String getStopScanBarcodeAddress(String host, int port) {
    return createAddress(host, port, StopScanBarcodeMethodName);
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
