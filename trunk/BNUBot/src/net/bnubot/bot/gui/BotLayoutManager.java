/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JSplitPane;

public class BotLayoutManager implements LayoutManager {
	private static final int textHeight = 19;
	private static final int padding = 10;
	private static final int paddingEdge = 2;
	
	public void addLayoutComponent(String name, Component comp) {}
	public void removeLayoutComponent(Component comp) {}

	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(400, 200);
	}

	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(1024, 768);
	}
	
	public void layoutContainer(Container parent) {
		if(parent.getComponentCount() != 1) {
			parent.setBackground(Color.RED);
			return;
		}
		parent.setBackground(null);
		
		JSplitPane jsp = (JSplitPane)parent.getComponent(0);
		jsp.setBounds(
				paddingEdge,
				paddingEdge,
				parent.getWidth() - paddingEdge*2,
				parent.getHeight() - paddingEdge*2);
		
		{
			Container leftSide = (Container)jsp.getLeftComponent();
			int height = leftSide.getHeight() - paddingEdge*2;
			int width = jsp.getDividerLocation() - paddingEdge*2;
			
			int height1 = height - textHeight - padding;

			Dimension d0 = new Dimension(width, height1);
			Dimension d1 = new Dimension(width, textHeight);
			
			leftSide.getComponent(0).setPreferredSize(d0);
			leftSide.getComponent(1).setPreferredSize(d1);
		}
		
		{
			Container rightSide = (Container)jsp.getRightComponent();
			int height = rightSide.getHeight() - paddingEdge*2;
			int width = rightSide.getWidth() - paddingEdge*2;
			
			int height2 = height - textHeight - padding;
			
			Dimension d0 = new Dimension(width, textHeight);
			Dimension d1 = new Dimension(width, height2);

			rightSide.getComponent(0).setLocation(0, 0);
			rightSide.getComponent(0).setPreferredSize(d0);
			rightSide.getComponent(1).setPreferredSize(d1);
		}
	}
}
