package bnubot.bot;

import bnubot.core.RealmConnection;

public interface RealmEventHandler {
	//Initialization
	public void initialize(RealmConnection rc);
	
	//Realm events
	public void realmConnected();
	public void realmDisconnected();
	public void recieveInfo(String text);
	public void recieveError(String text);
}
