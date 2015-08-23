package net.deepthought.communication.messages;

/**
 * Created by ganymed on 20/08/15.
 */
public class Response {

  public final static Response OK = new Response(ResponseValue.Ok);

  public final static Response Denied = new Response(ResponseValue.Denied);


  protected ResponseValue responseValue;

  protected String message = null;


  public Response() {

  }

  public Response(ResponseValue responseValue) {
    this.responseValue = responseValue;
  }

  public Response(ResponseValue responseValue, String message) {
    this(responseValue);
    this.message = message;
  }


  public ResponseValue getResponseValue() {
    return responseValue;
  }

  public void setResponseValue(ResponseValue responseValue) {
    this.responseValue = responseValue;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public String toString() {
    String description = "" + responseValue;

    if(message != null)
      description += " (" + message + ")";

    return description;
  }

}
