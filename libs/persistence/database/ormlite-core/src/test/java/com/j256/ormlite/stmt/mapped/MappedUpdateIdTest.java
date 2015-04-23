package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.support.ConnectionSource;

import org.junit.Test;

import java.sql.SQLException;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class MappedUpdateIdTest {

	private final DatabaseType databaseType = new StubDatabaseType();
	private final ConnectionSource connectionSource;

	{
		connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(connectionSource);
	}

	@Test(expected = SQLException.class)
	public void testUpdateIdNoId() throws Exception {
		MappedUpdateId.build(databaseType, new EntityConfig<NoId, Void>(connectionSource, null, NoId.class));
	}

	protected static class NoId {
		@DatabaseField
		String id;
	}

	private static class StubDatabaseType extends BaseDatabaseType {
		@Override
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDatabaseName() {
			return "fake";
		}
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
	}
}
