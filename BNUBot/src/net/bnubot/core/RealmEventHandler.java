/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

public interface RealmEventHandler {
	//Initialization
	public void initialize(RealmConnection rc);

	//Realm events
	public void realmConnected();
	public void realmDisconnected();
	public void recieveRealmInfo(String text);
	public void recieveRealmError(String text);
}
