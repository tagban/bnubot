/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.vercheck;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


import util.Constants;

public class VersionCheck {
	protected static XMLElementDecorator elem = null;
	protected static VersionNumber vnLatest = null;
	
	public static boolean checkVersion() throws Exception {
		elem = new XMLElementDecorator("root", null);
		
		XMLReader xr = XMLReaderFactory.createXMLReader();
		
		xr.setContentHandler(new ContentHandler() {
			public void startDocument() throws SAXException {}
			public void endDocument() throws SAXException {}

			public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
				XMLElementDecorator child = new XMLElementDecorator(name, elem);
				elem.addChild(child);
				elem = child;
			}

			public void endElement(String uri, String localName, String name) throws SAXException {
				elem = elem.getParent();
			}
			
			public void characters(char[] ch, int start, int length) throws SAXException {
				elem.setContents(new String(ch, start, length));
			}

			public void startPrefixMapping(String prefix, String uri) throws SAXException {}
			public void endPrefixMapping(String prefix) throws SAXException {}
			public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
			public void processingInstruction(String target, String data) throws SAXException { }
			public void setDocumentLocator(Locator locator) {}
			public void skippedEntity(String name) throws SAXException {}
		});
		xr.setErrorHandler(new ErrorHandler() {
			public void error(SAXParseException arg0) throws SAXException {
				arg0.printStackTrace();
			}
			
			public void fatalError(SAXParseException arg0) throws SAXException {
				arg0.printStackTrace();
			}
			
			public void warning(SAXParseException arg0) throws SAXException {
				arg0.printStackTrace();
			}
		});
		xr.parse("http://www.clanbnu.ws/bnubot/version.php");
		
		XMLElementDecorator gamesElem = elem.getPath("bnubot/games");
		if(gamesElem != null)
			for(int i = 0; i < Constants.prods.length; i++) {
				String game = Constants.prods[i];
				int verByte = Constants.IX86verbytes[i];
				
				XMLElementDecorator gameElem = gamesElem.getPath(game);
				if(gameElem == null)
					continue;
				
				int vb = gameElem.getPath("verbyte").getInt();
				
				if(verByte != vb) {
					System.err.println("Verbyte for game " + game + " is updating from 0x" + Integer.toHexString(verByte) + " to 0x" + Integer.toHexString(vb));
					Constants.IX86verbytes[i] = vb;
				}
			}
		else
			System.out.println("Version check resulted in no games!");

		XMLElementDecorator verLatest = elem.getPath("bnubot/latestVersion");
		
		vnLatest = new VersionNumber(
				verLatest.getPath("major").getInt(),
				verLatest.getPath("minor").getInt(),
				verLatest.getPath("revision").getInt(),
				verLatest.getPath("alpha").getInt(),
				verLatest.getPath("beta").getInt(),
				verLatest.getPath("rc").getInt());
		VersionNumber vnCurrent = CurrentVersion.version();

		boolean update = vnLatest.isNewerThan(vnCurrent);
		if(update) {
			System.out.println("Current version: " + vnCurrent.toString());
			System.out.println("Latest version: " + vnLatest.toString());
		}
		return update;
	}
	
	public static VersionNumber getLatestVersion() throws Exception {
		if(vnLatest == null)
			checkVersion();
		return vnLatest;
	}
}
