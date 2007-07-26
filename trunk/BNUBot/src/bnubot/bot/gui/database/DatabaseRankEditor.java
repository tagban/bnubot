/**
 * This file is distributed under the GPL 
 * $Id: $
 */

package bnubot.bot.gui.database;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bnubot.bot.database.Database;

@SuppressWarnings("serial")
public class DatabaseRankEditor extends JFrame {
	private Database d = null;

	private DefaultListModel lm;
	private JList lstRanks;
	private JTextArea txtID;
	private JTextArea txtShortPrefix;
	private JTextArea txtPrefix;
	private JTextArea txtVerbstr;
	private JTextArea txtGreeting;
	private JTextArea txtExpireDays;
	private JTextArea txtAPDays;
	private JTextArea txtAPWins;
	private JTextArea txtAPD2Level;
	private JTextArea txtAPW3Level;
	private JButton cmdNew;
	private JButton cmdDelete;
	private JButton cmdApply;
	private JButton cmdRevert;
	
	private ResultSet rsRank = null;
	
	public DatabaseRankEditor(Database d) {
		this.d = d;
		
		initializeGui();
		pack();
		setTitle("Database Rank Editor");
		setAlwaysOnTop(true);
		setVisible(true);
	}
	
	private void initializeGui() {
		Box majorColumns = new Box(BoxLayout.X_AXIS);
		{
			Box majorRows = new Box(BoxLayout.Y_AXIS);
			{
				majorRows.add(new JLabel("Ranks:"));
				
				lm = new DefaultListModel();
				rebuildRanks();
				
				lstRanks = new JList(lm);
				lstRanks.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						displayEditor((Long)lstRanks.getSelectedValue());
					}});
				lstRanks.setMinimumSize(new Dimension(50, 300));
				majorRows.add(lstRanks);
			}
			majorColumns.add(majorRows);
			
			majorRows = new Box(BoxLayout.Y_AXIS);
			{
				Box boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("ID"));
					
					txtID = new JTextArea();
					txtID.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtID.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value != null)
										rsRank.updateLong("id", value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtID);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("shortPrefix"));
					
					txtShortPrefix = new JTextArea();
					txtShortPrefix.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtShortPrefix.getText();
								try {
									if((txt == null) || (txt.length() == 0))
										rsRank.updateNull("shortPrefix");
									else
										rsRank.updateString("shortPrefix", txt);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtShortPrefix);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("prefix"));
					
					txtPrefix = new JTextArea();
					txtPrefix.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtPrefix.getText();
								try {
									if((txt == null) || (txt.length() == 0))
										rsRank.updateNull("prefix");
									else
										rsRank.updateString("prefix", txt);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtPrefix);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("verbstr"));
					
					txtVerbstr = new JTextArea();
					txtVerbstr.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtVerbstr.getText();
								try {
									if((txt == null) || (txt.length() == 0))
										rsRank.updateNull("verbstr");
									else
										rsRank.updateString("verbstr", txt);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtVerbstr);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("greeting"));
					
					txtGreeting = new JTextArea();
					txtGreeting.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtGreeting.getText();
								try {
									if((txt == null) || (txt.length() == 0))
										rsRank.updateNull("greeting");
									else
										rsRank.updateString("greeting", txt);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtGreeting);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("expireDays"));
					
					txtExpireDays = new JTextArea();
					txtExpireDays.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtExpireDays.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value == null)
										rsRank.updateNull("expireDays");
									else
										rsRank.updateLong("expireDays", value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtExpireDays);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("apDays"));
					
					txtAPDays = new JTextArea();
					txtAPDays.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtAPDays.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value == null)
										rsRank.updateNull("apDays");
									else
										rsRank.updateLong("apDays", value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtAPDays);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("apWins"));
					
					txtAPWins = new JTextArea();
					txtAPWins.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtAPWins.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value == null)
										rsRank.updateNull("apWins");
									else
										rsRank.updateLong("apWins", value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtAPWins);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("apD2Level"));
					
					txtAPD2Level = new JTextArea();
					txtAPD2Level.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtAPD2Level.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value == null)
										rsRank.updateNull("apD2Level");
									else
										rsRank.updateLong("apD2Level", value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtAPD2Level);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("apW3Level"));
					
					txtAPW3Level = new JTextArea();
					txtAPW3Level.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtAPW3Level.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value == null)
										rsRank.updateNull("apW3Level");
									else
										rsRank.updateLong("apW3Level", value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtAPW3Level);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					cmdNew = new JButton("New");
					cmdNew.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if(rsRank != null) {
								d.close(rsRank);
								rsRank = null;
							}
							try {
								long rankid = d.createRank();
								rebuildRanks();
								displayEditor(rankid);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					});
					boxLine.add(cmdNew);
					
					cmdDelete = new JButton("Delete");
					cmdDelete.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							try {
								if(rsRank != null) {
									rsRank.deleteRow();
									d.close(rsRank);
									rsRank = null;
									rebuildRanks();
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					});
					boxLine.add(cmdDelete);
					
					cmdApply = new JButton("Apply");
					cmdApply.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if(rsRank != null) {
								try {
									rsRank.updateRow();
									rebuildRanks();
									displayEditor(rsRank.getLong("id"));
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(cmdApply);
					
					cmdRevert = new JButton("Revert");
					cmdRevert.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if(rsRank != null)
								try {
									displayEditor(rsRank.getLong("id"));
								} catch (SQLException e) {
									e.printStackTrace();
								}
						}
					});
					boxLine.add(cmdRevert);
				}
				majorRows.add(boxLine);
			}
			majorColumns.add(majorRows);
		}
		add(majorColumns);
	}

	private void rebuildRanks() {
		try {
			lm.clear();
			ResultSet rsRanks = d.getRanks();
			while(rsRanks.next())
				lm.addElement(rsRanks.getLong("id"));
			d.close(rsRanks);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(lstRanks != null)
			lstRanks.validate();
	}
	
	private void displayEditor(Long id) {
		/* `id` INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
		 * `shortPrefix` VARCHAR(32),
		 * `prefix` VARCHAR(32),
		 * `verbstr` VARCHAR(64) NOT NULL,
		 * `greeting` VARCHAR(255),
		 * `expireDays` TINYINT NOT NULL DEFAULT '90',
		 * `apDays` INTEGER DEFAULT NULL,
		 * `apWins` INTEGER DEFAULT NULL,
		 * `apD2Level` INTEGER DEFAULT NULL,
		 * `apW3Level` INTEGER DEFAULT NULL
		 */
		if(id == null) {
			txtID.setText(null);
			txtShortPrefix.setText(null);
			txtPrefix.setText(null);
			txtVerbstr.setText(null);
			txtGreeting.setText(null);
			txtExpireDays.setText(null);
			txtAPDays.setText(null);
			txtAPWins.setText(null);
			txtAPD2Level.setText(null);
			txtAPW3Level.setText(null);
		} else try {
			if(rsRank != null) {
				d.close(rsRank);
				rsRank = null;
			}
			
			rsRank = d.getRank(id);
			rsRank.next();
			txtID.setText(rsRank.getString("id"));
			txtShortPrefix.setText(rsRank.getString("shortPrefix"));
			txtPrefix.setText(rsRank.getString("prefix"));
			txtVerbstr.setText(rsRank.getString("verbstr"));
			txtGreeting.setText(rsRank.getString("greeting"));
			txtExpireDays.setText(rsRank.getString("expireDays"));
			txtAPDays.setText(rsRank.getString("apDays"));
			txtAPWins.setText(rsRank.getString("apWins"));
			txtAPD2Level.setText(rsRank.getString("apD2Level"));
			txtAPW3Level.setText(rsRank.getString("apW3Level"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		pack();
	}
}
