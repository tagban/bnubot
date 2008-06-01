package net.bnubot.webbot.client;

import net.bnubot.webbot.client.types.BeanEvent;

import com.google.gwt.user.client.rpc.RemoteService;

public interface IWebBotService extends RemoteService {
	BeanEvent poll();
	
	void sendChat(String text);
}
