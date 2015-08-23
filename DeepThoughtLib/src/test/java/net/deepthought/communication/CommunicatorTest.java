package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.listener.AskForDeviceRegistrationListener;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 20/08/15.
 */
public class CommunicatorTest extends CommunicationTestBase {


  @Test
  public void askForDeviceRegistration_ListenerMethodAllowDeviceToRegisterGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);

    connector.openUserDeviceRegistrationServer(new UserDeviceRegistrationRequestListener() {
      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
        methodCalled.set(true);
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), null);

    Assert.assertTrue(methodCalled.get());
  }

  @Test
  public void askForDeviceRegistration_ServerResponseIsReceived() {
    final List<AskForDeviceRegistrationResponseMessage> responses = new ArrayList<>();

    connector.openUserDeviceRegistrationServer(new UserDeviceRegistrationRequestListener() {
      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
//        return AllowDeviceToRegisterResult.createDenyRegistrationResult();
        // TODO:
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), new net.deepthought.communication.listener.AskForDeviceRegistrationListener() {
      @Override
      public void serverResponded(AskForDeviceRegistrationResponseMessage response) {
        responses.add(response);
      }
    });

    Assert.assertEquals(1, responses.size());
  }

  @Test
  public void askForDeviceRegistration_RegistrationIsProhibitedByServer_RegistrationDeniedResponseIsReceived() {
    final List<AskForDeviceRegistrationResponseMessage> responses = new ArrayList<>();

    connector.openUserDeviceRegistrationServer(new UserDeviceRegistrationRequestListener() {
      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
//        return AllowDeviceToRegisterResult.createDenyRegistrationResult();
        // TODO:
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), new net.deepthought.communication.listener.AskForDeviceRegistrationListener() {
      @Override
      public void serverResponded(AskForDeviceRegistrationResponseMessage response) {
        responses.add(response);
      }
    });

    Assert.assertFalse(responses.get(0).allowsRegistration());
  }

  @Test
  public void askForDeviceRegistration_ServerAllowsRegistration_RegistrationAllowedResponseIsReceived() {
    final List<AskForDeviceRegistrationResponseMessage> responses = new ArrayList<>();

    connector.openUserDeviceRegistrationServer(new UserDeviceRegistrationRequestListener() {
      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
//        return AllowDeviceToRegisterResult.createAllowRegistrationResult(true);
        // TODO:
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), new AskForDeviceRegistrationListener() {
      @Override
      public void serverResponded(AskForDeviceRegistrationResponseMessage response) {
        responses.add(response);
      }
    });

    Assert.assertTrue(responses.get(0).allowsRegistration());
  }


  protected HostInfo createLocalHostServerInfo() {
    HostInfo hostInfo = HostInfo.fromUserAndDevice(Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
    hostInfo.setIpAddress(NetworkHelper.getIPAddressString(true));
    hostInfo.setPort(Application.getDeepThoughtsConnector().getMessageReceiverPort());

    return hostInfo;
  }


  protected void sendMessagesToCommunicator(int port, String... messages) throws IOException, InterruptedException {
    Runtime rt = Runtime.getRuntime();
    Process pr = rt.exec("telnet localhost " + port);
    Thread.sleep(50); // wait till connection has been established

    PrintStream streamWriter = new PrintStream(pr.getOutputStream());
    for(String message : messages) {
      streamWriter.print(message);
      streamWriter.flush();
    }

    streamWriter.close();
    pr.destroy();
  }
}
