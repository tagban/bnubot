/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.swt;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Text;

/**
 * @author scotta
 */
public class SWTEventHandler extends EventHandler {
	/**
	 *
	 */
	private static final int TEXT_HEIGHT = 20;
	private Connection firstConnection = null;
	private final Composite frame;
	private final StyledText mainTextArea;

    private static final int limit = 20, percent = 80;
	public static void buildSplit(final Composite parent, Control c1, Control c2) {
	    final Sash sash = new Sash(parent, SWT.VERTICAL);

	    FormData c1Data = new FormData();
	    c1Data.left = new FormAttachment(0, 0);
	    c1Data.right = new FormAttachment(sash, 0);
	    c1Data.top = new FormAttachment(0, 0);
	    c1Data.bottom = new FormAttachment(100, 0);
	    c1.setLayoutData(c1Data);

	    final FormData sashData = new FormData();
	    sashData.left = new FormAttachment(percent, 0);
	    sashData.top = new FormAttachment(0, 0);
	    sashData.bottom = new FormAttachment(100, 0);
	    sash.setLayoutData(sashData);
	    sash.addListener(SWT.Selection, new Listener() {
	      @Override
		public void handleEvent(Event e) {
	        Rectangle sashRect = sash.getBounds();
	        Rectangle shellRect = parent.getClientArea();
	        int right = shellRect.width - sashRect.width - limit;
	        e.x = Math.max(Math.min(e.x, right), limit);
	        if (e.x != sashRect.x) {
	          sashData.left = new FormAttachment(0, e.x);
	          parent.layout();
	        }
	      }
	    });

	    FormData c2Data = new FormData();
	    c2Data.left = new FormAttachment(sash, 0);
	    c2Data.right = new FormAttachment(100, 0);
	    c2Data.top = new FormAttachment(0, 0);
	    c2Data.bottom = new FormAttachment(100, 0);
	    c2.setLayoutData(c2Data);
	}

	public SWTEventHandler(Composite frame, Profile profile) {
		super(profile);
		this.frame = frame;
		frame.setLayout(new FormLayout());

		// Left
		Composite sfLeft = new Composite(frame, SWT.NULL);
		sfLeft.setLayout(new FormLayout());

		// Right
		Composite sfRight = new Composite(frame, SWT.NULL);
		sfRight.setLayout(new FormLayout());

		// Build the split
		buildSplit(frame, sfLeft, sfRight);

		// Top Left
		mainTextArea = new StyledText(sfLeft, SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		mainTextArea.setEditable(false);

		// Bottom Left
		Text textInput = new Text(sfLeft, SWT.SINGLE | SWT.BORDER);

		// Set the layout data
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.bottom = new FormAttachment(textInput);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		mainTextArea.setLayoutData(fd);

		fd = new FormData();
		fd.height = TEXT_HEIGHT;
		fd.bottom = new FormAttachment(100, 0);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		textInput.setLayoutData(fd);

		// Top Right
		Text txtChannel = new Text(sfRight, SWT.NULL);
		txtChannel.setEditable(false);
		txtChannel.setText("Top Right");

		// Bottom Right
		Button btnBottom = new Button(sfRight, SWT.PUSH);
		btnBottom.setText("Bottom Right");

		// Set the layout data
		fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.height = TEXT_HEIGHT;
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		txtChannel.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(txtChannel);
		fd.bottom = new FormAttachment(100, 0);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		btnBottom.setLayoutData(fd);
	}

	public Composite getFrame() {
		return frame;
	}

	@Override
	public void initialize(Connection source) {
		if(firstConnection == null)
			firstConnection = source;
	}

	@Override
	public void disable(Connection source) {
		//if(source == firstConnection)
		//	SWTDesktop.remove(this);
		//menuBar.remove(settingsMenuItems.remove(source));
		titleChanged(source);
	}

	public Connection getFirstConnection() {
		return this.firstConnection;
	}

	@Override
	public void titleChanged(Connection source) {
		SWTDesktop.setTitle(this, source.getProductID());
	}

	@Override
	public String toString() {
		if(firstConnection == null)
			return null;
		Profile p = firstConnection.getProfile();
		if((p == null) || (p.getConnections().size() == 1))
			return firstConnection.toString();
		return p.getName();
	}
}
