package net.dankito.deepthought.controls;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 26/09/16.
 */
public class LazyLoadingFragmentPagerAdapter extends FragmentPagerAdapter {

  protected LazyLoadingFragmentManager lazyLoadingFragmentManager;

  protected FragmentPagerAdapter actualAdapter;

  protected List<Integer> loadedPages = new ArrayList<>();

  protected Map<Integer, Fragment> placeholderFragments = new HashMap<>();


  public LazyLoadingFragmentPagerAdapter(FragmentManager fm, FragmentPagerAdapter actualAdapter) {
    this(new LazyLoadingFragmentManager(fm), actualAdapter);
  }

  protected LazyLoadingFragmentPagerAdapter(LazyLoadingFragmentManager lazyLoadingFragmentManager, FragmentPagerAdapter actualAdapter) {
    super(lazyLoadingFragmentManager);

    this.lazyLoadingFragmentManager = lazyLoadingFragmentManager;
    this.actualAdapter = actualAdapter;
  }


  @Override
  public int getCount() {
    return actualAdapter.getCount();
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return actualAdapter.getPageTitle(position);
  }

  @Override
  public long getItemId(int position) {
    return actualAdapter.getItemId(position);
  }

  @Override
  public int getItemPosition(Object object) {
    return actualAdapter.getItemPosition(object);
  }

  @Override
  public float getPageWidth(int position) {
    return actualAdapter.getPageWidth(position);
  }

  @Override
  public Object instantiateItem(View container, int position) {
    return super.instantiateItem(container, position);
  }

  @Override
  public Fragment getItem(int position) {
    lazyLoadingFragmentManager.setNextItemToGet(position);

    if(shouldUseActualAdapter(position)) {
      return actualAdapter.getItem(position);
    }

    return getPlaceholderFragment(position);
  }

  protected boolean shouldUseActualAdapter(int position) {
    return position == 0 || loadedPages.contains(position);
  }

  protected Fragment getPlaceholderFragment(int position) {
    Fragment placeholderFragment = placeholderFragments.get(position);

    if(placeholderFragment == null) {
      placeholderFragment = new Fragment();
      placeholderFragments.put(position, placeholderFragment);
    }

    return placeholderFragment;
  }


  public void itemGotSelected(int position) {
    loadedPages.add(position);

    placeholderFragments.remove(position);
  }

}
