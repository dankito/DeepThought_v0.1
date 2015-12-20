package net.deepthought.clipboard;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.ContentExtractOptions;

import java.util.HashSet;
import java.util.Set;

import javafx.scene.input.Clipboard;
import javafx.stage.Stage;

/**
 * Created by ganymed on 20/12/15.
 */
public class JavaFxClipboardWatcher implements IClipboardWatcher {

  protected Stage stage;

  protected Object sourceOfLastShownPopup = null;

  protected Set<ClipboardContentChangedListener> clipboardContentChangedExternallyListeners = new HashSet<>();


  public JavaFxClipboardWatcher(Stage stage) {
    this.stage = stage;

    stage.focusedProperty().addListener((observable, oldValue, newValue) -> checkForChangedClipboardContent(newValue));
  }

  public JavaFxClipboardWatcher(Stage stage, ClipboardContentChangedListener listener) {
    this(stage);
    addClipboardContentChangedExternallyListener(listener);
  }


  protected void checkForChangedClipboardContent(Boolean isStageFocused) {
    if(isStageFocused) {
      checkForChangedClipboardContent();
    }
  }

  protected void checkForChangedClipboardContent() {
    ClipboardContent clipboardContent = new JavaFxClipboardContent(Clipboard.getSystemClipboard());
    Application.getContentExtractorManager().getContentExtractorOptionsForClipboardContentAsync(clipboardContent, contentExtractOptions -> {
      if (contentExtractOptions.getSource().equals(sourceOfLastShownPopup) == false) {
        sourceOfLastShownPopup = contentExtractOptions.getSource();
        callClipboardContentChangedExternallyListeners(contentExtractOptions);
      }
    });
  }

  protected void callClipboardContentChangedExternallyListeners(ContentExtractOptions contentExtractOptions) {
    for(ClipboardContentChangedListener listener : clipboardContentChangedExternallyListeners) {
      listener.clipboardContentChanged(contentExtractOptions);
    }
  }


  @Override
  public boolean addClipboardContentChangedExternallyListener(ClipboardContentChangedListener listener) {
    return clipboardContentChangedExternallyListeners.add(listener);
  }

  @Override
  public boolean removeClipboardContentChangedExternallyListener(ClipboardContentChangedListener listener) {
    return clipboardContentChangedExternallyListeners.remove(listener);
  }

}
