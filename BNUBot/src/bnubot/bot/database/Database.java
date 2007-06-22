package bnubot.bot.database;

import java.io.*;
import java.sql.*;
import java.util.Hashtable;

import bnubot.core.BNetUser;

public class Database {
	private static final long databaseVersion = 2;		// Current schema version
	private static final long compatibleVersion = 2;	// Minimum version compatible
	private Connection conn;
	
	public Database(String driver, String url, String username, String password, String schemaFile) throws SQLException, ClassNotFoundException {
		Class.forName(driver);
		
		System.out.println("Connecting to " + url);
		conn = DriverManager.getConnection(url, username, password);
		System.out.println("Connected!");
		
		if(!checkSchema())
			createSchema(schemaFile);
	}

	public Statement createStatement() throws SQLException {
		return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
	public ResultSet getUser(BNetUser user) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM `user` WHERE `login`=? LIMIT 1");
		ps.setString(1, user.getFullAccountName());
		return ps.executeQuery();
	}
	
	public ResultSet getCreateUser(BNetUser user) throws SQLException {
		ResultSet rsUser = getUser(user);
		if(rsUser.next())
			return getUser(user);
		
		PreparedStatement ps = prepareStatement("INSERT INTO `user` (`login`) VALUES(?)");
		ps.setString(1, user.getFullAccountName());
		ps.execute();
		ps.close();
		
		rsUser = getUser(user);
		if(rsUser.next())
			return getUser(user);
		
		throw new SQLException("The user was created but not found");
	}
	
