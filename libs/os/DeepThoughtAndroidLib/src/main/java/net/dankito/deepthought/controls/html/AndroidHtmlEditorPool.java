package net.dankito.deepthought.controls.html;

import android.app.Activity;
import android.view.ViewGroup;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.html.ImageElementData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ganymed on 17/09/15.
 */
public class AndroidHtmlEditorPool implements ICleanUp {

  private static final Logger log = LoggerFactory.getLogger(AndroidHtmlEditorPool.class);


  protected static final int MaxHtmlEditorsToCache = 2;


  protected static AndroidHtmlEditorPool instance = null;

//  public static void init(Activity contextToPreloadHtmlEditors) {
//    instance = new AndroidHtmlEditorPool(contextToPreloadHtmlEditors);
//  }

  public static AndroidHtmlEditorPool getInstance() {
    if(instance == null) {
      instance = new AndroidHtmlEditorPool(null);
    }
    return instance;
  }


//  protected Activity contextToPreloadHtmlEditors;

  protected Queue<AndroidHtmlEditor> availableHtmlEditors = new ConcurrentLinkedQueue<>();


  private AndroidHtmlEditorPool(Activity contextToPreloadHtmlEditors) {
//    this.contextToPreloadHtmlEditors = contextToPreloadHtmlEditors;
//    preloadHtmlEditors(contextToPreloadHtmlEditors, MaxHtmlEditorsToCache);
  }


  public AndroidHtmlEditor getHtmlEditor(Activity context, IHtmlEditorListener listener) {
//    contextToPreloadHtmlEditors.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        preloadHtmlEditors(contextToPreloadHtmlEditors, availableHtmlEditors.size() == 0 ? MaxHtmlEditorsToCache : MaxHtmlEditorsToCache - availableHtmlEditors.size() + 1);
//      }
//    });

    if(availableHtmlEditors.size() > 0) {
      AndroidHtmlEditor editor = availableHtmlEditors.poll();
      editor.reInitHtmlEditor(context, listener);
      return editor;
    }

    log.info("Creating new HtmlEditor");
    return new AndroidHtmlEditor(context, listener);
  }

  public void htmlEditorReleased(AndroidHtmlEditor htmlEditor) {
    htmlEditor.resetInstanceVariables();
    if(htmlEditor.getParent() instanceof ViewGroup)
      ((ViewGroup)htmlEditor.getParent()).removeView(htmlEditor);

    // do not cache release HtmlEditors anymore, they are using to much Memory by time
//    if(availableHtmlEditors.contains(htmlEditor) == false)
//      availableHtmlEditors.offer(htmlEditor);

//    log.info("Released HtmlEditor. There are now " + availableHtmlEditors.size() + " available HtmlEditors in Pool.");
  }

  public void preloadHtmlEditors(Activity context, int numberOfHtmlEditors) {
    final Map<Integer, AndroidHtmlEditor> preloadedHtmlEditors = new HashMap<>(numberOfHtmlEditors);

    for(int i = 0; i < numberOfHtmlEditors; i++) {
      final Integer instance = i;
      AndroidHtmlEditor htmlEditor = getHtmlEditor(context, new IHtmlEditorListener() {
        @Override
        public void editorHasLoaded(HtmlEditor editor) {
          // Editor is loaded now
          if(preloadedHtmlEditors.containsKey(instance)) {
            AndroidHtmlEditor htmlEditor = preloadedHtmlEditors.remove(instance);
            htmlEditorReleased(htmlEditor);
          }
        }

        @Override
        public void htmlCodeUpdated() {

        }

        @Override
        public void htmlCodeHasBeenReset() {

        }

        @Override
        public boolean handleCommand(HtmlEditor editor, HtmEditorCommand command) {
          return false;
        }

        @Override
        public boolean elementDoubleClicked(HtmlEditor editor, ImageElementData elementData) {
          return false;
        }
      });

      preloadedHtmlEditors.put(instance, htmlEditor);
    }
  }

  @Override
  public void cleanUp() {
    for(AndroidHtmlEditor editor : availableHtmlEditors)
      editor.cleanUp();
    instance = null;
  }
}
