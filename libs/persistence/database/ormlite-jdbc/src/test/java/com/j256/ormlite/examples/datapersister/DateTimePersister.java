package com.j256.ormlite.examples.datapersister;

import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.DateTimeType;
import com.j256.ormlite.jpa.PropertyConfig;

import org.joda.time.DateTime;

/**
 * A custom persister that is able to store the Joda {@link DateTime} class in the database as epoch-millis long
 * integer. This overrides the {@link DateTimeType} which uses reflection instead. This should run faster.
 * 
 * This can be specified using {@link DatabaseField#persisterClass()} or registered with
 * {@link DataPersisterManager#registerDataPersisters(com.j256.ormlite.field.DataPersister...)}.
 * 
 * @author graywatson
 */
public class DateTimePersister extends DateTimeType {

	private static final DateTimePersister singleTon = new DateTimePersister();

	private DateTimePersister() {
		super(SqlType.LONG, new Class<?>[] { DateTime.class });
	}

	public static DateTimePersister getSingleton() {
		return singleTon;
	}

	@Override
	public Object javaToSqlArg(PropertyConfig fieldType, Object javaObject) {
		DateTime dateTime = (DateTime) javaObject;
		if (dateTime == null) {
			return null;
		} else {
			return dateTime.getMillis();
		}
	}

	@Override
	public Object sqlArgToJava(PropertyConfig fieldType, Object sqlArg, int columnPos) {
		return new DateTime((Long) sqlArg);
	}
}
