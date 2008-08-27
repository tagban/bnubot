/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.KeyboardFocusManager;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 * @author scotta
 */
public class ConfigTextField extends JTextField {
	private static final long serialVersionUID = -2894805163754230265L;

	public ConfigTextField(String text) {
		super(text);
		setBorder(BorderFactory.createLoweredBevelBorder());

		// Enable tab key for focus traversal
		// http://forum.java.sun.com/thread.jspa?threadID=283320&messageID=2194505
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
	}
}