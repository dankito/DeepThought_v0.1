package net.deepthought.data.persistence.serializer;

/**
 * Created by cdankl on 09.07.2014.
 */
public class SerializationResult {

  protected boolean successful = false;

  protected Exception error = null;

  protected String serializationResult = null;

  public SerializationResult(Exception error) {
    this.successful = false;
    this.error = error;
  }

  public SerializationResult(String serializationResult) {
    this.successful = serializationResult != null;
    this.serializationResult = serializationResult;
  }

  public boolean successful() {
    return successful;
  }

  public Exception getError() {
    return error;
  }

  public String getSerializationResult() {
    return serializationResult;
  }


  @Override
  public String toString() {
    String description = "Successful? " + successful;

    if(error != null)
      description += "; Error: " + error;

    return description;
  }

}
