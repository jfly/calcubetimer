package net.gnehzr.cct.main;

import java.io.File;

import net.gnehzr.cct.configuration.Configuration;

public class Profile {
	private String name;
	private File directory, configuration;
	public Profile(String name) {
		this.name = name;
		directory = new File(Configuration.profilesFolder, name+"/");
		configuration = new File(Configuration.profilesFolder, name+"/"+name+".properties");
	}
	public String getName() {
		return name;
	}
	public File getConfigurationFile() {
		return configuration;
	}
	public boolean createProfileDirectory() {
		return directory.mkdir();
	}
	public boolean renameTo(Profile newProfile) {
		boolean success = this.directory.renameTo(newProfile.directory);
		//the properties file may not exist, so there's no reason to fail if it doesn't
		new File(newProfile.directory, name + ".properties").renameTo(newProfile.configuration);
		if(success) {
			this.name = newProfile.name;
			System.out.println(this.name);
		}
		return success;
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
