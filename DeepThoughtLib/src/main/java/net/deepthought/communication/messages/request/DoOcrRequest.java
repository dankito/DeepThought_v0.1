package net.deepthought.communication.messages.request;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.messages.MultipartPart;
import net.deepthought.communication.messages.MultipartType;
import net.deepthought.communication.model.DoOcrConfiguration;

/**
 * Created by ganymed on 23/08/15.
 */
public class DoOcrRequest extends MultipartRequest {

  protected DoOcrConfiguration configuration;


  public DoOcrRequest() {
    // for Reflection
  }

  public DoOcrRequest(String address, int port, DoOcrConfiguration configuration) {
    super(address, port);
    setConfiguration(configuration);
  }

  public DoOcrRequest(int messageId, String address, int port, DoOcrConfiguration configuration) {
    super(messageId, address, port);
    setConfiguration(configuration);
  }

  protected void setConfiguration(DoOcrConfiguration configuration) {
    this.configuration = configuration;

    parts.add(new MultipartPart<DoOcrConfiguration>(ConnectorMessagesCreator.DoOcrMultipartKeyConfiguration, MultipartType.Text, configuration));

    // send CaptureImageResult's binary image data in an extra Multipart
    if(configuration.hasImageToRecognize()) {
      parts.add(new MultipartPart<byte[]>(ConnectorMessagesCreator.DoOcrMultipartKeyImage, MultipartType.Binary, configuration.getAndResetImageToRecognize()));
    }
  }


  public DoOcrConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public boolean addPart(MultipartPart part) {
    if(ConnectorMessagesCreator.DoOcrMultipartKeyConfiguration.equals(part.getPartName()) &&
        part.getData() instanceof DoOcrConfiguration) {
      this.configuration = (DoOcrConfiguration)part.getData();
    }
    else if(ConnectorMessagesCreator.DoOcrMultipartKeyImage.equals(part.getPartName())) {
      if(part.getData() instanceof String && configuration != null) {
        configuration.setImageUri((String)part.getData());
      }
    }

    return super.addPart(part);
  }

}
