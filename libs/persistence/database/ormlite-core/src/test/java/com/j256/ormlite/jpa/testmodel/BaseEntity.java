package com.j256.ormlite.jpa.testmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * Created by ganymed on 12/10/14.
 */
@MappedSuperclass
public class BaseEntity implements Serializable {

  protected final static Logger log = LoggerFactory.getLogger(BaseEntity.class);


//  @JsonIgnore
  @Column(name = TableConfig.BaseEntityIdColumnName)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // Toplink cannot handle this in conjunction with SQlite (SEQ_GEN_IDENTITY: platform DatabasePlatform doesn't support NativeSequence.)
//  @GeneratedValue(strategy = GenerationType.TABLE)
  protected Long id;

//  @JsonIgnore
  @Column(name = TableConfig.BaseEntityCreatedOnColumnName/*, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"*/, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date createdOn;
//  @JsonIgnore
  @Column(name = TableConfig.BaseEntityModifiedOnColumnName/*, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"*/)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date modifiedOn;

//  @JsonIgnore
  @Version
  @Column(name = TableConfig.BaseEntityVersionColumnName, nullable = false, columnDefinition = "BIGINT DEFAULT 1")
  protected Long version;

//  @JsonIgnore
  @Column(name = TableConfig.BaseEntityDeletedColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
  protected boolean deleted = false;


  public BaseEntity() {
    createdOn = modifiedOn = new Date(); // to ensure they are not null
  }


  public Long getId() {
    return id;
  }

  /**
   * <p>
   *  We hope you really know what you are doing when calling this.
   *  We use this only when adding backed up or imported data to existing collection as Entity's ID from other collection may not be meaningful to this ones.
   * </p>
   */
  public void resetId() {
    id = null;
  }

  public Date getCreatedOn() {
    return new Date(createdOn.getTime()); // to avoid user can modify created Timestamp
  }

  public Date getModifiedOn() {
    return new Date(modifiedOn.getTime());
  }

  public Long getVersion() {
    return version;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted() {
    deleted = true;
  }


  @PrePersist
  protected void prePersist() {
    createdOn = new Date();
    modifiedOn = createdOn;
    version = 1L;
  }

  @PreUpdate
  protected void preUpdate() {
    modifiedOn = new Date();
  }

  @PreRemove
  protected void preRemove() {
    modifiedOn = new Date();
  }


  @Transient
  public String getTextRepresentation() {
    return "";
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }

  //  private final static Logger log = LoggerFactory.getLogger(BaseEntity.class);

//  @PostConstruct
//  protected void postConstructTest() {
//log.debug("PostConstruct {}", this);
//  }
//
//  @PostLoad
//  protected void postLoadTest() {
//    log.debug("PostLoad {}", this);
//  }

}
