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
	private boolean saveable = true;
	public Profile(File directory) {
		saveable = false;
		this.directory = directory;
		this.name = directory.getAbsolutePath();
		configuration = new File(directory, directory.getName() + ".properties");
	}
	public boolean isSaveable() {
		return saveable;
	}
	public String getName() {
		return name;
	}
	public File getConfigurationFile() {
		return configuration;
	}
	public void createProfileDirectory() {
		directory.mkdir();
	}
	public void renameTo(String newName) {
		this.name = newName;
	}
	public void renameTo(Profile newProfile) {
		this.directory.renameTo(newProfile.directory);
		new File(newProfile.directory, name + ".properties").renameTo(newProfile.configuration);
		this.name = newProfile.name;
	}
	public void delete() {
		configuration.delete();
		directory.delete();
	}
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(o instanceof Profile) {
			return ((Profile) o).directory.equals(directory);
		}
		return this.toString().equalsIgnoreCase(o.toString());
	}
	public String toString() {
		return name;
	}
}
