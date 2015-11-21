package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.TestApplicationConfiguration;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;

/**
 * Created by ganymed on 20/08/15.
 */
public class CommunicationTestBase {


  protected ConnectorMessagesCreator messagesCreator = new ConnectorMessagesCreator();

  protected IDeepThoughtsConnector connector;

  protected Communicator communicator;


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
