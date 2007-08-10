/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core.mcp;

public interface MCPCommandIDs {
	public static final byte MCP_STARTUP			= (byte)0x01;  
	public static final byte MCP_CHARCREATE			= (byte)0x02;
	public static final byte MCP_CREATEGAME			= (byte)0x03;
	public static final byte MCP_JOINGAME			= (byte)0x04;
	public static final byte MCP_GAMELIST			= (byte)0x05;
	public static final byte MCP_GAMEINFO			= (byte)0x06;
	public static final byte MCP_CHARLOGON			= (byte)0x07;
	public static final byte MCP_CHARDELETE			= (byte)0x0A;
	public static final byte MCP_REQUESTLADDERDATA	= (byte)0x11;
	public static final byte MCP_MOTD				= (byte)0x12;
	public static final byte MCP_CANCELGAMECREATE	= (byte)0x13;
	public static final byte MCP_CREATEQUEUE		= (byte)0x14;
	public static final byte MCP_CHARLIST			= (byte)0x17;
	public static final byte MCP_CHARUPGRADE		= (byte)0x18;
	public static final byte MCP_CHARLIST2			= (byte)0x19;
}
