/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.trivia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import net.bnubot.bot.database.AccountResultSet;
import net.bnubot.bot.database.Database;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.util.BNetUser;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;
import net.bnubot.util.StatString;

public class TriviaEventHandler implements EventHandler {
	private boolean triviaEnabled = false;
	private ArrayList<TriviaItem> trivia = new ArrayList<TriviaItem>();
	private Connection c = null;
	private String triviaAnswers[] = null;
	private boolean gotAnswer = false;
	private BNetUser answerUser = null;
	private String answerUsed = null;
	private Database d = null;
	private int unanswered = 0;
	
	public TriviaEventHandler() {
		this.d = Database.getInstance();
	}
	
	private void readFile(String fileName) {
		BufferedReader is = null;
		try {
			File f = new File(fileName);
			if(!f.exists())
				throw new FileNotFoundException(fileName);
			is = new BufferedReader(new FileReader(f));
		} catch (Exception e) {
			Out.fatalException(e);
		}
		
		String defaultCategory = fileName;
		if(defaultCategory.indexOf('.') != -1)
			defaultCategory = defaultCategory.split("\\.", 2)[0];
		while(defaultCategory.indexOf('/') != -1)
			defaultCategory = defaultCategory.split("\\/", 2)[1];
		while(defaultCategory.indexOf('\\') != -1)
			defaultCategory = defaultCategory.split("\\\\", 2)[1];
		
		long linenumber = 0;
		do {
			String line = null;
			linenumber++;
			try {
				line = is.readLine();
			} catch (IOException e) {
				Out.fatalException(e);
			}
			if(line == null)
				break;
			
			line = line.trim();
			if(line.length() == 0)
				continue;
			
			try {
				trivia.add(new TriviaItem(line, defaultCategory));
			} catch(IllegalArgumentException e) {
				Out.error(getClass(), "Failed to parse line #" + linenumber + " from " + fileName + ": " + line);
			}
		} while(true);
		
		try { is.close(); } catch (Exception e) {}
	}
	
	private void reloadTrivia() {
		File f = new File("trivia");
		if(f.isDirectory())
			for(String fname : f.list())
				readFile(f.getPath() + System.getProperty("file.separator") + fname);
		
		c.recieveInfo("Trivia initialized with " + trivia.size() + " questions");
	}

	public void bnetConnected() {}
	public void bnetDisconnected() {}
	public void titleChanged() {}

	public void channelJoin(BNetUser user, StatString statstr) {}
	public void channelLeave(BNetUser user) {}
	public void channelUser(BNetUser user, StatString statstr) {}
	
	private void showLeaderBoard() {
		try {
			if(d == null)
				return;
			
			ResultSet rsLeaders = d.getTriviaLeaders();
			if(rsLeaders == null)
				return;
			
			String out = "Trivia Leader Board: ";
			while(rsLeaders.next()) {
				out += rsLeaders.getString("name");
				out += "(";
				out += rsLeaders.getLong("trivia_correct");
				out += ") ";
			}
			d.close(rsLeaders);
			out += "Total=" + d.getTriviaSum();
			c.sendChat(out);
		} catch (SQLException e) {
			Out.excepton(e);
		}
	}
	
