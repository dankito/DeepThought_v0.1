package net.deepthought.data.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 12/04/15.
 */
public abstract class SearchBase {

  private final static Logger log = LoggerFactory.getLogger(SearchBase.class);


  protected String searchTerm;

  protected AtomicBoolean interrupt = new AtomicBoolean(false);

  protected boolean completed = false;

  protected long startTime = new Date().getTime();


  public SearchBase(String searchTerm) {
    this.searchTerm = searchTerm;
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

  public void fireSearchCompleted() {
    if(isInterrupted()) // do not call completedListener then
      return;

    if(completed)
      log.warn("Completed has been called more than once on Search " + searchTerm);

    completed = true;
    long millisecondsElapsed = (new Date().getTime() - startTime);
    log.debug("Search " + searchTerm + " took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds to complete");

    callCompletedListener();
  }

  protected abstract void callCompletedListener();


  @Override
  public String toString() {
    return searchTerm;
  }

}
