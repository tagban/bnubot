/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class TaskTest extends TestCase {

	public void testTasks() throws Exception {
		Task ind = TaskManager.createTask("indeterminate task");
		Task det = TaskManager.createTask("determinate task", 10, "steps");
		for(int i = 0; i < 10; i++) {
			ind.updateProgress(Integer.toString(i));
			det.advanceProgress();
		}
		ind.complete();
	}
}
