/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.webbot.client.types;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BeanDate implements IsSerializable {
	public BeanDate() {}
	
	public long time;
	public String string;
}
