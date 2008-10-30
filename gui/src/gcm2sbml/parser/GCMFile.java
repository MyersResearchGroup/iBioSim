package gcm2sbml.parser;

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
import java.util.prefs.Preferences;
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
		components = new HashMap<String, Properties>();
		globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		loadDefaultParameters();
	}
	
	public String getSBMLFile() {
		return sbmlFile;
	}

	public void setSBMLFile(String file) {
		sbmlFile = file;
	}
	
	public boolean getDimAbs() {
		return dimAbs;
	}

	public void setDimAbs(boolean dimAbs) {
		this.dimAbs = dimAbs;
	}
	
	public boolean getBioAbs() {
		return bioAbs;
	}

	public void setBioAbs(boolean bioAbs) {
		this.bioAbs = bioAbs;
	}

	public void save(String filename) {
		try {
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			StringBuffer buffer = new StringBuffer("digraph G {\n");
			for (String s : species.keySet()) {
				buffer.append(s + " [");
				Properties prop = species.get(s);
				for (Object propName : prop.keySet()) {
					if ((propName.toString().equals(GlobalConstants.NAME)) ||
						(propName.toString().equals("label"))) {
						buffer.append(checkCompabilitySave(propName.toString()) + "="
								+ "\"" + prop.getProperty(propName.toString()).toString()
								+ "\"" + ",");
					} else {
						buffer.append(checkCompabilitySave(propName.toString()) + "="
								+ prop.getProperty(propName.toString()).toString()
								+ ",");
					}
				}
				if (!prop.containsKey("shape")) {
					buffer.append("shape=ellipse,");
				}
				if (!prop.containsKey("label")) {
					buffer.append("label=\"" + s + "\"");
				} else {
					buffer.deleteCharAt(buffer.lastIndexOf(","));
				}
				// buffer.deleteCharAt(buffer.length() - 1);
				buffer.append("]\n");
			}
			for (String s : influences.keySet()) {
				buffer.append(getInput(s) + " -> "// + getArrow(s) + " "
						+ getOutput(s) + " [");				
				Properties prop = influences.get(s);
				String promo = "default";
				if (prop.containsKey(GlobalConstants.PROMOTER)) {
					promo = prop.getProperty(GlobalConstants.PROMOTER);
				}
				prop.setProperty(GlobalConstants.NAME, "\""+ getInput(s) + " " + getArrow(s) + " "
						+ getOutput(s)+ ", Promoter " + promo + "\"");
				for (Object propName : prop.keySet()) {
					buffer.append(checkCompabilitySave(propName.toString()) + "="
							+ prop.getProperty(propName.toString()).toString()
							+ ",");
				}
				
				String type = "";
				if (!prop.containsKey("arrowhead")) {
					if (prop.getProperty(GlobalConstants.TYPE).equals(
							GlobalConstants.ACTIVATION)) {
						type = "vee";
					} else {
						type = "tee";
					}
					buffer.append("arrowhead=" + type + "");
				}
				if (buffer.charAt(buffer.length() - 1) == ',') {
					buffer.deleteCharAt(buffer.length() - 1);
				}
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
					if (propName.toString().equals(GlobalConstants.NAME)) {
						buffer.append(checkCompabilitySave(propName.toString()) + "="
								+ "\"" + prop.getProperty(propName.toString()).toString()
								+ "\"" + ",");
					} else {
						buffer.append(checkCompabilitySave(propName.toString()) + "="
								+ prop.getProperty(propName.toString()).toString()
								+ ",");
					}
				}
				if (buffer.charAt(buffer.length() - 1) == ',') {
					buffer.deleteCharAt(buffer.length() - 1);
				}
				buffer.append("]\n");
			}
			buffer.append("}\nComponents {\n");
			for (String s : components.keySet()) {
				buffer.append(s + " [");
				Properties prop = components.get(s);
				for (Object propName : prop.keySet()) {
					if (propName.toString().equals(GlobalConstants.ID)) {
					} else {
						buffer.append(checkCompabilitySave(propName.toString()) + "="
								+ prop.getProperty(propName.toString()).toString()
								+ ",");
					}
				}
				if (buffer.charAt(buffer.length() - 1) == ',') {
					buffer.deleteCharAt(buffer.length() - 1);
				}
				buffer.append("]\n");
			}
			buffer.append("}\n");
			buffer.append(GlobalConstants.SBMLFILE + "=\"" + sbmlFile + "\"\n");
			if (bioAbs) {
				buffer.append(GlobalConstants.BIOABS + "=true\n");
			}
			if (dimAbs) {
				buffer.append(GlobalConstants.DIMABS + "=true\n");
			} 
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
		components = new HashMap<String, Properties>();
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
			parseComponents(data);
			parseSBMLFile(data);
			parseBioAbs(data);
			parseDimAbs(data);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse GCM");
//			JOptionPane.showMessageDialog(null,
//					"Unable to parse model, creating a blank model.", "Error",
//					JOptionPane.ERROR_MESSAGE);
//			species = new HashMap<String, Properties>();
//			influences = new HashMap<String, Properties>();
//			promoters = new HashMap<String, Properties>();
//			globalParameters = new HashMap<String, String>();
		}
	}

	public void changePromoterName(String oldName, String newName) {
		String[] sArray = new String[influences.keySet().size()];
		sArray = influences.keySet().toArray(sArray);
		for (int i = 0; i < sArray.length; i++) {
			String s = sArray[i];
			String input = getInput(s);
			String arrow = getArrow(s);
			String output = getOutput(s);
			String newInfluenceName = "";
			if (influences.get(s).containsKey(GlobalConstants.PROMOTER) && influences.get(s).get(GlobalConstants.PROMOTER).equals(oldName)) {
				newInfluenceName = input + " " + arrow + " " + output + ", Promoter " + newName;				
				influences.put(newInfluenceName, influences.get(s));				
				influences.remove(s);
				influences.get(newInfluenceName).setProperty(GlobalConstants.PROMOTER, newName);
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
			String arrow = getArrow(s);
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
					newInfluenceName = newInfluenceName + " " + arrow + " " + newName;
				} else {
					newInfluenceName = newInfluenceName + " " + arrow + " " + output;
				}
				String promoterName = "default";
				if (influences.get(s).containsKey(GlobalConstants.PROMOTER)) {
					promoterName = influences.get(s).get(
							GlobalConstants.PROMOTER).toString();
				}
				newInfluenceName = newInfluenceName + ", Promoter "
						+ promoterName;
				influences.put(newInfluenceName, influences.get(s));
				influences.remove(s);
			}
		}
		species.put(newName, species.get(oldName));
		species.remove(oldName);
	}
	
	public void changeComponentName(String oldName, String newName) {
		String[] sArray = new String[influences.keySet().size()];
		sArray = influences.keySet().toArray(sArray);
		for (int i = 0; i < sArray.length; i++) {
			String s = sArray[i];
			String input = getInput(s);
			String arrow = getArrow(s);
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
					newInfluenceName = newInfluenceName + " " + arrow + " " + newName;
				} else {
					newInfluenceName = newInfluenceName + " " + arrow + " " + output;
				}
				String promoterName = "default";
				if (influences.get(s).containsKey(GlobalConstants.PROMOTER)) {
					promoterName = influences.get(s).get(
							GlobalConstants.PROMOTER).toString();
				}
				newInfluenceName = newInfluenceName + ", Promoter "
						+ promoterName;
				influences.put(newInfluenceName, influences.get(s));
				influences.remove(s);
			}
		}
		components.put(newName, components.get(oldName));
		components.remove(oldName);
	}

	public void addSpecies(String name, Properties property) {
		species.put(name, property);
	}

	public void addPromoter(String name, Properties properties) {
		promoters.put(name.replace("\"", ""), properties);
	}
	
	public void addComponent(String name, Properties properties) {
		components.put(name, properties);
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
	
	public HashMap<String, Properties> getComponents() {
		return components;
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
	
	public boolean removeComponentCheck(String name) {
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
	
	public void removeComponent(String name) {
		if (name != null && components.containsKey(name)) {
			components.remove(name);
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

	public String getArrow(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(3) + matcher.group(4);
	}

	public String getOutput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(5);
	}

	public String getPromoter(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(6);
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

	public void removeParameter(String parameter) {
		globalParameters.remove(parameter);
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
				if (propMatcher.group(3)!=null) {
					properties.put(propMatcher.group(1), propMatcher.group(3));
				} else {
					properties.put(propMatcher.group(1), propMatcher.group(4));
				}
			}
			//for backwards compatibility
			if (properties.containsKey("const")) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.CONSTANT);				
			} else if (!properties.containsKey(GlobalConstants.TYPE)) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.NORMAL);
			}
			if (properties.getProperty(GlobalConstants.TYPE).equals("constant")) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.CONSTANT);					
			}
			
			//for backwards compatibility
			if (properties.containsKey("label")) {
				properties.put(GlobalConstants.ID, properties.getProperty("label").replace("\"", ""));
			}
			species.put(name, properties);
		}
	}
	
	private void parseSBMLFile(StringBuffer data) {
		Pattern pattern = Pattern.compile(SBMLFILE);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			sbmlFile = matcher.group(1);
		}
	}

	private void parseDimAbs(StringBuffer data) {
		Pattern pattern = Pattern.compile(DIMABS);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			if (matcher.group(1).equals("true")) {
				dimAbs = true;
			}
		}
	}

	private void parseBioAbs(StringBuffer data) {
		Pattern pattern = Pattern.compile(BIOABS);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			if (matcher.group(1).equals("true")) {
				bioAbs = true;
			}
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
				if (matcher.group(3)!=null) {
					globalParameters.put(matcher.group(1), matcher.group(3));
					parameters.put(matcher.group(1), matcher.group(3));
				} else {
					globalParameters.put(matcher.group(1), matcher.group(4));
					parameters.put(matcher.group(1), matcher.group(4));
				}
			}
		}
	}

	private void parsePromoters(StringBuffer data) {
		Pattern network = Pattern.compile(PROMOTERS_LIST);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		if (!matcher.find()) {
			return;
		}
		matcher = pattern.matcher(matcher.group(1));
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3)!=null) {
					properties.put(propMatcher.group(1), propMatcher.group(3));
				} else {
					properties.put(propMatcher.group(1), propMatcher.group(4));
				}
			}
			promoters.put(name, properties);
		}
	}
	
	private void parseComponents(StringBuffer data) {
		Pattern network = Pattern.compile(COMPONENTS_LIST);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		if (!matcher.find()) {
			return;
		}
		matcher = pattern.matcher(matcher.group(1));
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3)!=null) {
					properties.put(propMatcher.group(1), propMatcher.group(3));
				} else {
					properties.put(propMatcher.group(1), propMatcher.group(4));
				}
			}
			components.put(name, properties);
		}
	}

	private void parseInfluences(StringBuffer data) {
		Pattern pattern = Pattern.compile(REACTION);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		while (matcher.find()) {
			Matcher propMatcher = propPattern.matcher(matcher.group(6));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3)!=null) {
					properties.put(checkCompabilityLoad(propMatcher.group(1)), propMatcher.group(3));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(3))) {
						promoters.put(propMatcher.group(3).replaceAll("\"", ""),
								new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(3).replace("\"", "")); //for backwards compatibility
					}
				} else {
					properties.put(checkCompabilityLoad(propMatcher.group(1)), propMatcher.group(4));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(4))) {
						promoters.put(propMatcher.group(4).replaceAll("\"", ""),
								new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(4).replace("\"", "")); //for backwards compatibility
					}
				}
			}

			String name = "";
			if (properties.containsKey("arrowhead")) {
				if (properties.getProperty("arrowhead").indexOf("vee") != -1) {
					properties.setProperty(GlobalConstants.TYPE,
							GlobalConstants.ACTIVATION);
					if (properties.containsKey(GlobalConstants.BIO) && properties.get(GlobalConstants.BIO).equals("yes")) {
						name = matcher.group(2) + " +> " + matcher.group(5);
					} else {
						name = matcher.group(2) + " -> " + matcher.group(5);
					}
				} else {
					properties.setProperty(GlobalConstants.TYPE,
							GlobalConstants.REPRESSION);
					if (properties.containsKey(GlobalConstants.BIO) && properties.get(GlobalConstants.BIO).equals("yes")) {
						name = matcher.group(2) + " +| " + matcher.group(5);
					} else {
						name = matcher.group(2) + " -| " + matcher.group(5);
					}
				}
			}
			if (properties.getProperty(GlobalConstants.PROMOTER) != null) {
				name = name + ", Promoter "
						+ properties.getProperty(GlobalConstants.PROMOTER);
			} else {
				name = name + ", Promoter " + "default";
			}
			if (!properties.containsKey("label")) {
				String label = properties.getProperty(GlobalConstants.PROMOTER);
				if (label == null) {
					label = "";
				}
				if (properties.containsKey(GlobalConstants.BIO) && properties.get(GlobalConstants.BIO).equals("yes")) {
					label = label + "+";					
				}
				properties.put("label", "\""+label+"\"");
			}
			properties.put(GlobalConstants.NAME, name);
			influences.put(name, properties);
		}
	}

	public void loadDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();
		defaultParameters = new HashMap<String, String>();
		defaultParameters.put(GlobalConstants.KDECAY_STRING,
				biosimrc.get("biosim.gcm.KDECAY_VALUE", ""));
		defaultParameters.put(GlobalConstants.KASSOCIATION_STRING,
				biosimrc.get("biosim.gcm.KASSOCIATION_VALUE", ""));
		defaultParameters.put(GlobalConstants.KBIO_STRING,
				biosimrc.get("biosim.gcm.KBIO_VALUE", ""));
		defaultParameters.put(GlobalConstants.COOPERATIVITY_STRING,
				biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", ""));
		defaultParameters.put(GlobalConstants.KREP_STRING,
				biosimrc.get("biosim.gcm.KREP_VALUE", ""));
		defaultParameters.put(GlobalConstants.KACT_STRING,
				biosimrc.get("biosim.gcm.KACT_VALUE", ""));
		defaultParameters.put(GlobalConstants.RNAP_BINDING_STRING,
				biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", ""));
		defaultParameters.put(GlobalConstants.RNAP_STRING,
				biosimrc.get("biosim.gcm.RNAP_VALUE", ""));
		defaultParameters.put(GlobalConstants.OCR_STRING,
				biosimrc.get("biosim.gcm.OCR_VALUE", ""));
		defaultParameters.put(GlobalConstants.KBASAL_STRING,
				biosimrc.get("biosim.gcm.KBASAL_VALUE", ""));
		defaultParameters.put(GlobalConstants.PROMOTER_COUNT_STRING,
				biosimrc.get("biosim.gcm.PROMOTER_COUNT_VALUE", ""));
		defaultParameters.put(GlobalConstants.STOICHIOMETRY_STRING,
				biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", ""));
		defaultParameters.put(GlobalConstants.ACTIVED_STRING,
				biosimrc.get("biosim.gcm.ACTIVED_VALUE", ""));
		defaultParameters.put(GlobalConstants.MAX_DIMER_STRING,
				biosimrc.get("biosim.gcm.MAX_DIMER_VALUE", ""));
		defaultParameters.put(GlobalConstants.INITIAL_STRING,
				biosimrc.get("biosim.gcm.INITIAL_VALUE", ""));

		for (String s : defaultParameters.keySet()) {
			parameters.put(s, defaultParameters.get(s));
		}

	}

	
	private String checkCompabilitySave(String key) {
		if (key.equals(GlobalConstants.MAX_DIMER_STRING)) {
			return "maxDimer";
		}
		return key;
	}
	
	private String checkCompabilityLoad(String key) {
		if (key.equals("maxDimer")) {
			return GlobalConstants.MAX_DIMER_STRING;
		}
		return key;
	}	

	private static final String NETWORK = "digraph\\sG\\s\\{([^}]*)\\s\\}";

	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	private static final String REACTION = "(^|\\n) *([^ \\n]*) (\\-|\\+)(\\>|\\|) *([^ \n]*) *\\[([^\\]]*)]";

	// private static final String PARSE = "(^|\\n) *([^ \\n,]*) *\\-\\> *([^
	// \n,]*)";

	private static final String PARSE = "(^|\\n) *([^ \\n,]*) (\\-|\\+)(\\>|\\|) *([^ \n,]*), Promoter ([a-zA-Z\\d_]+)";

	private static final String PROPERTY = "([a-zA-Z\\ \\-]+)=(\"([^\"]*)\"|([^\\s,]+))";

	private static final String GLOBAL = "Global\\s\\{([^}]*)\\s\\}";
	
	private static final String SBMLFILE = GlobalConstants.SBMLFILE + "=\"([^\"]*)\"";
	
	private static final String DIMABS = GlobalConstants.DIMABS + "=(true|false)";

	private static final String BIOABS = GlobalConstants.BIOABS + "=(true|false)";

	private static final String PROMOTERS_LIST = "Promoters\\s\\{([^}]*)\\s\\}";
	
	private static final String COMPONENTS_LIST = "Components\\s\\{([^}]*)\\s\\}";
	
	private String sbmlFile = "";
	
	private boolean dimAbs = false;

	private boolean bioAbs = false;

	private HashMap<String, Properties> species;

	private HashMap<String, Properties> influences;

	private HashMap<String, Properties> promoters;
	
	private HashMap<String, Properties> components;

	private HashMap<String, String> parameters;

	private HashMap<String, String> defaultParameters;

	private HashMap<String, String> globalParameters;
}
