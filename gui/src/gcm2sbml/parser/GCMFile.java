package gcm2sbml.parser;

import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.util.Utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.ListDataListener;

/**
 * This class describes a GCM file
 * 
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class GCMFile {
	
	public GCMFile() {
		species = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
	}

	public void save(String filename) {
		try {
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer("digraph G {\n");
			for (String s : species.keySet()) {
				buffer.append(s + " [");
				Properties prop = species.get(s);
				for (Object propName : prop.keySet()) {
					buffer.append(propName + "="
							+ prop.getProperty(propName.toString()).toString()
							+ ",");
				}
				buffer.deleteCharAt(buffer.length() - 1);
				buffer.append("]\n");
			}
			for (String s : influences.keySet()) {
				buffer.append(s + " [");
				Properties prop = influences.get(s);
				for (Object propName : prop.keySet()) {
					buffer.append(propName + "="
							+ prop.getProperty(propName.toString()).toString()
							+ ",");
				}
				buffer.deleteCharAt(buffer.length() - 1);
				buffer.append("]\n");
			}
			buffer.append("}");
			p.print(buffer);
			p.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		StringBuffer data = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}

		parseStates(data);
		parseInfluences(data);
	}

	public void changeSpeciesName(String oldName, String newName) {
		String[] sArray = new String[influences.keySet().size()]; 
		sArray = influences.keySet().toArray(sArray);
		for (int i = 0; i < sArray.length; i++) {
			String s = sArray[i];
			String input = getInput(s);
			String output = getOutput(s);
			boolean replaceInput = input.equals(oldName);
			boolean replaceOutput = output.equals(oldName);
			String newInfluenceName = "";
			if (replaceInput || replaceOutput) {
				if (replaceInput) {
					newInfluenceName = newInfluenceName + newName;
				} else {
					newInfluenceName = newInfluenceName + input;
				}
				if (replaceOutput) {
					newInfluenceName = newInfluenceName + " -> " + newName;
				} else {
					newInfluenceName = newInfluenceName + " -> " + output;
				}
				species.put(newName, species.get(oldName));
				species.remove(oldName);
				influences.put(newInfluenceName, influences.get(input + " -> " + output));
				influences.remove(input + " -> " + output);
			}

		}
	}

	public void addSpecies(String name, Properties property) {
		species.put(name, property);
	}

	public void addInfluences(String name, Properties property) {
		influences.put(name, property);
	}

	public void removeSpecies(String name) {
		if (name != null && species.containsKey(name)) {
			species.remove(name);
		}
	}

	public HashMap<String, Properties> getSpecies() {
		return species;
	}

	public HashMap<String, Properties> getInfluences() {
		return influences;
	}

	/**
	 * Checks to see if removing influence is okay
	 * 
	 * @param name
	 *            influence to remove
	 * @return true, it is always okay to remove influence
	 */
	public boolean removeInfluenceCheck(String name) {
		return true;
	}

	/**
	 * Checks to see if removing specie is okay
	 * 
	 * @param name
	 *            specie to remove
	 * @return true if specie is in no influences
	 */
	public boolean removeSpeciesCheck(String name) {
		for (String s : influences.keySet()) {
			if (s.contains(name)) {
				return false;
			}
		}
		return true;
	}

	public void removeInfluence(String name) {
		if (name != null && influences.containsKey(name)) {
			influences.remove(name);
		}
	}

	public String getInput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(2);
	}

	public String getOutput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(3);
	}

	private void parseStates(StringBuffer data) {
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				properties.put(propMatcher.group(1), propMatcher.group(2));
			}
			species.put(name, properties);
		}
	}

	private void parseInfluences(StringBuffer data) {
		Pattern pattern = Pattern.compile(REACTION);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		while (matcher.find()) {
			String name = matcher.group(2) + " -> " + matcher.group(3);
			Matcher propMatcher = propPattern.matcher(matcher.group(4));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				properties.put(propMatcher.group(1), propMatcher.group(2));
			}
			influences.put(name, properties);
		}
	}

	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";
	private static final String REACTION = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*) *\\[([^\\]]*)]";
	private static final String PARSE = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*)";
	private static final String PROPERTY = "([a-zA-Z]+)=([^\\s,]+)";

	private HashMap<String, Properties> species;
	private HashMap<String, Properties> influences;
}
