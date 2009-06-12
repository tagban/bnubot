/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db.conf;

import java.awt.HeadlessException;
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
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.swing.JOptionPane;

import net.bnubot.JARLoader;
import net.bnubot.bot.gui.wizard.DatabaseWizard;
import net.bnubot.core.PluginManager;
import net.bnubot.settings.DatabaseSettings;
import net.bnubot.settings.Settings;
import net.bnubot.util.Out;

import org.apache.cayenne.access.ConnectionLogger;
import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.DbGenerator;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conf.DataSourceFactory;
import org.apache.cayenne.conn.PoolManager;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.merge.AddColumnToDb;
import org.apache.cayenne.merge.AddRelationshipToDb;
import org.apache.cayenne.merge.CreateTableToDb;
import org.apache.cayenne.merge.DbMerger;
import org.apache.cayenne.merge.ExecutingMergerContext;
import org.apache.cayenne.merge.MergerContext;
import org.apache.cayenne.merge.MergerToken;
import org.apache.cayenne.merge.SetAllowNullToDb;
import org.apache.cayenne.validation.ValidationFailure;
import org.apache.cayenne.validation.ValidationResult;

/**
 * @author scotta
 */
public class CayenneConfiguration implements DataSourceFactory {
	private static final long databaseVersion = 2;		// Current schema version
	private static final long compatibleVersion = 2;	// Minimum version compatible

	private static DataSource dataSource = null;
	private Connection conn = null;

	public CayenneConfiguration() throws Exception {
		super();
	}

	public DataSource getDataSource(String location) throws Exception {
		if(dataSource != null)
			return dataSource;

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
		dataSource = new PoolManager(
				null, // Setting this to null will force Cayenne to use the DriverManager
				settings.url,
				1,
				1,
				settings.username,
				settings.password,
				new ConnectionLogger());

		try {
			// Check if the schema is up to par
			boolean schemaValid = checkSchema();
			if(conn != null) {
				conn.close();
				conn = null;
			}

			// Bring the schema up to date by merging the differences
			DataDomain domain = Configuration.getSharedConfiguration().getDomain("BNUBotDomain");
			DataNode dataNode = domain.getNode("BNUBotDataNode");
			DataMap dataMap = domain.getMap("BNUBotMap");

			ValidationResult vr = null;
			if(!schemaValid) {
				// Invalid schema
				Out.error(getClass(), "The database requires rebuilding");

				// Generate schema from the mapping file
				DbGenerator generator = new DbGenerator(dataNode.getAdapter(), dataMap);
				generator.setShouldCreateFKConstraints(true);
				generator.setShouldCreatePKSupport(false);
				generator.setShouldCreateTables(true);
				generator.setShouldDropPKSupport(true);
				generator.setShouldDropTables(true);
				generator.runGenerator(dataSource);

				vr = generator.getFailures();

				Out.info(getClass(), "Database rebuild complete");
			} else {
				// Valid schema; check if merge is needed
				Out.debug(getClass(), "The database schema is valid; checking if upgrade is needed");

				MergerContext mc = new ExecutingMergerContext(dataMap, dataNode);
				List<MergerToken> allMergeTokens = new DbMerger().createMergeTokens(dataNode, dataMap);

				List<MergerToken> nonAutoMergeTokens = new ArrayList<MergerToken>();
				for(MergerToken mt : allMergeTokens) {
					if((mt instanceof AddRelationshipToDb)
					|| (mt instanceof CreateTableToDb)
					|| (mt instanceof AddColumnToDb)
					|| (mt instanceof SetAllowNullToDb)) {
						// These non-destructive types are allowed to merge in automatically
						Out.info(getClass(), mt.toString());
						mt.execute(mc);
					} else {
						// All other types require user approval
						nonAutoMergeTokens.add(mt);
					}
				}

				if(nonAutoMergeTokens.size() == 0) {
					Out.debug(getClass(), "No upgrade necessary");
					return dataSource;
				}

				try {
					String msg = "BNU-Bot must perform the following action(s) to upgrade your database:\n";
					for(MergerToken mt : nonAutoMergeTokens)
						msg += mt.toString() + "\n";
					msg += "\n" +
						"It is strongly recommended to backup your database before continuing!\n" +
						"Okay to continue?";
					if(JOptionPane.showConfirmDialog(
							null,
							msg,
							"Database schema update required",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE)
						!= JOptionPane.YES_OPTION) {
						throw new RuntimeException("User rejected schema update");
					}
				} catch(HeadlessException e) {
					// GUI is probably broken
					throw new Exception("A schema merge is required, but was unable to display " +
							"a window to query user if schema upgrade is acceptable.");
				}

				for(MergerToken mt : nonAutoMergeTokens) {
					Out.info(getClass(), mt.toString());
					mt.execute(mc);
				}

				vr = mc.getValidationResult();
			}

			for(ValidationFailure vf : vr.getFailures()) {
				// Don't bother the user with these
				if(vf.getSource().toString().startsWith("DROP TABLE "))
					continue;
				Out.error(getClass(),
						vf.getDescription().toString() + "\n" +
						vf.getSource().toString());
			}

			if(!schemaValid) {
				// Insert default values
				Out.info(getClass(), "Adding default values to database");

				insertDefault("schema.sql", "org.sqlite.JDBC".equals(settings.driver));
				if(conn != null) {
					conn.close();
					conn = null;
				}

				Out.info(getClass(), "Default values added");

				if(PluginManager.getEnableGui())
					new DatabaseWizard();
			}
		} catch(Exception e) {
			Out.error(getClass(), "Failed to initialize the database: " + e.getMessage());
			throw e;
		}

		// All done!
		return dataSource;
	}

	public void initializeWithParentConfiguration(Configuration parentConfiguration) {}

	/**
	 * @return
	 * @throws SQLException
	 */
	private Statement createStatement() throws SQLException {
		if(conn == null)
			conn = dataSource.getConnection();
		return conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	public void close(ResultSet rs) {
		try {
			rs.getStatement().close();
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

	private void insertDefault(String schemaFile, boolean isSQLite) throws SQLException {
		Statement stmt = createStatement();

		BufferedReader fr;
		try {
			fr = new BufferedReader(new FileReader(new File(Settings.getRootPath() + schemaFile)));
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

					if(isSQLite) {
						// SQLite doesn't support mutli-row insert
						String[] qp = query.split("\n", 2);
						String[] rows = qp[1].split(",\n");
						//Task t = TaskManager.createTask("Building table " + qp[0].split(" ", 4)[2], rows.length, "queries");
						for(String row : rows) {
							query = qp[0] + row;
							stmt.execute(query);
							//t.advanceProgress();
						}
						//t.complete();
					} else {
						stmt.execute(query);
					}

					query = "";
				}
			}

			query = "DROP TABLE dbVersion";
			try { stmt.execute(query); } catch(SQLException e) {}
			query = "CREATE TABLE dbVersion (version INTEGER NOT NULL)";
			stmt.execute(query);
			query = "INSERT INTO dbVersion (version) VALUES (" + databaseVersion + ")";
			stmt.execute(query);
			stmt.close();
		} catch(IOException e) {
			Out.fatalException(e);
		} catch(SQLException e) {
			Out.error(getClass(), "Failed to create schema\n" + query + "\n\n" + e.getMessage());
			throw e;
		}
	}
}
