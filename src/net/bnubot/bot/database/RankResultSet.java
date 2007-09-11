/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RankResultSet extends ExtendableResultSet {
	public static final String ROW_ID = "id";
	public static final String ROW_SHORT_PREFIX = "shortPrefix";
	public static final String ROW_PREFIX = "prefix";
	public static final String ROW_VERBSTR = "verbstr";
	public static final String ROW_GREETING = "greeting";
	public static final String ROW_EXPIRE_DAYS = "expireDays";
	public static final String ROW_AP_DAYS = "apDays";
	public static final String ROW_AP_WINS = "apWins";
	public static final String ROW_AP_D2_LEVEL = "apD2Level";
	public static final String ROW_AP_W3_LEVEL = "apW3Level";
	public static final String ROW_AP_RECRUIT_SCORE = "apRecruitScore";
	public static final String ROW_AP_MAIL = "apMail";

	public RankResultSet(ResultSet parent) {
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

	public String getShortPrefix() throws SQLException {
		String value = getString(ROW_SHORT_PREFIX);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setShortPrefix(String shortPrefix) throws SQLException {
		if(shortPrefix == null)
			updateNull(ROW_SHORT_PREFIX);
		else
			updateString(ROW_SHORT_PREFIX, shortPrefix);
	}

	public String getPrefix() throws SQLException {
		String value = getString(ROW_PREFIX);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setPrefix(String prefix) throws SQLException {
		if(prefix == null)
			updateNull(ROW_PREFIX);
		else
			updateString(ROW_PREFIX, prefix);
	}

	public String getVerbStr() throws SQLException {
		String value = getString(ROW_VERBSTR);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setVerbStr(String verbStr) throws SQLException {
		if(verbStr == null)
			updateNull(ROW_VERBSTR);
		else
			updateString(ROW_VERBSTR, verbStr);
	}

	public String getGreeting() throws SQLException {
		String value = getString(ROW_GREETING);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setGreeting(String greeting) throws SQLException {
		if(greeting == null)
			updateNull(ROW_GREETING);
		else
			updateString(ROW_GREETING, greeting);
	}

	public Long getExpireDays() throws SQLException {
		Long value = getLong(ROW_EXPIRE_DAYS);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setExpireDays(Long expireDays) throws SQLException {
		if(expireDays == null)
			updateNull(ROW_EXPIRE_DAYS);
		else
			updateLong(ROW_EXPIRE_DAYS, expireDays);
	}

	public Long getApDays() throws SQLException {
		Long value = getLong(ROW_AP_DAYS);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setApDays(Long apDays) throws SQLException {
		if(apDays == null)
			updateNull(ROW_AP_DAYS);
		else
			updateLong(ROW_AP_DAYS, apDays);
	}

	public Long getApWins() throws SQLException {
		Long value = getLong(ROW_AP_WINS);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setApWins(Long apWins) throws SQLException {
		if(apWins == null)
			updateNull(ROW_AP_WINS);
		else
			updateLong(ROW_AP_WINS, apWins);
	}

	public Long getApD2Level() throws SQLException {
		Long value = getLong(ROW_AP_D2_LEVEL);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setApD2Level(Long apD2Level) throws SQLException {
		if(apD2Level == null)
			updateNull(ROW_AP_D2_LEVEL);
		else
			updateLong(ROW_AP_D2_LEVEL, apD2Level);
	}

	public Long getApW3Level() throws SQLException {
		Long value = getLong(ROW_AP_W3_LEVEL);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setApW3Level(Long apW3Level) throws SQLException {
		if(apW3Level == null)
			updateNull(ROW_AP_W3_LEVEL);
		else
			updateLong(ROW_AP_W3_LEVEL, apW3Level);
	}

	public Long getApRecruitScore() throws SQLException {
		Long value = getLong(ROW_AP_RECRUIT_SCORE);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setApRecruitScore(Long apRecruitScore) throws SQLException {
		if(apRecruitScore == null)
			updateNull(ROW_AP_RECRUIT_SCORE);
		else
			updateLong(ROW_AP_RECRUIT_SCORE, apRecruitScore);
	}

	public String getApMail() throws SQLException {
		String value = getString(ROW_AP_MAIL);
		if(wasNull())
			return null;
		return value;
	}
	
	public void setApMail(String apMail) throws SQLException {
		if(apMail == null)
			updateNull(ROW_AP_MAIL);
		else
			updateString(ROW_AP_MAIL, apMail);
	}
}
