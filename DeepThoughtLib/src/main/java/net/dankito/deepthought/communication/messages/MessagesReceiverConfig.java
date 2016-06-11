package net.dankito.deepthought.communication.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 20/11/15.
 */
public class MessagesReceiverConfig {

  protected int port;

  protected String uriPathStart;

  protected AsynchronousResponseListenerManager listenerManager;

  protected List<WebMethodConfig> allowedMethods = new ArrayList<>();

  protected List<String> allowedMethodNames = new ArrayList<>();


  protected MessagesReceiverConfig(int port, String uriPathStart, AsynchronousResponseListenerManager listenerManager) {
    this.port = port;
    this.uriPathStart = uriPathStart;
    this.listenerManager = listenerManager;
  }

  public MessagesReceiverConfig(int port, String uriPathStart, AsynchronousResponseListenerManager listenerManager, List<WebMethodConfig> allowedMethods) {
    this(port, uriPathStart, listenerManager);
    setAllowedMethods(allowedMethods);
  }


  public int getPort() {
    return port;
  }

  public String getUriPathStart() {
    return uriPathStart;
  }

  public AsynchronousResponseListenerManager getListenerManager() {
    return listenerManager;
  }

  public List<WebMethodConfig> getAllowedMethods() {
    return allowedMethods;
  }

  protected void setAllowedMethods(List<WebMethodConfig> allowedMethods) {
    this.allowedMethods = allowedMethods;

    this.allowedMethodNames = extractAllowedMethodNames(allowedMethods);
  }

  protected List<String> extractAllowedMethodNames(List<WebMethodConfig> allowedMethods) {
    List<String> allowedMethodNames = new ArrayList<>();

    for(WebMethodConfig webMethod : allowedMethods) {
      allowedMethodNames.add(webMethod.getMethodName());
    }

    return allowedMethodNames;
  }

  public boolean isAllowedMethodName(String webMethodName) {
    return allowedMethodNames.contains(webMethodName);
  }

  public Class getRequestClassForMethod(String methodName) {
    for(WebMethodConfig webMethod : allowedMethods) {
      if(webMethod.getMethodName().equals(methodName)) {
        return webMethod.getRequestClass();
      }
    }

    return null;
  }

  public List<MultipartPart> getMultipartPartsConfigForMethod(String methodName) {
    for(WebMethodConfig webMethod : allowedMethods) {
      if(webMethod.getMethodName().equals(methodName)) {
        return webMethod.getMultipartPartsConfig();
      }
    }

    return null;
  }

}
