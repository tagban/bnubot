package bnubot.bot.gui.database;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.*;

import javax.swing.*;

import bnubot.bot.database.Database;

@SuppressWarnings("serial")
public class DatabaseRankEditor extends JFrame {
	private Database d = null;
	
	private JList lstRanks;
	private JTextArea txtShortPrefix;
	private JTextArea txtPrefix;
	private JTextArea txtVerbstr;
	private JTextArea txtGreeting;
	private JTextArea txtAPDays;
	private JTextArea txtAPWins;
	private JTextArea txtAPD2Level;
	private JTextArea txtAPW3Level;
	private JButton cmdApply;
	private JButton cmdRevert;
	
	private ResultSet rsRank = null;
	
	public DatabaseRankEditor(Database d) {
		this.d = d;
		
		initializeGui();
		setVisible(true);
	}
	
	private void initializeGui() {
		Box b = new Box(BoxLayout.X_AXIS);
		{
			Box b2 = new Box(BoxLayout.Y_AXIS);
			{
				b2.add(new JLabel("Ranks:"));
				
				DefaultListModel lm = new DefaultListModel();
				
				try {
					ResultSet rsRanks = d.getRanks();
					while(rsRanks.next()) {
						lm.addElement(rsRanks.getLong("id"));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				lstRanks = new JList(lm);
				lstRanks.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent arg0) {
						displayEditor((Long)lstRanks.getSelectedValue());
					}
					public void mouseEntered(MouseEvent arg0) {}
					public void mouseExited(MouseEvent arg0) {}
					public void mousePressed(MouseEvent arg0) {}
					public void mouseReleased(MouseEvent arg0) {}
					
				});
				b2.add(lstRanks);
			}
			b.add(b2);
			
			b2 = new Box(BoxLayout.Y_AXIS);
			{
				b2.add(new JLabel("shortPrefix"));
				b2.add(new JLabel("prefix"));
				b2.add(new JLabel("verbstr"));
				b2.add(new JLabel("greeting"));
				b2.add(new JLabel("apDays"));
				b2.add(new JLabel("apWins"));
				b2.add(new JLabel("apWins"));
				b2.add(new JLabel("apD2Level"));
				b2.add(new JLabel("apW3Level"));
			}
			b.add(b2);
			
			b2 = new Box(BoxLayout.Y_AXIS);
			{
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
									rsRank.updateString("shortPrefix", txtShortPrefix.getText());
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
					
				});
				b2.add(txtShortPrefix);
				
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
									rsRank.updateString("prefix", txtPrefix.getText());
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
					
				});
				b2.add(txtPrefix);
				
				txtVerbstr = new JTextArea();
				b2.add(txtVerbstr);
				
				txtGreeting = new JTextArea();
				b2.add(txtGreeting);
				
				txtAPDays = new JTextArea();
				b2.add(txtAPDays);
				
				txtAPWins = new JTextArea();
				b2.add(txtAPWins);
				
				txtAPD2Level = new JTextArea();
				b2.add(txtAPD2Level);
				
				txtAPW3Level = new JTextArea();
				b2.add(txtAPW3Level);
				
				Box b3 = new Box(BoxLayout.X_AXIS);
				{
					cmdApply = new JButton("Apply");
					b3.add(cmdApply);
					
					cmdRevert = new JButton("Revert");
					b3.add(cmdRevert);
				}
				b2.add(b3);
			}
			b.add(b2);
		}
		add(b);
	}

	private void displayEditor(long id) {
		/* `id` INTEGER PRIMARY KEY NOT NULL,
		 * `shortPrefix` VARCHAR(32),
		 * `prefix` VARCHAR(32),
		 * `verbstr` VARCHAR(64) NOT NULL,
		 * `greeting` VARCHAR(255),
		 * `apDays` INTEGER DEFAULT NULL,
		 * `apWins` INTEGER DEFAULT NULL,
		 * `apD2Level` INTEGER DEFAULT NULL,
		 * `apW3Level` INTEGER DEFAULT NULL
		 */
		
		try {
			if(rsRank != null)
				d.close(rsRank);
			
			rsRank = d.getRank(id);
			rsRank.next();
			txtShortPrefix.setText(rsRank.getString("shortPrefix"));
			txtPrefix.setText(rsRank.getString("prefix"));
			txtVerbstr.setText(rsRank.getString("verbstr"));
			txtGreeting.setText(rsRank.getString("greeting"));
			txtAPDays.setText(rsRank.getString("apDays"));
			txtAPWins.setText(rsRank.getString("apWins"));
			txtAPD2Level.setText(rsRank.getString("apD2Level"));
			txtAPW3Level.setText(rsRank.getString("apW3Level"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
