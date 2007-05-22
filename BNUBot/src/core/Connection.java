package core;

public abstract class Connection extends Thread {
	protected ConnectionSettings cs;
	protected EventHandler e;
	
	public Connection(ConnectionSettings cs, EventHandler e) {
		this.cs = cs;
		this.e = e;
	}

	public abstract void Connect();
	public abstract void Disconnect();
	
}
