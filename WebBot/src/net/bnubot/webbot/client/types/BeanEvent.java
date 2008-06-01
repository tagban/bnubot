package net.bnubot.webbot.client.types;

import net.bnubot.webbot.client.types.events.BeanChatEvent;
import net.bnubot.webbot.client.types.events.BeanInfo;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BeanEvent implements IsSerializable {
	public BeanEvent() {}
	
	public BeanDate when;
	
	public BeanChatEvent bce;
	public BeanInfo bi;
}
