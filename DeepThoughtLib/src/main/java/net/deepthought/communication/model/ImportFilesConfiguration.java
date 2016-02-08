package net.deepthought.communication.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 04/10/15.
 */
public class ImportFilesConfiguration {

  private final static Logger log = LoggerFactory.getLogger(ImportFilesConfiguration.class);


  protected ImportFilesSource source = ImportFilesSource.AskUser;

  protected String requestedFileTypes = null;


  public ImportFilesConfiguration(ImportFilesSource source) {
    this.source = source;
  }

  public ImportFilesConfiguration(ImportFilesSource source, String requestedFileTypes) {
    this(source);
    this.requestedFileTypes = requestedFileTypes;
  }


  public ImportFilesSource getSource() {
    return source;
  }

  public String getRequestedFileTypes() {
    return requestedFileTypes;
  }

}
