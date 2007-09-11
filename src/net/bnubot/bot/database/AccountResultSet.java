/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AccountResultSet extends ExtendableResultSet {
	public static final String ROW_ID = "id";
	public static final String ROW_ACCESS = "access";
	public static final String ROW_NAME = "name";
	public static final String ROW_CREATED = "created";
	public static final String ROW_LAST_RANK_CHANGE = "lastRankChange";
	public static final String ROW_CREATED_BY = "createdby";
	public static final String ROW_TRIVIA_CORRECT = "trivia_correct";
	public static final String ROW_TRIVIA_WIN = "trivia_win";
	public static final String ROW_BIRTHDAY = "birthday";

	public AccountResultSet(ResultSet parent) {
		super(parent);
	}
	
	public Long getId() throws SQLException {
		long value = getLong(ROW_ID);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setId(Long id) throws SQLException {
		if(id == null)
			updateNull(ROW_ID);
		else
			updateLong(ROW_ID, id);
	}
	
	public Long getAccess() throws SQLException {
		long value = getLong(ROW_ACCESS);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setAccess(Long access) throws SQLException {
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
	
	public void setName(String name) throws SQLException {
		if(name == null)
			updateNull(ROW_NAME);
		else
			updateString(ROW_NAME, name);
	}
	
	public Timestamp getCreated() throws SQLException {
		Timestamp value = getTimestamp(ROW_CREATED);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setCreated(Timestamp created) throws SQLException {
		if(created == null)
			updateNull(ROW_CREATED);
		else
			updateTimestamp(ROW_CREATED, created);
	}
	
	public Timestamp getLastRankChange() throws SQLException {
		Timestamp value = getTimestamp(ROW_LAST_RANK_CHANGE);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setLastRankChange(Timestamp lastRankChange) throws SQLException {
		if(lastRankChange == null)
			updateNull(ROW_LAST_RANK_CHANGE);
		else
			updateTimestamp(ROW_LAST_RANK_CHANGE, lastRankChange);
	}
	
	public Long getCreatedBy() throws SQLException {
		long value = getLong(ROW_CREATED_BY);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setCreatedBy(Long createdby) throws SQLException {
		if(createdby == null)
			updateNull(ROW_CREATED_BY);
		else
			updateLong(ROW_CREATED_BY, createdby);
	}
	
	public Long getTriviaCorrect() throws SQLException {
		long value = getLong(ROW_TRIVIA_CORRECT);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setTriviaCorrect(Long trivia_correct) throws SQLException {
		if(trivia_correct == null)
			updateNull(ROW_TRIVIA_CORRECT);
		else
			updateLong(ROW_TRIVIA_CORRECT, trivia_correct);
	}
	
	public Long getTriviaWin() throws SQLException {
		long value = getLong(ROW_TRIVIA_WIN);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setTriviaWin(Long trivia_win) throws SQLException {
		if(trivia_win == null)
			updateNull(ROW_TRIVIA_WIN);
		else
			updateLong(ROW_TRIVIA_WIN, trivia_win);
	}
	
	public Date getBirthday() throws SQLException {
		Date value = getDate(ROW_BIRTHDAY);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setBirthday(Date birthday) throws SQLException {
		if(birthday == null)
			updateNull(ROW_BIRTHDAY);
		else
			updateDate(ROW_BIRTHDAY, birthday);
	}
}
