/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import junit.framework.TestCase;
import net.bnubot.util.task.Task;
import net.bnubot.util.task.TaskManager;

/**
 * @author scotta
 */
public class TaskTest extends TestCase {

	public void testTasks() throws Exception {
		Task ind = TaskManager.createTask("indeterminate task");
		Task det = TaskManager.createTask("determinate task", 10, "steps");
		for(int i = 0; i < 10; i++) {
			ind.updateProgress(Integer.toString(i));
			Thread.sleep(100);
			det.advanceProgress();
		}
		ind.complete();
	}
}
