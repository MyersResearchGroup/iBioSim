package gcm2sbml.network;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;
import gcm2sbml.visitor.PrintActivatedBindingVisitor;
import gcm2sbml.visitor.PrintActivatedProductionVisitor;
import gcm2sbml.visitor.PrintBiochemicalVisitor;
import gcm2sbml.visitor.PrintDecaySpeciesVisitor;
import gcm2sbml.visitor.PrintDimerizationVisitor;
import gcm2sbml.visitor.PrintRepressionBindingVisitor;
import gcm2sbml.visitor.PrintSpeciesVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;

/**
 * This class represents a genetic network
 * 
 * @author Nam
 * 
 */
public class GeneticNetwork {
	/**
	 * Constructor
	 * 
	 * @param species
	 *            a hashmap of species
	 * @param stateMap
	 *            a hashmap of statename to species name
	 * @param promoters
	 *            a hashmap of promoters
	 */
	public GeneticNetwork(HashMap<String, SpeciesInterface> species,
			HashMap<String, SpeciesInterface> stateMap,
			HashMap<String, Promoter> promoters) {
		this(species, stateMap, promoters, null);
	}

	/**
	 * Constructor
	 * 
	 * @param species
	 *            a hashmap of species
	 * @param stateMap
	 *            a hashmap of statename to species name
	 * @param promoters
	 *            a hashmap of promoters
	 * @param gcm
	 *            a gcm file containing extra information
	 */
	public GeneticNetwork(HashMap<String, SpeciesInterface> species,
			HashMap<String, SpeciesInterface> stateMap,
			HashMap<String, Promoter> promoters, GCMFile gcm) {
		this.species = species;
		this.stateMap = stateMap;
		this.promoters = promoters;
		this.properties = gcm;
		
		initialize();
	}		

	/**
	 * Loads in a properties file
	 * 
	 * @param filename
	 *            the file to load
	 */
	public void loadProperties(GCMFile gcm) {
		properties = gcm;
	}
	
	public void setSBMLFile(String file) {
		sbmlDocument = file;
	}

	/**
	 * Outputs the network to an SBML file
	 * 
	 * @param filename
	 * @return the sbml document
	 */
	public SBMLDocument outputSBML(String filename) {
		SBMLDocument document = new SBMLDocument(2, 3);
		currentDocument = document;
		Model m = document.createModel();
		document.setModel(m);
		Utility.addCompartments(document, compartment);
		document.getModel().getCompartment(compartment).setSize(1);
		outputSBML(filename, document);
		return document;
	}

	public void outputSBML(String filename, SBMLDocument document) {
		try {
			Model m = document.getModel();
			checkConsistancy(document);
			SBMLWriter writer = new SBMLWriter();
			printSpecies(document);
			printPromoters(document);
			printRNAP(document);
			printDecay(document);
			// System.out.println(counter++);
			checkConsistancy(document);
			if (!dimerizationAbstraction) {
				printDimerization(document);
			}
			if (!biochemicalAbstraction) {
				printBiochemical(document);
			}
			// System.out.println(counter++);
			checkConsistancy(document);
			printPromoterProduction(document);
			// System.out.println(counter++);
			checkConsistancy(document);
			printPromoterBinding(document);
			// System.out.println(counter++);
			checkConsistancy(document);
			PrintStream p = new PrintStream(new FileOutputStream(filename));

			m.setName("Created from " + new File(filename).getName().replace("sbml", "gcm"));
			m.setId(new File(filename).getName().replace(".gcm", ""));			

			p.print(writer.writeToString(document));

			p.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to output to SBML");
		}
	}

	/**
	 * Merges an SBML file network to an SBML file
	 * 
	 * @param filename
	 * @return the sbml document
	 */
	public void mergeSBML(String filename) {
		try {			
			if (sbmlDocument.equals("")) {
				outputSBML(filename);
				return;
			}

			SBMLDocument document = new SBMLReader().readSBML(currentRoot + sbmlDocument);
			checkConsistancy(document);
			currentDocument = document;
			outputSBML(filename, document);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to output to SBML");
		}
	}