	public ResultSet getAccount(String account) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM `account` WHERE `name`=? LIMIT 1");
		ps.setString(1, account);
		return ps.executeQuery();
	}
	
	public ResultSet getAccount(Long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM `account` WHERE `id`=? LIMIT 1");
		ps.setLong(1, accountID);
		return ps.executeQuery();
	}
	
	public ResultSet getAccount(BNetUser user) throws SQLException {
		/*ResultSet rsUser = getUser(user);
		if(!rsUser.next())
			return null;
		
		String account = rsUser.getString("account");
		if(account == null)
			return null;
		return getAccount(account);*/

		PreparedStatement ps = prepareStatement(
				"SELECT A.* " +
				"FROM `account` AS A " +
				"JOIN `user` AS U " +
					"ON A.`id`=U.`account` " +
				"WHERE U.`login`=? " +
				"LIMIT 1");
		ps.setString(1, user.getFullAccountName());
		return ps.executeQuery();
	}
	
	public long[] getAccountWinsLevels(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT SUM(winsSTAR), SUM(winsSEXP), SUM(winsW2BN), MAX(levelD2), MAX(levelW3) FROM `user` WHERE `account`=? LIMIT 1");
		ps.setLong(1, accountID);
		ResultSet rs = ps.executeQuery();
		if(!rs.next())
			throw new SQLException("getAccountWinsLevels query failed");
		long w[] = new long[3];
		w[0] = rs.getLong(1) + rs.getLong(2) + rs.getLong(3);
		w[1] = rs.getLong(4);
		w[2] = rs.getLong(5);
		rs.close();
		return w;
	}
	
	public ResultSet getAccountUsers(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM `user` WHERE `account`=?");
		ps.setLong(1, accountID);
		return ps.executeQuery();
	}

	public ResultSet createAccount(String account, long access, Long creator) throws SQLException {
		PreparedStatement ps = prepareStatement("INSERT INTO `account` (`name`, `access`, `createdby`) VALUES(?, ?, ?)");
		ps.setString(1, account);
		ps.setLong(2, access);
		ps.setLong(2, creator);
		ps.execute();
		ps.close();
		
		ResultSet rsAccount = getAccount(account);
		if((rsAccount == null) || (!rsAccount.next()))
			throw new SQLException("The account was created but not found");
		
		return rsAccount;
	}
	
	public ResultSet getCreateAccount(String account, Long access, Long creator) throws SQLException {
		ResultSet rsAccount = getAccount(account);
		if(rsAccount != null)
			return rsAccount;
		
		return createAccount(account, access, creator);
	}
	
	public ResultSet getRankedAccounts(long minRank) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM `account` WHERE `access`>=?");
		ps.setLong(1, minRank);
		return ps.executeQuery();
	}
	
	public ResultSet getRank(long access) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT * FROM `rank` WHERE `id`=?");
		ps.setLong(1, access);
		return ps.executeQuery();
	}
	
	//Cache command alias mappings to improve performance
	private static Hashtable<String, String> aliases = new Hashtable<String, String>();
	public String resolveCommandAlias(String command) throws SQLException {
		String tmp = aliases.get(command);
		if(tmp != null)
			return tmp;
		
		PreparedStatement ps = prepareStatement("SELECT `name` FROM `command_alias` WHERE `alias`=? LIMIT 1");
		ps.setString(1, command);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			String name = rs.getString(1);
			rs.close();
			//System.out.println("Alias " + command + " resolves to " + name + "; caching");
			aliases.put(command, name);
			return name;
		}
		rs.close();
		//System.out.println("Command " + command + " is not an alias; caching");
		aliases.put(command, command);
		return command;
	}

	public ResultSet getCommand(String command) throws SQLException {
		command = resolveCommandAlias(command);
		PreparedStatement ps = prepareStatement("SELECT * FROM `command` WHERE `name`=? LIMIT 1");
		ps.setString(1, command);
		return ps.executeQuery();
	}
	
	public void sendMail(long senderID, long targetID, String message) throws SQLException {
		PreparedStatement ps = prepareStatement("INSERT INTO `mail` (`from`, `to`, `message`) VALUES (?, ?, ?)");
		ps.setLong(1, senderID);
		ps.setLong(2, targetID);
		ps.setString(3, message);
		ps.execute();
	}
	
	public long getUnreadMailCount(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT COUNT(*) FROM `mail` WHERE `to`=? AND `read`=FALSE");
		ps.setLong(1, accountID);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			long c = rs.getLong(1);
			rs.close();
			return c;
		}
		rs.close();
		throw new SQLException("COUNT(*) returned 0 rows");
	}
	
	public long getMailCount(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT COUNT(*) FROM `mail` WHERE `to`=?");
		ps.setLong(1, accountID);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			long c = rs.getLong(1);
			rs.close();
			return c;
		}
		rs.close();
		throw new SQLException("COUNT(*) returned 0 rows");
	}
	
	public void clearMail(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("DELETE FROM `mail` WHERE `to`=? AND `read`=TRUE");
		ps.setLong(1, accountID);
		ps.execute();
		ps.close();
	}
	
	public ResultSet getMail(long accountID) throws SQLException {
		PreparedStatement ps = prepareStatement("SELECT M.`id`, M.`sent`, A.`name`, M.`read`, M.`message` FROM `mail` AS M JOIN `account` AS A ON (A.`id` = M.`from`) WHERE M.`to`=? ORDER BY M.`id` ASC");
		ps.setLong(1, accountID);
		return ps.executeQuery();
	}
	
	public void setMailRead(long mailID) throws SQLException {
		PreparedStatement ps = prepareStatement("UPDATE `mail` SET `read`=TRUE WHERE `id`=? LIMIT 1");
		ps.setLong(1, mailID);
		ps.execute();
		ps.close();
	}
	
	/**
	 * Check whether or not the database schema is valid
	 * @return boolean indicating if database is up to date
	 * @throws SQLException 
	 */
	private boolean checkSchema() {
		ResultSet rs = null;
		try {
			rs = createStatement().executeQuery("SELECT `version` FROM `dbVersion` LIMIT 1");
			if(!rs.next()) {
				rs.close();
				return false;
			}
			
			long version = rs.getLong(1);
			if(version >= compatibleVersion) {
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
	
	private void createSchema(String schemaFile) throws SQLException {
		System.out.println("The database requires rebuilding.");
		
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
					stmt.execute(query);
					query = "";
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch(SQLException e) {
			System.err.println("Failed to create schema\n" + query + "\n\n" + e.getMessage());
			throw e;
		}
		
		stmt.execute("DROP TABLE `dbVersion`;");
		stmt.execute("CREATE TABLE `dbVersion` (`version` INTEGER NOT NULL);");
		stmt.execute("INSERT INTO `dbVersion` (`version`) VALUES (" + databaseVersion + ");");
		stmt.close();
	}
}
