/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db.conf;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.bnubot.JARLoader;
import net.bnubot.logging.Out;
import net.bnubot.settings.DatabaseSettings;

import org.apache.cayenne.access.ConnectionLogger;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conf.DataSourceFactory;
import org.apache.cayenne.conn.PoolManager;

/**
 * @author scotta
 */
public class CayenneConfiguration implements DataSourceFactory {
	private static DataSource dataSource = null;

	public CayenneConfiguration() throws Exception {
		super();
	}

	@Override
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

		// All done!
		return dataSource;
	}

	@Override
	public void initializeWithParentConfiguration(Configuration parentConfiguration) {}
}
