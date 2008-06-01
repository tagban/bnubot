/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.webbot.client;

import net.bnubot.webbot.client.types.BeanEvent;
import net.bnubot.webbot.client.types.events.BeanChatEvent;
import net.bnubot.webbot.client.types.events.BeanInfo;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WebBot extends TrapCallback<BeanEvent> implements EntryPoint {
	private final VerticalPanel textArea = new VerticalPanel();
	public static final IWebBotServiceAsync svc = (IWebBotServiceAsync) GWT.create(IWebBotService.class);
	static {
		((ServiceDefTarget)svc).setServiceEntryPoint(GWT.getModuleBaseURL() + "ajax");	
	}
	
	public void onModuleLoad() {
		final TextBox txtSend = new TextBox();
		txtSend.setWidth("100%");
		txtSend.addKeyboardListener(new KeyboardListener() {
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if(keyCode == '\r') {
					svc.sendChat(txtSend.getText(), new TrapCallback<Object>() {
						public void success(Object result) {}
					});
					txtSend.setText("");
				}
			}
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {}
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {}
		});
		
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("90%");
		vp.add(new ScrollPanel(textArea));
		vp.add(txtSend);
		
		// Add image and button to the RootPanel
		RootPanel.get().add(vp);
		
		// Set up an AJAX poller
		new Timer() {
			public void run() {
				svc.poll(WebBot.this);
			}
		}.scheduleRepeating(1000);
	}
	
	/**
	 * Called when a new battle.net event has been recieved
	 */
	public void success(BeanEvent result) {
		if(result == null)
			return;
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(new Label("[" + result.when.string + "] "));
		if(result.bce != null) {
			BeanChatEvent bce = result.bce;
			if(bce.emote) {
				Label txt = new Label("<" + bce.who.display + "> " + bce.text);
				hp.add(txt);
			} else {
				Label user = new Label("<" + bce.who.display + "> ");
				Label txt = new Label(bce.text);
				hp.add(user);
				hp.add(txt);
			}
		} else if(result.bi != null) {
			BeanInfo bi = result.bi;
			if(bi.type != null)
				hp.add(new Label("(" + bi.type + ") "));
			hp.add(new Label(bi.text));
		} else {
			throw new IllegalArgumentException(result.getClass().getName());
		}
		
		textArea.add(hp);
		svc.poll(this);
	}
}
