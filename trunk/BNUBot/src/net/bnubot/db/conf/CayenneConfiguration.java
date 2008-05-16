/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import net.bnubot.JARLoader;
import net.bnubot.settings.DatabaseSettings;
import net.bnubot.util.Out;

import org.apache.cayenne.access.ConnectionLogger;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conf.DataSourceFactory;
import org.apache.cayenne.conn.PoolManager;

public class CayenneConfiguration implements DataSourceFactory {
	private static final long databaseVersion = 2;		// Current schema version
	private static final long compatibleVersion = 2;	// Minimum version compatible
	public static boolean firstRun = false;

	private Connection conn = null;

	public CayenneConfiguration() throws Exception {
		super();
	}

	public DataSource getDataSource(String location) throws Exception {
		DatabaseSettings settings = new DatabaseSettings();
		settings.load();

		// Set up the driver, in case it's not on the classpath
		try {
			DriverManager.getDriver(settings.url);
		} catch(SQLException e) {
			Out.debug(getClass(), "Registering " + settings.driver);
			Driver d = (Driver)JARLoader.forName(settings.driver).newInstance();
			DriverManager.registerDriver(new DriverShim(d));
		}

		// Connect
		Out.debug(getClass(), "Connecting to " + settings.url);
		PoolManager poolManager = new PoolManager(
				null, // Setting this to null will force Cayenne to use the DriverManager
				settings.url,
				1,
				1,
				settings.username,
				settings.password,
				new ConnectionLogger());
		conn = poolManager.getConnection();

		// Check if the schema is up to par
		if(!checkSchema()) {
			firstRun = true;
			createSchema(settings.schema);
		}
		//deleteOldUsers();

		conn.close();

		// All done!
		return poolManager;
	}

	public void initializeWithParentConfiguration(Configuration parentConfiguration) {}

	/**
	 * @return
	 * @throws SQLException
	 */
	private Statement createStatement() throws SQLException {
		return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public void close(Statement stmt) throws SQLException {
		stmt.close();

		/*int i = openStatements.indexOf(stmt);
		if(i == -1)
			throw new IllegalStateException("Statement not found in cache");

		openStatements.remove(i);
		openStmtExcept.remove(i);*/
	}

	public void close(ResultSet rs) {
		try {
			close(rs.getStatement());
		} catch (SQLException e) {
			Out.exception(e);
		}
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
			if(rs.next()) {
				long version = rs.getLong(1);
				if(version >= compatibleVersion) {
					close(rs);
					return true;
				}

				Out.error(getClass(), "Database version is " + version + ", we require " + compatibleVersion);
			}
		} catch(SQLException e) {}

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

	/*public void deleteOldUsers() throws SQLException {
		String SQL;
		if(conn.getClass().getName().startsWith("org.apache.derby"))
			SQL = "{fn TIMESTAMPDIFF(SQL_TSI_DAY, CURRENT_TIMESTAMP, lastSeen)}";
		else
			SQL = "DATEDIFF(NOW(), lastSeen)";

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
			long expireDays = rsOld.getLong("expireDays");
			if(expireDays == 0)
				continue;
			long dss = rsOld.getLong("dss");
			if(dss > expireDays) {
				String login = rsOld.getString("login");

				BNLoginResultSet rsUser = getUser(new BNetUser(null, login));
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
	}*/
}
