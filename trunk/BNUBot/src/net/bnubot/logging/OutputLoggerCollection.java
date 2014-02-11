/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An {@link OutputLogger} that dispatches messages to
 * a collection of {@link OutputLogger}s
 * @author scotta
 */
class OutputLoggerCollection implements OutputLogger {

	private final OutputLogger defaultLogger;
	private final Collection<OutputLogger> loggers = new ArrayList<OutputLogger>();

	public OutputLoggerCollection(PrintStream defaultOutput) {
		this(new PrintStreamOutputLogger(defaultOutput));
	}

	public OutputLoggerCollection(OutputLogger defaultLogger) {
		this.defaultLogger = defaultLogger;
	}

	public void addLogger(OutputLogger logger) {
		loggers.add(logger);
	}

	@Override
	public void exception(Throwable e) {
		if(loggers.size() == 0) {
			defaultLogger.exception(e);
		} else {
			for(OutputLogger logger : loggers)
				logger.exception(e);
		}
	}

	@Override
	public void error(Class<?> source, String text) {
		if(loggers.size() == 0) {
			defaultLogger.error(source, text);
		} else {
			for(OutputLogger logger : loggers)
				logger.error(source, text);
		}
	}

	@Override
	public void info(Class<?> source, String text) {
		if(loggers.size() == 0) {
			defaultLogger.info(source, text);
		} else {
			for(OutputLogger logger : loggers)
				logger.info(source, text);
		}
	}

	@Override
	public void debug(Class<?> source, String text) {
		if(loggers.size() == 0) {
			defaultLogger.debug(source, text);
		} else {
			for(OutputLogger logger : loggers)
				logger.debug(source, text);
		}
	}

}
