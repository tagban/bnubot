/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import net.bnubot.core.EventHandler;
import net.bnubot.core.PluginManager;
import net.bnubot.logging.Out;
import net.bnubot.settings.Settings;

/**
 * A class for loading classes from JARs in the lib folder
 * @author scotta
 */
public class JARLoader {
	private static final URLClassLoader loader;
	static {
		String folder = Settings.getRootPath() + "lib";

		File f = new File(folder);
		if(!f.exists())
			f.mkdir();
		if(!f.exists() || !f.isDirectory())
			Out.fatalException(new FileNotFoundException(f.getName()));

		FilenameFilter fnf = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}};

		String[] files = f.list(fnf);
		URL[] urls = new URL[files.length];
		boolean debug = Out.isDebug(JARLoader.class);
		for(int i = 0; i < files.length; i++)
			try {
				if(debug)
					Out.debugAlways(JARLoader.class, "Loading " + files[i]);
				urls[i] = new URL("file:" + folder + "/" + files[i]);
			} catch (MalformedURLException e) {
				Out.exception(e);
			}

		loader = new URLClassLoader(urls);

		// Look at each JAR
		for(URL url : urls) {
			File file = new File(url.toExternalForm().substring(5));
			try (JarFile jf = new JarFile(file)) {
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

							checkClass(name);
						}
					} catch(Throwable t) {}
				}
			} catch (Exception e) {
				Out.exception(e);
			}
		}
	}

	/**
	 * Check if a class is a JDBC driver, a Look and Feel, or a plugin
	 * @param name The fully qualified class name
	 */
	@SuppressWarnings("unchecked")
	private static void checkClass(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, HeadlessException {
		// Get a Class for it
		Class<?> clazz = JARLoader.forName(name);

		for(Class<?> superClazz = clazz; superClazz != null; superClazz = superClazz.getSuperclass()) {
			// Check if it's a look and feel
			if(superClazz.equals(LookAndFeel.class)) {
				LookAndFeel laf = (LookAndFeel)clazz.newInstance();
				UIManager.installLookAndFeel(laf.getName(), name);
				break;
			}

			// Check the interfaces
			for(Class<?> cif : superClazz.getInterfaces()) {
				// Check if it's a plugin
				if(cif.equals(EventHandler.class)) {
					PluginManager.register((Class<? extends EventHandler>) clazz);
					break;
				}
			}
		}
	}

	public static Class<?> forName(String name) throws ClassNotFoundException {
		try {
			return loader.loadClass(name);
		} catch(ClassNotFoundException e) {
			Out.error(JARLoader.class, "Failed to load " + name);
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