	/**
	 * Prints each promoter binding
	 * 
	 * @param document
	 *            the SBMLDocument to print to
	 */
	private void printPromoterBinding(SBMLDocument document) {
		double rnap = .033;
		double rep = .05;
		double act = .0033;
		double kdimer = .05;
		double kbio = .05;
		double kcoop = 1;
		double dimer = 1;

		if (properties != null) {
			kbio = Double.parseDouble(properties
					.getParameter(GlobalConstants.KBIO_STRING));
			kdimer = Double.parseDouble(properties
					.getParameter(GlobalConstants.KASSOCIATION_STRING));
			rnap = Double.parseDouble(properties
					.getParameter(GlobalConstants.RNAP_BINDING_STRING));
			rep = Double.parseDouble(properties
					.getParameter(GlobalConstants.KREP_STRING));
			act = Double.parseDouble(properties
					.getParameter(GlobalConstants.KACT_STRING));
			kcoop = Double.parseDouble(properties
					.getParameter(GlobalConstants.COOPERATIVITY_STRING));
			dimer = Double.parseDouble(properties
					.getParameter(GlobalConstants.MAX_DIMER_STRING));

		}

		for (Promoter p : promoters.values()) {
			// First setup RNAP binding
			org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(
					"R_RNAP_" + p.getName());
			r.addReactant(new SpeciesReference("RNAP", 1));
			r.addReactant(new SpeciesReference(p.getName(), 1));
			r.addProduct(new SpeciesReference("RNAP_" + p.getName()));
			r.setReversible(true);
			KineticLaw kl = new KineticLaw();
			kl.addParameter(new Parameter("kf", rnap, getMoleTimeParameter(2)));
			kl.addParameter(new Parameter("kr", 1, getMoleTimeParameter(1)));
			kl.setFormula("kf*" + "RNAP*" + p.getName() + "-kr*RNAP_"
					+ p.getName());
			r.setKineticLaw(kl);
			Utility.addReaction(document, r);

			// Next setup activated binding
			PrintActivatedBindingVisitor v = new PrintActivatedBindingVisitor(
					document, p, p.getActivators(), act, kdimer, kcoop, kbio,
					dimer);
			v.setBiochemicalAbstraction(biochemicalAbstraction);
			v.setDimerizationAbstraction(dimerizationAbstraction);
			v.setCooperationAbstraction(cooperationAbstraction);
			v.run();

			// Next setup repression binding
			p.getRepressors();
			PrintRepressionBindingVisitor v2 = new PrintRepressionBindingVisitor(
					document, p, p.getRepressors(), rep, kdimer, kcoop, kbio,
					dimer);
			v2.setBiochemicalAbstraction(biochemicalAbstraction);
			v2.setDimerizationAbstraction(dimerizationAbstraction);
			v2.setCooperationAbstraction(cooperationAbstraction);
			v2.run();
		}

	}

