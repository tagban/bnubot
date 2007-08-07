package net.bnubot.bot.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CommandResultSet extends ExtendableResultSet {
	public static final String ROW_ID = "id";
	public static final String ROW_ACCESS = "access";
	public static final String ROW_NAME = "name";
	public static final String ROW_DESCRIPTION = "description";
	public static final String ROW_CMDGROUP = "cmdgroup";

	public CommandResultSet(ResultSet parent) {
		super(parent);
	}
	
	public Long getAccess() throws SQLException {
		long value = getLong(ROW_ACCESS);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setId(Long access) throws SQLException {
		if(access == null)
			updateNull(ROW_ACCESS);
		else
			updateLong(ROW_ACCESS, access);
	}
	
	public String getName() throws SQLException {
		String value = getString(ROW_NAME);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setName(String access) throws SQLException {
		if(access == null)
			updateNull(ROW_NAME);
		else
			updateString(ROW_NAME, access);
	}
	
	public String getCmdGroup() throws SQLException {
		String value = getString(ROW_CMDGROUP);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setCmdGroup(String cmdgroup) throws SQLException {
		if(cmdgroup == null)
			updateNull(ROW_CMDGROUP);
		else
			updateString(ROW_CMDGROUP, cmdgroup);
	}
}
