package sbmleditor;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import org.sbml.libsbml.*;
import biomodelsim.*;
import reb2sac.*;
import buttons.*;

/**
 * This is the SBML_Editor class. It takes in an sbml file and allows the user
 * to edit it by changing different fields displayed in a frame. It also
 * implements the ActionListener class, the MouseListener class, and the
 * KeyListener class which allows it to perform certain actions when buttons are
 * clicked on the frame, when one of the JList's items is double clicked, or
 * when text is entered into the model's ID.
 * 
 * @author Curtis Madsen
 */
public class SBML_Editor extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 8236967001410906807L;

	private SBMLDocument document; // sbml document

	private String file; // SBML file

	/*
	 * compartment buttons
	 */
	private JButton addCompart, removeCompart, editCompart;

	private JButton addFunction, removeFunction, editFunction;

	private JButton addUnit, removeUnit, editUnit;

	private JButton addList, removeList, editList;

	private JButton addCompType, removeCompType, editCompType;

	private JButton addSpecType, removeSpecType, editSpecType;

	private JButton addInit, removeInit, editInit;

	private JButton addRule, removeRule, editRule;

	private JButton addEvent, removeEvent, editEvent;

	private JButton addAssignment, removeAssignment, editAssignment;

	private JButton addConstraint, removeConstraint, editConstraint;

	private String[] comps; // array of compartments

	private String[] funcs; // array of functions

	private String[] units; // array of units

	private String[] uList; // unit list array

	private String[] cpTyp; // array of compartment types

	private String[] spTyp; // array of species types

	private String[] inits; // array of initial assignments

	private String[] rul; // array of rules

	private String[] ev; // array of events

	private String[] assign; // array of event assignments

	private String[] cons; // array of constraints

	private JList compartments; // JList of compartments

	private JList functions; // JList of functions

	private JList unitDefs; // JList of units

	private JList unitList; // unit JList

	private JList compTypes; // JList of compartment types

	private JList specTypes; // JList of species types

	private JList initAssigns; // JList of initial assignments

	private JList rules; // JList of rules

	private JList events; // JList of events

	private JList eventAssign; // JList of event assignments

	private JList constraints; // JList of constraints

	private JTextField compID, compSize, compName; // compartment fields;

	private JTextField funcID, funcName, eqn, args; // function fields;

	private JTextField unitID, unitName; // unit defn fields;

	private JTextField exp, scale, mult; // unit list fields;

	private JTextField compTypeID, compTypeName; // compartment type fields;

	private JTextField specTypeID, specTypeName; // species type fields;

	private JComboBox initVar; // init fields;

	private JTextField initMath; // init fields;

	private JComboBox ruleVar; // rule fields;

	private JTextField ruleMath; // rule fields;

	private JTextField eventID, eventTrigger, eventDelay; // event fields;

	private JComboBox eaID; // event assignment fields;

	private JTextField consMath, consMessage; // constraints fields;

	private JButton addSpec, removeSpec, editSpec; // species buttons

	private String[] specs; // array of species

	private JList species; // JList of species

	private JTextField ID, init, Name; // species text fields

	private JComboBox compUnits, compOutside, compConstant; // compartment units

	// combo box

	private JComboBox compTypeBox, dimBox; // compartment type combo box

	private JComboBox specTypeBox, specBoundary, specConstant; // species combo

	// boxes

        private JComboBox specUnits, initLabel, stoiciLabel;

	private JComboBox comp; // compartment combo box

	private boolean change; // determines if any changes were made

	private JTextField modelID; // the model's ID

	private JTextField modelName; // the model's Name

	private JList reactions; // JList of reactions

	private String[] reacts; // array of reactions

	/*
	 * reactions buttons
	 */
	private JButton addReac, removeReac, editReac, copyReac;

	private JList parameters; // JList of parameters

	private String[] params; // array of parameters

	private JButton addParam, removeParam, editParam; // parameters buttons

	/*
	 * parameters text fields
	 */
	private JTextField paramID, paramName, paramValue;

	private JComboBox paramUnits;

	private JComboBox paramConst;

	private JList reacParameters; // JList of reaction parameters

	private String[] reacParams; // array of reaction parameters

	/*
	 * reaction parameters buttons
	 */
	private JButton reacAddParam, reacRemoveParam, reacEditParam;

	/*
	 * reaction parameters text fields
	 */
	private JTextField reacParamID, reacParamValue, reacParamName;

	private JComboBox reacParamUnits;

	private ArrayList<Parameter> changedParameters; // ArrayList of parameters

	private JTextField reacID, reacName; // reaction name and id text

	// fields

	private JComboBox reacReverse, reacFast; // reaction reversible, fast combo

	// boxes

	/*
	 * reactant buttons
	 */
	private JButton addReactant, removeReactant, editReactant;

	private JList reactants; // JList for reactants

	private String[] reacta; // array for reactants

	/*
	 * ArrayList of reactants
	 */
	private ArrayList<SpeciesReference> changedReactants;

	/*
	 * product buttons
	 */
	private JButton addProduct, removeProduct, editProduct;

	private JList products; // JList for products

	private String[] product; // array for products

	/*
	 * ArrayList of products
	 */
	private ArrayList<SpeciesReference> changedProducts;

	/*
	 * modifier buttons
	 */
	private JButton addModifier, removeModifier, editModifier;

	private JList modifiers; // JList for modifiers

	private String[] modifier; // array for modifiers

	/*
	 * ArrayList of modifiers
	 */
        private ArrayList<ModifierSpeciesReference> changedModifiers;

	private JComboBox productSpecies; // ComboBox for product editing

	private JComboBox modifierSpecies; // ComboBox for modifier editing

	private JTextField productStoiciometry; // text field for editing products

	private JComboBox reactantSpecies; // ComboBox for reactant editing

	/*
	 * text field for editing reactants
	 */
	private JTextField reactantStoiciometry;

	private JTextArea kineticLaw; // text area for editing kinetic law

	private String kineticL; // kinetic law

	private Reb2Sac reb2sac; // reb2sac options

	private JButton saveNoRun, run, saveAs; // save and run buttons

	private Log log;

	private ArrayList<String> usedIDs;

	private ArrayList<String> thisReactionParams;

	private BioSim biosim;

	private JButton useMassAction, clearKineticLaw;

	private String separator;

	private boolean paramsOnly;

	private String simDir;

	private String paramFile;

	private ArrayList<String> parameterChanges;

	private Pattern IDpat = Pattern.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");

	/**
	 * Creates a new SBML_Editor and sets up the frame where the user can edit a
	 * new sbml file.
	 */
	public SBML_Editor(Reb2Sac reb2sac, Log log, BioSim biosim, String simDir, String paramFile) {
		this.reb2sac = reb2sac;
		paramsOnly = (reb2sac != null);
		this.log = log;
		this.biosim = biosim;
		this.simDir = simDir;
		this.paramFile = paramFile;
		createSbmlFrame("");
	}

	/**
	 * Creates a new SBML_Editor and sets up the frame where the user can edit the
	 * sbml file given to this constructor.
	 */
	public SBML_Editor(String file, Reb2Sac reb2sac, Log log, BioSim biosim, String simDir,
			String paramFile) {
		this.reb2sac = reb2sac;
		paramsOnly = (reb2sac != null);
		this.log = log;
		this.biosim = biosim;
		this.simDir = simDir;
		this.paramFile = paramFile;
		createSbmlFrame(file);
	}

	/**
	 * Private helper method that helps create the sbml frame.
	 */
	private void createSbmlFrame(String file) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		// intitializes the member variables
		if (!file.equals("")) {
			this.file = file;
		}
		else {
			this.file = null;
		}

		// creates the sbml reader and reads the sbml file
		Model model;
		if (!file.equals("")) {
			SBMLReader reader = new SBMLReader();
			document = reader.readSBML(file);
			model = document.getModel();
			if (model.getId().equals("")) {
				model.setId(file.split(separator)[file.split(separator).length - 1]);
				save(false);
			}
		}
		else {
			document = new SBMLDocument();
			model = document.createModel();
		}

		usedIDs = new ArrayList<String>();
		if (model.isSetId()) {
			usedIDs.add(model.getId());
		}
		ListOf ids = model.getListOfFunctionDefinitions();
		for (int i = 0; i < model.getNumFunctionDefinitions(); i++) {
			usedIDs.add(((FunctionDefinition) ids.get(i)).getId());
		}
		ids = model.getListOfUnitDefinitions();
		for (int i = 0; i < model.getNumUnitDefinitions(); i++) {
			usedIDs.add(((UnitDefinition) ids.get(i)).getId());
		}
		ids = model.getListOfCompartmentTypes();
		for (int i = 0; i < model.getNumCompartmentTypes(); i++) {
			usedIDs.add(((CompartmentType) ids.get(i)).getId());
		}
		ids = model.getListOfSpeciesTypes();
		for (int i = 0; i < model.getNumSpeciesTypes(); i++) {
			usedIDs.add(((SpeciesType) ids.get(i)).getId());
		}
		ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			usedIDs.add(((Compartment) ids.get(i)).getId());
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < model.getNumParameters(); i++) {
			usedIDs.add(((Parameter) ids.get(i)).getId());
		}
		ids = model.getListOfReactions();
		for (int i = 0; i < model.getNumReactions(); i++) {
			usedIDs.add(((Reaction) ids.get(i)).getId());
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			usedIDs.add(((Species) ids.get(i)).getId());
		}

		// sets up the compartments editor
		JPanel comp = new JPanel(new BorderLayout());
		JPanel addRemComp = new JPanel();
		addCompart = new JButton("Add Compartment");
		removeCompart = new JButton("Remove Compartment");
		editCompart = new JButton("Edit Compartment");
		addRemComp.add(addCompart);
		addRemComp.add(removeCompart);
		addRemComp.add(editCompart);
		addCompart.addActionListener(this);
		removeCompart.addActionListener(this);
		editCompart.addActionListener(this);
		if (paramsOnly) {
			parameterChanges = new ArrayList<String>();
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					scan.nextLine();
				}
				while (scan.hasNextLine()) {
					parameterChanges.add(scan.nextLine());
				}
				scan.close();
			}
			catch (Exception e) {
			}
			addCompart.setEnabled(false);
			removeCompart.setEnabled(false);
			editCompart.setEnabled(false);
		}
		JLabel compartmentsLabel = new JLabel("List of Compartments:");
		compartments = new JList();
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(compartments);
		ListOf listOfCompartments = model.getListOfCompartments();
		comps = new String[(int) model.getNumCompartments()];
		for (int i = 0; i < model.getNumCompartments(); i++) {
			Compartment compartment = (Compartment) listOfCompartments.get(i);
			if (compartment.isSetCompartmentType()) {
				comps[i] = compartment.getId() + " " + compartment.getCompartmentType();
			}
			else {
				comps[i] = compartment.getId();
			}
			if (compartment.isSetSize()) {
				comps[i] += " " + compartment.getSize();
			}
			if (compartment.isSetUnits()) {
				comps[i] += " " + compartment.getUnits();
			}
		}
		sort(comps);
		compartments.setListData(comps);
		compartments.addMouseListener(this);
		comp.add(compartmentsLabel, "North");
		comp.add(scroll, "Center");
		comp.add(addRemComp, "South");

		// sets up the species editor
		JPanel spec = new JPanel(new BorderLayout());
		JPanel addSpecs = new JPanel();
		addSpec = new JButton("Add Species");
		removeSpec = new JButton("Remove Species");
		editSpec = new JButton("Edit Species");
		addSpecs.add(addSpec);
		addSpecs.add(removeSpec);
		addSpecs.add(editSpec);
		addSpec.addActionListener(this);
		removeSpec.addActionListener(this);
		editSpec.addActionListener(this);
		if (paramsOnly) {
			addSpec.setEnabled(false);
			removeSpec.setEnabled(false);
		}
		JLabel speciesLabel = new JLabel("List of Species:");
		species = new JList();
		species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll1 = new JScrollPane();
		scroll1.setMinimumSize(new Dimension(260, 220));
		scroll1.setPreferredSize(new Dimension(276, 152));
		scroll1.setViewportView(species);
		ListOf listOfSpecies = model.getListOfSpecies();
		specs = new String[(int) model.getNumSpecies()];
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = (Species) listOfSpecies.get(i);
			String s;
			if (species.isSetSpeciesType()) {
				s = species.getId() + " " + species.getSpeciesType() + " " + species.getCompartment();
			}
			else {
				s = species.getId() + " " + species.getCompartment();
			}
			if (species.isSetInitialAmount()) {
				s += " " + species.getInitialAmount();
			}
			else {
				s += " " + species.getInitialConcentration();
			}
			if (species.isSetUnits()) {
				s += " " + species.getUnits();
			}
			if (paramsOnly) {
				for (int j = 0; j < parameterChanges.size(); j++) {
					if (parameterChanges.get(j).split(" ")[0].equals(species.getId())) {
						s = parameterChanges.get(j);
						if (species.isSetInitialAmount()) {
							species.setInitialAmount(Double.parseDouble(parameterChanges.get(j).split(" ")[2]));
						}
						else {
							try {
								species.setInitialConcentration(Double.parseDouble(parameterChanges.get(j).split(
										" ")[2]));
							}
							catch (Exception e) {
								species.setInitialConcentration(Double.parseDouble(parameterChanges.get(j).split(
										" ")[2].split(",")[0].substring(1).trim()));
							}
						}
					}
				}
			}
			specs[i] = s;
		}
		sort(specs);
		species.setListData(specs);
		species.setSelectedIndex(0);
		species.addMouseListener(this);
		spec.add(speciesLabel, "North");
		spec.add(scroll1, "Center");
		spec.add(addSpecs, "South");

		// sets up the reactions editor
		JPanel reac = new JPanel(new BorderLayout());
		JPanel addReacs = new JPanel();
		addReac = new JButton("Add Reaction");
		removeReac = new JButton("Remove Reaction");
		editReac = new JButton("Edit Reaction");
		copyReac = new JButton("Copy Reaction");
		addReacs.add(addReac);
		addReacs.add(removeReac);
		addReacs.add(editReac);
		addReacs.add(copyReac);
		addReac.addActionListener(this);
		removeReac.addActionListener(this);
		editReac.addActionListener(this);
		copyReac.addActionListener(this);
		if (paramsOnly) {
			addReac.setEnabled(false);
			removeReac.setEnabled(false);
			copyReac.setEnabled(false);
		}
		JLabel reactionsLabel = new JLabel("List of Reactions:");
		reactions = new JList();
		reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(400, 220));
		scroll2.setPreferredSize(new Dimension(436, 152));
		scroll2.setViewportView(reactions);
		ListOf listOfReactions = model.getListOfReactions();
		reacts = new String[(int) model.getNumReactions()];
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) listOfReactions.get(i);
			reacts[i] = reaction.getId();
			if (paramsOnly) {
				ListOf params = reaction.getKineticLaw().getListOfParameters();
				for (int j = 0; j < reaction.getKineticLaw().getNumParameters(); j++) {
					Parameter paramet = ((Parameter) (params.get(j)));
					for (int k = 0; k < parameterChanges.size(); k++) {
						if (parameterChanges.get(k).split(" ")[0].equals(reaction.getId() + "/"
								+ paramet.getId())) {
							try {
								paramet.setValue(Double.parseDouble(parameterChanges.get(k).split(" ")[1]));
							}
							catch (Exception e) {
							}
							if ((reacts[i].contains("Custom") && parameterChanges.get(k).split(" ")[2]
									.equals("Sweep"))
									|| (reacts[i].contains("Sweep") && parameterChanges.get(k).split(" ")[2]
											.equals("Custom"))) {
								reacts[i] = reaction.getId() + " Custom/Sweep";
							}
							else {
								reacts[i] = reaction.getId() + " " + parameterChanges.get(k).split(" ")[2];
							}
						}
					}
				}
			}
		}
		sort(reacts);
		reactions.setListData(reacts);
		reactions.setSelectedIndex(0);
		reactions.addMouseListener(this);
		reac.add(reactionsLabel, "North");
		reac.add(scroll2, "Center");
		reac.add(addReacs, "South");

		// sets up the parameters editor
		JPanel param = new JPanel(new BorderLayout());
		JPanel addParams = new JPanel();
		addParam = new JButton("Add Parameter");
		removeParam = new JButton("Remove Parameter");
		editParam = new JButton("Edit Parameter");
		addParams.add(addParam);
		addParams.add(removeParam);
		addParams.add(editParam);
		addParam.addActionListener(this);
		removeParam.addActionListener(this);
		editParam.addActionListener(this);
		if (paramsOnly) {
			addParam.setEnabled(false);
			removeParam.setEnabled(false);
		}
		JLabel parametersLabel = new JLabel("List of Global Parameters:");
		parameters = new JList();
		parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll3 = new JScrollPane();
		scroll3.setMinimumSize(new Dimension(260, 220));
		scroll3.setPreferredSize(new Dimension(276, 152));
		scroll3.setViewportView(parameters);
		ListOf listOfParameters = model.getListOfParameters();
		params = new String[(int) model.getNumParameters()];
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameter = (Parameter) listOfParameters.get(i);
			String p;
			if (parameter.isSetUnits()) {
				p = parameter.getId() + " " + parameter.getValue() + " " + parameter.getUnits();
			}
			else {
				p = parameter.getId() + " " + parameter.getValue();
			}
			if (paramsOnly) {
				for (int j = 0; j < parameterChanges.size(); j++) {
					if (parameterChanges.get(j).split(" ")[0].equals(parameter.getId())) {
						p = parameterChanges.get(j);
						parameter.setValue(Double.parseDouble(parameterChanges.get(j).split(" ")[1]));
					}
				}
			}
			params[i] = p;
		}
		sort(params);
		parameters.setListData(params);
		parameters.setSelectedIndex(0);
		parameters.addMouseListener(this);
		param.add(parametersLabel, "North");
		param.add(scroll3, "Center");
		param.add(addParams, "South");

		// adds the main panel to the frame and displays it
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterDown = new JPanel();
		mainPanelCenterUp.add(comp);
		mainPanelCenterUp.add(spec);
		mainPanelCenterDown.add(reac);
		mainPanelCenterDown.add(param);
		mainPanelCenter.add(mainPanelCenterUp, "North");
		mainPanelCenter.add(mainPanelCenterDown, "South");
		modelID = new JTextField(model.getId(), 16);
		modelName = new JTextField(model.getName(), 50);
		JLabel modelIDLabel = new JLabel("Model ID:");
		JLabel modelNameLabel = new JLabel("Model Name:");
		modelID.setEditable(false);
		mainPanelNorth.add(modelIDLabel);
		mainPanelNorth.add(modelID);
		mainPanelNorth.add(modelNameLabel);
		mainPanelNorth.add(modelName);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainPanelNorth, "North");
		mainPanel.add(mainPanelCenter, "Center");

		JPanel defnPanel = createDefnFrame(model);
		JPanel rulesPanel = createRuleFrame(model);
		if (!paramsOnly) {
			JTabbedPane tab = new JTabbedPane();
			tab.addTab("Main Elements", mainPanel);
			tab.addTab("Definitions/Types", defnPanel);
			tab.addTab("Initial Assignments/Rules/Constraints/Events", rulesPanel);
			this.add(tab, "Center");
		}
		else {
			this.add(mainPanel, "Center");
		}

		change = false;
		if (paramsOnly) {
			saveNoRun = new JButton("Save Parameters");
			run = new JButton("Save And Run");
			saveNoRun.setMnemonic(KeyEvent.VK_S);
			run.setMnemonic(KeyEvent.VK_R);
			saveNoRun.addActionListener(this);
			run.addActionListener(this);
			JPanel saveRun = new JPanel();
			saveRun.add(saveNoRun);
			saveRun.add(run);
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, saveRun, null);
			splitPane.setDividerSize(0);
			this.add(splitPane, "South");
		}
		else {
			saveNoRun = new JButton("Save SBML");
			saveAs = new JButton("Save As");
			saveNoRun.setMnemonic(KeyEvent.VK_S);
			saveAs.setMnemonic(KeyEvent.VK_A);
			saveNoRun.addActionListener(this);
			saveAs.addActionListener(this);
			JPanel saveRun = new JPanel();
			saveRun.add(saveNoRun);
			saveRun.add(saveAs);
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, saveRun, null);
			splitPane.setDividerSize(0);
			this.add(splitPane, "South");
		}
	}

	/**
	 * Private helper method to create definitions/types frame.
	 */
	private JPanel createDefnFrame(Model model) {
		libsbml sbmlLib = new libsbml();

		/* Create function definition panel */
		addFunction = new JButton("Add Function");
		removeFunction = new JButton("Remove Function");
		editFunction = new JButton("Edit Function");
		functions = new JList();
		ListOf listOfFunctions = model.getListOfFunctionDefinitions();
		funcs = new String[(int) model.getNumFunctionDefinitions()];
		for (int i = 0; i < model.getNumFunctionDefinitions(); i++) {
			FunctionDefinition function = (FunctionDefinition) listOfFunctions.get(i);
			funcs[i] = function.getId() + " ( ";
			for (long j = 0; j < function.getNumArguments(); j++) {
				if (j != 0) {
					funcs[i] += ", ";
				}
				funcs[i] += sbmlLib.formulaToString(function.getArgument(j));
			}
			if (function.isSetMath()) {
				funcs[i] += " ) = " + sbmlLib.formulaToString(function.getBody());
			}
		}
		JPanel funcdefnPanel = createPanel(model, "Function Definitions", functions, funcs,
				addFunction, removeFunction, editFunction);

		/* Create unit definition panel */
		addUnit = new JButton("Add Unit");
		removeUnit = new JButton("Remove Unit");
		editUnit = new JButton("Edit Unit");
		unitDefs = new JList();
		ListOf listOfUnits = model.getListOfUnitDefinitions();
		units = new String[(int) model.getNumUnitDefinitions()];
		for (int i = 0; i < model.getNumUnitDefinitions(); i++) {
			UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
			units[i] = unit.getId();
			// GET OTHER THINGS
		}
		JPanel unitdefnPanel = createPanel(model, "Unit Definitions", unitDefs, units, addUnit,
				removeUnit, editUnit);

		/* Create compartment type panel */
		addCompType = new JButton("Add Type");
		removeCompType = new JButton("Remove Type");
		editCompType = new JButton("Edit Type");
		compTypes = new JList();
		ListOf listOfCompartmentTypes = model.getListOfCompartmentTypes();
		cpTyp = new String[(int) model.getNumCompartmentTypes()];
		for (int i = 0; i < model.getNumCompartmentTypes(); i++) {
			CompartmentType compType = (CompartmentType) listOfCompartmentTypes.get(i);
			cpTyp[i] = compType.getId();
		}
		JPanel compTypePanel = createPanel(model, "Compartment Types", compTypes, cpTyp, addCompType,
				removeCompType, editCompType);

		/* Create species type panel */
		addSpecType = new JButton("Add Type");
		removeSpecType = new JButton("Remove Type");
		editSpecType = new JButton("Edit Type");
		specTypes = new JList();
		ListOf listOfSpeciesTypes = model.getListOfSpeciesTypes();
		spTyp = new String[(int) model.getNumSpeciesTypes()];
		for (int i = 0; i < model.getNumSpeciesTypes(); i++) {
			SpeciesType specType = (SpeciesType) listOfSpeciesTypes.get(i);
			spTyp[i] = specType.getId();
		}
		JPanel specTypePanel = createPanel(model, "Species Types", specTypes, spTyp, addSpecType,
				removeSpecType, editSpecType);

		JPanel defnPanelNorth = new JPanel();
		JPanel defnPanelSouth = new JPanel();
		JPanel defnPanel = new JPanel(new BorderLayout());
		defnPanelNorth.add(funcdefnPanel);
		defnPanelNorth.add(unitdefnPanel);
		defnPanelSouth.add(compTypePanel);
		defnPanelSouth.add(specTypePanel);
		defnPanel.add(defnPanelNorth, "North");
		defnPanel.add(defnPanelSouth, "South");
		return defnPanel;
	}

	/**
	 * Private helper method to create rules/events/constraints frame.
	 */
	private JPanel createRuleFrame(Model model) {
		libsbml sbmlLib = new libsbml();

		/* Create initial assignment panel */
		addInit = new JButton("Add Initial");
		removeInit = new JButton("Remove Initial");
		editInit = new JButton("Edit Initial");
		initAssigns = new JList();
		ListOf listOfInits = model.getListOfInitialAssignments();
		inits = new String[(int) model.getNumInitialAssignments()];
		for (int i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment init = (InitialAssignment) listOfInits.get(i);
			inits[i] = init.getSymbol() + " = " + sbmlLib.formulaToString(init.getMath());
		}
		JPanel initPanel = createPanel(model, "Initial Assignments", initAssigns, inits, addInit,
				removeInit, editInit);

		/* Create rule panel */
		addRule = new JButton("Add Rule");
		removeRule = new JButton("Remove Rule");
		editRule = new JButton("Edit Rule");
		rules = new JList();
		ListOf listOfRules = model.getListOfRules();
		rul = new String[(int) model.getNumRules()];
		for (int i = 0; i < model.getNumRules(); i++) {
			Rule rule = (Rule) listOfRules.get(i);
			if (rule.isAlgebraic()) {
				rul[i] = "0 = " + sbmlLib.formulaToString(rule.getMath());
			}
			else if (rule.isAssignment()) {
				rul[i] = rule.getVariable() + " = " + sbmlLib.formulaToString(rule.getMath());
			}
			else {
				rul[i] = "d( " + rule.getVariable() + " )/dt = " + sbmlLib.formulaToString(rule.getMath());
			}
		}
		JPanel rulePanel = createPanel(model, "Rules", rules, rul, addRule, removeRule, editRule);

		/* Create constraint panel */
		addConstraint = new JButton("Add Constraint");
		removeConstraint = new JButton("Remove Constraint");
		editConstraint = new JButton("Edit Constraint");
		constraints = new JList();
		ListOf listOfConstraints = model.getListOfConstraints();
		cons = new String[(int) model.getNumConstraints()];
		for (int i = 0; i < model.getNumConstraints(); i++) {
			Constraint constraint = (Constraint) listOfConstraints.get(i);
			cons[i] = sbmlLib.formulaToString(constraint.getMath());
		}
		JPanel constraintPanel = createPanel(model, "Constraints", constraints, cons, addConstraint,
				removeConstraint, editConstraint);

		/* Create event panel */
		addEvent = new JButton("Add Event");
		removeEvent = new JButton("Remove Event");
		editEvent = new JButton("Edit Event");
		events = new JList();
		ListOf listOfEvents = model.getListOfEvents();
		ev = new String[(int) model.getNumEvents()];
		for (int i = 0; i < model.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) listOfEvents.get(i);
			ev[i] = sbmlLib.formulaToString(event.getTrigger().getMath());
		}
		JPanel eventPanel = createPanel(model, "Events", events, ev, addEvent, removeEvent, editEvent);

		JPanel recPanelNorth = new JPanel();
		JPanel recPanelSouth = new JPanel();
		JPanel recPanel = new JPanel(new BorderLayout());
		recPanelNorth.add(initPanel);
		recPanelNorth.add(rulePanel);
		recPanelSouth.add(constraintPanel);
		recPanelSouth.add(eventPanel);
		recPanel.add(recPanelNorth, "North");
		recPanel.add(recPanelSouth, "South");
		return recPanel;
	}

	/* Create add/remove/edit panel */
	private JPanel createPanel(Model model, String panelName, JList panelJList, String[] panelList,
			JButton addButton, JButton removeButton, JButton editButton) {
		JPanel Panel = new JPanel(new BorderLayout());
		JPanel addRem = new JPanel();
		addRem.add(addButton);
		addRem.add(removeButton);
		addRem.add(editButton);
		addButton.addActionListener(this);
		removeButton.addActionListener(this);
		editButton.addActionListener(this);
		JLabel panelLabel = new JLabel("List of " + panelName + ":");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(panelJList);
		sort(panelList);
		panelJList.setListData(panelList);
		panelJList.addMouseListener(this);
		Panel.add(panelLabel, "North");
		Panel.add(scroll, "Center");
		Panel.add(addRem, "South");
		return Panel;
	}

	/**
	 * This method performs different functions depending on what buttons are
	 * pushed and what input fields contain data.
	 */
	public void actionPerformed(ActionEvent e) {
		libsbml sbmlLib = new libsbml();

		// if the run button is clicked
		if (e.getSource() == run) {
			reb2sac.getRunButton().doClick();
		}
		// if the save button is clicked
		else if (e.getSource() == saveNoRun) {
			if (paramsOnly) {
				reb2sac.getSaveButton().doClick();
			}
			else {
				save(false);
			}
		}
		// if the save as button is clicked
		else if (e.getSource() == saveAs) {
			String simName = JOptionPane.showInputDialog(biosim.frame(), "Enter Model ID:", "Model ID",
					JOptionPane.PLAIN_MESSAGE);
			if (simName != null && !simName.equals("")) {
				if (simName.length() > 4) {
					if (!simName.substring(simName.length() - 5).equals(".sbml")
							&& !simName.substring(simName.length() - 4).equals(".xml")) {
						simName += ".sbml";
					}
				}
				else {
					simName += ".sbml";
				}
				String modelID = "";
				if (simName.length() > 4) {
					if (simName.substring(simName.length() - 5).equals(".sbml")) {
						modelID = simName.substring(0, simName.length() - 5);
					}
					else {
						modelID = simName.substring(0, simName.length() - 4);
					}
				}
				String oldId = document.getModel().getId();
				document.getModel().setId(modelID);
				document.getModel().setName(modelName.getText().trim());
				String newFile = file;
				newFile = newFile.substring(0, newFile.length()
						- newFile.split(separator)[newFile.split(separator).length - 1].length())
						+ simName;
				try {
					log.addText("Saving sbml file as:\n" + newFile + "\n");
					FileOutputStream out = new FileOutputStream(new File(newFile));
					SBMLWriter writer = new SBMLWriter();
					String doc = writer.writeToString(document);
					byte[] output = doc.getBytes();
					out.write(output);
					out.close();
					JTabbedPane tab = biosim.getTab();
					for (int i = 0; i < tab.getTabCount(); i++) {
						if (tab.getTitleAt(i).equals(file.split(separator)[file.split(separator).length - 1])) {
							tab.setTitleAt(i, simName);
							tab.setComponentAt(i, new SBML_Editor(newFile, reb2sac, log, biosim, simDir,
									paramFile));
							tab.getComponentAt(i).setName("SBML Editor");
						}
					}
					biosim.refreshTree();
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(biosim.frame(), "Unable to save sbml file.",
							"Error Saving File", JOptionPane.ERROR_MESSAGE);
				}
				finally {
					document.getModel().setId(oldId);
				}
			}
		}
		// if the add function button is clicked
		else if (e.getSource() == addFunction) {
			functionEditor("Add");
		}
		// if the edit function button is clicked
		else if (e.getSource() == editFunction) {
			functionEditor("OK");
		}
		// if the remove function button is clicked
		else if (e.getSource() == removeFunction) {
			if (functions.getSelectedIndex() != -1) {
				FunctionDefinition tempFunc = document.getModel().getFunctionDefinition(
						((String) functions.getSelectedValue()).split(" ")[0]);
				ListOf f = document.getModel().getListOfFunctionDefinitions();
				for (int i = 0; i < document.getModel().getNumFunctionDefinitions(); i++) {
					if (((FunctionDefinition) f.get(i)).getId().equals(tempFunc.getId())) {
						f.remove(i);
					}
				}
				usedIDs.remove(((String) functions.getSelectedValue()).split(" ")[0]);
				functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				funcs = (String[]) Buttons.remove(functions, funcs);
				functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				functions.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add unit button is clicked
		else if (e.getSource() == addUnit) {
			unitEditor("Add");
		}
		// if the edit unit button is clicked
		else if (e.getSource() == editUnit) {
			unitEditor("OK");
		}
		// if the remove unit button is clicked
		else if (e.getSource() == removeUnit) {
			if (unitDefs.getSelectedIndex() != -1) {
				UnitDefinition tempUnit = document.getModel().getUnitDefinition(
						((String) unitDefs.getSelectedValue()).split(" ")[0]);
				ListOf u = document.getModel().getListOfUnitDefinitions();
				for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
					if (((UnitDefinition) u.get(i)).getId().equals(tempUnit.getId())) {
						u.remove(i);
					}
				}
				usedIDs.remove(((String) unitDefs.getSelectedValue()).split(" ")[0]);
				unitDefs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				units = (String[]) Buttons.remove(unitDefs, units);
				unitDefs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				unitDefs.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add to unit list button is clicked
		else if (e.getSource() == addList) {
			unitListEditor("Add");
		}
		// if the edit unit list button is clicked
		else if (e.getSource() == editList) {
			unitListEditor("OK");
		}
		// if the remove from unit list button is clicked
		else if (e.getSource() == removeList) {
			if (unitDefs.getSelectedIndex() != -1) {
				UnitDefinition tempUnit = document.getModel().getUnitDefinition(
						((String) unitDefs.getSelectedValue()).split(" ")[0]);
				if (unitList.getSelectedIndex() != -1) {
					String selected = (String) unitList.getSelectedValue();
					ListOf u = tempUnit.getListOfUnits();
					for (int i = 0; i < tempUnit.getNumUnits(); i++) {
						if (selected.contains(unitToString(tempUnit.getUnit(i)))) {
							u.remove(i);
						}
					}
				}
				unitList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				uList = (String[]) Buttons.remove(unitList, uList);
				unitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				unitList.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add compartment type button is clicked
		else if (e.getSource() == addCompType) {
			compTypeEditor("Add");
		}
		// if the edit compartment type button is clicked
		else if (e.getSource() == editCompType) {
			compTypeEditor("OK");
		}
		// if the remove compartment type button is clicked
		else if (e.getSource() == removeCompType) {
			if (compTypes.getSelectedIndex() != -1) {
				boolean remove = true;
				ArrayList<String> compartmentUsing = new ArrayList<String>();
				for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
					Compartment compartment = (Compartment) document.getModel().getListOfCompartments()
							.get(i);
					if (compartment.isSetCompartmentType()) {
						if (compartment.getCompartmentType().equals(
								((String) compTypes.getSelectedValue()).split(" ")[0])) {
							remove = false;
							compartmentUsing.add(compartment.getId());
						}
					}
				}
				if (remove) {
					CompartmentType tempCompType = document.getModel().getCompartmentType(
							((String) compTypes.getSelectedValue()).split(" ")[0]);
					ListOf c = document.getModel().getListOfCompartmentTypes();
					for (int i = 0; i < document.getModel().getNumCompartmentTypes(); i++) {
						if (((CompartmentType) c.get(i)).getId().equals(tempCompType.getId())) {
							c.remove(i);
						}
					}
					usedIDs.remove(((String) compTypes.getSelectedValue()).split(" ")[0]);
					compTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					cpTyp = (String[]) Buttons.remove(compTypes, cpTyp);
					compTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					compTypes.setSelectedIndex(0);
					change = true;
				}
				else {
					String compartment = "";
					String[] comps = compartmentUsing.toArray(new String[0]);
					sort(comps);
					for (int i = 0; i < comps.length; i++) {
						if (i == comps.length - 1) {
							compartment += comps[i];
						}
						else {
							compartment += comps[i] + "\n";
						}
					}
					String message = "Unable to remove the selected compartment type.";
					if (compartmentUsing.size() != 0) {
						message += "\n\nIt is used by the following compartments:\n" + compartment;
					}
					JTextArea messageArea = new JTextArea(message);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(300, 300));
					scroll.setPreferredSize(new Dimension(300, 300));
					scroll.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scroll,
							"Unable To Remove Compartment Type", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the add species type button is clicked
		else if (e.getSource() == addSpecType) {
			specTypeEditor("Add");
		}
		// if the edit species type button is clicked
		else if (e.getSource() == editSpecType) {
			specTypeEditor("OK");
		}
		// if the remove species type button is clicked
		else if (e.getSource() == removeSpecType) {
			if (specTypes.getSelectedIndex() != -1) {
				boolean remove = true;
				ArrayList<String> speciesUsing = new ArrayList<String>();
				for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
					Species species = (Species) document.getModel().getListOfSpecies().get(i);
					if (species.isSetSpeciesType()) {
						if (species.getSpeciesType().equals(
								((String) specTypes.getSelectedValue()).split(" ")[0])) {
							remove = false;
							speciesUsing.add(species.getId());
						}
					}
				}
				if (remove) {
					SpeciesType tempSpecType = document.getModel().getSpeciesType(
							((String) specTypes.getSelectedValue()).split(" ")[0]);
					ListOf s = document.getModel().getListOfSpeciesTypes();
					for (int i = 0; i < document.getModel().getNumSpeciesTypes(); i++) {
						if (((SpeciesType) s.get(i)).getId().equals(tempSpecType.getId())) {
							s.remove(i);
						}
					}
					usedIDs.remove(((String) specTypes.getSelectedValue()).split(" ")[0]);
					specTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					spTyp = (String[]) Buttons.remove(specTypes, spTyp);
					specTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					specTypes.setSelectedIndex(0);
					change = true;
				}
				else {
					String species = "";
					String[] specs = speciesUsing.toArray(new String[0]);
					sort(specs);
					for (int i = 0; i < specs.length; i++) {
						if (i == specs.length - 1) {
							species += specs[i];
						}
						else {
							species += specs[i] + "\n";
						}
					}
					String message = "Unable to remove the selected species type.";
					if (speciesUsing.size() != 0) {
						message += "\n\nIt is used by the following species:\n" + species;
					}
					JTextArea messageArea = new JTextArea(message);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(300, 300));
					scroll.setPreferredSize(new Dimension(300, 300));
					scroll.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scroll, "Unable To Remove Species Type",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the add init button is clicked
		else if (e.getSource() == addInit) {
			initEditor("Add");
		}
		// if the edit init button is clicked
		else if (e.getSource() == editInit) {
			initEditor("OK");
		}
		// if the remove rule button is clicked
		else if (e.getSource() == removeInit) {
			if (initAssigns.getSelectedIndex() != -1) {
				String selected = ((String) initAssigns.getSelectedValue());
				String tempVar = selected.split(" ")[0];
				String tempMath = selected.substring(selected.indexOf('=') + 2);
				ListOf r = document.getModel().getListOfInitialAssignments();
				for (int i = 0; i < document.getModel().getNumInitialAssignments(); i++) {
					if (sbmlLib.formulaToString(((InitialAssignment) r.get(i)).getMath()).equals(tempMath)
							&& ((InitialAssignment) r.get(i)).getSymbol().equals(tempVar)) {
						r.remove(i);
					}
				}
				initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				inits = (String[]) Buttons.remove(initAssigns, inits);
				initAssigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				initAssigns.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add rule button is clicked
		else if (e.getSource() == addRule) {
			ruleEditor("Add");
		}
		// if the edit rule button is clicked
		else if (e.getSource() == editRule) {
			ruleEditor("OK");
		}
		// if the remove rule button is clicked
		else if (e.getSource() == removeRule) {
			if (rules.getSelectedIndex() != -1) {
				String selected = ((String) rules.getSelectedValue());
				// algebraic rule
				if ((selected.split(" ")[0]).equals("0")) {
					String tempMath = selected.substring(4);
					ListOf r = document.getModel().getListOfRules();
					for (int i = 0; i < document.getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isAlgebraic())
								&& ((Rule) r.get(i)).getFormula().equals(tempMath)) {
							r.remove(i);
						}
					}
				}
				// rate rule
				else if ((selected.split(" ")[0]).equals("d(")) {
					String tempVar = selected.split(" ")[1];
					String tempMath = selected.substring(selected.indexOf('=') + 2);
					ListOf r = document.getModel().getListOfRules();
					for (int i = 0; i < document.getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isRate()) && ((Rule) r.get(i)).getFormula().equals(tempMath)
								&& ((Rule) r.get(i)).getVariable().equals(tempVar)) {
							r.remove(i);
						}
					}
				}
				// assignment rule
				else {
					String tempVar = selected.split(" ")[0];
					String tempMath = selected.substring(selected.indexOf('=') + 2);
					ListOf r = document.getModel().getListOfRules();
					for (int i = 0; i < document.getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isAssignment())
								&& ((Rule) r.get(i)).getFormula().equals(tempMath)
								&& ((Rule) r.get(i)).getVariable().equals(tempVar)) {
							r.remove(i);
						}
					}
				}
				rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				rul = (String[]) Buttons.remove(rules, rul);
				rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				rules.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add event button is clicked
		else if (e.getSource() == addEvent) {
			eventEditor("Add");
		}
		// if the edit event button is clicked
		else if (e.getSource() == editEvent) {
			eventEditor("OK");
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeEvent) {
			if (events.getSelectedIndex() != -1) {
				String selected = ((String) events.getSelectedValue());
				ListOf EL = document.getModel().getListOfEvents();
				for (int i = 0; i < document.getModel().getNumEvents(); i++) {
					org.sbml.libsbml.Event E = (org.sbml.libsbml.Event) EL.get(i);
					if (sbmlLib.formulaToString(E.getTrigger().getMath()).equals(selected)) {
						EL.remove(i);
					}
				}
				events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				ev = (String[]) Buttons.remove(events, ev);
				events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				events.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add event assignment button is clicked
		else if (e.getSource() == addAssignment) {
			eventAssignEditor("Add");
		}
		// if the edit event assignment button is clicked
		else if (e.getSource() == editAssignment) {
			eventAssignEditor("OK");
		}
		// if the remove event assignment button is clicked
		else if (e.getSource() == removeAssignment) {
			if (eventAssign.getSelectedIndex() != -1) {
				eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				assign = (String[]) Buttons.remove(eventAssign, assign);
				eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				eventAssign.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add constraint button is clicked
		else if (e.getSource() == addConstraint) {
			constraintEditor("Add");
		}
		// if the edit constraint button is clicked
		else if (e.getSource() == editConstraint) {
			constraintEditor("OK");
		}
		// if the remove constraint button is clicked
		else if (e.getSource() == removeConstraint) {
			if (constraints.getSelectedIndex() != -1) {
				String selected = ((String) constraints.getSelectedValue());
				ListOf c = document.getModel().getListOfConstraints();
				for (int i = 0; i < document.getModel().getNumConstraints(); i++) {
					if (sbmlLib.formulaToString(((Constraint) c.get(i)).getMath()).equals(selected)) {
						c.remove(i);
					}
				}
				constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				cons = (String[]) Buttons.remove(constraints, cons);
				constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				constraints.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add comparment button is clicked
		else if (e.getSource() == addCompart) {
			compartEditor("Add");
		}
		// if the edit comparment button is clicked
		else if (e.getSource() == editCompart) {
			compartEditor("OK");
		}
		// if the remove compartment button is clicked
		else if (e.getSource() == removeCompart) {
			if (compartments.getSelectedIndex() != -1) {
				if (document.getModel().getNumCompartments() != 1) {
					boolean remove = true;
					ArrayList<String> speciesUsing = new ArrayList<String>();
					for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
						Species species = (Species) document.getModel().getListOfSpecies().get(i);
						if (species.isSetCompartment()) {
							if (species.getCompartment().equals(
									((String) compartments.getSelectedValue()).split(" ")[0])) {
								remove = false;
								speciesUsing.add(species.getId());
							}
						}
					}
					if (remove) {
						Compartment tempComp = document.getModel().getCompartment(
								((String) compartments.getSelectedValue()).split(" ")[0]);
						ListOf c = document.getModel().getListOfCompartments();
						for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
							if (((Compartment) c.get(i)).getId().equals(tempComp.getId())) {
								c.remove(i);
							}
						}
						usedIDs.remove(((String) compartments.getSelectedValue()).split(" ")[0]);
						compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						comps = (String[]) Buttons.remove(compartments, comps);
						compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						compartments.setSelectedIndex(0);
						change = true;
					}
					else {
						String species = "";
						String[] specs = speciesUsing.toArray(new String[0]);
						sort(specs);
						for (int i = 0; i < specs.length; i++) {
							if (i == specs.length - 1) {
								species += specs[i];
							}
							else {
								species += specs[i] + "\n";
							}
						}
						String message = "Unable to remove the selected compartment.";
						if (speciesUsing.size() != 0) {
							message += "\n\nIt contains the following species:\n" + species;
						}
						JTextArea messageArea = new JTextArea(message);
						messageArea.setEditable(false);
						JScrollPane scroll = new JScrollPane();
						scroll.setMinimumSize(new Dimension(300, 300));
						scroll.setPreferredSize(new Dimension(300, 300));
						scroll.setViewportView(messageArea);
						JOptionPane.showMessageDialog(biosim.frame(), scroll, "Unable To Remove Compartment",
								JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Each model must contain at least one compartment.", "Unable To Remove Compartment",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the add species button is clicked
		else if (e.getSource() == addSpec) {
			speciesEditor("Add");
		}
		// if the edit species button is clicked
		else if (e.getSource() == editSpec) {
			speciesEditor("OK");
		}
		// if the remove species button is clicked
		else if (e.getSource() == removeSpec) {
			if (species.getSelectedIndex() != -1) {
				Model model = document.getModel();
				boolean remove = true;
				ArrayList<String> reactantsUsing = new ArrayList<String>();
				ArrayList<String> productsUsing = new ArrayList<String>();
				ArrayList<String> modifiersUsing = new ArrayList<String>();
				for (int i = 0; i < model.getNumReactions(); i++) {
					Reaction reaction = (Reaction) document.getModel().getListOfReactions().get(i);
					for (int j = 0; j < reaction.getNumProducts(); j++) {
						if (reaction.getProduct(j).isSetSpecies()) {
							String specRef = reaction.getProduct(j).getSpecies();
							if (model.getSpecies(((String) species.getSelectedValue()).split(" ")[0]).getId()
									.equals(specRef)) {
								remove = false;
								productsUsing.add(reaction.getId());
							}
						}
					}
					for (int j = 0; j < reaction.getNumModifiers(); j++) {
						if (reaction.getModifier(j).isSetSpecies()) {
							String specRef = reaction.getModifier(j).getSpecies();
							if (model.getSpecies(((String) species.getSelectedValue()).split(" ")[0]).getId()
									.equals(specRef)) {
								remove = false;
								modifiersUsing.add(reaction.getId());
							}
						}
					}
					for (int j = 0; j < reaction.getNumReactants(); j++) {
						if (reaction.getReactant(j).isSetSpecies()) {
							String specRef = reaction.getReactant(j).getSpecies();
							if (model.getSpecies(((String) species.getSelectedValue()).split(" ")[0]).getId()
									.equals(specRef)) {
								remove = false;
								reactantsUsing.add(reaction.getId());
							}
						}
					}
				}
				if (remove) {
					Species tempSpecies = document.getModel().getSpecies(
							((String) species.getSelectedValue()).split(" ")[0]);
					ListOf s = document.getModel().getListOfSpecies();
					for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
						if (((Species) s.get(i)).getId().equals(tempSpecies.getId())) {
							s.remove(i);
						}
					}
					usedIDs.remove(tempSpecies.getId());
					species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					specs = (String[]) Buttons.remove(species, specs);
					species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					species.setSelectedIndex(0);
					change = true;
				}
				else {
					String reactants = "";
					String products = "";
					String modifiers = "";
					String[] reacts = reactantsUsing.toArray(new String[0]);
					sort(reacts);
					String[] prods = productsUsing.toArray(new String[0]);
					sort(prods);
					String[] mods = modifiersUsing.toArray(new String[0]);
					sort(mods);
					for (int i = 0; i < reacts.length; i++) {
						if (i == reacts.length - 1) {
							reactants += reacts[i];
						}
						else {
							reactants += reacts[i] + "\n";
						}
					}
					for (int i = 0; i < prods.length; i++) {
						if (i == prods.length - 1) {
							products += prods[i];
						}
						else {
							products += prods[i] + "\n";
						}
					}
					for (int i = 0; i < mods.length; i++) {
						if (i == mods.length - 1) {
							modifiers += mods[i];
						}
						else {
							modifiers += mods[i] + "\n";
						}
					}
					String message = "Unable to remove the selected species.";
					if (reactantsUsing.size() != 0) {
						message += "\n\nIt is used as a reactant in the following reactions:\n" + reactants;
					}
					if (productsUsing.size() != 0) {
						message += "\n\nIt is used as a product in the following reactions:\n" + products;
					}
					if (modifiersUsing.size() != 0) {
						message += "\n\nIt is used as a modifier in the following reactions:\n" + modifiers;
					}
					JTextArea messageArea = new JTextArea(message);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(300, 300));
					scroll.setPreferredSize(new Dimension(300, 300));
					scroll.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scroll, "Unable To Remove Species",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the add reactions button is clicked
		else if (e.getSource() == addReac) {
			reactionsEditor("Add");
		}
		// if the edit reactions button is clicked
		else if (e.getSource() == editReac) {
			reactionsEditor("OK");
		}
		// if the copy reactions button is clicked
		else if (e.getSource() == copyReac) {
			String reacID = JOptionPane.showInputDialog(biosim.frame(), "Enter New Reaction ID:",
					"Reaction ID", JOptionPane.PLAIN_MESSAGE);
			if (reacID == null) {
				return;
			}
			if (!usedIDs.contains(reacID.trim()) && !reacID.trim().equals("")) {
				Reaction react = document.getModel().createReaction();
				react.setKineticLaw(new KineticLaw());
				int index = reactions.getSelectedIndex();
				Reaction r = document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]);
				for (int i = 0; i < r.getKineticLaw().getNumParameters(); i++) {
					react.getKineticLaw().addParameter(
							(Parameter) r.getKineticLaw().getListOfParameters().get(i));
				}
				for (int i = 0; i < r.getNumProducts(); i++) {
					react.addProduct(r.getProduct(i));
				}
				for (int i = 0; i < r.getNumModifiers(); i++) {
					react.addModifier(r.getModifier(i));
				}
				for (int i = 0; i < r.getNumReactants(); i++) {
					react.addReactant(r.getReactant(i));
				}
				react.setReversible(r.getReversible());
				react.setId(reacID.trim());
				usedIDs.add(reacID.trim());
				react.getKineticLaw().setFormula(r.getKineticLaw().getFormula());
				JList add = new JList();
				Object[] adding = { reacID.trim() };
				add.setListData(adding);
				add.setSelectedIndex(0);
				reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				adding = Buttons.add(reacts, reactions, add, false, null, null, null, null, null, null,
						biosim.frame());
				reacts = new String[adding.length];
				for (int i = 0; i < adding.length; i++) {
					reacts[i] = (String) adding[i];
				}
				sort(reacts);
				reactions.setListData(reacts);
				reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (document.getModel().getNumReactions() == 1) {
					reactions.setSelectedIndex(0);
				}
				else {
					reactions.setSelectedIndex(index);
				}
				change = true;
			}
			else {
				JOptionPane.showMessageDialog(biosim.frame(), "You must enter a unique id.",
						"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the remove reactions button is clicked
		else if (e.getSource() == removeReac) {
			if (reactions.getSelectedIndex() != -1) {
				Reaction tempReaction = document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]);
				ListOf r = document.getModel().getListOfReactions();
				for (int i = 0; i < document.getModel().getNumReactions(); i++) {
					if (((Reaction) r.get(i)).getId().equals(tempReaction.getId())) {
						r.remove(i);
					}
				}
				usedIDs.remove(((String) reactions.getSelectedValue()).split(" ")[0]);
				reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				reacts = (String[]) Buttons.remove(reactions, reacts);
				reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				reactions.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add parameters button is clicked
		else if (e.getSource() == addParam) {
			parametersEditor("Add");
		}
		// if the edit parameters button is clicked
		else if (e.getSource() == editParam) {
			parametersEditor("OK");
		}
		// if the remove parameters button is clicked
		else if (e.getSource() == removeParam) {
			if (parameters.getSelectedIndex() != -1) {
				Parameter tempParameter = document.getModel().getParameter(
						((String) parameters.getSelectedValue()).split(" ")[0]);
				ListOf p = document.getModel().getListOfParameters();
				for (int i = 0; i < document.getModel().getNumParameters(); i++) {
					if (((Parameter) p.get(i)).getId().equals(tempParameter.getId())) {
						p.remove(i);
					}
				}
				usedIDs.remove(tempParameter.getId());
				parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				params = (String[]) Buttons.remove(parameters, params);
				parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				parameters.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add reactions parameters button is clicked
		else if (e.getSource() == reacAddParam) {
			reacParametersEditor("Add");
		}
		// if the edit reactions parameters button is clicked
		else if (e.getSource() == reacEditParam) {
			reacParametersEditor("OK");
		}
		// if the remove reactions parameters button is clicked
		else if (e.getSource() == reacRemoveParam) {
			if (reacParameters.getSelectedIndex() != -1) {
				String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < changedParameters.size(); i++) {
					if (changedParameters.get(i).getId().equals(v)) {
						changedParameters.remove(i);
					}
				}
				thisReactionParams.remove(v);
				reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				reacParams = (String[]) Buttons.remove(reacParameters, reacParams);
				reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				reacParameters.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add reactants button is clicked
		else if (e.getSource() == addReactant) {
			reactantsEditor("Add");
		}
		// if the edit reactants button is clicked
		else if (e.getSource() == editReactant) {
			reactantsEditor("OK");
		}
		// if the remove reactants button is clicked
		else if (e.getSource() == removeReactant) {
			if (reactants.getSelectedIndex() != -1) {
				String v = ((String) reactants.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < changedReactants.size(); i++) {
					if (changedReactants.get(i).getSpecies().equals(v)) {
						changedReactants.remove(i);
					}
				}
				reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				reacta = (String[]) Buttons.remove(reactants, reacta);
				reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				reactants.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add products button is clicked
		else if (e.getSource() == addProduct) {
			productsEditor("Add");
		}
		// if the edit products button is clicked
		else if (e.getSource() == editProduct) {
			productsEditor("OK");
		}
		// if the remove products button is clicked
		else if (e.getSource() == removeProduct) {
			if (products.getSelectedIndex() != -1) {
				String v = ((String) products.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < changedProducts.size(); i++) {
					if (changedProducts.get(i).getSpecies().equals(v)) {
						changedProducts.remove(i);
					}
				}
				products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				product = (String[]) Buttons.remove(products, product);
				products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				products.setSelectedIndex(0);
				change = true;
			}
		}
		// if the add modifiers button is clicked
		else if (e.getSource() == addModifier) {
			modifiersEditor("Add");
		}
		// if the edit modifiers button is clicked
		else if (e.getSource() == editModifier) {
			modifiersEditor("OK");
		}
		// if the remove modifiers button is clicked
		else if (e.getSource() == removeModifier) {
			if (modifiers.getSelectedIndex() != -1) {
				String v = ((String) modifiers.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < changedModifiers.size(); i++) {
					if (changedModifiers.get(i).getSpecies().equals(v)) {
						changedModifiers.remove(i);
					}
				}
				modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				modifier = (String[]) Buttons.remove(modifiers, modifier);
				modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				modifiers.setSelectedIndex(0);
				change = true;
			}
		}
		// if the clear button is clicked
		else if (e.getSource() == clearKineticLaw) {
			kineticLaw.setText("");
			change = true;
		}
		// if the use mass action button is clicked
		else if (e.getSource() == useMassAction) {
			String kf;
			String kr;
			if (changedParameters.size() == 0) {
				kf = "kf";
				kr = "kr";
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
			for (SpeciesReference s : changedReactants) {
				if (s.getStoichiometry() == 1) {
					kinetic += " * " + s.getSpecies();
				}
				else {
					kinetic += " * pow(" + s.getSpecies() + ", " + s.getStoichiometry() + ")";
				}
			}
			for (ModifierSpeciesReference s : changedModifiers) {
			        kinetic += " * " + s.getSpecies();
			}
			if (reacReverse.getSelectedItem().equals("true")) {
				kinetic += " - " + kr;
				for (SpeciesReference s : changedProducts) {
					if (s.getStoichiometry() == 1) {
						kinetic += " * " + s.getSpecies();
					}
					else {
						kinetic += " * pow(" + s.getSpecies() + ", " + s.getStoichiometry() + ")";
					}
				}
				for (ModifierSpeciesReference s : changedModifiers) {
				  kinetic += " * " + s.getSpecies();
				}
			}
			kineticLaw.setText(kinetic);
			change = true;
		}
	}

	/**
	 * Creates a frame used to edit functions or create new ones.
	 */
	private void functionEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && functions.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No function selected.",
					"Must Select a Function", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel functionPanel = new JPanel();
			JPanel funcPanel = new JPanel(new GridLayout(4, 2));
			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			JLabel argLabel = new JLabel("Arguments:");
			JLabel eqnLabel = new JLabel("Definition:");
			funcID = new JTextField(12);
			funcName = new JTextField(12);
			args = new JTextField(12);
			eqn = new JTextField(12);
			// eqn.setText("1.0");
			if (option.equals("OK")) {
				try {
					FunctionDefinition function = document.getModel().getFunctionDefinition(
							(((String) functions.getSelectedValue()).split(" ")[0]));
					funcID.setText(function.getId());
					funcName.setText(function.getName());
					String argStr = "";
					for (long j = 0; j < function.getNumArguments(); j++) {
						if (j != 0) {
							argStr += ", ";
						}
						argStr += sbmlLib.formulaToString(function.getArgument(j));
					}
					args.setText(argStr);
					if (function.isSetMath()) {
						eqn.setText("" + sbmlLib.formulaToString(function.getBody()));
					}
					else {
						eqn.setText("");
					}
				}
				catch (Exception e) {
				}
			}
			funcPanel.add(idLabel);
			funcPanel.add(funcID);
			funcPanel.add(nameLabel);
			funcPanel.add(funcName);
			funcPanel.add(argLabel);
			funcPanel.add(args);
			funcPanel.add(eqnLabel);
			funcPanel.add(eqn);
			functionPanel.add(funcPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), functionPanel, "Function Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (funcID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an ID into the ID field.",
										"Enter an ID", JOptionPane.ERROR_MESSAGE);
						error = true;
						value = JOptionPane.showOptionDialog(biosim.frame(), functionPanel, "Function Editor",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}
					else {
						String addFunc = "";
						addFunc = funcID.getText().trim();
						if (!(IDpat.matcher(addFunc).matches())) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"A function ID can only contain letters, numbers, and underscores.",
									"Invalid ID", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else if (usedIDs.contains(addFunc)) {
							if (option.equals("OK")
									&& !addFunc.equals(((String) functions.getSelectedValue()).split(" ")[0])) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter a Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (option.equals("Add")) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter a Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						if (eqn.getText().trim().equals("")
								|| sbmlLib.parseFormula("lambda(" + args.getText().trim() + ","
										+ eqn.getText().trim() + ")") == null) {
							JOptionPane.showMessageDialog(biosim.frame(), "You must enter a valid formula.",
									"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						if (!error) {
							if (option.equals("OK")) {
								int index = functions.getSelectedIndex();
								String val = ((String) functions.getSelectedValue()).split(" ")[0];
								functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								funcs = Buttons.getList(funcs, functions);
								functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								FunctionDefinition f = document.getModel().getFunctionDefinition(val);
								f.setId(funcID.getText().trim());
								f.setName(funcName.getText().trim());
								f.setMath(sbmlLib.parseFormula("lambda(" + args.getText().trim() + ","
										+ eqn.getText().trim() + ")"));
								for (int i = 0; i < usedIDs.size(); i++) {
									if (usedIDs.get(i).equals(val)) {
										usedIDs.set(i, addFunc);
									}
								}
								funcs[index] = addFunc + " ( " + args.getText().trim() + " ) = "
										+ eqn.getText().trim();
								sort(funcs);
								functions.setListData(funcs);
								functions.setSelectedIndex(index);
							}
							else {
								int index = functions.getSelectedIndex();
								FunctionDefinition f = document.getModel().createFunctionDefinition();
								f.setId(funcID.getText().trim());
								f.setName(funcName.getText().trim());
								f.setMath(sbmlLib.parseFormula("lambda(" + args.getText().trim() + ","
										+ eqn.getText().trim() + ")"));
								usedIDs.add(addFunc);
								JList add = new JList();
								String addStr;
								addStr = addFunc + " ( " + args.getText().trim() + " ) = " + eqn.getText().trim();
								Object[] adding = { addStr };
								add.setListData(adding);
								add.setSelectedIndex(0);
								functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(funcs, functions, add, false, null, null, null, null, null,
										null, biosim.frame());
								funcs = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									funcs[i] = (String) adding[i];
								}
								sort(funcs);
								functions.setListData(funcs);
								functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (document.getModel().getNumFunctionDefinitions() == 1) {
									functions.setSelectedIndex(0);
								}
								else {
									functions.setSelectedIndex(index);
								}
							}
							change = true;
						}
						if (error) {
							value = JOptionPane.showOptionDialog(biosim.frame(), functionPanel,
									"Function Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
									options, options[0]);
						}
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Creates a frame used to edit units or create new ones.
	 */
	private void unitEditor(String option) {
		if (option.equals("OK") && unitDefs.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No unit definition selected.",
					"Must Select a Unit Definition", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel unitDefPanel = new JPanel(new BorderLayout());
			JPanel unitPanel = new JPanel(new GridLayout(2, 2));
			JLabel idLabel = new JLabel("ID:");
			unitID = new JTextField(12);
			JLabel nameLabel = new JLabel("Name:");
			unitName = new JTextField(12);
			JPanel unitListPanel = new JPanel(new BorderLayout());
			JPanel addUnitList = new JPanel();
			addList = new JButton("Add to List");
			removeList = new JButton("Remove from List");
			editList = new JButton("Edit List");
			addUnitList.add(addList);
			addUnitList.add(removeList);
			addUnitList.add(editList);
			addList.addActionListener(this);
			removeList.addActionListener(this);
			editList.addActionListener(this);
			JLabel unitListLabel = new JLabel("List of Units:");
			unitList = new JList();
			unitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(260, 220));
			scroll.setPreferredSize(new Dimension(276, 152));
			scroll.setViewportView(unitList);
			uList = new String[0];
			if (option.equals("OK")) {
				try {
					UnitDefinition unit = document.getModel().getUnitDefinition(
							(((String) unitDefs.getSelectedValue()).split(" ")[0]));
					unitID.setText(unit.getId());
					unitName.setText(unit.getName());
					uList = new String[(int) unit.getNumUnits()];
					for (int i = 0; i < unit.getNumUnits(); i++) {
						uList[i] = "";
						if (unit.getUnit(i).getMultiplier() != 1.0) {
							uList[i] = unit.getUnit(i).getMultiplier() + " * ";
						}
						if (unit.getUnit(i).getScale() != 0) {
							uList[i] = uList[i] + "10^" + unit.getUnit(i).getScale() + " * ";
						}
						uList[i] = uList[i] + unitToString(unit.getUnit(i));
						if (unit.getUnit(i).getExponent() != 1) {
							uList[i] = "( " + uList[i] + " )^" + unit.getUnit(i).getExponent();
						}
					}
				}
				catch (Exception e) {
				}
			}

			sort(uList);
			unitList.setListData(uList);
			unitList.setSelectedIndex(0);
			unitList.addMouseListener(this);
			unitListPanel.add(unitListLabel, "North");
			unitListPanel.add(scroll, "Center");
			unitListPanel.add(addUnitList, "South");
			unitPanel.add(idLabel);
			unitPanel.add(unitID);
			unitPanel.add(nameLabel);
			unitPanel.add(unitName);
			unitDefPanel.add(unitPanel, "North");
			unitDefPanel.add(unitListPanel, "South");
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), unitDefPanel,
					"Unit Definition Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (unitID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an ID into the ID field.",
										"Enter an ID", JOptionPane.ERROR_MESSAGE);
						error = true;
						value = JOptionPane.showOptionDialog(biosim.frame(), unitDefPanel,
								"Unit Definition Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
								null, options, options[0]);
					}
					else {
						String addUnit = "";
						addUnit = unitID.getText().trim();
						if (!(IDpat.matcher(addUnit).matches())) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"A unit definition ID can only contain letters, numbers, and underscores.",
									"Invalid ID", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else if (usedIDs.contains(addUnit)) {
							if (option.equals("OK")
									&& !addUnit.equals(((String) unitDefs.getSelectedValue()).split(" ")[0])) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter a Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (option.equals("Add")) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter a Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						// check valid eqn
						if (!error) {
							if (option.equals("OK")) {
								int index = unitDefs.getSelectedIndex();
								String val = ((String) unitDefs.getSelectedValue()).split(" ")[0];
								unitDefs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								units = Buttons.getList(units, unitDefs);
								unitDefs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								UnitDefinition u = document.getModel().getUnitDefinition(val);
								u.setId(unitID.getText().trim());
								u.setName(unitName.getText().trim());
								for (int i = 0; i < usedIDs.size(); i++) {
									if (usedIDs.get(i).equals(val)) {
										usedIDs.set(i, addUnit);
									}
								}
								while (u.getNumUnits() > 0) {
									u.getListOfUnits().remove(0);
								}
								for (int i = 0; i < uList.length; i++) {
									Unit unit = new Unit(extractUnitKind(uList[i]), Integer.valueOf(
											extractUnitExp(uList[i])).intValue(), Integer.valueOf(
											extractUnitScale(uList[i])).intValue(), Double.valueOf(
											extractUnitMult(uList[i])).doubleValue());
									u.addUnit(unit);
								}
								units[index] = addUnit;
								sort(units);
								unitDefs.setListData(units);
								unitDefs.setSelectedIndex(index);
							}
							else {
								int index = unitDefs.getSelectedIndex();
								UnitDefinition u = document.getModel().createUnitDefinition();
								u.setId(unitID.getText().trim());
								u.setName(unitName.getText().trim());
								usedIDs.add(addUnit);
								for (int i = 0; i < uList.length; i++) {
									Unit unit = new Unit(extractUnitKind(uList[i]), Integer.valueOf(
											extractUnitExp(uList[i])).intValue(), Integer.valueOf(
											extractUnitScale(uList[i])).intValue(), Double.valueOf(
											extractUnitMult(uList[i])).doubleValue());
									u.addUnit(unit);
								}
								JList add = new JList();
								Object[] adding = { addUnit };
								add.setListData(adding);
								add.setSelectedIndex(0);
								unitDefs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(units, unitDefs, add, false, null, null, null, null, null,
										null, biosim.frame());
								units = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									units[i] = (String) adding[i];
								}
								sort(units);
								unitDefs.setListData(units);
								unitDefs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (document.getModel().getNumUnitDefinitions() == 1) {
									unitDefs.setSelectedIndex(0);
								}
								else {
									unitDefs.setSelectedIndex(index);
								}
							}
							change = true;
						}
						if (error) {
							value = JOptionPane.showOptionDialog(biosim.frame(), unitDefPanel,
									"Unit Definition Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, options, options[0]);
						}
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Creates a frame used to edit unit list elements or create new ones.
	 */
	private void unitListEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && unitList.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No unit selected.", "Must Select an Unit",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel unitListPanel = new JPanel();
			JPanel ULPanel = new JPanel(new GridLayout(4, 2));
			JLabel kindLabel = new JLabel("Kind:");
			JLabel expLabel = new JLabel("Exponent:");
			JLabel scaleLabel = new JLabel("Scale:");
			JLabel multLabel = new JLabel("Multiplier:");
			String[] kinds = { "ampere", "becquerel", "candela", "celsius", "coulomb", "dimensionless",
					"farad", "gram", "gray", "henry", "hertz", "item", "joule", "katal", "kelvin",
					"kilogram", "litre", "lumen", "lux", "metre", "mole", "newton", "ohm", "pascal",
					"radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
			final JComboBox kindBox = new JComboBox(kinds);
			exp = new JTextField(12);
			exp.setText("1");
			scale = new JTextField(12);
			scale.setText("0");
			mult = new JTextField(12);
			mult.setText("1.0");
			if (option.equals("OK")) {
				String selected = (String) unitList.getSelectedValue();
				kindBox.setSelectedItem(extractUnitKind(selected));
				exp.setText(extractUnitExp(selected));
				scale.setText(extractUnitScale(selected));
				mult.setText(extractUnitMult(selected));
			}
			ULPanel.add(kindLabel);
			ULPanel.add(kindBox);
			ULPanel.add(expLabel);
			ULPanel.add(exp);
			ULPanel.add(scaleLabel);
			ULPanel.add(scale);
			ULPanel.add(multLabel);
			ULPanel.add(mult);
			unitListPanel.add(ULPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), unitListPanel, "Unit List Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (!error) {
						if (option.equals("OK")) {
							int index = unitList.getSelectedIndex();
							unitList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							uList = Buttons.getList(uList, unitList);
							unitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							uList[index] = "";
							if (!mult.getText().trim().equals("1.0")) {
								uList[index] = mult.getText().trim() + " * ";
							}
							if (!scale.getText().trim().equals("0")) {
								uList[index] = uList[index] + "10^" + scale.getText().trim() + " * ";
							}
							uList[index] = uList[index] + kindBox.getSelectedItem();
							if (!exp.getText().trim().equals("1")) {
								uList[index] = "( " + uList[index] + " )^" + exp.getText().trim();
							}
							sort(uList);
							unitList.setListData(uList);
							unitList.setSelectedIndex(index);
						}
						else {
							JList add = new JList();
							int index = unitList.getSelectedIndex();
							String addStr;
							addStr = "";
							if (!mult.getText().trim().equals("1.0")) {
								addStr = mult.getText().trim() + " * ";
							}
							if (!scale.getText().trim().equals("0")) {
								addStr = addStr + "10^" + scale.getText().trim() + " * ";
							}
							addStr = addStr + kindBox.getSelectedItem();
							if (!exp.getText().trim().equals("1")) {
								addStr = "( " + addStr + " )^" + exp.getText().trim();
							}
							Object[] adding = { addStr };
							add.setListData(adding);
							add.setSelectedIndex(0);
							unitList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							adding = Buttons.add(uList, unitList, add, false, null, null, null, null, null, null,
									biosim.frame());
							uList = new String[adding.length];
							for (int i = 0; i < adding.length; i++) {
								uList[i] = (String) adding[i];
							}
							sort(uList);
							unitList.setListData(uList);
							unitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							if (adding.length == 1) {
								unitList.setSelectedIndex(0);
							}
							else {
								unitList.setSelectedIndex(index);
							}
						}
						change = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), unitListPanel, "Unit List Editor",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Convert unit kind to string
	 */
	private String unitToString(Unit unit) {
		if (unit.isAmpere()) {
			return "ampere";
		}
		else if (unit.isBecquerel()) {
			return "becquerel";
		}
		else if (unit.isCandela()) {
			return "candela";
		}
		else if (unit.isCelsius()) {
			return "celsius";
		}
		else if (unit.isCoulomb()) {
			return "coulomb";
		}
		else if (unit.isDimensionless()) {
			return "dimensionless";
		}
		else if (unit.isFarad()) {
			return "farad";
		}
		else if (unit.isGram()) {
			return "gram";
		}
		else if (unit.isGray()) {
			return "gray";
		}
		else if (unit.isHenry()) {
			return "henry";
		}
		else if (unit.isHertz()) {
			return "hertz";
		}
		else if (unit.isItem()) {
			return "item";
		}
		else if (unit.isJoule()) {
			return "joule";
		}
		else if (unit.isKatal()) {
			return "katal";
		}
		else if (unit.isKelvin()) {
			return "kelvin";
		}
		else if (unit.isKilogram()) {
			return "kilogram";
		}
		else if (unit.isLitre()) {
			return "litre";
		}
		else if (unit.isLumen()) {
			return "lumen";
		}
		else if (unit.isLux()) {
			return "lux";
		}
		else if (unit.isMetre()) {
			return "metre";
		}
		else if (unit.isMole()) {
			return "mole";
		}
		else if (unit.isNewton()) {
			return "newton";
		}
		else if (unit.isOhm()) {
			return "ohm";
		}
		else if (unit.isPascal()) {
			return "pascal";
		}
		else if (unit.isRadian()) {
			return "radian";
		}
		else if (unit.isSecond()) {
			return "second";
		}
		else if (unit.isSiemens()) {
			return "siemens";
		}
		else if (unit.isSievert()) {
			return "sievert";
		}
		else if (unit.isSteradian()) {
			return "steradian";
		}
		else if (unit.isTesla()) {
			return "tesla";
		}
		else if (unit.isVolt()) {
			return "volt";
		}
		else if (unit.isWatt()) {
			return "watt";
		}
		else if (unit.isWeber()) {
			return "weber";
		}
		return "Unknown";
	}

	/**
	 * Extract unit kind from string
	 */
	private String extractUnitKind(String selected) {
		if (selected.contains(")^")) {
			if (selected.contains("10^")) {
				return selected.substring(selected.lastIndexOf("*") + 2, selected.indexOf(")") - 1);
			}
			else if (selected.contains("*")) {
				return selected.substring(selected.lastIndexOf("*") + 2, selected.indexOf(")") - 1);
			}
			else {
				return selected.substring(2, selected.indexOf(")") - 1);
			}
		}
		else if (selected.contains("10^")) {
			return selected.substring(selected.lastIndexOf("*") + 2);
		}
		else if (selected.contains("*")) {
			mult.setText(selected.substring(0, selected.indexOf("*") - 1));
			return selected.substring(selected.indexOf("*") + 2);
		}
		else {
			return selected;
		}
	}

	/**
	 * Extract unit exponent from string
	 */
	private String extractUnitExp(String selected) {
		if (selected.contains(")^")) {
			return selected.substring(selected.indexOf(")^") + 2);
		}
		return "1";
	}

	/**
	 * Extract unit scale from string
	 */
	private String extractUnitScale(String selected) {
		if (selected.contains(")^")) {
			if (selected.contains("10^")) {
				return selected.substring(selected.indexOf("10^") + 3, selected.lastIndexOf("*") - 1);
			}
		}
		else if (selected.contains("10^")) {
			return selected.substring(selected.indexOf("10^") + 3, selected.lastIndexOf("*") - 1);
		}
		return "0";
	}

	/**
	 * Extract unit multiplier from string
	 */
	private String extractUnitMult(String selected) {
		if (selected.contains(")^")) {
			if (selected.contains("10^")) {
				String multStr = selected.substring(2, selected.indexOf("*") - 1);
				if (!multStr.contains("10^")) {
					return multStr;
				}
			}
			else if (selected.contains("*")) {
				return selected.substring(2, selected.indexOf("*") - 1);
			}
			else if (selected.contains("10^")) {
				String multStr = selected.substring(0, selected.indexOf("*") - 1);
				if (!multStr.contains("10^")) {
					return multStr;
				}
			}
			else if (selected.contains("*")) {
				return selected.substring(0, selected.indexOf("*") - 1);
			}
		}
		return "1.0";
	}

	/**
	 * Creates a frame used to edit compartment types or create new ones.
	 */
	private void compTypeEditor(String option) {
		if (option.equals("OK") && compTypes.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No compartment type selected.",
					"Must Select a Compartment Type", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel compTypePanel = new JPanel();
			JPanel cpTypPanel = new JPanel(new GridLayout(2, 2));
			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			compTypeID = new JTextField(12);
			compTypeName = new JTextField(12);
			if (option.equals("OK")) {
				try {
					CompartmentType compType = document.getModel().getCompartmentType(
							(((String) compTypes.getSelectedValue()).split(" ")[0]));
					compTypeID.setText(compType.getId());
					compTypeName.setText(compType.getName());
				}
				catch (Exception e) {
				}
			}
			cpTypPanel.add(idLabel);
			cpTypPanel.add(compTypeID);
			cpTypPanel.add(nameLabel);
			cpTypPanel.add(compTypeName);
			compTypePanel.add(cpTypPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), compTypePanel,
					"Compartment Type Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (compTypeID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an ID into the ID field.",
										"Enter an ID", JOptionPane.ERROR_MESSAGE);
						error = true;
						value = JOptionPane.showOptionDialog(biosim.frame(), compTypePanel,
								"Compartment Type Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
								null, options, options[0]);
					}
					else {
						String addCompType = "";
						addCompType = compTypeID.getText().trim();
						if (!(IDpat.matcher(addCompType).matches())) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"A compartment type ID can only contain letters, numbers, and underscores.",
									"Invalid ID", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else if (usedIDs.contains(addCompType)) {
							if (option.equals("OK")
									&& !addCompType.equals(((String) compTypes.getSelectedValue()).split(" ")[0])) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter a Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (option.equals("Add")) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter a Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						// check valid eqn
						if (!error) {
							if (option.equals("OK")) {
								int index = compTypes.getSelectedIndex();
								String val = ((String) compTypes.getSelectedValue()).split(" ")[0];
								compTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								cpTyp = Buttons.getList(cpTyp, compTypes);
								compTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								CompartmentType c = document.getModel().getCompartmentType(val);
								c.setId(compTypeID.getText().trim());
								c.setName(compTypeName.getText().trim());
								for (int i = 0; i < usedIDs.size(); i++) {
									if (usedIDs.get(i).equals(val)) {
										usedIDs.set(i, addCompType);
									}
								}
								cpTyp[index] = addCompType;
								sort(cpTyp);
								compTypes.setListData(cpTyp);
								compTypes.setSelectedIndex(index);
								for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
									Compartment compartment = document.getModel().getCompartment(i);
									if (compartment.getCompartmentType().equals(val)) {
										compartment.setCompartmentType(addCompType);
									}
								}
								int index1 = compartments.getSelectedIndex();
								compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								comps = Buttons.getList(comps, compartments);
								compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								for (int i = 0; i < comps.length; i++) {
									if (comps[i].split(" ")[1].equals(val)) {
										comps[i] = comps[i].split(" ")[0] + " " + addCompType + " "
												+ comps[i].split(" ")[2];
									}
								}
								sort(comps);
								compartments.setListData(comps);
								compartments.setSelectedIndex(index1);
							}
							else {
								int index = compTypes.getSelectedIndex();
								CompartmentType c = document.getModel().createCompartmentType();
								c.setId(compTypeID.getText().trim());
								c.setName(compTypeName.getText().trim());
								usedIDs.add(addCompType);
								JList add = new JList();
								Object[] adding = { addCompType };
								add.setListData(adding);
								add.setSelectedIndex(0);
								compTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(cpTyp, compTypes, add, false, null, null, null, null, null,
										null, biosim.frame());
								cpTyp = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									cpTyp[i] = (String) adding[i];
								}
								sort(cpTyp);
								compTypes.setListData(cpTyp);
								compTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (document.getModel().getNumCompartmentTypes() == 1) {
									compTypes.setSelectedIndex(0);
								}
								else {
									compTypes.setSelectedIndex(index);
								}
							}
							change = true;
						}
						if (error) {
							value = JOptionPane.showOptionDialog(biosim.frame(), compTypePanel,
									"Compartment Type Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, options, options[0]);
						}
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Creates a frame used to edit species types or create new ones.
	 */
	private void specTypeEditor(String option) {
		if (option.equals("OK") && specTypes.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No species type selected.",
					"Must Select a Species Type", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel specTypePanel = new JPanel();
			JPanel spTypPanel = new JPanel(new GridLayout(2, 2));
			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			specTypeID = new JTextField(12);
			specTypeName = new JTextField(12);
			if (option.equals("OK")) {
				try {
					SpeciesType specType = document.getModel().getSpeciesType(
							(((String) specTypes.getSelectedValue()).split(" ")[0]));
					specTypeID.setText(specType.getId());
					specTypeName.setText(specType.getName());
				}
				catch (Exception e) {
				}
			}
			spTypPanel.add(idLabel);
			spTypPanel.add(specTypeID);
			spTypPanel.add(nameLabel);
			spTypPanel.add(specTypeName);
			specTypePanel.add(spTypPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), specTypePanel,
					"Species Type Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (specTypeID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an ID into the ID field.",
										"Enter an ID", JOptionPane.ERROR_MESSAGE);
						error = true;
						value = JOptionPane.showOptionDialog(biosim.frame(), specTypePanel,
								"Species Type Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
								options, options[0]);
					}
					else {
						String addSpecType = "";
						addSpecType = specTypeID.getText().trim();
						if (!(IDpat.matcher(addSpecType).matches())) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"A species type ID can only contain letters, numbers, and underscores.",
									"Invalid ID", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else if (usedIDs.contains(addSpecType)) {
							if (option.equals("OK")
									&& !addSpecType.equals(((String) specTypes.getSelectedValue()).split(" ")[0])) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter a Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (option.equals("Add")) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter a Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						// check valid eqn
						if (!error) {
							if (option.equals("OK")) {
								int index = specTypes.getSelectedIndex();
								String val = ((String) specTypes.getSelectedValue()).split(" ")[0];
								specTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								spTyp = Buttons.getList(spTyp, specTypes);
								specTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								SpeciesType s = document.getModel().getSpeciesType(val);
								s.setId(specTypeID.getText().trim());
								s.setName(specTypeName.getText().trim());
								for (int i = 0; i < usedIDs.size(); i++) {
									if (usedIDs.get(i).equals(val)) {
										usedIDs.set(i, addSpecType);
									}
								}
								spTyp[index] = addSpecType;
								sort(spTyp);
								specTypes.setListData(spTyp);
								specTypes.setSelectedIndex(index);
								for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
									Species species = document.getModel().getSpecies(i);
									if (species.getSpeciesType().equals(val)) {
										species.setSpeciesType(addSpecType);
									}
								}
								int index1 = species.getSelectedIndex();
								species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								specs = Buttons.getList(specs, species);
								species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								for (int i = 0; i < specs.length; i++) {
									if (specs[i].split(" ")[1].equals(val)) {
										specs[i] = specs[i].split(" ")[0] + " " + addSpecType + " "
												+ specs[i].split(" ")[2] + " " + specs[i].split(" ")[3];
									}
								}
								sort(specs);
								species.setListData(specs);
								species.setSelectedIndex(index1);
							}
							else {
								int index = specTypes.getSelectedIndex();
								SpeciesType s = document.getModel().createSpeciesType();
								s.setId(specTypeID.getText().trim());
								s.setName(specTypeName.getText().trim());
								usedIDs.add(addSpecType);
								JList add = new JList();
								Object[] adding = { addSpecType };
								add.setListData(adding);
								add.setSelectedIndex(0);
								specTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(spTyp, specTypes, add, false, null, null, null, null, null,
										null, biosim.frame());
								spTyp = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									spTyp[i] = (String) adding[i];
								}
								sort(spTyp);
								specTypes.setListData(spTyp);
								specTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (document.getModel().getNumSpeciesTypes() == 1) {
									specTypes.setSelectedIndex(0);
								}
								else {
									specTypes.setSelectedIndex(index);
								}
							}
							change = true;
						}
						if (error) {
							value = JOptionPane.showOptionDialog(biosim.frame(), specTypePanel,
									"Species Type Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, options, options[0]);
						}
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Creates a frame used to edit initial assignments or create new ones.
	 */
	private void initEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && initAssigns.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No initial assignment selected.",
					"Must Select an Initial Assignment", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel initAssignPanel = new JPanel();
			JPanel initPanel = new JPanel(new GridLayout(2, 2));
			JLabel varLabel = new JLabel("Symbol:");
			JLabel assignLabel = new JLabel("Assignment:");
			initVar = new JComboBox();
			String selected;
			if (option.equals("OK")) {
				selected = ((String) initAssigns.getSelectedValue());
			}
			else {
				selected = new String("");
			}
			Model model = document.getModel();
			ListOf ids = model.getListOfCompartments();
			for (int i = 0; i < model.getNumCompartments(); i++) {
				String id = ((Compartment) ids.get(i)).getId();
				if (keepVar(selected.split(" ")[0], id, true, false, false, false)
						&& ((Compartment) ids.get(i)).getSpatialDimensions() != 0) {
					initVar.addItem(id);
				}
			}
			ids = model.getListOfParameters();
			for (int i = 0; i < model.getNumParameters(); i++) {
				String id = ((Parameter) ids.get(i)).getId();
				if (keepVar(selected.split(" ")[0], id, true, false, false, false)) {
					initVar.addItem(id);
				}
			}
			ids = model.getListOfSpecies();
			for (int i = 0; i < model.getNumSpecies(); i++) {
				String id = ((Species) ids.get(i)).getId();
				if (keepVar(selected.split(" ")[0], id, true, false, false, false)) {
					initVar.addItem(id);
				}
			}
			initMath = new JTextField(12);
			int Rindex = -1;
			if (option.equals("OK")) {
				initVar.setSelectedItem(selected.split(" ")[0]);
				initMath.setText(selected.substring(selected.indexOf('=') + 2));
				ListOf r = document.getModel().getListOfInitialAssignments();
				for (int i = 0; i < document.getModel().getNumInitialAssignments(); i++) {
					if (sbmlLib.formulaToString(((InitialAssignment) r.get(i)).getMath()).equals(
							initMath.getText())
							&& ((InitialAssignment) r.get(i)).getSymbol().equals(initVar.getSelectedItem())) {
						Rindex = i;
					}
				}
			}
			initPanel.add(varLabel);
			initPanel.add(initVar);
			initPanel.add(assignLabel);
			initPanel.add(initMath);
			initAssignPanel.add(initPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), initAssignPanel,
					"Initial Assignment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					String addVar = "";
					addVar = (String) initVar.getSelectedItem();
					if (initMath.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(), "Initial assignment is missing.",
								"Enter Assignment", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (sbmlLib.parseFormula(initMath.getText().trim()) == null) {
						JOptionPane.showMessageDialog(biosim.frame(), "Initial assignment is not valid.",
								"Enter Valid Assignment", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (!error) {
						if (option.equals("OK")) {
							int index = initAssigns.getSelectedIndex();
							initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							inits = Buttons.getList(inits, initAssigns);
							initAssigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							InitialAssignment r = (InitialAssignment) (document.getModel()
									.getListOfInitialAssignments()).get(Rindex);
							String addStr;
							r.setSymbol(addVar);
							r.setMath(sbmlLib.parseFormula(initMath.getText().trim()));
							inits[index] = addVar + " = " + sbmlLib.formulaToString(r.getMath());
							sort(inits);
							initAssigns.setListData(inits);
							initAssigns.setSelectedIndex(index);
						}
						else {
							JList add = new JList();
							int index = rules.getSelectedIndex();
							String addStr;
							InitialAssignment r = document.getModel().createInitialAssignment();
							r.setSymbol(addVar);
							r.setMath(sbmlLib.parseFormula(initMath.getText().trim()));
							addStr = addVar + " = " + sbmlLib.formulaToString(r.getMath());
							Object[] adding = { addStr };
							add.setListData(adding);
							add.setSelectedIndex(0);
							initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							adding = Buttons.add(inits, initAssigns, add, false, null, null, null, null, null,
									null, biosim.frame());
							inits = new String[adding.length];
							for (int i = 0; i < adding.length; i++) {
								inits[i] = (String) adding[i];
							}
							sort(inits);
							initAssigns.setListData(inits);
							initAssigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							if (document.getModel().getNumInitialAssignments() == 1) {
								initAssigns.setSelectedIndex(0);
							}
							else {
								initAssigns.setSelectedIndex(index);
							}
						}
						change = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), initAssignPanel,
								"Initial Assignment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
								null, options, options[0]);
					}
				}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
	}

	/**
	 * Determines if a variable is already in an initial or assignment rule
	 */
	private boolean keepVar(String selected, String id, boolean checkInit, boolean checkRate,
			boolean checkEventAssign, boolean checkOnlyCurEvent) {
		libsbml sbmlLib = new libsbml();
		if (!selected.equals(id)) {
			if (checkInit) {
				for (int j = 0; j < inits.length; j++) {
					if (id.equals(inits[j].split(" ")[0])) {
						return false;
					}
				}
			}
			if (checkOnlyCurEvent) {
				for (int j = 0; j < assign.length; j++) {
					if (id.equals(assign[j].split(" ")[0])) {
						return false;
					}
				}
			}
			if (checkEventAssign) {
				ListOf e = document.getModel().getListOfEvents();
				for (int i = 0; i < document.getModel().getNumEvents(); i++) {
					org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) e.get(i);
					for (int j = 0; j < event.getNumEventAssignments(); j++) {
						if (id.equals(event.getEventAssignment(j).getVariable())) {
							return false;
						}
					}
				}
			}
			for (int j = 0; j < rul.length; j++) {
				if (id.equals(rul[j].split(" ")[0])) {
					return false;
				}
				if (checkRate && id.equals(rul[j].split(" ")[1])) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Creates a frame used to edit rules or create new ones.
	 */
	private void ruleEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && rules.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No rule selected.", "Must Select a Rule",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel rulePanel = new JPanel();
			JPanel rulPanel = new JPanel(new GridLayout(3, 2));
			JLabel typeLabel = new JLabel("Type:");
			JLabel varLabel = new JLabel("Variable:");
			JLabel ruleLabel = new JLabel("Rule:");
			String[] list = { "Algebraic", "Assignment", "Rate" };
			final JComboBox ruleType = new JComboBox(list);
			ruleVar = new JComboBox();
			ruleMath = new JTextField(12);
			ruleVar.setEnabled(false);
			ruleType.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (((String) ruleType.getSelectedItem()).equals("Assignment")) {
						assignRuleVar("");
						ruleVar.setEnabled(true);
					}
					else if (((String) ruleType.getSelectedItem()).equals("Rate")) {
						rateRuleVar("");
						ruleVar.setEnabled(true);
					}
					else {
						ruleVar.removeAllItems();
						ruleVar.setEnabled(false);
					}
				}
			});
			int Rindex = -1;
			if (option.equals("OK")) {
				ruleType.setEnabled(false);
				String selected = ((String) rules.getSelectedValue());
				// algebraic rule
				if ((selected.split(" ")[0]).equals("0")) {
					ruleType.setSelectedItem("Algebraic");
					ruleVar.setEnabled(false);
					ruleMath.setText(selected.substring(4));
					ListOf r = document.getModel().getListOfRules();
					for (int i = 0; i < document.getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isAlgebraic())
								&& ((Rule) r.get(i)).getFormula().equals(ruleMath.getText())) {
							Rindex = i;
						}
					}
				}
				else if ((selected.split(" ")[0]).equals("d(")) {
					ruleType.setSelectedItem("Rate");
					rateRuleVar(selected.split(" ")[1]);
					ruleVar.setEnabled(true);
					ruleVar.setSelectedItem(selected.split(" ")[1]);
					ruleMath.setText(selected.substring(selected.indexOf('=') + 2));
					ListOf r = document.getModel().getListOfRules();
					for (int i = 0; i < document.getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isRate())
								&& ((Rule) r.get(i)).getFormula().equals(ruleMath.getText())
								&& ((Rule) r.get(i)).getVariable().equals(ruleVar.getSelectedItem())) {
							Rindex = i;
						}
					}
				}
				else {
					ruleType.setSelectedItem("Assignment");
					assignRuleVar(selected.split(" ")[0]);
					ruleVar.setEnabled(true);
					ruleVar.setSelectedItem(selected.split(" ")[0]);
					ruleMath.setText(selected.substring(selected.indexOf('=') + 2));
					ListOf r = document.getModel().getListOfRules();
					for (int i = 0; i < document.getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isAssignment())
								&& ((Rule) r.get(i)).getFormula().equals(ruleMath.getText())
								&& ((Rule) r.get(i)).getVariable().equals(ruleVar.getSelectedItem())) {
							Rindex = i;
						}
					}
				}
			}
			rulPanel.add(typeLabel);
			rulPanel.add(ruleType);
			rulPanel.add(varLabel);
			rulPanel.add(ruleVar);
			rulPanel.add(ruleLabel);
			rulPanel.add(ruleMath);
			rulePanel.add(rulPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), rulePanel, "Rule Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					String addVar = "";
					addVar = (String) ruleVar.getSelectedItem();
					if (ruleMath.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(), "Rule must have formula.",
								"Enter Rule Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (sbmlLib.parseFormula(ruleMath.getText().trim()) == null) {
						JOptionPane.showMessageDialog(biosim.frame(), "Rule formula is not valid.",
								"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (!error) {
						if (option.equals("OK")) {
							int index = rules.getSelectedIndex();
							rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							rul = Buttons.getList(rul, rules);
							rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							Rule r = (Rule) (document.getModel().getListOfRules()).get(Rindex);
							String addStr;
							if (ruleType.getSelectedItem().equals("Algebraic")) {
								r.setMath(sbmlLib.parseFormula(ruleMath.getText().trim()));
								rul[index] = "0 = " + sbmlLib.formulaToString(r.getMath());
							}
							else if (ruleType.getSelectedItem().equals("Rate")) {
								r.setVariable(addVar);
								r.setMath(sbmlLib.parseFormula(ruleMath.getText().trim()));
								rul[index] = "d( " + addVar + " )/dt = " + sbmlLib.formulaToString(r.getMath());
							}
							else {
								r.setVariable(addVar);
								r.setMath(sbmlLib.parseFormula(ruleMath.getText().trim()));
								rul[index] = addVar + " = " + sbmlLib.formulaToString(r.getMath());
							}
							sort(rul);
							rules.setListData(rul);
							rules.setSelectedIndex(index);
						}
						else {
							JList add = new JList();
							int index = rules.getSelectedIndex();
							String addStr;
							if (ruleType.getSelectedItem().equals("Algebraic")) {
								AlgebraicRule r = document.getModel().createAlgebraicRule();
								r.setMath(sbmlLib.parseFormula(ruleMath.getText().trim()));
								addStr = "0 = " + sbmlLib.formulaToString(r.getMath());
							}
							else if (ruleType.getSelectedItem().equals("Rate")) {
								RateRule r = document.getModel().createRateRule();
								r.setVariable(addVar);
								r.setMath(sbmlLib.parseFormula(ruleMath.getText().trim()));
								addStr = "d( " + addVar + " )/dt = " + sbmlLib.formulaToString(r.getMath());
							}
							else {
								AssignmentRule r = document.getModel().createAssignmentRule();
								r.setVariable(addVar);
								r.setMath(sbmlLib.parseFormula(ruleMath.getText().trim()));
								addStr = addVar + " = " + sbmlLib.formulaToString(r.getMath());
							}
							Object[] adding = { addStr };
							add.setListData(adding);
							add.setSelectedIndex(0);
							rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							adding = Buttons.add(rul, rules, add, false, null, null, null, null, null, null,
									biosim.frame());
							rul = new String[adding.length];
							for (int i = 0; i < adding.length; i++) {
								rul[i] = (String) adding[i];
							}
							sort(rul);
							rules.setListData(rul);
							rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							if (document.getModel().getNumRules() == 1) {
								rules.setSelectedIndex(0);
							}
							else {
								rules.setSelectedIndex(index);
							}
						}
						change = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), rulePanel, "Rule Editor",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Create comboBox for assignments rules
	 */
	private void assignRuleVar(String selected) {
		ruleVar.removeAllItems();
		Model model = document.getModel();
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			String id = ((Compartment) ids.get(i)).getId();
			if (!((Compartment) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, true, true, true, false))
					ruleVar.addItem(((Compartment) ids.get(i)).getId());
			}
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < model.getNumParameters(); i++) {
			String id = ((Parameter) ids.get(i)).getId();
			if (!((Parameter) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, true, true, true, false))
					ruleVar.addItem(((Parameter) ids.get(i)).getId());
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			String id = ((Species) ids.get(i)).getId();
			if (!((Species) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, true, true, true, false))
					ruleVar.addItem(((Species) ids.get(i)).getId());
			}
		}
	}

	/**
	 * Create comboBox for rate rules
	 */
	private void rateRuleVar(String selected) {
		ruleVar.removeAllItems();
		Model model = document.getModel();
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			String id = ((Compartment) ids.get(i)).getId();
			if (!((Compartment) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, false, true, false, false))
					ruleVar.addItem(((Compartment) ids.get(i)).getId());
			}
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < model.getNumParameters(); i++) {
			String id = ((Parameter) ids.get(i)).getId();
			if (!((Parameter) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, false, true, false, false))
					ruleVar.addItem(((Parameter) ids.get(i)).getId());
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			String id = ((Species) ids.get(i)).getId();
			if (!((Species) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, false, true, false, false))
					ruleVar.addItem(((Species) ids.get(i)).getId());
			}
		}
	}

	/**
	 * Creates a frame used to edit events or create new ones.
	 */
	private void eventEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && events.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No event selected.", "Must Select an Event",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel eventPanel = new JPanel(new BorderLayout());
			JPanel evPanel = new JPanel(new GridLayout(2, 2));
			// JPanel evPanel = new JPanel(new GridLayout(3, 2));
			// JLabel IDLabel = new JLabel("ID:");
			JLabel triggerLabel = new JLabel("Trigger:");
			JLabel delayLabel = new JLabel("Delay:");
			// eventID = new JTextField(12);
			eventTrigger = new JTextField(12);
			eventDelay = new JTextField(12);
			JPanel eventAssignPanel = new JPanel(new BorderLayout());
			JPanel addEventAssign = new JPanel();
			addAssignment = new JButton("Add Assignment");
			removeAssignment = new JButton("Remove Assignment");
			editAssignment = new JButton("Edit Assignment");
			addEventAssign.add(addAssignment);
			addEventAssign.add(removeAssignment);
			addEventAssign.add(editAssignment);
			addAssignment.addActionListener(this);
			removeAssignment.addActionListener(this);
			editAssignment.addActionListener(this);
			JLabel eventAssignLabel = new JLabel("List of Event Assignments:");
			eventAssign = new JList();
			eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(260, 220));
			scroll.setPreferredSize(new Dimension(276, 152));
			scroll.setViewportView(eventAssign);
			assign = new String[0];
			int Eindex = -1;
			if (option.equals("OK")) {
				String selected = ((String) events.getSelectedValue());
				eventTrigger.setText(selected);
				ListOf e = document.getModel().getListOfEvents();
				for (int i = 0; i < document.getModel().getNumEvents(); i++) {
					org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) e.get(i);
					if (sbmlLib.formulaToString(event.getTrigger().getMath()).equals(selected)) {
						Eindex = i;
						// eventID.setText(e.getId());
						if (event.isSetDelay()) {
							eventDelay.setText(sbmlLib.formulaToString(event.getDelay().getMath()));
						}
						assign = new String[(int) event.getNumEventAssignments()];
						for (int j = 0; j < event.getNumEventAssignments(); j++) {
							assign[j] = event.getEventAssignment(j).getVariable() + " = "
									+ sbmlLib.formulaToString(event.getEventAssignment(j).getMath());
						}
					}
				}
			}
			sort(assign);
			eventAssign.setListData(assign);
			eventAssign.setSelectedIndex(0);
			eventAssign.addMouseListener(this);
			eventAssignPanel.add(eventAssignLabel, "North");
			eventAssignPanel.add(scroll, "Center");
			eventAssignPanel.add(addEventAssign, "South");
			// evPanel.add(IDLabel);
			// evPanel.add(eventID);
			evPanel.add(triggerLabel);
			evPanel.add(eventTrigger);
			evPanel.add(delayLabel);
			evPanel.add(eventDelay);
			eventPanel.add(evPanel, "North");
			eventPanel.add(eventAssignPanel, "South");
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), eventPanel, "Event Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (eventTrigger.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(), "Event must have a trigger formula.",
								"Enter Trigger Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (sbmlLib.parseFormula(eventTrigger.getText().trim()) == null) {
						JOptionPane.showMessageDialog(biosim.frame(), "Trigger formula is not valid.",
								"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (!eventDelay.getText().trim().equals("")
							&& sbmlLib.parseFormula(eventDelay.getText().trim()) == null) {
						JOptionPane.showMessageDialog(biosim.frame(), "Delay formula is not valid.",
								"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (assign.length == 0) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"Event must have at least one event assignment.", "Event Assignment Needed",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (!error) {
						if (option.equals("OK")) {
							int index = events.getSelectedIndex();
							events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							ev = Buttons.getList(ev, events);
							events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							org.sbml.libsbml.Event e = (org.sbml.libsbml.Event) (document.getModel()
									.getListOfEvents()).get(Eindex);
							Trigger trigger = new Trigger(sbmlLib.parseFormula(eventTrigger.getText().trim()));
							e.setTrigger(trigger);
							if (eventDelay.getText().trim().equals("")) {
								e.unsetDelay();
							}
							else {
								Delay delay = new Delay(sbmlLib.parseFormula(eventDelay.getText().trim()));
								e.setDelay(delay);
							}
							while (e.getNumEventAssignments() > 0) {
								e.getListOfEventAssignments().remove(0);
							}
							for (int i = 0; i < assign.length; i++) {
								EventAssignment ea = e.createEventAssignment();
								ea.setVariable(assign[i].split(" ")[0]);
								ea.setMath(sbmlLib.parseFormula(assign[i].split(" ")[2]));
							}
							// if (eventID.getText().trim().equals("")) {
							// e.unsetId();
							// } else {
							// e.setId(eventID.getText().trim());
							// }
							ev[index] = sbmlLib.formulaToString(e.getTrigger().getMath());
							sort(ev);
							events.setListData(ev);
							events.setSelectedIndex(index);
						}
						else {
							JList add = new JList();
							int index = events.getSelectedIndex();
							org.sbml.libsbml.Event e = document.getModel().createEvent();
							Trigger trigger = new Trigger(sbmlLib.parseFormula(eventTrigger.getText().trim()));
							e.setTrigger(trigger);
							if (eventDelay.getText().trim().equals("")) {
								e.unsetDelay();
							}
							else {
								Delay delay = new Delay(sbmlLib.parseFormula(eventDelay.getText().trim()));
								e.setDelay(delay);
							}
							for (int i = 0; i < assign.length; i++) {
								EventAssignment ea = e.createEventAssignment();
								ea.setVariable(assign[i].split(" ")[0]);
								ea.setMath(sbmlLib.parseFormula(assign[i].split(" ")[2]));
							}
							// if (eventID.getText().trim().equals("")) {
							// e.unsetId();
							// } else {
							// e.setId(eventID.getText().trim());
							// }
							Object[] adding = { sbmlLib.formulaToString(e.getTrigger().getMath()) };
							add.setListData(adding);
							add.setSelectedIndex(0);
							events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							adding = Buttons.add(ev, events, add, false, null, null, null, null, null, null,
									biosim.frame());
							ev = new String[adding.length];
							for (int i = 0; i < adding.length; i++) {
								ev[i] = (String) adding[i];
							}
							sort(ev);
							events.setListData(ev);
							events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							if (document.getModel().getNumEvents() == 1) {
								events.setSelectedIndex(0);
							}
							else {
								events.setSelectedIndex(index);
							}
						}
						change = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), eventPanel, "Event Editor",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Creates a frame used to edit event assignments or create new ones.
	 */
	private void eventAssignEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && eventAssign.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No event assignment selected.",
					"Must Select an Event Assignment", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel eventAssignPanel = new JPanel();
			JPanel EAPanel = new JPanel(new GridLayout(2, 2));
			JLabel idLabel = new JLabel("Variable:");
			JLabel eqnLabel = new JLabel("Assignment:");
			eaID = new JComboBox();
			String selected;
			if (option.equals("OK")) {
				selected = ((String) eventAssign.getSelectedValue()).split(" ")[0];
			}
			else {
				selected = "";
			}
			Model model = document.getModel();
			ListOf ids = model.getListOfCompartments();
			for (int i = 0; i < model.getNumCompartments(); i++) {
				String id = ((Compartment) ids.get(i)).getId();
				if (!((Compartment) ids.get(i)).getConstant()) {
					if (keepVar(selected, id, false, false, false, true)) {
						eaID.addItem(id);
					}
				}
			}
			ids = model.getListOfParameters();
			for (int i = 0; i < model.getNumParameters(); i++) {
				String id = ((Parameter) ids.get(i)).getId();
				if (!((Parameter) ids.get(i)).getConstant()) {
					if (keepVar(selected, id, false, false, false, true)) {
						eaID.addItem(id);
					}
				}
			}
			ids = model.getListOfSpecies();
			for (int i = 0; i < model.getNumSpecies(); i++) {
				String id = ((Species) ids.get(i)).getId();
				if (!((Species) ids.get(i)).getConstant()) {
					if (keepVar(selected, id, false, false, false, true)) {
						eaID.addItem(id);
					}
				}
			}
			eqn = new JTextField(12);
			if (option.equals("OK")) {
				String selectAssign = ((String) eventAssign.getSelectedValue());
				eaID.setSelectedItem(selectAssign.split(" ")[0]);
				eqn.setText(selectAssign.split(" ")[2]);
			}
			EAPanel.add(idLabel);
			EAPanel.add(eaID);
			EAPanel.add(eqnLabel);
			EAPanel.add(eqn);
			eventAssignPanel.add(EAPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), eventAssignPanel,
					"Event Asssignment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (eqn.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(), "Event assignment is missing.",
								"Enter Assignment", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (sbmlLib.parseFormula(eqn.getText().trim()) == null) {
						JOptionPane.showMessageDialog(biosim.frame(), "Event assignment is not valid.",
								"Enter Valid Assignment", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (!error) {
						if (option.equals("OK")) {
							int index = eventAssign.getSelectedIndex();
							eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							assign = Buttons.getList(assign, eventAssign);
							eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							assign[index] = eaID.getSelectedItem() + " = " + eqn.getText().trim();
							sort(assign);
							eventAssign.setListData(assign);
							eventAssign.setSelectedIndex(index);
						}
						else {
							JList add = new JList();
							int index = eventAssign.getSelectedIndex();
							Object[] adding = { eaID.getSelectedItem() + " = " + eqn.getText().trim() };
							add.setListData(adding);
							add.setSelectedIndex(0);
							eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							adding = Buttons.add(assign, eventAssign, add, false, null, null, null, null, null,
									null, biosim.frame());
							assign = new String[adding.length];
							for (int i = 0; i < adding.length; i++) {
								assign[i] = (String) adding[i];
							}
							sort(assign);
							eventAssign.setListData(assign);
							eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							if (adding.length == 1) {
								eventAssign.setSelectedIndex(0);
							}
							else {
								eventAssign.setSelectedIndex(index);
							}
						}
						change = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), eventAssignPanel,
								"Event Assignment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
								null, options, options[0]);
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Creates a frame used to edit constraints or create new ones.
	 */
	private void constraintEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && constraints.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No constraint selected.",
					"Must Select a Constraint", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel constraintPanel = new JPanel();
			JPanel consPanel = new JPanel(new GridLayout(2, 2));
			JLabel mathLabel = new JLabel("Constraint:");
			JLabel messageLabel = new JLabel("Messsage:");
			consMath = new JTextField(20);
			consMessage = new JTextField(20);
			int Cindex = -1;
			if (option.equals("OK")) {
				String selected = ((String) constraints.getSelectedValue());
				consMath.setText(selected);
				ListOf c = document.getModel().getListOfConstraints();
				for (int i = 0; i < document.getModel().getNumConstraints(); i++) {
					if (sbmlLib.formulaToString(((Constraint) c.get(i)).getMath()).equals(selected)) {
						Cindex = i;
						if (((Constraint) c.get(i)).isSetMessage()) {
							consMessage.setText((((Constraint) c.get(i)).getMessage()).getCharacters());
						}
					}
				}
			}
			consPanel.add(mathLabel);
			consPanel.add(consMath);
			consPanel.add(messageLabel);
			consPanel.add(consMessage);
			constraintPanel.add(consPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), constraintPanel,
					"Constraint Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
					options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (consMath.getText().trim().equals("")
							|| sbmlLib.parseFormula(consMath.getText().trim()) == null) {
						JOptionPane.showMessageDialog(biosim.frame(), "You must enter a valid formula.",
								"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (!error) {
						if (option.equals("OK")) {
							int index = constraints.getSelectedIndex();
							constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							cons = Buttons.getList(cons, constraints);
							constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							Constraint c = (Constraint) (document.getModel().getListOfConstraints()).get(Cindex);
							c.setMath(sbmlLib.parseFormula(consMath.getText().trim()));
							if (!consMessage.getText().trim().equals("")) {
								XMLNode xmlNode = new XMLNode();
								xmlNode.append(consMessage.getText().trim());
								c.setMessage(xmlNode);
							}
							else {
								c.unsetMessage();
							}
							cons[index] = sbmlLib.formulaToString(c.getMath());
							sort(cons);
							constraints.setListData(cons);
							constraints.setSelectedIndex(index);
						}
						else {
							JList add = new JList();
							int index = rules.getSelectedIndex();
							Constraint c = document.getModel().createConstraint();
							c.setMath(sbmlLib.parseFormula(consMath.getText().trim()));
							if (!consMessage.getText().trim().equals("")) {
								XMLNode xmlNode = new XMLNode();
								xmlNode.append(consMessage.getText().trim());
								c.setMessage(xmlNode);
							}
							else {
								c.unsetMessage();
							}
							Object[] adding = { sbmlLib.formulaToString(c.getMath()) };
							add.setListData(adding);
							add.setSelectedIndex(0);
							constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							adding = Buttons.add(cons, constraints, add, false, null, null, null, null, null,
									null, biosim.frame());
							cons = new String[adding.length];
							for (int i = 0; i < adding.length; i++) {
								cons[i] = (String) adding[i];
							}
							sort(cons);
							constraints.setListData(cons);
							constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							if (document.getModel().getNumConstraints() == 1) {
								constraints.setSelectedIndex(0);
							}
							else {
								constraints.setSelectedIndex(index);
							}
						}
						change = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), constraintPanel,
								"Constraint Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
								options, options[0]);
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Creates a frame used to edit compartments or create new ones.
	 */
	private void compartEditor(String option) {
		if (option.equals("OK") && compartments.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No compartment selected.",
					"Must Select a Compartment", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel compartPanel = new JPanel();
			JPanel compPanel = new JPanel(new GridLayout(8, 2));
			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			JLabel typeLabel = new JLabel("Type:");
			JLabel dimLabel = new JLabel("Dimensions:");
			JLabel sizeLabel = new JLabel("Size:");
			JLabel compUnitsLabel = new JLabel("Units:");
			JLabel outsideLabel = new JLabel("Outside:");
			JLabel constLabel = new JLabel("Constant:");
			compID = new JTextField(12);
			compName = new JTextField(12);
			ListOf listOfCompTypes = document.getModel().getListOfCompartmentTypes();
			String[] compTypeList = new String[(int) document.getModel().getNumCompartmentTypes() + 1];
			compTypeList[0] = "( none )";
			for (int i = 0; i < document.getModel().getNumCompartmentTypes(); i++) {
				compTypeList[i + 1] = ((CompartmentType) listOfCompTypes.get(i)).getId();
			}
			sort(compTypeList);
			Object[] choices = compTypeList;
			compTypeBox = new JComboBox(choices);
			Object[] dims = { "0", "1", "2", "3" };
			dimBox = new JComboBox(dims);
			dimBox.setSelectedItem("3");
			compSize = new JTextField(12);
			compUnits = new JComboBox();
			compOutside = new JComboBox();
			String[] optionsTF = { "true", "false" };
			compConstant = new JComboBox(optionsTF);
			compConstant.setSelectedItem("true");
			String selected = "";
			if (option.equals("OK")) {
				selected = ((String) compartments.getSelectedValue()).split(" ")[0];
			}
			setCompartOptions(selected, "3");
			dimBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setCompartOptions(((String) compartments.getSelectedValue()).split(" ")[0],
							(String) dimBox.getSelectedItem());
				}
			});
			if (option.equals("OK")) {
				try {
					Compartment compartment = document.getModel().getCompartment(
							(((String) compartments.getSelectedValue()).split(" ")[0]));
					compID.setText(compartment.getId());
					compName.setText(compartment.getName());
					if (compartment.isSetCompartmentType()) {
						compTypeBox.setSelectedItem(compartment.getCompartmentType());
					}
					dimBox.setSelectedItem(String.valueOf(compartment.getSpatialDimensions()));
					setCompartOptions(((String) compartments.getSelectedValue()).split(" ")[0], String
							.valueOf(compartment.getSpatialDimensions()));
					if (compartment.isSetSize()) {
						compSize.setText("" + compartment.getSize());
					}
					if (compartment.isSetUnits()) {
						compUnits.setSelectedItem(compartment.getUnits());
					}
					else {
						compUnits.setSelectedItem("( none )");
					}
					if (compartment.isSetOutside()) {
						compOutside.setSelectedItem(compartment.getOutside());
					}
					else {
						compOutside.setSelectedItem("( none )");
					}
					if (compartment.getConstant()) {
						compConstant.setSelectedItem("true");
					}
					else {
						compConstant.setSelectedItem("false");
					}
				}
				catch (Exception e) {
				}
			}
			compPanel.add(idLabel);
			compPanel.add(compID);
			compPanel.add(nameLabel);
			compPanel.add(compName);
			compPanel.add(typeLabel);
			compPanel.add(compTypeBox);
			compPanel.add(dimLabel);
			compPanel.add(dimBox);
			compPanel.add(sizeLabel);
			compPanel.add(compSize);
			compPanel.add(compUnitsLabel);
			compPanel.add(compUnits);
			compPanel.add(outsideLabel);
			compPanel.add(compOutside);
			compPanel.add(constLabel);
			compPanel.add(compConstant);
			compartPanel.add(compPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), compartPanel, "Compartment Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (compID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an ID into the ID field.",
										"Enter An ID", JOptionPane.ERROR_MESSAGE);
						error = true;
						value = JOptionPane.showOptionDialog(biosim.frame(), compartPanel,
								"Compartment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
								options, options[0]);
					}
					else {
						String addComp = "";
						addComp = compID.getText().trim();
						if (!(IDpat.matcher(addComp).matches())) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"A compartment ID can only contain letters, numbers, and underscores.",
									"Invalid ID", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else if (usedIDs.contains(addComp)) {
							if (option.equals("OK")
									&& !addComp.equals(((String) compartments.getSelectedValue()).split(" ")[0])) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter A Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (option.equals("Add")) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "Enter A Unique ID",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						String selCompType = (String) compTypeBox.getSelectedItem();
						if (!compSize.getText().trim().equals("")) {
							try {
								Double addCompSize = Double.parseDouble(compSize.getText().trim());
							}
							catch (Exception e1) {
								error = true;
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a real number into the size field.", "Enter a Valid Size",
										JOptionPane.ERROR_MESSAGE);
							}
						}
						if (!error) {
							if (option.equals("OK")) {
								int index = compartments.getSelectedIndex();
								String val = ((String) compartments.getSelectedValue()).split(" ")[0];
								compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								comps = Buttons.getList(comps, compartments);
								compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								Compartment c = document.getModel().getCompartment(val);
								c.setId(compID.getText().trim());
								c.setName(compName.getText().trim());
								if (!selCompType.equals("( none )")) {
									c.setCompartmentType(selCompType);
								}
								else {
									c.unsetCompartmentType();
								}
								c.setSpatialDimensions(Integer.parseInt((String) dimBox.getSelectedItem()));
								if (compSize.getText().trim().equals("")) {
									c.unsetSize();
								}
								else {
									c.setSize(Double.parseDouble(compSize.getText().trim()));
								}
								if (compUnits.getSelectedItem().equals("( none )")) {
									c.unsetUnits();
								}
								else {
									c.setUnits((String) compUnits.getSelectedItem());
								}
								if (compOutside.getSelectedItem().equals("( none )")) {
									c.unsetOutside();
								}
								else {
									c.setOutside((String) compOutside.getSelectedItem());
								}
								if (compConstant.getSelectedItem().equals("true")) {
									c.setConstant(true);
								}
								else {
									c.setConstant(false);
								}
								for (int i = 0; i < usedIDs.size(); i++) {
									if (usedIDs.get(i).equals(val)) {
										usedIDs.set(i, addComp);
									}
								}
								if (!selCompType.equals("( none )")) {
									comps[index] = addComp + " " + selCompType + " " + compSize.getText().trim();
								}
								else {
									comps[index] = addComp + " " + compSize.getText().trim();
								}
								if (!compUnits.getSelectedItem().equals("( none )")) {
									comps[index] += " " + compUnits.getSelectedItem();
								}
								sort(comps);
								compartments.setListData(comps);
								compartments.setSelectedIndex(index);
								for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
									Species species = document.getModel().getSpecies(i);
									if (species.getCompartment().equals(val)) {
										species.setCompartment(addComp);
									}
								}
								int index1 = species.getSelectedIndex();
								species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								specs = Buttons.getList(specs, species);
								species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								for (int i = 0; i < specs.length; i++) {
									if (specs[i].split(" ")[1].equals(val)) {
										specs[i] = specs[i].split(" ")[0] + " " + addComp + " "
												+ specs[i].split(" ")[2];
									}
									else if (specs[i].split(" ")[2].equals(val)) {
										specs[i] = specs[i].split(" ")[0] + " " + specs[i].split(" ")[1] + " "
												+ addComp + " " + specs[i].split(" ")[3];
									}
								}
								sort(specs);
								species.setListData(specs);
								species.setSelectedIndex(index1);
							}
							else {
								int index = compartments.getSelectedIndex();
								Compartment c = document.getModel().createCompartment();
								c.setId(compID.getText().trim());
								c.setName(compName.getText().trim());
								if (!selCompType.equals("( none )")) {
									c.setCompartmentType(selCompType);
								}
								else {
									c.unsetCompartmentType();
								}
								c.setSpatialDimensions(Integer.parseInt((String) dimBox.getSelectedItem()));
								if (compSize.getText().trim().equals("")) {
									c.unsetSize();
								}
								else {
									c.setSize(Double.parseDouble(compSize.getText().trim()));
								}
								if (compUnits.getSelectedItem().equals("( none )")) {
									c.unsetUnits();
								}
								else {
									c.setUnits((String) compUnits.getSelectedItem());
								}
								if (compOutside.getSelectedItem().equals("( none )")) {
									c.unsetOutside();
								}
								else {
									c.setOutside((String) compOutside.getSelectedItem());
								}
								if (compConstant.getSelectedItem().equals("true")) {
									c.setConstant(true);
								}
								else {
									c.setConstant(false);
								}
								usedIDs.add(addComp);
								JList add = new JList();
								String addStr;
								if (!selCompType.equals("( none )")) {
									addStr = addComp + " " + selCompType + " " + compSize.getText().trim();
								}
								else {
									addStr = addComp + " " + compSize.getText().trim();
								}
								if (!compUnits.getSelectedItem().equals("( none )")) {
									addStr += " " + compUnits.getSelectedItem();
								}
								Object[] adding = { addStr };
								add.setListData(adding);
								add.setSelectedIndex(0);
								compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(comps, compartments, add, false, null, null, null, null, null,
										null, biosim.frame());
								comps = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									comps[i] = (String) adding[i];
								}
								sort(comps);
								compartments.setListData(comps);
								compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (document.getModel().getNumCompartments() == 1) {
									compartments.setSelectedIndex(0);
								}
								else {
									compartments.setSelectedIndex(index);
								}
							}
							change = true;
						}
						if (error) {
							value = JOptionPane.showOptionDialog(biosim.frame(), compartPanel,
									"Compartment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
									options, options[0]);
						}
					}
				}
				if (value == JOptionPane.NO_OPTION) {
					return;
				}
			}
		}
	}

	/**
	 * Set compartment options based on number of dimensions
	 */
	private void setCompartOptions(String selected, String dim) {
		if (dim.equals("3")) {
			compSize.setEnabled(true);
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1)
						&& (unit.getUnit(0).isLitre() && unit.getUnit(0).getExponent() == 1)
						|| (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 3)) {
					compUnits.addItem(unit.getId());
				}
			}
			String[] unitIds = { "volume", "litre", "dimensionless" };
			for (int i = 0; i < unitIds.length; i++) {
				compUnits.addItem(unitIds[i]);
			}
			compOutside.removeAllItems();
			compOutside.addItem("( none )");
			ListOf listOfComps = document.getModel().getListOfCompartments();
			for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
				Compartment compartment = (Compartment) listOfComps.get(i);
				if (!compartment.getId().equals(selected) && compartment.getSpatialDimensions() != 0) {
					compOutside.addItem(compartment.getId());
				}
			}
			compConstant.setEnabled(true);
		}
		else if (dim.equals("2")) {
			compSize.setEnabled(true);
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1)
						&& (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 2)) {
					compUnits.addItem(unit.getId());
				}
			}
			String[] unitIds = { "area", "dimensionless" };
			for (int i = 0; i < unitIds.length; i++) {
				compUnits.addItem(unitIds[i]);
			}
			compOutside.removeAllItems();
			compOutside.addItem("( none )");
			ListOf listOfComps = document.getModel().getListOfCompartments();
			for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
				Compartment compartment = (Compartment) listOfComps.get(i);
				if (!compartment.getId().equals(selected) && compartment.getSpatialDimensions() != 0) {
					compOutside.addItem(compartment.getId());
				}
			}
			compConstant.setEnabled(true);
		}
		else if (dim.equals("1")) {
			compSize.setEnabled(true);
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1)
						&& (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 1)) {
					compUnits.addItem(unit.getId());
				}
			}
			String[] unitIds = { "length", "metre", "dimensionless" };
			for (int i = 0; i < unitIds.length; i++) {
				compUnits.addItem(unitIds[i]);
			}
			compOutside.removeAllItems();
			compOutside.addItem("( none )");
			ListOf listOfComps = document.getModel().getListOfCompartments();
			for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
				Compartment compartment = (Compartment) listOfComps.get(i);
				if (!compartment.getId().equals(selected) && compartment.getSpatialDimensions() != 0) {
					compOutside.addItem(compartment.getId());
				}
			}
			compConstant.setEnabled(true);
		}
		else if (dim.equals("0")) {
			compSize.setText("");
			compSize.setEnabled(false);
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			compOutside.removeAllItems();
			compOutside.addItem("( none )");
			ListOf listOfComps = document.getModel().getListOfCompartments();
			for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
				Compartment compartment = (Compartment) listOfComps.get(i);
				if (!compartment.getId().equals(selected)) {
					compOutside.addItem(compartment.getId());
				}
			}
			compConstant.setEnabled(false);
		}
		if (!selected.equals("")) {
			Compartment compartment = document.getModel().getCompartment(selected);
			if (compartment.isSetOutside()) {
				compOutside.setSelectedItem(compartment.getOutside());
			}
			if (compartment.isSetUnits()) {
				compUnits.setSelectedItem(compartment.getUnits());
			}
		}
	}

	/**
	 * Creates a frame used to edit species or create new ones.
	 */
	private void speciesEditor(String option) {
		if (option.equals("OK") && species.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No species selected.",
					"Must Select A Species", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel speciesPanel;
			if (paramsOnly) {
				speciesPanel = new JPanel(new GridLayout(13, 2));
			}
			else {
				speciesPanel = new JPanel(new GridLayout(8, 2));
			}
			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			JLabel specTypeLabel = new JLabel("Type:");
			JLabel compLabel = new JLabel("Compartment:");
			// JLabel initLabel;
			// if (amount) {
			// initLabel = new JLabel("Initial Amount:");
			// }
			// else {
			// initLabel = new JLabel("Initial Concentration:");
			// }
			String[] initLabels = { "Initial Amount", "Initial Concentration" };
			initLabel = new JComboBox(initLabels);
			JLabel unitLabel = new JLabel("Units:");
			JLabel boundLabel = new JLabel("Boundary Condition:");
			JLabel constLabel = new JLabel("Constant:");
			ID = new JTextField();
			Name = new JTextField();
			init = new JTextField("0.0");
			specUnits = new JComboBox();
			specUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1)
						&& (unit.getUnit(0).isMole() || unit.getUnit(0).isItem() || unit.getUnit(0).isGram() || unit
								.getUnit(0).isKilogram()) && (unit.getUnit(0).getExponent() == 1)) {
					specUnits.addItem(unit.getId());
				}
			}
			String[] unitIds = { "substance", "dimensionless", "gram", "item", "kilogram", "mole" };
			for (int i = 0; i < unitIds.length; i++) {
				specUnits.addItem(unitIds[i]);
			}
			String[] optionsTF = { "true", "false" };
			specBoundary = new JComboBox(optionsTF);
			specBoundary.setSelectedItem("false");
			specConstant = new JComboBox(optionsTF);
			specConstant.setSelectedItem("false");
			JLabel startLabel = new JLabel("Start:");
			JLabel stopLabel = new JLabel("Stop:");
			JLabel stepLabel = new JLabel("Step:");
			JLabel levelLabel = new JLabel("Level:");
			ListOf listOfSpecTypes = document.getModel().getListOfSpeciesTypes();
			String[] specTypeList = new String[(int) document.getModel().getNumSpeciesTypes() + 1];
			specTypeList[0] = "( none )";
			for (int i = 0; i < document.getModel().getNumSpeciesTypes(); i++) {
				specTypeList[i + 1] = ((SpeciesType) listOfSpecTypes.get(i)).getId();
			}
			sort(specTypeList);
			Object[] choices = specTypeList;
			specTypeBox = new JComboBox(choices);
			// int[] index = compartments.getSelectedIndices();
			// compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			// String[] add = Buttons.getList(comps, compartments);
			// compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// compartments.setSelectedIndices(index);
			ListOf listOfCompartments = document.getModel().getListOfCompartments();
			String[] add = new String[(int) document.getModel().getNumCompartments()];
			for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
				add[i] = ((Compartment) listOfCompartments.get(i)).getId();
			}
			try {
				add[0].getBytes();
			}
			catch (Exception e) {
				add = new String[1];
				add[0] = "default";
			}
			comp = new JComboBox(add);
			String[] list = { "Original", "Custom", "Sweep" };
			String[] list1 = { "1", "2" };
			final JComboBox type = new JComboBox(list);
			final JTextField start = new JTextField();
			final JTextField stop = new JTextField();
			final JTextField step = new JTextField();
			final JComboBox level = new JComboBox(list1);
			if (paramsOnly) {
				ID.setEditable(false);
				Name.setEditable(false);
				specTypeBox.setEnabled(false);
				specBoundary.setEnabled(false);
				specConstant.setEnabled(false);
				comp.setEnabled(false);
				init.setEnabled(false);
				initLabel.setEnabled(false);
				specUnits.setEnabled(false);
				start.setEnabled(false);
				stop.setEnabled(false);
				step.setEnabled(false);
				level.setEnabled(false);
			}
			if (option.equals("OK")) {
				try {
					Species specie = document.getModel().getSpecies(
							((String) species.getSelectedValue()).split(" ")[0]);
					ID.setText(specie.getId());
					Name.setText(specie.getName());
					specTypeBox.setSelectedItem(specie.getSpeciesType());
					if (specie.getBoundaryCondition()) {
						specBoundary.setSelectedItem("true");
					}
					else {
						specBoundary.setSelectedItem("false");
					}
					if (specie.getConstant()) {
						specConstant.setSelectedItem("true");
					}
					else {
						specConstant.setSelectedItem("false");
					}
					if (specie.isSetInitialAmount()) {
						init.setText("" + specie.getInitialAmount());
						initLabel.setSelectedItem("Initial Amount");
					}
					else {
						init.setText("" + specie.getInitialConcentration());
						initLabel.setSelectedItem("Initial Concentration");
					}
					specUnits.setSelectedItem(specie.getUnits());
					comp.setSelectedItem(specie.getCompartment());
					if (paramsOnly
							&& (((String) species.getSelectedValue()).contains("Custom") || ((String) species
									.getSelectedValue()).contains("Sweep"))) {
						if (((String) species.getSelectedValue()).contains("Custom")) {
							type.setSelectedItem("Custom");
						}
						else {
							type.setSelectedItem("Sweep");
						}
						if (((String) type.getSelectedItem()).equals("Sweep")) {
							start.setText((((String) species.getSelectedValue()).split(" ")[2]).split(",")[0]
									.substring(1).trim());
							stop.setText((((String) species.getSelectedValue()).split(" ")[2]).split(",")[1]
									.trim());
							step.setText((((String) species.getSelectedValue()).split(" ")[2]).split(",")[2]
									.trim());
							level.setSelectedItem((((String) species.getSelectedValue()).split(" ")[2])
									.split(",")[3].replace(")", "").trim());
							start.setEnabled(true);
							stop.setEnabled(true);
							step.setEnabled(true);
							level.setEnabled(true);
							init.setEnabled(false);
							initLabel.setEnabled(false);
							specUnits.setEnabled(false);
						}
						else {
							start.setEnabled(false);
							stop.setEnabled(false);
							step.setEnabled(false);
							level.setEnabled(false);
							init.setEnabled(true);
							initLabel.setEnabled(false);
							specUnits.setEnabled(false);
						}
					}
				}
				catch (Exception e) {
				}
			}
			speciesPanel.add(idLabel);
			speciesPanel.add(ID);
			speciesPanel.add(nameLabel);
			speciesPanel.add(Name);
			speciesPanel.add(specTypeLabel);
			speciesPanel.add(specTypeBox);
			speciesPanel.add(compLabel);
			speciesPanel.add(comp);
			speciesPanel.add(boundLabel);
			speciesPanel.add(specBoundary);
			speciesPanel.add(constLabel);
			speciesPanel.add(specConstant);
			if (paramsOnly) {
				JLabel typeLabel = new JLabel("Initial Type:");
				type.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (!((String) type.getSelectedItem()).equals("Original")) {
							if (((String) type.getSelectedItem()).equals("Sweep")) {
								start.setEnabled(true);
								stop.setEnabled(true);
								step.setEnabled(true);
								level.setEnabled(true);
								init.setEnabled(false);
								initLabel.setEnabled(false);
								specUnits.setEnabled(false);
							}
							else {
								start.setEnabled(false);
								stop.setEnabled(false);
								step.setEnabled(false);
								level.setEnabled(false);
								init.setEnabled(true);
								initLabel.setEnabled(false);
								specUnits.setEnabled(false);
							}
						}
						else {
							start.setEnabled(false);
							stop.setEnabled(false);
							step.setEnabled(false);
							level.setEnabled(false);
							init.setEnabled(false);
							initLabel.setEnabled(false);
							specUnits.setEnabled(false);
							SBMLReader reader = new SBMLReader();
							SBMLDocument d = reader.readSBML(file);
							if (d.getModel().getSpecies(((String) species.getSelectedValue()).split(" ")[0])
									.isSetInitialAmount()) {
								init.setText(d.getModel().getSpecies(
										((String) species.getSelectedValue()).split(" ")[0]).getInitialAmount()
										+ "");
								initLabel.setSelectedItem("Initial Amount");
							}
							else {
								init.setText(d.getModel().getSpecies(
										((String) species.getSelectedValue()).split(" ")[0]).getInitialConcentration()
										+ "");
								initLabel.setSelectedItem("Initial Concentration");
							}
							if (d.getModel().getSpecies(((String) species.getSelectedValue()).split(" ")[0])
									.isSetUnits()) {
								specUnits.setSelectedItem(d.getModel().getSpecies(
										((String) species.getSelectedValue()).split(" ")[0]).getUnits());
							}
						}
					}
				});
				speciesPanel.add(typeLabel);
				speciesPanel.add(type);
			}
			speciesPanel.add(initLabel);
			speciesPanel.add(init);
			speciesPanel.add(unitLabel);
			speciesPanel.add(specUnits);
			if (paramsOnly) {
				speciesPanel.add(startLabel);
				speciesPanel.add(start);
				speciesPanel.add(stopLabel);
				speciesPanel.add(stop);
				speciesPanel.add(stepLabel);
				speciesPanel.add(step);
				speciesPanel.add(levelLabel);
				speciesPanel.add(level);
			}
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), speciesPanel, "Species Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (ID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an id into the id field.",
										"Enter An ID", JOptionPane.ERROR_MESSAGE);
						error = true;
						value = JOptionPane.showOptionDialog(biosim.frame(), speciesPanel, "Species Editor",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}
					else {
						try {
							double initial = Double.parseDouble(init.getText().trim());
							String unit = (String) specUnits.getSelectedItem();
							String addSpec;
							String selSpecType = (String) specTypeBox.getSelectedItem();
							if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
								if (((String) type.getSelectedItem()).equals("Sweep")) {
									double startVal = Double.parseDouble(start.getText().trim());
									double stopVal = Double.parseDouble(stop.getText().trim());
									double stepVal = Double.parseDouble(step.getText().trim());
									if (!selSpecType.equals("( none )")) {
										addSpec = ID.getText().trim() + " " + selSpecType + " "
												+ comp.getSelectedItem() + " (" + startVal + "," + stopVal + "," + stepVal
												+ "," + level.getSelectedItem() + ") " + type.getSelectedItem();
									}
									else {
										addSpec = ID.getText().trim() + " " + comp.getSelectedItem() + " (" + startVal
												+ "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ") "
												+ type.getSelectedItem();
									}
								}
								else {
									if (!selSpecType.equals("( none )")) {
										if (!unit.equals("( none )")) {
											addSpec = ID.getText().trim() + " " + selSpecType + " "
													+ comp.getSelectedItem() + " " + initial + " " + unit + " "
													+ type.getSelectedItem();
										}
										else {
											addSpec = ID.getText().trim() + " " + selSpecType + " "
													+ comp.getSelectedItem() + " " + initial + " " + type.getSelectedItem();
										}
									}
									else {
										if (!unit.equals("( none )")) {
											addSpec = ID.getText().trim() + " " + comp.getSelectedItem() + " " + initial
													+ " " + unit + " " + type.getSelectedItem();
										}
										else {
											addSpec = ID.getText().trim() + " " + comp.getSelectedItem() + " " + initial
													+ " " + type.getSelectedItem();
										}
									}
								}
							}
							else {
								if (!selSpecType.equals("( none )")) {
									if (!unit.equals("( none )")) {
										addSpec = ID.getText().trim() + " " + selSpecType + " "
												+ comp.getSelectedItem() + " " + initial + " " + unit;
									}
									else {
										addSpec = ID.getText().trim() + " " + selSpecType + " "
												+ comp.getSelectedItem() + " " + initial;
									}
								}
								else {
									if (!unit.equals("( none )")) {
										addSpec = ID.getText().trim() + " " + comp.getSelectedItem() + " " + initial
												+ " " + unit;
									}
									else {
										addSpec = ID.getText().trim() + " " + comp.getSelectedItem() + " " + initial;
									}
								}
							}
							if (!((IDpat.matcher(ID.getText().trim())).matches())) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"A species ID can only contain letters, numbers, and underscores.",
										"Invalid ID", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (usedIDs.contains(ID.getText().trim())) {
								if (option.equals("OK")
										&& !ID.getText().trim().equals(
												((String) species.getSelectedValue()).split(" ")[0])) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique id into the id field.", "Enter A Unique ID",
											JOptionPane.ERROR_MESSAGE);
									error = true;
								}
								else if (option.equals("Add")) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique id into the id field.", "Enter A Unique ID",
											JOptionPane.ERROR_MESSAGE);
									error = true;
								}
							}
							if (!error) {
								if (option.equals("OK")) {
									int index1 = species.getSelectedIndex();
									String val = ((String) species.getSelectedValue()).split(" ")[0];
									species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									specs = Buttons.getList(specs, species);
									species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									Species specie = document.getModel().getSpecies(val);
									String speciesName = specie.getId();
									specie.setCompartment((String) comp.getSelectedItem());
									specie.setId(ID.getText().trim());
									specie.setName(Name.getText().trim());
									if (specBoundary.getSelectedItem().equals("true")) {
										specie.setBoundaryCondition(true);
									}
									else {
										specie.setBoundaryCondition(false);
									}
									if (specConstant.getSelectedItem().equals("true")) {
										specie.setConstant(true);
									}
									else {
										specie.setConstant(false);
									}
									for (int i = 0; i < usedIDs.size(); i++) {
										if (usedIDs.get(i).equals(val)) {
											usedIDs.set(i, ID.getText().trim());
										}
									}
									if (!selSpecType.equals("( none )")) {
										specie.setSpeciesType(selSpecType);
									}
									else {
										specie.unsetSpeciesType();
									}
									Model model = document.getModel();
									for (int i = 0; i < model.getNumReactions(); i++) {
										Reaction reaction = (Reaction) document.getModel().getListOfReactions().get(i);
										for (int j = 0; j < reaction.getNumProducts(); j++) {
											if (reaction.getProduct(j).isSetSpecies()) {
												SpeciesReference specRef = reaction.getProduct(j);
												if (speciesName.equals(specRef.getSpecies())) {
													specRef.setSpecies(specie.getId());
												}
											}
										}
										for (int j = 0; j < reaction.getNumModifiers(); j++) {
											if (reaction.getModifier(j).isSetSpecies()) {
												ModifierSpeciesReference specRef = reaction.getModifier(j);
												if (speciesName.equals(specRef.getSpecies())) {
													specRef.setSpecies(specie.getId());
												}
											}
										}
										for (int j = 0; j < reaction.getNumReactants(); j++) {
											if (reaction.getReactant(j).isSetSpecies()) {
												SpeciesReference specRef = reaction.getReactant(j);
												if (speciesName.equals(specRef.getSpecies())) {
													specRef.setSpecies(specie.getId());
												}
											}
										}
										String s = " " + reaction.getKineticLaw().getFormula() + " ";
										s = s.replace(" " + speciesName + " ", " " + specie.getId() + " ");
										s = s.replace("(" + speciesName + ")", "(" + specie.getId() + ")");
										s = s.replace("(" + speciesName + " ", "(" + specie.getId() + " ");
										s = s.replace("(" + speciesName + ",", "(" + specie.getId() + ",");
										s = s.replace(" " + speciesName + ")", " " + specie.getId() + ")");
										reaction.getKineticLaw().setFormula(s.trim());
									}
									if (initLabel.getSelectedItem().equals("Initial Amount")) {
										specie.setInitialAmount(initial);
										specie.unsetInitialConcentration();
									}
									else {
										specie.unsetInitialAmount();
										specie.setInitialConcentration(initial);
									}
									if (unit.equals("( none )")) {
										specie.unsetUnits();
									}
									else {
										specie.setUnits(unit);
									}
									specs[index1] = addSpec;
									sort(specs);
									species.setListData(specs);
									species.setSelectedIndex(index1);
									if (paramsOnly) {
										int remove = -1;
										for (int i = 0; i < parameterChanges.size(); i++) {
											if (parameterChanges.get(i).split(" ")[0].equals(ID.getText().trim())) {
												remove = i;
											}
										}
										if (remove != -1) {
											parameterChanges.remove(remove);
										}
										if (!((String) type.getSelectedItem()).equals("Original")) {
											parameterChanges.add(addSpec);
										}
									}
								}
								else {
									int index1 = species.getSelectedIndex();
									Species specie = document.getModel().createSpecies();
									specie.setCompartment((String) comp.getSelectedItem());
									specie.setId(ID.getText().trim());
									specie.setName(Name.getText().trim());
									if (specBoundary.getSelectedItem().equals("true")) {
										specie.setBoundaryCondition(true);
									}
									else {
										specie.setBoundaryCondition(false);
									}
									if (specConstant.getSelectedItem().equals("true")) {
										specie.setConstant(true);
									}
									else {
										specie.setConstant(false);
									}
									usedIDs.add(ID.getText().trim());
									if (!selSpecType.equals("( none )")) {
										specie.setSpeciesType(selSpecType);
									}
									else {
										specie.unsetSpeciesType();
									}
									if (initLabel.getSelectedItem().equals("Initial Amount")) {
										specie.setInitialAmount(initial);
										specie.unsetInitialConcentration();
									}
									else {
										specie.unsetInitialAmount();
										specie.setInitialConcentration(initial);
									}
									if (unit.equals("( none )")) {
										specie.unsetUnits();
									}
									else {
										specie.setUnits(unit);
									}
									JList addIt = new JList();
									Object[] adding = { addSpec };
									addIt.setListData(adding);
									addIt.setSelectedIndex(0);
									species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									adding = Buttons.add(specs, species, addIt, false, null, null, null, null, null,
											null, biosim.frame());
									specs = new String[adding.length];
									for (int i = 0; i < adding.length; i++) {
										specs[i] = (String) adding[i];
									}
									sort(specs);
									species.setListData(specs);
									species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									if (document.getModel().getNumSpecies() == 1) {
										species.setSelectedIndex(0);
									}
									else {
										species.setSelectedIndex(index1);
									}
								}
								change = true;
							}
						}
						catch (Exception e1) {
							error = true;
							JOptionPane.showMessageDialog(biosim.frame(),
									"You must enter a real number into the initial value field.",
									"Enter a Valid Initial Value", JOptionPane.ERROR_MESSAGE);
						}
						if (error) {
							value = JOptionPane.showOptionDialog(biosim.frame(), speciesPanel, "Species Editor",
									JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
						}
					}
				}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
	}

	/**
	 * Creates a frame used to edit reactions or create new ones.
	 */
	private void reactionsEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && reactions.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No reaction selected.",
					"Must Select A Reaction", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel reactionPanelNorth = new JPanel();
			JPanel reactionPanelNorth1 = new JPanel();
			JPanel reactionPanelNorth1b = new JPanel();
			JPanel reactionPanelNorth2 = new JPanel();
			JPanel reactionPanelCentral = new JPanel(new GridLayout(1, 3));
			JPanel reactionPanelSouth = new JPanel(new GridLayout(1, 2));
			JPanel reactionPanel = new JPanel(new BorderLayout());
			JLabel id = new JLabel("ID:");
			reacID = new JTextField(15);
			JLabel name = new JLabel("Name:");
			reacName = new JTextField(50);
			JLabel reverse = new JLabel("Reversible:");
			String[] options = { "true", "false" };
			reacReverse = new JComboBox(options);
			reacReverse.setSelectedItem("false");
			JLabel fast = new JLabel("Fast:");
			reacFast = new JComboBox(options);
			reacFast.setSelectedItem("false");
			if (option.equals("OK")) {
				Reaction reac = document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]);
				reacID.setText(reac.getId());
				reacName.setText(reac.getName());
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
			}
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
			scroll.setMinimumSize(new Dimension(260, 220));
			scroll.setPreferredSize(new Dimension(276, 152));
			scroll.setViewportView(reacParameters);
			reacParams = new String[0];
			changedParameters = new ArrayList<Parameter>();
			thisReactionParams = new ArrayList<String>();
			if (option.equals("OK")) {
				Reaction reac = document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]);
				ListOf listOfParameters = reac.getKineticLaw().getListOfParameters();
				reacParams = new String[(int) reac.getKineticLaw().getNumParameters()];
				for (int i = 0; i < reac.getKineticLaw().getNumParameters(); i++) {
					Parameter parameter = (Parameter) listOfParameters.get(i);
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
							if (parameterChanges.get(j).split(" ")[0].equals(((String) reactions
									.getSelectedValue()).split(" ")[0]
									+ "/" + parameter.getId())) {
								p = parameterChanges.get(j).split("/")[1];
							}
						}
					}
					reacParams[i] = p;
				}
			}
			else {
				Parameter p = new Parameter();
				p.setId("kf");
				p.setValue(0.1);
				changedParameters.add(p);
				p = new Parameter();
				p.setId("kr");
				p.setValue(0.1);
				changedParameters.add(p);
				reacParams = new String[2];
				reacParams[0] = "kf 0.1";
				reacParams[1] = "kr 0.1";
				thisReactionParams.add("kf");
				thisReactionParams.add("kr");
			}
			sort(reacParams);
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
			scroll2.setMinimumSize(new Dimension(260, 220));
			scroll2.setPreferredSize(new Dimension(276, 152));
			scroll2.setViewportView(reactants);
			reacta = new String[0];
			changedReactants = new ArrayList<SpeciesReference>();
			kineticL = "";
			if (option.equals("OK")) {
				Reaction reac = document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]);
				ListOf listOfReactants = reac.getListOfReactants();
				reacta = new String[(int) reac.getNumReactants()];
				for (int i = 0; i < reac.getNumReactants(); i++) {
					SpeciesReference reactant = (SpeciesReference) listOfReactants.get(i);
					changedReactants.add(reactant);
					if (reactant.isSetStoichiometryMath()) {
					  reacta[i] = reactant.getSpecies() + " " + sbmlLib.formulaToString(reactant.getStoichiometryMath().getMath());
					} else {
					  reacta[i] = reactant.getSpecies() + " " + reactant.getStoichiometry();
					}
				}
				kineticL = document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]).getKineticLaw().getFormula();
			}
			sort(reacta);
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
			scroll3.setMinimumSize(new Dimension(260, 220));
			scroll3.setPreferredSize(new Dimension(276, 152));
			scroll3.setViewportView(products);
			product = new String[0];
			changedProducts = new ArrayList<SpeciesReference>();
			if (option.equals("OK")) {
				Reaction reac = document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]);
				ListOf listOfProducts = reac.getListOfProducts();
				product = new String[(int) reac.getNumProducts()];
				for (int i = 0; i < reac.getNumProducts(); i++) {
					SpeciesReference product = (SpeciesReference) listOfProducts.get(i);
					changedProducts.add(product);
					if (product.isSetStoichiometryMath()) {
					  this.product[i] = product.getSpecies() + " " + sbmlLib.formulaToString(product.getStoichiometryMath().getMath());
					} else {
					  this.product[i] = product.getSpecies() + " " + product.getStoichiometry();
					}
				}
			}
			sort(product);
			products.setListData(product);
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
			scroll5.setMinimumSize(new Dimension(260, 220));
			scroll5.setPreferredSize(new Dimension(276, 152));
			scroll5.setViewportView(modifiers);
			modifier = new String[0];
			changedModifiers = new ArrayList<ModifierSpeciesReference>();
			if (option.equals("OK")) {
				Reaction reac = document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]);
				ListOf listOfModifiers = reac.getListOfModifiers();
				modifier = new String[(int) reac.getNumModifiers()];
				for (int i = 0; i < reac.getNumModifiers(); i++) {
					ModifierSpeciesReference modifier = (ModifierSpeciesReference) listOfModifiers.get(i);
					changedModifiers.add(modifier);
					this.modifier[i] = modifier.getSpecies();
				}
			}
			sort(modifier);
			modifiers.setListData(modifier);
			modifiers.setSelectedIndex(0);
			modifiers.addMouseListener(this);
			modifierPanel.add(modifiersLabel, "North");
			modifierPanel.add(scroll5, "Center");
			modifierPanel.add(addModifiers, "South");

			JLabel kineticLabel = new JLabel("Kinetic Law:");
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
			scroll4.setMinimumSize(new Dimension(100, 100));
			scroll4.setPreferredSize(new Dimension(100, 100));
			scroll4.setViewportView(kineticLaw);
			if (option.equals("OK")) {
				kineticLaw.setText(document.getModel().getReaction(
						((String) reactions.getSelectedValue()).split(" ")[0]).getKineticLaw().getFormula());
			}
			JPanel kineticPanel = new JPanel(new BorderLayout());
			kineticPanel.add(kineticLabel, "North");
			kineticPanel.add(scroll4, "Center");
			kineticPanel.add(kineticButtons, "South");
			reactionPanelNorth1.add(id);
			reactionPanelNorth1.add(reacID);
			reactionPanelNorth1.add(name);
			reactionPanelNorth1.add(reacName);
			reactionPanelNorth1b.add(reverse);
			reactionPanelNorth1b.add(reacReverse);
			reactionPanelNorth1b.add(fast);
			reactionPanelNorth1b.add(reacFast);
			reactionPanelNorth2.add(reactionPanelNorth1);
			reactionPanelNorth2.add(reactionPanelNorth1b);
			reactionPanelNorth.add(reactionPanelNorth2);
			reactionPanelCentral.add(reactantsPanel);
			reactionPanelCentral.add(productsPanel);
			reactionPanelCentral.add(modifierPanel);
			reactionPanelSouth.add(param);
			reactionPanelSouth.add(kineticPanel);
			reactionPanel.add(reactionPanelNorth, "North");
			reactionPanel.add(reactionPanelCentral, "Center");
			reactionPanel.add(reactionPanelSouth, "South");
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
			}
			Object[] options1 = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), reactionPanel, "Reaction Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (reacID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an ID into the ID field.",
										"Enter An ID", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						String reac;
						reac = reacID.getText().trim();
						if (!((IDpat.matcher(reac).matches()))) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"A reaction ID can only contain letters, numbers, and underscores.",
									"Invalid ID", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else if (usedIDs.contains(reacID.getText().trim())) {
							if (option.equals("OK")
									&& !reacID.getText().trim().equals(
											((String) reactions.getSelectedValue()).split(" ")[0])) {
								JOptionPane.showMessageDialog(biosim.frame(), "A reaction ID must be unique.",
										"ID Not Unique", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (option.equals("Add")) {
								JOptionPane.showMessageDialog(biosim.frame(), "A reaction ID must be unique.",
										"ID Not Unique", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						if (!error) {
							if (kineticLaw.getText().trim().equals("")) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"A reaction must have a kinetic law.", "Enter A Kinetic Law",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if ((changedReactants.size() == 0) && (changedProducts.size() == 0)) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"A reaction must have at least one reactant or product.",
										"No Reactants or Products", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						String kineticCheck;
						if (!error) {
							if (option.equals("OK")) {
								int index = reactions.getSelectedIndex();
								String val = ((String) reactions.getSelectedValue()).split(" ")[0];
								kineticCheck = reacID.getText().trim();
								reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacts = Buttons.getList(reacts, reactions);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								Reaction react = document.getModel().getReaction(val);
								ListOf remove;
								long size;
								try {
									remove = react.getKineticLaw().getListOfParameters();
									size = react.getKineticLaw().getNumParameters();
									for (int i = 0; i < size; i++) {
										remove.remove(0);
									}
								}
								catch (Exception e1) {
								}
								for (int i = 0; i < changedParameters.size(); i++) {
									react.getKineticLaw().addParameter(changedParameters.get(i));
								}
								remove = react.getListOfProducts();
								size = react.getNumProducts();
								for (int i = 0; i < size; i++) {
									remove.remove(0);
								}
								for (int i = 0; i < changedProducts.size(); i++) {
									react.addProduct(changedProducts.get(i));
								}
								remove = react.getListOfModifiers();
								size = react.getNumModifiers();
								for (int i = 0; i < size; i++) {
									remove.remove(0);
								}
								for (int i = 0; i < changedModifiers.size(); i++) {
									react.addModifier(changedModifiers.get(i));
								}
								remove = react.getListOfReactants();
								size = react.getNumReactants();
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
								if (reacFast.getSelectedItem().equals("true")) {
									react.setFast(true);
								}
								else {
									react.setFast(false);
								}
								react.setId(reacID.getText().trim());
								react.setName(reacName.getText().trim());
								for (int i = 0; i < usedIDs.size(); i++) {
									if (usedIDs.get(i).equals(val)) {
										usedIDs.set(i, reacID.getText().trim());
									}
								}
								react.getKineticLaw().setFormula(kineticLaw.getText().trim());
								if (!paramsOnly) {
									reacts[index] = reac;
								}
								sort(reacts);
								reactions.setListData(reacts);
								reactions.setSelectedIndex(index);
							}
							else {
								Reaction react = document.getModel().createReaction();
								react.setKineticLaw(new KineticLaw());
								int index = reactions.getSelectedIndex();
								kineticCheck = reacID.getText().trim();
								for (int i = 0; i < changedParameters.size(); i++) {
									react.getKineticLaw().addParameter(changedParameters.get(i));
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
								react.setId(reacID.getText().trim());
								react.setName(reacName.getText().trim());
								usedIDs.add(reacID.getText().trim());
								react.getKineticLaw().setFormula(kineticLaw.getText().trim());
								JList add = new JList();
								Object[] adding = { reac };
								add.setListData(adding);
								add.setSelectedIndex(0);
								reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(reacts, reactions, add, false, null, null, null, null, null,
										null, biosim.frame());
								reacts = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									reacts[i] = (String) adding[i];
								}
								sort(reacts);
								reactions.setListData(reacts);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (document.getModel().getNumReactions() == 1) {
									reactions.setSelectedIndex(0);
								}
								else {
									reactions.setSelectedIndex(index);
								}
							}
							change = true;
							if (!kineticLaw.getText().trim().equals("")) {
								ArrayList<String> validKineticVars = new ArrayList<String>();
								ArrayList<String> invalidKineticVars = new ArrayList<String>();
								Reaction r;
								r = document.getModel().getReaction(kineticCheck);
								SBMLDocument docu = new SBMLDocument();
								Model m = docu.createModel();
								m.addReaction(r);
								SBMLWriter writer = new SBMLWriter();
								String doc = writer.writeToString(docu);
								String documentWritten = writer.writeToString(document);
								SBMLReader reader = new SBMLReader();
								docu = reader.readSBMLFromString(doc);
								document = reader.readSBMLFromString(documentWritten);
								ListOf sbml = r.getKineticLaw().getListOfParameters();
								for (int i = 0; i < r.getKineticLaw().getNumParameters(); i++) {
									validKineticVars.add(((Parameter) sbml.get(i)).getId());
								}
								sbml = r.getListOfReactants();
								for (int i = 0; i < r.getNumReactants(); i++) {
									validKineticVars.add(((SpeciesReference) sbml.get(i)).getSpecies());
								}
								sbml = r.getListOfProducts();
								for (int i = 0; i < r.getNumProducts(); i++) {
									validKineticVars.add(((SpeciesReference) sbml.get(i)).getSpecies());
								}
								sbml = r.getListOfModifiers();
								for (int i = 0; i < r.getNumModifiers(); i++) {
									validKineticVars.add(((ModifierSpeciesReference) sbml.get(i)).getSpecies());
								}
								sbml = document.getModel().getListOfFunctionDefinitions();
								for (int i = 0; i < document.getModel().getNumFunctionDefinitions(); i++) {
									validKineticVars.add(((FunctionDefinition) sbml.get(i)).getId());
								}
								sbml = document.getModel().getListOfCompartments();
								for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
									validKineticVars.add(((Compartment) sbml.get(i)).getId());
								}
								sbml = document.getModel().getListOfParameters();
								for (int i = 0; i < document.getModel().getNumParameters(); i++) {
									validKineticVars.add(((Parameter) sbml.get(i)).getId());
								}
								if (!docu.getModel().getReaction(0).getKineticLaw().isSetFormula()) {
									document.getModel().getReaction(kineticCheck).getKineticLaw()
											.setFormula(kineticL);
									if (option.equals("OK")) {
										JOptionPane.showMessageDialog(biosim.frame(), "Unable to parse kinetic law.",
												"Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
									}
									else {
										JOptionPane.showMessageDialog(biosim.frame(), "Unable to parse kinetic law."
												+ "\nAll others parts of the reaction have been saved.",
												"Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
									}
								}
								else {
									String[] splitLaw = docu.getModel().getReaction(0).getKineticLaw().getFormula()
											.split(" |\\(|\\)|\\,");
									boolean pass = true;
									for (int i = 0; i < splitLaw.length; i++) {
										if (splitLaw[i].equals("+") || splitLaw[i].equals("-")
												|| splitLaw[i].equals("*") || splitLaw[i].equals("/")
												|| splitLaw[i].equals("abs") || splitLaw[i].equals("acos")
												|| splitLaw[i].equals("asin") || splitLaw[i].equals("atan")
												|| splitLaw[i].equals("ceil") || splitLaw[i].equals("cos")
												|| splitLaw[i].equals("exp") || splitLaw[i].equals("floor")
												|| splitLaw[i].equals("log") || splitLaw[i].equals("sqr")
												|| splitLaw[i].equals("pow") || splitLaw[i].equals("sqrt")
												|| splitLaw[i].equals("root") || splitLaw[i].equals("sin")
												|| splitLaw[i].equals("tan") || splitLaw[i].equals("")
												|| splitLaw[i].equals("INF")) {
										}
										else {
											try {
												Double.parseDouble(splitLaw[i]);
											}
											catch (Exception e1) {
												invalidKineticVars.add(splitLaw[i]);
												for (int j = 0; j < validKineticVars.size(); j++) {
													if (splitLaw[i].equals(validKineticVars.get(j))) {
														pass = true;
														invalidKineticVars.remove(splitLaw[i]);
														break;
													}
													pass = false;
												}
											}
										}
									}
									if (!pass || validKineticVars.size() == 0 || invalidKineticVars.size() != 0) {
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
										if (!option.equals("OK")) {
											message = "Kinetic law contains unknown variables.\n\n"
													+ "Unkown variables:\n" + invalid;
										}
										else {
											message = "Kinetic law contains unknown variables.\n"
													+ "However, the reaction with this kinetic law has been saved.\n\n"
													+ "Unkown variables:\n" + invalid;
										}
										JTextArea messageArea = new JTextArea(message);
										messageArea.setLineWrap(true);
										messageArea.setWrapStyleWord(true);
										messageArea.setEditable(false);
										JScrollPane scrolls = new JScrollPane();
										scrolls.setMinimumSize(new Dimension(300, 300));
										scrolls.setPreferredSize(new Dimension(300, 300));
										scrolls.setViewportView(messageArea);
										JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Kinetic Law Error",
												JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), reactionPanel, "Reaction Editor",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
					}
				}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
	}

	/**
	 * Creates a frame used to edit parameters or create new ones.
	 */
	private void parametersEditor(String option) {
		if (option.equals("OK") && parameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No parameter selected.",
					"Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel parametersPanel;
			if (paramsOnly) {
				parametersPanel = new JPanel(new GridLayout(10, 2));
			}
			else {
				parametersPanel = new JPanel(new GridLayout(5, 2));
			}
			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			JLabel valueLabel = new JLabel("Value:");
			JLabel unitLabel = new JLabel("Units:");
			JLabel constLabel = new JLabel("Constant:");
			JLabel startLabel = new JLabel("Start:");
			JLabel stopLabel = new JLabel("Stop:");
			JLabel stepLabel = new JLabel("Step:");
			JLabel levelLabel = new JLabel("Level:");
			paramID = new JTextField();
			paramName = new JTextField();
			paramValue = new JTextField();
			paramUnits = new JComboBox();
			paramUnits.addItem("( none )");
			for (int i = 0; i < units.length; i++) {
				paramUnits.addItem(units[i]);
			}
			String[] unitIds = { "substance", "volume", "area", "length", "time", "ampere", "becquerel",
					"candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray", "henry",
					"hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux",
					"metre", "mole", "newton", "ohm", "pascal", "radian", "second", "siemens", "sievert",
					"steradian", "tesla", "volt", "watt", "weber" };
			for (int i = 0; i < unitIds.length; i++) {
				paramUnits.addItem(unitIds[i]);
			}
			paramConst = new JComboBox();
			paramConst.addItem("true");
			paramConst.addItem("false");
			String[] list = { "Original", "Custom", "Sweep" };
			String[] list1 = { "1", "2" };
			final JComboBox type = new JComboBox(list);
			final JTextField start = new JTextField();
			final JTextField stop = new JTextField();
			final JTextField step = new JTextField();
			final JComboBox level = new JComboBox(list1);
			if (paramsOnly) {
				paramID.setEditable(false);
				paramName.setEditable(false);
				paramValue.setEnabled(false);
				paramUnits.setEnabled(false);
				paramConst.setEnabled(false);
				start.setEnabled(false);
				stop.setEnabled(false);
				step.setEnabled(false);
				level.setEnabled(false);
			}
			if (option.equals("OK")) {
				try {
					Parameter paramet = document.getModel().getParameter(
							((String) parameters.getSelectedValue()).split(" ")[0]);
					paramID.setText(paramet.getId());
					paramName.setText(paramet.getName());
					if (paramet.getConstant()) {
						paramConst.setSelectedItem("true");
					}
					else {
						paramConst.setSelectedItem("false");
					}
					if (paramet.isSetValue()) {
						paramValue.setText("" + paramet.getValue());
					}
					if (paramet.isSetUnits()) {
						paramUnits.setSelectedItem(paramet.getUnits());
					}
					if (paramsOnly && (((String) parameters.getSelectedValue()).contains("Custom"))
							|| (((String) parameters.getSelectedValue()).contains("Sweep"))) {
						if (((String) parameters.getSelectedValue()).contains("Custom")) {
							type.setSelectedItem("Custom");
						}
						else {
							type.setSelectedItem("Sweep");
						}
						if (((String) type.getSelectedItem()).equals("Sweep")) {
							start.setText((((String) parameters.getSelectedValue()).split(" ")[1]).split(",")[0]
									.substring(1).trim());
							stop.setText((((String) parameters.getSelectedValue()).split(" ")[1]).split(",")[1]
									.trim());
							step.setText((((String) parameters.getSelectedValue()).split(" ")[1]).split(",")[2]
									.trim());
							level.setSelectedItem((((String) parameters.getSelectedValue()).split(" ")[1])
									.split(",")[3].replace(")", "").trim());
							start.setEnabled(true);
							stop.setEnabled(true);
							step.setEnabled(true);
							level.setEnabled(true);
							paramValue.setEnabled(false);
							paramUnits.setEnabled(false);
						}
						else {
							start.setEnabled(false);
							stop.setEnabled(false);
							step.setEnabled(false);
							level.setEnabled(false);
							paramValue.setEnabled(true);
							paramUnits.setEnabled(false);
						}
					}
				}
				catch (Exception e) {
				}
			}
			parametersPanel.add(idLabel);
			parametersPanel.add(paramID);
			parametersPanel.add(nameLabel);
			parametersPanel.add(paramName);
			if (paramsOnly) {
				JLabel typeLabel = new JLabel("Type:");
				type.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (!((String) type.getSelectedItem()).equals("Original")) {
							if (((String) type.getSelectedItem()).equals("Sweep")) {
								start.setEnabled(true);
								stop.setEnabled(true);
								step.setEnabled(true);
								level.setEnabled(true);
								paramValue.setEnabled(false);
								paramUnits.setEnabled(false);
							}
							else {
								start.setEnabled(false);
								stop.setEnabled(false);
								step.setEnabled(false);
								level.setEnabled(false);
								paramValue.setEnabled(true);
								paramUnits.setEnabled(false);
							}
						}
						else {
							start.setEnabled(false);
							stop.setEnabled(false);
							step.setEnabled(false);
							level.setEnabled(false);
							paramValue.setEnabled(false);
							paramUnits.setEnabled(false);
							SBMLReader reader = new SBMLReader();
							SBMLDocument d = reader.readSBML(file);
							if (d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0])
									.isSetValue()) {
								paramValue.setText(d.getModel().getParameter(
										((String) parameters.getSelectedValue()).split(" ")[0]).getValue()
										+ "");
							}
							else {
								paramValue.setText("");
							}
							if (d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0])
									.isSetUnits()) {
								paramUnits.setSelectedItem(d.getModel().getParameter(
										((String) parameters.getSelectedValue()).split(" ")[0]).getUnits()
										+ "");
							}
						}
					}
				});
				parametersPanel.add(typeLabel);
				parametersPanel.add(type);
			}
			parametersPanel.add(valueLabel);
			parametersPanel.add(paramValue);
			parametersPanel.add(unitLabel);
			parametersPanel.add(paramUnits);
			parametersPanel.add(constLabel);
			parametersPanel.add(paramConst);
			if (paramsOnly) {
				parametersPanel.add(startLabel);
				parametersPanel.add(start);
				parametersPanel.add(stopLabel);
				parametersPanel.add(stop);
				parametersPanel.add(stepLabel);
				parametersPanel.add(step);
				parametersPanel.add(levelLabel);
				parametersPanel.add(level);
			}
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel, "Parameter Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (paramID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an ID into the ID field.",
										"Enter An ID", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						try {
							double val = Double.parseDouble(paramValue.getText().trim());
							String unit = (String) paramUnits.getSelectedItem();
							String param;
							if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
								if (((String) type.getSelectedItem()).equals("Sweep")) {
									double startVal = Double.parseDouble(start.getText().trim());
									double stopVal = Double.parseDouble(stop.getText().trim());
									double stepVal = Double.parseDouble(step.getText().trim());
									param = paramID.getText().trim() + " (" + startVal + "," + stopVal + ","
											+ stepVal + "," + level.getSelectedItem() + ") " + type.getSelectedItem();
								}
								else {
									if (unit.equals("( none )")) {
										param = paramID.getText().trim() + " " + val + " " + type.getSelectedItem();
									}
									else {
										param = paramID.getText().trim() + " " + val + " " + unit + " "
												+ type.getSelectedItem();
									}
								}
							}
							else {
								if (unit.equals("( none )")) {
									param = paramID.getText().trim() + " " + val;
								}
								else {
									param = paramID.getText().trim() + " " + val + " " + unit;
								}
							}
							if (!((IDpat.matcher(paramID.getText().trim()).matches()))) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"A parameter ID can only contain letters, numbers, and underscores.",
										"Invalid ID", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (usedIDs.contains(paramID.getText().trim())) {
								if (option.equals("OK")
										&& !paramID.getText().trim().equals(
												((String) parameters.getSelectedValue()).split(" ")[0])) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique ID into the ID field.", "ID Not Unique",
											JOptionPane.ERROR_MESSAGE);
									error = true;
								}
								else if (option.equals("Add")) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique ID into the ID field.", "ID Not Unique",
											JOptionPane.ERROR_MESSAGE);
									error = true;
								}
							}
							if (!error) {
								if (option.equals("OK")) {
									int index = parameters.getSelectedIndex();
									String v = ((String) parameters.getSelectedValue()).split(" ")[0];
									Parameter paramet = document.getModel().getParameter(v);
									parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									params = Buttons.getList(params, parameters);
									parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									paramet.setId(paramID.getText().trim());
									paramet.setName(paramName.getText().trim());
									if (paramConst.getSelectedItem().equals("true")) {
										paramet.setConstant(true);
									}
									else {
										paramet.setConstant(false);
									}
									for (int i = 0; i < usedIDs.size(); i++) {
										if (usedIDs.get(i).equals(v)) {
											usedIDs.set(i, paramID.getText().trim());
										}
									}
									paramet.setValue(val);
									if (unit.equals("( none )")) {
										paramet.unsetUnits();
									}
									else {
										paramet.setUnits(unit);
									}
									params[index] = param;
									sort(params);
									parameters.setListData(params);
									parameters.setSelectedIndex(index);
									if (paramsOnly) {
										int remove = -1;
										for (int i = 0; i < parameterChanges.size(); i++) {
											if (parameterChanges.get(i).split(" ")[0].equals(paramID.getText().trim())) {
												remove = i;
											}
										}
										if (remove != -1) {
											parameterChanges.remove(remove);
										}
										if (!((String) type.getSelectedItem()).equals("Original")) {
											parameterChanges.add(param);
										}
									}
								}
								else {
									int index = parameters.getSelectedIndex();
									Parameter paramet = document.getModel().createParameter();
									paramet.setId(paramID.getText().trim());
									paramet.setName(paramName.getText().trim());
									usedIDs.add(paramID.getText().trim());
									if (paramConst.getSelectedItem().equals("true")) {
										paramet.setConstant(true);
									}
									else {
										paramet.setConstant(false);
									}
									paramet.setValue(val);
									if (unit.equals("( none )")) {
										paramet.unsetUnits();
									}
									else {
										paramet.setUnits(unit);
									}
									JList add = new JList();
									Object[] adding = { param };
									add.setListData(adding);
									add.setSelectedIndex(0);
									parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									adding = Buttons.add(params, parameters, add, false, null, null, null, null,
											null, null, biosim.frame());
									params = new String[adding.length];
									for (int i = 0; i < adding.length; i++) {
										params[i] = (String) adding[i];
									}
									sort(params);
									parameters.setListData(params);
									parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									if (document.getModel().getNumParameters() == 1) {
										parameters.setSelectedIndex(0);
									}
									else {
										parameters.setSelectedIndex(index);
									}
								}
								change = true;
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"You must enter a real number into the value" + " field.", "Enter A Valid Value",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel,
								"Parameter Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
								options, options[0]);
					}
				}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
	}

	/**
	 * Creates a frame used to edit reactions parameters or create new ones.
	 */
	private void reacParametersEditor(String option) {
		if (option.equals("OK") && reacParameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No parameter selected.",
					"Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel parametersPanel;
			if (paramsOnly) {
				parametersPanel = new JPanel(new GridLayout(9, 2));
			}
			else {
				parametersPanel = new JPanel(new GridLayout(4, 2));
			}
			JLabel idLabel = new JLabel("ID:");
			JLabel nameLabel = new JLabel("Name:");
			JLabel valueLabel = new JLabel("Value:");
			JLabel unitsLabel = new JLabel("Units:");
			JLabel startLabel = new JLabel("Start:");
			JLabel stopLabel = new JLabel("Stop:");
			JLabel stepLabel = new JLabel("Step:");
			JLabel levelLabel = new JLabel("Level:");
			reacParamID = new JTextField();
			reacParamName = new JTextField();
			reacParamValue = new JTextField();
			reacParamUnits = new JComboBox();
			reacParamUnits.addItem("( none )");
			for (int i = 0; i < units.length; i++) {
				reacParamUnits.addItem(units[i]);
			}
			String[] unitIds = { "substance", "volume", "area", "length", "time", "ampere", "becquerel",
					"candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray", "henry",
					"hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux",
					"metre", "mole", "newton", "ohm", "pascal", "radian", "second", "siemens", "sievert",
					"steradian", "tesla", "volt", "watt", "weber" };
			for (int i = 0; i < unitIds.length; i++) {
				reacParamUnits.addItem(unitIds[i]);
			}
			String[] list = { "Original", "Custom", "Sweep" };
			String[] list1 = { "1", "2" };
			final JComboBox type = new JComboBox(list);
			final JTextField start = new JTextField();
			final JTextField stop = new JTextField();
			final JTextField step = new JTextField();
			final JComboBox level = new JComboBox(list1);
			if (paramsOnly) {
				reacParamID.setEditable(false);
				reacParamName.setEditable(false);
				reacParamValue.setEnabled(false);
				reacParamUnits.setEnabled(false);
				start.setEnabled(false);
				stop.setEnabled(false);
				step.setEnabled(false);
				level.setEnabled(false);
			}
			if (option.equals("OK")) {
				String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
				Parameter paramet = null;
				for (Parameter p : changedParameters) {
					if (p.getId().equals(v)) {
						paramet = p;
					}
				}
				reacParamID.setText(paramet.getId());
				reacParamName.setText(paramet.getName());
				reacParamValue.setText("" + paramet.getValue());
				if (paramet.isSetUnits()) {
					reacParamUnits.setSelectedItem(paramet.getUnits());
				}
				if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
					if (((String) type.getSelectedItem()).equals("Sweep")) {
						type.setSelectedItem("Custom");
					}
					else {
						type.setSelectedItem("Sweep");
					}
					if (((String) type.getSelectedItem()).equals("Sweep")) {
						start
								.setText((((String) reacParameters.getSelectedValue()).split(" ")[1]).split(",")[0]
										.substring(1).trim());
						stop.setText((((String) reacParameters.getSelectedValue()).split(" ")[1]).split(",")[1]
								.trim());
						step.setText((((String) reacParameters.getSelectedValue()).split(" ")[1]).split(",")[2]
								.trim());
						level.setSelectedItem((((String) reacParameters.getSelectedValue()).split(" ")[1])
								.split(",")[3].replace(")", "").trim());
						start.setEnabled(true);
						stop.setEnabled(true);
						step.setEnabled(true);
						level.setEnabled(true);
						reacParamValue.setEnabled(false);
						reacParamUnits.setEnabled(false);
					}
					else {
						start.setEnabled(false);
						stop.setEnabled(false);
						step.setEnabled(false);
						level.setEnabled(false);
						reacParamValue.setEnabled(true);
						reacParamUnits.setEnabled(false);
					}
				}
			}
			parametersPanel.add(idLabel);
			parametersPanel.add(reacParamID);
			parametersPanel.add(nameLabel);
			parametersPanel.add(reacParamName);
			if (paramsOnly) {
				JLabel typeLabel = new JLabel("Type:");
				type.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (!((String) type.getSelectedItem()).equals("Original")) {
							if (((String) type.getSelectedItem()).equals("Sweep")) {
								start.setEnabled(true);
								stop.setEnabled(true);
								step.setEnabled(true);
								level.setEnabled(true);
								reacParamValue.setEnabled(false);
								reacParamUnits.setEnabled(false);
							}
							else {
								start.setEnabled(false);
								stop.setEnabled(false);
								step.setEnabled(false);
								level.setEnabled(false);
								reacParamValue.setEnabled(true);
								reacParamUnits.setEnabled(false);
							}
						}
						else {
							start.setEnabled(false);
							stop.setEnabled(false);
							step.setEnabled(false);
							level.setEnabled(false);
							reacParamValue.setEnabled(false);
							reacParamUnits.setEnabled(false);
							SBMLReader reader = new SBMLReader();
							SBMLDocument d = reader.readSBML(file);
							KineticLaw KL = d.getModel().getReaction(
									((String) reactions.getSelectedValue()).split(" ")[0]).getKineticLaw();
							ListOf list = KL.getListOfParameters();
							int number = -1;
							for (int i = 0; i < KL.getNumParameters(); i++) {
								if (((Parameter) list.get(i)).getId().equals(
										((String) reacParameters.getSelectedValue()).split(" ")[0])) {
									number = i;
								}
							}
							reacParamValue.setText(d.getModel().getReaction(
									((String) reactions.getSelectedValue()).split(" ")[0]).getKineticLaw()
									.getParameter(number).getValue()
									+ "");
							if (d.getModel().getReaction(((String) reactions.getSelectedValue()).split(" ")[0])
									.getKineticLaw().getParameter(number).isSetUnits()) {
								reacParamUnits.setSelectedItem(d.getModel().getReaction(
										((String) reactions.getSelectedValue()).split(" ")[0]).getKineticLaw()
										.getParameter(number).getUnits());
							}
							reacParamValue.setText(d.getModel().getReaction(
									((String) reactions.getSelectedValue()).split(" ")[0]).getKineticLaw()
									.getParameter(number).getValue()
									+ "");
						}
					}
				});
				parametersPanel.add(typeLabel);
				parametersPanel.add(type);
			}
			parametersPanel.add(valueLabel);
			parametersPanel.add(reacParamValue);
			parametersPanel.add(unitsLabel);
			parametersPanel.add(reacParamUnits);
			if (paramsOnly) {
				parametersPanel.add(startLabel);
				parametersPanel.add(start);
				parametersPanel.add(stopLabel);
				parametersPanel.add(stop);
				parametersPanel.add(stepLabel);
				parametersPanel.add(step);
				parametersPanel.add(levelLabel);
				parametersPanel.add(level);
			}
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel, "Parameter Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (reacParamID.getText().trim().equals("")) {
						JOptionPane
								.showMessageDialog(biosim.frame(), "You must enter an ID into the ID field.",
										"Enter An ID", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						try {
							double val = Double.parseDouble(reacParamValue.getText().trim());
							String unit = (String) reacParamUnits.getSelectedItem();
							String param;
							if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
								if (((String) type.getSelectedItem()).equals("Sweep")) {
									double startVal = Double.parseDouble(start.getText().trim());
									double stopVal = Double.parseDouble(stop.getText().trim());
									double stepVal = Double.parseDouble(step.getText().trim());
									param = reacParamID.getText().trim() + " (" + startVal + "," + stopVal + ","
											+ stepVal + "," + level.getSelectedItem() + ") " + type.getSelectedItem();
								}
								else {
									if (unit.equals("( none )")) {
										param = reacParamID.getText().trim() + " " + val + " " + type.getSelectedItem();
									}
									else {
										param = reacParamID.getText().trim() + " " + val + " " + unit + " "
												+ type.getSelectedItem();
									}
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
							if (!((IDpat.matcher(reacParamID.getText().trim()).matches()))) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"A parameter ID can only contain letters, numbers, and underscores.",
										"Invalid ID", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							else if (thisReactionParams.contains(reacParamID.getText().trim())) {
								if (option.equals("OK")
										&& !reacParamID.getText().trim().equals(
												((String) reacParameters.getSelectedValue()).split(" ")[0])) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique ID into the ID field.", "ID Not Unique",
											JOptionPane.ERROR_MESSAGE);
									error = true;
								}
								else if (option.equals("Add")) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique ID into the ID field.", "ID Not Unique",
											JOptionPane.ERROR_MESSAGE);
									error = true;
								}
							}
							else if (usedIDs.contains(reacParamID.getText().trim())) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique ID into the ID field.", "ID Not Unique",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							if (!error) {
								if (option.equals("OK")) {
									int index = reacParameters.getSelectedIndex();
									String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
									Parameter paramet = null;
									for (Parameter p : changedParameters) {
										if (p.getId().equals(v)) {
											paramet = p;
										}
									}
									reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									reacParams = Buttons.getList(reacParams, reacParameters);
									reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									paramet.setId(reacParamID.getText().trim());
									paramet.setName(reacParamName.getText().trim());
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
									reacParams[index] = param;
									sort(reacParams);
									reacParameters.setListData(reacParams);
									reacParameters.setSelectedIndex(index);
									if (paramsOnly) {
										int remove = -1;
										for (int i = 0; i < parameterChanges.size(); i++) {
											if (parameterChanges.get(i).split(" ")[0].equals(((String) reactions
													.getSelectedValue()).split(" ")[0]
													+ "/" + reacParamID.getText().trim())) {
												remove = i;
											}
										}
										if (remove != -1) {
											parameterChanges.remove(remove);
											int index1 = reactions.getSelectedIndex();
											reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
											reacts = Buttons.getList(reacts, reactions);
											reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
											reacts[index1] = ((String) reactions.getSelectedValue()).split(" ")[0];
											sort(reacts);
											reactions.setListData(reacts);
											reactions.setSelectedIndex(index1);
										}
										if (!((String) type.getSelectedItem()).equals("Original")) {
											parameterChanges.add(((String) reactions.getSelectedValue()).split(" ")[0]
													+ "/" + param);
											int index1 = reactions.getSelectedIndex();
											reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
											reacts = Buttons.getList(reacts, reactions);
											reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
											if (((String) type.getSelectedItem()).equals("Custom")) {
												reacts[index1] = ((String) reactions.getSelectedValue()).split(" ")[0]
														+ " Custom";
											}
											else {
												reacts[index1] = ((String) reactions.getSelectedValue()).split(" ")[0]
														+ " Sweep";
											}
											sort(reacts);
											reactions.setListData(reacts);
											reactions.setSelectedIndex(index1);
										}
									}
								}
								else {
									int index = reacParameters.getSelectedIndex();
									Parameter paramet = new Parameter();
									changedParameters.add(paramet);
									paramet.setId(reacParamID.getText().trim());
									paramet.setName(reacParamName.getText().trim());
									thisReactionParams.add(reacParamID.getText().trim());
									paramet.setValue(val);
									if (unit.equals("( none )")) {
										paramet.unsetUnits();
									}
									else {
										paramet.setUnits(unit);
									}
									JList add = new JList();
									Object[] adding = { param };
									add.setListData(adding);
									add.setSelectedIndex(0);
									reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									adding = Buttons.add(reacParams, reacParameters, add, false, null, null, null,
											null, null, null, biosim.frame());
									reacParams = new String[adding.length];
									for (int i = 0; i < adding.length; i++) {
										reacParams[i] = (String) adding[i];
									}
									sort(reacParams);
									reacParameters.setListData(reacParams);
									reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									try {
										if (document.getModel().getReaction(
												((String) reactions.getSelectedValue()).split(" ")[0]).getKineticLaw()
												.getNumParameters() == 1) {
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
								change = true;
							}
						}
						catch (Exception e1) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"You must enter a real number into the value" + " field.", "Enter A Valid Value",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel,
								"Parameter Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
								options, options[0]);
					}
				}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
	}

	/**
	 * Creates a frame used to edit products or create new ones.
	 */
	public void productsEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && products.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No product selected.",
					"Must Select A Product", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel productsPanel = new JPanel(new GridLayout(2, 2));
			JLabel speciesLabel = new JLabel("Species:");
			Object [] stoiciOptions = { "Stoichiometry", "Stoichiometry Math" };
			stoiciLabel = new JComboBox(stoiciOptions);
			ListOf listOfSpecies = document.getModel().getListOfSpecies();
			String[] speciesList = new String[(int) document.getModel().getNumSpecies()];
			for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
				speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
			}
			sort(speciesList);
			Object[] choices = speciesList;
			productSpecies = new JComboBox(choices);
			productStoiciometry = new JTextField("1");
			if (option.equals("OK")) {
				String v = ((String) products.getSelectedValue()).split(" ")[0];
				SpeciesReference product = null;
				for (SpeciesReference p : changedProducts) {
					if (p.getSpecies().equals(v)) {
						product = p;
					}
				}
				productSpecies.setSelectedItem(product.getSpecies());
				if (product.isSetStoichiometryMath()) {
				  stoiciLabel.setSelectedItem("Stoichiometry Math");
				  productStoiciometry.setText("" + sbmlLib.formulaToString(product.getStoichiometryMath().getMath()));
				} else {
				  productStoiciometry.setText("" + product.getStoichiometry());
				}
			}
			productsPanel.add(speciesLabel);
			productsPanel.add(productSpecies);
			productsPanel.add(stoiciLabel);
			productsPanel.add(productStoiciometry);
			if (choices.length == 0) {
				JOptionPane
						.showMessageDialog(biosim.frame(), "There are no species availiable to be products."
								+ "\nAdd species to this sbml file first.", "No Species", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), productsPanel, "Products Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					try {
					        String prod;
						double val = 1.0;
					        if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
						  val = Double.parseDouble(productStoiciometry.getText().trim());
						  prod = productSpecies.getSelectedItem() + " " + val;
						} else {
						  prod = productSpecies.getSelectedItem() + " " + productStoiciometry.getText().trim();
						}
						if (option.equals("OK")) {
							int index = products.getSelectedIndex();
							products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							product = Buttons.getList(product, products);
							products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							products.setSelectedIndex(index);
							for (int i = 0; i < product.length; i++) {
								if (i != index) {
									if (product[i].split(" ")[0].equals(productSpecies.getSelectedItem())) {
										error = true;
										JOptionPane.showMessageDialog(biosim.frame(),
												"Unable to add species as a product.\n"
														+ "Each species can only be used as a product once.",
												"Species Can Only Be Used Once", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
							if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
							  if (productStoiciometry.getText().trim().equals("")) {
							    JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry math must have formula.",
											  "Enter Stoichiometry Formula", JOptionPane.ERROR_MESSAGE);
							    error = true;
							  }
							  else if (sbmlLib.parseFormula(productStoiciometry.getText().trim()) == null) {
							    JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry formula is not valid.",
											  "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							    error = true;
							  }
							}
							if (!error) {
								String v = ((String) products.getSelectedValue()).split(" ")[0];
								SpeciesReference produ = null;
								for (SpeciesReference p : changedProducts) {
									if (p.getSpecies().equals(v)) {
										produ = p;
									}
								}
								products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								product = Buttons.getList(product, products);
								products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								produ.setSpecies((String) productSpecies.getSelectedItem());
								if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
								  produ.setStoichiometry(val);
								  produ.unsetStoichiometryMath();
								} else {
								  StoichiometryMath sm = new StoichiometryMath(sbmlLib.parseFormula(productStoiciometry.getText().trim()));
								  produ.setStoichiometryMath(sm);
								}
								product[index] = prod;
								sort(product);
								products.setListData(product);
								products.setSelectedIndex(index);
							}
						}
						else {
							int index = products.getSelectedIndex();
							products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							product = Buttons.getList(product, products);
							products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							products.setSelectedIndex(index);
							for (int i = 0; i < product.length; i++) {
								if (product[i].split(" ")[0].equals(productSpecies.getSelectedItem())) {
									error = true;
									JOptionPane.showMessageDialog(biosim.frame(),
											"Unable to add species as a product.\n"
													+ "Each species can only be used as a product once.",
											"Species Can Only Be Used Once", JOptionPane.ERROR_MESSAGE);
								}
							}
							if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
							  if (productStoiciometry.getText().trim().equals("")) {
							    JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry math must have formula.",
											  "Enter Stoichiometry Formula", JOptionPane.ERROR_MESSAGE);
							    error = true;
							  }
							  else if (sbmlLib.parseFormula(productStoiciometry.getText().trim()) == null) {
							    JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry formula is not valid.",
											  "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							    error = true;
							  }
							}
							if (!error) {
								SpeciesReference produ = new SpeciesReference();
								changedProducts.add(produ);
								produ.setSpecies((String) productSpecies.getSelectedItem());
								if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
								  produ.setStoichiometry(val);
								  produ.unsetStoichiometryMath();
								} else {
								  StoichiometryMath sm = new StoichiometryMath(sbmlLib.parseFormula(productStoiciometry.getText().trim()));
								  produ.setStoichiometryMath(sm);
								}
								JList add = new JList();
								Object[] adding = { prod };
								add.setListData(adding);
								add.setSelectedIndex(0);
								products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(product, products, add, false, null, null, null, null, null,
										null, biosim.frame());
								product = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									product[i] = (String) adding[i];
								}
								sort(product);
								products.setListData(product);
								products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								try {
									if (document.getModel().getReaction(
											((String) reactions.getSelectedValue()).split(" ")[0]).getNumProducts() == 1) {
										products.setSelectedIndex(0);
									}
									else {
										products.setSelectedIndex(index);
									}
								}
								catch (Exception e2) {
									products.setSelectedIndex(0);
								}
							}
						}
						change = true;
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter a real number into the stoiciometry" + " field.",
								"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), productsPanel, "Products Editor",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}
				}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
	}

	/**
	 * Creates a frame used to edit modifiers or create new ones.
	 */
	public void modifiersEditor(String option) {
		if (option.equals("OK") && modifiers.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No modifier selected.",
					"Must Select A Modifier", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel modifiersPanel = new JPanel(new GridLayout(1, 2));
			JLabel speciesLabel = new JLabel("Species:");
			ListOf listOfSpecies = document.getModel().getListOfSpecies();
			String[] speciesList = new String[(int) document.getModel().getNumSpecies()];
			for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
				speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
			}
			sort(speciesList);
			Object[] choices = speciesList;
			modifierSpecies = new JComboBox(choices);
			if (option.equals("OK")) {
				String v = ((String) modifiers.getSelectedValue()).split(" ")[0];
				ModifierSpeciesReference modifier = null;
				for (ModifierSpeciesReference p : changedModifiers) {
					if (p.getSpecies().equals(v)) {
						modifier = p;
					}
				}
				modifierSpecies.setSelectedItem(modifier.getSpecies());
			}
			modifiersPanel.add(speciesLabel);
			modifiersPanel.add(modifierSpecies);
			if (choices.length == 0) {
				JOptionPane
						.showMessageDialog(biosim.frame(), "There are no species availiable to be modifiers."
								+ "\nAdd species to this sbml file first.", "No Species", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), modifiersPanel, "Modifiers Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					String mod = (String)modifierSpecies.getSelectedItem();
					if (option.equals("OK")) {
					  int index = modifiers.getSelectedIndex();
					  modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					  modifier = Buttons.getList(modifier, modifiers);
					  modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					  modifiers.setSelectedIndex(index);
					  for (int i = 0; i < modifier.length; i++) {
					    if (i != index) {
					      if (modifier[i].equals(modifierSpecies.getSelectedItem())) {
						error = true;
						JOptionPane.showMessageDialog(biosim.frame(),
									      "Unable to add species as a modifier.\n"
									      + "Each species can only be used as a modifier once.",
									      "Species Can Only Be Used Once", JOptionPane.ERROR_MESSAGE);
					      }
					    }
					  }
					  if (!error) {
					    String v = ((String) modifiers.getSelectedValue());
					    ModifierSpeciesReference modi = null;
					    for (ModifierSpeciesReference p : changedModifiers) {
					      if (p.getSpecies().equals(v)) {
						modi = p;
					      }
					    }
					    modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					    modifier = Buttons.getList(modifier, modifiers);
					    modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					    modi.setSpecies((String) modifierSpecies.getSelectedItem());
					    modifier[index] = mod;
					    sort(modifier);
					    modifiers.setListData(modifier);
					    modifiers.setSelectedIndex(index);
					  }
					}
					else {
					  int index = modifiers.getSelectedIndex();
					  modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					  modifier = Buttons.getList(modifier, modifiers);
					  modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					  modifiers.setSelectedIndex(index);
					  for (int i = 0; i < modifier.length; i++) {
					    if (modifier[i].equals(modifierSpecies.getSelectedItem())) {
					      error = true;
					      JOptionPane.showMessageDialog(biosim.frame(),
									    "Unable to add species as a modifier.\n"
									    + "Each species can only be used as a modifier once.",
									    "Species Can Only Be Used Once", JOptionPane.ERROR_MESSAGE);
					    }
					  }
					  if (!error) {
					    ModifierSpeciesReference modi = new ModifierSpeciesReference();
					    changedModifiers.add(modi);
					    modi.setSpecies((String) modifierSpecies.getSelectedItem());
					    JList add = new JList();
					    Object[] adding = { mod };
					    add.setListData(adding);
					    add.setSelectedIndex(0);
					    modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					    adding = Buttons.add(modifier, modifiers, add, false, null, null, null, null, null,
								 null, biosim.frame());
					    modifier = new String[adding.length];
					    for (int i = 0; i < adding.length; i++) {
					      modifier[i] = (String) adding[i];
					    }
					    sort(modifier);
					    modifiers.setListData(modifier);
					    modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					    try {
					      if (document.getModel().getReaction(
										  ((String) reactions.getSelectedValue()).split(" ")[0]).getNumModifiers() == 1) {
						modifiers.setSelectedIndex(0);
					      }
					      else {
						modifiers.setSelectedIndex(index);
					      }
					    }
					    catch (Exception e2) {
					      modifiers.setSelectedIndex(0);
					    }
					  }
					}
					change = true;
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), modifiersPanel, "Modifiers Editor",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}
				}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
	}

	/**
	 * Creates a frame used to edit reactants or create new ones.
	 */
	public void reactantsEditor(String option) {
		libsbml sbmlLib = new libsbml();
		if (option.equals("OK") && reactants.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No reactant selected.",
					"Must Select A Reactant", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JPanel reactantsPanel = new JPanel(new GridLayout(2, 2));
			JLabel speciesLabel = new JLabel("Species:");
			Object [] stoiciOptions = { "Stoichiometry", "Stoichiometry Math" };
			stoiciLabel = new JComboBox(stoiciOptions);
			ListOf listOfSpecies = document.getModel().getListOfSpecies();
			String[] speciesList = new String[(int) document.getModel().getNumSpecies()];
			for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
				speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
			}
			sort(speciesList);
			Object[] choices = speciesList;
			reactantSpecies = new JComboBox(choices);
			reactantStoiciometry = new JTextField("1");
			if (option.equals("OK")) {
				String v = ((String) reactants.getSelectedValue()).split(" ")[0];
				SpeciesReference reactant = null;
				for (SpeciesReference r : changedReactants) {
					if (r.getSpecies().equals(v)) {
						reactant = r;
					}
				}
				reactantSpecies.setSelectedItem(reactant.getSpecies());
				if (reactant.isSetStoichiometryMath()) {
				  stoiciLabel.setSelectedItem("Stoichiometry Math");
				  reactantStoiciometry.setText("" + sbmlLib.formulaToString(reactant.getStoichiometryMath().getMath()));
				} else {
				  reactantStoiciometry.setText("" + reactant.getStoichiometry());
				}
			}
			reactantsPanel.add(speciesLabel);
			reactantsPanel.add(reactantSpecies);
			reactantsPanel.add(stoiciLabel);
			reactantsPanel.add(reactantStoiciometry);
			if (choices.length == 0) {
				JOptionPane
						.showMessageDialog(biosim.frame(), "There are no species availiable to be reactants."
								+ "\nAdd species to this sbml file first.", "No Species", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), reactantsPanel, "Reactants Editor",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					try {
					        String react;
						double val = 1.0;
					        if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
						  val = Double.parseDouble(reactantStoiciometry.getText().trim());
						  react = reactantSpecies.getSelectedItem() + " " + val;
						} else {
						  react = reactantSpecies.getSelectedItem() + " " + reactantStoiciometry.getText().trim();
						}
						if (option.equals("OK")) {
							int index = reactants.getSelectedIndex();
							reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							reacta = Buttons.getList(reacta, reactants);
							reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							reactants.setSelectedIndex(index);
							for (int i = 0; i < reacta.length; i++) {
								if (i != index) {
									if (reacta[i].split(" ")[0].equals(reactantSpecies.getSelectedItem())) {
										error = true;
										JOptionPane.showMessageDialog(biosim.frame(),
												"Unable to add species as a reactant.\n"
														+ "Each species can only be used as a reactant once.",
												"Species Can Only Be Used Once", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
							if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
							  if (reactantStoiciometry.getText().trim().equals("")) {
							    JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry math must have formula.",
											  "Enter Stoichiometry Formula", JOptionPane.ERROR_MESSAGE);
							    error = true;
							  }
							  else if (sbmlLib.parseFormula(reactantStoiciometry.getText().trim()) == null) {
							    JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry formula is not valid.",
											  "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							    error = true;
							  }
							}
							if (!error) {
								String v = ((String) reactants.getSelectedValue()).split(" ")[0];
								SpeciesReference reactan = null;
								for (SpeciesReference r : changedReactants) {
									if (r.getSpecies().equals(v)) {
										reactan = r;
									}
								}
								reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacta = Buttons.getList(reacta, reactants);
								reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reactan.setSpecies((String) reactantSpecies.getSelectedItem());
								if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
								  reactan.setStoichiometry(val);
								  reactan.unsetStoichiometryMath();
								} else {
								  StoichiometryMath sm = new StoichiometryMath(sbmlLib.parseFormula(reactantStoiciometry.getText().trim()));
								  reactan.setStoichiometryMath(sm);
								}
								reacta[index] = react;
								sort(reacta);
								reactants.setListData(reacta);
								reactants.setSelectedIndex(index);
							}
						}
						else {
							int index = reactants.getSelectedIndex();
							reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							reacta = Buttons.getList(reacta, reactants);
							reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							reactants.setSelectedIndex(index);
							for (int i = 0; i < reacta.length; i++) {
								if (reacta[i].split(" ")[0].equals(reactantSpecies.getSelectedItem())) {
									error = true;
									JOptionPane.showMessageDialog(biosim.frame(),
											"Unable to add species as a reactant.\n"
													+ "Each species can only be used as a reactant once.",
											"Species Can Only Be Used Once", JOptionPane.ERROR_MESSAGE);
								}
							}
							if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
							  if (reactantStoiciometry.getText().trim().equals("")) {
							    JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry math must have formula.",
											  "Enter Stoichiometry Formula", JOptionPane.ERROR_MESSAGE);
							    error = true;
							  }
							  else if (sbmlLib.parseFormula(reactantStoiciometry.getText().trim()) == null) {
							    JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry formula is not valid.",
											  "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							    error = true;
							  }
							}
							if (!error) {
								SpeciesReference reactan = new SpeciesReference();
								changedReactants.add(reactan);
								reactan.setSpecies((String) reactantSpecies.getSelectedItem());
								if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
								  reactan.setStoichiometry(val);
								  reactan.unsetStoichiometryMath();
								} else {
								  StoichiometryMath sm = new StoichiometryMath(sbmlLib.parseFormula(reactantStoiciometry.getText().trim()));
								  reactan.setStoichiometryMath(sm);
								}
								JList add = new JList();
								Object[] adding = { react };
								add.setListData(adding);
								add.setSelectedIndex(0);
								reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(reacta, reactants, add, false, null, null, null, null, null,
										null, biosim.frame());
								reacta = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									reacta[i] = (String) adding[i];
								}
								sort(reacta);
								reactants.setListData(reacta);
								reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								try {
									if (document.getModel().getReaction(
											((String) reactions.getSelectedValue()).split(" ")[0]).getNumReactants() == 1) {
										reactants.setSelectedIndex(0);
									}
									else {
										reactants.setSelectedIndex(index);
									}
								}
								catch (Exception e2) {
									reactants.setSelectedIndex(0);
								}
							}
						}
						change = true;
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter a real number into the stoiciometry" + " field.",
								"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), reactantsPanel,
								"Reactants Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
								options, options[0]);
					}
				}
			}
			if (value == JOptionPane.NO_OPTION) {
				return;
			}
		}
	}

	/**
	 * Invoked when the mouse is double clicked in one of the JLists. Opens the
	 * editor for the selected item.
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == compartments) {
				if (!paramsOnly) {
					compartEditor("OK");
				}
			}
			else if (e.getSource() == functions) {
				functionEditor("OK");
			}
			else if (e.getSource() == unitDefs) {
				unitEditor("OK");
			}
			else if (e.getSource() == compTypes) {
				compTypeEditor("OK");
			}
			else if (e.getSource() == specTypes) {
				specTypeEditor("OK");
			}
			else if (e.getSource() == initAssigns) {
				initEditor("OK");
			}
			else if (e.getSource() == rules) {
				ruleEditor("OK");
			}
			else if (e.getSource() == events) {
				eventEditor("OK");
			}
			else if (e.getSource() == constraints) {
				constraintEditor("OK");
			}
			else if (e.getSource() == species) {
				speciesEditor("OK");
			}
			else if (e.getSource() == reactions) {
				reactionsEditor("OK");
			}
			else if (e.getSource() == parameters) {
				parametersEditor("OK");
			}
			else if (e.getSource() == reacParameters) {
				reacParametersEditor("OK");
			}
			else if (e.getSource() == reactants) {
				if (!paramsOnly) {
					reactantsEditor("OK");
				}
			}
			else if (e.getSource() == products) {
				if (!paramsOnly) {
					productsEditor("OK");
				}
			}
			else if (e.getSource() == modifiers) {
				if (!paramsOnly) {
					modifiersEditor("OK");
				}
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	public boolean hasChanged() {
		return change;
	}

	private void sort(String[] sort) {
		int i, j;
		String index;
		for (i = 1; i < sort.length; i++) {
			index = sort[i];
			j = i;
			while ((j > 0) && sort[j - 1].compareToIgnoreCase(index) > 0) {
				sort[j] = sort[j - 1];
				j = j - 1;
			}
			sort[j] = index;
		}
	}

	public void createSBML(String direct) {
		try {
			FileOutputStream out = new FileOutputStream(new File(paramFile));
			out.write((file + "\n").getBytes());
			for (String s : parameterChanges) {
				out.write((s + "\n").getBytes());
			}
			out.close();
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to save parameter file.",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		try {
			if (!direct.equals(".")) {
				String[] d = direct.split("_");
				ArrayList<String> dd = new ArrayList<String>();
				for (int i = 0; i < d.length; i++) {
					if (!d[i].contains("=")) {
						String di = d[i];
						while (!d[i].contains("=")) {
							i++;
							di += "_" + d[i];
						}
						dd.add(di);
					}
					else {
						dd.add(d[i]);
					}
				}
				for (String di : dd) {
					if (di.contains("/")) {
						KineticLaw KL = document.getModel().getReaction(di.split("=")[0].split("/")[0])
								.getKineticLaw();
						ListOf p = KL.getListOfParameters();
						for (int i = 0; i < KL.getNumParameters(); i++) {
							Parameter param = ((Parameter) p.get(i));
							if (param.getId().equals(di.split("=")[0].split("/")[1])) {
								param.setValue(Double.parseDouble(di.split("=")[1]));
							}
						}
					}
					else {
						if (document.getModel().getParameter(di.split("=")[0]) != null) {
							document.getModel().getParameter(di.split("=")[0]).setValue(
									Double.parseDouble(di.split("=")[1]));
						}
						else {
							if (document.getModel().getSpecies(di.split("=")[0]).isSetInitialAmount()) {
								document.getModel().getSpecies(di.split("=")[0]).setInitialAmount(
										Double.parseDouble(di.split("=")[1]));
							}
							else {
								document.getModel().getSpecies(di.split("=")[0]).setInitialConcentration(
										Double.parseDouble(di.split("=")[1]));
							}
						}
					}
				}
			}
			direct = direct.replace("/", "-");
			FileOutputStream out = new FileOutputStream(new File(simDir + separator + direct + separator
					+ file.split(separator)[file.split(separator).length - 1]));
			document.getModel().setName(modelName.getText().trim());
			SBMLWriter writer = new SBMLWriter();
			String doc = writer.writeToString(document);
			byte[] output = doc.getBytes();
			out.write(output);
			out.close();
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to create sbml file.",
					"Error Creating File", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves the sbml file.
	 */
	public void save(boolean run) {
		if (paramsOnly) {
			if (run) {
				ArrayList<String> sweepThese1 = new ArrayList<String>();
				ArrayList<ArrayList<Double>> sweep1 = new ArrayList<ArrayList<Double>>();
				ArrayList<String> sweepThese2 = new ArrayList<String>();
				ArrayList<ArrayList<Double>> sweep2 = new ArrayList<ArrayList<Double>>();
				for (String s : parameterChanges) {
					if (s.split(" ")[s.split(" ").length - 1].equals("Sweep")) {
						if ((s.split(" ")[s.split(" ").length - 2]).split(",")[3].replace(")", "").trim()
								.equals("1")) {
							sweepThese1.add(s.split(" ")[0]);
							double start = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 2]).split(",")[0].substring(1)
											.trim());
							double stop = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 2]).split(",")[1].trim());
							double step = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 2]).split(",")[2].trim());
							ArrayList<Double> add = new ArrayList<Double>();
							for (double i = start; i <= stop; i += step) {
								add.add(i);
							}
							sweep1.add(add);
						}
						else {
							sweepThese2.add(s.split(" ")[0]);
							double start = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 2]).split(",")[0].substring(1)
											.trim());
							double stop = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 2]).split(",")[1].trim());
							double step = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 2]).split(",")[2].trim());
							ArrayList<Double> add = new ArrayList<Double>();
							for (double i = start; i <= stop; i += step) {
								add.add(i);
							}
							sweep2.add(add);
						}
					}
				}
				if (sweepThese1.size() > 0) {
					int max = 0;
					for (ArrayList<Double> d : sweep1) {
						max = Math.max(max, d.size());
					}
					for (int j = 0; j < max; j++) {
						String sweep = "";
						for (int i = 0; i < sweepThese1.size(); i++) {
							int k = j;
							if (k >= sweep1.get(i).size()) {
								k = sweep1.get(i).size() - 1;
							}
							if (sweep.equals("")) {
								sweep += sweepThese1.get(i) + "=" + sweep1.get(i).get(k);
							}
							else {
								sweep += "_" + sweepThese1.get(i) + "=" + sweep1.get(i).get(k);
							}
						}
						if (sweepThese2.size() > 0) {
							int max2 = 0;
							for (ArrayList<Double> d : sweep2) {
								max2 = Math.max(max2, d.size());
							}
							for (int l = 0; l < max2; l++) {
								String sweepTwo = sweep;
								for (int i = 0; i < sweepThese2.size(); i++) {
									int k = l;
									if (k >= sweep2.get(i).size()) {
										k = sweep2.get(i).size() - 1;
									}
									if (sweepTwo.equals("")) {
										sweepTwo += sweepThese2.get(i) + "=" + sweep2.get(i).get(k);
									}
									else {
										sweepTwo += "_" + sweepThese2.get(i) + "=" + sweep2.get(i).get(k);
									}
								}
								new File(simDir + separator + sweepTwo.replace("/", "-")).mkdir();
								createSBML(sweepTwo);
								reb2sac.setDir(sweepTwo.replace("/", "-"));
								Thread t = new Thread(reb2sac);
								t.start();
								reb2sac.emptyFrames();
							}
						}
						else {
							new File(simDir + separator + sweep.replace("/", "-")).mkdir();
							createSBML(sweep);
							reb2sac.setDir(sweep.replace("/", "-"));
							Thread t = new Thread(reb2sac);
							t.start();
							reb2sac.emptyFrames();
						}
					}
				}
				else {
					createSBML(".");
					reb2sac.setDir(".");
					new Thread(reb2sac).start();
					reb2sac.emptyFrames();
				}
			}
			else {
				createSBML(".");
			}
			change = false;
		}
		else {
			try {
				log.addText("Saving sbml file:\n" + file + "\n");
				FileOutputStream out = new FileOutputStream(new File(file));
				document.getModel().setName(modelName.getText().trim());
				SBMLWriter writer = new SBMLWriter();
				String doc = writer.writeToString(document);
				byte[] output = doc.getBytes();
				out.write(output);
				out.close();
				change = false;
				if (paramsOnly) {
					reb2sac.updateSpeciesList();
				}
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(biosim.frame(), "Unable to save sbml file.",
						"Error Saving File", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void setFile(String newFile) {
		file = newFile;
	}

	public void setModelID(String modelID) {
		this.modelID.setText(modelID);
		document.getModel().setId(modelID);
	}
}
