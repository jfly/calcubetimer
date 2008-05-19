package net.gnehzr.cct.main;

import java.security.Permission;
import java.util.Arrays;

public class ScramblePluginSecurityManager extends SecurityManager {
	public void checkPermission(Permission perm) {
//		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
//		for(StackTraceElement ste : stack) {
//			if(ste.getClassName().toLowerCase().indexOf("net.gnehzr.cct.scrambles.scrambleplugin") != -1) {
//				System.out.println(ste);
//				System.out.println("NOT ALLOWED: " + perm.getName());
////				throw new SecurityException();
//			}
//		}
	}
}
