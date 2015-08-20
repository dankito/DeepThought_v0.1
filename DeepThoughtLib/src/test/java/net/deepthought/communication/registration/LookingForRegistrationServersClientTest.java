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
public class LookingForRegistrationServersClientTest extends CommunicationTestBase {


  @Test
  public void registrationServerIsOpen_ServerGetsFound() {
    final List<HostInfo> receivedRegistrationRequest = new ArrayList<>();
    final CountDownLatch waitForRequestLatch = new CountDownLatch(1);

    RegistrationServer registrationServer = new RegistrationServer(messagesCreator);
    registrationServer.startRegistrationServerAsync(new RegistrationServerListener() {
      @Override
      public void registrationRequestReceived(HostInfo info) {
        receivedRegistrationRequest.add(info);
        waitForRequestLatch.countDown();
      }
    });

    LookingForRegistrationServersClient client = new LookingForRegistrationServersClient(messagesCreator);
    client.findRegistrationServersAsync(null);

    try { waitForRequestLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, receivedRegistrationRequest.size());

    registrationServer.closeRegistrationServer();
    client.stopSearchingForRegistrationServers();
  }

  @Test
  public void registrationServerIsOpen_RequestGetsReplied() {
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
}
