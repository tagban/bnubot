/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

public class EnumIdNotPresentException extends Exception {
	private static final long serialVersionUID = 6009972824248091374L;

	public EnumIdNotPresentException(Class<? extends Enum<?>> enumType, int constant) {
		super(enumType.getClass().getName() + " 0x" + Integer.toHexString(constant));
	}
}
