package com.j256.ormlite.stmt.query;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.BaseCoreStmtTest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Oh yes it _is_ a test, just of the NOT operation.
 */
public class NotTest extends BaseCoreStmtTest {

	@Test(expected = IllegalArgumentException.class)
	public void test() {
		Not not = new Not();
		Clause clause = new Comparison() {
			public void appendOperation(StringBuilder sb) {
			}
			public void appendValue(DatabaseType databaseType, StringBuilder sb, List<ArgumentHolder> argList) {
			}
			public String getColumnName() {
				return null;
			}
			public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb,
					List<ArgumentHolder> argList) {
			}
		};
		not.setMissingClause(clause);
		not.setMissingClause(clause);
	}

	@Test(expected = IllegalStateException.class)
	public void testNoClause() throws Exception {
		Not not = new Not();
		not.appendSql(databaseType, null, new StringBuilder(), new ArrayList<ArgumentHolder>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBaseNotClause() {
		Not not = new Not();
		not.setMissingClause(new ManyClause((Clause) null, "AND"));
	}

	@Test
	public void testToString() throws Exception {
		String name = "foo";
		String value = "bar";
		SimpleComparison eq = new SimpleComparison(name, numberPropertyConfig, value, SimpleComparison.EQUAL_TO_OPERATION);
		Not not = new Not();
		assertTrue(not.toString().contains("NOT without comparison"));
		not.setMissingClause(eq);
		assertTrue(not.toString().contains("NOT comparison"));
		assertTrue(not.toString().contains(eq.toString()));
	}
}
