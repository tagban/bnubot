/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Component;
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

import net.bnubot.db.BNLogin;
import net.bnubot.db.CustomDataObject;
import net.bnubot.db.conf.DatabaseContext;

import org.apache.cayenne.DataObjectUtils;
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
	
	public static void main(String[] args) throws Exception {
		new DatabaseEditor(BNLogin.class);
	}
	
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
			final String disp = new StringBuilder("[")
				.append(DataObjectUtils.intPKForObject(row))
				.append("] ")
				.append(row.toDisplayString())
				.toString();
			model.addElement(disp);
			data.put(disp, row);
		}
		
		jl.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				CustomDataObject row = data.get(jl.getSelectedValue());
				Box box = new Box(BoxLayout.Y_AXIS);
				for (ObjAttribute attr : row.getObjEntity().getAttributes())
					box.add(addField(attr.getJavaClass(), attr.getName(), row.readProperty(attr.getName())));
				
				jp.removeAll();
				jp.add(box);
				jf.pack();
			}});
		
		jf.add(box);
		jf.pack();
		jf.setVisible(true);
		jf.setModal(true);
		jf.setAlwaysOnTop(true);
	}
	
	public Component addField(Class<?> fieldType, String propName, Object value) {
		String v = null;
		if(value != null)
			v = value.toString();
		return new JLabel(propName + "=" + v + ",");
	}
}
