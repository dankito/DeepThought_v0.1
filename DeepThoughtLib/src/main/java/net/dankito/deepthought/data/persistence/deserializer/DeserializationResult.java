package net.dankito.deepthought.data.persistence.deserializer;

/**
 * Created by cdankl on 09.07.2014.
 */
public class DeserializationResult<T> {

  protected boolean successful = false;

  protected Exception error = null;

  protected T result = null;

  public DeserializationResult(Exception error) {
    this.successful = false;
    this.error = error;
  }

  public DeserializationResult(T result) {
    this.successful = result != null;
    this.result = result;
  }

  public boolean successful() {
    return successful;
  }

  public Exception getError() {
    return error;
  }

  public T getResult() {
    return result;
  }


  @Override
  public String toString() {
    String description = "Successful? " + successful;

    if(error != null)
      description += "; Error: " + error;

    return description;
  }

}
