package bnubot.core;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import bnubot.bot.EventHandler;
import bnubot.core.bnftp.BNFTPConnection;

public abstract class Connection extends Thread implements EventHandler {
	protected ConnectionSettings cs;
	protected LinkedList<EventHandler> eventHandlers = new LinkedList<EventHandler>();
	protected BNetUser myUser = null;
	protected boolean connected = false;
	
	public Connection(ConnectionSettings cs) {
		this.cs = cs;
	}
	
	public void addEventHandler(EventHandler e) {
		System.out.println("Loading EventHandler: " + e.getClass().getName());
		eventHandlers.add(e);
		e.initialize(this);
	}
	
	public void removeEventHandler(EventHandler ee) {
		eventHandlers.remove(ee);
	}

	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean c) {
		connected = c;
		
		if(c)
			bnetConnected();
		else
			bnetDisconnected();
	}
	
	public abstract void sendChat(String text);
	public abstract void sendChat(BNetUser to, String text);
	public abstract void setClanRank(String string, int newRank) throws Exception;
	public abstract void reconnect();
	
	public File downloadFile(String filename) {
		return BNFTPConnection.downloadFile(cs, filename);
	}
	
	public ConnectionSettings getConnectionSettings() {
		return cs;
	}

	public BNetUser getMyUser() {
		return myUser;
	}
	
	/*
	 * EventHandler methods follow
	 * 
	 */

	public void initialize(Connection c) {
		new Exception("Invalid use of EventHandler.initialize()").printStackTrace();
		System.exit(1);
	}
	
	public void bnetConnected() {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().bnetConnected();
	}
	
	public void bnetDisconnected() {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().bnetDisconnected();
	}
	
	public void joinedChannel(String channel) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().joinedChannel(channel);
	}
	
	public void channelUser(BNetUser user, int flags, int ping, String statstr) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelUser(user, flags, ping, statstr);
	}
	
	public void channelJoin(BNetUser user, int flags, int ping, String statstr) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelJoin(user, flags, ping, statstr);
	}
	
	public void channelLeave(BNetUser user, int flags, int ping, String statstr) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelLeave(user, flags, ping, statstr);
	}

	public void recieveChat(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveChat(user, flags, ping, text);
	}

	public void recieveEmote(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveEmote(user, flags, ping, text);
	}

	public void recieveInfo(String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveInfo(text);
	}

	public void recieveError(String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveError(text);
	}
	
	public void whisperSent(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().whisperSent(user, flags, ping, text);
	}
	
	public void whisperRecieved(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().whisperRecieved(user, flags, ping, text);
	}
}
