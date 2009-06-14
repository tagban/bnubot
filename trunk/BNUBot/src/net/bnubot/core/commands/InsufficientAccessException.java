/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.commands;

/**
 * @author scotta
 */
public class InsufficientAccessException extends Exception {
	private static final long serialVersionUID = -1954683087381833989L;

	private final boolean contactUser;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public boolean canContactUser() {
		return contactUser;
	}

	public InsufficientAccessException(String message, boolean contactUser) {
		super(message);
		this.contactUser = contactUser;
	}
}