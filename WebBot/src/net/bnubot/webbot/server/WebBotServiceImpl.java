/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.webbot.server;

import net.bnubot.webbot.client.IWebBotService;
import net.bnubot.webbot.client.types.BeanEvent;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class WebBotServiceImpl extends RemoteServiceServlet implements IWebBotService {
	static {
		System.getProperties().put("net.bnubot.rootpath", "C:\\workspace\\BNUBot\\");
		net.bnubot.core.PluginManager.register(WebBotEventHandler.class);
		net.bnubot.Main.main(new String[] {}); //"-nogui", "-nocli"});
	}
	
	Session sess = new Session("asdf");

	@Override
	public BeanEvent poll() {
		/*final long startTime = System.currentTimeMillis();
		do {
			final BeanEvent be = sess.remove();
			if(be != null)
				return be;
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {}
		} while(System.currentTimeMillis() - startTime < 4000);
		return null;*/
		return sess.remove();
	}

	@Override
	public void sendChat(final String text) {
		Session.sendChat(text);
	}
}
