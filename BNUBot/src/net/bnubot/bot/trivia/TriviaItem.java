/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.trivia;

import java.util.LinkedList;
import java.util.List;

import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class TriviaItem {
	private String category;
	private String question;
	private String[] answers;
	private String[] answersAlphaNumeric;
	private String hint0;
	private String hint1;
	private String hint2;

	private String scrambleWord(String word) {
		List<Character> bytes = new LinkedList<Character>();
		for(int i = 0; i < word.length(); i++)
			bytes.add(word.charAt(i));

		String out = "";
		while(bytes.size() > 0)
			out += bytes.remove((int)(Math.random() * bytes.size()));

		return out;
	}

	private boolean isAlphaNumeric(char b) {
		if((b >= 'a') && (b <= 'z'))
			return true;
		if((b >= 'A') && (b <= 'Z'))
			return true;
		if((b >= '0') && (b <= '9'))
			return true;

		return false;
	}

	private void makeHints() {
		hint0 = "";
		hint1 = "";
		hint2 = "";
		int numHidden = 0;
		for(char c : answers[0].toCharArray()) {
			if(isAlphaNumeric(c)) {
				numHidden++;
				if(numHidden % 3 < 2) { //(Math.random() * 3 < 2) {
					hint0 += '?';
					hint1 += '?';
					if(numHidden % 3 < 1) //(Math.random() * 2 < 1)
						hint2 += '?';
					else
						hint2 += c;
				} else {
					hint0 += '?';
					hint1 += c;
					hint2 += c;
				}
			} else {
				hint0 += c;
				hint1 += c;
				hint2 += c;
			}
		}
	}

	public TriviaItem(String line, String defaultCategory) {
		if(line.charAt(0) == '/') {
			// "/category/answer1/answer2//question"
			String qa[] = line.split("//", 2);
			String ca[] = qa[0].split("/");
			this.category = ca[1];
			this.question = qa[1];
			this.answers = new String[ca.length - 2];
			for(int i = 2; i < ca.length; i++)
				this.answers[i-2] = ca[i];
		} else {
			// "Question*answer*answer2*..."
			// "Scramble*word"
			this.category = defaultCategory;

			String splitRegex = "\\*";

			String qa[] = line.split(splitRegex, 2);
			if(qa.length != 2) {
				splitRegex = "\\|";
				qa = line.split(splitRegex, 2);
			}

			if(qa.length == 2) {
				if("Scramble".equals(qa[0])) {
					this.question = "Scramble: " + scrambleWord(qa[1]);
					this.answers = new String[1];
					this.answers[0] = qa[1];
				} else {
					this.question = qa[0];
					this.answers = qa[1].split(splitRegex);
				}
			} else
				throw new IllegalArgumentException(line);
		}

		this.answersAlphaNumeric = new String[this.answers.length];
		for(int i = 0; i < this.answers.length; i++)
			this.answersAlphaNumeric[i] = HexDump.getAlphaNumerics(this.answers[i]);

		makeHints();
	}

	public String getCategory() {
		return category;
	}

	public String getQuestion() {
		return question;
	}

	public String[] getAnswers() {
		return answers;
	}

	public String[] getAnswersAlphaNumeric() {
		return answersAlphaNumeric;
	}

	public String getHint0() {
		return hint0;
	}

	public String getHint1() {
		return hint1;
	}

	public String getHint2() {
		return hint2;
	}
}
