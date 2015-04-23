package com.j256.ormlite.dao.cda.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by ganymed on 16/10/14.
 */
@Entity(name = "entry_keyword")
public class EntryKeywordJoinTable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @Column(name = "entry_id")
  protected Long entryId;

  @Column(name = "keyword_id")
  protected Long keywordId;


  public EntryKeywordJoinTable() {

  }

  public EntryKeywordJoinTable(Long entryId, Long keywordId) {
    this.entryId = entryId;
    this.keywordId = keywordId;
  }


  public Long getId() {
    return id;
  }

  public Long getEntryId() {
    return entryId;
  }

  public Long getKeywordId() {
    return keywordId;
  }
}
