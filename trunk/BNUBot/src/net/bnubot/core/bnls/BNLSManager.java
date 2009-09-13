/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bnls;

import java.io.IOException;

import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.util.task.Task;

/**
 * @author scotta
 */
public class BNLSManager {
	private static BNLSConnection bnls;

	public static void initialize(Task connect) throws IOException {
		if(bnls == null)
			bnls = new BNLSConnection();
		bnls.initialize(connect);
	}

	public static BNLSConnection getConnection() {
		return bnls;
	}

	public static int getVerByte(ProductIDs product) throws IOException {
		return getConnection().getVerByte(product);
	}

	public static VersionCheckResult sendVersionCheckEx2(Task task, ProductIDs productID, long mpqFileTime, String mpqFileName, byte[] valueStr) throws IOException, InterruptedException {
		return getConnection().sendVersionCheckEx2(task, productID, mpqFileTime, mpqFileName, valueStr);
	}
}
