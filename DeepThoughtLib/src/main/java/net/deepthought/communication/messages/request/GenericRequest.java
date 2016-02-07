package net.deepthought.communication.messages.request;

/**
 * Created by ganymed on 23/08/15.
 */
public class GenericRequest<T> extends Request {

  protected T requestBody;

  public GenericRequest(T requestBody) {
    this.requestBody = requestBody;
  }

  public T getRequestBody() {
    return requestBody;
  }

}
