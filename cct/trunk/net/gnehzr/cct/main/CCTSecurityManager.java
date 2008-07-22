package net.gnehzr.cct.main;

import java.security.Permission;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;

public class CCTSecurityManager extends SecurityManager implements ConfigurationChangeListener {
	private ClassLoader pluginLoader;
	private boolean enabled = true;
	public CCTSecurityManager(ClassLoader pluginLoader) {
		this.pluginLoader = pluginLoader;
		Configuration.addConfigurationChangeListener(this);
	}
	public void configurationChanged() {
		enabled = Configuration.getBoolean(VariableKey.SCRAMBLE_PLUGINS_SECURE, false);
	}
	public void checkPermission(Permission perm) {
		//we can't do this by setting a policy,
		//because of doPrivileged() calls in APIs
		//like swing
//		if(enabled)
//			for(Class<?> c : getClassContext())
//				if(pluginLoader.equals(c.getClassLoader()))
//					throw new SecurityException(perm.toString());
//		super.checkPermission(perm);
	}
}
