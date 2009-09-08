/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.clan;

/**
 * @author scotta
 */
public class ClanMember {
	String username;
	byte rank;
	byte online;
	String location;

	public ClanMember(String username, byte rank, byte online, String location) {
		this.username = username;
		this.rank = rank;
		this.online = online;
		this.location = location;
	}

	public String getUsername() {
		return username;
	}

	public byte getRank() {
		return rank;
	}

	public void setRank(byte r) {
		rank = r;
	}

	@Override
	public String toString() {
		String out = username;
		if(online == 0)
			out += " (offline)";
		//out += ClanRankIDs.ClanRank[rank];
		//out += ", ";
		if(location.length() > 0)
			out += ", " + location;
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof String) {
			if(username.compareToIgnoreCase((String)o) == 0)
				return true;
			return false;
		}

		return super.equals(o);
	}
}
