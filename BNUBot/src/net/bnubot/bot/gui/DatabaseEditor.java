/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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
import net.bnubot.util.Out;

import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.query.SelectQuery;

/**
 * @author sanderson
 *
 */
public class DatabaseEditor {
	private final Map<String, CustomDataObject> dataMap = new HashMap<String, CustomDataObject>();
	private CustomDataObject currentRow = null;
	private final Map<ObjAttribute, getValueDelegate> data = new HashMap<ObjAttribute, getValueDelegate>();
	private final JDialog jf = new JDialog();
	private final JPanel jp = new JPanel(new GridBagLayout());
	private final DefaultComboBoxModel model = new DefaultComboBoxModel();
	private final JList jl = new JList(model);
	
	private interface getValueDelegate {
		public Object getValue();
	}
	
	@SuppressWarnings("unchecked")
	public DatabaseEditor(Class<? extends CustomDataObject> clazz) throws Exception {
		List<CustomDataObject>dataRows = DatabaseContext.getContext().performQuery(new SelectQuery(clazz));
		
		jf.setTitle(clazz.getSimpleName() + " Editor");
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(new JScrollPane(jl));
		
		Box box2 = new Box(BoxLayout.Y_AXIS);
		box2.add(jp);
		
		Box box3 = new Box(BoxLayout.X_AXIS);

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					for(ObjAttribute attr : data.keySet()) {
						String key = attr.getName();
						Object value = data.get(attr).getValue();
						currentRow.writeProperty(key, value);
					}
					currentRow.updateRow();
				} catch(Exception ex) {
					Out.popupException(ex);
				}
			}});
		box3.add(btnSave);
		JButton btnRevert = new JButton("Revert");
		btnRevert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadData();
			}});
		box3.add(btnRevert);
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: Confirm
				try {
					currentRow.getObjectContext().deleteObject(currentRow);
					currentRow.updateRow();
				} catch(Exception ex) {
					Out.popupException(ex);
				}
			}});
		box3.add(btnDelete);
		
		box2.add(box3);
		
		box.add(box2);
		
		for(CustomDataObject row : dataRows) {
			final String disp = row.toDisplayString();
			model.addElement(disp);
			dataMap.put(disp, row);
		}
		
		jl.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				loadData();
			}});
		
		jf.add(box);
		jf.pack();
		jf.setVisible(true);
		jf.setModal(true);
		jf.setAlwaysOnTop(true);
	}
	
	private void loadData() {
		jp.removeAll();
		data.clear();
		int y = 0;
		
		currentRow = dataMap.get(jl.getSelectedValue());
		for (ObjAttribute attr : currentRow.getObjEntity().getAttributes()) {
			if(attr.getDbAttribute().isGenerated())
				continue;
			addField(jp, y++, attr, currentRow);
		}
		
		jf.pack();
	}
	
	public void addField(Container jp, int y, ObjAttribute attr, CustomDataObject row) {
		final Class<?> fieldType = attr.getJavaClass();
		final String propName = attr.getName();
		final Object value = row.readProperty(propName);
		final boolean isNullable = !attr.getDbAttribute().isMandatory();
		
		String v = null;
		if(value != null)
			v = value.toString();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = y;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		jp.add(new JLabel(propName), gbc);
		
		final ConfigCheckBox bNull;
		gbc.gridx++;
		if(isNullable) {
			bNull = new ConfigCheckBox("NULL", v == null);
			jp.add(bNull, gbc);
		} else {
			bNull = null;
			jp.add(new JLabel(), gbc);
		}
		
		final Component valueComponent;
		if(fieldType.equals(Boolean.class))
			valueComponent = new ConfigCheckBox(null, ((Boolean)value).booleanValue());
		else
			valueComponent = new ConfigTextArea(v);
		gbc.gridx++;
		jp.add(valueComponent, gbc);
		
		data.put(attr, new getValueDelegate() {
			@SuppressWarnings("deprecation")
			public Object getValue() {
				// If it's nullable, and it's null, return null
				if(isNullable && bNull.isSelected())
					return null;
				
				// If it's a boolean, return a Boolean
				if(fieldType.equals(Boolean.class))
					return new Boolean(((ConfigCheckBox)valueComponent).isSelected());
				
				String value = ((ConfigTextArea)valueComponent).getText();
				if(fieldType.equals(String.class))
					return value;
				if(fieldType.equals(Integer.class) || fieldType.equals(int.class))
					return new Integer(value);
				if(fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
					return new Boolean(value);
				if(fieldType.equals(Date.class))
					return new Date(value);
				
				throw new IllegalStateException("asfd");
			}});
	}
}
