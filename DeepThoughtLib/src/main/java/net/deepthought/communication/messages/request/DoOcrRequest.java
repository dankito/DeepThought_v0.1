package net.deepthought.communication.messages.request;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.messages.MultipartPart;
import net.deepthought.communication.messages.MultipartType;
import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.util.file.FileUtils;

import java.io.File;

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

    // send Image to be recognized in an extra Multipart
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
        String filename = (String)part.getData();
        filename = maySetOriginalFilename(configuration, filename);

        configuration.setImageUri(filename);
      }
    }

    return super.addPart(part);
  }

  // TODO: remove again
  protected String maySetOriginalFilename(DoOcrConfiguration configuration, String filename) {
    String adjustedFilename = filename;

    if(configuration.getImageUri() != null) {
      try {
        String originalFilename = FileUtils.getFileNameIncludingExtension(configuration.getImageUri());
        File currentFile = new File(filename);
        File adjustedFile = new File(currentFile.getParentFile(), originalFilename);

        FileUtils.moveFile(currentFile, adjustedFile);
        adjustedFilename = adjustedFile.toURI().toString();
      } catch(Exception ex) { }
    }

    return adjustedFilename;
  }

}
