package net.bnubot;

import net.bnubot.db.Account;
import net.bnubot.db.Mail;
import net.bnubot.util.BNetUser;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.conf.Configuration;

public class DatabaseContext {
	private static ThreadLocal<DataContext> contexts = new ThreadLocal<DataContext>();
	static {
		Configuration.initializeSharedConfiguration();
	}
	
	public static DataContext getContext() {
		DataContext dc = contexts.get();
		if(dc == null) {
			dc = DataContext.createDataContext();
			contexts.set(dc);
		}
		return dc;
	}
	
	public static void main(String[] args) {
		BNetUser camel = new BNetUser(null, "BNU-Camel@USEast");
		System.out.println(camel.toString());
		
		/*DataContext context = getContext();
		Account camel = DataObjectUtils.objectForPK(context, Account.class, 1);
		Account john = DataObjectUtils.objectForPK(context, Account.class, 2);
		Mail.send(context, john, camel, "asdf");
		for(Mail m : camel.getRecievedMail())
			System.out.println(m.toString());*/
	}
}
