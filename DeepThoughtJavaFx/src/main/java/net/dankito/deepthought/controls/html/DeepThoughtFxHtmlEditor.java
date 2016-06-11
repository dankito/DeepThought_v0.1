package net.dankito.deepthought.controls.html;

import net.dankito.deepthought.controls.ICleanUp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import netscape.javascript.JSObject;

/**
 * Created by ganymed on 28/08/15.
 */
public class DeepThoughtFxHtmlEditor extends HBox implements IJavaScriptExecutor, ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(DeepThoughtFxHtmlEditor.class);


  protected WebView webView = new WebView();

  protected WebEngine engine;

  protected HtmlEditor htmlEditor;


  public DeepThoughtFxHtmlEditor() {
    this(null);
  }

  public DeepThoughtFxHtmlEditor(IHtmlEditorListener listener) {
    this.engine = webView.getEngine();
    this.htmlEditor = new HtmlEditor(this, listener);

    setupHtmlEditor();
  }

  protected void setupHtmlEditor() {
    setMinHeight(200);
    setPrefHeight(Region.USE_COMPUTED_SIZE);
    setMaxHeight(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);
    webView.setMinHeight(200);
    webView.setPrefHeight(Region.USE_COMPUTED_SIZE);
    webView.setMaxHeight(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);

    this.getChildren().add(webView);
    HBox.setHgrow(webView, Priority.ALWAYS);
    webView.prefHeightProperty().bind(this.heightProperty());
    webView.prefWidthProperty().bind(this.widthProperty());

    setFillHeight(true);

    loadCKEditor();

//    testEvents();
  }

  protected void loadCKEditor() {
    engine.getLoadWorker().stateProperty().addListener(
        new ChangeListener<Worker.State>() {
          @Override
          public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
            if (newState == Worker.State.SUCCEEDED) {
              htmlEditor.webControlLoaded();
            } else if (newState == Worker.State.FAILED) {
              log.error("Loading CKEditor failed");
              // TODO: notify user
            }
          }
        }
    );

//    engine.setUserDataDirectory(directory);
    engine.load(htmlEditor.getHtmlEditorPath());
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


  public void insertHtml(String html) {
    htmlEditor.insertHtml(html);
  }


  protected void testEvents() {
    engine.locationProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

      }
    });

    engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
      @Override
      public void handle(WebEvent<String> event) {

      }

    });

    engine.setConfirmHandler(new Callback<String, Boolean>()

                             {
                               @Override
                               public Boolean call(String param) {
                                 return false;
                               }
                             }

    );

    engine.setPromptHandler(new Callback<PromptData, String>()

                            {
                              @Override
                              public String call(PromptData param) {
                                return null;
                              }
                            }

    );

    engine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>()

                                 {
                                   @Override
                                   public WebEngine call(PopupFeatures param) {
                                     return engine;
                                   }
                                 }

    );

    engine.setOnAlert(new EventHandler<WebEvent<String>>()

                      {
                        @Override
                        public void handle(WebEvent<String> event) {

                        }
                      }

    );
  }


  @Override
  public void executeScript(String javaScript) {
    executeScript(javaScript, null);
  }

  @Override
  public void executeScript(final String javaScript, final ExecuteJavaScriptResultListener listener) {
    net.dankito.deepthought.controls.utils.FXUtils.runOnUiThread(() -> executeScriptOnUiThread(javaScript, listener));
  }

  public void executeScriptOnUiThread(String javaScript, ExecuteJavaScriptResultListener listener) {
    try {
      Object result = engine.executeScript(javaScript);
      if(listener != null)
        listener.scriptExecuted(result);
    } catch(Exception ex) {
      log.error("Could not execute JavaScript " + javaScript, ex);
      if(listener != null)
        listener.scriptExecuted(null); // TODO: what to return in this case? A NullObject? How to get JavaScript 'undefined' JSObject?
    }
  }

  @Override
  public void setJavaScriptMember(final String name, final IJavaScriptBridge member) {
    executeScript("window", new ExecuteJavaScriptResultListener() {
      @Override
      public void scriptExecuted(Object result) {
        try {
          JSObject win = (JSObject) result;
          win.setMember(name, member);
        } catch (Exception ex) {
          log.error("Could not set JavaScript member '" + name + "' to " + member, ex);
        }
      }
    });
  }

  @Override
  public void cleanUp() {
    htmlEditor.cleanUp();
  }

}
