package net.deepthought.communication.registration;

import net.deepthought.communication.CommunicationTestBase;
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
public class RegistrationServerTest extends CommunicationTestBase {


  @Test
  public void noRegistrationServerOpen_ClientReceivesNoResponse() {
    final List<HostInfo> serverResponse = new ArrayList<>();

    LookingForRegistrationServersClient client = new LookingForRegistrationServersClient(messagesCreator);
    client.findRegistrationServersAsync(new RegistrationRequestListener() {
      @Override
      public void openRegistrationServerFound(HostInfo serverInfo) {
        serverResponse.add(serverInfo);
      }
    });

    CountDownLatch latch = new CountDownLatch(1);
    try { latch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }
    Assert.assertEquals(0, serverResponse.size());

    client.stopSearchingForRegistrationServers();
  }

  @Test
  public void registrationServerIsOpen_ServerIsFound() {
    final List<HostInfo> serverInfos = new ArrayList<>();
    final CountDownLatch waitForResponseLatch = new CountDownLatch(1);

    RegistrationServer registrationServer = new RegistrationServer(messagesCreator);
    registrationServer.startRegistrationServerAsync();

    LookingForRegistrationServersClient client = new LookingForRegistrationServersClient(messagesCreator);
    client.findRegistrationServersAsync(new RegistrationRequestListener() {
      @Override
      public void openRegistrationServerFound(HostInfo serverInfo) {
        serverInfos.add(serverInfo);
        waitForResponseLatch.countDown();
      }
    });

    try { waitForResponseLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, serverInfos.size());

    registrationServer.closeRegistrationServer();
    client.stopSearchingForRegistrationServers();
  }

  @Test
  public void registrationServerIsOpen_ThreeClientsLikeToRegister_AllRequestAreReceived() {
    final List<HostInfo> receivedRegistrationRequest = new ArrayList<>();
    final CountDownLatch waitForResponseLatch = new CountDownLatch(1);

    RegistrationServer registrationServer = new RegistrationServer(messagesCreator);
    registrationServer.startRegistrationServerAsync(new RegistrationServerListener() {
      @Override
      public void registrationRequestReceived(HostInfo info) {
        receivedRegistrationRequest.add(info);
        if(receivedRegistrationRequest.size() == 3)
          waitForResponseLatch.countDown();
      }
    });

    LookingForRegistrationServersClient client1 = new LookingForRegistrationServersClient(messagesCreator);
    client1.findRegistrationServersAsync(null);
    LookingForRegistrationServersClient client2 = new LookingForRegistrationServersClient(messagesCreator);
    client2.findRegistrationServersAsync(null);
    LookingForRegistrationServersClient client3 = new LookingForRegistrationServersClient(messagesCreator);
    client3.findRegistrationServersAsync(null);

    try { waitForResponseLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, receivedRegistrationRequest.size());

    registrationServer.closeRegistrationServer();
    client1.stopSearchingForRegistrationServers();
    client2.stopSearchingForRegistrationServers();
    client3.stopSearchingForRegistrationServers();
  }
}