	/**
	 * Prints each promoter production values
	 * 
	 * @param document
	 *            the SBMLDocument to print to
	 */
	private void printPromoterProduction(SBMLDocument document) {
		double basal = .0001;
		double koc = .25;
		int stoc = 1;
		double act = .25;
		if (properties != null) {
			basal = Double.parseDouble(properties
					.getParameter(GlobalConstants.KBASAL_STRING));
			koc = Double.parseDouble(properties
					.getParameter(GlobalConstants.OCR_STRING));
			stoc = Integer.parseInt(properties
					.getParameter(GlobalConstants.STOICHIOMETRY_STRING));
			act = Double.parseDouble(properties
					.getParameter(GlobalConstants.ACTIVED_STRING));
		}

		for (Promoter p : promoters.values()) {
			if (p.getActivators().size() > 0 && p.getRepressors().size() == 0) {
				org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(
						"R_basal_production_" + p.getName());
				r.addReactant(new SpeciesReference("RNAP_" + p.getName(), 1));
				for (SpeciesInterface species : p.getOutputs()) {
					r.addProduct(new SpeciesReference(species.getName(), stoc));
				}
				r.addProduct(new SpeciesReference("RNAP_" + p.getName(), 1));
				r.setReversible(false);
				r.setFast(false);
				KineticLaw kl = new KineticLaw();
				if (p.getProperty(GlobalConstants.KBASAL_STRING) != null) {
					kl.addParameter(new Parameter("basal", Double.parseDouble(p
							.getProperty(GlobalConstants.KBASAL_STRING)),
							getMoleTimeParameter(1)));
				} else {
					kl.addParameter(new Parameter("basal", basal,
							getMoleTimeParameter(1)));
				}
				kl.setFormula("basal*" + "RNAP_" + p.getName());
				r.setKineticLaw(kl);
				Utility.addReaction(document, r);

				PrintActivatedProductionVisitor v = new PrintActivatedProductionVisitor(
						document, p, p.getActivators(), act, stoc);
				v.run();
			} else if (p.getActivators().size() == 0
					&& p.getRepressors().size() > 0) {
				org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(
						"R_production_" + p.getName());
				r.addReactant(new SpeciesReference("RNAP_" + p.getName(), 1));
				for (SpeciesInterface species : p.getOutputs()) {
					r.addProduct(new SpeciesReference(species.getName(), stoc));
				}
				r.addProduct(new SpeciesReference("RNAP_" + p.getName(), 1));
				r.setReversible(false);
				r.setFast(false);
				KineticLaw kl = new KineticLaw();
				if (p.getProperty(GlobalConstants.OCR_STRING) != null) {
					kl.addParameter(new Parameter("koc", Double.parseDouble(p
							.getProperty(GlobalConstants.OCR_STRING)),
							getMoleTimeParameter(1)));
				} else {
					kl.addParameter(new Parameter("koc", koc,
							getMoleTimeParameter(1)));
				}
				kl.setFormula("koc*" + "RNAP_" + p.getName());
				r.setKineticLaw(kl);
				Utility.addReaction(document, r);
			} else {
				// TODO: Should ask Chris how to handle
				// Both activated and repressed
				org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(
						"R_basal_production_" + p.getName());
				r.addReactant(new SpeciesReference("RNAP_" + p.getName(), 1));
				for (SpeciesInterface species : p.getOutputs()) {
					r.addProduct(new SpeciesReference(species.getName(), stoc));
				}
				r.addProduct(new SpeciesReference("RNAP_" + p.getName(), 1));
				r.setReversible(false);
				r.setFast(false);
				KineticLaw kl = new KineticLaw();
				if (p.getProperty(GlobalConstants.KBASAL_STRING) != null) {
					kl.addParameter(new Parameter("basal", Double.parseDouble(p
							.getProperty(GlobalConstants.KBASAL_STRING)),
							getMoleTimeParameter(1)));
				} else {
					kl.addParameter(new Parameter("basal", basal,
							getMoleTimeParameter(1)));
				}
				kl.setFormula("basal*" + "RNAP_" + p.getName());
				r.setKineticLaw(kl);
				Utility.addReaction(document, r);

				PrintActivatedProductionVisitor v = new PrintActivatedProductionVisitor(
						document, p, p.getActivators(), act, stoc);
				v.run();
			}
		}
	}

	/**
	 * Prints the decay reactions
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printDecay(SBMLDocument document) {
		// Check to see if number of promoters is a property, if not, default to
		// 1
		double decay = .0075;
		if (properties != null) {
			decay = Double.parseDouble(properties
					.getParameter(GlobalConstants.KDECAY_STRING));
		}

		PrintDecaySpeciesVisitor visitor = new PrintDecaySpeciesVisitor(
				document, species.values(), decay);
		visitor.setBiochemicalAbstraction(biochemicalAbstraction);
		visitor.setDimerizationAbstraction(dimerizationAbstraction);
		visitor.run();
	}

	/**
	 * Prints the dimerization reactions
	 * 
	 * @param document
	 *            the SBML document to print to
	 */
	private void printDimerization(SBMLDocument document) {
		double kdimer = .05;
		double dimer = 2;
		if (properties != null) {
			kdimer = Double.parseDouble(properties
					.getParameter(GlobalConstants.KASSOCIATION_STRING));
			dimer = Double.parseDouble(properties
					.getParameter(GlobalConstants.MAX_DIMER_STRING));

		}
		PrintDimerizationVisitor visitor = new PrintDimerizationVisitor(
				document, species.values(), kdimer, dimer);
		visitor.setBiochemicalAbstraction(biochemicalAbstraction);
		visitor.setDimerizationAbstraction(dimerizationAbstraction);
		visitor.run();
	}

