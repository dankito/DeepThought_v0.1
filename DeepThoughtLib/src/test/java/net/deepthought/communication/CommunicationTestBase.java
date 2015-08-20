package net.deepthought.communication;

import net.deepthought.Application;
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

  protected IDeepThoughtsConnector connector;

  protected Communicator communicator;


  @Before
  public void setup() {
    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager()));

    connector = Application.getDeepThoughtsConnector();
    communicator = connector.getCommunicator();
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }

}
