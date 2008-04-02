/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.List;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._Command;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;

public class Command extends _Command {
	private static final long serialVersionUID = 8794076397315891153L;
	private static final SQLTemplate commandGroups = new SQLTemplate(Command.class,
			"SELECT " +
			"#result('max(id)' 'Integer' 'id'), " +
			"#result('max(name)' 'String' 'name'), " +
			"#result('max(description)' 'String' 'description'), " +
			"#result('max(access)' 'Integer' 'access'), " +
			"#result('cmdgroup' 'String' 'cmdgroup') " +
			"FROM command " +
			"GROUP BY cmdgroup ");
	
	/**
	 * Get a command by name
	 * @param command The name of the command
	 * @return The command, or null
	 */
	public static Command get(String command) {
		Expression expression = ExpressionFactory.lessOrEqualExp(Command.NAME_PROPERTY, command);
		SelectQuery query = new SelectQuery(Command.class, expression);
		return (Command)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
	}

	/**
	 * Get commands available with at least access
	 * @param access Minimum access
	 * @return List of commands
	 */
	@SuppressWarnings("unchecked")
	public static List<Command> getCommands(int access) {
		Expression expression = ExpressionFactory.lessOrEqualExp(Command.ACCESS_PROPERTY, new Integer(access));
		SelectQuery query = new SelectQuery(Command.class, expression);
		return DatabaseContext.getContext().performQuery(query);
	}

	/**
	 * Get commands in category available with at least access
	 * @param category The category to search in
	 * @param access Minimum access
	 * @return List of commands
	 */
	@SuppressWarnings("unchecked")
	public static List<Command> getCommands(String category, int access) {
		Expression expression = ExpressionFactory.lessOrEqualExp(Command.ACCESS_PROPERTY, new Integer(access));
		expression = expression.andExp(ExpressionFactory.matchExp(Command.CMDGROUP_PROPERTY, category));
		SelectQuery query = new SelectQuery(Command.class, expression);
		return DatabaseContext.getContext().performQuery(query);
	}

	/**
	 * Get command groups
	 * @return List of distinct command groups
	 */
	@SuppressWarnings("unchecked")
	public static List<Command> getGroups() {
		return DatabaseContext.getContext().performQuery(commandGroups);
	}
}
