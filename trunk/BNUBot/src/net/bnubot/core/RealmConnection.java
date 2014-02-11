/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bnubot.core.mcp.MCPCharacter;

/**
 * @author scotta
 */
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

	@Override
	public void initialize(RealmConnection rc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void realmConnected() {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().realmConnected();
	}

	@Override
	public void realmDisconnected() {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().realmDisconnected();
	}

	@Override
	public void recieveRealmError(String text) {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveRealmError(text);
	}

	@Override
	public void recieveRealmInfo(String text) {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveRealmInfo(text);
	}

	@Override
	public void recieveCharacterList(List<MCPCharacter> chars) {
		Iterator<RealmEventHandler> it = realmEventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveCharacterList(chars);
	}

	public abstract void sendLogonCharacter(String c);

}
