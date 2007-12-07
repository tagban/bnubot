/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;

import net.bnubot.bot.gui.GuiDesktop;
import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.settings.GlobalSettings;

public class TaskManager extends Dialog {
	private static final long serialVersionUID = 641763656953338296L;
	private static Box box = null;
	private static TaskManager tm = null;
	
	private TaskManager(Frame owner) {
		super(owner);
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
		boolean enableGUI;
		Frame owner = null;
		try {
			enableGUI = GlobalSettings.enableGUI;
			if(enableGUI)
				owner = GuiDesktop.getInstance();
		} catch(NoClassDefFoundError e) {
			enableGUI = true;
			owner = new Frame();
		}
		
		if(enableGUI && (tm == null)) {
			tm = new TaskManager(owner);
			WindowPosition.load(tm);
			tm.setTitle("Running Tasks");
			box = new Box(BoxLayout.Y_AXIS);
			tm.add(box);
			tm.setResizable(false);
		}
		
		if(tm == null)
			return new Task();
		
		TaskGui t = new TaskGui(title, max, units);
		box.add(t.getProgressBar());
		tm.pack();
		tm.setVisible(true);
		return t;
	}
	
	protected static void complete(TaskGui t) {
		if(tm != null) {
			box.remove(t.getProgressBar());
			if(box.getComponentCount() == 0) {
				tm.setVisible(false);
				tm.dispose();
				tm = null;
			} else
				tm.pack();
		}
	}
}
