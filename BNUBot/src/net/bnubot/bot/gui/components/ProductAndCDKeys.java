/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import net.bnubot.bot.gui.KeyManager;
import net.bnubot.bot.gui.KeyManager.CDKey;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.settings.ConnectionSettings;

/**
 * @author scotta
 */
public class ProductAndCDKeys extends JComponent {
	private static final long serialVersionUID = -8664330132641987612L;

	private ConnectionSettings cs;
	private JComboBox<ProductIDs> cmbProduct;
	private JComboBox<CDKey> cmbCDKey;
	private JComboBox<CDKey> cmbCDKey2;

	public ProductAndCDKeys(ConnectionSettings cs, ConfigPanel parent) {
		this.cs = cs;
		cmbProduct = parent.makeCombo("Product", (new ProductIDs[0]), false);
		cmbCDKey = parent.makeCombo("CD key", (new CDKey[0]), false);
		cmbCDKey2 = parent.makeCombo("CD key 2", (new CDKey[0]), false);

		// Add a product-change listener
		cmbProduct.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateKeys();
			}});

		// Update visible products
		updateProducts();
	}

	public void addProductListener(ItemListener aListener) {
		cmbProduct.addItemListener(aListener);
	}

	public void updateProducts() {
		cmbProduct.removeAllItems();
		for(ProductIDs prod : KeyManager.getProducts()) {
			cmbProduct.addItem(prod);
			if(prod == cs.product)
				cmbProduct.setSelectedItem(prod);
		}
		updateKeys();
	}

	@SuppressWarnings("fallthrough")
	private void updateKeys() {
		// Disable the dropdowns
		cmbCDKey.setVisible(false);
		cmbCDKey2.setVisible(false);

		ProductIDs prod = getProduct();
		if(prod == null)
			return;

		switch(prod) {
		case DRTL:
		case DSHR:
		case SSHR:
			// No keys
			return;

		case D2XP:
		case W3XP:
			setExpansionKeys();
			// fall through to original keys
		case STAR:
		case SEXP:
		case JSTR:
		case W2BN:
		case D2DV:
		case WAR3:
			setOriginalKeys();
			break;
		default:
			throw new RuntimeException("Unknown product " + prod);
		}
	}

	private void setOriginalKeys() {
		cmbCDKey.setVisible(true);
		cmbCDKey.removeAllItems();
		int prod = KeyManager.PRODUCT_ALLNORMAL;

		switch(getProduct()) {
		case STAR:
		case SEXP:
		case JSTR:
			prod = KeyManager.PRODUCT_STAR;
			break;
		case W2BN:
			prod = KeyManager.PRODUCT_W2BN;
			break;
		case D2DV:
		case D2XP:
			prod = KeyManager.PRODUCT_D2DV;
			break;
		case WAR3:
		case W3XP:
			prod = KeyManager.PRODUCT_WAR3;
			break;
		}

		if(prod == KeyManager.PRODUCT_ALLNORMAL)
			throw new RuntimeException("Unknown product " + getProduct());

		for(CDKey key : KeyManager.getKeys(prod)) {
			cmbCDKey.addItem(key);

			if(key.getKey().equalsIgnoreCase(cs.cdkey))
				cmbCDKey.setSelectedItem(key);
		}
	}

	private void setExpansionKeys() {
		cmbCDKey2.setVisible(true);
		cmbCDKey2.removeAllItems();
		int prod = KeyManager.PRODUCT_ALLNORMAL;

		switch(getProduct()) {
		case D2XP:
			prod = KeyManager.PRODUCT_D2XP;
			break;
		case W3XP:
			prod = KeyManager.PRODUCT_W3XP;
			break;
		}

		if(prod == KeyManager.PRODUCT_ALLNORMAL)
			throw new RuntimeException("Unknown product " + getProduct());

		for(CDKey key : KeyManager.getKeys(prod)) {
			cmbCDKey2.addItem(key);

			if(key.getKey().equalsIgnoreCase(cs.cdkey2))
				cmbCDKey2.setSelectedItem(key);
		}
	}

	public ProductIDs getProduct() {
		return (ProductIDs)cmbProduct.getSelectedItem();
	}

	public CDKey getCDKey() {
		if(!cmbCDKey.isVisible())
			return null;
		return (CDKey)cmbCDKey.getSelectedItem();
	}

	public CDKey getCDKey2() {
		if(!cmbCDKey2.isVisible())
			return null;
		return (CDKey)cmbCDKey2.getSelectedItem();
	}

	public void setProduct(ProductIDs product) {
		cmbProduct.setSelectedItem(product);
	}

	public void setCDKey(String key) {
		cmbCDKey.setSelectedItem(key);
	}

	public void setCDKey2(String key) {
		cmbCDKey2.setSelectedItem(key);
	}
}
