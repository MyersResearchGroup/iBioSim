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
		gcm = new GCMFile();
		gcm.load(filename);
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

	public GeneticNetwork buildNetwork() {
		HashMap<String, Properties> speciesMap = gcm.getSpecies();
		HashMap<String, Properties> reactionMap = gcm.getInfluences();
		HashMap<String, Properties> promoterMap = gcm.getPromoters();

		species = new HashMap<String, SpeciesInterface>();
		stateMap = new HashMap<String, SpeciesInterface>();
		promoters = new HashMap<String, Promoter>();

		for (String s : speciesMap.keySet()) {
			SpeciesInterface specie = parseSpeciesData(s, speciesMap.get(s));
			species.put(specie.getName(), specie);
			stateMap.put(specie.getStateName(), specie);
		}
		
		for (String s : reactionMap.keySet()) {
			Reaction reaction = parseReactionData(s, reactionMap.get(s));			
		}
		
		for (String s : promoterMap.keySet()) {
			promoters.get(s).addProperties(promoterMap.get(s));
		}
		
		GeneticNetwork network = new GeneticNetwork(species, stateMap,
				promoters);
		return network;
	}

	public void printFile() {
		System.out.println(data.toString());
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
	private Reaction parseReactionData(String reaction, Properties property) {

		String promoterName = "";
		Promoter promoter = null;
		Reaction r = new Reaction();		
		r.generateName();		

		if (property.containsKey(GlobalConstants.PROMOTER)) {
			promoterName = property.getProperty(GlobalConstants.PROMOTER);
		} else {
			promoterName = "Promoter_" + gcm.getOutput(reaction);
		}

		// Check if promoter exists. If not, create it.
		if (promoters.containsKey(promoterName)) {
			promoter = promoters.get(promoterName);
		} else {
			promoter = new Promoter();
			promoter.setName(promoterName);
			promoters.put(promoter.getName(), promoter);
		}

		if (property.containsKey(GlobalConstants.BIO)) {
			Utility.print(debug, "GCMParser: Biochemical");
			r.setBiochemical(true);
		}
		
		r.setInputState(gcm.getInput(reaction));
		r.setOutputState(gcm.getOutput(reaction));
		if (property.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION)) {
			r.setType("vee");
			if (property.containsKey(GlobalConstants.KACT_STRING)) {
				r.setBindingConstant(Double.parseDouble(property.getProperty(GlobalConstants.KACT_STRING)));
			}		
		} else {
			r.setType("tee");
			if (property.containsKey(GlobalConstants.KREP_STRING)) {
				r.setBindingConstant(Double.parseDouble(property.getProperty(GlobalConstants.KREP_STRING)));
			}					
		}
		promoter.addReaction(r);
		return r;
	}
	

	/**
	 * Parses the data and put it into the species
	 * 
	 * @param name
	 *            the name of the species
	 * @param properties
	 *            the properties of the species
	 */
	private SpeciesInterface parseSpeciesData(String name, Properties property) {
		SpeciesInterface species = null;

		if (property.getProperty(GlobalConstants.TYPE).equals(
				GlobalConstants.CONSTANT)) {
			species = new ConstantSpecies();
		} else if (property.getProperty(GlobalConstants.TYPE).equals(
				GlobalConstants.SPASTIC)) {
			species = new SpasticSpecies();
		} else {
			species = new BaseSpecies();
		}

		species.setName(property.getProperty(GlobalConstants.NAME));
		species.setStateName(property.getProperty(GlobalConstants.NAME));
		species.setProperties(property);
		return species;
	}

	// Holds the text of the GCM
	private StringBuffer data = null;

	private HashMap<String, SpeciesInterface> species = null;

	// StateMap, species
	private HashMap<String, SpeciesInterface> stateMap = null;

	private HashMap<String, Promoter> promoters = null;

	private GCMFile gcm = null;

	// A regex that matches information
	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	private static final String REACTION = "(^|\\n) *([^ \\n]*) *\\-\\> *([^ \n]*) *\\[(.*)arrowhead=([^,\\]]*)(.*)";

	private static final String PROPERTY_NUMBER = "([a-zA-Z]+)=\"([\\d]*[\\.\\d]?\\d+)\"";

	// private static final String PROPERTY_STATE = "([a-zA-Z]+)=([^\\s,.\"]+)";

	// private static final String PROPERTY_QUOTE =
	// "([a-zA-Z]+)=\"([^\\s,.\"]+)\"";

	private static final String PROPERTY_STATE = "([a-zA-Z\\s\\-]+)=([^\\s,]+)";

	// Debug level
	private boolean debug = false;
}
