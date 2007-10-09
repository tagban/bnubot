/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui.main;

import java.awt.Color;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

public class GuiDesktop extends JFrame {
	private static final long serialVersionUID = -7144648179041514994L;
	private static final GuiDesktop instance = new GuiDesktop();
	
	private GuiDesktop() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);
        desktop.setBackground(Color.BLACK);
        desktop.setForeground(Color.WHITE);
		
        setSize(800, 500);
		
        setVisible(true);
	}
	
	public static GuiDesktop getInstance() {
		return instance;
	}
	
	public static void add(JInternalFrame gui) {
		instance.getContentPane().add(gui);
	}
}
