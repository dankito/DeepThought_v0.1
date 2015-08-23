package net.deepthought.communication.listener;

import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.OcrResultResponse;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;

/**
 * Created by ganymed on 21/08/15.
 */
public interface MessagesReceiverListener extends UserDeviceRegistrationRequestListener, CaptureImageOrDoOcrListener {

  void askForDeviceRegistrationResponseReceived(AskForDeviceRegistrationResponseMessage message);

  void ocrResult(OcrResultResponse response);

}
