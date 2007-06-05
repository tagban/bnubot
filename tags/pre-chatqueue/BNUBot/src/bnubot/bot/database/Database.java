package bnubot.bot.database;

import java.io.*;

public class Database implements Serializable {
	private static final long serialVersionUID = 9064719758285921969L;
	UserDatabase ud = null;
	File f;
	
	public static Database load(File f) {
		Database d = null;
		try {
			ObjectInputStream si = new ObjectInputStream(new FileInputStream(f));
			d = (Database)si.readObject();
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
		
		if(d == null)
			d = new Database(f);
		d.setFile(f);
		if(d.ud == null)
			d.ud = new UserDatabase(d);
		return d;
	}
	
	public Database(File f) {
		ud = new UserDatabase(this);
		this.f = f;
	}
	
	public void setFile(File f) {
		this.f = f;
	}
	
	public void save() {
		try {
			ObjectOutputStream so = new ObjectOutputStream(new FileOutputStream(f));
			so.writeObject(this);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public UserDatabase getUserDatabase() {
		return ud;
	}
}
