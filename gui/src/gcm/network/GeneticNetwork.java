package gcm.network;

import gcm.parser.CompatibilityFixer;
import gcm.parser.GCMFile;
import gcm.parser.GCMParser;
import gcm.util.GlobalConstants;
import gcm.util.Utility;
import gcm.visitor.AbstractPrintVisitor;
import gcm.visitor.AbstractionEngine;
import gcm.visitor.PrintActivatedBindingVisitor;
import gcm.visitor.PrintActivatedProductionVisitor;
import gcm.visitor.PrintComplexVisitor;
import gcm.visitor.PrintDecaySpeciesVisitor;
import gcm.visitor.PrintRepressionBindingVisitor;
import gcm.visitor.PrintSpeciesVisitor;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import main.Gui;

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
			HashMap<String, ArrayList<PartSpecies>> complexMap, HashMap<String, ArrayList<PartSpecies>> partsMap,
			HashMap<String, Promoter> promoters) {
		this(species, complexMap, partsMap, promoters, null);
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
			HashMap<String, ArrayList<PartSpecies>> complexMap, HashMap<String, ArrayList<PartSpecies>> partsMap, 
			HashMap<String, Promoter> promoters, GCMFile gcm) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.species = species;
		this.promoters = promoters;
		this.complexMap = complexMap;
		this.partsMap = partsMap;
		this.properties = gcm;
		this.compartments = gcm.getCompartments();
		
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
		
		SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		currentDocument = document;
		Model m = document.createModel();
		document.setModel(m);
		Utility.addCompartments(document, "default");
		document.getModel().getCompartment("default").setSize(1);
		
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
		complexAbstraction = gcm.getBioAbs();
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
		SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		currentDocument = document;
		Model m = document.createModel();
		document.setModel(m);
		Utility.addCompartments(document, "default");
		document.getModel().getCompartment("default").setSize(1);
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
			
			printPromoterProduction(document);
			
			printPromoterBinding(document);
			
			printComplexBinding(document);
			
			PrintStream p = new PrintStream(new FileOutputStream(filename));

			m.setName("Created from " + new File(filename).getName().replace("xml", "gcm"));
			m.setId(new File(filename).getName().replace(".xml", ""));			
			m.setVolumeUnits("litre");
			m.setSubstanceUnits("mole");
			if (document != null) {
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, true);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, true);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
				document.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
				long numErrors = document.checkConsistency();
				if (numErrors > 0) {
					String message = "";
					for (long i = 0; i < numErrors; i++) {
						String error = document.getError(i).getMessage(); // .replace(". ",
						// ".\n");
						message += i + ":" + error + "\n";
					}
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(600, 600));
					scroll.setPreferredSize(new Dimension(600, 600));
					scroll.setViewportView(messageArea);
					JOptionPane.showMessageDialog(Gui.frame, scroll, "Generated SBML Has Errors",
							JOptionPane.ERROR_MESSAGE);
				}
			}
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

				SBMLDocument document = Gui.readSBML(currentRoot + sbmlDocument);
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
	 * Prints each promoter binding
	 * 
	 * @param document
	 *            the SBMLDocument to print to
	 */
	private void printPromoterBinding(SBMLDocument document) {

		for (Promoter p : promoters.values()) {
			// First setup RNAP binding
			if (p.getOutputs().size()==0) continue;
			String compartment = checkCompartments(p.getId());
			String rnapName = "RNAP";
			if (!compartment.equals("default"))
				rnapName = compartment + "__RNAP";
			org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(Gui.SBML_LEVEL, Gui.SBML_VERSION);
			r.setCompartment(compartment);
			r.setId("R_" + p.getId() + "_RNAP");
			r.addReactant(Utility.SpeciesReference(rnapName, 1));
			r.addReactant(Utility.SpeciesReference(p.getId(), 1));
			r.addProduct(Utility.SpeciesReference(p.getId() + "_RNAP", 1));
			r.setReversible(true);
			r.setFast(false);
			KineticLaw kl = r.createKineticLaw();
			double[] Krnap = p.getKrnap();
			if (Krnap.length == 2) {
				kl.addParameter(Utility.Parameter("kr", Krnap[1], GeneticNetwork
						.getMoleTimeParameter(1)));
				kl.addParameter(Utility.Parameter(krnapString, Krnap[0]/Krnap[1],
						GeneticNetwork.getMoleParameter(2)));
			} else {
				kl.addParameter(Utility.Parameter("kr", 1, GeneticNetwork
						.getMoleTimeParameter(1)));
				kl.addParameter(Utility.Parameter(krnapString, Krnap[0],
						GeneticNetwork.getMoleParameter(2)));
			}
			kl.setFormula("kr*" + krnapString + "*" + rnapName + "*" + p.getId() + "-kr*"
					+ p.getId() + "_RNAP");		
			Utility.addReaction(document, r);

			// Next setup activated binding
			PrintActivatedBindingVisitor v = new PrintActivatedBindingVisitor(
					document, p, species, compartment, complexMap, partsMap);
			v.setCooperationAbstraction(cooperationAbstraction);
			v.setComplexAbstraction(complexAbstraction);
			v.run();

			// Next setup repression binding
			PrintRepressionBindingVisitor v2 = new PrintRepressionBindingVisitor(
					document, p, species, compartment, complexMap, partsMap);
			v2.setCooperationAbstraction(cooperationAbstraction);
			v2.setComplexAbstraction(complexAbstraction);
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
		
		for (Promoter p : promoters.values()) {
			if (p.getOutputs().size()==0) continue;
			String compartment = checkCompartments(p.getId());
			org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(Gui.SBML_LEVEL, Gui.SBML_VERSION);
			r.setCompartment(compartment);
			r.addModifier(Utility.ModifierSpeciesReference(p.getId() + "_RNAP"));
			for (SpeciesInterface species : p.getOutputs()) {
				r.addProduct(Utility.SpeciesReference(species.getId(), p.getStoich()));
			}
			r.setReversible(false);
			r.setFast(false);
			KineticLaw kl = r.createKineticLaw();
			if (p.getActivators().size() > 0) {
				r.setId("R_basal_production_" + p.getId());
				kl.addParameter(Utility.Parameter(kBasalString, p.getKbasal(),
							getMoleTimeParameter(1)));
				kl.setFormula(kBasalString + "*" + p.getId() + "_RNAP");
				
			} else {
				r.setId("R_constitutive_production_" + p.getId());
				kl.addParameter(Utility.Parameter(kOcString, p.getKoc(),
							getMoleTimeParameter(1)));
				kl.setFormula(kOcString + "*" + p.getId() + "_RNAP");
			}
			Utility.addReaction(document, r);
			if (p.getActivators().size() > 0) {
				PrintActivatedProductionVisitor v = new PrintActivatedProductionVisitor(
						document, p, compartment);
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
		PrintDecaySpeciesVisitor visitor = new PrintDecaySpeciesVisitor(
				document, species, compartments, complexMap, partsMap);
		visitor.setComplexAbstraction(complexAbstraction);
		visitor.run();
	}

	/**
	 * Prints the species in the network
	 * 
	 * @param document
	 *            the SBML document
	 */
	private void printSpecies(SBMLDocument document) {
		PrintSpeciesVisitor visitor = new PrintSpeciesVisitor(document, species
				, compartments);
		visitor.setComplexAbstraction(complexAbstraction);
		visitor.run();
	}
	
	private void printComplexBinding(SBMLDocument document) {
		PrintComplexVisitor v = new PrintComplexVisitor(document, species, compartments, complexMap, partsMap);
		v.setComplexAbstraction(complexAbstraction);
		v.run();
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
	
	//This currently isn't used
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
		
		for (Promoter p : promoters.values()) {
			if (p.getOutputs().size()==0) continue;
			// First print out the promoter, and promoter bound to RNAP
			// But first check if promoter belongs to a compartment other than default
			String compartment = checkCompartments(p.getId());
			Species s = Utility.makeSpecies(p.getId(), compartment,
					p.getPcount());
		    if ((p.getProperties() != null) &&
		    	(p.getProperties().containsKey(GlobalConstants.NAME))) {
		    	s.setName(p.getProperty(GlobalConstants.NAME));
		    }
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);			
			s = Utility.makeSpecies(p.getId() + "_RNAP", compartment,
					0);
			s.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, s);
			// Now cycle through all activators and repressors and add 
			// those species bound to promoter
			for (SpeciesInterface specie : p.getActivators()) {
				String activator = specie.getId();
				String[] splitted = activator.split("__");
				if (splitted.length > 1)
					activator = splitted[1];
				s = Utility.makeSpecies(p.getId() + "_"
						+ activator + "_RNAP", compartment, 0);
				s.setHasOnlySubstanceUnits(true);
				Utility.addSpecies(document, s);
			}
			for (SpeciesInterface specie : p.getRepressors()) {
				String repressor = specie.getId();
				String[] splitted = repressor.split("__");
				if (splitted.length > 1)
					repressor = splitted[1];
				s = Utility.makeSpecies(p.getId() + "_"
						+ repressor + "_bound", compartment, 0);
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

		for (Promoter p : promoters.values()) {
			Species s = Utility.makeSpecies(p.getId(), "default",
					p.getPcount());
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
		Species s = Utility.makeSpecies("RNAP", "default", rnap);		
		s.setHasOnlySubstanceUnits(true);
		Utility.addSpecies(document, s);
		//Adds RNA polymerase for compartments other than default
		for (String compartment : compartments) {
			Species sc = Utility.makeSpecies(compartment + "__RNAP", compartment, rnap);
			sc.setHasOnlySubstanceUnits(true);
			Utility.addSpecies(document, sc);
		}
	}
	
	//Checks if species belongs in a compartment other than default
	private String checkCompartments(String species) {
		String compartment = "default";
		String[] splitted = species.split("__");
		if (compartments.contains(splitted[0]))
			compartment = splitted[0];
		return compartment;
	}

	//Returns abstracted rate law for the formation of a complex species
	public String abstractExpression(String id) {
		String expression = id;	
		AbstractionEngine e = new AbstractionEngine(species, complexMap, partsMap);
		if (species.get(id).isSequesterable())
			expression = e.sequesterSpecies(id, "");
		else if (complexMap.containsKey(id))
			expression = e.abstractComplex(id, 0, "");
		return expression;
	}
	
	/**
	 * Initializes the network
	 * 
	 */
	private void initialize() {
		buildComplexes();
		buildComplexInfluences();
		markAbstractable();
	}

	/**
	 * Builds the complex species
	 * 
	 */
	private void buildComplexes() {
		for (String complexId : complexMap.keySet()) {
			ComplexSpecies c = new ComplexSpecies(species.get(complexId));
			species.put(complexId, c);
		}
	}
	
	/**
	 * 
	 * 
	 */
	private void buildComplexInfluences() {
		for (Promoter p : promoters.values()) {
			for (Reaction r : p.getActivatingReactions()) {
				String inputId = r.getInput();
				if (complexMap.containsKey(inputId)) {
					p.addActivator(inputId, species.get(inputId));
					species.get(inputId).setActivator(true);
					String outputId = r.getOutput();
					if (!r.getOutput().equals("none")) {
						p.addOutput(outputId, species.get(outputId));
					}
				}
			}
			for (Reaction r : p.getRepressingReactions()) {
				String inputId = r.getInput();
				if (complexMap.containsKey(inputId)) {
					p.addRepressor(inputId, species.get(inputId));
					species.get(inputId).setRepressor(true);
					String outputId = r.getOutput();
					if (!r.getOutput().equals("none")) {
						p.addOutput(outputId, species.get(outputId));
					}
				}
			}
		}
	}
	
	private void markAbstractable() {
		for (Promoter p : promoters.values()) {
			//Checks if activators are sequesterable
			//Marks non-sequesterable, non-output complex activators as abstractable and checks constituents of complex activators
			for (SpeciesInterface s : p.getActivators()) {
				boolean sequesterable = false;
				if (partsMap.containsKey(s.getId())) {
					sequesterable = checkSequester(s.getId(), "");
				}
				if (complexMap.containsKey(s.getId()) && !s.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)) {
					checkComplex(s.getId(), "");
					if (!sequesterable)
						s.setAbstractable(true);
				}
			}
			//Checks if repressors are sequesterable
			//Marks non-sequesterable, non-output complex repressors as abstractable and checks constituents of complex repressors
			for (SpeciesInterface s : p.getRepressors()) {
				boolean sequesterable = false;
				if (partsMap.containsKey(s.getId())) {
					sequesterable = checkSequester(s.getId(), "");
				}
				if (complexMap.containsKey(s.getId()) && !s.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)) {
					checkComplex(s.getId(), "");
					if (!sequesterable)
						s.setAbstractable(true);
				}
			}
		}	
		//Checks if constituents of non-constituent, non-influencing complex species are abstractable or sequesterable
		for (String complexId : complexMap.keySet()) {
			if (species.get(complexId).getProperty(GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT))
				checkComplex(complexId, "");
		}
	}
	
	//Checks if constituents of complex are sequesterable
	//Marks non-output, non-sequesterable complex constituents as abstractable and checks constituents of complex constituents 
	private boolean checkComplex(String complexId, String payNoMind) {
		for (PartSpecies part : complexMap.get(complexId)) {
			String speciesId = part.getSpeciesId();
			if (!speciesId.equals(payNoMind)) {
				boolean sequesterable = false;
				if (partsMap.get(speciesId).size() > 1) {
					if (!payNoMind.equals(""))
						return false;
					sequesterable = checkSequester(speciesId, complexId);
				}
				if (!payNoMind.equals("") && (species.get(speciesId).isActivator() || species.get(speciesId).isRepressor()))
					return false;
				if (complexMap.containsKey(speciesId) && !species.get(speciesId).getProperty(GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)
						&& !species.get(speciesId).isActivator() && !species.get(speciesId).isRepressor()) {
					checkComplex(speciesId, "");
					if (!sequesterable)
						species.get(speciesId).setAbstractable(true);
				}
			}
		}
		return true;
	}
	
	//Marks constituent species as sequesterable if its complexes are not used elsewhere and its sequestering species are 
	//constituents of only those complexes
	//Checks if constituents of any sequestering complexes are abstractable or sequesterable
	private boolean checkSequester(String speciesId, String payNoMind) {
		boolean sequesterable = true;
		ArrayList<String> abstractableComplexes = new ArrayList<String>();
		for (PartSpecies part : partsMap.get(speciesId)) {
			String complexId = part.getComplexId();
			if (!complexId.equals(payNoMind)) {
				if (part.getStoich() == 1 && !species.get(complexId).isActivator() 
						&& !species.get(complexId).isRepressor() && !partsMap.containsKey(complexId)
						&& !species.get(complexId).getProperty(GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)
						&& checkComplex(complexId, speciesId)) 
					abstractableComplexes.add(complexId);
				else
					sequesterable = false;
			}
		}
		if (sequesterable && abstractableComplexes.size() > 0) {
			species.get(speciesId).setSequesterable(true);
			for (String complexId : abstractableComplexes)
				species.get(complexId).setAbstractable(true);
		}
		return sequesterable;
	}
	
	//Unmarks species as abstractable if they are interesting
	public void unMarkInterestingSpecies(String[] interestingSpecies) {
		for (String interestingId : interestingSpecies) {
			if (species.containsKey(interestingId) && complexMap.containsKey(interestingId)) {
				species.get(interestingId).setAbstractable(false);
				for (PartSpecies part : complexMap.get(interestingId))
					species.get(part.getSpeciesId()).setSequesterable(false);
			}
		}
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

	private boolean cooperationAbstraction = false;
	
	private boolean complexAbstraction = false;

	private HashMap<String, SpeciesInterface> species = null;

	private HashMap<String, Promoter> promoters = null;
	
	private HashMap<String, ArrayList<PartSpecies>> complexMap = null;
	private HashMap<String, ArrayList<PartSpecies>> partsMap;
	
	private ArrayList<String> compartments;

	private GCMFile properties = null;
	
	private String krnapString = GlobalConstants.RNAP_BINDING_STRING;
	
	private String kBasalString = GlobalConstants.KBASAL_STRING;
	
	private String kOcString = GlobalConstants.OCR_STRING;

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
		UnitDefinition t = new UnitDefinition(Gui.SBML_LEVEL, Gui.SBML_VERSION);
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
