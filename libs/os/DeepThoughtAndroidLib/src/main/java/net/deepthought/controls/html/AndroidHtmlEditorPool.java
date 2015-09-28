package net.deepthought.controls.html;

import android.app.Activity;
import android.view.ViewGroup;

import net.deepthought.controls.ICleanUp;
import net.deepthought.data.html.ImageElementData;

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

  private final static Logger log = LoggerFactory.getLogger(AndroidHtmlEditorPool.class);


  protected static AndroidHtmlEditorPool instance = null;

  public static AndroidHtmlEditorPool getInstance() {
    if(instance == null)
      instance = new AndroidHtmlEditorPool();
    return instance;
  }


  protected Queue<AndroidHtmlEditor> availableHtmlEditors = new ConcurrentLinkedQueue<>();


  public AndroidHtmlEditorPool() {

  }


  public AndroidHtmlEditor getHtmlEditor(Activity context, IHtmlEditorListener listener) {
    if(availableHtmlEditors.size() > 0) {
      log.info("Returning cached HtmlEditor");
      AndroidHtmlEditor editor = availableHtmlEditors.poll();
      editor.reInitHtmlEditor(context, listener);
      log.info("There are now " + availableHtmlEditors.size() + " cached HtmlEditor");
      return editor;
    }

    log.info("Creating new HtmlEditor");
    return new AndroidHtmlEditor(context, listener);
  }

  public void htmlEditorReleased(AndroidHtmlEditor htmlEditor) {
    log.info("HtmlEditor released");

    htmlEditor.resetInstanceVariables();
    if(htmlEditor.getParent() instanceof ViewGroup)
      ((ViewGroup)htmlEditor.getParent()).removeView(htmlEditor);
    if(availableHtmlEditors.contains(htmlEditor) == false)
      availableHtmlEditors.offer(htmlEditor);

    log.info("There are now " + availableHtmlEditors.size() + " cached HtmlEditor");
  }

  public void preloadHtmlEditors(Activity context, int numberOfHtmlEditors) {
    final Map<Integer, AndroidHtmlEditor> preloadedHtmlEditors = new HashMap<>(numberOfHtmlEditors);

    for(int i = 0; i < numberOfHtmlEditors; i++) {
      final Integer instance = i;
      AndroidHtmlEditor htmlEditor = getHtmlEditor(context, new IHtmlEditorListener() {
        @Override
        public void editorHasLoaded(HtmlEditor editor) {
          log.info("editorHasLoaded() called");
          // Editor is loaded now
          if(preloadedHtmlEditors.containsKey(instance)) {
            log.info("Releasing preloaded HtmlEditor");
            AndroidHtmlEditor htmlEditor = preloadedHtmlEditors.remove(instance);
            htmlEditorReleased(htmlEditor);
          }
        }

        @Override
        public void htmlCodeUpdated(String newHtmlCode) {

        }

        @Override
        public boolean handleCommand(HtmlEditor editor, HtmEditorCommand command) {
          return false;
        }

        @Override
        public boolean elementDoubleClicked(HtmlEditor editor, ImageElementData elementData) {
          return false;
        }

        @Override
        public void imageAdded(ImageElementData addedImage) {

        }

        @Override
        public void imageHasBeenDeleted(ImageElementData deletedImage, boolean isStillInAnotherInstanceOnHtml) {

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
