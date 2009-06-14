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

	private final int required;
	private final int actual;

	public InsufficientAccessException(int required, int actual) {
		this.required = required;
		this.actual = actual;
	}

	public int getRequired() {
		return required;
	}

	public int getActual() {
		return actual;
	}
}