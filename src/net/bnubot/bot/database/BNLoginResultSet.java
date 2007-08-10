/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.database;

import java.sql.*;

public class BNLoginResultSet extends ExtendableResultSet {
	public static final String ROW_ID = "id";
	public static final String ROW_LOGIN = "login";
	public static final String ROW_ACCOUNT = "account";
	public static final String ROW_CREATED = "created";
	public static final String ROW_LAST_SEEN = "lastSeen";
	public static final String ROW_LAST_ACTION = "lastAction";
	public static final String ROW_WINS_STAR = "winsSTAR";
	public static final String ROW_WINS_SEXP = "winsSEXP";
	public static final String ROW_WINS_W2BN = "winsW2BN";
	public static final String ROW_LEVEL_D2 = "levelD2";
	public static final String ROW_LEVEL_W3 = "levelW3";

	public BNLoginResultSet(ResultSet parent) {
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
	
	public String getLogin() throws SQLException {
		String value = getString(ROW_LOGIN);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setLogin(String login) throws SQLException {
		if(login == null)
			updateNull(ROW_LOGIN);
		else
			updateString(ROW_LOGIN, login);
	}
	
	public Long getAccount() throws SQLException {
		Long value = getLong(ROW_ACCOUNT);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setAccount(Long account) throws SQLException {
		if(account == null)
			updateNull(ROW_ACCOUNT);
		else
			updateLong(ROW_ACCOUNT, account);
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
	
	public Timestamp getLastSeen() throws SQLException {
		Timestamp value = getTimestamp(ROW_LAST_SEEN);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setLastSeen(Timestamp lastSeen) throws SQLException {
		if(lastSeen == null)
			updateNull(ROW_LAST_SEEN);
		else
			updateTimestamp(ROW_LAST_SEEN, lastSeen);
	}

	public String getLastAction() throws SQLException {
		String value = getString(ROW_LAST_ACTION);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setLastAction(String lastAction) throws SQLException {
		if(lastAction == null)
			updateNull(ROW_LAST_ACTION);
		else
			updateString(ROW_LAST_ACTION, lastAction);
	}
	
	public Long getWinsSTAR() throws SQLException {
		Long value = getLong(ROW_WINS_STAR);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setWinsSTAR(Long winsSTAR) throws SQLException {
		if(winsSTAR == null)
			updateNull(ROW_WINS_STAR);
		else
			updateLong(ROW_WINS_STAR, winsSTAR);
	}

	public Long getWinsSEXP() throws SQLException {
		Long value = getLong(ROW_WINS_SEXP);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setWinsSEXP(Long winsSEXP) throws SQLException {
		if(winsSEXP == null)
			updateNull(ROW_WINS_SEXP);
		else
			updateLong(ROW_WINS_SEXP, winsSEXP);
	}

	public Long getWinsW2BN() throws SQLException {
		Long value = getLong(ROW_WINS_W2BN);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setWinsW2BN(Long winsW2BN) throws SQLException {
		if(winsW2BN == null)
			updateNull(ROW_WINS_W2BN);
		else
			updateLong(ROW_WINS_W2BN, winsW2BN);
	}

	public Long getLevelD2() throws SQLException {
		Long value = getLong(ROW_LEVEL_D2);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setLevelD2(Long D2Level) throws SQLException {
		if(D2Level == null)
			updateNull(ROW_LEVEL_D2);
		else
			updateLong(ROW_LEVEL_D2, D2Level);
	}

	public Long getLevelW3() throws SQLException {
		Long value = getLong(ROW_LEVEL_W3);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setLevelW3(Long w3Level) throws SQLException {
		if(w3Level == null)
			updateNull(ROW_LEVEL_W3);
		else
			updateLong(ROW_LEVEL_W3, w3Level);
	}
}
