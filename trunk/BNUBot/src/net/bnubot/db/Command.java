/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.ArrayList;
import java.util.List;

import net.bnubot.db.auto._Command;
import net.bnubot.db.conf.DatabaseContext;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;

/**
 * @author scotta
 */
public class Command extends _Command {
	private static final long serialVersionUID = 8794076397315891153L;
	/**
	 * Get a Command by name
	 * @param command The name of the Command
	 * @return The Command, or NULL if the command is not in the database
	 */
	public static Command get(String command) {
		try {
			// Check for aliases
			CommandAlias ca = CommandAlias.get(command);
			if(ca != null)
				return ca.getToCommand();

			// No aliases found; check for the real command
			Expression expression = ExpressionFactory.matchExp(Command.NAME_PROPERTY, command);
			SelectQuery query = new SelectQuery(Command.class, expression);
			return (Command)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * Get commands available with at least access
	 * @param access Minimum access
	 * @return List of commands
	 */
	@SuppressWarnings("unchecked")
	public static List<Command> getCommands(int access) {
		SelectQuery query = new SelectQuery(Command.class);
		List<Command> commands = DatabaseContext.getContext().performQuery(query);
		List<Command> ret = new ArrayList<Command>(commands.size());
		for(Command c : commands)
			if(c.getAccess() <= access)
				ret.add(c);
		return ret;
	}

	/**
	 * Get commands in category available with at least access
	 * @param category The category to search in
	 * @param access Minimum access
	 * @return List of commands
	 */
	@SuppressWarnings("unchecked")
	public static List<Command> getCommands(String category, int access) {
		Expression expression = ExpressionFactory.likeIgnoreCaseExp(Command.CMDGROUP_PROPERTY, category);
		SelectQuery query = new SelectQuery(Command.class, expression);
		List<Command> commands = DatabaseContext.getContext().performQuery(query);
		List<Command> ret = new ArrayList<Command>(commands.size());
		for(Command c : commands)
			if(c.getAccess() <= access)
				ret.add(c);
		return ret;
	}

	/**
	 * Get command groups
	 * @param access the minimum access to look for
	 * @return List of distinct command groups
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getGroups(int access) {
		List<Command> cmds = DatabaseContext.getContext().performQuery(new SQLTemplate(Command.class,
				"SELECT " +
				"#result('max(name)' 'String' 'name'), " +
				"#result('max(description)' 'String' 'description'), " +
				"#result('min(access)' 'Integer' 'access'), " +
				"#result('cmdgroup' 'String' 'cmdgroup') " +
				"FROM command " +
				"WHERE access <= " + access + " " +
				"GROUP BY cmdgroup "));
		List<String> groups = new ArrayList<String>(cmds.size());
		for(Command cmd : cmds)
			groups.add(cmd.getCmdgroup());
		return groups;
	}

	@Override
	public String toSortField() {
		return getName();
	}

	public int getAccess() {
		return getRank().getAccess();
	}
}
