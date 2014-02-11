/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.wizard;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.ConfigSpinner;
import net.bnubot.bot.gui.components.ConfigTextField;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.GlobalSettings.SOCKSType;

/**
 * @author scotta
 */
public class ProxyDetailsWizardPage extends AbstractWizardPage {
	private final String header;
	private JCheckBox chkProxyEnabled;
	private JComboBox<SOCKSType> cmbProxyType;
	private ConfigTextField txtProxyHost;
	private ConfigSpinner spnProxyPort;

	public ProxyDetailsWizardPage(String header) {
		this.header = header;
	}

	@Override
	public void createComponent(ConfigPanel cp) {
		cp.add(new JLabel("<html>" +
				"<h1>" + header + "</h1>" +
				"<hr/><br/>" +
				"Do you require a proxy to connect to battle.net? If so, enter the details<br/>" +
				"below. If not, you may skip this step.<br/>" +
				"<br/></html>"));

		chkProxyEnabled = cp.makeCheck("Enabled", GlobalSettings.socksEnabled);
		cmbProxyType = cp.makeCombo("Type", GlobalSettings.SOCKSType.values(), false);
		txtProxyHost = cp.makeText("Host", GlobalSettings.socksHost);
		spnProxyPort = cp.makeSpinner("Port", GlobalSettings.socksPort);
	}

	@Override
	public String isPageComplete() {
		GlobalSettings.socksEnabled = chkProxyEnabled.isSelected();
		GlobalSettings.socksType = (SOCKSType)cmbProxyType.getSelectedItem();
		GlobalSettings.socksHost = txtProxyHost.getText();
		GlobalSettings.socksPort = spnProxyPort.getValue().intValue();
		GlobalSettings.save();
		return null;
	}}