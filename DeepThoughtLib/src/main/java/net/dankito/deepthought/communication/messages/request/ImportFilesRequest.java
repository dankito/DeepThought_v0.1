package net.dankito.deepthought.communication.messages.request;

import net.dankito.deepthought.communication.messages.MultipartPart;
import net.dankito.deepthought.communication.model.ImportFilesConfiguration;
import net.dankito.deepthought.communication.ConnectorMessagesCreator;
import net.dankito.deepthought.communication.messages.MultipartType;

/**
 * Created by ganymed on 23/08/15.
 */
public class ImportFilesRequest extends MultipartRequest {

  protected ImportFilesConfiguration configuration;


  public ImportFilesRequest() {
    // for Reflection
  }

  public ImportFilesRequest(String address, int port, ImportFilesConfiguration configuration) {
    super(address, port);
    setConfiguration(configuration);
  }

  public ImportFilesRequest(int messageId, String address, int port, ImportFilesConfiguration configuration) {
    super(messageId, address, port);
    setConfiguration(configuration);
  }

  protected void setConfiguration(ImportFilesConfiguration configuration) {
    this.configuration = configuration;

    parts.add(new MultipartPart<ImportFilesConfiguration>(ConnectorMessagesCreator.ImportFilesMultipartKeyConfiguration, MultipartType.Text, configuration));
  }


  public ImportFilesConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public boolean addPart(MultipartPart part) {
    if(ConnectorMessagesCreator.ImportFilesMultipartKeyConfiguration.equals(part.getPartName()) &&
        part.getData() instanceof ImportFilesConfiguration) {
      this.configuration = (ImportFilesConfiguration)part.getData();
    }

    return super.addPart(part);
  }

}
