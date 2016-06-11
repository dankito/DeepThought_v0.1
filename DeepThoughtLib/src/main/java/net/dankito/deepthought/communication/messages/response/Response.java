package net.dankito.deepthought.communication.messages.response;

/**
 * Created by ganymed on 20/08/15.
 */
public class Response {

  public final static Response OK = new Response(ResponseCode.Ok);

  public final static Response Denied = new Response(ResponseCode.Denied);

  public final static Response CouldNotDeserializeRequest = new Response(ResponseCode.Error);

  public final static Response CouldNotHandleRequest = new Response(ResponseCode.Error);


  protected ResponseCode responseCode;

  protected String message = null;


  public Response() {

  }

  public Response(ResponseCode responseCode) {
    this.responseCode = responseCode;
  }

  public Response(ResponseCode responseCode, String message) {
    this(responseCode);
    this.message = message;
  }


  public ResponseCode getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(ResponseCode responseCode) {
    this.responseCode = responseCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public String toString() {
    String description = "" + responseCode;

    if(message != null)
      description += " (" + message + ")";

    return description;
  }

}
