package net.deepthought.communication.connected_device;

import net.deepthought.communication.CommunicationTestBase;
import net.deepthought.communication.DeepThoughtsConnector;
import net.deepthought.communication.listener.RegisteredDeviceConnectedListener;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.RegisteredDevicesManager;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 22/08/15.
 */
public class RegisteredDevicesSearcherTest extends CommunicationTestBase {

  @Test
  public void sendPacketToServer_PacketGetsReceived() {
    final List<DatagramPacket> receivedPackets = new ArrayList<>();
    final CountDownLatch waitForPacketsLatch = new CountDownLatch(1);

    RegisteredDevicesSearcher searcher = new RegisteredDevicesSearcher(messagesCreator) {
      @Override
      protected void serverReceivedPacket(byte[] buffer, DatagramPacket packet) {
        receivedPackets.add(packet);
        waitForPacketsLatch.countDown();
      }
    };

    searcher.startSearchingAsync(null);

    try { waitForPacketsLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    searcher.stopSearching();

    Assert.assertEquals(1, receivedPackets.size());
  }

  @Test
  public void sendPacketToServer_ClientIsNotRegistered_PacketDoesNotGetReplied() {
    final List<DatagramPacket> packetsRespondedTo = new ArrayList<>();
    final CountDownLatch waitForResponseCreationLatch = new CountDownLatch(1);

    RegisteredDevicesSearcher searcher = new RegisteredDevicesSearcher(messagesCreator) {
      @Override
      protected void respondToSearchingForRegisteredDevicesMessage(DatagramPacket requestPacket) {
        packetsRespondedTo.add(requestPacket);
        waitForResponseCreationLatch.countDown();
      }
    };

    searcher.startSearchingAsync(null);

    try { waitForResponseCreationLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    searcher.stopSearching();

    Assert.assertEquals(0, packetsRespondedTo.size());
  }

  @Test
  public void sendPacketToServer_ClientIsRegistered_PacketGetsReplied() {
    mockDeviceIsRegistered();

    final List<DatagramPacket> packetsRespondedTo = new ArrayList<>();
    final CountDownLatch waitForResponseCreationLatch = new CountDownLatch(1);

    RegisteredDevicesSearcher searcher = new RegisteredDevicesSearcher(messagesCreator) {
      @Override
      protected void respondToSearchingForRegisteredDevicesMessage(DatagramPacket requestPacket) {
        packetsRespondedTo.add(requestPacket);
        waitForResponseCreationLatch.countDown();
      }
    };

    searcher.startSearchingAsync(null);

    try { waitForResponseCreationLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    searcher.stopSearching();

    Assert.assertEquals(1, packetsRespondedTo.size());
  }


  @Test
  public void sendPacketToServer_PacketGetsReplied() {
    mockDeviceIsRegistered();

    final List<DatagramPacket> receivedPackets = new ArrayList<>();
    final CountDownLatch waitForPacketsLatch = new CountDownLatch(1);

    RegisteredDevicesSearcher searcher = new RegisteredDevicesSearcher(messagesCreator) {
      @Override
      protected void clientReceivedResponseFromServer(RegisteredDeviceConnectedListener listener, byte[] buffer, DatagramPacket packet) {
        receivedPackets.add(packet);
        waitForPacketsLatch.countDown();
      }
    };

    searcher.startSearchingAsync(null);

    try { waitForPacketsLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    searcher.stopSearching();

    Assert.assertEquals(1, receivedPackets.size());
  }

  @Test
  public void sendPacketToServer_ClientIsNotRegistered_ClientReceivesNoResponses() {
    final List<DatagramPacket> responsesReceived = new ArrayList<>();
    final CountDownLatch waitForResponseCreationLatch = new CountDownLatch(1);

    RegisteredDevicesSearcher searcher = new RegisteredDevicesSearcher(messagesCreator) {
      @Override
      protected void clientReceivedResponseFromServer(RegisteredDeviceConnectedListener listener, byte[] buffer, DatagramPacket packet) {
        responsesReceived.add(packet);
        waitForResponseCreationLatch.countDown();
      }
    };

    searcher.startSearchingAsync(null);

    try { waitForResponseCreationLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    searcher.stopSearching();

    Assert.assertEquals(0, responsesReceived.size());
  }

  @Test
  public void sendPacketToServer_ClientIsRegistered_ServerConnectsToClient() {
    mockDeviceIsRegistered();

    final List<ConnectedDevice> connectedDevices = new ArrayList<>();
    final CountDownLatch waitForResponseCreationLatch = new CountDownLatch(1);

    RegisteredDevicesSearcher searcher = new RegisteredDevicesSearcher(messagesCreator);

    searcher.startSearchingAsync(new RegisteredDeviceConnectedListener() {
      @Override
      public void registeredDeviceConnected(ConnectedDevice device) {
        connectedDevices.add(device);
        waitForResponseCreationLatch.countDown();
      }
    });

    try { waitForResponseCreationLatch.await(1, TimeUnit.SECONDS); } catch(Exception ex) { }

    searcher.stopSearching();

    Assert.assertEquals(1, connectedDevices.size());
  }

  protected void mockDeviceIsRegistered() {
    RegisteredDevicesManager registeredDevicesManager = Mockito.mock(RegisteredDevicesManager.class);
    ((DeepThoughtsConnector)connector).setRegisteredDevicesManager(registeredDevicesManager);
    Mockito.when(registeredDevicesManager.isDeviceRegistered(Mockito.any(HostInfo.class))).thenReturn(true);
    Mockito.when(registeredDevicesManager.isDeviceRegistered(Mockito.any(ConnectedDevice.class))).thenReturn(true);
  }
}
