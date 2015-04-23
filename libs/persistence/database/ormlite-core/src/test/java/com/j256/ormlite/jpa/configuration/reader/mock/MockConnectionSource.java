package com.j256.ormlite.jpa.configuration.reader.mock;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.BaseConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by ganymed on 08/03/15.
 */
public class MockConnectionSource extends BaseConnectionSource {

  private final DatabaseType databaseType = new MockDatabaseType();


  @Override
  public DatabaseConnection getReadOnlyConnection() throws SQLException {
    return null;
  }

  @Override
  public DatabaseConnection getReadWriteConnection() throws SQLException {
    return null;
  }

  @Override
  public void releaseConnection(DatabaseConnection connection) throws SQLException {

  }

  @Override
  public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
    return false;
  }

  @Override
  public void clearSpecialConnection(DatabaseConnection connection) {

  }

  @Override
  public void closeQuietly() {

  }

  @Override
  public DatabaseType getDatabaseType() {
    return this.databaseType;
  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public void close() throws IOException {

  }
}
