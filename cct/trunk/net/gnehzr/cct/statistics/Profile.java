package net.gnehzr.cct.statistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.main.CALCubeTimer;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class Profile {
	//TODO - this will leak memory as cct runs
	private static HashMap<String, Profile> profiles = new HashMap<String, Profile>();
	public static Profile getProfileByName(String name) {
		Profile p = profiles.get(name);
		if(p == null) {
			p = new Profile(name);
			profiles.put(name, p);
		}
		return p;
	}
	
	private String name;
	private File directory, configuration, statistics;
	
	//constructors are private because we want only 1 instance of a profile
	//pointing to a given database
	private Profile(String name) {
		this.name = name;
		directory = getDirectory(name);
		configuration = getConfiguration(directory, name);
		statistics = getStatistics(directory, name);
	}
	private File getDirectory(String name) {
		return new File(Configuration.profilesFolder, name+"/");
	}
	private File getConfiguration(File directory, String name) {
		return new File(directory, name+".properties");
	}
	private File getStatistics(File directory, String name) {
		return new File(directory, name+".xml");
	}
	
	private boolean saveable = true;
	//I assume that this will only get called once for a given directory
	public Profile(File directory) {
		saveable = false;
		this.directory = directory;
		this.name = directory.getAbsolutePath();
		configuration = getConfiguration(directory, directory.getName());
		statistics = getStatistics(directory, directory.getName());
		profiles.put(name, this);
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
	private String newName;
	public void renameTo(String newName) {
		this.newName = newName;
	}
	public void discardRename() {
		newName = null;
	}
	public void commitRename() {
		File newDir = getDirectory(newName);
		File oldConfig = getConfiguration(newDir, name);
		File oldStats = getStatistics(newDir, name);
		configuration = getConfiguration(newDir, newName);
		statistics = getStatistics(newDir, newName);
		boolean currentProfile = (dbFile != null);
		if(currentProfile) {
			try {
				dbFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				dbFile = null;
			}
		}
		directory.renameTo(newDir);
		directory = newDir;

		oldConfig.renameTo(configuration);
		oldStats.renameTo(statistics);

		if(currentProfile) {
			RandomAccessFile t = null;
			try {
				t = new RandomAccessFile(statistics, "rw");
				t.getChannel().tryLock();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				dbFile = t;
			}
		}
		
		profiles.remove(this.name);
		this.name = newName;
		profiles.put(this.name, this);
	}
	public void delete() {
		if(dbFile != null) {
			try {
				dbFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				dbFile = null;
			}
		}
		configuration.delete();
		statistics.delete();
		directory.delete();
		if(Configuration.getSelectedProfile() == this)
			Configuration.setSelectedProfile(null);
	}
	public int hashCode() {
		return name.hashCode();
	}
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(o instanceof Profile) {
			return ((Profile) o).directory.equals(directory);
		}
		return this.name.equalsIgnoreCase(o.toString());
	}
	//this is the only indication to the user of whether we successfully loaded the database file
	public String toString() {
		return (newName != null ? newName : name) + (dbFile == null && this == Configuration.getSelectedProfile() ? StringAccessor.getString("Profile.loggingdisabled") : "");
	}
	
	private class DatabaseLoader extends DefaultHandler {
		public DatabaseLoader() {}
		
		public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
			return new InputSource(new FileInputStream(Configuration.databaseDTD));
		}
		private int level = 0;
		private String customization;
		private String seshCommentOrSolveTime;
		private Session session;
		private SolveTime solve;
		private String solveCommentOrScrambleOrSplits;
		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if(name.equalsIgnoreCase("database")) {
				if(level != 0)
					throw new SAXException("Root element must be database tag.");
			} else if(name.equalsIgnoreCase("puzzle")) {
				if(level != 1)
					throw new SAXException("1st level expected for puzzle tag.");
				customization = attributes.getValue("customization");
				if(customization == null) {
					throw new SAXException("Customization attribute needed for puzzle tag.");
				}
			} else if(name.equalsIgnoreCase("session")) {
				if(level != 2)
					throw new SAXException("2nd level expected for session tag.");
				try {
					session = new Session(Configuration.getDateFormat().parse(attributes.getValue("date")));
					puzzleDB.getPuzzleStatistics(customization).addSession(session);
				} catch (ParseException e) {
					throw new SAXException(e);
				}
				if(Boolean.parseBoolean(attributes.getValue("loadonstartup")))
					CALCubeTimer.statsModel.setSession(session);
			} else if(name.equalsIgnoreCase("solve")) {
				if(level != 3)
					throw new SAXException("3rd level expected for solve tag.");
				solve = new SolveTime();
				seshCommentOrSolveTime = "";
			} else if(name.equalsIgnoreCase("comment")) {
				if(level == 3)
					seshCommentOrSolveTime = "";
				else if(level == 4)
					solveCommentOrScrambleOrSplits = "";
				else
					throw new SAXException("3rd or 4th level expected for " + name + " tag.");
			} else if(name.equalsIgnoreCase("scramble")) {
				if(level == 4)
					solveCommentOrScrambleOrSplits = "";
				else
					throw new SAXException("4th level expected for " + name + " tag.");
			} else if(name.equalsIgnoreCase("splits")) {
				if(level == 4)
					solveCommentOrScrambleOrSplits = "";
				else
					throw new SAXException("4th level expected for " + name + " tag.");
			} else {
				throw new SAXException("Unexpected element encountered: " + name);
			}
			
			level++;
		}
		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			level--;

			if(name.equalsIgnoreCase("solve")) {
				try {
					solve.parseTime(seshCommentOrSolveTime);
					session.getStatistics().add(solve);
				} catch (Exception e) {
					e.printStackTrace();
					throw new SAXException("Unable to parse time: " + seshCommentOrSolveTime + " " + e.toString());
				}
			} else if(name.equalsIgnoreCase("comment")) {
				if(level == 3) {
					session.setComment(seshCommentOrSolveTime);
				} else if(level == 4) {
					solve.setComment(solveCommentOrScrambleOrSplits);
				}
			} else if(name.equalsIgnoreCase("scramble")) {
				solve.setScramble(solveCommentOrScrambleOrSplits);
			} else if(name.equalsIgnoreCase("splits"))
				solve.setSplitsFromString(solveCommentOrScrambleOrSplits);
		}
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			switch(level) {
			case 4: //solvetime or session comment
				seshCommentOrSolveTime += new String(ch, start, length);
				break;
			case 5: //comment or scramble or splits
				solveCommentOrScrambleOrSplits += new String(ch, start, length);
				break;
			}
		}
	}
	
	//Database stuff
	//this maps from ScrambleVariations to PuzzleStatistics
	ProfileDatabase puzzleDB = new ProfileDatabase(this);
	public ProfileDatabase getPuzzleDatabase() {
		return puzzleDB;
	}
	
	//I can't believe I had to create these two silly little classses
	private static class RandomInputStream extends InputStream {
		private RandomAccessFile raf;
		public RandomInputStream(RandomAccessFile raf) {
			this.raf = raf;
		}
		public int read() throws IOException {
			return raf.read();
		}
	}
	//this is apparently breaking indenting
	private static class RandomOutputStream extends OutputStream {
		private RandomAccessFile raf;
		public RandomOutputStream(RandomAccessFile raf) {
			this.raf = raf;
		}
		public void write(int b) throws IOException {
			raf.write(b);
		}
	}

	private RandomAccessFile dbFile = null;
	//this can only be called once, until after saveDatabase() is called
	public boolean loadDatabase() {
		if(this == Configuration.guestProfile) { //disable logging for guest
			//TODO - there is definitely a bug here where guestSession == null when switching profiles
			if(puzzleDB.getRowCount() > 0)
				CALCubeTimer.statsModel.setSession(guestSession); //TODO - does this really need to be here?
			return false;
		}
		FileLock fl = null;
		try {
			CALCubeTimer.setWaiting(true);
			RandomAccessFile t = new RandomAccessFile(statistics, "rw");
			fl = t.getChannel().tryLock();
			if(fl != null) {
				puzzleDB = new ProfileDatabase(this); //reset the database
				if(t.length() != 0) { //if the file is empty, don't bother to parse it
					DefaultHandler handler = new DatabaseLoader();
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser saxParser = factory.newSAXParser();
					saxParser.parse(new RandomInputStream(t), handler);
				}
				dbFile = t;

				return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(SAXParseException spe) {
			System.err.println(spe.getSystemId() + ":" + spe.getLineNumber() + ": parse error: " + spe.getMessage());
			
			Exception x = spe;
			if(spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();
		} catch(SAXException se) {
			Exception x = se;
			if(se.getException() != null)
				x = se.getException();
			x.printStackTrace();
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} finally {
			CALCubeTimer.setWaiting(false);
		}

		if(fl != null)
			try {
				fl.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return false;
	}
	
	private Session guestSession = null; //need this so we can load the guest's last session, since it doesn't have a file
	
	public void saveDatabase() throws IOException, FileNotFoundException, TransformerConfigurationException, SAXException {
		puzzleDB.removeEmptySessions();
		if(this == Configuration.guestProfile) {
			guestSession = CALCubeTimer.statsModel.getCurrentSession();
		}
		if(dbFile == null)
			return;
		try {
			CALCubeTimer.setWaiting(true);
			dbFile.setLength(0);
			StreamResult streamResult = new StreamResult(new RandomOutputStream(dbFile));
			SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			tf.setAttribute("indent-number", Integer.valueOf(4));
			// SAX2.0 ContentHandler.
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "../database.dtd");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			hd.startDocument();
			AttributesImpl atts = new AttributesImpl();
			hd.startElement("", "", "database", atts);
			for(PuzzleStatistics ps : puzzleDB.getPuzzlesStatistics()) {
				//TODO - check if there are 0 sessions here and continue? NOTE: this isn't good enough, as there could be a bunch of empty sessions
				atts.clear();
				atts.addAttribute("", "", "customization", "CDATA", ps.getCustomization());
				hd.startElement("", "", "puzzle", atts);
				for(Session s : ps.toSessionIterable()) {
					Statistics stats = s.getStatistics();
					if(stats.getAttemptCount() == 0) //this indicates that the session wasn't started
						continue;
					atts.clear();
					atts.addAttribute("", "", "date", "CDATA", s.toDateString());
					if(s == CALCubeTimer.statsModel.getCurrentSession())
						atts.addAttribute("", "", "loadonstartup", "CDATA", "true");
					hd.startElement("", "", "session", atts);
					atts.clear();
					String temp = s.getComment();
					if(!temp.isEmpty()) {
						hd.startElement("", "", "comment", atts);
						char[] chs = temp.toCharArray();
						hd.characters(chs, 0, chs.length);
						hd.endElement("", "", "comment");
					}
					for(int ch = 0; ch < stats.getAttemptCount(); ch++) {
						SolveTime st = stats.get(ch);
						atts.clear();
						hd.startElement("", "", "solve", atts);
						char[] chs = st.toExternalizableString().toCharArray();
						hd.characters(chs, 0, chs.length);
						temp = st.getComment();
						if(!temp.isEmpty()) {
							atts.clear();
							hd.startElement("", "", "comment", atts);
							chs = temp.toCharArray();
							hd.characters(chs, 0, chs.length);
							hd.endElement("", "", "comment");
						}
						temp = st.toSplitsString();
						if(!temp.isEmpty()) {
							atts.clear();
							hd.startElement("", "", "splits", atts);
							chs = temp.toCharArray();
							hd.characters(chs, 0, chs.length);
							hd.endElement("", "", "splits");
						}
						temp = st.getScramble();
						if(!temp.isEmpty()) {
							atts.clear();
							hd.startElement("", "", "scramble", atts);
							chs = temp.toCharArray();
							hd.characters(chs, 0, chs.length);
							hd.endElement("", "", "scramble");
						}
						
						hd.endElement("", "", "solve");
					}
					hd.endElement("", "", "session");
				}
				hd.endElement("", "", "puzzle");
			}
			hd.endElement("", "", "database");
			hd.endDocument();
		} finally {
			CALCubeTimer.setWaiting(false);
		}
		try {
			dbFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			dbFile = null;
		}
	}
}
