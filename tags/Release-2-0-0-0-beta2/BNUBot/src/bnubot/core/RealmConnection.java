package bnubot.core;

import java.util.Iterator;
import java.util.LinkedList;

import bnubot.bot.RealmEventHandler;

public abstract class RealmConnection extends Thread implements RealmEventHandler {
	protected LinkedList<RealmEventHandler> realmEventHandlers = new LinkedList<RealmEventHandler>();
	protected boolean connected = false;
	
	public void addRealmEventHandler(RealmEventHandler e) {
		realmEventHandlers.add(e);
		e.initialize(this);
	}
	
	public void setConnected(boolean c) {
		connected = c;
		
		if(c)
			realmConnected();
		else
			realmDisconnected();
	}

	public void initialize(RealmConnection rc) {
		throw new UnsupportedOperationException();
	}
	
	public void realmConnected() {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().realmConnected();
	}
	
	public void realmDisconnected() {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().realmDisconnected();
	}
	
	public void recieveError(String text) {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveError(text);
	}

	public void recieveInfo(String text) {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveInfo(text);
	}

}