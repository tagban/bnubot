/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import net.bnubot.util.Out;

public class JARLoader {
	private static final URLClassLoader loader;
	static {
		String folder = "lib";
		
		File f = new File(folder);
		if(!f.exists() || !f.isDirectory())
			Out.fatalException(new FileNotFoundException(f.getName()));
		
		FilenameFilter fnf = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}};
		
		String[] files = f.list(fnf);
		URL[] urls = new URL[files.length];
		for(int i = 0; i < files.length; i++)
			try {
				Out.debug(JARLoader.class, "Loading " + files[i]);
				urls[i] = new URL("file:" + folder + "/" + files[i]);
			} catch (MalformedURLException e) {
				Out.exception(e);
			}
		
		loader = new URLClassLoader(urls);
	}
	
	public static Class<?> forName(String name) throws ClassNotFoundException {
		Out.debug(JARLoader.class, name);
		return loader.loadClass(name);
	}
}
