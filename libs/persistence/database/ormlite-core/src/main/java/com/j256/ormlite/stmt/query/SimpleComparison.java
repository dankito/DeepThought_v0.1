package com.j256.ormlite.stmt.query;

import com.j256.ormlite.jpa.PropertyConfig;

import java.sql.SQLException;

/**
 * Internal class handling a simple comparison query part where the operation is passed in.
 * 
 * @author graywatson
 */
public class SimpleComparison extends BaseComparison {

	public final static String EQUAL_TO_OPERATION = "=";
	public final static String GREATER_THAN_OPERATION = ">";
	public final static String GREATER_THAN_EQUAL_TO_OPERATION = ">=";
	public final static String LESS_THAN_OPERATION = "<";
	public final static String LESS_THAN_EQUAL_TO_OPERATION = "<=";
	public final static String LIKE_OPERATION = "LIKE";
	public final static String NOT_EQUAL_TO_OPERATION = "<>";

	private final String operation;

	public SimpleComparison(String columnName, PropertyConfig propertyConfig, Object value, String operation) throws SQLException {
		super(columnName, propertyConfig, value, true);
		this.operation = operation;
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append(operation);
		sb.append(' ');
	}
}
