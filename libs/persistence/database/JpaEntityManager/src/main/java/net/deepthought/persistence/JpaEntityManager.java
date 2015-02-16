package net.deepthought.persistence;

import net.deepthought.Application;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;

import org.rhq.enterprise.server.safeinvoker.HibernateDetachUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * Created by ganymed on 02/01/15.
 */
public class JpaEntityManager implements IEntityManager {

  private final static Logger log = LoggerFactory.getLogger(JpaEntityManager.class);


  protected EntityManagerFactory entityManagerFactory = null;
  protected EntityManager entityManager;

  protected String databasePath;


  public JpaEntityManager() throws Exception {
    this(null);
  }

  public JpaEntityManager(EntityManagerConfiguration configuration) throws Exception {
    try {
      entityManagerFactory = Persistence.createEntityManagerFactory("deep_thought", configuration.getEntityManagerConfiguration());

      entityManager = entityManagerFactory.createEntityManager();

      extractDatabasePath(entityManagerFactory.getProperties());
    } catch(Exception ex) {
      log.error("Could not create EntityManagerFactory", ex);
      throw ex;
    }
  }

  protected void extractDatabasePath(Map<String, Object> entityManagerFactoryProperties) {
    this.databasePath = (String)entityManagerFactoryProperties.get("javax.persistence.jdbc.url");
    if(databasePath == null)
      databasePath = (String)entityManagerFactoryProperties.get("eclipselink.jdbc.url");

    if(databasePath != null) {
      boolean isH2Database = databasePath.toLowerCase().contains("h2");

      if(databasePath.contains(":")) // cut off first ':' (e.g. 'jdbc:')
        databasePath = databasePath.substring(databasePath.indexOf(":") + 1);
      if(databasePath.contains(":")) // cut off second ':' (e.g. 'derby:' or 'sqlite:')
        databasePath = databasePath.substring(databasePath.indexOf(":") + 1);

      if(databasePath.contains(";create=true"))
        databasePath = databasePath.replace(";create=true", "");

      if(databasePath.contains(";IFEXISTS=TRUE"))
        databasePath = databasePath.replace(";IFEXISTS=TRUE", "");

      if(databasePath.startsWith("./"))
        databasePath = databasePath.substring(2);

      if(isH2Database == true)
        databasePath += ".mv.db"; // H2 adds a '.mv.db' at end of database filename
    }
  }


  @Override
  public String getDatabasePath() {
    return databasePath;
  }

  public boolean persistEntity(BaseEntity entity) {
    synchronized(entityManager) {
      log.debug("persistEntity has been called for {}", entity);

      // TODO: why do i need a transaction in order that entity gets persisted immediately / an id is created?
      EntityTransaction transaction = null;
      try {
        transaction = entityManager.getTransaction();
        if (transaction.isActive() == false)
          transaction.begin();

        entityManager.persist(entity);
//    entityManager.flush();
        transaction.commit();
        return true;
      } catch (Exception ex) {
        log.error("Could not persist entity " + entity, ex);
        try {
          if (transaction != null && transaction.isActive())
            transaction.rollback();
        } catch (Exception ex2) {
          log.error("Could not rollback transaction", ex);
        }
        return false;
      }
    }
  }

  public boolean updateEntity(BaseEntity entity) {
    synchronized(entityManager) {
      log.debug("updateEntity has been called for {}", entity);

      try {
        EntityTransaction transaction = entityManager.getTransaction();
        if (transaction.isActive() == false)
          transaction.begin();

        try {
          entityManager.merge(entity);
          //    entityManager.flush();
          transaction.commit();
          return true;
        } catch (Exception ex) {
          log.error("Could not persist entity " + entity + ", rolling back ...", ex);
          try {
            if (transaction.isActive())
              transaction.rollback();
          } catch (Exception ex2) {
            log.error("Could not rollback transaction", ex);
          }
        }
      } catch (Exception ex) {
        log.error("Could not persisted updated entity " + entity, ex);
      }
    }

    return false;
  }

