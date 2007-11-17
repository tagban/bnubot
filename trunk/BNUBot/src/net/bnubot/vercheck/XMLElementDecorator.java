/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import net.bnubot.util.TimeFormatter;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLElementDecorator {
	private String name = null;
	private XMLElementDecorator parent = null;
	private ArrayList<XMLElementDecorator> children = new ArrayList<XMLElementDecorator>();
	private String contents = null;
	private static XMLElementDecorator elem = null; 
	
	public synchronized static XMLElementDecorator parse(String url) throws Exception {
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
				elem.appendContents(new String(ch, start, length));
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
		
		xr.parse(url);
		
		return elem;
	}
	
	public XMLElementDecorator(String name, XMLElementDecorator parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public void addChild(XMLElementDecorator child) {
		children.add(child);
	}
	
	public XMLElementDecorator getChild(String name) {
		for(XMLElementDecorator child : children)
			if(child.getName().equals(name))
				return child;
		return null;
	}
	
	public XMLElementDecorator[] getChildren(String name) {
		ArrayList<XMLElementDecorator> matches = new ArrayList<XMLElementDecorator>();
		for(XMLElementDecorator child : children)
			if(child.getName().equals(name))
				matches.add(child);
		return matches.toArray(new XMLElementDecorator[matches.size()]);
	}
	
	public XMLElementDecorator getPath(String path) {
		XMLElementDecorator ed = this;
		for(String id : path.split("\\/")) {
			ed = ed.getChild(id);
			if(ed == null)
				return null;
		}
		return ed;
	}

	public String getName() {
		return name;
	}

	public XMLElementDecorator getParent() {
		return parent;
	}
	
	public void appendContents(String contents) {
		if(this.contents == null)
			this.contents = contents;
		else
			this.contents += contents;
	}
	
	public String getString() {
		return contents;
	}
	
	public Integer getInt() {
		if(contents == null)
			return null;
		if(contents.matches("[0-9]+"))
			return Integer.parseInt(contents);
		if(contents.matches("0x[a-fA-F0-9]+"))
			return Integer.parseInt(contents.substring(2), 16);
		throw new NumberFormatException(contents);
	}
	
	public Date getDate() {
		if(contents == null)
			return null;

		if(contents.matches("[0-9]+"))
			return new Date(Long.parseLong(contents));
		
		try {
			return new Date(TimeFormatter.parseDate(contents));
		} catch(ParseException e) {}
		
		throw new NumberFormatException(contents);
	}
	
	public String toString() {
		String out;
		if(children.size() == 0)
			out = contents;
		else {
			out = "";
			for(XMLElementDecorator child : children)
				out += "\n" + child.toString();
			out = out.replace("\n", "\n\t") + "\n";
		}
		
		return "<" + name + ">" + out + "</" + name + ">";
	}
}
