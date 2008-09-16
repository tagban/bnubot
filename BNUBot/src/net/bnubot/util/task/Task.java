/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

/**
 * @author scotta
 */
public class Task {
	public void updateProgress(String currentStep) {}
	public void advanceProgress() {}
	public void setProgress(int step) {}
	public void setProgress(int step, String currentStep) {}
	public void complete() {}
	public void setDeterminate(int length, String units) {}
	public void setIndeterminate() {}
}
