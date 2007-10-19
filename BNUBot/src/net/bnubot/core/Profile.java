/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

import java.util.LinkedList;

public class Profile {
	private static LinkedList<Profile> profiles = new LinkedList<Profile>();

	public static Profile findCreateProfile(String name) {
		synchronized(profiles) {
			for(Profile p : profiles)
				if(p.getName().equals(name))
					return p;
		}
		return new Profile(name);
	}

	private LinkedList<Connection> cons = new LinkedList<Connection>();
	private String name;

	public Profile(String name) {
		this.name = name;

		synchronized(profiles) {
			profiles.add(this);
		}
	}

	public boolean add(Connection c) {
		synchronized(cons) {
			return cons.add(c);
		}
	}

	public String getName() {
		return name;
	}
}
