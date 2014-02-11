/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.bnubot.util.OperatingSystem;

/**
 * @author scotta
 */
public class TaskGui extends Task {
	private final Box box;
	private final JProgressBar pb;
	private final JLabel jl;
	private int count = 0;
	private int max;
	private String units;
	private final String title;
	private boolean complete = false;

	protected TaskGui(String title, int max, String units) {
		this.max = max;
		this.units = units;
		this.title = title;

		pb = new JProgressBar(0, max);
		pb.setString(title);
		pb.setStringPainted(true);
		pb.setIndeterminate(max == 0);

		Dimension d = new Dimension(379, 24);
		pb.setPreferredSize(d);

		switch(OperatingSystem.userOS) {
		case OSX:
			box = new Box(BoxLayout.Y_AXIS);
			box.add(jl = new JLabel(title));
			box.add(pb);

			box.setBackground(Color.WHITE);
			jl.setForeground(Color.BLACK);
			break;
		default:
			box = null;
			jl = null;
			break;
		}
	}

	@Override
	public void setIndeterminate() {
		pb.setIndeterminate(true);
		this.max = 0;
		this.units = null;
	}

	@Override
	public void setDeterminate(int length, String units) {
		pb.setIndeterminate(false);
		pb.setMaximum(length);
		pb.setValue(0);
		this.max = length;
		this.units = units;
	}

	protected Component getComponent() {
		if(box != null)
			return box;
		return pb;
	}

	protected boolean isDeterminant() {
		return (max != 0);
	}

	/**
	 * Update the count and then update the progress indicator.
	 * @param currentStep A string indicating the current step of the task
	 */
	@Override
	public void updateProgress(String currentStep) {
		if(complete) {
			setString("Complete");
		} else {
			StringBuilder s = new StringBuilder();
			if(isDeterminant()) {
				int percentComplete = (int)(count * 100.0 / max);
				s.append(String.valueOf(percentComplete)).append(" % ");
				if(units != null) {
					s.append("(");
					s.append(count).append("/");
					s.append(max).append(" ");
					s.append(units).append(") ");
				}
			}
			if(currentStep != null)
				s.append(currentStep);
			setString(s.toString());
		}
	}

	/**
	 * Set the progressbar string and repaint
	 */
	private void setString(String s) {
		if(jl == null) {
			s = title + ": " + s;
			if(!s.equals(pb.getString()))
				pb.setString(s);
		} else {
			if(!s.equals(jl.getText()))
				jl.setText(s);
		}
	}

	/**
	 * Increment the count and then update the progress indicator.
	 */
	@Override
	public void advanceProgress() {
		setProgress(++count);
	}


	/**
	 * Update the count and then update the progress indicator.
	 */
	@Override
	public void setProgress(int step) {
		setProgress(step, null);
	}

	@Override
	public void setProgress(int step, String currentStep) {
		count = step;
		updateProgress(currentStep);
		if(count >= max) {
			pb.setValue(max);
			complete();
		} else {
			pb.setValue(count);
		}
	}

	@Override
	public void complete() {
		complete = true;
		TaskManager.complete(this);
	}
}
