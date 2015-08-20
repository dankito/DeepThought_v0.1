package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.AllowDeviceToRegisterResult;
import net.deepthought.communication.model.ConnectedPeer;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.TestDependencyResolver;

import org.junit.After;
import org.junit.Before;

/**
 * Created by ganymed on 20/08/15.
 */
public class CommunicationTestBase {


  protected ConnectorMessagesCreator messagesCreator = new ConnectorMessagesCreator();

  protected Communicator communicator;

  protected DeepThoughtsConnectorListener testMethodConnectorListener = null;


  @Before
  public void setup() {
    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager(), connectorListener));

    communicator = Application.getDeepThoughtsConnector().getCommunicator();
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }


  protected DeepThoughtsConnectorListener connectorListener = new DeepThoughtsConnectorListener() {
    @Override
    public AllowDeviceToRegisterResult registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
      if(testMethodConnectorListener != null)
        return testMethodConnectorListener.registerDeviceRequestRetrieved(request);
      return AllowDeviceToRegisterResult.createDenyRegistrationResult();
    }

    @Override
    public void registeredDeviceConnected(ConnectedPeer peer) {
      if(testMethodConnectorListener != null)
        testMethodConnectorListener.registeredDeviceConnected(peer);
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedPeer peer) {
      if(testMethodConnectorListener != null)
        testMethodConnectorListener.registeredDeviceDisconnected(peer);
    }
  };

}
