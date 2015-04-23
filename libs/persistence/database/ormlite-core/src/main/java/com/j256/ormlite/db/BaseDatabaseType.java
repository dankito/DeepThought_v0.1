package com.j256.ormlite.db;

import com.j256.ormlite.field.BaseFieldConverter;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;

/**
 * Base class for all of the {@link DatabaseType} classes that provide the per-database type functionality to create
 * tables and build queries.
 * 
 * <p>
 * Here's a good page which shows some of the <a href="http://troels.arvin.dk/db/rdbms/" >differences between SQL
 * databases</a>.
 * </p>
 * 
 * @author graywatson
 */
public abstract class BaseDatabaseType implements DatabaseType {

	protected static String DEFAULT_SEQUENCE_SUFFIX = "_id_seq";
	protected Driver driver;

	/**
	 * Return the name of the driver class associated with this database type.
	 */
	protected abstract String getDriverClassName();

	public void loadDriver() throws SQLException {
		String className = getDriverClassName();
		if (className != null) {
			// this instantiates the driver class which wires in the JDBC glue
			try {
				Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw SqlExceptionUtil.create("Driver class was not found for " + getDatabaseName()
						+ " database.  Missing jar with class " + className + ".", e);
			}
		}
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public void appendColumnArg(String tableName, StringBuilder sb, PropertyConfig propertyConfig, List<String> additionalArgs,
			List<String> statementsBefore, List<String> statementsAfter, List<String> queriesAfter) throws SQLException {
		appendEscapedEntityName(sb, propertyConfig.getColumnName());
		sb.append(' ');
		DataPersister dataPersister = propertyConfig.getDataPersister();
		// first try the per-field width
		int fieldWidth = propertyConfig.getLength();
		if (fieldWidth == 0) {
			// next try the per-data-type width
			fieldWidth = dataPersister.getDefaultWidth();
		}
		switch (dataPersister.getSqlType()) {

			case STRING :
				appendStringType(sb, propertyConfig, fieldWidth);
				break;

			case LONG_STRING :
				appendLongStringType(sb, propertyConfig, fieldWidth);
				break;

			case BOOLEAN :
				appendBooleanType(sb, propertyConfig, fieldWidth);
				break;

			case DATE :
				appendDateType(sb, propertyConfig, fieldWidth);
				break;

			case CHAR :
				appendCharType(sb, propertyConfig, fieldWidth);
				break;

			case BYTE :
				appendByteType(sb, propertyConfig, fieldWidth);
				break;

			case BYTE_ARRAY :
				appendByteArrayType(sb, propertyConfig, fieldWidth);
				break;

			case SHORT :
				appendShortType(sb, propertyConfig, fieldWidth);
				break;

			case INTEGER :
				appendIntegerType(sb, propertyConfig, fieldWidth);
				break;

			case LONG :
				appendLongType(sb, propertyConfig, fieldWidth);
				break;

			case FLOAT :
				appendFloatType(sb, propertyConfig, fieldWidth);
				break;

			case DOUBLE :
				appendDoubleType(sb, propertyConfig, fieldWidth);
				break;

			case SERIALIZABLE :
				appendSerializableType(sb, propertyConfig, fieldWidth);
				break;

			case BIG_DECIMAL :
				appendBigDecimalNumericType(sb, propertyConfig, fieldWidth);
				break;

			case UNKNOWN :
			default :
				// shouldn't be able to get here unless we have a missing case
				throw new IllegalArgumentException("Unknown SQL-type " + dataPersister.getSqlType());
		}
		sb.append(' ');

		/*
		 * NOTE: the configure id methods must be in this order since isGeneratedIdSequence is also isGeneratedId and
		 * isId. isGeneratedId is also isId.
		 */
		if (propertyConfig.isGeneratedIdSequence() && !propertyConfig.isSelfGeneratedId()) {
			configureGeneratedIdSequence(sb, propertyConfig, statementsBefore, additionalArgs, queriesAfter);
		} else if (propertyConfig.isGeneratedId() && !propertyConfig.isSelfGeneratedId()) {
			configureGeneratedId(tableName, sb, propertyConfig, statementsBefore, statementsAfter, additionalArgs,
					queriesAfter);
		} else if (propertyConfig.isId()) {
			configureId(sb, propertyConfig, statementsBefore, additionalArgs, queriesAfter);
		}
		// if we have a generated-id then neither the not-null nor the default make sense and cause syntax errors
		if (!propertyConfig.isGeneratedId()) {
			Object defaultValue = propertyConfig.getDefaultValue();
			if (defaultValue != null) {
				sb.append("DEFAULT ");
				appendDefaultValue(sb, propertyConfig, defaultValue);
				sb.append(' ');
			}
			if (propertyConfig.canBeNull()) {
				appendCanBeNull(sb, propertyConfig);
			} else {
				sb.append("NOT NULL ");
			}
			if (propertyConfig.isUnique()) {
				addSingleUnique(sb, propertyConfig, additionalArgs, statementsAfter);
			}
		}
	}

	/**
	 * Output the SQL type for a Java String.
	 */
	protected void appendStringType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		if (isVarcharFieldWidthSupported()) {
			sb.append("VARCHAR(").append(fieldWidth).append(")");
		} else {
			sb.append("VARCHAR");
		}
	}

