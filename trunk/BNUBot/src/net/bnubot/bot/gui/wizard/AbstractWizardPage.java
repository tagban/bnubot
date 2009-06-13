package net.bnubot.bot.gui.wizard;

import net.bnubot.bot.gui.components.ConfigPanel;


public abstract class AbstractWizardPage {
	public abstract boolean isPageComplete();
	public abstract void createComponent(ConfigPanel cp);
	public void display() {}
}
