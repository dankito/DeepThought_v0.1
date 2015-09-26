package net.deepthought.controls.html;

import android.app.Activity;
import android.view.ViewGroup;

import net.deepthought.controls.ICleanUp;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ganymed on 17/09/15.
 */
public class AndroidHtmlEditorPool implements ICleanUp {

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
      AndroidHtmlEditor editor = availableHtmlEditors.poll();
      editor.reInitHtmlEditor(context, listener);
      return editor;
    }

    return new AndroidHtmlEditor(context, listener);
  }

  public void htmlEditorReleased(AndroidHtmlEditor htmlEditor) {
    htmlEditor.resetInstanceVariables();
    if(htmlEditor.getParent() instanceof ViewGroup)
      ((ViewGroup)htmlEditor.getParent()).removeView(htmlEditor);
    availableHtmlEditors.offer(htmlEditor);
  }

  @Override
  public void cleanUp() {
    for(AndroidHtmlEditor editor : availableHtmlEditors)
      editor.cleanUp();
  }
}
