package net.gnehzr.cct.main;

import java.io.File;

import net.gnehzr.cct.configuration.Configuration;

public class Profile {
	private String name;
	private File directory, configuration;
	public Profile(String name) {
		this.name = name;
		directory = new File(Configuration.profilesFolder, name+"/");
		directory.mkdir();
		configuration = new File(Configuration.profilesFolder, name+"/"+name+".properties");
	}
	public String getName() {
		return name;
	}
	public File getConfigurationFile() {
		return configuration;
	}
	public boolean renameTo(Profile newProfile) {
		return this.directory.renameTo(newProfile.directory) &&
			new File(newProfile.directory, name + ".properties").renameTo(newProfile.configuration);
	}
	public boolean delete() {
		configuration.delete();
		return directory.delete();
	}
	public boolean equals(Object o) {
		if(o == null)
			return false;
		return this.toString().equalsIgnoreCase(o.toString());
	}
	public String toString() {
		return name;
	}
}
