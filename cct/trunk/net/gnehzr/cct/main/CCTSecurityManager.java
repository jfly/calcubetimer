package net.gnehzr.cct.main;

public class CCTSecurityManager extends SecurityManager {
	public Class<?>[] getClassesInStack() {
		return getClassContext();
	}
}
