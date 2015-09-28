package net.deepthought.controls.html;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.deepthought.AndroidHelper;
import net.deepthought.controls.ICleanUp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 26/09/15.
 */
public class AndroidHtmlEditor extends WebView implements IJavaScriptBridge, IJavaScriptExecutor, ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(AndroidHtmlEditor.class);


  protected Activity activity;

  protected HtmlEditor htmlEditor = null;

  protected List<IJavaScriptBridge> javaScriptBridgesToCall = new ArrayList<>();


  public AndroidHtmlEditor(Activity context, IHtmlEditorListener listener) {
    super(context);
    this.activity = context;
    setupHtmlEditor(listener);
  }


  protected void setupHtmlEditor(IHtmlEditorListener listener) {
    this.getSettings().setJavaScriptEnabled(true);
//    setInitialScale(95);
    getSettings().setTextZoom(70);

    htmlEditor = new HtmlEditor(this, listener);

    setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        htmlEditor.webControlLoaded();
        executeScript("resizeEditorToFitWindow()");
      }

      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        log.error("An error occurred in WebView when calling Url " + failingUrl + ": " + description);
        super.onReceivedError(view, errorCode, description, failingUrl);
      }
    });

    setWebChromeClient(new WebChromeClient() {
      @Override
      public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//        return super.onJsAlert(view, url, message, result);
        result.confirm(); // do not show any JavaScript alert to user
        return true;
      }
    });

    addJavascriptInterface(this, "app"); // has to be set already here otherwise loaded event will not be recognized
    loadUrl(htmlEditor.getHtmlEditorPath());
  }


  public void insertHtml(String html) {
    htmlEditor.insertHtml(html);
  }


  public String getHtml() {
    return htmlEditor.getHtml();
  }

  public void setHtml(String html) {
    setHtml(html, false);
  }

  public void setHtml(String html, boolean resetUndoStack) {
    htmlEditor.setHtml(html, resetUndoStack);
  }

  public void reInitHtmlEditor(Activity context, IHtmlEditorListener listener) {
    this.activity = context;
    htmlEditor.reInitHtmlEditor(listener);
  }

  public void resetInstanceVariables() {
    this.activity = null;
    htmlEditor.setListener(null);
  }


  @Override
  public void executeScript(String javaScript) {
    executeScript(javaScript, null);
  }

  @Override
  public void executeScript(final String javaScript, final ExecuteJavaScriptResultListener listener) {
    if(AndroidHelper.isRunningOnUiThread())
      executeScriptOnUiThread(javaScript, listener);
    else if(activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          executeScriptOnUiThread(javaScript, listener);
        }
      });
    }
    else
      log.error("Trying to execute Script '" + javaScript + "', but activity is null");
  }

  protected void executeScriptOnUiThread(final String javaScript, final ExecuteJavaScriptResultListener listener) {
    try {
//      loadUrl("javascript:" + javaScript);

      evaluateJavascript(javaScript, new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
          if(listener != null)
            listener.scriptExecuted(value);
        }
      });
    } catch(Exception ex) {
      log.error("Could not evaluate JavaScript " + javaScript, ex);
    }
  }

  @Override
  public void setJavaScriptMember(String name, IJavaScriptBridge member) {
    // since Android Api 17 all methods callable from JavaScript must be annotated with @JavascriptInterface, an Android specific annotation
    // -> HtmlEditor cannot know this annotation, so we save the member instance, let the method call on ourself and then pass the method call on to the member
    javaScriptBridgesToCall.add(member);
  }

  @Override
  @JavascriptInterface
  public void ckEditorLoaded() {
    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      bridge.ckEditorLoaded();
  }

  @Override
  @JavascriptInterface
  public void htmlChanged(String newHtmlCode) {
    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      bridge.htmlChanged(newHtmlCode);
  }

  @Override
  @JavascriptInterface
  public boolean elementClicked(String element, int button, int clickX, int clickY) {
    boolean result = true;

    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      result &= bridge.elementClicked(element, button, clickX, clickY);

    return result;
  }

  @Override
  @JavascriptInterface
  public boolean elementDoubleClicked(String element) {
    boolean result = true;

    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      result &= bridge.elementDoubleClicked(element);

    return result;
  }

  @Override
  @JavascriptInterface
  public boolean beforeCommandExecution(String commandName) {
    boolean result = true;

    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      result &= bridge.beforeCommandExecution(commandName);

    return result;
  }

  @Override
  public void cleanUp() {
    htmlEditor.cleanUp();
  }
}
