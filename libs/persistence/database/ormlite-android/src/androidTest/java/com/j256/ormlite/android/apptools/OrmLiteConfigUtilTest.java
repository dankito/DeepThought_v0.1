package com.j256.ormlite.android.apptools;

//import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;

//import org.junit.Test;
import android.test.AndroidTestCase;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class OrmLiteConfigUtilTest extends AndroidTestCase {

	////@Test
	public void testBasic() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OrmLiteConfigUtil.writeConfigFile(output, new Class[] { Foo.class });
		String result = output.toString();
    assertTrue(result, result.contains("\nfieldName=id\nid=true\n"));
	}

	//@Test
	public void testCurrentDir() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OrmLiteConfigUtil.writeConfigFile(output, new File("src/androidTest/java/com/j256/ormlite/android/apptools/"));
		String result = output.toString();
		assertTrue(result, result.contains("\nfieldName=id\nid=true\n"));
	}

	//@Test
	public void testForeignCollection() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OrmLiteConfigUtil.writeConfigFile(output, new Class[] { ForeignCollectionTest.class });
		String result = output.toString();
		assertTrue(result, result.contains("\nfieldName=collection\nforeignCollection=true\n"));
	}

	//@Test
	public void testForeign() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OrmLiteConfigUtil.writeConfigFile(output, new Class[] { Foo.class });
		String result = output.toString();
		assertTrue(result, result.contains("\nfieldName=foreign\nforeign=true\n"));
	}

	protected static class Foo {
		@DatabaseField(id = true)
		int id;
		@DatabaseField(foreign = true)
		Foreign foreign;
	}

	protected static class Foreign {
		@DatabaseField(id = true)
		int id;
	}

	protected static class ForeignCollectionTest {
		@ForeignCollectionField
		Collection<Foo> collection;
	}
}
