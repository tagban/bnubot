package bnubot.bot.database;

import java.io.*;
import java.sql.*;
import java.util.Iterator;
import java.util.List;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.dialect.MySQLDialect;

import bnubot.bot.database.pojo.*;

public class Database implements Serializable {
	private static final long serialVersionUID = 9064719758285921969L;
	private static final long databaseVersion = 0;
	private static final long compatibleVersion = 0;
	private Connection conn;
	private SessionFactory sessionFactory;
	private Root root;
	
	public Database(File f) throws SQLException {
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		boolean fileExists = f.exists();
		conn = DriverManager.getConnection("jdbc:sqlite:" + f.getName());
		Statement stmt = conn.createStatement();
		stmt.execute("PRAGMA synchronous = FULL;");
		stmt.close();
	    
		if(fileExists) {
			if(!checkSchema())
				createSchema();
		}
		else
			createSchema();
		
		sessionFactory = new Configuration().configure().buildSessionFactory();
		
		Session session = sessionFactory.openSession();
		
		Transaction tx = session.beginTransaction();
		List roots = session.createQuery("SELECT r FROM " + Root.class.getName() + " AS r").list();
		if(roots.size() > 0) {
			root = (Root) roots.get(0);
			System.out.println(root.toString());
		} else {
			System.out.println("Generating a new root.");
			root = new Root();
			session.saveOrUpdate(root);
		}
		tx.commit();
		
		session.close();
	}

	public Session getSession() {
		return sessionFactory.openSession();
	}
	
	/**
	 * Check whether or not the database schema is valid
	 * @return boolean indicating if database is up to date
	 * @throws SQLException 
	 */
	private boolean checkSchema() {
		ResultSet rs = null;
		try {
			rs = conn.createStatement().executeQuery("SELECT version FROM dbVersion;");
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
		} catch(SQLException e) {
		}
		
		if(rs != null)
			try { rs.close(); } catch(SQLException e) {}
		return false;
	}
	
	private void createSchema() throws SQLException {
		System.out.println("The database requires rebuilding.");
		
		Statement stmt = conn.createStatement();

		stmt.execute("DROP TABLE IF EXISTS dbVersion;");
		stmt.execute("DROP TABLE IF EXISTS root;");
		stmt.execute("DROP TABLE IF EXISTS account;");
		stmt.execute("DROP TABLE IF EXISTS user;");
		stmt.execute("DROP TABLE IF EXISTS rank;");
		
		stmt.execute("CREATE TABLE dbVersion (version INTEGER NOT NULL);");
		stmt.execute("INSERT INTO dbVersion (version) VALUES (" + databaseVersion + ");");
		
		stmt.execute("CREATE TABLE root (id INTEGER INTEGER NOT NULL);");
		stmt.execute("INSERT INTO root (id) VALUES (1);");
		
		stmt.execute(
			"CREATE TABLE account ( " +
				"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ON CONFLICT ROLLBACK, " +
				"root INTEGER NOT NULL DEFAULT 1, " +
				"access INTEGER NOT NULL, " +
				"name TEXT UNIQUE NOT NULL, " +
				"created INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP " +
			");");
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO account (access, name) VALUES (?, ?);");
		ps.setInt(1, 100);
		ps.setString(2, "Camel");
		ps.execute();
		ps.close();
		
		stmt.execute(
			"CREATE TABLE user ( " +
				"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ON CONFLICT ROLLBACK, " +
				"login TEXT UNIQUE NOT NULL, " +
				"account INTEGER DEFAULT NULL, " +
				"created INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
				"lastSeen INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP " +
			");");
		
		ps = conn.prepareStatement("INSERT INTO user (login, account) VALUES (?, ?);");
		ps.setString(1, "bnu-camel@useast");
		ps.setString(2, "Camel");
		ps.execute();
		
		stmt.execute(
			"CREATE TABLE rank ( " +
				"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ON CONFLICT ROLLBACK, " +
				"name TEXT UNIQUE NOT NULL " +
			");");
		
		ps = conn.prepareStatement("INSERT INTO rank (id, name) VALUES (?, ?);");
		ps.setInt(1, 100);
		ps.setString(2, "Master");
		ps.execute();
		ps.close();
		
		stmt.close();
	}
}