	private void printBiochemical(SBMLDocument document) {
		double kbio = .05;
		if (properties != null) {
			kbio = Double.parseDouble(properties
					.getParameter(GlobalConstants.KBIO_STRING));
		}
		PrintBiochemicalVisitor visitor = new PrintBiochemicalVisitor(document,
				species.values(), kbio);
		visitor.setBiochemicalAbstraction(biochemicalAbstraction);
		visitor.setDimerizationAbstraction(dimerizationAbstraction);
		visitor.run();
	}

	/**
	 * Prints the species in the network
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printSpecies(SBMLDocument document) {
		double init = 1;
		if (properties != null) {
			init = Double.parseDouble(properties
					.getParameter(GlobalConstants.INITIAL_STRING));
		}
		PrintSpeciesVisitor visitor = new PrintSpeciesVisitor(document, species
				.values(), compartment, init);
		visitor.setBiochemicalAbstraction(biochemicalAbstraction);
		visitor.setDimerizationAbstraction(dimerizationAbstraction);
		visitor.run();
	}

	/**
	 * Prints the promoters in the network
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printPromoters(SBMLDocument document) {
		// Check to see if number of promoters is a property, if not, default to
		// 1
		String numPromoters = "1";
		if (properties != null) {
			numPromoters = properties
					.getParameter(GlobalConstants.PROMOTER_COUNT_STRING);
		}

		for (Promoter promoter : promoters.values()) {
			// First print out the promoter, and promoter bound to RNAP
			String tempPromoters = numPromoters;
			if (promoter.getProperty(GlobalConstants.PROMOTER_COUNT_STRING) != null) {
				tempPromoters = promoter
						.getProperty(GlobalConstants.PROMOTER_COUNT_STRING);
			}
			Species s = Utility.makeSpecies(promoter.getName(), compartment,
					Double.parseDouble(tempPromoters));
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);			
			s = Utility.makeSpecies("RNAP_" + promoter.getName(), compartment,
					0);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
			// Now cycle through all activators and repressors and add those
			// bindings
			for (SpeciesInterface species : promoter.getActivators()) {
				s = Utility.makeSpecies("RNAP_" + promoter.getName() + "_"
						+ species.getName(), compartment, 0);
				s.setHasOnlySubstanceUnits(true);
				Utility.addSpecies(document, s);
			}
			for (SpeciesInterface species : promoter.getRepressors()) {
				s = Utility.makeSpecies("bound_" + promoter.getName() + "_"
						+ species.getName(), compartment, 0);
				s.setHasOnlySubstanceUnits(true);
				Utility.addSpecies(document, s);
			}
		}

	}

	/**
	 * Prints the RNAP molecule to the document
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printRNAP(SBMLDocument document) {
		double rnap = 30;
		if (properties != null) {
			rnap = Double.parseDouble(properties
					.getParameter(GlobalConstants.RNAP_STRING));
		}
		Species s = Utility.makeSpecies("RNAP", compartment, rnap);
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
	}

	/**
	 * Initializes the network
	 * 
	 */
	private void initialize() {
		buildDimers();
		buildPromoters();
		buildBiochemical();
	}

	/**
	 * Configurates the promoters
	 * 
	 */
	private void buildPromoters() {
		for (Promoter promoter : promoters.values()) {
			for (Reaction reaction : promoter.getActivatingReactions()) {
				if (!reaction.isBiochemical() && reaction.getDimer() <= 1) {
					promoter.addActivator(stateMap
							.get(reaction.getInputState()));
					promoter.addToReactionMap(stateMap.get(reaction
							.getInputState()), reaction);
				}
				promoter.addOutput(stateMap.get(reaction.getOutputState()));
			}
			for (Reaction reaction : promoter.getRepressingReactions()) {
				if (!reaction.isBiochemical() && reaction.getDimer() <= 1) {
					promoter.addRepressor(stateMap
							.get(reaction.getInputState()));
					promoter.addToReactionMap(stateMap.get(reaction
							.getInputState()), reaction);
				}
				promoter.addOutput(stateMap.get(reaction.getOutputState()));
			}
		}
	}

