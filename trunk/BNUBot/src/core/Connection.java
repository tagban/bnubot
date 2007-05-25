package core;

public abstract class Connection extends Thread {
	protected ConnectionSettings cs;
	
	public Connection(ConnectionSettings cs) {
		this.cs = cs;
	}

	public abstract void Connect();
	public abstract void Disconnect();
	
}
