package net.deepthought.controls;

import com.sun.webkit.WebPage;

import net.deepthought.util.Localization;

import org.w3c.dom.Document;

import java.lang.reflect.Field;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebView;

/**
 * Created by ganymed on 04/03/15.
 */
public class ContextHelpControl extends ScrollPane {

  protected String helpTextResourceKeyPrefix = "";

  protected WebView contextHelpView;


  public ContextHelpControl() {
    setupContextHelpView();

    this.setPrefWidth(200);
    this.setFitToHeight(true);
    this.setFitToWidth(true);
    this.setPannable(true);
  }

  public ContextHelpControl(String helpTextResourceKeyPrefix) {
    this();

    this.helpTextResourceKeyPrefix = helpTextResourceKeyPrefix;
    showContextHelpForResourceKey("default");
  }


  protected void setupContextHelpView() {
    this.contextHelpView = new WebView();
//    contextHelpView.setContextMenuEnabled(false);
    contextHelpView.setFontScale(0.85);

    this.setContent(contextHelpView);

    // set Page's background color to default help color (a light blue)
    try {
      // Use reflection to retrieve the WebEngine's private 'page' field.
      Field f = contextHelpView.getEngine().getClass().getDeclaredField("page");
      f.setAccessible(true);
      final WebPage page = (WebPage) f.get(contextHelpView.getEngine());
      contextHelpView.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
        @Override
        public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue) {
          page.setBackgroundColor(Constants.ContextHelpBackgroundColor);
        }
      });
    } catch (Exception e) { }
  }

  public void showContextHelpForResourceKey(String contextHelpResourceKey) {
    showContextHelp(Localization.getLocalizedStringForResourceKey(helpTextResourceKeyPrefix + contextHelpResourceKey));
  }

  public void showContextHelp(String contextHelp) {
    contextHelpView.getEngine().loadContent(contextHelp);
  }

}
