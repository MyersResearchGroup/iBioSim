package gcm2sbml.parser;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.Reaction;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses a genetic circuit model.
 * 
 * @author Nam Nguyen
 * 
 */
public class GCMParser {

	public GCMParser(String filename) {
		this(filename, false);
	}

	public GCMParser(String filename, boolean debug) {
		this.debug = debug;
		data = new StringBuffer();
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
	}

	public void printFile() {
		System.out.println(data.toString());
	}

	public GeneticNetwork buildNetwork() {
		species = new HashMap<String, SpeciesInterface>();
		stateMap = new HashMap<String, SpeciesInterface>();
		promoters = new HashMap<String, Promoter>();
		parseFile();
		GeneticNetwork network = new GeneticNetwork(species, stateMap,
				promoters);
		return network;
	}

	/**
	 * Parses the file to build the genetic network
	 * 
	 */
	private void parseFile() {
		parseStates();
		parseReactions();
	}

	/**
	 * This function parses the states, determining all the base species in the
	 * network.
	 * 
	 */
	private void parseStates() {
		Pattern pattern = Pattern.compile(STATE);
		Matcher matcher = pattern.matcher(data.toString());
		while (matcher.find()) {
			Utility
					.print(debug, "GCMParser: I found the text \""
							+ matcher.group().trim() + "\" starting at "
							+ "index " + matcher.start()
							+ " and ending at index " + matcher.end());
			Utility.print(debug, "GCMParser: Group 2: " + matcher.group(2));
			Utility.print(debug, "GCMParser: Group 3: " + matcher.group(3));

			SpeciesInterface specie = parseSpeciesData(matcher.group(3),
					matcher.group(2));
			species.put(specie.getName(), specie);
			stateMap.put(specie.getStateName(), specie);
		}
	}

	/**
	 * Parses reactions.
	 * 
	 */
	private void parseReactions() {
		Pattern pattern = Pattern.compile(REACTION);
		Matcher matcher = pattern.matcher(data.toString());
		while (matcher.find()) {
			Utility
					.print(debug, "GCMParser: I found the text \""
							+ matcher.group().trim() + "\" starting at "
							+ "index " + matcher.start()
							+ " and ending at index " + matcher.end());
			Utility.print(debug, "GCMParser: Group 2: " + matcher.group(2));
			Utility.print(debug, "GCMParser: Group 3: " + matcher.group(3));
			Utility.print(debug, "GCMParser: Group 5: " + matcher.group(5));
			String stateNameInput = matcher.group(2);
			String stateNameOutput = matcher.group(3);
			String type = matcher.group(5);
			String totalInfo = matcher.group(4) + " " + matcher.group(6);
			Utility.print(debug, "GCMParser: " + totalInfo);
			Reaction r = parseReactionData(totalInfo, stateNameInput,
					stateNameOutput, type);
		}

	}

	/**
	 * Parses the data and put it into the species
	 * 
	 * @param species
	 *            the species to fill
	 * @param info
	 *            the information to parse from
	 */
	// TODO: Match decay rates, dimerization rates
	private SpeciesInterface parseSpeciesData(String info, String stateName) {
		SpeciesInterface species = null;
		Pattern number_pattern = Pattern.compile(PROPERTY_NUMBER);
		Matcher matcher = number_pattern.matcher(info);
		Properties number_property = new Properties();
		while (matcher.find()) {
			number_property.put(matcher.group(1), matcher.group(2));
		}

		Pattern quote_pattern = Pattern.compile(PROPERTY_QUOTE);
		matcher = quote_pattern.matcher(info);
		Properties label_property = new Properties();
		while (matcher.find()) {
			label_property.put(matcher.group(1), matcher.group(2));
		}
		if (label_property.getProperty(GlobalConstants.TYPE).indexOf("spastic") != -1) {
			Utility.print(debug, "GCMParser: Found spastic species "
					+ label_property.getProperty(GlobalConstants.NAME));
			species = new SpasticSpecies();
		} else if (label_property.getProperty(GlobalConstants.TYPE).indexOf(
				"constant") != -1) {
			Utility.print(debug, "GCMParser: Found constant species "
					+ label_property.getProperty(GlobalConstants.NAME));
			species = new ConstantSpecies();
		} else {
			Utility.print(debug, "GCMParser: Found base species "
					+ label_property.getProperty(GlobalConstants.NAME));
			species = new BaseSpecies();
		}

		species.setName(label_property.getProperty(GlobalConstants.NAME));
		species.setStateName(stateName);
		species.setNumberProperties(number_property);
		species.setLabelProperties(label_property);
		if (number_property.containsKey(GlobalConstants.MAX_DIMER_STRING)) {
			species.setMaxDimer(Integer.parseInt(number_property
					.getProperty(GlobalConstants.MAX_DIMER_STRING)));
		}
		if (number_property.containsKey(GlobalConstants.KASSOCIATION_STRING)) {
			species.setDimerizationConstant(Double.parseDouble(number_property
					.getProperty(GlobalConstants.KASSOCIATION_STRING)));
		}
		if (number_property.containsKey(GlobalConstants.KDECAY_STRING)) {
			species.setDimerizationConstant(Double.parseDouble(number_property
					.getProperty(GlobalConstants.KDECAY_STRING)));
		}
		if (number_property.containsKey(GlobalConstants.INITIAL_STRING)) {
			species.setInitial(Double.parseDouble(number_property
					.getProperty(GlobalConstants.INITIAL_STRING)));
		}
		// Pattern dimer = Pattern.compile(MAX_DIMER);
		// matcher = dimer.matcher(info);
		// if (matcher.find()) {
		// Utility.print(debug, "GCMParser: Max dimer " + matcher.group(1));
		// species.setMaxDimer(Integer.parseInt(matcher.group(1)));
		// }
		return species;
	}

