package com.j256.ormlite.stmt;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;

import org.junit.Before;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public abstract class BaseCoreStmtTest extends BaseCoreTest {

	protected EntityConfig<Foo, Integer> baseFooEntityConfig;
	protected PropertyConfig numberPropertyConfig;
	protected PropertyConfig stringPropertyConfig;
	protected PropertyConfig foreignPropertyConfig;

	@Override
	@Before
	public void before() throws Exception {
		super.before();

		Field field = Foo.class.getDeclaredField("stringField");
		assertEquals(String.class, field.getType());
		stringPropertyConfig = Instances.getFieldTypeCreator().createFieldType(connectionSource, "BaseFoo", field, Foo.class);
		stringPropertyConfig.configDaoInformation(connectionSource, Foo.class);
		field = Foo.class.getDeclaredField("val");
		assertEquals(int.class, field.getType());
		numberPropertyConfig = Instances.getFieldTypeCreator().createFieldType(connectionSource, "BaseFoo", field, Foo.class);
		numberPropertyConfig.configDaoInformation(connectionSource, Foo.class);
		field = Foreign.class.getDeclaredField("foo");
		assertEquals(Foo.class, field.getType());
		foreignPropertyConfig = Instances.getFieldTypeCreator().createFieldType(connectionSource, "BaseFoo", field, Foreign.class);
		foreignPropertyConfig.configDaoInformation(connectionSource, Foreign.class);

		baseFooEntityConfig = new EntityConfig<Foo, Integer>(connectionSource, null, Foo.class);
	}
}
