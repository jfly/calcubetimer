package net.gnehzr.cct.main;

import java.security.Permission;

public class CCTSecurityManager extends SecurityManager {
	private ClassLoader pluginLoader;
	public CCTSecurityManager(ClassLoader pluginLoader) {
		this.pluginLoader = pluginLoader;
	}
	public void checkPermission(Permission perm) {
		//we can't do this by setting a policy,
		//because of doPrivileged() calls in APIs
		//like swing
		for(Class<?> c : getClassContext())
			if(pluginLoader.equals(c.getClassLoader()))
				throw new SecurityException();
//		super.checkPermission(perm);
	}
}
