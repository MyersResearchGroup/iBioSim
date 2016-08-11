package biomodel.parser;


import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;
import org.sbml.jsbml.ext.arrays.flattening.ArraysFlattening;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
//CompartmentType not supported in Level 3
//import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.ext.comp.SBaseRef;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.SpeciesReference;
//SpeciesType not supported in Level 3
//import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FBCReactionPlugin;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.libsbml.libsbmlConstants;

import biomodel.annotation.AnnotationUtility;
import biomodel.gui.comp.Grid;
import biomodel.gui.sbmlcore.Compartments;
import biomodel.gui.sbmlcore.Constraints;
import biomodel.gui.sbmlcore.Events;
import biomodel.gui.sbmlcore.MySpecies;
import biomodel.gui.sbmlcore.Parameters;
import biomodel.gui.sbmlcore.Reactions;
import biomodel.gui.sbmlcore.Rules;
import biomodel.network.AbstractionEngine;
import biomodel.network.GeneticNetwork;
import biomodel.network.Promoter;
import biomodel.network.SpeciesInterface;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;
import biomodel.util.UndoManager;
import biomodel.util.Utility;
import lpn.parser.ExprTree;
import lpn.parser.LhpnFile;
import lpn.parser.Place;
import lpn.parser.Transition;
import lpn.parser.Variable;
import main.Gui;
import main.Log;
import main.util.MutableString;


