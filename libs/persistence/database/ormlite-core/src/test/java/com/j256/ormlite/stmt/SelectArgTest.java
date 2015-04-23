package com.j256.ormlite.stmt;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SelectArgTest extends BaseCoreStmtTest {

	@Test(expected = SQLException.class)
	public void testGetBeforeSetValue() throws Exception {
		SelectArg selectArg = new SelectArg();
		selectArg.getSqlArgValue();
	}

	@Test
	public void testSetValue() throws Exception {
		SelectArg selectArg = new SelectArg();
		Object foo = new Object();
		selectArg.setValue(foo);
		assertSame(foo, selectArg.getSqlArgValue());
	}

	@Test
	public void testSetNumber() throws Exception {
		SelectArg selectArg = new SelectArg();
		int val = 10;
		selectArg.setMetaInfo("val", numberPropertyConfig);
		selectArg.setValue(val);
		assertSame(val, selectArg.getSqlArgValue());
	}

	@Test
	public void testGetColumnNameOk() {
		SelectArg selectArg = new SelectArg();
		String name = "fwewfwef";
		selectArg.setMetaInfo(name, stringPropertyConfig);
		assertSame(name, selectArg.getColumnName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetColumnNameTwice() {
		SelectArg selectArg = new SelectArg();
		selectArg.setMetaInfo("1", numberPropertyConfig);
		selectArg.setMetaInfo("2", numberPropertyConfig);
	}

	@Test
	public void testSetNullValue() throws Exception {
		SelectArg selectArg = new SelectArg();
		selectArg.setValue(null);
		assertNull(selectArg.getSqlArgValue());
	}

	@Test
	public void testForeignValue() {
		SelectArg selectArg = new SelectArg();
		assertTrue(selectArg.toString().contains("[unset]"));
		Foo foo = new Foo();
		selectArg.setValue(foo);
		selectArg.setMetaInfo("id", foreignPropertyConfig);
		assertTrue(selectArg + " wrong value", selectArg.toString().contains(Integer.toString(foo.id)));
	}

	@Test
	public void testToString() {
		SelectArg selectArg = new SelectArg();
		assertTrue(selectArg.toString().contains("[unset]"));
		selectArg.setValue(null);
		assertTrue(selectArg.toString().contains("[null]"));
		String value = "fwefefewf";
		selectArg.setValue(value);
		assertTrue(selectArg.toString().contains(value));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDoubleSet() {
		SelectArg selectArg = new SelectArg();
		selectArg.setMetaInfo("id", numberPropertyConfig);
		selectArg.setMetaInfo("id", stringPropertyConfig);
	}
}
