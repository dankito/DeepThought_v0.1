package com.j256.ormlite.stmt;

import com.j256.ormlite.jpa.PropertyConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NullArgHolderTest {

	@Test
	public void testStuff() {
		NullArgHolder holder = new NullArgHolder();
		assertEquals("null-holder", holder.getColumnName());
		holder.setMetaInfo((String) null);
		holder.setMetaInfo((PropertyConfig) null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetValueThrows() {
		new NullArgHolder().setValue(null);
	}
}
