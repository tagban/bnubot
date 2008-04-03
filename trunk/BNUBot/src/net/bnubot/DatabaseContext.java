/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.conf.Configuration;

public class DatabaseContext {
	private static ThreadLocal<ObjectContext> contexts = new ThreadLocal<ObjectContext>();
	static {
		try {
			Configuration.initializeSharedConfiguration();
		} catch(Exception e) {
			contexts = null;
		}
	}
	
	public static ObjectContext getContext() {
		if(contexts == null)
			return null;
		
		ObjectContext oc = contexts.get();
		if(oc == null) {
			oc = DataContext.createDataContext();
			contexts.set(oc);
		}
		return oc;
	}
}
