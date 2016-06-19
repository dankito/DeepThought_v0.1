package net.dankito.deepthought.util;

import android.content.Context;
import android.net.Uri;

import net.dankito.deepthought.clipboard.IClipboardHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 19/06/16.
 */
public class AndroidClipboardHelper implements IClipboardHelper {

  private static final Logger log = LoggerFactory.getLogger(AndroidClipboardHelper.class);


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

  @Override
  public void copyUrlToClipboard(String url) {
    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
      android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
      clipboard.setText(url);
    }
    else {
      android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
      try {
        android.content.ClipData clip = android.content.ClipData.newRawUri("", Uri.parse(url));
//        clip.addItem(new ClipData.Item(url)); // also copy URL as Plain Text
        clipboard.setPrimaryClip(clip);
      } catch(Exception ex) {
        log.error("Could not create Uri from String '" + url + "'", ex);
      }
    }
  }

}
