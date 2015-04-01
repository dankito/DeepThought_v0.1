package com.j256.ormlite.db;

import com.j256.ormlite.TestUtils;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.support.DatabaseConnection;

import org.junit.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class H2DatabaseTypeTest extends BaseJdbcDatabaseTypeTest {

	private final static String DATABASE_NAME = "ormlite";
	private final String DB_DIRECTORY = "target/" + getClass().getSimpleName();

	@Override
	protected void setDatabaseParams() {
		databaseUrl = "jdbc:h2:mem:" + DATABASE_NAME;
		databaseType = new H2DatabaseType();
	}

	@Test(expected = SQLException.class)
	public void testGeneratedIdSequenceNotSupported() throws Exception {
		EntityConfig<GeneratedIdSequence, Integer> tableInfo =
				new EntityConfig<GeneratedIdSequence, Integer>(connectionSource, null, GeneratedIdSequence.class);
		assertEquals(2, tableInfo.getPropertyConfigs().length);
		StringBuilder sb = new StringBuilder();
		ArrayList<String> additionalArgs = new ArrayList<String>();
		ArrayList<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(null, sb, tableInfo.getPropertyConfigs()[0], additionalArgs, statementsBefore, null,
				null);
	}

//	@Test
//	public void testUsernamePassword() throws Exception {
//		closeConnectionSource();
//		databaseType = new DerbyEmbeddedDatabaseType();
//	}

	@Test(expected = SQLException.class)
	public void testRemotePort() throws Exception {
		File dbDir = new File(DB_DIRECTORY);
		TestUtils.deleteDirectory(dbDir);
		dbDir.mkdirs();
		// bad port
		int notTheRightPort = 12345;
		closeConnectionSource();
		// try to disable the retry feature which delays this test failure
		System.setProperty("h2.socketConnectRetry", "0");
		String dbUrl = "jdbc:h2:tcp://localhost:" + notTheRightPort + "/" + dbDir.getPath() + "/" + DATABASE_NAME;
		connectionSource = new JdbcConnectionSource(dbUrl);
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		try {
			DatabaseTypeUtils.createDatabaseType(dbUrl);
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}
}
