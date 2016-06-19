package net.dankito.deepthought.util;

import android.content.Context;

import net.dankito.deepthought.clipboard.IClipboardHelper;

/**
 * Created by ganymed on 19/06/16.
 */
public class AndroidClipboardHelper implements IClipboardHelper {

  protected Context context;


  public AndroidClipboardHelper(Context context) {
    this.context = context;
  }


  @Override
  public void copyStringToClipboard(String clipboardContent) {
    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
      android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
      clipboard.setText(clipboardContent);
    }
    else {
      android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
      android.content.ClipData clip = android.content.ClipData.newPlainText("", clipboardContent);
      clipboard.setPrimaryClip(clip);
    }
  }

}
