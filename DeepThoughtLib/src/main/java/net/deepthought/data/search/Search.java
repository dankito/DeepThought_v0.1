package net.deepthought.data.search;

import net.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 12/04/15.
 */
public class Search<T extends BaseEntity> {

  private final static Logger log = LoggerFactory.getLogger(Search.class);


  protected String searchTerm;

  protected AtomicBoolean interrupt = new AtomicBoolean(false);

  protected boolean completed = false;

  protected Collection<T> results = new HashSet<>();

  protected SearchCompletedListener<T> completedListener = null;

  protected long startTime = new Date().getTime();


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
    log.debug("interrupt() has been called on search " + searchTerm + " (completed = " + completed + ")");
  }

  public boolean isInterrupted() {
    return interrupt.get();
  }

  public boolean isCompleted() {
    return completed;
  }

  public boolean addResult(T result) {
    return results.add(result);
  }

  public void setResults(Collection<T> results) {
    this.results = results;
  }

  public Collection<T> getResults() {
    return results;
  }

  public void fireSearchCompleted() {
    if(isInterrupted()) // do not call completedListener then
      return;

    if(completed)
      log.warn("Completed has been called more than once on Search " + searchTerm);

    completed = true;
    long millisecondsElapsed = (new Date().getTime() - startTime);
    log.debug("Search " + searchTerm + " took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds to complete");

    if(completedListener != null)
      completedListener.completed(results);
  }


  @Override
  public String toString() {
    return searchTerm;
  }

}