	/**
	 * Builds the dimer list from reactions and species and adds it to the
	 * promoter as input.
	 */
	private void buildDimers() {
		// First go through all species list and add all dimers found to hashMap
		HashMap<String, SpeciesInterface> dimers = new HashMap<String, SpeciesInterface>();
		for (SpeciesInterface specie : species.values()) {
			int dimerValue = 1;
			if (properties != null) {
				dimerValue = (int) Integer.parseInt((properties
						.getParameter(GlobalConstants.MAX_DIMER_STRING)));
			}
			if (specie.getProperty(GlobalConstants.MAX_DIMER_STRING) != null) {
				dimerValue = (int) Integer.parseInt(specie
						.getProperty(GlobalConstants.MAX_DIMER_STRING));
			}
			for (int i = 2; i <= dimerValue; i++) {
				DimerSpecies dimer = new DimerSpecies(specie, i);
				dimers.put(dimer.getName(), dimer);
			}
		}
		// Now go through reaction list to see if any are missed
		for (Promoter promoter : promoters.values()) {
			for (Reaction reaction : promoter.getActivatingReactions()) {
				if (reaction.getDimer() > 1) {
					DimerSpecies dimer = new DimerSpecies(stateMap.get(reaction
							.getInputState()), reaction.getDimer());
					dimers.put(dimer.getName(), dimer);
					promoter.addToReactionMap(dimer, reaction);
					promoter.getActivators().add(dimer);
				}
			}
			for (Reaction reaction : promoter.getRepressingReactions()) {
				if (reaction.getDimer() > 1) {
					DimerSpecies dimer = new DimerSpecies(stateMap.get(reaction
							.getInputState()), reaction.getDimer());
					dimers.put(dimer.getName(), dimer);
					promoter.addToReactionMap(dimer, reaction);
					promoter.getRepressors().add(dimer);
				}
			}

		}
		// Now put dimers back into network
		for (SpeciesInterface specie : dimers.values()) {
			species.put(specie.getName(), specie);
		}
	}

	/**
	 * Builds the biochemical species, and adds it to the promoter as input
	 */
	private void buildBiochemical() {
		// Cycle through each promoter
		for (Promoter promoter : promoters.values()) {
			// keep track of all activating and repressing reactions in separate
			// lists
			ArrayList<SpeciesInterface> biochem = new ArrayList<SpeciesInterface>();
			ArrayList<Reaction> reactions = new ArrayList<Reaction>();
			for (Reaction reaction : promoter.getActivatingReactions()) {
				if (reaction.isBiochemical()) {
					reactions.add(reaction);
					biochem.add(stateMap.get(reaction.getInputState()));
				}
			}
			if (biochem.size() == 1) {
				throw new IllegalStateException(
						"Must have more than 1 biochemical reaction");
			} else if (biochem.size() >= 2) {
				BiochemicalSpecies bio = new BiochemicalSpecies(biochem);
				promoter.addActivator(bio);
				for (Reaction reaction : reactions) {
					promoter.addToReactionMap(bio, reaction);
				}
				species.put(bio.getName(), bio);
			}

			biochem = new ArrayList<SpeciesInterface>();
			reactions = new ArrayList<Reaction>();
			for (Reaction reaction : promoter.getRepressingReactions()) {
				if (reaction.isBiochemical()) {
					reactions.add(reaction);
					biochem.add(stateMap.get(reaction.getInputState()));
				}
			}
			if (biochem.size() == 1) {
				throw new IllegalStateException(
						"Must have more than 1 biochemical reaction");
			} else if (biochem.size() >= 2) {
				BiochemicalSpecies bio = new BiochemicalSpecies(biochem);
				for (Reaction reaction : reactions) {
					promoter.addToReactionMap(bio, reaction);
				}
				promoter.addRepressor(bio);
				species.put(bio.getName(), bio);
			}
		}

	}

