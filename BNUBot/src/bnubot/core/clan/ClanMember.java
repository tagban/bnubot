package bnubot.core.clan;

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
	
	public String toString() {
		return username + " (" + ClanRankIDs.ClanRank[rank] + ", online=" + online + ", location=" + location + ")";
	}
}
