package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.listener.IEntityChangesService;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 21/09/16.
 */
public class EntityPreviewService implements IEntityPreviewService {

  protected EntryPreviewService entryPreviewService;


  public EntityPreviewService(IEntityChangesService changesService, IHtmlHelper htmlHelper) {
    this.entryPreviewService = new EntryPreviewService(htmlHelper);

    changesService.addAllEntitiesListener(allEntitiesListener);
  }


  @Override
  public String getReferenceOrPersonsPreview(Entry entry) {
    return entryPreviewService.getReferenceOrPersonsPreview(entry);
  }

  @Override
  public String getReferencePreview(Entry entry) {
    return entryPreviewService.getReferencePreview(entry);
  }

  @Override
  public String getPersonsPreview(Entry entry) {
    return entryPreviewService.getLongPersonsPreview(entry);
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {

    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      EntityPreviewService.this.entityUpdated(entity, propertyName);
    }

    @Override
    public void entityDeleted(BaseEntity entity) {
      EntityPreviewService.this.entityDeleted(entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      collectionOfEntityUpdated(collectionHolder, addedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      collectionOfEntityUpdated(collectionHolder, removedEntity);
    }
  };


  protected void entityUpdated(BaseEntity entity, String propertyName) {
    entryPreviewService.entityUpdated(entity, propertyName);
  }


  protected void entityDeleted(BaseEntity entity) {
    entryPreviewService.entityDeleted(entity);
  }

  protected void collectionOfEntityUpdated(BaseEntity collectionHolder, BaseEntity addedOrRemovedEntity) {
    entryPreviewService.collectionOfEntityUpdated(collectionHolder, addedOrRemovedEntity);
  }

}
