package net.dankito.deepthought.controls.html;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.dankito.deepthought.AndroidHelper;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.util.OsHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 26/09/15.
 */
public class AndroidHtmlEditor extends WebView implements IJavaScriptBridge, IJavaScriptExecutor, ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(AndroidHtmlEditor.class);


  protected Activity activity;

  protected HtmlEditor htmlEditor = null;

  protected List<IJavaScriptBridge> javaScriptBridgesToCall = new ArrayList<>();

  public AndroidHtmlEditor(Context context) {
    super(context);
    setupHtmlEditor(null);
  }

  public AndroidHtmlEditor(Context context, AttributeSet attrs) {
    super(context, attrs);
    setupHtmlEditor(null);
  }

  public AndroidHtmlEditor(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setupHtmlEditor(null);
  }

  public AndroidHtmlEditor(Activity context, IHtmlEditorListener listener) {
    super(context);
    this.activity = context;
    setupHtmlEditor(listener);
  }


  protected void setupHtmlEditor(IHtmlEditorListener listener) {
    this.getSettings().setJavaScriptEnabled(true);

    if(OsHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(14))
      getSettings().setTextZoom(85);
    else
      setInitialScale(95);

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

    // crashes in Emulator, and only in the Emulator, for Android 2.3
    if(Application.getPlatformConfiguration().isRunningInEmulator() == false || (OsHelper.isRunningOnAndroidApiLevel(9) == false && OsHelper.isRunningOnAndroidApiLevel(10) == false)) {
      addJavascriptInterface(this, "app"); // has to be set already here otherwise loaded event will not be recognized
      if(OsHelper.isRunningOnAndroidAtLeastOfApiLevel(19) == false) { // before Android 19 there was no way to get automatically informed of JavaScript results -> use this as workaround
        addJavascriptInterface(this, "android");
      }
    }
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
    htmlEditor.setListener(null);
    htmlEditor.setHtml("", true);
    htmlEditor.releaseData();

    this.activity = null;
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
      if(OsHelper.isRunningOnAndroidAtLeastOfApiLevel(19)) {
        executeScriptOnUiThreadForAndroid19AndAbove(javaScript, listener);
      }
      else {
        executeScriptOnUiThreadForAndroidPre19(javaScript, listener);
      }

    } catch(Exception ex) {
      log.error("Could not evaluate JavaScript " + javaScript, ex);
    }
  }

  protected void executeScriptOnUiThreadForAndroid19AndAbove(String javaScript, final ExecuteJavaScriptResultListener listener) {
    // evaluateJavascript() only works on API 19 and newer!
    evaluateJavascript(javaScript, new ValueCallback<String>() {
      @Override
      public void onReceiveValue(String value) {
        if (listener != null)
          listener.scriptExecuted(value);
      }
    });
  }

  protected void executeScriptOnUiThreadForAndroidPre19(String javaScript, ExecuteJavaScriptResultListener listener) {
    if(listener == null) { // no response is needed
      loadUrl("javascript:" + javaScript);
    }
    else {
      // as via loadUrl() we cannot execute JavaScript and wait for its result ->
      // for each JavaScript Method create an extra responseToXyz method (like responseToGetHtml() below) in ckeditor_control.js, with it then call to tell us result
      if(javaScript == HtmlEditor.JavaScriptCommandGetHtml) {
        listenerForGetHtml = listener;
        waitForGetHtmlResponseLatch = new CountDownLatch(1);

        loadUrl("javascript:androidGetHtml()");

        // i really hate writing this code as method runs on UI thread and in worst case UI thread gets then blocked
        try { waitForGetHtmlResponseLatch.await(500, TimeUnit.MILLISECONDS); } catch(Exception ex) { }
        listenerForGetHtml = null;
      }
      else {
        log.error("An unknown JavaScript command with result has been executed, add handling for it in AndroidHtmlEditor");
      }
    }
  }


  /*  Response handling as Android pre 19 doesn't support getting result of executed JavaScript    */

  protected ExecuteJavaScriptResultListener listenerForGetHtml = null;
  protected CountDownLatch waitForGetHtmlResponseLatch = null;

  @JavascriptInterface
  public void responseToGetHtml(String htmlData) {
    if(listenerForGetHtml != null) {
      listenerForGetHtml.scriptExecuted(htmlData);
    }
  }


  @Override
  public void setJavaScriptMember(String name, IJavaScriptBridge member) {
    // since Android Api 17 all methods callable from JavaScript must be annotated with @JavascriptInterface, an Android specific annotation
    // -> HtmlEditor cannot know this annotation, so we save the member instance, let the method call on ourselves and then pass the method call on to the member
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
  public void htmlChanged() {
    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      bridge.htmlChanged();
  }

  @Override
  @JavascriptInterface
  public void htmlHasBeenReset() {
    for(IJavaScriptBridge bridge : javaScriptBridgesToCall)
      bridge.htmlHasBeenReset();
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

  public boolean isLoaded() {
    return htmlEditor.isCKEditorLoaded();
  }

  @Override
  public void cleanUp() {
    htmlEditor.cleanUp();
  }
}