/**
 * This class describes a GCM file
 * 
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class BioModel {

	private String separator;

	private String filename = null;

	private UndoManager undoManager;

	public BioModel(String path) {
		//gcm2sbml = new GCM2SBML(this);
		undoManager = new UndoManager();
		separator = Gui.separator;
		this.path = path;
		grid = new Grid();
		compartments = new HashMap<String, Properties>();
	}
	
	public void createSBMLDocument(String modelId,boolean grid,boolean lema) {
		sbml = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		modelId = modelId.replaceAll("[\\W]|_", "_");
		if (Character.isDigit(modelId.charAt(0))) modelId = "m" + modelId;
		Model m = sbml.createModel(modelId);
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, m, metaIDIndex); 
		sbmlFile = modelId + ".xml";
		createCompPlugin();
		createLayoutPlugin();
		createFBCPlugin();
		if (!lema) {
			Compartment c = m.createCompartment();
			if (grid) {
				if (!modelId.equals("Grid")) {
					c.setId("Grid");
				} else {
					c.setId("GridComp");
				}
				loadDefaultParameters();
			} else {
				if (!modelId.equals("Cell")) {
					c.setId("Cell");
				} else {
					c.setId("CellComp");
				}
				Port port = sbmlCompModel.createPort();
				port.setId(GlobalConstants.COMPARTMENT+"__"+c.getId());
				port.setIdRef(c.getId());
			}
			c.setSize(1);
			c.setSpatialDimensions(3);
			c.setConstant(true);
		}
	}

	private void loadDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();

		createGlobalParameter(GlobalConstants.FORWARD_KREP_STRING, biosimrc.get("biosim.gcm.KREP_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KREP_STRING, "1");
		
		createGlobalParameter(GlobalConstants.FORWARD_KACT_STRING, biosimrc.get("biosim.gcm.KACT_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KACT_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING, biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING, 
				biosimrc.get("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING, biosimrc.get("biosim.gcm.KCOMPLEX_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING, biosimrc.get("biosim.gcm.FORWARD_MEMDIFF_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING, biosimrc.get("biosim.gcm.REVERSE_MEMDIFF_VALUE", ""));

		createGlobalParameter(GlobalConstants.KDECAY_STRING, biosimrc.get("biosim.gcm.KDECAY_VALUE", ""));
		createGlobalParameter(GlobalConstants.KECDECAY_STRING, biosimrc.get("biosim.gcm.KECDECAY_VALUE", ""));
		createGlobalParameter(GlobalConstants.COOPERATIVITY_STRING, biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", ""));
		createGlobalParameter(GlobalConstants.RNAP_STRING, biosimrc.get("biosim.gcm.RNAP_VALUE", ""));
		createGlobalParameter(GlobalConstants.OCR_STRING, biosimrc.get("biosim.gcm.OCR_VALUE", ""));
		createGlobalParameter(GlobalConstants.KBASAL_STRING, biosimrc.get("biosim.gcm.KBASAL_VALUE", ""));
		createGlobalParameter(GlobalConstants.PROMOTER_COUNT_STRING, biosimrc.get("biosim.gcm.PROMOTER_COUNT_VALUE", ""));
		createGlobalParameter(GlobalConstants.STOICHIOMETRY_STRING, biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", ""));
		createGlobalParameter(GlobalConstants.ACTIVATED_STRING, biosimrc.get("biosim.gcm.ACTIVED_VALUE", ""));
		createGlobalParameter(GlobalConstants.KECDIFF_STRING, biosimrc.get("biosim.gcm.KECDIFF_VALUE", ""));
	}

	private void loadDefaultParameterMap() {
		Preferences biosimrc = Preferences.userRoot();
		defaultParameters = new HashMap<String,String>();

		defaultParameters.put(GlobalConstants.FORWARD_KREP_STRING, biosimrc.get("biosim.gcm.KREP_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_KREP_STRING, "1");
		
		defaultParameters.put(GlobalConstants.FORWARD_KACT_STRING, biosimrc.get("biosim.gcm.KACT_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_KACT_STRING, "1");

		defaultParameters.put(GlobalConstants.FORWARD_RNAP_BINDING_STRING, biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_RNAP_BINDING_STRING, "1");

		defaultParameters.put(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING, 
				biosimrc.get("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING, "1");

		defaultParameters.put(GlobalConstants.FORWARD_KCOMPLEX_STRING, biosimrc.get("biosim.gcm.KCOMPLEX_VALUE", ""));
		defaultParameters.put(GlobalConstants.REVERSE_KCOMPLEX_STRING, "1");

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
	}

	private void createDegradationDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();

		createGlobalParameter(GlobalConstants.KDECAY_STRING, biosimrc.get("biosim.gcm.KDECAY_VALUE", ""));
	}

	private void createDiffusionDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();

		createGlobalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING, biosimrc.get("biosim.gcm.FORWARD_MEMDIFF_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING, biosimrc.get("biosim.gcm.REVERSE_MEMDIFF_VALUE", ""));
		createGlobalParameter(GlobalConstants.KECDECAY_STRING, biosimrc.get("biosim.gcm.KECDECAY_VALUE", ""));
		createGlobalParameter(GlobalConstants.KECDIFF_STRING, biosimrc.get("biosim.gcm.KECDIFF_VALUE", ""));
	}

	private void createComplexDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();

		createGlobalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING, biosimrc.get("biosim.gcm.KCOMPLEX_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING, "1");
		createGlobalParameter(GlobalConstants.COOPERATIVITY_STRING, biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", ""));
	}

	private void createConstitutiveDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();

		createGlobalParameter(GlobalConstants.STOICHIOMETRY_STRING, biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", ""));
		createGlobalParameter(GlobalConstants.OCR_STRING, biosimrc.get("biosim.gcm.OCR_VALUE", ""));
	}
	
	private void createProductionDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();

		createGlobalParameter(GlobalConstants.FORWARD_KREP_STRING, biosimrc.get("biosim.gcm.KREP_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KREP_STRING, "1");
		
		createGlobalParameter(GlobalConstants.FORWARD_KACT_STRING, biosimrc.get("biosim.gcm.KACT_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KACT_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING, biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING, 
				biosimrc.get("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING, "1");

		createGlobalParameter(GlobalConstants.COOPERATIVITY_STRING, biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", ""));
		createGlobalParameter(GlobalConstants.RNAP_STRING, biosimrc.get("biosim.gcm.RNAP_VALUE", ""));
		createGlobalParameter(GlobalConstants.OCR_STRING, biosimrc.get("biosim.gcm.OCR_VALUE", ""));
		createGlobalParameter(GlobalConstants.KBASAL_STRING, biosimrc.get("biosim.gcm.KBASAL_VALUE", ""));
		createGlobalParameter(GlobalConstants.PROMOTER_COUNT_STRING, biosimrc.get("biosim.gcm.PROMOTER_COUNT_VALUE", ""));
		createGlobalParameter(GlobalConstants.STOICHIOMETRY_STRING, biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", ""));
		createGlobalParameter(GlobalConstants.ACTIVATED_STRING, biosimrc.get("biosim.gcm.ACTIVED_VALUE", ""));
	}
	
	public static boolean IsDefaultParameter(String paramId) {
		if (paramId.equals(GlobalConstants.FORWARD_KREP_STRING) ||
			paramId.equals(GlobalConstants.REVERSE_KREP_STRING) ||
			paramId.equals(GlobalConstants.FORWARD_KACT_STRING) ||
			paramId.equals(GlobalConstants.REVERSE_KACT_STRING) ||
			paramId.equals(GlobalConstants.FORWARD_KCOMPLEX_STRING) ||
			paramId.equals(GlobalConstants.REVERSE_KCOMPLEX_STRING) ||
			paramId.equals(GlobalConstants.FORWARD_RNAP_BINDING_STRING) ||
			paramId.equals(GlobalConstants.REVERSE_RNAP_BINDING_STRING) ||
			paramId.equals(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING) || 
			paramId.equals(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING) ||
			paramId.equals(GlobalConstants.FORWARD_MEMDIFF_STRING) ||
			paramId.equals(GlobalConstants.REVERSE_MEMDIFF_STRING) ||
			paramId.equals(GlobalConstants.KDECAY_STRING) ||
			paramId.equals(GlobalConstants.KECDECAY_STRING) ||
			paramId.equals(GlobalConstants.COOPERATIVITY_STRING) ||
			paramId.equals(GlobalConstants.RNAP_STRING) ||
			paramId.equals(GlobalConstants.OCR_STRING) ||
			paramId.equals(GlobalConstants.KBASAL_STRING) ||
			paramId.equals(GlobalConstants.PROMOTER_COUNT_STRING) ||
			paramId.equals(GlobalConstants.STOICHIOMETRY_STRING) ||
			paramId.equals(GlobalConstants.ACTIVATED_STRING) ||
			paramId.equals(GlobalConstants.KECDIFF_STRING)) {
			return true;
		}
		return false;
	}
	
	public static boolean IsDefaultProductionParameter(String paramId) {
		if (paramId.equals(GlobalConstants.FORWARD_KREP_STRING) ||
			paramId.equals(GlobalConstants.REVERSE_KREP_STRING) ||
			paramId.equals(GlobalConstants.FORWARD_KACT_STRING) ||
			paramId.equals(GlobalConstants.REVERSE_KACT_STRING) ||
			paramId.equals(GlobalConstants.FORWARD_RNAP_BINDING_STRING) ||
			paramId.equals(GlobalConstants.REVERSE_RNAP_BINDING_STRING) ||
			paramId.equals(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING) || 
			paramId.equals(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING) ||
			paramId.equals(GlobalConstants.COOPERATIVITY_STRING) ||
			paramId.equals(GlobalConstants.RNAP_STRING) ||
			paramId.equals(GlobalConstants.OCR_STRING) ||
			paramId.equals(GlobalConstants.KBASAL_STRING) ||
			paramId.equals(GlobalConstants.PROMOTER_COUNT_STRING) ||
			paramId.equals(GlobalConstants.STOICHIOMETRY_STRING) ||
			paramId.equals(GlobalConstants.ACTIVATED_STRING)) {
			return true;
		}
		return false;
	}
	
	public boolean IsWithinCompartment() {
		if (sbml.getModel().getCompartmentCount()==0) return false;
		for (int i = 0; i < sbml.getModel().getCompartmentCount(); i++) {
			Compartment compartment = sbml.getModel().getCompartment(i);
			if (getPortByIdRef(compartment.getId())!=null) return false;
			//if (sbmlCompModel.getListOfPorts().get(GlobalConstants.COMPARTMENT + "__" + compartment.getId()) != null) return false;
		}
		return true;
	}
	
	/*
	public void setIsWithinCompartment(boolean isWithinCompartment) {
		if (isWithinCompartment && !this.isWithinCompartment) {
			sbmlCompModel.getListOfPorts().get(GlobalConstants.DEFAULT_COMPARTMENT).setId(GlobalConstants.ENCLOSING_COMPARTMENT);
		} else if  (!isWithinCompartment && this.isWithinCompartment) {
			sbmlCompModel.getListOfPorts().get(GlobalConstants.ENCLOSING_COMPARTMENT).setId(GlobalConstants.DEFAULT_COMPARTMENT);
		}
		this.isWithinCompartment = isWithinCompartment;
	}
	*/
	
	public String getSBMLFile() {
		return sbmlFile;
	}

	public void setSBMLFile(String file) {
		sbmlFile = file;
	}

	public MySpecies getSpeciesPanel() {
		return speciesPanel;
	}

	public void setSpeciesPanel(MySpecies speciesPanel) {
		this.speciesPanel = speciesPanel;
	}

	public Compartments getCompartmentPanel() {
		return compartmentPanel;
	}

	public void setCompartmentPanel(Compartments compartmentPanel) {
		this.compartmentPanel = compartmentPanel;
	}

	public Reactions getReactionPanel() {
		return reactionPanel;
	}

	public void setReactionPanel(Reactions reactionPanel) {
		this.reactionPanel = reactionPanel;
	}

	public Rules getRulePanel() {
		return rulePanel;
	}

	public void setRulePanel(Rules rulePanel) {
		this.rulePanel = rulePanel;
	}

	public Constraints getConstraintPanel() {
		return constraintPanel;
	}

	public void setConstraintPanel(Constraints constraintPanel) {
		this.constraintPanel = constraintPanel;
	}

	public Events getEventPanel() {
		return eventPanel;
	}

	public void setEventPanel(Events eventPanel) {
		this.eventPanel = eventPanel;
	}

	public Parameters getParameterPanel() {
		return parameterPanel;
	}

	public void setParameterPanel(Parameters parameterPanel) {
		this.parameterPanel = parameterPanel;
	}

	public SBMLDocument getSBMLDocument() {
		return sbml;
	}

	
	public FBCModelPlugin getSBMLFBC() {
		return sbmlFBC;
	}
	
	public LayoutModelPlugin getSBMLLayout() {
		return sbmlLayout;
	}

	public CompSBMLDocumentPlugin getSBMLComp() {
		return sbmlComp;
	}

	public CompModelPlugin getSBMLCompModel() {
		return sbmlCompModel;
	}

	public void setSBMLDocument(SBMLDocument sbmlDoc) {
		sbml = sbmlDoc;
		SBMLutilities.fillBlankMetaIDs(sbml);
	}

	public void setSBMLLayout(LayoutModelPlugin sbmlLayout) {
		this.sbmlLayout = sbmlLayout;
	}

	public void setSBMLComp(CompSBMLDocumentPlugin sbmlComp) {
		this.sbmlComp = sbmlComp;
	}

	public void setSBMLCompModel(CompModelPlugin sbmlCompModel) {
		this.sbmlCompModel = sbmlCompModel;
	}

	private static ArrayList<String> copyArray(ArrayList<String> original) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String element : original) {
			copy.add(element);
		}
		return copy;
	}
	
	private static double parseValue(String value) {
		if (value == null) return 0.0;
		if (value.contains("/")) {
			String[] parts = value.split("/");
			return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
		}
		return Double.parseDouble(value);
	}

	public LhpnFile convertToLHPN(ArrayList<String> specs, ArrayList<Object[]> conLevel, MutableString lpnProperty) {
		GCMParser parser = new GCMParser(this);
		SBMLDocument sbml = this.flattenModel(true);		
		GeneticNetwork network = parser.buildNetwork(sbml);
		if (network == null) return null;
		network.markAbstractable();
		AbstractionEngine abs = network.createAbstractionEngine();
		ArrayList<String> biochemical = getBiochemicalSpecies();
		LhpnFile LHPN = new LhpnFile();
		for (int i = 0; i < specs.size(); i++) {
			if (sbml.getModel().getSpecies(specs.get(i))==null) continue;
			double initial = parseValue(getParameter(GlobalConstants.INITIAL_STRING));
			double selectedThreshold = 0;
			try {
				if (sbml.getModel().getSpecies(specs.get(i)).isSetInitialAmount()) {
					initial = sbml.getModel().getSpecies(specs.get(i)).getInitialAmount();
				} else {
					initial = sbml.getModel().getSpecies(specs.get(i)).getInitialConcentration();
				}
			}
			catch (Exception e) {
			}
			double difference = Math.abs(selectedThreshold - initial);
			for (Object threshold : conLevel.get(i)) {
				double thisThreshold = Double.parseDouble((String) threshold);
				double diff = Math.abs(thisThreshold - initial);
				if (diff < difference) {
					difference = diff;
					selectedThreshold = thisThreshold;
				}
			}
			LHPN.addInteger(specs.get(i), "" + ((int) selectedThreshold));
		}
		for (String input : getInputSpecies()) {
			double value;
			try {
				if (sbml.getModel().getSpecies(input).isSetInitialAmount()) {
					value = sbml.getModel().getSpecies(input).getInitialAmount();
				} else {
					value = sbml.getModel().getSpecies(input).getInitialConcentration();
				}
			}
			catch (Exception e) {
				value = 0;
			}
			LHPN.addInteger(input, "" + value);
		}
		for (String species : getSpecies()) {
			if (!specs.contains(species) && !getInputSpecies().contains(species)) {
				double value;
				try {
					if (sbml.getModel().getSpecies(species).isSetInitialAmount()) {
						value = sbml.getModel().getSpecies(species).getInitialAmount();
					} else {
						value = sbml.getModel().getSpecies(species).getInitialConcentration();
					}
				}
				catch (Exception e) {
					value = 0;
				}
				LHPN.addInteger(species, "" + value);				
			}
		}
		for (int i = 0; i < specs.size(); i++) {
			if (sbml.getModel().getSpecies(specs.get(i))==null) continue;
			if (!biochemical.contains(specs.get(i)) && !getInputSpecies().contains(specs.get(i))) {
				int placeNum = 0;
				int transNum = 0;
				String previousPlaceName = specs.get(i) + placeNum;
				placeNum++;
				if (LHPN.getIntegers().get(specs.get(i)).equals("0")) {
					LHPN.addPlace(previousPlaceName, true);
				}
				else {
					LHPN.addPlace(previousPlaceName, false);
				}
				String number = "0";
				for (Object threshold : conLevel.get(i)) {
					if (LHPN.getIntegers().get(specs.get(i)).equals("" + ((int) Double.parseDouble((String) threshold)))) {
						LHPN.addPlace(specs.get(i) + placeNum, true);
					}
					else {
						LHPN.addPlace(specs.get(i) + placeNum, false);
					}
					if (!(isInput(specs.get(i)))) {
						ArrayList<String> proms = new ArrayList<String>();
						Double global_np = parseValue(getParameter(GlobalConstants.STOICHIOMETRY_STRING));
						Double global_kd = parseValue(getParameter(GlobalConstants.KDECAY_STRING));
						Double np = global_np;
						Double kd = global_kd;
						for (Promoter p : network.getPromoters().values()) {
							for (SpeciesInterface s : p.getOutputs()) {
								if (s.getId().equals(specs.get(i))) {
									proms.add(p.getId());
								}
							}
							
						}
						String rate = "";
						for (String promoter : proms) {
							String promRate = abs.abstractOperatorSite(network.getPromoters().get(promoter));
//							for (String species : getSpecies()) {
//								if (promRate.contains(species) && !LHPN.getIntegers().keySet().contains(species)) {
//									double value;
//									try {
//										if (sbml.getModel().getSpecies(species).isSetInitialAmount()) {
//											value = sbml.getModel().getSpecies(species).getInitialAmount();
//										} else {
//											value = sbml.getModel().getSpecies(species).getInitialConcentration();
//										}
//									}
//									catch (Exception e) {
//										value = 0;
//									}
//									LHPN.addInteger(species, "" + value);
//								}
//							}
							if (rate.equals("")) {
								rate = "(" + promRate + ")";
							}
							else {
								rate += "+(" + promRate + ")";
							}
						}
						if (!rate.equals("")) {
							rate = "(" + np + "*(" + rate + "))";
						}
						String reactionProductions = "";
						String reactionDegradations = "";
						ListOf<Reaction> reactions = sbml.getModel().getListOfReactions();
						for (int j = 0; j < sbml.getModel().getReactionCount(); j++) {
							Reaction r = reactions.get(j);
							if (!BioModel.isProductionReaction(r) && !BioModel.isDegradationReaction(r)
									&& !BioModel.isComplexReaction(r)) {
								for (int k = 0; k < r.getReactantCount(); k++) {
									if (r.getReactant(k).getSpecies().equals(specs.get(i))) {
										KineticLaw law = r.getKineticLaw();
										HashMap<String, String> parameters = new HashMap<String, String>();
										for (String parameter : getParameters()) {
											parameters.put(parameter, getParameter(parameter));
										}
										for (int l = 0; l < law.getLocalParameterCount(); l++) {
											parameters.put(law.getLocalParameter(l).getId(), ""
													+ law.getLocalParameter(l).getValue());
										}
										if (r.isSetReversible() && law.getMath().getCharacter() == '-') {
											reactionDegradations += "(("
													+ SBMLutilities.myFormulaToString(replaceParams(law.getMath().getLeftChild(),
															parameters)) + ")*" + r.getReactant(k).getStoichiometry()
													+ ") + ";
											reactionProductions += "(("
													+ SBMLutilities.myFormulaToString(replaceParams(law.getMath().getRightChild(),
															parameters)) + ")*" + r.getReactant(k).getStoichiometry()
													+ ") + ";
										}
										else {
											reactionDegradations += "(("
													+ SBMLutilities.myFormulaToString(replaceParams(law.getMath(),
															parameters)) + ")*" + r.getReactant(k).getStoichiometry()
													+ ") + ";
										}
									}
								}
								for (int k = 0; k < r.getProductCount(); k++) {
									if (r.getProduct(k).getSpecies().equals(specs.get(i))) {
										KineticLaw law = r.getKineticLaw();
										HashMap<String, String> parameters = new HashMap<String, String>();
										for (String parameter : getParameters()) {
											parameters.put(parameter, getParameter(parameter));
										}
										for (int l = 0; l < law.getLocalParameterCount(); l++) {
											parameters.put(law.getLocalParameter(l).getId(), ""
													+ law.getLocalParameter(l).getValue());
										}
										if (r.isSetReversible() && law.getMath().getCharacter() == '-') {
											reactionProductions += "(("
													+ SBMLutilities.myFormulaToString(replaceParams(law.getMath().getLeftChild(),
															parameters)) + ")*" + r.getProduct(k).getStoichiometry()
													+ ") + ";
											reactionDegradations += "(("
													+ SBMLutilities.myFormulaToString(replaceParams(law.getMath().getRightChild(),
															parameters)) + ")*" + r.getProduct(k).getStoichiometry()
													+ ") + ";
										}
										else {
											reactionProductions += "(("
													+ SBMLutilities.myFormulaToString(replaceParams(law.getMath(),
															parameters)) + ")*" + r.getProduct(k).getStoichiometry()
													+ ") + ";
										}
									}
								}
							}
						}
						if (!reactionProductions.equals("")) {
							reactionProductions = reactionProductions.substring(0, reactionProductions.length() - 3);
							reactionProductions = reactionProductions.replaceAll(" ", "");
						}
						if (!reactionDegradations.equals("")) {
							reactionDegradations = reactionDegradations.substring(0, reactionDegradations.length() - 3);
							reactionDegradations = reactionDegradations.replaceAll(" ", "");
						}
						if (!rate.equals("") || !reactionProductions.equals("")) {
							if (rate.equals("")) {
								rate = "(" + reactionProductions + ")";
							}
							else if (!reactionProductions.equals("")) {
								rate = "(" + rate + "+" + reactionProductions + ")";
							}
							LHPN.addTransition(specs.get(i) + "_trans" + transNum);
							LHPN.addMovement(previousPlaceName, specs.get(i) + "_trans" + transNum);
							LHPN.addMovement(specs.get(i) + "_trans" + transNum, specs.get(i) + placeNum);
							LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i), (String) threshold);
							LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, rate + "/" + "(" + threshold
									+ "-" + number + "))");
							transNum++;
						}
						String specExpr = specs.get(i);
						if (network.getSpecies().get(specs.get(i)).isSequesterable()) {
							specExpr = abs.sequesterSpecies(specs.get(i), 0, false);
						}
						else if (network.getComplexMap().containsKey(specs.get(i))) {
							specExpr = abs.abstractComplex(specs.get(i), 0, false);
						}
						if (!specExpr.equals(specs.get(i))) {
							specExpr = "(" + specExpr + ")";
						}
						kd = global_kd;
						Reaction reaction = getDegradationReaction(specs.get(i));
						if (reaction != null) {
							LocalParameter param = reaction.getKineticLaw().getLocalParameter(
									GlobalConstants.KDECAY_STRING);
							if (param != null) {
								kd = param.getValue();
							}
							LHPN.addTransition(specs.get(i) + "_trans" + transNum);
							LHPN.addMovement(specs.get(i) + placeNum, specs.get(i) + "_trans" + transNum);
							LHPN.addMovement(specs.get(i) + "_trans" + transNum, previousPlaceName);
							LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i), number);
							if (!reactionDegradations.equals("")) {
								LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, "((" + specExpr + "*" + kd + ")"
										+ "+" + reactionDegradations + ")/" + "(" + threshold + "-" + number + ")");
							}
							else {
								LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, "(" + specExpr + "*" + kd + ")/"
										+ "(" + threshold + "-" + number + ")");
							}
							transNum++;
						}
						else if (!reactionDegradations.equals("")) {
							LHPN.addTransition(specs.get(i) + "_trans" + transNum);
							LHPN.addMovement(specs.get(i) + placeNum, specs.get(i) + "_trans" + transNum);
							LHPN.addMovement(specs.get(i) + "_trans" + transNum, previousPlaceName);
							LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i), number);
							LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, "(" + reactionDegradations
									+ ")/" + "(" + threshold + "-" + number + ")");
							transNum++;
						}
					}
					previousPlaceName = specs.get(i) + placeNum;
					placeNum++;
					number = (String) threshold;
				}
			}
		}
		if (lpnProperty.getString() != null) {
			ArrayList<String> sortedSpecies = new ArrayList<String>();
			for (String s : getSpecies()) {
				sortedSpecies.add(s);
			}
			int i, j;
			String index;
			for (i = 1; i < sortedSpecies.size(); i++) {
				index = sortedSpecies.get(i);
				j = i;
				while ((j > 0) && sortedSpecies.get(j - 1).length() < index.length()) {
					sortedSpecies.set(j, sortedSpecies.get(j - 1));
					j--;
				}
				sortedSpecies.set(j, index);
			}
			for (String s : sortedSpecies) {
				String replace = null;
				if (network.getSpecies().get(s).isSequesterable()) {
					replace = abs.sequesterSpecies(s, 0, false);
				}
				else if (network.getComplexMap().containsKey(s)) {
					replace = abs.abstractComplex(s, 0, false);
				}
				if (replace != null && lpnProperty.getString().contains(s)) {
					replace = "(" + replace + ")";
					lpnProperty.setString(lpnProperty.getString().replaceAll(s, replace));
				}
			}
			LHPN.addProperty(lpnProperty.getString());
		}
		return LHPN;
	}
	
	public void convertLPN2PRISM(Log log,FileWriter logFile,LhpnFile LPN,String filename) {
		File file = new File(filename);
		log.addText("Saving SBML file as PRISM file:\n" + filename + "\n");
		try {
			logFile.write("Saving SBML file as PRISM file:\n" + filename + "\n\n");
			FileWriter out = new FileWriter(file);
			out.write("ctmc\n");
			for (String var : LPN.getVariables()) {
				int i=0;
				Place place;
				String lastValue="";
				while ((place = LPN.getPlace(var+i))!=null) {
					Transition inTrans = place.getPreset()[0];
					ExprTree assign = inTrans.getAssignTree(var);
					lastValue = assign.toString();
					i++;
				}
				Variable variable = LPN.getVariable(var);
				String initValue = variable.getInitValue();
				initValue = Long.toString((Math.round(Double.valueOf(initValue))));
				if (lastValue.equals("")) {
					out.write("const int "+var+"="+initValue+";\n");
				} else {
					out.write("module "+var+"_def\n");
					out.write("  "+var+" : "+"[0.."+lastValue+"] init "+initValue+";\n");
					i=0;
					while ((place = LPN.getPlace(var+i))!=null) {
						Transition inTrans = place.getPreset()[0];
						ExprTree assign = inTrans.getAssignTree(var);
						out.write("  [] "+var+"="+assign.toString()+" -> ");
						boolean first = true;
						for (Transition outTrans : place.getPostset()) {
							assign = outTrans.getAssignTree(var);
							ExprTree delay = outTrans.getDelayTree();
							String rate = delay.toString("prism");
							rate = rate.replace("exponential", "");
							if (!first) out.write(" + ");
							out.write(rate+":("+var+"'="+assign.toString()+")");
							first = false;
						}
						out.write(";\n");
						i++;
					}
					out.write("endmodule\n");
				}
			}
			out.close();
			for (int i = 0; i < sbml.getModel().getConstraintCount(); i++) {
				file = new File(filename.replace(".prism", ".pctl"));
				log.addText("Saving PRISM Property file:\n" + filename + "\n");
				logFile.write("Saving PRISM Property file:\n" + filename + "\n\n");
				out = new FileWriter(file);
				out.write(SBMLutilities.convertMath2PrismProperty(sbml.getModel().getConstraint(i).getMath()));
				out.close();
			}
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame, "I/O error when writing PRISM file","Error Writing File", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private ASTNode replaceParams(ASTNode formula, HashMap<String,String> parameters) {
		if (formula.getChildCount() > 0) {
			for (int i = 0; i < formula.getChildCount(); i ++) {
				formula.replaceChild(i, replaceParams(formula.getChild(i), parameters));
			}
		}
		else if (parameters.keySet().contains(formula.getName())) {
			return SBMLutilities.myParseFormula(parameters.get(formula.getName()));
		}
		return formula;
	}

	public void createCondition(String s,int condition) {
		for (int i = 0; i < sbml.getModel().getConstraintCount(); i++) {
			if (sbml.getModel().getConstraint(i).isSetMetaId() &&
				sbml.getModel().getConstraint(i).getMetaId().equals("Condition_"+condition)) return;
		}
		s = s.replace("<="," less than or equal ");
		s = s.replace(">="," greater than or equal ");
		s = s.replace("<"," less than ");
		s = s.replace(">"," greater than ");
		s = s.replace("&"," and ");
		s = s.replace("|"," or ");
		XMLNode xmlNode;
		try {
			xmlNode = XMLNode.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
					+ s.trim() + "</p></message>");
			Constraint c = sbml.getModel().createConstraint();
			SBMLutilities.setMetaId(c, "Condition_"+condition);
			c.setMath(SBMLutilities.myParseFormula("true"));
			c.setMessage(xmlNode);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
	}

	public void createGlobalParameter(String global,String value) {
		if (!global.equals("") && sbml.getModel().getParameter(global)==null) {
			Parameter parameter = sbml.getModel().createParameter();
			parameter.setId(global);
			if (CompatibilityFixer.getGuiName(global)!=null) 
				parameter.setName(CompatibilityFixer.getGuiName(global));
			parameter.setValue(Double.parseDouble(value));
			parameter.setConstant(true);
		}
	}
	
	public String getDefaultCompartment() {
		if (sbml.getModel().getCompartmentCount() > 0) {
			return sbml.getModel().getCompartment(0).getId();
		}
		return "";	
	}
	
	// Note this doesn't appear to be used anywhere
	public void createSpeciesFromGCM(String s,Properties property) {
		Species species = sbml.getModel().getSpecies(s);
		if (species==null) {
			species = sbml.getModel().createSpecies();
			species.setId(s);
			species.setCompartment(getDefaultCompartment());
			species.setBoundaryCondition(false);
			species.setConstant(false);
			species.setInitialAmount(0);
			species.setHasOnlySubstanceUnits(true);
		}
		if (property.containsKey(GlobalConstants.INITIAL_STRING)) {
			String initialString = property.getProperty(GlobalConstants.INITIAL_STRING);
			if (Utility.isValid(initialString, Utility.NUMstring)) {
				species.setInitialAmount(Double.parseDouble(initialString));
			} 
			else if (Utility.isValid(initialString, Utility.CONCstring)) {
				species.setInitialConcentration(Double.parseDouble(initialString.substring(1,initialString.length()-1)));
			} 
		}
		boolean onPort = false;
		if (property.containsKey(GlobalConstants.TYPE)) {
			String type = property.getProperty(GlobalConstants.TYPE).replace("diffusible","").replace("constitutive","");
			createDirPort(species.getId(),type);
			if (type.equals(GlobalConstants.INPUT)) {
				species.setBoundaryCondition(true);
			}
			onPort = (type.equals(GlobalConstants.INPUT)||type.equals(GlobalConstants.OUTPUT));
		} 
		double kd = -1;
		if (property.containsKey(GlobalConstants.KDECAY_STRING)) {
			kd = Double.parseDouble(property.getProperty(GlobalConstants.KDECAY_STRING));
		} 
		if (kd != 0) {
			createDegradationReaction(s,kd,null,onPort,null);
		} 
		if (property.containsKey(GlobalConstants.TYPE) && 
			property.getProperty(GlobalConstants.TYPE).contains("diffusible")) {
			createDiffusionReaction(s,property.getProperty(GlobalConstants.MEMDIFF_STRING),onPort,null);
		}
		if (property.containsKey(GlobalConstants.TYPE) && 
				property.getProperty(GlobalConstants.TYPE).contains("constitutive")) {
			createConstitutiveReaction(s,null, null, onPort, null);
		}
	}
	
	// Note this doesn't appear to be used anywhere
	public void createPromoterFromGCM(String s,Properties property) {
		Species promoter = sbml.getModel().getSpecies(s);
		if (promoter==null) {
			promoter = sbml.getModel().createSpecies();
			promoter.setId(s);
		}
		if (property != null && property.containsKey(GlobalConstants.PROMOTER_COUNT_STRING)) {
			promoter.setInitialAmount(Double.parseDouble(property.getProperty(GlobalConstants.PROMOTER_COUNT_STRING)));
		} else {
			promoter.setInitialAmount(sbml.getModel().getParameter(GlobalConstants.PROMOTER_COUNT_STRING).getValue());
		} 
		promoter.setCompartment(getDefaultCompartment());
		promoter.setBoundaryCondition(false);
		promoter.setConstant(false);
		promoter.setHasOnlySubstanceUnits(true);
		promoter.setSBOTerm(GlobalConstants.SBO_PROMOTER_BINDING_REGION);
		if (property==null) {
			createProductionReaction(s,GlobalConstants.ACTIVATED_STRING,
					GlobalConstants.STOICHIOMETRY_STRING,
					GlobalConstants.OCR_STRING,
					GlobalConstants.KBASAL_STRING,
					GlobalConstants.RNAP_BINDING_STRING,
					GlobalConstants.ACTIVATED_RNAP_BINDING_STRING,false,null);
		} else {
			createProductionReaction(s,property.getProperty(GlobalConstants.ACTIVATED_STRING),
					property.getProperty(GlobalConstants.STOICHIOMETRY_STRING),
					property.getProperty(GlobalConstants.OCR_STRING),
					property.getProperty(GlobalConstants.KBASAL_STRING),
					property.getProperty(GlobalConstants.RNAP_BINDING_STRING),
					property.getProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING),false,null);
		}
	}
	
	private void convertLayout(Layout layout) {
		for (int i = 0; i < layout.getNumTextGlyphs(); i++) {
			TextGlyph textGlyph = layout.getTextGlyph(i);
			CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(textGlyph.getGraphicalObject());
			if (compartmentGlyph!=null) {
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+compartmentGlyph.getCompartment());
				textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+compartmentGlyph.getCompartment());
				continue;
			} 
			SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(textGlyph.getGraphicalObject());
			if (speciesGlyph!=null) {
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+speciesGlyph.getSpecies());
				textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+speciesGlyph.getSpecies());
				continue;
			}
			ReactionGlyph reactionGlyph = layout.getReactionGlyph(textGlyph.getGraphicalObject());
			if (reactionGlyph!=null) {
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+reactionGlyph.getReaction());
				textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+reactionGlyph.getReaction());
				continue;
			}
		}
		for (int i = 0; i < layout.getNumCompartmentGlyphs(); i++) {
			CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(i);
			compartmentGlyph.setId(GlobalConstants.GLYPH+"__"+compartmentGlyph.getCompartment());
		}
		for (int i = 0; i < layout.getNumSpeciesGlyphs(); i++) {
			SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(i);
			speciesGlyph.setId(GlobalConstants.GLYPH+"__"+speciesGlyph.getSpecies());
		}
		int i = 0; 
		while (i < layout.getNumReactionGlyphs()) {
			ReactionGlyph reactionGlyph = layout.getReactionGlyph(i);
			reactionGlyph.setId(GlobalConstants.GLYPH+"__"+reactionGlyph.getReaction());
			if (!reactionGlyph.isSetBoundingBox()) {
				reactionGlyph.removeFromParent();
			} else {
				i++;
			}
		}
	}
	
	public Layout getLayout() {
		if (sbmlLayout==null) {
			sbmlLayout = SBMLutilities.getLayoutModelPlugin(sbml.getModel());
		}
		Layout layout;
		if (sbmlLayout.getLayoutCount()>0) {
			layout = sbmlLayout.getLayout(0);
			if (!layout.getId().endsWith("iBioSim")) {
				convertLayout(layout);
			} 
		} else {
			layout = sbmlLayout.createLayout();
			layout.createDimensions(0, 0, 0);
		}
		layout.setId("iBioSim");
		return layout;
	}
		
	public void placeSpecies(String s,Double x,Double y,Double h,Double w) {
		Layout layout = getLayout();
		SpeciesGlyph speciesGlyph;
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s)!=null) {
			speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s);
		} else {
			speciesGlyph = layout.createSpeciesGlyph(GlobalConstants.GLYPH+"__"+s);
			speciesGlyph.setSpecies(s);
		}
		//SBMLutilities.copyDimensionsIndices(sbml.getModel().getSpecies(s), speciesGlyph, "layout:species");
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.createPosition();
		boundingBox.getPosition().setX(x);
		boundingBox.getPosition().setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		if (layout.getDimensions() == null) {
			layout.createDimensions(0, 0, 0);
		}
		if (layout.getDimensions().getWidth() < x+w) {
			layout.getDimensions().setWidth(x+w);
		}
		if (layout.getDimensions().getHeight() < y+h) {
			layout.getDimensions().setHeight(y+h);
		}
		speciesGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s)!=null) {
			textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
		} else {
			textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
			textGlyph.createBoundingBox();
			textGlyph.getBoundingBox().createDimensions();
			textGlyph.getBoundingBox().createPosition();
		}
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+s);
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+s);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,s));
		textGlyph.setBoundingBox(speciesGlyph.getBoundingBox().clone());
		//SBMLutilities.copyDimensionsIndices(speciesGlyph, textGlyph, "layout:graphicalObject");
	}

	public void placeReaction(String s,Double x,Double y,Double h,Double w) {
		Layout layout = getLayout();
		ReactionGlyph reactionGlyph;
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+s)!=null) {
			reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+s);
		} else {
			reactionGlyph = layout.createReactionGlyph(GlobalConstants.GLYPH+"__"+s);
		}
		reactionGlyph.setId(GlobalConstants.GLYPH+"__"+s);
		reactionGlyph.setReaction(s);
		//SBMLutilities.copyDimensionsIndices(sbml.getModel().getReaction(s), reactionGlyph, "layout:reaction");
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.createPosition();
		boundingBox.getPosition().setX(x);
		boundingBox.getPosition().setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		if (layout.getDimensions().getWidth() < x+w) {
			layout.getDimensions().setWidth(x+w);
		}
		if (layout.getDimensions().getHeight() < y+h) {
			layout.getDimensions().setHeight(y+h);
		}
		reactionGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s)!=null) {
			textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
		} else {
			textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
			textGlyph.createBoundingBox();
			textGlyph.getBoundingBox().createDimensions();
			textGlyph.getBoundingBox().createPosition();
		}
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+s);
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+s);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,s));
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox().clone());
		//SBMLutilities.copyDimensionsIndices(reactionGlyph, textGlyph, "layout:graphicalObject");
	}

	public void placeCompartment(String s,Double x,Double y,Double h,Double w) {
		Layout layout = getLayout();
		CompartmentGlyph compartmentGlyph;
		if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+s)!=null) {
			compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+s);
		} else {
			compartmentGlyph = layout.createCompartmentGlyph(GlobalConstants.GLYPH+"__"+s);
		}
		compartmentGlyph.setId(GlobalConstants.GLYPH+"__"+s);
		compartmentGlyph.setCompartment(s);
		//SBMLutilities.copyDimensionsIndices(sbml.getModel().getCompartment(s), compartmentGlyph, "layout:compartment");
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.createPosition();
		boundingBox.getPosition().setX(x);
		boundingBox.getPosition().setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		if (layout.getDimensions().getWidth() < x+w) {
			layout.getDimensions().setWidth(x+w);
		}
		if (layout.getDimensions().getHeight() < y+h) {
			layout.getDimensions().setHeight(y+h);
		}
		compartmentGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s)!=null) {
			textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
		} else {
			textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
			textGlyph.createBoundingBox();
			textGlyph.getBoundingBox().createDimensions();
			textGlyph.getBoundingBox().createPosition();
		}
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+s);
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+s);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,s));
		textGlyph.setBoundingBox(compartmentGlyph.getBoundingBox().clone());
		//SBMLutilities.copyDimensionsIndices(compartmentGlyph, textGlyph, "layout:graphicalObject");
	}
	
	public void placeGeneral(String s,Double x,Double y,Double h,Double w,String metaidRef) {
		Layout layout = getLayout();
		GeneralGlyph generalGlyph;
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+s)!=null) {
			generalGlyph = (GeneralGlyph)layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+s);
		} else {
			generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+s);
			generalGlyph.createBoundingBox();
			generalGlyph.getBoundingBox().createDimensions();
			generalGlyph.getBoundingBox().createPosition();
			if (metaidRef == null) {
				generalGlyph.setReference(s);
				//SBMLutilities.copyDimensionsIndices(SBMLutilities.getElementBySId(sbml.getModel(),s), generalGlyph, 
				//		"layout:reference");
			} else {
				generalGlyph.setMetaidRef(metaidRef);
				//SBMLutilities.copyDimensionsIndices(SBMLutilities.getElementByMetaId(sbml.getModel(),metaidRef), generalGlyph, 
				//		"layout:metaidRef");
			}
		}
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.createPosition();
		boundingBox.getPosition().setX(x);
		boundingBox.getPosition().setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		if (layout.getDimensions() == null) {
			layout.createDimensions(0, 0, 0);
		}
		if (layout.getDimensions().getWidth() < x+w) {
			layout.getDimensions().setWidth(x+w);
		}
		if (layout.getDimensions().getHeight() < y+h) {
			layout.getDimensions().setHeight(y+h);
		}
		generalGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s)!=null) {
			textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
		} else {
			textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
			textGlyph.createBoundingBox();
			textGlyph.getBoundingBox().createDimensions();
			textGlyph.getBoundingBox().createPosition();
		}
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+s);
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+s);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,s));
		textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());
		//SBMLutilities.copyDimensionsIndices(generalGlyph, textGlyph, "layout:graphicalObject");
	}
	
	public void updateLayoutDimensions() {
		Layout layout = getLayout();
		for (int i=0; i<layout.getListOfCompartmentGlyphs().size(); i++ ) {
			CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(i);
			if (compartmentGlyph.getBoundingBox().getPosition().getX() +
			    compartmentGlyph.getBoundingBox().getDimensions().getWidth() >
				layout.getDimensions().getWidth()) {
					layout.getDimensions().setWidth(compartmentGlyph.getBoundingBox().getPosition().getX() +
							compartmentGlyph.getBoundingBox().getDimensions().getWidth());
			}
			if (compartmentGlyph.getBoundingBox().getPosition().getY() +
				    compartmentGlyph.getBoundingBox().getDimensions().getHeight() >
					layout.getDimensions().getHeight()) {
						layout.getDimensions().setHeight(compartmentGlyph.getBoundingBox().getPosition().getY() +
								compartmentGlyph.getBoundingBox().getDimensions().getHeight());
			}
		}
		for (int i=0; i<layout.getListOfSpeciesGlyphs().size(); i++ ) {
			SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(i);
			if (speciesGlyph.getBoundingBox().getPosition().getX() +
			    speciesGlyph.getBoundingBox().getDimensions().getWidth() >
				layout.getDimensions().getWidth()) {
					layout.getDimensions().setWidth(speciesGlyph.getBoundingBox().getPosition().getX() +
							speciesGlyph.getBoundingBox().getDimensions().getWidth());
			}
			if (speciesGlyph.getBoundingBox().getPosition().getY() +
				    speciesGlyph.getBoundingBox().getDimensions().getHeight() >
					layout.getDimensions().getHeight()) {
						layout.getDimensions().setHeight(speciesGlyph.getBoundingBox().getPosition().getY() +
								speciesGlyph.getBoundingBox().getDimensions().getHeight());
			}
		}
		for (int i=0; i<layout.getListOfReactionGlyphs().size(); i++ ) {
			ReactionGlyph reactionGlyph = layout.getReactionGlyph(i);
			if (reactionGlyph.getBoundingBox().getPosition().getX() +
			    reactionGlyph.getBoundingBox().getDimensions().getWidth() >
				layout.getDimensions().getWidth()) {
					layout.getDimensions().setWidth(reactionGlyph.getBoundingBox().getPosition().getX() +
							reactionGlyph.getBoundingBox().getDimensions().getWidth());
			}
			if (reactionGlyph.getBoundingBox().getPosition().getY() +
				    reactionGlyph.getBoundingBox().getDimensions().getHeight() >
					layout.getDimensions().getHeight()) {
						layout.getDimensions().setHeight(reactionGlyph.getBoundingBox().getPosition().getY() +
								reactionGlyph.getBoundingBox().getDimensions().getHeight());
			}
		}
	}
	
	public void setGridSize(int rows, int cols) {
		
		//get the grid compartment
		Compartment gridComp = sbml.getModel().getCompartment(0);
		if (gridComp != null) {
			if (rows > 0 && cols > 0) {
				AnnotationUtility.setGridAnnotation(gridComp, rows, cols);
			} 
			else {
				AnnotationUtility.removeGridAnnotation(gridComp);
			}
		}
	}
	
	public void createComponentFromGCM(String s,Properties prop) {
		ExternalModelDefinition extModel = null;
		String extId = prop.getProperty("gcm").replace(".gcm","");
		if (sbmlComp.getListOfExternalModelDefinitions().get(extId)==null) { 	
			extModel = sbmlComp.createExternalModelDefinition();
		} else {
			extModel = sbmlComp.getListOfExternalModelDefinitions().get(extId);
		}
		extModel.setId(extId);
		extModel.setSource(prop.getProperty("gcm").replace(".gcm",".xml"));

		String submodelID = s;
		
		//if a gridded/arrayed submodel exists, it'll have this ID
		String gridSubmodelID = "GRID__" + extId;
		int row = 0;
		int col = 0;
		
		//if the submodel is not on a grid
		if (!(prop.keySet().contains("row") && prop.keySet().contains("col"))) {
			
			gridSubmodelID = submodelID + "__" + extId;
		}
		else {
			
			row = Integer.parseInt(prop.getProperty("row"));
			col = Integer.parseInt(prop.getProperty("col"));
		}
		
		Submodel potentialGridSubmodel = sbmlCompModel.getListOfSubmodels().get(gridSubmodelID);
		
		if (potentialGridSubmodel != null) {
			
			//if the annotation string already exists, then one of these existed before
			//so update its count
			int size = AnnotationUtility.parseArraySizeAnnotation(potentialGridSubmodel);
			if (size > 0 && prop.keySet().contains("row") && prop.keySet().contains("col")) {
				AnnotationUtility.setArraySizeAnnotation(potentialGridSubmodel, ++size);
			}
			else {
				AnnotationUtility.setArraySizeAnnotation(potentialGridSubmodel, 1);
			}
		}
		else {
			potentialGridSubmodel = sbmlCompModel.createSubmodel();
			potentialGridSubmodel.setId(gridSubmodelID);
			AnnotationUtility.setArraySizeAnnotation(potentialGridSubmodel, 1);
		}
		
		potentialGridSubmodel.setModelRef(extId);
		
		//add an entry to the location parameter for the external model			
		String locationParameterID = extModel.getId() + "__locations";
		
		if (!(prop.keySet().contains("row") && prop.keySet().contains("col")))
			locationParameterID = gridSubmodelID + "__locations";
		
		Parameter locationParameter = sbml.getModel().getParameter(locationParameterID);
		
		if (locationParameter == null) {
			
			locationParameter = sbml.getModel().createParameter();
			locationParameter.setId(locationParameterID);
			locationParameter.setConstant(false);
			locationParameter.setValue(0);
		}
		AnnotationUtility.appendArrayAnnotation(locationParameter, submodelID + "=\"(" + row + "," + col + ")\"");
		
		if (prop.keySet().contains("row") && prop.keySet().contains("col")
				&& prop.getProperty("compartment").equals("true"))
			createGridSpecies(gridSubmodelID);
		
		for (Object propName : prop.keySet()) {
			if (!propName.toString().equals("gcm")
					&& !propName.toString().equals(GlobalConstants.ID)
					&& prop.keySet().contains("type_" + propName)) {
				if (prop.getProperty("type_" + propName).equals("Output")) {
					String speciesId = prop.getProperty(propName.toString()).toString();
					CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(sbml.getModel().getSpecies(speciesId));
					ReplacedElement replacement = null;
					boolean found = false;
					for (int i = 0; i < sbmlSBase.getListOfReplacedElements().size(); i++) {
						replacement = sbmlSBase.getListOfReplacedElements().get(i);
						if (replacement.getSubmodelRef().equals(s) && 
							replacement.getPortRef().equals(GlobalConstants.OUTPUT+"__"+propName.toString())) {
							found = true;
							break;
						}
					}
					if (!found) {
						replacement = sbmlSBase.createReplacedElement();
						replacement.setSubmodelRef(s);
						replacement.setPortRef(GlobalConstants.OUTPUT+"__"+propName.toString());
					}
				}
				else {
					String speciesId = prop.getProperty(propName.toString()).toString();
					CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(sbml.getModel().getSpecies(speciesId));
					ReplacedElement replacement = null;
					boolean found = false;
					for (int i = 0; i < sbmlSBase.getListOfReplacedElements().size(); i++) {
						replacement = sbmlSBase.getListOfReplacedElements().get(i);
						if (replacement.getSubmodelRef().equals(s) && 
							replacement.getPortRef().equals(GlobalConstants.INPUT+"__"+propName.toString())) {
							found = true;
							break;
						}
					}
					if (!found) {
						replacement = sbmlSBase.createReplacedElement();
						replacement.setSubmodelRef(s);
						replacement.setPortRef(GlobalConstants.INPUT+"__"+propName.toString());
					}
				}
			}
		}
	}

	public void createLayoutPlugin() {
		sbml.enablePackage(LayoutConstants.namespaceURI);
		sbmlLayout = SBMLutilities.getLayoutModelPlugin(sbml.getModel());
	}

	public void createFBCPlugin() {
		sbml.enablePackage(FBCConstants.namespaceURI);
		sbmlFBC = SBMLutilities.getFBCModelPlugin(sbml.getModel());
		sbmlFBC.setStrict(false);
	}

	public void createCompPlugin() {
		sbml.enablePackage(CompConstants.namespaceURI);
		sbmlComp = SBMLutilities.getCompSBMLDocumentPlugin(sbml);
		sbmlCompModel = SBMLutilities.getCompModelPlugin(sbml.getModel());
	}
	
	public static void updateComplexParameters(Reaction r,String KcStr) {
		if (KcStr != null && KcStr.startsWith("(")) {
			KineticLaw k = r.getKineticLaw();
			LocalParameter p = k.getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING);
			if (p==null) {	
				p = k.createLocalParameter();
				p.setId(GlobalConstants.FORWARD_KCOMPLEX_STRING);
			}
			p.setValue(1.0);
			AnnotationUtility.setSweepAnnotation(p, KcStr);
			p = k.getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING);
			if (p==null) {	
				p = k.createLocalParameter();
				p.setId(GlobalConstants.REVERSE_KCOMPLEX_STRING);
			}
			p.setValue(1.0);
		} else {
			double [] Kc = Utility.getEquilibrium(KcStr); 
			if (Kc[0] >= 0) { 	
				KineticLaw k = r.getKineticLaw();
				LocalParameter p = k.getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING);
				if (p==null) {	
					p = k.createLocalParameter();
					p.setId(GlobalConstants.FORWARD_KCOMPLEX_STRING);
				}
				p.setValue(Kc[0]);
				p = k.getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING);
				if (p==null) {	
					p = k.createLocalParameter();
					p.setId(GlobalConstants.REVERSE_KCOMPLEX_STRING);
				}
				p.setValue(Kc[1]);
			}
		}		
	}
	
	public Reaction createBiochemicalReaction(String id,String SBOid,ArrayList<String> reactants,
			ArrayList<String> modifiers,ArrayList<String> products) {
		Reaction r = sbml.getModel().getReaction(id);
		if (r==null) {
			r = sbml.getModel().createReaction();
			r.setId(id);
			r.setSBOTerm(SBOid);
			r.setReversible(false);
			r.setFast(false);
			String kLaw = "k";
			for (String reactant : reactants) {
				SpeciesReference ref = r.createReactant();
				ref.setSpecies(reactant);
				ref.setStoichiometry(1);
				ref.setConstant(true);
				kLaw += "*" + reactant;
			}
			for (String modifier : modifiers) {
				ModifierSpeciesReference ref = r.createModifier();
				ref.setSpecies(modifier);
			}
			for (String product : products) {
				SpeciesReference ref = r.createProduct();
				ref.setSpecies(product);
				ref.setStoichiometry(1);
				ref.setConstant(true);
			}
			KineticLaw k = r.createKineticLaw();
			LocalParameter lp = k.createLocalParameter("k");
			lp.setValue(0.1);
			k.setMath(SBMLutilities.myParseFormula(kLaw));
		}
		return r;
	}

	public Reaction createComplexReaction(String s,String KcStr,boolean onPort) {
		createComplexDefaultParameters();
		Reaction r = getComplexReaction(s);
		if (r==null) {
			r = sbml.getModel().createReaction();
			r.setId(GlobalConstants.COMPLEXATION + "_" + s);
			r.setSBOTerm(GlobalConstants.SBO_ASSOCIATION);
			r.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
			r.setReversible(true);
			r.setFast(false);
			SpeciesReference product = r.createProduct();
			product.setSpecies(s);
			product.setStoichiometry(1);
			product.setConstant(true);		
			r.createKineticLaw();
		} else {
			KineticLaw k = r.getKineticLaw();
			if (k.getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.FORWARD_KCOMPLEX_STRING);
			}
			if (k.getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.REVERSE_KCOMPLEX_STRING);
			}
		}
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
		ArraysSBasePlugin sBasePluginSpecies = SBMLutilities.getArraysSBasePlugin(sbml.getModel().getSpecies(s));
		ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(r.getProduct(0));
		sBasePlugin.unsetListOfDimensions();
		sBasePluginProduct.unsetListOfIndices();
		String indexStr = "";
		for (int i = 0; i < sBasePluginSpecies.getListOfDimensions().size(); i++) {
			Dimension prodDim = sBasePluginSpecies.getDimensionByArrayDimension(i);
			Dimension dim = sBasePlugin.createDimension(prodDim.getId());
			dim.setSize(prodDim.getSize());
			dim.setArrayDimension(prodDim.getArrayDimension());
			Index index = sBasePluginProduct.createIndex();
			index.setReferencedAttribute("species");
			index.setArrayDimension(i);
			index.setMath(SBMLutilities.myParseFormula(dim.getId()));
			indexStr = "[" + dim.getId() + "]" + indexStr;
		}
		updateComplexParameters(r,KcStr);
		r.getKineticLaw().setMath(SBMLutilities.myParseFormula(createComplexKineticLaw(r)));
		Port port = getPortByIdRef(r.getId());
		if (port!=null) {
			if (onPort) {
				port.setId(r.getId());
				port.setIdRef(r.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());		
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			} else {
				sbmlCompModel.removePort(port);
			}
		} else {
			if (onPort) {
				port = sbmlCompModel.createPort();
				port.setId(r.getId());
				port.setIdRef(r.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());		
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			}
		}
		return r;
	}

	public Reaction addNoInfluenceToProductionReaction(String promoterId,String regulatorId,String productId) {
		Reaction r = getProductionReaction(promoterId);
		ModifierSpeciesReference modifier = r.getModifierForSpecies(regulatorId);
		if (!regulatorId.equals("none") && modifier==null) {
			modifier = r.createModifier();
			modifier.setSpecies(regulatorId);
			modifier.setSBOTerm(GlobalConstants.SBO_NEUTRAL);
		} else if (modifier != null) {
			return r;
		}
		if (modifier!=null) {
			if (SBMLutilities.dimensionsMatch(r,sbml.getModel().getSpecies(regulatorId))) {
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
				ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(modifier);
				sBasePluginProduct.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
					Index index = sBasePluginProduct.createIndex();
					index.setReferencedAttribute("species");
					index.setArrayDimension(i);
					index.setMath(SBMLutilities.myParseFormula(dim.getId()));
				}
			}			
		}
		if (!productId.equals("none") && r.getProductForSpecies(productId)==null) {
			SpeciesReference product = r.createProduct();
			product.setSpecies(productId);
			double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			product.setStoichiometry(np);
			product.setConstant(true);
			if (r.getProductForSpecies(promoterId + "_mRNA")!=null) {
				r.removeProduct(promoterId+"_mRNA");
			}
			if (sbml.getModel().getSpecies(promoterId + "_mRNA")!=null) {
				sbml.getModel().removeSpecies(promoterId+"_mRNA");
			}
		}
		r.getKineticLaw().setMath(SBMLutilities.myParseFormula(createProductionKineticLaw(r)));
		return r;
	}
	
	public static boolean isGridReaction(Reaction reaction) {
		return (AnnotationUtility.parseGridAnnotation(reaction)!=null);
	}
	
	public static boolean isComplexReaction(Reaction react) {
		if (AnnotationUtility.checkObsoleteAnnotation(react, GlobalConstants.COMPLEXATION)) {
			react.setSBOTerm(GlobalConstants.SBO_ASSOCIATION);
			AnnotationUtility.removeObsoleteAnnotation(react);
			return checkComplexationStructure(react);
		} else if (react.isSetSBOTerm()) {
			if (react.getSBOTerm() == GlobalConstants.SBO_COMPLEX) {
				react.setSBOTerm(GlobalConstants.SBO_ASSOCIATION);
				return checkComplexationStructure(react);
			} else if (react.getSBOTerm() == GlobalConstants.SBO_ASSOCIATION)
				return checkComplexationStructure(react);
		} 
		return false;
	}

	private static boolean checkComplexationStructure(Reaction complexation) {	
		return (complexation.getNumReactants() >= 1 && complexation.getNumModifiers() == 0 
				&& complexation.getNumProducts() == 1);
	}

	public static boolean isConstitutiveReaction(Reaction reaction) {
		if (reaction.isSetSBOTerm()) {
			if (reaction.getSBOTerm()==GlobalConstants.SBO_CONSTITUTIVE) return true;
		} else if (AnnotationUtility.checkObsoleteAnnotation(reaction,"Constitutive")) {
			reaction.setSBOTerm(GlobalConstants.SBO_CONSTITUTIVE);
			AnnotationUtility.removeObsoleteAnnotation(reaction);
			return true;
		}
		return false;
	}
	
	public static boolean isDegradationReaction(Reaction react) {
		if (AnnotationUtility.checkObsoleteAnnotation(react, GlobalConstants.DEGRADATION)) {
			react.setSBOTerm(GlobalConstants.SBO_DEGRADATION);
			AnnotationUtility.removeObsoleteAnnotation(react);
			return checkDegradationStructure(react);
		} else if (react.isSetSBOTerm() && react.getSBOTerm() == GlobalConstants.SBO_DEGRADATION) 
			return checkDegradationStructure(react);
		return false;
	}
	
	private static boolean checkDegradationStructure(Reaction degradation) {
		return (degradation.getNumReactants() == 1 && degradation.getNumModifiers() == 0
				&& degradation.getNumProducts() == 0);
	}
	
	public static boolean isDiffusionReaction(Reaction reaction) {
		if (reaction.isSetSBOTerm()) {
			if (reaction.getSBOTerm()==GlobalConstants.SBO_DIFFUSION) return true;
		} else if (AnnotationUtility.checkObsoleteAnnotation(reaction,"Diffusion")) {
			reaction.setSBOTerm(GlobalConstants.SBO_DIFFUSION);
			AnnotationUtility.removeObsoleteAnnotation(reaction);
			return true;
		}
		return false;
	}
	
	public static boolean isProductionReaction(Reaction react) {
		if (AnnotationUtility.checkObsoleteAnnotation(react, GlobalConstants.PRODUCTION)) {
			react.setSBOTerm(GlobalConstants.SBO_GENETIC_PRODUCTION);
			AnnotationUtility.removeObsoleteAnnotation(react);
			return checkProductionStructure(react);
		} else if (react.isSetSBOTerm()) {
			if (react.getSBOTerm() == GlobalConstants.SBO_PRODUCTION) {
				react.setSBOTerm(GlobalConstants.SBO_GENETIC_PRODUCTION);
				return checkProductionStructure(react);
			} else if (react.getSBOTerm() == GlobalConstants.SBO_GENETIC_PRODUCTION) 
				return checkProductionStructure(react);
		} 
		return false;
	}

	private static boolean checkProductionStructure(Reaction production) {
		int numPromoters = 0;
		for (int i = 0; i < production.getNumModifiers(); i++)
			if (isPromoter(production.getModifier(i)))
				numPromoters++;
		return (numPromoters == 1 && production.getNumReactants() == 0 
				&& production.getNumProducts() > 0);
	}
	
	public static boolean isMRNASpecies(SBMLDocument doc, Species species) {
		for (int i = 0; i < doc.getModel().getReactionCount(); i++) {
			Reaction r = doc.getModel().getReaction(i);
			if (isProductionReaction(r)) continue;
			for (int j = 0; j < r.getReactantCount(); j++) {
				if (r.getReactant(j).getSpecies().equals(species.getId())) return false;
			}
			for (int j = 0; j < r.getModifierCount(); j++) {
				if (r.getModifier(j).getSpecies().equals(species.getId())) return false;
			}
			for (int j = 0; j < r.getProductCount(); j++) {
				if (r.getProduct(j).getSpecies().equals(species.getId())) return false;
			}
		}
		if (species.isSetSBOTerm()) {
			if (species.getSBOTerm()==GlobalConstants.SBO_MRNA || species.getSBOTerm()==GlobalConstants.SBO_MRNA_OLD) {
				species.setSBOTerm(GlobalConstants.SBO_MRNA);
				return species.getId().endsWith("_mRNA");
			}
		}
		if (AnnotationUtility.checkObsoleteAnnotation(species,GlobalConstants.TYPE+"="+GlobalConstants.MRNA)) {
			species.setSBOTerm(GlobalConstants.SBO_MRNA);
			AnnotationUtility.removeObsoleteAnnotation(species);
			return species.getId().endsWith("_mRNA");
		}
		return false;
	}
	
	public static boolean isPromoterSpecies(Species species) {
		if (species.isSetSBOTerm()) {
			if (species.getSBOTerm()==GlobalConstants.SBO_OLD_PROMOTER_SPECIES) {
				species.setSBOTerm(GlobalConstants.SBO_PROMOTER_BINDING_REGION);
				return true;
			}
			if (species.getSBOTerm()==GlobalConstants.SBO_PROMOTER_SPECIES) {
				species.setSBOTerm(GlobalConstants.SBO_PROMOTER_BINDING_REGION);
				return true;
			} else if (species.getSBOTerm()==GlobalConstants.SBO_PROMOTER_BINDING_REGION) {
				return true;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_DNA_SEGMENT)) {
				//species.setSBOTerm(GlobalConstants.SBO_PROMOTER_BINDING_REGION);
				return true;
				// TODO: assuming DNA_SEGMENT is promoter
			}
		}
		if (AnnotationUtility.checkObsoleteAnnotation(species,GlobalConstants.TYPE+"="+GlobalConstants.PROMOTER)) {
			species.setSBOTerm(GlobalConstants.SBO_PROMOTER_BINDING_REGION);
			AnnotationUtility.removeObsoleteAnnotation(species);
			return true;
		}
		return false;
	}
	
	public static boolean isPromoter(ModifierSpeciesReference mod) {
		if (AnnotationUtility.checkObsoleteAnnotation(mod,"promoter")) {
			mod.setSBOTerm(GlobalConstants.SBO_PROMOTER_MODIFIER);
			AnnotationUtility.removeObsoleteAnnotation(mod);
			return true;
		} else if (mod.isSetSBOTerm()) {
			if (mod.getSBOTerm() == GlobalConstants.SBO_PROMOTER) {
				mod.setSBOTerm(GlobalConstants.SBO_PROMOTER_MODIFIER);
				return true;
			} else if (mod.getSBOTerm() == GlobalConstants.SBO_PROMOTER_MODIFIER) {
				return true;
			}
		} else if (isPromoterSpecies(mod.getSpeciesInstance())) {
			mod.setSBOTerm(GlobalConstants.SBO_PROMOTER_MODIFIER);
			return true;
		}
		return false;
	}
	
	public static boolean isActivator(ModifierSpeciesReference modifier) {
		if (modifier.isSetSBOTerm()) {
			if (modifier.getSBOTerm()==GlobalConstants.SBO_ACTIVATION) return true;
		}
		if (AnnotationUtility.checkObsoleteAnnotation(modifier,GlobalConstants.ACTIVATION)) {
			modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
			AnnotationUtility.removeObsoleteAnnotation(modifier);
			return true;
		}
		return false;
	}
	
	public static boolean isRepressor(ModifierSpeciesReference modifier) {
		if (modifier.isSetSBOTerm()) {
			if (modifier.getSBOTerm()==GlobalConstants.SBO_REPRESSION) return true;
		}
		if (AnnotationUtility.checkObsoleteAnnotation(modifier,GlobalConstants.REPRESSION)) {
			modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
			AnnotationUtility.removeObsoleteAnnotation(modifier);
			return true;
		}
		return false;
	}
	
	public static boolean isNeutral(ModifierSpeciesReference modifier) {
		if (modifier.isSetSBOTerm()) {
			if (modifier.getSBOTerm()==GlobalConstants.SBO_NEUTRAL) return true;
		}
		if (AnnotationUtility.checkObsoleteAnnotation(modifier,GlobalConstants.NOINFLUENCE)) {
			modifier.setSBOTerm(GlobalConstants.SBO_NEUTRAL);
			AnnotationUtility.removeObsoleteAnnotation(modifier);
			return true;
		}
		return false;
	}
	
	public static boolean isRegulator(ModifierSpeciesReference modifier) {
		if (modifier!=null) {
			if (modifier.isSetSBOTerm()) {
				if (modifier.getSBOTerm()==GlobalConstants.SBO_REGULATION) {
					modifier.setSBOTerm(GlobalConstants.SBO_DUAL_ACTIVITY);
					return true;
				} else if (modifier.getSBOTerm()==GlobalConstants.SBO_DUAL_ACTIVITY) {
					return true;
				}
			}
			if (AnnotationUtility.checkObsoleteAnnotation(modifier,GlobalConstants.REGULATION)) {
				modifier.setSBOTerm(GlobalConstants.SBO_DUAL_ACTIVITY);
				AnnotationUtility.removeObsoleteAnnotation(modifier);
				return true;
			} else if (AnnotationUtility.checkObsoleteAnnotation(modifier,"promoter")) {
				AnnotationUtility.removeObsoleteAnnotation(modifier);
				return false;
			}
		}
		return false;
	}
	
	public static void addProductionParameters(Reaction r, String factorId, String ncStr, String KaStr, String KrStr, String type) {
		KineticLaw k = r.getKineticLaw();
		if (ncStr!=null) {
			LocalParameter p = k.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+factorId+"_"+type);
			if (p==null) {
				p = k.createLocalParameter();
				p.setId(GlobalConstants.COOPERATIVITY_STRING+"_"+factorId+"_"+type);
			}
			p.setValue(Double.parseDouble(ncStr));
		} 							
		if (KaStr != null) {
			LocalParameter p = k.getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+factorId+"_"));
			if (p==null) {
				p = k.createLocalParameter();
				p.setId(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+factorId+"_"));
			}
			double [] Ka = Utility.getEquilibrium(KaStr);
			p.setValue(Ka[0]);
			p = k.getLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+factorId+"_"));
			if (p==null) {
				p = k.createLocalParameter();
				p.setId(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+factorId+"_"));
			}
			p.setValue(Ka[1]);
		} 
		if (KrStr != null) {
			LocalParameter p = k.getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+factorId+"_"));
			if (p==null) {
				p = k.createLocalParameter();
				p.setId(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+factorId+"_"));
			}
			double [] Kr = Utility.getEquilibrium(KrStr);
			p.setValue(Kr[0]);
			p = k.getLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+factorId+"_"));
			if (p==null) {
				p = k.createLocalParameter();
				p.setId(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+factorId+"_"));
			}
			p.setValue(Kr[1]);
		} 
		r.getKineticLaw().setMath(SBMLutilities.myParseFormula(createProductionKineticLaw(r)));
	}
	
	public Reaction addProductToProductionReaction(String promoterId,String productId,String npStr) {
		Reaction r = getProductionReaction(promoterId);
		if (r.getProductForSpecies(productId)==null) {
			SpeciesReference product = r.createProduct();
			product.setSpecies(productId);
			if (npStr != null) {
				double np = Double.parseDouble(npStr);
				product.setStoichiometry(np);
			} else {
				double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
				product.setStoichiometry(np);
			}
			product.setConstant(true);
			if (SBMLutilities.dimensionsMatch(r,sbml.getModel().getSpecies(productId))) {
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
				ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(product);
				sBasePluginProduct.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
					Index index = sBasePluginProduct.createIndex();
					index.setReferencedAttribute("species");
					index.setArrayDimension(i);
					index.setMath(SBMLutilities.myParseFormula(dim.getId()));
				}
			}
			if (r.getProductForSpecies(promoterId+"_mRNA")!=null) {
				r.removeProduct(promoterId+"_mRNA");
			}
			if (sbml.getModel().getSpecies(promoterId + "_mRNA")!=null) {
				sbml.getModel().removeSpecies(promoterId+"_mRNA");
			}
		}
		return r;
	}
		
	public Reaction addActivatorToProductionReaction(String promoterId,String activatorId,String productId,String npStr,
			String ncStr,String KaStr) {
		Reaction r = getProductionReaction(promoterId);
		return addActivatorToProductionReaction(promoterId, activatorId, productId, r, npStr, ncStr, KaStr);
	}
	
	public Reaction addActivatorToProductionReaction(String promoterId, String activatorId, String productId, 
			Reaction r, String npStr, String ncStr, String KaStr) {
		ModifierSpeciesReference modifier = r.getModifierForSpecies(activatorId);
		if (!activatorId.equals("none") && modifier==null) {
			modifier = r.createModifier();
			modifier.setSpecies(activatorId);
			modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
		} else if (modifier != null && BioModel.isRepressor(modifier)) {
			modifier.setSBOTerm(GlobalConstants.SBO_DUAL_ACTIVITY);
		}
		if (modifier!=null) {
			if (SBMLutilities.dimensionsMatch(r,sbml.getModel().getSpecies(activatorId))) {
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
				ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(modifier);
				sBasePluginProduct.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
					Index index = sBasePluginProduct.createIndex();
					index.setReferencedAttribute("species");
					index.setArrayDimension(i);
					index.setMath(SBMLutilities.myParseFormula(dim.getId()));
				}
			}			
		}
		if (!productId.equals("none") && r.getProductForSpecies(productId)==null) {
			SpeciesReference product = r.createProduct();
			product.setSpecies(productId);
			if (npStr != null) {
				double np = Double.parseDouble(npStr);
				product.setStoichiometry(np);
			} else {
				double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
				product.setStoichiometry(np);
			}
			product.setConstant(true);
			if (SBMLutilities.dimensionsMatch(r,sbml.getModel().getSpecies(productId))) {
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
				ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(product);
				sBasePluginProduct.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
					Index index = sBasePluginProduct.createIndex();
					index.setReferencedAttribute("species");
					index.setArrayDimension(i);
					index.setMath(SBMLutilities.myParseFormula(dim.getId()));
				}
			}
			if (r.getProductForSpecies(promoterId + "_mRNA")!=null) {
				r.removeProduct(promoterId + "_mRNA");
			}
			if (sbml.getModel().getSpecies(promoterId + "_mRNA")!=null) {
				sbml.getModel().removeSpecies(promoterId + "_mRNA");
			}
		}
		addProductionParameters(r,activatorId,ncStr,KaStr,null,"a");
		return r;
	}
	
	public Reaction addRepressorToProductionReaction(String promoterId,String repressorId,String productId,String npStr,
			String ncStr,String KrStr) {
		Reaction r = getProductionReaction(promoterId);
		return addRepressorToProductionReaction(promoterId, repressorId, productId, r, npStr, ncStr, KrStr);
	}
	
	public Reaction addRepressorToProductionReaction(String promoterId, String repressorId, String productId, 
			Reaction r, String npStr, String ncStr, String KrStr) {
		ModifierSpeciesReference modifier = r.getModifierForSpecies(repressorId);
		if (!repressorId.equals("none") && modifier==null) {
			modifier = r.createModifier();
			modifier.setSpecies(repressorId);
			modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
		} else if (modifier != null && BioModel.isActivator(modifier)) {
			modifier.setSBOTerm(GlobalConstants.SBO_DUAL_ACTIVITY);
		}
		if (modifier!=null) {
			if (SBMLutilities.dimensionsMatch(r,sbml.getModel().getSpecies(repressorId))) {
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
				ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(modifier);
				sBasePluginProduct.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
					Index index = sBasePluginProduct.createIndex();
					index.setReferencedAttribute("species");
					index.setArrayDimension(i);
					index.setMath(SBMLutilities.myParseFormula(dim.getId()));
				}
			}			
		}
		if (!productId.equals("none") && r.getProductForSpecies(productId)==null) {
			SpeciesReference product = r.createProduct();
			product.setSpecies(productId);
			if (npStr != null) {
				double np = Double.parseDouble(npStr);
				product.setStoichiometry(np);
			} else {
				double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
				product.setStoichiometry(np);
			}
			product.setConstant(true);
			if (r.getProductForSpecies(promoterId + "_mRNA")!=null) {
				r.removeProduct(promoterId + "_mRNA");
			}
			if (sbml.getModel().getSpecies(promoterId + "_mRNA")!=null) {
				sbml.getModel().removeSpecies(promoterId + "_mRNA");
			}
		}
		addProductionParameters(r,repressorId,ncStr,null,KrStr,"r");
		return r;
	}
	
	public static void updateComplexCooperativity(String reactantId, Reaction react, String CoopStr, Model model) {
		SpeciesReference reactant = react.getReactantForSpecies(reactantId);
		KineticLaw k = react.getKineticLaw();
		LocalParameter p = k.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+reactantId);
		if (CoopStr != null) {
			if (p==null) {
				p = k.createLocalParameter();
				p.setId(GlobalConstants.COOPERATIVITY_STRING+"_"+reactantId);
			} 
			double nc = Double.parseDouble(CoopStr);
			p.setValue(nc);
			reactant.setStoichiometry(nc);
		} else {
			if (p != null) {
				k.getListOfLocalParameters().remove(GlobalConstants.COOPERATIVITY_STRING+"_"+reactantId);
			}
			Parameter gp = model.getParameter(GlobalConstants.COOPERATIVITY_STRING);
			reactant.setStoichiometry(gp.getValue());
		}
		react.getKineticLaw().setMath(SBMLutilities.myParseFormula(createComplexKineticLaw(react)));
	}
	
	public Reaction addReactantToComplexReaction(String reactantId,String productId,String KcStr,String CoopStr, Reaction r) {
		SpeciesReference reactant = r.getReactantForSpecies(reactantId);
		if (reactant==null) {
			reactant = r.createReactant();
			reactant.setSpecies(reactantId);
			reactant.setConstant(true);
		}
		if (SBMLutilities.dimensionsMatch(r,sbml.getModel().getSpecies(reactantId))) {
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
			ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(reactant);
			sBasePluginProduct.unsetListOfIndices();
			for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
				Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
				Index index = sBasePluginProduct.createIndex();
				index.setReferencedAttribute("species");
				index.setArrayDimension(i);
				index.setMath(SBMLutilities.myParseFormula(dim.getId()));
			}
		}			
		updateComplexCooperativity(reactantId, r, CoopStr, sbml.getModel());
		return r;
	}

	public Reaction addReactantToComplexReaction(String reactantId,String productId,String KcStr,String CoopStr) {
		boolean onPort = (getPortByIdRef(productId)!=null);
		Reaction r = createComplexReaction(productId,KcStr,onPort);
		return addReactantToComplexReaction(reactantId, productId, KcStr, CoopStr, r);
	}
	
	public static void updateDiffusionParameters(Reaction reaction,String kmdiffStr) {
		KineticLaw k = reaction.getKineticLaw();
		if (kmdiffStr != null && kmdiffStr.startsWith("(")) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_MEMDIFF_STRING);
			p.setValue(1.0);
			AnnotationUtility.setSweepAnnotation(p, kmdiffStr);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_MEMDIFF_STRING);
			p.setValue(1.0);
		} else {
			double [] kmdiff = Utility.getEquilibrium(kmdiffStr);
			if (kmdiff[0] >= 0) {
				LocalParameter p = k.createLocalParameter();
				p.setId(GlobalConstants.FORWARD_MEMDIFF_STRING);
				p.setValue(kmdiff[0]);
				p = k.createLocalParameter();
				p.setId(GlobalConstants.REVERSE_MEMDIFF_STRING);
				p.setValue(kmdiff[1]);
			}
		}
	}
	
	public Reaction createDiffusionReaction(String s,String kmdiffStr,boolean onPort,String[] dimensions) {
		createDiffusionDefaultParameters();
		Reaction reaction = sbml.getModel().getReaction("MembraneDiffusion_"+s);		
		if (reaction==null) {
			reaction = sbml.getModel().createReaction();
			reaction.setId("MembraneDiffusion_"+s);
			reaction.setSBOTerm(GlobalConstants.SBO_DIFFUSION);
			reaction.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
			reaction.setReversible(true);
			reaction.setFast(false);
			SpeciesReference reactant = reaction.createReactant();
			reactant.setSpecies(s);
			reactant.setStoichiometry(1);
			reactant.setConstant(true);
		}
		String indexStr = "";
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(reaction);
		if (dimensions!=null && dimensions.length>0) {
			String [] dimIds = SBMLutilities.getDimensionIds("",dimensions.length-1);
			sBasePlugin.unsetListOfDimensions();
			ArraysSBasePlugin sBasePluginReactant = SBMLutilities.getArraysSBasePlugin(reaction.getReactant(0));
			sBasePluginReactant.unsetListOfIndices();
			for (int i = 1; i < dimensions.length; i++) {
				Dimension dim = sBasePlugin.createDimension(dimIds[i-1]);
				dim.setSize(dimensions[i]);
				dim.setArrayDimension(i-1);
				Index index = sBasePluginReactant.createIndex();
				index.setReferencedAttribute("species");
				index.setArrayDimension(i-1);
				index.setMath(SBMLutilities.myParseFormula(dimIds[i-1]));
				indexStr = "[" + SBMLutilities.myParseFormula(dimIds[i-1]) + "]" + indexStr;
			}
		}
		reaction.createKineticLaw();
		updateDiffusionParameters(reaction,kmdiffStr);
		reaction.getKineticLaw().setMath(SBMLutilities.myParseFormula(GlobalConstants.FORWARD_MEMDIFF_STRING+"*"+s+indexStr+"-"+
				GlobalConstants.REVERSE_MEMDIFF_STRING));		
		Port port = getPortByIdRef(reaction.getId());
		if (port!=null) {
			if (onPort) {
				port.setId(reaction.getId());
				port.setIdRef(reaction.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			} else {
				sbmlCompModel.removePort(port);
			}
		} else {
			if (onPort) {
				port = sbmlCompModel.createPort();
				port.setId(reaction.getId());
				port.setIdRef(reaction.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			}
		}
		return reaction;
	}

	public Reaction createConstitutiveReaction(String s,String ko,String np,boolean onPort,String[] dimensions) {
		Reaction r = sbml.getModel().getReaction("Constitutive_"+s);
		KineticLaw k = null;
		if (r==null) {
			createConstitutiveDefaultParameters();
			r = sbml.getModel().createReaction();
			r.setId("Constitutive_"+s);
			r.setSBOTerm(GlobalConstants.SBO_CONSTITUTIVE);
			r.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
			r.setReversible(false);
			r.setFast(false);
			SpeciesReference product = r.createProduct();
			product.setSpecies(s);
			//double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			//product.setStoichiometry(np);
			product.setConstant(true);
			k = r.createKineticLaw();
			k.setMath(SBMLutilities.myParseFormula(GlobalConstants.OCR_STRING));
		} else {
			k = r.getKineticLaw();
		}
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
		if (dimensions!=null && dimensions.length>0) {
			String [] dimIds = SBMLutilities.getDimensionIds("",dimensions.length-1);
			sBasePlugin.unsetListOfDimensions();
			ArraysSBasePlugin sBasePluginReactant = SBMLutilities.getArraysSBasePlugin(r.getProduct(0));
			sBasePluginReactant.unsetListOfIndices();
			for (int i = 1; i < dimensions.length; i++) {
				Dimension dim = sBasePlugin.createDimension(dimIds[i-1]);
				dim.setSize(dimensions[i]);
				dim.setArrayDimension(i-1);
				Index index = sBasePluginReactant.createIndex();
				index.setReferencedAttribute("species");
				index.setArrayDimension(i-1);
				index.setMath(SBMLutilities.myParseFormula(dimIds[i-1]));
			}
		}
		if (np != null) {
			LocalParameter p = k.getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
			if (p==null) {
				p = k.createLocalParameter();
				p.setId(GlobalConstants.STOICHIOMETRY_STRING);
			}
			double npVal = 1.0;
			if (np.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(p, np);
			} else {
				npVal = Double.parseDouble(np);
			}
			p.setValue(npVal);
			for (int i = 0; i < r.getProductCount(); i++) {
				r.getProduct(i).setStoichiometry(npVal);
			}
		} else {
			for (int i = 0; i < r.getProductCount(); i++) {
				r.getProduct(i).setStoichiometry(sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			}
		}
		if (ko != null) {
			LocalParameter p = k.getLocalParameter(GlobalConstants.OCR_STRING);
			if (p==null) {	
				p = k.createLocalParameter();
				p.setId(GlobalConstants.OCR_STRING);
			}
			if (ko.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(p, ko);
				p.setValue(1.0);
			} else {
				p.setValue(Double.parseDouble(ko));
			}
		} 		
		Port port = getPortByIdRef(r.getId());
		if (port!=null) {
			if (onPort) {
				port.setId(r.getId());
				port.setIdRef(r.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			} else {
				sbmlCompModel.removePort(port);
			}
		} else {
			if (onPort) {
				port = sbmlCompModel.createPort();
				port.setId(r.getId());
				port.setIdRef(r.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			}
		}
		return r;
	}
	
	public Reaction createDegradationReaction(String s,double kd,String sweep,boolean onPort, String[] dimensions) {
		createDegradationDefaultParameters();
		Reaction reaction = getDegradationReaction(s);
		if (reaction==null) {
			reaction = sbml.getModel().createReaction();
			reaction.setId(GlobalConstants.DEGRADATION + "_" + s);
			reaction.setSBOTerm(GlobalConstants.SBO_DEGRADATION);
			reaction.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
			reaction.setReversible(false);
			reaction.setFast(false);
			SpeciesReference reactant = reaction.createReactant();
			reactant.setSpecies(s);
			reactant.setStoichiometry(1);
			reactant.setConstant(true);
		} 
		String indexStr = "";
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(reaction);
		if (dimensions!=null && dimensions.length>0) {
			String [] dimIds = SBMLutilities.getDimensionIds("",dimensions.length-1);
			sBasePlugin.unsetListOfDimensions();
			ArraysSBasePlugin sBasePluginReactant = SBMLutilities.getArraysSBasePlugin(reaction.getReactant(0));
			sBasePluginReactant.unsetListOfIndices();
			for (int i = 1; i < dimensions.length; i++) {
				Dimension dim = sBasePlugin.createDimension(dimIds[i-1]);
				dim.setSize(dimensions[i]);
				dim.setArrayDimension(i-1);
				Index index = sBasePluginReactant.createIndex();
				index.setReferencedAttribute("species");
				index.setArrayDimension(i-1);
				index.setMath(SBMLutilities.myParseFormula(dimIds[i-1]));
				indexStr = "[" + SBMLutilities.myParseFormula(dimIds[i-1]) + "]" + indexStr;
			}
		}
		KineticLaw k = reaction.createKineticLaw();
		if (kd > 0 || sweep != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId("kd");
			p.setValue(kd);
			if (sweep != null) {
				AnnotationUtility.setSweepAnnotation(p, sweep);
			} 
		}
		k.setMath(SBMLutilities.myParseFormula("kd*"+s+indexStr));
		Port port = getPortByIdRef(reaction.getId());
		if (port!=null) {
			if (onPort) {
				port.setId(reaction.getId());
				port.setIdRef(reaction.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			} else {
				sbmlCompModel.removePort(port);
			}
		} else {
			if (onPort) {
				port = sbmlCompModel.createPort();
				port.setId(reaction.getId());
				port.setIdRef(reaction.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());		
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			}
		}
		return reaction;
	}
	
	public Reaction createProductionReaction(String promoterId, String ka, String np, String ko,
			String kb, String bigKo, String bigKao, boolean onPort, String[] dimensions) {
		return createProductionReaction(promoterId, null, ka, np, ko, kb, bigKo, bigKao, onPort, dimensions);
	}

	public Reaction createProductionReaction(String promoterId, String reactionId, String ka, String np, String ko,
			String kb, String KoStr, String KaoStr, boolean onPort, String[] dimensions) {
		createProductionDefaultParameters();
		Reaction r = getProductionReaction(promoterId);
		KineticLaw k = null;
		if (r == null) {
			r = sbml.getModel().createReaction();
			if (reactionId == null)
				r.setId(GlobalConstants.PRODUCTION + "_" + promoterId);
			else
				r.setId(reactionId);
			r.setSBOTerm(GlobalConstants.SBO_GENETIC_PRODUCTION);
			r.setCompartment(sbml.getModel().getSpecies(promoterId).getCompartment());
			r.setReversible(false);
			r.setFast(false);
			ModifierSpeciesReference modifier = r.createModifier();
			modifier.setSpecies(promoterId);
			modifier.setSBOTerm(GlobalConstants.SBO_PROMOTER_MODIFIER);
			Species mRNA = sbml.getModel().getSpecies(promoterId+"_mRNA");
			if (mRNA==null) {
				mRNA = sbml.getModel().createSpecies();
				mRNA.setId(promoterId + "_mRNA");
				mRNA.setCompartment(r.getCompartment());
				mRNA.setInitialAmount(0.0);
				mRNA.setBoundaryCondition(false);
				mRNA.setConstant(false);
				mRNA.setHasOnlySubstanceUnits(true);
				mRNA.setSBOTerm(GlobalConstants.SBO_MRNA);
			}
			SpeciesReference product = r.createProduct();
			product.setSpecies(mRNA.getId());
			product.setStoichiometry(1.0);
			product.setConstant(true);
			k = r.createKineticLaw();
		} else {
			k = r.getKineticLaw();
			if (k.getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.STOICHIOMETRY_STRING);
			}
			if (k.getLocalParameter(GlobalConstants.OCR_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.OCR_STRING);
			}
			if (k.getLocalParameter(GlobalConstants.KBASAL_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.KBASAL_STRING);
			}
			if (k.getLocalParameter(GlobalConstants.ACTIVATED_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.ACTIVATED_STRING);
			}
			if (k.getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
			}
			if (k.getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
			}
			if (k.getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
			}
			if (k.getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING)!=null) {
				k.getListOfLocalParameters().remove(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
			}
		}
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, r, metaIDIndex);
		if (dimensions!=null && dimensions.length>0) {
			String [] dimIds = SBMLutilities.getDimensionIds("",dimensions.length-1);
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
			sBasePlugin.unsetListOfDimensions();
			for (int i = 1; i < dimensions.length; i++) {
				Dimension dim = sBasePlugin.createDimension(dimIds[i-1]);
				dim.setSize(dimensions[i]);
				dim.setArrayDimension(i-1);
			}
			for (int j=0;j<r.getModifierCount();j++) {
				if (BioModel.isPromoter(r.getModifier(j))) {
					ArraysSBasePlugin sBasePluginModifier = SBMLutilities.getArraysSBasePlugin(r.getModifier(j));
					sBasePluginModifier.unsetListOfIndices();
					for (int i = 1; i < dimensions.length; i++) {
						Index index = sBasePluginModifier.createIndex();
						index.setReferencedAttribute("species");
						index.setArrayDimension(i-1);
						index.setMath(SBMLutilities.myParseFormula(dimIds[i-1]));
						//indexStr = "[" + SBMLutilities.myParseFormula(dimIds[i-1]) + "]" + indexStr;
					}
				}
			}
		}
		if (np != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.STOICHIOMETRY_STRING);
			double npVal = 1.0;
			if (np.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(p, np);
			} else {
				npVal = Double.parseDouble(np);
			}
			p.setValue(npVal);
			for (int i = 0; i < r.getProductCount(); i++) {
				r.getProduct(i).setStoichiometry(npVal);
			}
		} else {
			for (int i = 0; i < r.getProductCount(); i++) {
				r.getProduct(i).setStoichiometry(sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			}
		}
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
		for (int i = 0; i < r.getProductCount(); i++) {
			String productId = r.getProduct(i).getSpecies();
			if (SBMLutilities.dimensionsMatch(r, sbml.getModel().getSpecies(productId))) {
				ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(r.getProduct(i));
				sBasePluginProduct.unsetListOfIndices();
				for (int j = 0; j < sBasePlugin.getListOfDimensions().size(); j++) {
					Dimension dim = sBasePlugin.getDimensionByArrayDimension(j);
					Index index = sBasePluginProduct.createIndex();
					index.setReferencedAttribute("species");
					index.setArrayDimension(j);
					index.setMath(SBMLutilities.myParseFormula(dim.getId()));
				}
			}
		}
		if (ko != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.OCR_STRING);
			if (ko.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(p, ko);
				p.setValue(1.0);
			} else {
				p.setValue(Double.parseDouble(ko));
			}
		} 							
		if (kb != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.KBASAL_STRING);
			if (kb.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(p, kb);
				p.setValue(1.0);
			} else {
				p.setValue(Double.parseDouble(kb));
			}
		} 
		if (ka != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.ACTIVATED_STRING);
			if (ka.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(p, ka);
				p.setValue(1.0);
			} else {
				p.setValue(Double.parseDouble(ka));
			}
		} 		
		if (KoStr != null) {
			double [] Ko;
			LocalParameter p = k.createLocalParameter();
			if (KoStr.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(p, KoStr);
				p.setValue(1.0);
				Ko = Utility.getEquilibrium("1.0/1.0");
			} else {
				Ko = Utility.getEquilibrium(KoStr);
			}
			p.setId(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
			p.setValue(Ko[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
			p.setValue(Ko[1]);
		} 							
		if (KaoStr != null) {
			double [] Kao;
			LocalParameter p = k.createLocalParameter();
			if (KaoStr.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(p, KaoStr);
				p.setValue(1.0);
				Kao = Utility.getEquilibrium("1.0/1.0");
			} else {
				Kao = Utility.getEquilibrium(KoStr);
			}
			p.setId(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
			p.setValue(Kao[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
			p.setValue(Kao[1]);
		} 
		r.getKineticLaw().setMath(SBMLutilities.myParseFormula(createProductionKineticLaw(r)));
		Port port = getPortByIdRef(r.getId());
		if (port!=null) {
			if (onPort) {
				port.setId(r.getId());
				port.setIdRef(r.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			} else {
				sbmlCompModel.removePort(port);
			}
		} else {
			if (onPort) {
				port = sbmlCompModel.createPort();
				port.setId(r.getId());
				port.setIdRef(r.getId());
				ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
				sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
				sBasePluginPort.unsetListOfIndices();
				for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
					org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
					Index portIndex = sBasePluginPort.createIndex();
					portIndex.setReferencedAttribute("comp:idRef");
					portIndex.setArrayDimension(i);
					portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
				}
			}
		}
		return r;
	}

	public static String createComplexKineticLaw(Reaction reaction) {
		String kineticLaw;
		kineticLaw = GlobalConstants.FORWARD_KCOMPLEX_STRING;
		for (int i=0;i<reaction.getReactantCount();i++) {
			String reactant = reaction.getReactant(i).getSpecies();
			String indexStr = "";
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(reaction.getReactant(i));
			for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
				Index index = sBasePlugin.getIndex(j,"species");
				if (index!=null)
					indexStr += "[" + SBMLutilities.myFormulaToString(index.getMath()) + "]";
			}
			if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + 
					reaction.getReactant(i).getSpecies())==null) {
				kineticLaw += "*pow(" + reactant+indexStr + "," + GlobalConstants.COOPERATIVITY_STRING + ")";
			} else {
				kineticLaw += "*pow(" + reactant+indexStr + "," + GlobalConstants.COOPERATIVITY_STRING + 
						"_" + reactant + ")";
			}
		}
		String indexStr = "";
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(reaction.getProduct(0));
		for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
			Index index = sBasePlugin.getIndex(j,"species");
			if (index!=null)
				indexStr += "[" + SBMLutilities.myFormulaToString(index.getMath()) + "]";
		}
		kineticLaw += "-" + GlobalConstants.REVERSE_KCOMPLEX_STRING + "*" + reaction.getProduct(0).getSpecies()+indexStr;
		return kineticLaw;
	}

	public static String createProductionKineticLaw(Reaction reaction) {
		String kineticLaw;
		boolean activated = false;
		String promoter = "";
		for (int i=0;i<reaction.getModifierCount();i++) {
			if (BioModel.isActivator(reaction.getModifier(i)) || BioModel.isRegulator(reaction.getModifier(i))) {
				activated = true;
			} else if (BioModel.isPromoter(reaction.getModifier(i))) {
				promoter = reaction.getModifier(i).getSpecies();
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(reaction.getModifier(i));
				for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
					Index index = sBasePlugin.getIndex(j,"species");
					if (index!=null)
						promoter += "[" + SBMLutilities.myFormulaToString(index.getMath()) + "]";
				}
			}
		}
		if (activated) {
			kineticLaw = promoter + "*(" + GlobalConstants.KBASAL_STRING + "*" +
					"(" + GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" 
					+ GlobalConstants.RNAP_STRING;
			String actBottom = "";
			for (int i=0;i<reaction.getModifierCount();i++) {
				if (BioModel.isActivator(reaction.getModifier(i)) || BioModel.isRegulator(reaction.getModifier(i))) {
					String activator = reaction.getModifier(i).getSpecies();
					String indexStr = "";
					ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(reaction.getModifier(i));
					for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
						Index index = sBasePlugin.getIndex(j,"species");
						if (index!=null)
							indexStr += "[" + SBMLutilities.myFormulaToString(index.getMath()) + "]";
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+activator+"_"))==null) {
						kineticLaw += "+" + GlobalConstants.ACTIVATED_STRING + "*" + 
								"(" + GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING + "/" + 
								GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING +
								"*pow((" + GlobalConstants.FORWARD_KACT_STRING + "/" + GlobalConstants.REVERSE_KACT_STRING + ")*" 
								+ activator+indexStr;
						actBottom += "+(" + GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING + "/" + 
								GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING +
								"*pow((" + GlobalConstants.FORWARD_KACT_STRING + "/" + GlobalConstants.REVERSE_KACT_STRING + ")*" 
								+ activator+indexStr;
					} else {
						kineticLaw += "+" + GlobalConstants.ACTIVATED_STRING + "*" + 
								"(" + GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING + "/" + 
								GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING +
								"*pow((" + GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + activator + "_") + "/" + 
								GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + activator + "_") + ")*" + activator+indexStr;
						actBottom += "+(" + GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING + "/" + 
								GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING +
								"*pow((" + GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + activator + "_") + "/" + 
								GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + activator +"_") + ")*" + activator+indexStr;
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+activator+"_a")==null) {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + ")";
						actBottom += "," + GlobalConstants.COOPERATIVITY_STRING + ")";
					} else {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + "_" + activator +"_a)";
						actBottom += "," + GlobalConstants.COOPERATIVITY_STRING + "_" + activator +"_a)";
					}
				}
			}
			kineticLaw += ")/(1+(" + GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + 
					GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING + actBottom;
			for (int i=0;i<reaction.getModifierCount();i++) {
				if (BioModel.isRepressor(reaction.getModifier(i)) || BioModel.isRegulator(reaction.getModifier(i))) {
					String repressor = reaction.getModifier(i).getSpecies();
					String indexStr = "";
					ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(reaction.getModifier(i));
					for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
						Index index = sBasePlugin.getIndex(j,"species");
						if (index!=null)
							indexStr += "[" + SBMLutilities.myFormulaToString(index.getMath()) + "]";
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+repressor+"_"))==null) {
						kineticLaw += "+pow((" + GlobalConstants.FORWARD_KREP_STRING + "/" + GlobalConstants.REVERSE_KREP_STRING + ")*" 
								+ repressor+indexStr;
					} else {
						kineticLaw += "+pow((" + GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + repressor + "_") + "/" + 
								GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + repressor + "_") + ")*" + repressor+indexStr;
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+repressor+"_r")==null) {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + ")";
					} else {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + "_" + repressor +"_r)";
					}
				}
			}
			kineticLaw += ")";
		} else {
			kineticLaw = "(" + promoter + "*" + GlobalConstants.OCR_STRING + "*" +
					"(" + GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" 
					+ GlobalConstants.RNAP_STRING + ")/(1+(" + 
					GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" 
					+ GlobalConstants.RNAP_STRING;
			for (int i=0;i<reaction.getModifierCount();i++) {
				if (BioModel.isRepressor(reaction.getModifier(i))) {
					String repressor = reaction.getModifier(i).getSpecies();
					String indexStr = "";
					ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(reaction.getModifier(i));
					for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
						Index index = sBasePlugin.getIndex(j,"species");
						if (index!=null)
							indexStr += "[" + SBMLutilities.myFormulaToString(index.getMath()) + "]";
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+repressor+"_"))==null) {
						kineticLaw += "+pow((" + GlobalConstants.FORWARD_KREP_STRING + "/" + GlobalConstants.REVERSE_KREP_STRING + ")*" 
								+ repressor+indexStr;
					} else {
						kineticLaw += "+pow((" + GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + repressor + "_")+"/" + 
								GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + repressor + "_") + ")*" + repressor+indexStr;
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+repressor+"_r")==null) {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + ")";
					} else {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + "_" + repressor +"_r)";
					}
				}
			}
			kineticLaw += ")";
		}
		return kineticLaw;
 	}
	
	public void setLayoutSize() {
		Layout layout = getLayout();
		//double width = layout.getDimensions().getWidth();
		//double height = layout.getDimensions().getHeight();
		double width = 0;
		double height = 0;
		for (int i = 0; i < layout.getCompartmentGlyphCount(); i++) {
			CompartmentGlyph glyph = layout.getCompartmentGlyph(i);
			if (glyph.isSetBoundingBox()) {
				double x = glyph.getBoundingBox().getPosition().getX() + glyph.getBoundingBox().getDimensions().getWidth();
				if (x > width) width = x;
				double y = glyph.getBoundingBox().getPosition().getY() + glyph.getBoundingBox().getDimensions().getHeight();
				if (y > height) height = y;
			}
		}
		for (int i = 0; i < layout.getSpeciesGlyphCount(); i++) {
			SpeciesGlyph glyph = layout.getSpeciesGlyph(i);
			if (glyph.isSetBoundingBox()) {
				double x = glyph.getBoundingBox().getPosition().getX() + glyph.getBoundingBox().getDimensions().getWidth();
				if (x > width) width = x;
				double y = glyph.getBoundingBox().getPosition().getY() + glyph.getBoundingBox().getDimensions().getHeight();
				if (y > height) height = y;
			}
		}
		for (int i = 0; i < layout.getReactionGlyphCount(); i++) {
			ReactionGlyph glyph = layout.getReactionGlyph(i);
			if (glyph.isSetBoundingBox()) {
				double x = glyph.getBoundingBox().getPosition().getX() + glyph.getBoundingBox().getDimensions().getWidth();
				if (x > width) width = x;
				double y = glyph.getBoundingBox().getPosition().getY() + glyph.getBoundingBox().getDimensions().getHeight();
				if (y > height) height = y;
			}
		}
		for (int i = 0; i < layout.getAdditionalGraphicalObjectCount(); i++) {
			GraphicalObject glyph = layout.getAdditionalGraphicalObject(i);
			if (glyph.isSetBoundingBox()) {
				double x = glyph.getBoundingBox().getPosition().getX() + glyph.getBoundingBox().getDimensions().getWidth();
				if (x > width) width = x;
				double y = glyph.getBoundingBox().getPosition().getY() + glyph.getBoundingBox().getDimensions().getHeight();
				if (y > height) height = y;
			}
		}
		if (!layout.isSetDimensions()) {
			layout.createDimensions(0, 0, 0);
		}
		layout.getDimensions().setWidth(width);
		layout.getDimensions().setHeight(height);
	}
	
	/**
	 * Save the current object to file.
	 * 
	 * @param filename
	 */
	public void save(String filename) {		
		updatePorts();
		setGridSize(grid.getNumRows(),grid.getNumCols());
		setLayoutSize();
		SBMLutilities.pruneUnusedSpecialFunctions(sbml);
		SBMLWriter writer = new SBMLWriter();
		try {
			writer.writeSBMLToFile(sbml, filename.replace(".gcm",".xml"));
		}
		catch (SBMLException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public boolean saveAsLPN(String filename) {
		HashMap<String,Integer> constants = new HashMap<String,Integer>();
		ArrayList<String> booleans = new ArrayList<String>();
		HashMap<String,String> rates = new HashMap<String,String>();
		SBMLDocument flatSBML = flattenModel(true);
		SBMLutilities.expandFunctionDefinitions(flatSBML);
		SBMLutilities.expandInitialAssignments(flatSBML);
		LhpnFile lpn = new LhpnFile();
		String message = "The following items cannot be converted to an LPN:\n";
		boolean error = false;
		if (flatSBML.getModel().getCompartmentCount()>0) {
			message += "Compartments: ";
			for (int i = 0; i < flatSBML.getModel().getCompartmentCount(); i++) {
				message += flatSBML.getModel().getCompartment(i).getId() + " ";
			}
			message += "\n";
			error = true;
		}
		if (flatSBML.getModel().getSpeciesCount()>0) {
			message += "Species: ";
			for (int i = 0; i < flatSBML.getModel().getSpeciesCount(); i++) {
				message += flatSBML.getModel().getSpecies(i).getId() + " ";
			}
			message += "\n";
			error = true;
		}
		for (int i = 0; i < flatSBML.getModel().getRuleCount(); i++) {
			Rule r = flatSBML.getModel().getRule(i);
			if (r.isRate()) {
				if (r.getMath().isName()) {
					rates.put(r.getMath().getName(), SBMLutilities.getVariable(r));
				} else {
					error = true;
					message += "Rate rule: d(" + SBMLutilities.getVariable(r) + ")/dt := " + SBMLutilities.myFormulaToString(r.getMath()) + "\n";
				}
 			} else if (r.isAssignment()) {
 				if (!SBMLutilities.getVariable(r).startsWith(GlobalConstants.TRIGGER+"_")) {
 					error = true;
 					message += "Assignment rule: " + SBMLutilities.getVariable(r) + " := " + SBMLutilities.myFormulaToString(r.getMath()) + "\n";
 				}
 			} else {
				error = true;
 				message += "Algebraic rule: 0 := " + SBMLutilities.myFormulaToString(r.getMath()) + "\n";
 			}
 		}
		for (int i = 0; i < flatSBML.getModel().getParameterCount(); i++) {
			Parameter p = flatSBML.getModel().getParameter(i);
			if (p.getId().startsWith(GlobalConstants.TRIGGER+"_")) continue;
			if (SBMLutilities.isPlace(p)) {
				lpn.addPlace(p.getId(), (p.getValue()==1));
			} else if (SBMLutilities.isBoolean(p)){
				booleans.add(p.getId());
				if (p.getId().equals(GlobalConstants.FAIL)||
						p.getId().endsWith("__"+GlobalConstants.FAIL)) continue;
				Port port = getPortByIdRef(p.getId());
				if (port != null) {
					if (SBMLutilities.isInputPort(port)) {
						if (p.getValue()==0) {	
							lpn.addInput(p.getId(), "boolean", "false");
						} else {
							lpn.addInput(p.getId(), "boolean", "true");
						}						
					} else {
						if (p.getValue()==0) {	
							lpn.addOutput(p.getId(), "boolean", "false");
						} else {
							lpn.addOutput(p.getId(), "boolean", "true");
						}						
					}
				} else {
					if (p.getValue()==0) {	
						lpn.addBoolean(p.getId(), "false");
					} else {
						lpn.addBoolean(p.getId(), "true");
					}
				}
			} else {
				//if (rates.containsKey(p.getId())) continue;
				if (!p.getConstant()) {
					String type = "integer";
					if (rates.containsValue(p.getId())) type = "continuous"; 
					Port port = getPortByIdRef(p.getId());
					int lower = (int)Math.floor(p.getValue());
					int upper = (int)Math.ceil(p.getValue());
					String bound = String.valueOf(lower);
					if (lower!=upper) {
						bound = "[" + lower + "," + upper + "]";
					}
					InitialAssignment ia = flatSBML.getModel().getInitialAssignment(p.getId());
					if (ia != null) {
						ASTNode math = ia.getMath();
						if (math.getType()==ASTNode.Type.FUNCTION && math.getName().equals("uniform")) {
							ASTNode left = math.getLeftChild();
							ASTNode right = math.getRightChild();
							if (left.getType()==ASTNode.Type.INTEGER && right.getType()==ASTNode.Type.INTEGER) {
								lower = left.getInteger();
								upper = right.getInteger();
							}
						}
					}
					if (port != null) {
						if (SBMLutilities.isInputPort(port)) {
							lpn.addInput(p.getId(),type,bound);
						} else {
							lpn.addOutput(p.getId(),type,bound);
						}
					} 
					if (type.equals("integer")) {
						lpn.addInteger(p.getId(),bound);
					} else {
						for (String key : rates.keySet()) {
							if (rates.get(key).equals(p.getId())) {
								Parameter rp = flatSBML.getModel().getParameter(key);
								int lrate = (int)Math.floor(rp.getValue());
								int urate = (int)Math.ceil(rp.getValue());
								String boundRate = String.valueOf(lrate);
								if (lrate!=urate) {
									boundRate = "[" + lrate + "," + urate + "]";
								}
								ia = flatSBML.getModel().getInitialAssignment(rp.getId());
								if (ia != null) {
									ASTNode math = ia.getMath();
									if (math.getType()==ASTNode.Type.FUNCTION && math.getName().equals("uniform")) {
										ASTNode left = math.getLeftChild();
										ASTNode right = math.getRightChild();
										if (left.getType()==ASTNode.Type.INTEGER && right.getType()==ASTNode.Type.INTEGER) {
											lrate = left.getInteger();
											urate = right.getInteger();
										}
									}
								}
								lpn.addContinuous(p.getId(), bound,	boundRate);
								break;
							}
						}
					}
				} else {
					constants.put(p.getId(),(int)p.getValue());
				}
			}
		}
		boolean foundFail = false;
		for (int i = 0; i < flatSBML.getModel().getConstraintCount(); i++) {
			Constraint c = flatSBML.getModel().getConstraint(i);
			ASTNode math = c.getMath();
			if (math.getType()==ASTNode.Type.RELATIONAL_EQ) {
				ASTNode left = math.getLeftChild();
				ASTNode right = math.getRightChild();
				if (left.getType()==ASTNode.Type.NAME && 
						(left.getName().equals(GlobalConstants.FAIL)||
						left.getName().endsWith("__"+GlobalConstants.FAIL)) &&
					right.getType()==ASTNode.Type.INTEGER && right.getInteger()==0) {
					foundFail = true;
					continue;
				} else {
					error = true;
					message += "Constraint: " + SBMLutilities.myFormulaToString(math) + "\n";
				}
			}
			/*
			if (math.getType()==ASTNode.Type.FUNCTION && math.getName().equals("G") && math.getChildCount()==2) {
				ASTNode left = c.getMath().getLeftChild();
				ASTNode right = c.getMath().getRightChild();
				if (left.getType()==ASTNode.Type.CONSTANT_TRUE && right.getType()==ASTNode.Type.LOGICAL_NOT) {
					ASTNode child = right.getChild(0);
					if (child.getType()==ASTNode.Type.RELATIONAL_EQ) {
						left = child.getLeftChild();
						right = child.getRightChild();
						if (left.getType()==ASTNode.Type.NAME && left.getName().endsWith(GlobalConstants.FAIL) &&
							right.getType()==ASTNode.Type.INTEGER && right.getInteger()==1) {
							foundFail = true;
							continue;
						}
					}
				}
			} 
			*/
			//lpn.addProperty(SBMLutilities.SBMLMathToLPNString(c.getMath(), constants, booleans));
		}
		for (int i = 0; i < flatSBML.getModel().getEventCount(); i++) {
			Event e = flatSBML.getModel().getEvent(i);
			if (SBMLutilities.isTransition(e)) {
				Transition t = new Transition();
				t.setLpn(lpn);
				t.setName(e.getId());
				t.setPersistent(false);
				lpn.addTransition(t);
				ArrayList<String> preset = SBMLutilities.getPreset(flatSBML,e);
				for (int j = 0; j < preset.size(); j++) {
					t.addPreset(lpn.getPlace(preset.get(j)));
				}
				ArrayList<String> postset = SBMLutilities.getPostset(flatSBML,e);
				for (int j = 0; j < postset.size(); j++) {
					t.addPostset(lpn.getPlace(postset.get(j)));
				}
				Rule r = sbml.getModel().getRule(GlobalConstants.TRIGGER + "_" + e.getId());
				if (r != null) {
					t.setPersistent(true);
					ASTNode triggerMath = r.getMath();
					String trigger = SBMLutilities.myFormulaToString(triggerMath);
					if (triggerMath.getType()==ASTNode.Type.FUNCTION_PIECEWISE && triggerMath.getChildCount() > 2) {
						triggerMath = triggerMath.getChild(1);
						if (triggerMath.getType()==ASTNode.Type.LOGICAL_OR) {
							triggerMath = triggerMath.getLeftChild();
							for (int j = 0; j < sbml.getModel().getParameterCount(); j++) {
								Parameter parameter = sbml.getModel().getParameter(j);
								if (parameter!=null && SBMLutilities.isPlace(parameter)) {
									if (trigger.contains("eq("+parameter.getId()+", 1)")||
											trigger.contains("("+parameter.getId()+" == 1)")){
										triggerMath = SBMLutilities.removePreset(triggerMath, parameter.getId());
									}
								} 
							}
						}
					}
					t.addEnabling(SBMLutilities.SBMLMathToLPNString(triggerMath,constants,booleans));
				} else if (e.isSetTrigger()) {
					ASTNode triggerMath = e.getTrigger().getMath();
					String trigger = SBMLutilities.myFormulaToString(triggerMath);
					for (int j = 0; j < flatSBML.getModel().getParameterCount(); j++) {
						Parameter parameter = flatSBML.getSBMLDocument().getModel().getParameter(j);
						if (parameter!=null && SBMLutilities.isPlace(parameter)) {
							if (trigger.contains("eq("+parameter.getId()+", 1)")||
									trigger.contains("("+parameter.getId()+" == 1)")) {
								triggerMath = SBMLutilities.removePreset(triggerMath, parameter.getId());
							}
						}						
					}
					t.addEnabling(SBMLutilities.SBMLMathToLPNString(triggerMath,constants,booleans));
				}
				if (e.isSetDelay()) {
					t.addDelay(SBMLutilities.SBMLMathToLPNString(e.getDelay().getMath(),constants,booleans));
				}
				if (e.isSetPriority()) {
					t.addPriority(SBMLutilities.SBMLMathToLPNString(e.getPriority().getMath(),constants,booleans));
				}
				for (int j = 0; j < e.getEventAssignmentCount(); j++) {
					EventAssignment ea = e.getListOfEventAssignments().get(j);
					Parameter p = flatSBML.getModel().getParameter(ea.getVariable());
					if (p != null && !SBMLutilities.isPlace(p)) {
						if (rates.containsKey(ea.getVariable())) {
							t.addContAssign(ea.getVariable(), SBMLutilities.SBMLMathToLPNString(ea.getMath(),constants,booleans));
							t.addRateAssign(rates.get(ea.getVariable()), SBMLutilities.SBMLMathToLPNString(ea.getMath(),constants,booleans));
						} else if (rates.containsValue(ea.getVariable())) {
							t.addContAssign(ea.getVariable(), SBMLutilities.SBMLMathToLPNString(ea.getMath(),constants,booleans));
						} else if (booleans.contains(ea.getVariable())){
							if ((ea.getVariable().equals(GlobalConstants.FAIL)||
									ea.getVariable().endsWith("__"+GlobalConstants.FAIL)) && foundFail) {
								t.setFail(true);
							} else {
								t.addBoolAssign(ea.getVariable(), SBMLutilities.SBMLMathToBoolLPNString(ea.getMath(),constants,booleans));
							}
						} else {
							t.addIntAssign(ea.getVariable(), SBMLutilities.SBMLMathToLPNString(ea.getMath(),constants,booleans));
						}
					}
				}
			} else {
				error = true;
				message += "Event: " + e.getId() + "\n";
			}
		}
		if (error) {
			JTextArea messageArea = new JTextArea(message);
			messageArea.setEditable(false);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new java.awt.Dimension(400, 400));
			scroll.setPreferredSize(new java.awt.Dimension(400, 400));
			scroll.setViewportView(messageArea);		
			Object[] options = { "OK", "Cancel" };
			int value = JOptionPane.showOptionDialog(Gui.frame, scroll, "Conversion Errors",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.NO_OPTION) return false;
		}
		lpn.save(filename);
		return true;
	}

	public StringBuffer saveToBuffer() {
		setGridSize(grid.getNumRows(),grid.getNumCols());
		SBMLWriter writer = new SBMLWriter();
		String SBMLstr = null;
		try {
			SBMLstr = writer.writeSBMLToString(sbml);
		} catch (SBMLException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		StringBuffer buffer = new StringBuffer(SBMLstr);
		return buffer;
	}
	
	public boolean load(String filename) {
		//gcm2sbml.load(filename);
		this.filename = filename;
		String[] splitPath = filename.split(separator);
		sbmlFile = splitPath[splitPath.length-1].replace(".gcm",".xml");
		return loadSBMLFile(sbmlFile);
	}
	
//	public void correctPromoterToSBOLAnnotations() {
//		Model sbmlModel = sbml.getModel();
//		for (int i = 0; i < sbmlModel.getSpeciesCount(); i++) {
//			Species sbmlSpecies = sbmlModel.getSpecies(i);
//			if (isPromoterSpecies(sbmlSpecies)) {
//				List<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(sbmlSpecies);
//				if (sbolURIs.size() > 0)
//					
//			}
//			
//		}
//	}
	
	// Descriptor array should be size 3 and contain DNA component ID, name, and description in that order
	public void setSBOLDescriptors(String[] descriptors) {
		sbolDescriptors = descriptors;
	}
	
	public void setSBOLSaveFilePath(String saveFile) {
		sbolSaveFilePath = saveFile;
	}
	
//	public void setRemovedBioSimURI(URI dissociatedBioSimURI) {
//		this.dissociatedBioSimURI = dissociatedBioSimURI;
//	}
	
	public String[] getSBOLDescriptors() {
		return sbolDescriptors;
	}
	
	public String getSBOLSaveFilePath() {
		return sbolSaveFilePath;
	}
	
//	public URI getRemovedBioSimURI() {
//		return dissociatedBioSimURI;
//	}
	
//	public void deleteRemovedBioSimComponent(Set<String> sbolFilePaths) {
//		if (dissociatedBioSimURI != null) {
//			for (String filePath : sbolFilePaths) {
//				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
//				SBOLUtility.deleteDNAComponent(dissociatedBioSimURI, sbolDoc);
//				SBOLUtility.writeSBOLDocument(filePath, sbolDoc);
//			}
//			dissociatedBioSimURI = null;
//		}
//	}
	
	public int getPlaceHolderIndex() {
		return placeHolderIndex;
	}
	
	public void setPlaceHolderIndex(int set) {
		placeHolderIndex = set;
	}

	public void changePromoterId(String oldId, String newId) {
		SBMLutilities.updateVarId(sbml, true, oldId, newId);
		Species promoter = sbml.getModel().getSpecies(oldId); 
		if (promoter != null) 
			promoter.setId(newId);
		Reaction production = getProductionReaction(oldId);
		if (production.getId().contains(GlobalConstants.PRODUCTION)) 
			if (newId.contains("__")) {
				production.setId(GlobalConstants.PRODUCTION + "_" 
						+ newId.substring(newId.lastIndexOf("__") + 2));
			} else {
				production.setId(GlobalConstants.PRODUCTION + "_" + newId);
			}
		SpeciesReference product = production.getProductForSpecies(oldId+"_mRNA");
		if (product!=null) {
			sbml.getModel().getSpecies(oldId+"_mRNA").setId(newId+"_mRNA");
			product.setSpecies(newId+"_mRNA");
		}

		Layout layout = getLayout();
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+oldId)!=null) {
			SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+oldId);
			speciesGlyph.setId(GlobalConstants.GLYPH+"__"+newId);
			speciesGlyph.setSpecies(newId);
			//SBMLutilities.copyDimensionsIndices(promoter, speciesGlyph, "layout:species");
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldId)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldId);
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+newId);
				textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+newId);
				textGlyph.setText(SBMLutilities.getArrayId(sbml,newId));
				//SBMLutilities.copyDimensionsIndices(speciesGlyph, textGlyph, "layout:graphicalObject");
			}
		}
	}

	public void changeSpeciesId(String oldId, String newId) {
		if (sbml != null) {
			if (sbml.getModel() != null) {
				SBMLutilities.updateVarId(sbml, true, oldId, newId);
				if (sbml.getModel().getSpecies(oldId) != null) {
					sbml.getModel().getSpecies(oldId).setId(newId);
				}
			}
		}
		String baseId = newId;
		if (newId.contains("__")) {
			baseId = newId.substring(newId.lastIndexOf("__")+2);
		}
		if (isSpeciesConstitutive(oldId)) {
			Reaction reaction = sbml.getModel().getReaction("Constitutive_"+oldId);
			reaction.setId("Constitutive_"+baseId);
		}
		Reaction diffusion = getDiffusionReaction(oldId,sbml.getModel());
		if (diffusion != null) {
			diffusion.setId("MembraneDiffusion_"+baseId);
		}
		Reaction degradation = getDegradationReaction(oldId);
		if (degradation != null && degradation.getId().contains(GlobalConstants.DEGRADATION))
			degradation.setId(GlobalConstants.DEGRADATION + "_" + baseId);
		Reaction complexation = getComplexReaction(oldId);
		if (complexation != null && complexation.getId().contains(GlobalConstants.COMPLEXATION))
			complexation.setId(GlobalConstants.COMPLEXATION + "_" + baseId);
		for (int i=0;i<sbml.getModel().getReactionCount();i++) {
			Reaction reaction = sbml.getModel().getReaction(i);
			if (BioModel.isComplexReaction(reaction)) {
				KineticLaw k = reaction.getKineticLaw();
				for (int j=0;j<k.getLocalParameterCount();j++) {
					LocalParameter param = k.getLocalParameter(j);
					if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldId)) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newId);
					}
				}
				reaction.getKineticLaw().setMath(SBMLutilities.myParseFormula(createComplexKineticLaw(reaction)));
			} else if (BioModel.isProductionReaction(reaction)) {
				KineticLaw k = reaction.getKineticLaw();
				for (int j=0;j<k.getLocalParameterCount();j++) {
					LocalParameter param = k.getLocalParameter(j);
					if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldId + "_r")) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newId + "_r");
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.COOPERATIVITY_STRING + "_" + oldId + "_r",
								GlobalConstants.COOPERATIVITY_STRING + "_" + newId + "_r"));
					} else if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldId + "_a")) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newId + "_a");
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.COOPERATIVITY_STRING + "_" + oldId + "_a",
								GlobalConstants.COOPERATIVITY_STRING + "_" + newId + "_a"));
					} else if (param.getId().equals(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + oldId + "_"))) {
						param.setId(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + newId + "_"));
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + oldId + "_"),
								GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + newId + "_")));
					} else if (param.getId().equals(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + oldId + "_"))) {
						param.setId(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + newId + "_"));
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + oldId + "_"),
								GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + newId + "_")));
					} else if (param.getId().equals(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + oldId + "_"))) {
						param.setId(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + newId + "_"));
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + oldId + "_"),
								GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + newId + "_")));
					} else if (param.getId().equals(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + oldId + "_"))) {
						param.setId(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + newId + "_"));
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + oldId + "_"),
								GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + newId + "_")));
					}
				}
			}
		}
		Layout layout = getLayout();
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+oldId)!=null) {
			SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+oldId);
			speciesGlyph.setId(GlobalConstants.GLYPH+"__"+newId);
			speciesGlyph.setSpecies(newId);
			//SBMLutilities.copyDimensionsIndices(sbml.getModel().getSpecies(newId), speciesGlyph, "layout:species");
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldId)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldId);
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+newId);
				textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+newId);
				textGlyph.setText(SBMLutilities.getArrayId(sbml, newId));
				//SBMLutilities.copyDimensionsIndices(speciesGlyph, textGlyph, "layout:graphicalObject");
			}
		}
	}

	public void changeComponentName(String oldName, String newName) {
		String submodelID = oldName;
		
		//look through locations parameters for find the submodel
		for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
			
			Parameter param = sbml.getModel().getParameter(i);
			
			if (param.getId().contains("__locations"))
				if (AnnotationUtility.parseArrayAnnotation(param, oldName)!=null)
					submodelID = "GRID__" + param.getId().replace("__locations", "");
		}
		
		Submodel subModel = sbmlCompModel.getListOfSubmodels().get(submodelID);
		
		if (subModel == null) {
			submodelID = submodelID.replace("GRID__", "");
			subModel = sbmlCompModel.getListOfSubmodels().get(submodelID);
		}
		
		if (sbml.getModel().getParameter(subModel.getId().replace("GRID__","") + "__locations") != null) {
			
			Parameter param = sbml.getModel().getParameter(subModel.getId().replace("GRID__","") + "__locations");
			String value = AnnotationUtility.parseArrayAnnotation(param, oldName);
			if (value != null) {
				AnnotationUtility.removeArrayAnnotation(param, oldName);
				value = value.split("=")[1];
				AnnotationUtility.setArrayAnnotation(param, newName + "=" + value);
			}
			if (param.getId().contains(oldName))
				param.setId(param.getId().replace(oldName, newName));
		}
		else
			subModel.setId(newName);
		
		subModel.setId(submodelID.replace(oldName, newName));
		
		for (int i = 0; i < sbml.getModel().getSpeciesCount(); i++) {
			CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(sbml.getModel().getSpecies(i));
			ReplacedElement replacement = null;
			for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
				replacement = sbmlSBase.getListOfReplacedElements().get(j);
				if (replacement.getSubmodelRef().equals(oldName)) {
					replacement.setSubmodelRef(newName);
				}
			}
		}
		Layout layout = getLayout();
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+oldName)!=null) {
			GeneralGlyph generalGlyph = (GeneralGlyph)
					layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+oldName);
			generalGlyph.setId(GlobalConstants.GLYPH+"__"+newName);
			generalGlyph.setReference(newName);
			//SBMLutilities.copyDimensionsIndices(subModel, generalGlyph, "layout:reference");
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldName)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldName);
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+newName);
				textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+newName);
				textGlyph.setText(SBMLutilities.getArrayId(sbml,newName));
				//SBMLutilities.copyDimensionsIndices(generalGlyph, textGlyph, "layout:graphicalObject");
			}
		}
	}

	public void addReactantToReaction(String reactantId,String reactionId) {
		Reaction r = sbml.getModel().getReaction(reactionId);
		SpeciesReference s = r.createReactant();
		s.setSpecies(reactantId);
		s.setStoichiometry(1.0);
		s.setConstant(true);
		if (SBMLutilities.dimensionsMatch(r,sbml.getModel().getSpecies(reactantId))) {
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
			ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(s);
			sBasePluginProduct.unsetListOfIndices();
			for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
				Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
				Index index = sBasePluginProduct.createIndex();
				index.setReferencedAttribute("species");
				index.setArrayDimension(i);
				index.setMath(SBMLutilities.myParseFormula(dim.getId()));
			}
		}			
	}
	
	public void addProductToReaction(String productID, Reaction rxn) {
		SpeciesReference s = rxn.createProduct();
		s.setSpecies(productID);
		s.setStoichiometry(1.0);
		s.setConstant(true);
		if (SBMLutilities.dimensionsMatch(rxn,sbml.getModel().getSpecies(productID))) {
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(rxn);
			ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(s);
			sBasePluginProduct.unsetListOfIndices();
			for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
				Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
				Index index = sBasePluginProduct.createIndex();
				index.setReferencedAttribute("species");
				index.setArrayDimension(i);
				index.setMath(SBMLutilities.myParseFormula(dim.getId()));
			}
		}	
	}

	public void addProductToReaction(String productID, String rxnID) {
		Reaction rxn = sbml.getModel().getReaction(rxnID);
		addProductToReaction(productID, rxn);
	}

	public void addModifierToReaction(String modifierId,String reactionId) {
		Reaction r = sbml.getModel().getReaction(reactionId);
		ModifierSpeciesReference s = r.createModifier();
		s.setSpecies(modifierId);
		if (SBMLutilities.dimensionsMatch(r,sbml.getModel().getSpecies(modifierId))) {
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
			ArraysSBasePlugin sBasePluginProduct = SBMLutilities.getArraysSBasePlugin(s);
			sBasePluginProduct.unsetListOfIndices();
			for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
				Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
				Index index = sBasePluginProduct.createIndex();
				index.setReferencedAttribute("species");
				index.setArrayDimension(i);
				index.setMath(SBMLutilities.myParseFormula(dim.getId()));
			}
		}			
	}
	
	public void addReaction(String sourceID,String targetID,boolean isModifier) {
		Model m = sbml.getModel();
		JPanel reactionListPanel = new JPanel(new GridLayout(1, 1));
		ArrayList<String> choices = new ArrayList<String>();
		choices.add("Create a new reaction");
		for (int i=0; i < m.getReactionCount(); i++) {
			Reaction r = m.getReaction(i);
			if (BioModel.isDegradationReaction(r)) continue;
			if (BioModel.isDiffusionReaction(r)) continue;
			if (BioModel.isProductionReaction(r)) continue;
			if (BioModel.isComplexReaction(r)) continue;
			if (BioModel.isConstitutiveReaction(r)) continue;
			if (isGridReaction(r)) continue;
			
			if (!isModifier && r.getReactantForSpecies(sourceID) != null) {
				choices.add("Add " + targetID + " as a product of reaction " + r.getId());
			}
			if (r.getProductForSpecies(targetID) != null) {
				if (isModifier) {
					choices.add("Add " + sourceID + " as a modifier of reaction " + r.getId());
				} else {
					choices.add("Add " + sourceID + " as a reactant of reaction " + r.getId());
				}
			}
		}
		
		Object[] options = { "OK", "Cancel" };
		JComboBox reactionList = new JComboBox(choices.toArray());
		if (choices.size()>1) {
			reactionListPanel.add(reactionList);
			int value = JOptionPane.showOptionDialog(Gui.frame, reactionListPanel, "Reaction Choice",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.NO_OPTION) return;
		}
		if (((String)reactionList.getSelectedItem()).contains("reactant")) {
			String[] selection = ((String)reactionList.getSelectedItem()).split(" ");
			String reactionId = selection[selection.length-1];
			Reaction r = m.getReaction(reactionId);
			SpeciesReference s = r.createReactant();
			s.setSpecies(sourceID);
			s.setStoichiometry(1.0);
			s.setConstant(true);
		} else if (((String)reactionList.getSelectedItem()).contains("modifier")) {
			String[] selection = ((String)reactionList.getSelectedItem()).split(" ");
			String reactionId = selection[selection.length-1];
			Reaction r = m.getReaction(reactionId);
			ModifierSpeciesReference s = r.createModifier();
			s.setSpecies(sourceID);
		} else if (((String)reactionList.getSelectedItem()).contains("product")) {
			String[] selection = ((String)reactionList.getSelectedItem()).split(" ");
			String reactionId = selection[selection.length-1];
			Reaction r = m.getReaction(reactionId);
			SpeciesReference s = r.createProduct();
			s.setSpecies(targetID);
			s.setStoichiometry(1.0);
			s.setConstant(true);
		} else {
			Reaction r = m.createReaction();
			String reactionId = "r0";
			int i = 0;
			while (SBMLutilities.getElementBySId(sbml, reactionId)!=null) {
				i++;
				reactionId = "r" + i;
			}
			r.setId(reactionId);
			r.setReversible(false);
			r.setFast(false);
			ArrayList<String> CompChoices = new ArrayList<String>();
			if (isModifier) { 
				ModifierSpeciesReference source = r.createModifier();
				source.setSpecies(sourceID);
				CompChoices.add(sbml.getModel().getSpecies(sourceID).getCompartment());
				r.setCompartment(sbml.getModel().getSpecies(sourceID).getCompartment());
			} else {
				SpeciesReference source = r.createReactant();
				source.setSpecies(sourceID);
				source.setConstant(true);
				source.setStoichiometry(1.0);
				CompChoices.add(sbml.getModel().getSpecies(sourceID).getCompartment());
				r.setCompartment(sbml.getModel().getSpecies(sourceID).getCompartment());
			}
			SpeciesReference target = r.createProduct();
			target.setSpecies(targetID);
			target.setConstant(true);
			target.setStoichiometry(1.0);
			if (!r.getCompartment().equals(sbml.getModel().getSpecies(targetID).getCompartment())) {
				CompChoices.add(sbml.getModel().getSpecies(targetID).getCompartment());
				JComboBox compartmentList = new JComboBox(CompChoices.toArray());
				JPanel compartmentListPanel = new JPanel(new GridLayout(1, 1));
				compartmentListPanel.add(compartmentList);
				JOptionPane.showOptionDialog(Gui.frame, compartmentListPanel, "Compartment Choice",
						JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				r.setCompartment((String)compartmentList.getSelectedItem());
			}
			KineticLaw k = r.createKineticLaw();
			LocalParameter p = k.createLocalParameter();
			p.setId("kf");
			p.setValue(0.1);
			p = k.createLocalParameter();
			p.setId("kr");
			p.setValue(1.0);
			k.setMath(SBMLutilities.myParseFormula("kf*"+sourceID));
		}
	}

	/**
	 * Add a component given the specified name and properties. If either is
	 * null then they will be created using (hopefully) sensible defaults.
	 * 
	 * @param name
	 * @param properties
	 * 
	 * @return: the id of the created component.
	 */
	public String addComponent(String submodelID, String modelFile, boolean enclosed, ArrayList<String> compartmentPorts, 
			int row, int col, double x, double y, String md5) {
		
		ExternalModelDefinition extModel = null;
		String extId = modelFile.replace(".gcm","").replace(".xml","");
		
		if (sbmlComp.getListOfExternalModelDefinitions().get(extId) == null)
			extModel = sbmlComp.createExternalModelDefinition();
		else
			extModel = sbmlComp.getListOfExternalModelDefinitions().get(extId);
		
		extModel.setId(extId);
		extModel.setSource(modelFile.replace(".gcm",".xml"));
		extModel.setMd5(md5);
		
		//figure out what the submodel's ID should be if it's not provided
		if (submodelID == null) {
			
			int count = 1;
			submodelID = "C" + count;
			boolean changed = false;
			do { 
				changed = false;
				if (SBMLutilities.getElementBySId(sbml, submodelID)!=null) {
					++count;
					submodelID = "C" + count;
					changed = true;
				}
				for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
					Parameter parameter = sbml.getModel().getParameter(i);
					if (parameter.getId().endsWith("__locations")) {
						while (AnnotationUtility.parseArrayAnnotation(parameter, submodelID)!=null) {
							++count;
							submodelID = "C" + count;
							changed = true;
						}
					}
				}
			} while (changed);
			while (this.getSBMLCompModel().getListOfSubmodels().get(submodelID) != null) {
				
				++count;
				submodelID = "C" + count;
			}			
		}
		
		Compartment compartment = sbml.getModel().getCompartment(getDefaultCompartment());
		if (!isGridEnabled()) {
			String comp = getCompartmentByLocation((float)x,(float)y,GlobalConstants.DEFAULT_COMPONENT_WIDTH,
					GlobalConstants.DEFAULT_COMPONENT_HEIGHT);
			// TODO: not sure if this is right approach.  What about the default compartment above?
			if (comp.equals("")) {
				if (sbml.getModel().getCompartmentCount() > 0) {
					comp = sbml.getModel().getCompartment(0).getId();
				}
			}
			sbml.getModel().getCompartment(comp);
		}
			
		int numRows = grid.getNumRows();
		int numCols = grid.getNumCols();
		
		//if a gridded/arrayed submodel exists, it'll have this ID
		String gridSubmodelID = "GRID__" + extId;
		
		Submodel potentialGridSubmodel = sbmlCompModel.getListOfSubmodels().get(gridSubmodelID);
		
		if (potentialGridSubmodel != null) {
				
			//if the annotation string already exists, then one of these existed before
			//so update its count
			int size = AnnotationUtility.parseArraySizeAnnotation(potentialGridSubmodel);
			if (size >= 0 && !(numRows == 0 && numCols == 0)) {
				AnnotationUtility.setArraySizeAnnotation(potentialGridSubmodel, ++size);
			}
			else {
				AnnotationUtility.setArraySizeAnnotation(potentialGridSubmodel, 1);
			}
			
			potentialGridSubmodel.setModelRef(extId);
		}
		else {
			
			if (!(numRows == 0 && numCols == 0)) {
			
				potentialGridSubmodel = sbmlCompModel.createSubmodel();
				potentialGridSubmodel.setId(gridSubmodelID);
				AnnotationUtility.setArraySizeAnnotation(potentialGridSubmodel, 1);
				potentialGridSubmodel.setModelRef(extId);
				
				if (compartment!=null) {
					CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(compartment);
					boolean foundIt = false;
					for (int i=0;i<sbmlSBase.getListOfReplacedElements().size();i++) {
						ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(i);
						if (replacement.getSubmodelRef().equals(gridSubmodelID)) foundIt = true;
					}
					if (!foundIt) {
						for (String compartmentPort : compartmentPorts) {
							ReplacedElement replacement = sbmlSBase.createReplacedElement();
							replacement.setSubmodelRef(gridSubmodelID);
							replacement.setPortRef(GlobalConstants.COMPARTMENT+"__"+compartmentPort);
						}
					}
				}
			}
			else {
				
				Submodel submodel = null;
				
				if (sbmlCompModel.getListOfSubmodels().get(submodelID) == null)
					submodel = sbmlCompModel.createSubmodel();
				else
					submodel = sbmlCompModel.getListOfSubmodels().get(submodelID);
				
				submodel.setId(submodelID);
				submodel.setModelRef(extId);
				// Set default submodel metaID
				metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, submodel, metaIDIndex); 
				
				if (compartment!=null) {
					CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(compartment);
					boolean foundIt = false;
					for (int i=0;i<sbmlSBase.getListOfReplacedElements().size();i++) {
						ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(i);
						if (replacement.getSubmodelRef().equals(submodelID)) foundIt = true;
					}
					if (!foundIt) {
						for (String compartmentPort : compartmentPorts) {
							ReplacedElement replacement = sbmlSBase.createReplacedElement();
							replacement.setSubmodelRef(submodelID);
							replacement.setPortRef(GlobalConstants.COMPARTMENT+"__"+compartmentPort);
						}
					}
				}
			}
		}
		
		if (!(numRows == 0 && numCols == 0)) {
			
			//add an entry to the location parameter for the external model			
			String locationParameterID = extModel.getId() + "__locations";
			
			Parameter locationParameter = sbml.getModel().getParameter(locationParameterID);
			
			if (locationParameter == null) {
				
				locationParameter = sbml.getModel().createParameter();
				locationParameter.setId(locationParameterID);
				locationParameter.setConstant(false);
				locationParameter.setValue(0);
			}
			AnnotationUtility.appendArrayAnnotation(locationParameter, submodelID + "=\"(" + row + "," + col + ")\"");
			
			if (enclosed)
				createGridSpecies(gridSubmodelID);
		}
		
		//set layout information
		
		Layout layout = getLayout();
		
		GeneralGlyph generalGlyph = null;
		
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+submodelID)!=null) {
			generalGlyph = (GeneralGlyph)layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+submodelID);
		} else {
			generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+submodelID);
			generalGlyph.createBoundingBox();
			generalGlyph.getBoundingBox().createDimensions();
			generalGlyph.getBoundingBox().createPosition();
			generalGlyph.setReference(submodelID);
		}
		
		generalGlyph.getBoundingBox().getPosition().setX(x);
		generalGlyph.getBoundingBox().getPosition().setY(y);
		generalGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_COMPONENT_WIDTH);
		generalGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_COMPONENT_HEIGHT);
		
		TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+submodelID);
		textGlyph.createBoundingBox();
		textGlyph.getBoundingBox().createDimensions();
		textGlyph.getBoundingBox().createPosition();
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+submodelID);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,submodelID));
		textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());
		
		return submodelID;
	}
	
	/**
	 * loads the grid size from the gcm file
	 * 
	 * @param data string data from a gcm file
	 */
	private void loadGridSize() {
		int[] gridSize = null;
		if (sbml.getModel().getCompartment(0) != null) {
			gridSize = AnnotationUtility.parseGridAnnotation(sbml.getModel().getCompartment(0));
		}
		if (gridSize == null && sbmlLayout != null && getLayout()!=null) {
			gridSize = AnnotationUtility.parseGridAnnotation(getLayout());
			if (gridSize != null) {
				AnnotationUtility.removeGridAnnotation(getLayout());
				AnnotationUtility.setGridAnnotation(sbml.getModel().getCompartment(0), gridSize[0], gridSize[1]);
			}
		}
		if (gridSize != null && gridSize[0] > 0 && gridSize[1] > 0) {
			buildGrid(gridSize[0], gridSize[1]);
		} 
	}

	public void removeSpecies(String id) {
		if (id != null) {
			ListOf<InitialAssignment> r = sbml.getModel().getListOfInitialAssignments();
			for (int i = 0; i < sbml.getModel().getInitialAssignmentCount(); i++) {
				if (r.get(i).getVariable().equals(id)) {
					r.remove(i);
				}
			}
			sbml.getModel().removeSpecies(id);
			if (isSpeciesConstitutive(id)) {
				removeReaction("Constitutive_"+id);
			}
			if (getDiffusionReaction(id,sbml.getModel())!=null) {
				removeReaction("MembraneDiffusion_"+id);
			}
			Reaction degradation = getDegradationReaction(id);
			if (degradation != null) {
				removeReaction(degradation.getId());
			}
			Layout layout = getLayout();
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				layout.getListOfSpeciesGlyphs().remove(GlobalConstants.GLYPH+"__"+id);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id) != null) {
				layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+id);
			}
			for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
				Port port = sbmlCompModel.getListOfPorts().get(i);
				if (port.isSetIdRef() && port.getIdRef().equals(id)) {
					sbmlCompModel.removePort(port);
				}
			}
			speciesPanel.refreshSpeciesPanel(this);
			reactionPanel.refreshReactionPanel(this);
			compartmentPanel.refreshCompartmentPanel(this);
		}
	}
	
	public void removeReaction(String id) {
		if (SBMLutilities.variableInUse(sbml, id, false, true, true)) return;
//		Reaction tempReaction = sbml.getModel().getReaction(id);
//
//		ListOf<Reaction> r = sbml.getModel().getListOfReactions();
//		for (int i = 0; i < sbml.getModel().getReactionCount(); i++) {
//			if (r.get(i).getId().equals(tempReaction.getId())) {
//				r.remove(i);
//			}
//		}
		sbml.getModel().removeReaction(id);
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
			Port port = sbmlCompModel.getListOfPorts().get(i);
			if (port.isSetIdRef() && port.getIdRef().equals(id)) {
				sbmlCompModel.getListOfPorts().remove(i);
				break;
			}
		}
