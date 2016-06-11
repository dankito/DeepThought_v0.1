package net.dankito.deepthought.clipboard;

import net.dankito.deepthought.data.contentextractor.ContentExtractOptions;

/**
 * Created by ganymed on 20/12/15.
 */
public interface ClipboardContentChangedListener {

  void clipboardContentChanged(ContentExtractOptions contentExtractOptions);

}
