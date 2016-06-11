package net.dankito.deepthought.clipboard;

/**
 * Created by ganymed on 20/12/15.
 */
public interface IClipboardWatcher {

  boolean addClipboardContentChangedExternallyListener(ClipboardContentChangedListener listener);

  boolean removeClipboardContentChangedExternallyListener(ClipboardContentChangedListener listener);

}
