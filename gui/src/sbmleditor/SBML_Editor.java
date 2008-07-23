package sbmleditor;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.parser.GCMParser;

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

	private String[] origAssign; // array of original event assignments

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

	private JComboBox ruleType, ruleVar; // rule fields;

	private JTextField ruleMath; // rule fields;

	private JTextField eventID, eventName, eventTrigger, eventDelay; // event

	// fields;

	private JComboBox eaID; // event assignment fields;

	private JTextField consID, consMath, consMessage; // constraints fields;

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

	private Reb2Sac reb2sac; // reb2sac options

	private JButton saveNoRun, run, saveAs, check; // save and run buttons

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

	private boolean editComp = false;

	private String refFile;

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
		if (paramFile != null) {
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					refFile = scan.nextLine();
				}
				scan.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(biosim.frame(), "Unable to read parameter file.", "Error",
						JOptionPane.ERROR_MESSAGE);
				refFile = "";
			}
		}
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
		if (paramFile != null) {
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					refFile = scan.nextLine();
				}
				scan.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(biosim.frame(), "Unable to read parameter file.", "Error",
						JOptionPane.ERROR_MESSAGE);
				refFile = file;
			}
		}
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
			document.setLevelAndVersion(2, 3);
			model = document.getModel();
			modelName = new JTextField(model.getName(), 50);
			if (model.getId().equals("")) {
				String modelID = file.split(separator)[file.split(separator).length - 1];
				if (modelID.indexOf('.') >= 0) {
					modelID = modelID.substring(0, modelID.indexOf('.'));
				}
				model.setId(modelID);
				save(false, "");
			}
			createFunction(model,"uniform","Uniform distribution","lambda(a,b,(a+b)/2)");
			createFunction(model,"normal","Normal distribution","lambda(m,s,m)");
			createFunction(model,"exponential","Exponential distribution","lambda(mu,mu)");
			createFunction(model,"gamma","Gamma distribution","lambda(a,b,a*b)");
			createFunction(model,"lognormal","Lognormal distribution","lambda(z,s,exp(z+s^2/2))");
			createFunction(model,"chisq","Chi-squared distribution","lambda(nu,nu)");
			createFunction(model,"laplace","Laplace distribution","lambda(a,a)");
			createFunction(model,"cauchy","Cauchy distribution","lambda(a,a)");
			createFunction(model,"rayleigh","Rayleigh distribution","lambda(s,s*sqrt(pi/2))");
			createFunction(model,"poisson","Poisson distribution","lambda(mu,mu)");
			createFunction(model,"binomial","Binomial distribution","lambda(p,n,p*n)");
			createFunction(model,"bernoulli","Bernoulli distribution","lambda(p,p)");
		}
		else {
			document = new SBMLDocument();
			document.setLevelAndVersion(2, 3);
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
		ids = model.getListOfConstraints();
		for (int i = 0; i < model.getNumConstraints(); i++) {
			if (((Constraint) ids.get(i)).isSetMetaId()) {
				usedIDs.add(((Constraint) ids.get(i)).getMetaId());
			}
		}
		ids = model.getListOfEvents();
		for (int i = 0; i < model.getNumEvents(); i++) {
			if (((org.sbml.libsbml.Event) ids.get(i)).isSetId()) {
				usedIDs.add(((org.sbml.libsbml.Event) ids.get(i)).getId());
			}
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
		ArrayList<String> getParams = new ArrayList<String>();
		if (paramsOnly) {
			parameterChanges = new ArrayList<String>();
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					scan.nextLine();
				}
				while (scan.hasNextLine()) {
					getParams.add(scan.nextLine());
				}
				scan.close();
			}
			catch (Exception e) {
			}
			addCompart.setEnabled(false);
			removeCompart.setEnabled(false);
		}
		JLabel compartmentsLabel = new JLabel("List of Compartments:");
		compartments = new JList();
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 130));
		scroll.setPreferredSize(new Dimension(276, 130));
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
			if (paramsOnly) {
				for (int j = 0; j < getParams.size(); j++) {
					if (getParams.get(j).split(" ")[0].equals(compartment.getId())) {
						parameterChanges.add(getParams.get(j));
						String[] splits = getParams.get(j).split(" ");
						if (splits[splits.length - 2].equals("Custom")) {
							String value = splits[splits.length - 1];
							compartment.setSize(Double.parseDouble(value));
							comps[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
						else if (splits[splits.length - 2].equals("Sweep")) {
							String value = splits[splits.length - 1];
							compartment.setSize(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							comps[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
					}
				}
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
		scroll1.setMinimumSize(new Dimension(260, 130));
		scroll1.setPreferredSize(new Dimension(276, 130));
		scroll1.setViewportView(species);
		ListOf listOfSpecies = model.getListOfSpecies();
		specs = new String[(int) model.getNumSpecies()];
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = (Species) listOfSpecies.get(i);
			if (species.isSetSpeciesType()) {
				specs[i] = species.getId() + " " + species.getSpeciesType() + " "
						+ species.getCompartment();
			}
			else {
				specs[i] = species.getId() + " " + species.getCompartment();
			}
			if (species.isSetInitialAmount()) {
				specs[i] += " " + species.getInitialAmount();
			}
			else {
				specs[i] += " " + species.getInitialConcentration();
			}
			if (species.isSetUnits()) {
				specs[i] += " " + species.getUnits();
			}
			if (paramsOnly) {
				for (int j = 0; j < getParams.size(); j++) {
					if (getParams.get(j).split(" ")[0].equals(species.getId())) {
						parameterChanges.add(getParams.get(j));
						String[] splits = getParams.get(j).split(" ");
						if (splits[splits.length - 2].equals("Custom")) {
							String value = splits[splits.length - 1];
							if (species.isSetInitialAmount()) {
								species.setInitialAmount(Double.parseDouble(value));
							}
							else {
								species.setInitialConcentration(Double.parseDouble(value));
							}
							specs[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
						else if (splits[splits.length - 2].equals("Sweep")) {
							String value = splits[splits.length - 1];
							if (species.isSetInitialAmount()) {
								species.setInitialAmount(Double
										.parseDouble(value.split(",")[0].substring(1).trim()));
							}
							else {
								species.setInitialConcentration(Double.parseDouble(value.split(",")[0].substring(1)
										.trim()));
							}
							specs[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
					}
				}
			}
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
		//addReacs.add(copyReac);
		addReac.addActionListener(this);
		removeReac.addActionListener(this);
		editReac.addActionListener(this);
		//copyReac.addActionListener(this);
		if (paramsOnly) {
			addReac.setEnabled(false);
			removeReac.setEnabled(false);
			//copyReac.setEnabled(false);
		}
		JLabel reactionsLabel = new JLabel("List of Reactions:");
		reactions = new JList();
		reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(400, 130));
		scroll2.setPreferredSize(new Dimension(436, 130));
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
					for (int k = 0; k < getParams.size(); k++) {
						if (getParams.get(k).split(" ")[0].equals(reaction.getId() + "/" + paramet.getId())) {
							parameterChanges.add(getParams.get(k));
							String[] splits = getParams.get(k).split(" ");
							if (splits[splits.length - 2].equals("Custom")) {
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
		scroll3.setMinimumSize(new Dimension(260, 130));
		scroll3.setPreferredSize(new Dimension(276, 130));
		scroll3.setViewportView(parameters);
		ListOf listOfParameters = model.getListOfParameters();
		params = new String[(int) model.getNumParameters()];
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameter = (Parameter) listOfParameters.get(i);
			if (parameter.isSetUnits()) {
				params[i] = parameter.getId() + " " + parameter.getValue() + " " + parameter.getUnits();
			}
			else {
				params[i] = parameter.getId() + " " + parameter.getValue();
			}
			if (paramsOnly) {
				for (int j = 0; j < getParams.size(); j++) {
					if (getParams.get(j).split(" ")[0].equals(parameter.getId())) {
						parameterChanges.add(getParams.get(j));
						String[] splits = getParams.get(j).split(" ");
						if (splits[splits.length - 2].equals("Custom")) {
							String value = splits[splits.length - 1];
							parameter.setValue(Double.parseDouble(value));
							params[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
						else if (splits[splits.length - 2].equals("Sweep")) {
							String value = splits[splits.length - 1];
							parameter.setValue(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							params[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
					}
				}
			}
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
		modelName = new JTextField(model.getName(), 40);
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
		this.setLayout(new BorderLayout());

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
			run = new JButton("Save and Run");
			saveNoRun.setMnemonic(KeyEvent.VK_S);
			run.setMnemonic(KeyEvent.VK_R);
			saveNoRun.addActionListener(this);
			run.addActionListener(this);
			JPanel saveRun = new JPanel();
			saveRun.add(run);
			saveRun.add(saveNoRun);
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, saveRun, null);
			splitPane.setDividerSize(0);
			this.add(splitPane, "South");
		}
		else {
			check = new JButton("Save and Check SBML");
			check.setMnemonic(KeyEvent.VK_C);
			check.addActionListener(this);
			saveNoRun = new JButton("Save SBML");
			saveAs = new JButton("Save As");
			saveNoRun.setMnemonic(KeyEvent.VK_S);
			saveAs.setMnemonic(KeyEvent.VK_A);
			saveNoRun.addActionListener(this);
			saveAs.addActionListener(this);
			JPanel saveRun = new JPanel();
			saveRun.add(saveNoRun);
			saveRun.add(check);
			saveRun.add(saveAs);
			this.add(saveRun, "South");
		}
	}

	/**
	 * Private helper method to create definitions/types frame.
	 */
	private JPanel createDefnFrame(Model model) {
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
				funcs[i] += myFormulaToString(function.getArgument(j));
			}
			if (function.isSetMath()) {
				funcs[i] += " ) = " + myFormulaToString(function.getBody());
			}
		}
		String[] oldFuncs = funcs;
		try {
			funcs = sortFunctions(funcs);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in function definitions.",
					"Cycle Detected", JOptionPane.ERROR_MESSAGE);
			funcs = oldFuncs;
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
		/* Create initial assignment panel */
		addInit = new JButton("Add Initial");
		removeInit = new JButton("Remove Initial");
		editInit = new JButton("Edit Initial");
		initAssigns = new JList();
		ListOf listOfInits = model.getListOfInitialAssignments();
		inits = new String[(int) model.getNumInitialAssignments()];
		for (int i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment init = (InitialAssignment) listOfInits.get(i);
			inits[i] = init.getSymbol() + " = " + myFormulaToString(init.getMath());
		}
		String[] oldInits = inits;
		boolean cycle = false;
		try {
			inits = sortInitRules(inits);
		}
		catch (Exception e) {
			cycle = true;
			JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in initial assignments.",
					"Cycle Detected", JOptionPane.ERROR_MESSAGE);
			inits = oldInits;
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
				rul[i] = "0 = " + myFormulaToString(rule.getMath());
			}
			else if (rule.isAssignment()) {
				rul[i] = rule.getVariable() + " = " + myFormulaToString(rule.getMath());
			}
			else {
				rul[i] = "d( " + rule.getVariable() + " )/dt = " + myFormulaToString(rule.getMath());
			}
		}
		String[] oldRul = rul;
		try {
			rul = sortRules(rul);
		}
		catch (Exception e) {
			cycle = true;
			JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in assignments.",
					"Cycle Detected", JOptionPane.ERROR_MESSAGE);
			rul = oldRul;
		}
		if (!cycle && checkCycles(inits, rul)) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Cycle detected within initial assignments, assignment rules, and rate laws.", "Cycle Detected",
					JOptionPane.ERROR_MESSAGE);
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
			if (!constraint.isSetMetaId()) {
				String constraintId = "constraint0";
				int cn = 0;
				while (usedIDs.contains(constraintId)) {
					cn++;
					constraintId = "constraint" + cn;
				}
				usedIDs.add(constraintId);
				constraint.setMetaId(constraintId);
			}
			cons[i] = constraint.getMetaId();
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
			if (!event.isSetId()) {
				String eventId = "event0";
				int en = 0;
				while (usedIDs.contains(eventId)) {
					en++;
					eventId = "event" + en;
				}
				usedIDs.add(eventId);
				event.setId(eventId);
			}
			ev[i] = event.getId();
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
		if (!panelName.equals("Rules") && !panelName.equals("Function Definitions")
				&& !panelName.equals("Initial Assignments")) {
			sort(panelList);
		}
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
		// if the run button is clicked
		if (e.getSource() == run) {
			reb2sac.getRunButton().doClick();
		}
		// if the check button is clicked
		else if (e.getSource() == check) {
			save(false, "");
			check();
		}
		// if the save button is clicked
		else if (e.getSource() == saveNoRun) {
			if (paramsOnly) {
				reb2sac.getSaveButton().doClick();
			}
			else {
				save(false, "");
			}
		}
		// if the save as button is clicked
		else if (e.getSource() == saveAs) {
			saveAs();
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
			removeFunction();
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
			removeUnit();
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
			removeList();
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
			removeCompType();
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
			removeSpecType();
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
			removeInit();
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
			removeRule();
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
			removeEvent();
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
			removeAssignment();
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
			removeConstraint();
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
			removeCompartment();
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
			removeSpecies();
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
			copyReaction();
		}
		// if the remove reactions button is clicked
		else if (e.getSource() == removeReac) {
			removeReaction();
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
			removeParameter();
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
			reacRemoveParam();
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
			removeReactant();
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
			removeProduct();
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
			removeModifier();
		}
		// if the clear button is clicked
		else if (e.getSource() == clearKineticLaw) {
			kineticLaw.setText("");
			change = true;
		}
		// if the use mass action button is clicked
		else if (e.getSource() == useMassAction) {
			useMassAction();
		}
	}

	/**
	 * Remove a unit from list
	 */
	private void removeList() {
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

	/**
	 * Remove a unit
	 */
	private void removeUnit() {
		if (unitDefs.getSelectedIndex() != -1) {
			if (!unitsInUse(((String) unitDefs.getSelectedValue()).split(" ")[0])) {
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
	}

	/**
	 * Remove a compartment type
	 */
	private void removeCompType() {
		if (compTypes.getSelectedIndex() != -1) {
			boolean remove = true;
			ArrayList<String> compartmentUsing = new ArrayList<String>();
			for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
				Compartment compartment = (Compartment) document.getModel().getListOfCompartments().get(i);
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
				JOptionPane.showMessageDialog(biosim.frame(), scroll, "Unable To Remove Compartment Type",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Remove an initial assignment
	 */
	private void removeInit() {
		if (initAssigns.getSelectedIndex() != -1) {
			String selected = ((String) initAssigns.getSelectedValue());
			String tempVar = selected.split(" ")[0];
			String tempMath = selected.substring(selected.indexOf('=') + 2);
			ListOf r = document.getModel().getListOfInitialAssignments();
			for (int i = 0; i < document.getModel().getNumInitialAssignments(); i++) {
				if (myFormulaToString(((InitialAssignment) r.get(i)).getMath()).equals(tempMath)
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

	/**
	 * Remove a species type
	 */
	private void removeSpecType() {
		if (specTypes.getSelectedIndex() != -1) {
			boolean remove = true;
			ArrayList<String> speciesUsing = new ArrayList<String>();
			for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
				Species species = (Species) document.getModel().getListOfSpecies().get(i);
				if (species.isSetSpeciesType()) {
					if (species.getSpeciesType()
							.equals(((String) specTypes.getSelectedValue()).split(" ")[0])) {
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

	/**
	 * Remove a rule
	 */
	private void removeRule() {
		if (rules.getSelectedIndex() != -1) {
			String selected = ((String) rules.getSelectedValue());
			removeTheRule(selected);
			rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			rul = (String[]) Buttons.remove(rules, rul);
			rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			rules.setSelectedIndex(0);
			change = true;
		}
	}

	/**
	 * Remove the rule
	 */
	private void removeTheRule(String selected) {
		// algebraic rule
		if ((selected.split(" ")[0]).equals("0")) {
			String tempMath = selected.substring(4);
			ListOf r = document.getModel().getListOfRules();
			for (int i = 0; i < document.getModel().getNumRules(); i++) {
				if ((((Rule) r.get(i)).isAlgebraic()) && ((Rule) r.get(i)).getFormula().equals(tempMath)) {
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
				if ((((Rule) r.get(i)).isAssignment()) && ((Rule) r.get(i)).getFormula().equals(tempMath)
						&& ((Rule) r.get(i)).getVariable().equals(tempVar)) {
					r.remove(i);
				}
			}
		}
	}

	/**
	 * Remove an event
	 */
	private void removeEvent() {
		if (events.getSelectedIndex() != -1) {
			String selected = ((String) events.getSelectedValue());
			removeTheEvent(selected);
			usedIDs.remove(((String) events.getSelectedValue()).split(" ")[0]);
			events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			ev = (String[]) Buttons.remove(events, ev);
			events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			events.setSelectedIndex(0);
			change = true;
		}
	}

	/**
	 * Remove the event
	 */
	private void removeTheEvent(String selected) {
		ListOf EL = document.getModel().getListOfEvents();
		for (int i = 0; i < document.getModel().getNumEvents(); i++) {
			org.sbml.libsbml.Event E = (org.sbml.libsbml.Event) EL.get(i);
			if (E.getId().equals(selected)) {
				EL.remove(i);
			}
		}
	}

	/**
	 * Remove an assignment
	 */
	private void removeAssignment() {
		if (eventAssign.getSelectedIndex() != -1) {
			eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			assign = (String[]) Buttons.remove(eventAssign, assign);
			eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			eventAssign.setSelectedIndex(0);
			change = true;
		}
	}

	/**
	 * Remove a constraint
	 */
	private void removeConstraint() {
		if (constraints.getSelectedIndex() != -1) {
			String selected = ((String) constraints.getSelectedValue());
			ListOf c = document.getModel().getListOfConstraints();
			for (int i = 0; i < document.getModel().getNumConstraints(); i++) {
				if ((((Constraint) c.get(i)).getMetaId()).equals(selected)) {
					usedIDs.remove(((Constraint) c.get(i)).getMetaId());
					c.remove(i);
					break;
				}
			}
			constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			cons = (String[]) Buttons.remove(constraints, cons);
			constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			constraints.setSelectedIndex(0);
			change = true;
		}
	}

	/**
	 * Remove a compartment
	 */
	private void removeCompartment() {
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
				ArrayList<String> outsideUsing = new ArrayList<String>();
				for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
					Compartment compartment = document.getModel().getCompartment(i);
					if (compartment.isSetOutside()) {
						if (compartment.getOutside().equals(
								((String) compartments.getSelectedValue()).split(" ")[0])) {
							remove = false;
							outsideUsing.add(compartment.getId());
						}
					}
				}
				if (!remove) {
					String message = "Unable to remove the selected compartment.";
					if (speciesUsing.size() != 0) {
						message += "\n\nIt contains the following species:\n";
						String[] vars = speciesUsing.toArray(new String[0]);
						sort(vars);
						for (int i = 0; i < vars.length; i++) {
							if (i == vars.length - 1) {
								message += vars[i];
							}
							else {
								message += vars[i] + "\n";
							}
						}
					}
					if (outsideUsing.size() != 0) {
						message += "\n\nIt outside the following compartments:\n";
						String[] vars = outsideUsing.toArray(new String[0]);
						sort(vars);
						for (int i = 0; i < vars.length; i++) {
							if (i == vars.length - 1) {
								message += vars[i];
							}
							else {
								message += vars[i] + "\n";
							}
						}
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
				else if (!variableInUse(((String) compartments.getSelectedValue()).split(" ")[0], false)) {
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
			}
			else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Each model must contain at least one compartment.", "Unable To Remove Compartment",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Remove a species
	 */
	private void removeSpecies() {
		if (species.getSelectedIndex() != -1) {
			if (!variableInUse(((String) species.getSelectedValue()).split(" ")[0], false)) {
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
		}
	}

	/**
	 * Copy a reaction
	 */
	private void copyReaction() {
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
			JOptionPane.showMessageDialog(biosim.frame(), "ID is not unique.", "Enter A Unique ID",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Remove a reaction
	 */
	private void removeReaction() {
		if (reactions.getSelectedIndex() != -1) {
			String selected = ((String) reactions.getSelectedValue()).split(" ")[0];
			removeTheReaction(selected);
			usedIDs.remove(selected);
			reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			reacts = (String[]) Buttons.remove(reactions, reacts);
			reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			reactions.setSelectedIndex(0);
			change = true;
		}
	}

	/**
	 * Remove the reaction
	 */
	private void removeTheReaction(String selected) {
		Reaction tempReaction = document.getModel().getReaction(selected);
		ListOf r = document.getModel().getListOfReactions();
		for (int i = 0; i < document.getModel().getNumReactions(); i++) {
			if (((Reaction) r.get(i)).getId().equals(tempReaction.getId())) {
				r.remove(i);
			}
		}
		usedIDs.remove(selected);
	}

	/**
	 * Remove a global parameter
	 */
	private void removeParameter() {
		if (parameters.getSelectedIndex() != -1) {
			if (!variableInUse(((String) parameters.getSelectedValue()).split(" ")[0], false)) {
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
	}

	/**
	 * Remove a reactant from a reaction
	 */
	private void removeReactant() {
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

	/**
	 * Remove a product from a reaction
	 */
	private void removeProduct() {
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

	/**
	 * Remove a modifier from a reaction
	 */
	private void removeModifier() {
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

	/**
	 * Remove a function if not in use
	 */
	private void useMassAction() {
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
			if (s.isSetStoichiometryMath()) {
				kinetic += " * pow(" + s.getSpecies() + ", "
						+ myFormulaToString(s.getStoichiometryMath().getMath()) + ")";
			}
			else {
				if (s.getStoichiometry() == 1) {
					kinetic += " * " + s.getSpecies();
				}
				else {
					kinetic += " * pow(" + s.getSpecies() + ", " + s.getStoichiometry() + ")";
				}
			}
		}
		for (ModifierSpeciesReference s : changedModifiers) {
			kinetic += " * " + s.getSpecies();
		}
		if (reacReverse.getSelectedItem().equals("true")) {
			kinetic += " - " + kr;
			for (SpeciesReference s : changedProducts) {
				if (s.isSetStoichiometryMath()) {
					kinetic += " * pow(" + s.getSpecies() + ", "
							+ myFormulaToString(s.getStoichiometryMath().getMath()) + ")";
				}
				else {
					if (s.getStoichiometry() == 1) {
						kinetic += " * " + s.getSpecies();
					}
					else {
						kinetic += " * pow(" + s.getSpecies() + ", " + s.getStoichiometry() + ")";
					}
				}
			}
			for (ModifierSpeciesReference s : changedModifiers) {
				kinetic += " * " + s.getSpecies();
			}
		}
		kineticLaw.setText(kinetic);
		change = true;
	}

	/**
	 * Remove a function if not in use
	 */
	private void removeFunction() {
		if (functions.getSelectedIndex() != -1) {
			if (!variableInUse(((String) functions.getSelectedValue()).split(" ")[0], false)) {
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
	}

	/**
	 * Save SBML file with a new name
	 */
	private void saveAs() {
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
						tab
								.setComponentAt(i,
										new SBML_Editor(newFile, reb2sac, log, biosim, simDir, paramFile));
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

	/**
	 * Remove a reaction parameter, if allowed
	 */
	private void reacRemoveParam() {
		if (reacParameters.getSelectedIndex() != -1) {
			String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
			Reaction reaction = document.getModel().getReaction(
					((String) reactions.getSelectedValue()).split(" ")[0]);
			String[] vars = reaction.getKineticLaw().getFormula().split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(v)) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Cannot remove reaction parameter because it is used in the kinetic law.",
							"Cannot Remove Parameter", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					String specRef = reaction.getProduct(j).getSpecies();
					if (reaction.getProduct(j).isSetStoichiometryMath()) {
						vars = myFormulaToString(reaction.getProduct(j).getStoichiometryMath().getMath())
								.split(" |\\(|\\)|\\,");
						for (int k = 0; k < vars.length; k++) {
							if (vars[k].equals(v)) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"Cannot remove reaction parameter because it is used in the stoichiometry math for product "
												+ specRef + ".", "Cannot Remove Parameter", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
				}
			}
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				if (reaction.getReactant(j).isSetSpecies()) {
					String specRef = reaction.getReactant(j).getSpecies();
					if (reaction.getReactant(j).isSetStoichiometryMath()) {
						vars = myFormulaToString(reaction.getReactant(j).getStoichiometryMath().getMath())
								.split(" |\\(|\\)|\\,");
						for (int k = 0; k < vars.length; k++) {
							if (vars[k].equals(v)) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"Cannot remove reaction parameter because it is used in the stoichiometry math for reactant "
												+ specRef + ".", "Cannot Remove Parameter", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
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
			reacParams = (String[]) Buttons.remove(reacParameters, reacParams);
			reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			reacParameters.setSelectedIndex(0);
			change = true;
		}
	}

	/**
	 * Check if a unit is in use.
	 */
	private boolean unitsInUse(String unit) {
		Model model = document.getModel();
		boolean inUse = false;
		ArrayList<String> compartmentsUsing = new ArrayList<String>();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			Compartment compartment = (Compartment) model.getListOfCompartments().get(i);
			if (compartment.getUnits().equals(unit)) {
				inUse = true;
				compartmentsUsing.add(compartment.getId());
			}
		}
		ArrayList<String> speciesUsing = new ArrayList<String>();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = (Species) model.getListOfSpecies().get(i);
			if (species.getUnits().equals(unit)) {
				inUse = true;
				speciesUsing.add(species.getId());
			}
		}
		ArrayList<String> parametersUsing = new ArrayList<String>();
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameters = (Parameter) model.getListOfParameters().get(i);
			if (parameters.getUnits().equals(unit)) {
				inUse = true;
				parametersUsing.add(parameters.getId());
			}
		}
		ArrayList<String> reacParametersUsing = new ArrayList<String>();
		for (int i = 0; i < model.getNumReactions(); i++) {
			for (int j = 0; j < model.getReaction(i).getKineticLaw().getNumParameters(); j++) {
				Parameter parameters = (Parameter) model.getReaction(i).getKineticLaw()
						.getListOfParameters().get(j);
				if (parameters.getUnits().equals(unit)) {
					inUse = true;
					reacParametersUsing.add(model.getReaction(i).getId() + "/" + parameters.getId());
				}
			}
		}
		if (inUse) {
			String message = "Unable to remove the selected unit.";
			String[] ids;
			if (compartmentsUsing.size() != 0) {
				ids = compartmentsUsing.toArray(new String[0]);
				sort(ids);
				message += "\n\nIt is used by the following compartments:\n";
				for (int i = 0; i < ids.length; i++) {
					if (i == ids.length - 1) {
						message += ids[i];
					}
					else {
						message += ids[i] + "\n";
					}
				}
			}
			if (speciesUsing.size() != 0) {
				ids = speciesUsing.toArray(new String[0]);
				sort(ids);
				message += "\n\nIt is used by the following species:\n";
				for (int i = 0; i < ids.length; i++) {
					if (i == ids.length - 1) {
						message += ids[i];
					}
					else {
						message += ids[i] + "\n";
					}
				}
			}
			if (parametersUsing.size() != 0) {
				ids = parametersUsing.toArray(new String[0]);
				sort(ids);
				message += "\n\nIt is used by the following parameters:\n";
				for (int i = 0; i < ids.length; i++) {
					if (i == ids.length - 1) {
						message += ids[i];
					}
					else {
						message += ids[i] + "\n";
					}
				}
			}
			if (reacParametersUsing.size() != 0) {
				ids = reacParametersUsing.toArray(new String[0]);
				sort(ids);
				message += "\n\nIt is used by the following reaction/parameters:\n";
				for (int i = 0; i < ids.length; i++) {
					if (i == ids.length - 1) {
						message += ids[i];
					}
					else {
						message += ids[i] + "\n";
					}
				}
			}
			JTextArea messageArea = new JTextArea(message);
			messageArea.setEditable(false);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(350, 350));
			scroll.setPreferredSize(new Dimension(350, 350));
			scroll.setViewportView(messageArea);
			JOptionPane.showMessageDialog(biosim.frame(), scroll, "Unable To Remove Variable",
					JOptionPane.ERROR_MESSAGE);
		}
		return inUse;
	}

	/**
	 * Check if a variable is in use.
	 */
	private boolean variableInUse(String species, boolean zeroDim) {
		Model model = document.getModel();
		boolean inUse = false;
		ArrayList<String> stoicMathUsing = new ArrayList<String>();
		ArrayList<String> reactantsUsing = new ArrayList<String>();
		ArrayList<String> productsUsing = new ArrayList<String>();
		ArrayList<String> modifiersUsing = new ArrayList<String>();
		ArrayList<String> kineticLawsUsing = new ArrayList<String>();
		ArrayList<String> initsUsing = new ArrayList<String>();
		ArrayList<String> rulesUsing = new ArrayList<String>();
		ArrayList<String> constraintsUsing = new ArrayList<String>();
		ArrayList<String> eventsUsing = new ArrayList<String>();
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					String specRef = reaction.getProduct(j).getSpecies();
					if (species.equals(specRef)) {
						inUse = true;
						productsUsing.add(reaction.getId());
					}
					else if (reaction.getProduct(j).isSetStoichiometryMath()) {
						String[] vars = myFormulaToString(
								reaction.getProduct(j).getStoichiometryMath().getMath()).split(" |\\(|\\)|\\,");
						for (int k = 0; k < vars.length; k++) {
							if (vars[k].equals(species)) {
								stoicMathUsing.add(reaction.getId() + "/" + specRef);
								inUse = true;
								break;
							}
						}
					}
				}
			}
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				if (reaction.getReactant(j).isSetSpecies()) {
					String specRef = reaction.getReactant(j).getSpecies();
					if (species.equals(specRef)) {
						inUse = true;
						reactantsUsing.add(reaction.getId());
					}
					else if (reaction.getReactant(j).isSetStoichiometryMath()) {
						String[] vars = myFormulaToString(
								reaction.getReactant(j).getStoichiometryMath().getMath()).split(" |\\(|\\)|\\,");
						for (int k = 0; k < vars.length; k++) {
							if (vars[k].equals(species)) {
								stoicMathUsing.add(reaction.getId() + "/" + specRef);
								inUse = true;
								break;
							}
						}
					}
				}
			}
			for (int j = 0; j < reaction.getNumModifiers(); j++) {
				if (reaction.getModifier(j).isSetSpecies()) {
					String specRef = reaction.getModifier(j).getSpecies();
					if (species.equals(specRef)) {
						inUse = true;
						modifiersUsing.add(reaction.getId());
					}
				}
			}
			String[] vars = reaction.getKineticLaw().getFormula().split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					kineticLawsUsing.add(reaction.getId());
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < inits.length; i++) {
			String[] vars = inits[i].split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					initsUsing.add(inits[i]);
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < rul.length; i++) {
			String[] vars = rul[i].split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					rulesUsing.add(rul[i]);
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < cons.length; i++) {
			String[] vars = cons[i].split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					constraintsUsing.add(cons[i]);
					inUse = true;
					break;
				}
			}
		}
		ListOf e = model.getListOfEvents();
		for (int i = 0; i < model.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) e.get(i);
			String trigger = myFormulaToString(event.getTrigger().getMath());
			String eventStr = trigger;
			if (event.isSetDelay()) {
				eventStr += " " + myFormulaToString(event.getDelay().getMath());
			}
			for (int j = 0; j < event.getNumEventAssignments(); j++) {
				eventStr += " " + (event.getEventAssignment(j).getVariable()) + " = "
						+ myFormulaToString(event.getEventAssignment(j).getMath());
			}
			String[] vars = eventStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					eventsUsing.add(trigger);
					inUse = true;
					break;
				}
			}
		}
		if (inUse) {
			String reactants = "";
			String products = "";
			String modifiers = "";
			String kineticLaws = "";
			String stoicMath = "";
			String initAssigns = "";
			String rules = "";
			String constraints = "";
			String events = "";
			String[] reacts = reactantsUsing.toArray(new String[0]);
			sort(reacts);
			String[] prods = productsUsing.toArray(new String[0]);
			sort(prods);
			String[] mods = modifiersUsing.toArray(new String[0]);
			sort(mods);
			String[] kls = kineticLawsUsing.toArray(new String[0]);
			sort(kls);
			String[] sm = stoicMathUsing.toArray(new String[0]);
			sort(sm);
			String[] inAs = initsUsing.toArray(new String[0]);
			sort(inAs);
			String[] ruls = rulesUsing.toArray(new String[0]);
			sort(ruls);
			String[] consts = constraintsUsing.toArray(new String[0]);
			sort(consts);
			String[] evs = eventsUsing.toArray(new String[0]);
			sort(evs);
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
			for (int i = 0; i < kls.length; i++) {
				if (i == kls.length - 1) {
					kineticLaws += kls[i];
				}
				else {
					kineticLaws += kls[i] + "\n";
				}
			}
			for (int i = 0; i < sm.length; i++) {
				if (i == sm.length - 1) {
					stoicMath += sm[i];
				}
				else {
					stoicMath += sm[i] + "\n";
				}
			}
			for (int i = 0; i < inAs.length; i++) {
				if (i == inAs.length - 1) {
					initAssigns += inAs[i];
				}
				else {
					initAssigns += inAs[i] + "\n";
				}
			}
			for (int i = 0; i < ruls.length; i++) {
				if (i == ruls.length - 1) {
					rules += ruls[i];
				}
				else {
					rules += ruls[i] + "\n";
				}
			}
			for (int i = 0; i < consts.length; i++) {
				if (i == consts.length - 1) {
					constraints += consts[i];
				}
				else {
					constraints += consts[i] + "\n";
				}
			}
			for (int i = 0; i < evs.length; i++) {
				if (i == evs.length - 1) {
					events += evs[i];
				}
				else {
					events += evs[i] + "\n";
				}
			}
			String message;
			if (zeroDim) {
				message = "Unable to change compartment to 0-dimensions.";
			}
			else {
				message = "Unable to remove the selected species.";
			}
			if (reactantsUsing.size() != 0) {
				message += "\n\nIt is used as a reactant in the following reactions:\n" + reactants;
			}
			if (productsUsing.size() != 0) {
				message += "\n\nIt is used as a product in the following reactions:\n" + products;
			}
			if (modifiersUsing.size() != 0) {
				message += "\n\nIt is used as a modifier in the following reactions:\n" + modifiers;
			}
			if (kineticLawsUsing.size() != 0) {
				message += "\n\nIt is used in the kinetic law in the following reactions:\n" + kineticLaws;
			}
			if (stoicMathUsing.size() != 0) {
				message += "\n\nIt is used in the stoichiometry math for the following reaction/species:\n"
						+ stoicMath;
			}
			if (initsUsing.size() != 0) {
				message += "\n\nIt is used in the following initial assignments:\n" + initAssigns;
			}
			if (rulesUsing.size() != 0) {
				message += "\n\nIt is used in the following rules:\n" + rules;
			}
			if (constraintsUsing.size() != 0) {
				message += "\n\nIt is used in the following constraints:\n" + constraints;
			}
			if (eventsUsing.size() != 0) {
				message += "\n\nIt is used in the following events:\n" + events;
			}
			JTextArea messageArea = new JTextArea(message);
			messageArea.setEditable(false);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(400, 400));
			scroll.setPreferredSize(new Dimension(400, 400));
			scroll.setViewportView(messageArea);
			JOptionPane.showMessageDialog(biosim.frame(), scroll, "Unable To Remove Variable",
					JOptionPane.ERROR_MESSAGE);
		}
		return inUse;
	}

	/**
	 * Check that ID is valid and unique
	 */
	private boolean checkID(String ID, String selectedID, boolean isReacParam) {
		if (ID.equals("")) {
			JOptionPane.showMessageDialog(biosim.frame(), "An ID is required.", "Enter an ID",
					JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (!(IDpat.matcher(ID).matches())) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"An ID can only contain letters, numbers, and underscores.", "Invalid ID",
					JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (ID.equals("t") || ID.equals("time") || ID.equals("true") || ID.equals("false")
				|| ID.equals("notanumber") || ID.equals("pi") || ID.equals("infinity")
				|| ID.equals("exponentiale") || ID.equals("abs") || ID.equals("arccos")
				|| ID.equals("arccosh") || ID.equals("arcsin") || ID.equals("arcsinh")
				|| ID.equals("arctan") || ID.equals("arctanh") || ID.equals("arccot")
				|| ID.equals("arccoth") || ID.equals("arccsc") || ID.equals("arccsch")
				|| ID.equals("arcsec") || ID.equals("arcsech") || ID.equals("acos") || ID.equals("acosh")
				|| ID.equals("asin") || ID.equals("asinh") || ID.equals("atan") || ID.equals("atanh")
				|| ID.equals("acot") || ID.equals("acoth") || ID.equals("acsc") || ID.equals("acsch")
				|| ID.equals("asec") || ID.equals("asech") || ID.equals("cos") || ID.equals("cosh")
				|| ID.equals("cot") || ID.equals("coth") || ID.equals("csc") || ID.equals("csch")
				|| ID.equals("ceil") || ID.equals("factorial") || ID.equals("exp") || ID.equals("floor")
				|| ID.equals("ln") || ID.equals("log") || ID.equals("sqr") || ID.equals("log10")
				|| ID.equals("pow") || ID.equals("sqrt") || ID.equals("root") || ID.equals("piecewise")
				|| ID.equals("sec") || ID.equals("sech") || ID.equals("sin") || ID.equals("sinh")
				|| ID.equals("tan") || ID.equals("tanh") || ID.equals("and") || ID.equals("or")
				|| ID.equals("xor") || ID.equals("not") || ID.equals("eq") || ID.equals("geq")
				|| ID.equals("leq") || ID.equals("gt") || ID.equals("neq") || ID.equals("lt")
				|| ID.equals("delay")) {
			JOptionPane.showMessageDialog(biosim.frame(), "ID cannot be a reserved word.", "Illegal ID",
					JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (usedIDs.contains(ID) && !ID.equals(selectedID)) {
			if (isReacParam) {
				JOptionPane.showMessageDialog(biosim.frame(), "ID shadows a global ID.", "Not a Unique ID",
						JOptionPane.WARNING_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(biosim.frame(), "ID is not unique.", "Enter a Unique ID",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}


	/**
	 * Add a new function
	 */
        private void createFunction(Model model, String id, String name, String formula) {
	    if (document.getModel().getFunctionDefinition(id)==null) {
		FunctionDefinition f = model.createFunctionDefinition();
		f.setId(id);
		f.setName(name);
		f.setMath(libsbml.parseFormula(formula));
	    }
	}

	/**
	 * Creates a frame used to edit functions or create new ones.
	 */
	private void functionEditor(String option) {
		if (option.equals("OK") && functions.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No function selected.",
					"Must Select a Function", JOptionPane.ERROR_MESSAGE);
			return;
		}
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
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				FunctionDefinition function = document.getModel().getFunctionDefinition(
						(((String) functions.getSelectedValue()).split(" ")[0]));
				funcID.setText(function.getId());
				selectedID = function.getId();
				funcName.setText(function.getName());
				String argStr = "";
				for (long j = 0; j < function.getNumArguments(); j++) {
					if (j != 0) {
						argStr += ", ";
					}
					argStr += myFormulaToString(function.getArgument(j));
				}
				args.setText(argStr);
				if (function.isSetMath()) {
					eqn.setText("" + myFormulaToString(function.getBody()));
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(funcID.getText().trim(), selectedID, false);
			if (!error) {
				String[] vars = eqn.getText().trim().split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
				for (int i = 0; i < vars.length; i++) {
					if (vars[i].equals(funcID.getText().trim())) {
						JOptionPane.showMessageDialog(biosim.frame(), "Recursive functions are not allowed.",
								"Recursion Illegal", JOptionPane.ERROR_MESSAGE);
						error = true;
						break;
					}
				}
			}
			if (!error) {
				if (eqn.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(biosim.frame(), "Formula is not valid.",
							"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (args.getText().trim().equals("")
						&& libsbml.parseFormula("lambda(" + eqn.getText().trim() + ")") == null) {
					JOptionPane.showMessageDialog(biosim.frame(), "Formula is not valid.",
							"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!args.getText().trim().equals("")
						&& libsbml.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim()
								+ ")") == null) {
					JOptionPane.showMessageDialog(biosim.frame(), "Formula is not valid.",
							"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else {
					ArrayList<String> invalidVars = getInvalidVariables(eqn.getText().trim(), false, args
							.getText().trim(), true);
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
						message = "Function can only contain the arguments or other function calls.\n\n"
								+ "Illegal variables:\n" + invalid;
						JTextArea messageArea = new JTextArea(message);
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(300, 300));
						scrolls.setPreferredSize(new Dimension(300, 300));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Illegal Variables",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
			}
			if (!error) {
				error = checkNumFunctionArguments(myParseFormula(eqn.getText().trim()));
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
					if (args.getText().trim().equals("")) {
						f.setMath(libsbml.parseFormula("lambda(" + eqn.getText().trim() + ")"));
					}
					else {
						f.setMath(libsbml.parseFormula("lambda(" + args.getText().trim() + ","
								+ eqn.getText().trim() + ")"));
					}
					for (int i = 0; i < usedIDs.size(); i++) {
						if (usedIDs.get(i).equals(val)) {
							usedIDs.set(i, funcID.getText().trim());
						}
					}
					String oldVal = funcs[index];
					funcs[index] = funcID.getText().trim() + " ( " + args.getText().trim() + " ) = "
							+ eqn.getText().trim();
					try {
						funcs = sortFunctions(funcs);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in functions.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
						funcs[index] = oldVal;
					}
					functions.setListData(funcs);
					functions.setSelectedIndex(index);
					updateVarId(false, val, funcID.getText().trim());
				}
				else {
					int index = functions.getSelectedIndex();
					JList add = new JList();
					String addStr;
					addStr = funcID.getText().trim() + " ( " + args.getText().trim() + " ) = "
							+ eqn.getText().trim();
					Object[] adding = { addStr };
					add.setListData(adding);
					add.setSelectedIndex(0);
					functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Buttons.add(funcs, functions, add, false, null, null, null, null, null, null,
							biosim.frame());
					String[] oldVal = funcs;
					funcs = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						funcs[i] = (String) adding[i];
					}
					try {
						funcs = sortFunctions(funcs);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in functions.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
						funcs = oldVal;
					}
					if (!error) {
						FunctionDefinition f = document.getModel().createFunctionDefinition();
						f.setId(funcID.getText().trim());
						f.setName(funcName.getText().trim());
						if (args.getText().trim().equals("")) {
							f.setMath(libsbml.parseFormula("lambda(" + eqn.getText().trim() + ")"));
						}
						else {
							f.setMath(libsbml.parseFormula("lambda(" + args.getText().trim() + ","
									+ eqn.getText().trim() + ")"));
						}
						usedIDs.add(funcID.getText().trim());
					}
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
				value = JOptionPane.showOptionDialog(biosim.frame(), functionPanel, "Function Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Sort functions in order to be evaluated
	 */
	private String[] sortFunctions(String[] funcs) {
		String[] result = new String[funcs.length];
		String temp;
		String temp2;
		int j = 0;
		int start = 0;
		int end = 0;

		for (int i = 0; i < funcs.length; i++) {
			String[] func = funcs[i].split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
			start = -1;
			end = -1;
			for (int k = 0; k < j; k++) {
				String[] f = result[k].split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
				for (int l = 1; l < f.length; l++) {
					if (f[l].equals(func[0])) {
						end = k;
					}
				}
				for (int l = 1; l < func.length; l++) {
					if (func[l].equals(f[0])) {
						start = k;
					}
				}
			}
			if (end == -1) {
				result[j] = funcs[i];
			}
			else if (start < end) {
				temp = result[end];
				result[end] = funcs[i];
				for (int k = end + 1; k < j; k++) {
					temp2 = result[k];
					result[k] = temp;
					temp = temp2;
				}
				result[j] = temp;
			}
			else {
				result[j] = funcs[i];
				throw new RuntimeException();
			}
			j++;
		}
		return result;
	}

	/**
	 * Creates a frame used to edit units or create new ones.
	 */
	private void unitEditor(String option) {
		if (option.equals("OK") && unitDefs.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No unit definition selected.",
					"Must Select a Unit Definition", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String[] kinds = { "ampere", "becquerel", "candela", "celsius", "coulomb", "dimensionless",
				"farad", "gram", "gray", "henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram",
				"litre", "lumen", "lux", "metre", "mole", "newton", "ohm", "pascal", "radian", "second",
				"siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if (unitID.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(biosim.frame(), "A unit definition ID is required.",
						"Enter an ID", JOptionPane.ERROR_MESSAGE);
				error = true;
				value = JOptionPane.showOptionDialog(biosim.frame(), unitDefPanel,
						"Unit Definition Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
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
				else {
					for (int i = 0; i < kinds.length; i++) {
						if (kinds[i].equals(addUnit)) {
							JOptionPane.showMessageDialog(biosim.frame(), "Unit ID matches a predefined unit.",
									"Enter a Unique ID", JOptionPane.ERROR_MESSAGE);
							error = true;
							break;
						}
					}
				}
				if (!error) {
					for (int i = 0; i < units.length; i++) {
						if (option.equals("OK")) {
							if (units[i].equals((String) unitDefs.getSelectedValue()))
								continue;
						}
						if (units[i].equals(addUnit)) {
							JOptionPane.showMessageDialog(biosim.frame(), "Unit ID is not unique.",
									"Enter a Unique ID", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
				}
				if ((!error) && (uList.length == 0)) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Unit definition must have at least one unit.", "Unit Needed",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if ((!error)
						&& ((addUnit.equals("substance")) || (addUnit.equals("length"))
								|| (addUnit.equals("area")) || (addUnit.equals("volume")) || (addUnit
								.equals("time")))) {
					if (uList.length > 1) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"Redefinition of built-in unit must have a single unit.", "Single Unit Required",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (!error) {
						if (addUnit.equals("substance")) {
							if (!(extractUnitKind(uList[0]).equals("dimensionless")
									|| (extractUnitKind(uList[0]).equals("mole") && Integer
											.valueOf(extractUnitExp(uList[0])) == 1)
									|| (extractUnitKind(uList[0]).equals("item") && Integer
											.valueOf(extractUnitExp(uList[0])) == 1)
									|| (extractUnitKind(uList[0]).equals("gram") && Integer
											.valueOf(extractUnitExp(uList[0])) == 1) || (extractUnitKind(uList[0])
									.equals("kilogram") && Integer.valueOf(extractUnitExp(uList[0])) == 1))) {
								JOptionPane
										.showMessageDialog(
												biosim.frame(),
												"Redefinition of substance must be dimensionless or\n in terms of moles, items, grams, or kilograms.",
												"Incorrect Redefinition", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						else if (addUnit.equals("time")) {
							if (!(extractUnitKind(uList[0]).equals("dimensionless") || (extractUnitKind(uList[0])
									.equals("second") && Integer.valueOf(extractUnitExp(uList[0])) == 1))) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"Redefinition of time must be dimensionless or in terms of seconds.",
										"Incorrect Redefinition", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						else if (addUnit.equals("length")) {
							if (!(extractUnitKind(uList[0]).equals("dimensionless") || (extractUnitKind(uList[0])
									.equals("metre") && Integer.valueOf(extractUnitExp(uList[0])) == 1))) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"Redefinition of length must be dimensionless or in terms of metres.",
										"Incorrect Redefinition", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						else if (addUnit.equals("area")) {
							if (!(extractUnitKind(uList[0]).equals("dimensionless") || (extractUnitKind(uList[0])
									.equals("metre") && Integer.valueOf(extractUnitExp(uList[0])) == 2))) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"Redefinition of area must be dimensionless or in terms of metres^2.",
										"Incorrect Redefinition", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						else if (addUnit.equals("volume")) {
							if (!(extractUnitKind(uList[0]).equals("dimensionless")
									|| (extractUnitKind(uList[0]).equals("litre") && Integer
											.valueOf(extractUnitExp(uList[0])) == 1) || (extractUnitKind(uList[0])
									.equals("metre") && Integer.valueOf(extractUnitExp(uList[0])) == 3))) {
								JOptionPane
										.showMessageDialog(
												biosim.frame(),
												"Redefinition of volume must be dimensionless or in terms of litres or metres^3.",
												"Incorrect Redefinition", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
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
									extractUnitExp(uList[i])).intValue(), Integer.valueOf(extractUnitScale(uList[i]))
									.intValue(), Double.valueOf(extractUnitMult(uList[i])).doubleValue());
							u.addUnit(unit);
						}
						units[index] = addUnit;
						sort(units);
						unitDefs.setListData(units);
						unitDefs.setSelectedIndex(index);
						updateUnitId(val, unitID.getText().trim());
					}
					else {
						int index = unitDefs.getSelectedIndex();
						UnitDefinition u = document.getModel().createUnitDefinition();
						u.setId(unitID.getText().trim());
						u.setName(unitName.getText().trim());
						usedIDs.add(addUnit);
						for (int i = 0; i < uList.length; i++) {
							Unit unit = new Unit(extractUnitKind(uList[i]), Integer.valueOf(
									extractUnitExp(uList[i])).intValue(), Integer.valueOf(extractUnitScale(uList[i]))
									.intValue(), Double.valueOf(extractUnitMult(uList[i])).doubleValue());
							u.addUnit(unit);
						}
						JList add = new JList();
						Object[] adding = { addUnit };
						add.setListData(adding);
						add.setSelectedIndex(0);
						unitDefs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Buttons.add(units, unitDefs, add, false, null, null, null, null, null, null,
								biosim.frame());
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
							"Unit Definition Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
							options, options[0]);
				}
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit unit list elements or create new ones.
	 */
	private void unitListEditor(String option) {
		if (option.equals("OK") && unitList.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No unit selected.", "Must Select an Unit",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel unitListPanel = new JPanel();
		JPanel ULPanel = new JPanel(new GridLayout(4, 2));
		JLabel kindLabel = new JLabel("Kind:");
		JLabel expLabel = new JLabel("Exponent:");
		JLabel scaleLabel = new JLabel("Scale:");
		JLabel multLabel = new JLabel("Multiplier:");
		String[] kinds = { "ampere", "becquerel", "candela", "celsius", "coulomb", "dimensionless",
				"farad", "gram", "gray", "henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram",
				"litre", "lumen", "lux", "metre", "mole", "newton", "ohm", "pascal", "radian", "second",
				"siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			try {
				Integer.valueOf(exp.getText().trim()).intValue();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(biosim.frame(), "Exponent must be an integer.",
						"Integer Expected", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			if (!error) {
				try {
					Integer.valueOf(scale.getText().trim()).intValue();
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(biosim.frame(), "Scale must be an integer.",
							"Integer Expected", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				try {
					Double.valueOf(mult.getText().trim()).doubleValue();
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(biosim.frame(), "Multiplier must be a double.",
							"Double Expected", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
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
			return;
		}
		JPanel compTypePanel = new JPanel();
		JPanel cpTypPanel = new JPanel(new GridLayout(2, 2));
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		compTypeID = new JTextField(12);
		compTypeName = new JTextField(12);
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				CompartmentType compType = document.getModel().getCompartmentType(
						(((String) compTypes.getSelectedValue()).split(" ")[0]));
				compTypeID.setText(compType.getId());
				selectedID = compType.getId();
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(compTypeID.getText().trim(), selectedID, false);
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
							usedIDs.set(i, compTypeID.getText().trim());
						}
					}
					cpTyp[index] = compTypeID.getText().trim();
					sort(cpTyp);
					compTypes.setListData(cpTyp);
					compTypes.setSelectedIndex(index);
					for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
						Compartment compartment = document.getModel().getCompartment(i);
						if (compartment.getCompartmentType().equals(val)) {
							compartment.setCompartmentType(compTypeID.getText().trim());
						}
					}
					int index1 = compartments.getSelectedIndex();
					compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					comps = Buttons.getList(comps, compartments);
					compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					for (int i = 0; i < comps.length; i++) {
						if (comps[i].split(" ")[1].equals(val)) {
							comps[i] = comps[i].split(" ")[0] + " " + compTypeID.getText().trim() + " "
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
					usedIDs.add(compTypeID.getText().trim());
					JList add = new JList();
					Object[] adding = { compTypeID.getText().trim() };
					add.setListData(adding);
					add.setSelectedIndex(0);
					compTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Buttons.add(cpTyp, compTypes, add, false, null, null, null, null, null, null,
							biosim.frame());
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
						"Compartment Type Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit species types or create new ones.
	 */
	private void specTypeEditor(String option) {
		if (option.equals("OK") && specTypes.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No species type selected.",
					"Must Select a Species Type", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel specTypePanel = new JPanel();
		JPanel spTypPanel = new JPanel(new GridLayout(2, 2));
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		specTypeID = new JTextField(12);
		specTypeName = new JTextField(12);
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				SpeciesType specType = document.getModel().getSpeciesType(
						(((String) specTypes.getSelectedValue()).split(" ")[0]));
				specTypeID.setText(specType.getId());
				selectedID = specType.getId();
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
		int value = JOptionPane.showOptionDialog(biosim.frame(), specTypePanel, "Species Type Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(specTypeID.getText().trim(), selectedID, false);
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
							usedIDs.set(i, specTypeID.getText().trim());
						}
					}
					spTyp[index] = specTypeID.getText().trim();
					sort(spTyp);
					specTypes.setListData(spTyp);
					specTypes.setSelectedIndex(index);
					for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
						Species species = document.getModel().getSpecies(i);
						if (species.getSpeciesType().equals(val)) {
							species.setSpeciesType(specTypeID.getText().trim());
						}
					}
					int index1 = species.getSelectedIndex();
					species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					specs = Buttons.getList(specs, species);
					species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					for (int i = 0; i < specs.length; i++) {
						if (specs[i].split(" ")[1].equals(val)) {
							specs[i] = specs[i].split(" ")[0] + " " + specTypeID.getText().trim() + " "
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
					usedIDs.add(specTypeID.getText().trim());
					JList add = new JList();
					Object[] adding = { specTypeID.getText().trim() };
					add.setListData(adding);
					add.setSelectedIndex(0);
					specTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Buttons.add(spTyp, specTypes, add, false, null, null, null, null, null, null,
							biosim.frame());
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
				value = JOptionPane.showOptionDialog(biosim.frame(), specTypePanel, "Species Type Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit initial assignments or create new ones.
	 */
	private void initEditor(String option) {
		if (option.equals("OK") && initAssigns.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No initial assignment selected.",
					"Must Select an Initial Assignment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel initAssignPanel = new JPanel();
		JPanel initPanel = new JPanel();
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
		initMath = new JTextField(30);
		int Rindex = -1;
		if (option.equals("OK")) {
			initVar.setSelectedItem(selected.split(" ")[0]);
			initMath.setText(selected.substring(selected.indexOf('=') + 2));
			ListOf r = document.getModel().getListOfInitialAssignments();
			for (int i = 0; i < document.getModel().getNumInitialAssignments(); i++) {
				if (myFormulaToString(((InitialAssignment) r.get(i)).getMath()).equals(initMath.getText())
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
			else if (myParseFormula(initMath.getText().trim()) == null) {
				JOptionPane.showMessageDialog(biosim.frame(), "Initial assignment is not valid.",
						"Enter Valid Assignment", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else {
				ArrayList<String> invalidVars = getInvalidVariables(initMath.getText().trim(), false, "",
						false);
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
					message = "Rule contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(300, 300));
					scrolls.setPreferredSize(new Dimension(300, 300));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Unknown Variables",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (!error) {
					error = checkNumFunctionArguments(myParseFormula(initMath.getText().trim()));
				}
				if (!error) {
					if (myParseFormula(initMath.getText().trim()).isBoolean()) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"Initial assignment must evaluate to a number.", "Number Expected",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = initAssigns.getSelectedIndex();
					initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					inits = Buttons.getList(inits, initAssigns);
					initAssigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					InitialAssignment r = (InitialAssignment) (document.getModel()
							.getListOfInitialAssignments()).get(Rindex);
					String oldSymbol = r.getSymbol();
					String oldInit = myFormulaToString(r.getMath());
					String oldVal = inits[index];
					r.setSymbol(addVar);
					r.setMath(myParseFormula(initMath.getText().trim()));
					inits[index] = addVar + " = " + myFormulaToString(r.getMath());
					error = checkInitialAssignmentUnits(r);
					if (!error) {
						try {
							inits = sortInitRules(inits);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"Cycle detected in initial assignments.", "Cycle Detected",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
					if (!error && checkCycles(inits, rul)) {
						JOptionPane.showMessageDialog(biosim.frame(),
					      "Cycle detected within initial assignments, assignment rules, and rate laws.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (error) {
						r.setSymbol(oldSymbol);
						r.setMath(myParseFormula(oldInit));
						inits[index] = oldVal;
					}
					initAssigns.setListData(inits);
					initAssigns.setSelectedIndex(index);
				}
				else {
					JList add = new JList();
					int index = rules.getSelectedIndex();
					String addStr;
					InitialAssignment r = document.getModel().createInitialAssignment();
					r.setSymbol(addVar);
					r.setMath(myParseFormula(initMath.getText().trim()));
					addStr = addVar + " = " + myFormulaToString(r.getMath());
					Object[] adding = { addStr };
					add.setListData(adding);
					add.setSelectedIndex(0);
					initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Buttons.add(inits, initAssigns, add, false, null, null, null, null, null, null,
							biosim.frame());
					String[] oldInits = inits;
					inits = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						inits[i] = (String) adding[i];
					}
					error = checkInitialAssignmentUnits(r);
					if (!error) {
						try {
							inits = sortInitRules(inits);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"Cycle detected in initial assignments.", "Cycle Detected",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
					if (!error && checkCycles(inits, rul)) {
						JOptionPane.showMessageDialog(biosim.frame(),
							"Cycle detected within initial assignments, assignment rules, and rate laws.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (error) {
						inits = oldInits;
						ListOf ia = document.getModel().getListOfInitialAssignments();
						for (int i = 0; i < document.getModel().getNumInitialAssignments(); i++) {
							if (myFormulaToString(((InitialAssignment) ia.get(i)).getMath()).equals(
									myFormulaToString(r.getMath()))
									&& ((InitialAssignment) ia.get(i)).getSymbol().equals(r.getSymbol())) {
								ia.remove(i);
							}
						}
					}
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
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Determines if a variable is already in an initial or assignment rule
	 */
	private boolean keepVar(String selected, String id, boolean checkInit, boolean checkRate,
			boolean checkEventAssign, boolean checkOnlyCurEvent) {
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
		if (option.equals("OK") && rules.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No rule selected.", "Must Select a Rule",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel rulePanel = new JPanel();
		JPanel rulPanel = new JPanel();
		JLabel typeLabel = new JLabel("Type:");
		JLabel varLabel = new JLabel("Variable:");
		JLabel ruleLabel = new JLabel("Rule:");
		String[] list = { "Algebraic", "Assignment", "Rate" };
		ruleType = new JComboBox(list);
		ruleVar = new JComboBox();
		ruleMath = new JTextField(30);
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
							&& (myFormulaToString(((Rule) r.get(i)).getMath()).equals(ruleMath.getText()))) {
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
							&& ((Rule) r.get(i)).getVariable().equals(ruleVar.getSelectedItem())) {
						Rindex = i;
					}
				}
			}
		}
		else {
			if (!assignRuleVar("") && !rateRuleVar("")) {
				String[] list1 = { "Algebraic" };
				ruleType = new JComboBox(list1);
			}
			else if (!assignRuleVar("")) {
				String[] list1 = { "Algebraic", "Rate" };
				ruleType = new JComboBox(list1);
			}
			else if (!rateRuleVar("")) {
				String[] list1 = { "Algebraic", "Assignment" };
				ruleType = new JComboBox(list1);
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
			else if (myParseFormula(ruleMath.getText().trim()) == null) {
				JOptionPane.showMessageDialog(biosim.frame(), "Rule formula is not valid.",
						"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else {
				ArrayList<String> invalidVars = getInvalidVariables(ruleMath.getText().trim(), false, "",
						false);
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
					message = "Rule contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(300, 300));
					scrolls.setPreferredSize(new Dimension(300, 300));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Unknown Variables",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (!error) {
					error = checkNumFunctionArguments(myParseFormula(ruleMath.getText().trim()));
				}
				if (!error) {
					if (myParseFormula(ruleMath.getText().trim()).isBoolean()) {
						JOptionPane.showMessageDialog(biosim.frame(), "Rule must evaluate to a number.",
								"Number Expected", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = rules.getSelectedIndex();
					rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					rul = Buttons.getList(rul, rules);
					rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					Rule r = (Rule) (document.getModel().getListOfRules()).get(Rindex);
					String addStr;
					String oldVar = "";
					String oldMath = myFormulaToString(r.getMath());
					if (ruleType.getSelectedItem().equals("Algebraic")) {
						r.setMath(myParseFormula(ruleMath.getText().trim()));
						addStr = "0 = " + myFormulaToString(r.getMath());
						checkOverDetermined();
					}
					else if (ruleType.getSelectedItem().equals("Rate")) {
						oldVar = r.getVariable();
						r.setVariable(addVar);
						r.setMath(myParseFormula(ruleMath.getText().trim()));
						error = checkRateRuleUnits(r);
						addStr = "d( " + addVar + " )/dt = " + myFormulaToString(r.getMath());
					}
					else {
						oldVar = r.getVariable();
						r.setVariable(addVar);
						r.setMath(myParseFormula(ruleMath.getText().trim()));
						error = checkAssignmentRuleUnits(r);
						addStr = addVar + " = " + myFormulaToString(r.getMath());
					}
					String oldVal = rul[index];
					rul[index] = addStr;
					if (!error) {
						try {
							rul = sortRules(rul);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in assignments.",
									"Cycle Detected", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
					if (!error && checkCycles(inits, rul)) {
						JOptionPane.showMessageDialog(biosim.frame(),
							"Cycle detected within initial assignments, assignment rules, and rate laws.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (error) {
						if (!oldVar.equals("")) {
							r.setVariable(oldVar);
						}
						r.setMath(myParseFormula(oldMath));
						rul[index] = oldVal;
					}
					updateRules();
					rules.setListData(rul);
					rules.setSelectedIndex(index);
				}
				else {
					JList add = new JList();
					int index = rules.getSelectedIndex();
					String addStr;
					if (ruleType.getSelectedItem().equals("Algebraic")) {
						addStr = "0 = " + myFormulaToString(myParseFormula(ruleMath.getText().trim()));
					}
					else if (ruleType.getSelectedItem().equals("Rate")) {
						addStr = "d( " + addVar + " )/dt = "
								+ myFormulaToString(myParseFormula(ruleMath.getText().trim()));
					}
					else {
						addStr = addVar + " = " + myFormulaToString(myParseFormula(ruleMath.getText().trim()));
					}
					Object[] adding = { addStr };
					add.setListData(adding);
					add.setSelectedIndex(0);
					rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Buttons.add(rul, rules, add, false, null, null, null, null, null, null, biosim
							.frame());
					String[] oldRul = rul;
					rul = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						rul[i] = (String) adding[i];
					}
					try {
						rul = sortRules(rul);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in assignments.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
						rul = oldRul;
					}
					if (!error && checkCycles(inits, rul)) {
						JOptionPane.showMessageDialog(biosim.frame(),
							"Cycle detected within initial assignments, assignment rules, and rate laws.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
						rul = oldRul;
					}
					if (!error) {
						if (ruleType.getSelectedItem().equals("Algebraic")) {
							AlgebraicRule r = document.getModel().createAlgebraicRule();
							r.setMath(myParseFormula(ruleMath.getText().trim()));
							checkOverDetermined();
						}
						else if (ruleType.getSelectedItem().equals("Rate")) {
							RateRule r = document.getModel().createRateRule();
							r.setVariable(addVar);
							r.setMath(myParseFormula(ruleMath.getText().trim()));
							error = checkRateRuleUnits(r);
						}
						else {
							AssignmentRule r = document.getModel().createAssignmentRule();
							r.setVariable(addVar);
							r.setMath(myParseFormula(ruleMath.getText().trim()));
							error = checkAssignmentRuleUnits(r);
						}
					}
					if (error) {
						rul = oldRul;
						removeTheRule(addStr);
					}
					updateRules();
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

	/**
	 * Update rules
	 */
	private void updateRules() {
		ListOf r = document.getModel().getListOfRules();
		while (document.getModel().getNumRules() > 0) {
			r.remove(0);
		}
		for (int i = 0; i < rul.length; i++) {
			if (rul[i].split(" ")[0].equals("0")) {
				AlgebraicRule rule = document.getModel().createAlgebraicRule();
				rule.setMath(myParseFormula(rul[i].substring(rul[i].indexOf("=") + 1)));
			}
			else if (rul[i].split(" ")[0].equals("d(")) {
				RateRule rule = document.getModel().createRateRule();
				rule.setVariable(rul[i].split(" ")[1]);
				rule.setMath(myParseFormula(rul[i].substring(rul[i].indexOf("=") + 1)));
			}
			else {
				AssignmentRule rule = document.getModel().createAssignmentRule();
				rule.setVariable(rul[i].split(" ")[0]);
				rule.setMath(myParseFormula(rul[i].substring(rul[i].indexOf("=") + 1)));
			}
		}
	}

	/**
	 * Sort rules in order to be evaluated
	 */
	private String[] sortRules(String[] rules) {
		String[] result = new String[rules.length];
		int j = 0;
		boolean[] used = new boolean[rules.length];
		for (int i = 0; i < rules.length; i++) {
			used[i] = false;
		}
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].split(" ")[0].equals("0")) {
				result[j] = rules[i];
				used[i] = true;
				j++;
			}
		}
		boolean progress;
		do {
			progress = false;
			for (int i = 0; i < rules.length; i++) {
				if (used[i] || (rules[i].split(" ")[0].equals("0"))
						|| (rules[i].split(" ")[0].equals("d(")))
					continue;
				String[] rule = rules[i].split(" ");
				boolean insert = true;
				for (int k = 1; k < rule.length; k++) {
					for (int l = 0; l < rules.length; l++) {
						if (used[l] || (rules[l].split(" ")[0].equals("0"))
								|| (rules[l].split(" ")[0].equals("d(")))
							continue;
						String[] rule2 = rules[l].split(" ");
						if (rule[k].equals(rule2[0])) {
							insert = false;
							break;
						}
					}
					if (!insert)
						break;
				}
				if (insert) {
					result[j] = rules[i];
					j++;
					progress = true;
					used[i] = true;
				}
			}
		}
		while ((progress) && (j < rules.length));
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].split(" ")[0].equals("d(")) {
				result[j] = rules[i];
				j++;
			}
		}
		if (j != rules.length) {
			throw new RuntimeException();
		}
		return result;
	}

	/**
	 * Sort initial rules in order to be evaluated
	 */
	private String[] sortInitRules(String[] initRules) {
		String[] result = new String[initRules.length];
		int j = 0;
		boolean[] used = new boolean[initRules.length];
		for (int i = 0; i < initRules.length; i++) {
			used[i] = false;
		}
		boolean progress;
		do {
			progress = false;
			for (int i = 0; i < initRules.length; i++) {
				if (used[i])
					continue;
				String[] initRule = initRules[i].split(" ");
				boolean insert = true;
				for (int k = 1; k < initRule.length; k++) {
					for (int l = 0; l < initRules.length; l++) {
						if (used[l])
							continue;
						String[] initRule2 = initRules[l].split(" ");
						if (initRule[k].equals(initRule2[0])) {
							insert = false;
							break;
						}
					}
					if (!insert)
						break;
				}
				if (insert) {
					result[j] = initRules[i];
					j++;
					progress = true;
					used[i] = true;
				}
			}
		}
		while ((progress) && (j < initRules.length));
		if (j != initRules.length) {
			throw new RuntimeException();
		}
		return result;
	}

	/**
	 * Check for cycles in initialAssignments and assignmentRules
	 */
	private boolean checkCycles(String[] initRules, String[] rules) {
		Model model = document.getModel();
		ListOf listOfReactions = model.getListOfReactions();
		String[] rateLaws = new String[(int)model.getNumReactions()];
		for (int i = 0; i < model.getNumReactions(); i++) {
		    Reaction reaction = (Reaction) listOfReactions.get(i);
		    rateLaws[i] = reaction.getId() + " = " + reaction.getKineticLaw().getFormula();
		}
		String[] result = new String[rules.length + initRules.length + rateLaws.length];
		int j = 0;
		boolean[] used = new boolean[rules.length + initRules.length + rateLaws.length];
		for (int i = 0; i < rules.length + initRules.length + rateLaws.length; i++) {
			used[i] = false;
		}
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].split(" ")[0].equals("0")) {
				result[j] = rules[i];
				used[i] = true;
				j++;
			}
		}
		boolean progress;
		do {
			progress = false;
			for (int i = 0; i < rules.length + initRules.length + rateLaws.length; i++) {
				String[] rule;
				if (i < rules.length) {
					if (used[i] || (rules[i].split(" ")[0].equals("0"))
					    || (rules[i].split(" ")[0].equals("d(")))
					    continue;
					rule = rules[i].split(" ");
				}
				else if (i < rules.length + initRules.length) {
					if (used[i]) continue;
					rule = initRules[i - rules.length].split(" ");
				} 
				else {
					if (used[i]) continue;
					rule = rateLaws[i - (rules.length + initRules.length)].split(" ");
				}
				boolean insert = true;
				for (int k = 1; k < rule.length; k++) {
					for (int l = 0; l < rules.length + initRules.length + rateLaws.length; l++) {
					        String rule2;
						if (l < rules.length) {
							if (used[l] || (rules[l].split(" ")[0].equals("0"))
							    || (rules[l].split(" ")[0].equals("d(")))
							    continue;
							rule2 = rules[l].split(" ")[0];
						}
						else if (l < rules.length + initRules.length) {
							if (used[l]) continue;
							rule2 = initRules[l - rules.length].split(" ")[0];
						}
						else {
							if (used[l]) continue;
							rule2 = rateLaws[l - (rules.length + initRules.length)].split(" ")[0];
						}
						if (rule[k].equals(rule2)) {
							insert = false;
							break;
						}
					}
					if (!insert)
						break;
				}
				if (insert) {
					if (i < rules.length) {
						result[j] = rules[i];
					}
					else if (i < rules.length + initRules.length) {
						result[j] = initRules[i - rules.length];
					}
					else {
					    result[j] = rateLaws[i - (rules.length + initRules.length)];
					}
					j++;
					progress = true;
					used[i] = true;
				}
			}
		}
		while ((progress) && (j < rules.length + initRules.length + rateLaws.length));
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].split(" ")[0].equals("d(")) {
				result[j] = rules[i];
				j++;
			}
		}
		if (j != rules.length + initRules.length + rateLaws.length) {
			return true;
		}
		return false;
	}

	/**
	 * Create check if species used in reaction
	 */
	private boolean usedInReaction(String id) {
		for (int i = 0; i < document.getModel().getNumReactions(); i++) {
			for (int j = 0; j < document.getModel().getReaction(i).getNumReactants(); j++) {
				if (document.getModel().getReaction(i).getReactant(j).getSpecies().equals(id)) {
					return true;
				}
			}
			for (int j = 0; j < document.getModel().getReaction(i).getNumProducts(); j++) {
				if (document.getModel().getReaction(i).getProduct(j).getSpecies().equals(id)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Create comboBox for assignments rules
	 */
	private boolean assignRuleVar(String selected) {
		boolean assignOK = false;
		ruleVar.removeAllItems();
		Model model = document.getModel();
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			String id = ((Compartment) ids.get(i)).getId();
			if (!((Compartment) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, true, true, true, false)) {
					ruleVar.addItem(((Compartment) ids.get(i)).getId());
					assignOK = true;
				}
			}
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < model.getNumParameters(); i++) {
			String id = ((Parameter) ids.get(i)).getId();
			if (!((Parameter) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, true, true, true, false)) {
					ruleVar.addItem(((Parameter) ids.get(i)).getId());
					assignOK = true;
				}
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			String id = ((Species) ids.get(i)).getId();
			if (!((Species) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, true, true, true, false))
					if (((Species) ids.get(i)).getBoundaryCondition() || !usedInReaction(id)) {
						ruleVar.addItem(((Species) ids.get(i)).getId());
						assignOK = true;
					}
			}
		}
		return assignOK;
	}

	/**
	 * Create comboBox for rate rules
	 */
	private boolean rateRuleVar(String selected) {
		boolean rateOK = false;
		ruleVar.removeAllItems();
		Model model = document.getModel();
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			String id = ((Compartment) ids.get(i)).getId();
			if (!((Compartment) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, false, true, false, false)) {
					ruleVar.addItem(((Compartment) ids.get(i)).getId());
					rateOK = true;
				}
			}
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < model.getNumParameters(); i++) {
			String id = ((Parameter) ids.get(i)).getId();
			if (!((Parameter) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, false, true, false, false)) {
					ruleVar.addItem(((Parameter) ids.get(i)).getId());
					rateOK = true;
				}
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			String id = ((Species) ids.get(i)).getId();
			if (!((Species) ids.get(i)).getConstant()) {
				if (keepVar(selected, id, false, true, false, false))
					if (((Species) ids.get(i)).getBoundaryCondition() || !usedInReaction(id)) {
						ruleVar.addItem(((Species) ids.get(i)).getId());
						rateOK = true;
					}
			}
		}
		return rateOK;
	}

	/**
	 * Creates a frame used to edit events or create new ones.
	 */
	private void eventEditor(String option) {
		if (option.equals("OK") && events.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No event selected.", "Must Select an Event",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		int index = events.getSelectedIndex();
		JPanel eventPanel = new JPanel(new BorderLayout());
		// JPanel evPanel = new JPanel(new GridLayout(2, 2));
		JPanel evPanel = new JPanel(new GridLayout(4, 2));
		JLabel IDLabel = new JLabel("ID:");
		JLabel NameLabel = new JLabel("Name:");
		JLabel triggerLabel = new JLabel("Trigger:");
		JLabel delayLabel = new JLabel("Delay:");
		eventID = new JTextField(12);
		eventName = new JTextField(12);
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
		String selectedID = "";
		if (option.equals("OK")) {
			String selected = ((String) events.getSelectedValue());
			ListOf e = document.getModel().getListOfEvents();
			for (int i = 0; i < document.getModel().getNumEvents(); i++) {
				org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) e.get(i);
				if (event.getId().equals(selected)) {
					Eindex = i;
					eventID.setText(event.getId());
					selectedID = event.getId();
					eventName.setText(event.getName());
					eventTrigger.setText(myFormulaToString(event.getTrigger().getMath()));
					if (event.isSetDelay()) {
						eventDelay.setText(myFormulaToString(event.getDelay().getMath()));
					}
					assign = new String[(int) event.getNumEventAssignments()];
					origAssign = new String[(int) event.getNumEventAssignments()];
					for (int j = 0; j < event.getNumEventAssignments(); j++) {
						assign[j] = event.getEventAssignment(j).getVariable() + " = "
								+ myFormulaToString(event.getEventAssignment(j).getMath());
						origAssign[j] = event.getEventAssignment(j).getVariable() + " = "
								+ myFormulaToString(event.getEventAssignment(j).getMath());
					}
				}
			}
		}
		else {
			String eventId = "event0";
			int en = 0;
			while (usedIDs.contains(eventId)) {
				en++;
				eventId = "event" + en;
			}
			eventID.setText(eventId);
		}
		sort(assign);
		eventAssign.setListData(assign);
		eventAssign.setSelectedIndex(0);
		eventAssign.addMouseListener(this);
		eventAssignPanel.add(eventAssignLabel, "North");
		eventAssignPanel.add(scroll, "Center");
		eventAssignPanel.add(addEventAssign, "South");
		evPanel.add(IDLabel);
		evPanel.add(eventID);
		evPanel.add(NameLabel);
		evPanel.add(eventName);
		evPanel.add(triggerLabel);
		evPanel.add(eventTrigger);
		evPanel.add(delayLabel);
		evPanel.add(eventDelay);
		eventPanel.add(evPanel, "North");
		eventPanel.add(eventAssignPanel, "South");
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(biosim.frame(), eventPanel, "Event Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(eventID.getText().trim(), selectedID, false);
			if (eventTrigger.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(biosim.frame(), "Event must have a trigger formula.",
						"Enter Trigger Formula", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else if (myParseFormula(eventTrigger.getText().trim()) == null) {
				JOptionPane.showMessageDialog(biosim.frame(), "Trigger formula is not valid.",
						"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else if (!myParseFormula(eventTrigger.getText().trim()).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(), "Trigger formula must be of type Boolean.",
						"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else if (!eventDelay.getText().trim().equals("")
					&& myParseFormula(eventDelay.getText().trim()) == null) {
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
			else {
				ArrayList<String> invalidVars = getInvalidVariables(eventTrigger.getText().trim(), false,
						"", false);
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
					message = "Event trigger contains unknown variables.\n\n" + "Unknown variables:\n"
							+ invalid;
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(300, 300));
					scrolls.setPreferredSize(new Dimension(300, 300));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Unknown Variables",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else {
					invalidVars = getInvalidVariables(eventDelay.getText().trim(), false, "", false);
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
						message = "Event delay contains unknown variables.\n\n" + "Unknown variables:\n"
								+ invalid;
						JTextArea messageArea = new JTextArea(message);
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(300, 300));
						scrolls.setPreferredSize(new Dimension(300, 300));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Unknown Variables",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error) {
					error = checkNumFunctionArguments(myParseFormula(eventTrigger.getText().trim()));
				}
				if ((!error) && (!eventDelay.getText().trim().equals(""))) {
					error = checkNumFunctionArguments(myParseFormula(eventDelay.getText().trim()));
					if (!error) {
						if (myParseFormula(eventDelay.getText().trim()).isBoolean()) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"Event delay must evaluate to a number.", "Number Expected",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					ev = Buttons.getList(ev, events);
					events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					org.sbml.libsbml.Event e = (org.sbml.libsbml.Event) (document.getModel()
							.getListOfEvents()).get(Eindex);
					while (e.getNumEventAssignments() > 0) {
						e.getListOfEventAssignments().remove(0);
					}
					for (int i = 0; i < assign.length; i++) {
						EventAssignment ea = e.createEventAssignment();
						ea.setVariable(assign[i].split(" ")[0]);
						ea.setMath(myParseFormula(assign[i].split("=")[1].trim()));
						error = checkEventAssignmentUnits(ea);
						if (error)
							break;
					}
					if (!error) {
						if (eventDelay.getText().trim().equals("")) {
							e.unsetDelay();
						}
						else {
							String oldDelayStr = "";
							if (e.isSetDelay()) {
								oldDelayStr = myFormulaToString(e.getDelay().getMath());
							}
							Delay delay = new Delay(myParseFormula(eventDelay.getText().trim()));
							delay.setSBMLDocument(document);
							e.setDelay(delay);
							error = checkEventDelayUnits(e.getDelay());
							if (error) {
								if (oldDelayStr.equals("")) {
									e.unsetDelay();
								}
								else {
									Delay oldDelay = new Delay(myParseFormula(oldDelayStr));
									oldDelay.setSBMLDocument(document);
									e.setDelay(oldDelay);
								}
							}
						}
					}
					if (!error) {
						Trigger trigger = new Trigger(myParseFormula(eventTrigger.getText().trim()));
						e.setTrigger(trigger);
						if (eventID.getText().trim().equals("")) {
							e.unsetId();
						}
						else {
							e.setId(eventID.getText().trim());
						}
						if (eventName.getText().trim().equals("")) {
							e.unsetName();
						}
						else {
							e.setName(eventName.getText().trim());
						}
						for (int i = 0; i < usedIDs.size(); i++) {
							if (usedIDs.get(i).equals(selectedID)) {
								usedIDs.set(i, eventID.getText().trim());
							}
						}
						ev[index] = e.getId();
						sort(ev);
						events.setListData(ev);
						events.setSelectedIndex(index);
					}
					else {
						while (e.getNumEventAssignments() > 0) {
							e.getListOfEventAssignments().remove(0);
						}
						for (int i = 0; i < origAssign.length; i++) {
							EventAssignment ea = e.createEventAssignment();
							ea.setVariable(origAssign[i].split(" ")[0]);
							ea.setMath(myParseFormula(origAssign[i].split("=")[1].trim()));
						}
					}
				}
				else {
					JList add = new JList();
					org.sbml.libsbml.Event e = document.getModel().createEvent();
					Trigger trigger = new Trigger(myParseFormula(eventTrigger.getText().trim()));
					e.setTrigger(trigger);
					if (!eventDelay.getText().trim().equals("")) {
						Delay delay = new Delay(myParseFormula(eventDelay.getText().trim()));
						delay.setSBMLDocument(document);
						e.setDelay(delay);
						error = checkEventDelayUnits(e.getDelay());
					}
					if (!error) {
						for (int i = 0; i < assign.length; i++) {
							EventAssignment ea = e.createEventAssignment();
							ea.setVariable(assign[i].split(" ")[0]);
							ea.setMath(myParseFormula(assign[i].split("=")[1].trim()));
							error = checkEventAssignmentUnits(ea);
							if (error)
								break;
						}
					}
					if (!eventID.getText().trim().equals("")) {
						e.setId(eventID.getText().trim());
					}
					if (!eventName.getText().trim().equals("")) {
						e.setName(eventName.getText().trim());
					}
					Object[] adding = { e.getId() };
					// Object[] adding = { myFormulaToString(e.getTrigger().getMath()) };
					add.setListData(adding);
					add.setSelectedIndex(0);
					events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Buttons.add(ev, events, add, false, null, null, null, null, null, null, biosim
							.frame());
					usedIDs.add(eventID.getText().trim());
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
					if (error) {
						removeTheEvent(myFormulaToString(e.getTrigger().getMath()));
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

	/**
	 * Creates a frame used to edit event assignments or create new ones.
	 */
	private void eventAssignEditor(String option) {
		if (option.equals("OK") && eventAssign.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No event assignment selected.",
					"Must Select an Event Assignment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel eventAssignPanel = new JPanel();
		JPanel EAPanel = new JPanel();
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
		eqn = new JTextField(30);
		if (option.equals("OK")) {
			String selectAssign = ((String) eventAssign.getSelectedValue());
			eaID.setSelectedItem(selectAssign.split(" ")[0]);
			eqn.setText(selectAssign.split("=")[1].trim());
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if (eqn.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(biosim.frame(), "Event assignment is missing.",
						"Enter Assignment", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else if (myParseFormula(eqn.getText().trim()) == null) {
				JOptionPane.showMessageDialog(biosim.frame(), "Event assignment is not valid.",
						"Enter Valid Assignment", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else {
				ArrayList<String> invalidVars = getInvalidVariables(eqn.getText().trim(), false, "", false);
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
					message = "Event assignment contains unknown variables.\n\n" + "Unknown variables:\n"
							+ invalid;
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(300, 300));
					scrolls.setPreferredSize(new Dimension(300, 300));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Unknown Variables",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (!error) {
					error = checkNumFunctionArguments(myParseFormula(eqn.getText().trim()));
				}
				if (!error) {
					if (myParseFormula(eqn.getText().trim()).isBoolean()) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"Event assignment must evaluate to a number.", "Number Expected",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
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
					adding = Buttons.add(assign, eventAssign, add, false, null, null, null, null, null, null,
							biosim.frame());
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
						"Event Assignment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit constraints or create new ones.
	 */
	private void constraintEditor(String option) {
		if (option.equals("OK") && constraints.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No constraint selected.",
					"Must Select a Constraint", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel constraintPanel = new JPanel();
		JPanel consPanel = new JPanel();
		JLabel IDLabel = new JLabel("ID:");
		JLabel mathLabel = new JLabel("Constraint:");
		JLabel messageLabel = new JLabel("Messsage:");
		consID = new JTextField(12);
		consMath = new JTextField(30);
		consMessage = new JTextField(30);
		String selectedID = "";
		int Cindex = -1;
		if (option.equals("OK")) {
			String selected = ((String) constraints.getSelectedValue());
			ListOf c = document.getModel().getListOfConstraints();
			for (int i = 0; i < document.getModel().getNumConstraints(); i++) {
				if ((((Constraint) c.get(i)).getMetaId()).equals(selected)) {
					Cindex = i;
					consMath.setText(myFormulaToString(((Constraint) c.get(i)).getMath()));
					if (((Constraint) c.get(i)).isSetMetaId()) {
						selectedID = ((Constraint) c.get(i)).getMetaId();
						consID.setText(selectedID);
					}
					if (((Constraint) c.get(i)).isSetMessage()) {
						String message = XMLNode.convertXMLNodeToString(((Constraint) c.get(i)).getMessage());
						message = message.substring(message.indexOf("xhtml\">") + 7, message.indexOf("</p>"));
						consMessage.setText(message);
					}
				}
			}
		}
		else {
			String constraintId = "constraint0";
			int cn = 0;
			while (usedIDs.contains(constraintId)) {
				cn++;
				constraintId = "constraint" + cn;
			}
			consID.setText(constraintId);
		}
		consPanel.add(IDLabel);
		consPanel.add(consID);
		consPanel.add(mathLabel);
		consPanel.add(consMath);
		consPanel.add(messageLabel);
		consPanel.add(consMessage);
		constraintPanel.add(consPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(biosim.frame(), constraintPanel, "Constraint Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(consID.getText().trim(), selectedID, false);
			if (!error) {
				if (consMath.getText().trim().equals("")
						|| myParseFormula(consMath.getText().trim()) == null) {
					JOptionPane.showMessageDialog(biosim.frame(), "Formula is not valid.",
							"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!myParseFormula(consMath.getText().trim()).isBoolean()) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Constraint formula must be of type Boolean.", "Enter Valid Formula",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else {
					ArrayList<String> invalidVars = getInvalidVariables(consMath.getText().trim(), false, "",
							false);
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
						message = "Constraint contains unknown variables.\n\n" + "Unknown variables:\n"
								+ invalid;
						JTextArea messageArea = new JTextArea(message);
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(300, 300));
						scrolls.setPreferredSize(new Dimension(300, 300));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Unknown Variables",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error) {
					if (option.equals("OK")) {
						int index = constraints.getSelectedIndex();
						constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						cons = Buttons.getList(cons, constraints);
						constraints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						Constraint c = (Constraint) (document.getModel().getListOfConstraints()).get(Cindex);
						c.setMath(myParseFormula(consMath.getText().trim()));
						c.setMetaId(consID.getText().trim());
						if (!consMessage.getText().trim().equals("")) {
							XMLNode xmlNode = XMLNode
									.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
											+ consMessage.getText().trim() + "</p></message>");
							c.setMessage(xmlNode);
						}
						else {
							c.unsetMessage();
						}
						for (int i = 0; i < usedIDs.size(); i++) {
							if (usedIDs.get(i).equals(selectedID)) {
								usedIDs.set(i, consID.getText().trim());
							}
						}
						cons[index] = c.getMetaId();
						sort(cons);
						constraints.setListData(cons);
						constraints.setSelectedIndex(index);
					}
					else {
						JList add = new JList();
						int index = rules.getSelectedIndex();
						Constraint c = document.getModel().createConstraint();
						c.setMath(myParseFormula(consMath.getText().trim()));
						c.setMetaId(consID.getText().trim());
						if (!consMessage.getText().trim().equals("")) {
							XMLNode xmlNode = XMLNode
									.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
											+ consMessage.getText().trim() + "</p></message>");
							c.setMessage(xmlNode);
						}
						usedIDs.add(consID.getText().trim());
						Object[] adding = { c.getMetaId() };
						add.setListData(adding);
						add.setSelectedIndex(0);
						constraints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Buttons.add(cons, constraints, add, false, null, null, null, null, null, null,
								biosim.frame());
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
			}
			if (error) {
				value = JOptionPane.showOptionDialog(biosim.frame(), constraintPanel, "Constraint Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit compartments or create new ones.
	 */
	private void compartEditor(String option) {
		if (option.equals("OK") && compartments.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No compartment selected.",
					"Must Select a Compartment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel compartPanel = new JPanel();
		JPanel compPanel;
		if (paramsOnly) {
			compPanel = new JPanel(new GridLayout(13, 2));
		}
		else {
			compPanel = new JPanel(new GridLayout(8, 2));
		}
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel compTypeLabel = new JLabel("Type:");
		JLabel dimLabel = new JLabel("Dimensions:");
		JLabel outsideLabel = new JLabel("Outside:");
		JLabel constLabel = new JLabel("Constant:");
		JLabel sizeLabel = new JLabel("Size:");
		JLabel compUnitsLabel = new JLabel("Units:");
		JLabel startLabel = new JLabel("Start:");
		JLabel stopLabel = new JLabel("Stop:");
		JLabel stepLabel = new JLabel("Step:");
		JLabel levelLabel = new JLabel("Level:");
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
		compSize.setText("1.0");
		compUnits = new JComboBox();
		compOutside = new JComboBox();
		String[] optionsTF = { "true", "false" };
		compConstant = new JComboBox(optionsTF);
		compConstant.setSelectedItem("true");
		String selected = "";
		editComp = false;
		String selectedID = "";
		if (option.equals("OK")) {
			selected = ((String) compartments.getSelectedValue()).split(" ")[0];
			editComp = true;
		}
		setCompartOptions(selected, "3");
		dimBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (editComp) {
					setCompartOptions(((String) compartments.getSelectedValue()).split(" ")[0],
							(String) dimBox.getSelectedItem());
				}
				else {
					setCompartOptions("", (String) dimBox.getSelectedItem());
				}
			}
		});
		String[] list = { "Original", "Custom", "Sweep" };
		String[] list1 = { "1", "2" };
		final JComboBox type = new JComboBox(list);
		final JTextField start = new JTextField();
		final JTextField stop = new JTextField();
		final JTextField step = new JTextField();
		final JComboBox level = new JComboBox(list1);
		if (paramsOnly) {
			compID.setEnabled(false);
			compName.setEnabled(false);
			compTypeBox.setEnabled(false);
			dimBox.setEnabled(false);
			compUnits.setEnabled(false);
			compOutside.setEnabled(false);
			compConstant.setEnabled(false);
			compSize.setEnabled(false);
			start.setEnabled(false);
			stop.setEnabled(false);
			step.setEnabled(false);
			level.setEnabled(false);
		}
		if (option.equals("OK")) {
			try {
				Compartment compartment = document.getModel().getCompartment(
						(((String) compartments.getSelectedValue()).split(" ")[0]));
				compID.setText(compartment.getId());
				selectedID = compartment.getId();
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
				if (paramsOnly
						&& (((String) compartments.getSelectedValue()).contains("Custom") || ((String) compartments
								.getSelectedValue()).contains("Sweep"))) {
					if (((String) compartments.getSelectedValue()).contains("Custom")) {
						type.setSelectedItem("Custom");
					}
					else {
						type.setSelectedItem("Sweep");
					}
					if (((String) type.getSelectedItem()).equals("Sweep")) {
						String[] splits = ((String) compartments.getSelectedValue()).split(" ");
						String sweepVal = splits[splits.length - 1];
						start.setText((sweepVal).split(",")[0].substring(1).trim());
						stop.setText((sweepVal).split(",")[1].trim());
						step.setText((sweepVal).split(",")[2].trim());
						level.setSelectedItem((sweepVal).split(",")[3].replace(")", "").trim());
						start.setEnabled(true);
						stop.setEnabled(true);
						step.setEnabled(true);
						level.setEnabled(true);
						compSize.setEnabled(false);
					}
					else {
						start.setEnabled(false);
						stop.setEnabled(false);
						step.setEnabled(false);
						level.setEnabled(false);
						compSize.setEnabled(true);
					}
				}
			}
			catch (Exception e) {
			}
		}
		compPanel.add(idLabel);
		compPanel.add(compID);
		compPanel.add(nameLabel);
		compPanel.add(compName);
		compPanel.add(compTypeLabel);
		compPanel.add(compTypeBox);
		compPanel.add(dimLabel);
		compPanel.add(dimBox);
		compPanel.add(outsideLabel);
		compPanel.add(compOutside);
		compPanel.add(constLabel);
		compPanel.add(compConstant);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Value Type:");
			type.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						if (((String) type.getSelectedItem()).equals("Sweep")) {
							start.setEnabled(true);
							stop.setEnabled(true);
							step.setEnabled(true);
							level.setEnabled(true);
							compSize.setEnabled(false);
						}
						else {
							start.setEnabled(false);
							stop.setEnabled(false);
							step.setEnabled(false);
							level.setEnabled(false);
							compSize.setEnabled(true);
						}
					}
					else {
						start.setEnabled(false);
						stop.setEnabled(false);
						step.setEnabled(false);
						level.setEnabled(false);
						compSize.setEnabled(false);
						SBMLReader reader = new SBMLReader();
						SBMLDocument d = reader.readSBML(file);
						if (d.getModel().getCompartment(
								((String) compartments.getSelectedValue()).split(" ")[0]).isSetSize()) {
							compSize.setText(d.getModel().getCompartment(
									((String) compartments.getSelectedValue()).split(" ")[0]).getSize()
									+ "");
						}
					}
				}
			});
			compPanel.add(typeLabel);
			compPanel.add(type);
		}
		compPanel.add(sizeLabel);
		compPanel.add(compSize);
		compPanel.add(compUnitsLabel);
		compPanel.add(compUnits);
		if (paramsOnly) {
			compPanel.add(startLabel);
			compPanel.add(start);
			compPanel.add(stopLabel);
			compPanel.add(stop);
			compPanel.add(stepLabel);
			compPanel.add(step);
			compPanel.add(levelLabel);
			compPanel.add(level);
		}
		compartPanel.add(compPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(biosim.frame(), compartPanel, "Compartment Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(compID.getText().trim(), selectedID, false);
			if (!error && option.equals("OK") && compConstant.getSelectedItem().equals("true")) {
				String val = ((String) compartments.getSelectedValue()).split(" ")[0];
				error = checkConstant("Compartment", val);
			}
			if (!error && option.equals("OK") && dimBox.getSelectedItem().equals("0")) {
				String val = ((String) compartments.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
					Species species = document.getModel().getSpecies(i);
					if ((species.getCompartment().equals(val)) && (species.isSetInitialConcentration())) {
						JOptionPane
								.showMessageDialog(
										biosim.frame(),
										"Compartment with 0-dimensions cannot contain species with an initial concentration.",
										"Cannot be 0 Dimensions", JOptionPane.ERROR_MESSAGE);
						error = true;
						break;
					}
				}
				if (!error) {
					for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
						Compartment compartment = document.getModel().getCompartment(i);
						if (compartment.getOutside().equals(val)) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"Compartment with 0-dimensions cannot be outside another compartment.",
									"Cannot be 0 Dimensions", JOptionPane.ERROR_MESSAGE);
							error = true;
							break;
						}
					}
				}
			}
			Double addCompSize = 1.0;
			if ((!error) && (Integer.parseInt((String) dimBox.getSelectedItem()) != 0)) {
				try {
					addCompSize = Double.parseDouble(compSize.getText().trim());
				}
				catch (Exception e1) {
					error = true;
					JOptionPane.showMessageDialog(biosim.frame(),
							"The compartment size must be a real number.", "Enter a Valid Size",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			if (!error) {
				if (!compOutside.getSelectedItem().equals("( none )")) {
					if (checkOutsideCycle(compID.getText().trim(), (String) compOutside.getSelectedItem(), 0)) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"Compartment contains itself through outside references.",
								"Cycle in Outside References", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
			}
			if (!error) {
				if (((String) dimBox.getSelectedItem()).equals("0") && (variableInUse(selected, true))) {
					error = true;
				}
			}
			if (!error) {
				String addComp = "";
				String selCompType = (String) compTypeBox.getSelectedItem();
				String unit = (String) compUnits.getSelectedItem();
				if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
					int index = compartments.getSelectedIndex();
					String[] splits = comps[index].split(" ");
					for (int i = 0; i < splits.length - 2; i++) {
						addComp += splits[i] + " ";
					}
					if (!splits[splits.length - 2].equals("Custom")
							&& !splits[splits.length - 2].equals("Sweep")) {
						addComp += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
					}
					if (((String) type.getSelectedItem()).equals("Sweep")) {
						try {
							double startVal = Double.parseDouble(start.getText().trim());
							double stopVal = Double.parseDouble(stop.getText().trim());
							double stepVal = Double.parseDouble(step.getText().trim());
							addComp += "Sweep (" + startVal + "," + stopVal + "," + stepVal + ","
									+ level.getSelectedItem() + ")";
						}
						catch (Exception e1) {
							error = true;
							JOptionPane.showMessageDialog(biosim.frame(),
									"The start, stop, and step fields must be real numbers.", "Enter a Valid Sweep",
									JOptionPane.ERROR_MESSAGE);
						}
					}
					else {
						addComp += "Custom " + addCompSize;
					}
				}
				else {
					addComp = compID.getText().trim();
					if (!selCompType.equals("( none )")) {
						addComp += " " + selCompType;
					}
					if (!unit.equals("( none )")) {
						addComp += " " + addCompSize + " " + unit;
					}
					else {
						addComp += " " + addCompSize;
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
								usedIDs.set(i, compID.getText().trim());
							}
						}
						comps[index] = addComp;
						sort(comps);
						compartments.setListData(comps);
						compartments.setSelectedIndex(index);
						for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
							Species species = document.getModel().getSpecies(i);
							if (species.getCompartment().equals(val)) {
								species.setCompartment(compID.getText().trim());
							}
						}
						int index1 = species.getSelectedIndex();
						species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						specs = Buttons.getList(specs, species);
						species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						for (int i = 0; i < specs.length; i++) {
							if (specs[i].split(" ")[1].equals(val)) {
								specs[i] = specs[i].split(" ")[0] + " " + compID.getText().trim() + " "
										+ specs[i].split(" ")[2];
							}
							else if (specs[i].split(" ")[2].equals(val)) {
								specs[i] = specs[i].split(" ")[0] + " " + specs[i].split(" ")[1] + " "
										+ compID.getText().trim() + " " + specs[i].split(" ")[3];
							}
						}
						sort(specs);
						species.setListData(specs);
						species.setSelectedIndex(index1);
						if (paramsOnly) {
							int remove = -1;
							for (int i = 0; i < parameterChanges.size(); i++) {
								if (parameterChanges.get(i).split(" ")[0].equals(compID.getText().trim())) {
									remove = i;
								}
							}
							if (remove != -1) {
								parameterChanges.remove(remove);
							}
							if (!((String) type.getSelectedItem()).equals("Original")) {
								parameterChanges.add(comps[index]);
							}
						}
						else {
							updateVarId(false, val, compID.getText().trim());
							for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
								Compartment compartment = document.getModel().getCompartment(i);
								if (compartment.getOutside().equals(val)) {
									compartment.setOutside(compID.getText().trim());
								}
							}
						}
					}
					else {
						int index = compartments.getSelectedIndex();
						Compartment c = document.getModel().createCompartment();
						c.setId(compID.getText().trim());
						c.setName(compName.getText().trim());
						if (!selCompType.equals("( none )")) {
							c.setCompartmentType(selCompType);
						}
						c.setSpatialDimensions(Integer.parseInt((String) dimBox.getSelectedItem()));
						if (!compSize.getText().trim().equals("")) {
							c.setSize(Double.parseDouble(compSize.getText().trim()));
						}
						if (!compUnits.getSelectedItem().equals("( none )")) {
							c.setUnits((String) compUnits.getSelectedItem());
						}
						if (!compOutside.getSelectedItem().equals("( none )")) {
							c.setOutside((String) compOutside.getSelectedItem());
						}
						if (compConstant.getSelectedItem().equals("true")) {
							c.setConstant(true);
						}
						else {
							c.setConstant(false);
						}
						usedIDs.add(compID.getText().trim());
						JList add = new JList();
						String addStr;
						if (!selCompType.equals("( none )")) {
							addStr = compID.getText().trim() + " " + selCompType + " "
									+ compSize.getText().trim();
						}
						else {
							addStr = compID.getText().trim() + " " + compSize.getText().trim();
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
			}
			if (error) {
				value = JOptionPane.showOptionDialog(biosim.frame(), compartPanel, "Compartment Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Variable that is updated by a rule or event cannot be constant
	 */
	private boolean checkOutsideCycle(String compID, String outside, int depth) {
		depth++;
		if (depth > document.getModel().getNumCompartments())
			return true;
		Compartment compartment = document.getModel().getCompartment(outside);
		if (compartment.isSetOutside()) {
			if (compartment.getOutside().equals(compID)) {
				return true;
			}
			return checkOutsideCycle(compID, compartment.getOutside(), depth);
		}
		return false;
	}

	/**
	 * Variable that is updated by a rule or event cannot be constant
	 */
	private boolean checkConstant(String varType, String val) {
		for (int i = 0; i < document.getModel().getNumRules(); i++) {
			Rule rule = document.getModel().getRule(i);
			if (rule.getVariable().equals(val)) {
				JOptionPane.showMessageDialog(biosim.frame(), varType
						+ " cannot be constant if updated by a rule.", varType + " Cannot Be Constant",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		for (int i = 0; i < document.getModel().getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) document.getModel().getListOfEvents()
					.get(i);
			for (int j = 0; j < event.getNumEventAssignments(); j++) {
				EventAssignment ea = (EventAssignment) event.getListOfEventAssignments().get(j);
				if (ea.getVariable().equals(val)) {
					JOptionPane.showMessageDialog(biosim.frame(), varType
							+ " cannot be constant if updated by an event.", varType + " Cannot Be Constant",
							JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Species that is a reactant or product cannot be constant unless it is a
	 * boundary condition
	 */
	private boolean checkBoundary(String val, boolean checkRule) {
		Model model = document.getModel();
		boolean inRule = false;
		if (checkRule) {
			if (model.getNumRules() > 0) {
				for (int i = 0; i < model.getNumRules(); i++) {
					Rule rule = (Rule) model.getListOfRules().get(i);
					if (rule.isSetVariable() && val.equals(rule.getVariable())) {
						inRule = true;
						break;
					}
				}
			}
			if (!inRule)
				return false;
		}
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getProduct(j);
					if (val.equals(specRef.getSpecies())) {
						if (checkRule) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"Boundary condition cannot be false if a species is used\n"
											+ "in a rule and as a reactant or product in a reaction.",
									"Boundary Condition Cannot be False", JOptionPane.ERROR_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(biosim.frame(),
									"Species cannot be reactant if constant and not a boundary condition.",
									"Invalid Species Attributes", JOptionPane.ERROR_MESSAGE);
						}
						return true;
					}
				}
			}
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				if (reaction.getReactant(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getReactant(j);
					if (val.equals(specRef.getSpecies())) {
						if (checkRule) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"Boundary condition cannot be false if a species is used\n"
											+ "in a rule and as a reactant or product in a reaction.",
									"Boundary Condition Cannot be False", JOptionPane.ERROR_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(biosim.frame(),
									"Species cannot be product if constant and not a boundary condition.",
									"Invalid Species Attributes", JOptionPane.ERROR_MESSAGE);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Set compartment options based on number of dimensions
	 */
	private void setCompartOptions(String selected, String dim) {
		if (dim.equals("3")) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1)
						&& (unit.getUnit(0).isLitre() && unit.getUnit(0).getExponent() == 1)
						|| (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 3)) {
					if (!unit.getId().equals("volume")) {
						compUnits.addItem(unit.getId());
					}
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
			if (!paramsOnly) {
				compConstant.setEnabled(true);
				compSize.setEnabled(true);
			}
		}
		else if (dim.equals("2")) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1)
						&& (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 2)) {
					if (!unit.getId().equals("area")) {
						compUnits.addItem(unit.getId());
					}
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
			if (!paramsOnly) {
				compConstant.setEnabled(true);
				compSize.setEnabled(true);
			}
		}
		else if (dim.equals("1")) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1)
						&& (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 1)) {
					if (!unit.getId().equals("length")) {
						compUnits.addItem(unit.getId());
					}
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
			if (!paramsOnly) {
				compConstant.setEnabled(true);
				compSize.setEnabled(true);
			}
		}
		else if (dim.equals("0")) {
			compSize.setText("");
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
			compSize.setEnabled(false);
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
			return;
		}
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
		String[] initLabels = { "Initial Amount", "Initial Concentration" };
		initLabel = new JComboBox(initLabels);
		JLabel unitLabel = new JLabel("Units:");
		JLabel boundLabel = new JLabel("Boundary Condition:");
		JLabel constLabel = new JLabel("Constant:");
		ID = new JTextField();
		String selectedID = "";
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
				if (!unit.getId().equals("substance")) {
					specUnits.addItem(unit.getId());
				}
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
				selectedID = specie.getId();
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
						String[] splits = ((String) compartments.getSelectedValue()).split(" ");
						String sweepVal = splits[splits.length - 1];
						start.setText((sweepVal).split(",")[0].substring(1).trim());
						stop.setText((sweepVal).split(",")[1].trim());
						step.setText((sweepVal).split(",")[2].trim());
						level.setSelectedItem((sweepVal).split(",")[3].replace(")", "").trim());
						start.setEnabled(true);
						stop.setEnabled(true);
						step.setEnabled(true);
						level.setEnabled(true);
						init.setEnabled(false);
						initLabel.setEnabled(false);
					}
					else {
						start.setEnabled(false);
						stop.setEnabled(false);
						step.setEnabled(false);
						level.setEnabled(false);
						init.setEnabled(true);
						initLabel.setEnabled(false);
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
			JLabel typeLabel = new JLabel("Value Type:");
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
						}
						else {
							start.setEnabled(false);
							stop.setEnabled(false);
							step.setEnabled(false);
							level.setEnabled(false);
							init.setEnabled(true);
							initLabel.setEnabled(false);
						}
					}
					else {
						start.setEnabled(false);
						stop.setEnabled(false);
						step.setEnabled(false);
						level.setEnabled(false);
						init.setEnabled(false);
						initLabel.setEnabled(false);
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(ID.getText().trim(), selectedID, false);
			double initial = 0;
			if (!error) {
				try {
					initial = Double.parseDouble(init.getText().trim());
				}
				catch (Exception e1) {
					error = true;
					JOptionPane.showMessageDialog(biosim.frame(),
							"The initial value field must be a real number.", "Enter a Valid Initial Value",
							JOptionPane.ERROR_MESSAGE);
				}
				String unit = (String) specUnits.getSelectedItem();
				String addSpec = "";
				String selSpecType = (String) specTypeBox.getSelectedItem();
				if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
					int index = species.getSelectedIndex();
					String[] splits = specs[index].split(" ");
					for (int i = 0; i < splits.length - 2; i++) {
						addSpec += splits[i] + " ";
					}
					if (!splits[splits.length - 2].equals("Custom")
							&& !splits[splits.length - 2].equals("Sweep")) {
						addSpec += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
					}
					if (((String) type.getSelectedItem()).equals("Sweep")) {
						double startVal = Double.parseDouble(start.getText().trim());
						double stopVal = Double.parseDouble(stop.getText().trim());
						double stepVal = Double.parseDouble(step.getText().trim());
						addSpec += "Sweep (" + startVal + "," + stopVal + "," + stepVal + ","
								+ level.getSelectedItem() + ")";
					}
					else {
						addSpec += "Custom " + initial;
					}
				}
				else {
					addSpec = ID.getText().trim();
					if (!selSpecType.equals("( none )")) {
						addSpec += " " + selSpecType;
					}
					addSpec += " " + comp.getSelectedItem() + " " + initial;
					if (!unit.equals("( none )")) {
						addSpec += " " + unit;
					}
				}
				if (!error) {
					ListOf listOfSpecies = document.getModel().getListOfSpecies();
					String selected = "";
					if (option.equals("OK")) {
						selected = ((String) species.getSelectedValue()).split(" ")[0];
					}
					for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
						if (!listOfSpecies.get(i).getId().equals(selected)) {
							if (((Species) listOfSpecies.get(i)).getCompartment().equals(
									(String) comp.getSelectedItem())
									&& ((Species) listOfSpecies.get(i)).getSpeciesType().equals(selSpecType)) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"Compartment already contains another species of this type.",
										"Species Type Not Unique", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
				if (!error) {
					Compartment compartment = document.getModel().getCompartment(
							(String) comp.getSelectedItem());
					if (initLabel.getSelectedItem().equals("Initial Concentration")
							&& compartment.getSpatialDimensions() == 0) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"Species in a 0 dimensional compartment cannot have an initial concentration.",
								"Concentration Not Possible", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error && option.equals("OK") && specConstant.getSelectedItem().equals("true")) {
					String val = ((String) species.getSelectedValue()).split(" ")[0];
					error = checkConstant("Species", val);
				}
				if (!error && option.equals("OK") && specBoundary.getSelectedItem().equals("false")) {
					String val = ((String) species.getSelectedValue()).split(" ")[0];
					error = checkBoundary(val, specConstant.getSelectedItem().equals("false"));
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
						if (initLabel.getSelectedItem().equals("Initial Amount")) {
							specie.setInitialAmount(initial);
							specie.unsetInitialConcentration();
							specie.setHasOnlySubstanceUnits(true);
						}
						else {
							specie.unsetInitialAmount();
							specie.setInitialConcentration(initial);
							specie.setHasOnlySubstanceUnits(false);
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
						else {
							updateVarId(true, speciesName, specie.getId());
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
						if (initLabel.getSelectedItem().equals("Initial Amount")) {
							specie.setInitialAmount(initial);
							specie.setHasOnlySubstanceUnits(true);
						}
						else {
							specie.setInitialConcentration(initial);
							specie.setHasOnlySubstanceUnits(false);
						}
						if (!unit.equals("( none )")) {
							specie.setUnits(unit);
						}
						JList addIt = new JList();
						Object[] adding = { addSpec };
						addIt.setListData(adding);
						addIt.setSelectedIndex(0);
						species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Buttons.add(specs, species, addIt, false, null, null, null, null, null, null,
								biosim.frame());
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
			if (error) {
				value = JOptionPane.showOptionDialog(biosim.frame(), speciesPanel, "Species Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Update variable Id
	 */
	private void updateVarId(boolean isSpecies, String origId, String newId) {
		if (origId.equals(newId))
			return;
		Model model = document.getModel();
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getProduct(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
					if (specRef.isSetStoichiometryMath()) {
						StoichiometryMath sm = new StoichiometryMath(updateMathVar(specRef
								.getStoichiometryMath().getMath(), origId, newId));
						specRef.setStoichiometryMath(sm);
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
						StoichiometryMath sm = new StoichiometryMath(updateMathVar(specRef
								.getStoichiometryMath().getMath(), origId, newId));
						specRef.setStoichiometryMath(sm);
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
				inits[i] = init.getSymbol() + " = " + myFormulaToString(init.getMath());
			}
			String[] oldInits = inits;
			try {
				inits = sortInitRules(inits);
				if (checkCycles(inits, rul)) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Cycle detected within initial assignments, assignment rules, and rate laws.", 
								      "Cycle Detected",
							JOptionPane.ERROR_MESSAGE);
					inits = oldInits;
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in assignments.",
						"Cycle Detected", JOptionPane.ERROR_MESSAGE);
				inits = oldInits;
			}
			initAssigns.setListData(inits);
			initAssigns.setSelectedIndex(0);
		}
		if (model.getNumRules() > 0) {
			for (int i = 0; i < model.getNumRules(); i++) {
				Rule rule = (Rule) model.getListOfRules().get(i);
				if (rule.isSetVariable() && origId.equals(rule.getVariable())) {
					rule.setVariable(newId);
				}
				rule.setMath(updateMathVar(rule.getMath(), origId, newId));
				if (rule.isAlgebraic()) {
					rul[i] = "0 = " + myFormulaToString(rule.getMath());
				}
				else if (rule.isAssignment()) {
					rul[i] = rule.getVariable() + " = " + myFormulaToString(rule.getMath());
				}
				else {
					rul[i] = "d( " + rule.getVariable() + " )/dt = " + myFormulaToString(rule.getMath());
				}
			}
			String[] oldRul = rul;
			try {
				rul = sortRules(rul);
				if (checkCycles(inits, rul)) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Cycle detected within initial assignments, assignment rules, and rate laws.", 
								      "Cycle Detected",
							JOptionPane.ERROR_MESSAGE);
					rul = oldRul;
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(biosim.frame(), "Cycle detected in assignments.",
						"Cycle Detected", JOptionPane.ERROR_MESSAGE);
				rul = oldRul;
			}
			rules.setListData(rul);
			rules.setSelectedIndex(0);
		}
		if (model.getNumConstraints() > 0) {
			for (int i = 0; i < model.getNumConstraints(); i++) {
				Constraint constraint = (Constraint) model.getListOfConstraints().get(i);
				constraint.setMath(updateMathVar(constraint.getMath(), origId, newId));
				cons[i] = myFormulaToString(constraint.getMath());
			}
			sort(cons);
			constraints.setListData(cons);
			constraints.setSelectedIndex(0);
		}
		if (model.getNumEvents() > 0) {
			for (int i = 0; i < model.getNumEvents(); i++) {
				org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) model.getListOfEvents().get(i);
				if (event.isSetTrigger()) {
					Trigger trigger = new Trigger(updateMathVar(event.getTrigger().getMath(), origId, newId));
					event.setTrigger(trigger);
				}
				if (event.isSetDelay()) {
					Delay delay = new Delay(updateMathVar(event.getDelay().getMath(), origId, newId));
					delay.setSBMLDocument(document);
					event.setDelay(delay);
				}
				ev[i] = event.getId();
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
			sort(ev);
			events.setListData(ev);
			events.setSelectedIndex(0);
		}
	}

	/**
	 * Update variable in math formula using String
	 */
	private String updateFormulaVar(String s, String origVar, String newVar) {
		s = " " + s + " ";
		s = s.replace(" " + origVar + " ", " " + newVar + " ");
		s = s.replace(" " + origVar + "(", " " + newVar + "(");
		s = s.replace("(" + origVar + ")", "(" + newVar + ")");
		s = s.replace("(" + origVar + " ", "(" + newVar + " ");
		s = s.replace("(" + origVar + ",", "(" + newVar + ",");
		s = s.replace(" " + origVar + ")", " " + newVar + ")");
		return s.trim();
	}

	/**
	 * Update variable in math formula using ASTNode
	 */
	private ASTNode updateMathVar(ASTNode math, String origVar, String newVar) {
		String s = updateFormulaVar(myFormulaToString(math), origVar, newVar);
		return myParseFormula(s);
	}

	/**
	 * Update unit Id
	 */
	private void updateUnitId(String origId, String newId) {
		if (origId.equals(newId))
			return;
		Model model = document.getModel();
		if (model.getNumCompartments() > 0) {
			for (int i = 0; i < model.getNumCompartments(); i++) {
				Compartment compartment = (Compartment) model.getListOfCompartments().get(i);
				if (compartment.getUnits().equals(origId)) {
					compartment.setUnits(newId);
				}
				comps[i] = compartment.getId();
				if (compartment.isSetCompartmentType()) {
					comps[i] += " " + compartment.getCompartmentType();
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
			compartments.setSelectedIndex(0);
		}
		if (model.getNumSpecies() > 0) {
			for (int i = 0; i < model.getNumSpecies(); i++) {
				Species species = (Species) model.getListOfSpecies().get(i);
				if (species.getUnits().equals(origId)) {
					species.setUnits(newId);
				}
				if (species.isSetSpeciesType()) {
					specs[i] = species.getId() + " " + species.getSpeciesType() + " "
							+ species.getCompartment();
				}
				else {
					specs[i] = species.getId() + " " + species.getCompartment();
				}
				if (species.isSetInitialAmount()) {
					specs[i] += " " + species.getInitialAmount();
				}
				else {
					specs[i] += " " + species.getInitialConcentration();
				}
				if (species.isSetUnits()) {
					specs[i] += " " + species.getUnits();
				}
			}
			sort(specs);
			species.setListData(specs);
			species.setSelectedIndex(0);
		}
		if (model.getNumParameters() > 0) {
			for (int i = 0; i < model.getNumParameters(); i++) {
				Parameter parameter = (Parameter) model.getListOfParameters().get(i);
				if (parameter.getUnits().equals(origId)) {
					parameter.setUnits(newId);
				}
				if (parameter.isSetUnits()) {
					params[i] = parameter.getId() + " " + parameter.getValue() + " " + parameter.getUnits();
				}
				else {
					params[i] = parameter.getId() + " " + parameter.getValue();
				}
			}
			sort(params);
			parameters.setListData(params);
			parameters.setSelectedIndex(0);
		}
		for (int i = 0; i < model.getNumReactions(); i++) {
			KineticLaw kineticLaw = (KineticLaw) model.getReaction(i).getKineticLaw();
			for (int j = 0; j < kineticLaw.getNumParameters(); j++) {
				if (kineticLaw.getParameter(j).getUnits().equals(origId)) {
					kineticLaw.getParameter(j).setUnits(newId);
				}
			}
		}
	}

	/**
	 * Creates a frame used to edit reactions or create new ones.
	 */
	private void reactionsEditor(String option) {
		if (option.equals("OK") && reactions.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No reaction selected.",
					"Must Select A Reaction", JOptionPane.ERROR_MESSAGE);
			return;
		}
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
		String selectedID = "";
		Reaction copyReact = null;
		if (option.equals("OK")) {
			Reaction reac = document.getModel().getReaction(
					((String) reactions.getSelectedValue()).split(" ")[0]);
			copyReact = (Reaction) reac.cloneObject();
			reacID.setText(reac.getId());
			selectedID = reac.getId();
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
		else {
			String reactionId = "r0";
			int i = 0;
			while (usedIDs.contains(reactionId)) {
				i++;
				reactionId = "r" + i;
			}
			reacID.setText(reactionId);
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
						if (parameterChanges.get(j).split(" ")[0]
								.equals(((String) reactions.getSelectedValue()).split(" ")[0] + "/"
										+ parameter.getId())) {
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
		if (option.equals("OK")) {
			Reaction reac = document.getModel().getReaction(
					((String) reactions.getSelectedValue()).split(" ")[0]);
			ListOf listOfReactants = reac.getListOfReactants();
			reacta = new String[(int) reac.getNumReactants()];
			for (int i = 0; i < reac.getNumReactants(); i++) {
				SpeciesReference reactant = (SpeciesReference) listOfReactants.get(i);
				changedReactants.add(reactant);
				if (reactant.isSetStoichiometryMath()) {
					reacta[i] = reactant.getSpecies() + " "
							+ myFormulaToString(reactant.getStoichiometryMath().getMath());
				}
				else {
					reacta[i] = reactant.getSpecies() + " " + reactant.getStoichiometry();
				}
			}
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
					this.product[i] = product.getSpecies() + " "
							+ myFormulaToString(product.getStoichiometryMath().getMath());
				}
				else {
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			String reac = reacID.getText().trim();
			error = checkID(reac, selectedID, false);
			if (!error) {
				if (kineticLaw.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(biosim.frame(), "A reaction must have a kinetic law.",
							"Enter A Kinetic Law", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if ((changedReactants.size() == 0) && (changedProducts.size() == 0)) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"A reaction must have at least one reactant or product.", "No Reactants or Products",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (myParseFormula(kineticLaw.getText().trim()) == null) {
					JOptionPane.showMessageDialog(biosim.frame(), "Unable to parse kinetic law.",
							"Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else {
					ArrayList<String> invalidKineticVars = getInvalidVariables(kineticLaw.getText().trim(),
							true, "", false);
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
						message = "Kinetic law contains unknown variables.\n\n" + "Unknown variables:\n"
								+ invalid;
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
						error = true;
					}
					if (!error) {
						error = checkNumFunctionArguments(myParseFormula(kineticLaw.getText().trim()));
					}
				}
			}
			if (!error) {
				if (myParseFormula(kineticLaw.getText().trim()).isBoolean()) {
					JOptionPane.showMessageDialog(biosim.frame(), "Kinetic law must evaluate to a number.",
							"Number Expected", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = reactions.getSelectedIndex();
					String val = ((String) reactions.getSelectedValue()).split(" ")[0];
					reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacts = Buttons.getList(reacts, reactions);
					reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					Reaction react = document.getModel().getReaction(val);
					ListOf remove;
					long size;
					remove = react.getKineticLaw().getListOfParameters();
					size = react.getKineticLaw().getNumParameters();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
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
					react.getKineticLaw().setFormula(kineticLaw.getText().trim());
					error = checkKineticLawUnits(react.getKineticLaw());
					if (!error) {
					    error = checkCycles(inits, rul);
					    if (error) {
						JOptionPane.showMessageDialog(biosim.frame(),
						      "Cycle detected within initial assignments, assignment rules, and rate laws.", 
									      "Cycle Detected",
									      JOptionPane.ERROR_MESSAGE);
					    }
					}
					if (!error) {
						for (int i = 0; i < usedIDs.size(); i++) {
							if (usedIDs.get(i).equals(val)) {
								usedIDs.set(i, reacID.getText().trim());
							}
						}
						if (!paramsOnly) {
							reacts[index] = reac;
						}
						sort(reacts);
						reactions.setListData(reacts);
						reactions.setSelectedIndex(index);
					}
					else {
						changedParameters = new ArrayList<Parameter>();
						ListOf listOfParameters = react.getKineticLaw().getListOfParameters();
						for (int i = 0; i < react.getKineticLaw().getNumParameters(); i++) {
							Parameter parameter = (Parameter) listOfParameters.get(i);
							changedParameters.add(parameter);
						}
						changedProducts = new ArrayList<SpeciesReference>();
						ListOf listOfProducts = react.getListOfProducts();
						for (int i = 0; i < react.getNumProducts(); i++) {
							SpeciesReference product = (SpeciesReference) listOfProducts.get(i);
							changedProducts.add(product);
						}
						changedReactants = new ArrayList<SpeciesReference>();
						ListOf listOfReactants = react.getListOfReactants();
						for (int i = 0; i < react.getNumReactants(); i++) {
							SpeciesReference reactant = (SpeciesReference) listOfReactants.get(i);
							changedReactants.add(reactant);
						}
						changedModifiers = new ArrayList<ModifierSpeciesReference>();
						ListOf listOfModifiers = react.getListOfModifiers();
						for (int i = 0; i < react.getNumModifiers(); i++) {
							ModifierSpeciesReference modifier = (ModifierSpeciesReference) listOfModifiers.get(i);
							changedModifiers.add(modifier);
						}
					}
				}
				else {
					Reaction react = document.getModel().createReaction();
					react.setKineticLaw(new KineticLaw());
					int index = reactions.getSelectedIndex();
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
					react.getKineticLaw().setFormula(kineticLaw.getText().trim());
					error = checkKineticLawUnits(react.getKineticLaw());
					if (!error) {
					    error = checkCycles(inits, rul);
					    if (error) {
						JOptionPane.showMessageDialog(biosim.frame(),
						      "Cycle detected within initial assignments, assignment rules, and rate laws.", 
									      "Cycle Detected",
									      JOptionPane.ERROR_MESSAGE);
					    }
					}
					if (!error) {
						usedIDs.add(reacID.getText().trim());
						JList add = new JList();
						Object[] adding = { reac };
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
					}
					else {
						removeTheReaction(reac);
					}
				}
				change = true;
			}
			if (error) {
				value = JOptionPane.showOptionDialog(biosim.frame(), reactionPanel, "Reaction Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			if (option.equals("OK")) {
				String reac = ((String) reactions.getSelectedValue()).split(" ")[0];
				removeTheReaction(reac);
				document.getModel().addReaction(copyReact);
			}
			return;
		}
	}

	/**
	 * Find invalid reaction variables in a formula
	 */
	private ArrayList<String> getInvalidVariables(String formula, boolean isReaction,
			String arguments, boolean isFunction) {
		ArrayList<String> validVars = new ArrayList<String>();
		ArrayList<String> invalidVars = new ArrayList<String>();
		ListOf sbml = document.getModel().getListOfFunctionDefinitions();
		for (int i = 0; i < document.getModel().getNumFunctionDefinitions(); i++) {
			validVars.add(((FunctionDefinition) sbml.get(i)).getId());
		}
		if (isReaction) {
			for (int i = 0; i < changedParameters.size(); i++) {
				validVars.add(((Parameter) changedParameters.get(i)).getId());
			}
			for (int i = 0; i < changedReactants.size(); i++) {
				validVars.add(((SpeciesReference) changedReactants.get(i)).getSpecies());
			}
			for (int i = 0; i < changedProducts.size(); i++) {
				validVars.add(((SpeciesReference) changedProducts.get(i)).getSpecies());
			}
			for (int i = 0; i < changedModifiers.size(); i++) {
				validVars.add(((ModifierSpeciesReference) changedModifiers.get(i)).getSpecies());
			}
		}
		else if (!isFunction) {
			sbml = document.getModel().getListOfSpecies();
			for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
				validVars.add(((Species) sbml.get(i)).getId());
			}
		}
		if (isFunction) {
			String[] args = arguments.split(" |\\,");
			for (int i = 0; i < args.length; i++) {
				validVars.add(args[i]);
			}
		}
		else {
			sbml = document.getModel().getListOfCompartments();
			for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
				if (((Compartment) sbml.get(i)).getSpatialDimensions() != 0) {
					validVars.add(((Compartment) sbml.get(i)).getId());
				}
			}
			sbml = document.getModel().getListOfParameters();
			for (int i = 0; i < document.getModel().getNumParameters(); i++) {
				validVars.add(((Parameter) sbml.get(i)).getId());
			}
			sbml = document.getModel().getListOfReactions();
			for (int i = 0; i < document.getModel().getNumReactions(); i++) {
				validVars.add(((Reaction) sbml.get(i)).getId());
			}
		}
		String[] splitLaw = formula.split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
		for (int i = 0; i < splitLaw.length; i++) {
			if (splitLaw[i].equals("abs") || splitLaw[i].equals("arccos")
					|| splitLaw[i].equals("arccosh") || splitLaw[i].equals("arcsin")
					|| splitLaw[i].equals("arcsinh") || splitLaw[i].equals("arctan")
					|| splitLaw[i].equals("arctanh") || splitLaw[i].equals("arccot")
					|| splitLaw[i].equals("arccoth") || splitLaw[i].equals("arccsc")
					|| splitLaw[i].equals("arccsch") || splitLaw[i].equals("arcsec")
					|| splitLaw[i].equals("arcsech") || splitLaw[i].equals("acos")
					|| splitLaw[i].equals("acosh") || splitLaw[i].equals("asin")
					|| splitLaw[i].equals("asinh") || splitLaw[i].equals("atan")
					|| splitLaw[i].equals("atanh") || splitLaw[i].equals("acot")
					|| splitLaw[i].equals("acoth") || splitLaw[i].equals("acsc")
					|| splitLaw[i].equals("acsch") || splitLaw[i].equals("asec")
					|| splitLaw[i].equals("asech") || splitLaw[i].equals("cos") || splitLaw[i].equals("cosh")
					|| splitLaw[i].equals("cot") || splitLaw[i].equals("coth") || splitLaw[i].equals("csc")
					|| splitLaw[i].equals("csch") || splitLaw[i].equals("ceil")
					|| splitLaw[i].equals("factorial") || splitLaw[i].equals("exp")
					|| splitLaw[i].equals("floor") || splitLaw[i].equals("ln") || splitLaw[i].equals("log")
					|| splitLaw[i].equals("sqr") || splitLaw[i].equals("log10") || splitLaw[i].equals("pow")
					|| splitLaw[i].equals("sqrt") || splitLaw[i].equals("root")
					|| splitLaw[i].equals("piecewise") || splitLaw[i].equals("sec")
					|| splitLaw[i].equals("sech") || splitLaw[i].equals("sin") || splitLaw[i].equals("sinh")
					|| splitLaw[i].equals("tan") || splitLaw[i].equals("tanh") || splitLaw[i].equals("")
					|| splitLaw[i].equals("and") || splitLaw[i].equals("or") || splitLaw[i].equals("xor")
					|| splitLaw[i].equals("not") || splitLaw[i].equals("eq") || splitLaw[i].equals("geq")
					|| splitLaw[i].equals("leq") || splitLaw[i].equals("gt") || splitLaw[i].equals("neq")
					|| splitLaw[i].equals("lt") || splitLaw[i].equals("delay") || splitLaw[i].equals("t")
					|| splitLaw[i].equals("time") || splitLaw[i].equals("true")
					|| splitLaw[i].equals("false") || splitLaw[i].equals("pi")
					|| splitLaw[i].equals("exponentiale")) {
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
	 * Creates a frame used to edit parameters or create new ones.
	 */
	private void parametersEditor(String option) {
		if (option.equals("OK") && parameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No parameter selected.",
					"Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
			return;
		}
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
			if (!units[i].equals("substance") && !units[i].equals("volume") && !units[i].equals("area")
					&& !units[i].equals("length") && !units[i].equals("time")) {
				paramUnits.addItem(units[i]);
			}
		}
		String[] unitIds = { "substance", "volume", "area", "length", "time", "ampere", "becquerel",
				"candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray", "henry",
				"hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre",
				"mole", "newton", "ohm", "pascal", "radian", "second", "siemens", "sievert", "steradian",
				"tesla", "volt", "watt", "weber" };
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
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				Parameter paramet = document.getModel().getParameter(
						((String) parameters.getSelectedValue()).split(" ")[0]);
				paramID.setText(paramet.getId());
				selectedID = paramet.getId();
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
						String[] splits = ((String) parameters.getSelectedValue()).split(" ");
						String sweepVal = splits[splits.length - 1];
						start.setText((sweepVal).split(",")[0].substring(1).trim());
						stop.setText((sweepVal).split(",")[1].trim());
						step.setText((sweepVal).split(",")[2].trim());
						level.setSelectedItem((sweepVal).split(",")[3].replace(")", "").trim());
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(paramID.getText().trim(), selectedID, false);
			if (!error) {
				double val = 0.0;
				try {
					val = Double.parseDouble(paramValue.getText().trim());
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(biosim.frame(), "The value must be a real number.",
							"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (!error) {
					String unit = (String) paramUnits.getSelectedItem();
					String param = "";
					if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
						int index = parameters.getSelectedIndex();
						String[] splits = params[index].split(" ");
						for (int i = 0; i < splits.length - 2; i++) {
							param += splits[i] + " ";
						}
						if (!splits[splits.length - 2].equals("Custom")
								&& !splits[splits.length - 2].equals("Sweep")) {
							param += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
						}
						if (((String) type.getSelectedItem()).equals("Sweep")) {
							double startVal = Double.parseDouble(start.getText().trim());
							double stopVal = Double.parseDouble(stop.getText().trim());
							double stepVal = Double.parseDouble(step.getText().trim());
							param += "Sweep (" + startVal + "," + stopVal + "," + stepVal + ","
									+ level.getSelectedItem() + ")";
						}
						else {
							param += "Custom " + val;
						}
					}
					else {
						param = paramID.getText().trim() + " " + val;
						if (!unit.equals("( none )")) {
							param = paramID.getText().trim() + " " + val + " " + unit;
						}
					}
					if (!error && option.equals("OK") && paramConst.getSelectedItem().equals("true")) {
						String v = ((String) parameters.getSelectedValue()).split(" ")[0];
						error = checkConstant("Parameters", v);
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
							else {
								updateVarId(false, v, paramID.getText().trim());
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
							if (!unit.equals("( none )")) {
								paramet.setUnits(unit);
							}
							JList add = new JList();
							Object[] adding = { param };
							add.setListData(adding);
							add.setSelectedIndex(0);
							parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							adding = Buttons.add(params, parameters, add, false, null, null, null, null, null,
									null, biosim.frame());
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
			}
			if (error) {
				value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel, "Parameter Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit reactions parameters or create new ones.
	 */
	private void reacParametersEditor(String option) {
		if (option.equals("OK") && reacParameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No parameter selected.",
					"Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
			return;
		}
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
			if (!units[i].equals("substance") && !units[i].equals("volume") && !units[i].equals("area")
					&& !units[i].equals("length") && !units[i].equals("time")) {
				reacParamUnits.addItem(units[i]);
			}
		}
		String[] unitIds = { "substance", "volume", "area", "length", "time", "ampere", "becquerel",
				"candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray", "henry",
				"hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre",
				"mole", "newton", "ohm", "pascal", "radian", "second", "siemens", "sievert", "steradian",
				"tesla", "volt", "watt", "weber" };
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
		String selectedID = "";
		if (option.equals("OK")) {
			String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
			Parameter paramet = null;
			for (Parameter p : changedParameters) {
				if (p.getId().equals(v)) {
					paramet = p;
				}
			}
			reacParamID.setText(paramet.getId());
			selectedID = paramet.getId();
			reacParamName.setText(paramet.getName());
			reacParamValue.setText("" + paramet.getValue());
			if (paramet.isSetUnits()) {
				reacParamUnits.setSelectedItem(paramet.getUnits());
			}
			if (paramsOnly && (((String) reacParameters.getSelectedValue()).contains("Custom"))
					|| (((String) reacParameters.getSelectedValue()).contains("Sweep"))) {
				if (((String) reacParameters.getSelectedValue()).contains("Custom")) {
					type.setSelectedItem("Custom");
				}
				else {
					type.setSelectedItem("Sweep");
				}
				if (((String) type.getSelectedItem()).equals("Sweep")) {
					String[] splits = ((String) reacParameters.getSelectedValue()).split(" ");
					String sweepVal = splits[splits.length - 1];
					start.setText((sweepVal).split(",")[0].substring(1).trim());
					stop.setText((sweepVal).split(",")[1].trim());
					step.setText((sweepVal).split(",")[2].trim());
					level.setSelectedItem((sweepVal).split(",")[3].replace(")", "").trim());
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = checkID(reacParamID.getText().trim(), selectedID, true);
			if (!error) {
				if (thisReactionParams.contains(reacParamID.getText().trim())
						&& (!reacParamID.getText().trim().equals(selectedID))) {
					JOptionPane.showMessageDialog(biosim.frame(), "ID is not unique.", "ID Not Unique",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				double val = 0;
				try {
					val = Double.parseDouble(reacParamValue.getText().trim());
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(biosim.frame(), "The value must be a real number.",
							"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
					error = true;
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
						if (!splits[splits.length - 2].equals("Custom")
								&& !splits[splits.length - 2].equals("Sweep")) {
							param += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
						}
						if (((String) type.getSelectedItem()).equals("Sweep")) {
							double startVal = Double.parseDouble(start.getText().trim());
							double stopVal = Double.parseDouble(stop.getText().trim());
							double stepVal = Double.parseDouble(step.getText().trim());
							param += "Sweep (" + startVal + "," + stopVal + "," + stepVal + ","
									+ level.getSelectedItem() + ")";
						}
						else {
							param += "Custom " + val;
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
							String reacValue = ((String) reactions.getSelectedValue()).split(" ")[0];
							int index1 = reactions.getSelectedIndex();
							if (remove != -1) {
								parameterChanges.remove(remove);
								reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacts = Buttons.getList(reacts, reactions);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reacts[index1] = reacValue.split(" ")[0];
								sort(reacts);
								reactions.setListData(reacts);
								reactions.setSelectedIndex(index1);
							}
							if (!((String) type.getSelectedItem()).equals("Original")) {
								parameterChanges.add(reacValue + "/" + param);
								reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacts = Buttons.getList(reacts, reactions);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reacts[index1] = reacValue + " Modified";
								sort(reacts);
								reactions.setListData(reacts);
								reactions.setSelectedIndex(index1);
							}
						}
						else {
							kineticLaw.setText(updateFormulaVar(kineticLaw.getText().trim(), v, reacParamID
									.getText().trim()));
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
						if (!unit.equals("( none )")) {
							paramet.setUnits(unit);
						}
						JList add = new JList();
						Object[] adding = { param };
						add.setListData(adding);
						add.setSelectedIndex(0);
						reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Buttons.add(reacParams, reacParameters, add, false, null, null, null, null,
								null, null, biosim.frame());
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
			if (error) {
				value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel, "Parameter Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit products or create new ones.
	 */
	public void productsEditor(String option) {
		if (option.equals("OK") && products.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No product selected.",
					"Must Select A Product", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel productsPanel = new JPanel(new GridLayout(2, 2));
		JLabel speciesLabel = new JLabel("Species:");
		Object[] stoiciOptions = { "Stoichiometry", "Stoichiometry Math" };
		stoiciLabel = new JComboBox(stoiciOptions);
		ListOf listOfSpecies = document.getModel().getListOfSpecies();
		String[] speciesList = new String[(int) document.getModel().getNumSpecies()];
		for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
			speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
		}
		sort(speciesList);
		productSpecies = new JComboBox();
		for (int i = 0; i < speciesList.length; i++) {
			Species species = document.getModel().getSpecies(speciesList[i]);
			if (species.getBoundaryCondition()
					|| (!species.getConstant() && keepVar("", speciesList[i], false, true, false, false))) {
				productSpecies.addItem(speciesList[i]);
			}
		}
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
				productStoiciometry.setText(""
						+ myFormulaToString(product.getStoichiometryMath().getMath()));
			}
			else {
				productStoiciometry.setText("" + product.getStoichiometry());
			}
		}
		productsPanel.add(speciesLabel);
		productsPanel.add(productSpecies);
		productsPanel.add(stoiciLabel);
		productsPanel.add(productStoiciometry);
		if (speciesList.length == 0) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"There are no species availiable to be products."
							+ "\nAdd species to this sbml file first.", "No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(biosim.frame(), productsPanel, "Products Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String prod;
			double val = 1.0;
			if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
				try {
					val = Double.parseDouble(productStoiciometry.getText().trim());
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(biosim.frame(), "The stoichiometry must be a real number.",
							"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (val <= 0) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"The stoichiometry value must be greater than 0.", "Enter A Valid Value",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				prod = productSpecies.getSelectedItem() + " " + val;
			}
			else {
				prod = productSpecies.getSelectedItem() + " " + productStoiciometry.getText().trim();
			}
			int index = -1;
			if (!error) {
				if (option.equals("OK")) {
					index = products.getSelectedIndex();
				}
				products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				product = Buttons.getList(product, products);
				products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index >= 0) {
					products.setSelectedIndex(index);
				}
				for (int i = 0; i < product.length; i++) {
					if (i != index) {
						if (product[i].split(" ")[0].equals(productSpecies.getSelectedItem())) {
							error = true;
							JOptionPane.showMessageDialog(biosim.frame(), "Unable to add species as a product.\n"
									+ "Each species can only be used as a product once.",
									"Species Can Only Be Used Once", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
					if (productStoiciometry.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry math must have formula.",
								"Enter Stoichiometry Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (myParseFormula(productStoiciometry.getText().trim()) == null) {
						JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry formula is not valid.",
								"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						ArrayList<String> invalidVars = getInvalidVariables(productStoiciometry.getText()
								.trim(), true, "", false);
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
							message = "Stoiciometry math contains unknown variables.\n\n"
									+ "Unknown variables:\n" + invalid;
							JTextArea messageArea = new JTextArea(message);
							messageArea.setLineWrap(true);
							messageArea.setWrapStyleWord(true);
							messageArea.setEditable(false);
							JScrollPane scrolls = new JScrollPane();
							scrolls.setMinimumSize(new Dimension(300, 300));
							scrolls.setPreferredSize(new Dimension(300, 300));
							scrolls.setViewportView(messageArea);
							JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Stoiciometry Math Error",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						if (!error) {
							error = checkNumFunctionArguments(myParseFormula(productStoiciometry.getText().trim()));
						}
						if (!error) {
							if (myParseFormula(productStoiciometry.getText().trim()).isBoolean()) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"Stoichiometry math must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
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
					}
					else {
						StoichiometryMath sm = new StoichiometryMath(myParseFormula(productStoiciometry
								.getText().trim()));
						produ.setStoichiometryMath(sm);
						produ.setStoichiometry(1);
					}
					product[index] = prod;
					sort(product);
					products.setListData(product);
					products.setSelectedIndex(index);
				}
				else {
					SpeciesReference produ = new SpeciesReference();
					changedProducts.add(produ);
					produ.setSpecies((String) productSpecies.getSelectedItem());
					if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
						produ.setStoichiometry(val);
					}
					else {
						StoichiometryMath sm = new StoichiometryMath(myParseFormula(productStoiciometry
								.getText().trim()));
						produ.setStoichiometryMath(sm);
					}
					JList add = new JList();
					Object[] adding = { prod };
					add.setListData(adding);
					add.setSelectedIndex(0);
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Buttons.add(product, products, add, false, null, null, null, null, null, null,
							biosim.frame());
					product = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						product[i] = (String) adding[i];
					}
					sort(product);
					products.setListData(product);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					products.setSelectedIndex(0);
				}
				change = true;
			}
			if (error) {
				value = JOptionPane.showOptionDialog(biosim.frame(), productsPanel, "Products Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit modifiers or create new ones.
	 */
	public void modifiersEditor(String option) {
		if (option.equals("OK") && modifiers.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No modifier selected.",
					"Must Select A Modifier", JOptionPane.ERROR_MESSAGE);
			return;
		}
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
			JOptionPane.showMessageDialog(biosim.frame(),
					"There are no species availiable to be modifiers."
							+ "\nAdd species to this sbml file first.", "No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(biosim.frame(), modifiersPanel, "Modifiers Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String mod = (String) modifierSpecies.getSelectedItem();
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
						JOptionPane.showMessageDialog(biosim.frame(), "Unable to add species as a modifier.\n"
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
					adding = Buttons.add(modifier, modifiers, add, false, null, null, null, null, null, null,
							biosim.frame());
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
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit reactants or create new ones.
	 */
	public void reactantsEditor(String option) {
		if (option.equals("OK") && reactants.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No reactant selected.",
					"Must Select A Reactant", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel reactantsPanel = new JPanel(new GridLayout(2, 2));
		JLabel speciesLabel = new JLabel("Species:");
		Object[] stoiciOptions = { "Stoichiometry", "Stoichiometry Math" };
		stoiciLabel = new JComboBox(stoiciOptions);
		ListOf listOfSpecies = document.getModel().getListOfSpecies();
		String[] speciesList = new String[(int) document.getModel().getNumSpecies()];
		for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
			speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
		}
		sort(speciesList);
		reactantSpecies = new JComboBox();
		for (int i = 0; i < speciesList.length; i++) {
			Species species = document.getModel().getSpecies(speciesList[i]);
			if (species.getBoundaryCondition()
					|| (!species.getConstant() && keepVar("", speciesList[i], false, true, false, false))) {
				reactantSpecies.addItem(speciesList[i]);
			}
		}
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
				reactantStoiciometry.setText(""
						+ myFormulaToString(reactant.getStoichiometryMath().getMath()));
			}
			else {
				reactantStoiciometry.setText("" + reactant.getStoichiometry());
			}
		}
		reactantsPanel.add(speciesLabel);
		reactantsPanel.add(reactantSpecies);
		reactantsPanel.add(stoiciLabel);
		reactantsPanel.add(reactantStoiciometry);
		if (speciesList.length == 0) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"There are no species availiable to be reactants."
							+ "\nAdd species to this sbml file first.", "No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(biosim.frame(), reactantsPanel, "Reactants Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String react;
			double val = 1.0;
			if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
				try {
					val = Double.parseDouble(reactantStoiciometry.getText().trim());
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(biosim.frame(), "The stoichiometry must be a real number.",
							"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (val <= 0) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"The stoichiometry value must be greater than 0.", "Enter A Valid Value",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				react = reactantSpecies.getSelectedItem() + " " + val;
			}
			else {
				react = reactantSpecies.getSelectedItem() + " " + reactantStoiciometry.getText().trim();
			}
			int index = -1;
			if (!error) {
				if (option.equals("OK")) {
					index = reactants.getSelectedIndex();
				}
				reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				reacta = Buttons.getList(reacta, reactants);
				reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index >= 0) {
					reactants.setSelectedIndex(index);
				}
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
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
					if (reactantStoiciometry.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry math must have formula.",
								"Enter Stoichiometry Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (myParseFormula(reactantStoiciometry.getText().trim()) == null) {
						JOptionPane.showMessageDialog(biosim.frame(), "Stoichiometry formula is not valid.",
								"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						ArrayList<String> invalidVars = getInvalidVariables(reactantStoiciometry.getText()
								.trim(), true, "", false);
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
							message = "Stoiciometry math contains unknown variables.\n\n"
									+ "Unknown variables:\n" + invalid;
							JTextArea messageArea = new JTextArea(message);
							messageArea.setLineWrap(true);
							messageArea.setWrapStyleWord(true);
							messageArea.setEditable(false);
							JScrollPane scrolls = new JScrollPane();
							scrolls.setMinimumSize(new Dimension(300, 300));
							scrolls.setPreferredSize(new Dimension(300, 300));
							scrolls.setViewportView(messageArea);
							JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Stoiciometry Math Error",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						if (!error) {
							error = checkNumFunctionArguments(myParseFormula(reactantStoiciometry.getText()
									.trim()));
						}
						if (!error) {
							if (myParseFormula(reactantStoiciometry.getText().trim()).isBoolean()) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"Stoichiometry math must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
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
					}
					else {
						StoichiometryMath sm = new StoichiometryMath(myParseFormula(reactantStoiciometry
								.getText().trim()));
						reactan.setStoichiometryMath(sm);
						reactan.setStoichiometry(1);
					}
					reacta[index] = react;
					sort(reacta);
					reactants.setListData(reacta);
					reactants.setSelectedIndex(index);
				}
				else {
					SpeciesReference reactan = new SpeciesReference();
					changedReactants.add(reactan);
					reactan.setSpecies((String) reactantSpecies.getSelectedItem());
					if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
						reactan.setStoichiometry(val);
					}
					else {
						StoichiometryMath sm = new StoichiometryMath(myParseFormula(reactantStoiciometry
								.getText().trim()));
						reactan.setStoichiometryMath(sm);
					}
					JList add = new JList();
					Object[] adding = { react };
					add.setListData(adding);
					add.setSelectedIndex(0);
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Buttons.add(reacta, reactants, add, false, null, null, null, null, null, null,
							biosim.frame());
					reacta = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						reacta[i] = (String) adding[i];
					}
					sort(reacta);
					reactants.setListData(reacta);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					reactants.setSelectedIndex(0);
				}
				change = true;
			}
			if (error) {
				value = JOptionPane.showOptionDialog(biosim.frame(), reactantsPanel, "Reactants Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
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

	public void setChanged(boolean change) {
		this.change = change;
	}

	/**
	 * Sorting function
	 */
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

	/**
	 * Create the SBML file
	 */
	public void createSBML(String stem, String direct) {
		try {
			FileOutputStream out = new FileOutputStream(new File(paramFile));
			out.write((refFile + "\n").getBytes());
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
			if (direct.equals(".") && !stem.equals("")) {
				direct = "";
			}
			FileOutputStream out = new FileOutputStream(new File(simDir + separator + stem + direct
					+ separator + file.split(separator)[file.split(separator).length - 1]));
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
	 * Checks consistency of the sbml file.
	 */
	public void checkOverDetermined() {
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
		long numErrors = document.checkConsistency();
		String message = "";
		for (long i = 0; i < numErrors; i++) {
			String error = document.getError(i).getMessage(); // .replace(". ",".\n");
			message += i + ":" + error + "\n";
		}
		if (numErrors > 0) {
			JOptionPane.showMessageDialog(biosim.frame(), "Algebraic rules make model overdetermined.",
					"Model is Overdetermined", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Checks consistency of the sbml file.
	 */
	public void check() {
		// Hack to avoid wierd bug.
		// By reloading the file before consistency checks, it seems to avoid a
		// crash when attempting to save a newly added parameter with no units
		SBMLReader reader = new SBMLReader();
		document = reader.readSBML(file);
		long numErrors = document.checkConsistency();
		String message = "";
		for (long i = 0; i < numErrors; i++) {
			String error = document.getError(i).getMessage(); // .replace(". ",".\n");
			message += i + ":" + error + "\n";
		}
		if (numErrors > 0) {
			JTextArea messageArea = new JTextArea(message);
			messageArea.setLineWrap(true);
			messageArea.setEditable(false);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(600, 600));
			scroll.setPreferredSize(new Dimension(600, 600));
			scroll.setViewportView(messageArea);
			JOptionPane.showMessageDialog(biosim.frame(), scroll, "SBML Errors and Warnings",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves the sbml file.
	 * 
	 * @param stem
	 */
	public void save(boolean run, String stem) {
		if (paramsOnly) {
			if (run) {
				for (int i = 0; i < biosim.getTab().getTabCount(); i++) {
					if (biosim.getTab().getTitleAt(i).equals(refFile)) {
						if (biosim.getTab().getComponentAt(i) instanceof SBML_Editor) {
							SBML_Editor sbml = ((SBML_Editor) (biosim.getTab().getComponentAt(i)));
							if (sbml.hasChanged()) {
								Object[] options = { "Yes", "No" };
								int value = JOptionPane
										.showOptionDialog(biosim.frame(), "Do you want to save changes to " + refFile
												+ " before running the simulation?", "Save Changes",
												JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
												options[0]);
								if (value == JOptionPane.YES_OPTION) {
									sbml.save(run, stem);
									SBMLReader reader = new SBMLReader();
									document = reader.readSBML(file);
								}
							}
						}
						else if (biosim.getTab().getComponentAt(i) instanceof GCM2SBMLEditor) {
							GCM2SBMLEditor gcm = ((GCM2SBMLEditor) (biosim.getTab().getComponentAt(i)));
							if (gcm.isDirty()) {
								Object[] options = { "Yes", "No" };
								int value = JOptionPane
										.showOptionDialog(biosim.frame(), "Do you want to save changes to " + refFile
												+ " before running the simulation?", "Save Changes",
												JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
												options[0]);
								if (value == JOptionPane.YES_OPTION) {
									gcm.save("gcm");
									GCMParser parser = new GCMParser(biosim.getRoot() + separator + refFile);
									GeneticNetwork network = parser.buildNetwork();
									network.outputSBML(file);
									SBMLReader reader = new SBMLReader();
									document = reader.readSBML(file);
								}
							}
						}
					}
				}
				ArrayList<String> sweepThese1 = new ArrayList<String>();
				ArrayList<ArrayList<Double>> sweep1 = new ArrayList<ArrayList<Double>>();
				ArrayList<String> sweepThese2 = new ArrayList<String>();
				ArrayList<ArrayList<Double>> sweep2 = new ArrayList<ArrayList<Double>>();
				for (String s : parameterChanges) {
					if (s.split(" ")[s.split(" ").length - 2].equals("Sweep")) {
						if ((s.split(" ")[s.split(" ").length - 1]).split(",")[3].replace(")", "").trim()
								.equals("1")) {
							sweepThese1.add(s.split(" ")[0]);
							double start = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[0].substring(1)
											.trim());
							double stop = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[1].trim());
							double step = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[2].trim());
							ArrayList<Double> add = new ArrayList<Double>();
							for (double i = start; i <= stop; i += step) {
								add.add(i);
							}
							sweep1.add(add);
						}
						else {
							sweepThese2.add(s.split(" ")[0]);
							double start = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[0].substring(1)
											.trim());
							double stop = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[1].trim());
							double step = Double
									.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[2].trim());
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
								new File(simDir + separator + stem + sweepTwo.replace("/", "-")).mkdir();
								createSBML(stem, sweepTwo);
								new Reb2SacThread(reb2sac).start(stem + sweepTwo.replace("/", "-"));
								reb2sac.emptyFrames();
							}
						}
						else {
							new File(simDir + separator + stem + sweep.replace("/", "-")).mkdir();
							createSBML(stem, sweep);
							new Reb2SacThread(reb2sac).start(stem + sweep.replace("/", "-"));
							reb2sac.emptyFrames();
						}
					}
				}
				else {
					if (!stem.equals("")) {
						new File(simDir + separator + stem).mkdir();
					}
					createSBML(stem, ".");
					if (!stem.equals("")) {
						new Reb2SacThread(reb2sac).start(stem);
					}
					else {
						new Reb2SacThread(reb2sac).start(".");
					}
					reb2sac.emptyFrames();
				}
			}
			else {
				if (!stem.equals("")) {
					new File(simDir + separator + stem).mkdir();
				}
				createSBML(stem, ".");
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
				biosim.updateViews(file.split(separator)[file.split(separator).length - 1]);
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(biosim.frame(), "Unable to save sbml file.",
						"Error Saving File", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void updateSBML(int tab, int tab2) {
		SBML_Editor sbml = new SBML_Editor(file, reb2sac, log, biosim, simDir, paramFile);
		((JTabbedPane) (biosim.getTab().getComponentAt(tab))).setComponentAt(tab2, sbml);
		reb2sac.setSbml(sbml);
		reb2sac.updateSpeciesList();
		((JTabbedPane) (biosim.getTab().getComponentAt(tab))).getComponentAt(tab2).setName(
				"SBML Editor");
	}

	/**
	 * Set the file name
	 */
	public void setFile(String newFile) {
		file = newFile;
	}

	/**
	 * Set the model ID
	 */
	public void setModelID(String modelID) {
		this.modelID.setText(modelID);
		document.getModel().setId(modelID);
	}

	/**
	 * Convert ASTNodes into a string
	 */
	public String myFormulaToString(ASTNode mathFormula) {
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

	/**
	 * Convert String into ASTNodes
	 */
	public ASTNode myParseFormula(String formula) {
		ASTNode mathFormula = libsbml.parseFormula(formula);
		if (mathFormula == null)
			return null;
		setTimeAndTrigVar(mathFormula);
		return mathFormula;
	}

	/**
	 * Recursive function to set time and trig functions
	 */
	public void setTimeAndTrigVar(ASTNode node) {
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

	/**
	 * Check the number of arguments to a function
	 */
	public boolean checkNumFunctionArguments(ASTNode node) {
		ListOf sbml = document.getModel().getListOfFunctionDefinitions();
		switch (node.getType()) {
		case libsbml.AST_FUNCTION_ABS:
		case libsbml.AST_FUNCTION_ARCCOS:
		case libsbml.AST_FUNCTION_ARCCOSH:
		case libsbml.AST_FUNCTION_ARCSIN:
		case libsbml.AST_FUNCTION_ARCSINH:
		case libsbml.AST_FUNCTION_ARCTAN:
		case libsbml.AST_FUNCTION_ARCTANH:
		case libsbml.AST_FUNCTION_ARCCOT:
		case libsbml.AST_FUNCTION_ARCCOTH:
		case libsbml.AST_FUNCTION_ARCCSC:
		case libsbml.AST_FUNCTION_ARCCSCH:
		case libsbml.AST_FUNCTION_ARCSEC:
		case libsbml.AST_FUNCTION_ARCSECH:
		case libsbml.AST_FUNCTION_COS:
		case libsbml.AST_FUNCTION_COSH:
		case libsbml.AST_FUNCTION_SIN:
		case libsbml.AST_FUNCTION_SINH:
		case libsbml.AST_FUNCTION_TAN:
		case libsbml.AST_FUNCTION_TANH:
		case libsbml.AST_FUNCTION_COT:
		case libsbml.AST_FUNCTION_COTH:
		case libsbml.AST_FUNCTION_CSC:
		case libsbml.AST_FUNCTION_CSCH:
		case libsbml.AST_FUNCTION_SEC:
		case libsbml.AST_FUNCTION_SECH:
		case libsbml.AST_FUNCTION_CEILING:
		case libsbml.AST_FUNCTION_FACTORIAL:
		case libsbml.AST_FUNCTION_EXP:
		case libsbml.AST_FUNCTION_FLOOR:
		case libsbml.AST_FUNCTION_LN:
		case libsbml.AST_FUNCTION_LOG:
			if (node.getNumChildren() != 1) {
				JOptionPane.showMessageDialog(biosim.frame(), "Expected 1 argument for " + node.getName()
						+ " but found " + node.getNumChildren() + ".", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(), "Argument for " + node.getName()
						+ " function must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_LOGICAL_NOT:
			if (node.getNumChildren() != 1) {
				JOptionPane.showMessageDialog(biosim.frame(), "Expected 1 argument for " + node.getName()
						+ " but found " + node.getNumChildren() + ".", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (!node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument for not function must be of type Boolean.", "Boolean Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_LOGICAL_AND:
		case libsbml.AST_LOGICAL_OR:
		case libsbml.AST_LOGICAL_XOR:
			if (node.getNumChildren() != 2) {
				JOptionPane.showMessageDialog(biosim.frame(), "Expected 2 arguments for " + node.getName()
						+ " but found " + node.getNumChildren() + ".", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (!node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(), "Argument 1 for " + node.getName()
						+ " function is not of type Boolean.", "Boolean Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (!node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(), "Argument 2 for " + node.getName()
						+ " function is not of type Boolean.", "Boolean Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_PLUS:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 1 for + operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 2 for + operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_MINUS:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 1 for - operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 2 for - operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_TIMES:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 1 for * operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 2 for * operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_DIVIDE:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 1 for / operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 2 for / operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_POWER:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 1 for ^ operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Argument 2 for ^ operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_FUNCTION_DELAY:
		case libsbml.AST_FUNCTION_POWER:
		case libsbml.AST_FUNCTION_ROOT:
		case libsbml.AST_RELATIONAL_GEQ:
		case libsbml.AST_RELATIONAL_LEQ:
		case libsbml.AST_RELATIONAL_LT:
		case libsbml.AST_RELATIONAL_GT:
			if (node.getNumChildren() != 2) {
				JOptionPane.showMessageDialog(biosim.frame(), "Expected 2 arguments for " + node.getName()
						+ " but found " + node.getNumChildren() + ".", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(), "Argument 1 for " + node.getName()
						+ " function must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(biosim.frame(), "Argument 2 for " + node.getName()
						+ " function must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_RELATIONAL_EQ:
		case libsbml.AST_RELATIONAL_NEQ:
			if (node.getNumChildren() != 2) {
				JOptionPane.showMessageDialog(biosim.frame(), "Expected 2 arguments for " + node.getName()
						+ " but found " + node.getNumChildren() + ".", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if ((node.getChild(0).isBoolean() && !node.getChild(1).isBoolean())
					|| (!node.getChild(0).isBoolean() && node.getChild(1).isBoolean())) {
				JOptionPane.showMessageDialog(biosim.frame(), "Arguments for " + node.getName()
						+ " function must either both be numbers or Booleans.", "Argument Mismatch",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_FUNCTION_PIECEWISE:
			if (node.getNumChildren() < 1) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Piecewise function requires at least 1 argument.", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			for (int i = 1; i < node.getNumChildren(); i += 2) {
				if (!node.getChild(i).isBoolean()) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Even arguments of piecewise function must be of type Boolean.", "Boolean Expected",
							JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			int pieceType = -1;
			for (int i = 0; i < node.getNumChildren(); i += 2) {
				if (node.getChild(i).isBoolean()) {
					if (pieceType == 2) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"All odd arguments of a piecewise function must agree.", "Type Mismatch",
								JOptionPane.ERROR_MESSAGE);
						return true;
					}
					pieceType = 1;
				}
				else {
					if (pieceType == 1) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"All odd arguments of a piecewise function must agree.", "Type Mismatch",
								JOptionPane.ERROR_MESSAGE);
						return true;
					}
					pieceType = 2;
				}
			}
		case libsbml.AST_FUNCTION:
			for (int i = 0; i < document.getModel().getNumFunctionDefinitions(); i++) {
				if (((FunctionDefinition) sbml.get(i)).getId().equals(node.getName())) {
					long numArgs = ((FunctionDefinition) sbml.get(i)).getNumArguments();
					if (numArgs != node.getNumChildren()) {
						JOptionPane.showMessageDialog(biosim.frame(), "Expected " + numArgs
								+ " argument(s) for " + node.getName() + " but found " + node.getNumChildren()
								+ ".", "Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
						return true;
					}
					break;
				}
			}
			break;
		case libsbml.AST_NAME:
			if (node.getName().equals("abs") || node.getName().equals("arccos")
					|| node.getName().equals("arccosh") || node.getName().equals("arcsin")
					|| node.getName().equals("arcsinh") || node.getName().equals("arctan")
					|| node.getName().equals("arctanh") || node.getName().equals("arccot")
					|| node.getName().equals("arccoth") || node.getName().equals("arccsc")
					|| node.getName().equals("arccsch") || node.getName().equals("arcsec")
					|| node.getName().equals("arcsech") || node.getName().equals("acos")
					|| node.getName().equals("acosh") || node.getName().equals("asin")
					|| node.getName().equals("asinh") || node.getName().equals("atan")
					|| node.getName().equals("atanh") || node.getName().equals("acot")
					|| node.getName().equals("acoth") || node.getName().equals("acsc")
					|| node.getName().equals("acsch") || node.getName().equals("asec")
					|| node.getName().equals("asech") || node.getName().equals("cos")
					|| node.getName().equals("cosh") || node.getName().equals("cot")
					|| node.getName().equals("coth") || node.getName().equals("csc")
					|| node.getName().equals("csch") || node.getName().equals("ceil")
					|| node.getName().equals("factorial") || node.getName().equals("exp")
					|| node.getName().equals("floor") || node.getName().equals("ln")
					|| node.getName().equals("log") || node.getName().equals("sqr")
					|| node.getName().equals("log10") || node.getName().equals("sqrt")
					|| node.getName().equals("sec") || node.getName().equals("sech")
					|| node.getName().equals("sin") || node.getName().equals("sinh")
					|| node.getName().equals("tan") || node.getName().equals("tanh")
					|| node.getName().equals("not")) {
				JOptionPane.showMessageDialog(biosim.frame(), "Expected 1 argument for " + node.getName()
						+ " but found 0.", "Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getName().equals("and") || node.getName().equals("or")
					|| node.getName().equals("xor") || node.getName().equals("pow")
					|| node.getName().equals("eq") || node.getName().equals("geq")
					|| node.getName().equals("leq") || node.getName().equals("gt")
					|| node.getName().equals("neq") || node.getName().equals("lt")
					|| node.getName().equals("delay") || node.getName().equals("root")) {
				JOptionPane.showMessageDialog(biosim.frame(), "Expected 2 arguments for " + node.getName()
						+ " but found 0.", "Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getName().equals("piecewise")) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Piecewise function requires at least 1 argument.", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			for (int i = 0; i < document.getModel().getNumFunctionDefinitions(); i++) {
				if (((FunctionDefinition) sbml.get(i)).getId().equals(node.getName())) {
					long numArgs = ((FunctionDefinition) sbml.get(i)).getNumArguments();
					JOptionPane.showMessageDialog(biosim.frame(), "Expected " + numArgs + " argument(s) for "
							+ node.getName() + " but found 0.", "Number of Arguments Incorrect",
							JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			break;
		}
		for (int c = 0; c < node.getNumChildren(); c++) {
			if (checkNumFunctionArguments(node.getChild(c))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the units of a rate rule
	 */
	public boolean checkRateRuleUnits(Rule rule) {
		document.getModel().populateListFormulaUnitsData();
		if (rule.containsUndeclaredUnits()) {
		    if (biosim.checkUndeclared) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Rate rule contains literals numbers or parameters with undeclared units.\n"
							+ "Therefore, it is not possible to completely verify the consistency of the units.",
					"Contains Undeclared Units", JOptionPane.WARNING_MESSAGE);
		    }
		    return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = rule.getDerivedUnitDefinition();
			UnitDefinition unitDefVar;
			Species species = document.getModel().getSpecies(rule.getVariable());
			Compartment compartment = document.getModel().getCompartment(rule.getVariable());
			Parameter parameter = document.getModel().getParameter(rule.getVariable());
			if (species != null) {
				unitDefVar = species.getDerivedUnitDefinition();
			}
			else if (compartment != null) {
				unitDefVar = compartment.getDerivedUnitDefinition();
			}
			else {
				unitDefVar = parameter.getDerivedUnitDefinition();
			}
			if (document.getModel().getUnitDefinition("time") != null) {
				UnitDefinition timeUnitDef = document.getModel().getUnitDefinition("time");
				for (int i = 0; i < timeUnitDef.getNumUnits(); i++) {
					Unit timeUnit = timeUnitDef.getUnit(i);
					Unit recTimeUnit = new Unit(timeUnit.getKind(), timeUnit.getExponent() * (-1), timeUnit
							.getScale(), timeUnit.getMultiplier());
					unitDefVar.addUnit(recTimeUnit);
				}
			}
			else {
				Unit unit = new Unit("second", -1, 0, 1.0);
				unitDefVar.addUnit(unit);
			}
			if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Units on the left and right-hand side of the rate rule do not agree.",
						"Units Do Not Match", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the units of an assignment rule
	 */
	public boolean checkAssignmentRuleUnits(Rule rule) {
		document.getModel().populateListFormulaUnitsData();
		if (rule.containsUndeclaredUnits()) {
		    if (biosim.checkUndeclared) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Assignment rule contains literals numbers or parameters with undeclared units.\n"
							+ "Therefore, it is not possible to completely verify the consistency of the units.",
					"Contains Undeclared Units", JOptionPane.WARNING_MESSAGE);
		    }
		    return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = rule.getDerivedUnitDefinition();
			UnitDefinition unitDefVar;
			Species species = document.getModel().getSpecies(rule.getVariable());
			Compartment compartment = document.getModel().getCompartment(rule.getVariable());
			Parameter parameter = document.getModel().getParameter(rule.getVariable());
			if (species != null) {
				unitDefVar = species.getDerivedUnitDefinition();
			}
			else if (compartment != null) {
				unitDefVar = compartment.getDerivedUnitDefinition();
			}
			else {
				unitDefVar = parameter.getDerivedUnitDefinition();
			}
			if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Units on the left and right-hand side of the assignment rule do not agree.",
						"Units Do Not Match", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the units of an initial assignment
	 */
	public boolean checkInitialAssignmentUnits(InitialAssignment init) {
		document.getModel().populateListFormulaUnitsData();
		if (init.containsUndeclaredUnits()) {
		    if (biosim.checkUndeclared) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Initial assignment contains literals numbers or parameters with undeclared units.\n"
							+ "Therefore, it is not possible to completely verify the consistency of the units.",
					"Contains Undeclared Units", JOptionPane.WARNING_MESSAGE);
		    }
		    return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = init.getDerivedUnitDefinition();
			UnitDefinition unitDefVar;
			Species species = document.getModel().getSpecies(init.getSymbol());
			Compartment compartment = document.getModel().getCompartment(init.getSymbol());
			Parameter parameter = document.getModel().getParameter(init.getSymbol());
			if (species != null) {
				unitDefVar = species.getDerivedUnitDefinition();
			}
			else if (compartment != null) {
				unitDefVar = compartment.getDerivedUnitDefinition();
			}
			else {
				unitDefVar = parameter.getDerivedUnitDefinition();
			}
			if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Units on the left and right-hand side of the initial assignment do not agree.",
						"Units Do Not Match", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			// for (int i = 0; i < unitDef.getNumUnits(); i++) {
			// Unit unit = unitDef.getUnit(i);
			// System.out.println(unit.getKind() + " Exp = " + unit.getExponent() + "
			// Mult = " + unit.getMultiplier() + " Scale = " + unit.getScale());
			// }
			// for (int i = 0; i < unitDefVar.getNumUnits(); i++) {
			// Unit unit = unitDefVar.getUnit(i);
			// System.out.println(unit.getKind() + " Exp = " + unit.getExponent() + "
			// Mult = " + unit.getMultiplier() + " Scale = " + unit.getScale());
			// }
		}
		return false;
	}

	/**
	 * Check the units of an event assignment
	 */
	public boolean checkEventAssignmentUnits(EventAssignment assign) {
		document.getModel().populateListFormulaUnitsData();
		if (assign.containsUndeclaredUnits()) {
		    if (biosim.checkUndeclared) {
			JOptionPane.showMessageDialog(biosim.frame(), "Event assignment to " + assign.getVariable()
					+ " contains literals numbers or parameters with undeclared units.\n"
					+ "Therefore, it is not possible to completely verify the consistency of the units.",
					"Contains Undeclared Units", JOptionPane.WARNING_MESSAGE);
		    }
		    return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = assign.getDerivedUnitDefinition();
			UnitDefinition unitDefVar;
			Species species = document.getModel().getSpecies(assign.getVariable());
			Compartment compartment = document.getModel().getCompartment(assign.getVariable());
			Parameter parameter = document.getModel().getParameter(assign.getVariable());
			if (species != null) {
				unitDefVar = species.getDerivedUnitDefinition();
			}
			else if (compartment != null) {
				unitDefVar = compartment.getDerivedUnitDefinition();
			}
			else {
				unitDefVar = parameter.getDerivedUnitDefinition();
			}
			if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Units on the left and right-hand side for the event assignment "
								+ assign.getVariable() + " do not agree.", "Units Do Not Match",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the units of an event delay
	 */
	public boolean checkEventDelayUnits(Delay delay) {
		document.getModel().populateListFormulaUnitsData();
		if (delay.containsUndeclaredUnits()) {
		    if (biosim.checkUndeclared) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Event assignment delay contains literals numbers or parameters with undeclared units.\n"
							+ "Therefore, it is not possible to completely verify the consistency of the units.",
					"Contains Undeclared Units", JOptionPane.WARNING_MESSAGE);
		    }
		    return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = delay.getDerivedUnitDefinition();
			if (!(unitDef.isVariantOfTime())) {
				JOptionPane.showMessageDialog(biosim.frame(), "Event delay should be units of time.",
						"Event Delay Not Time Units", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the units of a kinetic law
	 */
	public boolean checkKineticLawUnits(KineticLaw law) {
		document.getModel().populateListFormulaUnitsData();
		if (law.containsUndeclaredUnits()) {
		    if (biosim.checkUndeclared) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Kinetic law contains literals numbers or parameters with undeclared units.\n"
							+ "Therefore, it is not possible to completely verify the consistency of the units.",
					"Contains Undeclared Units", JOptionPane.WARNING_MESSAGE);
		    }
		    return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = law.getDerivedUnitDefinition();
			UnitDefinition unitDefLaw = new UnitDefinition();
			if (document.getModel().getUnitDefinition("substance") != null) {
				UnitDefinition subUnitDef = document.getModel().getUnitDefinition("substance");
				for (int i = 0; i < subUnitDef.getNumUnits(); i++) {
					Unit subUnit = subUnitDef.getUnit(i);
					unitDefLaw.addUnit(subUnit);
				}
			}
			else {
				Unit unit = new Unit("mole", 1, 0, 1.0);
				unitDefLaw.addUnit(unit);
			}
			if (document.getModel().getUnitDefinition("time") != null) {
				UnitDefinition timeUnitDef = document.getModel().getUnitDefinition("time");
				for (int i = 0; i < timeUnitDef.getNumUnits(); i++) {
					Unit timeUnit = timeUnitDef.getUnit(i);
					Unit recTimeUnit = new Unit(timeUnit.getKind(), timeUnit.getExponent() * (-1), timeUnit
							.getScale(), timeUnit.getMultiplier());
					unitDefLaw.addUnit(recTimeUnit);
				}
			}
			else {
				Unit unit = new Unit("second", -1, 0, 1.0);
				unitDefLaw.addUnit(unit);
			}
			if (!UnitDefinition.areEquivalent(unitDef, unitDefLaw)) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"Kinetic law units should be substance / time.", "Units Do Not Match",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}
}
