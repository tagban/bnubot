/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;

import net.bnubot.JARLoader;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;

public class Database {
	private static final long databaseVersion = 2;		// Current schema version
	private static final long compatibleVersion = 2;	// Minimum version compatible
	private static Database instance;
	private Connection conn;

	private ArrayList<Statement> openStatements = new ArrayList<Statement>();
	private ArrayList<Exception> openStmtExcept = new ArrayList<Exception>();
	
	public Database(String driver, String url, String username, String password, String schemaFile) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Driver d = (Driver)JARLoader.forName(driver).newInstance();
		DriverManager.registerDriver(new DriverShim(d));
		
		Out.debug(getClass(), "Connecting to " + url);
		conn = DriverManager.getConnection(url, username, password);
		Out.debug(getClass(), "Connected!");
		
		instance = this;
		
		if(!checkSchema())
			createSchema(schemaFile);
		try {
			deleteOldUsers();
		} catch(Exception e) {
			Out.exception(e);
		}
	}
	
	public static Database getInstance() {
		return instance;
	}
	
	public void close(ResultSet rs) {
		try {
			close(rs.getStatement());
		} catch (SQLException e) {
			Out.exception(e);
		}
	}
	
	public void close(ExtendableResultSet rs) throws SQLException {
		close(rs.getStatement());
	}
	
	public void close(Statement stmt) throws SQLException {
		stmt.close();
			
		int i = openStatements.indexOf(stmt);
		if(i == -1)
			throw new IllegalStateException("Statement not found in cache");
		
		openStatements.remove(i);
		openStmtExcept.remove(i);
	}
	
	private Statement pushStatement(Statement stmt) {
		if(openStatements.size() >= 10) {
			int original = openStatements.size();
			
			//Purge the statements that are already closed
			for(int i = 0; i < openStatements.size(); i++) {
				Statement s = openStatements.get(i);
				
				Integer row = null;
				try {
					ResultSet rs = s.getResultSet();
					if(rs != null)
						row = rs.getRow();
					else
						s.close();
				} catch (SQLException e) {}
				if(row == null) {
					openStatements.remove(i);
					openStmtExcept.remove(i);
				}
			}
			
			final int cushion = 3;
			if(openStatements.size() > cushion) {
				Out.info(getClass(), "Out of " + original + " cached statements, " + openStatements.size() + " were left open (cushion=" + cushion + ")");
				
				//Close all but the last cushion statements
				for(int i = 0; i < openStatements.size() - cushion; i++) {
					Statement s = openStatements.get(i);
					try {
						close(s);
					} catch (SQLException e) {
						Out.exception(e);
					}
					Out.exception(openStmtExcept.get(i));
				}
			}
		}

		openStmtExcept.add(new Exception());
		openStatements.add(stmt);
		return stmt;
	}

	public Statement createStatement() throws SQLException {
		return pushStatement(conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return (PreparedStatement)pushStatement(conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
	}
	
	public BNLoginResultSet getUser(BNetUser user) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM bnlogin WHERE LOWER(login)=LOWER(?)");
		ps.setString(1, user.getFullAccountName());
		return new BNLoginResultSet(ps.executeQuery());
	}
	
	public void deleteOldUsers() throws SQLException {
		String SQL = "DATEDIFF(NOW(), lastSeen)";
		try {
			if(conn.getClass().getName().startsWith("org.apache.derby"))
				SQL = "{fn TIMESTAMPDIFF(SQL_TSI_DAY, CURRENT_TIMESTAMP, lastSeen)}";
		} catch(NoClassDefFoundError e) {}
		
		SQL =
			"SELECT login, " + SQL + " as dss, rank.id AS rank, rank.expireDays " +
				"FROM bnlogin " +
				"JOIN account ON (bnlogin.account=account.id) " +
				"JOIN rank ON (account.access=rank.id) " +
			"UNION " +
			"SELECT login, " + SQL + " as dss, 0 AS rank, 90 AS expireDays " +
				"FROM bnlogin " +
				"WHERE account IS NULL " +
			"ORDER BY dss DESC";
	
		ResultSet rsOld = createStatement().executeQuery(SQL);
		while(rsOld.next()) {
			long dss = rsOld.getLong("dss");
			long expireDays = rsOld.getLong("expireDays");
			if(dss > expireDays) {
				String login = rsOld.getString("login");
	
				BNLoginResultSet rsUser = getUser(new BNetUser(login));
				if(rsUser.next()) {
					Long rank = rsOld.getLong("rank");
					if(rsOld.wasNull())
						rank = null;
					
					String out = "Removing user ";
					out += login;
					out += " (";
					if((rank != null) && (rank != 0))
						out += "rank=" + rank + ", ";
					out += "dss=";
					out += dss;
					out += "/";
					out += expireDays;
					out += ")";
					Out.info(getClass(), out);
	
					//Delete them!
					rsUser.deleteRow();
				}
				close(rsUser);
			}
		}
		close(rsOld);
		
		//Find accounts that are not instrumental to the recruitment tree, and have no accounts
		AccountResultSet rsAccount = getAccounts();
		while(rsAccount.next()) {
			//Check number of connected logins
			{
				PreparedStatement ps = prepareStatement("SELECT COUNT(*) FROM bnlogin WHERE bnlogin.account=?");
				ps.setLong(1, rsAccount.getId());
				ResultSet rsLogins = ps.executeQuery();
				if(!rsLogins.next())
					throw new SQLException("fetch failed");
				long logins = rsLogins.getLong(1);
				close(rsLogins);
				if(logins > 0)
					continue;
			}
			
			// Check if they have recruits
			if(getAccountRecruits(rsAccount.getId()) > 0)
				continue;
			
			Long cb = rsAccount.getCreatedBy();
			
			String out = "Removing account ";
			out += rsAccount.getName();
			out += " (rank=";
			out += rsAccount.getAccess();
			if(cb != null) {
				out += ", createdby=";
				out += cb;
			}
			out += ")";
			Out.info(getClass(), out);
			
			if(cb != null)
				sendMail(cb, cb, "Your recruit " + rsAccount.getName() + " has been removed due to inactivity");

			//Restore the cursor to the appropriate row
			deleteAccount(rsAccount.getId());
		}
		close(rsAccount);
	}
	
	public BNLoginResultSet getCreateUser(BNetUser user) throws SQLException {
		BNLoginResultSet rsUser = getUser(user);
		if(rsUser.next()) {
			rsUser.beforeFirst();
			return rsUser;
		}
		close(rsUser);
		
		PreparedStatement ps = prepareStatement("INSERT INTO bnlogin (login, lastSeen) VALUES(?, CURRENT_TIMESTAMP)");
		ps.setString(1, user.getFullAccountName());
		ps.execute();
		close(ps);
		
		rsUser = getUser(user);
		if(rsUser.next()) {
			rsUser.beforeFirst();
			return rsUser;
		}
		close(rsUser);
		
		throw new SQLException("The user was created but not found");
	}
	
	public AccountResultSet getAccount(String account) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM account WHERE LOWER(name)=LOWER(?)");
		ps.setString(1, account);
		return new AccountResultSet(ps.executeQuery());
	}
	
	public AccountResultSet getAccount(Long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM account WHERE id=?");
		ps.setLong(1, accountID);
		return new AccountResultSet(ps.executeQuery());
	}
	
	public AccountResultSet getAccount(BNetUser user) throws SQLException {
		PreparedStatement ps = prepareStatement(
				"SELECT A.* " +
				"FROM account AS A " +
				"JOIN bnlogin AS U " +
					"ON A.id=U.account " +
				"WHERE LOWER(U.login)=LOWER(?)");
		String login = user.getFullAccountName();
		if(login == null)
			ps.setNull(1, java.sql.Types.VARCHAR);
		else
			ps.setString(1, login);
		return new AccountResultSet(ps.executeQuery());
	}
	
	public void deleteAccount(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("DELETE FROM account WHERE id=?");
		ps.setLong(1, accountID);
		ps.execute();
		close(ps);
	}
	
	public long[] getAccountWinsLevels(long accountID, String prefix, String suffix) throws SQLException {
		int questionMark = 1;
		String SQL = "SELECT SUM(winsSTAR), SUM(winsSEXP), SUM(winsW2BN), MAX(levelD2), MAX(levelW3) FROM bnlogin WHERE ";
		if(prefix != null)
			SQL += "login LIKE ? AND ";
		
		//TODO: Fix this so it actually works; PREFIX-USER-SUFFIX@REALM will break it!
		if(suffix != null)
			SQL += "login LIKE ? AND ";
		
		SQL += "account=?";
		PreparedStatement ps = prepareStatement(SQL);
		if(prefix != null)
			ps.setString(questionMark++, prefix + "%");
		if(suffix != null)
			ps.setString(questionMark++, "%" + suffix);
		ps.setLong(questionMark++, accountID);
		ResultSet rs = ps.executeQuery();
		if(!rs.next()) {
			close(rs);
			throw new SQLException("getAccountWinsLevels query failed");
		}
		long w[] = new long[3];
		w[0] = rs.getLong(1) + rs.getLong(2) + rs.getLong(3);
		w[1] = rs.getLong(4);
		w[2] = rs.getLong(5);
		close(rs);
		return w;
	}
	
	public BNLoginResultSet getAccountUsers(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM bnlogin WHERE account=?");
		ps.setLong(1, accountID);
		return new BNLoginResultSet(ps.executeQuery());
	}
	
	public AccountResultSet getAccountRecruits(long accountID, long withAccess) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM account WHERE createdby=? AND access>=?");
		ps.setLong(1, accountID);
		ps.setLong(2, withAccess);
		return new AccountResultSet(ps.executeQuery());
	}
	
	public long getAccountRecruits(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT COUNT(*) FROM account WHERE createdby=?");
		ps.setLong(1, accountID);
		ResultSet rs = ps.executeQuery();
		if(!rs.next())
			throw new SQLException("fetch failed");
		long num = rs.getLong(1);
		close(rs);
		return num;
	}
	
	public long getAccountRecruitScore(long accountID, long withAccess) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT SUM(access-?) FROM account WHERE createdby=? AND access>=?");
		ps.setLong(1, withAccess);
		ps.setLong(2, accountID);
		ps.setLong(3, withAccess);
		ResultSet rs = ps.executeQuery();
		if(!rs.next()) {
			close(rs);
			throw new SQLException("Fetch failed");
		}
		long score = rs.getLong(1);
		close(rs);
		return score;
	}

	public AccountResultSet createAccount(String account, long access, Long creator) throws SQLException {
		PreparedStatement ps = prepareStatement("INSERT INTO account (name, access, createdby, lastRankChange) VALUES(?, ?, ?, ?)");
		ps.setString(1, account);
		ps.setLong(2, access);
		if(creator == null)
			ps.setNull(3, java.sql.Types.INTEGER);
		else
			ps.setLong(3, creator);
		ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
		ps.execute();
		close(ps);
		
		AccountResultSet rsAccount = getAccount(account);
		if((rsAccount == null) || (!rsAccount.next()))
			throw new SQLException("The account was created but not found");
		
		rsAccount.beforeFirst();
		return rsAccount;
	}

	public AccountResultSet createAccount() throws SQLException {
		return createAccount("NEW_ACCOUNT_" + new java.util.Date().getTime(), 0, null);
	}
	
	public AccountResultSet getRankedAccounts(long minRank) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM account WHERE access>=?");
		ps.setLong(1, minRank);
		return new AccountResultSet(ps.executeQuery());
	}
	
	public AccountResultSet getAccounts() throws SQLException {
		return new AccountResultSet(createStatement().executeQuery("SELECT * FROM account ORDER BY LOWER(name) ASC"));
	}
	
	public Long createRank() throws SQLException {
		Statement stmt = createStatement();
		stmt.executeUpdate("INSERT INTO rank (id) VALUES (NULL)");
		ResultSet rs = stmt.getGeneratedKeys();
		if(rs.next()) {
			long id = rs.getLong(1);
			close(stmt);
			return id;
		}
		close(stmt);
		return null;
	}
	
	public RankResultSet getRanks() throws SQLException {
		return new RankResultSet(createStatement().executeQuery("SELECT * FROM rank ORDER BY id ASC"));
	}
	
	public RankResultSet getRank(long access) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM rank WHERE id=?");
		ps.setLong(1, access);
		return new RankResultSet(ps.executeQuery());
	}
	
	//Cache command alias mappings to improve performance
	private static Hashtable<String, String> aliases = new Hashtable<String, String>();
	public String resolveCommandAlias(String command) throws SQLException {
		String tmp = aliases.get(command);
		if(tmp != null)
			return tmp;
		
		PreparedStatement ps = prepareStatement("SELECT name FROM command_alias WHERE alias=?");
		ps.setString(1, command);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			String name = rs.getString(1);
			close(rs);
			//Out.info(getClass(), "Alias " + command + " resolves to " + name + "; caching");
			aliases.put(command, name);
			return name;
		}
		close(rs);
		//Out.info(getClass(), "Command " + command + " is not an alias; caching");
		aliases.put(command, command);
		return command;
	}

	public CommandResultSet getCommand(String command) throws SQLException {
		command = resolveCommandAlias(command);
		PreparedStatement ps = prepareStatement("SELECT * FROM command WHERE name=?");
		ps.setString(1, command);
		return new CommandResultSet(ps.executeQuery());
	}

	public CommandResultSet getCommands(long access) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM command WHERE access<=?");
		ps.setLong(1, access);
		return new CommandResultSet(ps.executeQuery());
	}
	
	public CommandResultSet getCommandCategory(String category, long access) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM command WHERE cmdgroup=? AND access<=?");
		ps.setString(1, category);
		ps.setLong(2, access);
		return new CommandResultSet(ps.executeQuery());
	}

	public CommandResultSet getCommandCategories(long access) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT cmdgroup FROM command WHERE access<=? GROUP BY cmdgroup");
		ps.setLong(1, access);
		return new CommandResultSet(ps.executeQuery());
	}
	
	public void sendMail(long senderID, long targetID, String message) throws SQLException {
		PreparedStatement ps = prepareStatement("INSERT INTO mail (sentfrom, sentto, message, sent) VALUES (?, ?, ?, CURRENT_TIMESTAMP)");
		ps.setLong(1, senderID);
		ps.setLong(2, targetID);
		ps.setString(3, message);
		ps.execute();
		close(ps);
	}
	
	public long getUnreadMailCount(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT COUNT(*) FROM mail WHERE sentto=? AND isread=0");
		ps.setLong(1, accountID);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			long c = rs.getLong(1);
			close(rs);
			return c;
		}
		close(rs);
		throw new SQLException("COUNT(*) returned 0 rows");
	}
	
	public long getMailCount(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT COUNT(*) FROM mail WHERE sentto=?");
		ps.setLong(1, accountID);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			long c = rs.getLong(1);
			close(rs);
			return c;
		}
		close(rs);
		throw new SQLException("COUNT(*) returned 0 rows");
	}
	
	public void clearMail(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("DELETE FROM mail WHERE sentto=? AND isread=?");
		ps.setLong(1, accountID);
		ps.setBoolean(2, true);
		ps.execute();
		close(ps);
	}
	
	public ResultSet getMail(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT M.id, A.name, M.sent, M.isread, M.message FROM mail AS M JOIN account AS A ON (A.id = M.sentfrom) WHERE M.sentto=? ORDER BY M.id ASC");
		ps.setLong(1, accountID);
		return ps.executeQuery();
	}
	
	public void setMailRead(long mailID) throws SQLException {
		PreparedStatement ps = prepareStatement("UPDATE mail SET isread=? WHERE id=?");
		ps.setBoolean(1, true);
		ps.setLong(2, mailID);
		ps.execute();
		close(ps);
	}
	
	public ResultSet getTriviaLeaders() throws SQLException {
		return createStatement().executeQuery("SELECT * FROM account WHERE trivia_correct > 0 ORDER BY trivia_correct DESC");
	}
	
	public long getTriviaSum() throws SQLException {
		ResultSet rs = createStatement().executeQuery("SELECT SUM(trivia_correct) FROM account");
		rs.next();
		long sum = rs.getLong(1);
		close(rs);
		return sum;
	}
	
	public long[] getTriviaTopTwo() throws SQLException {
		ResultSet rs = createStatement().executeQuery("SELECT trivia_correct FROM account ORDER BY trivia_correct DESC");
		if(rs.next()) {
			long top1 = rs.getLong(1);
			if(!rs.next()) {
				close(rs);
				return null;
			}
			
			long top2 = rs.getLong(1);
			close(rs);
			return new long[] {top1, top2};
		}
		return null;
	}
	
	/**
	 * Resets the trivia leader board, gives the winner a trivia_win, and returns the winner
	 * @return The account.name of the winner
	 * @throws SQLException
	 */
	public String resetTrivia() throws SQLException {
		String out = null;
		ResultSet rs = createStatement().executeQuery("SELECT MAX(trivia_correct) FROM account");
		if(!rs.next())
			throw new SQLException("query failed");
		long correct = rs.getLong(1);
		close(rs);
		
		PreparedStatement ps = prepareStatement("SELECT id, name, trivia_win FROM account WHERE trivia_correct=?");
		ps.setLong(1, correct);
		rs = ps.executeQuery();
		while(rs.next()) {
			//Get the account it
			if(out == null)
				out = rs.getString(2);
			else
				out += " and " + rs.getString(2);
			//trivia_wins++
			rs.updateLong(3, rs.getLong(3)+1);
			//Commit
			rs.updateRow();
		}
		close(rs);
		
		Statement stmt = createStatement();
		stmt.execute("UPDATE account SET trivia_correct=0");
		close(stmt);
		return out;
	}
	
	/**
	 * Check whether or not the database schema is valid
	 * @return boolean indicating if database is up to date
	 * @throws SQLException 
	 */
	private boolean checkSchema() {
		ResultSet rs = null;
		try {
			rs = createStatement().executeQuery("SELECT version FROM dbVersion");
		} catch(SQLException e) {
			return false;
		}
		
		try {
			if(!rs.next()) {
				close(rs);
				return false;
			}
			
			long version = rs.getLong(1);
			if(version >= compatibleVersion) {
				close(rs);
				return true;
			}
			
			Out.error(getClass(), "Database version is " + version + ", we require " + compatibleVersion);
		} catch(SQLException e) {
			Out.exception(e);
		}
		
		if(rs != null)
			close(rs);
		return false;
	}
	
	private void createSchema(String schemaFile) throws SQLException {
		Out.info(getClass(), "The database requires rebuilding.");
		
		Statement stmt = createStatement();
		
		BufferedReader fr;
		try {
			fr = new BufferedReader(new FileReader(new File(schemaFile)));
		} catch (FileNotFoundException e) {
			throw new SQLException("File not found: " +schemaFile);
		}
		
		String query = "";
		try {
			while(fr.ready()) {
				if(query.length() == 0)
					query = fr.readLine();
				else
					query += '\n' + fr.readLine(); 
						
				if(query.length() == 0)
					continue;
				
				if(query.charAt(0) == '#') {
					query = "";
					continue;
				}
				
				if(query.charAt(query.length()-1) == ';') {
					query = query.substring(0, query.length()-1);
					stmt.execute(query);
					query = "";
				}
			}

			query = "DROP TABLE dbVersion";
			try { stmt.execute(query); } catch(SQLException e) {}
			query = "CREATE TABLE dbVersion (version INTEGER NOT NULL)";
			stmt.execute(query);
			query = "INSERT INTO dbVersion (version) VALUES (" + databaseVersion + ")";
			stmt.execute(query);
			close(stmt);
		} catch(IOException e) {
			Out.fatalException(e);
		} catch(SQLException e) {
			Out.error(getClass(), "Failed to create schema\n" + query + "\n\n" + e.getMessage());
			throw e;
		}
	}
}
