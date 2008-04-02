/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import net.bnubot.db.Command;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.conf.Configuration;

public class DatabaseContext {
	private static ThreadLocal<ObjectContext> contexts = new ThreadLocal<ObjectContext>();
	static {
		Configuration.initializeSharedConfiguration();
	}
	
	public static ObjectContext getContext() {
		ObjectContext oc = contexts.get();
		if(oc == null) {
			oc = DataContext.createDataContext();
			contexts.set(oc);
		}
		return oc;
	}
	
	public static void main(String[] args) {
		/*BNetUser camel = new BNetUser(null, "BNU-Camel@USEast");
		System.out.println(camel.toString());*/
		
		/*DataContext context = getContext();
		Account camel = DataObjectUtils.objectForPK(context, Account.class, 1);
		Account john = DataObjectUtils.objectForPK(context, Account.class, 2);
		Mail.send(context, john, camel, "asdf");
		for(Mail m : camel.getRecievedMail())
			System.out.println(m.toString());*/
		
		for(Command c : Command.getGroups()) {
			System.out.println(c.getCmdgroup());
		}
	}
}
