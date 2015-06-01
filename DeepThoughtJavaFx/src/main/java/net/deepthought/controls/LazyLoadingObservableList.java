package net.deepthought.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

/**
 * Created by ganymed on 23/05/15.
 */
public class LazyLoadingObservableList<T> extends ObservableListBase<T> implements ObservableList<T> {

  protected Collection<T> underlyingCollection = null;


  public LazyLoadingObservableList() {
    setUnderlyingCollection(new ArrayList<T>());
  }


  @Override
  public int size() {
    if(underlyingCollection == null)
      return 0;
    return underlyingCollection.size();
  }

  @Override
  public Iterator<T> iterator() {
    return underlyingCollection.iterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    if(underlyingCollection instanceof List)
      ((List)underlyingCollection).listIterator(index);
    return super.listIterator(index);
  }

  @Override
  public T get(int index) {
    return getItemFromCollection(index, underlyingCollection);
  }

  protected T getItemFromCollection(int index, Collection<T> collection) {
    if(collection instanceof List)
      return ((List<T>) collection).get(index);

    int i = 0;
    Iterator<T> iterator = collection.iterator();
    while(iterator.hasNext()) {
      if(i == index)
        return iterator.next();

      iterator.next();
      i++;
    }

    return null;
  }


  public void setUnderlyingCollection(final Collection<T> underlyingCollection) {
    if(Platform.isFxApplicationThread()) // simply make sure that collection is set (or better: endChange() ) is called on UI Thread (otherwise a IllegalStateException would be thrown)
      setUnderlyingCollectionOnUiThread(underlyingCollection);
    else
      Platform.runLater(() -> setUnderlyingCollectionOnUiThread(underlyingCollection));
  }

  protected void setUnderlyingCollectionOnUiThread(Collection<T> underlyingCollection) {
    beginChange();
    // this only makes removed items getting loaded from Database. And as ObservableList is also working without why implementing it?
//    if(this.underlyingCollection instanceof List)
//      nextRemove(0, (List<T>)this.underlyingCollection);
//    else if(this.underlyingCollection != null) { // TODO: test
//      int i = 0;
//      for(T item : this.underlyingCollection) {
//        nextRemove(i, item);
//        i++;
//      }
//    }

    this.underlyingCollection = underlyingCollection;

    nextAdd(0, size());
    endChange();
  }

  @Override
  public void clear() {
    setUnderlyingCollection(new ArrayList<T>());
  }

  @Override
  public void add(int index, T element) {
    beginChange();

    if(underlyingCollection instanceof List) {
      ((List)underlyingCollection).add(index, element);
    }
    else {
      underlyingCollection.add(element);
    }

    nextAdd(index -1, index);
    endChange();
  }

  @Override
  public boolean remove(Object element) {
    boolean result = false;
    beginChange();

    try {
      result = underlyingCollection.remove((T)element);

//      nextRemove(index, element);
    } catch(Exception ex) {

    }
    finally {
      endChange();
    }

    return result;
  }


}
