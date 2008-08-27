/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.swt;

import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.util.crypto.HexDump;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author scotta
 */
public class SWTIconViewer {
	public static void main(String[] args) {
		Display d = new Display();
		Shell s = new Shell(d);
		Color background = d.getSystemColor(SWT.COLOR_BLACK);
		Color foreground = d.getSystemColor(SWT.COLOR_WHITE);
		s.setImage(new Image(null, "tray.gif"));
		s.setText("Icons");
		s.setLayout(new RowLayout());
		s.setBackground(background);

		BNetIcon[][] iconss = {
				IconsDotBniReader.getIcons(),
				IconsDotBniReader.getIconsSTAR(),
				IconsDotBniReader.getIconsWAR3(),
				IconsDotBniReader.getIconsW3XP(),
				IconsDotBniReader.getLegacyIcons(),
				IconsDotBniReader.getIconsLag()};
		for(BNetIcon[] icons : iconss) {
			if(icons == null)
				continue;

			Group grpIcons = new Group(s, SWT.NULL);
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginHeight = 1;
			gridLayout.marginWidth = 1;
			gridLayout.horizontalSpacing = 1;
			gridLayout.verticalSpacing = 1;
			grpIcons.setLayout(gridLayout);
			grpIcons.setBackground(background);
			for(int i = 0; i < icons.length; i++) {
				BNetIcon bni = icons[i];

				Label label = new Label(grpIcons, SWT.NULL);
				label.setImage(bni.getImage());
				label.pack();

				String text = "";
				if(bni.getProducts() != null)
					text += " " + HexDump.DWordToPretty(bni.getProducts()[0]);
				if(bni.getFlags() != 0)
					text += " 0x" + Integer.toHexString(bni.getFlags());

				label = new Label(grpIcons, SWT.NULL);
				label.setBackground(background);
				label.setForeground(foreground);
				label.setText(Integer.toString(i) + text);
				label.pack();
			}
			grpIcons.pack();
		}

		s.pack();
		s.open();
		while(!s.isDisposed()) {
			if(!d.readAndDispatch())
				d.sleep();
		}
		d.dispose();

		System.exit(0);
	}
}
