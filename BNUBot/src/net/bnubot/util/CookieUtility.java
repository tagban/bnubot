/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.util.LinkedList;
import java.util.List;

/**
 * @author scotta
 */
public class CookieUtility {
	private static class Cookie {
		private final int id;
		private final Object obj;

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

	private static final List<Cookie> cookies = new LinkedList<Cookie>();
	private static int currentCookieNumber = 0;

	/**
	 * Creates a cookie
	 * @param obj	The Object associated with the cookie
	 * @return		Cookie ID
	 */
	public static int createCookie(Object obj) {
		cookies.add(new Cookie(currentCookieNumber, obj));
		return currentCookieNumber++;
	}

	/**
	 * Retrieve a cookie
	 * @param id	Cookie ID
	 * @return		The Object associated with the cookie
	 */
	public static Object destroyCookie(int id) {
		for(Cookie c : cookies) {
			if(c.getId() == id) {
				cookies.remove(c);
				return c.getObj();
			}
		}

		return null;
	}
}
