/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.webbot.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.Profile;
import net.bnubot.webbot.client.types.BeanEvent;

public class Session {
	private static HashMap<String, Session> sessions = new java.util.HashMap<String, Session>();
	private static Profile profile = null;
	
	public static void sendChat(String text) {
		if(profile != null) {
			Connection c = profile.getPrimaryConnection();
			if(c != null)
				c.sendChat(text, true);
		}
	}
	
	public static Session get(String sid) {
		return sessions.get(sid);
	}
	
	public static void postEvent(BeanEvent event) {
		for(String sid : sessions.keySet()) {
			Session s = sessions.get(sid);
			s.unpostedEvents.add(event);
		}
	}

	private List<BeanEvent> unpostedEvents = new LinkedList<BeanEvent>();
	
	public Session(String sid) {
		sessions.put(sid, this);
	}
	
	/**
	 * Get a posted event
	 * @return
	 */
	public BeanEvent remove() {
		synchronized(unpostedEvents) {
			if(unpostedEvents.size() < 1)
				return null;
			return unpostedEvents.remove(0);
		}
	}

	public static void setProfile(Profile profile) {
		Session.profile = profile;
	}
}
