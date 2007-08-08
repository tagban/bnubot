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

import net.bnubot.bot.database.AccountResultSet;
import net.bnubot.bot.database.Database;
import net.bnubot.util.TimeFormatter;

@SuppressWarnings("serial")
public class DatabaseAccountEditor extends JFrame {
	private Database d = null;
	
	private DefaultListModel lm;
	private JList lstAccounts;
	private JTextArea txtID;
	private JTextArea txtAccess;
	private JTextArea txtName;
	private JTextArea txtCreated;
	private JTextArea txtLastRankChange;
	private JTextArea txtCreatedBy;
	private JTextArea txtTriviaCorrect;
	private JTextArea txtTriviaWin;
	private JTextArea txtBirthday;
	private JButton cmdNew;
	private JButton cmdDelete;
	private JButton cmdApply;
	private JButton cmdRevert;
	
	private AccountResultSet rsAccount = null;
	
	public DatabaseAccountEditor(Database d) {
		this.d = d;
		
		initializeGui();
		pack();
		setTitle("Account Editor");
		setAlwaysOnTop(true);
		setVisible(true);
	}

	private void initializeGui() {
		Box majorColumns = new Box(BoxLayout.X_AXIS);
		{
			Box majorRows = new Box(BoxLayout.Y_AXIS);
			{
				majorRows.add(new JLabel("Accounts:"));
				
				lm = new DefaultListModel();
				rebuildAccounts();
				
				lstAccounts = new JList(lm);
				lstAccounts.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						String s = (String)lstAccounts.getSelectedValue();
						if(s == null)
							return;
						
						if(s.indexOf(' ') != -1)
							s = s.substring(0, s.indexOf(' '));
						
						displayEditor(s);
					}});
				lstAccounts.setMinimumSize(new Dimension(50, 300));
				majorRows.add(lstAccounts);
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
							if(rsAccount != null) {
								String txt = txtID.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value != null)
										rsAccount.setId(value);
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
					boxLine.add(new JLabel("Access"));
					
					txtAccess = new JTextArea();
					txtAccess.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsAccount != null) {
								String txt = txtAccess.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value != null)
										rsAccount.setAccess(value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtAccess);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("Name"));
					
					txtName = new JTextArea();
					txtName.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsAccount != null) {
								String txt = txtName.getText();
								try {
									if((txt == null) || (txt.length() == 0))
										rsAccount.setName(null);
									else
										rsAccount.setName(txt);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtName);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("Created"));
					
					txtCreated = new JTextArea();
					txtCreated.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsAccount != null) {
								String txt = txtCreated.getText();
								Timestamp value = null;
								try {value = new Timestamp(TimeFormatter.parse(txt));} catch(Exception e) {}
								try {
									if(value != null)
										rsAccount.setCreated(value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtCreated);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("Last Rank Change"));
					
					txtLastRankChange = new JTextArea();
					txtLastRankChange.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsAccount != null) {
								String txt = txtLastRankChange.getText();
								Timestamp value = null;
								try {value = new Timestamp(TimeFormatter.parse(txt));} catch(Exception e) {}
								try {
									if(value != null)
										rsAccount.setLastRankChange(value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtLastRankChange);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("Created By"));
					
					txtCreatedBy = new JTextArea();
					txtCreatedBy.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsAccount != null) {
								String txt = txtCreatedBy.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value != null)
										rsAccount.setCreatedBy(value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtCreatedBy);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("Trivia Correct"));
					
					txtTriviaCorrect = new JTextArea();
					txtTriviaCorrect.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsAccount != null) {
								String txt = txtTriviaCorrect.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value != null)
										rsAccount.setTriviaCorrect(value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtTriviaCorrect);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("Trivia Win"));
					
					txtTriviaWin = new JTextArea();
					txtTriviaWin.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsAccount != null) {
								String txt = txtTriviaWin.getText();
								Long value = null;
								try {value = Long.parseLong(txt);} catch(Exception e) {}
								try {
									if(value != null)
										rsAccount.setTriviaWin(value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtTriviaWin);
				}
				majorRows.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(new JLabel("Birthday"));
					
					txtTriviaWin = new JTextArea();
					txtTriviaWin.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent arg0) {}
						public void focusLost(FocusEvent arg0) {
							if(rsAccount != null) {
								String txt = txtBirthday.getText();
								java.sql.Date value = null;
								try {value = new java.sql.Date(TimeFormatter.parse(txt));} catch(Exception e) {}
								try {
									if(value != null)
										rsAccount.setBirthday(value);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					});
					boxLine.add(txtTriviaWin);
				}
				majorRows.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					cmdNew = new JButton("New");
					cmdNew.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if(rsAccount != null) {
								d.close(rsAccount);
								rsAccount = null;
							}
							try {
								long id = d.createAccount();
								rebuildAccounts();
								displayEditor(id);
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
								if(rsAccount != null) {
									rsAccount.deleteRow();
									d.close(rsAccount);
									rsAccount = null;
									rebuildAccounts();
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
							if(rsAccount != null) {
								try {
									rsAccount.updateRow();
									rebuildAccounts();
									displayEditor(rsAccount.getName());
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
							if(rsAccount != null)
								try {
									displayEditor(rsAccount.getName());
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

	private void rebuildAccounts() {
		try {
			lm.clear();
			AccountResultSet rsAccounts = d.getAccounts();
			while(rsAccounts.next()) {
				String title = rsAccounts.getName();
				title += " (";
				title += rsAccounts.getAccess();
				title += ")";
				lm.addElement(title);
			}
			d.close(rsAccounts);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(lstAccounts != null)
			lstAccounts.validate();
	}
	
	private AccountResultSet getAccount(Object identifier) throws SQLException {
		if(identifier instanceof String)
			return d.getAccount((String)identifier);
		if(identifier instanceof Long)
			return d.getAccount((Long)identifier);
		throw new SQLException(identifier.getClass().getName());
	}
	
	private void displayEditor(Object identifier) {
		if(identifier == null) {
			txtID.setText(null);
			txtAccess.setText(null);
			txtName.setText(null);
			txtCreated.setText(null);
			txtLastRankChange.setText(null);
			txtCreatedBy.setText(null);
			txtTriviaCorrect.setText(null);
			txtTriviaWin.setText(null);
			txtBirthday.setText(null);
		} else try {
			if(rsAccount != null) {
				d.close(rsAccount);
				rsAccount = null;
			}
			
			rsAccount = getAccount(identifier);
			if(!rsAccount.next()) {
				displayEditor(null);
				return;
			}
			txtID.setText(rsAccount.getId().toString());
			txtAccess.setText(rsAccount.getAccess().toString());
			txtName.setText(rsAccount.getName());
			txtCreated.setText(rsAccount.getCreated().toString());
			txtLastRankChange.setText(rsAccount.getLastRankChange().toString());
			txtCreatedBy.setText(rsAccount.getCreatedBy().toString());
			txtTriviaCorrect.setText(rsAccount.getTriviaCorrect().toString());
			txtTriviaWin.setText(rsAccount.getTriviaWin().toString());
			txtBirthday.setText(rsAccount.getBirthday().toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		pack();
	}
}
