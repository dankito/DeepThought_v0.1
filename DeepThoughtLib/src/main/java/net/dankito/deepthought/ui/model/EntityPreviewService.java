package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.listener.IEntityChangesService;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.localization.LanguageChangedListener;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;

/**
 * Created by ganymed on 21/09/16.
 */
public class EntityPreviewService implements IEntityPreviewService {

  protected EntryPreviewService entryPreviewService;

  protected TagPreviewService tagPreviewService;

  protected ReferenceBasePreviewService referenceBasePreviewService;

  protected PersonPreviewService personPreviewService;


  public EntityPreviewService(IEntityChangesService changesService, IHtmlHelper htmlHelper) {
    this.tagPreviewService = new TagPreviewService();
    this.personPreviewService = new PersonPreviewService();
    this.referenceBasePreviewService = new ReferenceBasePreviewService(personPreviewService);
    this.entryPreviewService = new EntryPreviewService(tagPreviewService, referenceBasePreviewService, personPreviewService, htmlHelper);

    Localization.addLanguageChangedListener(languageChangedListener);
    changesService.addAllEntitiesListener(allEntitiesListener);
  }


  @Override
  public void cleanUp() {
    Localization.removeLanguageChangedListener(languageChangedListener);
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


  @Override
  public String getTagsPreview(Entry entry) {
    return entryPreviewService.getTagsPreview(entry);
  }

  @Override
  public String getTagsPreview(Collection<Tag> tags, boolean showNoTagsSetMessage) {
    return tagPreviewService.createTagsPreview(tags, showNoTagsSetMessage);
  }


  @Override
  public String getReferenceBaseUrl(ReferenceBase referenceBase) {
    return referenceBasePreviewService.getReferenceBaseUrl(referenceBase);
  }


  protected LanguageChangedListener languageChangedListener = new LanguageChangedListener() {
    @Override
    public void languageChanged(ApplicationLanguage newLanguage) {
      EntityPreviewService.this.languageChanged();
    }
  };

  protected void languageChanged() {
    entryPreviewService.languageChanged();
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
