/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.AWTEvent;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

import net.bnubot.settings.Settings;

/**
 * Globally load/save window position
 */
public class WindowPosition {
	private static final ComponentAdapter windowSaver = new ComponentAdapter() {
		public void componentMoved(ComponentEvent event) {
			Window window = ((WindowEvent)event).getWindow();
			WindowPosition.save(window);
		}};
	
	static {
		// Globally save/load window positions
		Toolkit.getDefaultToolkit().addAWTEventListener(
				new AWTEventListener() {
					public void eventDispatched(AWTEvent event) {
						if(event.getID() != WindowEvent.WINDOW_OPENED)
							return;
						Window window = ((WindowEvent)event).getWindow();
						load(window);
						window.addComponentListener(windowSaver);
					}},
					AWTEvent.WINDOW_EVENT_MASK);
	}

	public static void load(Window w) {
		String header = w.getClass().getSimpleName();
		Rectangle bounds = w.getBounds();
		if((w instanceof Frame) && ((Frame)w).isResizable()) {
			bounds.height = Integer.valueOf(Settings.read(header, "height", Integer.toString(bounds.height)));
			bounds.width = Integer.valueOf(Settings.read(header, "width", Integer.toString(bounds.width)));
		}
		bounds.x = Integer.valueOf(Settings.read(header, "x", Integer.toString(bounds.x)));
		bounds.y = Integer.valueOf(Settings.read(header, "y", Integer.toString(bounds.y)));
		w.setBounds(bounds);
	}
	
	public static void save(Window w) {
		String header = w.getClass().getSimpleName();
		Rectangle bounds = w.getBounds();
		if((w instanceof Frame) && ((Frame)w).isResizable()) {
			Settings.write(header, "height", Integer.toString(bounds.height));
			Settings.write(header, "width", Integer.toString(bounds.width));
		}
		Settings.write(header, "x", Integer.toString(bounds.x));
		Settings.write(header, "y", Integer.toString(bounds.y));
		Settings.store();
	}
}
