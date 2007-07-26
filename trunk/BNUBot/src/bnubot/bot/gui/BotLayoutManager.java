/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.bot.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class BotLayoutManager implements LayoutManager {
	private static final int channelWidth = 200;
	private static final int textHeight = 17;
	private static final int padding = 5;
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
		if(parent.getComponentCount() != 4) {
			parent.setBackground(Color.RED);
			return;
		}
		parent.setBackground(null);
		
		int height = parent.getHeight() - paddingEdge*2;
		int width = parent.getWidth() - paddingEdge*2;
		
		int col1x = paddingEdge;
		int col2x = paddingEdge + width - channelWidth;
		int row1y = paddingEdge;
		int row2y = paddingEdge + textHeight + padding;
		int row3y = paddingEdge + height - textHeight;
		int mainw = width - padding - channelWidth;
		int mainh = height - padding - textHeight;

		parent.getComponent(0).setBounds(col1x, row1y, mainw, mainh);
		parent.getComponent(1).setBounds(col1x, row3y, mainw, textHeight);
		parent.getComponent(2).setBounds(col2x, row1y, channelWidth, textHeight);
		parent.getComponent(3).setBounds(col2x, row2y, channelWidth, mainh);
	}
}
