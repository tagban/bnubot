package bnubot.bot;

import bnubot.core.RealmConnection;

public interface RealmEventHandler {
	//Initialization
	public void initialize(RealmConnection rc);
	
	//Realm events
	public void realmConnected();
	public void realmDisconnected();
	public void recieveRealmInfo(String text);
	public void recieveRealmError(String text);
}
