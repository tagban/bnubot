package bnubot.core.friend;

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
	
	public String toString() {
		if(account != null)
			return account + " (locationName=" + locationName + ")";
		return entry + " (locationName=" + locationName + ")";
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
