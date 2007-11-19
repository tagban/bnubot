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
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import net.bnubot.bot.database.DriverShim;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.Out;

/**
 * A class for loading classes from JARs in the lib folder
 * @author scotta
 */
public class JARLoader {
	private static final URLClassLoader loader;
	static {
		String folder = "lib";
		
		File f = new File(folder);
		if(!f.exists())
			f.mkdir();
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
		
		// Look at each JAR
		for(URL url : urls) {
			try {
				File file = new File(url.toExternalForm().substring(5));
				JarFile jf = new JarFile(file);
				// Look at each class inside the jar
				Enumeration<JarEntry> en = jf.entries();
				while(en.hasMoreElements()) {
					try {
						JarEntry je = en.nextElement();
						String name = je.getName();
						if(name.endsWith(".class")) {
							// Convert the filename to an FQ class name
							name = name.substring(0, name.length() - 6);
							name = name.replace('/', '.');
							
							// Get a Class for it
							Class<?> clazz = JARLoader.forName(name);
							
							// Check if it's a JDBC driver
							for(Class<?> cif : clazz.getInterfaces()) {
								if(cif.equals(Driver.class)) {
									DriverManager.registerDriver(new DriverShim((Driver)clazz.newInstance()));
									break;
								}
							}

							// Check if it's a look and feel
							if(GlobalSettings.enableGUI)
								for(Class<?> superClazz = clazz; superClazz != null; superClazz = superClazz.getSuperclass()) {
									if(superClazz.equals(LookAndFeel.class)) {
										LookAndFeel laf = (LookAndFeel)clazz.newInstance();
										UIManager.installLookAndFeel(laf.getName(), name);
										break;
									}
								}
						}
					} catch(NoClassDefFoundError e) {
					} catch(ClassNotFoundException e) {
					} catch(InstantiationException e) {
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Class<?> forName(String name) throws ClassNotFoundException {
		try {
			Class<?> c = loader.loadClass(name);
			Out.debug(JARLoader.class, "Loaded " + name);
			return c;
		} catch(ClassNotFoundException e) {
			Out.debug(JARLoader.class, "Failed to load " + name);
			throw e;
		} catch(UnsupportedClassVersionError e) {
			String msg = "Unsupported class version " + name;
			Out.debug(JARLoader.class, msg);
			throw new ClassNotFoundException(msg, e);
		}
	}

	public static ClassLoader getClassLoader() {
		return loader;
	}
}
