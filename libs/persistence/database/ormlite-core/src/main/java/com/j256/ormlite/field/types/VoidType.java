package com.j256.ormlite.field.types;

import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.DatabaseResults;

import java.lang.reflect.Field;

/**
 * Marker class used to see if we have a customer persister defined.
 * 
 * @author graywatson
 */
public class VoidType extends BaseDataType {

	VoidType() {
		super(null, new Class<?>[] {});
	}

	@Override
	public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) {
		return null;
	}

	@Override
	public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) {
		return null;
	}

	@Override
	public boolean isValidForField(Field field) {
		return false;
	}
}
