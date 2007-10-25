/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.settings;

public class DatabaseSettings {
	private static final String header = "database";
	public String driver;
	public String url;
	public String username;
	public String password;
	public String schema;
	
	public void load() {
		driver = Settings.read(header, "driver", "org.apache.derby.jdbc.EmbeddedDriver");
		url = Settings.read(header, "url", "jdbc:derby:database;create=true");
		username = Settings.read(header, "username", null);
		password = Settings.read(header, "password", null);
		schema = Settings.read(header, "schema", "schema.derby");
	}
	
	public void save() {
		Settings.write(header, "driver", driver);
		Settings.write(header, "url", url);
		Settings.write(header, "username", username);
		Settings.write(header, "password", password);
		Settings.write(header, "schema", schema);
	}
}