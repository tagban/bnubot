package bnubot.bot.trivia;

import java.util.ArrayList;

public class TriviaItem {
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
	
	private void makeHints() {
		byte[] a = answers[0].getBytes();
		hint0 = "";
		hint1 = "";
		hint2 = "";
		int numHidden = 0;
		for(int i = 0; i < a.length; i++) {
			switch(a[i]) {
			case ' ':
			case '.':
			case '!':
			case '@':
			case '#':
			case '$':
			case '%':
			case '^':
			case '&':
			case '*':
			case '(':
			case ')':
				hint0 += (char)a[i];
				hint1 += (char)a[i];
				hint2 += (char)a[i];
				break;
			default:
				numHidden++;
				if(numHidden % 3 < 2) { //(Math.random() * 3 < 2) {
					hint0 += '?';
					hint1 += '?';
					if(numHidden % 3 < 1) //(Math.random() * 2 < 1)
						hint2 += '?';
					else
						hint2 += (char)a[i];
				} else {
					hint0 += '?';
					hint1 += (char)a[i];
					hint2 += (char)a[i];
				}
			}
		}
	}
	
	public TriviaItem(String scramble) {
		this.question = "Scramble: " + scrambleWord(scramble);
		this.answers = new String[1];
		this.answers[0] = scramble;
		makeHints();
	}
	
	public TriviaItem(String question, String answer) {
		this.question = question;
		this.answers = answer.split("\\*");
		makeHints();
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
