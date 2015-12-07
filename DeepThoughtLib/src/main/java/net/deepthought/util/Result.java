package net.deepthought.util;

/**
 * Created by ganymed on 07/12/15.
 */
public class Result {

  protected boolean successful = false;

  protected Exception error;


  public Result(boolean successful) {
    this.successful = successful;
  }

  public Result(Exception error) {
    this(false);
    this.error = error;
  }


  public boolean isSuccessful() {
    return successful;
  }

  public Exception getError() {
    return error;
  }


  @Override
  public String toString() {
    String description = "Successful? " + successful;

    if(error != null) {
      description += ": " + error;
    }

    return description;
  }

}
