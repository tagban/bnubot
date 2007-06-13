package bnubot.bot.database;

import java.sql.*;

import bnubot.core.BNetUser;

public class Database {
	private static final long databaseVersion = 0;		// Current schema version
	private static final long compatibleVersion = 0;	// Minimum version compatible
	private Connection conn;
	
	public Database(String driver, String url, String username, String password) throws SQLException {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Connecting to " + url);
		conn = DriverManager.getConnection(url, username, password);
		System.out.println("Connected!");
		
		if(!checkSchema())
			createSchema();
	}
	
	/*public Database(File f) throws SQLException {
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		boolean fileExists = f.exists();
		conn = DriverManager.getConnection("jdbc:sqlite:" + f.getName());
		Statement stmt = conn.createStatement();
		try {
			stmt.execute("PRAGMA synchronous = FULL;");
			stmt.close();
		} catch(SQLException e) {
			File newName = new File("database-corrupt-" + String.format("%1$tY-%1$tm-%1$te %1$tH-%1$tM-%1$tS", new GregorianCalendar()) + ".db");
			System.out.println("Database is damaged; renaming to " + newName.getName());
			conn.close();
			f.renameTo(newName);
			
			conn = DriverManager.getConnection("jdbc:sqlite:" + f.getName());
			stmt = conn.createStatement();
			stmt.execute("PRAGMA synchronous = FULL;");
			stmt.close();
		}
	    
		if(fileExists) {
			if(!checkSchema())
				createSchema();
		}
		else
			createSchema();
	}*/

	public Statement createStatement() throws SQLException {
		return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
	public ResultSet getUser(BNetUser user) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM user WHERE login=?");
		ps.setString(1, user.getFullAccountName().toLowerCase());
		return ps.executeQuery();
	}
	
	public ResultSet getCreateUser(BNetUser user) throws SQLException {
		ResultSet rsUser = getUser(user);
		if(rsUser.next())
			return getUser(user);
		
		PreparedStatement ps = prepareStatement("INSERT INTO user (login) VALUES(?)");
		ps.setString(1, user.getFullAccountName().toLowerCase());
		ps.execute();
		ps.close();
		
		rsUser = getUser(user);
		if(rsUser.next())
			return getUser(user);
		
		throw new SQLException("The user was created but not found");
	}
	
