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
	public InsufficientAccessException(String string) {
		super(string);
	}
}