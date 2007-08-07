/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.database;

import java.sql.*;

public class ExtendableResultSet {
	private ResultSet parent;
	
	protected ExtendableResultSet(ResultSet parent) {
		this.parent = parent;
	}

	protected void beforeFirst() throws SQLException {
		parent.beforeFirst();
	}

	protected void close() throws SQLException {
		parent.close();
	}

	public void deleteRow() throws SQLException {
		parent.deleteRow();
	}

	protected boolean first() throws SQLException {
		return parent.first();
	}

	protected Date getDate(String columnName) throws SQLException {
		return parent.getDate(columnName);
	}

	protected long getLong(int columnIndex) throws SQLException {
		return parent.getLong(columnIndex);
	}

	protected long getLong(String columnName) throws SQLException {
		return parent.getLong(columnName);
	}

	protected Statement getStatement() throws SQLException {
		return parent.getStatement();
	}

	protected String getString(int columnIndex) throws SQLException {
		return parent.getString(columnIndex);
	}

	protected String getString(String columnName) throws SQLException {
		return parent.getString(columnName);
	}

	protected Timestamp getTimestamp(String columnName) throws SQLException {
		return parent.getTimestamp(columnName);
	}

	public boolean next() throws SQLException {
		return parent.next();
	}

	protected boolean previous() throws SQLException {
		return parent.previous();
	}

	protected void refreshRow() throws SQLException {
		parent.refreshRow();
	}

	protected void updateDate(int columnIndex, Date x) throws SQLException {
		parent.updateDate(columnIndex, x);
	}

	protected void updateDate(String columnName, Date x) throws SQLException {
		parent.updateDate(columnName, x);
	}

	protected void updateLong(int columnIndex, long x) throws SQLException {
		parent.updateLong(columnIndex, x);
	}

	protected void updateLong(String columnName, long x) throws SQLException {
		parent.updateLong(columnName, x);
	}

	protected void updateNull(int columnIndex) throws SQLException {
		parent.updateNull(columnIndex);
	}

	protected void updateNull(String columnName) throws SQLException {
		parent.updateNull(columnName);
	}

	public void updateRow() throws SQLException {
		parent.updateRow();
	}

	protected void updateString(int columnIndex, String x) throws SQLException {
		parent.updateString(columnIndex, x);
	}

	protected void updateString(String columnName, String x) throws SQLException {
		parent.updateString(columnName, x);
	}

	protected void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		parent.updateTimestamp(columnIndex, x);
	}

	protected void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		parent.updateTimestamp(columnName, x);
	}

	protected boolean wasNull() throws SQLException {
		return parent.wasNull();
	}

}
