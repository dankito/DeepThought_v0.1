package com.j256.ormlite.jpa.configuration.reader.mock;

import com.j256.ormlite.db.BaseSqliteDatabaseType;

/**
 * Created by ganymed on 08/03/15.
 */
public class MockDatabaseType extends BaseSqliteDatabaseType {

  @Override
  protected String getDriverClassName() {
    return "Mock Driver";
  }

  @Override
  public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
    return false;
  }

  @Override
  public String getDatabaseName() {
    return "Mock DB";
  }

  @Override
  public boolean isEntityNamesMustBeUpCase() {
    return true;
  }

}
