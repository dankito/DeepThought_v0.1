package com.j256.ormlite.stmt.query;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.ColumnArg;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal base class for all comparison operations.
 * 
 * @author graywatson
 */
abstract class BaseComparison implements Comparison {

	private static final String NUMBER_CHARACTERS = "0123456789.-+";
	protected final String columnName;
	protected final PropertyConfig propertyConfig;
	private final Object value;

	protected BaseComparison(String columnName, PropertyConfig propertyConfig, Object value, boolean isComparison)
			throws SQLException {
		if (isComparison && propertyConfig != null && !propertyConfig.isComparable()) {
			throw new SQLException("Field '" + columnName + "' is of data type " + propertyConfig.getDataPersister()
					+ " which can not be compared");
		}
		this.columnName = columnName;
		this.propertyConfig = propertyConfig;
		this.value = value;
	}

	public abstract void appendOperation(StringBuilder sb);

	public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		if (tableName != null) {
			databaseType.appendEscapedEntityName(sb, tableName);
			sb.append('.');
		}
		databaseType.appendEscapedEntityName(sb, columnName);
		sb.append(' ');
		appendOperation(sb);
		// this needs to call appendValue (not appendArgOrValue) because it may be overridden
		appendValue(databaseType, sb, argList);
	}

	public String getColumnName() {
		return columnName;
	}

	public void appendValue(DatabaseType databaseType, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		appendArgOrValue(databaseType, propertyConfig, sb, argList, value);
	}

	/**
	 * Append to the string builder either a {@link ArgumentHolder} argument or a value object.
	 */
	protected void appendArgOrValue(DatabaseType databaseType, PropertyConfig propertyConfig, StringBuilder sb,
			List<ArgumentHolder> argList, Object argOrValue) throws SQLException {
		boolean appendSpace = true;
		if (argOrValue == null) {
			throw new SQLException("argument for '" + propertyConfig.getFieldName() + "' is null");
		} else if (argOrValue instanceof ArgumentHolder) {
			sb.append('?');
			ArgumentHolder argHolder = (ArgumentHolder) argOrValue;
			argHolder.setMetaInfo(columnName, propertyConfig);
			argList.add(argHolder);
		} else if (argOrValue instanceof ColumnArg) {
			ColumnArg columnArg = (ColumnArg) argOrValue;
			String tableName = columnArg.getTableName();
			if (tableName != null) {
				databaseType.appendEscapedEntityName(sb, tableName);
				sb.append('.');
			}
			databaseType.appendEscapedEntityName(sb, columnArg.getColumnName());
		} else if (propertyConfig.isArgumentHolderRequired()) {
			sb.append('?');
			ArgumentHolder argHolder = new SelectArg();
			argHolder.setMetaInfo(columnName, propertyConfig);
			// conversion is done when the getValue() is called
			argHolder.setValue(argOrValue);
			argList.add(argHolder);
		} else if (propertyConfig.isForeign() && propertyConfig.getType().isAssignableFrom(argOrValue.getClass())) {
			/*
			 * If we have a foreign field and our argument is an instance of the foreign object (i.e. not its id), then
			 * we need to extract the id. We allow super-classes of the field but not sub-classes.
			 */
			PropertyConfig idPropertyConfig = propertyConfig.getForeignIdField();
			appendArgOrValue(databaseType, idPropertyConfig, sb, argList, idPropertyConfig.extractJavaFieldValue(argOrValue));
			// no need for the space since it was done in the recursion
			appendSpace = false;
		} else if (propertyConfig.isEscapedValue()) {
			databaseType.appendEscapedWord(sb, propertyConfig.convertJavaFieldToSqlArgValue(argOrValue).toString());
		} else if (propertyConfig.isForeign()) {
			/*
			 * I'm not entirely sure this is correct. This is trying to protect against someone trying to pass an object
			 * into a comparison with a foreign field. Typically if they pass the same field type, then ORMLite will
			 * extract the ID of the foreign.
			 */
			String value = propertyConfig.convertJavaFieldToSqlArgValue(argOrValue).toString();
			if (value.length() > 0) {
				if (NUMBER_CHARACTERS.indexOf(value.charAt(0)) < 0) {
					throw new SQLException("Foreign field " + propertyConfig
							+ " does not seem to be producing a numerical value '" + value
							+ "'. Maybe you are passing the wrong object to comparison: " + this);
				}
			}
			sb.append(value);
		} else {
			// numbers can't have quotes around them in derby
			sb.append(propertyConfig.convertJavaFieldToSqlArgValue(argOrValue));
		}
		if (appendSpace) {
			sb.append(' ');
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(columnName).append(' ');
		appendOperation(sb);
		sb.append(' ');
		sb.append(value);
		return sb.toString();
	}
}
