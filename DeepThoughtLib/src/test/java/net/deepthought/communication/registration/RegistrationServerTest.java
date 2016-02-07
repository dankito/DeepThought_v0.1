package net.deepthought.communication.registration;

import net.deepthought.communication.CommunicationTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 19/08/15.
 */
public class RegistrationServerTest extends CommunicationTestBase {


  @Test
  public void registrationServerIsOpen_ServerGetsFound() {
    final List<DatagramPacket> receivedRequestPackets = new ArrayList<>();
    final CountDownLatch waitForRequestPacketLatch = new CountDownLatch(1);

    RegistrationServer registrationServer = new RegistrationServer(messagesCreator, threadPool) {
      @Override
      protected void respondToRegistrationRequest(DatagramPacket requestPacket) {
        receivedRequestPackets.add(requestPacket);
        waitForRequestPacketLatch.countDown();
      }
    };
    registrationServer.startRegistrationServerAsync();

    LookingForRegistrationServersClient client = new LookingForRegistrationServersClient(messagesCreator, registeredDevicesManager, threadPool);
    client.findRegistrationServersAsync(null);

    try { waitForRequestPacketLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, receivedRequestPackets.size());

    registrationServer.closeRegistrationServer();
    client.stopSearchingForRegistrationServers();
  }

  @Test
  public void registrationServerIsOpen_ThreeClientsLikeToRegister_AllRequestAreReceived() {
    final Set<Integer> portsMessagesReceivedFrom = new HashSet<>();
    final CountDownLatch waitForRequestsLatch = new CountDownLatch(1);

    RegistrationServer registrationServer = new RegistrationServer(messagesCreator, threadPool) {
      @Override
      protected void respondToRegistrationRequest(DatagramPacket requestPacket) {
        portsMessagesReceivedFrom.add(requestPacket.getPort());
        if(portsMessagesReceivedFrom.size() == 3)
          waitForRequestsLatch.countDown();
      }
    };
    registrationServer.startRegistrationServerAsync();

    LookingForRegistrationServersClient client1 = new LookingForRegistrationServersClient(messagesCreator, registeredDevicesManager, threadPool);
    client1.findRegistrationServersAsync(null);
    LookingForRegistrationServersClient client2 = new LookingForRegistrationServersClient(messagesCreator, registeredDevicesManager, threadPool);
    client2.findRegistrationServersAsync(null);
    LookingForRegistrationServersClient client3 = new LookingForRegistrationServersClient(messagesCreator, registeredDevicesManager, threadPool);
    client3.findRegistrationServersAsync(null);

    try { waitForRequestsLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, portsMessagesReceivedFrom.size());

    registrationServer.closeRegistrationServer();
    client1.stopSearchingForRegistrationServers();
    client2.stopSearchingForRegistrationServers();
    client3.stopSearchingForRegistrationServers();
  }
}