	/**
	 * Parses the reactions in the network
	 * 
	 * @param reaction
	 *            the reaction to parse
	 * @param stateNameOutput
	 *            the name of the output
	 * 
	 */
	// TODO: Match rate constants
	private Reaction parseReactionData(String reaction, String stateNameInput,
			String stateNameOutput, String type) {
		Pattern number_pattern = Pattern.compile(PROPERTY_NUMBER);
		Matcher matcher = number_pattern.matcher(reaction);
		Properties number_property = new Properties();
		while (matcher.find()) {
			number_property.put(matcher.group(1), matcher.group(2));
		}

		Pattern label_pattern = Pattern.compile(PROPERTY_QUOTE);
		matcher = label_pattern.matcher(reaction);
		Properties quote_property = new Properties();
		while (matcher.find()) {
			quote_property.put(matcher.group(1), matcher.group(2));
		}

		String promoterName = "";
		Promoter promoter = null;
		Reaction r = new Reaction();
		r.generateName();

		if (quote_property.containsKey(GlobalConstants.PROMOTER)) {
			promoterName = quote_property.getProperty(GlobalConstants.PROMOTER);
		} else {
			promoterName = "Promoter_" + stateNameOutput;
		}

		// Check if promoter exists. If not, create it.
		if (promoters.containsKey(promoterName)) {
			promoter = promoters.get(promoterName);
		} else {
			promoter = new Promoter();
			promoter.setName(promoterName);
			promoters.put(promoter.getName(), promoter);
		}

		if (quote_property.containsKey(GlobalConstants.BIO)) {
			Utility.print(debug, "GCMParser: Biochemical");
			r.setBiochemical(true);
		}

		// if (number_property.containsKey(GlobalConstants.COOP)) {
		// r.setCoop(Double.parseDouble(number_property.get(Reaction.COOP)
		// .toString()));
		// Utility.print(debug, "GCMParser: Coop: " + r.getCoop());
		// }

		if (number_property.containsKey(GlobalConstants.MAX_DIMER_STRING)) {
			r.setDimer(Integer.parseInt(number_property.get(
					GlobalConstants.MAX_DIMER_STRING).toString()));
			Utility.print(debug, "GCMParser: Dimer: " + r.getDimer());
		}

		r.setInputState(stateNameInput);
		r.setOutputState(stateNameOutput);
		r.setType(type);
		promoter.addReaction(r);
		return r;
	}

	// Holds the text of the GCM
	private StringBuffer data = null;

	private HashMap<String, SpeciesInterface> species = null;

	// StateMap, species
	private HashMap<String, SpeciesInterface> stateMap = null;

	private HashMap<String, Promoter> promoters = null;

	// A regex that matches information
	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	private static final String REACTION = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*) *\\[(.*)arrowhead=([^,\\]]*)(.*)";

	private static final String PROPERTY_NUMBER = "([a-zA-Z]+)=\"([\\d]*[\\.\\d]?\\d+)\"";

	// private static final String PROPERTY_STATE = "([a-zA-Z]+)=([^\\s,.\"]+)";

	// private static final String PROPERTY_QUOTE =
	// "([a-zA-Z]+)=\"([^\\s,.\"]+)\"";

	private static final String PROPERTY_QUOTE = "([a-zA-Z]+)=([^\\s,.\"]+)";

	// Debug level
	private boolean debug = false;
}
