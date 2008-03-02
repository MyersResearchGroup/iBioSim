package gcm2sbml.parser;

import gcm2sbml.network.Reaction;
import gcm2sbml.util.GlobalConstants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import sun.misc.Compare;
import sun.misc.Sort;

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
		parameters = new HashMap<String, String>();
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
				buffer.append("label=\""+ s + "\"");
				//buffer.deleteCharAt(buffer.length() - 1);
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
				String type = "";
				if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION)) {
					type = "vee";
				} else {
					type = "tee";
				}
				buffer.append("arrowhead="+ type + "");
				//buffer.deleteCharAt(buffer.length() - 1);
				buffer.append("]\n");
			}
			buffer.append("}\nGlobal {\n");
			for (String s : defaultParameters.keySet()) {				
				if (globalParameters.containsKey(s)) {
					String value = globalParameters.get(s);
					buffer.append(s + "=" + value + "\n");
				}				
			}
			buffer.append("}\nPromoters {\n");
			for (String s : promoters.keySet()) {
				buffer.append(s + " [");
				Properties prop = promoters.get(s);
				for (Object propName : prop.keySet()) {
					buffer.append(propName + "="
							+ prop.getProperty(propName.toString()).toString()
							+ ",");
				}
				if (buffer.charAt(buffer.length() - 1) == ',') {
					buffer.deleteCharAt(buffer.length() - 1);
				}
				buffer.append("]\n");
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
		parameters = new HashMap<String, String>();
		StringBuffer data = new StringBuffer();
		loadDefaultParameters();
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
		try {
			parseStates(data);
			parseInfluences(data);
			parseGlobal(data);
			parsePromoters(data);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to parse model, creating a blank model.", "Error",
					JOptionPane.ERROR_MESSAGE);
			species = new HashMap<String, Properties>();
			influences = new HashMap<String, Properties>();
			promoters = new HashMap<String, Properties>();
			globalParameters = new HashMap<String, String>();
			//promoters.put("none", null);
		}
	}

	public void changePromoterName(String oldName, String newName) {
		for (Properties p : influences.values()) {
			if (p.containsKey(GlobalConstants.PROMOTER)
					&& p.getProperty(GlobalConstants.PROMOTER).equals(oldName)) {
				p.setProperty(GlobalConstants.PROMOTER, newName);
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
		promoters.put(name.replace("\"", ""), properties);
	}

	public void addInfluences(String name, Properties property) {
		influences.put(name, property);
		// Now check to see if a promoter exists in the property
		if (property.containsKey("promoter")) {
			promoters.put(
					property.getProperty("promoter").replaceAll("\"", ""),
					new Properties());
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
			if (p.containsKey(GlobalConstants.PROMOTER)
					&& p.getProperty(GlobalConstants.PROMOTER).equals(name)) {
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

	public String[] getSpeciesAsArray() {
		String[] s = new String[species.size()];
		s = species.keySet().toArray(s);
		Arrays.sort(s);
		return s;
	}

	public String[] getPromotersAsArray() {
		String[] s = new String[promoters.size()];
		s = promoters.keySet().toArray(s);
		Arrays.sort(s);
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
	
	public HashMap<String, String> getParameters() {		
		return parameters;
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
		parameters.put(parameter, value);
	}

	private void parseStates(StringBuffer data) {
		Pattern network = Pattern.compile(NETWORK);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		matcher.find();
		matcher = pattern.matcher(matcher.group(1));
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
				parameters.put(matcher.group(1), matcher.group(2));
			}
		}
	}

	private void parsePromoters(StringBuffer data) {
		Pattern network = Pattern.compile(PROMOTERS_LIST);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		matcher.find();
		matcher = pattern.matcher(matcher.group(1));
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				properties.put(propMatcher.group(1), propMatcher.group(2));
			}
			promoters.put(name, properties);
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
				if (propMatcher.group(1).equals(GlobalConstants.PROMOTER)
						&& !promoters.containsKey(propMatcher.group(2))) {
					promoters.put(propMatcher.group(2).replaceAll("\"", ""),
							new Properties());
				}
			}
			if (properties.containsKey("arrowhead")) {
				if (properties.getProperty("arrowhead").indexOf("vee") != -1) {
					properties.setProperty(GlobalConstants.TYPE, GlobalConstants.ACTIVATION);
				} else {
					properties.setProperty(GlobalConstants.TYPE, GlobalConstants.REPRESSION);
				}
			}
			influences.put(name, properties);
		}
	}

	private void loadDefaultParameters() {
		defaultParameters = new HashMap<String, String>();
		defaultParameters.put(GlobalConstants.KDECAY_STRING,
				GlobalConstants.KDECAY_VALUE);
		defaultParameters.put(GlobalConstants.KASSOCIATION_STRING,
				GlobalConstants.KASSOCIATION_VALUE);
		defaultParameters.put(GlobalConstants.KBIO_STRING,
				GlobalConstants.KBIO_VALUE);
		defaultParameters.put(GlobalConstants.COOPERATIVITY_STRING,
				GlobalConstants.COOPERATIVITY_VALUE);
		defaultParameters.put(GlobalConstants.KREP_STRING,
				GlobalConstants.KREP_VALUE);
		defaultParameters.put(GlobalConstants.KACT_STRING,
				GlobalConstants.KACT_VALUE);
		defaultParameters.put(GlobalConstants.RNAP_BINDING_STRING,
				GlobalConstants.RNAP_BINDING_VALUE);
		defaultParameters.put(GlobalConstants.RNAP_STRING,
				GlobalConstants.RNAP_VALUE);
		defaultParameters.put(GlobalConstants.OCR_STRING,
				GlobalConstants.OCR_VALUE);
		defaultParameters.put(GlobalConstants.KBASAL_STRING,
				GlobalConstants.KBASAL_VALUE);
		defaultParameters.put(GlobalConstants.PROMOTER_COUNT_STRING,
				GlobalConstants.PROMOTER_COUNT_VALUE);
		defaultParameters.put(GlobalConstants.STOICHIOMETRY_STRING,
				GlobalConstants.STOICHIOMETRY_VALUE);
		defaultParameters.put(GlobalConstants.ACTIVED_STRING,
				GlobalConstants.ACTIVED_VALUE);
		defaultParameters.put(GlobalConstants.MAX_DIMER_STRING,
				GlobalConstants.MAX_DIMER_VALUE);
		defaultParameters.put(GlobalConstants.INITIAL_STRING,
				GlobalConstants.INITIAL_VALUE);
		
		for (String s : defaultParameters.keySet()) {
			parameters.put(s, defaultParameters.get(s));
		}

	}

	private static final String NETWORK = "digraph\\sG\\s\\{([^}]*)\\s\\}";

	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	private static final String REACTION = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*) *\\[([^\\]]*)]";

	private static final String PARSE = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*)";

	private static final String PROPERTY = "([a-zA-Z\\s\\-]+)=([^\\s,]+)";

	private static final String GLOBAL = "Global\\s\\{([^}]*)\\s\\}";

	private static final String PROMOTERS_LIST = "Promoters\\s\\{([^}]*)\\s\\}";

	private HashMap<String, Properties> species;

	private HashMap<String, Properties> influences;

	private HashMap<String, Properties> promoters;
	
	private HashMap<String, String> parameters;

	private HashMap<String, String> defaultParameters;

	private HashMap<String, String> globalParameters;
}
