package net.dankito.deepthought.adapter;

import android.app.Activity;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;

import java.util.Collection;

/**
 * Created by ganymed on 21/09/16.
 */
public abstract class AsyncLoadingEntityAdapter extends AsyncLoadingAdapter implements ICleanUp {

  public AsyncLoadingEntityAdapter(Activity context, int listItemLayoutId) {
    super(context, listItemLayoutId);

    Application.addApplicationListener(applicationListener);

    if(Application.isInstantiated()) {
      addAllEntitiesListener();
      applicationInstantiated();
    }

    if(Application.getDeepThought() != null) {
      deepThoughtChanged(Application.getDeepThought());
    }
  }

  @Override
  public void cleanUp() {
    Application.removeApplicationListener(applicationListener);

    Application.getEntityChangesService().removeAllEntitiesListener(allEntitiesListener);
  }


  protected void deepThoughtChanged(DeepThought deepThought) {

  }

  protected void applicationInstantiated() {

  }


  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      AsyncLoadingEntityAdapter.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {
      if(notification.getType() == NotificationType.ApplicationInstantiated) {
        addAllEntitiesListener();
        applicationInstantiated();
      }
    }
  };

  protected void addAllEntitiesListener() {
    Application.getEntityChangesService().addAllEntitiesListener(allEntitiesListener);
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {
      checkIfRelevantEntityHasChanged(entity);
    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      checkIfRelevantEntityHasChanged(entity);
    }

    @Override
    public void entityDeleted(BaseEntity entity) {
      checkIfRelevantEntityHasChanged(entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      checkIfRelevantEntityOfCollectionHasChanged(collectionHolder, addedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfRelevantEntityOfCollectionHasChanged(collectionHolder, removedEntity);
    }
  };

  protected void checkIfRelevantEntityOfCollectionHasChanged(BaseEntity collectionHolder, BaseEntity changedEntity) {
    if(collectionHolder instanceof DeepThought) {
      checkIfRelevantEntityHasChanged(changedEntity);
    }
    else {
      checkIfRelevantEntityHasChanged(collectionHolder);
    }
  }

  protected abstract void checkIfRelevantEntityHasChanged(BaseEntity entity);

}
