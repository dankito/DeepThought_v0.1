package net.dankito.deepthought.util;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import net.dankito.deepthought.controls.ICleanUp;

/**
 * Oh man, i was searching for a solution to this bug for ages till i found this:
 * https://stackoverflow.com/a/19494006
 * Thank you so much Joseph Johnson!
 *
 * For more information, see https://code.google.com/p/android/issues/detail?id=5497
 */
public class AndroidBug5497Workaround implements ICleanUp {

  protected View contentView;

  protected int usableHeightPrevious;

  protected ViewGroup.LayoutParams layoutParams;


  public AndroidBug5497Workaround(View contentView) {
    this.contentView = contentView;

    layoutParams = this.contentView.getLayoutParams();

    this.contentView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
  }

  @Override
  public void cleanUp() {
    if(contentView != null) {
      if (OsHelper.isRunningOnAndroidAtLeastOfApiLevel(16)) {
        removeGlobalLayoutListenerPostJellyBean();
      } else {
        removeGlobalLayoutListenerPreJellyBean();
      }
    }

    contentView = null;
    layoutParams = null;
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  protected void removeGlobalLayoutListenerPostJellyBean() {
    contentView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
  }

  protected void removeGlobalLayoutListenerPreJellyBean() {
    contentView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
  }


  protected ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
    public void onGlobalLayout() {
      possiblyResizeChildOfContent();
    }
  };


  protected void possiblyResizeChildOfContent() {
    int usableHeightNow = computeUsableHeight();
    if (usableHeightNow != usableHeightPrevious) {
      int usableHeightSansKeyboard = getUsableHeightSansKeyboard();

      int heightDifference = usableHeightSansKeyboard - usableHeightNow;
      if (heightDifference > (usableHeightSansKeyboard/4)) {
        // keyboard probably just became visible
        layoutParams.height = usableHeightSansKeyboard - heightDifference;
      } else {
        // keyboard probably just became hidden
        layoutParams.height = usableHeightSansKeyboard; // i don't know why
      }
      contentView.requestLayout();
      usableHeightPrevious = usableHeightNow;
    }
  }

  protected int getUsableHeightSansKeyboard() {
    View rootView = contentView.getRootView();
    ViewGroup.LayoutParams rootLayoutParams = rootView.getLayoutParams();

    return rootView.getHeight() - tryToGetTop(contentView.getLayoutParams());
  }

  protected int tryToGetTop(ViewGroup.LayoutParams layoutParams) {
    if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
      return ((ViewGroup.MarginLayoutParams)layoutParams).topMargin;
    }

    return 0;
  }

  protected int computeUsableHeight() {
    Rect r = new Rect();
    contentView.getWindowVisibleDisplayFrame(r);
    return (r.bottom - r.top);
  }

}
