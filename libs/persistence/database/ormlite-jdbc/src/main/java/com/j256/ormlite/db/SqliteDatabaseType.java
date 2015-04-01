package com.j256.ormlite.db;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

/**
 * Sqlite database type information used to create the tables, etc..
 * 
 * @author graywatson
 */
public class SqliteDatabaseType extends BaseSqliteDatabaseType {

	private final static String DATABASE_URL_PORTION = "sqlite";
	private final static String DRIVER_CLASS_NAME = "org.sqlite.JDBC";
	private final static String DATABASE_NAME = "SQLite";
	private final static String XERIAL_DRIVER_CLASS = "org.sqlite.Interpreter";
  private final static String XERIAL_DRIVER_CLASS_NEW = "org.sqlite.JDBC";

	private static final Logger logger = LoggerFactory.getLogger(SqliteDatabaseType.class);

	public SqliteDatabaseType() {
	}

	public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
		return DATABASE_URL_PORTION.equals(dbTypePart);
	}

	@Override
	protected String getDriverClassName() {
    boolean isXerialDriverAvailable = false;

		try {
			// make sure we are using the Xerial driver
			Class.forName(XERIAL_DRIVER_CLASS);
      isXerialDriverAvailable = true;
		} catch (Exception e) { }

    if(isXerialDriverAvailable == false) {
      try {
        Class.forName(XERIAL_DRIVER_CLASS_NEW);
        isXerialDriverAvailable = true;
      } catch (Exception e) { }
    }

    if(isXerialDriverAvailable == false)
      logger.error("WARNING: you seem to not be using the Xerial SQLite driver.  "
          + "See ORMLite docs on SQLite: http://ormlite.com/docs/sqlite");

		return DRIVER_CLASS_NAME;
	}

	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	@Override
	public void appendLimitValue(StringBuilder sb, long limit, Long offset) {
		sb.append("LIMIT ");
		if (offset != null) {
			sb.append(offset).append(',');
		}
		sb.append(limit).append(' ');
	}

	@Override
	public boolean isOffsetLimitArgument() {
		return true;
	}

	@Override
	public boolean isNestedSavePointsSupported() {
		return false;
	}

	@Override
	public void appendOffsetValue(StringBuilder sb, long offset) {
		throw new IllegalStateException("Offset is part of the LIMIT in database type " + getClass());
	}
}
