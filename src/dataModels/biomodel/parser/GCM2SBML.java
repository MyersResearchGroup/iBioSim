package dataModels.biomodel.parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.comp.Port;

import dataModels.biomodel.util.SBMLutilities;
import dataModels.util.GlobalConstants;


public class GCM2SBML {
	
	public GCM2SBML(BioModel gcm) {
		species = new HashMap<String, Properties>();
		reactions = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		parameters = new HashMap<String, String>();
		loadDefaultParameters();
		this.bioModel = gcm;
	}
	

	/**
	 * load the GCM file from a buffer.
	 */
	private void loadFromBuffer(StringBuffer data) {
		
		species = new HashMap<String, Properties>();
		reactions = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		parameters = new HashMap<String, String>();
		//grid = new Grid();
		
		loadDefaultParameters();
		
		try {
			parseStates(data);
			parseInfluences(data);
			parseGlobal(data);
			parsePromoters(data);
			parseSBMLFile(data);
			parseConditions(data);
			parseGridSize(data);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		while (filename.endsWith(".temp")) {
			filename = filename.substring(0, filename.length() - 5);
		}
		//this.filename = filename;

		StringBuffer data = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}
		loadFromBuffer(data);

	}
	//PARSE METHODS
	
	private void parseStates(StringBuffer data) {
		Pattern network = Pattern.compile(NETWORK);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		if (matcher.find()) {
			matcher = pattern.matcher(matcher.group(1));
		}
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				String prop = CompatibilityFixer.convertOLDName(propMatcher.group(1));
				if (propMatcher.group(3) != null) {
					properties.put(prop, propMatcher.group(3));
				}
				else {
					properties.put(prop, propMatcher.group(4));
				}
			}
			// for backwards compatibility
			if (properties.containsKey("const")) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.INPUT);
			}
			else if (!properties.containsKey(GlobalConstants.TYPE)) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.OUTPUT);
			}
			if (properties.getProperty(GlobalConstants.TYPE).equals("constant")) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.INPUT);
			}
			if (properties.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.CONSTANT)) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.INPUT);
			}
			if (properties.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.NORMAL)) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.OUTPUT);
			}

			// for backwards compatibility
			if (!properties.containsKey("ID") && properties.containsKey("label")) {
				properties.put(GlobalConstants.ID, properties.getProperty("label")
						.replace("\"", ""));
			}
			if (properties.containsKey("gcm")) {
				if (properties.containsKey(GlobalConstants.TYPE)) {
					properties.remove(GlobalConstants.TYPE);
				}
				properties.put("gcm", properties.getProperty("gcm").replace("\"", ""));
				components.put(name, properties);
			}
			else if (properties.containsKey("shape") && properties.getProperty("shape").equals("circle")){
				if (properties.containsKey(GlobalConstants.TYPE)) {
					properties.remove(GlobalConstants.TYPE);
				}
				reactions.put(name, properties);
			} else {
				species.put(name, properties);
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
				if (matcher.group(1).equals("compartment")) {
					if (matcher.group(2).equals("true")) {
						isWithinCompartment=true;
					} else {
						isWithinCompartment=false;
					}
					continue;
				}
				if (matcher.group(1).equals("compartmentId")) {
					//enclosingCompartment = matcher.group(2);
				} else {
					String prop = CompatibilityFixer.convertOLDName(matcher.group(1));
					if (matcher.group(3) != null) {
						//globalParameters.put(prop, matcher.group(3));
						parameters.put(prop, matcher.group(3));
					}
					else {
						//globalParameters.put(prop, matcher.group(4));
						parameters.put(prop, matcher.group(4));
					}
				}
			}
		}
	}

	private void parseConditions(StringBuffer data) {
		Pattern pattern = Pattern.compile(CONDITION);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			String s = matcher.group(1).trim();
			for (String cond : s.split("\n")) {
				if (!cond.equals(""))
					addCondition(cond.trim());
			}
		}
	}
	
	private String addCondition(String condition) {
		conditions.add(condition);
		return condition;
	}

	/*
	private void removeCondition(String condition) {
		conditions.remove(condition);
	}

	private ArrayList<String> getConditions() {
		return conditions;
	}
	*/
	
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
				String prop = CompatibilityFixer.convertOLDName(propMatcher.group(1));
				if (propMatcher.group(3) != null) {
					properties.put(prop, propMatcher.group(3));
				}
				else {
					properties.put(prop, propMatcher.group(4));
				}
			}
			promoters.put(name, properties);
		}
	}

	private void parseSBMLFile(StringBuffer data) {
		Pattern network = Pattern.compile(SBMLFILE);
		Matcher matcher = network.matcher(data.toString());
		if (!matcher.find()) return;
		sbmlFile = matcher.group(1);
	}
	
	/**
	 * loads the grid size from the gcm file
	 * 
	 * @param data string data from a gcm file
	 */
	private void parseGridSize(StringBuffer data) {
		Pattern network = Pattern.compile(GRID);
		Matcher matcher = network.matcher(data.toString());
		
		numRows = -1;
		numCols = -1;
		
		if (!matcher.find()) return;
		
		String info = matcher.group(1);
		
		if (info != null) {
			
			String[] rowcolInfo = info.split("\n");
			
			String[] rowInfo = rowcolInfo[1].split("=");
			String[] colInfo = rowcolInfo[2].split("=");
			
			String row = rowInfo[1];
			String col = colInfo[1];
			numRows = Integer.parseInt(row);
			numCols = Integer.parseInt(col);
		}
	}
	
	/*
	 * private void parseComponents(StringBuffer data) { Pattern network =
	 * Pattern.compile(COMPONENTS_LIST); Matcher matcher =
	 * network.matcher(data.toString()); Pattern pattern =
	 * Pattern.compile(STATE); Pattern propPattern = Pattern.compile(PROPERTY);
	 * if (!matcher.find()) { return; } matcher =
	 * pattern.matcher(matcher.group(1)); while (matcher.find()) { String name =
	 * matcher.group(2); Matcher propMatcher =
	 * propPattern.matcher(matcher.group(3)); Properties properties = new
	 * Properties(); while (propMatcher.find()) { if
	 * (propMatcher.group(3)!=null) { properties.put(propMatcher.group(1),
	 * propMatcher.group(3)); } else { properties.put(propMatcher.group(1),
	 * propMatcher.group(4)); } } components.put(name, properties); } }
	 */
	private boolean parseInfluences(StringBuffer data) {
		Pattern pattern = Pattern.compile(REACTION);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		HashMap<String, ArrayList<String>> actBioMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, Properties> actBioPropMap = new HashMap<String, Properties>();
		HashMap<String, ArrayList<String>> repBioMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, Properties> repBioPropMap = new HashMap<String, Properties>();
		ArrayList<Properties> actDimerList = new ArrayList<Properties>();
		ArrayList<Properties> repDimerList = new ArrayList<Properties>();
		boolean complexConversion = false;
		while (matcher.find()) {
			Matcher propMatcher = propPattern.matcher(matcher.group(6));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				String prop = CompatibilityFixer.convertOLDName(checkCompabilityLoad(propMatcher.group(1)));
				prop = prop.replace(" ","");
				if (propMatcher.group(3) != null) {
					properties.setProperty(prop, propMatcher.group(3));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(3))) {
						promoters.put(propMatcher.group(3).replaceAll("\"", ""), new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(3)
								.replace("\"", "")); // for backwards
						// compatibility
					}
				}
				else {
					properties.put(prop, propMatcher.group(4));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(4))) {
						promoters.put(propMatcher.group(4).replaceAll("\"", ""), new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(4)
								.replace("\"", "")); // for backwards
						// compatibility
					}
				}
			}

			if (properties.containsKey("port")) {
				if (components.containsKey(matcher.group(2))) {
					components.get(matcher.group(2)).put(properties.get("port"), matcher.group(5));
					if (properties.containsKey("type")) {
						components.get(matcher.group(2)).put("type_" + properties.get("port"),
								properties.get("type"));
					}
					else {
						/*
						GCMFile file = new GCMFile(path);
						file.load(path + separator
								+ components.get(matcher.group(2)).getProperty("gcm"));
						if (((String)file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE))
								.contains(GlobalConstants.INPUT)) {
							components.get(matcher.group(2)).put("type_" + properties.get("port"),
									"Input");
						}
						else if (((String)file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE))
								.contains(GlobalConstants.OUTPUT)) {
							components.get(matcher.group(2)).put("type_" + properties.get("port"),
									"Output");
						}
						*/
					}
				}
				else {
					components.get(matcher.group(5)).put(properties.get("port"), matcher.group(2));
					if (properties.containsKey("type")) {
						components.get(matcher.group(5)).put("type_" + properties.get("port"),
								properties.get("type"));
					}
					else {
						/*
						GCMFile file = new GCMFile(path);
						file.load(path + separator + components.get(matcher.group(5)).getProperty("gcm"));
						if (file.getSpecies().get(properties.get("port")) != null) {
							if (((String)file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE))
									.contains(GlobalConstants.INPUT)) {
								components.get(matcher.group(5)).put("type_" + properties.get("port"),"Input");
							}
							else if (((String)file.getSpecies().get(properties.get("port")).get(
									GlobalConstants.TYPE)).contains(GlobalConstants.OUTPUT)) {
								components.get(matcher.group(5)).put("type_" + properties.get("port"),"Output");
							}
						}
						*/
					}
				}
			}
			else {
				String name = "";
				int nDimer = -1;/*Integer
						.parseInt(getProp(properties, GlobalConstants.MAX_DIMER_STRING));*/
				if (properties.containsKey("arrowhead")) {
					if (properties.getProperty("arrowhead").indexOf("vee") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.ACTIVATION);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							complexConversion = true;
							String promoter = properties.getProperty(GlobalConstants.PROMOTER);
							if (actBioMap.containsKey(promoter)) {
								ArrayList<String> parts = actBioMap.get(promoter);
								parts.add(matcher.group(2));
							}
							else {
								ArrayList<String> parts = new ArrayList<String>();
								parts.add(matcher.group(2));
								actBioMap.put(promoter, parts);
							}
							properties.setProperty(GlobalConstants.GENE_PRODUCT, matcher.group(5));
							actBioPropMap.put(promoter, properties);
						}
						else if (nDimer > 1) {
							complexConversion = true;
							properties.setProperty(GlobalConstants.TRANSCRIPTION_FACTOR, matcher.group(2));
							properties.setProperty(GlobalConstants.GENE_PRODUCT, matcher.group(5));
							actDimerList.add(properties);
						}
						else {
							name = matcher.group(2) + " -> " + matcher.group(5);
						}
					}
					else if (properties.getProperty("arrowhead").indexOf("tee") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.REPRESSION);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							complexConversion = true;
							String promoter = properties.getProperty(GlobalConstants.PROMOTER);
							if (repBioMap.containsKey(promoter)) {
								ArrayList<String> parts = repBioMap.get(promoter);
								parts.add(matcher.group(2));
							}
							else {
								ArrayList<String> parts = new ArrayList<String>();
								parts.add(matcher.group(2));
								repBioMap.put(promoter, parts);
							}
							properties.setProperty(GlobalConstants.GENE_PRODUCT, matcher.group(5));
							repBioPropMap.put(promoter, properties);
						}
						else if (nDimer > 1) {
							complexConversion = true;
							properties.setProperty(GlobalConstants.TRANSCRIPTION_FACTOR, matcher.group(2));
							properties.setProperty(GlobalConstants.GENE_PRODUCT, matcher.group(5));
							repDimerList.add(properties);
						}
						else {
							name = matcher.group(2) + " -| " + matcher.group(5);
						}
					}
					else if (properties.getProperty("arrowhead").indexOf("plus") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.COMPLEX);
						name = matcher.group(2) + " +> " + matcher.group(5);
					}
					else {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.NOINFLUENCE);
						name = matcher.group(2) + " x> " + matcher.group(5);
					}
				}
				if (!complexConversion) {
					if (properties.containsKey(GlobalConstants.PROMOTER)) {
						name = name + ", Promoter "
								+ properties.getProperty(GlobalConstants.PROMOTER);
					}
					else if (properties.getProperty(GlobalConstants.TYPE).equals("complex") || 
							properties.getProperty(GlobalConstants.TYPE).equals("no influence")){
						name = name + ", Promoter " + "none";
					}
					//Instantiates default promoter
					else {
						String defaultPromoterName = "Promoter_" + getOutput(name);
						name = name + ", Promoter " + defaultPromoterName;
						Properties prom_prop = new Properties();
						promoters.put(defaultPromoterName, prom_prop);
						//createPromoter(defaultPromoterName, 0, 0, false);
						properties.setProperty(GlobalConstants.PROMOTER, defaultPromoterName);
					}
					String label = properties.getProperty(GlobalConstants.PROMOTER);
					if (label == null) {
						label = "";
					}
					// if (properties.containsKey(GlobalConstants.BIO)
					// && properties.get(GlobalConstants.BIO).equals("yes")) {
					// label = label + "+";
					// }
					properties.setProperty("label", "\"" + label + "\"");
					properties.setProperty(GlobalConstants.NAME, name);
					influences.put(name, properties);
				}
			}
		}
		// Parses mapped biochemical activation influences and adds them to the
		// gcm as complex influences
		parseBioInfluences(repBioMap, repBioPropMap);
		parseBioInfluences(actBioMap, actBioPropMap);
		// Parses collected dimer activation influences and adds them to the gcm
		// as complex influences
		parseDimerInfluences(repDimerList);
		parseDimerInfluences(actDimerList);
		// Removes local and global instances of old bio/dimer parameters from
		// gcm file
		for (Properties inflProp : influences.values()) {
			inflProp.remove(GlobalConstants.BIO);
			//inflProp.remove(GlobalConstants.KBIO_STRING);
			inflProp.remove(GlobalConstants.MAX_DIMER_STRING);
		}
		for (Properties specProp : species.values()) {
			specProp.remove(GlobalConstants.KASSOCIATION_STRING);
		}
		//globalParameters.remove(GlobalConstants.KBIO_STRING);
		//globalParameters.remove(GlobalConstants.MAX_DIMER_STRING);
		//globalParameters.remove(GlobalConstants.KASSOCIATION_STRING);

		return complexConversion;
	}

	private String getParameter(String parameter) {
		if (parameters.containsKey(parameter)) {
			return parameters.get(parameter);
		}
		return null;
	}

	private String getProp(Properties props, String prop) {
		if (props.containsKey(prop))
			return props.getProperty(prop);
		return getParameter(prop);
	}

	private static String checkCompabilityLoad(String key) {
		if (key.equals("maxDimer")) {
			return GlobalConstants.MAX_DIMER_STRING;
		}
		return key;
	}

	// Parses mapped biochemical repression influences and adds them to the gcm
	// as complex influences
	private void parseBioInfluences(HashMap<String, ArrayList<String>> bioMap,
			HashMap<String, Properties> bioPropMap) {
		for (String promoter : bioMap.keySet()) {
			// Adds activation or repression influence with complex species as
			// input
			String complex = "";
			ArrayList<String> parts = bioMap.get(promoter);
			for (String part : parts) {
				complex = complex + part;
			}
			Properties inflProp = bioPropMap.get(promoter);
			String influence = complex;
			if (inflProp.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION))
				influence = influence + " -> ";
			else
				influence = influence + " -| ";
			influence = influence + inflProp.getProperty(GlobalConstants.GENE_PRODUCT)
					+ ", Promoter ";
			if (inflProp.containsKey(GlobalConstants.PROMOTER)) {
				influence = influence + inflProp.getProperty(GlobalConstants.PROMOTER);
			} 
			String label = inflProp.getProperty(GlobalConstants.PROMOTER);
			label = "\"" + label + "\"";
			inflProp.setProperty("label", label);
			inflProp.setProperty(GlobalConstants.NAME, influence);
			influences.put(influence, inflProp);
			// Adds complex species
			Properties compProp = new Properties();
			compProp.setProperty(GlobalConstants.NAME, complex);
			compProp.setProperty(GlobalConstants.TYPE, GlobalConstants.INTERNAL);
//			compProp.setProperty(GlobalConstants.KCOMPLEX_STRING, getProp(inflProp, GlobalConstants.KBIO_STRING));
			compProp.setProperty(GlobalConstants.INITIAL_STRING, "0");
			compProp.setProperty(GlobalConstants.KDECAY_STRING, "0");
			species.put(complex, compProp);
			// Adds complex formation influences with biochemical species as
			// inputs
			for (String part : parts) {
				String compInfluence = part + " +> " + complex;
				Properties compFormProp = new Properties();
				compFormProp.setProperty("label", "\"\"");
				compFormProp.setProperty(GlobalConstants.NAME, compInfluence);
				compFormProp.setProperty(GlobalConstants.TYPE, GlobalConstants.COMPLEX);
				compFormProp.setProperty(GlobalConstants.COOPERATIVITY_STRING, "1");
				influences.put(compInfluence, compFormProp);
			}
		}
	}

	private void parseDimerInfluences(ArrayList<Properties> dimerList) {
		for (Properties inflProp : dimerList) {
			// Adds activation or repression influence with complex species as
			// input
			String monomer = getProp(inflProp, GlobalConstants.TRANSCRIPTION_FACTOR);
			String nDimer = getProp(inflProp, GlobalConstants.MAX_DIMER_STRING);
			String complex = monomer + "_" + nDimer;
			String influence = complex;
			if (inflProp.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION))
				influence = influence + " -> ";
			else
				influence = influence + " -| ";
			influence = influence + inflProp.getProperty(GlobalConstants.GENE_PRODUCT)
					+ ", Promoter ";
			if (inflProp.containsKey(GlobalConstants.PROMOTER)) {
				
				influence = influence + inflProp.getProperty(GlobalConstants.PROMOTER);
			} 
			String label = inflProp.getProperty(GlobalConstants.PROMOTER);
			label = "\"" + label + "\"";
			inflProp.put(GlobalConstants.NAME, influence);
			influences.put(influence, inflProp);
			// Adds complex species
			Properties compProp = new Properties();
			compProp.setProperty(GlobalConstants.NAME, complex);
			compProp.setProperty(GlobalConstants.TYPE, GlobalConstants.INTERNAL);
			compProp.setProperty(GlobalConstants.KCOMPLEX_STRING, getProp(species.get(monomer),
					GlobalConstants.KASSOCIATION_STRING));
			compProp.setProperty(GlobalConstants.INITIAL_STRING, "0");
			compProp.setProperty(GlobalConstants.KDECAY_STRING, "0");
			species.put(complex, compProp);
			// Adds complex formation influence with monomer as input
			String compInfluence = monomer + " +> " + complex;
			Properties compFormProp = new Properties();
			compFormProp.setProperty("label", "\"\"");
			compFormProp.setProperty(GlobalConstants.NAME, compInfluence);
			compFormProp.setProperty(GlobalConstants.TYPE, GlobalConstants.COMPLEX);
			compFormProp.setProperty(GlobalConstants.COOPERATIVITY_STRING, nDimer);
			influences.put(compInfluence, compFormProp);
		}
	}

	private void loadDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();

		HashMap<String, String> defaultParameters;

		defaultParameters = new HashMap<String, String>();
		
		defaultParameters.put(GlobalConstants.FORWARD_KREP_STRING, biosimrc.get("biosim.gcm.KREP_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_KREP_STRING, "1");
		
			defaultParameters.put(GlobalConstants.FORWARD_KACT_STRING, biosimrc.get("biosim.gcm.KACT_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_KACT_STRING, "1");

			defaultParameters.put(GlobalConstants.FORWARD_KCOMPLEX_STRING, biosimrc.get("biosim.gcm.KCOMPLEX_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_KCOMPLEX_STRING, "1");

			defaultParameters.put(GlobalConstants.FORWARD_RNAP_BINDING_STRING, biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_RNAP_BINDING_STRING, "1");

			defaultParameters.put(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING, 
				biosimrc.get("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING, "1");

		defaultParameters.put(GlobalConstants.FORWARD_MEMDIFF_STRING, biosimrc.get("biosim.gcm.FORWARD_MEMDIFF_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_MEMDIFF_STRING, biosimrc.get("biosim.gcm.REVERSE_MEMDIFF_VALUE", ""));

		defaultParameters.put(GlobalConstants.KDECAY_STRING, biosimrc.get("biosim.gcm.KDECAY_VALUE", ""));
		defaultParameters.put(GlobalConstants.KECDECAY_STRING, biosimrc.get("biosim.gcm.KECDECAY_VALUE", ""));
		defaultParameters.put(GlobalConstants.COOPERATIVITY_STRING, biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", ""));
		defaultParameters.put(GlobalConstants.RNAP_STRING, biosimrc.get("biosim.gcm.RNAP_VALUE", ""));
		defaultParameters.put(GlobalConstants.OCR_STRING, biosimrc.get("biosim.gcm.OCR_VALUE", ""));
		defaultParameters.put(GlobalConstants.KBASAL_STRING, biosimrc.get("biosim.gcm.KBASAL_VALUE", ""));
		defaultParameters.put(GlobalConstants.PROMOTER_COUNT_STRING, biosimrc.get("biosim.gcm.PROMOTER_COUNT_VALUE", ""));
		defaultParameters.put(GlobalConstants.STOICHIOMETRY_STRING, biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", ""));
		defaultParameters.put(GlobalConstants.ACTIVATED_STRING, biosimrc.get("biosim.gcm.ACTIVED_VALUE", ""));
		defaultParameters.put(GlobalConstants.KECDIFF_STRING, biosimrc.get("biosim.gcm.KECDIFF_VALUE", ""));
	
		for (String s : defaultParameters.keySet()) {
			parameters.put(s, defaultParameters.get(s));
		}

	}

	private static String getInput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(2);
	}

	/*
	private String getArrow(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(3) + matcher.group(4);
	}
	*/

	private static String getOutput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(5);
	}

	/*
	private String getPromoter(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(6);
	}
	*/
	
	//REMOVAL METHODS
	/**
	 * erases everything in the model but doesn't touch anything file-related
	 */
	/*
	private void clear() {
		
		species = new HashMap<String, Properties>();
		reactions = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		//globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		isWithinCompartment = false;
		grid = new Grid();
		loadDefaultParameters();
	}
	*/
	
	/**
	 * Save the contents to a StringBuffer. Can later be written to a file or
	 * other stream.
	 * 
	 * @return
	 * @throws IOException 
	 * @throws XMLStreamException 
	 */
	public void convertGCM2SBML(String root, String fileName) throws XMLStreamException, IOException {
		String filename = root + GlobalConstants.separator + fileName;
		int condCnt = 0;
		for (String s : conditions) {
			bioModel.createCondition(s,condCnt);
			condCnt++;
		}
		for (String global : parameters.keySet()) {
			bioModel.createGlobalParameter(global, parameters.get(global));
		}
		for (String s : species.keySet()) {
			bioModel.createSpeciesFromGCM(s, species.get(s)); 
		}
		for (String s : promoters.keySet()) {
			bioModel.createPromoterFromGCM(s, promoters.get(s)); 
		}
		for (String s : influences.keySet()) {
			Properties prop = influences.get(s);
			String promoter = prop.getProperty(GlobalConstants.PROMOTER);
			if (promoters.get(promoter)==null) {
				continue;
			}
			if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION)) {
				bioModel.addActivatorToProductionReaction(promoter,getInput(s),getOutput(s),
						promoters.get(promoter).getProperty(GlobalConstants.STOICHIOMETRY_STRING),
						prop.getProperty(GlobalConstants.COOPERATIVITY_STRING),
						prop.getProperty(GlobalConstants.KACT_STRING));
			}
			else if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.REPRESSION)) {
				bioModel.addRepressorToProductionReaction(promoter,getInput(s),getOutput(s),
						promoters.get(promoter).getProperty(GlobalConstants.STOICHIOMETRY_STRING),
						prop.getProperty(GlobalConstants.COOPERATIVITY_STRING),prop.getProperty(GlobalConstants.KREP_STRING));
			}
			else if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.NOINFLUENCE)) {
				if (promoter==null) {
					promoter = bioModel.createPromoter(null,0, 0, false);
					bioModel.createProductionReaction(promoter,null,null,null,null,null,null,false,null);
				}
				bioModel.addNoInfluenceToProductionReaction(promoter,getInput(s),getOutput(s));
			}
			else if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.COMPLEX)) {
				String KcStr = null;
				if (species.get(getOutput(s))!=null) {
					KcStr = species.get(getOutput(s)).getProperty(GlobalConstants.KCOMPLEX_STRING);
				}
				bioModel.addReactantToComplexReaction(getInput(s),getOutput(s), KcStr, 
						prop.getProperty(GlobalConstants.COOPERATIVITY_STRING));
			} 
		}
		if (isWithinCompartment) {
			//gcm.setIsWithinCompartment(true);
			//gcm.setDefaultCompartment(enclosingCompartment);
		} else {
			//gcm.setIsWithinCompartment(false);
			//gcm.setDefaultCompartment("");
			for (int j = 0; j < bioModel.getSBMLDocument().getModel().getCompartmentCount(); j++) {
				Compartment compartment = bioModel.getSBMLDocument().getModel().getCompartment(j);
				Port port = bioModel.getSBMLCompModel().getPort(GlobalConstants.COMPARTMENT+"__"+compartment.getId());
				if (port==null) {
					port = bioModel.getSBMLCompModel().createPort();
					port.setId(GlobalConstants.COMPARTMENT+"__"+compartment.getId());
				}
				port.setIdRef(compartment.getId());
			}
		}
		bioModel.getLayout();
		for (String s : species.keySet()) {
			Properties prop = species.get(s);
			if (prop.containsKey("graphx") && prop.containsKey("graphy") &&
					prop.containsKey("graphheight")&&prop.containsKey("graphwidth")) {
				bioModel.placeSpecies(s,Double.parseDouble(prop.getProperty("graphx")),Double.parseDouble(prop.getProperty("graphy")),
						Double.parseDouble(prop.getProperty("graphheight")),Double.parseDouble(prop.getProperty("graphwidth")));
			}
		}
		for (String s : promoters.keySet()) {
			Properties prop = promoters.get(s);
			if (prop.containsKey("graphx") && prop.containsKey("graphy") &&
					prop.containsKey("graphheight")&&prop.containsKey("graphwidth")) {
				bioModel.placeSpecies(s,Double.parseDouble(prop.getProperty("graphx")),Double.parseDouble(prop.getProperty("graphy")),
						Double.parseDouble(prop.getProperty("graphheight")),Double.parseDouble(prop.getProperty("graphwidth")));
			}
		}
		for (String s : reactions.keySet()) {
			Properties prop = reactions.get(s);
			if (prop.containsKey("graphx") && prop.containsKey("graphy") &&
					prop.containsKey("graphheight")&&prop.containsKey("graphwidth")) {
				bioModel.placeReaction(s,Double.parseDouble(prop.getProperty("graphx")),Double.parseDouble(prop.getProperty("graphy")),
						Double.parseDouble(prop.getProperty("graphheight")),Double.parseDouble(prop.getProperty("graphwidth")));
			}
		}
		for (String s : components.keySet()) {
			Properties prop = components.get(s);
			if (prop.containsKey("graphx") && prop.containsKey("graphy") &&
					prop.containsKey("graphheight")&&prop.containsKey("graphwidth")) {
				bioModel.placeCompartment(s,Double.parseDouble(prop.getProperty("graphx")),Double.parseDouble(prop.getProperty("graphy")),
						Double.parseDouble(prop.getProperty("graphheight")),Double.parseDouble(prop.getProperty("graphwidth")));
			}
		}
		bioModel.updateLayoutDimensions();
		
		if (numRows > 0 || numCols > 0) {
			//modelEditor.buildGrid();
			bioModel.setGridSize(numRows,numCols);
		}
		
		bioModel.createCompPlugin();
		for (String s : components.keySet()) {
			bioModel.createComponentFromGCM(s,components.get(s));
		}
		if (sbmlFile!=null && !sbmlFile.equals("")) {
			SBMLDocument document = SBMLutilities.readSBML(root + GlobalConstants.separator + sbmlFile);
			if (document!=null) {
				Model model = document.getModel();
				Model modelNew = bioModel.getSBMLDocument().getModel();
				for (int i = 0; i < model.getParameterCount(); i++) {
					if (modelNew.getParameter(model.getParameter(i).getId())==null) {
						modelNew.addParameter(model.getParameter(i).clone());
					}
				}
				for (int i = 0; i < model.getEventCount(); i++) {
					modelNew.addEvent(model.getEvent(i).clone());
				}
				new File(root + GlobalConstants.separator + sbmlFile).delete();
			}
		}
		new File(filename).delete();
	}
	
	//CONSTANTS AND VARIABLES

	private static final String NETWORK = "digraph\\sG\\s\\{([^}]*)\\s\\}";

	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	private static final String REACTION = "(^|\\n) *([^ \\n]*) (\\-|\\+|x)(\\>|\\|) *([^ \n]*) *\\[([^\\]]*)]";

	// private static final String PARSE = "(^|\\n) *([^ \\n,]*) *\\-\\> *([^
	// \n,]*)";

	private static final String PARSE = "(^|\\n) *([^ \\n,]*) (\\-|\\+|x)(\\>|\\|) *([^ \n,]*)(, Promoter ([a-zA-Z\\d_]+))?";

	private static final String PROPERTY = "([a-zA-Z\\ \\-]+)=(\"([^\"]*)\"|([^\\s,]+))";

	private static final String GLOBAL = "Global\\s\\{([^}]*)\\s\\}";

	private static final String CONDITION = "Conditions\\s\\{([^@]*)\\s\\}";

	private static final String SBMLFILE = GlobalConstants.SBMLFILE + "=\"([^\"]*)\"";

	//private static final String SBML = "SBML=((.*)\n)*";

	private static final String PROMOTERS_LIST = "Promoters\\s\\{([^}]*)\\s\\}";
	
	private static final String GRID = "Grid\\s\\{([^}]*)\\s\\}";

	// private static final String COMPONENTS_LIST =
	// "Components\\s\\{([^}]*)\\s\\}";

	//private String separator;

	//private String filename = null;

	private String sbmlFile = "";
	
	private HashMap<String, Properties> species;

	private HashMap<String, Properties> reactions;

	private HashMap<String, Properties> influences;

	private HashMap<String, Properties> promoters;

	private HashMap<String, Properties> components;

	private ArrayList<String> conditions;

	private HashMap<String, String> parameters;
	
	//private Grid grid = null;
	
	private boolean isWithinCompartment = false;
	
	//private String enclosingCompartment = "";
	
	//private String path;
	
	private BioModel bioModel;
	
	private int numRows;
	
	private int numCols;
}
