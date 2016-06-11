package net.dankito.deepthought.controls.html;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ganymed on 17/09/15.
 */
public class DeepThoughtFxHtmlEditorPool implements IHtmlEditorPool<DeepThoughtFxHtmlEditor> {


  protected Queue<DeepThoughtFxHtmlEditor> availableHtmlEditors = new ConcurrentLinkedQueue<>();


  public DeepThoughtFxHtmlEditorPool() {

  }


  public DeepThoughtFxHtmlEditor getHtmlEditor(IHtmlEditorListener listener) {
    if(availableHtmlEditors.size() > 0) {
      DeepThoughtFxHtmlEditor editor = availableHtmlEditors.poll();
      editor.reInitHtmlEditor(listener);
      return editor;
    }

    return new DeepThoughtFxHtmlEditor(listener);
  }

  public void htmlEditorReleased(DeepThoughtFxHtmlEditor htmlEditor) {
    htmlEditor.setListener(null);
    htmlEditor.setHtml("", true);
    availableHtmlEditors.offer(htmlEditor);
  }

  @Override
  public void cleanUp() {
    for(DeepThoughtFxHtmlEditor editor : availableHtmlEditors)
      editor.cleanUp();
  }
}