//		int i=0;
//		while (i < sbmlFBC.getFluxBoundCount()) {
//			FluxBound fluxBound = sbmlFBC.getFluxBound(i);
//			if (fluxBound.getReaction().equals(id)) {
//				sbmlFBC.removeFluxBound(i);
//			} else {
//				i++;
//			}
//		}
		Layout layout = getLayout();
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			layout.getListOfReactionGlyphs().remove(GlobalConstants.GLYPH+"__"+id);
		}
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id) != null) {
			layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+id);
		}
	}
	
	public void removeByMetaId(String metaId) {
		SBase sbase = SBMLutilities.getElementByMetaId(sbml.getModel(), metaId);
		if (sbase != null) {
			sbase.removeFromParent();
			for (int j = 0; j < sbmlCompModel.getListOfPorts().size(); j++) {
				Port port = sbmlCompModel.getListOfPorts().get(j);
				if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(metaId)) {
					sbmlCompModel.getListOfPorts().remove(j);
					break;
				}
			}
		}
		Layout layout = getLayout();
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+metaId)!=null) {
			layout.getListOfAdditionalGraphicalObjects().remove(GlobalConstants.GLYPH+"__"+metaId);
		}
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+metaId) != null) {
			layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+metaId);
		}
	}
	
	public void removeById(String id) {
		SBase sbase = SBMLutilities.getElementBySId(sbml.getModel(), id);
		ListOf<InitialAssignment> r = sbml.getModel().getListOfInitialAssignments();
		for (int i = 0; i < sbml.getModel().getInitialAssignmentCount(); i++) {
			if (r.get(i).getVariable().equals(id)) {
				r.remove(i);
			}
		}
		if (sbase != null) {
			sbase.removeFromParent();
			for (int j = 0; j < sbmlCompModel.getListOfPorts().size(); j++) {
				Port port = sbmlCompModel.getListOfPorts().get(j);
				if (port.isSetIdRef() && port.getIdRef().equals(id)) {
					sbmlCompModel.getListOfPorts().remove(j);
					break;
				}
			}
		}
		Layout layout = getLayout();
		if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			layout.getListOfCompartmentGlyphs().remove(GlobalConstants.GLYPH+"__"+id);
		}
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			layout.getListOfSpeciesGlyphs().remove(GlobalConstants.GLYPH+"__"+id);
		}
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			layout.getListOfReactionGlyphs().remove(GlobalConstants.GLYPH+"__"+id);
		}
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id)!=null) {
			layout.getListOfAdditionalGraphicalObjects().remove(GlobalConstants.GLYPH+"__"+id);
		}
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id) != null) {
			layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+id);
		}
	}

	public boolean isSIdInUse(String id) {
		return (SBMLutilities.getElementBySId(sbml, id)!=null);
	}

	public ArrayList<String> getCompartments() {
		ArrayList<String> compartmentSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getCompartmentCount(); i++) {
				Compartment compartment = sbml.getModel().getCompartment(i);
				compartmentSet.add(compartment.getId());
			}
		}
		return compartmentSet;
	}

	// TODO: remove special functions
	public ArrayList<String> getFunctions() {
		ArrayList<String> functionSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getFunctionDefinitionCount(); i++) {
				FunctionDefinition function = sbml.getModel().getFunctionDefinition(i);
				functionSet.add(function.getId());
			}
		}
		return functionSet;
	}

	public ArrayList<String> getUnits() {
		ArrayList<String> unitSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getUnitDefinitionCount(); i++) {
				UnitDefinition unit = sbml.getModel().getUnitDefinition(i);
				unitSet.add(unit.getId());
			}
		}
		return unitSet;
	}
	
	public ArrayList<String> getAlgebraicRules() {
		ArrayList<String> ruleSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getRuleCount(); i++) {
				Rule rule = sbml.getModel().getRule(i);
				if (rule.isAlgebraic()) {
					ruleSet.add(rule.getMetaId());
				}
			}
		}
		return ruleSet;
	}
	
	public ArrayList<String> getAssignmentRules() {
		ArrayList<String> ruleSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getRuleCount(); i++) {
				Rule rule = sbml.getModel().getRule(i);
				if (rule.isAssignment()) {
					ruleSet.add(rule.getMetaId());
				}
			}
		}
		return ruleSet;
	}
	
	public ArrayList<String> getRateRules() {
		ArrayList<String> ruleSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getRuleCount(); i++) {
				Rule rule = sbml.getModel().getRule(i);
				if (rule.isRate()) {
					ruleSet.add(rule.getMetaId());
				}
			}
		}
		return ruleSet;
	}
	
	public ArrayList<String> getConstraints() {
		ArrayList<String> constraintSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getConstraintCount(); i++) {
				Constraint constraint = sbml.getModel().getConstraint(i);
				constraintSet.add(constraint.getMetaId());
			}
		}
		return constraintSet;
	}

	public ArrayList<String> getEvents() {
		ArrayList<String> eventSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getEventCount(); i++) {
				Event event = sbml.getModel().getEvent(i);
				if (SBMLutilities.isTransition(event)) continue;
				eventSet.add(event.getId());
			}
		}
		return eventSet;
	}

	public ArrayList<String> getTransitions() {
		ArrayList<String> eventSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getEventCount(); i++) {
				Event event = sbml.getModel().getEvent(i);
				if (!SBMLutilities.isTransition(event)) continue;
				eventSet.add(event.getId());
			}
		}
		return eventSet;
	}

	public ArrayList<String> getReactions() {
		ArrayList<String> reactionSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getReactionCount(); i++) {
				Reaction r = sbml.getModel().getReaction(i);
				if (BioModel.isDegradationReaction(r)) continue;
				if (BioModel.isDiffusionReaction(r)) continue;
				if (BioModel.isProductionReaction(r)) continue;
				if (BioModel.isComplexReaction(r)) continue;
				if (BioModel.isConstitutiveReaction(r)) continue;
				if (isGridReaction(r)) continue;
				reactionSet.add(r.getId());
			}
		}
		return reactionSet;
	}

	public ArrayList<String> getParameters() {
		ArrayList<String> parameterSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getParameterCount(); i++) {
				Parameter parameter = sbml.getModel().getParameter(i);
				if (SBMLutilities.isBoolean(parameter)) continue;
				if (SBMLutilities.isPlace(parameter)) continue;
				parameterSet.add(parameter.getId());
			}
		}
		return parameterSet;
	}

	public ArrayList<String> getBooleans() {
		ArrayList<String> parameterSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getParameterCount(); i++) {
				Parameter parameter = sbml.getModel().getParameter(i);
				if (!SBMLutilities.isBoolean(parameter)) continue;
				parameterSet.add(parameter.getId());
			}
		}
		return parameterSet;
	}

	public ArrayList<String> getPlaces() {
		ArrayList<String> parameterSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getParameterCount(); i++) {
				Parameter parameter = sbml.getModel().getParameter(i);
				if (!SBMLutilities.isPlace(parameter)) continue;
				parameterSet.add(parameter.getId());
			}
		}
		return parameterSet;
	}

	public ArrayList<String> getConstantUserParameters() {
		ArrayList<String> parameterSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getParameterCount(); i++) {
				Parameter parameter = sbml.getModel().getParameter(i);
				if (parameter.getConstant() && !IsDefaultParameter(parameter.getId())) {
					parameterSet.add(parameter.getId());
				}
			}
		}
		return parameterSet;
	}
	
	public ArrayList<String> getSpecies() {
		ArrayList<String> speciesSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getSpeciesCount(); i++) {
				Species species = sbml.getModel().getSpecies(i);
				if (!BioModel.isPromoterSpecies(species) && !BioModel.isMRNASpecies(sbml, species)) {
					speciesSet.add(species.getId());
				}
			}
		}
		return speciesSet;
	}

	public ArrayList<String> getPromoters() {
		ArrayList<String> promoterSet = new ArrayList<String>();
		for (int i = 0; i < sbml.getModel().getSpeciesCount(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			if (BioModel.isPromoterSpecies(species)) {
				promoterSet.add(species.getId());
			}
		}
		return promoterSet;
	}
	
	public ArrayList<String> getCompartmentPorts() {
		ArrayList<String> compartments = new ArrayList<String>();
		for (int i = 0; i < sbml.getModel().getCompartmentCount(); i++) {
			Compartment compartment = sbml.getModel().getCompartment(i);
			if (sbmlCompModel.getListOfPorts().get(GlobalConstants.COMPARTMENT+"__"+compartment.getId())!=null) {
				compartments.add(compartment.getId());
			}
		}
		return compartments;
	}
	
	public Port getPortByIdRef(String idRef) {
		return SBMLutilities.getPortByIdRef(sbmlCompModel, idRef);
	}
	
	public Port getPortByMetaIdRef(String metaIdRef) {
		return SBMLutilities.getPortByMetaIdRef(sbmlCompModel, metaIdRef);
	}
	
	public Port getPortByUnitRef(String unitIdRef) {
		return SBMLutilities.getPortByUnitRef(sbmlCompModel, unitIdRef);
	}
	
	public Port getPortBySBaseRef(SBaseRef sbaseRef) {
		return SBMLutilities.getPortBySBaseRef(sbmlCompModel, sbaseRef);
	}

	public ArrayList<String> getPorts() {
		ArrayList<String> ports = new ArrayList<String>();
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
			Port port = sbmlCompModel.getListOfPorts().get(i);
			String dimStr = SBMLutilities.getDimensionString(port);
			String id = port.getId();
			if (port.isSetIdRef()) {
				String idRef = port.getIdRef();
				SBase sbase = SBMLutilities.getElementBySId(sbml, idRef);
				if (sbase!=null) {
					String type = SBMLutilities.getElementBySId(sbml, idRef).getElementName();
					if (type.equals(GlobalConstants.SBMLREACTION)) {
						Reaction r = sbml.getModel().getReaction(idRef);
						if (BioModel.isDegradationReaction(r)) continue;
						if (BioModel.isDiffusionReaction(r)) continue;
						if (BioModel.isProductionReaction(r)) continue;
						if (BioModel.isComplexReaction(r)) continue;
						if (BioModel.isConstitutiveReaction(r)) continue;
						if (isGridReaction(r)) continue;						
					}
					if (sbase.isSetSBOTerm()) {
						if (sbase.getSBOTerm()==GlobalConstants.SBO_PROMOTER_BINDING_REGION) {
							type = GlobalConstants.PROMOTER;
						} else if (sbase.getSBOTerm()==GlobalConstants.SBO_LOGICAL) {
							type = GlobalConstants.BOOLEAN;
						} else if (sbase.getSBOTerm()==GlobalConstants.SBO_PETRI_NET_PLACE) {
							type = GlobalConstants.PLACE;
						} else if (sbase.getSBOTerm()==GlobalConstants.SBO_PETRI_NET_TRANSITION) {
							type = GlobalConstants.TRANSITION;
						}
					}
					ports.add(type + ":" + id + ":" + idRef+dimStr);
				}
			} else if (port.isSetMetaIdRef()) {
				String idRef = port.getMetaIdRef();
				SBase sbase = SBMLutilities.getElementByMetaId(sbml, idRef);
				if (sbase!=null) {
					String type = SBMLutilities.getElementByMetaId(sbml, idRef).getElementName();
					ports.add(type + ":" + id + ":" + idRef+dimStr);
				}
			} else if (port.isSetUnitRef()) {
				String idRef = port.getUnitRef();
				SBase sbase = sbml.getModel().getUnitDefinition(idRef);
				if (sbase!=null) {
					String type = sbml.getModel().getUnitDefinition(idRef).getElementName();
					ports.add(type + ":" + id + ":" + idRef+dimStr);
				}
			}
		}
		Collections.sort(ports);
		return ports;
	}
	
	public ArrayList<String> getInputSpecies() {
		ArrayList<String> inputs = new ArrayList<String>();
		for (String spec : getSpecies()) {
			if (isInput(spec)) {
				inputs.add(spec);
			}
		}
		return inputs;
	}

	public ArrayList<String> getOutputSpecies() {
		ArrayList<String> outputs = new ArrayList<String>();
		for (String spec : getSpecies()) {
			if (isOutput(spec)) {
				outputs.add(spec);
			}
		}
		return outputs;
	}
	
	public ArrayList<String> getBiochemicalSpecies() {
		ArrayList<String> inputs = getInputSpecies();
		ArrayList<String> genetic = new ArrayList<String>();
		ArrayList<String> biochemical = new ArrayList<String>();
		for (String spec : getSpecies()) {
			for (int i=0; i<sbml.getModel().getReactionCount(); i++) {
				Reaction r = sbml.getModel().getReaction(i);
				if (r.getProductForSpecies(spec)!=null) {
					genetic.add(spec);
				}
			}
			if (!genetic.contains(spec) && !inputs.contains(spec)) {
				biochemical.add(spec);
			}
		}
		return biochemical;
	}

	public String getModelFileName(String id) {
		
		String componentModelRef = "";
		
		if (sbmlCompModel.getListOfSubmodels().get(id) != null) {
			
			componentModelRef = sbmlCompModel.getListOfSubmodels().get(id).getModelRef();
		}
		else {
			
			//search through the parameter location arrays to find the correct one
			for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
				
				Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter
				if (parameter.getId().contains("__locations")) {
					if (AnnotationUtility.parseArrayAnnotation(parameter,id)!=null) {
						componentModelRef = parameter.getId().replace("__locations","");
						if (componentModelRef.split("__").length > 1)
							componentModelRef = componentModelRef.split("__")[1];
						
						break;
					}		
				}				
			}
		}
		ExternalModelDefinition extModel = sbmlComp.getListOfExternalModelDefinitions().get(componentModelRef);
		if (extModel==null) {
			return "";
		}		
		return extModel.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
	}

	public HashMap<String, String> getInputConnections(BioModel compBioModel,String compId) {
		HashMap<String, String> inputs = new HashMap<String, String>();
		for (int i = 0; i < sbml.getModel().getSpeciesCount(); i++) {
			CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(sbml.getModel().getSpecies(i));
			for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
				ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
				if (replacement.getSubmodelRef().equals(compId) && 
						AnnotationUtility.checkObsoleteAnnotation(replacement, "Input")) {
					String IdRef = replacement.getIdRef();
					Port port = compBioModel.getPortByIdRef(IdRef);
					if (port==null) {
						sbmlSBase.getListOfReplacedElements().remove(j);
						continue;
					}
					if (SBMLutilities.isInputPort(port)) {
						replacement.unsetIdRef();
						replacement.setPortRef(port.getId());
						AnnotationUtility.removeObsoleteAnnotation(replacement);
						inputs.put(port.getId().replace(GlobalConstants.INPUT+"__",""),sbml.getModel().getSpecies(i).getId());
					}
				} else if (replacement.getSubmodelRef().equals(compId) && replacement.isSetPortRef()) {
					Port port = compBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
					if (port==null) {
						sbmlSBase.getListOfReplacedElements().remove(j);
						continue;
					}
					if (SBMLutilities.isInputPort(port)) {
						inputs.put(port.getId().replace(GlobalConstants.INPUT+"__",""),sbml.getModel().getSpecies(i).getId());
					}
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(compId) && (replacement.isSetPortRef())) {
					Port port = compBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
					if (port==null) {
						sbmlSBase.unsetReplacedBy();
						continue;
					}
					if (SBMLutilities.isInputPort(port)) {
						inputs.put(port.getId().replace(GlobalConstants.INPUT+"__",""),sbml.getModel().getSpecies(i).getId());
					}
				}
			}
		}
		return inputs;
	}

	public HashMap<String, String> getOutputConnections(BioModel compBioModel,String compId) {
		HashMap<String, String> outputs = new HashMap<String, String>();
		for (int i = 0; i < sbml.getModel().getSpeciesCount(); i++) {
			CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(sbml.getModel().getSpecies(i));
			for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
				ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
				if (replacement.getSubmodelRef().equals(compId) && 
						AnnotationUtility.checkObsoleteAnnotation(replacement, "Output")) {
					String IdRef = replacement.getIdRef();
					Port port = compBioModel.getPortByIdRef(IdRef);
					if (port==null) {
						sbmlSBase.getListOfReplacedElements().remove(j);
						continue;
					}
					if (SBMLutilities.isOutputPort(port)) {
						replacement.unsetIdRef();
						replacement.setPortRef(port.getId());
						AnnotationUtility.removeObsoleteAnnotation(replacement);
						outputs.put(port.getId().replace(GlobalConstants.OUTPUT+"__",""),sbml.getModel().getSpecies(i).getId());
					}
				} else if (replacement.getSubmodelRef().equals(compId) && replacement.isSetPortRef()) {
					Port port = compBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
					if (port==null) {
						sbmlSBase.getListOfReplacedElements().remove(j);
						continue;
					}
					if (SBMLutilities.isOutputPort(port)) {
						outputs.put(port.getId().replace(GlobalConstants.OUTPUT+"__",""),sbml.getModel().getSpecies(i).getId());
					}
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(compId) && (replacement.isSetPortRef())) {
					Port port = compBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
					if (port==null) {
						sbmlSBase.unsetReplacedBy();
						continue;
					}
					if (SBMLutilities.isOutputPort(port)) {
						outputs.put(port.getId().replace(GlobalConstants.OUTPUT+"__",""),sbml.getModel().getSpecies(i).getId());
					}
				}
			}
		}
		return outputs;
	}

	public HashMap<String, String> getVariableInputConnections(BioModel compBioModel,String compId) {
		HashMap<String, String> variables = new HashMap<String, String>();
		for (int i = 0; i < sbml.getModel().getParameterCount(); i++) {
			Parameter p = sbml.getModel().getParameter(i);
			if (!p.getConstant()) {
				CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(p);
				for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
					ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
					if (replacement.getSubmodelRef().equals(compId) && replacement.isSetPortRef()) {
						Port port = compBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
						if (port==null) {
							sbmlSBase.getListOfReplacedElements().remove(j);
							continue;
						}
						if (SBMLutilities.isInputPort(port)) {
							variables.put(port.getId().replace(GlobalConstants.INPUT+"__",""),
									sbml.getModel().getParameter(i).getId());
						}
					}
				}
				if (sbmlSBase.isSetReplacedBy()) {
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if (replacement.getSubmodelRef().equals(compId) && replacement.isSetPortRef()) {
						Port port = compBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
						if (port==null) {
							sbmlSBase.unsetReplacedBy();
							continue;
						}
						if (SBMLutilities.isInputPort(port)) {
							variables.put(port.getId().replace(GlobalConstants.INPUT+"__",""),
									sbml.getModel().getParameter(i).getId());
						}
					}
				}
			}
		}
		return variables;
	}
	
	public HashMap<String, String> getVariableOutputConnections(BioModel compBioModel,String compId) {
		HashMap<String, String> variables = new HashMap<String, String>();
		for (int i = 0; i < sbml.getModel().getParameterCount(); i++) {
			Parameter p = sbml.getModel().getParameter(i);
			if (!p.getConstant()) {
				CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(p);
				for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
					ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
					if (replacement.getSubmodelRef().equals(compId) && replacement.isSetPortRef()) {
						Port port = compBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
						if (port==null) {
							sbmlSBase.getListOfReplacedElements().remove(j);
							continue;
						}
						if (SBMLutilities.isOutputPort(port)) {
							variables.put(port.getId().replace(GlobalConstants.OUTPUT+"__",""),
									sbml.getModel().getParameter(i).getId());
						}
					}
				}
				if (sbmlSBase.isSetReplacedBy()) {
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if (replacement.getSubmodelRef().equals(compId) && replacement.isSetPortRef()) {
						Port port = compBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
						if (port==null) {
							sbmlSBase.unsetReplacedBy();
							continue;
						}
						if (SBMLutilities.isOutputPort(port)) {
							variables.put(port.getId().replace(GlobalConstants.OUTPUT+"__",""),
									sbml.getModel().getParameter(i).getId());
						}
					}
				}
			}
		}
		return variables;
	}	

