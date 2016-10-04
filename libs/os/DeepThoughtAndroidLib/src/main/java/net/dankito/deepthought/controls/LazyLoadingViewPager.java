package net.dankito.deepthought.controls;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by ganymed on 26/09/16.
 */
public class LazyLoadingViewPager extends ViewPager {

  public LazyLoadingViewPager(Context context) {
    super(context);
    initPager();
  }

  public LazyLoadingViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    initPager();
  }


  protected void initPager() {
    addOnPageChangeListener(onPageChangeListener);
  }


  @Override
  public void setAdapter(PagerAdapter adapter) {
    if(adapter instanceof FragmentPagerAdapter) {
      FragmentPagerAdapter fragmentPagerAdapter = (FragmentPagerAdapter)adapter;
      super.setAdapter(new LazyLoadingFragmentPagerAdapter(((FragmentActivity)getContext()).getSupportFragmentManager(), fragmentPagerAdapter));
    }
    else {
      super.setAdapter(adapter);
    }
  }

  protected void pageSelected(int position) {
    if(getAdapter() instanceof LazyLoadingFragmentPagerAdapter) {
      ((LazyLoadingFragmentPagerAdapter)getAdapter()).itemGotSelected(position);
    }
  }

  protected OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
      pageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
  };

}
