/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.friend;

import java.util.ArrayList;

/**
 * @author scotta
 */
public class FriendEntry {
	String account = null;
	Long entry = null;
	byte status;
	byte location;
	int product;
	String locationName;

	public FriendEntry(String account, byte status, byte location, int product, String locationName) {
		this.account = account;
		this.status = status;
		this.location = location;
		this.product = product;
		this.locationName = locationName;
	}

	public FriendEntry(long entry, byte status, byte location, int product, String locationName) {
		this.entry = entry;
		this.status = status;
		this.location = location;
		this.product = product;
		this.locationName = locationName;
	}

	@Override
	public String toString() {
		String out = account;
		if(account == null)
			out = entry.toString();

		ArrayList<String> attributes = new ArrayList<String>(5);

		switch(location) {
		case FriendLocationIDs.FRIENDLOCATION_OFFLINE:
			return out + " (offline)";

		case FriendLocationIDs.FRIENDLOCATION_NOT_IN_CHAT:
			attributes.add("Not in chat");
			break;

		case FriendLocationIDs.FRIENDLOCATION_IN_CHAT:
			attributes.add("In channel");
			break;

		case FriendLocationIDs.FRIENDLOCATION_IN_A_PUBLIC_GAME:
			attributes.add("In a public game");
			break;

		case FriendLocationIDs.FRIENDLOCATION_IN_A_PRIVATE_GAME_NOT_MUTUAL:
		case FriendLocationIDs.FRIENDLOCATION_IN_A_PRIVATE_GAME_MUTUAL:
			attributes.add("In a private game");
			break;

		}

		if(locationName.length() > 0)
			attributes.add(locationName);

		if((status & FriendStatusFlags.FRIENDSTATUS_AWAY) != 0)
			attributes.add("away");
		if((status & FriendStatusFlags.FRIENDSTATUS_DND) != 0)
			attributes.add("DND");
		if((status & FriendStatusFlags.FRIENDSTATUS_MUTUAL) != 0)
			attributes.add("mutual");


		if(attributes.size() > 0) {
			out += " (";
			while(attributes.size() > 0) {
				out += attributes.remove(0);
				if(attributes.size() > 0)
					out += ", ";
			}
			out += ")";
		}

		return out;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getAccount() {
		return account;
	}

	public Long getEntry() {
		return entry;
	}

	public byte getLocation() {
		return location;
	}

	public String getLocationName() {
		return locationName;
	}

	public int getProduct() {
		return product;
	}

	public byte getStatus() {
		return status;
	}
}
