package net.deepthought.controls.html;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 26/09/15.
 */
public class AndroidHtmlEditor extends WebView implements IJavaScriptBridge, IJavaScriptExecutor {

  private final static Logger log = LoggerFactory.getLogger(AndroidHtmlEditor.class);


  protected Activity activity;

  protected HtmlEditor htmlEditor = null;

  protected List<IJavaScriptBridge> javaScriptBridgesToCall = new ArrayList<>();


  public AndroidHtmlEditor(Activity context, IHtmlEditorListener listener) {
    super(context);
    this.activity = context;
    setupHtmlEditor(listener);
  }

  public AndroidHtmlEditor(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AndroidHtmlEditor(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public AndroidHtmlEditor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
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
        htmlEditor.editorLoaded();
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



  public String getHtml() {
    return htmlEditor.getHtml();
  }

  public void setHtml(String html) {
    setHtml(html, false);
  }

  public void setHtml(String html, boolean resetUndoStack) {
    htmlEditor.setHtml(html, resetUndoStack);
  }

  public void setListener(IHtmlEditorListener listener) {
    htmlEditor.setListener(listener);
  }

  public void reInitHtmlEditor(IHtmlEditorListener listener) {
    htmlEditor.reInitHtmlEditor(listener);
  }


  @Override
  public void executeScript(String javaScript) {
    executeScript(javaScript, null);
  }

  @Override
  public void executeScript(final String javaScript, final ExecuteJavaScriptResultListener listener) {
    if(true) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          executeScriptOnUiThread(javaScript, listener);
        }
      });
    }
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

  @JavascriptInterface
  public void loaded() {
    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      bridge.loaded();
  }

  @JavascriptInterface
  public void htmlChanged(String newHtmlCode) {
    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      bridge.htmlChanged(newHtmlCode);
  }

}
