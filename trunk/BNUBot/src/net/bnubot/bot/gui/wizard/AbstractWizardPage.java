/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.wizard;

import net.bnubot.bot.gui.components.ConfigPanel;

/**
 * @author scotta
 */
public abstract class AbstractWizardPage {
	public abstract boolean isPageComplete();
	public abstract void createComponent(ConfigPanel cp);
	public void display() {}
}
