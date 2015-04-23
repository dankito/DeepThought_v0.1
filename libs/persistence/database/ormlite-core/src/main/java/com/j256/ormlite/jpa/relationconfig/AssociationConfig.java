package com.j256.ormlite.jpa.relationconfig;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;

/**
 * Created by ganymed on 11/11/14.
 */
public abstract class AssociationConfig {

  protected boolean isBidirectional = false;

  protected FetchType fetch;
  protected CascadeType[] cascade;

  protected Boolean cascadePersist = null;
  protected Boolean cascadeRefresh = null;
  protected Boolean cascadeMerge = null; // TODO
  protected Boolean cascadeRemove = null;


  public AssociationConfig(FetchType fetch, CascadeType[] cascade) {
    this.fetch = fetch;
    this.cascade = cascade;
  }

  public AssociationConfig(FetchType fetch, CascadeType[] cascade, boolean isBidirectional) {
    this(fetch, cascade);
    this.isBidirectional = isBidirectional;
  }


  public boolean cascadePersist() {
    if(cascadePersist == null) {
      cascadePersist = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.PERSIST.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadePersist = true;
          break;
        }
      }
    }

    return cascadePersist;
  }

  public boolean cascadeRefresh() {
    if(cascadeRefresh == null) {
      cascadeRefresh = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.REFRESH.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadeRefresh = true;
          break;
        }
      }
    }

    return cascadeRefresh;
  }

  public boolean cascadeRemove() {
    if(cascadeRemove == null) {
      cascadeRemove = false;

      for(CascadeType enabledCascade : cascade) {
        if(CascadeType.REMOVE.equals(enabledCascade) || CascadeType.ALL.equals(enabledCascade)) {
          cascadeRemove = true;
          break;
        }
      }
    }

    return cascadeRemove;
  }

  public boolean isBidirectional() {
    return isBidirectional;
  }

  public FetchType getFetch() {
    return fetch;
  }

  public CascadeType[] getCascade() {
    return cascade;
  }

}
