package net.deepthought.data.search;

import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 12/04/15.
 */
public class Search<T extends BaseEntity> {

  protected String searchTerm;

  protected AtomicBoolean interrupt = new AtomicBoolean(false);

  protected Set<T> results = new HashSet<>();

  protected SearchCompletedListener<T> completedListener = null;


  public Search(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  public Search(String searchTerm, SearchCompletedListener<T> completedListener) {
    this(searchTerm);
    this.completedListener = completedListener;
  }


  public String getSearchTerm() {
    return searchTerm;
  }

  public void interrupt() {
    this.interrupt.set(true);
  }

  public boolean isInterrupted() {
    return interrupt.get();
  }


  public boolean addResult(T result) {
    return results.add(result);
  }

  public boolean addResults(Collection<T> results) {
    return this.results.addAll(results);
  }

  public Collection<T> getResults() {
    return results;
  }

  public void fireSearchCompleted() {
    if(completedListener != null)
      completedListener.completed(results);
  }


  @Override
  public String toString() {
    return searchTerm;
  }

}
