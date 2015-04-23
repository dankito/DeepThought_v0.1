package com.j256.ormlite.stmt.query;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.BaseCoreStmtTest;
import com.j256.ormlite.stmt.SelectArg;

import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BaseComparisonTest extends BaseCoreStmtTest {

	private static final String INT_COLUMN_NAME = "column1";
	private static final String STRING_COLUMN_NAME = "column2";

	private BaseComparison cmpInt;
	private BaseComparison cmpString;
	private BaseComparison cmpForeign;

	{
		try {
			cmpInt = new BaseComparison(INT_COLUMN_NAME, numberPropertyConfig, 10L, true) {
				@Override
				public void appendOperation(StringBuilder sb) {
					sb.append("op");
				}
			};
			cmpString = new BaseComparison(STRING_COLUMN_NAME, stringPropertyConfig, 10L, true) {
				@Override
				public void appendOperation(StringBuilder sb) {
					sb.append("op");
				}
			};
			cmpForeign = new BaseComparison(INT_COLUMN_NAME, foreignPropertyConfig, 10L, true) {
				@Override
				public void appendOperation(StringBuilder sb) {
					sb.append("op");
				}
			};
		} catch (SQLException e) {
			fail("Could not creat our test comparisons");
		}
	}

	@Test(expected = SQLException.class)
	public void testAppendArgOrValueNull() throws Exception {
		cmpInt.appendArgOrValue(null, numberPropertyConfig, new StringBuilder(), new ArrayList<ArgumentHolder>(), null);
	}

	@Test
	public void testAppendArgOrValueLong() throws SQLException {
		long value = 23213L;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, numberPropertyConfig, sb, new ArrayList<ArgumentHolder>(), value);
		assertEquals(Long.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueInteger() throws SQLException {
		int value = 23213;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, numberPropertyConfig, sb, new ArrayList<ArgumentHolder>(), value);
		assertEquals(Integer.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueShort() throws SQLException {
		short value = 23213;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, numberPropertyConfig, sb, new ArrayList<ArgumentHolder>(), value);
		assertEquals(Short.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueString() throws SQLException {
		String value = "23wbdqwbdq13";
		StringBuilder sb = new StringBuilder();
		DatabaseType databaseType = createMock(DatabaseType.class);
		databaseType.appendEscapedWord(sb, value);
		replay(databaseType);
		cmpString.appendArgOrValue(databaseType, stringPropertyConfig, sb, new ArrayList<ArgumentHolder>(), value);
		verify(databaseType);
	}

	@Test
	public void testAppendArgOrValueSelectArg() throws SQLException {
		SelectArg value = new SelectArg();
		StringBuilder sb = new StringBuilder();
		List<ArgumentHolder> argList = new ArrayList<ArgumentHolder>();
		cmpInt.appendArgOrValue(null, numberPropertyConfig, sb, argList, value);
		assertEquals(1, argList.size());
		assertEquals(INT_COLUMN_NAME, value.getColumnName());
	}

	@Test
	public void testForeignId() throws SQLException {
		StringBuilder sb = new StringBuilder();
		Foo baseFoo = new Foo();
		cmpForeign.appendArgOrValue(databaseType, foreignPropertyConfig, sb, new ArrayList<ArgumentHolder>(), baseFoo);
		StringBuilder expectSb = new StringBuilder();
		expectSb.append(baseFoo.id);
		expectSb.append(' ');
		assertEquals(expectSb.toString(), sb.toString());
	}

	@Test(expected = SQLException.class)
	public void testForeignIdNull() throws Exception {
		StringBuilder sb = new StringBuilder();
		Field field = ForeignNull.class.getDeclaredField("foreign");
		PropertyConfig propertyConfig = Instances.getFieldTypeCreator().createFieldType(connectionSource, "BaseFoo", field, ForeignNull.class);
		propertyConfig.configDaoInformation(connectionSource, ForeignNull.class);
		ForeignNullForeign foo = new ForeignNullForeign();
		foo.id = null;
		cmpForeign.appendArgOrValue(databaseType, propertyConfig, sb, new ArrayList<ArgumentHolder>(), foo);
	}

	protected static class ForeignNull {
		@DatabaseField(id = true)
		String id;
		@DatabaseField(foreign = true)
		ForeignNullForeign foreign;
		public ForeignNull() {
		}
	}

	protected static class ForeignNullForeign {
		@DatabaseField(id = true)
		String id;
		public ForeignNullForeign() {
		}
	}
}
