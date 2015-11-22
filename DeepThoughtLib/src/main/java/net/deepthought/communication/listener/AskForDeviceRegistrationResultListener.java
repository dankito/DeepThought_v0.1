package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;

/**
 * Created by ganymed on 20/08/15.
 */
public interface AskForDeviceRegistrationResultListener extends AsynchronousResponseListener<AskForDeviceRegistrationRequest, AskForDeviceRegistrationResponse> {

}