	/**
	 * Checks the consistancy of the document
	 * 
	 * @param doc
	 *            the SBML document to check
	 */
	private void checkConsistancy(SBMLDocument doc) {
		if (doc.checkConsistency() > 0) {
			for (int i = 0; i < doc.getNumErrors(); i++) {
				System.out.println(doc.getError(i).getMessage());
			}
		}

	}
	
	private String sbmlDocument = "";

	private static SBMLDocument currentDocument = null;
	
	private static String currentRoot = "";

	private boolean biochemicalAbstraction = false;

	private boolean dimerizationAbstraction = false;

	private boolean cooperationAbstraction = false;

	private HashMap<String, SpeciesInterface> species = null;

	private HashMap<String, SpeciesInterface> stateMap = null;

	private HashMap<String, Promoter> promoters = null;

	private GCMFile properties = null;

	private String compartment = "default";

	/**
	 * Returns the curent SBML document being built
	 * 
	 * @return the curent SBML document being built
	 */
	public static SBMLDocument getCurrentDocument() {
		return currentDocument;
	}
	
	/**
	 * Sets the current root
	 * @param root the root directory
	 */
	public static void setRoot(String root) {
		currentRoot = root;
	}	

	public static String getUnitString(ArrayList<String> unitNames,
			ArrayList<Integer> exponents, ArrayList<Integer> multiplier,
			Model model) {

		// First build the name of the unit and see if it exists, start by
		// sorting the units to build a unique string
		for (int i = 0; i < unitNames.size(); i++) {
			for (int j = i; j > 0; j--) {
				if (unitNames.get(j - 1).compareTo(unitNames.get(i)) > 0) {
					Integer tempD = multiplier.get(j);
					Integer tempI = exponents.get(j);
					String tempS = unitNames.get(j);

					multiplier.set(j, multiplier.get(j - 1));
					unitNames.set(j, unitNames.get(j - 1));
					exponents.set(j, exponents.get(j - 1));

					multiplier.set(j - 1, tempD);
					unitNames.set(j - 1, tempS);
					exponents.set(j - 1, tempI);
				}
			}
		}
		UnitDefinition t = new UnitDefinition();
		String name = "u_";
		for (int i = 0; i < unitNames.size(); i++) {
			String sign = "";
			if (exponents.get(i).intValue() < 0) {
				sign = "n";
			}
			name = name + multiplier.get(i) + "_" + unitNames.get(i) + "_"
					+ sign + Math.abs(exponents.get(i)) + "_";
			t.addUnit(new Unit(unitNames.get(i), exponents.get(i).intValue(),
					multiplier.get(i).intValue()));
		}
		name = name.substring(0, name.length() - 1);
		t.setId(name);
		if (model.getUnitDefinition(name) == null) {
			model.addUnitDefinition(t);
		}
		return name;
	}

	/**
	 * Returns a unit name for a parameter based on the number of molecules
	 * involved
	 * 
	 * @param numMolecules
	 *            the number of molecules involved
	 * @return a unit name
	 */
	public static String getMoleTimeParameter(int numMolecules) {
		ArrayList<String> unitS = new ArrayList<String>();
		ArrayList<Integer> unitE = new ArrayList<Integer>();
		ArrayList<Integer> unitM = new ArrayList<Integer>();

		if (numMolecules > 1) {
			unitS.add("mole");
			unitE.add(new Integer(-(numMolecules - 1)));
			unitM.add(new Integer(0));
		}

		unitS.add("second");
		unitE.add(new Integer(-1));
		unitM.add(new Integer(0));

		return GeneticNetwork.getUnitString(unitS, unitE, unitM,
				currentDocument.getModel());
	}

	/**
	 * Returns a unit name for a parameter based on the number of molecules
	 * involved
	 * 
	 * @param numMolecules
	 *            the number of molecules involved
	 * @return a unit name
	 */
	public static String getMoleParameter(int numMolecules) {
		ArrayList<String> unitS = new ArrayList<String>();
		ArrayList<Integer> unitE = new ArrayList<Integer>();
		ArrayList<Integer> unitM = new ArrayList<Integer>();

		unitS.add("mole");
		unitE.add(new Integer(-(numMolecules - 1)));
		unitM.add(new Integer(0));

		return GeneticNetwork.getUnitString(unitS, unitE, unitM,
				currentDocument.getModel());
	}
}
