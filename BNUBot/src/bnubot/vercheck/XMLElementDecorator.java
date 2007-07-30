/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.vercheck;

import java.util.ArrayList;

public class XMLElementDecorator {
	private String name;
	private XMLElementDecorator parent = null;
	private ArrayList<XMLElementDecorator> children = new ArrayList<XMLElementDecorator>();
	private String contents;
	
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
	
	public void setContents(String contents) {
		this.contents = contents;
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
