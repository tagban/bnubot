/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Component;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.bnubot.db.Account;
import net.bnubot.db.CustomDataObject;
import net.bnubot.db.conf.DatabaseContext;

import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.query.SelectQuery;

/**
 * @author sanderson
 *
 */
public class DatabaseEditor {
	private JDialog jf = new JDialog();
	
	public static void main(String[] args) throws Exception {
		new DatabaseEditor(Account.class, "name");
	}
	
	@SuppressWarnings("unchecked")
	public DatabaseEditor(Class<? extends CustomDataObject> clazz, String displayField) throws Exception {
		List<CustomDataObject> dataRows = DatabaseContext.getContext().performQuery(new SelectQuery(clazz));
		
		jf.setTitle(clazz.getSimpleName() + " Editor");
		
		Box box = new Box(BoxLayout.Y_AXIS);
		for(CustomDataObject row : dataRows) {
			Box box2 = new Box(BoxLayout.X_AXIS);
			for (ObjAttribute attr : row.getObjEntity().getAttributes())
				box2.add(addField(attr.getJavaClass(), attr.getName(), row.readProperty(attr.getName())));
			box.add(box2);
		}
		jf.add(box);
		

		jf.pack();
		jf.setVisible(true);
		jf.setModal(true);
	}
	
	public Component addField(Class<?> fieldType, String propName, Object value) {
		String v = null;
		if(value != null) {
			v = value.toString();
			if(v.length() > 10)
				v = v.substring(0, 9) + "...";
		}
		return new JLabel(propName + "=" + v + ",");
	}
}
