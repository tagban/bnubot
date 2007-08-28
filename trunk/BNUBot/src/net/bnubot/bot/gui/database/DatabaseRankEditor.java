/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui.database;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.bnubot.bot.database.Database;
import net.bnubot.bot.database.RankResultSet;
import net.bnubot.util.TimeFormatter;

public class DatabaseRankEditor extends JFrame {
	private static final long serialVersionUID = 8358635720495103894L;

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
	private JTextArea txtAPRS;
	private JTextArea txtAPMail;
	private JButton cmdNew;
	private JButton cmdDelete;
	private JButton cmdApply;
	private JButton cmdRevert;
	
	private RankResultSet rsRank = null;
	
	public DatabaseRankEditor(Database d) {
		this.d = d;
		
		initializeGui();
		pack();
		setTitle("Rank Editor");
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
						String s = (String)lstRanks.getSelectedValue();
						if(s == null)
							return;
						
						if(s.indexOf(' ') != -1)
							s = s.substring(0, s.indexOf(' '));
						
						displayEditor(Long.parseLong(s));
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
										rsRank.setId(value);
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
										rsRank.setShortPrefix(null);
									else
										rsRank.setShortPrefix(txt);
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
										rsRank.setPrefix(null);
									else
										rsRank.setPrefix(txt);
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
										rsRank.setVerbStr(null);
									else
										rsRank.setVerbStr(txt);
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
										rsRank.setGreeting(null);
									else
										rsRank.setGreeting(txt);
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
										rsRank.setExpireDays(null);
									else
										rsRank.setExpireDays(value);
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
										rsRank.setApDays(null);
									else
										rsRank.setApDays(value);
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
					boxLine.add(new JLabel("apRecruitScore"));
					
					txtAPRS = new JTextArea();
					txtAPRS.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtAPRS.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value == null)
										rsRank.setApRecruitScore(null);
									else
										rsRank.setApRecruitScore(value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtAPRS);
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
										rsRank.setApWins(null);
									else
										rsRank.setApWins(value);
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
										rsRank.setApD2Level(null);
									else
										rsRank.setApD2Level(value);
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
										rsRank.setApW3Level(null);
									else
										rsRank.setApW3Level(value);
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
					boxLine.add(new JLabel("apMail"));
					
					txtAPMail = new JTextArea();
					txtAPMail.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsRank != null) {
								String txt = txtAPMail.getText();
								try {
									if((txt == null) || (txt.length() == 0))
										rsRank.setApMail(null);
									else
										rsRank.setApMail(txt);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtAPMail);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					cmdNew = new JButton("New");
					cmdNew.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							try {
								if(rsRank != null) {
									d.close(rsRank);
									rsRank = null;
								}
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
									displayEditor(rsRank.getId());
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
									displayEditor(rsRank.getId());
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
			RankResultSet rsRanks = d.getRanks();
			while(rsRanks.next()) {
				String title = rsRanks.getPrefix();
				if(title != null)
					title = rsRanks.getId() + " (" + title + ")";
				else
					title = Long.toString(rsRanks.getId());
				
				lm.addElement(title);
			}
			d.close(rsRanks);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(lstRanks != null)
			lstRanks.validate();
	}
	
	private String valueOf(Object obj) {
		if(obj == null)
			return null;
		if(obj instanceof Timestamp)
			return TimeFormatter.formatDateTime((Timestamp)obj);
		if(obj instanceof java.sql.Date)
			return TimeFormatter.formatDate((java.sql.Date)obj);
		return obj.toString();
	}
	
	private void displayEditor(Long id) {
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
			txtAPRS.setText(null);
			txtAPMail.setText(null);
		} else try {
			if(rsRank != null) {
				d.close(rsRank);
				rsRank = null;
			}
			
			rsRank = d.getRank(id);
			if(!rsRank.next()) {
				displayEditor(null);
				return;
			}
			txtID.setText(valueOf(rsRank.getId()));
			txtShortPrefix.setText(valueOf(rsRank.getShortPrefix()));
			txtPrefix.setText(valueOf(rsRank.getPrefix()));
			txtVerbstr.setText(valueOf(rsRank.getVerbStr()));
			txtGreeting.setText(valueOf(rsRank.getGreeting()));
			txtExpireDays.setText(valueOf(rsRank.getExpireDays()));
			txtAPDays.setText(valueOf(rsRank.getApDays()));
			txtAPWins.setText(valueOf(rsRank.getApWins()));
			txtAPD2Level.setText(valueOf(rsRank.getApD2Level()));
			txtAPW3Level.setText(valueOf(rsRank.getApW3Level()));
			txtAPRS.setText(valueOf(rsRank.getApRecruitScore()));
			txtAPMail.setText(valueOf(rsRank.getApMail()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		pack();
	}
}
