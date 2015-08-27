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
    EntityConfig[] entities = new JpaEntityConfigurationReader(connectionSource).readConfiguration(configuration.getEntityClasses());

    return entities;
  }
}
