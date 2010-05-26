package net.gnehzr.cct.scrambles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ScramblePluginClassLoader extends ClassLoader {
	private HashMap<String, Class<?>> classMap = new HashMap<String, Class<?>>();
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(!isAllowedClass(name))
			throw new ClassNotFoundException("Scramble plugins may not access " + name);
		Class<?> cls = classMap.get(name);
		if(cls != null) {
			return cls;
		}
		cls = getClassImplFromDataBase(name);
		if(cls != null) {
			classMap.put(name, cls);
			return cls;
		}
		return super.loadClass(name, resolve);
	}
	//we restrict access to almost all cct classes (net.gnehzr....)
	private boolean isAllowedClass(String className) {
		return !className.startsWith("net.gnehzr") || 
		className.equals("net.gnehzr.cct.scrambles.Scramble") || 
		className.equals("net.gnehzr.cct.scrambles.Scramble$InvalidScrambleException") ||
		className.equals("net.gnehzr.cct.scrambles.Scramble$JavascriptArray") ||
		className.equals("net.gnehzr.cct.scrambles.CrossSolver") ||
		className.equals("net.gnehzr.cct.misc.Utils");
	}

	private Class<?> getClassImplFromDataBase(String className) {
		if(!className.startsWith(ScramblePlugin.SCRAMBLE_PLUGIN_PACKAGE))
			return null;
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(new File(ScramblePlugin.getRootDirectory(), className.replaceAll(Pattern.quote("."), "/") + ".class"));
			byte[] result = new byte[fi.available()];
			fi.read(result);
			return defineClass(className, result, 0, result.length, null);
		} catch (FileNotFoundException e) {
			return null;
		} catch(IOException e) {
			return null;
		} finally {
			if(fi != null)
				try {
					fi.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
