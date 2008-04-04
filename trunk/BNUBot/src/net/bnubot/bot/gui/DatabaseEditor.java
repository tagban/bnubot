/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Container;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.bnubot.bot.gui.components.ConfigCheckBox;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.db.CustomDataObject;
import net.bnubot.db.conf.DatabaseContext;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.query.SelectQuery;

/**
 * @author sanderson
 *
 */
public class DatabaseEditor {
	private Map<String, CustomDataObject> data = new HashMap<String, CustomDataObject>();
	private JDialog jf = new JDialog();
	
	@SuppressWarnings("unchecked")
	public DatabaseEditor(Class<? extends CustomDataObject> clazz) throws Exception {
		ObjectContext context = DatabaseContext.getContext();
		List<CustomDataObject>dataRows = context.performQuery(new SelectQuery(clazz));
		
		jf.setTitle(clazz.getSimpleName() + " Editor");
		Box box = new Box(BoxLayout.X_AXIS);
		
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		final JList jl = new JList(model);
		box.add(new JScrollPane(jl));
		final JPanel jp = new JPanel();
		box.add(jp);
		
		for(CustomDataObject row : dataRows) {
			final String disp = row.toDisplayString();
			model.addElement(disp);
			data.put(disp, row);
		}
		
		jl.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				CustomDataObject row = data.get(jl.getSelectedValue());
				Box box1 = new Box(BoxLayout.Y_AXIS);
				Box box2 = new Box(BoxLayout.Y_AXIS);
				Box box3 = new Box(BoxLayout.Y_AXIS);
				for (ObjAttribute attr : row.getObjEntity().getAttributes()) {
					if(attr.getDbAttribute().isGenerated())
						continue;
					addField(box1, box2, box3, attr, row);
				}
				
				jp.removeAll();
				jp.add(box1);
				jp.add(box2);
				jp.add(box3);
				jf.pack();
			}});
		
		jf.add(box);
		jf.pack();
		jf.setVisible(true);
		jf.setModal(true);
		jf.setAlwaysOnTop(true);
	}
	
	public void addField(Container row1, Container row2, Container row3, ObjAttribute attr, CustomDataObject row) {
		//final Class<?> fieldType = attr.getJavaClass();
		final String propName = attr.getName();
		final Object value = row.readProperty(propName);
		final boolean isNullable = !attr.getDbAttribute().isMandatory();
		
		String v = null;
		if(value != null)
			v = value.toString();
		
		row1.add(new JLabel(propName));
		if(isNullable) {
			ConfigCheckBox bNull = new ConfigCheckBox("NULL", v == null);
			row2.add(bNull);
		} else {
			row2.add(new JLabel());
		}
		row3.add(new ConfigTextArea(v));
	}
}
