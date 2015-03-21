package net.deepthought.data.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 17/03/15.
 */
public class IndexTerm {

  protected String term;

  protected int numberOfEntriesContainingTerm;

  protected List<Long> entriesContainingTermIds = new ArrayList<>();


  public IndexTerm(String term, int numberOfEntriesContainingTerm) {
    this.term = term;
    this.numberOfEntriesContainingTerm = numberOfEntriesContainingTerm;
  }

  public boolean addEntryContainingTerm(Long id) {
    return entriesContainingTermIds.add(id);
  }


  @Override
  public String toString() {
    return term + " (" + numberOfEntriesContainingTerm + ")";
  }

}
