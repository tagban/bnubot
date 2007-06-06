package bnubot.core.bncs.clan;

public class ClanMember {
	String name;
	byte rank;
	byte online;
	String location;
	
	public ClanMember(String name, byte rank, byte online, String location) {
		this.name = name;
		this.rank = rank;
		this.online = online;
		this.location = location;
	}
	
	public String toString() {
		return name + " (" + ClanRankIDs.ClanRank[rank] + ", online=" + online + ", location=" + location + ")";
	}
}
