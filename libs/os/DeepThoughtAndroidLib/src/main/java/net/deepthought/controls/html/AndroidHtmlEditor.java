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

/**
 * Created by ganymed on 26/09/15.
 */
public class AndroidHtmlEditor extends WebView implements IJavaScriptBridge, IJavaScriptExecutor {

  private final static Logger log = LoggerFactory.getLogger(AndroidHtmlEditor.class);


  protected HtmlEditor htmlEditor = null;

  protected Activity activity;


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

    htmlEditor = new HtmlEditor(this, listener);

    setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        htmlEditor.editorLoaded();
      }

      @Override
      public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
      }

      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
      }
    });

    setWebChromeClient(new WebChromeClient() {
      @Override
      public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//        return super.onJsAlert(view, url, message, result);
        result.confirm();
        return true;
      }
    });

    addJavascriptInterface(this, "app");
    addJavascriptInterface(this, "android");
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
      evaluateJavascript(javaScript, new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
          if(listener != null)
            listener.scriptExecuted(value);
        }
      });

//      loadUrl("javascript:" + javaScript);
    } catch(Exception ex) {
      log.error("Could not evaluate JavaScript " + javaScript, ex);
    }
  }

  @Override
  public void setJavaScriptMember(String name, Object member) {
    // since Android Api 17 all methods callable from JavaScript must be annotated with @JavascriptInterface, an Android specific annotation -> HtmlEditor cannot know this annotation
//    addJavascriptInterface(member, name);
  }

  @JavascriptInterface
  public void loaded() {
    htmlEditor.loaded();
  }

  @JavascriptInterface
  public void htmlChanged(String newHtmlCode) {
    htmlEditor.htmlChanged(newHtmlCode);
  }

}
