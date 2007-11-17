package bnubot.core;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import bnubot.bot.EventHandler;
import bnubot.core.bnftp.BNFTPConnection;


public abstract class Connection extends Thread implements EventHandler {
	protected ConnectionSettings cs;
	protected Hashtable<String, EventHandler> eventHandlers = new Hashtable<String, EventHandler>();
	
	public Connection(ConnectionSettings cs) {
		this.cs = cs;
	}
	
	public void addEventHandler(String key, EventHandler e) {
		eventHandlers.put(key, e);
		e.initialize(this);
	}
	
	public void removeEventHandler(String key) {
		eventHandlers.remove(key);
	}
	
	public void removeEventHandler(EventHandler eh) {
		eventHandlers.remove(eh);
	}

	public abstract boolean isConnected();
	public abstract void setConnected(boolean c);
	public abstract void sendChat(String text);
	
	public File downloadFile(String filename) {
		return BNFTPConnection.downloadFile(cs, filename);
	}
	
	public ConnectionSettings getConnectionSettings() {
		return cs;
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
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().bnetConnected();
	}
	
	public void bnetDisconnected() {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().bnetDisconnected();
	}
	
	public void joinedChannel(String channel) {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().joinedChannel(channel);
	}
	
	public void channelUser(String user, int flags, int ping, String statstr) {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().channelUser(user, flags, ping, statstr);
	}
	
	public void channelJoin(String user, int flags, int ping, String statstr) {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().channelJoin(user, flags, ping, statstr);
	}
	
	public void channelLeave(String user, int flags, int ping, String statstr) {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().channelLeave(user, flags, ping, statstr);
	}

	public void recieveChat(String user, String text) {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().recieveChat(user, text);
	}

	public void recieveEmote(String user, String text) {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().recieveEmote(user, text);
	}

	public void recieveInfo(String text) {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().recieveInfo(text);
	}

	public void recieveError(String text) {
		Enumeration<EventHandler> en = eventHandlers.elements();
		while(en.hasMoreElements())
			en.nextElement().recieveError(text);
	}
}