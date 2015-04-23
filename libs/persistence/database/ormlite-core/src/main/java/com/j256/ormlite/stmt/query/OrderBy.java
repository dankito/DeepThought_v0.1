package com.j256.ormlite.stmt.query;

import com.j256.ormlite.jpa.Property;
import com.j256.ormlite.jpa.Registry;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Internal class handling the SQL 'ORDER BY' operation. Used by {@link QueryBuilder#orderBy(String, boolean)} and
 * {@link QueryBuilder#orderByRaw(String)}.
 * 
 * @author graywatson
 */
public class OrderBy {

  protected final static String OrderByColumnNotYetLoaded = "OrderBy Column not yet loaded";


  protected String columnName;
  protected boolean ascending;
  protected String rawSql;
  protected ArgumentHolder[] orderByArgs;

  protected Class targetEntityClass = null;
  protected Property orderByPropertyForNotYetLoadedOrderByColumn = null;


	public OrderBy(String columnName, boolean ascending) {
		this.columnName = columnName;
		this.ascending = ascending;
		this.rawSql = null;
		this.orderByArgs = null;
	}

  public OrderBy(Class targetEntityClass, Property orderByProperty, boolean ascending) {
    this(OrderByColumnNotYetLoaded, ascending);
    this.targetEntityClass = targetEntityClass;
    this.orderByPropertyForNotYetLoadedOrderByColumn = orderByProperty;
  }

	public OrderBy(String rawSql, ArgumentHolder[] orderByArgs) {
		this.columnName = null;
		this.ascending = true;
		this.rawSql = rawSql;
		this.orderByArgs = orderByArgs;
	}

	public String getColumnName() {
    if(OrderByColumnNotYetLoaded.equals(columnName)) {
      if(Registry.getPropertyRegistry().hasPropertyConfiguration(targetEntityClass, orderByPropertyForNotYetLoadedOrderByColumn))
        this.columnName = Registry.getPropertyRegistry().getPropertyConfiguration(targetEntityClass, orderByPropertyForNotYetLoadedOrderByColumn).getColumnName();
      // TODO: what to return if property is not found?
    }
		return columnName;
	}

	public boolean isAscending() {
		return ascending;
	}

	public String getRawSql() {
		return rawSql;
	}

	public ArgumentHolder[] getOrderByArgs() {
		return orderByArgs;
	}
}
