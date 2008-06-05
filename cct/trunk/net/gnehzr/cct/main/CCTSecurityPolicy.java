package net.gnehzr.cct.main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

import net.gnehzr.cct.configuration.Configuration;
import sun.security.provider.PolicyFile;

public class CCTSecurityPolicy extends Policy {
	private PermissionCollection rootPerms, pluginsPerms;
	private CodeSource rootSource, pluginsSource;
	public CCTSecurityPolicy() {
		//Unfortunately, there is no way to know the directory of cct from within the
		//policy file. This class will effectively translate references to cctRoot and cctScramblePlugins
		//in the policy file to their proper locations on disk. This allows cct to run
		//with a security manager even if it is invoked from a different directory than
		//the one that contains CALCubeTimer.jar
		try {
			Policy f = new PolicyFile(new File(Configuration.getRootDirectory(), "scramblePlugin.policy").toURI().toURL());
			rootPerms = f.getPermissions(new ProtectionDomain(new CodeSource(new URL("file:cctRoot"), (Certificate[]) null), null));
			pluginsPerms = f.getPermissions(new ProtectionDomain(new CodeSource(new URL("file:cctScramblePlugins"), (Certificate[]) null), null));
			URL root = new URL(Configuration.getRootDirectory().toURI().toURL(), "*");
			rootSource = new CodeSource(root, (Certificate[]) null);
			root = new URL(Configuration.scramblePluginsFolder.toURI().toURL(), "*");
			pluginsSource = new CodeSource(root, (Certificate[]) null);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	@Override
	public PermissionCollection getPermissions(CodeSource codesource) {
		//note: the order of these checks doesn't matter, because we did not specify a recursive
		//code source for rootSource or pluginsSource.
		//in other words, rootSource does not imply pluginsSource
		if(rootSource.implies(codesource))
			return rootPerms;
		else if(pluginsSource.implies(codesource))
			return pluginsPerms;
		else
			return super.getPermissions(codesource);
	}
}