	public ResultSet getAccount(String account) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM account WHERE name=?");
		ps.setString(1, account.toLowerCase());
		return ps.executeQuery();
	}
	
	public ResultSet getAccount(BNetUser user) throws SQLException {
		ResultSet rsUser = getUser(user);
		if(!rsUser.next())
			return null;
		
		String account = rsUser.getString("account");
		if(account == null)
			return null;
		return getAccount(account);
	}
	
	public ResultSet getAccountUsers(String account) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM user WHERE account=?");
		ps.setString(1, account.toLowerCase());
		return ps.executeQuery();
	}

	public ResultSet createAccount(String account, Long access) throws SQLException {
		PreparedStatement ps = prepareStatement("INSERT INTO account (name, access) VALUES(?, ?)");
		ps.setString(1, account.toLowerCase());
		ps.setLong(2, access);
		ps.execute();
		ps.close();
		
		ResultSet rsAccount = getAccount(account);
		if(rsAccount == null)
			throw new SQLException("The account was created but not found");
		
		return rsAccount;
	}
	
	public ResultSet getCreateAccount(String account, Long access) throws SQLException {
		ResultSet rsAccount = getAccount(account);
		if(rsAccount != null)
			return rsAccount;
		
		return createAccount(account, access);
	}
	
	public void setAccountAccess(String account, long access) throws SQLException {
		PreparedStatement ps = prepareStatement("UPDATE account SET access=? WHERE name=?");
		ps.setLong(1, access);
		ps.setString(2, account.toLowerCase());
		ps.execute();
		int uc = ps.getUpdateCount();
		ps.close();
		
		if(uc != 1)
			throw new SQLException("Affected rows was " + uc);
	}
	

	public void setUserAccount(BNetUser user, String account) throws SQLException {
		ResultSet rsAccount = getAccount(account);
		if(!rsAccount.next())
			throw new SQLException("The account [" + account + "] does not exist");
		
		
		PreparedStatement ps = prepareStatement("UPDATE user SET account=? WHERE login=?");
		ps.setString(1, account.toLowerCase());
		ps.setString(2, user.getFullAccountName().toLowerCase());
		ps.execute();
		int uc = ps.getUpdateCount();
		ps.close();
		
		if(uc == 0)
			throw new SQLException("The user [" + user.getFullAccountName() + "] does not exist");
		
		if(uc != 1)
			throw new SQLException("Affected rows was " + uc);
	}
	
	/**
	 * Check whether or not the database schema is valid
	 * @return boolean indicating if database is up to date
	 * @throws SQLException 
	 */
	private boolean checkSchema() {
		ResultSet rs = null;
		try {
			rs = createStatement().executeQuery("SELECT version FROM dbVersion;");
			if(!rs.next()) {
				rs.close();
				return false;
			}
			
			long version = rs.getLong(1);
			if(version >= compatibleVersion) {
				System.out.println("The database is up to date.");
				rs.close();
				return true;
			}
			
			System.err.println("Database version is " + version + ", we require " + compatibleVersion);
		} catch(SQLException e) {
			System.err.println(e.getMessage());
		}
		
		if(rs != null)
			try { rs.close(); } catch(SQLException e) {}
		return false;
	}
	
	private void createSchema() throws SQLException {
		System.out.println("The database requires rebuilding.");
		
		Statement stmt = createStatement();

		stmt.execute("DROP TABLE IF EXISTS dbVersion;");
		stmt.execute("DROP TABLE IF EXISTS account;");
		stmt.execute("DROP TABLE IF EXISTS user;");
		stmt.execute("DROP TABLE IF EXISTS rank;");
		
		stmt.execute("CREATE TABLE dbVersion (version INTEGER NOT NULL);");
		stmt.execute("INSERT INTO dbVersion (version) VALUES (" + databaseVersion + ");");
		
		try {
			// SQLite
			stmt.execute(
				"CREATE TABLE account ( " +
					"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ON CONFLICT ROLLBACK, " +
					"access INTEGER NOT NULL, " +
					"name TEXT UNIQUE NOT NULL, " +
					"created INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP " +
				");");
		} catch(SQLException e) {
			// MySQL
			stmt.execute(
				"CREATE TABLE account ( " +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL, " +
					"access INTEGER NOT NULL, " +
					"name VARCHAR(32) UNIQUE NOT NULL, " +
					"created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP " +
				");");
		}
		
		/*PreparedStatement ps = conn.prepareStatement("INSERT INTO account (access, name) VALUES (?, ?);");
		ps.setInt(1, 100);
		ps.setString(2, "Camel");
		ps.execute();
		ps.close();*/

		try {
			// SQLite
			stmt.execute(
				"CREATE TABLE user ( " +
					"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ON CONFLICT ROLLBACK, " +
					"login TEXT UNIQUE NOT NULL, " +
					"account TEXT DEFAULT NULL, " +
					"created INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
					"lastSeen INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP " +
				");");
		} catch(SQLException e) {
			// MySQL
			stmt.execute(
				"CREATE TABLE user ( " +
					"id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL, " +
					"login VARCHAR(32) UNIQUE NOT NULL, " +
					"account VARCHAR(32) DEFAULT NULL, " +
					"created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
					"lastSeen TIMESTAMP NOT NULL " +
				");");
		}
		
		/*ps = conn.prepareStatement("INSERT INTO user (login, account) VALUES (?, ?);");
		ps.setString(1, "bnu-camel@useast");
		ps.setString(2, "Camel");
		ps.execute();*/

		try {
			// SQLite
			stmt.execute(
				"CREATE TABLE rank ( " +
					"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ON CONFLICT ROLLBACK, " +
					"name TEXT UNIQUE NOT NULL " +
				");");
		} catch(SQLException e) {
			// MySQL
			stmt.execute(
					"CREATE TABLE rank ( " +
						"id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL, " +
						"name VARCHAR(32) UNIQUE NOT NULL " +
					");");
		}
		
		/*ps = conn.prepareStatement("INSERT INTO rank (id, name) VALUES (?, ?);");
		ps.setInt(1, 100);
		ps.setString(2, "Master");
		ps.execute();
		ps.close();*/
		
		stmt.close();
	}
}
