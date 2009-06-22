/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.console;

import net.bnubot.core.Connection;
import net.bnubot.logging.Out;

/**
 * @author scotta
 */
public class CLIThread extends Thread {
	private Connection c;
	private boolean disabled = false;

	public CLIThread(Connection c) {
		this.c = c;
	}

	@Override
	public void run() {
		try {
			String text = "";
			while(!disabled) {
				if(System.in.available() > 0) {
					int b = System.in.read();
					if((b == '\r') || (b == '\n')) {
						if(text.length() > 0)
							c.sendChatInternal(text);
						text = "";
					} else {
						text += (char)b;
					}
				} else {
					sleep(200);
				}
				yield();
			}
		} catch(Exception e) {
			Out.fatalException(e);
		}
	}

	public void disable() {
		disabled = true;
	}
}
