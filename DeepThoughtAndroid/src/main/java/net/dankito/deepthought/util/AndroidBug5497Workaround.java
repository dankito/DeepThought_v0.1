package net.dankito.deepthought.util;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * Oh man, i was searching for a solution to this bug for ages till i found this:
 * https://stackoverflow.com/a/19494006
 * Thank you so much Joseph Johnson!
 */
public class AndroidBug5497Workaround {

  // For more information, see https://code.google.com/p/android/issues/detail?id=5497
  // To use this class, simply invoke assistContentView() on an Activity that already has its content view set.

  public static void assistContentView(View contentView) {
    new AndroidBug5497Workaround(contentView);
  }


  protected View mChildOfContent;
  protected int usableHeightPrevious;
  protected ViewGroup.LayoutParams layoutParams;


  private AndroidBug5497Workaround(View contentView) {
    mChildOfContent = contentView;

    layoutParams = mChildOfContent.getLayoutParams();
    mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      public void onGlobalLayout() {
        possiblyResizeChildOfContent();
      }
    });
  }

  private void possiblyResizeChildOfContent() {
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
      mChildOfContent.requestLayout();
      usableHeightPrevious = usableHeightNow;
    }
  }

  protected int getUsableHeightSansKeyboard() {
    View rootView = mChildOfContent.getRootView();
    ViewGroup.LayoutParams rootLayoutParams = rootView.getLayoutParams();

    return rootView.getHeight() - tryToGetTop(mChildOfContent.getLayoutParams());
  }

  protected int tryToGetTop(ViewGroup.LayoutParams layoutParams) {
    if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
      return ((ViewGroup.MarginLayoutParams)layoutParams).topMargin;
    }

    return 0;
  }

  private int computeUsableHeight() {
    Rect r = new Rect();
    mChildOfContent.getWindowVisibleDisplayFrame(r);
    return (r.bottom - r.top);
  }

}
