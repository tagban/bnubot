package bnubot.core.bncs.friend;

public class FriendEntry {
	String account;
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
	
	public String toString() {
		return account + " (locationName=" + locationName + ")";
	}
}
