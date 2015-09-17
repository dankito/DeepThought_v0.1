package net.deepthought.controls.html;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ganymed on 17/09/15.
 */
public class DeepThoughtFxHtmlEditorPool {

  protected static DeepThoughtFxHtmlEditorPool instance = null;

  public static DeepThoughtFxHtmlEditorPool getInstance() {
    if(instance == null)
      instance = new DeepThoughtFxHtmlEditorPool();
    return instance;
  }


  protected Queue<DeepThoughtFxHtmlEditor> availableHtmlEditors = new ConcurrentLinkedQueue<>();


  public DeepThoughtFxHtmlEditorPool() {

  }


  public DeepThoughtFxHtmlEditor getHtmlEditor(HtmlEditorListener listener) {
    if(availableHtmlEditors.size() > 0) {
      DeepThoughtFxHtmlEditor editor = availableHtmlEditors.poll();
      editor.setListener(listener);
      return editor;
    }

    return new DeepThoughtFxHtmlEditor(listener);
  }

  public void htmlEditorReleased(DeepThoughtFxHtmlEditor htmlEditor) {
    availableHtmlEditors.offer(htmlEditor);
    htmlEditor.setListener(null);
  }

}
