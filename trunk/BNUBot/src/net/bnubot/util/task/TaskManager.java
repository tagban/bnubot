/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import java.awt.Container;
import java.awt.Window;

/**
 * @author scotta
 */
public class TaskManager {
	private static Container box = null;
	private static Window window = null;

	public static void setTaskLocation(Container box) {
		TaskManager.box = box;
	}

	public static void setWindow(Window window) {
		TaskManager.window = window;
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
		if(box == null)
			return new Task();

		TaskGui t = new TaskGui(title, max, units);
		box.add(t.getComponent());
		box.setVisible(true);
		if(window != null) {
			window.setVisible(true);
			window.pack();
		}
		return t;
	}

	protected static void complete(TaskGui t) {
		box.remove(t.getComponent());
		if(box.getComponentCount() == 0) {
			box.setVisible(false);
			if(window != null)
				window.setVisible(false);
		}
	}
}
