package net.deepthought.communication.registration;

import net.deepthought.communication.CommunicationTestBase;
import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.ConnectorMessagesCreatorConfig;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.HostInfo;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 19/08/15.
 */
public class LookingForRegistrationServersClientTest extends CommunicationTestBase {

  @Override
  public void setup() throws Exception {
    super.setup();

    messagesCreator = new ConnectorMessagesCreator(new ConnectorMessagesCreatorConfig(loggedOnUser, localDevice, NetworkHelper.getIPAddressString(true), CommunicatorPort));
  }


  @Test
  public void noRegistrationServersOpen_ClientReceivesNoResponse() {
    final List<HostInfo> serverResponse = new ArrayList<>();
    final CountDownLatch waitForResponseLatch = new CountDownLatch(1);

    LookingForRegistrationServersClient client = new LookingForRegistrationServersClient(messagesCreator, registeredDevicesManager, threadPool);
    client.findRegistrationServersAsync(new IUnregisteredDevicesListener() {
      @Override
      public void unregisteredDeviceFound(HostInfo serverInfo) {
        serverResponse.add(serverInfo);
        waitForResponseLatch.countDown();
      }

      @Override
      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {

      }
    });

    try { waitForResponseLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }
    Assert.assertEquals(0, serverResponse.size());

    client.stopSearchingForRegistrationServers();
  }

  @Test
  public void registrationServerIsOpen_RequestGetsReplied() {
    final List<HostInfo> serverInfos = new ArrayList<>();
    final CountDownLatch waitForResponseLatch = new CountDownLatch(1);

    RegistrationServer registrationServer = new RegistrationServer(messagesCreator, threadPool);
    registrationServer.startRegistrationServerAsync();

    LookingForRegistrationServersClient client = new LookingForRegistrationServersClient(messagesCreator, registeredDevicesManager, threadPool);
    client.findRegistrationServersAsync(new IUnregisteredDevicesListener() {
      @Override
      public void unregisteredDeviceFound(HostInfo serverInfo) {
        serverInfos.add(serverInfo);
        waitForResponseLatch.countDown();
      }

      @Override
      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {

      }
    });

    try { waitForResponseLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, serverInfos.size());

    registrationServer.closeRegistrationServer();
    client.stopSearchingForRegistrationServers();
  }

  @Test
  public void requestGetsReplied_HostInfoIsValid() {
    final List<HostInfo> serverInfos = new ArrayList<>();
    final CountDownLatch waitForResponseLatch = new CountDownLatch(1);

    RegistrationServer registrationServer = new RegistrationServer(messagesCreator, threadPool);
    registrationServer.startRegistrationServerAsync();

    LookingForRegistrationServersClient client = new LookingForRegistrationServersClient(messagesCreator, registeredDevicesManager, threadPool);
    client.findRegistrationServersAsync(new IUnregisteredDevicesListener() {
      @Override
      public void unregisteredDeviceFound(HostInfo serverInfo) {
        serverInfos.add(serverInfo);
        waitForResponseLatch.countDown();
      }

      @Override
      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {

      }
    });

    try { waitForResponseLatch.await(100, TimeUnit.SECONDS); } catch(Exception ex) { }

    HostInfo serverInfo = serverInfos.get(0);
    Assert.assertNotNull(serverInfo);
    Assert.assertEquals(NetworkHelper.getIPAddressString(true), serverInfo.getIpAddress());

    registrationServer.closeRegistrationServer();
    client.stopSearchingForRegistrationServers();
  }
}
