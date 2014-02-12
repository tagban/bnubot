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
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.bnubot.bot.gui.components.ConfigFlagChecks;
import net.bnubot.bot.gui.components.ConfigTextField;
import net.bnubot.db.CustomDataObject;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.logging.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.util.UnloggedException;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.SelectQuery;

/**
 * @author scotta
 * @param <T> The {@link CustomDataObject} type
 * @param <S> The sortable field type of the {@link CustomDataObject}
 */
public class DatabaseEditor<T extends CustomDataObject<S>, S extends Comparable<? super S>> {
	private final ObjectContext context;
	private final String editorType;

	private final Map<String, T> dataMap = new HashMap<String, T>();
	private T currentRow = null;
	private final Map<ObjAttribute, getValueDelegate> data = new HashMap<ObjAttribute, getValueDelegate>();
	private final Map<ObjRelationship, getValueDelegate> dataRel = new HashMap<ObjRelationship, getValueDelegate>();
	private final JDialog jf = new JDialog();
	private final JPanel jp = new JPanel(new GridBagLayout());
	private final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
	private final JList<String> jl = new JList<String>(model);

	private interface getValueDelegate {
		public Object getValue();
	}

	@SuppressWarnings("unchecked")
	public DatabaseEditor(Class<T> clazz) throws Exception {
		context = DatabaseContext.getContext();
		if(context == null)
			throw new UnloggedException("No database is initialized");
		List<T> dataRows = context.performQuery(new SelectQuery(clazz));
		Collections.sort(dataRows);

		editorType = clazz.getSimpleName();
		jf.setTitle(editorType + " Editor");
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(new JScrollPane(jl));

		Box box2 = new Box(BoxLayout.Y_AXIS);
		box2.add(jp);

		Box box3 = new Box(BoxLayout.X_AXIS);

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveData();
				loadData();
			}});
		box3.add(btnSave);
		JButton btnRevert = new JButton("Revert");
		btnRevert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadData();
			}});
		box3.add(btnRevert);
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int option = JOptionPane.showConfirmDialog(
						jf,
						"Are you sure you want to delete this row?",
						"Delete row?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if(option != JOptionPane.YES_OPTION)
					return;

				try {
					String disp = getDisplayString(currentRow);
					dataMap.remove(disp);

					context.deleteObject(currentRow);
					currentRow.updateRow();

					currentRow = null;
					model.removeElement(disp);
					jp.removeAll();
					jp.repaint();
					data.clear();
					dataRel.clear();
				} catch(Exception ex) {
					Out.popupException(ex, jf);
				}
			}});
		box3.add(btnDelete);

		box2.add(box3);

		box.add(box2);

		for(T row : dataRows) {
			String disp = getDisplayString(row);
			model.addElement(disp);
			dataMap.put(disp, row);
		}

		jl.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(changesMade()) {
					int option = JOptionPane.showConfirmDialog(
							jf,
							"You have made changes to the " + editorType + " " + currentRow.toString() + ". Do you want to save them?",
							"Save changes?",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if(option == JOptionPane.YES_OPTION)
						saveData();
				}
				context.rollbackChanges();
				loadData();
			}});

		jf.add(box);
		jf.pack();
		jf.setVisible(true);
		jf.setModal(true);
	}

	private String getDisplayString(T row) {
		if(row == null)
			return "NULL";
		return row.toString();
	}

	private void loadData() {
		jp.removeAll();
		data.clear();
		dataRel.clear();
		int y = 0;

		currentRow = dataMap.get(jl.getSelectedValue());
		if(currentRow == null)
			return;

		ObjEntity objEntity = currentRow.getObjEntity();
		for (ObjAttribute attr : objEntity.getAttributes()) {
			DbAttribute dbAttribute = attr.getDbAttribute();
			if(dbAttribute.isGenerated() || dbAttribute.isForeignKey())
				continue;
			addField(jp, y++, attr, currentRow);
		}
		for (ObjRelationship rel : objEntity.getRelationships()) {
			if(rel.isToMany())
				continue;
			addField(jp, y++, rel, currentRow);
		}

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = y++;
		gbc.weighty = 1;
		jp.add(Box.createVerticalGlue(), gbc);

		jf.pack();
	}

	@SuppressWarnings("unchecked")
	public void addField(Container jp, int y, ObjRelationship rel, CustomDataObject<?> row) {
		final Class<?> fieldType = ((ObjEntity)rel.getTargetEntity()).getJavaClass();
		final String propName = rel.getName();

		boolean isNullable = true;
		for(DbRelationship dbr : rel.getDbRelationships())
			for(DbAttribute dba : dbr.getSourceAttributes()) {
				if(dba.isMandatory()) {
					isNullable = false;
					break;
				}
			}

		final T value = (T)row.readProperty(propName);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = y;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		jp.add(new JLabel(propName), gbc);

		final JCheckBox bNull;
		gbc.gridx++;
		if(isNullable) {
			bNull = new JCheckBox("NULL", value == null);
			jp.add(bNull, gbc);
		} else {
			bNull = null;
			jp.add(new JLabel(), gbc);
		}

		final HashMap<String, T> theseOptions = new HashMap<String, T>();
		List<T> relTargets = context.performQuery(new SelectQuery(fieldType));
		Collections.sort(relTargets);
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
		for(T v : relTargets) {
			String s = getDisplayString(v);
			model.addElement(s);
			theseOptions.put(s, v);
		}
		final JComboBox<String> valueComponent = new JComboBox<String>(model);
		valueComponent.setSelectedItem((value == null) ? null : getDisplayString(value));

		gbc.gridx++;
		gbc.weightx = 1;
		jp.add(valueComponent, gbc);

		dataRel.put(rel, new getValueDelegate() {
			@Override
			public Object getValue() {
				if((bNull != null) && bNull.isSelected())
					return null;
				String key = (String)valueComponent.getSelectedItem();
				return theseOptions.get(key);
			}});
	}

	public void addField(Container jp, int y, ObjAttribute attr, CustomDataObject<?> row) {
		final Class<?> fieldType = attr.getJavaClass();
		final String propName = attr.getName();
		final Object value = row.readProperty(propName);
		final boolean isNullable = !attr.getDbAttribute().isMandatory();

		final String v =
			(value == null) ? null :
			(value instanceof Date) ? TimeFormatter.formatDateTime((Date)value) :
			value.toString();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = y;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		jp.add(new JLabel(propName), gbc);

		final JCheckBox bNull;
		gbc.gridx++;
		if(isNullable) {
			bNull = new JCheckBox("NULL", value == null);
			jp.add(bNull, gbc);
		} else {
			bNull = null;
			jp.add(new JLabel(), gbc);
		}

		final Component valueComponent;
		if(fieldType.equals(Boolean.class)) {
			valueComponent = new JCheckBox((String)null, ((Boolean)value).booleanValue());
		} else if(fieldType.equals(int.class) && attr.getName().equals("flagSpoof")) {
			int flags = 0;
			if(value != null)
				flags = ((Integer)value).intValue();
			valueComponent = new ConfigFlagChecks(flags);
		} else {
			valueComponent = new ConfigTextField(v);
		}
		gbc.gridx++;
		jp.add(valueComponent, gbc);

		data.put(attr, new getValueDelegate() {
			@Override
			public Object getValue() {
				// If it's nullable, and it's null, return null
				if(isNullable && bNull.isSelected())
					return null;

				// If it's a boolean, return a Boolean
				if(valueComponent instanceof JCheckBox)
					return new Boolean(((JCheckBox)valueComponent).isSelected());

				// If it's a flag set, return an Integer
				if(valueComponent instanceof ConfigFlagChecks)
					return ((ConfigFlagChecks)valueComponent).getFlags();

				String value = ((ConfigTextField)valueComponent).getText();
				if(fieldType.equals(String.class))
					return value;
				if(fieldType.equals(Integer.class) || fieldType.equals(int.class))
					return Integer.valueOf(value);
				if(fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
					return new Boolean(value);
				if(fieldType.equals(Date.class))
					try {
						return new Date(TimeFormatter.parseDateTime(value));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}

				throw new IllegalStateException("Unknown fieldType " + fieldType.getSimpleName());
			}});
	}

	private void saveData() {
		try {
			for(ObjAttribute attr : data.keySet()) {
				String key = attr.getName();
				Object value = data.get(attr).getValue();
				currentRow.writeProperty(key, value);
			}
			for(ObjRelationship rel : dataRel.keySet()) {
				String key = rel.getName();
				Object value = dataRel.get(rel).getValue();
				currentRow.writeProperty(key, value);
			}
			currentRow.updateRow();
		} catch(Exception ex) {
			Out.popupException(ex, jf);
		}
	}

	private boolean changesMade() {
		if(currentRow == null)
			return false;

		ObjEntity objEntity = currentRow.getObjEntity();
		for (ObjAttribute attr : objEntity.getAttributes()) {
			DbAttribute dbAttribute = attr.getDbAttribute();
			if(dbAttribute.isGenerated() || dbAttribute.isForeignKey())
				continue;

			final Object value_frm = data.get(attr).getValue();
			final Object value_db = currentRow.readProperty(attr.getName());

			if(!equal(value_frm, value_db))
				return true;
		}
		for (ObjRelationship rel : objEntity.getRelationships()) {
			if(rel.isToMany())
				continue;

			final Object value_frm = dataRel.get(rel).getValue();
			final Object value_db = currentRow.readProperty(rel.getName());

			if(!equal(value_frm, value_db))
				return true;
		}

		return false;
	}

	private boolean equal(Object value_frm, Object value_db) {
		if(value_frm == value_db)
			return true;
		if(value_frm == null)
			return false;
		if((value_frm instanceof Date)
		&& (value_db instanceof Date)) {
			Date d1 = (Date)value_frm;
			Date d2 = (Date)value_db;
			// Don't compare miliseconds
			return (d1.getTime()/1000) == (d2.getTime()/1000);
		}

		return value_frm.equals(value_db);
	}
}
