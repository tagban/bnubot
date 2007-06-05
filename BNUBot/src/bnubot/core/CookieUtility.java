package bnubot.core;

import java.util.Iterator;
import java.util.LinkedList;

public class CookieUtility {
	private static class Cookie {
		private int id;
		private Object obj;
		
		public Cookie(int id, Object obj) {
			this.id = id;
			this.obj = obj;
		}
		
		public int getId() {
			return id;
		}
		
		public Object getObj() {
			return obj;
		}
	}

	private static LinkedList<Cookie> cookies = new LinkedList<Cookie>(); 
	private static int currentCookieNumber = 0;
	
	/**
	 * Creates a cookie
	 * @param obj	The Object associated with the cookie
	 * @return		Cookie ID
	 */
	public static int createCookie(Object obj) {
		Cookie c = new Cookie(currentCookieNumber, obj);
		cookies.add(c);
		return currentCookieNumber++;
	}
	
	/**
	 * Retrieve a cookie
	 * @param id	Cookie ID
	 * @return		The Object associated with the cookie
	 */
	public static Object destroyCookie(int id) {
		Iterator<Cookie> it = cookies.iterator();
		while(it.hasNext()) {
			Cookie c = it.next();
			if(c.getId() == id) {
				cookies.remove(c);
				return c.getObj();
			}
		}
		
		return null;
	}
}
