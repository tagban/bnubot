/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.webbot.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class TrapCallback<T> implements AsyncCallback<T> {
	public final void onFailure(Throwable caught) {
		RootPanel.get().add(new Label(caught.getMessage()));
	}
	
	public final void onSuccess(T result) {
		try {
			success(result);
		} catch(Throwable t) {
			onFailure(t);
		}
	}
	
	public abstract void success(T result);
}
