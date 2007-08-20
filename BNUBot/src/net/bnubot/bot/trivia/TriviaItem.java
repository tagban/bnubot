/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.trivia;

import java.util.ArrayList;

import net.bnubot.util.HexDump;

public class TriviaItem {
	private String category;
	private String question;
	private String[] answers;
	private String hint0;
	private String hint1;
	private String hint2;
	
	private String scrambleWord(String word) {
		ArrayList<Character> bytes = new ArrayList<Character>();
		for(int i = 0; i < word.length(); i++)
			bytes.add(word.charAt(i));
		
		String out = "";
		while(bytes.size() > 0)
			out += bytes.remove((int)(Math.random() * bytes.size()));
		
		return out;
	}
	
	private boolean isAlphaNumeric(byte b) {
		if((b >= 'a') && (b <= 'z'))
			return true;
		if((b >= 'A') && (b <= 'Z'))
			return true;
		if((b >= '0') && (b <= '9'))
			return true;
		
		return false;
	}
	
	private void makeHints() {
		byte[] a = answers[0].getBytes();
		hint0 = "";
		hint1 = "";
		hint2 = "";
		int numHidden = 0;
		for(byte element : a) {
			if(isAlphaNumeric(element)) {
				numHidden++;
				if(numHidden % 3 < 2) { //(Math.random() * 3 < 2) {
					hint0 += '?';
					hint1 += '?';
					if(numHidden % 3 < 1) //(Math.random() * 2 < 1)
						hint2 += '?';
					else
						hint2 += (char)element;
				} else {
					hint0 += '?';
					hint1 += (char)element;
					hint2 += (char)element;
				}
			} else {
				hint0 += (char)element;
				hint1 += (char)element;
				hint2 += (char)element;
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
		
		for(int i = 0; i < this.answers.length; i++)
			this.answers[i] = HexDump.getAlphaNumerics(this.answers[i]);
		
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
