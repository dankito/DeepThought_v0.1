package com.j256.ormlite.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Utility class to help with SQLException throwing.
 * 
 * @author graywatson
 */
public class SqlExceptionUtil {

  private final static Logger log = LoggerFactory.getLogger(SqlExceptionUtil.class);

	/**
	 * Should be used in a static context only.
	 */
	private SqlExceptionUtil() {
	}

	/**
	 * Convenience method to allow a cause. Grrrr.
	 */
	public static SQLException create(String message, Throwable cause) {
    log.error("Catch exception: " + message, cause);
		SQLException sqlException = new SQLException(message);
		sqlException.initCause(cause);
		return sqlException;
	}
}
