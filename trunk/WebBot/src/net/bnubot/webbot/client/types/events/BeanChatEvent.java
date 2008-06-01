package net.bnubot.webbot.client.types.events;

import net.bnubot.webbot.client.types.BeanBNetUser;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BeanChatEvent implements IsSerializable {
	public BeanChatEvent() {}
	
	public boolean emote;
	public BeanBNetUser who;
	public String text;
}
