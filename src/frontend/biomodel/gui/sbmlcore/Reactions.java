package frontend.biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.fbc.FBCReactionPlugin;

import dataModels.biomodel.annotation.AnnotationUtility;
import dataModels.biomodel.annotation.SBOLAnnotation;
import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.util.GlobalConstants;
import dataModels.util.exceptions.BioSimException;
import frontend.biomodel.gui.sbol.SBOLField2;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.biomodel.gui.schematic.Utils;
import frontend.main.Gui;
import frontend.main.util.SpringUtilities;
import frontend.main.util.Utility;

/**
 * This is a class for creating SBML parameters
 * 
 * @author Chris Myers
 * 
 */
public class Reactions extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JComboBox stoiciLabel;

	private JComboBox reactionSBO;

	private JComboBox reactionComp; // compartment combo box

	private JList reactions; // JList of reactions

	private String[] reacts; // array of reactions

	/*
	 * reactions buttons
	 */
	private JButton addReac, removeReac, editReac;

	private JList reacParameters; // JList of reaction parameters

	private String[] reacParams; // array of reaction parameters

	/*
	 * reaction parameters buttons
	 */
	private JButton reacAddParam, reacRemoveParam, reacEditParam;

	private ArrayList<LocalParameter> changedParameters; // ArrayList of parameters

	/*
	 * reaction parameters text fields
	 */
	private JTextField reacParamID, reacParamValue, reacParamName;

	private JComboBox reacParamUnits;

	private JTextField reacID, reacName; // reaction name and id text

	private JCheckBox onPort, paramOnPort;

	// fields

	private JComboBox reacReverse, reacFast; // reaction reversible, fast combo

	// boxes

	/*
	 * reactant buttons
	 */
	private JButton addReactant, removeReactant, editReactant;

	private JList reactants; // JList for reactants

	private String[] reacta; // array for reactants

	private JComboBox reactantConstant;
	/*
	 * ArrayList of reactants
	 */
	private ArrayList<SpeciesReference> changedReactants;

	/*
	 * product buttons
	 */
	private JButton addProduct, removeProduct, editProduct;

	private JList products; // JList for products

	private String[] proda; // array for products

	private JComboBox productConstant;
	/*
	 * ArrayList of products
	 */
	private ArrayList<SpeciesReference> changedProducts;

	/*
	 * modifier buttons
	 */
	private JButton addModifier, removeModifier, editModifier;

	private JList modifiers; // JList for modifiers

	private String[] modifierArray; // array for modifiers

	/*
	 * ArrayList of modifiers
	 */
	private ArrayList<ModifierSpeciesReference> changedModifiers;

	private JComboBox productSpecies; // ComboBox for product editing

	private JComboBox modifierSpecies; // ComboBox for modifier editing

	private JTextField productId;

	private JTextField productName;

	private JTextField modifierName;

	private JTextField productStoichiometry; // text field for editing products

	private JComboBox reactantSpecies; // ComboBox for reactant editing

	private JTextField RiIndex;

	private JTextField PiIndex;
	
	private JTextField MiIndex, modifierId;
	
	private JTextField CiIndex;
	
	/*
	 * text field for editing reactants
	 */
	private JTextField reactantId;

	private JTextField reactantName;

	private SBOLField2 sbolField;

	private JTextField reactantStoichiometry;

	private JTextArea kineticLaw; // text area for editing kinetic law

	private ArrayList<String> thisReactionParams;

	private JButton useMassAction, clearKineticLaw;

	private BioModel bioModel;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;

	private String selectedReaction;

	private ModelEditor modelEditor;

	private Reaction complex = null;

	private Reaction production = null;

	private JComboBox SBOTerms = null;

	private JTextField repCooperativity, actCooperativity, repBinding, actBinding;
	
	public Reactions(BioModel gcm, Boolean paramsOnly, ArrayList<String> getParams, String file, ArrayList<String> parameterChanges, 
			ModelEditor gcmEditor) {
		super(new BorderLayout());
		this.bioModel = gcm;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
		this.modelEditor = gcmEditor;
		Model model = gcm.getSBMLDocument().getModel();
		JPanel addReacs = new JPanel();
		addReac = new JButton("Add Reaction");
		removeReac = new JButton("Remove Reaction");
		editReac = new JButton("Edit Reaction");
		addReacs.add(addReac);
		addReacs.add(removeReac);
		addReacs.add(editReac);
		addReac.addActionListener(this);
		removeReac.addActionListener(this);
		editReac.addActionListener(this);
		if (paramsOnly) {
			addReac.setEnabled(false);
			removeReac.setEnabled(false);
		}
		JLabel reactionsLabel = new JLabel("List of Reactions:");
		reactions = new JList();
		reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setViewportView(reactions);
		ListOf<Reaction> listOfReactions = model.getListOfReactions();
		reacts = new String[model.getReactionCount()];
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = listOfReactions.get(i);
			reacts[i] = reaction.getId();
			reacts[i] += SBMLutilities.getDimensionString(reaction);
			if (paramsOnly && reaction.getKineticLaw()!=null) {
				ListOf<LocalParameter> params = reaction.getKineticLaw().getListOfLocalParameters();
				for (int j = 0; j < reaction.getKineticLaw().getLocalParameterCount(); j++) {
					LocalParameter paramet = (params.get(j));
					for (int k = 0; k < getParams.size(); k++) {
						if (getParams.get(k).split(" ")[0].equals(reaction.getId() + "/" + paramet.getId())) {
							parameterChanges.add(getParams.get(k));
							String[] splits = getParams.get(k).split(" ");
							if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
								String value = splits[splits.length - 1];
								paramet.setValue(Double.parseDouble(value));
							}
							else if (splits[splits.length - 2].equals("Sweep")) {
								String value = splits[splits.length - 1];
								paramet.setValue(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							}
							if (!reacts[i].contains("Modified")) {
								reacts[i] += " Modified";
							}
						}
					}
				}
			}
		}
		dataModels.biomodel.util.Utility.sort(reacts);
		reactions.setListData(reacts);
		reactions.setSelectedIndex(0);
		reactions.addMouseListener(this);
		this.add(reactionsLabel, "North");
		this.add(scroll2, "Center");
		this.add(addReacs, "South");
	}

	private static boolean checkFluxBound(SBMLDocument document,String boundStr, String attribute,
			String[] dimensionIds,String[] idDims) 
	{
		if (boundStr.contains("[")) {
			String id = boundStr.substring(0,boundStr.indexOf("["));
			if (document.getModel().getParameter(id)==null) return false;
			String index = boundStr.substring(boundStr.indexOf("["));
			SBase variable = SBMLutilities.getElementBySId(document, id);
			String[] dex = Utils.checkIndices(index, variable, document, dimensionIds, attribute, 
					idDims, null, null);
			if (dex==null) return false;
		} else {
			return (document.getModel().getParameter(boundStr)!=null);
		}
		return true;
	}
	
	private boolean setLowerFluxBound(Reaction reaction,FBCReactionPlugin rBounds,String boundStr,
			String[] dimensionIds,String[] idDims) 
	{
		if (boundStr.contains("[")) {
			String id = boundStr.substring(0,boundStr.indexOf("["));
			rBounds.setLowerFluxBound(id);
			String index = boundStr.substring(boundStr.indexOf("["));
			SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), id);
			String[] dex = Utils.checkIndices(index, variable, bioModel.getSBMLDocument(), dimensionIds, "fbc:lowerFluxBound", 
					idDims, null, null);
			if (dex==null) return false;
			SBMLutilities.addIndices(reaction, "fbc:lowerFluxBound", dex, 1);
		} else {
			rBounds.setLowerFluxBound(boundStr);
		}
		return true;
	}
	
	private boolean setUpperFluxBound(Reaction reaction,FBCReactionPlugin rBounds,String boundStr,
			String[] dimensionIds,String[] idDims) 
	{
		if (boundStr.contains("[")) {
			String id = boundStr.substring(0,boundStr.indexOf("["));
			rBounds.setUpperFluxBound(id);
			String index = boundStr.substring(boundStr.indexOf("["));
			SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), id);
			String[] dex = Utils.checkIndices(index, variable, bioModel.getSBMLDocument(), dimensionIds, 
					"fbc:upperFluxBound", idDims, null, null);
			if (dex==null) return false;
			SBMLutilities.addIndices(reaction, "fbc:upperFluxBound", dex, 1);
		} else {
			rBounds.setUpperFluxBound(boundStr);
		}
		return true;
	}
	
	private boolean createReactionFluxBounds(String reactionId,String[] dimID,String[] dimensionIds) {
		Reaction r = bioModel.getSBMLDocument().getModel().getReaction(reactionId);
		FBCReactionPlugin rBounds = SBMLutilities.getFBCReactionPlugin(r);
		if(kineticLaw.getText().contains("<=")){
			String[] userInput = kineticLaw.getText().replaceAll("\\s","").split("<=");
			if (userInput.length==3) {
				if (!setLowerFluxBound(r,rBounds,userInput[0],dimensionIds,dimID)) return false;
				if (!setUpperFluxBound(r,rBounds,userInput[2],dimensionIds,dimID)) return false;
			} 
			else {
				if (userInput[0].startsWith(reactionId)) {
					if (!setUpperFluxBound(r,rBounds,userInput[1],dimensionIds,dimID)) return false;
				} else {
					if (!setLowerFluxBound(r,rBounds,userInput[0],dimensionIds,dimID)) return false;
				}
			}			
		} 
		else if(kineticLaw.getText().contains(">=")){
			String[] userInput = kineticLaw.getText().replaceAll("\\s","").split(">=");
			if (userInput.length==3) {
				if (!setLowerFluxBound(r,rBounds,userInput[2],dimensionIds,dimID)) return false;
				if (!setUpperFluxBound(r,rBounds,userInput[0],dimensionIds,dimID)) return false;
			} 
			else {
				if (userInput[0].startsWith(reactionId)) {
					if (!setLowerFluxBound(r,rBounds,userInput[1],dimensionIds,dimID)) return false;
				} else {
					if (!setUpperFluxBound(r,rBounds,userInput[0],dimensionIds,dimID)) return false;
				}
			}	
		}
		else{
			String[] userInput = kineticLaw.getText().replaceAll("\\s","").split("=");
			if(userInput[0].startsWith(reactionId)){
				if (!setLowerFluxBound(r,rBounds,userInput[1],dimensionIds,dimID)) return false;
				if (!setUpperFluxBound(r,rBounds,userInput[1],dimensionIds,dimID)) return false;
			} else {
				if (!setLowerFluxBound(r,rBounds,userInput[0],dimensionIds,dimID)) return false;
				if (!setUpperFluxBound(r,rBounds,userInput[0],dimensionIds,dimID)) return false;
			}
		}
		return true;
	}

	/**
	 * Creates a frame used to edit reactions or create new ones.
	 */
	public void reactionsEditor(BioModel bioModel, String option, String reactionId, boolean inSchematic) {
		/*
		 * if (option.equals("OK") && reactions.getSelectedIndex() == -1) {
		 * JOptionPane.showMessageDialog(Gui.frame, "No reaction selected.",
		 * "Must Select A Reaction", JOptionPane.ERROR_MESSAGE); return; }
		 */
		selectedReaction = reactionId;
		JLabel id = new JLabel("ID:");
		reacID = new JTextField(15);
		JLabel name = new JLabel("Name:");
		reacName = new JTextField(30);
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		onPort = new JCheckBox();
		JLabel sboTermLabel = new JLabel(GlobalConstants.SBOTERM);
		reactionSBO = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_INTERACTION));
		JLabel reactionCompLabel = new JLabel("Compartment:");
		ListOf<Compartment> listOfCompartments = bioModel.getSBMLDocument().getModel().getListOfCompartments();
		String[] addC = new String[bioModel.getSBMLDocument().getModel().getCompartmentCount()];
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getCompartmentCount(); i++) {
			addC[i] = listOfCompartments.get(i).getId();
		}
		reactionComp = new JComboBox(addC);
		reactionComp.addActionListener(this);
		JLabel reverse = new JLabel("Reversible:");
		String[] options = { "true", "false" };
		reacReverse = new JComboBox(options);
		reacReverse.setSelectedItem("false");
		JLabel fast = new JLabel("Fast:");
		reacFast = new JComboBox(options);
		reacFast.setSelectedItem("false");
		Reaction copyReact = null;
		JPanel param = new JPanel(new BorderLayout());
		JPanel addParams = new JPanel();
		reacAddParam = new JButton("Add Parameter");
		reacRemoveParam = new JButton("Remove Parameter");
		reacEditParam = new JButton("Edit Parameter");
		addParams.add(reacAddParam);
		addParams.add(reacRemoveParam);
		addParams.add(reacEditParam);
		reacAddParam.addActionListener(this);
		reacRemoveParam.addActionListener(this);
		reacEditParam.addActionListener(this);
		JLabel parametersLabel = new JLabel("List Of Local Parameters:");
		reacParameters = new JList();
		reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new java.awt.Dimension(260, 220));
		scroll.setPreferredSize(new java.awt.Dimension(276, 152));
		scroll.setViewportView(reacParameters);
		reacParams = new String[0];
		changedParameters = new ArrayList<LocalParameter>();
		thisReactionParams = new ArrayList<String>();
		if (option.equals("OK")) {
			Reaction reac = bioModel.getSBMLDocument().getModel().getReaction(reactionId);
			if (reac.getKineticLaw()!=null) {
				//reac.createKineticLaw();
				ListOf<LocalParameter> listOfParameters = reac.getKineticLaw().getListOfLocalParameters();
				reacParams = new String[reac.getKineticLaw().getLocalParameterCount()];
				for (int i = 0; i < reac.getKineticLaw().getLocalParameterCount(); i++) {
					/* TODO
					 * This code is a hack to get around a local parameter
					 * conversion bug in libsbml
					 */
					LocalParameter pp = listOfParameters.get(i);
					LocalParameter parameter = new LocalParameter(bioModel.getSBMLDocument().getLevel(), bioModel.getSBMLDocument().getVersion());
					parameter.setId(pp.getId());
					SBMLutilities.setMetaId(parameter, pp.getMetaId());
					parameter.setName(pp.getName());
					parameter.setValue(pp.getValue());
					parameter.setUnits(pp.getUnits());

					changedParameters.add(parameter);
					thisReactionParams.add(parameter.getId());
					String p;
					if (parameter.isSetUnits()) {
						p = parameter.getId() + " " + parameter.getValue() + " " + parameter.getUnits();
					}
					else {
						p = parameter.getId() + " " + parameter.getValue();
					}
					if (paramsOnly) {
						for (int j = 0; j < parameterChanges.size(); j++) {
							if (parameterChanges.get(j).split(" ")[0].equals(selectedReaction + "/"	+ parameter.getId())) {
								p = parameterChanges.get(j).split("/")[1];
							}
						}
					}
					reacParams[i] = p;
				}
			}
		}
		else {
			// Parameter p = new Parameter(BioSim.SBML_LEVEL,
			// BioSim.SBML_VERSION);
			LocalParameter p = new LocalParameter(bioModel.getSBMLDocument().getLevel(), bioModel.getSBMLDocument().getVersion());
			p.setId("kf");
			p.setValue(0.1);
			changedParameters.add(p);
			// p = new Parameter(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
			p = new LocalParameter(bioModel.getSBMLDocument().getLevel(), bioModel.getSBMLDocument().getVersion());
			p.setId("kr");
			p.setValue(1.0);
			changedParameters.add(p);
			reacParams = new String[2];
			reacParams[0] = "kf 0.1";
			reacParams[1] = "kr 1.0";
			thisReactionParams.add("kf");
			thisReactionParams.add("kr");
		}
		dataModels.biomodel.util.Utility.sort(reacParams);
		reacParameters.setListData(reacParams);
		reacParameters.setSelectedIndex(0);
		reacParameters.addMouseListener(this);
		param.add(parametersLabel, "North");
		param.add(scroll, "Center");
		param.add(addParams, "South");

		JPanel reactantsPanel = new JPanel(new BorderLayout());
		JPanel addReactants = new JPanel();
		addReactant = new JButton("Add Reactant");
		removeReactant = new JButton("Remove Reactant");
		editReactant = new JButton("Edit Reactant");
		addReactants.add(addReactant);
		addReactants.add(removeReactant);
		addReactants.add(editReactant);
		addReactant.addActionListener(this);
		removeReactant.addActionListener(this);
		editReactant.addActionListener(this);
		JLabel reactantsLabel = new JLabel("List Of Reactants:");
		reactants = new JList();
		reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new java.awt.Dimension(260, 220));
		scroll2.setPreferredSize(new java.awt.Dimension(276, 152));
		scroll2.setViewportView(reactants);
		reacta = new String[0];
		changedReactants = new ArrayList<SpeciesReference>();
		if (option.equals("OK")) {
			Reaction reac = bioModel.getSBMLDocument().getModel().getReaction(reactionId);
			ListOf<SpeciesReference> listOfReactants = reac.getListOfReactants();
			reacta = new String[reac.getReactantCount()];
			for (int i = 0; i < reac.getReactantCount(); i++) {
				SpeciesReference reactant = listOfReactants.get(i);
				changedReactants.add(reactant);
				if (reactant.isSetId()) {
					reacta[i] = reactant.getId() + SBMLutilities.getDimensionString(reactant);
					reacta[i] += " : " + reactant.getSpecies();
				} else {
					reacta[i] = reactant.getSpecies();
				}
				reacta[i] += SBMLutilities.getIndicesString(reactant, "species");
				reacta[i] += " " + reactant.getStoichiometry();
			}
		}
		dataModels.biomodel.util.Utility.sort(reacta);
		reactants.setListData(reacta);
		reactants.setSelectedIndex(0);
		reactants.addMouseListener(this);
		reactantsPanel.add(reactantsLabel, "North");
		reactantsPanel.add(scroll2, "Center");
		reactantsPanel.add(addReactants, "South");

		JPanel productsPanel = new JPanel(new BorderLayout());
		JPanel addProducts = new JPanel();
		addProduct = new JButton("Add Product");
		removeProduct = new JButton("Remove Product");
		editProduct = new JButton("Edit Product");
		addProducts.add(addProduct);
		addProducts.add(removeProduct);
		addProducts.add(editProduct);
		addProduct.addActionListener(this);
		removeProduct.addActionListener(this);
		editProduct.addActionListener(this);
		JLabel productsLabel = new JLabel("List Of Products:");
		products = new JList();
		products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll3 = new JScrollPane();
		scroll3.setMinimumSize(new java.awt.Dimension(260, 220));
		scroll3.setPreferredSize(new java.awt.Dimension(276, 152));
		scroll3.setViewportView(products);
		proda = new String[0];
		changedProducts = new ArrayList<SpeciesReference>();
		if (option.equals("OK")) {
			Reaction reac = bioModel.getSBMLDocument().getModel().getReaction(reactionId);
			ListOf<SpeciesReference> listOfProducts = reac.getListOfProducts();
			proda = new String[reac.getProductCount()];
			for (int i = 0; i < reac.getProductCount(); i++) {
				SpeciesReference product = listOfProducts.get(i);
				changedProducts.add(product);
				if (product.isSetId()) {
					proda[i] = product.getId() + SBMLutilities.getDimensionString(product);
					proda[i] += " : " + product.getSpecies();
				} else {
					proda[i] = product.getSpecies();
				}
				proda[i] += SBMLutilities.getIndicesString(product, "species");
				proda[i] += " " + product.getStoichiometry();
			}
		}
		dataModels.biomodel.util.Utility.sort(proda);
		products.setListData(proda);
		products.setSelectedIndex(0);
		products.addMouseListener(this);
		productsPanel.add(productsLabel, "North");
		productsPanel.add(scroll3, "Center");
		productsPanel.add(addProducts, "South");

		JPanel modifierPanel = new JPanel(new BorderLayout());
		JPanel addModifiers = new JPanel();
		addModifier = new JButton("Add Modifier");
		removeModifier = new JButton("Remove Modifier");
		editModifier = new JButton("Edit Modifier");
		addModifiers.add(addModifier);
		addModifiers.add(removeModifier);
		addModifiers.add(editModifier);
		addModifier.addActionListener(this);
		removeModifier.addActionListener(this);
		editModifier.addActionListener(this);
		JLabel modifiersLabel = new JLabel("List Of Modifiers:");
		modifiers = new JList();
		modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll5 = new JScrollPane();
		scroll5.setMinimumSize(new java.awt.Dimension(260, 220));
		scroll5.setPreferredSize(new java.awt.Dimension(276, 152));
		scroll5.setViewportView(modifiers);
		modifierArray = new String[0];
		changedModifiers = new ArrayList<ModifierSpeciesReference>();
		if (option.equals("OK")) {
			Reaction reac = bioModel.getSBMLDocument().getModel().getReaction(reactionId);
			ListOf<ModifierSpeciesReference> listOfModifiers = reac.getListOfModifiers();
			modifierArray = new String[reac.getModifierCount()];
			for (int i = 0; i < reac.getModifierCount(); i++) {
				ModifierSpeciesReference modifier = listOfModifiers.get(i);
				changedModifiers.add(modifier);
				if (modifier.isSetId()) {
					modifierArray[i] = modifier.getId() + SBMLutilities.getDimensionString(modifier);
					modifierArray[i] += " : " + modifier.getSpecies();
				} else {
					modifierArray[i] = modifier.getSpecies();
				}
				modifierArray[i] += SBMLutilities.getIndicesString(modifier, "species");
			}
		}
		dataModels.biomodel.util.Utility.sort(modifierArray);
		modifiers.setListData(modifierArray);
		modifiers.setSelectedIndex(0);
		modifiers.addMouseListener(this);
		modifierPanel.add(modifiersLabel, "North");
		modifierPanel.add(scroll5, "Center");
		modifierPanel.add(addModifiers, "South");

		JComboBox kineticFluxLabel = new JComboBox(new String[] {"Kinetic Law:","Flux Bounds:"});
		kineticLaw = new JTextArea();
		kineticLaw.setLineWrap(true);
		kineticLaw.setWrapStyleWord(true);
		useMassAction = new JButton("Use Mass Action");
		clearKineticLaw = new JButton("Clear");
		useMassAction.addActionListener(this);
		clearKineticLaw.addActionListener(this);
		JPanel kineticButtons = new JPanel();
		kineticButtons.add(useMassAction);
		kineticButtons.add(clearKineticLaw);
		JScrollPane scroll4 = new JScrollPane();
		scroll4.setMinimumSize(new java.awt.Dimension(100, 100));
		scroll4.setPreferredSize(new java.awt.Dimension(100, 100));
		scroll4.setViewportView(kineticLaw);
		if (option.equals("OK")) {
			if (bioModel.getSBMLDocument().getModel().getReaction(reactionId).getKineticLaw()!=null) {
				kineticFluxLabel.setSelectedIndex(0);
				kineticLaw.setText(bioModel.removeBooleans(bioModel.getSBMLDocument().getModel().getReaction(reactionId).getKineticLaw().getMath()));
			} else {
				kineticFluxLabel.setSelectedIndex(1);
				String fluxbounds = "";
				Reaction r = bioModel.getSBMLDocument().getModel().getReaction(reactionId);
				FBCReactionPlugin rBounds = SBMLutilities.getFBCReactionPlugin(r);
				if (rBounds != null) {
					fluxbounds = "";
					if (rBounds.isSetLowerFluxBound()) {
						fluxbounds += rBounds.getLowerFluxBound() + 
								SBMLutilities.getIndicesString(r, "fbc:lowerFluxBound") + "<="; 
					} 
					fluxbounds += reactionId;
					if (rBounds.isSetUpperFluxBound()) {
						fluxbounds += "<=" + rBounds.getUpperFluxBound() + 
								SBMLutilities.getIndicesString(r, "fbc:upperFluxBound");
					} 
				}
				kineticLaw.setText(fluxbounds);
			}
		}
		JPanel kineticPanel = new JPanel(new BorderLayout());
		kineticPanel.add(kineticFluxLabel, "North");
		kineticPanel.add(scroll4, "Center");
		kineticPanel.add(kineticButtons, "South");
		JPanel reactionPanel = new JPanel(new BorderLayout());
		JPanel reactionPanelNorth = new JPanel();
		reactionPanelNorth.setLayout(new GridLayout(3, 1));
		JPanel reactionPanelNorth1 = new JPanel();
		JPanel reactionPanelNorth3 = new JPanel();
		JPanel reactionPanelNorth4 = new JPanel();
		CiIndex = new JTextField(20);
		reactionPanelNorth1.add(id);
		reactionPanelNorth1.add(reacID);
		reactionPanelNorth1.add(name);
		reactionPanelNorth1.add(reacName);
		reactionPanelNorth1.add(onPortLabel);
		reactionPanelNorth1.add(onPort);

		reactionPanelNorth3.add(sboTermLabel);
		reactionPanelNorth3.add(reactionSBO);
		reactionPanelNorth3.add(reactionCompLabel);
		reactionPanelNorth3.add(reactionComp);
		reactionPanelNorth3.add(new JLabel("Compartment Indices:"));
		reactionPanelNorth3.add(CiIndex);
		
		reactionPanelNorth4.add(reverse);
		reactionPanelNorth4.add(reacReverse);
		reactionPanelNorth4.add(fast);
		reactionPanelNorth4.add(reacFast);

		// Parse out SBOL annotations and add to SBOL field
		if (!paramsOnly) {
			// Field for annotating reaction with SBOL DNA components
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = "";
			if (option.equals("OK")) {
				Reaction reac = bioModel.getSBMLDocument().getModel().getReaction(reactionId);
				sbolStrand = AnnotationUtility.parseSBOLAnnotation(reac, sbolURIs);
			}
			sbolField = new SBOLField2(sbolURIs, sbolStrand, GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 2, false);
			reactionPanelNorth4.add(sbolField);
		}
		reactionPanelNorth.add(reactionPanelNorth1);
		reactionPanelNorth.add(reactionPanelNorth3);
		reactionPanelNorth.add(reactionPanelNorth4);
		
		if (inSchematic) {
			reactionPanel.add(reactionPanelNorth, "North");
			reactionPanel.add(param, "Center");
			reactionPanel.add(kineticPanel, "South");

		}
		else {
			JPanel reactionPanelCentral = new JPanel(new GridLayout(1, 3));
			JPanel reactionPanelSouth = new JPanel(new GridLayout(1, 2));
			reactionPanelCentral.add(reactantsPanel);
			reactionPanelCentral.add(productsPanel);
			reactionPanelCentral.add(modifierPanel);
			reactionPanelSouth.add(param);
			reactionPanelSouth.add(kineticPanel);
			reactionPanel.add(reactionPanelNorth, "North");
			reactionPanel.add(reactionPanelCentral, "Center");
			reactionPanel.add(reactionPanelSouth, "South");
		}
		if (option.equals("OK")) {
			Reaction reac = bioModel.getSBMLDocument().getModel().getReaction(reactionId);
			copyReact = reac.clone();
			String dimInID = SBMLutilities.getDimensionString(reac);
			String freshIndex = SBMLutilities.getIndicesString(reac, "compartment");
			CiIndex.setText(freshIndex);
			reacID.setText(reac.getId()+dimInID);
			reacName.setText(reac.getName());
			if (bioModel.getPortByIdRef(reac.getId())!=null) {
				onPort.setSelected(true);
			} else {
				onPort.setSelected(false);
			}
			if (reac.getReversible()) {
				reacReverse.setSelectedItem("true");
			}
			else {
				reacReverse.setSelectedItem("false");
			}
			if (reac.getFast()) {
				reacFast.setSelectedItem("true");
			}
			else {
				reacFast.setSelectedItem("false");
			}
			if (reac.isSetSBOTerm()) {
				reactionSBO.setSelectedItem(SBMLutilities.sbo.getName(reac.getSBOTermID()));
			}
			reactionComp.setSelectedItem(reac.getCompartment());
			complex = null;
			production = null;
			if (reac.isSetSBOTerm()) {
				if (BioModel.isComplexReaction(reac)) {
					complex = reac;
					reacID.setEnabled(false);
					reacName.setEnabled(false);
					onPort.setEnabled(false);
					reacReverse.setEnabled(false);
					reacFast.setEnabled(false);
					reactionComp.setEnabled(false);
					reacAddParam.setEnabled(false);
					reacRemoveParam.setEnabled(false);
					reacEditParam.setEnabled(false);
					addProduct.setEnabled(false);
					removeProduct.setEnabled(false);
					editProduct.setEnabled(false);
					products.removeMouseListener(this);
					addModifier.setEnabled(false);
					removeModifier.setEnabled(false);
					editModifier.setEnabled(false);
					modifiers.removeMouseListener(this);
					kineticLaw.setEditable(false);
					clearKineticLaw.setEnabled(false);
					reacParameters.setEnabled(false);
					useMassAction.setEnabled(false);
				} else if (BioModel.isConstitutiveReaction(reac) || BioModel.isDegradationReaction(reac) || 
						BioModel.isDiffusionReaction(reac)) {
					reacID.setEnabled(false);
					reacName.setEnabled(false);
					onPort.setEnabled(false);
					reacReverse.setEnabled(false);
					reacFast.setEnabled(false);
					reactionComp.setEnabled(false);
					reacAddParam.setEnabled(false);
					reacRemoveParam.setEnabled(false);
					reacEditParam.setEnabled(false);
					addReactant.setEnabled(false);
					removeReactant.setEnabled(false);
					editReactant.setEnabled(false);
					reactants.removeMouseListener(this);
					addProduct.setEnabled(false);
					removeProduct.setEnabled(false);
					editProduct.setEnabled(false);
					products.removeMouseListener(this);
					addModifier.setEnabled(false);
					removeModifier.setEnabled(false);
					editModifier.setEnabled(false);
					modifiers.removeMouseListener(this);
					kineticLaw.setEditable(false);
					useMassAction.setEnabled(false);
					clearKineticLaw.setEnabled(false);
					reacParameters.setEnabled(false);
				} else if (BioModel.isProductionReaction(reac)) {
					production = reac;
					reacID.setEnabled(false);
					reacName.setEnabled(false);
					onPort.setEnabled(false);
					reacReverse.setEnabled(false);
					reacFast.setEnabled(false);
					reactionComp.setEnabled(false);
					reacAddParam.setEnabled(false);
					reacRemoveParam.setEnabled(false);
					reacEditParam.setEnabled(false);
					addReactant.setEnabled(false);
					removeReactant.setEnabled(false);
					editReactant.setEnabled(false);
					reactants.removeMouseListener(this);
					kineticLaw.setEditable(false);
					clearKineticLaw.setEnabled(false);
					reacParameters.setEnabled(false);
					useMassAction.setEnabled(false);
				}
			} 
		}
		else {
			String NEWreactionId = "r0";
			int i = 0;
			while (bioModel.isSIdInUse(NEWreactionId)) {
				i++;
				NEWreactionId = "r" + i;
			}
			reacID.setText(NEWreactionId);
		}
		if (paramsOnly) {
			reacID.setEditable(false);
			reacName.setEditable(false);
			reacReverse.setEnabled(false);
			reacFast.setEnabled(false);
			reacAddParam.setEnabled(false);
			reacRemoveParam.setEnabled(false);
			addReactant.setEnabled(false);
			removeReactant.setEnabled(false);
			editReactant.setEnabled(false);
			addProduct.setEnabled(false);
			removeProduct.setEnabled(false);
			editProduct.setEnabled(false);
			addModifier.setEnabled(false);
			removeModifier.setEnabled(false);
			editModifier.setEnabled(false);
			kineticLaw.setEditable(false);
			useMassAction.setEnabled(false);
			clearKineticLaw.setEnabled(false);
			reactionComp.setEnabled(false);
			onPort.setEnabled(false);
		}
		Object[] options1 = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, reactionPanel, "Reaction Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options1, options1[0]);
		String[] dimID = new String[]{""};
		String[] dex = new String[]{""};
		String[] dimensionIds = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			dimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), reacID.getText(), false);
			String reactionID = "";
			if(dimID!=null){
				reactionID = dimID[0].trim();
				dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
				error = Utils.checkID(bioModel.getSBMLDocument(), reactionID, reactionId.trim(), false);
			}
			else{
				error = true;
			}
			if(reactionComp.isEnabled() && !error){
				SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)reactionComp.getSelectedItem());
				dex = Utils.checkIndices(CiIndex.getText().trim(), variable, bioModel.getSBMLDocument(), dimensionIds, "compartment", dimID, null, null);
				error = (dex==null);
			}
			if (!error) {
				if (complex==null && production==null && kineticLaw.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(Gui.frame, "A reaction must have a kinetic law.", "Enter A Kinetic Law", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if ((changedReactants.size() == 0) && (changedProducts.size() == 0)) {
					JOptionPane.showMessageDialog(Gui.frame, "A reaction must have at least one reactant or product.", "No Reactants or Products",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if(kineticFluxLabel.getSelectedItem().equals("Kinetic Law:")){
					if (complex==null && production==null && SBMLutilities.myParseFormula(kineticLaw.getText().trim()) == null) {
						JOptionPane.showMessageDialog(Gui.frame, "Unable to parse kinetic law.", "Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (complex==null && production==null){
						ArrayList<String> invalidKineticVars = getInvalidVariablesInReaction(kineticLaw.getText().trim(), dimensionIds, true, "", false);
						if (invalidKineticVars.size() > 0) {
							String invalid = "";
							for (int i = 0; i < invalidKineticVars.size(); i++) {
								if (i == invalidKineticVars.size() - 1) {
									invalid += invalidKineticVars.get(i);
								}
								else {
									invalid += invalidKineticVars.get(i) + "\n";
								}
							}
							String message;
							message = "Kinetic law contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
							JTextArea messageArea = new JTextArea(message);
							messageArea.setLineWrap(true);
							messageArea.setWrapStyleWord(true);
							messageArea.setEditable(false);
							JScrollPane scrolls = new JScrollPane();
							scrolls.setMinimumSize(new java.awt.Dimension(300, 300));
							scrolls.setPreferredSize(new java.awt.Dimension(300, 300));
							scrolls.setViewportView(messageArea);
							JOptionPane.showMessageDialog(Gui.frame, scrolls, "Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						if (!error) {
							error = Utils.checkNumFunctionArguments(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(kineticLaw.getText().trim()));
						}
						if (!error) {
							error = Utils.checkFunctionArgumentTypes(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(kineticLaw.getText().trim()));
						}
					}
				}
				else {
					// TODO: need to update for arrays
					error = !isFluxBoundValid(bioModel.getSBMLDocument(),
							kineticLaw.getText().replaceAll("\\s",""), reactionId,dimensionIds,dimID);
					error = false;
				}
			}
			if(kineticFluxLabel.getSelectedItem().equals("Kinetic Law:")){
				if (!error && complex==null && production==null) {
					if (SBMLutilities.returnsBoolean(bioModel.addBooleans(kineticLaw.getText().trim()), bioModel.getSBMLDocument().getModel())) {
						JOptionPane.showMessageDialog(Gui.frame, "Kinetic law must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = reactions.getSelectedIndex();
					String val = reactionId;
					Reaction react = bioModel.getSBMLDocument().getModel().getReaction(val);
					ListOf remove;
					long size;
					if (react.getKineticLaw()==null) {
						react.createKineticLaw();
					}
					remove = react.getKineticLaw().getListOfLocalParameters();
					size = react.getKineticLaw().getLocalParameterCount();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedParameters.size(); i++) {
						react.getKineticLaw().addLocalParameter(changedParameters.get(i));
					}
					remove = react.getListOfProducts();
					size = react.getProductCount();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedProducts.size(); i++) {
						react.addProduct(changedProducts.get(i));
					}
					remove = react.getListOfModifiers();
					size = react.getModifierCount();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedModifiers.size(); i++) {
						react.addModifier(changedModifiers.get(i));
					}
					remove = react.getListOfReactants();
					size = react.getReactantCount();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedReactants.size(); i++) {
						react.addReactant(changedReactants.get(i));
					}
					if (reacReverse.getSelectedItem().equals("true")) {
						react.setReversible(true);
					}
					else {
						react.setReversible(false);
					}
					if (reactionSBO.getSelectedItem().equals("(unspecified)")) {
						react.unsetSBOTerm();
					} else {
						react.setSBOTerm(SBMLutilities.sbo.getId((String)reactionSBO.getSelectedItem()));
					}
					react.setCompartment((String) reactionComp.getSelectedItem());
					if (reacFast.getSelectedItem().equals("true")) {
						react.setFast(true);
					}
					else {
						react.setFast(false);
					}
					react.setId(reactionID);
					react.setName(reacName.getText().trim());
					Port port = bioModel.getPortByIdRef(val);
					SBMLutilities.createDimensions(react, dimensionIds, dimID);
					SBMLutilities.addIndices(react, "compartment", dex, 1);
					if (port!=null) {
						if (onPort.isSelected()) {
							port.setId(GlobalConstants.SBMLREACTION+"__"+react.getId());
							port.setIdRef(react.getId());
							SBMLutilities.cloneDimensionAddIndex(react, port, "comp:idRef");
						} else {
							bioModel.getSBMLCompModel().removePort(port);
						}
					} else {
						if (onPort.isSelected()) {
							port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.SBMLREACTION+"__"+react.getId());
							port.setIdRef(react.getId());
							SBMLutilities.cloneDimensionAddIndex(react, port, "comp:idRef");
						}
					}
					if(kineticFluxLabel.getSelectedItem().equals("Kinetic Law:")){
						if (complex==null && production==null) {
							react.getKineticLaw().setMath(bioModel.addBooleans(kineticLaw.getText().trim()));
						} else if (complex!=null) {
							react.getKineticLaw().setMath(SBMLutilities.myParseFormula(BioModel.createComplexKineticLaw(complex)));
						} else {
							react.getKineticLaw().setMath(SBMLutilities.myParseFormula(BioModel.createProductionKineticLaw(production)));
						}
						error = checkKineticLawUnits(react.getKineticLaw());
					}
					else{
						react.unsetKineticLaw();
						error = !createReactionFluxBounds(reactionId,dimID,dimensionIds);
					}

					if (!error) {
						error = SBMLutilities.checkCycles(bioModel.getSBMLDocument());
						if (error) {
							JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
									"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (!error) {
						if (index >= 0) {
							if (!paramsOnly) {
								reacts[index] = reactionID;
								for (int i = 1; dimID!=null && i < dimID.length; i++) {
									reacts[index] += "[" + dimID[i] + "]";
								}
							}
							reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							reacts = Utility.getList(reacts, reactions);
							reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							dataModels.biomodel.util.Utility.sort(reacts);
							reactions.setListData(reacts);
							reactions.setSelectedIndex(index);
						}
					}
					else {
						changedParameters = new ArrayList<LocalParameter>();
						if (react.isSetKineticLaw()) {
							ListOf<LocalParameter> listOfParameters = react.getKineticLaw().getListOfLocalParameters();
							for (int i = 0; i < react.getKineticLaw().getLocalParameterCount(); i++) {
								LocalParameter parameter = listOfParameters.get(i);
								changedParameters.add(new LocalParameter(parameter));
							}
						}
						changedProducts = new ArrayList<SpeciesReference>();
						ListOf<SpeciesReference> listOfProducts = react.getListOfProducts();
						for (int i = 0; i < react.getProductCount(); i++) {
							SpeciesReference product = listOfProducts.get(i);
							changedProducts.add(product);
						}
						changedReactants = new ArrayList<SpeciesReference>();
						ListOf<SpeciesReference> listOfReactants = react.getListOfReactants();
						for (int i = 0; i < react.getReactantCount(); i++) {
							SpeciesReference reactant = listOfReactants.get(i);
							changedReactants.add(reactant);
						}
						changedModifiers = new ArrayList<ModifierSpeciesReference>();
						ListOf<ModifierSpeciesReference> listOfModifiers = react.getListOfModifiers();
						for (int i = 0; i < react.getModifierCount(); i++) {
							ModifierSpeciesReference modifier = listOfModifiers.get(i);
							changedModifiers.add(modifier);
						}
					}
					// Handle SBOL data
					if (!error && inSchematic && !paramsOnly) {
						if (!error) {
							// Add SBOL annotation to reaction
							if (sbolField.getSBOLURIs().size() > 0) {
								if (!react.isSetMetaId() || react.getMetaId().equals(""))
									SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), react, 
											bioModel.getMetaIDIndex());
								SBOLAnnotation sbolAnnot = new SBOLAnnotation(react.getMetaId(), sbolField.getSBOLURIs(),
										sbolField.getSBOLStrand());
								AnnotationUtility.setSBOLAnnotation(react, sbolAnnot);
							} else 
								AnnotationUtility.removeSBOLAnnotation(react);
						}
					}
				}
				else {
					Reaction react = bioModel.getSBMLDocument().getModel().createReaction();
					int index = reactions.getSelectedIndex();
					if(kineticFluxLabel.getSelectedItem().equals("Kinetic Law:")){
						react.createKineticLaw();
						for (int i = 0; i < changedParameters.size(); i++) {
							react.getKineticLaw().addLocalParameter(changedParameters.get(i));
						}
					}
					for (int i = 0; i < changedProducts.size(); i++) {
						react.addProduct(changedProducts.get(i));
					}
					for (int i = 0; i < changedModifiers.size(); i++) {
						react.addModifier(changedModifiers.get(i));
					}
					for (int i = 0; i < changedReactants.size(); i++) {
						react.addReactant(changedReactants.get(i));
					}
					if (reacReverse.getSelectedItem().equals("true")) {
						react.setReversible(true);
					}
					else {
						react.setReversible(false);
					}
					if (reacFast.getSelectedItem().equals("true")) {
						react.setFast(true);
					}
					else {
						react.setFast(false);
					}
					if (reactionSBO.getSelectedItem().equals("(unspecified)")) {
						react.unsetSBOTerm();
					} else {
						react.setSBOTerm(SBMLutilities.sbo.getId((String)reactionSBO.getSelectedItem()));
					}
					react.setCompartment((String) reactionComp.getSelectedItem());
					react.setId(reactionID);
					react.setName(reacName.getText().trim());
					SBMLutilities.createDimensions(react, dimensionIds, dimID);
					SBMLutilities.addIndices(react, "compartment", dex, 1);
					if (onPort.isSelected()) {
						Port port = bioModel.getSBMLCompModel().createPort();
						port.setId(GlobalConstants.SBMLREACTION+"__"+react.getId());
						port.setIdRef(react.getId());
						SBMLutilities.cloneDimensionAddIndex(react, port, "comp:idRef");
					}
					if(kineticFluxLabel.getSelectedItem().equals("Kinetic Law:")){
						if (complex==null && production==null) {
							react.getKineticLaw().setMath(bioModel.addBooleans(kineticLaw.getText().trim()));
						} else if (complex!=null) {
							react.getKineticLaw().setMath(SBMLutilities.myParseFormula(BioModel.createComplexKineticLaw(complex)));
						} else {
							react.getKineticLaw().setMath(SBMLutilities.myParseFormula(BioModel.createProductionKineticLaw(production)));
						}
						error = checkKineticLawUnits(react.getKineticLaw());
					}
					else{
						error = !isFluxBoundValid(bioModel.getSBMLDocument(),
								kineticLaw.getText().replaceAll("\\s",""), reactionId,dimensionIds,dimID);
						if (!error)	error = !createReactionFluxBounds(reactionId,dimID,dimensionIds);
					}
						
					if (!error) {
						error = SBMLutilities.checkCycles(bioModel.getSBMLDocument());
						if (error) {
							JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
									"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (!error) {
						JList add = new JList();
						String reactionEntry = reactionID;
						for (int i = 1; dimID!=null && i < dimID.length; i++) {
							reactionEntry += "[" + dimID[i] + "]";
						}
						Object[] adding = { reactionEntry };
						add.setListData(adding);
						add.setSelectedIndex(0);
						reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(reacts, reactions, add);
						reacts = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							reacts[i] = (String) adding[i];
						}
						dataModels.biomodel.util.Utility.sort(reacts);
						reactions.setListData(reacts);
						reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (bioModel.getSBMLDocument().getModel().getReactionCount() == 1) {
							reactions.setSelectedIndex(0);
						}
						else {
							reactions.setSelectedIndex(index);
						}
					}
					else {
						bioModel.getSBMLDocument().getModel().removeReaction(reactionID);
						//bioModel.removeReaction(reactionID);//removeTheReaction(bioModel, reactionID);
					}
				}
				modelEditor.setDirty(true);
				bioModel.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, reactionPanel, "Reaction Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			if (option.equals("OK")) {
				String reactId = reactionId;
				bioModel.getSBMLDocument().getModel().removeReaction(reactId);
				//bioModel.removeReaction(reactId); 
				bioModel.getSBMLDocument().getModel().addReaction(copyReact);
			}
			return;
		}
	}

	/**
	 * Find invalid reaction variables in a formula
	 */
	private ArrayList<String> getInvalidVariablesInReaction(String formula, String[] dimensionIds, boolean isReaction, String arguments, boolean isFunction) {
		ArrayList<String> validVars = new ArrayList<String>();
		ArrayList<String> invalidVars = new ArrayList<String>();
		Model model = bioModel.getSBMLDocument().getModel();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getFunctionDefinitionCount(); i++) {
			validVars.add(model.getFunctionDefinition(i).getId());
		}
		if (isReaction) {
			for (int i = 0; i < changedParameters.size(); i++) {
				validVars.add(changedParameters.get(i).getId());
			}
			for (int i = 0; i < changedReactants.size(); i++) {
				validVars.add(changedReactants.get(i).getSpecies());
				validVars.add(changedReactants.get(i).getId());
			}
			for (int i = 0; i < changedProducts.size(); i++) {
				validVars.add(changedProducts.get(i).getSpecies());
				validVars.add(changedProducts.get(i).getId());
			}
			for (int i = 0; i < changedModifiers.size(); i++) {
				validVars.add(changedModifiers.get(i).getSpecies());
			}
			if (dimensionIds != null) {
				for (int i = 0; i < dimensionIds.length; i++) {
					validVars.add(dimensionIds[i]);
				}
			}
		}
		else if (!isFunction) {
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getSpeciesCount(); i++) {
				validVars.add(model.getSpecies(i).getId());
			}
		}
		if (isFunction) {
			String[] args = arguments.split(" |\\,");
			for (int i = 0; i < args.length; i++) {
				validVars.add(args[i]);
			}
		}
		else {
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getCompartmentCount(); i++) {
				validVars.add(model.getCompartment(i).getId());
			}
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
				validVars.add(model.getParameter(i).getId());
			}
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getReactionCount(); i++) {
				Reaction reaction = model.getReaction(i);
				validVars.add(reaction.getId());
				for (int j = 0; j < reaction.getReactantCount(); j++) {
					SpeciesReference reactant = reaction.getReactant(j);
					if ((reactant.isSetId()) && (!reactant.getId().equals(""))) {
						validVars.add(reactant.getId());
					}
				}
				for (int j = 0; j < reaction.getProductCount(); j++) {
					SpeciesReference product = reaction.getProduct(j);
					if ((product.isSetId()) && (!product.getId().equals(""))) {
						validVars.add(product.getId());
					}
				}
			}
			String[] kindsL3V1 = { "ampere", "avogadro", "becquerel", "candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray", "henry",
					"hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre", "mole", "newton", "ohm", "pascal",
					"radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
			for (int i = 0; i < kindsL3V1.length; i++) {
				validVars.add(kindsL3V1[i]);
			}
			for (int i = 0; i < model.getUnitDefinitionCount(); i++) {
				validVars.add(model.getUnitDefinition(i).getId());
			}
		}
		String[] splitLaw = formula.split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-|>|=|<|\\^|%|&|\\||!|\\[|\\]|\\{|\\}");
		for (int i = 0; i < splitLaw.length; i++) {
			if (splitLaw[i].equals("abs") || splitLaw[i].equals("arccos") || splitLaw[i].equals("arccosh") || splitLaw[i].equals("arcsin")
					|| splitLaw[i].equals("arcsinh") || splitLaw[i].equals("arctan") || splitLaw[i].equals("arctanh") || splitLaw[i].equals("arccot")
					|| splitLaw[i].equals("arccoth") || splitLaw[i].equals("arccsc") || splitLaw[i].equals("arccsch") || splitLaw[i].equals("arcsec")
					|| splitLaw[i].equals("arcsech") || splitLaw[i].equals("acos") || splitLaw[i].equals("acosh") || splitLaw[i].equals("asin")
					|| splitLaw[i].equals("asinh") || splitLaw[i].equals("atan") || splitLaw[i].equals("atanh") || splitLaw[i].equals("acot")
					|| splitLaw[i].equals("acoth") || splitLaw[i].equals("acsc") || splitLaw[i].equals("acsch") || splitLaw[i].equals("asec")
					|| splitLaw[i].equals("asech") || splitLaw[i].equals("cos") || splitLaw[i].equals("cosh") || splitLaw[i].equals("cot")
					|| splitLaw[i].equals("coth") || splitLaw[i].equals("csc") || splitLaw[i].equals("csch") || splitLaw[i].equals("ceil")
					|| splitLaw[i].equals("factorial") || splitLaw[i].equals("exp") || splitLaw[i].equals("floor") || splitLaw[i].equals("ln")
					|| splitLaw[i].equals("log") || splitLaw[i].equals("sqr") || splitLaw[i].equals("log10") || splitLaw[i].equals("pow")
					|| splitLaw[i].equals("sqrt") || splitLaw[i].equals("root") || splitLaw[i].equals("piecewise") || splitLaw[i].equals("sec")
					|| splitLaw[i].equals("sech") || splitLaw[i].equals("sin") || splitLaw[i].equals("sinh") || splitLaw[i].equals("tan")
					|| splitLaw[i].equals("tanh") || splitLaw[i].equals("") || splitLaw[i].equals("and") || splitLaw[i].equals("or")
					|| splitLaw[i].equals("xor") || splitLaw[i].equals("not") || splitLaw[i].equals("eq") || splitLaw[i].equals("geq")
					|| splitLaw[i].equals("leq") || splitLaw[i].equals("gt") || splitLaw[i].equals("neq") || splitLaw[i].equals("lt")
					|| splitLaw[i].equals("delay") || splitLaw[i].equals("t") || splitLaw[i].equals("time") || splitLaw[i].equals("true")
					|| splitLaw[i].equals("false") || splitLaw[i].equals("pi") || splitLaw[i].equals("exponentiale")
					|| splitLaw[i].equals("avogadro")) {
			}
			else {
				String temp = splitLaw[i];
				if (splitLaw[i].substring(splitLaw[i].length() - 1, splitLaw[i].length()).equals("e")) {
					temp = splitLaw[i].substring(0, splitLaw[i].length() - 1);
				}
				try {
					Double.parseDouble(temp);
				}
				catch (Exception e1) {
					if (!validVars.contains(splitLaw[i])) {
						invalidVars.add(splitLaw[i]);
					}
				}
			}
		}
		return invalidVars;
	}

	/**
	 * Creates a frame used to edit reactions parameters or create new ones.
	 */
	private void reacParametersEditor(BioModel bioModel,String option) {
		if (option.equals("OK") && reacParameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No parameter selected.", "Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel parametersPanel;
		parametersPanel = new JPanel(new SpringLayout());
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel valueLabel = new JLabel("Value:");
		JLabel unitsLabel = new JLabel("Units:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		JLabel sboTermLabel = new JLabel(GlobalConstants.SBOTERM);
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_PARAMETER));
		paramOnPort = new JCheckBox();
		reacParamID = new JTextField();
		reacParamName = new JTextField();
		reacParamValue = new JTextField();
		reacParamUnits = new JComboBox();
		reacParamUnits.addItem("( none )");
		Model model = bioModel.getSBMLDocument().getModel();
		ListOf<UnitDefinition> listOfUnits = model.getListOfUnitDefinitions();
		String[] units = new String[model.getUnitDefinitionCount()];
		for (int i = 0; i < model.getUnitDefinitionCount(); i++) {
			UnitDefinition unit = listOfUnits.get(i);
			units[i] = unit.getId();
			// GET OTHER THINGS
		}
		for (int i = 0; i < units.length; i++) {
			reacParamUnits.addItem(units[i]);
		}
		String[] unitIdsL3V1 = { "ampere", "avogadro", "becquerel", "candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray",
				"henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre", "mole", "newton", "ohm",
				"pascal", "radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
		String[] unitIds;
		unitIds = unitIdsL3V1;
		for (int i = 0; i < unitIds.length; i++) {
			reacParamUnits.addItem(unitIds[i]);
		}
		String[] list = { "Original", "Modified" };
		String[] list1 = { "1", "2" };
		final JComboBox type = new JComboBox(list);
		final JTextField start = new JTextField();
		final JTextField stop = new JTextField();
		final JTextField step = new JTextField();
		final JComboBox level = new JComboBox(list1);
		final JButton sweep = new JButton("Sweep");
		sweep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "Ok", "Close" };
				JPanel p = new JPanel(new GridLayout(4, 2));
				JLabel startLabel = new JLabel("Start:");
				JLabel stopLabel = new JLabel("Stop:");
				JLabel stepLabel = new JLabel("Step:");
				JLabel levelLabel = new JLabel("Level:");
				p.add(startLabel);
				p.add(start);
				p.add(stopLabel);
				p.add(stop);
				p.add(stepLabel);
				p.add(step);
				p.add(levelLabel);
				p.add(level);
				int i = JOptionPane.showOptionDialog(Gui.frame, p, "Sweep", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
				if (i == JOptionPane.YES_OPTION) {
					double startVal = 0.0;
					double stopVal = 0.0;
					double stepVal = 0.0;
					try {
						startVal = Double.parseDouble(start.getText().trim());
						stopVal = Double.parseDouble(stop.getText().trim());
						stepVal = Double.parseDouble(step.getText().trim());
					}
					catch (Exception e1) {
					}
					reacParamValue.setText("(" + startVal + "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ")");
				}
			}
		});
		if (paramsOnly) {
			reacParamID.setEditable(false);
			reacParamName.setEditable(false);
			reacParamValue.setEnabled(false);
			reacParamUnits.setEnabled(false);
			sweep.setEnabled(false);
			paramOnPort.setEnabled(false);
		}
		String selectedID = "";
		if (option.equals("OK")) {
			String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
			LocalParameter paramet = null;
			for (LocalParameter p : changedParameters) {
				if (p.getId().equals(v)) {
					paramet = p;
				}
			}
			if (paramet==null) return;
			reacParamID.setText(paramet.getId());
			selectedID = paramet.getId();
			reacParamName.setText(paramet.getName());
			reacParamValue.setText("" + paramet.getValue());
			if (paramet.isSetUnits()) {
				reacParamUnits.setSelectedItem(paramet.getUnits());
			}
			if (paramet.isSetSBOTerm()) {
				SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(paramet.getSBOTermID()));
			}
			if (bioModel.getPortByMetaIdRef(paramet.getMetaId())!=null) {
				paramOnPort.setSelected(true);
			} else {
				paramOnPort.setSelected(false);
			}
			if (paramsOnly && (((String) reacParameters.getSelectedValue()).contains("Modified"))
					|| (((String) reacParameters.getSelectedValue()).contains("Custom"))
					|| (((String) reacParameters.getSelectedValue()).contains("Sweep"))) {
				type.setSelectedItem("Modified");
				sweep.setEnabled(true);
				reacParamValue.setText(((String) reacParameters.getSelectedValue()).split(" ")[((String) reacParameters.getSelectedValue())
				                                                                               .split(" ").length - 1]);
				reacParamValue.setEnabled(true);
				reacParamUnits.setEnabled(false);
				SBOTerms.setEnabled(false);
				if (reacParamValue.getText().trim().startsWith("(")) {
					try {
						start.setText((reacParamValue.getText().trim()).split(",")[0].substring(1).trim());
						stop.setText((reacParamValue.getText().trim()).split(",")[1].trim());
						step.setText((reacParamValue.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((reacParamValue.getText().trim()).split(",")[3].replace(")", "").trim());
						if (lev == 1) {
							level.setSelectedIndex(0);
						}
						else {
							level.setSelectedIndex(1);
						}
					}
					catch (Exception e1) {
					}
				}
			}
		}
		parametersPanel.add(idLabel);
		parametersPanel.add(reacParamID);
		parametersPanel.add(nameLabel);
		parametersPanel.add(reacParamName);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Value Type:");
			type.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						sweep.setEnabled(true);
						reacParamValue.setEnabled(true);
						reacParamUnits.setEnabled(false);
						SBOTerms.setEnabled(false);
					}
					else {
						sweep.setEnabled(false);
						reacParamValue.setEnabled(false);
						reacParamUnits.setEnabled(false);
						SBOTerms.setEnabled(false);
						SBMLDocument d;
            try {
              d = SBMLutilities.readSBML(file);
              KineticLaw KL = d.getModel().getReaction(selectedReaction).getKineticLaw();
              ListOf<LocalParameter> list = KL.getListOfLocalParameters();
              int number = -1;
              for (int i = 0; i < KL.getLocalParameterCount(); i++) {
                if (list.get(i).getId().equals(((String) reacParameters.getSelectedValue()).split(" ")[0])) {
                  number = i;
                }
              }
              reacParamValue.setText(d.getModel().getReaction(selectedReaction).getKineticLaw()
                  .getLocalParameter(number).getValue()
                  + "");
              if (d.getModel().getReaction(selectedReaction).getKineticLaw().getLocalParameter(number)
                  .isSetUnits()) {
                reacParamUnits.setSelectedItem(d.getModel().getReaction(selectedReaction)
                    .getKineticLaw().getLocalParameter(number).getUnits());
              }
              reacParamValue.setText(d.getModel().getReaction(selectedReaction).getKineticLaw().getLocalParameter(number).getValue()  + "");
            
            } catch (XMLStreamException e1) {
              JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
              e1.printStackTrace();
            } catch (IOException e1) {
              JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file", "Error Opening File", JOptionPane.ERROR_MESSAGE);
              e1.printStackTrace();
            }
					}
				}
			});
			parametersPanel.add(typeLabel);
			parametersPanel.add(type);
		}
		parametersPanel.add(valueLabel);
		parametersPanel.add(reacParamValue);
		if (paramsOnly) {
			parametersPanel.add(new JLabel());
			parametersPanel.add(sweep);
		}
		parametersPanel.add(unitsLabel);
		parametersPanel.add(reacParamUnits);
		parametersPanel.add(onPortLabel);
		parametersPanel.add(paramOnPort);
		parametersPanel.add(sboTermLabel);
		parametersPanel.add(SBOTerms);
		if (paramsOnly) {
			SpringUtilities.makeCompactGrid(parametersPanel, 8, 2, 6, 6, 6, 6);
		} else {
			SpringUtilities.makeCompactGrid(parametersPanel, 6, 2, 6, 6, 6, 6);
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, "Parameter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = Utils.checkID(bioModel.getSBMLDocument(), reacParamID.getText().trim(), selectedID, true);
			if (!error) {
				if (thisReactionParams.contains(reacParamID.getText().trim()) && (!reacParamID.getText().trim().equals(selectedID))) {
					JOptionPane.showMessageDialog(Gui.frame, "ID is not unique.", "ID Not Unique", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				double val = 0;
				if (reacParamValue.getText().trim().startsWith("(") && reacParamValue.getText().trim().endsWith(")")) {
					try {
						Double.parseDouble((reacParamValue.getText().trim()).split(",")[0].substring(1).trim());
						Double.parseDouble((reacParamValue.getText().trim()).split(",")[1].trim());
						Double.parseDouble((reacParamValue.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((reacParamValue.getText().trim()).split(",")[3].replace(")", "").trim());
						if (lev != 1 && lev != 2) {
							error = true;
							JOptionPane.showMessageDialog(Gui.frame, "The level can only be 1 or 2.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception e1) {
						error = true;
						JOptionPane.showMessageDialog(Gui.frame, "Invalid sweeping parameters.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					try {
						val = Double.parseDouble(reacParamValue.getText().trim());
					}
					catch (Exception e1) {
						JOptionPane
						.showMessageDialog(Gui.frame, "The value must be a real number.", "Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error) {
					String unit = (String) reacParamUnits.getSelectedItem();
					String param = "";
					if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
						int index = reacParameters.getSelectedIndex();
						String[] splits = reacParams[index].split(" ");
						for (int i = 0; i < splits.length - 2; i++) {
							param += splits[i] + " ";
						}
						if (!splits[splits.length - 2].equals("Modified") && !splits[splits.length - 2].equals("Custom")
								&& !splits[splits.length - 2].equals("Sweep")) {
							param += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
						}
						if (reacParamValue.getText().trim().startsWith("(") && reacParamValue.getText().trim().endsWith(")")) {
							double startVal = Double.parseDouble((reacParamValue.getText().trim()).split(",")[0].substring(1).trim());
							double stopVal = Double.parseDouble((reacParamValue.getText().trim()).split(",")[1].trim());
							double stepVal = Double.parseDouble((reacParamValue.getText().trim()).split(",")[2].trim());
							int lev = Integer.parseInt((reacParamValue.getText().trim()).split(",")[3].replace(")", "").trim());
							param += "Sweep (" + startVal + "," + stopVal + "," + stepVal + "," + lev + ")";
						}
						else {
							param += "Modified " + val;
						}
					}
					else {
						if (unit.equals("( none )")) {
							param = reacParamID.getText().trim() + " " + val;
						}
						else {
							param = reacParamID.getText().trim() + " " + val + " " + unit;
						}
					}
					if (option.equals("OK")) {
						int index = reacParameters.getSelectedIndex();
						String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
						LocalParameter paramet = null;
						for (LocalParameter p : changedParameters) {
							if (p.getId().equals(v)) {
								paramet = p;
							}
						}
						if (paramet==null) return;
						reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						reacParams = Utility.getList(reacParams, reacParameters);
						reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						paramet.setId(reacParamID.getText().trim());
						paramet.setName(reacParamName.getText().trim());
						SBMLutilities.setMetaId(paramet, reacID.getText()+"___"+reacParamID.getText().trim());
						for (int i = 0; i < thisReactionParams.size(); i++) {
							if (thisReactionParams.get(i).equals(v)) {
								thisReactionParams.set(i, reacParamID.getText().trim());
							}
						}
						paramet.setValue(val);
						if (unit.equals("( none )")) {
							paramet.unsetUnits();
						}
						else {
							paramet.setUnits(unit);
						}
						if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
							paramet.unsetSBOTerm();
						} else {
							paramet.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
						}
						reacParams[index] = param;
						dataModels.biomodel.util.Utility.sort(reacParams);
						reacParameters.setListData(reacParams);
						reacParameters.setSelectedIndex(index);
						if (paramsOnly) {
							int remove = -1;
							for (int i = 0; i < parameterChanges.size(); i++) {
								if (parameterChanges.get(i).split(" ")[0].equals(selectedReaction + "/"	+ reacParamID.getText().trim())) {
									remove = i;
								}
							}
							String reacValue = selectedReaction;
							int index1 = reactions.getSelectedIndex();
							if (remove != -1) {
								parameterChanges.remove(remove);
								reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacts = Utility.getList(reacts, reactions);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reacts[index1] = reacValue.split("\\[| ")[0];
								dataModels.biomodel.util.Utility.sort(reacts);
								reactions.setListData(reacts);
								reactions.setSelectedIndex(index1);
							}
							if (!((String) type.getSelectedItem()).equals("Original")) {
								parameterChanges.add(reacValue + "/" + param);
								reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacts = Utility.getList(reacts, reactions);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reacts[index1] = reacValue + " Modified";
								dataModels.biomodel.util.Utility.sort(reacts);
								reactions.setListData(reacts);
								reactions.setSelectedIndex(index1);
							}
						}
						else {
							kineticLaw.setText(SBMLutilities.updateFormulaVar(kineticLaw.getText().trim(), v, reacParamID.getText().trim()));
						}
						Port port = bioModel.getPortByMetaIdRef(paramet.getMetaId());
						if (port!=null) {
							if (paramOnPort.isSelected()) {
								port.setId(GlobalConstants.LOCALPARAMETER+"__"+paramet.getMetaId());
								port.setMetaIdRef(paramet.getMetaId());
							} else {
								bioModel.getSBMLCompModel().removePort(port);
							}
						} else {
							if (paramOnPort.isSelected()) {
								port = bioModel.getSBMLCompModel().createPort();
								port.setId(GlobalConstants.LOCALPARAMETER+"__"+paramet.getMetaId());
								port.setMetaIdRef(paramet.getMetaId());
							}
						}
					}
					else {
						int index = reacParameters.getSelectedIndex();
						// Parameter paramet = new Parameter(BioSim.SBML_LEVEL,
						// BioSim.SBML_VERSION);
						LocalParameter paramet = new LocalParameter(bioModel.getSBMLDocument().getLevel(), bioModel.getSBMLDocument().getVersion());
						changedParameters.add(paramet);
						paramet.setId(reacParamID.getText().trim());
						paramet.setName(reacParamName.getText().trim());
						SBMLutilities.setMetaId(paramet, reacID.getText()+"___"+reacParamID.getText().trim());
						thisReactionParams.add(reacParamID.getText().trim());
						paramet.setValue(val);
						if (!unit.equals("( none )")) {
							paramet.setUnits(unit);
						}
						if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
							paramet.unsetSBOTerm();
						} else {
							paramet.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
						}
						if (paramOnPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.LOCALPARAMETER+"__"+paramet.getMetaId());
							port.setMetaIdRef(paramet.getMetaId());
						}
						JList add = new JList();
						Object[] adding = { param };
						add.setListData(adding);
						add.setSelectedIndex(0);
						reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(reacParams, reacParameters, add);
						reacParams = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							reacParams[i] = (String) adding[i];
						}
						dataModels.biomodel.util.Utility.sort(reacParams);
						reacParameters.setListData(reacParams);
						reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						try {
							if (bioModel.getSBMLDocument().getModel().getReaction(selectedReaction).getKineticLaw()
									.getLocalParameterCount() == 1) {
								reacParameters.setSelectedIndex(0);
							}
							else {
								reacParameters.setSelectedIndex(index);
							}
						}
						catch (Exception e2) {
							reacParameters.setSelectedIndex(0);
						}
					}
					modelEditor.setDirty(true);
					bioModel.makeUndoPoint();
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, "Parameter Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	private void addLocalParameter(String id,double val) {
		LocalParameter paramet = new LocalParameter(bioModel.getSBMLDocument().getLevel(), bioModel.getSBMLDocument().getVersion());
		changedParameters.add(paramet);
		paramet.setId(id);
		paramet.setName(id);
		SBMLutilities.setMetaId(paramet, reacID.getText()+"___"+id);
		thisReactionParams.add(id);
		paramet.setValue(val);
		JList add = new JList();
		String param = id + " " + val;
		Object[] adding = { param };
		add.setListData(adding);
		add.setSelectedIndex(0);
		reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		adding = Utility.add(reacParams, reacParameters, add);
		reacParams = new String[adding.length];
		for (int i = 0; i < adding.length; i++) {
			reacParams[i] = (String) adding[i];
		}
		dataModels.biomodel.util.Utility.sort(reacParams);
		reacParameters.setListData(reacParams);
		reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reacParameters.setSelectedIndex(0);
		modelEditor.setDirty(true);
		bioModel.makeUndoPoint();
	}

	/**
	 * Creates a frame used to edit products or create new ones.
	 */
	public void productsEditor(BioModel bioModel, String option, String selectedProductId, SpeciesReference product,
			boolean inSchematic, Reaction reaction) {
		JPanel productsPanel;
		productsPanel = new JPanel(new GridLayout(7, 2));
		JLabel productIdLabel = new JLabel("Id:");
		JLabel productNameLabel = new JLabel("Name:");
		JLabel speciesLabel = new JLabel("Species:");
		JLabel SBOTermLabel = new JLabel(GlobalConstants.SBOTERM);
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_PARTICIPANT_ROLE));
		Object[] stoiciOptions = { "Stoichiometry", "Stoichiometry Math" };
		stoiciLabel = new JComboBox(stoiciOptions);
		JLabel stoichiometryLabel = new JLabel("Stoichiometry:");
		JLabel constantLabel = new JLabel("Constant:");
		Object[] productConstantOptions = { "true", "false" };
		productConstant = new JComboBox(productConstantOptions);
		ListOf<Species> listOfSpecies = bioModel.getSBMLDocument().getModel().getListOfSpecies();
		String[] speciesList = new String[bioModel.getSBMLDocument().getModel().getSpeciesCount()];
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getSpeciesCount(); i++) {
			speciesList[i] = listOfSpecies.get(i).getId();
		}
		dataModels.biomodel.util.Utility.sort(speciesList);
		productSpecies = new JComboBox();
		PiIndex = new JTextField(10);
		PiIndex.setEnabled(true);
		productSpecies.addActionListener(this);
		productSpecies.setEnabled(true);
		for (int i = 0; i < speciesList.length; i++) {
			Species species = bioModel.getSBMLDocument().getModel().getSpecies(speciesList[i]);
			if (species.getBoundaryCondition() || (!species.getConstant() && Rules.keepVarRateRule(bioModel, "", speciesList[i]))) {
				productSpecies.addItem(speciesList[i]);
			}
		}
		productId = new JTextField("");
		/*
		 * int j = 0; while (usedIDs.contains("product"+j)) { j++; }
		 * productId.setText("product"+j);
		 */
		productName = new JTextField("");
		productStoichiometry = new JTextField("1");
		String selectedID = "";
		if (option.equals("OK")) {
			if (product == null || !inSchematic) {
				for (SpeciesReference p : changedProducts) {
					if (p.getId().equals(selectedProductId)||p.getSpecies().equals(selectedProductId)) {
						if (speciesReferenceMatch(p,(String)products.getSelectedValue())) {
							product = p;
							break;
						}
					}
				}
			}
			if (product==null) return;
			if (product.isSetName()) {
				productName.setText(product.getName());
			}
			productSpecies.setSelectedItem(product.getSpecies());
			productStoichiometry.setText("" + product.getStoichiometry());
			if (product.isSetId()) {
				selectedID = product.getId();
				productId.setText(product.getId());
				InitialAssignment init = bioModel.getSBMLDocument().getModel().getInitialAssignment(selectedID);
				if (init!=null) {
					productStoichiometry.setText("" + bioModel.removeBooleans(init.getMath()));
				} 						
			}
			if (product.isSetSBOTerm()) {
				SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(product.getSBOTermID()));
			}
			if (!product.getConstant()) {
				productConstant.setSelectedItem("false");
			}
			String dimInID = SBMLutilities.getDimensionString(product);
			productId.setText(productId.getText()+dimInID);
			String freshIndex = SBMLutilities.getIndicesString(product, "species");
			PiIndex.setText(freshIndex);
		}
		if (production!=null) {
			double np = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			if (production.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING)!=null) {
				np = production.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			}
			productStoichiometry.setText(""+np);
			productStoichiometry.setEnabled(false);
		}
		String[] reactdimIDs = null; 
		String[] reactdimIDSizes = null;
		if (reaction!=null) {
			reactdimIDs = SBMLutilities.getDimensionIds(reaction);
			reactdimIDSizes = SBMLutilities.getDimensionSizes(reaction);
			reactdimIDSizes[0] = reaction.getId();
		} else {
			reactdimIDs = new String[]{""};
			reactdimIDSizes = Utils.checkSizeParameters(bioModel.getSBMLDocument(), reacID.getText(), false);
			if(reactdimIDSizes!=null){
				reactdimIDs = SBMLutilities.getDimensionIds("",reactdimIDSizes.length-1);
			}
		}
		
		productsPanel.add(productIdLabel);
		productsPanel.add(productId);
		productsPanel.add(productNameLabel);
		productsPanel.add(productName);
		productsPanel.add(speciesLabel);
		productsPanel.add(productSpecies);
		productsPanel.add(new JLabel("Indices:"));
		productsPanel.add(PiIndex);
		productsPanel.add(SBOTermLabel);
		productsPanel.add(SBOTerms);
		productsPanel.add(stoichiometryLabel);
		productsPanel.add(productStoichiometry);
		productsPanel.add(constantLabel);
		productsPanel.add(productConstant);
		if (speciesList.length == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "There are no species availiable to be products." + "\nAdd species to this sbml file first.",
					"No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, productsPanel, "Products Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		String[] dimID = new String[]{""};
		String[] dex = new String[]{""};
		String[] dimensionIds = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String prod = "";
			double val = 1.0;
			String productID = "";
			dimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), productId.getText(), true);
			if(dimID!=null){
				dimensionIds = SBMLutilities.getDimensionIds("p",dimID.length-1);
				productID = dimID[0].trim();
				if (productID.equals("")) {
					error = SBMLutilities.variableInUse(bioModel.getSBMLDocument(), selectedID, false, true, true);
				}
				else {
					error = Utils.checkID(bioModel.getSBMLDocument(), productID, selectedID, false);
				}
			}
			else{
				error = true;
			}
			if(!error){
				SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)productSpecies.getSelectedItem());
				dex = Utils.checkIndices(PiIndex.getText(), variable, bioModel.getSBMLDocument(), dimensionIds, "species", dimID, reactdimIDs, reactdimIDSizes);
				error = (dex==null);
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
					InitialAssignments.removeInitialAssignment(bioModel, selectedID);
					try {
						val = Double.parseDouble(productStoichiometry.getText().trim());
					}
					catch (Exception e1) {
						if (productId.getText().equals("")) {
							JOptionPane.showMessageDialog(Gui.frame, "The stoichiometry must be a real number if no id is provided.", "Enter A Valid Value",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						} else {
							if (dimID.length>1) {
								JOptionPane.showMessageDialog(Gui.frame, "Initial assignments on arrayed reactants not currently allowed.", "Illegal Initial Assignment",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							} else {
								error = InitialAssignments.addInitialAssignment(bioModel, productStoichiometry.getText().trim(), dimID);
								val = 1.0;
							}
						}
					}
					if (val <= 0) {
						JOptionPane.showMessageDialog(Gui.frame, "The stoichiometry value must be greater than 0.", "Enter A Valid Value",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (productId.getText().equals("")) {
						prod = productSpecies.getSelectedItem() + PiIndex.getText() + " " + val;
					} else {
						prod = productId.getText() + " : " + productSpecies.getSelectedItem() + PiIndex.getText() + " " + val;
					}
				}
				else {
					prod = productSpecies.getSelectedItem() + PiIndex.getText() + " " + productStoichiometry.getText().trim();
				}
			}
			int index = -1;
			if (!error) {
				if (product == null || !inSchematic) {
					if (option.equals("OK")) {
						index = products.getSelectedIndex();
					}
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					proda = Utility.getList(proda, products);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index >= 0) {
						products.setSelectedIndex(index);
					}
					/*
					for (int i = 0; i < proda.length; i++) {
						if (i != index) {
							if (proda[i].split(" ")[0].equals(productSpecies.getSelectedItem())) {
								error = true;
								JOptionPane.showMessageDialog(Gui.frame, "Unable to add species as a product.\n"
										+ "Each species can only be used as a product once.", "Species Can Only Be Used Once",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					*/
				}
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
					if (productStoichiometry.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry math must have formula.", "Enter Stoichiometry Formula",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (SBMLutilities.myParseFormula(productStoichiometry.getText().trim()) == null) {
						JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry formula is not valid.", "Enter Valid Formula",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						ArrayList<String> invalidVars = getInvalidVariablesInReaction(productStoichiometry.getText().trim(), dimensionIds, true, "", false);
						if (invalidVars.size() > 0) {
							String invalid = "";
							for (int i = 0; i < invalidVars.size(); i++) {
								if (i == invalidVars.size() - 1) {
									invalid += invalidVars.get(i);
								}
								else {
									invalid += invalidVars.get(i) + "\n";
								}
							}
							String message;
							message = "Stoiciometry math contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
							JTextArea messageArea = new JTextArea(message);
							messageArea.setLineWrap(true);
							messageArea.setWrapStyleWord(true);
							messageArea.setEditable(false);
							JScrollPane scrolls = new JScrollPane();
							scrolls.setMinimumSize(new java.awt.Dimension(300, 300));
							scrolls.setPreferredSize(new java.awt.Dimension(300, 300));
							scrolls.setViewportView(messageArea);
							JOptionPane.showMessageDialog(Gui.frame, scrolls, "Stoiciometry Math Error", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						if (!error) {
							error = Utils.checkNumFunctionArguments(bioModel.getSBMLDocument(),
									SBMLutilities.myParseFormula(productStoichiometry.getText().trim()));
						}
						if (!error) {
							error = Utils.checkFunctionArgumentTypes(bioModel.getSBMLDocument(),
									SBMLutilities.myParseFormula(productStoichiometry.getText().trim()));
						}
						if (!error) {
							if (SBMLutilities.returnsBoolean(SBMLutilities.myParseFormula(productStoichiometry.getText().trim()), bioModel.getSBMLDocument().getModel())) {
								JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry math must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error && option.equals("OK") && productConstant.getSelectedItem().equals("true")) {
				String id = selectedID;
				error = Utils.checkConstant(bioModel.getSBMLDocument(), "Product stoiciometry", id);
			}
			if (!error) {
				if (option.equals("OK")) {
					SpeciesReference produ = product;
					if (product == null || !inSchematic) {
						for (SpeciesReference p : changedProducts) {
							if (p.getId().equals(selectedProductId)||p.getSpecies().equals(selectedProductId)) {
								if (speciesReferenceMatch(p,(String)products.getSelectedValue())) {
									produ = p;
									break;
								}
							}
						}
						products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						proda = Utility.getList(proda, products);
						products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					}
					if (produ==null) return;
					produ.setId(productID);
					produ.setName(productName.getText().trim());
					produ.setSpecies((String) productSpecies.getSelectedItem());
					produ.setStoichiometry(val);
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						produ.unsetSBOTerm();
					} else {
						produ.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					if (productConstant.getSelectedItem().equals("true")) {
						produ.setConstant(true);
					}
					else {
						produ.setConstant(false);
					}
					if(!error){
						SBMLutilities.createDimensions(produ, dimensionIds, dimID);
						SBMLutilities.addIndices(produ, "species", dex, 1);
					}
					if (product == null || !inSchematic) {
						proda[index] = prod;
						dataModels.biomodel.util.Utility.sort(proda);
						products.setListData(proda);
						products.setSelectedIndex(index);
					}
					try {
            SBMLutilities.updateVarId(bioModel.getSBMLDocument(), false, selectedID, productID);
          } catch (BioSimException e1) {
            JOptionPane.showMessageDialog(Gui.frame,  e1.getMessage(), e1.getTitle(), JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
          }
					if (product == null || !inSchematic) {
						kineticLaw.setText(SBMLutilities.updateFormulaVar(kineticLaw.getText().trim(), selectedID, productID));
					}
				}
				else {
					// SpeciesReference produ = new
					// SpeciesReference(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
					SpeciesReference produ = new SpeciesReference(bioModel.getSBMLDocument().getLevel(), bioModel.getSBMLDocument().getVersion());
					produ.setId(productID);
					produ.setName(productName.getText().trim());
					changedProducts.add(produ);
					produ.setSpecies((String) productSpecies.getSelectedItem());
					produ.setStoichiometry(val);
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						produ.unsetSBOTerm();
					} else {
						produ.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					if (productConstant.getSelectedItem().equals("true")) {
						produ.setConstant(true);
					}
					else {
						produ.setConstant(false);
					}
					if(!error){
						SBMLutilities.createDimensions(produ, dimensionIds, dimID);
						SBMLutilities.addIndices(produ, "species", dex, 1);
					}
					JList add = new JList();
					Object[] adding = { prod };
					add.setListData(adding);
					add.setSelectedIndex(0);
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(proda, products, add);
					proda = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						proda[i] = (String) adding[i];
					}
					dataModels.biomodel.util.Utility.sort(proda);
					products.setListData(proda);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					products.setSelectedIndex(0);
				}
				modelEditor.setDirty(true);
				bioModel.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, productsPanel, "Products Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	private LocalParameter getChangedParameter(String paramStr) {
		for (LocalParameter r : changedParameters) {
			if (r.getId().equals(paramStr)) {
				return r;
			}
		}
		return null;
	}
	
	private static boolean modifierSpeciesReferenceMatch(ModifierSpeciesReference ref,String refStr) {
		String[] split = refStr.split(" ");
		String id = "";
		String dim = "";
		String species = "";
		String index = "";
		int offset = 0;
		if (refStr.contains(":")) {
			id = split[0].split("\\[")[0];
			if (split[0].contains("["))
				dim = split[0].substring(split[0].indexOf("["));
			offset = 2;
		}
		species = split[offset].split("\\[")[0];
		if (split[offset].contains("["))
			index = split[offset].substring(split[offset].indexOf("["));
		if (ref.isSetId() && !ref.getId().equals(id)) return false;
		if (!ref.getSpecies().equals(species)) return false;
		String refDim = SBMLutilities.getDimensionString(ref);
		if (!refDim.equals(dim)) return false;
		String refIndex = SBMLutilities.getIndicesString(ref, "species");
		if (!refIndex.equals(index)) return false;
		return true;
	}


	/**
	 * Creates a frame used to edit modifiers or create new ones.
	 */
	public void modifiersEditor(BioModel bioModel,String option,String selectedModifierId, ModifierSpeciesReference modifier,
			boolean inSchematic, Reaction reaction) {
		JPanel modifiersPanel;
		MiIndex = new JTextField(10);
		modifierId = new JTextField(10);
		modifierName = new JTextField(10);
		JLabel speciesLabel = new JLabel("Species:");
		ListOf<Species> listOfSpecies = bioModel.getSBMLDocument().getModel().getListOfSpecies();
		String[] speciesList = new String[bioModel.getSBMLDocument().getModel().getSpeciesCount()];
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getSpeciesCount(); i++) {
			speciesList[i] = listOfSpecies.get(i).getId();
		}
		dataModels.biomodel.util.Utility.sort(speciesList);
		Object[] choices = speciesList;
		modifierSpecies = new JComboBox(choices);
		modifierSpecies.addActionListener(this);
		modifierSpecies.setEnabled(true);
		JLabel SBOTermsLabel = new JLabel(GlobalConstants.SBOTERM);
		JLabel RepStoichiometryLabel = new JLabel("Stoichiometry of repression (nc)");
		JLabel RepBindingLabel = new JLabel("Repression binding equilibrium (Kr)");
		JLabel ActStoichiometryLabel = new JLabel("Stoichiometry of activation (nc)");
		JLabel ActBindingLabel = new JLabel("Activation binding equilibrium (Ka)");
		
		String selectedID = "";	
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_PARTICIPANT_ROLE));
		
		if (option.equals("OK")) {
			if (modifier == null || !inSchematic) {
				for (ModifierSpeciesReference m : changedModifiers) {
					if (m.getId().equals(selectedModifierId)||m.getSpecies().equals(selectedModifierId)) {
						if (modifierSpeciesReferenceMatch(m,(String)modifiers.getSelectedValue())) {
							modifier = m;
							break;
						}
					}
				}
			}
			if (modifier==null) return;
			if (modifier.isSetName()) {
				modifierName.setText(modifier.getName());
			}
			if (modifier.isSetId()) {
				selectedID = modifier.getId();
				modifierId.setText(modifier.getId());
			}

			String dimInID = SBMLutilities.getDimensionString(modifier);
			modifierId.setText(modifierId.getText()+dimInID);
			String freshIndex = SBMLutilities.getIndicesString(modifier, "species");
			MiIndex.setText(freshIndex);
			modifierSpecies.setSelectedItem(modifier.getSpecies());
			if (modifier.isSetSBOTerm()) {
				SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(modifier.getSBOTermID()));
			}
		} 
		if (production==null) {
			modifiersPanel = new JPanel(new GridLayout(5, 2));
		} else {
			if (SBOTerms.getSelectedItem().equals("promoter")) {
				modifiersPanel = new JPanel(new GridLayout(5, 2));
			} else {
				modifiersPanel = new JPanel(new GridLayout(9, 2));
			}
		}
		String[] reactdimIDs = null; 
		String[] reactdimIDSizes = null;
		if (reaction!=null) {
			reactdimIDs = SBMLutilities.getDimensionIds(reaction);
			reactdimIDSizes = SBMLutilities.getDimensionSizes(reaction);
			reactdimIDSizes[0] = reaction.getId();
		} else {
			reactdimIDs = new String[]{""};
			reactdimIDSizes = Utils.checkSizeParameters(bioModel.getSBMLDocument(), reacID.getText(), false);
			if(reactdimIDSizes!=null){
				reactdimIDs = SBMLutilities.getDimensionIds("",reactdimIDSizes.length-1);
			}
		}
		
		modifiersPanel.add(new JLabel("Id:"));
		modifiersPanel.add(modifierId);
		modifiersPanel.add(new JLabel("Name:"));
		modifiersPanel.add(modifierName);
		modifiersPanel.add(speciesLabel);
		modifiersPanel.add(modifierSpecies);
		modifiersPanel.add(new JLabel("Indices:"));
		modifiersPanel.add(MiIndex);
		modifiersPanel.add(SBOTermsLabel);
		modifiersPanel.add(SBOTerms);
		if (production!=null) {
			if (SBOTerms.getSelectedItem().equals("promoter")) {
				modifierSpecies.setEnabled(false);
				SBOTerms.setEnabled(false);
			} else {
				String selectedSpecies = (String)modifierSpecies.getSelectedItem();

				modifiersPanel.add(RepStoichiometryLabel);
				repCooperativity = new JTextField();
				double nc = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue();
				repCooperativity.setText(""+nc);
				LocalParameter p = getChangedParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+selectedSpecies+"_r");
				if (p!=null) {
					repCooperativity.setText(""+p.getValue());
				}
				modifiersPanel.add(repCooperativity);
				modifiersPanel.add(RepBindingLabel);
				repBinding = new JTextField(bioModel.getParameter(GlobalConstants.FORWARD_KREP_STRING) + "/" + 
						bioModel.getParameter(GlobalConstants.REVERSE_KREP_STRING));
				LocalParameter kr_f = getChangedParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + selectedSpecies + "_"));
				LocalParameter kr_r = getChangedParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + selectedSpecies + "_"));
				if (kr_f!=null && kr_r!=null) {
					repBinding.setText(""+kr_f.getValue()+"/"+kr_r.getValue());
				}
				modifiersPanel.add(repBinding);

				modifiersPanel.add(ActStoichiometryLabel);
				actCooperativity = new JTextField();
				nc = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue();
				actCooperativity.setText(""+nc);
				p = getChangedParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+selectedSpecies+"_a");
				if (p!=null) {
					actCooperativity.setText(""+p.getValue());
				}
				modifiersPanel.add(actCooperativity);
				modifiersPanel.add(ActBindingLabel);
				actBinding = new JTextField(bioModel.getParameter(GlobalConstants.FORWARD_KACT_STRING) + "/" + 
						bioModel.getParameter(GlobalConstants.REVERSE_KACT_STRING));
				LocalParameter ka_f = getChangedParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + selectedSpecies + "_"));
				LocalParameter ka_r = getChangedParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + selectedSpecies + "_"));
				if (ka_f!=null && ka_r!=null) {
					actBinding.setText(""+ka_f.getValue()+"/"+ka_r.getValue());
				}
				modifiersPanel.add(actBinding);
			}
		}
		if (choices.length == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "There are no species availiable to be modifiers." + "\nAdd species to this sbml file first.",
					"No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, modifiersPanel, "Modifiers Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		String[] dex = new String[]{""};
		String[] dimensionIds = new String[]{""};
		String[] dimID = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			int index = -1;
			String modifierID = "";
			dimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), modifierId.getText(), true);
			if(dimID!=null){
				modifierID = dimID[0].trim();
				dimensionIds = SBMLutilities.getDimensionIds("m",dimID.length-1);
				if (modifierID.equals("")) {
					error = SBMLutilities.variableInUse(bioModel.getSBMLDocument(), selectedID, false, true, true);
				}
				else {
					error = Utils.checkID(bioModel.getSBMLDocument(), modifierID, selectedID, false);
				}
			}
			else{
				error = true;
			}
			if(!error){
				SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)modifierSpecies.getSelectedItem());
				dex = Utils.checkIndices(MiIndex.getText(), variable, bioModel.getSBMLDocument(), dimensionIds, "species", dimID, reactdimIDs, reactdimIDSizes);
				error = (dex==null);
			}
			if (modifier == null || !inSchematic) {
				if (option.equals("OK")) {
					index = modifiers.getSelectedIndex();
				}
				modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				modifierArray = Utility.getList(modifierArray, modifiers);
				modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index >= 0) {
					modifiers.setSelectedIndex(index);
				}
				/*
				for (int i = 0; i < modifierArray.length; i++) {
					if (i != index) {
						if (modifierArray[i].split(" ")[0].equals(modifierSpecies.getSelectedItem())) {
							error = true;
							JOptionPane.showMessageDialog(Gui.frame, "Unable to add species as a modifier.\n"
									+ "Each species can only be used as a modifier once.", "Species Can Only Be Used Once",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				*/
			}
			
			String mod = (String) modifierSpecies.getSelectedItem();
			double repCoop = 0.0;
			double actCoop = 0.0;
			double repBindf = 0.0;
			double repBindr = 1.0;
			double actBindf = 0.0;
			double actBindr = 1.0;
			if (production!=null) {
				try {
					repCoop = Double.parseDouble(repCooperativity.getText().trim());
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(Gui.frame, "The repression cooperativity must be a real number.", "Enter A Valid Value",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				try {
					repBindf = Double.parseDouble(repBinding.getText().trim().split("/")[0]);
					repBindr = Double.parseDouble(repBinding.getText().trim().split("/")[1]);
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(Gui.frame, "The repression binding must be a forward rate / reverse rate.", "Enter A Valid Value",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				try {
					actCoop = Double.parseDouble(actCooperativity.getText().trim());
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(Gui.frame, "The activation cooperativity must be a real number.", "Enter A Valid Value",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}			
				try {
					actBindf = Double.parseDouble(actBinding.getText().trim().split("/")[0]);
					actBindr = Double.parseDouble(actBinding.getText().trim().split("/")[1]);
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(Gui.frame, "The activation binding must be a forward rate / reverse rate.", "Enter A Valid Value",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					ModifierSpeciesReference modi = modifier;
					if (modifier == null || !inSchematic) {
						for (ModifierSpeciesReference m : changedModifiers) {
							if (m.getId().equals(selectedModifierId)||m.getSpecies().equals(selectedModifierId)) {
								if (modifierSpeciesReferenceMatch(m,(String)modifiers.getSelectedValue())) {
									modi = m;
									break;
								}
							}
						}
						modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						modifierArray = Utility.getList(modifierArray, modifiers);
						modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						//modifiers.setSelectedIndex(index);
					}
					if (modi==null) return;
					modi.setId(modifierID);
					modi.setName(modifierName.getText());
					modi.setSpecies((String) modifierSpecies.getSelectedItem());
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						modi.unsetSBOTerm();
					} else {
						modi.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					if (production!=null) {
						double nc = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue();
						String ncStr = GlobalConstants.COOPERATIVITY_STRING+"_"+mod+"_r";
						LocalParameter paramet = getChangedParameter(ncStr);
						if (paramet != null) {
							removeLocalParameter(ncStr);
						}
						if (nc!=repCoop) {
							addLocalParameter(ncStr,repCoop);
						}
						ncStr = GlobalConstants.COOPERATIVITY_STRING+"_"+mod+"_a";
						paramet = getChangedParameter(ncStr);
						if (paramet != null) {
							removeLocalParameter(ncStr);
						}
						if (nc!=actCoop) {
							addLocalParameter(ncStr,actCoop);
						}		
						double bindf = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.FORWARD_KREP_STRING).getValue();
						double bindr = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.REVERSE_KREP_STRING).getValue();
						LocalParameter kr_f = getChangedParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + mod + "_"));
						LocalParameter kr_r = getChangedParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + mod + "_"));
						if (kr_f != null) {
							removeLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + mod + "_"));
						}
						if (kr_r != null) {
							removeLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + mod + "_"));
						}
						if (repBindf!=bindf || repBindr!=bindr) {
							addLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + mod + "_"),repBindf);
							addLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + mod + "_"),repBindr);
						}
						bindf = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.FORWARD_KACT_STRING).getValue();
						bindr = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.REVERSE_KACT_STRING).getValue();
						LocalParameter ka_f = getChangedParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + mod + "_"));
						LocalParameter ka_r = getChangedParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + mod + "_"));
						if (ka_f != null) {
							removeLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + mod + "_"));
						}
						if (ka_r != null) {
							removeLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + mod + "_"));
						}
						if (actBindf!=bindf || actBindr!=bindr) {
							addLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + mod + "_"),actBindf);
							addLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + mod + "_"),actBindr);
						}
					}
					if (modifier == null || !inSchematic) {
						if (modifierId.getText().equals("")) {
							modifierArray[index] = mod + MiIndex.getText();
						} else {
							modifierArray[index] = modifierId.getText() + " : " + mod + MiIndex.getText();
						}
						dataModels.biomodel.util.Utility.sort(modifierArray);
						modifiers.setListData(modifierArray);
						modifiers.setSelectedIndex(index);
					}
					SBMLutilities.createDimensions(modifier, dimensionIds, dimID);
					SBMLutilities.addIndices(modifier, "species", dex, 1);
				}
				else {
					modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					modifierArray = Utility.getList(modifierArray, modifiers);
					modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					modifiers.setSelectedIndex(index);
					ModifierSpeciesReference modi = new ModifierSpeciesReference(bioModel.getSBMLDocument().getLevel(), bioModel.getSBMLDocument().getVersion());
					modi.setId(modifierID);
					modi.setName(modifierName.getText());
					changedModifiers.add(modi);
					modi.setSpecies(mod);
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						modi.unsetSBOTerm();
					} else {
						modi.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					if (production!=null) {
						double nc = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue();
						String ncStr = GlobalConstants.COOPERATIVITY_STRING+"_"+mod+"_r";
						LocalParameter paramet = getChangedParameter(ncStr);
						if (paramet != null) {
							removeLocalParameter(ncStr);
						}
						if (nc!=repCoop) {
							addLocalParameter(ncStr,repCoop);
						}
						ncStr = GlobalConstants.COOPERATIVITY_STRING+"_"+mod+"_a";
						paramet = getChangedParameter(ncStr);
						if (paramet != null) {
							removeLocalParameter(ncStr);
						}
						if (nc!=actCoop) {
							addLocalParameter(ncStr,actCoop);
						}		
						double bindf = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.FORWARD_KREP_STRING).getValue();
						double bindr = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.REVERSE_KREP_STRING).getValue();
						LocalParameter kr_f = getChangedParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + mod + "_"));
						LocalParameter kr_r = getChangedParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + mod + "_"));
						if (kr_f != null) {
							removeLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + mod + "_"));
						}
						if (kr_r != null) {
							removeLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + mod + "_"));
						}
						if (repBindf!=bindf || repBindr!=bindr) {
							addLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + mod + "_"),repBindf);
							addLocalParameter(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + mod + "_"),repBindr);
						}
						bindf = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.FORWARD_KACT_STRING).getValue();
						bindr = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.REVERSE_KACT_STRING).getValue();
						LocalParameter ka_f = getChangedParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + mod + "_"));
						LocalParameter ka_r = getChangedParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + mod + "_"));
						if (ka_f != null) {
							removeLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + mod + "_"));
						}
						if (ka_r != null) {
							removeLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + mod + "_"));
						}
						if (actBindf!=bindf || actBindr!=bindr) {
							addLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + mod + "_"),actBindf);
							addLocalParameter(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + mod + "_"),actBindr);
						}
					} 
					JList add = new JList();
					String addingStr;
					if (modifierId.getText().equals("")) {
						addingStr = mod + MiIndex.getText();
					} else {
						addingStr = modifierId.getText() + " : " + mod + MiIndex.getText();
					}
					Object[] adding = { addingStr };
					add.setListData(adding);
					add.setSelectedIndex(0);
					modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(modifierArray, modifiers, add);
					modifierArray = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						modifierArray[i] = (String) adding[i];
					}
					dataModels.biomodel.util.Utility.sort(modifierArray);
					modifiers.setListData(modifierArray);
					modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					try {
						if (bioModel.getSBMLDocument().getModel().getReaction(selectedReaction).getModifierCount() == 1) {
							modifiers.setSelectedIndex(0);
						}
						else {
							modifiers.setSelectedIndex(index);
						}
					}
					catch (Exception e2) {
						modifiers.setSelectedIndex(0);
					}
					SBMLutilities.createDimensions(modi, dimensionIds, dimID);
					SBMLutilities.addIndices(modi, "species", dex, 1);
				}
			}
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, modifiersPanel, "Modifiers Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}
	
	private static boolean speciesReferenceMatch(SpeciesReference ref,String refStr) {
		String[] split = refStr.split(" ");
		String id = "";
		String dim = "";
		String species = "";
		String index = "";
		String stoich = "";
		int offset = 0;
		if (refStr.contains(":")) {
			id = split[0].split("\\[")[0];
			if (split[0].contains("["))
				dim = split[0].substring(split[0].indexOf("["));
			offset = 2;
		}
		species = split[offset].split("\\[")[0];
		if (split[offset].contains("["))
			index = split[offset].substring(split[offset].indexOf("["));
		stoich = split[offset+1];
		if (ref.isSetId() && !ref.getId().equals(id)) return false;
		if (!ref.getSpecies().equals(species)) return false;
		if (ref.getStoichiometry() != Double.parseDouble(stoich)) return false;
		String refDim = SBMLutilities.getDimensionString(ref);
		if (!refDim.equals(dim)) return false;
		String refIndex = SBMLutilities.getIndicesString(ref, "species");
		if (!refIndex.equals(index)) return false;
		return true;
	}

	/**
	 * Creates a frame used to edit reactants or create new ones.
	 */
	public void reactantsEditor(BioModel gcm, String option, String selectedReactantId, SpeciesReference reactant, 
			boolean inSchematic, Reaction reaction) {
		JPanel reactantsPanel;
		reactantsPanel = new JPanel(new GridLayout(7, 2));
		JLabel reactantIdLabel = new JLabel("Id:");
		JLabel reactantNameLabel = new JLabel("Name:");
		JLabel speciesLabel = new JLabel("Species:");
		JLabel SBOTermLabel = new JLabel(GlobalConstants.SBOTERM);
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_PARTICIPANT_ROLE));
		Object[] stoiciOptions = { "Stoichiometry", "Stoichiometry Math" };
		stoiciLabel = new JComboBox(stoiciOptions);
		JLabel stoichiometryLabel = new JLabel("Stoichiometry:");
		JLabel constantLabel = new JLabel("Constant:");
		Object[] reactantConstantOptions = { "true", "false" };
		reactantConstant = new JComboBox(reactantConstantOptions);
		ListOf<Species> listOfSpecies = gcm.getSBMLDocument().getModel().getListOfSpecies();
		String[] speciesList = new String[gcm.getSBMLDocument().getModel().getSpeciesCount()];
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getSpeciesCount(); i++) {
			speciesList[i] = listOfSpecies.get(i).getId();
		}
		dataModels.biomodel.util.Utility.sort(speciesList);
		RiIndex = new JTextField(10);
		RiIndex.setEnabled(true);
		reactantSpecies = new JComboBox();
		reactantSpecies.addActionListener(this);
		reactantSpecies.setEnabled(true);
		for (int i = 0; i < speciesList.length; i++) {
			Species species = gcm.getSBMLDocument().getModel().getSpecies(speciesList[i]);
			if (species.getBoundaryCondition() || (!species.getConstant() && Rules.keepVarRateRule(gcm, "", speciesList[i]))) {
				reactantSpecies.addItem(speciesList[i]);
			}
		}
		reactantId = new JTextField("");
		reactantName = new JTextField("");
		reactantStoichiometry = new JTextField("1");
		String selectedID = "";
		if (complex!=null) {
			double nc = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue();
			reactantStoichiometry.setText(""+nc);
		}
		if (option.equals("OK")) {
			if (reactant == null) {
				for (SpeciesReference r : changedReactants) {
					if (r.getId().equals(selectedReactantId) || r.getSpecies().equals(selectedReactantId)) {
						if (speciesReferenceMatch(r,(String)reactants.getSelectedValue()))
							reactant = r;
					}
				}
			}
			if (reactant==null) return;
			reactantSpecies.setSelectedItem(reactant.getSpecies());
			if (reactant.isSetName()) {
				reactantName.setText(reactant.getName());
			}
			reactantStoichiometry.setText("" + reactant.getStoichiometry());
			if (reactant.isSetId()) {
				selectedID = reactant.getId();
				reactantId.setText(reactant.getId());
				InitialAssignment init = bioModel.getSBMLDocument().getModel().getInitialAssignment(selectedID);
				if (init!=null) {
					reactantStoichiometry.setText("" + bioModel.removeBooleans(init.getMath()));
				} 
			}
			if (reactant.isSetSBOTerm()) {
				SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(reactant.getSBOTermID()));
			}
			if (!reactant.getConstant()) {
				reactantConstant.setSelectedItem("false");
			}
			String dimInID = SBMLutilities.getDimensionString(reactant);
			reactantId.setText(reactantId.getText()+dimInID);
			String freshIndex = SBMLutilities.getIndicesString(reactant, "species");
			RiIndex.setText(freshIndex);			
			if (complex!=null) {
				if (complex.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+selectedID)!=null) {
					double nc = complex.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+selectedID).getValue();
					reactantStoichiometry.setText(""+nc);
				}
			}
		}
		String[] reactdimIDs = null; 
		String[] reactdimIDSizes = null;
		if (reaction!=null) {
			reactdimIDs = SBMLutilities.getDimensionIds(reaction);
			reactdimIDSizes = SBMLutilities.getDimensionSizes(reaction);
			reactdimIDSizes[0] = reaction.getId();
		} else {
			reactdimIDs = new String[]{""};
			reactdimIDSizes = Utils.checkSizeParameters(bioModel.getSBMLDocument(), reacID.getText(), false);
			if(reactdimIDSizes!=null){
				reactdimIDs = SBMLutilities.getDimensionIds("",reactdimIDSizes.length-1);
			}
		}
		
		reactantsPanel.add(reactantIdLabel);
		reactantsPanel.add(reactantId);
		reactantsPanel.add(reactantNameLabel);
		reactantsPanel.add(reactantName);
		reactantsPanel.add(speciesLabel);
		reactantsPanel.add(reactantSpecies);
		reactantsPanel.add(new JLabel("Indices:"));
		reactantsPanel.add(RiIndex);
		reactantsPanel.add(SBOTermLabel);
		reactantsPanel.add(SBOTerms);
		reactantsPanel.add(stoichiometryLabel);
		reactantsPanel.add(reactantStoichiometry);
		reactantsPanel.add(constantLabel);
		reactantsPanel.add(reactantConstant);
		if (speciesList.length == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "There are no species availiable to be reactants." + "\nAdd species to this sbml file first.",
					"No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, reactantsPanel, "Reactants Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		String[] dimID = new String[]{""};
		String[] dex = new String[]{""};
		String[] dimensionIds = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String react = "";
			double val = 1.0;
			String reactantID = "";
			dimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), reactantId.getText(), true);
			if(dimID!=null){
				reactantID = dimID[0].trim();
				dimensionIds = SBMLutilities.getDimensionIds("r",dimID.length-1);
				if (reactantID.equals("")) {
					error = SBMLutilities.variableInUse(gcm.getSBMLDocument(), selectedID, false, true, true);
				}
				else {
					error = Utils.checkID(gcm.getSBMLDocument(), reactantID, selectedID, false);
				}
			}
			else{
				error = true;
			}
			if(!error){
				SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)reactantSpecies.getSelectedItem());
				dex = Utils.checkIndices(RiIndex.getText(), variable, bioModel.getSBMLDocument(), dimensionIds, "species", dimID, reactdimIDs, reactdimIDSizes);
				error = (dex==null);
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
					InitialAssignments.removeInitialAssignment(bioModel, selectedID);
					try {
						val = Double.parseDouble(reactantStoichiometry.getText().trim());
					}
					catch (Exception e1) {
						if (reactantId.getText().equals("")) {
							JOptionPane.showMessageDialog(Gui.frame, "The stoichiometry must be a real number if no id is provided.", "Enter A Valid Value",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						} else {
							if (dimID.length>1) {
								JOptionPane.showMessageDialog(Gui.frame, "Initial assignments on arrayed reactants not currently allowed.", "Illegal Initial Assignment",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							} else {
								error = InitialAssignments.addInitialAssignment(bioModel, reactantStoichiometry.getText().trim(), dimID);
								val = 1.0;
							}
						}
					}
					if (val <= 0) {
						JOptionPane.showMessageDialog(Gui.frame, "The stoichiometry value must be greater than 0.", "Enter A Valid Value",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (reactantId.getText().equals(""))
						react = reactantSpecies.getSelectedItem() + RiIndex.getText() + " " + val;
					else 
						react = reactantId.getText() + " : " + reactantSpecies.getSelectedItem() + RiIndex.getText() + " " + val;
				}
				else {
					react = reactantSpecies.getSelectedItem() + RiIndex.getText() + " " + reactantStoichiometry.getText().trim();
				}
			}
			int index = -1;
			if (!error) {
				if (reactant == null || !inSchematic) {
					if (option.equals("OK")) {
						index = reactants.getSelectedIndex();
					}
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacta = Utility.getList(reacta, reactants);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index >= 0) {
						reactants.setSelectedIndex(index);
					}
					/*
					for (int i = 0; i < reacta.length; i++) {
						if (i != index) {
							if (reacta[i].split("\\[| ")[0].equals(reactantSpecies.getSelectedItem())) {
								error = true;
								JOptionPane.showMessageDialog(Gui.frame, "Unable to add species as a reactant.\n"
										+ "Each species can only be used as a reactant once.", "Species Can Only Be Used Once",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					*/
				}
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
					if (reactantStoichiometry.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry math must have formula.", "Enter Stoichiometry Formula",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (SBMLutilities.myParseFormula(reactantStoichiometry.getText().trim()) == null) {
						JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry formula is not valid.", "Enter Valid Formula",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						ArrayList<String> invalidVars = getInvalidVariablesInReaction(reactantStoichiometry.getText().trim(), dimensionIds, true, "", false);
						if (invalidVars.size() > 0) {
							String invalid = "";
							for (int i = 0; i < invalidVars.size(); i++) {
								if (i == invalidVars.size() - 1) {
									invalid += invalidVars.get(i);
								}
								else {
									invalid += invalidVars.get(i) + "\n";
								}
							}
							String message;
							message = "Stoiciometry math contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
							JTextArea messageArea = new JTextArea(message);
							messageArea.setLineWrap(true);
							messageArea.setWrapStyleWord(true);
							messageArea.setEditable(false);
							JScrollPane scrolls = new JScrollPane();
							scrolls.setMinimumSize(new java.awt.Dimension(300, 300));
							scrolls.setPreferredSize(new java.awt.Dimension(300, 300));
							scrolls.setViewportView(messageArea);
							JOptionPane.showMessageDialog(Gui.frame, scrolls, "Stoiciometry Math Error", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						if (!error) {
							error = Utils.checkNumFunctionArguments(gcm.getSBMLDocument(),
									SBMLutilities.myParseFormula(reactantStoichiometry.getText().trim()));
						}
						if (!error) {
							error = Utils.checkFunctionArgumentTypes(gcm.getSBMLDocument(),
									SBMLutilities.myParseFormula(reactantStoichiometry.getText().trim()));
						}
						if (!error) {
							if (SBMLutilities.returnsBoolean(SBMLutilities.myParseFormula(reactantStoichiometry.getText().trim()), bioModel.getSBMLDocument().getModel())) {
								JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry math must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error && option.equals("OK") && reactantConstant.getSelectedItem().equals("true")) {
				String id = selectedID;
				error = Utils.checkConstant(gcm.getSBMLDocument(), "Reactant stoiciometry", id);
			}
			if (!error) {
				if (option.equals("OK")) {
					SpeciesReference reactan = reactant;
					if (reactant == null || !inSchematic) {
						for (SpeciesReference r : changedReactants) {
							if (r.getId().equals(selectedReactantId) || r.getSpecies().equals(selectedReactantId)) {
								if (speciesReferenceMatch(r,(String)reactants.getSelectedValue())) {
									reactan = r;
									break;
								}
							}
						}
						reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						reacta = Utility.getList(reacta, reactants);
						reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					}
					if (reactan==null) return;
					reactan.setId(reactantID);
					reactan.setName(reactantName.getText().trim());
					reactan.setSpecies((String) reactantSpecies.getSelectedItem());
					reactan.setStoichiometry(val);
					if (complex!=null) {
						double nc = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue();
						String ncStr = GlobalConstants.COOPERATIVITY_STRING+"_"+reactan.getSpecies();
						LocalParameter paramet = null;
						for (LocalParameter p : changedParameters) {
							if (p.getId().equals(ncStr)) {
								paramet = p;
							}
						}
						if (nc==val) {
							if (paramet != null) {
								removeLocalParameter(ncStr);
							}
						} else {
							if (paramet != null) {
								removeLocalParameter(ncStr);
								addLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+reactan.getSpecies(),val);
							} else {
								addLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+reactan.getSpecies(),val);
							}
						}
					}
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						reactan.unsetSBOTerm();
					} else {
						reactan.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					if (reactantConstant.getSelectedItem().equals("true")) {
						reactan.setConstant(true);
					}
					else {
						reactan.setConstant(false);
					}
					if(!error){
						SBMLutilities.createDimensions(reactan, dimensionIds, dimID);
						SBMLutilities.addIndices(reactan, "species", dex, 1);
					} 
					if (reactant == null || !inSchematic) {
						reacta[index] = react;
						dataModels.biomodel.util.Utility.sort(reacta);
						reactants.setListData(reacta);
						reactants.setSelectedIndex(index);
					}
					try {
            SBMLutilities.updateVarId(gcm.getSBMLDocument(), false, selectedID, reactantID);
          } catch (BioSimException e1) {
            JOptionPane.showMessageDialog(Gui.frame,  e1.getMessage(), e1.getTitle(), JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
          }
					if (reactant == null || !inSchematic) {
						kineticLaw.setText(SBMLutilities.updateFormulaVar(kineticLaw.getText().trim(), selectedID, reactantID));
					}
				}
				else {
					// SpeciesReference reactan = new
					// SpeciesReference(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
					SpeciesReference reactan = new SpeciesReference(gcm.getSBMLDocument().getLevel(), gcm.getSBMLDocument().getVersion());
					reactan.setId(reactantID);
					reactan.setName(reactantName.getText().trim());
					reactan.setConstant(true);
					changedReactants.add(reactan);
					reactan.setSpecies((String) reactantSpecies.getSelectedItem());
					reactan.setStoichiometry(val);
					if (complex!=null) {
						double nc = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING).getValue();
						String ncStr = GlobalConstants.COOPERATIVITY_STRING+"_"+reactan.getSpecies();
						LocalParameter paramet = null;
						for (LocalParameter p : changedParameters) {
							if (p.getId().equals(ncStr)) {
								paramet = p;
							}
						}
						if (nc==val) {
							if (paramet != null) {
								removeLocalParameter(ncStr);
							}
						} else {
							if (paramet != null) {
								removeLocalParameter(ncStr);
								addLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+reactan.getSpecies(),val);
							} else {
								addLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+reactan.getSpecies(),val);
							}
						}
					}
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						reactan.unsetSBOTerm();
					} else {
						reactan.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					if (reactantConstant.getSelectedItem().equals("true")) {
						reactan.setConstant(true);
					}
					else {
						reactan.setConstant(false);
					}
					if(!error){
						SBMLutilities.createDimensions(reactan, dimensionIds, dimID);
						SBMLutilities.addIndices(reactan, "species", dex, 1);
					}
					JList add = new JList();
					Object[] adding = { react };
					add.setListData(adding);
					add.setSelectedIndex(0);
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(reacta, reactants, add);
					reacta = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						reacta[i] = (String) adding[i];
					}
					dataModels.biomodel.util.Utility.sort(reacta);
					reactants.setListData(reacta);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					reactants.setSelectedIndex(0);
				}
				modelEditor.setDirty(true);
				gcm.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, reactantsPanel, "Reactants Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Remove a reaction
	 */
	private void removeReaction() {
		int index = reactions.getSelectedIndex();
		if (index != -1) {
			String selected = ((String) reactions.getSelectedValue()).split("\\[| ")[0];
			Reaction reaction = bioModel.getSBMLDocument().getModel().getReaction(selected);
			if (BioModel.isProductionReaction(reaction)) {
				bioModel.removePromoter(SBMLutilities.getPromoterId(reaction));
			} else {
				bioModel.removeReaction(selected);
				reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				reacts = (String[]) Utility.remove(reactions, reacts);
				reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < reactions.getModel().getSize()) {
					reactions.setSelectedIndex(index);
				}
				else {
					reactions.setSelectedIndex(index - 1);
				}
			}
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
		}
	}

	/**
	 * Remove the reaction
	 */
	public static void removeTheReaction(BioModel gcm, String selected) {
		Reaction tempReaction = gcm.getSBMLDocument().getModel().getReaction(selected);
		ListOf<Reaction> r = gcm.getSBMLDocument().getModel().getListOfReactions();
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getReactionCount(); i++) {
			if (r.get(i).getId().equals(tempReaction.getId())) {
				r.remove(i);
			}
		}
	}

	/**
	 * Remove a reactant from a reaction
	 */
	private void removeReactant() {
		int index = reactants.getSelectedIndex();
		if (index != -1) {
			String v = ((String) reactants.getSelectedValue()).split("\\[| ")[0];
			for (int i = 0; i < changedReactants.size(); i++) {
				if ((changedReactants.get(i).getId().equals(v) || changedReactants.get(i).getSpecies().equals(v)) &&
						!SBMLutilities.variableInUse(bioModel.getSBMLDocument(), changedReactants.get(i).getId(), false, true,true)) {
					changedReactants.remove(i);
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacta = (String[]) Utility.remove(reactants, reacta);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index < reactants.getModel().getSize()) {
						reactants.setSelectedIndex(index);
					}
					else {
						reactants.setSelectedIndex(index - 1);
					}
					modelEditor.setDirty(true);
					bioModel.makeUndoPoint();
				}
			}
		}
	}

	/**
	 * Remove a product from a reaction
	 */
	private void removeProduct() {
		int index = products.getSelectedIndex();
		if (index != -1) {
			String v = ((String) products.getSelectedValue()).split("\\[| ")[0];
			for (int i = 0; i < changedProducts.size(); i++) {
				if ((changedProducts.get(i).getId().equals(v) || changedProducts.get(i).getSpecies().equals(v)) && 
						!SBMLutilities.variableInUse(bioModel.getSBMLDocument(), changedProducts.get(i).getId(), false, true, true)) {
					changedProducts.remove(i);
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					proda = (String[]) Utility.remove(products, proda);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index < products.getModel().getSize()) {
						products.setSelectedIndex(index);
					}
					else {
						products.setSelectedIndex(index - 1);
					}
					modelEditor.setDirty(true);
					bioModel.makeUndoPoint();
				}
			}
		}
	}

	/**
	 * Remove a modifier from a reaction
	 */
	private void removeModifier() {
		int index = modifiers.getSelectedIndex();
		if (index != -1) {
			String v = ((String) modifiers.getSelectedValue()).split("\\[| ")[0];
			for (int i = 0; i < changedModifiers.size(); i++) {
				if (changedModifiers.get(i).getId().equals(v) || changedModifiers.get(i).getSpecies().equals(v)) {
					if (!changedModifiers.get(i).isSetSBOTerm() || 
							changedModifiers.get(i).getSBOTerm()!=GlobalConstants.SBO_PROMOTER_MODIFIER) {
						changedModifiers.remove(i);
					} else {
						return;
					}
				}
			}
			modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			modifierArray = (String[]) Utility.remove(modifiers, modifierArray);
			modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < modifiers.getModel().getSize()) {
				modifiers.setSelectedIndex(index);
			}
			else {
				modifiers.setSelectedIndex(index - 1);
			}
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
		}
	}
	
	private static String indexedSpeciesRef(SimpleSpeciesReference reference) {
		String result = reference.getSpecies();
		result += SBMLutilities.getIndicesString(reference, "species");
		return result;
	}
		
	private static String indexedSpeciesRefId(SBMLDocument document,String reactionId,SimpleSpeciesReference reference) {
		String[] dimID = new String[]{""};
		String[] dimensionIds = new String[]{""};
		dimID = Utils.checkSizeParameters(document, reactionId, false);
		if (dimID==null) return null;
		dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
		String result = reference.getId();
		for(int i = dimensionIds.length-1; i >=0; i--){
			result += "[" + dimensionIds[i] + "]";
		}
		return result;
	}

	/**
	 * Remove a function if not in use
	 */
	private void useMassAction() {
		String kf;
		String kr;
		SBMLDocument doc = bioModel.getSBMLDocument();
		if (changedParameters.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to create mass action kinetic law.\n"
					+ "Requires at least one local parameter.", "Unable to Create Kinetic Law",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		else if (changedParameters.size() == 1) {
			kf = changedParameters.get(0).getId();
			kr = changedParameters.get(0).getId();
		}
		else {
			kf = changedParameters.get(0).getId();
			kr = changedParameters.get(1).getId();
		}
		String kinetic = kf;
		boolean addEquil = false;
		String equilExpr = "";
		for (SpeciesReference s : changedReactants) {
			if (SBMLutilities.hasDimensions(s)) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to create mass action kinetic law.\n"
						+ "Dimensions on species references not currently supported for use mass action button.", "Unable to Create Kinetic Law",
						JOptionPane.ERROR_MESSAGE);
				return;				
			}
			if (s.isSetId()) {
				addEquil = true;
				String stoichiometry = indexedSpeciesRefId(doc,reacID.getText(),s);
				if (stoichiometry==null) return;
				equilExpr += stoichiometry;
			}
			else {
				equilExpr += s.getStoichiometry();
			}
		}
		if (addEquil) {
			kinetic += " * pow(" + kf + "/" + kr + "," + equilExpr + "-2)";
		}
		for (SpeciesReference s : changedReactants) {
			if (s.isSetId()) {
				String stoichiometry = indexedSpeciesRefId(doc,reacID.getText(),s);
				if (stoichiometry==null) return;
				kinetic += " * pow(" + indexedSpeciesRef(s) + ", " + stoichiometry + ")";
			}
			else {
				if (s.getStoichiometry() == 1) {
					kinetic += " * " + indexedSpeciesRef(s);
				}
				else {
					kinetic += " * pow(" + indexedSpeciesRef(s) + ", " + s.getStoichiometry() + ")";
				}
			}
		}
		for (ModifierSpeciesReference s : changedModifiers) {
			if (SBMLutilities.hasDimensions(s)) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to create mass action kinetic law.\n"
						+ "Dimensions on species references not currently supported for use mass action button.", "Unable to Create Kinetic Law",
						JOptionPane.ERROR_MESSAGE);
				return;				
			}
			kinetic += " * " + indexedSpeciesRef(s);
		}
		if (reacReverse.getSelectedItem().equals("true")) {
			kinetic += " - " + kr;
			addEquil = false;
			equilExpr = "";
			for (SpeciesReference s : changedProducts) {
				if (SBMLutilities.hasDimensions(s)) {
					JOptionPane.showMessageDialog(Gui.frame, "Unable to create mass action kinetic law.\n"
							+ "Dimensions on species references not currently supported for use mass action button.", "Unable to Create Kinetic Law",
							JOptionPane.ERROR_MESSAGE);
					return;				
				}
				if (s.isSetId()) {
					addEquil = true;
					String stoichiometry = indexedSpeciesRefId(doc,reacID.getText(),s);
					if (stoichiometry==null) return;
					equilExpr += stoichiometry;
				}
				else {
					equilExpr += s.getStoichiometry();
				}
			}
			if (addEquil) {
				kinetic += " * pow(" + kf + "/" + kr + "," + equilExpr + "-1)";
			}
			for (SpeciesReference s : changedProducts) {
				if (s.isSetId()) {
					String stoichiometry = indexedSpeciesRefId(doc,reacID.getText(),s);
					if (stoichiometry==null) return;
					kinetic += " * pow(" + indexedSpeciesRef(s) + ", " + stoichiometry + ")";
				}
				else {
					if (s.getStoichiometry() == 1) {
						kinetic += " * " + indexedSpeciesRef(s);
					}
					else {
						kinetic += " * pow(" + indexedSpeciesRef(s) + ", " + s.getStoichiometry() + ")";
					}
				}
			}
			for (ModifierSpeciesReference s : changedModifiers) {
				kinetic += " * " + indexedSpeciesRef(s);
			}
		}
		kineticLaw.setText(kinetic);
		modelEditor.setDirty(true);
		bioModel.makeUndoPoint();
	}

	/**
	 * Remove a reaction parameter, if allowed
	 */
	private void reacRemoveParam() {
		int index = reacParameters.getSelectedIndex();
		if (index != -1) {
			String v = ((String) reacParameters.getSelectedValue()).split("\\[| ")[0];
			if (reactions.getSelectedIndex() != -1) {
				String kinetic = kineticLaw.getText().trim();
				String[] vars = new String[0];
				if (!kinetic.equals("")) {
					vars = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(kineticLaw.getText().trim())).split(" |\\(|\\)|\\,");
				}
				for (int j = 0; j < vars.length; j++) {
					if (vars[j].equals(v)) {
						JOptionPane.showMessageDialog(Gui.frame, "Cannot remove reaction parameter because it is used in the kinetic law.",
								"Cannot Remove Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			for (int i = 0; i < changedParameters.size(); i++) {
				if (changedParameters.get(i).getId().equals(v)) {
					changedParameters.remove(i);
				}
			}
			thisReactionParams.remove(v);
			reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			reacParams = (String[]) Utility.remove(reacParameters, reacParams);
			reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < reacParameters.getModel().getSize()) {
				reacParameters.setSelectedIndex(index);
			}
			else {
				reacParameters.setSelectedIndex(index - 1);
			}
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
		}
	}

	private void removeLocalParameter(String v) {
		for (int i = 0; i < reacParameters.getModel().getSize(); i++) {
			if (((String)reacParameters.getModel().getElementAt(i)).split("\\[| ")[0].equals(v)) {
				reacParameters.setSelectedIndex(i);
				break;
			}
		}
		for (int i = 0; i < changedParameters.size(); i++) {
			if (changedParameters.get(i).getId().equals(v)) {
				changedParameters.remove(i);
			}
		}
		thisReactionParams.remove(v);
		reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		reacParams = (String[]) Utility.remove(reacParameters, reacParams);
		reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reacParameters.setSelectedIndex(0);
		modelEditor.setDirty(true);
		bioModel.makeUndoPoint();		
	}

	/**
	 * Check the units of a kinetic law
	 */
	public boolean checkKineticLawUnits(KineticLaw law) {
		if (Gui.getCheckUndeclared()) {
			if (law.containsUndeclaredUnits()) {
				JOptionPane.showMessageDialog(Gui.frame, "Kinetic law contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (Gui.getCheckUnits()) {
			if (SBMLutilities.checkUnitsInKineticLaw(bioModel.getSBMLDocument(), law)) {
				JOptionPane.showMessageDialog(Gui.frame, "Kinetic law units should be substance / time.", "Units Do Not Match",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks the string to see if there are any errors. Retruns true if there are no errors, else returns false.
	 */
//	private static boolean fluxBoundisGood(String s, String reactionId){
//		if(s.contains("<=")){
//			String [] correctnessTest = s.split("<=");
//			if(correctnessTest.length == 3){
//				try{
//					Double.parseDouble(correctnessTest[0]);
//				}
//				catch(NumberFormatException e){
//					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0]+ " has to be a double.",
//							"Incorrect Element", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				try{
//					Double.parseDouble(correctnessTest[2]);
//				}
//				catch(NumberFormatException e){
//					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[2] + " has to be a double.", 
//							"Incorrect Element", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				String id = correctnessTest[1];
//				if (id.contains("[")) {
//					id = id.substring(0,id.indexOf("["));
//				} 
//				if(!id.equals(reactionId)){
//					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
//							JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				if(Double.parseDouble(correctnessTest[0]) > Double.parseDouble(correctnessTest[2])){
//					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0] + " must be less than " + correctnessTest[2], 
//							"Imbalance with Bounds", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				return true;
//			}
//			else if(correctnessTest.length == 2){
//				String id0 = correctnessTest[0];
//				if (id0.contains("[")) {
//					id0 = id0.substring(0,id0.indexOf("["));
//				} 
//				String id1 = correctnessTest[1];
//				if (id1.contains("[")) {
//					id1 = id1.substring(0,id1.indexOf("["));
//				} 
//				if(!id0.equals(reactionId) && !id1.equals(reactionId)){
//					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
//							JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				if(correctnessTest[0].equals(reactionId)){
//					try{
//						Double.parseDouble(correctnessTest[1]);
//					}
//					catch(Exception e){
//						if(e.equals(new NumberFormatException())){
//							JOptionPane.showMessageDialog(Gui.frame, correctnessTest[1] + " has to be a double.", "Incorrect Element",
//									JOptionPane.ERROR_MESSAGE);
//							return false;
//						}
//					}
//				}
//				else{
//					try{
//						Double.parseDouble(correctnessTest[0]);
//					}
//					catch(Exception e){
//						if(e.equals(new NumberFormatException())){
//							JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0] + " has to be a double.", "Incorrect Element",
//									JOptionPane.ERROR_MESSAGE);
//							return false;
//						}
//					}
//				}
//				return true;
//			} else{
//				JOptionPane.showMessageDialog(Gui.frame, "Wrong number of elements.", "Bad Format",
//						JOptionPane.ERROR_MESSAGE);
//				return false;
//			}
//		}
//		else if(s.contains(">=")){
//			String [] correctnessTest = s.split(">=");
//			if(correctnessTest.length == 3){
//				try{
//					Double.parseDouble(correctnessTest[0]);
//				}
//				catch(NumberFormatException e){
//					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0]+ " has to be a double.",
//							"Incorrect Element", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				try{
//					Double.parseDouble(correctnessTest[2]);
//				}
//				catch(NumberFormatException e){
//					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[2] + " has to be a double.", 
//							"Incorrect Element", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				String id = correctnessTest[1];
//				if (id.contains("[")) {
//					id = id.substring(0,id.indexOf("["));
//				} 
//				if(!id.equals(reactionId)){
//					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
//							JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				if(Double.parseDouble(correctnessTest[0]) < Double.parseDouble(correctnessTest[2])){
//					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0] + " must be greater than " + correctnessTest[2], 
//							"Imbalance with Bounds", JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				return true;
//			}
//			else if(correctnessTest.length == 2){
//				String id0 = correctnessTest[0];
//				if (id0.contains("[")) {
//					id0 = id0.substring(0,id0.indexOf("["));
//				} 
//				String id1 = correctnessTest[1];
//				if (id1.contains("[")) {
//					id1 = id1.substring(0,id1.indexOf("["));
//				} 
//				if(!id0.equals(reactionId) && !id1.equals(reactionId)){
//					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
//							JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				if(correctnessTest[0].equals(reactionId)){
//					try{
//						Double.parseDouble(correctnessTest[1]);
//					}
//					catch(Exception e){
//						if(e.equals(new NumberFormatException())){
//							JOptionPane.showMessageDialog(Gui.frame, correctnessTest[1] + " has to be a double.", "Incorrect Element",
//									JOptionPane.ERROR_MESSAGE);
//							return false;
//						}
//					}
//				}
//				else{
//					try{
//						Double.parseDouble(correctnessTest[0]);
//					}
//					catch(Exception e){
//						if(e.equals(new NumberFormatException())){
//							JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0] + " has to be a double.", "Incorrect Element",
//									JOptionPane.ERROR_MESSAGE);
//							return false;
//						}
//					}
//				}
//				return true;
//			} else{
//				JOptionPane.showMessageDialog(Gui.frame, "Wrong number of elements.", "Bad Format",
//						JOptionPane.ERROR_MESSAGE);
//				return false;
//			}
//		}
//		else if(s.contains("=")){
//			String [] correctnessTest = s.split("=");
//			if(correctnessTest.length == 2){
//				String id0 = correctnessTest[0];
//				if (id0.contains("[")) {
//					id0 = id0.substring(0,id0.indexOf("["));
//				} 
//				String id1 = correctnessTest[1];
//				if (id1.contains("[")) {
//					id1 = id1.substring(0,id1.indexOf("["));
//				} 
//				if(!id0.equals(reactionId) && !id1.equals(reactionId)){
//					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
//							JOptionPane.ERROR_MESSAGE);
//					return false;
//				}
//				if(correctnessTest[0].equals(reactionId)){
//					try{
//						Double.parseDouble(correctnessTest[1]);
//					}
//					catch(Exception e){
//						if(e.equals(new NumberFormatException())){
//							JOptionPane.showMessageDialog(Gui.frame, correctnessTest[1] + " has to be a double.", "Incorrect Element",
//									JOptionPane.ERROR_MESSAGE);
//							return false;
//						}
//					}
//				}
//				else{
//					try{
//						Double.parseDouble(correctnessTest[0]);
//					}
//					catch(Exception e){
//						if(e.equals(new NumberFormatException())){
//							JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0] + " has to be a double.", "Incorrect Element",
//									JOptionPane.ERROR_MESSAGE);
//							return false;
//						}
//					}
//				}
//				return true;
//			} 
//			JOptionPane.showMessageDialog(Gui.frame, "Wrong number of elements.", "Bad Format",
//					JOptionPane.ERROR_MESSAGE);
//			return false;
//		}
//		else{
//			JOptionPane.showMessageDialog(Gui.frame, "Need Operations.", "Bad Format",
//					JOptionPane.ERROR_MESSAGE);
//			return false;
//		}
//	}
	
	/**
	 * Checks the string to see if there are any errors. Retruns true if there are no errors, else returns false.
	 */
	public static boolean isFluxBoundValid(SBMLDocument document, String s, String reactionId, 
			String[] dimensionIds, String[] dimId){
		if(s.contains("<=")){
			String [] correctnessTest = s.split("<=");
			if(correctnessTest.length == 3){
				if (!checkFluxBound(document,correctnessTest[0],"fbc:lowerFluxBound",dimensionIds,dimId)) {
					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0]+ " is not a valid parameter.",
							"Invalid Bound", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (!checkFluxBound(document,correctnessTest[2],"fbc:upperFluxBound",dimensionIds,dimId)) {
					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[2]+ " is not a valid parameter.",
							"Invalid Bound", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				String id = correctnessTest[1];
				if (id.contains("[")) {
					id = id.substring(0,id.indexOf("["));
				} 
				if(!id.equals(reactionId)){
					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				/* TODO: should check parameter values
				if(Double.parseDouble(correctnessTest[0]) > Double.parseDouble(correctnessTest[2])){
					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0] + " must be less than " + correctnessTest[2], 
							"Imbalance with Bounds", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				*/
				return true;
			}
			else if(correctnessTest.length == 2){
				String id0 = correctnessTest[0];
				if (id0.contains("[")) {
					id0 = id0.substring(0,id0.indexOf("["));
				} 
				String id1 = correctnessTest[1];
				if (id1.contains("[")) {
					id1 = id1.substring(0,id1.indexOf("["));
				} 
				if(!id0.equals(reactionId) && !id1.equals(reactionId)){
					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if(correctnessTest[0].equals(reactionId)){
					if (!checkFluxBound(document,correctnessTest[1],"fbc:upperFluxBound",dimensionIds,dimId)) {
						JOptionPane.showMessageDialog(Gui.frame, correctnessTest[1]+ " is not a valid parameter.",
								"Invalid Bound", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				else{
					if (!checkFluxBound(document,correctnessTest[0],"fbc:lowerFluxBound",dimensionIds,dimId)) {
						JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0]+ " is not a valid parameter.",
								"Invalid Bound", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				return true;
			} else{
				JOptionPane.showMessageDialog(Gui.frame, "Wrong number of elements.", "Bad Format",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		else if(s.contains(">=")){
			String [] correctnessTest = s.split(">=");
			if(correctnessTest.length == 3){
				if (!checkFluxBound(document,correctnessTest[0],"fbc:upperFluxBound",dimensionIds,dimId)) {
					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0]+ " is not a valid parameter.",
							"Invalid Bound", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (!checkFluxBound(document,correctnessTest[2],"fbc:lowerFluxBound",dimensionIds,dimId)) {
					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[2]+ " is not a valid parameter.",
							"Invalid Bound", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				String id = correctnessTest[1];
				if (id.contains("[")) {
					id = id.substring(0,id.indexOf("["));
				} 
				if(!id.equals(reactionId)){
					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				/* TODO: need to update
				if(Double.parseDouble(correctnessTest[0]) < Double.parseDouble(correctnessTest[2])){
					JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0] + " must be greater than " + correctnessTest[2], 
							"Imbalance with Bounds", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				*/
				return true;
			}
			else if(correctnessTest.length == 2){
				String id0 = correctnessTest[0];
				if (id0.contains("[")) {
					id0 = id0.substring(0,id0.indexOf("["));
				} 
				String id1 = correctnessTest[1];
				if (id1.contains("[")) {
					id1 = id1.substring(0,id1.indexOf("["));
				} 
				if(!id0.equals(reactionId) && !id1.equals(reactionId)){
					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if(correctnessTest[0].equals(reactionId)){
					if (!checkFluxBound(document,correctnessTest[1],"fbc:lowerFluxBound",dimensionIds,dimId)) {
						JOptionPane.showMessageDialog(Gui.frame, correctnessTest[1]+ " is not a valid parameter.",
								"Invalid Bound", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				else{
					if (!checkFluxBound(document,correctnessTest[0],"fbc:upperFluxBound",dimensionIds,dimId)) {
						JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0]+ " is not a valid parameter.",
								"Invalid Bound", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				return true;
			} else{
				JOptionPane.showMessageDialog(Gui.frame, "Wrong number of elements.", "Bad Format",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		else if(s.contains("=")){
			String [] correctnessTest = s.split("=");
			if(correctnessTest.length == 2){
				String id0 = correctnessTest[0];
				if (id0.contains("[")) {
					id0 = id0.substring(0,id0.indexOf("["));
				} 
				String id1 = correctnessTest[1];
				if (id1.contains("[")) {
					id1 = id1.substring(0,id1.indexOf("["));
				} 
				if(!id0.equals(reactionId) && !id1.equals(reactionId)){
					JOptionPane.showMessageDialog(Gui.frame, "Must have "+ reactionId + " in the equation.", "No Reaction",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if(correctnessTest[0].equals(reactionId)){
					if (!checkFluxBound(document,correctnessTest[1],"fbc:lowerFluxBound",dimensionIds,dimId)||
							!checkFluxBound(document,correctnessTest[1],"fbc:upperFluxBound",dimensionIds,dimId)) {
						JOptionPane.showMessageDialog(Gui.frame, correctnessTest[1]+ " is not a valid parameter.",
								"Invalid Bound", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				else{
					if (!checkFluxBound(document,correctnessTest[0],"fbc:lowerFluxBound",dimensionIds,dimId)||
							!checkFluxBound(document,correctnessTest[0],"fbc:upperFluxBound",dimensionIds,dimId)) {
						JOptionPane.showMessageDialog(Gui.frame, correctnessTest[0]+ " is not a valid parameter.",
								"Invalid Bound", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				return true;
			} 
			JOptionPane.showMessageDialog(Gui.frame, "Wrong number of elements.", "Bad Format",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		else{
			JOptionPane.showMessageDialog(Gui.frame, "Need Operations.", "Bad Format",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add compartment type button is clicked
		// if the add species type button is clicked
		// if the add compartment button is clicked
		// if the add parameters button is clicked
		// if the add reactions button is clicked
		if (e.getSource() == addReac) {
			reactionsEditor(bioModel, "Add", "", false);
		}
		// if the edit reactions button is clicked
		else if (e.getSource() == editReac) {
			if (reactions.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No reaction selected.", "Must Select A Reaction", JOptionPane.ERROR_MESSAGE);
				return;
			}
			reactionsEditor(bioModel, "OK", ((String) reactions.getSelectedValue()).split("\\[| ")[0], false);
			initialsPanel.refreshInitialAssignmentPanel(bioModel);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove reactions button is clicked
		else if (e.getSource() == removeReac) {
			removeReaction();
		}
		// if the add reactions parameters button is clicked
		else if (e.getSource() == reacAddParam) {
			reacParametersEditor(bioModel,"Add");
		}
		// if the edit reactions parameters button is clicked
		else if (e.getSource() == reacEditParam) {
			reacParametersEditor(bioModel,"OK");
		}
		// if the remove reactions parameters button is clicked
		else if (e.getSource() == reacRemoveParam) {
			reacRemoveParam();
		}
		// if the add reactants button is clicked
		else if (e.getSource() == addReactant) {
			reactantsEditor(bioModel, "Add", "", null, false, null);
		}
		// if the edit reactants button is clicked
		else if (e.getSource() == editReactant) {
			if (reactants.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No reactant selected.", "Must Select A Reactant", JOptionPane.ERROR_MESSAGE);
				return;
			}
			reactantsEditor(bioModel, "OK", ((String) reactants.getSelectedValue()).split("\\[| ")[0], null, false, null);
			initialsPanel.refreshInitialAssignmentPanel(bioModel);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove reactants button is clicked
		else if (e.getSource() == removeReactant) {
			removeReactant();
		}
		// if the add products button is clicked
		else if (e.getSource() == addProduct) {
			productsEditor(bioModel, "Add", "", null, false, null);
		}
		// if the edit products button is clicked
		else if (e.getSource() == editProduct) {
			if (products.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No product selected.", "Must Select A Product", JOptionPane.ERROR_MESSAGE);
				return;
			}
			productsEditor(bioModel, "OK", ((String) products.getSelectedValue()).split("\\[| ")[0], null, false, null);
			initialsPanel.refreshInitialAssignmentPanel(bioModel);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove products button is clicked
		else if (e.getSource() == removeProduct) {
			removeProduct();
		}
		// if the add modifiers button is clicked
		else if (e.getSource() == addModifier) {
			modifiersEditor(bioModel, "Add", "", null, false, null);
		}
		// if the edit modifiers button is clicked
		else if (e.getSource() == editModifier) {
			if (modifiers.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No modifier selected.", "Must Select A Modifier", JOptionPane.ERROR_MESSAGE);
				return;
			}
			modifiersEditor(bioModel,"OK", ((String) modifiers.getSelectedValue()).split("\\[| ")[0], null, false, null);
		}
		// if the remove modifiers button is clicked
		else if (e.getSource() == removeModifier) {
			removeModifier();
		}
		// if the clear button is clicked
		else if (e.getSource() == clearKineticLaw) {
			kineticLaw.setText("");
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
		}
		// if the use mass action button is clicked
		else if (e.getSource() == useMassAction) {
			useMassAction();
		}
		else if (e.getSource() == reactionComp) {
			if (bioModel.isArray((String)reactionComp.getSelectedItem())) {
				CiIndex.setEnabled(true);
			} else {
				CiIndex.setText("");
				CiIndex.setEnabled(false);
			}
		}
		else if (e.getSource() == reactantSpecies){
			if (bioModel.isArray((String)reactantSpecies.getSelectedItem())) {
				RiIndex.setEnabled(true);
			} else {
				RiIndex.setText("");
				RiIndex.setEnabled(false);
			}
		}
		else if (e.getSource() == modifierSpecies){
			if (bioModel.isArray((String)modifierSpecies.getSelectedItem())) {
				MiIndex.setEnabled(true);
			} else {
				MiIndex.setText("");
				MiIndex.setEnabled(false);
			}
		}
		else if (e.getSource() == productSpecies){
			if (bioModel.isArray((String)productSpecies.getSelectedItem())) {
				PiIndex.setEnabled(true);
			} else {
				PiIndex.setText("");
				PiIndex.setEnabled(false);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == reactions) {
				if (reactions.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No reaction selected.", "Must Select A Reaction", JOptionPane.ERROR_MESSAGE);
					return;
				}
				reactionsEditor(bioModel, "OK", ((String) reactions.getSelectedValue()).split("\\[| ")[0], false);
				initialsPanel.refreshInitialAssignmentPanel(bioModel);
				rulesPanel.refreshRulesPanel();
			}
			else if (e.getSource() == reacParameters) {
				reacParametersEditor(bioModel,"OK");
			}
			else if (e.getSource() == reactants) {
				if (!paramsOnly) {
					if (reactants.getSelectedIndex() == -1) {
						JOptionPane.showMessageDialog(Gui.frame, "No reactant selected.", "Must Select A Reactant", JOptionPane.ERROR_MESSAGE);
						return;
					}
					reactantsEditor(bioModel, "OK", ((String) reactants.getSelectedValue()).split("\\[| ")[0], null, false, null);
					initialsPanel.refreshInitialAssignmentPanel(bioModel);
					rulesPanel.refreshRulesPanel();
				}
			}
			else if (e.getSource() == products) {
				if (!paramsOnly) {
					if (products.getSelectedIndex() == -1) {
						JOptionPane.showMessageDialog(Gui.frame, "No product selected.", "Must Select A Product", JOptionPane.ERROR_MESSAGE);
						return;
					}
					productsEditor(bioModel, "OK", ((String) products.getSelectedValue()).split("\\[| ")[0], null, false, null);
					initialsPanel.refreshInitialAssignmentPanel(bioModel);
					rulesPanel.refreshRulesPanel();
				}
			}
			else if (e.getSource() == modifiers) {
				if (!paramsOnly) {
					if (modifiers.getSelectedIndex() == -1) {
						JOptionPane.showMessageDialog(Gui.frame, "No modifier selected.", "Must Select A Modifier", JOptionPane.ERROR_MESSAGE);
						return;
					}
					modifiersEditor(bioModel,"OK", ((String) modifiers.getSelectedValue()).split("\\[| ")[0], null, false, null);
				}
			}
		}
	}

	/**
	 * Refresh reaction panel
	 */
	public void refreshReactionPanel(BioModel gcm) {
		String selectedReactionId = "";
		if (!reactions.isSelectionEmpty()) {
			selectedReactionId = ((String) reactions.getSelectedValue()).split("\\[| ")[0];
		}
		this.bioModel = gcm;
		Model model = gcm.getSBMLDocument().getModel();
		ListOf<Reaction> listOfReactions = model.getListOfReactions();
		reacts = new String[model.getReactionCount()];
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = listOfReactions.get(i);
			reacts[i] = reaction.getId() + SBMLutilities.getDimensionString(reaction);
			if (paramsOnly) {
				if (!reaction.isSetKineticLaw()) continue;
				ListOf<LocalParameter> params = reaction.getKineticLaw().getListOfLocalParameters();
				for (int j = 0; j < reaction.getKineticLaw().getLocalParameterCount(); j++) {
					LocalParameter paramet = (params.get(j));
					for (int k = 0; k < parameterChanges.size(); k++) {
						if (parameterChanges.get(k).split(" ")[0].equals(reaction.getId() + "/" + paramet.getId())) {
							String[] splits = parameterChanges.get(k).split(" ");
							if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
								String value = splits[splits.length - 1];
								paramet.setValue(Double.parseDouble(value));
							}
							else if (splits[splits.length - 2].equals("Sweep")) {
								String value = splits[splits.length - 1];
								paramet.setValue(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							}
							if (!reacts[i].contains("Modified")) {
								reacts[i] += " Modified";
							}
						}
					}
				}
			}
		}
		dataModels.biomodel.util.Utility.sort(reacts);
		int selected = 0;
		for (int i = 0; i < reacts.length; i++) {
			if (reacts[i].split(" ")[0].equals(selectedReactionId)) {
				selected = i;
			}
		}
		reactions.setListData(reacts);
		reactions.setSelectedIndex(selected);
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
