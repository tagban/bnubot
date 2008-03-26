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
import java.util.LinkedList;
import java.util.List;

import net.bnubot.bot.database.AccountResultSet;
import net.bnubot.bot.database.Database;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class TriviaEventHandler implements EventHandler {
	private boolean triviaEnabled = false;
	private final List<TriviaItem> trivia = new LinkedList<TriviaItem>();
	private String triviaAnswers[] = null;
	private BNetUser answerUser = null;
	private String answerUsed = null;
	private Database d = Database.getInstance();
	private int unanswered = 0;
	private Connection initializedConnection = null;
	private boolean disposed = false;
	
	public TriviaEventHandler() {}
	
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
	
	private void reloadTrivia(Connection source) {
		File f = new File("trivia");
		if(!f.exists())
			f.mkdir();
		if(f.isDirectory())
			for(String fname : f.list())
				readFile(f.getPath() + System.getProperty("file.separator") + fname);
		
		source.recieveInfo("Trivia initialized with " + trivia.size() + " questions");
	}

	public void bnetConnected(Connection source) {}
	public void bnetDisconnected(Connection source) {}
	public void titleChanged(Connection source) {}

	public void channelJoin(Connection source, BNetUser user) {}
	public void channelLeave(Connection source, BNetUser user) {}
	public void channelUser(Connection source, BNetUser user) {}
	
	private void showLeaderBoard(Connection source) {
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
			source.queueChatHelper(out, false);
		} catch (SQLException e) {
			Out.exception(e);
		}
	}
	
	private void triviaLoop(Connection source) {
		while(!disposed) {
			try {
				if(triviaEnabled && source.canSendChat()) {
					if(trivia.size() == 0) {
						source.queueChatHelper("There are no trivia questions left; game over.", false);
						triviaEnabled = false;
						continue;
					}
					
					if(d != null) {
						try {
							long max[] = d.getTriviaTopTwo();
							if(max != null) {
								final long total = d.getTriviaSum();
								final long target = GlobalSettings.triviaRoundLength;
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
									source.queueChatHelper(out, false);
								}
							}
						} catch (SQLException e) {
							Out.exception(e);
						}
					}
					
					TriviaItem ti = trivia.remove((int)(Math.random() * trivia.size()));
					
					if(true) {
						String q = "/me";
						if(ti.getCategory() != null)
							q += " - Category: " + ti.getCategory();
						q += " - Question: " + ti.getQuestion();
						q += " - Hint: " + ti.getHint0();
						source.queueChatHelper(q, false);
						//c.recieveInfo("Answer: " + ti.getAnswer());
					}
					
					triviaAnswers = ti.getAnswers();
					answerUser = null;
					
					long timeQuestionAsked = System.currentTimeMillis();
					long timeElapsed = 0;
					int numHints = 0;
					do {
						if(answerUser != null)
							break;
						
						timeElapsed = System.currentTimeMillis() - timeQuestionAsked;
						timeElapsed /= 1000;

						if((timeElapsed > 10) && (numHints < 1)) {
							source.queueChatHelper("/me - 20 seconds left! Hint: " + ti.getHint1(), false);
							numHints++;
						}
						
						if((timeElapsed > 20) && (numHints < 2)) {
							source.queueChatHelper("/me - 10 seconds left! Hint: " + ti.getHint2(), false);
							numHints++;
						}
						
						Thread.sleep(200);
						Thread.yield();
					} while((timeElapsed < 30) && triviaEnabled);

					if(answerUser != null) {
						unanswered = 0;
						String extra = "!";

						try {
							if(d != null) {
								AccountResultSet rsAccount = d.getAccount(answerUser);
								if((rsAccount != null) && rsAccount.next()) {
									long score = rsAccount.getTriviaCorrect();
									score++;
									rsAccount.setTriviaCorrect(score);
									rsAccount.updateRow();
									extra += " Your score is " + score + ".";
								}
								if(rsAccount != null)
									d.close(rsAccount);
							}
						} catch(Exception e) {
							Out.exception(e);
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
						
						source.queueChatHelper("/me - \"" + answerUsed + "\" is correct, " + answerUser.toString() + extra, false);
						
						showLeaderBoard(source);
					} else {
						String correct = " The correct answer was \"" + triviaAnswers[0] + "\"";
						for(int i = 1; i < triviaAnswers.length; i++)
							correct += ", or \"" + triviaAnswers[i] + "\"";
						
						if(triviaEnabled) {
							unanswered++;
							source.queueChatHelper("/me - Time's up!" + correct, false);
						} else {
							source.queueChatHelper("/me - Game over!" + correct, false);
							continue;
						}
					}

					if(unanswered == 9)
						source.queueChatHelper("Trivia will automaticly shut off after the next question. To extend trivia, type [ trivia on ]", false);
					if(unanswered >= 10) {
						source.queueChatHelper("Auto-disabling trivia.", false);
						triviaEnabled = false;
					}
				}
			
				Thread.sleep(1000);
				Thread.yield();
			} catch (InterruptedException e) {}
		}
	}
	
	public void initialize(final Connection source) {
		if(initializedConnection == null) {
			initializedConnection = source;
			new Thread() {
				public void run() {
					triviaLoop(source);
				}
			}.start();
		}
	}
	public void disable(final Connection source) {
		if(initializedConnection == source)
			disposed = true;
	}
	
	public void triviaOn(Connection source) {
		if(trivia.size() == 0)
			reloadTrivia(source);
			
		unanswered = 0;
		triviaEnabled = true;
	}
	
	public void triviaOff() {
		triviaEnabled = false;
	}
	
	public void joinedChannel(Connection source, String channel) {}
	public void recieveChat(Connection source, BNetUser user, String text) {
		if(!triviaEnabled) {
			if("trivia on".equals(text)) {
				triviaOn(source);
			} else if("trivia score".equals(text)) {
				if(!triviaEnabled)
					showLeaderBoard(source);
			}
		} else {
			if("trivia off".equals(text)) {
				triviaOff();
			} else {
				if(triviaAnswers != null) {
					text = HexDump.getAlphaNumerics(text);
					for(String triviaAnswer : triviaAnswers) {
						if(triviaAnswer.compareToIgnoreCase(text) == 0) {
							answerUser = user;
							answerUsed = triviaAnswer;
						}
					}
				}
			}
		}
	}
	public void recieveEmote(Connection source, BNetUser user, String text) {}
	public void recieveError(Connection source, String text) {}
	public void recieveInfo(Connection source, String text) {}
	public void recieveDebug(Connection source, String text) {}
	public void whisperRecieved(Connection source, BNetUser user, String text) {}
	public void whisperSent(Connection source, BNetUser user, String text) {}

	public void friendsList(Connection source, FriendEntry[] entries) {}
	public void friendsUpdate(Connection source, FriendEntry friend) {}
	public void friendsAdd(Connection source, FriendEntry friend) {}
	public void friendsPosition(Connection source, byte oldPosition, byte newPosition) {}
	public void friendsRemove(Connection source, byte entry) {}
	
	public void logonRealmEx(Connection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
	public void queryRealms2(Connection source, String[] realms) {}

	public boolean parseCommand(Connection source, BNetUser user, String command, boolean whisperBack) {return false;}

	public void clanMOTD(Connection source, Object cookie, String text) {}
	public void clanMemberList(Connection source, ClanMember[] members) {}
	public void clanMemberRemoved(Connection source, String username) {}
	public void clanMemberStatusChange(Connection source, ClanMember member) {}
	public void clanMemberRankChange(Connection source, byte oldRank, byte newRank, String user) {}
}
