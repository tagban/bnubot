/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.swt;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Holder;

import net.bnubot.bot.gui.WhatsNewWindow;
import net.bnubot.settings.Settings;
import net.bnubot.vercheck.CurrentVersion;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;

public class SWTDesktop extends Thread {
	private static boolean initialized = false;
	private static Display display = null;
	private static Shell shell = null;
	private static final List<SWTEventHandler> guis = new ArrayList<SWTEventHandler>();
	private static SWTEventHandler selectedGui = null;
	private static TrayItem tray = null;
	private static CTabFolder tabs = null;
	
	private static final SWTDesktop instance = new SWTDesktop();

	public static SWTDesktop getInstance() {
		return instance;
	}

	private SWTDesktop() {
		start();
	}
	
	public void run() {
		if(CurrentVersion.fromJar()) {
			// If we're launching a new version, pop up the what's new window
			long currentVersionBuilt = CurrentVersion.version().getBuildDate().getTime();
			long lastWhatsNewWindow = Settings.readLong(null, "whatsNewTime", 0);
			if(lastWhatsNewWindow != currentVersionBuilt) {
				Settings.writeLong(null, "whatsNewTime", currentVersionBuilt);
				Settings.store();
				new WhatsNewWindow();
			}
		}
		
		display = new Display();
		shell = new Shell(display);
		tabs = new CTabFolder(shell, SWT.BOTTOM);
		
		setTitle();
		initializeSystemTray();
		//WindowPosition.load(this);
		
		initialized = true;

		shell.pack();
		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		System.exit(0);
	}
	
	private void initializeSystemTray() {
		/*IconsDotBniReader.initialize(new ConnectionSettings(2));
		tray = new TrayItem(shell.getDisplay().getSystemTray(), SWT.NULL);
		tray.setImage(IconsDotBniReader.getIcons()[0].getImage());*/
	}
	
	private void setTitle() {
		String title = "BNU-Bot " + CurrentVersion.version();
		if(selectedGui != null) {
			title += " - ";
			title += selectedGui.toString();
		}
		shell.setText(title);
		
		if(tray != null)
			tray.setToolTipText(title);
	}

	public static SWTEventHandler createSWTEventHandler() {
		while(!initialized)
			yield();
		
		final Holder<SWTEventHandler> eh = new Holder<SWTEventHandler>(null); 
		display.syncExec(new Runnable() {
			public void run() {
				CTabItem tab = new CTabItem(tabs, SWT.CLOSE);
				Composite composite = new Composite(tabs, SWT.NULL);
				final SWTEventHandler seh = new SWTEventHandler(composite);
				tab.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent arg0) {
						try {
							seh.getFirstConnection().getProfile().dispose();
						} catch(Exception e) {}
					}});
				tab.setControl(composite);
				tab.setText(seh.toString());
				tabs.pack();
				shell.pack();
				// Set the divider location
				//seh.setDividerLocation(getDividerLocation());
				
				// Add the components to the display
				//seh.getMenuBar().setVisible(false);
				//menuBar.add(geh.getMenuBar());
				guis.add(seh);

				eh.value = seh;
			}
		});
		while(eh.value == null)
			yield();
		return eh.value;
	}
}