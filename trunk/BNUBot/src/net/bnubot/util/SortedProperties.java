/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class SortedProperties extends Properties {
	private static final long serialVersionUID = 213081373529965920L;
	
	private static final Comparator<Object> comparator = new Comparator<Object>() {
		public int compare(Object o1, Object o2) {
			return ((String)o1).compareTo((String)o2);
		}
	};
	
	public synchronized Enumeration<Object> keys() {
		Vector<Object> keyList = new Vector<Object>(super.keySet());
		Collections.sort(keyList, comparator);
		return keyList.elements();
	}
}