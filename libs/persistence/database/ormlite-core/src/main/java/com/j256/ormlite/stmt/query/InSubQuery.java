package com.j256.ormlite.stmt.query;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.QueryBuilder.InternalQueryBuilderWrapper;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal class handling the SQL 'in' query part. Used by {@link Where#in}.
 * 
 * @author graywatson
 */
public class InSubQuery extends BaseComparison {

	private final InternalQueryBuilderWrapper subQueryBuilder;
	private final boolean in;

	public InSubQuery(String columnName, PropertyConfig propertyConfig, InternalQueryBuilderWrapper subQueryBuilder, boolean in)
			throws SQLException {
		super(columnName, propertyConfig, null, true);
		this.subQueryBuilder = subQueryBuilder;
		this.in = in;
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		if (in) {
			sb.append("IN ");
		} else {
			sb.append("NOT IN ");
		}
	}

	@Override
	public void appendValue(DatabaseType databaseType, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		sb.append('(');
		subQueryBuilder.appendStatementString(sb, argList);
		PropertyConfig[] resultPropertyConfigs = subQueryBuilder.getResultFieldTypes();
		if (resultPropertyConfigs == null) {
			// we assume that if someone is doing a raw select, they know what they are doing
		} else if (resultPropertyConfigs.length != 1) {
			throw new SQLException("There must be only 1 result column in sub-query but we found "
					+ resultPropertyConfigs.length);
		} else if (propertyConfig.getSqlTypeOfFieldConverter() != resultPropertyConfigs[0].getSqlTypeOfFieldConverter()) {
			throw new SQLException("Outer column " + propertyConfig + " is not the same type as inner column "
					+ resultPropertyConfigs[0]);
		}
		sb.append(") ");
	}
}
