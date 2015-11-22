package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.TestApplicationConfiguration;
import net.deepthought.communication.model.ConnectedDevice;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;

/**
 * Created by ganymed on 20/08/15.
 */
public class CommunicationTestBase {

  protected final static String TestDeviceId = "Cuddle";

  protected final static String TestIpAddress = "0.0.0.0";

  protected final static int CommunicatorPort = 54321;


  protected ConnectorMessagesCreator messagesCreator = new ConnectorMessagesCreator();

  protected IDeepThoughtsConnector connector;

  protected Communicator communicator;

  protected ConnectedDevice localHost = new ConnectedDevice(TestDeviceId, TestIpAddress, CommunicatorPort);


  @Before
  public void setup() throws IOException {
    Application.instantiate(new TestApplicationConfiguration());

    connector = Application.getDeepThoughtsConnector();
    communicator = connector.getCommunicator();
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }

}