	/**
	 * Output the SQL type for a Java Long String.
	 */
	protected void appendLongStringType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("TEXT");
	}

	/**
	 * Output the SQL type for a Java Date.
	 */
	protected void appendDateType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("TIMESTAMP");
	}

	/**
	 * Output the SQL type for a Java boolean.
	 */
	protected void appendBooleanType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("BOOLEAN");
	}

	/**
	 * Output the SQL type for a Java char.
	 */
	protected void appendCharType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("CHAR");
	}

	/**
	 * Output the SQL type for a Java byte.
	 */
	protected void appendByteType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("TINYINT");
	}

	/**
	 * Output the SQL type for a Java short.
	 */
	protected void appendShortType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("SMALLINT");
	}

	/**
	 * Output the SQL type for a Java integer.
	 */
	private void appendIntegerType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("INTEGER");
	}

	/**
	 * Output the SQL type for a Java long.
	 */
	protected void appendLongType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("BIGINT");
	}

	/**
	 * Output the SQL type for a Java float.
	 */
	private void appendFloatType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("FLOAT");
	}

	/**
	 * Output the SQL type for a Java double.
	 */
	private void appendDoubleType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("DOUBLE PRECISION");
	}

	/**
	 * Output the SQL type for either a serialized Java object or a byte[].
	 */
	protected void appendByteArrayType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a serialized Java object.
	 */
	protected void appendSerializableType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a BigDecimal object.
	 */
	protected void appendBigDecimalNumericType(StringBuilder sb, PropertyConfig propertyConfig, int fieldWidth) {
		sb.append("NUMERIC");
	}

	/**
	 * Output the SQL type for the default value for the type.
	 */
	private void appendDefaultValue(StringBuilder sb, PropertyConfig propertyConfig, Object defaultValue) {
		if (propertyConfig.isEscapedDefaultValue()) {
			appendEscapedWord(sb, defaultValue.toString());
		} else {
			sb.append(defaultValue);
		}
	}

	/**
	 * Output the SQL necessary to configure a generated-id column. This may add to the before statements list or
	 * additional arguments later.
	 * 
	 * NOTE: Only one of configureGeneratedIdSequence, configureGeneratedId, or configureId will be called.
	 */
	protected void configureGeneratedIdSequence(StringBuilder sb, PropertyConfig propertyConfig, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) throws SQLException {
		throw new SQLException("GeneratedIdSequence is not supported by database " + getDatabaseName() + " for field "
				+ propertyConfig);
	}

	/**
	 * Output the SQL necessary to configure a generated-id column. This may add to the before statements list or
	 * additional arguments later.
	 * 
	 * NOTE: Only one of configureGeneratedIdSequence, configureGeneratedId, or configureId will be called.
	 */
	protected void configureGeneratedId(String tableName, StringBuilder sb, PropertyConfig propertyConfig,
			List<String> statementsBefore, List<String> statementsAfter, List<String> additionalArgs,
			List<String> queriesAfter) {
		throw new IllegalStateException("GeneratedId is not supported by database " + getDatabaseName() + " for field "
				+ propertyConfig);
	}

	/**
	 * Output the SQL necessary to configure an id column. This may add to the before statements list or additional
	 * arguments later.
	 * 
	 * NOTE: Only one of configureGeneratedIdSequence, configureGeneratedId, or configureId will be called.
	 */
	protected void configureId(StringBuilder sb, PropertyConfig propertyConfig, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		// default is noop since we do it at the end in appendPrimaryKeys()
	}

	public void addPrimaryKeySql(PropertyConfig[] propertyConfigs, List<String> additionalArgs, List<String> statementsBefore,
			List<String> statementsAfter, List<String> queriesAfter) {
		StringBuilder sb = null;
		for (PropertyConfig propertyConfig : propertyConfigs) {
			if (propertyConfig.isGeneratedId() && !generatedIdSqlAtEnd() && !propertyConfig.isSelfGeneratedId()) {
				// don't add anything
			} else if (propertyConfig.isId()) {
				if (sb == null) {
					sb = new StringBuilder(48);
					sb.append("PRIMARY KEY (");
				} else {
					sb.append(',');
				}
				appendEscapedEntityName(sb, propertyConfig.getColumnName());
			}
		}
		if (sb != null) {
			sb.append(") ");
			additionalArgs.add(sb.toString());
		}
	}

	/**
	 * Return true if we should add generated-id SQL in the {@link #addPrimaryKeySql} method at the end. If false then
	 * it needs to be done by hand inline.
	 */
	protected boolean generatedIdSqlAtEnd() {
		return true;
	}

	public void addUniqueComboSql(PropertyConfig[] propertyConfigs, List<String> additionalArgs, List<String> statementsBefore,
			List<String> statementsAfter, List<String> queriesAfter) {
		StringBuilder sb = null;
		for (PropertyConfig propertyConfig : propertyConfigs) {
			if (propertyConfig.isUniqueCombo()) {
				if (sb == null) {
					sb = new StringBuilder(48);
					sb.append("UNIQUE (");
				} else {
					sb.append(',');
				}
				appendEscapedEntityName(sb, propertyConfig.getColumnName());
			}
		}
		if (sb != null) {
			sb.append(") ");
			additionalArgs.add(sb.toString());
		}
	}

	public void dropColumnArg(PropertyConfig propertyConfig, List<String> statementsBefore, List<String> statementsAfter) {
		// by default this is a noop
	}

	public void appendEscapedWord(StringBuilder sb, String word) {
		sb.append('\'').append(word).append('\'');
	}

	public void appendEscapedEntityName(StringBuilder sb, String name) {
		sb.append('`').append(name).append('`');
	}

	public String generateIdSequenceName(String tableName, PropertyConfig idPropertyConfig) {
		String name = tableName + DEFAULT_SEQUENCE_SUFFIX;
		if (isEntityNamesMustBeUpCase()) {
			return name.toUpperCase();
		} else {
			return name;
		}
	}

	public String getCommentLinePrefix() {
		return "-- ";
	}

	public DataPersister getDataPersister(DataPersister defaultPersister, PropertyConfig propertyConfig) {
		// default is noop
		return defaultPersister;
	}

	public FieldConverter getFieldConverter(DataPersister dataPersister, PropertyConfig propertyConfig) {
		// default is to use the dataPersister itself
		return dataPersister;
	}

	public boolean isIdSequenceNeeded() {
		return false;
	}

	public boolean isVarcharFieldWidthSupported() {
		return true;
	}

	public boolean isLimitSqlSupported() {
		return true;
	}

	public boolean isOffsetSqlSupported() {
		return true;
	}

	public boolean isOffsetLimitArgument() {
		return false;
	}

	public boolean isLimitAfterSelect() {
		return false;
	}

	public void appendLimitValue(StringBuilder sb, long limit, Long offset) {
		sb.append("LIMIT ").append(limit).append(' ');
	}

	public void appendOffsetValue(StringBuilder sb, long offset) {
		sb.append("OFFSET ").append(offset).append(' ');
	}

	public void appendSelectNextValFromSequence(StringBuilder sb, String sequenceName) {
		// noop by default.
	}

	public void appendCreateTableSuffix(StringBuilder sb) {
		// noop by default.
	}

	public boolean isCreateTableReturnsZero() {
		return true;
	}

	public boolean isCreateTableReturnsNegative() {
		return false;
	}

	public boolean isEntityNamesMustBeUpCase() {
		return false;
	}

	public boolean isNestedSavePointsSupported() {
		return true;
	}

	public String getPingStatement() {
		return "SELECT 1";
	}

	public boolean isBatchUseTransaction() {
		return false;
	}

	public boolean isTruncateSupported() {
		return false;
	}

	public boolean isCreateIfNotExistsSupported() {
		return false;
	}

	public boolean isCreateIndexIfNotExistsSupported() {
		return isCreateIfNotExistsSupported();
	}

	public boolean isSelectSequenceBeforeInsert() {
		return false;
	}

	public boolean isAllowGeneratedIdInsertSupported() {
		return true;
	}

	/**
	 * @throws SQLException
	 *             for sub classes.
	 */
	public <T> DatabaseTableConfig<T> extractDatabaseTableConfig(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		// default is no default extractor
		return null;
	}

	public void appendInsertNoColumns(StringBuilder sb) {
		sb.append("() VALUES ()");
	}

	/**
	 * If the field can be nullable, do we need to add some sort of NULL SQL for the create table. By default it is a
	 * noop. This is necessary because MySQL has a auto default value for the TIMESTAMP type that required a default
	 * value otherwise it would stick in the current date automagically.
	 */
	private void appendCanBeNull(StringBuilder sb, PropertyConfig propertyConfig) {
		// default is a noop
	}

	/**
	 * Add SQL to handle a unique=true field. THis is not for uniqueCombo=true.
	 */
	private void addSingleUnique(StringBuilder sb, PropertyConfig propertyConfig, List<String> additionalArgs,
			List<String> statementsAfter) {
		StringBuilder alterSb = new StringBuilder();
		alterSb.append(" UNIQUE (");
		appendEscapedEntityName(alterSb, propertyConfig.getColumnName());
		alterSb.append(")");
		additionalArgs.add(alterSb.toString());
	}

	/**
	 * Conversion to/from the Boolean Java field as a number because some databases like the true/false.
	 */
	protected static class BooleanNumberFieldConverter extends BaseFieldConverter {
		public SqlType getSqlType() {
			return SqlType.BOOLEAN;
		}
		public Object parseDefaultString(PropertyConfig propertyConfig, String defaultStr) {
			boolean bool = (boolean) Boolean.parseBoolean(defaultStr);
			return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
		}
		@Override
		public Object javaToSqlArg(PropertyConfig propertyConfig, Object obj) {
			Boolean bool = (Boolean) obj;
			return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
		}
		public Object resultToSqlArg(PropertyConfig propertyConfig, DatabaseResults results, int columnPos) throws SQLException {
			return results.getByte(columnPos);
		}
		@Override
		public Object sqlArgToJava(PropertyConfig propertyConfig, Object sqlArg, int columnPos) {
			byte arg = (Byte) sqlArg;
			return (arg == 1 ? (Boolean) true : (Boolean) false);
		}
		public Object resultStringToJava(PropertyConfig propertyConfig, String stringValue, int columnPos) {
			return sqlArgToJava(propertyConfig, Byte.parseByte(stringValue), columnPos);
		}
	}
}
