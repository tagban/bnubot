/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.swt;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Holder;

import net.bnubot.bot.gui.WhatsNewWindow;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.Profile;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.settings.Settings;
import net.bnubot.vercheck.CurrentVersion;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;

/**
 * @author scotta
 */
public class SWTDesktop extends Thread {
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
		if(CurrentVersion.fromJar()) {
			// If we're launching a new version, pop up the what's new window
			long currentVersionBuilt = CurrentVersion.version().getBuildDate().getTime();
			long lastWhatsNewWindow = Settings.getSection(null).read("whatsNewTime", 0);
			if(lastWhatsNewWindow != currentVersionBuilt) {
				Settings.getSection(null).write("whatsNewTime", currentVersionBuilt);
				Settings.store();
				new WhatsNewWindow();
			}
		}

		display = Display.getDefault();
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		tabs = new CTabFolder(shell, SWT.TOP);

		setTitle();
		initializeSystemTray();
		//WindowPosition.load(this);

		shell.pack();
		shell.open();
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
		setTitle(title);

		if(tray != null)
			tray.setToolTipText(title);
	}

	protected static void setTitle(String title) {
		if(display.isDisposed())
			return;

		final String setTo = (title == null) ? "NULL" : title;
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				if(!shell.isDisposed())
					shell.setText(setTo);
			}});
	}

	public static void setTitle(final SWTEventHandler seh, ProductIDs product) {
		instance.setTitle();

		Image img = null;
		BNetIcon[] icons_bni = IconsDotBniReader.getIcons();
		if((icons_bni != null) && (product != null)) {
			for(BNetIcon element : IconsDotBniReader.getIcons()) {
				if(element.useFor(0, product.getDword())) {
					img = element.getImage();
					break;
				}
			}
		}

		final Image image = img;
		final Composite component = seh.getFrame();
		String t = seh.toString();
		final String title = (t == null) ? "NULL" : t;
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				for(CTabItem tab : tabs.getItems()) {
					if(component == tab.getControl()) {
						tab.setText(title);
						tab.setImage(image);
						// TODO: seh.getMenuBar().setIcon(image);
						break;
					}
				}
			}});
	}

	public static SWTEventHandler createSWTEventHandler(final Profile profile) {
		final Holder<SWTEventHandler> eh = new Holder<SWTEventHandler>(null);
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				CTabItem tab = new CTabItem(tabs, SWT.CLOSE);
				Composite composite = new Composite(tabs, SWT.NULL);
				final SWTEventHandler seh = new SWTEventHandler(composite, profile);
				tab.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent arg0) {
						try {
							seh.getFirstConnection().getProfile().dispose();
						} catch(Exception e) {}
					}});
				tab.setControl(composite);
				tabs.setSelection(tab);
				selectedGui = seh;
				setTitle(seh.toString());

				// TODO: Set the divider location
				//seh.setDividerLocation(getDividerLocation());

				// TODO: Add the components to the display
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
