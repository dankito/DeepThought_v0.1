package net.dankito.deepthought.communication.messages;

import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.request.GenericRequest;
import net.dankito.deepthought.communication.Addresses;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 20/11/15.
 */
public abstract class DefaultMessagesReceiverConfig extends MessagesReceiverConfig {

  public DefaultMessagesReceiverConfig(int port, String uriPathStart, AsynchronousResponseListenerManager listenerManager) {
    super(port, uriPathStart, listenerManager);

    setAllowedMethods(getDefaultAllowedMethods());
  }

  protected List<WebMethodConfig> getDefaultAllowedMethods() {
    List<WebMethodConfig> allowedMethods = new ArrayList<>();

    // TODO: place Method names somewhere else
    allowedMethods.add(new WebMethodConfig(Addresses.AskForDeviceRegistrationMethodName, AskForDeviceRegistrationRequest.class));
    allowedMethods.add(new WebMethodConfig(Addresses.AskForDeviceRegistrationResponseMethodName, AskForDeviceRegistrationResponse.class));
    allowedMethods.add(new WebMethodConfig(Addresses.NotifyRemoteWeHaveConnectedMethodName, GenericRequest.class));
    allowedMethods.add(new WebMethodConfig(Addresses.NotifyRemoteWeAreGoingToDisconnectedMethodName, GenericRequest.class));
    allowedMethods.add(new WebMethodConfig(Addresses.HeartbeatMethodName, GenericRequest.class));

    return allowedMethods;
  }
}
