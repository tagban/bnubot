/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.List;

import net.bnubot.core.mcp.MCPCharacter;

/**
 * @author scotta
 */
public interface RealmEventHandler {
	//Initialization
	public void initialize(RealmConnection rc);

	//Realm events
	public void realmConnected();
	public void realmDisconnected();
	public void recieveRealmInfo(String text);
	public void recieveRealmError(String text);
	public void recieveCharacterList(List<MCPCharacter> chars);
}
