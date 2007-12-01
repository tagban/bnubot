/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.console;

import net.bnubot.core.Connection;
import net.bnubot.util.Out;

public class CLIThread extends Thread {
	Connection c;
	
	public CLIThread(Connection c) {
		this.c = c;
	}
	
	public void run() {
		try {
			String text = "";
			while(true) {
				if(System.in.available() > 0) {
					int b = System.in.read();
					if(b == '\n') {
						c.queueChatHelper(text, true);
						text = "";
					} else {
						text += (char)b;
					}
				}
				yield();
			}
		} catch(Exception e) {
			Out.fatalException(e);
		}
	}
}
