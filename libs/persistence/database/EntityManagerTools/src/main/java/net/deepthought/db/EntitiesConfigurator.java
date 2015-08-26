package net.deepthought.db;

import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.support.ConnectionSource;

import net.deepthought.data.persistence.EntityManagerConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by ganymed on 26/08/15.
 */
public class EntitiesConfigurator {

  private final static Logger log = LoggerFactory.getLogger(EntitiesConfigurator.class);


  public EntityConfig[] readEntityConfiguration(EntityManagerConfiguration configuration, ConnectionSource connectionSource) throws SQLException {
//    String storedEntityConfiguration = Application.getPreferencesStore().getDataModel();
//    if (IPreferencesStore.DefaultDataModelString.equals(storedEntityConfiguration) == false) {
//      log.debug("Trying to deserialize stored Entities Configuration ...");
//      try {
//        return (EntityConfig[]) ConfigurationReader.jsonToJava(storedEntityConfiguration, connectionSource);
//      } catch(Exception ex) {
//        log.error("Could not deserialize stored Entities Configuration", ex); }
//    }

//    EntityConfig[] entities = new JpaEntityConfigurationReader(connectionSource).readConfigurationAndCreateTablesIfNotExists(configuration.getEntityClasses());
    EntityConfig[] entities = new JpaEntityConfigurationReader(connectionSource).readConfiguration(configuration.getEntityClasses());

//    try {
//      String json = ConfigurationWriter.objectToJson(entities);
//      if(json != null) {
//        Application.getPreferencesStore().setDataModel(json);
//      }
//    } catch(Exception ex) {
//      log.error("Could not read json string", ex);
//    }

    return entities;
  }
}