/*	
	public HashMap<String, Properties> getComponents() {
		return components;
	}
	public HashMap<String, Properties> getCompartments() {
		return compartments;
	}
	*/
	
	/**
	 * returns the submodel's row from the location annotation
	 */
	public int getSubmodelRow(String submodelID) {
		//search through the parameter location arrays to find the correct one
		String locationAnnotationString = null;
		for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
			Parameter parameter = sbml.getModel().getParameter(i);
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {
				locationAnnotationString = AnnotationUtility.parseArrayAnnotation(parameter,submodelID);
				if (locationAnnotationString != null) {
					String rowCol = locationAnnotationString.split("=")[1];
					String row = rowCol.split(",")[0].replace("(","");
					return Integer.parseInt(row);
				}
			}				
		}
		return -1;
	}
	
	/**
	 * returns the submodel's col from the location annotation
	 */
	public int getSubmodelCol(String submodelID) {
		//search through the parameter location arrays to find the correct one
		String locationAnnotationString = null;
		for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
			Parameter parameter = sbml.getModel().getParameter(i);
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {
				locationAnnotationString = AnnotationUtility.parseArrayAnnotation(parameter,submodelID);
				if (locationAnnotationString != null) {
					String rowCol = locationAnnotationString.split("=")[1];
					String col = rowCol.split(",")[1].replace(")", "");
					return Integer.parseInt(col);
				}
			}				
		}
		return -1;
	}
	
	/**
	 * changes the row/col of a specified submodel
	 * 
	 * @param submodelID
	 * @param row
	 * @param col
	 */
	public void setSubmodelRowCol(String submodelID, int row, int col) {
		
		String componentModelRefID = "";
		
		//search through the parameter location arrays to find the correct one
		for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
			
			Parameter parameter = sbml.getModel().getParameter(i);
			
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {
				if (AnnotationUtility.parseArrayAnnotation(parameter, submodelID)!=null) {
					componentModelRefID = parameter.getId().replace("__locations","");
					break;
				}		
			}				
		}
		
		String locationParameterString = componentModelRefID + "__locations";
		Parameter locationParameter = sbml.getModel().getParameter(locationParameterString);
		if (locationParameter != null) {
			AnnotationUtility.removeArrayAnnotation(locationParameter, submodelID);
			AnnotationUtility.appendArrayAnnotation(locationParameter, submodelID + "=\"(" + row + "," + col + ")\"");
		}		
	}

	/**
	 * returns all the ports in the gcm file matching type, which must be either
	 * GlobalConstants.INPUT or GlobalConstants.OUTPUT.
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<String> getInputPorts(String type) {
		ArrayList<String> ports = new ArrayList<String>();
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
			Port port = sbmlCompModel.getListOfPorts().get(i);
			if (!SBMLutilities.isInputPort(port)) continue;
			if (port.isSetIdRef()) {
				String idRef = port.getIdRef();
				SBase sbase = SBMLutilities.getElementBySId(sbml, idRef);
				if (sbase!=null) {
					if (sbase.getElementName().equals(GlobalConstants.SBMLSPECIES)) {
						if (type.equals(GlobalConstants.SBMLSPECIES)) {
							ports.add(idRef);
						}
					} else if (sbase.getElementName().equals(GlobalConstants.PARAMETER)) {
						Parameter p = sbml.getModel().getParameter(idRef);
						if (type.equals(GlobalConstants.BOOLEAN)) {
							if (SBMLutilities.isBoolean(p)) {
								ports.add(idRef);
							}
						} else if (type.equals(GlobalConstants.VARIABLE)) {
							if (!SBMLutilities.isBoolean(p) && !SBMLutilities.isPlace(p)) {
								ports.add(idRef);
							}
						} else if (type.equals(GlobalConstants.PLACE)) {
							if (SBMLutilities.isPlace(p)) {
								ports.add(idRef);
							}
						}
					}
				}
			}
		}
		return ports;
	}
	
	public ArrayList<String> getOutputPorts(String type) {
		ArrayList<String> ports = new ArrayList<String>();
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
			Port port = sbmlCompModel.getListOfPorts().get(i);
			if (!SBMLutilities.isOutputPort(port)) continue;
			if (port.isSetIdRef()) {
				String idRef = port.getIdRef();
				SBase sbase = SBMLutilities.getElementBySId(sbml, idRef);
				if (sbase!=null) {
					if (sbase.getElementName().equals(GlobalConstants.SBMLSPECIES)) {
						if (type.equals(GlobalConstants.SBMLSPECIES)) {
							ports.add(idRef);
						}
					} else if (sbase.getElementName().equals(GlobalConstants.PARAMETER)) {
						Parameter p = sbml.getModel().getParameter(idRef);
						if (type.equals(GlobalConstants.BOOLEAN)) {
							if (SBMLutilities.isBoolean(p)) {
								ports.add(idRef);
							}
						} else if (type.equals(GlobalConstants.VARIABLE)) {
							if (!SBMLutilities.isBoolean(p) && !SBMLutilities.isPlace(p)) {
								ports.add(idRef);
							}
						} else if (type.equals(GlobalConstants.PLACE)) {
							if (SBMLutilities.isPlace(p)) {
								ports.add(idRef);
							}
						}
					}
				}
			}
		}
		return ports;
	}
	
	public void createDirPort(String SId,String dir) {
		Port port = getPortByIdRef(SId);
		//if (port!=null) return;
		SBase variable = SBMLutilities.getElementBySId(sbml,SId);
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(variable);
		if (dir.equals(GlobalConstants.INPUT)) {
			if (port==null) {
				port = sbmlCompModel.createPort();
			}
			port.setId(dir + "__" + SId);
			port.setIdRef(SId);
			port.setSBOTerm(GlobalConstants.SBO_INPUT_PORT);
			ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
			sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
			sBasePluginPort.unsetListOfIndices();
			for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
				org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
				Index portIndex = sBasePluginPort.createIndex();
				portIndex.setReferencedAttribute("comp:idRef");
				portIndex.setArrayDimension(i);
				portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
			}
		} else if (dir.equals(GlobalConstants.OUTPUT)) {
			if (port==null) {
				port = sbmlCompModel.createPort();
			}
			port.setId(dir + "__" + SId);
			port.setIdRef(SId);
			port.setSBOTerm(GlobalConstants.SBO_OUTPUT_PORT);
			ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
			sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
			sBasePluginPort.unsetListOfIndices();
			for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
				org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
				Index portIndex = sBasePluginPort.createIndex();
				portIndex.setReferencedAttribute("comp:idRef");
				portIndex.setArrayDimension(i);
				portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
			}
		} else if (port != null) {
			sbmlCompModel.removePort(port);
		}
	}

	public boolean isSpeciesConstitutive(String speciesId) {
		Reaction constitutive = sbml.getModel().getReaction("Constitutive_"+speciesId);
		if (constitutive != null) {
			if (BioModel.isConstitutiveReaction(constitutive)) return true;
		}
		return false;
	}
	
	public Reaction getDegradationReaction(String speciesId) {
		return getDegradationReaction(speciesId, sbml.getModel());
	}
	
	public static Reaction getDegradationReaction(String speciesId, Model sbmlModel) {
		String componentId = "";
		String shortSpeciesId = speciesId;
		if (speciesId.contains("__")) {
			componentId = speciesId.substring(0,speciesId.lastIndexOf("__")+2);
			shortSpeciesId = speciesId.substring(speciesId.lastIndexOf("__")+2);
		}
		Reaction degradation = sbmlModel.getReaction(componentId + GlobalConstants.DEGRADATION + "_" + shortSpeciesId);
		if (degradation == null) {
			for (int i = 0; i < sbmlModel.getReactionCount(); i++) {
				Reaction r = sbmlModel.getReaction(i);
				if (BioModel.isDegradationReaction(r) && r.hasReactant(new Species(speciesId)))
					return r;
			}
		} else if (BioModel.isDegradationReaction(degradation))
			return degradation;
		return null;
	}
	
	public Reaction getProductionReaction(String promoterId) {
		return getProductionReaction(promoterId, sbml.getModel());
	}
	
	public static Reaction getProductionReaction(String promoterId, Model sbmlModel) {
		String componentId = "";
		String shortPromoterId = promoterId;
		if (promoterId.contains("__")) {
			componentId = promoterId.substring(0, promoterId.lastIndexOf("__") + 2);
			shortPromoterId = promoterId.substring(promoterId.lastIndexOf("__") + 2);
		}
		Reaction production = sbmlModel.getReaction(componentId + GlobalConstants.PRODUCTION + "_" + shortPromoterId);
		if (production == null)
			for (int i = 0; i < sbmlModel.getReactionCount(); i++) {
				Reaction r = sbmlModel.getReaction(i);
				if (BioModel.isProductionReaction(r) && r.hasModifier(new Species(promoterId)))
					return r;
			}
		else if (BioModel.isProductionReaction(production))
			return production;
		return null;
	}	
	
	public Reaction getDiffusionReaction(String speciesId) {
		return getDiffusionReaction(speciesId, sbml.getModel());
	}
	
	public static Reaction getDiffusionReaction(String speciesId, Model sbmlModel) {
		Reaction diffusion = sbmlModel.getReaction("MembraneDiffusion_"+speciesId);
		if (diffusion == null) {
			diffusion = sbmlModel.getReaction("Diffusion_"+speciesId);
			if (diffusion != null) {
				diffusion.setId("MembraneDiffusion_"+speciesId);
			}
		}
		if (diffusion != null) {
			if (diffusion.isSetSBOTerm()) {
				if (diffusion.getSBOTerm()==GlobalConstants.SBO_DIFFUSION) return diffusion;
			} else if (AnnotationUtility.checkObsoleteAnnotation(diffusion,"Diffusion")) {
				diffusion.setSBOTerm(GlobalConstants.SBO_DIFFUSION);
				AnnotationUtility.removeObsoleteAnnotation(diffusion);
				return diffusion;
			}
		}
		return null;
	}
	
	public Reaction getConstitutiveReaction(String speciesId) {
		Reaction constitutive = sbml.getModel().getReaction("Constitutive_"+speciesId);
		if (constitutive != null) {
			if (BioModel.isConstitutiveReaction(constitutive)) return constitutive;
		}
		return null;
	}
	
	public Reaction getComplexReaction(String speciesId) {
		return getComplexReaction(speciesId, sbml.getModel());
	}
	
	public static Reaction getComplexReaction(String speciesId, Model sbmlModel) {
		String componentId = "";
		String shortSpeciesId = speciesId;
		if (speciesId.contains("__")) {
			componentId = speciesId.substring(0,speciesId.lastIndexOf("__")+2);
			shortSpeciesId = speciesId.substring(speciesId.lastIndexOf("__")+2);
		}
		Reaction complexation = sbmlModel.getReaction(componentId + GlobalConstants.COMPLEXATION 
				+ "_" + shortSpeciesId);
		if (complexation == null) {
			for (int i = 0; i < sbmlModel.getReactionCount(); i++) {
				Reaction r = sbmlModel.getReaction(i);
				if (BioModel.isComplexReaction(r) && r.hasProduct(new Species(speciesId)))
					return r;
			}
		} else if (BioModel.isComplexReaction(complexation))
			return complexation;
		return null;
	}
	
	public boolean isSpeciesComplex(String speciesId) {
		Reaction complex = getComplexReaction(speciesId);
		return (complex != null);
	}
	
	public void connectComponentAndSBase(String compId, String port, CompSBasePlugin sbmlSBase, boolean output) {
		boolean found = false;
		ReplacedElement replacement = null;
		for (int i = 0; i < sbmlSBase.getListOfReplacedElements().size(); i++) {
			replacement = sbmlSBase.getListOfReplacedElements().get(i);
			if (replacement.getSubmodelRef().equals(compId) && 
				replacement.getPortRef().equals(port)) {
				if (!output) found = true;
				else sbmlSBase.removeReplacedElement(replacement);
				break;
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			ReplacedBy replacedBy = sbmlSBase.getReplacedBy();
			if (replacedBy.getSubmodelRef().equals(compId) &&
					replacedBy.getPortRef().equals(port)) {
				if (output) found = true;
				else sbmlSBase.unsetReplacedBy();
			}
		}
		if (!found) {
			if (output) {
				ReplacedBy replacedBy = sbmlSBase.createReplacedBy();
				replacedBy.setSubmodelRef(compId);
				replacedBy.setPortRef(port);
			} else {
				replacement = sbmlSBase.createReplacedElement();
				replacement.setSubmodelRef(compId);
				replacement.setPortRef(port);
			}
		}
	}
	
	public void connectComponentAndSpecies(String compId, String port, String specId, boolean output) {
		CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(sbml.getModel().getSpecies(specId));
		connectComponentAndSBase(compId,port,sbmlSBase,output);
	}

	public void connectComponentAndVariable(String compId, String port, String varId, boolean output) {
		CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(sbml.getModel().getParameter(varId));
		connectComponentAndSBase(compId,port,sbmlSBase,output);
	}
	
	/**
	 * Given a component and the name of a species, return true if that species
	 * is connected to that component. Optionally disconnect them as well.
	 */
	/*
	public boolean checkDisconnectComponentAndSpecies(Properties comp, String speciesId, boolean disconnect) {
		// now figure out which port the species is connected to
		for (Object p : comp.keySet()) {
			String key = p.toString();
			String value = (String) comp.get(key);
			if (value.equals(speciesId) && comp.containsKey("type_" + key)) {
				if (disconnect) {
					comp.remove(key);
					comp.remove("type_" + key);
				}
				return true;
			}
		}
		return false;
	}
	*/

	public String getComponentPortMap(String s) {
		String portmap = "(";
		boolean first = true;
		for (int i = 0; i < sbml.getModel().getSpeciesCount(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(species);
			for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
				ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
				if (replacement.getSubmodelRef().equals(s)) {
					if (!first) portmap += ", ";
					portmap += replacement.getPortRef().replace(GlobalConstants.INPUT+"__","").replace(GlobalConstants.OUTPUT+"__","") 
							+ "->" + species.getId();
					first = false;
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(s)) {
					if (!first) portmap += ", ";
					portmap += replacement.getPortRef().replace(GlobalConstants.INPUT+"__","").replace(GlobalConstants.OUTPUT+"__","") 
							+ "<-" + species.getId();
				}
			}
		}
		portmap += ")";
		return portmap;
	}
	
	/*
	public HashMap<String, Properties> getInfluences() {
		return influences;
	}
	*/
	
	/**
	 * Checks to see if removing influence is okay
	 * 
	 * @param name
	 *            influence to remove
	 * @return true, it is always okay to remove influence
	 */
	public static boolean removeInfluenceCheck(String name) {
		return true;
	}

	/**
	 * looks everywhere to see if the given species is connected to anything.
	 * Returns true if it is. Also will delete the connections if remove
	 * parameter is true. NOTE: This function does not check outside GCM files.
	 * Use speciesUsedInOtherGCM() for that.
	 * 
	 * @param remove
	 * @return
	 */
	/*
	private boolean checkRemoveSpeciesAssociations(String name, boolean remove) {
		boolean ret = false;

		boolean changed;
		do {
			changed = false;
			for (String s : influences.keySet()) {
				if (getInput(s).equals(name) || getOutput(s).equals(name)) {
					ret = true;
					if (remove) {
						influences.remove(s);
						// start over because the keyset changed and the forloop
						// could be broken.
						changed = true;
						break;
					}
				}
			}
		}
		while (changed == true);
//		for (String c : getComponents().keySet()) {
//			if (checkDisconnectComponentAndSpecies(getComponents().get(c), name, remove))
//				ret = true;
//		}
		
		return ret;
	}
	*/

	/*
	public boolean speciesUsedInOtherGCM(String name) {
		if (species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.INPUT)
				|| species.get(name).getProperty(GlobalConstants.TYPE).equals(
						GlobalConstants.OUTPUT)) {
			for (String s : new File(path).list()) {
				if (s.endsWith(".gcm")) {
					GCMFile g = new GCMFile(path);
					g.load(path + separator + s);
					for (String comp : g.getComponents().keySet()) {
						String compGCM = g.getComponents().get(comp).getProperty("gcm");
						if (filename.endsWith(compGCM)
								&& g.getComponents().get(comp).containsKey(name)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
    */
	
	/**
	 * Checks to see if removing specie is okay
	 * 
	 * @param name
	 *            specie to remove
	 * @return true if specie is in no influences
	 */
	/*
	public boolean removeSpeciesCheck(String name) {
		return !checkRemoveSpeciesAssociations(name, false);
	}*/

	/*
	public boolean editSpeciesCheck(String name, String newType) {
		if ((species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.INPUT) && 
				newType.contains(GlobalConstants.INPUT)) || 
			(species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT) &&
				newType.contains(GlobalConstants.OUTPUT))) {
			return true;
		}
		else if (species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.INPUT)
				|| species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT)) {
			for (String s : new File(path).list()) {
				if (s.endsWith(".gcm")) {
					GCMFile g = new GCMFile(path);
					g.load(path + separator + s);
					for (String comp : g.getComponents().keySet()) {
						String compGCM = g.getComponents().get(comp).getProperty("gcm");
						if (filename.endsWith(compGCM)
								&& g.getComponents().get(comp).containsKey(name)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
    */

	public void removePromoter(String id) {
		ListOf<InitialAssignment> r = sbml.getModel().getListOfInitialAssignments();
		for (int i = 0; i < sbml.getModel().getInitialAssignmentCount(); i++) {
			if (r.get(i).getVariable().equals(id)) {
				r.remove(i);
			}
		}
		sbml.getModel().removeSpecies(id);
		if (sbml.getModel().getSpecies(id + "_mRNA")!=null) {
			sbml.getModel().removeSpecies(id+"_mRNA");
		}
		removeReaction(getProductionReaction(id).getId());
		Layout layout = getLayout();
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			layout.getListOfSpeciesGlyphs().remove(GlobalConstants.GLYPH+"__"+id);
		}
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id) != null) {
			layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+id);
		}
		if (speciesPanel!=null)
			speciesPanel.refreshSpeciesPanel(this);
		if (reactionPanel!=null) {
			reactionPanel.refreshReactionPanel(this);
		}
		if (compartmentPanel!=null) {
			compartmentPanel.refreshCompartmentPanel(this);
		}
	}

	/**
	 * removes a component from the model
	 * 
	 * @param name
	 */
	public void removeComponent(String name) {
		
		String componentModelRef = "";
		
		if (sbmlCompModel.getListOfSubmodels().get(name) != null) {
			
			componentModelRef = sbmlCompModel.getListOfSubmodels().get(name).getModelRef();
		}
		else {
			
			//look through the location parameter arrays to find the correct model ref
			for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
				
				Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter
				if (parameter.getId().contains("__locations")) {
					
					if (AnnotationUtility.parseArrayAnnotation(parameter,name)!=null) {
						
						componentModelRef = parameter.getId().replace("__locations","");						
						break;
					}					
				}				
			}
		}
		
		String locationParameterString = componentModelRef + "__locations";
		Parameter locationParameter = sbml.getModel().getParameter(locationParameterString);
		if (locationParameter != null) {
			if (AnnotationUtility.removeArrayAnnotation(locationParameter, name)) {
				sbml.getModel().removeParameter(locationParameterString);
			}
		}
		
		//if a gridded/arrayed submodel exists, it'll have this ID	
		String gridSubmodelID = "GRID__" + componentModelRef;
		
		Submodel potentialGridSubmodel = sbmlCompModel.getListOfSubmodels().get(gridSubmodelID);
		
		if (potentialGridSubmodel == null)
			gridSubmodelID = componentModelRef;
		
		potentialGridSubmodel = sbmlCompModel.getListOfSubmodels().get(gridSubmodelID);
		int size = AnnotationUtility.parseArraySizeAnnotation(potentialGridSubmodel);
		
		if (potentialGridSubmodel != null && size >= 0) {
			
			//if the annotation string already exists, then one of these existed before
			//so update its count
			//int size = AnnotationUtility.parseArraySizeAnnotation(potentialGridSubmodel);
			if (size >= 0) {
				
				//if we're getting rid of the last submodel of its kind
				//then delete its grid species (if they exist) and the GRID__ submodel
				if (size == 1) {
					
					//find the right submodel index to delete it
					for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
						
						if (sbmlCompModel.getListOfSubmodels().get(i).getId().equals(gridSubmodelID))
							sbmlCompModel.removeSubmodel(i);
					}
					
					//remove the grid species this submodel had and its locations parameter
					sbml.getModel().removeParameter(locationParameterString);
					
					if (grid.getNumCols() > 0 && grid.getNumRows() > 0)
						removeGridSpecies(componentModelRef);
					
					sbmlComp.removeExternalModelDefinition(componentModelRef);
				}
				else {					
					AnnotationUtility.setArraySizeAnnotation(potentialGridSubmodel, --size);
				}
			}
		}
		else {
			
			int count = 0;
			for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); ++i) {
				if (sbmlCompModel.getListOfSubmodels().get(i).getId().equals(name)) {
					sbmlCompModel.removeSubmodel(i);
				} else if (sbmlCompModel.getListOfSubmodels().get(i).getModelRef().equals(componentModelRef)) {
					count++;
				}
			}
			if (count==0) {
				sbmlComp.removeExternalModelDefinition(componentModelRef);
			}
		}
		
		ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(sbml.getModel());
		for (int i = 0; i < elements.size(); i++) {
			SBase sbase = elements.get(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);
			if (sbmlSBase!=null) {
				for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
					ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
					if (replacement.getSubmodelRef().equals(name)) {
						sbmlSBase.removeReplacedElement(j);
						elements = SBMLutilities.getListOfAllElements(sbml.getModel());
						i--;
					}
				}
				if (sbmlSBase.isSetReplacedBy()) {
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if (replacement.getSubmodelRef().equals(name)) {
						sbmlSBase.unsetReplacedBy();
						elements = SBMLutilities.getListOfAllElements(sbml.getModel());
						i--;
					}
				}
			}
		}
		Layout layout = getLayout();
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+name)!=null) {
			layout.removeGeneralGlyph(GlobalConstants.GLYPH+"__"+name);
		}
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+name) != null) {
			layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+name);
		}
	}
	
	public void removeComponentConnection(String variableId,String componentId,String portId) {
		SBase variable = SBMLutilities.getElementBySId(sbml.getModel(), variableId);
		if (variable!=null) {
			CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(variable);
			for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
				ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
				if (replacement.getSubmodelRef().equals(componentId) && replacement.getPortRef().equals(portId)) {
					sbmlSBase.removeReplacedElement(j);
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				ReplacedBy replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(componentId) && replacement.getPortRef().equals(portId)) {
					sbmlSBase.unsetReplacedBy();
				}				
			}
		}
	}		

	public void removeInfluence(String name) {
		if (name.contains("+")) {
			Reaction reaction = getComplexReaction(name.substring(name.indexOf(">")+1));
			reaction.removeReactant(name.substring(0,name.indexOf("+")));
			if (reaction.getReactantCount()==0) {
				sbml.getModel().removeReaction(reaction.getId());
			} else {
				reaction.getKineticLaw().setMath(SBMLutilities.myParseFormula(createComplexKineticLaw(reaction)));
			}
		} else if (name.contains(",")) {
			Reaction reaction = getProductionReaction(name.substring(name.indexOf(",")+1));
			if (reaction!=null) {
				ModifierSpeciesReference modifier;
				if (name.contains("x>")) {
					modifier = reaction.getModifierForSpecies(name.substring(0,name.indexOf(">")-1));
				} else {
					modifier = reaction.getModifierForSpecies(name.substring(0,name.indexOf("-")));
				}
				if (BioModel.isRegulator(modifier)) {
					if (name.contains("|")) {
						modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
					} else {
						modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
					}
				} else {
					if (name.contains("x>")) {
						SBMLutilities.removeModifier(reaction, name.substring(0,name.indexOf(">")-1));
					} else {
						SBMLutilities.removeModifier(reaction, name.substring(0,name.indexOf("-")));
					}
				}
				if (reaction.getModifierCount()==1) {
					sbml.getModel().removeSpecies(name.substring(name.indexOf(",")+1));
					sbml.getModel().removeReaction(reaction.getId());
				} else {
					reaction.getKineticLaw().setMath(SBMLutilities.myParseFormula(createProductionKineticLaw(reaction)));
				}
			}
		} else if (name.contains("|")) {
			Reaction reaction = getProductionReaction(name.substring(name.indexOf("|")+1));
			ModifierSpeciesReference modifier = reaction.getModifierForSpecies(name.substring(0,name.indexOf("-")));
			if (BioModel.isRegulator(modifier)) {
				modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
			} else {
				if (name.contains("x>")) {
					SBMLutilities.removeModifier(reaction, name.substring(0,name.indexOf(">")-1));
				} else {
					SBMLutilities.removeModifier(reaction, name.substring(0,name.indexOf("-")));
				}
			}
			createProductionKineticLaw(reaction);
		} else if (name.contains(">")) {
			Reaction reaction = getProductionReaction(name.substring(name.indexOf(">")+1));
			if (reaction!=null) {
				ModifierSpeciesReference modifier = null;
				if (name.contains("x>")) {
					modifier = reaction.getModifierForSpecies(name.substring(0,name.indexOf(">")-1));
				} else {
					modifier = reaction.getModifierForSpecies(name.substring(0,name.indexOf("-")));
				}
				if (BioModel.isRegulator(modifier)) {
					modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
				} else {
					if (name.contains("x>")) {
						SBMLutilities.removeModifier(reaction, name.substring(0,name.indexOf(">")-1));
					} else {
						SBMLutilities.removeModifier(reaction, name.substring(0,name.indexOf("-")));
					}
				}
				createProductionKineticLaw(reaction);
			} else {
				String promoterId = name.substring(0,name.indexOf("-"));
				reaction = getProductionReaction(promoterId);
				reaction.removeProduct(name.substring(name.indexOf(">")+1));
				if (reaction.getProductCount()==0) {
					Species mRNA = sbml.getModel().createSpecies();
					mRNA.setId(promoterId+"_mRNA");
					mRNA.setCompartment(reaction.getCompartment());
					mRNA.setSBOTerm(GlobalConstants.SBO_MRNA);
					mRNA.setInitialAmount(0.0);
					mRNA.setBoundaryCondition(false);
					mRNA.setConstant(false);
					mRNA.setHasOnlySubstanceUnits(true);
					SpeciesReference product = reaction.createProduct();
					product.setSpecies(mRNA.getId());
					product.setStoichiometry(1.0);
					product.setConstant(true);
				}
			}
		}
	}
	
	public void updateSpeciesSize(Species species) {
		int width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
		int height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
		if (species.isSetSBOTerm()) {
			if (species.getSBOTermID().equals(GlobalConstants.SBO_DNA) ||
					species.getSBOTermID().equals(GlobalConstants.SBO_DNA_SEGMENT)) {
				width = GlobalConstants.DEFAULT_DNA_WIDTH;
				height = GlobalConstants.DEFAULT_DNA_HEIGHT;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_RNA) ||
					species.getSBOTermID().equals(GlobalConstants.SBO_RNA_SEGMENT)) {
				width = GlobalConstants.DEFAULT_RNA_WIDTH;
				height = GlobalConstants.DEFAULT_RNA_HEIGHT;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_PROTEIN)) {
				width = GlobalConstants.DEFAULT_PROTEIN_WIDTH;
				height = GlobalConstants.DEFAULT_PROTEIN_HEIGHT;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_NONCOVALENT_COMPLEX) ||
					SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_NONCOVALENT_COMPLEX)) {
				width = GlobalConstants.DEFAULT_COMPLEX_WIDTH;
				height = GlobalConstants.DEFAULT_COMPLEX_HEIGHT;
			} else if (species.getSBOTermID().equals(GlobalConstants.SBO_SIMPLE_CHEMICAL) ||
					SBMLutilities.sbo.isDescendantOf(species.getSBOTermID(), GlobalConstants.SBO_SIMPLE_CHEMICAL)) {
				width = GlobalConstants.DEFAULT_SMALL_MOLECULE_WIDTH;
				height = GlobalConstants.DEFAULT_SMALL_MOLECULE_HEIGHT;
			} else {
				width = GlobalConstants.DEFAULT_SPECIES_WIDTH;
				height = GlobalConstants.DEFAULT_SPECIES_HEIGHT;
			}
		} 
		Layout layout = getLayout();
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+species.getId())!=null) {
			SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+species.getId());
			speciesGlyph.getBoundingBox().getDimensions().setWidth(width);
			speciesGlyph.getBoundingBox().getDimensions().setHeight(height);
		}
	}

	/**
	 * creates/removes grid species if a file is updated
	 * 
	 * @param filename
	 */
	public void updateGridSpecies(String componentModelRef) {
		
		componentModelRef = componentModelRef.replace(".xml", "");
		
		//loop through all components of this model ref
		if (sbml.getModel().getParameter(componentModelRef + "__locations") != null) {
				
			removeGridSpecies(componentModelRef);
			createGridSpecies("GRID__" + componentModelRef);
		}
	}
	
	/**
	 * removes the diffusible grid species from the model (but checks to make sure other submodels
	 * don't have them first)
	 * 
	 * @param componentID
	 */
	public void removeGridSpecies(String componentModelRef) {
		String externalModelID = 
			this.getSBMLComp().getListOfExternalModelDefinitions().get(componentModelRef).getSource().replace("file://","").replace("file:","");
		
		SBMLDocument document = null;
		
		String path = this.getPath();
		
		if (this.getPath().charAt(this.getPath().length() - 1) != '/')
			path += '/';
		
		//load the sbml file
		try {
			document = SBMLReader.read(new File(path + externalModelID));
		} catch (XMLStreamException e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Model componentModel = document.getModel();
		
		ArrayList<String> speciesToRemove = new ArrayList<String>();
		
		//check all species in the component for diffusibility
		//if they're diffusible, they're candidates for being removed from the model
		//also, if they're not diffusible and on the grid species list, they're candidates for removal
		for (int speciesIndex = 0; speciesIndex < componentModel.getSpeciesCount(); ++speciesIndex) {
			
			String speciesID = componentModel.getListOfSpecies().get(speciesIndex).getId();			
			Reaction diffusionReaction = componentModel.getReaction("MembraneDiffusion_" + speciesID);
			
			if (diffusionReaction != null && BioModel.isDiffusionReaction(diffusionReaction)
					|| sbml.getModel().getSpecies(speciesID) != null) {
				
				speciesToRemove.add(speciesID);
				
				//see if the species is degradable.  if this status changes, then the degradation reactions
				//need to be updated.
				Reaction degradationReaction = getDegradationReaction(speciesID, componentModel);
				
				if (degradationReaction == null) {
					sbml.getModel().removeReaction(GlobalConstants.DEGRADATION + "_" + speciesID);
				}
			}
		}
		
		//check all other submodels to make sure the species really should be removed
		//(ie, they don't exist anywhere else)
		for (int submodelIndex = 0; submodelIndex < sbmlCompModel.getListOfSubmodels().size(); ++submodelIndex) {
			
			componentModelRef = sbmlCompModel.getListOfSubmodels().get(submodelIndex).getModelRef();
			
			//find the sbml file for the component
			externalModelID = 
				this.getSBMLComp().getListOfExternalModelDefinitions().get(componentModelRef).getSource().replace("file://","").replace("file:","");
			
			//load the sbml file
			try {
				document = SBMLReader.read(new File(path + externalModelID));
			} catch (XMLStreamException e1) {
				JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
				return;
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
				return;
			}
			componentModel = document.getModel();
			
			//check all species in the component for diffusibility
			//if they're diffusible and they're in the removal list, they shouldn't be removed as grid species
			for (int speciesIndex = 0; speciesIndex < componentModel.getSpeciesCount(); ++speciesIndex) {
				
				String speciesID = componentModel.getListOfSpecies().get(speciesIndex).getId();			
				Reaction diffusionReaction = componentModel.getReaction("MembraneDiffusion_" + speciesID);
				
				//if this is true, then this species shouldn't be removed because it exists elsewhere
				if (diffusionReaction != null && BioModel.isDiffusionReaction(diffusionReaction) &&
						speciesToRemove.contains(speciesID)) {
					
					speciesToRemove.remove(speciesID);
				}
			}			
		}
		
		//remove the grid species from the model
		for (String specToRemove : speciesToRemove) {
			sbml.getModel().removeSpecies(specToRemove);
		}
		
		if (speciesToRemove.size() > 0)
			removeGridSpeciesReactions(speciesToRemove);
	}
	
	/**
	 * searches a component for diffusible species and adds them to the grid level
	 * 
	 * @param componentID
	 */
	public void createGridSpecies(String submodelID) {
		
		String componentModelRef = sbmlCompModel.getListOfSubmodels().get(submodelID).getModelRef();
		
		//find the sbml file for the component
		String externalModelID = 
			this.getSBMLComp().getListOfExternalModelDefinitions().get(componentModelRef).getSource().replace("file://","").replace("file:","");
		
		SBMLDocument document = null;
		
		if (this.getPath().charAt(this.getPath().length() - 1) != '/')
			this.path += '/';
		
		//load the sbml file
		try {
			document = SBMLReader.read(new File(this.getPath() + externalModelID));
		} catch (XMLStreamException e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Model componentModel = document.getModel();
		
		ArrayList<Species> speciesToAdd = new ArrayList<Species>();
		
		//check all species in the component for diffusibility
		//if they're diffusible, they're candidates for being added as a grid species
		for (int speciesIndex = 0; speciesIndex < componentModel.getSpeciesCount(); ++speciesIndex) {
			
			String speciesID = componentModel.getListOfSpecies().get(speciesIndex).getId();			
			Reaction diffusionReaction = componentModel.getReaction("MembraneDiffusion_" + speciesID);
			
			if (diffusionReaction != null && BioModel.isDiffusionReaction(diffusionReaction)) {
				
				speciesToAdd.add(componentModel.getListOfSpecies().get(speciesIndex));
				
				//if it's degradable and there's no degradation reaction, add one
				if (getDegradationReaction(speciesID) == null /*&&
						getDegradationReaction(speciesID, componentModel) != null*/) {
					createGridDegradationReaction(componentModel.getListOfSpecies().get(speciesIndex));
				}				
			}
		}
		
		//add diffusible species as grid species if they don't already exist
		for (Species specToAdd : speciesToAdd) {
			
			if (sbml.getModel().getSpecies(specToAdd.getId()) == null) {
				Species newSpecies = this.getSBMLDocument().getModel().createSpecies();
				newSpecies.setId(specToAdd.getId());
				AnnotationUtility.setGridAnnotation(newSpecies, grid.getNumRows(), grid.getNumCols());
				newSpecies.setInitialAmount(0.0);
				newSpecies.setBoundaryCondition(specToAdd.getBoundaryCondition());
				newSpecies.setConstant(specToAdd.getConstant());
				newSpecies.setHasOnlySubstanceUnits(specToAdd.getHasOnlySubstanceUnits());
				newSpecies.setCompartment(getDefaultCompartment());
			}
		}
		
		//if new grid species were added, create diffusion/degradation reactions for them
		if (speciesToAdd.size() > 0)
			createGridSpeciesReactions(speciesToAdd, componentModel);
	}
	
	public void createGridDegradationReaction(Species species) {
		
		String speciesID = species.getId();
		Boolean speciesDegrades = false;			
		//Reaction degradation = BioModel.getDegradationReaction(speciesID, compModel);
		
		//fix the sbo term/annotation stuff if it's not correct
		//if (degradation != null)
			speciesDegrades = true;
		
		//only make grid degradation reactions if the species is degradable
		if (speciesDegrades) {
		
			//create array of grid degradation reactions
			String decayString = GlobalConstants.KECDECAY_STRING;
			double decayRate = sbml.getModel().getParameter("kecd").getValue();
				
			String decayUnitString = "u_1_second_n1";
			
			if (sbml.getModel().getUnitDefinition(decayUnitString) == null) {
				
				UnitDefinition ud = sbml.getModel().createUnitDefinition();
				Unit unit = ud.createUnit();
				unit.setExponent(-1.0);
				unit.setKind(Unit.Kind.valueOf("second".toUpperCase()));
				unit.setScale(0);
				unit.setMultiplier(1);
				ud.setId(decayUnitString);
			}
			
			Reaction r = sbml.getModel().createReaction();
			r.setId(GlobalConstants.DEGRADATION + "_" + speciesID);
			SBMLutilities.setMetaId(r, r.getId());
			r.setCompartment(sbml.getModel().getCompartment(0).getId());
			r.setReversible(false);
			r.setFast(false);
			r.setSBOTerm(GlobalConstants.SBO_DEGRADATION);
			
			AnnotationUtility.setGridAnnotation(r, grid.getNumRows(), grid.getNumCols());
			
			KineticLaw kl = r.createKineticLaw();
			
			if (decayRate > 0) {
				
				//this is the mathematical expression for the decay
				String decayExpression = decayString + "* get2DArrayElement(" + speciesID + ", i, j)";				
				
				SpeciesReference reactant = r.createReactant();
				reactant.setSpecies(speciesID);
				reactant.setStoichiometry(1);
				reactant.setConstant(false);
				
				LocalParameter i = kl.createLocalParameter();
				LocalParameter j = kl.createLocalParameter();
				
				AnnotationUtility.setArraySizeAnnotation(i, this.getGrid().getNumRows());
				AnnotationUtility.setArraySizeAnnotation(j, this.getGrid().getNumCols());

				i.setId("i");
				j.setId("j");
				
				//parameter: id="kecd" value=(usually 0.005) units="u_1_second_n1" (inverse seconds)
				kl.addLocalParameter(Utility.Parameter(decayString, decayRate, decayUnitString));
				
				//formula: kecd * species
				kl.setMath(SBMLutilities.myParseFormula(decayExpression));
				
				Utility.addReaction(sbml, r);
			}
		}		
	}
	
	/**
	 * create grid species reactions for the new grid species
	 * 
	 * @param newGridSpecies
	 */
	public void createGridSpeciesReactions(ArrayList<Species> newGridSpecies, Model compModel) {
		
		//create functions for getting an array element
		SBMLutilities.createFunction(
				sbml.getModel(), "get2DArrayElement", "get2DArrayElement", "lambda(a,b,c,a)");		
		
		for (Species newSpecies : newGridSpecies) {
			
			if (getDegradationReaction(newSpecies.getId()) == null &&
					getDegradationReaction(newSpecies.getId(), compModel) != null) {
				createGridDegradationReaction(newSpecies);
			}
			if (sbml.getModel().getReaction("Diffusion_"+newSpecies.getId()+"_Above")!=null) continue;
			
			String speciesID = newSpecies.getId();
			
			//create array of grid diffusion reactions
			//NOTE: does not do diffusion with component species
			//loop though each of the four directions and add a diffusion reaction
			//implicitly, these will be arrays of reactions
			
			String diffusionUnitString = "u_1_second_n1";
			
			if (sbml.getModel().getUnitDefinition(diffusionUnitString) == null) {
				
				UnitDefinition ud = sbml.getModel().createUnitDefinition();
				Unit unit = ud.createUnit();
				unit.setExponent(-1.0);
				unit.setKind(Unit.Kind.valueOf("second".toUpperCase()));
				unit.setScale(0);
				unit.setMultiplier(1);
				ud.setId(diffusionUnitString);
			}
			
			String diffusionString = GlobalConstants.KECDIFF_STRING;
			String diffComp = sbml.getModel().getCompartment(0).getId();
			double kecdiff = sbml.getModel().getParameter("kecdiff").getValue();
			
			for (int index = 0; index < 4; ++index) {
				
				String direction = "";
				String neighborRowIndexOffset = "0";
				String neighborColIndexOffset = "0";
				
				switch (index) {
				
					case 0: {direction = "Above"; neighborRowIndexOffset = "- 1"; neighborColIndexOffset = "+ 0"; break;}						
					case 1: {direction = "Below"; neighborRowIndexOffset = "+ 1"; neighborColIndexOffset = "+ 0"; break;}						
					case 2: {direction = "Left"; neighborRowIndexOffset = "+ 0"; neighborColIndexOffset = "- 1"; break;}						
					case 3: {direction = "Right"; neighborRowIndexOffset = "+ 0"; neighborColIndexOffset = "+ 1"; break;}			
				}
				
				//reversible between neighboring "outer" species
				//this is the diffusion across the "medium" if you will
				Reaction r = sbml.getModel().createReaction();
				r.setId("Diffusion_" + speciesID + "_" + direction);
				SBMLutilities.setMetaId(r, r.getId());
				r.setCompartment(diffComp);
				r.setReversible(true);
				r.setFast(false);

				AnnotationUtility.setGridAnnotation(r, grid.getNumRows(), grid.getNumCols());
				KineticLaw kl = r.createKineticLaw();
				
				if (kecdiff > 0) {
				
					//this is the rate times the current species minus the rate times the neighbor species
					String diffusionExpression = 
						diffusionString + " * " + "get2DArrayElement(" + speciesID + ", i, j)" + "-"
						+ diffusionString + " * " + "get2DArrayElement(" + speciesID + ", i " 
						+ String.valueOf(neighborRowIndexOffset) + ", j " + String.valueOf(neighborColIndexOffset) + ")";

					//reactant is current outer species					
					SpeciesReference reactant = r.createReactant();
					reactant.setSpecies(speciesID);
					reactant.setStoichiometry(1);
					reactant.setConstant(false);
					
					//product is neighboring species
					SpeciesReference product = r.createProduct();
					product.setSpecies(speciesID);
					product.setStoichiometry(1);
					product.setConstant(false);
					
					LocalParameter i = kl.createLocalParameter();
					LocalParameter j = kl.createLocalParameter();
					
					AnnotationUtility.setArraySizeAnnotation(i, this.getGrid().getNumRows());
					AnnotationUtility.setArraySizeAnnotation(j, this.getGrid().getNumCols());
					
					i.setId("i");
					j.setId("j");
					
					LocalParameter kecdiffParam = kl.createLocalParameter();
					kecdiffParam.setId(diffusionString);
					kecdiffParam.setValue(kecdiff);
					kecdiffParam.setUnits(diffusionUnitString);
					kl.setMath(SBMLutilities.myParseFormula(diffusionExpression));
					
					Utility.addReaction(sbml, r);
				}
			}		
			
			//create array of membrane diffusion reactions
			
			String membraneDiffusionComp = sbml.getModel().getCompartment(0).getId();
			
			Reaction r = sbml.getModel().createReaction();
			r.setId("MembraneDiffusion_" + speciesID);
			SBMLutilities.setMetaId(r, r.getId());
			r.setCompartment(membraneDiffusionComp);
			r.setReversible(true);
			r.setFast(false);
			
			AnnotationUtility.setGridAnnotation(r, grid.getNumRows(), grid.getNumCols());
			
			KineticLaw kl = r.createKineticLaw();
			
			//this is the rate times the inner species minus the rate times the outer species
			String membraneDiffusionExpression = "get2DArrayElement(kmdiff_f, i, j) * get2DArrayElement(" 
				+ speciesID + ", i, j" + ") - get2DArrayElement(kmdiff_r, i, j) * get2DArrayElement(" 
				+ speciesID + ", i, j" + ")";

			//reactant is inner/submodel species					
			SpeciesReference reactant = r.createReactant();
			reactant.setSpecies(speciesID);
			reactant.setStoichiometry(1);
			reactant.setConstant(false);
			
			//product is outer species
			SpeciesReference product = r.createProduct();
			product.setSpecies(speciesID);
			product.setStoichiometry(1);
			product.setConstant(false);
			
			LocalParameter i = kl.createLocalParameter();
			LocalParameter j = kl.createLocalParameter();

			AnnotationUtility.setArraySizeAnnotation(i, this.getGrid().getNumRows());
			AnnotationUtility.setArraySizeAnnotation(j, this.getGrid().getNumCols());
			
			i.setId("i");
			j.setId("j");
			
			kl.setMath(SBMLutilities.myParseFormula(membraneDiffusionExpression));
			Utility.addReaction(sbml, r);			
		}
	}
	
	/**
	 * removes grid species reactions for the old species that no longer exist
	 * @param oldSpecies
	 */
	public void removeGridSpeciesReactions(ArrayList<String> oldGridSpecies) {
		
		for (String oldSpeciesID : oldGridSpecies) {
			
			//remove degredation reaction
			String reactionID = GlobalConstants.DEGRADATION + "_" + oldSpeciesID;
			sbml.getModel().removeReaction(reactionID);
			
			//remove membrane diffusion reaction
			reactionID = "MembraneDiffusion_" + oldSpeciesID;
			sbml.getModel().removeReaction(reactionID);
			
			//remove diffusion reactions
			for (int i = 0; i < 4; ++i) {
				
				String direction = "";
				
				switch (i) {
				
					case 0: {direction = "Above"; break;}						
					case 1: {direction = "Below"; break;}						
					case 2: {direction = "Left"; break;}						
					case 3: {direction = "Right"; break;}			
				}
				
				sbml.getModel().removeReaction("Diffusion_" + oldSpeciesID + "_" + direction);				
			}
		}
	}
	
	public boolean checkCompartmentOverlap(String id,double x, double y, double w, double h) {
		for (int i = 0; i < sbml.getModel().getCompartmentCount(); i++) {
			Compartment c = sbml.getModel().getCompartment(i);
			if (c.getId().equals(id)) continue;
			Layout layout = getLayout();
			CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+c.getId());
			double cx = compartmentGlyph.getBoundingBox().getPosition().getX();
			double cy = compartmentGlyph.getBoundingBox().getPosition().getY();
			double cw = compartmentGlyph.getBoundingBox().getDimensions().getWidth();
			double ch = compartmentGlyph.getBoundingBox().getDimensions().getHeight();
			if (x >= cx && y >= cy && x+w <= cx+cw && y+h <= cy+ch) continue;
			if (x <= cx && y <= cy && x+w >= cx+cw && y+h >= cy+ch) continue;
			if (x+w <= cx) continue;
			if (x >= cx+cw) continue;
			if (y+h <= cy) continue;
			if (y >= cy+ch) continue;
			return false;
		}
		return true;
	}
	
	public boolean checkCompartmentLocation(String id,double cx, double cy, double cw, double ch) {
		Layout layout = getLayout();
		for (int i = 0; i < sbml.getModel().getSpeciesCount(); i++) {
			Species s = sbml.getModel().getSpecies(i);
			if (s.getCompartment().equals(id)) {
				SpeciesGlyph speciesGlyph = null;
				if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s.getId())!=null) {
					speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s.getId());
					double x = speciesGlyph.getBoundingBox().getPosition().getX();
					double y = speciesGlyph.getBoundingBox().getPosition().getY();
					double w = speciesGlyph.getBoundingBox().getDimensions().getWidth();
					double h = speciesGlyph.getBoundingBox().getDimensions().getHeight();
					if (x < cx) {
						return false;
					}
					if (y < cy) {
						return false;
					}
					if (x + w > cx + cw) {
						return false;
					}
					if (y + h > cy + ch) {
						return false;
					}
				}
			}
		}
		for (int i = 0; i < sbml.getModel().getReactionCount(); i++) {
			Reaction r = sbml.getModel().getReaction(i);
			if (r.getCompartment().equals(id)) {
				ReactionGlyph reactionGlyph = null;
				if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId())!=null) {
					reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId());
					double x = reactionGlyph.getBoundingBox().getPosition().getX();
					double y = reactionGlyph.getBoundingBox().getPosition().getY();
					double w = reactionGlyph.getBoundingBox().getDimensions().getWidth();
					double h = reactionGlyph.getBoundingBox().getDimensions().getHeight();
					if (x < cx) {
						return false;
					}
					if (y < cy) {
						return false;
					}
					if (x + w > cx + cw) {
						return false;
					}
					if (y + h > cy + ch) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean updateCompartmentsByLocation(boolean checkOnly) {
		Layout layout = getLayout();
		for (int i = 0; i < sbml.getModel().getSpeciesCount(); i++) {
			Species s = sbml.getModel().getSpecies(i);
			SpeciesGlyph speciesGlyph = null;
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s.getId())!=null) {
				speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s.getId());
				String compartment = getCompartmentByLocation((float)speciesGlyph.getBoundingBox().getPosition().getX(),
						(float)speciesGlyph.getBoundingBox().getPosition().getY(),(float)speciesGlyph.getBoundingBox().getDimensions().getWidth(),
						(float)speciesGlyph.getBoundingBox().getDimensions().getHeight());
				if (compartment.equals("")) {
					if (sbml.getModel().getCompartmentCount()>1) {
						return false;
					}
					CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+s.getCompartment());
					double x = speciesGlyph.getBoundingBox().getPosition().getX();
					double y = speciesGlyph.getBoundingBox().getPosition().getY();
					double w = speciesGlyph.getBoundingBox().getDimensions().getWidth();
					double h = speciesGlyph.getBoundingBox().getDimensions().getHeight();
					double cx = compartmentGlyph.getBoundingBox().getPosition().getX();
					double cy = compartmentGlyph.getBoundingBox().getPosition().getY();
					double cw = compartmentGlyph.getBoundingBox().getDimensions().getWidth();
					double ch = compartmentGlyph.getBoundingBox().getDimensions().getHeight();
					if (x < cx) {
						compartmentGlyph.getBoundingBox().getPosition().setX(x);
						compartmentGlyph.getBoundingBox().getDimensions().setWidth(cw + cx - x);
					}
					if (y < cy) {
						compartmentGlyph.getBoundingBox().getPosition().setY(y);
						compartmentGlyph.getBoundingBox().getDimensions().setHeight(ch + cy - y);
					}
					if (x + w > cx + cw) {
						compartmentGlyph.getBoundingBox().getDimensions().setWidth(x + w - cx);
					}
					if (y + h > cy + ch) {
						compartmentGlyph.getBoundingBox().getDimensions().setHeight(y + h - cy);
					}
				}
				if (!checkOnly)	s.setCompartment(compartment);
			}
		}
		for (int i = 0; i < sbml.getModel().getReactionCount(); i++) {
			Reaction r = sbml.getModel().getReaction(i);
			ReactionGlyph reactionGlyph = null;
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId())!=null) {
				reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId());
				String compartment = getCompartmentByLocation((float)reactionGlyph.getBoundingBox().getPosition().getX(),
						(float)reactionGlyph.getBoundingBox().getPosition().getY(),(float)reactionGlyph.getBoundingBox().getDimensions().getWidth(),
						(float)reactionGlyph.getBoundingBox().getDimensions().getHeight());
				if (compartment.equals("")) {
					if (sbml.getModel().getCompartmentCount()>1) {
						return false;
					}
					CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+r.getCompartment());
					double x = reactionGlyph.getBoundingBox().getPosition().getX();
					double y = reactionGlyph.getBoundingBox().getPosition().getY();
					double w = reactionGlyph.getBoundingBox().getDimensions().getWidth();
					double h = reactionGlyph.getBoundingBox().getDimensions().getHeight();
					double cx = compartmentGlyph.getBoundingBox().getPosition().getX();
					double cy = compartmentGlyph.getBoundingBox().getPosition().getY();
					double cw = compartmentGlyph.getBoundingBox().getDimensions().getWidth();
					double ch = compartmentGlyph.getBoundingBox().getDimensions().getHeight();
					if (x < cx) {
						compartmentGlyph.getBoundingBox().getPosition().setX(x);
						compartmentGlyph.getBoundingBox().getDimensions().setWidth(cw + cx - x);
					}
					if (y < cy) {
						compartmentGlyph.getBoundingBox().getPosition().setY(y);
						compartmentGlyph.getBoundingBox().getDimensions().setHeight(ch + cy - y);
					}
					if (x + w > cx + cw) {
						compartmentGlyph.getBoundingBox().getDimensions().setWidth(x + w - cx);
					}
					if (y + h > cy + ch) {
						compartmentGlyph.getBoundingBox().getDimensions().setHeight(y + h - cy);
					}
				}
				if (!checkOnly) r.setCompartment(compartment);
			}
		}
		return true;
	}
	
	public String getCompartmentByLocation(float x, float y, float w, float h) {
		String compartment = "";
		/*
		if (sbml.getModel().getCompartmentCount() > 0) {
			compartment = sbml.getModel().getCompartment(0).getId();
		}
		*/
		double distance = -1;
		for (int i = 0; i < sbml.getModel().getCompartmentCount(); i++) {
			Compartment c = sbml.getModel().getCompartment(i);
			Layout layout = getLayout();
			CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+c.getId());
			if (compartmentGlyph != null) {
				double cx = compartmentGlyph.getBoundingBox().getPosition().getX();
				double cy = compartmentGlyph.getBoundingBox().getPosition().getY();
				double cw = compartmentGlyph.getBoundingBox().getDimensions().getWidth();
				double ch = compartmentGlyph.getBoundingBox().getDimensions().getHeight();
				if (x >= cx && y >= cy && x + w <= cx+cw && y + h <= cy+ch) {
					double calcDist = (x - cx) + (y - cy);
					if (distance==-1 || distance > calcDist) {
						compartment = compartmentGlyph.getCompartment();
					}
				}
			} 
		}
		return compartment;
	}

	public String createSpecies(String id, float x, float y) {
		String compartment;
		if (x >= 0 && y >= 0) {
			compartment = getCompartmentByLocation(x,y, GlobalConstants.DEFAULT_SPECIES_WIDTH, 
					GlobalConstants.DEFAULT_SPECIES_HEIGHT);
			if (compartment.equals("")) {
				Utility.createErrorMessage("Compartment Required", "Species must be placed within a compartment.");
				return null;
			}
		} else {
			if (sbml.getModel().getCompartmentCount()==0) {
				Utility.createErrorMessage("Compartment Required", "Species must be placed within a compartment.");
				return null;
			}
			compartment = sbml.getModel().getCompartment(0).getId();
		}
		if (id == null) {
			do {
				creatingSpeciesID++;
				id = "S" + String.valueOf(creatingSpeciesID);
			} while (SBMLutilities.getElementBySId(sbml, id)!=null);
		}
		if (sbml != null && sbml.getModel().getSpecies(id)==null) {
			Model m = sbml.getModel();
			Species species = m.createSpecies();
			species.setId(id);
			// Set default species metaID
			metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, species, metaIDIndex); 
			species.setCompartment(compartment);
			species.setBoundaryCondition(false);
			species.setConstant(false);
			species.setInitialAmount(0);
			species.setHasOnlySubstanceUnits(true);
			if (speciesPanel!=null)
				speciesPanel.refreshSpeciesPanel(this);
			if (x >= 0 && y >= 0) {
				Layout layout = getLayout();
				SpeciesGlyph speciesGlyph = null;
				if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
					speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
				} else {
					speciesGlyph = layout.createSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
					speciesGlyph.createBoundingBox();
					speciesGlyph.getBoundingBox().createDimensions();
					speciesGlyph.getBoundingBox().createPosition();
					speciesGlyph.setSpecies(id);
				}
				speciesGlyph.getBoundingBox().getPosition().setX(x);
				speciesGlyph.getBoundingBox().getPosition().setY(y);
				speciesGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_SPECIES_WIDTH);
				speciesGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_SPECIES_HEIGHT);
				TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
				textGlyph.createBoundingBox();
				textGlyph.getBoundingBox().createDimensions();
				textGlyph.getBoundingBox().createPosition();
				textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
				textGlyph.setText(SBMLutilities.getArrayId(sbml,id));
				textGlyph.setBoundingBox(speciesGlyph.getBoundingBox().clone());
			}
		}
		return id;
	}

	public void createReaction(String id, float x, float y) {
		String compartment = getCompartmentByLocation(x,y, GlobalConstants.DEFAULT_REACTION_WIDTH, 
				GlobalConstants.DEFAULT_REACTION_HEIGHT);
		if (compartment.equals("")) {
			Utility.createErrorMessage("Compartment Required", "Reactions must be placed within a compartment.");
			return;
		}
		if (id == null) {
			do {
				creatingReactionID++;
				id = "R" + String.valueOf(creatingReactionID);
			}
			while (SBMLutilities.getElementBySId(sbml, id)!=null);
		}
		Layout layout = getLayout();
		ReactionGlyph reactionGlyph = null;
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id);
		} else {
			reactionGlyph = layout.createReactionGlyph(GlobalConstants.GLYPH+"__"+id);
			reactionGlyph.createBoundingBox();
			reactionGlyph.getBoundingBox().createDimensions();
			reactionGlyph.getBoundingBox().createPosition();
			reactionGlyph.setReaction(id);
		}
		reactionGlyph.getBoundingBox().getPosition().setX(x);
		reactionGlyph.getBoundingBox().getPosition().setY(y);
		reactionGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_REACTION_WIDTH);
		reactionGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_REACTION_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.createBoundingBox();
		textGlyph.getBoundingBox().createDimensions();
		textGlyph.getBoundingBox().createPosition();
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,id));
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox().clone());
		Model m = sbml.getModel();
		Reaction r = m.createReaction();
		r.setId(id);
		// Set default reaction metaID
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, r, metaIDIndex); 
		r.setCompartment(compartment);
		r.setReversible(false);
		r.setFast(false);
		KineticLaw k = r.createKineticLaw();
		LocalParameter p = k.createLocalParameter();
		p.setId("kf");
		p.setValue(0.1);
		p = k.createLocalParameter();
		p.setId("kr");
		p.setValue(1.0);
		k.setMath(SBMLutilities.myParseFormula("kf"));
	}

	public void createRule(String id, float x, float y) {
		Layout layout = getLayout();
		GeneralGlyph generalGlyph = null;
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id)!=null) {
			generalGlyph = (GeneralGlyph)layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id);
		} else {
			generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+id);
			generalGlyph.createBoundingBox();
			generalGlyph.getBoundingBox().createDimensions();
			generalGlyph.getBoundingBox().createPosition();
			generalGlyph.unsetReference();
			generalGlyph.setMetaidRef(id);
		}
		generalGlyph.getBoundingBox().getPosition().setX(x);
		generalGlyph.getBoundingBox().getPosition().setY(y);
		generalGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_RULE_WIDTH);
		generalGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_RULE_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.createBoundingBox();
		textGlyph.getBoundingBox().createDimensions();
		textGlyph.getBoundingBox().createPosition();
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,id));
		textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());
	}

	public void createConstraint(String id, float x, float y) {
		Layout layout = getLayout();
		GeneralGlyph generalGlyph = null;
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id)!=null) {
			generalGlyph = (GeneralGlyph)layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id);
		} else {
			generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+id);
			generalGlyph.createBoundingBox();
			generalGlyph.getBoundingBox().createDimensions();
			generalGlyph.getBoundingBox().createPosition();
			generalGlyph.unsetReference();
			generalGlyph.setMetaidRef(id);
		}
		generalGlyph.getBoundingBox().getPosition().setX(x);
		generalGlyph.getBoundingBox().getPosition().setY(y);
		generalGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_CONSTRAINT_WIDTH);
		generalGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_CONSTRAINT_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.createBoundingBox();
		textGlyph.getBoundingBox().createDimensions();
		textGlyph.getBoundingBox().createPosition();
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,id));
		textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());
	}

	public void createEvent(String id, float x, float y,boolean isTransition) {
		Layout layout = getLayout();
		GeneralGlyph generalGlyph = null;
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id)!=null) {
			generalGlyph = (GeneralGlyph)layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id);
		} else {
			generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+id);
			generalGlyph.createBoundingBox();
			generalGlyph.getBoundingBox().createDimensions();
			generalGlyph.getBoundingBox().createPosition();
			generalGlyph.setReference(id);
		}
		generalGlyph.getBoundingBox().getPosition().setX(x);
		generalGlyph.getBoundingBox().getPosition().setY(y);
		if (isTransition) {
			generalGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_TRANSITION_WIDTH);
			generalGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_TRANSITION_HEIGHT);
		} else {
			generalGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_EVENT_WIDTH);
			generalGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_EVENT_HEIGHT);
		}
		TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.createBoundingBox();
		textGlyph.getBoundingBox().createDimensions();
		textGlyph.getBoundingBox().createPosition();
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,id));
		textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());
	}
	
	public String createPromoter(String promoterId, float x, float y, boolean isExplicit) {
		return createPromoter(promoterId, x, y, isExplicit, true, null);
	}
	
	public String createPromoter(String promoterId, float x, float y, boolean isExplicit, 
			boolean createProduction, String reactionId) {
		createProductionDefaultParameters();
		String compartment;
		compartment = getCompartmentByLocation(x,y,GlobalConstants.DEFAULT_SPECIES_WIDTH,GlobalConstants.DEFAULT_SPECIES_HEIGHT);
		if (compartment.equals("")) {
			if (sbml.getModel().getCompartmentCount()==0) {
				Utility.createErrorMessage("Compartment Required", "Species must be placed within a compartment.");
				return "";
			}
			compartment = sbml.getModel().getCompartment(0).getId();
		} 
		Species promoter = sbml.getModel().createSpecies();
		// Set default species ID
		if (promoterId == null) {
			do {
				creatingPromoterID++;
				promoterId = "P" + String.valueOf(creatingPromoterID);
			}
			while (SBMLutilities.getElementBySId(sbml, promoterId)!=null);
		}
		promoter.setId(promoterId);
		// Set default promoter metaID
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, promoter, metaIDIndex); 
		
		promoter.setSBOTerm(GlobalConstants.SBO_PROMOTER_BINDING_REGION);
		promoter.setInitialAmount(sbml.getModel().getParameter(GlobalConstants.PROMOTER_COUNT_STRING).getValue());

		promoter.setCompartment(compartment);
		promoter.setBoundaryCondition(false);
		promoter.setConstant(false);
		promoter.setHasOnlySubstanceUnits(true);
		if (createProduction)
			createProductionReaction(promoterId, reactionId, null, null, null, null, null, null, false, null);
		if (speciesPanel!=null)
			speciesPanel.refreshSpeciesPanel(this);
		if (isExplicit) {
			Layout layout = getLayout();
			SpeciesGlyph speciesGlyph = null;
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+promoterId)!=null) {
				speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+promoterId);
			} else {
				speciesGlyph = layout.createSpeciesGlyph(GlobalConstants.GLYPH+"__"+promoterId);
				speciesGlyph.createBoundingBox();
				speciesGlyph.getBoundingBox().createDimensions();
				speciesGlyph.getBoundingBox().createPosition();
				speciesGlyph.setSpecies(promoterId);
			}
			speciesGlyph.getBoundingBox().getPosition().setX(x);
			speciesGlyph.getBoundingBox().getPosition().setY(y);
			speciesGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_SPECIES_WIDTH);
			speciesGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_SPECIES_HEIGHT);
			TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+promoterId);
			textGlyph.createBoundingBox();
			textGlyph.getBoundingBox().createDimensions();
			textGlyph.getBoundingBox().createPosition();
			textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+promoterId);
			textGlyph.setText(SBMLutilities.getArrayId(sbml,promoterId));
			textGlyph.setBoundingBox(speciesGlyph.getBoundingBox().clone());
		}

		return promoterId;
	}
	
	public String createVariable(String id, float x, float y, boolean isPlace, boolean isBoolean) {
		Parameter parameter = sbml.getModel().createParameter();
		// Set default species ID
		if (isPlace) {
			if (id == null) {
				do {
					creatingPlaceID++;
					id = "p" + String.valueOf(creatingPlaceID);
				}
				while ((SBMLutilities.getElementBySId(sbml, id)!=null)||(SBMLutilities.getElementByMetaId(sbml, id)!=null));
			}			
		} else {
			if (id == null) {
				do {
					creatingVariableID++;
					id = "V" + String.valueOf(creatingVariableID);
				}
				while ((SBMLutilities.getElementBySId(sbml, id)!=null)||(SBMLutilities.getElementByMetaId(sbml, id)!=null));
			}
		}
		parameter.setId(id);
		// Set default promoter metaID
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, parameter, metaIDIndex); 
		parameter.setConstant(false);
		parameter.setValue(0.0);
		if (isPlace) {
			parameter.setSBOTerm(GlobalConstants.SBO_PETRI_NET_PLACE);
		} else if (isBoolean) {
			parameter.setSBOTerm(GlobalConstants.SBO_LOGICAL);
		}

		Layout layout = getLayout();
		GeneralGlyph generalGlyph = null;
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id)!=null) {
			generalGlyph = (GeneralGlyph)layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+id);
		} else {
			generalGlyph = layout.createGeneralGlyph(GlobalConstants.GLYPH+"__"+id);
			generalGlyph.createBoundingBox();
			generalGlyph.getBoundingBox().createDimensions();
			generalGlyph.getBoundingBox().createPosition();
			generalGlyph.setReference(id);
		}
		generalGlyph.getBoundingBox().getPosition().setX(x);
		generalGlyph.getBoundingBox().getPosition().setY(y);
		generalGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_VARIABLE_WIDTH);
		generalGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_VARIABLE_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.createBoundingBox();
		textGlyph.getBoundingBox().createDimensions();
		textGlyph.getBoundingBox().createPosition();
		textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(SBMLutilities.getArrayId(sbml,id));
		textGlyph.setBoundingBox(generalGlyph.getBoundingBox().clone());

		return id;
	}
	
	public String createCompartment(String id, float x, float y) {
		if (!checkCompartmentOverlap(id,x,y,
				GlobalConstants.DEFAULT_COMPARTMENT_WIDTH,GlobalConstants.DEFAULT_COMPARTMENT_HEIGHT)) {
			Utility.createErrorMessage("Compartment Overlap", "Compartments must not overlap.");
			return "";
		}
		Compartment compartment = sbml.getModel().createCompartment();
		if (id == null) {
			do {
				creatingCompartmentID++;
				id = "Comp" + String.valueOf(creatingCompartmentID);
			}
			while ((SBMLutilities.getElementBySId(sbml, id)!=null)||(SBMLutilities.getElementByMetaId(sbml, id)!=null));
		}
		compartment.setId(id);
		// Set default promoter metaID
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, compartment, metaIDIndex); 
		compartment.setConstant(true);
		compartment.setSize(1);
		compartment.setSpatialDimensions(3);

		if (!isGridEnabled()) {
			Layout layout = getLayout();
			CompartmentGlyph compartmentGlyph = null;
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+id);
			} else {
				compartmentGlyph = layout.createCompartmentGlyph(GlobalConstants.GLYPH+"__"+id);
				compartmentGlyph.setCompartment(id);
				compartmentGlyph.createBoundingBox();
				compartmentGlyph.getBoundingBox().createDimensions();
				compartmentGlyph.getBoundingBox().createPosition();
			}
			compartmentGlyph.getBoundingBox().getPosition().setX(x);
			compartmentGlyph.getBoundingBox().getPosition().setY(y);
			compartmentGlyph.getBoundingBox().getDimensions().setWidth(GlobalConstants.DEFAULT_COMPARTMENT_WIDTH);
			compartmentGlyph.getBoundingBox().getDimensions().setHeight(GlobalConstants.DEFAULT_COMPARTMENT_HEIGHT);
			TextGlyph textGlyph = layout.createTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
			textGlyph.createBoundingBox();
			textGlyph.getBoundingBox().createDimensions();
			textGlyph.getBoundingBox().createPosition();
			textGlyph.setGraphicalObject(GlobalConstants.GLYPH+"__"+id);
			textGlyph.setText(SBMLutilities.getArrayId(sbml,id));
			textGlyph.setBoundingBox(compartmentGlyph.getBoundingBox().clone());
		}
		return id;
	}

	/**
	 * Given a properties list (species or components) and some coords, center
	 * over that point.
	 */
	/*
	public void centerVertexOverPoint(Properties prop, double x, double y) {
		x -= Double.parseDouble(prop.getProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH))) / 2.0;
		y -= Double.parseDouble(prop.getProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_HEIGHT))) / 2.0;
		prop.setProperty("graphx", String.valueOf(x));
		prop.setProperty("graphy", String.valueOf(y));
	}
	*/
	
	public boolean isSBOLGene(String promoterId) {
		Species species = sbml.getModel().getSpecies(promoterId);
		return AnnotationUtility.hasSBOLAnnotation(species);
	}

	public boolean isPromoterExplicit(String promoterId) {
		if (promoterId != null && getLayout().getSpeciesGlyph(GlobalConstants.GLYPH+"__"+promoterId)!=null)
			return true;
		return false;
	}
	
	public String influenceHasExplicitPromoter(String infName) {
		String promoterName;
		if (infName.contains(">")) {
			promoterName = infName.substring(infName.indexOf(">")+1);
		} else {
			promoterName = infName.substring(infName.indexOf("|")+1);
		}
		if (getPromoters().contains(promoterName)) return promoterName;
		return null;
	}

	/*
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
	*/

	public String[] getImplicitPromotersAsArray() {
		String[] s = new String[getPromoters().size()];
		int index = 0;
		for (String prom : getPromoters()) {
			if (!isPromoterExplicit(prom)) {
				s[index] = prom;
				index++;
			}
		}
		String[] implicit = new String[index];
		for (int i = 0; i < index; i++)
			implicit[i] = s[i];
		Arrays.sort(implicit);
		return implicit;
	}

	/*
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
	*/
	
	/*
	public boolean globalParameterIsSet(String parameter) {
		return globalParameters.containsKey(parameter);
	}
	*/

	public String getParameter(String parameter) {
		if (sbml != null && sbml.getModel().getInitialAssignment(parameter)!=null) {
			return SBMLutilities.myFormulaToString(sbml.getModel().getInitialAssignment(parameter).getMath());
		} else if (sbml != null && sbml.getModel().getParameter(parameter)!=null){
			return ""+sbml.getModel().getParameter(parameter).getValue();
		} else {
			return defaultParameters.get(parameter);
		}
		/*
		if (globalParameters.containsKey(parameter)) {
			return globalParameters.get(parameter);
		}
		else {
			return defaultParameters.get(parameter);
		}
		*/
	}

	public void setParameter(String parameter, String value) {
		//globalParameters.put(parameter, value);
		if (sbml != null) { 
			if (value.startsWith("(")) {
				AnnotationUtility.setSweepAnnotation(sbml.getModel().getParameter(parameter), value);
			} else {
				if (sbml.getModel().getParameter(parameter)!=null) {
					sbml.getModel().getParameter(parameter).setValue(Double.parseDouble(value));
				}
			}
		} 
		//parameters.put(parameter, value);
	}

	/*
	public void setDefaultParameter(String parameter, String value) {
		defaultParameters.put(parameter, value);
	}
	*/
	
	/*
	public void removeParameter(String parameter) {
		globalParameters.remove(parameter);
	}
	*/

	private void loadDefaultEnclosingCompartment() {
		if (sbml != null) {
			if (sbml.getModel().getCompartmentCount()==0) {
				/*
				Compartment c = sbml.getModel().createCompartment();
				c.setId("Cell");
				c.setSize(1);
				c.setSpatialDimensions(3);
				c.setConstant(true);
				*/
				return;
			}
			for (int i = 0; i < sbml.getModel().getCompartmentCount(); i++) {
				Compartment compartment = sbml.getModel().getCompartment(i);
				if (AnnotationUtility.checkObsoleteAnnotation(compartment,"EnclosingCompartment")) {
					AnnotationUtility.removeObsoleteAnnotation(compartment);
					return;
				} 
			}
			Port port = sbmlCompModel.getListOfPorts().get(GlobalConstants.ENCLOSING_COMPARTMENT);
			if (port!=null) {
				sbmlCompModel.removePort(port);
				return;
			} 
			port = sbmlCompModel.getListOfPorts().get(GlobalConstants.DEFAULT_COMPARTMENT);
			if (port!=null) {
				port.setId(GlobalConstants.COMPARTMENT + "__" + port.getIdRef());
				return;
			} 
		}
	}
	
	private boolean portReplacedBy(Submodel submodel,String portId,Port port) {
		ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(sbml.getModel());
		for (int i = 0; i < elements.size(); i++) {
			SBase sbase = elements.get(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);
			if (sbmlSBase!=null) {
				if (sbmlSBase.isSetReplacedBy()) {
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if (replacement.getSubmodelRef().equals(submodel.getId()) &&
							replacement.isSetPortRef() && replacement.getPortRef().equals(portId)) {
						if (sbase instanceof NamedSBase) {
							port.setIdRef(((NamedSBase)sbase).getId());
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private boolean isPortRemoved(Submodel submodel,String portId) {
		for (int i = 0; i < submodel.getListOfDeletions().size(); i++) {
			Deletion deletion = submodel.getListOfDeletions().get(i);
			if (deletion.isSetPortRef() && deletion.getPortRef().equals(portId)) {
				return true;
			}
		}
		ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(sbml.getModel());
		for (int i = 0; i < elements.size(); i++) {
			SBase sbase = elements.get(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);
			if (sbmlSBase!=null) {
				for (int j = 0; j < sbmlSBase.getListOfReplacedElements().size(); j++) {
					ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
					if (replacement.getSubmodelRef().equals(submodel.getId()) &&
							replacement.isSetPortRef() && replacement.getPortRef().equals(portId)) {
							return true;
						}
				}
				if (sbmlSBase.isSetReplacedBy()) {
					ReplacedBy replacement = sbmlSBase.getReplacedBy();
					if (replacement.getSubmodelRef().equals(submodel.getId()) &&
							replacement.isSetPortRef() && replacement.getPortRef().equals(portId)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void updatePorts() {
		int j = 0;
		while (j < sbmlCompModel.getPortCount()) {
			Port port = sbmlCompModel.getListOfPorts().get(j);
			if (port.isSetSBaseRef()) {
				sbmlCompModel.removePort(port);
			} else if (port.isSetIdRef() && SBMLutilities.getElementBySId(sbml, port.getIdRef()) == null) {
				sbmlCompModel.removePort(port);
			} else if (port.isSetMetaIdRef() && SBMLutilities.getElementByMetaId(sbml, port.getMetaIdRef()) == null) {
				sbmlCompModel.removePort(port);
			} else {
				/* TODO: temporary to restore SBO terms */
				SBMLutilities.isInputPort(port);
				SBMLutilities.isOutputPort(port);
				j++;
			}
		}
		//if (!this.isGridEnabled()) {
		for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
			Submodel submodel = sbmlCompModel.getListOfSubmodels().get(i);
			BioModel subBioModel = new BioModel(path);		
			String extModelFile = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef())
					.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subBioModel.load(path + separator + extModelFile);
//			TODO:  Not currently supported.
			String md5 = Utility.MD5(subBioModel.getSBMLDocument());
			if (!sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).getMd5().equals("") &&
					!sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).getMd5().equals(md5)) {
				//System.out.println("MD5 DOES NOT MATCH");
				sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).setMd5(md5);
			}
			SBMLutilities.removeStaleReplacementsDeletions(submodel, this, subBioModel);
			SBMLutilities.addImplicitReplacementsDeletions(submodel, this, subBioModel);
			for (j = 0; j < subBioModel.getSBMLCompModel().getListOfPorts().size(); j++) {
				Port subPort = subBioModel.getSBMLCompModel().getListOfPorts().get(j);
				if (!isPortRemoved(submodel,subPort.getId())) {
					Port port = sbmlCompModel.getPort(subPort.getId()+"__"+submodel.getId());
					if (port!=null) {
						sbmlCompModel.removePort(port);
					}
					port = sbmlCompModel.createPort();
					port.setId(subPort.getId()+"__"+submodel.getId());
					if (!portReplacedBy(submodel,subPort.getId(),port)) {
						port.setIdRef(submodel.getId());
						SBaseRef sbaseRef = port.createSBaseRef();
						sbaseRef.setPortRef(subPort.getId());
						// TODO: need to support arrays of subModels which has arrays of ports
						ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(submodel);
						ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
						ArraysSBasePlugin sBasePluginSBaseRef = SBMLutilities.getArraysSBasePlugin(sbaseRef);
						ArraysSBasePlugin sBasePluginSubPort = SBMLutilities.getArraysSBasePlugin(subPort);
						for (int k = 0; k < sBasePlugin.getListOfDimensions().size(); k++) {
							Dimension dimen = sBasePlugin.getDimensionByArrayDimension(k);
							Index portIndex = sBasePluginPort.createIndex();
							portIndex.setReferencedAttribute("comp:idRef");
							portIndex.setArrayDimension(k);
							portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
						}
						sBasePluginPort.setListOfDimensions(sBasePluginSubPort.getListOfDimensions().clone());
						sBasePluginSBaseRef.setListOfIndices(sBasePluginSubPort.getListOfIndices().clone());
						for (int k=0; k<sBasePluginSBaseRef.getIndexCount();k++) {
							sBasePluginSBaseRef.getIndex(k).setReferencedAttribute("comp:portRef");
						}
					} else {
						// TODO: need to deal with array replacedBy
					}
					if (subPort.isSetSBOTerm()) {
						port.setSBOTerm(subPort.getSBOTerm());
					}
				}
			}
		}
		//}
	}
	
	public boolean isArray(String id) {
		SBase variable = SBMLutilities.getElementBySId(sbml,id);
		if (variable!=null) {
			ArraysSBasePlugin ABV = SBMLutilities.getArraysSBasePlugin(variable);
			int varDimCount = ABV.getDimensionCount();
			if (varDimCount > 0) {
				return true;
			} 
			return false;
		} 
		return false;
	}

	public boolean isInput(String id) {
		Port port = getPortByIdRef(id);
		if (port != null) {
			return SBMLutilities.isInputPort(port);
		}
		return false;
	}
	
	public boolean isOutput(String id) {
		Port port = getPortByIdRef(id);
		if (port != null) {
			return SBMLutilities.isOutputPort(port);
		}
		return false;
	}
		
	public void removeStaleLayout() {
		Layout layout = getLayout();
		Model model = sbml.getSBMLDocument().getModel();
		int i = 0;
		while (i < layout.getListOfSpeciesGlyphs().size()) {
			SpeciesGlyph sg = layout.getSpeciesGlyph(i);
			if (sg.getSpecies() == null || model.getSpecies(sg.getSpecies())==null) {
				layout.removeSpeciesGlyph(sg);
			} else {
				i++;
			}
		}
		i = 0;
		while (i < layout.getListOfCompartmentGlyphs().size()) {
			CompartmentGlyph cg = layout.getCompartmentGlyph(i);
			if (cg.getCompartment() == null || model.getCompartment(cg.getCompartment())==null) {
				layout.removeCompartmentGlyph(cg);
			} else {
				i++;
			}
		}
		i = 0;
		while (i < layout.getListOfReactionGlyphs().size()) {
			ReactionGlyph rg = layout.getReactionGlyph(i);
			if (rg.getReaction() == null || model.getReaction(rg.getReaction())==null) {
				layout.removeReactionGlyph(rg);
			} else {
				i++;
			}
		}
		if (!isGridEnabled()) {
			i = 0;
			while (i < layout.getListOfAdditionalGraphicalObjects().size()) {
				GeneralGlyph g = (GeneralGlyph) layout.getListOfAdditionalGraphicalObjects().get(i);
				if (g.getReference() == null && g.getMetaidRef() == null) {
					layout.removeGeneralGlyph(g);
				} else if (g.isSetReference() && SBMLutilities.getElementBySId(model, g.getReference())==null) {
					if (SBMLutilities.getElementByMetaId(model, g.getReference())==null) { 
						layout.removeGeneralGlyph(g);
					} else {
						g.setMetaidRef(g.getReference());
						g.unsetReference();
					}
				} else if (g.isSetMetaidRef() && SBMLutilities.getElementByMetaId(model, g.getMetaidRef())==null) {
					layout.removeGeneralGlyph(g);
				} else {
					i++;
				}
			}
		}
		i = 0;
		while (i < layout.getListOfTextGlyphs().size()) {
			TextGlyph tg = layout.getTextGlyph(i);
			if (tg.getGraphicalObject() == null || (layout.getCompartmentGlyph(tg.getGraphicalObject())==null &&
					layout.getCompartmentGlyph(tg.getGraphicalObject())==null &&
					layout.getSpeciesGlyph(tg.getGraphicalObject())==null &&
					layout.getReactionGlyph(tg.getGraphicalObject())==null &&
					layout.getListOfAdditionalGraphicalObjects().get(tg.getGraphicalObject())==null &&
					SBMLutilities.getElementBySId(model, tg.getGraphicalObject())==null && 
					SBMLutilities.getElementByMetaId(model, tg.getGraphicalObject())==null)) {
				layout.removeTextGlyph(tg);
			} else {
				i++;
			}
		}
	}

	private boolean loadSBMLFile(String sbmlFile) {
		boolean successful = true;
		if (!sbmlFile.equals("")) {
			if (new File(path + separator + sbmlFile).exists()) {
				sbml = SBMLutilities.readSBML(path + separator + sbmlFile);
				createLayoutPlugin();
				createCompPlugin();
				createFBCPlugin();
			} else {
				createSBMLDocument(sbmlFile.replace(".xml",""),false,false);
				successful = false;
			}
		} 
		loadDefaultParameterMap();
		//loadDefaultParameters();
		loadDefaultEnclosingCompartment();
		//updateCompartmentReplacements();
		SBMLutilities.fillBlankMetaIDs(sbml);
		loadGridSize();
		updatePorts();
		removeStaleLayout();
		
		for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
			if (sbml.getModel().getParameter(i).getId().contains("__locations")) {
				updateGridSpecies(sbml.getModel().getParameter(i).getId().replace("__locations",""));
			}
		}
		return successful;
	}

	private void loadSBMLFromBuffer(StringBuffer buffer) {	
		try {
			sbml = SBMLReader.read(buffer.toString());
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		sbmlLayout = SBMLutilities.getLayoutModelPlugin(sbml.getModel());
		sbmlFBC = SBMLutilities.getFBCModelPlugin(sbml.getModel());
		sbmlComp = SBMLutilities.getCompSBMLDocumentPlugin(sbml);
		sbmlCompModel = SBMLutilities.getCompModelPlugin(sbml.getModel());
		loadDefaultEnclosingCompartment();
		loadGridSize();
	}
	
	public void recurseExportSingleFile(ArrayList<String> comps,CompModelPlugin subCompModel,CompSBMLDocumentPlugin subComp,
				CompSBMLDocumentPlugin documentComp) {
		for (int i = 0; i < subCompModel.getListOfSubmodels().size(); i++) {
			String subModelId = subCompModel.getListOfSubmodels().get(i).getId();
			String extModel = subComp.getListOfExternalModelDefinitions().get(subCompModel.getListOfSubmodels().get(subModelId)
					.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			if (!comps.contains(extModel)) {
				comps.add(extModel);
				SBMLDocument subDocument = SBMLutilities.readSBML(path + separator + extModel);
				CompModelPlugin subDocumentCompModel = SBMLutilities.getCompModelPlugin(subDocument.getModel());
				ModelDefinition md = new ModelDefinition(subDocument.getModel());
				String id = subDocument.getModel().getId();
				ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(subDocument);
				for (int j = 0; j < elements.size(); j++) {
					SBase sbase = elements.get(j);
					if (sbase.isSetMetaId()) {
						for (int k = 0; k < subDocumentCompModel.getListOfPorts().size(); k++) {
							Port port = subDocumentCompModel.getListOfPorts().get(k);
							if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(sbase.getMetaId())) {
								port.setMetaIdRef(id+"__"+sbase.getMetaId());
							}
						}
						SBMLutilities.setMetaId(sbase, id+"__"+sbase.getMetaId());
					}
				}
				documentComp.addModelDefinition(md);
				recurseExportSingleFile(comps,SBMLutilities.getCompModelPlugin(subDocument.getModel()),
						SBMLutilities.getCompSBMLDocumentPlugin(subDocument),documentComp);
			}
		}
	}
	
	public void exportSingleFile(String exportFile) {
		ArrayList<String> comps = new ArrayList<String>();
		SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		Model model = new Model(sbml.getModel());
		document.setModel(model);
		document.enablePackage(LayoutConstants.namespaceURI);
		SBMLutilities.getLayoutModelPlugin(document.getModel());
		if (sbmlCompModel.getListOfPorts().size() > 0 || sbmlCompModel.getListOfSubmodels().size() > 0) {
			document.enablePackage(CompConstants.namespaceURI);
		}
		if (sbmlFBC.getListOfObjectives().size() > 0) {
			document.enablePackage(FBCConstants.namespaceURI);
		} else {
			for (int i = 0; i < document.getModel().getNumReactions(); i++) {
				Reaction r = document.getModel().getReaction(i);
				FBCReactionPlugin rBounds = SBMLutilities.getFBCReactionPlugin(r);
				if (rBounds != null) {
					if (rBounds.isSetLowerFluxBound()) {
						document.enablePackage(FBCConstants.namespaceURI);
						break;
					} 
					if (rBounds.isSetUpperFluxBound()) {
						document.enablePackage(FBCConstants.namespaceURI);
						break;
					} 
				}
			}
		}
		document.enablePackage(ArraysConstants.namespaceURI);

		if (sbmlCompModel.getListOfSubmodels().size()>0) {
			CompSBMLDocumentPlugin documentComp = SBMLutilities.getCompSBMLDocumentPlugin(document);
			for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
				String subModelId = sbmlCompModel.getListOfSubmodels().get(i).getId();
				String extModel = sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(subModelId)
						.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
				if (!comps.contains(extModel)) {
					comps.add(extModel);
					SBMLDocument subDocument = SBMLutilities.readSBML(path + separator + extModel);
					CompModelPlugin subDocumentCompModel = SBMLutilities.getCompModelPlugin(subDocument.getModel());
					String id = subDocument.getModel().getId();
					// TODO: hack to avoid jsbml scope bug
					LayoutModelPlugin subDocumentLayoutModel = SBMLutilities.getLayoutModelPlugin(subDocument.getModel());
					Layout layout = subDocumentLayoutModel.getListOfLayouts().get("iBioSim");
					layout.setId(id+"__iBioSim");
					ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(subDocument);
					for (int j = 0; j < elements.size(); j++) {
						SBase sbase = elements.get(j);
						if (sbase.isSetMetaId()) {
							for (int k = 0; k < subDocumentCompModel.getListOfPorts().size(); k++) {
								Port port = subDocumentCompModel.getListOfPorts().get(k);
								if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(sbase.getMetaId())) {
									port.setMetaIdRef(id+"__"+sbase.getMetaId());
								}
							}
							SBMLutilities.setMetaId(sbase, id+"__"+sbase.getMetaId());
						}
					}
					ModelDefinition md = new ModelDefinition(subDocument.getModel());
					documentComp.addModelDefinition(md);
					recurseExportSingleFile(comps,SBMLutilities.getCompModelPlugin(subDocument.getModel()),
							SBMLutilities.getCompSBMLDocumentPlugin(subDocument),documentComp);
				}
			}
		}
		SBMLWriter writer = new SBMLWriter();
		try {
			writer.writeSBMLToFile(document, exportFile);
		} catch (SBMLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	private void expandListOfSubmodels(org.sbml.libsbml.CompModelPlugin docCompModel, org.sbml.libsbml.SBMLDocument sbml) {
		if (this.getGridEnabledFromFile(filename.replace(".gcm",".xml"))) {
			
			//look through the location parameter arrays
			for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
				
				org.sbml.libsbml.Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter, loop through the annotation and collect submodel IDs
				if (parameter.getId().contains("__locations")) {
					String modelId = parameter.getId().replace("__locations","");
					org.sbml.libsbml.Submodel submodel = docCompModel.getListOfSubmodels().get("GRID__"+modelId);
					submodel.removeFromParentAndDelete();
					String[] splitAnnotation = AnnotationUtility.parseArrayAnnotation(parameter);
					//find all components in the annotation
					for (int j = 1; j < splitAnnotation.length; j++) {
						submodel = docCompModel.createSubmodel();
						submodel.setId(splitAnnotation[j].split("=")[0].trim());
						submodel.setModelRef(modelId);
					}
				}	
			}
		}
	}
	
	private ArrayList<String> getListOfSubmodels() {
		ArrayList<String> comps = new ArrayList<String>();

		if (this.getGridEnabledFromFile(filename.replace(".gcm",".xml"))) {
			
			//look through the location parameter arrays
			for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
				
				Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter, loop through the annotation and collect submodel IDs
				if (parameter.getId().contains("__locations")) {
					String[] splitAnnotation = AnnotationUtility.parseArrayAnnotation(parameter);
					//find all components in the annotation
					for (int j = 1; j < splitAnnotation.length; j++) {
						comps.add(splitAnnotation[j].split("=")[0].trim());
					}
				}	
			}
		}
		else {
			
			for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++) {
				comps.add(sbmlCompModel.getListOfSubmodels().get(i).getId());
			}
		}
		return comps;
	}
	
	private String getExtModelFileName(String s) {
		
		String extModel;
		String componentModelRef = "";
		
		if (this.getGridEnabledFromFile(filename.replace(".gcm",".xml"))) {
		
			//look through the location parameter arrays to find the correct model ref
			for (int i = 0; i < sbml.getModel().getParameterCount(); ++i) {
				
				Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter
				if (parameter.getId().contains("__locations")) {
					
					if (AnnotationUtility.parseArrayAnnotation(parameter, s)!=null) {
						componentModelRef = parameter.getId().replace("__locations","");
						break;
					}					
				}				
			}
			
			extModel = componentModelRef + ".xml";
		}
		else {

			// load the component's gcm into a new GCMFile
			extModel = sbmlComp.getListOfExternalModelDefinitions().get(sbmlCompModel.getListOfSubmodels().get(s)
					.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
		}	
		
		return extModel;
	}
	
	public SBMLDocument flattenModelWithLibSBML(boolean removeComp) throws Exception {
		if (Gui.isLibsbmlFound()) {
			String tempFile = filename.replace(".gcm", "").replace(".xml", "") + "_temp.xml";
			save(tempFile);
			
			org.sbml.libsbml.SBMLReader reader = new org.sbml.libsbml.SBMLReader();
			org.sbml.libsbml.SBMLDocument document = reader.readSBML(tempFile);
			new File(tempFile).delete();
			
			document.enablePackage(org.sbml.libsbml.LayoutExtension.getXmlnsL3V1V1(), "layout", false);
			// TODO: Hack to allow flatten to work as it crashes with arrays namespace
			//document.enablePackage(org.sbml.libsbml.ArraysExtension.getXmlnsL3V1V1(), "arrays", false);
			document.enablePackage(org.sbml.libsbml.CompExtension.getXmlnsL3V1V1(), "comp", true);
			document.setPackageRequired("comp", true); 
			// following line causes unsatisfied link error via libsbml when attempting to save hierarchical models on windows machine
//			((org.sbml.libsbml.CompSBMLDocumentPlugin)document.getPlugin("comp")).setRequired(true);
			org.sbml.libsbml.CompModelPlugin sbmlCompModel = (org.sbml.libsbml.CompModelPlugin) document.getModel().getPlugin("comp");
			
			long numSubModels = sbmlCompModel.getNumSubmodels();
			if (numSubModels > 0 && isGridEnabled()) {
				expandListOfSubmodels(sbmlCompModel, document);
			}
			if (document.getErrorLog().getNumFailsWithSeverity(libsbmlConstants.LIBSBML_SEV_ERROR) > 0) {
				document.printErrors();
				return null;
			}
			else if (numSubModels > 0) {
				/* create a new conversion properties structure */
				org.sbml.libsbml.ConversionProperties props = new org.sbml.libsbml.ConversionProperties();

				/* add an option that we want to flatten */
				props.addOption("flatten comp", true, "flatten comp");

				/* add an option to leave ports if the user has requested this */
				props.addOption("leavePorts", !removeComp, "unused ports should be listed in the flattened model");
				
				props.addOption("abortIfUnflattenable", "none");
				
				props.addOption("performValidation", "false");
				
				props.addOption("basePath", path);

				//org.sbml.libsbml.SBMLWriter writer = new org.sbml.libsbml.SBMLWriter();
				//writer.writeSBMLToFile(document, tempFile);

				/* perform the conversion */
				if (document.convert(props) != libsbmlConstants.LIBSBML_OPERATION_SUCCESS) {
					return null;
				}
			}
			document.enablePackage(org.sbml.libsbml.CompExtension.getXmlnsL3V1V1(), "comp", !removeComp);
			org.sbml.libsbml.SBMLWriter writer = new org.sbml.libsbml.SBMLWriter();
			writer.writeSBMLToFile(document, tempFile);
			BioModel bioModel = new BioModel(path);
			bioModel.load(tempFile);
			new File(tempFile).delete();
			SBMLDocument doc = bioModel.getSBMLDocument();
			return doc;
		}
		throw new Exception("libsbml not found.  Unable to flatten model.");
	}
		
	public SBMLDocument flattenModel(boolean removeComp) {
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.flatten", "").equals("libsbml")) {
			//String tempFile = filename.replace(".gcm","").replace(".xml","")+"_temp.xml";
			//save(tempFile);
			SBMLDocument result = null;
			try {
				result = flattenModelWithLibSBML(removeComp);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result!=null) {
				result.getModel().setName("Created by libsbml flatten routine");
				return result;
			}
		}
		ArrayList<String> modelList = new ArrayList<String>();
		modelList.add(filename);
		String tempFile = filename.replace(".gcm","").replace(".xml","")+"_temp.xml";
		save(tempFile);
		
		BioModel model = new BioModel(path);
		model.load(tempFile);
		model.getSBMLDocument().getModel().unsetExtension(LayoutConstants.namespaceURI);
		model.getSBMLDocument().disablePackage(LayoutConstants.namespaceURI);
		if(model.getSBMLDocument().isPackageEnabled(ArraysConstants.shortLabel)) {
			// TODO: validate arrays before flattening
			//model.save(tempFile.replace("_temp", "_before"));
			try {
				SBMLDocument arrayFlat = ArraysFlattening.convert(model.getSBMLDocument());
				if (arrayFlat==null) {
					Utility.createErrorMessage("Array Flattening Failed", "Cannot flatten model.\n" + "There is a problem with arrays.");
					load(tempFile);
					new File(tempFile).delete();
					return null;
				}
				model.setSBMLDocument(arrayFlat);
				model.createCompPlugin();
				model.createFBCPlugin();
			} catch (Exception e) {
				Utility.createErrorMessage("Array Flattening Failed", "Cannot flatten model.\n" + "There is a problem with arrays.");
				load(tempFile);
				new File(tempFile).delete();
				return null;
			}
			/*
			SBMLWriter writer = new SBMLWriter();
			try {
				writer.writeSBMLToFile(model.getSBMLDocument(), tempFile.replace("_temp", "_after"));
			}
			catch (SBMLException e) {
				e.printStackTrace();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (XMLStreamException e) {
				e.printStackTrace();
			}
			*/
			//model.save(tempFile.replace("_temp", "_after"));
		}
		ArrayList<String> comps = model.getListOfSubmodels();

		// loop through the list of submodels
		for (String subModelId : comps) {
			BioModel subModel = new BioModel(path);		
			String extModelFile = model.getExtModelFileName(subModelId);
			subModel.load(path + separator + extModelFile);
			ArrayList<String> modelListCopy = copyArray(modelList);
			if (modelListCopy.contains(subModel.getFilename())) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten model.\n" + "There is a loop in the components.");
				load(tempFile);
				new File(tempFile).delete();
				return null;
			}
			modelListCopy.add(subModel.getFilename());

			// recursively add this component's sbml (and its inside components'
			// sbml, etc.) to the overall sbml
			BioModel flatSubModel = flattenModelRecurse(subModel, modelListCopy);
			if (flatSubModel!=null) {
				unionSBML(model, flatSubModel, subModelId, subModel.getParameter(GlobalConstants.RNAP_STRING));
			} else if (modelListCopy.isEmpty()) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten model.\n" + "There is a loop in the components.");
				load(tempFile);
				new File(tempFile).delete();
				return null;
			}
			else {
				Utility.createErrorMessage("Cannot Flatten Model", "Unable to flatten sbml files from components.");
				load(tempFile);
				new File(tempFile).delete();
				return null;
			}
		}
		model.getSBMLDocument().disablePackage(ArraysConstants.namespaceURI);
		if(removeComp)
		{
			model.getSBMLDocument().disablePackage(CompConstants.namespaceURI);
		}
		/* TODO: Removed this for now, needs better check
		if (model.getSBMLFBC().getObjectiveCount()==0 && 
				model.getSBMLFBC().getFluxBoundCount()==0) {
			model.getSBMLDocument().disablePackage(FBCConstants.namespaceURI);
		}
		*/
		model.getSBMLDocument().getModel().setName("Created by iBioSim flatten routine");
//		checkModelConsistency(model.getSBMLDocument());
		new File(tempFile).delete();
		return model.getSBMLDocument();
	}
	
	// TODO: check if this is up-to-date
	public SBMLDocument flattenBioModel() {
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.flatten", "").equals("libsbml")) {
			SBMLDocument result = null;
			try {
				// TODO: leavePorts or not?
				result = flattenModelWithLibSBML(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result!=null) {
				result.getModel().setName("Created by libsbml flatten routine");
				return result;
			}
		}

		ArrayList<String> modelList = new ArrayList<String>();
		modelList.add(filename);
		ArrayList<String> comps = getListOfSubmodels();

		// loop through the list of submodels
		for (String subModelId : comps) {
			BioModel subModel = new BioModel(path);		
			String extModelFile = getExtModelFileName(subModelId);
			subModel.load(path + separator + extModelFile);
			ArrayList<String> modelListCopy = copyArray(modelList);
			if (modelListCopy.contains(subModel.getFilename())) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten model.\n" + "There is a loop in the components.");
				return null;
			}
			modelListCopy.add(subModel.getFilename());

			// recursively add this component's sbml (and its inside components'
			// sbml, etc.) to the overall sbml
			BioModel flatSubModel = flattenModelRecurse(subModel, modelListCopy);
			if (flatSubModel!=null) {
				unionSBML(this, flatSubModel, subModelId, subModel.getParameter(GlobalConstants.RNAP_STRING));
			} else if (modelListCopy.isEmpty()) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten model.\n" + "There is a loop in the components.");
				return null;
			}
			else {
				Utility.createErrorMessage("Cannot Flatten Model", "Unable to flatten sbml files from components.");
				return null;
			}
		}
		//checkModelConsistency(this.getSBMLDocument());

		//SBMLUtil.check(this.getFilename(), this.getSBMLDocument(),false,false);

		return this.getSBMLDocument();
	}
	
	private BioModel flattenModelRecurse(BioModel model, ArrayList<String> modelList) {
		ArrayList<String> comps = new ArrayList<String>();
		
		model.getSBMLDocument().getModel().unsetExtension(LayoutConstants.namespaceURI);
		model.getSBMLDocument().disablePackage(LayoutConstants.namespaceURI);
		if(model.getSBMLDocument().isPackageEnabled(ArraysConstants.shortLabel)) {
			// TODO: validate arrays before flattening
			//model.save(model.filename.replace(".gcm","").replace(".xml","")+"_before.xml");
			
			model.setSBMLDocument(ArraysFlattening.convert(model.getSBMLDocument()));
			model.createCompPlugin();
			model.createFBCPlugin();
			
			//model.save(model.filename.replace(".gcm","").replace(".xml","")+"_after.xml");
		}
		
		for (int i = 0; i < model.getSBMLCompModel().getListOfSubmodels().size(); i++) {
			comps.add(model.getSBMLCompModel().getListOfSubmodels().get(i).getId());
		}
		
		for (String s : comps) {
			BioModel subModel = new BioModel(path);
			String extModel = model.getSBMLComp().getListOfExternalModelDefinitions().get(model.getSBMLCompModel().getListOfSubmodels().get(s)
					.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subModel.load(path + separator + extModel);
			ArrayList<String> modelListCopy = copyArray(modelList);
			if (modelListCopy.contains(subModel.getFilename())) {
				while (!modelList.isEmpty()) {
					modelList.remove(0);
				}
				return null;
			}
			modelListCopy.add(subModel.getFilename());
			//String subModelId = model.getSBMLDocument().getModel().getId();
			BioModel flatSubModel = flattenModelRecurse(subModel, modelListCopy);
			if (flatSubModel!=null) {
				unionSBML(model, flatSubModel, /*subModelId+"__"+*/s, subModel.getParameter(GlobalConstants.RNAP_STRING));
			} else {
				return null;
			}
		}
		return model;
	}

	private static String buildReplacementId(SBaseRef replacement) {
		String replacementId = replacement.getIdRef();
		for(SBaseRef sBaseRef = replacement.getSBaseRef(); sBaseRef != null;
				sBaseRef = sBaseRef.getSBaseRef()) {
			replacementId = replacementId + "__" + sBaseRef.getIdRef();
		}
		return replacementId;
	}
	
	private static void performDeletions(BioModel bioModel, BioModel subBioModel, String subModelId) {

		SBMLDocument subDocument = subBioModel.getSBMLDocument();
		Model subModel = subDocument.getModel();
		
		Submodel instance = bioModel.getSBMLCompModel().getListOfSubmodels().get(subModelId);			
		
		if (instance == null)
			return;
		
		for (int i = 0; i < instance.getListOfDeletions().size(); i++) {
			Deletion deletion = instance.getListOfDeletions().get(i);
			if (deletion.isSetPortRef()) {
				Port port = subBioModel.getSBMLCompModel().getListOfPorts().get(deletion.getPortRef());
				if (port!=null) {
					if (port.isSetIdRef()) {

						if (SBMLutilities.getElementBySId(subModel, port.getIdRef())!=null) {
							SBase sbase =  SBMLutilities.getElementBySId(subModel, port.getIdRef());
							sbase.removeFromParent();
						}
					} else if (port.isSetMetaIdRef()) {
						if (SBMLutilities.getElementByMetaId(subModel, port.getMetaIdRef())!=null) {
							SBase sbase =  SBMLutilities.getElementByMetaId(subModel, port.getMetaIdRef());
							sbase.removeFromParent();
						}
					} else if (port.isSetUnitRef()) {
						if (subModel.getUnitDefinition(port.getUnitRef())!=null) {
							SBase sbase = subModel.getUnitDefinition(port.getUnitRef());
							sbase.removeFromParent();
						}
					}
				}
			} else if (deletion.isSetIdRef()) {
				if (SBMLutilities.getElementBySId(subModel, deletion.getIdRef())!=null) {
					SBase sbase =  SBMLutilities.getElementBySId(subModel, deletion.getIdRef());
					sbase.removeFromParent();
				}
			} else if (deletion.isSetMetaIdRef()) {
				if (SBMLutilities.getElementByMetaId(subModel, deletion.getMetaIdRef())!=null) {
					SBase sbase =  SBMLutilities.getElementByMetaId(subModel, deletion.getMetaIdRef());
					sbase.removeFromParent();
				}
			} else if (deletion.isSetUnitRef()) {
				if (subModel.getUnitDefinition(deletion.getUnitRef())!=null) {
					SBase sbase = subModel.getUnitDefinition(deletion.getUnitRef());
					sbase.removeFromParent();
				}
			}
		}
	}
	
	private static String prepareReplacement(String newName,BioModel subBioModel,String subModelId,String replacementModelId,
			CompSBasePlugin sbmlSBase,String subId,String id) {

		for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) {
			ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
			if (replacement.getSubmodelRef().equals(replacementModelId)) {
				if (replacement.isSetPortRef()) {
					Port port = subBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
					if (port != null && subId.equals(port.getIdRef())) {
						newName = "_" + subModelId + "__" + id;
					}
				} else if (replacement.isSetIdRef()) {
					String replacementId = buildReplacementId(replacement);
					if (subId.equals(replacementId)) {
						newName = "_" + subModelId + "__" + id;
					}
				}
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			ReplacedBy replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(replacementModelId)) {
				if (replacement.isSetPortRef()) {
					Port port = subBioModel.getSBMLCompModel().getListOfPorts().get(replacement.getPortRef());
					if (port != null && subId.equals(port.getIdRef())) {
						newName = "__" + subModelId + "__" + id;
					}
				} else if (replacement.isSetIdRef()) {
					String replacementId = buildReplacementId(replacement);
					if (subId.equals(replacementId)) {
						newName = "__" + subModelId + "__" + id;
					}
				}
			}
		}
		return newName;
	}
	
	private BioModel unionSBML(BioModel bioModel, BioModel subBioModel, String subModelId, String RNAPamount) {
		SBMLDocument document = bioModel.getSBMLDocument();
		SBMLDocument subDocument = subBioModel.getSBMLDocument();
		SBMLutilities.setNamespaces(subDocument, document.getDeclaredNamespaces());

		Model model = document.getModel();
		Model subModel = subDocument.getModel();
		
		String replacementModelId = subModelId;
		if (bioModel.isGridEnabled()) {
			replacementModelId = "GRID__" + subModel.getId();
		} 	

		for (int i = 0; i < bioModel.getSBMLCompModel().getListOfPorts().size(); i++) {
			Port p = bioModel.getSBMLCompModel().getListOfPorts().get(i);
			if (p.isSetIdRef() && p.getIdRef().equals(subModelId) && 
					p.isSetSBaseRef() && p.getSBaseRef().isSetPortRef()) {
				Port subPort = subBioModel.getSBMLCompModel().getListOfPorts().get(p.getSBaseRef().getPortRef());
				if (subPort!=null) {
					if (subPort.isSetIdRef()) {
						p.setIdRef(subModelId + "__" + subPort.getIdRef());
					} else if (subPort.isSetMetaIdRef()) {
						p.unsetIdRef();
						p.setMetaIdRef(subModelId + "__" + subPort.getMetaIdRef());
					}
					p.unsetSBaseRef();
				} else {
					bioModel.getSBMLCompModel().removePort(p);
				}
			} 
		}
		
		performDeletions(bioModel,subBioModel,replacementModelId);
		
		// Rename compartments
		for (int i = 0; i < subModel.getCompartmentCount(); i++) {
			Compartment c = subModel.getCompartment(i);
			String newName = subModelId + "___" + c.getId();
			for (int j = 0; j < model.getCompartmentCount(); j++) {
				CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(model.getCompartment(j));
				newName = prepareReplacement(newName,subBioModel,subModelId,replacementModelId,sbmlSBase,c.getId(),
						model.getCompartment(j).getId());
			}
			updateVarId(false, c.getId(), newName, subBioModel);
			compartments.remove(c.getId());
			if (subModel.getCompartment(newName)==null) c.setId(newName);
			else c.setId("skip"+"___"+c.getId());
			if (c.isSetMetaId()) SBMLutilities.setMetaId(c, subModelId + "___" + c.getMetaId());
		}
		for (int i = 0; i < subModel.getCompartmentCount(); i++) {
			Compartment c = subModel.getCompartment(i).clone();
			if (c.getId().startsWith("skip___")) continue;
			if (c.getId().startsWith("_" + subModelId + "__")) {
				updateVarId(false, c.getId(), c.getId().substring(3 + subModelId.length()), subBioModel);
				compartments.remove(c.getId());
				c.setId(c.getId().substring(3 + subModelId.length()));
			} else if (c.getId().startsWith("__" + subModelId + "__")) {
				String topId = c.getId().substring(4 + subModelId.length());
				updateVarId(false, c.getId(), topId, subBioModel);
				compartments.remove(c.getId());
				c.setId(topId);
				CompSBasePlugin SbmlSBase = SBMLutilities.getCompSBasePlugin(model.getCompartment(topId));
				ArrayList<ReplacedElement> replacements = new ArrayList<ReplacedElement>();
				for (int j = 0; j < SbmlSBase.getListOfReplacedElements().size(); j++) {
					replacements.add(SbmlSBase.getListOfReplacedElements().get(j));
				}
				model.removeCompartment(topId);
				model.addCompartment(c);
				SbmlSBase = SBMLutilities.getCompSBasePlugin(model.getCompartment(c.getId()));
				for (ReplacedElement r : replacements) {
					SbmlSBase.addReplacedElement(r.clone());
				}
			} else {
				updateVarId(false, c.getId(), c.getId().replace("___", "__"), subBioModel);
				c.setId(c.getId().replace("___", "__"));
				if (c.isSetMetaId()) SBMLutilities.setMetaId(c, c.getMetaId().replace("___","__"));
				if (model.getCompartment(c.getId())==null) {
					model.addCompartment(c);
					CompSBasePlugin SbmlSBase = SBMLutilities.getCompSBasePlugin(model.getCompartment(c.getId()));
					SbmlSBase.unsetListOfReplacedElements();
					SbmlSBase.unsetReplacedBy();
					if (!compartments.containsKey(c.getId())) {
						Properties prop = new Properties();
						prop.put(GlobalConstants.RNAP_STRING,RNAPamount);
						compartments.put(c.getId(),prop);
					}
				} else {
					// TOOD: species not unique
				}
			}
		}
		// Rename species 
		for (int i = 0; i < subModel.getSpeciesCount(); i++) {
			Species spec = subModel.getSpecies(i);
			String newName = subModelId + "___" + spec.getId();
			for (int j = 0; j < model.getSpeciesCount(); j++) {
				CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(model.getSpecies(j));
				newName = prepareReplacement(newName,subBioModel,subModelId,replacementModelId,sbmlSBase,spec.getId(),
						model.getSpecies(j).getId());
			}
			updateVarId(true, spec.getId(), newName, subBioModel);
			if (subModel.getSpecies(newName)==null) spec.setId(newName);
			else spec.setId("skip"+"___"+spec.getId());
			if (spec.isSetMetaId()) SBMLutilities.setMetaId(spec, subModelId + "___" + spec.getMetaId());
		}
		for (int i = 0; i < subModel.getSpeciesCount(); i++) {
			Species spec = subModel.getSpecies(i).clone();
			if (spec.getId().startsWith("skip___")) continue;
			if (spec.getId().startsWith("_" + subModelId + "__")) {
				updateVarId(true, spec.getId(), spec.getId().substring(3 + subModelId.length()), subBioModel);
				spec.setId(spec.getId().substring(3 + subModelId.length()));
			} else if (spec.getId().startsWith("__" + subModelId + "__")) {
				String topId = spec.getId().substring(4 + subModelId.length());
				updateVarId(true, spec.getId(), topId, subBioModel);
				spec.setId(topId);
				CompSBasePlugin SbmlSBase = SBMLutilities.getCompSBasePlugin(model.getSpecies(topId));
				ArrayList<ReplacedElement> replacements = new ArrayList<ReplacedElement>();
				for (int j = 0; j < SbmlSBase.getListOfReplacedElements().size(); j++) {
					replacements.add(SbmlSBase.getListOfReplacedElements().get(j));
				}
				model.removeSpecies(topId);
				model.addSpecies(spec);
				SbmlSBase = SBMLutilities.getCompSBasePlugin(spec);
				for (ReplacedElement r : replacements) {
					SbmlSBase.addReplacedElement(r.clone());
				}
			} else {
				updateVarId(true, spec.getId(), spec.getId().replace("___", "__"), subBioModel);
				spec.setId(spec.getId().replace("___","__"));
				if (spec.isSetMetaId()) SBMLutilities.setMetaId(spec, spec.getMetaId().replace("___","__"));
				if (model.getSpecies(spec.getId())==null) {
					model.addSpecies(spec);
					CompSBasePlugin SbmlSBase = SBMLutilities.getCompSBasePlugin(model.getSpecies(spec.getId()));
					SbmlSBase.unsetListOfReplacedElements();
					SbmlSBase.unsetReplacedBy();
				} else {
					// TOOD: species not unique
				}
			}
		}
		// Rename parameters
		for (int i = 0; i < subModel.getParameterCount(); i++) {
			Parameter p = subModel.getParameter(i);
			String newName = subModelId + "___" + p.getId();
			for (int j = 0; j < model.getParameterCount(); j++) {
				CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(model.getParameter(j));
				newName = prepareReplacement(newName,subBioModel,subModelId,replacementModelId,sbmlSBase,p.getId(),
						model.getParameter(j).getId());
			}
			updateVarId(false, p.getId(), newName, subBioModel);
			if (subModel.getParameter(newName)==null) p.setId(newName);
			else p.setId("skip"+"___"+p.getId());
			if (p.isSetMetaId()) SBMLutilities.setMetaId(p, subModelId + "___" + p.getMetaId());
		}
		for (int i = 0; i < subModel.getParameterCount(); i++) {
			Parameter p = subModel.getParameter(i).clone();
			if (p.getId().startsWith("skip___")) continue;
			if (p.getId().startsWith("_" + subModelId + "__")) {
				updateVarId(false, p.getId(), p.getId().substring(3 + subModelId.length()), subBioModel);
				p.setId(p.getId().substring(3 + subModelId.length()));
			} else if (p.getId().startsWith("__" + subModelId + "__")) {
				String topId = p.getId().substring(4 + subModelId.length());
				updateVarId(false, p.getId(), topId, subBioModel);
				p.setId(topId);
				CompSBasePlugin SbmlSBase = SBMLutilities.getCompSBasePlugin(model.getParameter(topId));
				ArrayList<ReplacedElement> replacements = new ArrayList<ReplacedElement>();
				for (int j = 0; j < SbmlSBase.getListOfReplacedElements().size(); j++) {
					replacements.add(SbmlSBase.getListOfReplacedElements().get(j));
				}
				model.removeParameter(topId);
				model.addParameter(p);
				SbmlSBase = SBMLutilities.getCompSBasePlugin(p);
				for (ReplacedElement r : replacements) {
					SbmlSBase.addReplacedElement(r.clone());
				}
			} else {
				updateVarId(false, p.getId(), p.getId().replace("___", "__"), subBioModel);
				p.setId(p.getId().replace("___","__"));
				if (p.isSetMetaId()) SBMLutilities.setMetaId(p, p.getMetaId().replace("___","__"));
				if (model.getParameter(p.getId())==null) {
					model.addParameter(p);
					CompSBasePlugin SbmlSBase = SBMLutilities.getCompSBasePlugin(p);
					SbmlSBase.unsetListOfReplacedElements();
					SbmlSBase.unsetReplacedBy();
				} else {
					// TOOD: species not unique
				}
			}
		}
		// Rename reactions
		for (int i = 0; i < subModel.getReactionCount(); i++) {
			Reaction r = subModel.getReaction(i);
			if (r.getId().contains("MembraneDiffusion")) continue;
			String newName = subModelId + "___" + r.getId();
			for (int j = 0; j < model.getReactionCount(); j++) {
				CompSBasePlugin sbmlSBase = SBMLutilities.getCompSBasePlugin(model.getReaction(j));
				newName = prepareReplacement(newName,subBioModel,subModelId,replacementModelId,sbmlSBase,r.getId(),
						model.getReaction(j).getId());
			}
			updateVarId(false, r.getId(), newName, subBioModel);
			if (subModel.getReaction(newName)==null) r.setId(newName);
			else r.setId("skip"+"___"+r.getId());
			if (r.isSetMetaId()) SBMLutilities.setMetaId(r, subModelId + "___" + r.getMetaId());
			if (!r.isSetKineticLaw()) continue;
			for (int j = 0; j < r.getKineticLaw().getLocalParameterCount(); j++) {
				LocalParameter l = r.getKineticLaw().getLocalParameter(j);
				if (l.isSetMetaId()) SBMLutilities.setMetaId(l, subModelId + "___" + l.getMetaId());
			}
			for (int j = 0; j < r.getProductCount(); j++) {
				SimpleSpeciesReference product  = r.getProduct(j);
				if (product.isSetMetaId()) SBMLutilities.setMetaId(product, subModelId + "___" + product.getMetaId());
			}
			for (int j = 0; j < r.getReactantCount(); j++) {
				SimpleSpeciesReference reactant  = r.getReactant(j);
				if (reactant.isSetMetaId()) SBMLutilities.setMetaId(reactant, subModelId + "___" + reactant.getMetaId());
			}
			for (int j = 0; j < r.getModifierCount(); j++) {
				SimpleSpeciesReference modifier  = r.getModifier(j);
				if (modifier.isSetMetaId()) SBMLutilities.setMetaId(modifier, subModelId + "___" + modifier.getMetaId());
			}
		}
		for (int i = 0; i < subModel.getReactionCount(); i++) {
			Reaction r = subModel.getReaction(i).clone();
			if (r.getId().startsWith("skip___")) continue;
			if (r.getId().startsWith("_" + subModelId + "__")) {
				updateVarId(false, r.getId(), r.getId().substring(3 + subModelId.length()), subBioModel);
				r.setId(r.getId().substring(3 + subModelId.length()));
			} else if (r.getId().startsWith("__" + subModelId + "__")) {
				String topId = r.getId().substring(4 + subModelId.length());
				updateVarId(false, r.getId(), topId, subBioModel);
				r.setId(topId);
				CompSBasePlugin SbmlSBase = SBMLutilities.getCompSBasePlugin(model.getReaction(topId));
				ArrayList<ReplacedElement> replacements = new ArrayList<ReplacedElement>();
				for (int j = 0; j < SbmlSBase.getListOfReplacedElements().size(); j++) {
					replacements.add(SbmlSBase.getListOfReplacedElements().get(j));
				}
				model.removeReaction(topId);
				// TODO: need to remove flux bounds here?
				model.addReaction(r);
				SbmlSBase = SBMLutilities.getCompSBasePlugin(model.getReaction(r.getId()));
				for (ReplacedElement repl : replacements) {
					SbmlSBase.addReplacedElement(repl.clone());
				}
			} else {
				updateVarId(false, r.getId(), r.getId().replace("___", "__"), subBioModel);
				r.setId(r.getId().replace("___","__"));
				if (r.isSetMetaId()) SBMLutilities.setMetaId(r, r.getMetaId().replace("___","__"));
				for (int j = 0; j < r.getKineticLaw().getLocalParameterCount(); j++) {
					LocalParameter l = r.getKineticLaw().getLocalParameter(j);
					if (l.isSetMetaId()) SBMLutilities.setMetaId(l, l.getMetaId().replace("___", "__"));
				}
				if (model.getReaction(r.getId())==null) {
					model.addReaction(r);
					CompSBasePlugin SbmlSBase = SBMLutilities.getCompSBasePlugin(r);
					SbmlSBase.unsetListOfReplacedElements();
					SbmlSBase.unsetReplacedBy();

				} else {
					// TOOD: reaction not unique
				}
			}
		}
		// TODO: Species references?

		// TODO: no ___ trick, ok? Replacements?
		for (int i = 0; i < subModel.getInitialAssignmentCount(); i++) {
			InitialAssignment init = subModel.getListOfInitialAssignments().get(i).clone();
			if (init.isSetMetaId()) {
				SBMLutilities.setMetaId(init, subModelId + "__" + init.getMetaId());
			}
			model.addInitialAssignment(init);
		}

		// TODO: no ___ trick, ok? Replacements?
		for (int i = 0; i < subModel.getRuleCount(); i++) {
			org.sbml.jsbml.Rule r = subModel.getRule(i).clone();
			if (r.isSetMetaId()) {
				SBMLutilities.setMetaId(r, subModelId + "__" + r.getMetaId());
			}
			model.addRule(r);
		}
		
		// TODO: no ___ trick, ok? Replacements?
		for (int i = 0; i < subModel.getConstraintCount(); i++) {
			Constraint constraint = subModel.getListOfConstraints().get(i).clone();
			String newName = subModelId + "__" + constraint.getMetaId();
			SBMLutilities.setMetaId(constraint, newName);
			for (int j = 0; j < model.getConstraintCount(); j++) {
				if (model.getConstraint(j).getMetaId().equals(constraint.getMetaId())) {
					Constraint c = model.getConstraint(j);
					try {
						if (!c.getMessageString().equals(constraint.getMessageString())) {
							return null;
						}
					} catch (XMLStreamException e) {
						e.printStackTrace();
					}
					if (c.getMath() != constraint.getMath()) {
						return null;
					}
				}
			}
			model.addConstraint(constraint);
		}
		
		// TODO: no ___ trick, ok?  Replacements?
		for (int i = 0; i < subModel.getEventCount(); i++) {
			org.sbml.jsbml.Event event = subModel.getListOfEvents().get(i).clone();
			String newName = subModelId + "__" + event.getId();
			updateVarId(false, event.getId(), newName, subBioModel);
			event.setId(newName);
			if (event.isSetMetaId()) {
				SBMLutilities.setMetaId(event, subModelId + "__" + event.getMetaId());
			}
			model.addEvent(event);
		}
		
		// TODO: Replacements?  
		for (int i = 0; i < subModel.getUnitDefinitionCount(); i++) {
			UnitDefinition u = subModel.getUnitDefinition(i).clone();
			String newName = subModelId + "__" + u.getId();
			boolean add = true;
			for (int j = 0; j < model.getUnitDefinitionCount(); j++) {
				if (model.getUnitDefinition(j).getId().equals(u.getId())) {
					if (UnitDefinition.areIdentical(model.getUnitDefinition(j), u)) {
						add = false;
					}
				}
			}
			if (add) {
				String oldName = u.getId();
				u.setId(newName);
				if (u.isSetMetaId()) {
					SBMLutilities.setMetaId(u, subModelId + "__" + u.getMetaId());
				}
				if (model.getUnitDefinition(u.getId())==null) {
					model.addUnitDefinition(u);
				} 
				biomodel.gui.sbmlcore.Units.updateUnitId(model, oldName, newName);
			}
		}
		// TODO: functions not flattened right, no replacements
		for (int i = 0; i < subModel.getFunctionDefinitionCount(); i++) {
			FunctionDefinition f = subModel.getFunctionDefinition(i).clone();
			boolean add = true;
			for (int j = 0; j < model.getFunctionDefinitionCount(); j++) {
				if (model.getFunctionDefinition(j).getId().equals(f.getId())) {
					add = false;
				}
			}
			if (add) {
				if (f.isSetMetaId()) {
					SBMLutilities.setMetaId(f, subModelId + "__" + f.getMetaId());
				}
				model.addFunctionDefinition(f);
			}
		}
		
		return bioModel;
	}

	// TODO: This is an ugly hack of a method.  Should be rewritten.
	private static String updateFormulaVar(String s, String origVar, String newVar) {
		s = " " + s + " ";
		String olds;
		s = s.replace("*"," * ");
		s = s.replace("+"," + ");
		s = s.replace("/"," / ");
		s = s.replace("-"," - ");
		s = s.replace("%"," % ");
		s = s.replace("&&"," && ");
		s = s.replace("||"," || ");
		s = s.replace(">", " > ").replace("> =",">=");
		s = s.replace("<", " < ").replace("< =","<=");
		s = s.replace("=", " = ").replace("> =",">=").replace("< =", "<=");
		s = s.replace("=  =", "==");
		s = s.replace("! =", "!=");
		// TODO: Are these needed to be handled?  Or are arrays gone by this point?
		//formula.split("\\[|\\]|\\{|\\}")
		do { 
			olds = s;
			s = s.replace("," + origVar + ",", "," + newVar + ",");
			s = s.replace(" " + origVar + " ", " " + newVar + " ");
			s = s.replace("," + origVar + ")", "," + newVar + ")");
			s = s.replace(" " + origVar + "(", " " + newVar + "(");
			s = s.replace("(" + origVar + ")", "(" + newVar + ")");
			s = s.replace("(" + origVar + " ", "(" + newVar + " ");
			s = s.replace("(" + origVar + ",", "(" + newVar + ",");
			s = s.replace(" " + origVar + ")", " " + newVar + ")");
			s = s.replace(" " + origVar + "^", " " + newVar + "^");
			s = s.replace("^" + origVar + ")", "^" + newVar + ")");
		} while (s != olds);
		return s.trim();
	}

	private static ASTNode updateMathVar(ASTNode math, String origVar, String newVar) {
		String s = updateFormulaVar(SBMLutilities.myFormulaToString(math), origVar, newVar);
		return SBMLutilities.myParseFormula(s);
	}

	private static void updateVarId(boolean isSpecies, String origId, String newId, BioModel bioModel) {
		SBMLDocument document = bioModel.getSBMLDocument();
		if (origId.equals(newId))
			return;
		Model model = document.getModel();
		for (int i = 0; i < bioModel.getSBMLCompModel().getListOfPorts().size(); i++) {
			Port port = bioModel.getSBMLCompModel().getListOfPorts().get(i);
			if (port.isSetIdRef() && port.getIdRef().equals(origId)) {
				port.setIdRef(newId);
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			Species species = model.getSpecies(i);
			if (species.getCompartment().equals(origId)) {
				species.setCompartment(newId);
			}
		}
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getListOfReactions().get(i);
			if (!reaction.isSetCompartment() || reaction.getCompartment().equals(origId)) {
				reaction.setCompartment(newId);
			}
			for (int j = 0; j < reaction.getProductCount(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getProduct(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
				}
			}
			if (isSpecies) {
				for (int j = 0; j < reaction.getModifierCount(); j++) {
					if (reaction.getModifier(j).isSetSpecies()) {
						ModifierSpeciesReference specRef = reaction.getModifier(j);
						if (origId.equals(specRef.getSpecies())) {
							specRef.setSpecies(newId);
						}
					}
				}
			}
			for (int j = 0; j < reaction.getReactantCount(); j++) {
				if (reaction.getReactant(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getReactant(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
				}
			}
			if (reaction.isSetKineticLaw()) {
				reaction.getKineticLaw().setMath(
						updateMathVar(reaction.getKineticLaw().getMath(), origId, newId));
			}
		}
		if (model.getInitialAssignmentCount() > 0) {
			for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
				InitialAssignment init = model.getListOfInitialAssignments().get(i);
				if (origId.equals(init.getVariable())) {
					init.setVariable(newId);
				}
				init.setMath(updateMathVar(init.getMath(), origId, newId));
			}
		}
		if (model.getRuleCount() > 0) {
			for (int i = 0; i < model.getRuleCount(); i++) {
				Rule rule = model.getListOfRules().get(i);
				if (SBMLutilities.isSetVariable(rule) && origId.equals(SBMLutilities.getVariable(rule))) {
					SBMLutilities.setVariable(rule, newId);
				}
				rule.setMath(updateMathVar(rule.getMath(), origId, newId));
			}
		}
		if (model.getConstraintCount() > 0) {
			for (int i = 0; i < model.getConstraintCount(); i++) {
				Constraint constraint = model.getListOfConstraints().get(i);
				constraint.setMath(updateMathVar(constraint.getMath(), origId, newId));
			}
		}
		if (model.getEventCount() > 0) {
			for (int i = 0; i < model.getEventCount(); i++) {
				org.sbml.jsbml.Event event = model.getListOfEvents()
						.get(i);
				if (event.isSetTrigger()) {
					event.getTrigger().setMath(
							updateMathVar(event.getTrigger().getMath(), origId, newId));
				}
				if (event.isSetDelay()) {
					event.getDelay().setMath(
							updateMathVar(event.getDelay().getMath(), origId, newId));
				}
				for (int j = 0; j < event.getEventAssignmentCount(); j++) {
					EventAssignment ea = event.getListOfEventAssignments().get(j);
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

	public String getFilename() {
		return filename;
	}

	public String getPath() {
		return path;
	}
	
	public String getSeparator() {
		return separator;
	}
	
	//UNDO-REDO METHODS
	
	public void makeUndoPoint() {
		StringBuffer up = saveToBuffer();
		undoManager.makeUndoPoint(up);
	}

	public void undo() {
		StringBuffer p = (StringBuffer) undoManager.undo();
		
		if (p != null)
			this.loadSBMLFromBuffer(p);
	}

	public void redo() {
		StringBuffer p = (StringBuffer) undoManager.redo();
		if (p != null)
			this.loadSBMLFromBuffer(p);
	}
	
	
	//GRID

	public Grid getGrid() {
		return grid;
	}
	
	/*
	public void setGrid(Grid g) {
		grid = g;
	}	
	*/
	
	public void buildGrid(int rows, int cols) {
		
		grid.createGrid(rows, cols, this, null);
	}
	
	public boolean isGridEnabled() {
		if ((getGrid().getNumRows() > 0) && (getGrid().getNumCols() > 0)) return true;
		return false;
	}
	
	/**
	 * looks in the file to see if it is a gridded file
	 * 
	 * @param filename
	 * @return
	 */
	public boolean getGridEnabledFromFile(String filename) {
		
		BioModel subModel = new BioModel(path);
		subModel.load(filename);
		if ((subModel.getGrid().getNumRows() > 0) || (subModel.getGrid().getNumCols() > 0)) return true;
		return false;

		/*
		StringBuffer data = new StringBuffer();
		
		if (filename == null) return true;

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
		
		//grid=(0,0) means there's no grid
		if (data.toString().contains("grid=(0,0)") == false)
			return true;
		else
			return false;
			*/
	}
	
	
	public ASTNode addBooleans(String formula) {
		formula = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(formula));
		for (int j = 0; j < sbml.getModel().getParameterCount(); j++) {
			Parameter parameter = sbml.getModel().getParameter(j);
			if (SBMLutilities.isBoolean(parameter)) {
				formula = SBMLutilities.addBoolean(formula,parameter.getId());
			}
		}		
		return SBMLutilities.myParseFormula(formula);
	}
	
	public ASTNode addBooleanAssign(String formula) {
		formula = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(formula));
		for (int j = 0; j < sbml.getModel().getParameterCount(); j++) {
			Parameter parameter = sbml.getModel().getParameter(j);
			if (SBMLutilities.isBoolean(parameter)) {
				formula = SBMLutilities.addBoolean(formula,parameter.getId());
			}
		}		
		formula = "piecewise(1," + formula + ",0)";
		return SBMLutilities.myParseFormula(formula);
	}
	
	public String removeBooleans(ASTNode math) {
		for (int j = 0; j < sbml.getModel().getParameterCount(); j++) {
			Parameter parameter = sbml.getModel().getParameter(j);
			if (SBMLutilities.isBoolean(parameter)) {
				math = SBMLutilities.removeBoolean(math,parameter.getId());
			}
		}
		return SBMLutilities.myFormulaToString(math);
	}
	

	public String removeBooleanAssign(ASTNode math) {
		if (math.getType() == ASTNode.Type.FUNCTION_PIECEWISE && math.getChildCount() > 1) {
			ASTNode result = math.getChild(1);
			for (int j = 0; j < sbml.getModel().getParameterCount(); j++) {
				Parameter parameter = sbml.getModel().getParameter(j);
				if (SBMLutilities.isBoolean(parameter)) {
					result = SBMLutilities.removeBoolean(result,parameter.getId());
				}
			}
			return SBMLutilities.myFormulaToString(result);
		}
		return SBMLutilities.myFormulaToString(math);
	}
	
	public int getMetaIDIndex() {
		return metaIDIndex;
	}
	
	public void setMetaIDIndex(int metaIDIndex) {
		this.metaIDIndex = metaIDIndex;
	}
	
	private SBMLDocument sbml = null;
	
	private LayoutModelPlugin sbmlLayout = null;
	
	private CompSBMLDocumentPlugin sbmlComp = null;
	
	private CompModelPlugin sbmlCompModel = null;
	
	private FBCModelPlugin sbmlFBC = null;
	
	private MySpecies speciesPanel = null;
	
	private Compartments compartmentPanel = null;
	
	private Reactions reactionPanel = null;
	
	private Rules rulePanel = null;
	
	private Constraints constraintPanel = null;
	
	private Events eventPanel = null;
	
	private Parameters parameterPanel = null;
	
	private Grid grid = null;
	
	private HashMap<String,String> defaultParameters = null;
	
	private int creatingCompartmentID = -1;
	private int creatingVariableID = -1;
	private int creatingPlaceID = -1;
	private int creatingPromoterID = -1;
	private int creatingSpeciesID = -1;
	private int metaIDIndex = 1;
	private int creatingReactionID = -1;
	
	private String path;

	private String sbmlFile = "";
	
	//private GCM2SBML gcm2sbml = null;

	private HashMap<String, Properties> compartments;
	
	//private int elementSBOLCount;
	//private boolean modelSBOLAnnotationFlag;
	private String[] sbolDescriptors;
	private String sbolSaveFilePath;
	private int placeHolderIndex;
}
