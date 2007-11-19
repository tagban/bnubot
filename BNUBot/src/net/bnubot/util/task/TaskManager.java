/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;

import net.bnubot.settings.GlobalSettings;

public class TaskManager {
	private static final long serialVersionUID = 641763656953338296L;
	private static Box box = null;
	private static Dialog d = null;
	
	static {
		if(GlobalSettings.enableGUI) {
			d = new Dialog(new Frame());
			d.setTitle("Running Tasks");
			box = new Box(BoxLayout.Y_AXIS);
			d.add(box);
			d.setResizable(false);
		}
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
		if(d == null)
			return new Task();
		
		TaskGui t = new TaskGui(title, max, units);
		box.add(t.getProgressBar());
		d.pack();
		try {
			if(GlobalSettings.enableGUI)
				d.setVisible(true);
		} catch(NoClassDefFoundError e) {
			d.setVisible(true);
		}
		return t;
	}
	
	protected static void complete(TaskGui t) {
		if(d != null) {
			box.remove(t.getProgressBar());
			if(box.getComponentCount() == 0)
				d.setVisible(false);
			else
				d.pack();
		}
	}
}
