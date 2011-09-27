package gcm.parser;

import gcm.network.BaseSpecies;
import gcm.network.ComplexSpecies;
import gcm.network.ConstantSpecies;
import gcm.network.DiffusibleConstitutiveSpecies;
import gcm.network.DiffusibleSpecies;
import gcm.network.GeneticNetwork;
import gcm.network.Promoter;
import gcm.network.Influence;
import gcm.network.SpasticSpecies;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.sbml.libsbml.SBMLWriter;

import sbol.SbolSynthesizer;

/**
 * This class parses a genetic circuit model.
 * 
 * @author Nam Nguyen
 * 
 */
public class GCMParser {
	
	private String separator;

	public GCMParser(String filename) {
		this(filename, false);
	}

	public GCMParser(String filename, boolean debug) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.debug = debug;
		gcm = new GCMFile(filename.substring(0, filename.length()
				- filename.split(separator)[filename.split(separator).length - 1]
						.length()));
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
	
	public GCMParser(GCMFile gcm, boolean debug) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.debug = debug;
		this.gcm = gcm;
	}

	public GeneticNetwork buildNetwork() {
		org.sbml.libsbml.SBMLDocument sbml = gcm.flattenGCM(true);
		return buildTopLevelNetwork(sbml);
	}
	
	public GeneticNetwork buildTopLevelNetwork(org.sbml.libsbml.SBMLDocument sbml) {
		HashMap<String, Properties> speciesMap = gcm.getSpecies();
		HashMap<String, Properties> reactionMap = gcm.getInfluences();
		HashMap<String, Properties> promoterMap = gcm.getPromoters();

		species = new HashMap<String, SpeciesInterface>();
		promoters = new HashMap<String, Promoter>();
		complexMap = new HashMap<String, ArrayList<Influence>>();
		partsMap = new HashMap<String, ArrayList<Influence>>();

		for (String s : speciesMap.keySet()) {
			SpeciesInterface specie = parseSpeciesData(s, speciesMap.get(s));
			species.put(specie.getId(), specie);
		}
		
		for (String s : promoterMap.keySet()) {
			parsePromoterData(s, promoterMap.get(s));	
		}
		
		for (String s : reactionMap.keySet()) {
			parseReactionData(s, reactionMap.get(s));			
		}
		
		GeneticNetwork network = new GeneticNetwork(species, complexMap, partsMap, promoters, gcm);
		
		network.setSBMLFile(gcm.getSBMLFile());
		if (sbml != null) {
			network.setSBML(sbml);
		}
		return network;		
	}

	public void printFile() {
		System.out.println(data.toString());
	}

	public HashMap<String, SpeciesInterface> getSpecies() {
		return species;
	}

	public void setSpecies(HashMap<String, SpeciesInterface> species) {
		this.species = species;
	}

	public HashMap<String, Promoter> getPromoters() {
		return promoters;
	}

	public void setPromoters(HashMap<String, Promoter> promoters) {
		this.promoters = promoters;
	}

	private Promoter parsePromoterData(String promoterID, Properties property) {
		Promoter p = new Promoter();
		p.setId(promoterID);
		promoters.put(promoterID, p);
		
		if (property != null && property.containsKey(GlobalConstants.PROMOTER_COUNT_STRING)) {
			p.addProperty(GlobalConstants.PROMOTER_COUNT_STRING, property.getProperty(GlobalConstants.PROMOTER_COUNT_STRING));
		} else {
			p.addProperty(GlobalConstants.PROMOTER_COUNT_STRING, gcm.getParameter(GlobalConstants.PROMOTER_COUNT_STRING));
		} 
		
		if (property != null && property.containsKey(GlobalConstants.ACTIVED_STRING)) {
			p.addProperty(GlobalConstants.ACTIVED_STRING, property.getProperty(GlobalConstants.ACTIVED_STRING));
		} else {
			p.addProperty(GlobalConstants.ACTIVED_STRING, gcm.getParameter(GlobalConstants.ACTIVED_STRING));
		} 
		
		if (property != null && property.containsKey(GlobalConstants.STOICHIOMETRY_STRING)) {
			p.addProperty(GlobalConstants.STOICHIOMETRY_STRING, property.getProperty(GlobalConstants.STOICHIOMETRY_STRING));
		} else {
			p.addProperty(GlobalConstants.STOICHIOMETRY_STRING, gcm.getParameter(GlobalConstants.STOICHIOMETRY_STRING));
		} 
		
		if (property != null && property.containsKey(GlobalConstants.OCR_STRING)) {
			p.addProperty(GlobalConstants.OCR_STRING, property.getProperty(GlobalConstants.OCR_STRING));
		} else {
			p.addProperty(GlobalConstants.OCR_STRING, gcm.getParameter(GlobalConstants.OCR_STRING));
		} 
		
		if (property != null && property.containsKey(GlobalConstants.KBASAL_STRING)) {
			p.addProperty(GlobalConstants.KBASAL_STRING, property.getProperty(GlobalConstants.KBASAL_STRING));
		} else {
			p.addProperty(GlobalConstants.KBASAL_STRING, gcm.getParameter(GlobalConstants.KBASAL_STRING));
		} 
		

		if (property != null && property.containsKey(GlobalConstants.RNAP_BINDING_STRING)) {
			p.addProperty(GlobalConstants.RNAP_BINDING_STRING, property.getProperty(GlobalConstants.RNAP_BINDING_STRING));
		} else {
			p.addProperty(GlobalConstants.RNAP_BINDING_STRING, gcm.getParameter(GlobalConstants.RNAP_BINDING_STRING));
		} 
		
		if (property != null && property.containsKey(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING)) {
			p.addProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, property.getProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING));
		} else {
			p.addProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, gcm.getParameter(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING));
		} 
		return p;
		
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
	private void parseReactionData(String reaction, Properties property) {
		Influence infl = new Influence();		
		infl.generateName();		
		
		if (property.containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
			infl.addProperty(GlobalConstants.COOPERATIVITY_STRING, property.getProperty(GlobalConstants.COOPERATIVITY_STRING));
		} else {
			infl.addProperty(GlobalConstants.COOPERATIVITY_STRING, gcm.getParameter(GlobalConstants.COOPERATIVITY_STRING));
		} 
		
		if (property.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION)) {
			infl.setType("vee");
			if (property.containsKey(GlobalConstants.KACT_STRING)) {
				infl.addProperty(GlobalConstants.KACT_STRING, property.getProperty(GlobalConstants.KACT_STRING));
			} else {
				infl.addProperty(GlobalConstants.KACT_STRING, gcm.getParameter(GlobalConstants.KACT_STRING));
			} 
		} else if (property.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.REPRESSION)) {
			infl.setType("tee");
			if (property.containsKey(GlobalConstants.KREP_STRING)) {
				infl.addProperty(GlobalConstants.KREP_STRING, property.getProperty(GlobalConstants.KREP_STRING));
			} else {
				infl.addProperty(GlobalConstants.KREP_STRING, gcm.getParameter(GlobalConstants.KREP_STRING));
			} 	
		} else if (property.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.COMPLEX)) {
			infl.setType("plus");
		}
		else {
			infl.setType("dot");
		}	
		
		String input = GCMFile.getInput(reaction);
		String output = GCMFile.getOutput(reaction);
		infl.setInput(input);
		infl.setOutput(output);
		if (infl.getType().equals("plus")) {
			//Maps complex species to complex formation influences of which they're outputs
			ArrayList<Influence> complexInfl = null;
			if (complexMap.containsKey(output)) {
				complexInfl = complexMap.get(output);
			} else { 
				complexInfl = new ArrayList<Influence>();
				complexMap.put(output, complexInfl);
			}
			complexInfl.add(infl);
			//Maps part species to complex formation influences of which they're inputs
			complexInfl = null;
			if (partsMap.containsKey(input)) {
				complexInfl = partsMap.get(input);
			} else { 
				complexInfl = new ArrayList<Influence>();
				partsMap.put(input, complexInfl);
			}
			complexInfl.add(infl);
		} else if (!infl.getType().equals("dot")) {	
			String promoterName = property.getProperty(GlobalConstants.PROMOTER);
			Promoter p = promoters.get(promoterName);
			if (!input.equals("none")) {
				p.addToReactionMap(input, infl);
				if (infl.getType().equals("vee")) {
					p.addActivator(input, species.get(input));
					species.get(input).setActivator(true);
				} else {
					p.addRepressor(input, species.get(input));
					species.get(input).setRepressor(true);
				}
			}
			if (!output.equals("none"))
				p.addOutput(output,species.get(output));
		}
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
		
		SpeciesInterface specie = null;

		if (property.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.CONSTANT)) {
			specie = new ConstantSpecies();
		} 
		else if (property.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE) &&
				property.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.SPASTIC)) {
			specie = new DiffusibleConstitutiveSpecies();
		} 
		else if (property.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.DIFFUSIBLE)) {
			specie = new DiffusibleSpecies();
		} 
		else if (property.getProperty(GlobalConstants.TYPE).contains(GlobalConstants.SPASTIC)) {
			specie = new SpasticSpecies();
		}
		else {
			specie = new BaseSpecies();
		}

		if (property.containsKey(GlobalConstants.KCOMPLEX_STRING)) {
			specie.addProperty(GlobalConstants.KCOMPLEX_STRING, property.getProperty(GlobalConstants.KCOMPLEX_STRING));
		} else {
			specie.addProperty(GlobalConstants.KCOMPLEX_STRING, gcm.getParameter(GlobalConstants.KCOMPLEX_STRING));
		}
		
		if (property.containsKey(GlobalConstants.KASSOCIATION_STRING)) {
			specie.addProperty(GlobalConstants.KASSOCIATION_STRING, property.getProperty(GlobalConstants.KASSOCIATION_STRING));
		} else {
			specie.addProperty(GlobalConstants.KASSOCIATION_STRING, gcm.getParameter(GlobalConstants.KASSOCIATION_STRING));
		}

		if (property.containsKey(GlobalConstants.INITIAL_STRING)) {
			specie.addProperty(GlobalConstants.INITIAL_STRING, property.getProperty(GlobalConstants.INITIAL_STRING));
		} else {
			specie.addProperty(GlobalConstants.INITIAL_STRING, gcm.getParameter(GlobalConstants.INITIAL_STRING));
		}

		if (property.containsKey(GlobalConstants.KDECAY_STRING)) {
			specie.addProperty(GlobalConstants.KDECAY_STRING, property.getProperty(GlobalConstants.KDECAY_STRING));
		} else {
			specie.addProperty(GlobalConstants.KDECAY_STRING, gcm.getParameter(GlobalConstants.KDECAY_STRING));
		}
		
		if (property.containsKey(GlobalConstants.MEMDIFF_STRING)) {
			specie.addProperty(GlobalConstants.MEMDIFF_STRING, property.getProperty(GlobalConstants.MEMDIFF_STRING));
		} else {
			specie.addProperty(GlobalConstants.MEMDIFF_STRING, gcm.getParameter(GlobalConstants.MEMDIFF_STRING));
		}
		
		if (property.containsKey(GlobalConstants.KECDIFF_STRING)) {
			specie.addProperty(GlobalConstants.KECDIFF_STRING, property.getProperty(GlobalConstants.KECDIFF_STRING));
		} else {
			specie.addProperty(GlobalConstants.KECDIFF_STRING, gcm.getParameter(GlobalConstants.KECDIFF_STRING));
		}
		
		if (property.containsKey(GlobalConstants.KECDECAY_STRING)) {
			specie.addProperty(GlobalConstants.KECDECAY_STRING, property.getProperty(GlobalConstants.KECDECAY_STRING));
		} else {
			specie.addProperty(GlobalConstants.KECDECAY_STRING, gcm.getParameter(GlobalConstants.KECDECAY_STRING));
		}
		
		if (property.containsKey(GlobalConstants.TYPE)) {
			specie.addProperty(GlobalConstants.TYPE, property.getProperty(GlobalConstants.TYPE));
		} else {
			specie.addProperty(GlobalConstants.TYPE, gcm.getParameter(GlobalConstants.TYPE));
		}
		
		specie.setId(property.getProperty(GlobalConstants.ID));
		specie.setName(property.getProperty(GlobalConstants.NAME,
				property.getProperty(GlobalConstants.ID)));
		specie.setStateName(property.getProperty(GlobalConstants.ID));
		
		return specie;
	}
	
	public SbolSynthesizer buildSbolSynthesizer() {
		HashMap<String, Properties> speciesMap = gcm.getSpecies();
		HashMap<String, Properties> reactionMap = gcm.getInfluences();
		HashMap<String, Properties> promoterMap = gcm.getPromoters();

		species = new HashMap<String, SpeciesInterface>();
		promoters = new HashMap<String, Promoter>();
		
		for (String sId : speciesMap.keySet()) {
			SpeciesInterface s = new BaseSpecies();
			s.setId(sId);
			Properties sProp = speciesMap.get(sId);
			if (sProp.containsKey(GlobalConstants.SBOL_RBS))
				s.addProperty(GlobalConstants.SBOL_RBS, sProp.getProperty(GlobalConstants.SBOL_RBS));
			if (sProp.containsKey(GlobalConstants.SBOL_ORF))
				s.addProperty(GlobalConstants.SBOL_ORF, sProp.getProperty(GlobalConstants.SBOL_ORF));
			species.put(sId, s);
		}
		
		for (String pId : promoterMap.keySet()) {
			Promoter p = new Promoter();
			p.setId(pId);
			Properties pProp = promoterMap.get(pId);
			if (pProp.containsKey(GlobalConstants.SBOL_PROMOTER))
				p.addProperty(GlobalConstants.SBOL_PROMOTER, pProp.getProperty(GlobalConstants.SBOL_PROMOTER));
			if (pProp.containsKey(GlobalConstants.SBOL_TERMINATOR))
				p.addProperty(GlobalConstants.SBOL_TERMINATOR, pProp.getProperty(GlobalConstants.SBOL_TERMINATOR));
			promoters.put(pId, p);
		}
		
		for (String rId : reactionMap.keySet()) {
			if (reactionMap.get(rId).containsKey(GlobalConstants.PROMOTER)) {
				Promoter p = promoters.get(reactionMap.get(rId).getProperty(GlobalConstants.PROMOTER));
				String output = GCMFile.getOutput(rId);
				if (!output.equals("none"))
					p.addOutput(output, species.get(output));
			}
		}
		
		SbolSynthesizer synthesizer = new SbolSynthesizer(promoters);
		return synthesizer;
	}

	public void setParameters(HashMap<String, String> parameters) {
		gcm.setParameters(parameters);
	}
	
	// Holds the text of the GCM
	private StringBuffer data = null;

	private HashMap<String, SpeciesInterface> species;

	private HashMap<String, Promoter> promoters;
	
	private HashMap<String, ArrayList<Influence>> complexMap;
	private HashMap<String, ArrayList<Influence>> partsMap;

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
