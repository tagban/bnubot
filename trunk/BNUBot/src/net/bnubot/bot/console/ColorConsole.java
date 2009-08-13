/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.console;

import java.io.PrintStream;

import net.bnubot.util.TimeFormatter;

/**
 * @author scotta
 */
public class ColorConsole {
	public enum ColorConstant {
		FG_BLACK(30),
		FG_RED(31),
		FG_GREEN(32),
		FG_YELLOW(33),
		FG_BLUE(34),
		FG_MAGENTA(35),
		FG_CYAN(36),
		FG_WHITE(37),

		BG_BLACK(40),
		BG_RED(41),
		BG_GREEN(42),
		BG_YELLOW(43),
		BG_BLUE(44),
		BG_MAGENTA(45),
		BG_CYAN(46),
		BG_WHITE(47);

		final int code;
		ColorConstant(int code) {
			this.code = code;
		}
	}

	private PrintStream out;

	public ColorConsole(PrintStream out) {
		this.out = out;
	}

	public ColorConsole begin() {
		return color()
		.string("[")
		.string(TimeFormatter.getTimestamp())
		.string("] ");
	}

	public void end() {
		color();
		out.println();
	}

	public ColorConsole color(ColorConstant... color) {
		out.print("\033[");
		if(color.length == 0) {
			// Reset color to normal
			out.print("00");
		} else {
			out.print(color[0].code);
		}

		for(ColorConstant c : color) {
			out.print(';');
			out.print(c.code);
		}
		out.print("m");
		return this;
	}

	public ColorConsole string(String string) {
		out.print(string);
		return this;
	}
}
