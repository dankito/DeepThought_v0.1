package net.dankito.deepthought.data.persistence.db;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.listener.EntityListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

  @PostLoad
  protected void postLoad() {
    if(Application.getDataManager() != null)
      Application.getDataManager().lazyLoadedEntityMapped(this);
  }

  @PostPersist
  protected void postPersist() {
    if(Application.getDataManager() != null)
      Application.getDataManager().lazyLoadedEntityMapped(this);
  }


  @Transient
  public String getTextRepresentation() {
    return "";
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }


  protected transient Set<EntityListener> listeners = new CopyOnWriteArraySet<>();

  public boolean addEntityListener(EntityListener listener) {
    return listeners.add(listener);
  }

  public boolean removeEntityListener(EntityListener listener) {
    return listeners.remove(listener);
  }

  protected void callPropertyChangedListeners(String propertyName, Object previousValue, Object newValue) {
    callPropertyChangedListeners(this, propertyName, previousValue, newValue);
  }

  protected void callPropertyChangedListeners(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
    if((newValue != null && newValue.equals(previousValue) == false) || (previousValue != null && previousValue.equals(newValue) == false)) {
      log.debug("Property {} changed to {}", propertyName, newValue);
      for (EntityListener listener : listeners)
        listener.propertyChanged(entity, propertyName, previousValue, newValue);
    }
  }

  protected void callEntityAddedListeners(Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
    callEntityAddedListeners(this, collection, addedEntity);
  }

  protected void callEntityAddedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
    for(EntityListener listener : listeners)
      listener.entityAddedToCollection(collectionHolder, collection, addedEntity);
  }

  protected void callEntityOfCollectionUpdatedListeners(Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
    callEntityOfCollectionUpdatedListeners(this, collection, updatedEntity);
  }

  protected void callEntityOfCollectionUpdatedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
    for(EntityListener listener : listeners)
      listener.entityOfCollectionUpdated(collectionHolder, collection, addedEntity);
  }

  protected void callEntityRemovedListeners(Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
    callEntityRemovedListeners(this, collection, removedEntity);
  }

  protected void callEntityRemovedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
    for(EntityListener listener : listeners)
      listener.entityRemovedFromCollection(collectionHolder, collection, removedEntity);
  }

  @Transient
  public boolean isPersisted() {
    return id != null;
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
