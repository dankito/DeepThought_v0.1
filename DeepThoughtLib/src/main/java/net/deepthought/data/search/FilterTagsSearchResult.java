package net.deepthought.data.search;

import net.deepthought.data.model.Tag;

import java.util.Collection;

/**
 * Created by ganymed on 27/07/15.
 */
public class FilterTagsSearchResult {

  protected String searchTerm;

  protected boolean hasExactMatch;

  protected Tag exactMatch;

  protected Collection<Tag> allMatches;


  public FilterTagsSearchResult(String searchTerm, Collection<Tag> allMatches) {
    this.searchTerm = searchTerm;
    this.allMatches = allMatches;

    findExactMatch(searchTerm, allMatches);
  }

  public FilterTagsSearchResult(String searchTerm, Collection<Tag> allMatches, Tag exactMatch) {
    this.searchTerm = searchTerm;
    this.allMatches = allMatches;
    this.hasExactMatch = exactMatch != null;
    this.exactMatch = exactMatch;
  }


  protected void findExactMatch(String searchTerm, Collection<Tag> allMatches) {
    for(Tag match : allMatches) {
      if(searchTerm.equals(match.getName().toLowerCase())) {
        this.hasExactMatch = true;
        this.exactMatch = match;
        break;
      }
    }
  }


  public String getSearchTerm() {
    return searchTerm;
  }

  public boolean hasExactMatch() {
    return hasExactMatch;
  }

  public Tag getExactMatch() {
    return exactMatch;
  }

  public int getAllMatchesCount() {
    return allMatches.size();
  }

  public Collection<Tag> getAllMatches() {
    return allMatches;
  }


  @Override
  public String toString() {
    return searchTerm + " has " + getAllMatchesCount() + " matches, hasExactMatch = " + hasExactMatch();
  }

}
