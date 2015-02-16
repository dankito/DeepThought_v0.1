//package net.deepthought.persistence;
//
//import AppSettings;
//import Category;
//import DeepThought;
//import EntryFragment;
//import Keyword;
//import Tag;
//import User;
//import CategoriesChangedListener;
//import DeepThoughtListener;
//import EntityListener;
//import EntryFragmentsChangedListener;
//import KeywordsChangedListener;
//import TagsChangedListener;
//import BaseEntity;
//import IDataBaseDeepThoughtPersistenceManager;
//import TableConfig;
//import net.deepthought.query.IDeepThoughtQuery;
//import net.deepthought.util.DeepThoughtProperties;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.persistence.EntityTransaction;
//import javax.persistence.Query;
//
//// To find out when a lazy loading entity has been loaded,
//// - You can define a lot of different listeners for Hibernate, see for example package org.hibernate.event (http://docs.jboss.org/hibernate/orm/3.5/javadocs/org/hibernate/event/package-summary.html)
////   (see for example https://docs.jboss.org/hibernate/validator/5.1/reference/en-US/html/validator-integration.html or http://kurtstam.blogspot.de/2008/10/hibernate-interceptors-events-and-jpa.html)
//// - As well you can define JPA EntityListeners to *may) find out when a lazy loading entity gets loaded (http://www.objectdb.com/java/jpa/persistence/event)
//public class DeepThoughtPersistenceManager implements IDataBaseDeepThoughtPersistenceManager, IDeepThoughtQuery {
//
//  private final static Logger log = LoggerFactory.getLogger(DeepThoughtPersistenceManager.class);
//
//
//  protected IEntityManager entityManager = null;
//
//
//  public DeepThoughtPersistenceManager(IEntityManager entityManager) {
//    this.entityManager = entityManager;
//  }
//
//  @Override
//  public void deserializeDeepThoughtAsync(final DeserializeDeepThoughtResult result) {
//    new Thread(new Runnable() {
//      @Override
//      public void run() {
//        try {
//          result.result(true, null, deserializeDeepThought());
//        } catch(Exception ex) {
//          log.error("Could not deserialize DeepThought", ex);
//          result.result(false, ex, null);
//        }
//      }
//    });
//  }
//
//  @Override
//  public DeepThought deserializeDeepThought() throws Exception {
//    if(currentDeepThought != null) // may remove again; this is only for HibernatePersistenceTests as these call deserializeDeepThought() twice, therefore listeners are added twice
//      return currentDeepThought;
//
//    try {
//      List<AppSettings> appSettingsQueryResult = entityManager.getAllEntitiesOfType(AppSettings.class);
//
//      if (appSettingsQueryResult.size() > 0) {
//        AppSettings appSettings = appSettingsQueryResult.get(0);
//        if(appSettings.getDataModelVersion() < CurrentDatabaseVersion)
//          updateDatabaseToCurrentVersion(appSettings, CurrentDatabaseVersion);
//
//        User lastLoggedOnUser = appSettings.getLastLoggedOnUser();
//
//        if (appSettings.autoLogOnLastLoggedOnUser()) {
//          User.setCurrentlyLoggedOnUser(lastLoggedOnUser);
//
//          DeepThought deepThought = lastLoggedOnUser.getLastViewedDeepThought();
//          setCurrentDeepThought(deepThought);
//          return deepThought;
//        }
//        // TODO: what to return if user was already logged on but autoLogOn is set to false?
//      }
//    } catch(Exception ex) {
//      String dummy = ex.getMessage();
//      log.error("Could not deserialize DeepThought", ex);
//    }
//
//    return createAndPersistDefaultDeepThought();
//  }
//
//  protected DeepThought createAndPersistDefaultDeepThought() throws Exception {
//    // TODO: only if it doesn't already exist!
//    DeepThoughtProperties.saveDefaultValuesIfPropertiesDontExist();
//
//    User defaultLocalUser = User.createNewLocalUser();
//    entityCreated(defaultLocalUser);
//
//    AppSettings appSettings = new AppSettings(CurrentDatabaseVersion, defaultLocalUser, true);
//    entityCreated(appSettings);
//
//    DeepThought newDeepThought = new ArrayList<>(defaultLocalUser.getDeepThought()).get(0);
//    setCurrentDeepThought(newDeepThought);
//
//    return newDeepThought;
//  }
//
//  protected void setCurrentDeepThought(DeepThought deepThought) {
//    if(currentDeepThought != null)
//      removeDeepThoughtListeners(currentDeepThought);
//
//    currentDeepThought = deepThought;
//
//    setDeepThoughtListeners(currentDeepThought);
//  }
//
//  protected void setDeepThoughtListeners(DeepThought deepThought) {
//    deepThought.addDeepThoughtListener(deepThoughtListener);
//    deepThought.addEntityListener(entityListener);
////    deepThought.addCategoriesChangedListener(categoriesChangedListener);
////    deepThought.addEntriesChangedListener(entryFragmentsChangedListener);
////    deepThought.addTagsChangedListener(tagsChangedListener);
////    deepThought.addIndexTermsChangedListener(keywordsChangedListener);
//  }
//
//  protected void removeDeepThoughtListeners(DeepThought deepThought) {
//    deepThought.addDeepThoughtListener(deepThoughtListener);
//    deepThought.removeEntityListener(entityListener);
////    deepThought.removeCategoriesChangedListener(categoriesChangedListener);
////    deepThought.removeEntriesChangedListener(entryFragmentsChangedListener);
////    deepThought.removeTagsChangedListener(tagsChangedListener);
////    deepThought.removeIndexTermsChangedListener(keywordsChangedListener);
//  }
//
//  @Override
//  public boolean persistDeepThought(DeepThought deepThought) throws Exception {
//    return entityCreated(deepThought);
//  }
//
////  @Override
////  public List<EntryTemplate> getAllEntryTemplates() {
////    Query query = entityManager.createQuery("from " + TableConfig.EntryTemplateTableName + " entryTemplate");
////    return  query.getResultList();
////  }
////
////  @Override
////  public boolean addEntryTemplate(EntryTemplate entryTemplate) {
////    return entityCreated(entryTemplate);
////  }
//
//
//  protected DeepThoughtListener deepThoughtListener = new DeepThoughtListener() {
//    @Override
//    public void propertyChanged(DeepThought entity, String propertyName, Object newValue, Object previewValue) {
//      entityUpdated(entity);
//    }
//  };
//
//  protected EntityListener entityListener = new EntityListener() {
//    @Override
//    public void entityAdded(BaseEntity entity) {
//      entityCreated(entity);
//    }
//
//    @Override
//    public void entityUpdated(BaseEntity entity) {
//      entityUpdated(entity);
//    }
//
//    @Override
//    public void entityRemoved(BaseEntity entity) {
//      entityDeleted(entity);
//    }
//  };
//
//  protected CategoriesChangedListener categoriesChangedListener = new CategoriesChangedListener() {
//    @Override
//    public void categoryAdded(Category category) {
//      entityCreated(category);
//    }
//
//    @Override
//    public void categoryUpdated(Category category) {
//      entityUpdated(category);
//    }
//
//    @Override
//    public void categoryRemoved(Category category) {
//      entityDeleted(category);
//    }
//  };
//
//  protected EntryFragmentsChangedListener entryFragmentsChangedListener = new EntryFragmentsChangedListener() {
//    @Override
//    public void entryAdded(EntryFragment entryFragment) {
//      entityCreated(entryFragment);
//    }
//
//    @Override
//    public void entryUpdated(EntryFragment entryFragment) {
//      entityUpdated(entryFragment);
//    }
//
//    @Override
//    public void entryRemoved(EntryFragment entryFragment) {
//      entityDeleted(entryFragment);
//    }
//  };
//
//  protected TagsChangedListener tagsChangedListener = new TagsChangedListener() {
//    @Override
//    public void tagAdded(Tag tag) {
//      entityCreated(tag);
//    }
//
//    @Override
//    public void tagUpdated(Tag tag) {
//      entityUpdated(tag);
//    }
//
//    @Override
//    public void tagRemoved(Tag tag) {
//      entityDeleted(tag);
//    }
//  };
//
//  protected KeywordsChangedListener keywordsChangedListener = new KeywordsChangedListener() {
//    @Override
//    public void indexTermAdded(Keyword keyword) {
//      entityCreated(keyword);
//    }
//
//    @Override
//    public void indexTermUpdated(Keyword keyword) {
//      entityUpdated(keyword);
//    }
//
//    @Override
//    public void indexTermRemoved(Keyword keyword) {
//      entityDeleted(keyword);
//    }
//  };
//
//}
