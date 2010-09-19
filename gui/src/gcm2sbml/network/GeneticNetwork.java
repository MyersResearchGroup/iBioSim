package gcm2sbml.network;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.parser.GCMParser;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;
import gcm2sbml.visitor.AbstractPrintVisitor;
import gcm2sbml.visitor.PrintActivatedBindingVisitor;
import gcm2sbml.visitor.PrintActivatedProductionVisitor;
import gcm2sbml.visitor.PrintBiochemicalVisitor;
import gcm2sbml.visitor.PrintDecaySpeciesVisitor;
import gcm2sbml.visitor.PrintDimerizationVisitor;
import gcm2sbml.visitor.PrintRepressionBindingVisitor;
import gcm2sbml.visitor.PrintSpeciesVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;

import biomodelsim.BioSim;

/**
 * This class represents a genetic network
 * 
 * @author Nam
 * 
 */
public class GeneticNetwork {
	
	private String separator;
	
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
	 */
	public GeneticNetwork() {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
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
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.species = species;
		this.stateMap = stateMap;
		this.promoters = promoters;
		this.properties = gcm;
		
		AbstractPrintVisitor.setGCMFile(gcm);
		
		initialize();
	}
	
	public void buildTemplate(HashMap<String, SpeciesInterface> species,			
			HashMap<String, Promoter> promoters, String gcm, String filename) {
		
		GCMFile file = new GCMFile(currentRoot);
		file.load(currentRoot+gcm);
		AbstractPrintVisitor.setGCMFile(file);
		setSpecies(species);
		setPromoters(promoters);
		
		SBMLDocument document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		currentDocument = document;
		Model m = document.createModel();
		document.setModel(m);
		Utility.addCompartments(document, compartment);
		document.getModel().getCompartment(compartment).setSize(1);
		
		SBMLWriter writer = new SBMLWriter();
		printSpecies(document);
		printOnlyPromoters(document);
		
		try {
			PrintStream p = new PrintStream(new FileOutputStream(filename));
			m.setName("Created from " + gcm);
			m.setId(new File(filename).getName().replace(".xml", ""));	
			m.setVolumeUnits("litre");
			m.setSubstanceUnits("mole");
			p.print(writer.writeToString(document));
			p.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Loads in a properties file
	 * 
	 * @param filename
	 *            the file to load
	 */
	public void loadProperties(GCMFile gcm) {
		properties = gcm;
		dimerizationAbstraction = gcm.getDimAbs();
		biochemicalAbstraction = gcm.getBioAbs();
	}
	
	public void setSBMLFile(String file) {
		sbmlDocument = file;
	}
	
	public void setSBML(SBMLDocument doc) {
		document = doc;
	}

	/**
	 * Outputs the network to an SBML file
	 * 
	 * @param filename
	 * @return the sbml document
	 */
	public SBMLDocument outputSBML(String filename) {
		SBMLDocument document = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		currentDocument = document;
		Model m = document.createModel();
		document.setModel(m);
		Utility.addCompartments(document, compartment);
		document.getModel().getCompartment(compartment).setSize(1);
		return outputSBML(filename, document);
	}

	public SBMLDocument outputSBML(String filename, SBMLDocument document) {
		try {
			Model m = document.getModel();
			//checkConsistancy(document);
			SBMLWriter writer = new SBMLWriter();
			//printParameters(document);
			printSpecies(document);
			printPromoters(document);
			printRNAP(document);
			printDecay(document);
			// System.out.println(counter++);
			//checkConsistancy(document);
			if (!dimerizationAbstraction) {
				printDimerization(document);
			}
			if (!biochemicalAbstraction) {
				printBiochemical(document);
			}
			// System.out.println(counter++);
			//checkConsistancy(document);
			printPromoterProduction(document);
			// System.out.println(counter++);
			//checkConsistancy(document);
			printPromoterBinding(document);
			// System.out.println(counter++);
			//checkConsistancy(document);
			//printComponents(document, filename);
			PrintStream p = new PrintStream(new FileOutputStream(filename));

			m.setName("Created from " + new File(filename).getName().replace("xml", "gcm"));
			m.setId(new File(filename).getName().replace(".xml", ""));			
			m.setVolumeUnits("litre");
			m.setSubstanceUnits("mole");

			p.print(writer.writeToString(document));

			p.close();
			return document;
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
	public SBMLDocument mergeSBML(String filename) {
		try {
			if (document == null) {
				if (sbmlDocument.equals("")) {
					return outputSBML(filename);
				}

				SBMLDocument document = BioSim.readSBML(currentRoot + sbmlDocument);
				// checkConsistancy(document);
				currentDocument = document;
				return outputSBML(filename, document);
			}
			else {
				currentDocument = document;
				return outputSBML(filename, document);
			}
		}
		catch (Exception e) {
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
	public SBMLDocument mergeSBML(String filename, SBMLDocument document) {
		try {
			currentDocument = document;
			return outputSBML(filename, document);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to output to SBML");
		}
	}
	
	/**
	 * Prints the parameters to the SBMLDocument
	 * @param document the document to print to
	 */
	private void printParameters(SBMLDocument document) {
		if (properties != null) {
			for (String s : properties.getGlobalParameters().keySet()) {
				String param = properties.getParameter(s);
				//Utility.addGlobalParameter(document, new Parameter())
			}
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
			if (p.getOutputs().size()==0) continue;
			org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
			r.setId("R_RNAP_" + p.getId());
			r.addReactant(Utility.SpeciesReference("RNAP", 1));
			r.addReactant(Utility.SpeciesReference(p.getId(), 1));
			r.addProduct(Utility.SpeciesReference("RNAP_" + p.getId(), 1));
			r.setReversible(true);
			r.setFast(false);
			KineticLaw kl = r.createKineticLaw();
			kl.addParameter(Utility.Parameter("kf", rnap, getMoleTimeParameter(2)));
			kl.addParameter(Utility.Parameter("kr", 1, getMoleTimeParameter(1)));
			kl.setFormula("kf*" + "RNAP*" + p.getId() + "-kr*RNAP_"
					+ p.getId());		
			Utility.addReaction(document, r);

			// Next setup activated binding
			PrintActivatedBindingVisitor v = new PrintActivatedBindingVisitor(
					document, p, act, kdimer, kcoop, kbio,
					dimer);
			
			v.setBiochemicalAbstraction(biochemicalAbstraction);
			v.setDimerizationAbstraction(dimerizationAbstraction);
			v.setCooperationAbstraction(cooperationAbstraction);
			v.run();

			// Next setup repression binding
			p.getRepressors();
			PrintRepressionBindingVisitor v2 = new PrintRepressionBindingVisitor(
					document, p, rep, kdimer, kcoop, kbio,
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
			if (p.getOutputs().size()==0) continue;
			if (p.getActivators().size() > 0 && p.getRepressors().size() == 0) {
				org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
				r.setId("R_basal_production_" + p.getId());
				r.addModifier(Utility.ModifierSpeciesReference("RNAP_" + p.getId()));
				for (SpeciesInterface species : p.getOutputs()) {
					r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
				}
				r.setReversible(false);
				r.setFast(false);
				KineticLaw kl = r.createKineticLaw();
				if (p.getProperty(GlobalConstants.KBASAL_STRING) != null) {
					kl.addParameter(Utility.Parameter("basal", Double.parseDouble(p
							.getProperty(GlobalConstants.KBASAL_STRING)),
							getMoleTimeParameter(1)));
				} else {
					kl.addParameter(Utility.Parameter("basal", basal,
							getMoleTimeParameter(1)));
				}
				kl.setFormula("basal*" + "RNAP_" + p.getId());
				Utility.addReaction(document, r);

				PrintActivatedProductionVisitor v = new PrintActivatedProductionVisitor(
						document, p, p.getActivators(), act, stoc);
				v.run();
			} else if (p.getActivators().size() == 0
					&& p.getRepressors().size() >= 0) {
				org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
				r.setId("R_production_" + p.getId());
				r.addModifier(Utility.ModifierSpeciesReference("RNAP_" + p.getId()));
				for (SpeciesInterface species : p.getOutputs()) {
					r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
				}
				r.setReversible(false);
				r.setFast(false);
				KineticLaw kl = r.createKineticLaw();
				if (p.getProperty(GlobalConstants.OCR_STRING) != null) {
					kl.addParameter(Utility.Parameter("koc", Double.parseDouble(p
							.getProperty(GlobalConstants.OCR_STRING)),
							getMoleTimeParameter(1)));
				} else {
					kl.addParameter(Utility.Parameter("koc", koc,
							getMoleTimeParameter(1)));
				}
				kl.setFormula("koc*" + "RNAP_" + p.getId());
				Utility.addReaction(document, r);
			} else {
				// TODO: Should ask Chris how to handle
				// Both activated and repressed
				org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
				r.setId("R_basal_production_" + p.getId());
				r.addModifier(Utility.ModifierSpeciesReference("RNAP_" + p.getId()));
				for (SpeciesInterface species : p.getOutputs()) {
					r.addProduct(Utility.SpeciesReference(species.getId(), stoc));
				}
				r.setReversible(false);
				r.setFast(false);
				KineticLaw kl = r.createKineticLaw();
				if (p.getProperty(GlobalConstants.KBASAL_STRING) != null) {
					kl.addParameter(Utility.Parameter("basal", Double.parseDouble(p
							.getProperty(GlobalConstants.KBASAL_STRING)),
							getMoleTimeParameter(1)));
				} else {
					kl.addParameter(Utility.Parameter("basal", basal,
							getMoleTimeParameter(1)));
				}
				kl.setFormula("basal*" + "RNAP_" + p.getId());
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
		double init = 0;
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
	
	private ASTNode updateMathVar(ASTNode math, String origVar, String newVar) {
		String s = updateFormulaVar(myFormulaToString(math), origVar, newVar);
		return myParseFormula(s);
	}
	
	private String myFormulaToString(ASTNode mathFormula) {
		String formula = libsbml.formulaToString(mathFormula);
		formula = formula.replaceAll("arccot", "acot");
		formula = formula.replaceAll("arccoth", "acoth");
		formula = formula.replaceAll("arccsc", "acsc");
		formula = formula.replaceAll("arccsch", "acsch");
		formula = formula.replaceAll("arcsec", "asec");
		formula = formula.replaceAll("arcsech", "asech");
		formula = formula.replaceAll("arccosh", "acosh");
		formula = formula.replaceAll("arcsinh", "asinh");
		formula = formula.replaceAll("arctanh", "atanh");
		String newformula = formula.replaceFirst("00e", "0e");
		while (!(newformula.equals(formula))) {
			formula = newformula;
			newformula = formula.replaceFirst("0e\\+", "e+");
			newformula = newformula.replaceFirst("0e-", "e-");
		}
		formula = formula.replaceFirst("\\.e\\+", ".0e+");
		formula = formula.replaceFirst("\\.e-", ".0e-");
		return formula;
	}
	
	private String updateFormulaVar(String s, String origVar, String newVar) {
		s = " " + s + " ";
		s = s.replace(" " + origVar + " ", " " + newVar + " ");
		s = s.replace(" " + origVar + "(", " " + newVar + "(");
		s = s.replace("(" + origVar + ")", "(" + newVar + ")");
		s = s.replace("(" + origVar + " ", "(" + newVar + " ");
		s = s.replace("(" + origVar + ",", "(" + newVar + ",");
		s = s.replace(" " + origVar + ")", " " + newVar + ")");
		s = s.replace(" " + origVar + "^", " " + newVar + "^");
		return s.trim();
	}
	
	private ASTNode myParseFormula(String formula) {
		ASTNode mathFormula = libsbml.parseFormula(formula);
		if (mathFormula == null)
			return null;
		setTimeAndTrigVar(mathFormula);
		return mathFormula;
	}
	
	private void setTimeAndTrigVar(ASTNode node) {
		if (node.getType() == libsbml.AST_NAME) {
			if (node.getName().equals("t")) {
				node.setType(libsbml.AST_NAME_TIME);
			}
			else if (node.getName().equals("time")) {
				node.setType(libsbml.AST_NAME_TIME);
			}
		}
		if (node.getType() == libsbml.AST_FUNCTION) {
			if (node.getName().equals("acot")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOT);
			}
			else if (node.getName().equals("acoth")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOTH);
			}
			else if (node.getName().equals("acsc")) {
				node.setType(libsbml.AST_FUNCTION_ARCCSC);
			}
			else if (node.getName().equals("acsch")) {
				node.setType(libsbml.AST_FUNCTION_ARCCSCH);
			}
			else if (node.getName().equals("asec")) {
				node.setType(libsbml.AST_FUNCTION_ARCSEC);
			}
			else if (node.getName().equals("asech")) {
				node.setType(libsbml.AST_FUNCTION_ARCSECH);
			}
			else if (node.getName().equals("acosh")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOSH);
			}
			else if (node.getName().equals("asinh")) {
				node.setType(libsbml.AST_FUNCTION_ARCSINH);
			}
			else if (node.getName().equals("atanh")) {
				node.setType(libsbml.AST_FUNCTION_ARCTANH);
			}
		}

		for (int c = 0; c < node.getNumChildren(); c++)
			setTimeAndTrigVar(node.getChild(c));
	}
	
	private void updateVarId(boolean isSpecies, String origId, String newId, SBMLDocument document) {
		if (origId.equals(newId))
			return;
		Model model = document.getModel();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			org.sbml.libsbml.Species species = (org.sbml.libsbml.Species) model.getListOfSpecies().get(i);
			if (species.getCompartment().equals(origId)) {
				species.setCompartment(newId);
			}
			if (species.getSpeciesType().equals(origId)) {
				species.setSpeciesType(newId);
			}
		}
		for (int i = 0; i < model.getNumCompartments(); i++) {
			org.sbml.libsbml.Compartment compartment = (org.sbml.libsbml.Compartment) model.getListOfCompartments().get(i);
			if (compartment.getCompartmentType().equals(origId)) {
				compartment.setCompartmentType(newId);
			}
		}
		for (int i = 0; i < model.getNumReactions(); i++) {
			org.sbml.libsbml.Reaction reaction = (org.sbml.libsbml.Reaction) model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getProduct(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
					if (specRef.isSetStoichiometryMath()) {
						specRef.getStoichiometryMath().setMath(updateMathVar(specRef
								.getStoichiometryMath().getMath(), origId, newId));
					}
				}
			}
			if (isSpecies) {
				for (int j = 0; j < reaction.getNumModifiers(); j++) {
					if (reaction.getModifier(j).isSetSpecies()) {
						ModifierSpeciesReference specRef = reaction.getModifier(j);
						if (origId.equals(specRef.getSpecies())) {
							specRef.setSpecies(newId);
						}
					}
				}
			}
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				if (reaction.getReactant(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getReactant(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
					if (specRef.isSetStoichiometryMath()) {
						specRef.getStoichiometryMath().setMath(updateMathVar(specRef
								.getStoichiometryMath().getMath(), origId, newId));
					}
				}
			}
			reaction.getKineticLaw().setMath(
					updateMathVar(reaction.getKineticLaw().getMath(), origId, newId));
		}
		if (model.getNumInitialAssignments() > 0) {
			for (int i = 0; i < model.getNumInitialAssignments(); i++) {
				InitialAssignment init = (InitialAssignment) model.getListOfInitialAssignments().get(i);
				if (origId.equals(init.getSymbol())) {
					init.setSymbol(newId);
				}
				init.setMath(updateMathVar(init.getMath(), origId, newId));
			}
		}
		if (model.getNumRules() > 0) {
			for (int i = 0; i < model.getNumRules(); i++) {
				Rule rule = (Rule) model.getListOfRules().get(i);
				if (rule.isSetVariable() && origId.equals(rule.getVariable())) {
					rule.setVariable(newId);
				}
				rule.setMath(updateMathVar(rule.getMath(), origId, newId));
			}
		}
		if (model.getNumConstraints() > 0) {
			for (int i = 0; i < model.getNumConstraints(); i++) {
				Constraint constraint = (Constraint) model.getListOfConstraints().get(i);
				constraint.setMath(updateMathVar(constraint.getMath(), origId, newId));
			}
		}
		if (model.getNumEvents() > 0) {
			for (int i = 0; i < model.getNumEvents(); i++) {
				org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) model.getListOfEvents().get(i);
				if (event.isSetTrigger()) {
					event.getTrigger().setMath(updateMathVar(event.getTrigger().getMath(), origId, newId));
				}
				if (event.isSetDelay()) {
					event.getDelay().setMath(updateMathVar(event.getDelay().getMath(), origId, newId));
				}
				for (int j = 0; j < event.getNumEventAssignments(); j++) {
					EventAssignment ea = (EventAssignment) event.getListOfEventAssignments().get(j);
					if (ea.getVariable().equals(origId)) {
						ea.setVariable(newId);
					}
					if (ea.isSetMath()) {
						ea.setMath(updateMathVar(ea.getMath(), origId, newId));
					}
				}
			}
		}
	}
	
	private void unionSBML(SBMLDocument mainDoc, SBMLDocument doc, String compName) {
		Model m = doc.getModel();
		for (int i = 0; i < m.getNumCompartmentTypes(); i ++) {
			org.sbml.libsbml.CompartmentType c = m.getCompartmentType(i);
			String newName = compName + "_" + c.getId();
			updateVarId(false, c.getId(), newName, doc);
			c.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumCompartmentTypes(); j ++) {
				if (mainDoc.getModel().getCompartmentType(j).getId().equals(c.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addCompartmentType(c);
			}
		}
		for (int i = 0; i < m.getNumCompartments(); i ++) {
			org.sbml.libsbml.Compartment c = m.getCompartment(i);
			String newName = compName + "_" + c.getId();
			updateVarId(false, c.getId(), newName, doc);
			c.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumCompartments(); j ++) {
				if (mainDoc.getModel().getCompartment(j).getId().equals(c.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addCompartment(c);
			}
		}
		for (int i = 0; i < m.getNumSpeciesTypes(); i ++) {
			org.sbml.libsbml.SpeciesType s = m.getSpeciesType(i);
			String newName = compName + "_" + s.getId();
			updateVarId(false, s.getId(), newName, doc);
			s.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumSpeciesTypes(); j ++) {
				if (mainDoc.getModel().getSpeciesType(j).getId().equals(s.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addSpeciesType(s);
			}
		}
		for (int i = 0; i < m.getNumSpecies(); i ++) {
			Species spec = m.getSpecies(i);
			if (!spec.getId().equals("RNAP")) {
				String newName = compName + "_" + spec.getId();
				for (Object port : properties.getComponents().get(compName).keySet()) {
					if (spec.getId().equals((String) port)) {
						newName = "_" + compName + "_" + properties.getComponents().get(compName).getProperty((String) port);
						int removeDeg = -1;
						for (int j = 0; j < m.getNumReactions(); j ++) {
							org.sbml.libsbml.Reaction r = m.getReaction(j);
							if (r.getId().equals("Degradation_" + spec.getId())) {
								removeDeg = j;
							}
						}
						if (removeDeg != -1) {
							m.getListOfReactions().remove(removeDeg);
						}
					}
				}
				updateVarId(true, spec.getId(), newName, doc);
				spec.setId(newName);
			}
		}
		for (int i = 0; i < m.getNumSpecies(); i ++) {
			Species spec = m.getSpecies(i);
			if (spec.getId().startsWith("_" + compName + "_")) {
				updateVarId(true, spec.getId(), spec.getId().substring(2 + compName.length()), doc);
				spec.setId(spec.getId().substring(2 + compName.length()));
			}
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumSpecies(); j ++) {
				if (mainDoc.getModel().getSpecies(j).getId().equals(spec.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addSpecies(spec);
			}
		}
		for (int i = 0; i < m.getNumParameters(); i ++) {
			org.sbml.libsbml.Parameter p = m.getParameter(i);
			String newName = compName + "_" + p.getId();
			updateVarId(false, p.getId(), newName, doc);
			p.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumParameters(); j ++) {
				if (mainDoc.getModel().getParameter(j).getId().equals(p.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addParameter(p);
			}
		}
		for (int i = 0; i < m.getNumReactions(); i ++) {
			org.sbml.libsbml.Reaction r = m.getReaction(i);
			String newName = compName + "_" + r.getId();
			updateVarId(false, r.getId(), newName, doc);
			r.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumReactions(); j ++) {
				if (mainDoc.getModel().getReaction(j).getId().equals(r.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addReaction(r);
			}
		}
		for (int i = 0; i < m.getNumInitialAssignments(); i ++) {
			InitialAssignment init = (InitialAssignment) m.getListOfInitialAssignments().get(i);
			mainDoc.getModel().addInitialAssignment(init);
		}
		for (int i = 0; i < m.getNumRules(); i++) {
			org.sbml.libsbml.Rule r = m.getRule(i);
			mainDoc.getModel().addRule(r);
		}
		for (int i = 0; i < m.getNumConstraints(); i++) {
			Constraint constraint = (Constraint) m.getListOfConstraints().get(i);
			mainDoc.getModel().addConstraint(constraint);
		}
		for (int i = 0; i < m.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) m.getListOfEvents().get(i);
			String newName = compName + "_" + event.getId();
			updateVarId(false, event.getId(), newName, doc);
			event.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumEvents(); j ++) {
				if (mainDoc.getModel().getEvent(j).getId().equals(event.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addEvent(event);
			}
		}
		for (int i = 0; i < m.getNumUnitDefinitions(); i ++) {
			UnitDefinition u = m.getUnitDefinition(i);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumUnitDefinitions(); j ++) {
				if (mainDoc.getModel().getUnitDefinition(j).getId().equals(u.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addUnitDefinition(u);
			}
		}
		for (int i = 0; i < m.getNumFunctionDefinitions(); i ++) {
			FunctionDefinition f = m.getFunctionDefinition(i);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumFunctionDefinitions(); j ++) {
				if (mainDoc.getModel().getFunctionDefinition(j).getId().equals(f.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addFunctionDefinition(f);
			}
		}
	}
	
	private void printComponents(SBMLDocument document, String filename) {
		for (String s : properties.getComponents().keySet()) {
			GCMParser parser = new GCMParser(currentRoot + separator +
					properties.getComponents().get(s).getProperty("gcm"));
			parser.setParameters(properties.getParameters());
			GeneticNetwork network = parser.buildNetwork();
			SBMLDocument d =  network.mergeSBML(filename);
			unionSBML(document, d, s);
		}
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
			if (promoter.getOutputs().size()==0) continue;
			// First print out the promoter, and promoter bound to RNAP
			String tempPromoters = numPromoters;
			if (promoter.getProperty(GlobalConstants.PROMOTER_COUNT_STRING) != null) {
				tempPromoters = promoter
						.getProperty(GlobalConstants.PROMOTER_COUNT_STRING);
			}
			Species s = Utility.makeSpecies(promoter.getId(), compartment,
					Double.parseDouble(tempPromoters));
		    if ((promoter.getProperties() != null) &&
		    	(promoter.getProperties().containsKey(GlobalConstants.NAME))) {
		    	s.setName(promoter.getProperty(GlobalConstants.NAME));
		    }
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);			
			s = Utility.makeSpecies("RNAP_" + promoter.getId(), compartment,
					0);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
			// Now cycle through all activators and repressors and add those
			// bindings
			for (SpeciesInterface species : promoter.getActivators()) {
				s = Utility.makeSpecies("RNAP_" + promoter.getId() + "_"
						+ species.getId(), compartment, 0);
				s.setHasOnlySubstanceUnits(true);
				Utility.addSpecies(document, s);
			}
			for (SpeciesInterface species : promoter.getRepressors()) {
				s = Utility.makeSpecies("bound_" + promoter.getId() + "_"
						+ species.getId(), compartment, 0);
				s.setHasOnlySubstanceUnits(true);
				Utility.addSpecies(document, s);
			}
		}

	}
	
	/**
	 * Prints the promoters in the network
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printOnlyPromoters(SBMLDocument document) {
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
			Species s = Utility.makeSpecies(promoter.getId(), compartment,
					Double.parseDouble(tempPromoters));
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);			
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
		addProperties();
	}

	/**
	 * Configurates the promoters
	 * 
	 */
	private void buildPromoters() {
		for (Promoter promoter : promoters.values()) {
			for (Reaction reaction : promoter.getActivatingReactions()) {
				if (!reaction.isBiochemical() && reaction.getDimer() <= 1 &&
						!reaction.getInputState().equals("none")) {
					promoter.addActivator(stateMap
							.get(reaction.getInputState()));
					promoter.addToReactionMap(stateMap.get(reaction
							.getInputState()), reaction);
				}
				if (!reaction.getOutputState().equals("none")) {
					promoter.addOutput(stateMap.get(reaction.getOutputState()));
				}
			}
			for (Reaction reaction : promoter.getRepressingReactions()) {
				if (!reaction.isBiochemical() && reaction.getDimer() <= 1 &&
				!reaction.getInputState().equals("none")) {
					promoter.addRepressor(stateMap
							.get(reaction.getInputState()));
					promoter.addToReactionMap(stateMap.get(reaction
							.getInputState()), reaction);
				}
				if (!reaction.getOutputState().equals("none")) {
					promoter.addOutput(stateMap.get(reaction.getOutputState()));
				}
			}
		}
	}

	/**
	 * Builds the dimer list from reactions and species and adds it to the
	 * promoter as input.
	 */
	private void buildDimers() {
		HashMap<String, SpeciesInterface> dimers = new HashMap<String, SpeciesInterface>();
		
		// Go through reaction list to see if any are missed
		for (Promoter promoter : promoters.values()) {
			for (Reaction reaction : promoter.getActivatingReactions()) {
				if (reaction.getDimer() > 1) {
					SpeciesInterface specie = stateMap.get(reaction.getInputState());
					specie.addProperty(GlobalConstants.MAX_DIMER_STRING, "" + reaction.getDimer());
					DimerSpecies dimer = new DimerSpecies(specie, specie.getProperties());
					dimers.put(dimer.getId(), dimer);
					promoter.addToReactionMap(dimer, reaction);
					promoter.getActivators().add(dimer);
				}
			}
			for (Reaction reaction : promoter.getRepressingReactions()) {
				if (reaction.getDimer() > 1) {
					SpeciesInterface specie = stateMap.get(reaction.getInputState());
					specie.addProperty(GlobalConstants.MAX_DIMER_STRING, "" + reaction.getDimer());
					DimerSpecies dimer = new DimerSpecies(specie, specie.getProperties());
					dimers.put(dimer.getId(), dimer);
					promoter.addToReactionMap(dimer, reaction);
					promoter.getRepressors().add(dimer);
				}
			}

		}
		// Now put dimers back into network
		for (SpeciesInterface specie : dimers.values()) {
			species.put(specie.getId(), specie);
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
					SpeciesInterface specie = stateMap.get(reaction.getInputState());
					specie.addProperty(GlobalConstants.KBIO_STRING, "" + reaction.getKbio());
					biochem.add(specie);
				}
			}
			if (biochem.size() == 1) {
				throw new IllegalStateException(
						"Must have more than 1 biochemical reaction");
			} else if (biochem.size() >= 2) {
				BiochemicalSpecies bio = new BiochemicalSpecies(biochem, biochem.get(0).getProperties());
				promoter.addActivator(bio);
				for (Reaction reaction : reactions) {
					promoter.addToReactionMap(bio, reaction);
				}
				bio.addProperty(GlobalConstants.KDECAY_STRING, "0");
				species.put(bio.getId(), bio);
			}

			biochem = new ArrayList<SpeciesInterface>();
			reactions = new ArrayList<Reaction>();
			for (Reaction reaction : promoter.getRepressingReactions()) {
				if (reaction.isBiochemical()) {
					reactions.add(reaction);
					SpeciesInterface specie = stateMap.get(reaction.getInputState());
					specie.addProperty(GlobalConstants.KBIO_STRING, "" + reaction.getKbio());
					biochem.add(specie);
				}
			}
			if (biochem.size() == 1) {
				throw new IllegalStateException(
						"Must have more than 1 biochemical reaction");
			} else if (biochem.size() >= 2) {
				BiochemicalSpecies bio = new BiochemicalSpecies(biochem, biochem.get(0).getProperties());
				for (Reaction reaction : reactions) {
					promoter.addToReactionMap(bio, reaction);
				}
				promoter.addRepressor(bio);
				bio.addProperty(GlobalConstants.KDECAY_STRING, "0");
				species.put(bio.getId(), bio);
			}
		}

	}
	
	/**
	 * Adds activating/repressing binding constant and degree of cooperativity to species 
	 * properties
	 */
	private void addProperties() {
		for (Promoter promoter : promoters.values()) {
			for (Reaction reaction : promoter.getActivatingReactions()) {
				SpeciesInterface specie = stateMap.get(reaction.getInputState());
				specie.addProperty(GlobalConstants.KACT_STRING, "" + reaction.getBindingConstant());
				specie.addProperty(GlobalConstants.COOPERATIVITY_STRING, "" + reaction.getCoop());
			}
			for (Reaction reaction : promoter.getRepressingReactions()) {
				SpeciesInterface specie = stateMap.get(reaction.getInputState());
				specie.addProperty(GlobalConstants.KREP_STRING, "" + reaction.getBindingConstant());
				specie.addProperty(GlobalConstants.COOPERATIVITY_STRING, "" + reaction.getCoop());
			}

		}
	}

	public HashMap<String, SpeciesInterface> getSpecies() {
		return species;
	}

	public void setSpecies(HashMap<String, SpeciesInterface> species) {
		this.species = species;
	}

	public HashMap<String, SpeciesInterface> getStateMap() {
		return stateMap;
	}

	public void setStateMap(HashMap<String, SpeciesInterface> stateMap) {
		this.stateMap = stateMap;
	}

	public HashMap<String, Promoter> getPromoters() {
		return promoters;
	}

	public void setPromoters(HashMap<String, Promoter> promoters) {
		this.promoters = promoters;
	}

	public GCMFile getProperties() {
		return properties;
	}

	public void setProperties(GCMFile properties) {
		this.properties = properties;
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
	
	private SBMLDocument document = null;

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
		UnitDefinition t = new UnitDefinition(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		String name = "u_";
		for (int i = 0; i < unitNames.size(); i++) {
			String sign = "";
			if (exponents.get(i).intValue() < 0) {
				sign = "n";
			}
			name = name + multiplier.get(i) + "_" + unitNames.get(i) + "_"
					+ sign + Math.abs(exponents.get(i)) + "_";
			Unit u = t.createUnit();
			u.setKind(libsbml.UnitKind_forName(unitNames.get(i)));
			u.setExponent(exponents.get(i).intValue());
			u.setMultiplier(multiplier.get(i).intValue());
			u.setScale(0);
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
			unitM.add(new Integer(1));
		}

		unitS.add("second");
		unitE.add(new Integer(-1));
		unitM.add(new Integer(1));

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
		unitM.add(new Integer(1));

		return GeneticNetwork.getUnitString(unitS, unitE, unitM,
				currentDocument.getModel());
	}
	
	public static String getMoleParameter(String numMolecules) {
		return getMoleParameter(Integer.parseInt(numMolecules));
	}
}
