package net.deepthought.controls.html;

import net.deepthought.controls.ICleanUp;

/**
 * Created by ganymed on 07/01/16.
 */
public interface IHtmlEditorPool<T> extends ICleanUp {

  T getHtmlEditor(IHtmlEditorListener listener);

  void htmlEditorReleased(T htmlEditor);

}
