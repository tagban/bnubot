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
import net.bnubot.bot.gui.DatabaseWizard;
import net.bnubot.settings.DatabaseSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.Out;

import org.apache.cayenne.access.ConnectionLogger;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conf.DataSourceFactory;
import org.apache.cayenne.conn.PoolManager;

public class CayenneConfiguration implements DataSourceFactory {
	private static final long databaseVersion = 2;		// Current schema version
	private static final long compatibleVersion = 2;	// Minimum version compatible

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
			createSchema(settings.schema);
			if(GlobalSettings.enableGUI)
				new DatabaseWizard();
		}

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
}
