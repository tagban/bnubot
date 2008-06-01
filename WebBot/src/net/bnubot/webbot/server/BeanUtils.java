package net.bnubot.webbot.server;

import java.text.DateFormat;
import java.util.Date;

import net.bnubot.util.BNetUser;
import net.bnubot.webbot.client.types.BeanBNetUser;
import net.bnubot.webbot.client.types.BeanDate;

public class BeanUtils {

	public static BeanDate beanDate() {
		return beanDate(new Date());
	}

	private static BeanDate beanDate(Date date) {
		BeanDate bd = new BeanDate();
		bd.time = date.getTime();
		bd.string = DateFormat.getTimeInstance().format(date);
		return bd;
	}

	public static BeanBNetUser beanBNetUser(BNetUser user) {
		BeanBNetUser bbnu = new BeanBNetUser();
		bbnu.display = user.toString();
		bbnu.flags = user.getFlags();
		return bbnu;
	}

}
