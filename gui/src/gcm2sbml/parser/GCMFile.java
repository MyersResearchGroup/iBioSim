package gcm2sbml.parser;

import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Reaction;
import gcm2sbml.network.SpeciesInterface;

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
		promoters = new HashMap<String, Properties>();
		globalParameters = new HashMap<String, String>();
		promoters.put("none", null);
		loadDefaultParameters();
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
			buffer.append("}\nGlobal {\n");			
			for (String s : defaultParameters.keySet()) {
				String value = defaultParameters.get(s);
				if (globalParameters.containsKey(s)) {
					value = globalParameters.get(s);
				}
				buffer.append(s + "=" + value + "\n");
			}
			buffer.append("}\n");
			p.print(buffer);
			p.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		species = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		globalParameters = new HashMap<String, String>();
		promoters.put("none", null);
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
		parseGlobal(data);
	}

	public void changePromoterName(String oldName, String newName) {
		for (Properties p : influences.values()) {
			if (p.containsKey(Reaction.PROMOTER)
					&& p.getProperty(Reaction.PROMOTER).equals(oldName)) {
				p.setProperty(Reaction.PROMOTER, newName);
			}
		}
		promoters.put(newName, promoters.get(oldName));
		promoters.remove(oldName);
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
				influences.put(newInfluenceName, influences.get(input + " -> "
						+ output));
				influences.remove(input + " -> " + output);
			}
		}
		species.put(newName, species.get(oldName));
		species.remove(oldName);
	}

	public void addSpecies(String name, Properties property) {
		species.put(name, property);
	}

	public void addPromoter(String name, Properties properties) {
		promoters.put(name, properties);
	}

	public void addInfluences(String name, Properties property) {
		influences.put(name, property);
		// Now check to see if a promoter exists in the property
		if (property.containsKey("promoter")) {
			promoters.put(property.getProperty("promoter"), null);
		}

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

	/**
	 * Checks to see if removing promoter is okay
	 * 
	 * @param name
	 *            promoter to remove
	 * @return true if promoter is in no influences
	 */
	public boolean removePromoterCheck(String name) {
		for (Properties p : influences.values()) {
			if (p.containsKey(Reaction.PROMOTER)
					&& p.getProperty(Reaction.PROMOTER).equals(name)) {
				return false;
			}
		}
		return true;
	}

	public void removePromoter(String name) {
		if (name != null && promoters.containsKey(name)) {
			promoters.remove(name);
		}
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

	public String[] getPromotersAsArray() {
		String[] s = new String[promoters.size()];
		s = promoters.keySet().toArray(s);
		return s;
	}

	public HashMap<String, Properties> getPromoters() {
		return promoters;
	}

	public HashMap<String, String> getGlobalParameters() {
		return globalParameters;
	}

	public HashMap<String, String> getDefaultParameters() {
		return defaultParameters;
	}

	public String getParameter(String parameter) {
		if (globalParameters.containsKey(parameter)) {
			return globalParameters.get(parameter);
		} else {
			return defaultParameters.get(parameter);
		}
	}

	public void setParameter(String parameter, String value) {
		globalParameters.put(parameter, value);
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

	private void parseGlobal(StringBuffer data) {
		Pattern pattern = Pattern.compile(GLOBAL);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			String s = matcher.group(1);
			matcher = propPattern.matcher(s);
			while (matcher.find()) {
				globalParameters.put(matcher.group(1), matcher.group(2));
			}
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

	private void loadDefaultParameters() {
		defaultParameters = new HashMap<String, String>();
		defaultParameters.put(SpeciesInterface.DECAY, DEG);
		defaultParameters.put(SpeciesInterface.DIMER_CONST, KDIMER);
		defaultParameters.put(GeneticNetwork.KBIO, KBIO);
		defaultParameters.put(GeneticNetwork.KCOOP, KCOOP);
		defaultParameters.put(GeneticNetwork.KREP, KREP);
		defaultParameters.put(GeneticNetwork.KACT, KACT);
		defaultParameters.put(GeneticNetwork.KRNAP, KRNAP);
		defaultParameters.put(GeneticNetwork.RNAP, RNAP);
		defaultParameters.put(GeneticNetwork.OCR, OCR);
		defaultParameters.put(GeneticNetwork.BASAL, BASAL);
		defaultParameters.put(GeneticNetwork.PROMOTERS, PROMOTERS);
		defaultParameters.put(GeneticNetwork.STOC, STOC);
		defaultParameters.put(GeneticNetwork.ACTIVATED, ACTIVATED);
	}

	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";
	private static final String REACTION = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*) *\\[([^\\]]*)]";
	private static final String PARSE = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*)";
	private static final String PROPERTY = "([a-zA-Z]+)=([^\\s,]+)";
	private static final String GLOBAL = "Global\\s\\{([^}]*)\\s\\}";

	private HashMap<String, Properties> species;
	private HashMap<String, Properties> influences;
	private HashMap<String, Properties> promoters;
	private HashMap<String, String> defaultParameters;
	private HashMap<String, String> globalParameters;

	private static final String DEG = ".0075";
	private static final String KDIMER = ".05";
	private static final String KBIO = ".05";
	private static final String KCOOP = ".05";	
	private static final String KREP = "2.2";
	private static final String KACT = ".0033";
	private static final String KRNAP = ".033";
	private static final String OCR = ".25";
	private static final String ACTIVATED = ".25";
	private static final String PROMOTERS = "1";
	private static final String STOC = "1";
	private static final String RNAP = "30";
	private static final String BASAL = ".0001";
}