	private void triviaLoop() {
		while(true) {
			try {
				if(triviaEnabled && c.canSendChat()) {
					if(trivia.size() == 0) {
						c.sendChat("There are no trivia questions left; game over.");
						triviaEnabled = false;
						continue;
					}
					
					try {
						long max[] = d.getTriviaTopTwo();
						if(max != null) {
							long total = d.getTriviaSum();
							long target = c.getConnectionSettings().triviaRoundLength;
							boolean condition = false;
							// There are no questions left
							condition |= (total >= target);
							// First place has half of the points
							condition |= (max[0] > (target/2));
							if(max.length > 1) {
								long questionsLeft = (target - total);
								long bestTop2Score = max[1] + questionsLeft;
								// Second place can't pass first place
								condition |= (bestTop2Score < max[1]);
							}
							if(condition) {
								String out = "The trivia round is over! Congratulations to ";
								out += d.resetTrivia();
								out += " for winning the round!";
								c.sendChat(out);
							}
						}
					} catch (SQLException e) {
						Out.excepton(e);
					}
					
					TriviaItem ti = trivia.remove((int)(Math.random() * trivia.size()));
					
					if(true) {
						String q = "/me";
						if(ti.getCategory() != null)
							q += " - Category: " + ti.getCategory();
						q += " - Question: " + ti.getQuestion();
						q += " - Hint: " + ti.getHint0();
						c.sendChat(q);
						//c.recieveInfo("Answer: " + ti.getAnswer());
					}
					
					triviaAnswers = ti.getAnswers();
					gotAnswer = false;
					answerUser = null;
					
					long timeQuestionAsked = new Date().getTime();
					long timeElapsed = 0;
					int numHints = 0;
					do {
						if(gotAnswer)
							break;
						
						timeElapsed = new Date().getTime() - timeQuestionAsked;
						timeElapsed /= 1000;

						if((timeElapsed > 10) && (numHints < 1)) {
							c.sendChat("/me - 20 seconds left! Hint: " + ti.getHint1());
							numHints++;
						}
						
						if((timeElapsed > 20) && (numHints < 2)) {
							c.sendChat("/me - 10 seconds left! Hint: " + ti.getHint2());
							numHints++;
						}
						
						Thread.sleep(10);
						Thread.yield();
					} while((timeElapsed < 30) && triviaEnabled);

					if(gotAnswer) {
						unanswered = 0;
						String extra = "!";

						try {
							if(d != null) { 
								AccountResultSet rsAccount = d.getAccount(answerUser);
								if(rsAccount.next()) {
									long score = rsAccount.getTriviaCorrect();
									score++;
									rsAccount.setTriviaCorrect(score);
									rsAccount.updateRow();
									extra += " Your score is " + score + ".";
								}
								d.close(rsAccount);
							}
						} catch(Exception e) {
							Out.excepton(e);
						}

						if(triviaAnswers.length > 1) {
							extra += " Other acceptable answers were: ";
							boolean first = true;
							for(int i = 0; i < triviaAnswers.length; i++) {
								if(!triviaAnswers[i].equals(answerUsed)) {
									if(first)
										first = false;
									else
										extra += ", or ";
									extra += "\"" + triviaAnswers[i] + "\"";
								}
							}
						}
						
						c.sendChat("/me - \"" + answerUsed + "\" is correct, " + answerUser.getShortPrettyName() + extra);
						
						showLeaderBoard();
					} else {
						String correct = " The correct answer was \"" + triviaAnswers[0] + "\"";
						for(int i = 1; i < triviaAnswers.length; i++)
							correct += ", or \"" + triviaAnswers[i] + "\"";
						
						if(triviaEnabled) {
							unanswered++;
							c.sendChat("/me - Time's up!" + correct);
						} else {
							c.sendChat("/me - Game over!" + correct);
							continue;
						}
					}

					if(unanswered == 9)
						c.sendChat("Trivia will automaticly shut off after the next question. To extend trivia, type [ trivia on ]");
					if(unanswered >= 10) {
						c.sendChat("Auto-disabling trivia.");
						triviaEnabled = false;
					}
				}
			
				Thread.sleep(1000);
				Thread.yield();
			} catch (InterruptedException e) {}
		}
	}
	
	public void initialize(final Connection c) {
		this.c = c;
		
		new Thread() {
			public void run() {
				triviaLoop();
			}
		}.start();
	}
	
	public void triviaOn() {
		if(trivia.size() == 0)
			reloadTrivia();
			
		unanswered = 0;
		triviaEnabled = true;
	}
	
	public void triviaOff() {
		triviaEnabled = false;
	}
	
	public void joinedChannel(String channel) {}
	public void recieveChat(BNetUser user, String text) {
		if("trivia on".equals(text)) {
			triviaOn();
		} else if("trivia off".equals(text)) {
			triviaOff();
		} else if("trivia score".equals(text)) {
			if(!triviaEnabled)
				showLeaderBoard();
		} else {
			if(triviaAnswers != null) {
				text = HexDump.getAlphaNumerics(text);
				for(String triviaAnswer : triviaAnswers) {
					if(triviaAnswer.compareToIgnoreCase(text) == 0) {
						gotAnswer = true;
						answerUser = user;
						answerUsed = triviaAnswer;
					}
				}
			}
		}
	}
	public void recieveEmote(BNetUser user, String text) {}
	public void recieveError(String text) {}
	public void recieveInfo(String text) {}
	public void whisperRecieved(BNetUser user, String text) {}
	public void whisperSent(BNetUser user, String text) {}

	public void friendsList(FriendEntry[] entries) {}
	public void friendsUpdate(FriendEntry friend) {}
	public void friendsAdd(FriendEntry friend) {}
	public void friendsPosition(byte oldPosition, byte newPosition) {}
	public void friendsRemove(byte entry) {}
	
	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
	public void queryRealms2(String[] realms) {}

	public void parseCommand(BNetUser user, String command, String param, boolean wasWhispered) {}

	public void clanMOTD(Object cookie, String text) {}
	public void clanMemberList(ClanMember[] members) {}
	public void clanMemberRemoved(String username) {}
	public void clanMemberStatusChange(ClanMember member) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}
}
