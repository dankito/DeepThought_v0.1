package com.j256.ormlite.jpa.crud;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.h2.H2ConnectionSource;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.jpa.Registry;
import com.j256.ormlite.support.ConnectionSource;

import org.junit.After;
import org.junit.Before;

import java.sql.SQLException;

/**
 * Created by ganymed on 11/03/15.
 */
public class JpaCrudTestBase {

  protected static final String DEFAULT_DATABASE_URL = "jdbc:h2:mem:ormliteTestDb";


  protected JpaEntityConfigurationReader entityConfigurationReader = null;

  protected ConnectionSource connectionSource = null;


  @Before
  public void setup() throws SQLException {
    Registry.getEntityRegistry().clear();
    Registry.getPropertyRegistry().clear();

//    this.connectionSource = new MockConnectionSource();
    this.connectionSource = new H2ConnectionSource(DEFAULT_DATABASE_URL);
    entityConfigurationReader = new JpaEntityConfigurationReader(this.connectionSource);
  }

  @After
  public void tearDown() throws Exception {
    connectionSource.close();
    connectionSource = null;
    Instances.getDaoManager().clearCache(); // cda
  }


  protected Dao buildConfigurationAndGetDao(Class... testClasses) throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(testClasses);
    return entities[0].getDao();
  }
}
