package net.deepthought.communication.messages;

import net.deepthought.communication.messages.request.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 20/11/15.
 */
public class WebMethodConfig {

  protected String methodName;

  protected Class<? extends Request> requestClass;

  protected List<MultipartPart> multipartPartsConfig = new ArrayList<>();


  public WebMethodConfig(String methodName, Class<? extends Request> requestClass) {
    this.methodName = methodName;
    this.requestClass = requestClass;
  }

  public WebMethodConfig(String methodName, Class<? extends Request> requestClass, List<MultipartPart> multipartPartsConfig) {
    this(methodName, requestClass);
    this.multipartPartsConfig = multipartPartsConfig;
  }


  public String getMethodName() {
    return methodName;
  }

  public Class<? extends Request> getRequestClass() {
    return requestClass;
  }

  public List<MultipartPart> getMultipartPartsConfig() {
    return multipartPartsConfig;
  }

}
