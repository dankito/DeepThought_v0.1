package net.deepthought.data.persistence.db;

import net.deepthought.Application;
import net.deepthought.data.model.User;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

/**
 * Created by ganymed on 09/12/14.
 */
@MappedSuperclass
public class UserDataEntity extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.UserDataEntityCreatedByJoinColumnName)
  protected User createdBy = null;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.UserDataEntityModifiedByJoinColumnName)
  protected User modifiedBy = null;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.UserDataEntityDeletedByJoinColumnName)
  protected User deletedBy = null;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.UserDataEntityOwnerJoinColumnName)
  protected User owner = null;


  public User getCreatedBy() {
    return createdBy;
  }

  public User getModifiedBy() {
    return modifiedBy;
  }

  public User getDeletedBy() {
    return deletedBy;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }


  @Override
  protected void prePersist() {
    super.prePersist();

    createdBy = Application.getLoggedOnUser();
    modifiedBy = Application.getLoggedOnUser();

    if(owner == null) // if already set, don't overwrite
      owner = Application.getLoggedOnUser();
  }

  @Override
  protected void preUpdate() {
    super.preUpdate();

    if(Application.getLoggedOnUser() != null)
      modifiedBy = Application.getLoggedOnUser();
  }

  @Override
  public void setDeleted() {
    super.setDeleted();

    deletedBy = Application.getLoggedOnUser();
  }

}
