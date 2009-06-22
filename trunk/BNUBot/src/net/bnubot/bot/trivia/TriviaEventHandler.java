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
import java.util.LinkedList;
import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.UnloggedException;
import net.bnubot.util.crypto.HexDump;

import org.apache.cayenne.ObjectContext;

/**
 * @author scotta
 */
public class TriviaEventHandler extends EventHandler {
	private boolean triviaEnabled = false;
	private final List<TriviaItem> trivia = new LinkedList<TriviaItem>();
	private TriviaItem triviaCurrent = null;
	private BNetUser answerUser = null;
	private String answerUsed = null;
	private int unanswered = 0;
	private Connection initializedConnection = null;
	private boolean disposed = false;

	public TriviaEventHandler(Profile profile) {
		super(profile);
		if(DatabaseContext.getContext() == null)
			throw new UnloggedException("Can not enable trivia without a database!");
		initializeCommands();
	}

	private static boolean commandsInitialized = false;
	public static void initializeCommands() {
		if(commandsInitialized)
			return;
		commandsInitialized = true;

		Profile.registerCommand("trivia", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				TriviaEventHandler teh = EventHandler.findThis(source, TriviaEventHandler.class);
				if(teh == null) {
					user.sendChat("Trivia is not enabled.", whisperBack);
					return;
				}

				if(teh.triviaEnabled) {
					if("off".equals(param)) {
						teh.triviaOff();
						return;
					}
					user.sendChat("Use: %trigger%trivia off", whisperBack);
				} else {
					if("on".equals(param)) {
						teh.triviaOn(source);
						return;
					} else if("score".equals(param)) {
						teh.showLeaderBoard(source);
						return;
					}
					user.sendChat("Use: %trigger%trivia ( on | score )", whisperBack);
				}
			}});
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

	private void reloadTrivia(Connection source) {
		File f = new File("trivia");
		if(!f.exists())
			f.mkdir();
		if(f.isDirectory())
			for(String fname : f.list())
				readFile(f.getPath() + System.getProperty("file.separator") + fname);

		source.dispatchRecieveInfo("Trivia initialized with " + trivia.size() + " questions");
	}

	@Override
	public void bnetConnected(Connection source) {
		triviaEnabled = false;
	}

	@Override
	public void bnetDisconnected(Connection source) {
		triviaEnabled = false;
	}

	private void showLeaderBoard(Connection source) {
		try {
			ObjectContext context = DatabaseContext.getContext();
			if(context == null)
				return;

			List<Account> leaders = Account.getTriviaLeaders();
			if(leaders == null)
				return;
			if(leaders.size() == 0)
				return;

			StringBuilder out = new StringBuilder("Trivia Leader Board: ");
			for(Account a : leaders) {
				out.append(a.getName()).append('(');
				out.append(a.getTriviaCorrect()).append(") ");
			}
			out.append("Total=").append(getTriviaSum());
			source.sendChat(out.toString());
		} catch (Exception e) {
			Out.exception(e);
		}
	}

	private long getTriviaSum() {
		long total = 0;
		for(Account a : Account.getTriviaLeaders())
			total += a.getTriviaCorrect();
		return total;
	}

	private long[] getTriviaTopTwo() {
		List<Account> leaders = Account.getTriviaLeaders();
		if((leaders == null) || (leaders.size() == 0))
			return null;

		if(leaders.size() == 1)
			return new long[] {leaders.get(0).getTriviaCorrect()};

		return null;
	}

	private String resetTrivia() {
		List<Account> leaders = Account.getTriviaLeaders();
		if((leaders != null) && (leaders.size() > 0)) {
			Account winner = leaders.get(0);
			// Increment the winner's wins
			winner.setTriviaWin(winner.getTriviaWin() + 1);
			// Reset all scores to zero
			for(Account a : leaders)
				a.setTriviaCorrect(0);
			try {
				// Save changes
				winner.updateRow();
				// Return the winner's name
				return winner.getName();
			} catch(Exception e) {
				Out.exception(e);
			}
		}
		return null;
	}

	private void triviaLoop(Connection source) {
		while(!disposed) {
			try {
				if(triviaEnabled && source.canSendChat()) {
					if(trivia.size() == 0) {
						source.sendChat("There are no trivia questions left; game over.");
						triviaEnabled = false;
						continue;
					}

					if((DatabaseContext.getContext() != null) && (GlobalSettings.triviaRoundLength > 0)) {
						try {
							long max[] = getTriviaTopTwo();
							if(max != null) {
								final long total = getTriviaSum();
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
									out += resetTrivia();
									out += " for winning the round!";
									source.sendChat(out);
								}
							}
						} catch (Exception e) {
							Out.exception(e);
						}
					}

					triviaCurrent = trivia.remove((int)(Math.random() * trivia.size()));
					answerUser = null;

					if(true) {
						String q = "/me";
						if(triviaCurrent.getCategory() != null)
							q += " - Category: " + triviaCurrent.getCategory();
						q += " - Question: " + triviaCurrent.getQuestion();
						q += " - Hint: " + triviaCurrent.getHint0();
						source.sendChat(q);
						//c.recieveInfo("Answer: " + ti.getAnswer());
					}

					long timeQuestionAsked = System.currentTimeMillis();
					long timeElapsed = 0;
					int numHints = 0;
					do {
						if(answerUser != null)
							break;

						timeElapsed = System.currentTimeMillis() - timeQuestionAsked;
						timeElapsed /= 1000;

						if((timeElapsed > 10) && (numHints < 1)) {
							source.sendChat("/me - 20 seconds left! Hint: " + triviaCurrent.getHint1());
							numHints++;
						}

						if((timeElapsed > 20) && (numHints < 2)) {
							source.sendChat("/me - 10 seconds left! Hint: " + triviaCurrent.getHint2());
							numHints++;
						}

						Thread.sleep(200);
						Thread.yield();
					} while((timeElapsed < 30) && triviaEnabled);

					if(answerUser != null) {
						unanswered = 0;
						String extra = "!";

						if(DatabaseContext.getContext() != null) {
							try {
								Account answeredBy = Account.get(answerUser);
								if(answeredBy != null) {
									int score = answeredBy.getTriviaCorrect();
									score++;
									answeredBy.setTriviaCorrect(score);
									answeredBy.updateRow();
									extra += " Your score is " + score + ".";
								}
							} catch(Exception e) {
								Out.exception(e);
							}
						}

						String[] triviaAnswersAN = triviaCurrent.getAnswersAlphaNumeric();
						if(triviaAnswersAN.length > 1) {
							extra += " Other acceptable answers were: ";
							boolean first = true;
							for(String answer : triviaCurrent.getAnswers()) {
								if(!answer.equals(answerUsed)) {
									if(first)
										first = false;
									else
										extra += ", or ";
									extra += "\"" + answer + "\"";
								}
							}
						}

						source.sendChat("/me - \"" + answerUsed + "\" is correct, " + answerUser.toString() + extra);

						if(GlobalSettings.triviaRoundLength > 0)
							showLeaderBoard(source);
					} else {
						String[] triviaAnswers = triviaCurrent.getAnswers();
						String correct = " The correct answer was \"" + triviaAnswers[0] + "\"";
						for(int i = 1; i < triviaAnswers.length; i++)
							correct += ", or \"" + triviaAnswers[i] + "\"";

						if(triviaEnabled) {
							unanswered++;
							source.sendChat("/me - Time's up!" + correct);
						} else {
							source.sendChat("/me - Game over!" + correct);
							continue;
						}
					}

					if(unanswered == 9)
						source.sendChat("Trivia will automaticly shut off after the next question. To extend trivia, type [ trivia on ]");
					if(unanswered >= 10) {
						source.sendChat("Auto-disabling trivia.");
						triviaEnabled = false;
					}
				}

				Thread.sleep(1000);
				Thread.yield();
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public void initialize(final Connection source) {
		if(initializedConnection == null) {
			initializedConnection = source;
			new Thread() {
				@Override
				public void run() {
					triviaLoop(source);
				}
			}.start();
		}
	}
	@Override
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

	@Override
	public void joinedChannel(Connection source, String channel) {
		triviaEnabled = false;
	}

	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		if(!triviaEnabled)
			return;
		if(triviaCurrent == null)
			return;
		String textAN = HexDump.getAlphaNumerics(text);
		String[] triviaAnswersAN = triviaCurrent.getAnswersAlphaNumeric();
		for(int i = 0; i < triviaAnswersAN.length; i++) {
			if(triviaAnswersAN[i].equalsIgnoreCase(textAN)) {
				answerUser = user;
				answerUsed = triviaCurrent.getAnswers()[i];
			}
		}
	}
}
