package net.bnubot.webbot.client;

import net.bnubot.webbot.client.types.BeanEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IWebBotServiceAsync {
	void poll(AsyncCallback<BeanEvent> callback);
	
	void sendChat(String text, AsyncCallback<Object> callback);
}
