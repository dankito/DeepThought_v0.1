package net.deepthought.controls.html;

import net.deepthought.controls.ICleanableControl;

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

/**
 * Created by ganymed on 28/08/15.
 */
public class DeepThoughtFxHtmlEditor extends HBox implements IJavaScriptExecutor, ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(DeepThoughtFxHtmlEditor.class);


  protected WebView webView = new WebView();

  protected WebEngine engine;

  protected HtmlEditor htmlEditor;


  public DeepThoughtFxHtmlEditor() {
    this(null);
  }

  public DeepThoughtFxHtmlEditor(HtmlEditorListener listener) {
    this.engine = webView.getEngine();
    this.htmlEditor = new HtmlEditor(this, listener);

    setupHtmlEditor();
  }

  protected void setupHtmlEditor() {
    setMinHeight(200);
    setPrefHeight(Region.USE_COMPUTED_SIZE);
    setMaxHeight(Double.MAX_VALUE);
    webView.setMinHeight(200);
    webView.setPrefHeight(Region.USE_COMPUTED_SIZE);
    webView.setMaxHeight(Double.MAX_VALUE);

    this.getChildren().add(webView);
    HBox.setHgrow(webView, Priority.ALWAYS);
    webView.prefHeightProperty().bind(this.heightProperty());

    setFillHeight(true);

    loadCKEditor();

    testEvents();
  }

  protected void loadCKEditor() {
    engine.getLoadWorker().stateProperty().addListener(
        new ChangeListener<Worker.State>() {
          @Override
          public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
            if (newState == Worker.State.SUCCEEDED) {
              htmlEditor.editorLoaded();
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
    htmlEditor.setHtml(html);
  }

  public void setListener(HtmlEditorListener listener) {
    htmlEditor.setListener(listener);
  }


  protected void testEvents() {
    engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
      @Override
      public void handle(WebEvent<String> event) {

      }

    });

    engine.setConfirmHandler(new Callback<String, Boolean>()

                             {
                               @Override
                               public Boolean call (String param){
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
  public Object executeScript(String javaScript) {
    try {
      return engine.executeScript(javaScript);
    } catch(Exception ex) {
      log.error("Could not execute JavaScript " + javaScript, ex);
    }

    return null; // TODO: what to return in this case? A NullObject? How to get JavaScript 'undefined' JSObject?
  }

  @Override
  public void cleanUpControl() {
    htmlEditor.setListener(null);
  }

}
