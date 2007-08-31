/**
 * 
 */
package net.bnubot.bot;

import net.bnubot.util.BNetUser;

/**
 * @author sanderson
 *
 */
public class CommandResponseCookie {
	BNetUser source;
	boolean wasWhispered;
	
	public CommandResponseCookie(BNetUser source, boolean wasWhispered) {
		this.source = source;
		this.wasWhispered = wasWhispered;
	}
	
	public void sendChat(net.bnubot.core.Connection c, String text) {
		c.sendChat(source, text, wasWhispered);
	}
}
