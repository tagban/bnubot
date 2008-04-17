/**
 * This file is distributed under the GPL
 * $Id: CommandAlias.java 1262 2008-04-03 16:52:02Z scotta $
 */

package net.bnubot.db;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

import net.bnubot.db.auto._CommandAlias;
import net.bnubot.db.conf.DatabaseContext;

public class CommandAlias extends _CommandAlias {
	private static final long serialVersionUID = -3633743075279729925L;

	/**
	 * Get a CommandAlias by name
	 * @param command The name of the command alias
	 * @return The CommandAlias, or NULL if the command alias is not in the database
	 */
	public static CommandAlias get(String command) {
		Expression expression = ExpressionFactory.matchExp(CommandAlias.ALIAS_PROPERTY, command);
		SelectQuery query = new SelectQuery(CommandAlias.class, expression);
		return (CommandAlias)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
	}

	@Override
	public String toDisplayString() {
		return getAlias();
	}
}
