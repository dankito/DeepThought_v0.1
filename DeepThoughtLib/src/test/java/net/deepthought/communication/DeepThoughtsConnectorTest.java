package net.deepthought.communication;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by ganymed on 19/08/15.
 */
public class DeepThoughtsConnectorTest {


  @Test
  public void startTwoDeepThoughtsConnectors_BothMessageHandlersStartSuccessfully() {
    DeepThoughtsConnector connector1 = new DeepThoughtsConnector(null);
    connector1.runAsync();

    DeepThoughtsConnector connector2 = new DeepThoughtsConnector(null);
    connector2.runAsync();

    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    // now assure that both have started successfully even so by default the use the same port to listen on
    Assert.assertTrue(connector1.isStarted());
    Assert.assertTrue(connector2.isStarted());
    Assert.assertNotEquals(connector1.getMessageReceiverPort(), connector2.getMessageReceiverPort());
  }


  @Test
  public void registeredDevicesExists_NotConnectedToAllRegisteredDevices_RegisteredDevicesSearcherGetsStarted() {
    Assert.fail();
  }

  @Test
  public void registeredDevicesExists_AlreadyConnectedToAllRegisteredDevices_RegisteredDevicesSearcherWontBeStarted() {
    Assert.fail();
  }

  @Test
  public void connectsToARegisteredDevice_IsNowConnectedToAllRegisteredDevices_RegisteredDevicesSearcherGetsStopped() {
    Assert.fail();
  }

  @Test
  public void disconnectsFromARegisteredDevice_IsNowNotConnectedAnymoreToAllRegisteredDevices_RegisteredDevicesSearcherGetsStarted() {
    Assert.fail();
  }


  protected void sendMessagesToDeepThoughtsConnector(int port, String... messages) throws IOException, InterruptedException {
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
