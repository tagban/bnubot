package net.bnubot.db.auto;

import org.apache.cayenne.CayenneDataObject;

import net.bnubot.db.Command;

/**
 * Class _CommandAlias was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _CommandAlias extends CayenneDataObject {

    public static final String ALIAS_PROPERTY = "alias";
    public static final String NAME_PROPERTY = "name";
    public static final String TO_COMMAND_PROPERTY = "toCommand";

    public static final String ALIAS_PK_COLUMN = "alias";

    public void setAlias(String alias) {
        writeProperty("alias", alias);
    }
    public String getAlias() {
        return (String)readProperty("alias");
    }

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void setToCommand(Command toCommand) {
        setToOneTarget("toCommand", toCommand, true);
    }

    public Command getToCommand() {
        return (Command)readProperty("toCommand");
    }


}