  public boolean updateEntities(List<BaseEntity> entities) {
    boolean success = true;

    synchronized(entityManager) {
      log.debug("updateEntities has been called for {}", entities);

      try {
        EntityTransaction transaction = entityManager.getTransaction();
        if (transaction.isActive() == false)
          transaction.begin();

        try {
          for (BaseEntity entity : entities) {
            try {
              entityManager.merge(entity);
            } catch (Exception ex) {
              log.error("Could not persist entity " + entity, ex);
            }
          }

          //    entityManager.flush();
          transaction.commit();
        } catch (Exception ex) {
          log.error("Could not persist entities, rolling back ...", ex);
          success = false;
          try {
            if (transaction.isActive())
              transaction.rollback();
          } catch (Exception ex2) {
            log.error("Could not rollback transaction", ex);
          }
        }
      } catch (Exception ex) {
        log.error("Could not persisted updated entities", ex);
      }
    }

    return success;
  }

  public boolean deleteEntity(BaseEntity entity) {
    synchronized(entityManager) {
      log.debug("deleteEntity has been called for {}", entity);

      try {
        EntityTransaction transaction = entityManager.getTransaction();
        if (transaction.isActive() == false)
          transaction.begin();

        try {
          entityManager.remove(entity);
          //    entityManager.flush();
          transaction.commit();
          return true;
        } catch (Exception ex) {
          log.error("Could not remove entity " + entity + ", rolling back ...", ex);
          try {
            if (transaction.isActive())
              transaction.rollback();
          } catch (Exception ex2) {
            log.error("Could not rollback transaction", ex);
          }
        }
      } catch (Exception ex) {
        log.error("Could not remove entity " + entity, ex);
      }
    }

    return false;
  }

  @Override
  public <T extends BaseEntity> T getEntityById(Class<T> type, Long id) {
//    Query query = entityManager.createNativeQuery("select * from " + TableConfig.getTableNameForClass(type) + " where deleted=0 AND id=" + id, type);
    Query query = entityManager.createNativeQuery("select * from " + TableConfig.getTableNameForClass(type) + " where id=" + id, type);
    List<T> queryResult = query.getResultList();

    if(queryResult.size() == 0)
      return null;
    return queryResult.get(0);
  }

  @Override
  public <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type) {
//    Query query = entityManager.createQuery("from " + TableConfig.UserTableName + " user where user.autoLogon = 1");
//    List<User> autoLogonUsers = query.getResultList();
//    Query query = entityManager.createQuery("from " + TableConfig.AppSettingsTableName + " appSettings");
    Query query = entityManager.createNativeQuery("select * from " + TableConfig.getTableNameForClass(type) + " where deleted=0", type);
//      Query query = entityManager.createQuery("select entity from " + getTableNameForClass(type) + " entity");
    List<T> queryResult = query.getResultList();

    // TODO: move to DefaultDataManager
    if(type.equals(DeepThoughtApplication.class) && queryResult.size() > 0) {
      DeepThoughtApplication application = (DeepThoughtApplication)queryResult.get(0);
      if(application.getDataModelVersion() < Application.CurrentDataModelVersion)
        DatabaseUpdateManager.updateDatabaseToCurrentVersion(entityManager, application);
    }

    return queryResult;
  }

  public void resolveAllLazyRelations(BaseEntity entity) throws Exception {
    // TODO:
    HibernateDetachUtility.nullOutUninitializedFields(entity, HibernateDetachUtility.SerializationType.SERIALIZATION);
  }

  @Override
  public List doNativeQuery(String query) {
    return entityManager.createNativeQuery(query).getResultList();
  }

  public int doNativeExecute(String statement) {
    int result = 0;

    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();

    try {
      result = entityManager.createNativeQuery(statement).executeUpdate();

      transaction.commit();
    } catch(Exception ex) {
      log.error("Could not execute statement " + statement, ex);

      if(transaction.isActive()) {
        try {
          transaction.rollback();
        } catch(Exception ex2) {
          log.error("Could not rollback transaction", ex2);
        }
      }
    }
    finally {
      transaction = null;
    }

    return result;
  }

  public List<Tag> filterTags(String filterConstraint) {
    return entityManager.createQuery("from tag where lower(title) like '%" + filterConstraint.toLowerCase() + "%'").getResultList();
  }

  @Override
  public void close() {
    if(entityManager != null)
      entityManager.close();
    entityManagerFactory.close();
  }

}
