package net.bnubot.webbot.client.types.events;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BeanInfo implements IsSerializable {
	public BeanInfo() {}
	
	public boolean error;
	public String type;
	public String text;
}
