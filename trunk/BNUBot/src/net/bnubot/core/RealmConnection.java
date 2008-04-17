/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class RealmConnection extends Thread implements RealmEventHandler {
	protected List<RealmEventHandler> realmEventHandlers = new ArrayList<RealmEventHandler>();
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

	public void recieveRealmError(String text) {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveRealmError(text);
	}

	public void recieveRealmInfo(String text) {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveRealmInfo(text);
	}

}
