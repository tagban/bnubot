/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.util.task;

import java.awt.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;

import net.bnubot.settings.GlobalSettings;

public class TaskManager extends Frame {
	private static final long serialVersionUID = 641763656953338296L;
	private static final Box box = new Box(BoxLayout.Y_AXIS);
	private static final TaskManager instance = new TaskManager();
	
	private TaskManager() {
		super("Running Tasks");
		add(box);
		
		setResizable(false);
		setAlwaysOnTop(true);
	}

	public static Task createTask(String title) {
		return createTask(title, 0, null);
	}
	
	public static Task createTask(String title, int max, String units) {
		Task t = new Task(title, max, units);
		box.add(t.getProgressBar());
		instance.pack();
		try {
			if(GlobalSettings.enableGUI)
				instance.setVisible(true);
		} catch(NoClassDefFoundError e) {
			instance.setVisible(true);
		}
		return t;
	}
	
	protected static void complete(Task t) {
		box.remove(t.getProgressBar());
		if(box.getComponentCount() == 0)
			instance.setVisible(false);
		else
			instance.pack();
	}
}
