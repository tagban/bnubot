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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.swing.JOptionPane;

import net.bnubot.bot.gui.wizard.DatabaseWizard;
import net.bnubot.core.PluginManager;
import net.bnubot.db.Account;
import net.bnubot.logging.Out;
import net.bnubot.settings.DatabaseSettings;
import net.bnubot.settings.Settings;

import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.DbGenerator;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.merge.AddColumnToDb;
import org.apache.cayenne.merge.AddRelationshipToDb;
import org.apache.cayenne.merge.CreateTableToDb;
import org.apache.cayenne.merge.DbMerger;
import org.apache.cayenne.merge.DropTableToDb;
import org.apache.cayenne.merge.ExecutingMergerContext;
import org.apache.cayenne.merge.MergerContext;
import org.apache.cayenne.merge.MergerToken;
import org.apache.cayenne.merge.SetAllowNullToDb;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.validation.ValidationFailure;
import org.apache.cayenne.validation.ValidationResult;

/**
 * @author scotta
 */
public class SchemaValidator {
	private static final long databaseVersion = 2;		// Current schema version
	private static final long compatibleVersion = 2;	// Minimum version compatible

	private static Connection conn = null;

	public static boolean validate() {
		DatabaseSettings settings = new DatabaseSettings();
		settings.load();

		// Get the Cayenne map
		DataDomain domain = Configuration.getSharedConfiguration().getDomain("BNUBotDomain");
		DataNode dataNode = domain.getNode("BNUBotDataNode");
		DataMap dataMap = domain.getMap("BNUBotMap");

		DataSource dataSource = dataNode.getDataSource();
		DbAdapter adapter = dataNode.getAdapter();

		try {
			// Check if the schema is up to par
			boolean schemaValid = checkSchema(dataSource);
			if(conn != null) {
				conn.close();
				conn = null;
			}

			// Bring the schema up to date by merging the differences
			ValidationResult vr = null;
			if(!schemaValid) {
				// Invalid schema
				Out.error(SchemaValidator.class, "The database requires rebuilding");

				// Generate schema from the mapping file
				DbGenerator generator = new DbGenerator(adapter, dataMap);
				generator.setShouldCreateFKConstraints(adapter.supportsFkConstraints());
				generator.setShouldCreatePKSupport(!adapter.supportsGeneratedKeys());
				generator.setShouldCreateTables(true);
				generator.setShouldDropPKSupport(true);
				generator.setShouldDropTables(true);
				generator.runGenerator(dataSource);

				vr = generator.getFailures();

				Out.info(SchemaValidator.class, "Database rebuild complete");
			} else {
				// Valid schema; check if merge is needed
				Out.debug(SchemaValidator.class, "The database schema is valid; checking if upgrade is needed");

				MergerContext mc = new ExecutingMergerContext(dataMap, dataNode);
				List<MergerToken> allMergeTokens = new DbMerger().createMergeTokens(dataNode, dataMap);

				List<MergerToken> nonAutoMergeTokens = new ArrayList<MergerToken>();
				for(MergerToken mt : allMergeTokens) {
					if(mt instanceof DropTableToDb) {
						// Why would you want to drop AUTO_PK_SUPPORT?
						DropTableToDb drop = (DropTableToDb)mt;
						if(drop.getEntity().getName().equals("AUTO_PK_SUPPORT"))
							continue;
					}

					if((mt instanceof AddRelationshipToDb)
					|| (mt instanceof CreateTableToDb)
					|| (mt instanceof AddColumnToDb)
					|| (mt instanceof SetAllowNullToDb)) {
						// These non-destructive types are allowed to merge in automatically
						Out.info(SchemaValidator.class, mt.toString());
						mt.execute(mc);
					} else {
						// All other types require user approval
						nonAutoMergeTokens.add(mt);
					}
				}

				if(nonAutoMergeTokens.size() == 0) {
					Out.debug(SchemaValidator.class, "No upgrade necessary");
					checkRunWizard(schemaValid);
					return true;
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
					Out.info(SchemaValidator.class, mt.toString());
					mt.execute(mc);
				}

				vr = mc.getValidationResult();
			}

			if(vr != null) {
				for(ValidationFailure vf : vr.getFailures()) {
					// Don't bother the user with these
					if(vf.getSource().toString().startsWith("DROP TABLE "))
						continue;
					Out.error(SchemaValidator.class,
							vf.getDescription().toString() + "\n" +
							vf.getSource().toString());
				}
			}

			if(!schemaValid) {
				// Insert default values
				Out.info(SchemaValidator.class, "Adding default values to database");

				insertDefault(dataSource, "schema.sql", "org.sqlite.JDBC".equals(settings.driver));
				if(conn != null) {
					conn.close();
					conn = null;
				}

				Out.info(SchemaValidator.class, "Default values added");
			}

			checkRunWizard(schemaValid);
			return true;
		} catch(Exception e) {
			Out.exception(e);
			return false;
		}
	}

	/**
	 * Check if the DB wizard can and should run
	 */
	private static void checkRunWizard(boolean schemaValid) {
		// Don't display the wizard if the GUI is disabled
		if(!PluginManager.getEnableGui())
			return;

		// If the schema was not rebuilt...
		if(schemaValid) {
			// Search for any account in the DB
			SelectQuery query = new SelectQuery(Account.class);
			List<?> accounts = DatabaseContext.getContext().performQuery(query);
			// Only open the wizard if there are no accounts
			if(accounts.size() > 0)
				return;
		}

		// There are no accounts in the database; run the wizard
		new DatabaseWizard();
	}

	/**
	 * Check whether or not the database schema is valid
	 * @return boolean indicating if database is up to date
	 * @throws SQLException
	 */
	private static boolean checkSchema(DataSource dataSource) {
		ResultSet rs = null;
		try {
			rs = createStatement(dataSource).executeQuery("SELECT version FROM dbVersion");
			if(rs.next()) {
				long version = rs.getLong(1);
				if(version >= compatibleVersion) {
					close(rs);
					return true;
				}

				Out.error(SchemaValidator.class, "Database version is " + version + ", we require " + compatibleVersion);
			}
		} catch(SQLException e) {}

		if(rs != null)
			close(rs);
		return false;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	private static Statement createStatement(DataSource dataSource) throws SQLException {
		if(conn == null)
			conn = dataSource.getConnection();
		return conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	private static void insertDefault(DataSource dataSource, String schemaFile, boolean isSQLite) throws SQLException {
		Statement stmt = createStatement(dataSource);

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
			Out.error(SchemaValidator.class, "Failed to create schema\n" + query + "\n\n" + e.getMessage());
			throw e;
		}
	}

	private static void close(ResultSet rs) {
		try {
			rs.getStatement().close();
		} catch (SQLException e) {
			Out.exception(e);
		}
	}
}
