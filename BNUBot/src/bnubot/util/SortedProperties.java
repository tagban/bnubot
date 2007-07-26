package bnubot.util;

import java.util.*;

@SuppressWarnings("serial")
public class SortedProperties extends Properties {
	@SuppressWarnings("unchecked")
	public synchronized Enumeration keys() {
		Enumeration keysEnum = super.keys();
		Vector keyList = new Vector();
		while(keysEnum.hasMoreElements())
			keyList.add(keysEnum.nextElement());
		Collections.sort(keyList);
		return keyList.elements();
	}
}