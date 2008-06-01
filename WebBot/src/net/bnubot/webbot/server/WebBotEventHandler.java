package net.bnubot.webbot.server;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.util.BNetUser;
import net.bnubot.webbot.client.types.BeanEvent;
import net.bnubot.webbot.client.types.events.BeanChatEvent;
import net.bnubot.webbot.client.types.events.BeanInfo;

public class WebBotEventHandler extends EventHandler {
	@Override
	public void initialize(Connection source) {
		Session.setProfile(source.getProfile());
	}
	
	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		BeanChatEvent bce = new BeanChatEvent();
		bce.emote = false;
		bce.text = text;
		bce.who = BeanUtils.beanBNetUser(user);
		
		BeanEvent be = new BeanEvent();
		be.when = BeanUtils.beanDate();
		be.bce = bce;
		Session.postEvent(be);
	}
	
	@Override
	public void recieveEmote(Connection source, BNetUser user, String text) {
		BeanChatEvent bce = new BeanChatEvent();
		bce.emote = true;
		bce.text = text;
		bce.who = BeanUtils.beanBNetUser(user);
		
		BeanEvent be = new BeanEvent();
		be.when = BeanUtils.beanDate();
		be.bce = bce;
		Session.postEvent(be);
	}
	
	@Override
	public void recieveInfo(Connection source, String text) {
		BeanInfo bi = new BeanInfo();
		bi.error = false;
		bi.text = text;
		bi.type = null;
		
		BeanEvent be = new BeanEvent();
		be.when = BeanUtils.beanDate();
		be.bi = bi;
		Session.postEvent(be);
	}
	
	@Override
	public void recieveServerInfo(Connection source, String text) {
		BeanInfo bi = new BeanInfo();
		bi.error = false;
		bi.text = text;
		bi.type = source.getServerType();
		
		BeanEvent be = new BeanEvent();
		be.when = BeanUtils.beanDate();
		be.bi = bi;
		Session.postEvent(be);
	}
	
	@Override
	public void recieveError(Connection source, String text) {
		BeanInfo bi = new BeanInfo();
		bi.error = true;
		bi.text = text;
		bi.type = null;
		
		BeanEvent be = new BeanEvent();
		be.when = BeanUtils.beanDate();
		be.bi = bi;
		Session.postEvent(be);
	}
	
	@Override
	public void recieveServerError(Connection source, String text) {
		BeanInfo bi = new BeanInfo();
		bi.error = true;
		bi.text = text;
		bi.type = source.getServerType();
		
		BeanEvent be = new BeanEvent();
		be.when = BeanUtils.beanDate();
		be.bi = bi;
		Session.postEvent(be);
	}
}
