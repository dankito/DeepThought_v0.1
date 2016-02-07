package net.deepthought.data.search.specific;

import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.search.SearchBase;
import net.deepthought.data.search.SearchCompletedListener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ganymed on 27/07/15.
 */
public class ReferenceBasesSearch extends SearchBase {

  protected Collection<ReferenceBase> results = new ArrayList<>();

  protected ReferenceBaseType type = ReferenceBaseType.All;

  protected SearchCompletedListener<Collection<ReferenceBase>> completedListener = null;


  public ReferenceBasesSearch(String searchTerm, SearchCompletedListener<Collection<ReferenceBase>> completedListener) {
    this(searchTerm, ReferenceBaseType.All, completedListener);
  }

  public ReferenceBasesSearch(String searchTerm, ReferenceBaseType type, SearchCompletedListener<Collection<ReferenceBase>> completedListener) {
    super(searchTerm);
    this.type = type;
    this.completedListener = completedListener;
  }


  public void setResults(Collection<ReferenceBase> results) {
    this.results = results;
  }

  protected void callCompletedListener() {
    if(completedListener != null)
      completedListener.completed(results);
  }

  @Override
  protected int getResultsCount() {
    return results.size();
  }


  public boolean addResult(ReferenceBase result) {
    return this.results.add(result);
  }

  public Collection<ReferenceBase> getResults() {
    return results;
  }

  public ReferenceBaseType getType() {
    return type;
  }


}
