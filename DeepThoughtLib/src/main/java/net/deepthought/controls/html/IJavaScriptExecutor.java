package net.deepthought.controls.html;

/**
 * Created by ganymed on 28/08/15.
 */
public interface IJavaScriptExecutor {

  void executeScript(String javaScript);

  void executeScript(String javaScript, ExecuteJavaScriptResultListener listener);

  void setJavaScriptMember(String name, IJavaScriptBridge member);

}
