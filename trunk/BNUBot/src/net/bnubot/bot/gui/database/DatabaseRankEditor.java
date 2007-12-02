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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.bnubot.bot.database.Database;
import net.bnubot.bot.database.RankResultSet;
import net.bnubot.bot.gui.components.ConfigFactory;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;

public class DatabaseRankEditor extends JDialog {
	private static final long serialVersionUID = 8358635720495103894L;

	private Database d = null;

	private DefaultListModel lm;
	private JList lstRanks;
	private ConfigTextArea txtID;
	private ConfigTextArea txtShortPrefix;
	private ConfigTextArea txtPrefix;
	private ConfigTextArea txtVerbstr;
	private ConfigTextArea txtGreeting;
	private ConfigTextArea txtExpireDays;
	private ConfigTextArea txtAPDays;
	private ConfigTextArea txtAPWins;
	private ConfigTextArea txtAPD2Level;
	private ConfigTextArea txtAPW3Level;
	private ConfigTextArea txtAPRS;
	private ConfigTextArea txtAPMail;
	private JButton cmdNew;
	private JButton cmdDelete;
	private JButton cmdApply;
	private JButton cmdRevert;
	
	private RankResultSet rsRank = null;
	
	public DatabaseRankEditor(Database d) {
		this.d = d;
		initializeGui();
		setTitle("Rank Editor");
		
		pack();
		setModal(true);
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
				txtID = ConfigFactory.makeText("ID", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtShortPrefix = ConfigFactory.makeText("shortPrefix", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtPrefix = ConfigFactory.makeText("prefix", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtVerbstr = ConfigFactory.makeText("verbstr", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtGreeting = ConfigFactory.makeText("greeting", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtExpireDays = ConfigFactory.makeText("expireDays", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtAPDays = ConfigFactory.makeText("apDays", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtAPRS = ConfigFactory.makeText("apRecruitScore", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtAPWins = ConfigFactory.makeText("apWins", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtAPD2Level = ConfigFactory.makeText("apD2Level", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtAPW3Level = ConfigFactory.makeText("apW3Level", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				txtAPMail = ConfigFactory.makeText("apMail", null, majorRows);
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
								Out.exception(e);
							}
						}
					}
				});

				Box boxLine = new Box(BoxLayout.X_AXIS);
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
								Out.exception(e);
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
								Out.exception(e);
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
									Out.exception(e);
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
									Out.exception(e);
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
			Out.exception(e);
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
			Out.exception(e);
		}
		
		pack();
	}
}
