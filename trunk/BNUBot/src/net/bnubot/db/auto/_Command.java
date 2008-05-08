/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db.auto;

import java.util.List;

import net.bnubot.db.CommandAlias;
import net.bnubot.db.CustomDataObject;
import net.bnubot.db.Rank;

/**
 * Class _Command was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Command extends CustomDataObject {

    public static final String CMDGROUP_PROPERTY = "cmdgroup";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String NAME_PROPERTY = "name";
    public static final String ALIASES_PROPERTY = "aliases";
    public static final String RANK_PROPERTY = "rank";

    public static final String ID_PK_COLUMN = "id";

    public void setCmdgroup(String cmdgroup) {
        writeProperty("cmdgroup", cmdgroup);
    }
    public String getCmdgroup() {
        return (String)readProperty("cmdgroup");
    }

    public void setDescription(String description) {
        writeProperty("description", description);
    }
    public String getDescription() {
        return (String)readProperty("description");
    }

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void addToAliases(CommandAlias obj) {
        addToManyTarget("aliases", obj, true);
    }
    public void removeFromAliases(CommandAlias obj) {
        removeToManyTarget("aliases", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<CommandAlias> getAliases() {
        return (List<CommandAlias>)readProperty("aliases");
    }


    public void setRank(Rank rank) {
        setToOneTarget("rank", rank, true);
    }

    public Rank getRank() {
        return (Rank)readProperty("rank");
    }


}