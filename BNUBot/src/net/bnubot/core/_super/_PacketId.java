/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core._super;

import net.bnubot.core.Connection;

/**
 * @param <C> The connection type for this packet id
 * @author scotta
 */
public interface _PacketId<C extends Connection> {
	public String name();
	public int ordinal();
}
