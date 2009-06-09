/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import java.awt.Container;

import net.bnubot.bot.gui.GuiDesktop;
import net.bnubot.core.PluginManager;

/**
 * @author scotta
 */
public class TaskManager {
	private static final long serialVersionUID = 641763656953338296L;

	private static Container box = null;
	private static Container getBox() {
		if((box == null) && PluginManager.getEnableGui())
			box = GuiDesktop.getTasksLocation();
		return box;
	}

	public static Task createTask(String title) {
		return createTask(title, 0, null);
	}

	public static Task createTask(String title, String currentStep) {
		Task t = createTask(title);
		t.updateProgress(currentStep);
		return t;
	}

	public static Task createTask(String title, int max, String units) {
		if(getBox() == null)
			return new Task();

		TaskGui t = new TaskGui(title, max, units);
		box.add(t.getComponent());
		box.setVisible(true);
		return t;
	}

	protected static void complete(TaskGui t) {
		box.remove(t.getComponent());
		if(box.getComponentCount() == 0)
			box.setVisible(false);
	}
}
