package net.dankito.deepthought.controls;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 27/09/16.
 */
public class LazyLoadingFragmentManager extends FragmentManager {

  protected FragmentManager actualFragmentManager;

  protected int positionOfCurrentlySelectedFragment = 0;

  protected Integer nextItemToGetPosition = null;

  protected Map<String, Integer> tagToPositionMap = new HashMap<>();

  protected Map<String, Fragment> mapInstantiatedFragments = new HashMap<>();


  public LazyLoadingFragmentManager(FragmentManager actualFragmentManager) {
    this.actualFragmentManager = actualFragmentManager;
  }


  @Override
  public FragmentTransaction beginTransaction() {
    return actualFragmentManager.beginTransaction();
  }

  @Override
  public boolean executePendingTransactions() {
    return actualFragmentManager.executePendingTransactions();
  }

  @Override
  public Fragment findFragmentById(@IdRes int id) {
    return actualFragmentManager.findFragmentById(id);
  }

  @Override
  public Fragment findFragmentByTag(String tag) {
    if(nextItemToGetPosition != null) {
      tagToPositionMap.put(tag, nextItemToGetPosition);
      nextItemToGetPosition = null;
    }

    Fragment fragment = getInstantiatedFragmentForTag(tag);
    if(fragment == null) {
      if(shouldInstantiateFragment(tag)) {
        fragment = actualFragmentManager.findFragmentByTag(tag);
      }
      else {
        fragment = createPlaceholderFragment(tag);
        mapInstantiatedFragments.put(tag, fragment);
      }
    }

    return fragment;
  }

  protected Fragment getInstantiatedFragmentForTag(String tag) {
    return mapInstantiatedFragments.get(tag);
  }

  protected boolean shouldInstantiateFragment(String tag) {
    Integer fragmentPosition = tagToPositionMap.get(tag);
    return fragmentPosition == null || 0 == (int)fragmentPosition || positionOfCurrentlySelectedFragment == (int)fragmentPosition;
  }

  protected Fragment createPlaceholderFragment(String tag) {
    Fragment placeholder = new Fragment();

    return placeholder;
  }

  @Override
  public void popBackStack() {
    actualFragmentManager.popBackStack();
  }

  @Override
  public boolean popBackStackImmediate() {
    return actualFragmentManager.popBackStackImmediate();
  }

  @Override
  public void popBackStack(String name, int flags) {
    actualFragmentManager.popBackStack(name, flags);
  }

  @Override
  public boolean popBackStackImmediate(String name, int flags) {
    return actualFragmentManager.popBackStackImmediate(name, flags);
  }

  @Override
  public void popBackStack(int id, int flags) {
    actualFragmentManager.popBackStack(id, flags);
  }

  @Override
  public boolean popBackStackImmediate(int id, int flags) {
    return actualFragmentManager.popBackStackImmediate(id, flags);
  }

  @Override
  public int getBackStackEntryCount() {
    return actualFragmentManager.getBackStackEntryCount();
  }

  @Override
  public BackStackEntry getBackStackEntryAt(int index) {
    return actualFragmentManager.getBackStackEntryAt(index);
  }

  @Override
  public void addOnBackStackChangedListener(OnBackStackChangedListener listener) {
    actualFragmentManager.addOnBackStackChangedListener(listener);
  }

  @Override
  public void removeOnBackStackChangedListener(OnBackStackChangedListener listener) {
    actualFragmentManager.removeOnBackStackChangedListener(listener);
  }

  @Override
  public void putFragment(Bundle bundle, String key, Fragment fragment) {
    actualFragmentManager.putFragment(bundle, key, fragment);
  }

  @Override
  public Fragment getFragment(Bundle bundle, String key) {
    return actualFragmentManager.getFragment(bundle, key);
  }

  @Override
  public List<Fragment> getFragments() {
    return actualFragmentManager.getFragments();
  }

  @Override
  public Fragment.SavedState saveFragmentInstanceState(Fragment f) {
    return actualFragmentManager.saveFragmentInstanceState(f);
  }

  @Override
  public boolean isDestroyed() {
    return actualFragmentManager.isDestroyed();
  }

  @Override
  public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
    actualFragmentManager.dump(prefix, fd, writer, args);
  }


  public void setPositionOfCurrentlySelectedFragment(int positionOfCurrentlySelectedFragment) {
    this.positionOfCurrentlySelectedFragment = positionOfCurrentlySelectedFragment;
  }

  public void setNextItemToGet(int position) {
    nextItemToGetPosition = position;
  }
}
