package bnubot.core;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import bnubot.bot.EventHandler;
import bnubot.core.bnftp.BNFTPConnection;
import bnubot.core.queue.ChatQueue;

public abstract class Connection extends Thread implements EventHandler {
	protected ConnectionSettings cs;
	protected ChatQueue cq;
	protected LinkedList<EventHandler> eventHandlers = new LinkedList<EventHandler>();
	protected LinkedList<EventHandler> eventHandlers2 = new LinkedList<EventHandler>();
	protected BNetUser myUser = null;
	protected boolean connected = false;
	protected LinkedList<Connection> slaves = new LinkedList<Connection>();
	protected String channelName = null;
	protected long lastAntiIdle;
	
	public Connection(ConnectionSettings cs, ChatQueue cq) {
		this.cs = cs;
		this.cq = cq;
		
		if(cq != null)
			cq.add(this);
	}

	public abstract void joinChannel(String channel) throws Exception;
	
	public void addSlave(Connection c) {
		slaves.add(c);
	}
	
	public void addEventHandler(EventHandler e) {
		eventHandlers.add(e);
		e.initialize(this);
	}
	
	public void addSecondaryEventHandler(EventHandler e) {
		eventHandlers2.add(e);
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
	
	protected long lastChatTime = 0;
	protected int lastChatLen = 0;
	private int increaseDelay(int bytes) {
		long thisTime = new Date().getTime();
		
		if(lastChatTime == 0) {
			lastChatTime = thisTime;
			return 0;
		}
		
		int delay = (lastChatLen * 15) + 2700;
		delay -= (thisTime - lastChatTime);
		if(delay < 0)
			delay = 0;
		
		lastChatTime = thisTime;
		lastChatLen = bytes;
		return delay;
	}
	
	private int checkDelay() {
		if(lastChatTime == 0)
			return 0;

		long thisTime = new Date().getTime();
		int delay = (lastChatLen * 15) + 2700;
		delay -= (thisTime - lastChatTime);
		if(delay < 0)
			return 0;
		return delay;
	}

	public boolean canSendChat() {
		if(channelName == null)
			return false;
		
		return (checkDelay() <= 0);
	}
	
	public void sendChatNow(String text) {
		lastAntiIdle = new Date().getTime();
		
		if(canSendChat())
			increaseDelay(text.length());
		else
			// Fake the bot in to thinking we sent chat in the future
			lastChatTime = new Date().getTime() + increaseDelay(text.length());
	}
	
	public String cleanText(String text) {
		//Remove all chars under 0x20
		byte[] data = text.getBytes();
		text = "";
		for(int i = 0; i < data.length; i++) {
			if(data[i] >= 0x20)
				text += (char)data[i];
		}
		return text;
	}
	
	public void sendChat(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		if(!isConnected())
			return;
		
		text = cleanText(text);
		
		if(text.length() == 0)
			return;

		if(canSendChat()) {
			sendChatNow(text);
		} else {
			if(cq == null) {
				new Exception("cq == null").printStackTrace();
				System.exit(1);
			} else {
				cq.enqueue(text);
			}
		}
	}
	
	public void sendChat(BNetUser to, String text) {
		if(text == null)
			return;

		text = cleanText(text);
		
		if(myUser.equals(to))
			recieveInfo(text);
		else
			sendChat("/w " + to.getFullLogonName() + " " + text);
	}
	
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
		channelName = null;
		
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().bnetDisconnected();
	}
	
	public void joinedChannel(String channel) {
		channelName = channel;
		
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().joinedChannel(channel);
		
		Iterator<Connection> it2 = slaves.iterator();
		while(it2.hasNext())
			try {
				Connection c = it2.next();
				System.out.println("Telling [" + c.toString() + "] to join " + channel);
				c.joinChannel(channel);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().recieveInfo(text);
	}

	public void recieveError(String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveError(text);
		
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().recieveError(text);
	}
	
	public void whisperSent(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().whisperSent(user, flags, ping, text);
		
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().whisperSent(user, flags, ping, text);
	}
	
	public void whisperRecieved(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().whisperRecieved(user, flags, ping, text);
		
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().whisperRecieved(user, flags, ping, text);
	}
}
