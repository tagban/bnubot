/**
 * This file is distributed under the GPL
 *
 * This file originated from:
 * http://www.cs.princeton.edu/introcs/72regular/Wildcard.java.html
 * and has been modified to suit my needs.
 *
 * $Id$
 */

package net.bnubot.util;

/**
 *  Compilation:  javac Wildcard.java In.java
 *  Execution:    java Wildcard pattern < wordlist.txt
 *
 *  Find all lines in wordlist.txt that match the given pattern by
 *  simulating a nondeterminstic finite state automaton using an
 *  Boolean array states[] which records all states that the NFSA
 *  could be in after reading in a certain number of characters.
 *
 *     Patterns supported
 *     ------------------------------
 *     *  any zero or more characters
 *     .  any one character *REMOVED*
 *     c  character c
 *
 *  Note: not the most efficient algorithm.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * @author scotta
 */
public class Wildcard {
	/**
	 * Check if pattern string matches text string.
	 * @param pattern the pattern to search for
	 * @param text the text to search in
	 * @return true if pattern matches text
	 */
	public static boolean matches(String pattern, String text) {
		// add sentinel so don't need to worry about *'s at end of pattern
		text    += '\0';
		pattern += '\0';

		int N = pattern.length();

		/**
		 * states[j] = true if pattern[0..j] matches text[0..i]
		 */
		boolean[] states = new boolean[N+1];
		/**
		 * old[j]    = true if pattern[0..j] matches text[0..i-1]
		 */
		boolean[] old = new boolean[N+1];
		old[0] = true;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			states = new boolean[N+1];       // initialized to false
			for (int j = 0; j < N; j++) {
				char p = pattern.charAt(j);

				// hack to handle *'s that match 0 characters
				if (old[j] && (p == '*')) old[j+1] = true;

				if (old[j] && (p ==  c )) states[j+1] = true;
				//if (old[j] && (p == '.')) states[j+1] = true;
				if (old[j] && (p == '*')) states[j]   = true;
				if (old[j] && (p == '*')) states[j+1] = true;
			}
			old = states;
		}
		return states[N];
	}
}
