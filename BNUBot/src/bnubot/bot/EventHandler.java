package bnubot.bot;

import bnubot.core.BNetUser;
import bnubot.core.Connection;

public interface EventHandler {
	public void initialize(Connection c);
	
	public void bnetConnected();
	public void bnetDisconnected();
	
	public void joinedChannel(String channel);
	public void channelUser(BNetUser user, int flags, int ping, String statstr);
	public void channelJoin(BNetUser user, int flags, int ping, String statstr);
	public void channelLeave(BNetUser user, int flags, int ping, String statstr);
	public void recieveChat(BNetUser user, int flags, int ping, String text);
	public void recieveEmote(BNetUser user, int flags, int ping, String text);
	public void recieveInfo(String text);
	public void recieveError(String text);
	public void whisperSent(BNetUser user, int flags, int ping, String text);
	public void whisperRecieved(BNetUser user, int flags, int ping, String text);
}
