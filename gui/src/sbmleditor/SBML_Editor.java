package sbmleditor.core.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.sbml.libsbml.*;

import reb2sac.core.gui.Reb2Sac;
import buttons.core.gui.*;

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
public class SBML_Editor extends JPanel implements ActionListener, MouseListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8236967001410906807L;

	private SBMLDocument document; // sbml document

	private String file; // SBML file

	private JButton addCompart, removeCompart; // compartment buttons

	private JTextField compart; // compartment text field

	private Object[] comps; // array of compartments

	private JList compartments; // JList of compartments

	private JButton addSpec, removeSpec, editSpec; // species buttons

	private Object[] specs; // array of species

	private JList species; // JList of species

	private JTextField name, ID, init; // species text fields

	private JComboBox comp; // compartment combo box

	private JButton addSaveSpecies, cancelSpecies; // edit species buttons

	private JFrame speciesFrame; // frame for editting species

	/*
	 * new, load, save, view, and close menu items
	 */
	private JMenuItem newFile, load, save, view, close;

	private boolean amount; // determines if the species have amount set

	private boolean id; // determines if the compartments have id set

	private boolean model; // determines if the model has id set

	private boolean usingParamId; // determines if the parameters have id set

	private boolean usingReacId; // determines if the reactions have id set

	private boolean change; // determines if any changes were made

	private JTextField modelID; // the model's ID

	private JList reactions; // JList of reactions

	private Object[] reacts; // array of reactions

	private JButton addReac, removeReac, editReac; // reactions buttons

	private JFrame reactionFrame; // frame for editting reactions

	private JList parameters; // JList of parameters

	private Object[] params; // array of parameters

	private JButton addParam, removeParam, editParam; // parameters buttons

	private JFrame parameterFrame; // frame for editting parameters

	/*
	 * parameters text fields
	 */
	private JTextField paramName, paramID, paramValue;

	private JButton addSaveParams, cancelParams; // edit parameters buttons

	private JList reacParameters; // JList of reaction parameters

	private Object[] reacParams; // array of reaction parameters

	/*
	 * reaction parameters buttons
	 */
	private JButton reacAddParam, reacRemoveParam, reacEditParam;

	/*
	 * frame for editting reaction parameters
	 */
	private JFrame reacParameterFrame;

	/*
	 * determines if the reations parameters have id set
	 */
	private boolean reacUsingParamId;

	/*
	 * reaction parameters text fields
	 */
	private JTextField reacParamName, reacParamID, reacParamValue;

	/*
	 * edit reaction parameters buttons
	 */
	private JButton reacAddSaveParams, reacCancelParams;

	private ArrayList<Parameter> changedParameters; // ArrayList of parameters

	private JTextField reacName, reacID; // reaction name and id text fields

	private JComboBox reacReverse; // reaction reversible combo box

	private JButton addSaveReactions, cancelReactions; // buttons for reactions

	/*
	 * reactant buttons
	 */
	private JButton addReactant, removeReactant, editReactant;

	private JList reactants; // JList for reactants

	private Object[] reacta; // array for reactants

	/*
	 * ArrayList of reactants
	 */
	private ArrayList<SpeciesReference> changedReactants;

	/*
	 * product buttons
	 */
	private JButton addProduct, removeProduct, editProduct;

	private JList products; // JList for products

	private Object[] product; // array for products

	/*
	 * ArrayList of products
	 */
	private ArrayList<SpeciesReference> changedProducts;

	private JFrame productFrame; // product editing frame

	private JFrame reactantFrame; // reactant editing frame

	private JButton addSaveProducts, cancelProducts; // edit product buttons

	private JButton addSaveReactants, cancelReactants; // edit reactant buttons

	private JComboBox productSpecies; // ComboBox for product editing

	private JTextField productStoiciometry; // text field for editing products

	private JComboBox reactantSpecies; // ComboBox for reactant editing

	/*
	 * text field for editing reactants
	 */
	private JTextField reactantStoiciometry;

	private JTextArea kineticLaw; // text area for editing kinetic law

	private String kineticL; // kinetic law

	private Reb2Sac reb2sac; // reb2sac options

	private JButton saveNoRun, run; // save and run buttons

	/**
	 * Creates a new SBML_Editor and sets up the frame where the user can edit a
	 * new sbml file.
	 */
	public SBML_Editor(Reb2Sac reb2sac) {
		this.reb2sac = reb2sac;
		createSbmlFrame("");
	}

	/**
	 * Creates a new SBML_Editor and sets up the frame where the user can edit
	 * the sbml file given to this constructor.
	 */
	public SBML_Editor(String file, Reb2Sac reb2sac) {
		this.reb2sac = reb2sac;
		createSbmlFrame(file);
	}

	/**
	 * Private helper method that helps create the sbml frame.
	 */
	private void createSbmlFrame(String file) {
		// intitializes the member variables
		if (!file.equals("")) {
			this.file = file;
		} else {
			this.file = null;
		}

		/*
		 * // creates the sbml frame sbmlFrame = new JFrame("SBML Editor");
		 * WindowListener w = new WindowListener() { public void
		 * windowClosing(WindowEvent arg0) { sbmlFrame.dispose(); }
		 * 
		 * public void windowOpened(WindowEvent arg0) { }
		 * 
		 * public void windowClosed(WindowEvent arg0) { }
		 * 
		 * public void windowIconified(WindowEvent arg0) { }
		 * 
		 * public void windowDeiconified(WindowEvent arg0) { }
		 * 
		 * public void windowActivated(WindowEvent arg0) { }
		 * 
		 * public void windowDeactivated(WindowEvent arg0) { } };
		 * sbmlFrame.addWindowListener(w);
		 */

		// creates the sbml reader and reads the sbml file
		Model model;
		if (!file.equals("")) {
			SBMLReader reader = new SBMLReader();
			document = reader.readSBML(file);
			model = document.getModel();
		} else {
			document = new SBMLDocument();
			model = document.createModel();
		}

		// sets up the compartments editor
		JPanel comp = new JPanel(new BorderLayout());
		JPanel addComp = new JPanel(new BorderLayout());
		compart = new JTextField(15);
		JPanel addRemComp = new JPanel();
		addCompart = new JButton("Add Compartment");
		removeCompart = new JButton("Remove Compartment");
		addRemComp.add(addCompart);
		addRemComp.add(removeCompart);
		addCompart.addActionListener(this);
		removeCompart.addActionListener(this);
		addComp.add(compart, "North");
		addComp.add(addRemComp, "South");
		JLabel compartmentsLabel = new JLabel("List Of Compartments:");
		compartments = new JList();
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 200));
		scroll.setPreferredSize(new Dimension(276, 132));
		scroll.setViewportView(compartments);
		ListOf listOfCompartments = model.getListOfCompartments();
		ArrayList<String> sbml = new ArrayList<String>();
		id = false;
		for (int i = 0; i < model.getNumCompartments(); i++) {
			Compartment compartment = (Compartment) listOfCompartments.get(i);
			if (compartment.isSetId()) {
				sbml.add(compartment.getId());
			} else {
				sbml.add(compartment.getName());
			}
			id = compartment.isSetId();
		}
		comps = sbml.toArray();
		compartments.setListData(comps);
		compartments.addMouseListener(this);
		comp.add(compartmentsLabel, "North");
		comp.add(scroll, "Center");
		comp.add(addComp, "South");

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
		JLabel speciesLabel = new JLabel("List Of Species:");
		species = new JList();
		species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll1 = new JScrollPane();
		scroll1.setMinimumSize(new Dimension(260, 220));
		scroll1.setPreferredSize(new Dimension(276, 152));
		scroll1.setViewportView(species);
		ListOf listOfSpecies = model.getListOfSpecies();
		sbml = new ArrayList<String>();
		amount = false;
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = (Species) listOfSpecies.get(i);
			amount = species.isSetInitialAmount();
			sbml.add(species.getName() + " " + species.getCompartment() + " "
					+ species.getInitialAmount());
		}
		specs = sbml.toArray();
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
		addReacs.add(addReac);
		addReacs.add(removeReac);
		addReacs.add(editReac);
		addReac.addActionListener(this);
		removeReac.addActionListener(this);
		editReac.addActionListener(this);
		JLabel reactionsLabel = new JLabel("List Of Reactions:");
		reactions = new JList();
		reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(260, 220));
		scroll2.setPreferredSize(new Dimension(276, 152));
		scroll2.setViewportView(reactions);
		ListOf listOfReactions = model.getListOfReactions();
		sbml = new ArrayList<String>();
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) listOfReactions.get(i);
			if (reaction.isSetName()) {
				usingReacId = false;
				sbml.add(reaction.getName());
			} else {
				usingReacId = true;
				sbml.add(reaction.getId());
			}
		}
		reacts = sbml.toArray();
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
		JLabel parametersLabel = new JLabel("List Of Parameters Defined Outside The Kinetic Law:");
		parameters = new JList();
		parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll3 = new JScrollPane();
		scroll3.setMinimumSize(new Dimension(260, 220));
		scroll3.setPreferredSize(new Dimension(276, 152));
		scroll3.setViewportView(parameters);
		ListOf listOfParameters = model.getListOfParameters();
		sbml = new ArrayList<String>();
		usingParamId = false;
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameter = (Parameter) listOfParameters.get(i);
			if (parameter.isSetId()) {
				usingParamId = true;
				sbml.add(parameter.getId() + " " + parameter.getValue());
			} else {
				sbml.add(parameter.getName() + " " + parameter.getValue());
			}
		}
		params = sbml.toArray();
		parameters.setListData(params);
		parameters.setSelectedIndex(0);
		parameters.addMouseListener(this);
		param.add(parametersLabel, "North");
		param.add(scroll3, "Center");
		param.add(addParams, "South");

		// adds the main panel to the frame and displays it
		// JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel mainPanelNorth = new JPanel();
		JPanel mainPanelCenter = new JPanel(new BorderLayout());
		JPanel mainPanelCenterUp = new JPanel();
		JPanel mainPanelCenterDown = new JPanel();
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		newFile = new JMenuItem("New");
		load = new JMenuItem("Open");
		save = new JMenuItem("Save");
		view = new JMenuItem("View As Text File");
		close = new JMenuItem("Close");
		newFile.addActionListener(this);
		load.addActionListener(this);
		save.addActionListener(this);
		view.addActionListener(this);
		close.addActionListener(this);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		view.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		newFile.setMnemonic(KeyEvent.VK_N);
		load.setMnemonic(KeyEvent.VK_O);
		save.setMnemonic(KeyEvent.VK_S);
		view.setMnemonic(KeyEvent.VK_T);
		close.setMnemonic(KeyEvent.VK_C);
		fileMenu.add(newFile);
		fileMenu.add(load);
		fileMenu.addSeparator();
		fileMenu.add(save);
		viewMenu.add(view);
		fileMenu.addSeparator();
		fileMenu.add(close);
		mainPanelCenterUp.add(comp);
		mainPanelCenterUp.add(spec);
		mainPanelCenterDown.add(reac);
		mainPanelCenterDown.add(param);
		mainPanelCenter.add(mainPanelCenterUp, "North");
		mainPanelCenter.add(mainPanelCenterDown, "South");
		if (model.isSetId()) {
			modelID = new JTextField(model.getId(), 20);
			this.model = true;
		} else {
			modelID = new JTextField(model.getName(), 20);
			this.model = false;
		}
		modelID.addKeyListener(this);
		JLabel modelIDLabel = new JLabel("Model ID:");
		mainPanelNorth.add(modelIDLabel);
		mainPanelNorth.add(modelID);
		this.setLayout(new BorderLayout());
		this.add(mainPanelNorth, "North");
		this.add(mainPanelCenter, "Center");
		change = false;
		if (reb2sac != null) {
			saveNoRun = new JButton("Save");
			run = new JButton("Save And Run");
			saveNoRun.setMnemonic(KeyEvent.VK_S);
			run.setMnemonic(KeyEvent.VK_R);
			saveNoRun.addActionListener(this);
			run.addActionListener(this);
			JPanel saveRun = new JPanel();
			saveRun.add(saveNoRun);
			saveRun.add(run);
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null, saveRun);
			this.add(splitPane, "South");
		}
		/*
		 * sbmlFrame.setContentPane(mainPanel); sbmlFrame.setJMenuBar(menuBar);
		 * sbmlFrame.pack(); Dimension screenSize; try { Toolkit tk =
		 * Toolkit.getDefaultToolkit(); screenSize = tk.getScreenSize(); } catch
		 * (AWTError awe) { screenSize = new Dimension(640, 480); } Dimension
		 * frameSize = sbmlFrame.getSize();
		 * 
		 * if (frameSize.height > screenSize.height) { frameSize.height =
		 * screenSize.height; } if (frameSize.width > screenSize.width) {
		 * frameSize.width = screenSize.width; } int x = screenSize.width / 2 -
		 * frameSize.width / 2; int y = screenSize.height / 2 - frameSize.height /
		 * 2; sbmlFrame.setLocation(x, y); change = false;
		 * sbmlFrame.setVisible(true);
		 */
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
		// if the save button is clicked
		else if (e.getSource() == saveNoRun) {
			reb2sac.getSaveButton().doClick();
		}
		// if the add comparment button is clicked
		else if (e.getSource() == addCompart) {
			if (!compart.getText().trim().equals("")) {
				JList add = new JList();
				Object[] adding = { compart.getText().trim() };
				add.setListData(adding);
				add.setSelectedIndex(0);
				comps = Buttons.add(comps, compartments, add, false, null, null, null, null, null,
						null, null, this);
				if (id) {
					document.getModel().createCompartment().setId(compart.getText().trim());
				} else {
					document.getModel().createCompartment().setName(compart.getText().trim());
				}
				change = true;
			}
		}
		// if the remove compartment button is clicked
		else if (e.getSource() == removeCompart) {
			document.getModel().getListOfCompartments().remove(compartments.getSelectedIndex());
			Buttons.remove(compartments, comps);
		}
		// if the add species button is clicked
		else if (e.getSource() == addSpec) {
			speciesEditor("Add");
		}
		// if the edit species button is clicked
		else if (e.getSource() == editSpec) {
			speciesEditor("Save");
		}
		// if the remove species button is clicked
		else if (e.getSource() == removeSpec) {
			if (species.getSelectedIndex() != -1) {
				Model model = document.getModel();
				boolean remove = true;
				ArrayList<String> reactantsUsing = new ArrayList<String>();
				ArrayList<String> productsUsing = new ArrayList<String>();
				for (int i = 0; i < model.getNumReactions(); i++) {
					Reaction reaction = (Reaction) document.getModel().getListOfReactions().get(i);
					for (int j = 0; j < reaction.getNumProducts(); j++) {
						if (reaction.getProduct(j).isSetSpecies()) {
							String specRef = reaction.getProduct(j).getSpecies();
							if (((Species) model.getListOfSpecies().get(species.getSelectedIndex()))
									.getName().equals(specRef)) {
								remove = false;
								if (usingReacId) {
									productsUsing.add(reaction.getId());
								} else {
									productsUsing.add(reaction.getName());
								}
							}
						}
					}
					for (int j = 0; j < reaction.getNumReactants(); j++) {
						if (reaction.getReactant(j).isSetSpecies()) {
							String specRef = reaction.getReactant(j).getSpecies();
							if (((Species) model.getListOfSpecies().get(species.getSelectedIndex()))
									.getName().equals(specRef)) {
								remove = false;
								if (usingReacId) {
									reactantsUsing.add(reaction.getId());
								} else {
									reactantsUsing.add(reaction.getName());
								}
							}
						}
					}
				}
				if (remove) {
					model.getListOfSpecies().remove(species.getSelectedIndex());
					species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					Buttons.remove(species, specs);
					species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					species.setSelectedIndex(0);
				} else {
					String reactants = "";
					String products = "";
					for (int i = 0; i < reactantsUsing.size(); i++) {
						if (i == reactantsUsing.size() - 1) {
							reactants += reactantsUsing.get(i);
						} else {
							reactants += reactantsUsing.get(i) + "\n";
						}
					}
					for (int i = 0; i < productsUsing.size(); i++) {
						if (i == productsUsing.size() - 1) {
							products += productsUsing.get(i);
						} else {
							products += productsUsing.get(i) + "\n";
						}
					}
					String message = "Unable to remove the selected species.";
					if (reactantsUsing.size() != 0) {
						message += "\n\nIt is used as a reactant in the following reactions:\n"
								+ reactants;
					}
					if (productsUsing.size() != 0) {
						message += "\n\nIt is used as a product in the following reactions:\n"
								+ products;
					}
					JTextArea messageArea = new JTextArea(message);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(300, 300));
					scroll.setPreferredSize(new Dimension(300, 300));
					scroll.setViewportView(messageArea);
					JOptionPane.showMessageDialog(speciesFrame, scroll, "Unable To Remove Species",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the add reactions button is clicked
		else if (e.getSource() == addReac) {
			Reaction react = document.getModel().createReaction();
			react.setKineticLaw(new KineticLaw());
			reactionsEditor("Add");
		}
		// if the edit reactions button is clicked
		else if (e.getSource() == editReac) {
			reactionsEditor("Save");
		}
		// if the remove reactions button is clicked
		else if (e.getSource() == removeReac) {
			if (reactions.getSelectedIndex() != -1) {
				document.getModel().getListOfReactions().remove(reactions.getSelectedIndex());
				reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Buttons.remove(reactions, reacts);
				reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				reactions.setSelectedIndex(0);
			}
		}
		// if the add parameters button is clicked
		else if (e.getSource() == addParam) {
			parametersEditor("Add");
		}
		// if the edit parameters button is clicked
		else if (e.getSource() == editParam) {
			parametersEditor("Save");
		}
		// if the remove parameters button is clicked
		else if (e.getSource() == removeParam) {
			if (parameters.getSelectedIndex() != -1) {
				document.getModel().getListOfParameters().remove(parameters.getSelectedIndex());
				parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Buttons.remove(parameters, params);
				parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				parameters.setSelectedIndex(0);
			}
		}
		// if the add or save species button is clicked
		else if (e.getSource() == addSaveSpecies) {
			if (name.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(speciesFrame,
						"You must enter a name into the name field!", "Enter A Name",
						JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					double initial = Double.parseDouble(init.getText().trim());
					String addSpec = name.getText().trim() + " " + comp.getSelectedItem() + " "
							+ initial;
					if (addSaveSpecies.getText().equals("Save")) {
						int index = species.getSelectedIndex();
						species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						specs = Buttons.getList(specs, species);
						species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						Species specie = document.getModel().getSpecies(index);
						String speciesName = specie.getName();
						specie.setName(name.getText().trim());
						specie.setCompartment((String) comp.getSelectedItem());
						specie.setId(ID.getText().trim());
						Model model = document.getModel();
						for (int i = 0; i < model.getNumReactions(); i++) {
							Reaction reaction = (Reaction) document.getModel().getListOfReactions()
									.get(i);
							for (int j = 0; j < reaction.getNumProducts(); j++) {
								if (reaction.getProduct(j).isSetSpecies()) {
									SpeciesReference specRef = reaction.getProduct(j);
									if (speciesName.equals(specRef.getSpecies())) {
										specRef.setSpecies(specie.getName());
									}
								}
							}
							for (int j = 0; j < reaction.getNumReactants(); j++) {
								if (reaction.getReactant(j).isSetSpecies()) {
									SpeciesReference specRef = reaction.getReactant(j);
									if (speciesName.equals(specRef.getSpecies())) {
										specRef.setSpecies(specie.getName());
									}
								}
							}
						}
						if (amount) {
							specie.setInitialAmount(initial);
						} else {
							specie.setInitialConcentration(initial);
						}
						specs[index] = addSpec;
						species.setListData(specs);
						species.setSelectedIndex(index);
					} else {
						int index = species.getSelectedIndex();
						Species specie = document.getModel().createSpecies();
						specie.setName(name.getText().trim());
						specie.setCompartment((String) comp.getSelectedItem());
						specie.setId(ID.getText().trim());
						if (amount) {
							specie.setInitialAmount(initial);
						} else {
							specie.setInitialConcentration(initial);
						}
						JList add = new JList();
						Object[] adding = { addSpec };
						add.setListData(adding);
						add.setSelectedIndex(0);
						species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						specs = Buttons.add(specs, species, add, false, null, null, null, null,
								null, null, null, this);
						species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (document.getModel().getNumSpecies() == 1) {
							species.setSelectedIndex(0);
						} else {
							species.setSelectedIndex(index);
						}
					}
					change = true;
					speciesFrame.dispose();
				} catch (Exception e1) {
					if (amount) {
						JOptionPane.showMessageDialog(speciesFrame,
								"You must enter a double into the initial" + " amount field!",
								"Enter A Valid Initial Concentration", JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(speciesFrame,
								"You must enter a double into the initial"
										+ " concentration field!",
								"Enter A Valid Initial Concentration", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		// if the cancel species button is clicked
		else if (e.getSource() == cancelSpecies) {
			speciesFrame.dispose();
		}
		// if the add or save reactions button is clicked
		else if (e.getSource() == addSaveReactions) {
			if (usingReacId && reacID.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(reactionFrame,
						"You must enter an ID into the ID field!", "Enter An ID",
						JOptionPane.ERROR_MESSAGE);
			} else if (!usingReacId && reacName.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(reactionFrame,
						"You must enter a name into the name field!", "Enter A Name",
						JOptionPane.ERROR_MESSAGE);
			} else {
				String reac;
				if (usingReacId) {
					reac = reacID.getText().trim();
				} else {
					reac = reacName.getText().trim();
				}
				int kineticCheck;
				if (addSaveReactions.getText().equals("Save")) {
					int index = reactions.getSelectedIndex();
					kineticCheck = index;
					reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacts = Buttons.getList(reacts, reactions);
					reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					Reaction react = document.getModel().getReaction(index);
					ListOf remove;
					long size;
					try {
						remove = react.getKineticLaw().getListOfParameters();
						size = remove.getNumItems();
						for (int i = 0; i < size; i++) {
							remove.remove(0);
						}
					} catch (Exception e1) {
					}
					for (int i = 0; i < changedParameters.size(); i++) {
						react.getKineticLaw().addParameter(changedParameters.get(i));
					}
					remove = react.getListOfProducts();
					size = remove.getNumItems();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedProducts.size(); i++) {
						react.addProduct(changedProducts.get(i));
					}
					remove = react.getListOfReactants();
					size = remove.getNumItems();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedReactants.size(); i++) {
						react.addReactant(changedReactants.get(i));
					}
					react.setName(reacName.getText().trim());
					if (reacReverse.getSelectedItem().equals("true")) {
						react.setReversible(true);
					} else {
						react.setReversible(false);
					}
					react.setId(reacID.getText().trim());
					react.getKineticLaw().setFormula(kineticLaw.getText().trim());
					reacts[index] = reac;
					reactions.setListData(reacts);
					reactions.setSelectedIndex(index);
				} else {
					int index = reactions.getSelectedIndex();
					Reaction react = document.getModel().getReaction(
							document.getModel().getNumReactions() - 1);
					kineticCheck = (int) (document.getModel().getNumReactions() - 1);
					for (int i = 0; i < changedParameters.size(); i++) {
						react.getKineticLaw().addParameter(changedParameters.get(i));
					}
					for (int i = 0; i < changedProducts.size(); i++) {
						react.addProduct(changedProducts.get(i));
					}
					for (int i = 0; i < changedReactants.size(); i++) {
						react.addReactant(changedReactants.get(i));
					}
					react.setName(reacName.getText().trim());
					if (reacReverse.getSelectedItem().equals("true")) {
						react.setReversible(true);
					} else {
						react.setReversible(false);
					}
					react.setId(reacID.getText().trim());
					react.getKineticLaw().setFormula(kineticLaw.getText().trim());
					JList add = new JList();
					Object[] adding = { reac };
					add.setListData(adding);
					add.setSelectedIndex(0);
					reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacts = Buttons.add(reacts, reactions, add, false, null, null, null, null,
							null, null, null, this);
					reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (document.getModel().getNumReactions() == 1) {
						reactions.setSelectedIndex(0);
					} else {
						reactions.setSelectedIndex(index);
					}
				}
				change = true;
				if (!kineticLaw.getText().trim().equals("")) {
					ArrayList<String> validKineticVars = new ArrayList<String>();
					ArrayList<String> invalidKineticVars = new ArrayList<String>();
					Reaction r;
					if (addSaveReactions.getText().equals("Save")) {
						r = document.getModel().getReaction(kineticCheck);
					} else if (kineticCheck == -1) {
						r = document.getModel().getReaction(0);
					} else {
						r = document.getModel().getReaction(reacts.length - 1);
					}
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
					for (int i = 0; i < sbml.getNumItems(); i++) {
						if (reacUsingParamId) {
							validKineticVars.add(((Parameter) sbml.get(i)).getId());
						} else {
							validKineticVars.add(((Parameter) sbml.get(i)).getName());
						}
					}
					sbml = document.getModel().getListOfSpecies();
					for (int i = 0; i < sbml.getNumItems(); i++) {
						validKineticVars.add(((Species) sbml.get(i)).getName());
					}
					sbml = r.getListOfReactants();
					for (int i = 0; i < sbml.getNumItems(); i++) {
						validKineticVars.add(((SpeciesReference) sbml.get(i)).getSpecies());
					}
					sbml = r.getListOfProducts();
					for (int i = 0; i < sbml.getNumItems(); i++) {
						validKineticVars.add(((SpeciesReference) sbml.get(i)).getSpecies());
					}
					sbml = document.getModel().getListOfParameters();
					for (int i = 0; i < sbml.getNumItems(); i++) {
						if (usingParamId) {
							validKineticVars.add(((Parameter) sbml.get(i)).getId());
						} else {
							validKineticVars.add(((Parameter) sbml.get(i)).getName());
						}
					}
					if (!docu.getModel().getReaction(0).getKineticLaw().isSetFormula()) {
						if (addSaveReactions.getText().equals("Save")) {
							document.getModel().getReaction(kineticCheck).getKineticLaw()
									.setFormula(kineticL);
						} else if (kineticCheck != -1) {
							document.getModel().getReaction(0).getKineticLaw().setFormula(kineticL);
						} else {
							document.getModel().getReaction(reacts.length - 1).getKineticLaw()
									.setFormula(kineticL);
						}
						if (!addSaveReactions.getText().equals("Save")) {
							JOptionPane.showMessageDialog(parameterFrame,
									"Unable to parse kinetic law!", "Kinetic Law Error",
									JOptionPane.ERROR_MESSAGE);
							reactions
									.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							Buttons.remove(reactions, reacts);
							reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							reactions.setSelectedIndex(0);
						} else {
							JOptionPane
									.showMessageDialog(
											parameterFrame,
											"Unable to parse kinetic law!"
													+ "\nAll others parts of the reaction have been saved.",
											"Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						String[] splitLaw = docu.getModel().getReaction(0).getKineticLaw()
								.getFormula().split(" ");
						boolean pass = true;
						for (int i = 0; i < splitLaw.length; i++) {
							if (splitLaw[i].equals("+") || splitLaw[i].equals("-")
									|| splitLaw[i].equals("*") || splitLaw[i].equals("/")
									|| splitLaw[i].equals("INF")) {

							} else if ((splitLaw[i].contains("(") && splitLaw[i].contains(")"))) {
								String subString = (String) splitLaw[i].substring(splitLaw[i]
										.indexOf('(') + 1, splitLaw[i].indexOf(')'));
								try {
									Double.parseDouble(subString);
								} catch (Exception e1) {
									invalidKineticVars.add(subString);
									for (int j = 0; j < validKineticVars.size(); j++) {
										if (subString.equals(validKineticVars.get(j))) {
											pass = true;
											invalidKineticVars.remove(subString);
											break;
										}
										pass = false;
									}
								}
							} else if (splitLaw[i].contains("(") && splitLaw[i].contains(",")) {
								String subString = (String) splitLaw[i].substring(splitLaw[i]
										.indexOf('(') + 1, splitLaw[i].indexOf(','));
								try {
									Double.parseDouble(subString);
								} catch (Exception e1) {
									invalidKineticVars.add(subString);
									for (int j = 0; j < validKineticVars.size(); j++) {
										if (subString.equals(validKineticVars.get(j))) {
											pass = true;
											invalidKineticVars.remove(subString);
											break;
										}
										pass = false;
									}
								}
							} else if (splitLaw[i].endsWith(")")) {
								String subString = (String) splitLaw[i].substring(0, splitLaw[i]
										.indexOf(')'));
								try {
									Double.parseDouble(subString);
								} catch (Exception e1) {
									invalidKineticVars.add(subString);
									for (int j = 0; j < validKineticVars.size(); j++) {
										if (subString.equals(validKineticVars.get(j))) {
											pass = true;
											invalidKineticVars.remove(subString);
											break;
										}
										pass = false;
									}
								}
							} else if (splitLaw[i].startsWith("(")) {
								String subString = (String) splitLaw[i].substring(splitLaw[i]
										.indexOf('(') + 1);
								try {
									Double.parseDouble(subString);
								} catch (Exception e1) {
									invalidKineticVars.add(subString);
									for (int j = 0; j < validKineticVars.size(); j++) {
										if (subString.equals(validKineticVars.get(j))) {
											pass = true;
											invalidKineticVars.remove(subString);
											break;
										}
										pass = false;
									}
								}
							} else {
								try {
									Double.parseDouble(splitLaw[i]);
								} catch (Exception e1) {
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
								} else {
									invalid += invalidKineticVars.get(i) + "\n";
								}
							}
							String message;
							if (!addSaveReactions.getText().equals("Save")) {
								message = "Kinetic law contains unknown variables.\n\n"
										+ "Unkown variables:\n" + invalid;
							} else {
								message = "Kinetic law contains unknown variables.\n"
										+ "However, the reaction with this kinetic law has been saved.\n\n"
										+ "Unkown variables:\n" + invalid;
							}
							JTextArea messageArea = new JTextArea(message);
							messageArea.setLineWrap(true);
							messageArea.setWrapStyleWord(true);
							messageArea.setEditable(false);
							JScrollPane scroll = new JScrollPane();
							scroll.setMinimumSize(new Dimension(300, 300));
							scroll.setPreferredSize(new Dimension(300, 300));
							scroll.setViewportView(messageArea);
							JOptionPane.showMessageDialog(parameterFrame, scroll,
									"Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
							if (!addSaveReactions.getText().equals("Save")) {
								reactions
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								Buttons.remove(reactions, reacts);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reactions.setSelectedIndex(0);
							}
						} else {
							reactionFrame.dispose();
						}
					}
				} else {
					reactionFrame.dispose();
				}
			}
		}
		// if the cancel species button is clicked
		else if (e.getSource() == cancelReactions) {
			if (addSaveReactions.getText().equals("Add")) {
				document.getModel().getListOfReactions().remove(
						document.getModel().getNumReactions() - 1);
			}
			reactionFrame.dispose();
		}
		// if the add or save parameters button is clicked
		else if (e.getSource() == addSaveParams) {
			if (usingParamId && paramID.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(parameterFrame,
						"You must enter an ID into the ID field!", "Enter An ID",
						JOptionPane.ERROR_MESSAGE);
			} else if (!usingParamId && paramName.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(parameterFrame,
						"You must enter a name into the name field!", "Enter A Name",
						JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					double value = Double.parseDouble(paramValue.getText().trim());
					String param;
					if (!paramID.getText().trim().equals("")) {
						param = paramID.getText().trim() + " " + value;
					} else {
						param = paramName.getText().trim() + " " + value;
					}
					if (addSaveParams.getText().equals("Save")) {
						int index = parameters.getSelectedIndex();
						Parameter paramet = document.getModel().getParameter(index);
						parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						params = Buttons.getList(params, parameters);
						parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						paramet.setName(paramName.getText().trim());
						paramet.setId(paramID.getText().trim());
						paramet.setValue(value);
						params[index] = param;
						parameters.setListData(params);
						parameters.setSelectedIndex(index);
					} else {
						int index = parameters.getSelectedIndex();
						Parameter paramet = document.getModel().createParameter();
						paramet.setName(paramName.getText().trim());
						paramet.setId(paramID.getText().trim());
						paramet.setValue(value);
						JList add = new JList();
						Object[] adding = { param };
						add.setListData(adding);
						add.setSelectedIndex(0);
						parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						params = Buttons.add(params, parameters, add, false, null, null, null,
								null, null, null, null, this);
						parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (document.getModel().getNumParameters() == 1) {
							parameters.setSelectedIndex(0);
							if (!paramID.getText().trim().equals("")) {
								usingParamId = true;
							} else {
								usingParamId = false;
							}
						} else {
							parameters.setSelectedIndex(index);
						}
					}
					change = true;
					parameterFrame.dispose();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(parameterFrame,
							"You must enter a double into the value" + " field!",
							"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the cancel parameters button is clicked
		else if (e.getSource() == cancelParams) {
			parameterFrame.dispose();
		}
		// if the add or save reaction parameters button is clicked
		else if (e.getSource() == reacAddSaveParams) {
			if (reacUsingParamId && reacParamID.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(reacParameterFrame,
						"You must enter an ID into the ID field!", "Enter An ID",
						JOptionPane.ERROR_MESSAGE);
			} else if (!reacUsingParamId && reacParamName.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(reacParameterFrame,
						"You must enter a name into the name field!", "Enter A Name",
						JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					double value = Double.parseDouble(reacParamValue.getText().trim());
					String param;
					if (!reacParamID.getText().trim().equals("")) {
						param = reacParamID.getText().trim() + " " + value;
					} else {
						param = reacParamName.getText().trim() + " " + value;
					}
					if (reacAddSaveParams.getText().equals("Save")) {
						int index = reacParameters.getSelectedIndex();
						Parameter paramet = changedParameters.get(index);
						reacParameters
								.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						reacParams = Buttons.getList(reacParams, reacParameters);
						reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						paramet.setName(reacParamName.getText().trim());
						paramet.setId(reacParamID.getText().trim());
						paramet.setValue(value);
						reacParams[index] = param;
						reacParameters.setListData(reacParams);
						reacParameters.setSelectedIndex(index);
					} else {
						int index = reacParameters.getSelectedIndex();
						Parameter paramet = new Parameter();
						changedParameters.add(paramet);
						paramet.setName(reacParamName.getText().trim());
						paramet.setId(reacParamID.getText().trim());
						paramet.setValue(value);
						JList add = new JList();
						Object[] adding = { param };
						add.setListData(adding);
						add.setSelectedIndex(0);
						reacParameters
								.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						reacParams = Buttons.add(reacParams, reacParameters, add, false, null,
								null, null, null, null, null, null, reactionFrame);
						reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						try {
							if (document.getModel().getReaction(reactions.getSelectedIndex())
									.getKineticLaw().getNumParameters() == 1) {
								reacParameters.setSelectedIndex(0);
								if (!reacParamID.getText().trim().equals("")) {
									reacUsingParamId = true;
								} else {
									reacUsingParamId = false;
								}
							} else {
								reacParameters.setSelectedIndex(index);
							}
						} catch (Exception e2) {
							reacParameters.setSelectedIndex(0);
						}
					}
					change = true;
					reacParameterFrame.dispose();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(reacParameterFrame,
							"You must enter a double into the value" + " field!",
							"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// if the cancel reactions parameters button is clicked
		else if (e.getSource() == reacCancelParams) {
			reacParameterFrame.dispose();
		}
		// if the add reactions parameters button is clicked
		else if (e.getSource() == reacAddParam) {
			reacParametersEditor("Add");
		}
		// if the edit reactions parameters button is clicked
		else if (e.getSource() == reacEditParam) {
			reacParametersEditor("Save");
		}
		// if the remove reactions parameters button is clicked
		else if (e.getSource() == reacRemoveParam) {
			if (reacParameters.getSelectedIndex() != -1) {
				changedParameters.remove(reacParameters.getSelectedIndex());
				reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Buttons.remove(reacParameters, reacParams);
				reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				reacParameters.setSelectedIndex(0);
			}
		}
		// if the add reactants button is clicked
		else if (e.getSource() == addReactant) {
			reactantsEditor("Add");
		}
		// if the edit reactants button is clicked
		else if (e.getSource() == editReactant) {
			reactantsEditor("Save");
		}
		// if the remove reactants button is clicked
		else if (e.getSource() == removeReactant) {
			if (reacParameters.getSelectedIndex() != -1) {
				changedReactants.remove(reactants.getSelectedIndex());
				reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Buttons.remove(reactants, reacta);
				reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				reactants.setSelectedIndex(0);
			}
		}
		// if the add products button is clicked
		else if (e.getSource() == addProduct) {
			productsEditor("Add");
		}
		// if the edit products button is clicked
		else if (e.getSource() == editProduct) {
			productsEditor("Save");
		}
		// if the remove products button is clicked
		else if (e.getSource() == removeProduct) {
			if (reacParameters.getSelectedIndex() != -1) {
				changedProducts.remove(products.getSelectedIndex());
				products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Buttons.remove(products, product);
				products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				products.setSelectedIndex(0);
			}
		}
		// if the add or save products button is clicked
		else if (e.getSource() == addSaveProducts) {
			try {
				double value = Double.parseDouble(productStoiciometry.getText().trim());
				String prod = productSpecies.getSelectedItem() + " " + value;
				if (addSaveProducts.getText().equals("Save")) {
					int index = products.getSelectedIndex();
					SpeciesReference produ = changedProducts.get(index);
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					product = Buttons.getList(product, products);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					produ.setSpecies((String) productSpecies.getSelectedItem());
					produ.setStoichiometry(value);
					product[index] = prod;
					products.setListData(product);
					products.setSelectedIndex(index);
				} else {
					int index = products.getSelectedIndex();
					SpeciesReference produ = new SpeciesReference();
					changedProducts.add(produ);
					produ.setSpecies((String) productSpecies.getSelectedItem());
					produ.setStoichiometry(value);
					JList add = new JList();
					Object[] adding = { prod };
					add.setListData(adding);
					add.setSelectedIndex(0);
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					product = Buttons.add(product, products, add, false, null, null, null, null,
							null, null, null, reactionFrame);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					try {
						if (document.getModel().getReaction(reactions.getSelectedIndex())
								.getNumProducts() == 1) {
							products.setSelectedIndex(0);
						} else {
							products.setSelectedIndex(index);
						}
					} catch (Exception e2) {
						products.setSelectedIndex(0);
					}
				}
				change = true;
				productFrame.dispose();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(productFrame,
						"You must enter a double into the stoiciometry" + " field!",
						"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the cancel products button is clicked
		else if (e.getSource() == cancelProducts) {
			productFrame.dispose();
		}
		// if the add or save reactants button is clicked
		else if (e.getSource() == addSaveReactants) {
			try {
				double value = Double.parseDouble(reactantStoiciometry.getText().trim());
				String react = reactantSpecies.getSelectedItem() + " " + value;
				if (addSaveReactants.getText().equals("Save")) {
					int index = reactants.getSelectedIndex();
					SpeciesReference reactan = changedReactants.get(index);
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacta = Buttons.getList(reacta, reactants);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					reactan.setSpecies((String) reactantSpecies.getSelectedItem());
					reactan.setStoichiometry(value);
					reacta[index] = react;
					reactants.setListData(reacta);
					reactants.setSelectedIndex(index);
				} else {
					int index = reactants.getSelectedIndex();
					SpeciesReference reactan = new SpeciesReference();
					changedReactants.add(reactan);
					reactan.setSpecies((String) reactantSpecies.getSelectedItem());
					reactan.setStoichiometry(value);
					JList add = new JList();
					Object[] adding = { react };
					add.setListData(adding);
					add.setSelectedIndex(0);
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacta = Buttons.add(reacta, reactants, add, false, null, null, null, null,
							null, null, null, reactionFrame);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					try {
						if (document.getModel().getReaction(reactions.getSelectedIndex())
								.getNumReactants() == 1) {
							reactants.setSelectedIndex(0);
						} else {
							reactants.setSelectedIndex(index);
						}
					} catch (Exception e2) {
						reactants.setSelectedIndex(0);
					}
				}
				change = true;
				reactantFrame.dispose();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(reactantFrame,
						"You must enter a double into the stoiciometry" + " field!",
						"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the cancel reactants button is clicked
		else if (e.getSource() == cancelReactants) {
			reactantFrame.dispose();
		}
		// if the new menu item is clicked
		else if (e.getSource() == newFile) {
			if (change) {
				Object[] options = { "Save", "Cancel" };
				int value = JOptionPane.showOptionDialog(this, "Save Changes?", "Save",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
				if (value == JOptionPane.YES_OPTION) {
					File file;
					if (this.file != null) {
						file = new File(this.file);
					} else {
						file = null;
					}
					String filename = Buttons.browse(this, file, null, JFileChooser.FILES_ONLY,
							"Save");
					if (!filename.equals("")) {
						if (filename.substring((filename.length() - 5), filename.length()).equals(
								".sbml")
								|| filename.substring((filename.length() - 4), filename.length())
										.equals(".xml")) {
						} else {
							filename += ".sbml";
						}
						if (new File(filename).exists()) {
							Object[] options1 = { "Overwrite", "Cancel" };
							value = JOptionPane.showOptionDialog(this, "File already exists."
									+ " Overwrite?", "File Already Exists",
									JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
									options1, options1[0]);
							if (value == JOptionPane.YES_OPTION) {
								try {
									FileOutputStream out = new FileOutputStream(new File(filename));
									SBMLWriter writer = new SBMLWriter();
									String doc = writer.writeToString(document);
									byte[] output = doc.getBytes();
									out.write(output);
									out.close();
									this.file = filename;
								} catch (Exception e1) {
									JOptionPane.showMessageDialog(this,
											"Unable to save sbml file!", "Error Saving File",
											JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
						} else {
							try {
								FileOutputStream out = new FileOutputStream(new File(filename));
								SBMLWriter writer = new SBMLWriter();
								String doc = writer.writeToString(document);
								byte[] output = doc.getBytes();
								out.write(output);
								out.close();
								this.file = filename;
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(this, "Unable to save sbml file!",
										"Error Saving File", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
				}
			}
			new SBML_Editor(reb2sac);
			// sbmlFrame.dispose();
		}
		// if the load menu item is clicked
		else if (e.getSource() == load) {
			File file;
			if (this.file != null) {
				file = new File(this.file);
			} else {
				file = null;
			}
			String filename = Buttons.browse(this, file, null, JFileChooser.FILES_ONLY, "Open");
			if (!filename.equals("")) {
				try {
					SBMLReader reader = new SBMLReader();
					SBMLDocument docu = reader.readSBML(filename);
					if (docu.getNumFatals() > 0 || docu.getNumErrors() > 0
							|| docu.getNumWarnings() > 0) {
						JOptionPane.showMessageDialog(this, "Unable to load this sbml file!"
								+ "\nIt contains errors!", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						if (change) {
							Object[] options = { "Save", "Cancel" };
							int value = JOptionPane.showOptionDialog(this, "Save Changes?", "Save",
									JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
									options, options[0]);
							if (value == JOptionPane.YES_OPTION) {
								File file1;
								if (this.file != null) {
									file1 = new File(this.file);
								} else {
									file1 = null;
								}
								String filename1 = Buttons.browse(this, file1, null,
										JFileChooser.FILES_ONLY, "Save");
								if (!filename1.equals("")) {
									if (filename1.substring((filename1.length() - 5),
											filename1.length()).equals(".sbml")
											|| filename1.substring((filename1.length() - 4),
													filename1.length()).equals(".xml")) {
									} else {
										filename1 += ".sbml";
									}
									if (new File(filename1).exists()) {
										Object[] options1 = { "Overwrite", "Cancel" };
										value = JOptionPane.showOptionDialog(this,
												"File already exists." + " Overwrite?",
												"File Already Exists", JOptionPane.YES_NO_OPTION,
												JOptionPane.PLAIN_MESSAGE, null, options1,
												options1[0]);
										if (value == JOptionPane.YES_OPTION) {
											try {
												FileOutputStream out = new FileOutputStream(
														new File(filename1));
												SBMLWriter writer = new SBMLWriter();
												String doc = writer.writeToString(document);
												byte[] output = doc.getBytes();
												out.write(output);
												out.close();
												this.file = filename1;
											} catch (Exception e1) {
												JOptionPane.showMessageDialog(this,
														"Unable to save sbml file!",
														"Error Saving File",
														JOptionPane.ERROR_MESSAGE);
												return;
											}
										}
									} else {
										try {
											FileOutputStream out = new FileOutputStream(new File(
													filename1));
											SBMLWriter writer = new SBMLWriter();
											String doc = writer.writeToString(document);
											byte[] output = doc.getBytes();
											out.write(output);
											out.close();
											this.file = filename1;
										} catch (Exception e1) {
											JOptionPane.showMessageDialog(this,
													"Unable to save sbml file!",
													"Error Saving File", JOptionPane.ERROR_MESSAGE);
											return;
										}
									}
								}
							}
						}
						new SBML_Editor(filename, reb2sac);
						// sbmlFrame.dispose();
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this,
							"You must select a valid sbml file to load!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		/*
		 * // if the save menu item is clicked else if (e.getSource() == save) {
		 * File file; if (this.file != null) { file = new File(this.file); }
		 * else { file = null; } String filename = Buttons .browse(this, file,
		 * null, JFileChooser.FILES_ONLY, "Save"); if (!filename.equals("")) {
		 * if (filename.substring((filename.length() - 5),
		 * filename.length()).equals(".sbml") ||
		 * filename.substring((filename.length() - 4),
		 * filename.length()).equals( ".xml")) { } else { filename += ".sbml"; }
		 * if (new File(filename).exists()) { Object[] options = { "Overwrite",
		 * "Cancel" }; int value = JOptionPane.showOptionDialog(this, "File
		 * already exists." + " Overwrite?", "File Already Exists",
		 * JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
		 * options[0]); if (value == JOptionPane.YES_OPTION) { try {
		 * FileOutputStream out = new FileOutputStream(new File(filename));
		 * SBMLWriter writer = new SBMLWriter(); String doc =
		 * writer.writeToString(document); byte[] output = doc.getBytes();
		 * out.write(output); out.close(); this.file = filename;
		 * //sbmlFrame.dispose(); new SBML_Editor(filename); } catch (Exception
		 * e1) { JOptionPane.showMessageDialog(this, "Unable to save sbml
		 * file!", "Error Saving File", JOptionPane.ERROR_MESSAGE); } } } else {
		 * try { FileOutputStream out = new FileOutputStream(new
		 * File(filename)); SBMLWriter writer = new SBMLWriter(); String doc =
		 * writer.writeToString(document); byte[] output = doc.getBytes();
		 * out.write(output); out.close(); this.file = filename;
		 * //sbmlFrame.dispose(); new SBML_Editor(filename); } catch (Exception
		 * e1) { JOptionPane.showMessageDialog(this, "Unable to save sbml
		 * file!", "Error Saving File", JOptionPane.ERROR_MESSAGE); } } } }
		 */
		// if the view file menu item is clicked
		else if (e.getSource() == view) {
			int value = -1;
			JTextArea sbmlEditor = new JTextArea();
			try {
				JPanel sbmlPanel = new JPanel(new BorderLayout());
				JPanel labelPanel = new JPanel(new BorderLayout());
				JLabel sbmlLabel1;
				if (this.file != null) {
					sbmlLabel1 = new JLabel(this.file);
				} else {
					sbmlLabel1 = new JLabel("SBML Text File");
				}
				JLabel sbmlLabel2 = new JLabel("Warning, editing this file directly"
						+ " and saving it could cause the program to crash.");
				JScrollPane scroll = new JScrollPane();
				scroll.setMinimumSize(new Dimension(1000, 500));
				scroll.setPreferredSize(new Dimension(1000, 500));
				SBMLWriter writer = new SBMLWriter();
				String doc = writer.writeToString(document);
				sbmlEditor.setText(doc);
				sbmlEditor.setSelectionStart(0);
				sbmlEditor.setSelectionEnd(1);
				sbmlEditor.setSelectionEnd(0);
				scroll.setViewportView(sbmlEditor);
				labelPanel.add(sbmlLabel1, "North");
				labelPanel.add(sbmlLabel2, "Center");
				sbmlPanel.add(labelPanel, "North");
				sbmlPanel.add(scroll, "Center");
				Object[] options = { "Save", "Close" };
				if (this.file != null) {
					value = JOptionPane.showOptionDialog(this, sbmlPanel, this.file,
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
				} else {
					value = JOptionPane.showOptionDialog(this, sbmlPanel, "SBML Text File",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
							options[0]);
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this, "Unable to open sbml file for viewing!",
						"Error Opening File", JOptionPane.ERROR_MESSAGE);
			}
			try {
				if (value == JOptionPane.YES_OPTION) {
					File file;
					if (this.file != null) {
						file = new File(this.file);
					} else {
						file = null;
					}
					String filename = Buttons.browse(this, file, null, JFileChooser.FILES_ONLY,
							"Save");
					if (!filename.equals("")) {
						if (filename.substring((filename.length() - 4), filename.length()).equals(
								"sbml")
								|| filename.substring((filename.length() - 3), filename.length())
										.equals("xml")) {
						} else {
							filename += ".sbml";
						}
						if (new File(filename).exists()) {
							Object[] options = { "Overwrite", "Cancel" };
							value = JOptionPane.showOptionDialog(this, "File already exists."
									+ " Overwrite?", "File Already Exists",
									JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
									options, options[0]);
							if (value == JOptionPane.YES_OPTION) {
								OutputStream output = new FileOutputStream(new File(filename));
								output.write(sbmlEditor.getText().getBytes());
								output.close();
								try {
									SBMLReader reader = new SBMLReader();
									SBMLDocument docu = reader.readSBML(filename);
									if (docu.getNumFatals() > 0 || docu.getNumErrors() > 0
											|| docu.getNumWarnings() > 0) {
										JOptionPane.showMessageDialog(this,
												"Unable to load this sbml file!"
														+ "\nIt contains errors!", "Error",
												JOptionPane.ERROR_MESSAGE);
									} else {
										new SBML_Editor(filename, reb2sac);
										// sbmlFrame.dispose();
									}
								} catch (Exception e2) {
									JOptionPane.showMessageDialog(this,
											"Unable to load the new sbml file!",
											"Error Loading File", JOptionPane.ERROR_MESSAGE);
								}
							}
						} else {
							OutputStream output = new FileOutputStream(new File(filename));
							output.write(sbmlEditor.getText().getBytes());
							output.close();
							try {
								SBMLReader reader = new SBMLReader();
								SBMLDocument docu = reader.readSBML(filename);
								if (docu.getNumFatals() > 0 || docu.getNumErrors() > 0
										|| docu.getNumWarnings() > 0) {
									JOptionPane.showMessageDialog(this,
											"Unable to load this sbml file!"
													+ "\nIt contains errors!", "Error",
											JOptionPane.ERROR_MESSAGE);
								} else {
									new SBML_Editor(filename, reb2sac);
									// sbmlFrame.dispose();
								}
							} catch (Exception e2) {
								JOptionPane.showMessageDialog(this,
										"Unable to load the new sbml file!", "Error Loading File",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this, "Unable to save sbml file!",
						"Error Saving File", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the close menu item is clicked
		else if (e.getSource() == close) {
			// sbmlFrame.dispose();
		}
	}

	/**
	 * Creates a frame used to edit species or create new ones.
	 */
	private void speciesEditor(String option) {
		if (option.equals("Save") && species.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(this, "No species selected!", "Must Select A Species",
					JOptionPane.ERROR_MESSAGE);
		} else {
			speciesFrame = new JFrame("Species");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					speciesFrame.dispose();
				}

				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			speciesFrame.addWindowListener(w);
			JPanel speciesPanel = new JPanel(new GridLayout(5, 2));
			JLabel nameLabel = new JLabel("Name:");
			JLabel idLabel = new JLabel("ID:");
			JLabel compLabel = new JLabel("Compartment:");
			JLabel initLabel;
			if (amount) {
				initLabel = new JLabel("Initial Amount:");
			} else {
				initLabel = new JLabel("Initial Concentration:");
			}
			name = new JTextField();
			ID = new JTextField();
			init = new JTextField();
			int[] index = compartments.getSelectedIndices();
			String[] add = Buttons.getList(comps, compartments);
			compartments.setSelectedIndices(index);
			try {
				add[0].getBytes();
			} catch (Exception e) {
				add = new String[1];
				add[0] = "default";
			}
			comp = new JComboBox(add);
			addSaveSpecies = new JButton(option);
			cancelSpecies = new JButton("Cancel");
			if (option.equals("Save")) {
				try {
					Species specie = document.getModel().getSpecies(species.getSelectedIndex());
					name.setText(specie.getName());
					ID.setText(specie.getId());
					init.setText("" + specie.getInitialAmount());
					comp.setSelectedItem(specie.getCompartment());
				} catch (Exception e) {

				}
			}
			addSaveSpecies.addActionListener(this);
			cancelSpecies.addActionListener(this);
			speciesPanel.add(nameLabel);
			speciesPanel.add(name);
			speciesPanel.add(idLabel);
			speciesPanel.add(ID);
			speciesPanel.add(compLabel);
			speciesPanel.add(comp);
			speciesPanel.add(initLabel);
			speciesPanel.add(init);
			speciesPanel.add(addSaveSpecies);
			speciesPanel.add(cancelSpecies);
			speciesFrame.setContentPane(speciesPanel);
			speciesFrame.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = speciesFrame.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			speciesFrame.setLocation(x, y);
			speciesFrame.setVisible(true);
		}
	}

	/**
	 * Creates a frame used to edit reactions or create new ones.
	 */
	private void reactionsEditor(String option) {
		if (option.equals("Save") && reactions.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(this, "No reaction selected!", "Must Select A Reaction",
					JOptionPane.ERROR_MESSAGE);
		} else {
			reactionFrame = new JFrame("Reactions");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					reactionFrame.dispose();
				}

				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			reactionFrame.addWindowListener(w);
			JPanel reactionPanelNorth = new JPanel();
			JPanel reactionPanelNorth1 = new JPanel(new GridLayout(3, 2));
			JPanel reactionPanelNorth2 = new JPanel();
			JPanel reactionPanelCentral = new JPanel(new GridLayout(2, 2));
			JPanel reactionPanelSouth = new JPanel();
			JPanel reactionPanel = new JPanel(new BorderLayout());
			JLabel name = new JLabel("Name:");
			reacName = new JTextField(15);
			JLabel id = new JLabel("ID:");
			reacID = new JTextField(15);
			JLabel reverse = new JLabel("Reversible:");
			String[] options = { "true", "false" };
			reacReverse = new JComboBox(options);
			addSaveReactions = new JButton(option);
			cancelReactions = new JButton("Cancel");
			if (option.equals("Save")) {
				Reaction reac = document.getModel().getReaction(reactions.getSelectedIndex());
				reacName.setText(reac.getName());
				reacID.setText(reac.getId());
				if (reac.getReversible()) {
					reacReverse.setSelectedItem("true");
				} else {
					reacReverse.setSelectedItem("false");
				}
			}
			addSaveReactions.addActionListener(this);
			cancelReactions.addActionListener(this);
			JPanel param = new JPanel(new BorderLayout());
			JPanel addParams = new JPanel();
			reacAddParam = new JButton("Add Parameter");
			reacRemoveParam = new JButton("Remove Parameter");
			reacEditParam = new JButton("Edit Selected Parameter");
			addParams.add(reacAddParam);
			addParams.add(reacRemoveParam);
			addParams.add(reacEditParam);
			reacAddParam.addActionListener(this);
			reacRemoveParam.addActionListener(this);
			reacEditParam.addActionListener(this);
			JLabel parametersLabel = new JLabel(
					"List Of Parameters Defined Inside This Reaction's Kinetic Law:");
			reacParameters = new JList();
			reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(260, 220));
			scroll.setPreferredSize(new Dimension(276, 152));
			scroll.setViewportView(reacParameters);
			ArrayList<String> sbml = new ArrayList<String>();
			changedParameters = new ArrayList<Parameter>();
			reacUsingParamId = false;
			if (option.equals("Save")) {
				ListOf listOfParameters = document.getModel().getReaction(
						reactions.getSelectedIndex()).getKineticLaw().getListOfParameters();
				for (int i = 0; i < listOfParameters.getNumItems(); i++) {
					Parameter parameter = (Parameter) listOfParameters.get(i);
					changedParameters.add(parameter);
					if (parameter.isSetId()) {
						reacUsingParamId = true;
						sbml.add(parameter.getId() + " " + parameter.getValue());
					} else {
						sbml.add(parameter.getName() + " " + parameter.getValue());
					}
				}
			}
			reacParams = sbml.toArray();
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
			editReactant = new JButton("Edit Selected Reactant");
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
			sbml = new ArrayList<String>();
			changedReactants = new ArrayList<SpeciesReference>();
			kineticL = "";
			if (option.equals("Save")) {
				ListOf listOfReactants = document.getModel().getReaction(
						reactions.getSelectedIndex()).getListOfReactants();
				for (int i = 0; i < listOfReactants.getNumItems(); i++) {
					SpeciesReference reactant = (SpeciesReference) listOfReactants.get(i);
					changedReactants.add(reactant);
					sbml.add(reactant.getSpecies() + " " + reactant.getStoichiometry());
				}
				kineticL = document.getModel().getReaction(reactions.getSelectedIndex())
						.getKineticLaw().getFormula();
			}
			reacta = sbml.toArray();
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
			editProduct = new JButton("Edit Selected Product");
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
			sbml = new ArrayList<String>();
			changedProducts = new ArrayList<SpeciesReference>();
			if (option.equals("Save")) {
				ListOf listOfProducts = document.getModel().getReaction(
						reactions.getSelectedIndex()).getListOfProducts();
				for (int i = 0; i < listOfProducts.getNumItems(); i++) {
					SpeciesReference product = (SpeciesReference) listOfProducts.get(i);
					changedProducts.add(product);
					sbml.add(product.getSpecies() + " " + product.getStoichiometry());
				}
			}
			product = sbml.toArray();
			products.setListData(product);
			products.setSelectedIndex(0);
			products.addMouseListener(this);
			productsPanel.add(productsLabel, "North");
			productsPanel.add(scroll3, "Center");
			productsPanel.add(addProducts, "South");
			JLabel kineticLabel = new JLabel("Kinetic Law:");
			kineticLaw = new JTextArea();
			kineticLaw.setLineWrap(true);
			kineticLaw.setWrapStyleWord(true);
			JScrollPane scroll4 = new JScrollPane();
			scroll4.setMinimumSize(new Dimension(100, 100));
			scroll4.setPreferredSize(new Dimension(100, 100));
			scroll4.setViewportView(kineticLaw);
			if (option.equals("Save")) {
				kineticLaw.setText(document.getModel().getReaction(reactions.getSelectedIndex())
						.getKineticLaw().getFormula());
			}
			JPanel kineticPanel = new JPanel(new BorderLayout());
			kineticPanel.add(kineticLabel, "North");
			kineticPanel.add(scroll4, "Center");
			reactionPanelNorth1.add(name);
			reactionPanelNorth1.add(reacName);
			reactionPanelNorth1.add(id);
			reactionPanelNorth1.add(reacID);
			reactionPanelNorth1.add(reverse);
			reactionPanelNorth1.add(reacReverse);
			reactionPanelNorth2.add(reactionPanelNorth1);
			reactionPanelNorth.add(reactionPanelNorth2);
			reactionPanelCentral.add(reactantsPanel);
			reactionPanelCentral.add(productsPanel);
			reactionPanelCentral.add(param);
			reactionPanelCentral.add(kineticPanel);
			reactionPanelSouth.add(addSaveReactions);
			reactionPanelSouth.add(cancelReactions);
			reactionPanel.add(reactionPanelNorth, "North");
			reactionPanel.add(reactionPanelCentral, "Center");
			reactionPanel.add(reactionPanelSouth, "South");
			reactionFrame.setContentPane(reactionPanel);
			reactionFrame.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = reactionFrame.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			reactionFrame.setLocation(x, y);
			reactionFrame.setVisible(true);
		}
	}

	/**
	 * Creates a frame used to edit parameters or create new ones.
	 */
	private void parametersEditor(String option) {
		if (option.equals("Save") && parameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(this, "No parameter selected!",
					"Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
		} else {
			parameterFrame = new JFrame("Parameters");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					parameterFrame.dispose();
				}

				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			parameterFrame.addWindowListener(w);
			JPanel parametersPanel = new JPanel(new GridLayout(4, 2));
			JLabel nameLabel = new JLabel("Name:");
			JLabel idLabel = new JLabel("ID:");
			JLabel valueLabel = new JLabel("Value:");
			paramName = new JTextField();
			paramID = new JTextField();
			paramValue = new JTextField();
			addSaveParams = new JButton(option);
			cancelParams = new JButton("Cancel");
			if (option.equals("Save")) {
				try {
					Parameter paramet = document.getModel().getParameter(
							parameters.getSelectedIndex());
					paramID.setText(paramet.getId());
					paramName.setText(paramet.getName());
					paramValue.setText("" + paramet.getValue());
				} catch (Exception e) {

				}
			}
			addSaveParams.addActionListener(this);
			cancelParams.addActionListener(this);
			parametersPanel.add(nameLabel);
			parametersPanel.add(paramName);
			parametersPanel.add(idLabel);
			parametersPanel.add(paramID);
			parametersPanel.add(valueLabel);
			parametersPanel.add(paramValue);
			parametersPanel.add(addSaveParams);
			parametersPanel.add(cancelParams);
			parameterFrame.setContentPane(parametersPanel);
			parameterFrame.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = parameterFrame.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			parameterFrame.setLocation(x, y);
			parameterFrame.setVisible(true);
		}
	}

	/**
	 * Creates a frame used to edit reactions parameters or create new ones.
	 */
	private void reacParametersEditor(String option) {
		if (option.equals("Save") && reacParameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(reactionFrame, "No parameter selected!",
					"Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
		} else {
			reacParameterFrame = new JFrame("Parameters");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					reacParameterFrame.dispose();
				}

				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			reacParameterFrame.addWindowListener(w);
			JPanel parametersPanel = new JPanel(new GridLayout(4, 2));
			JLabel nameLabel = new JLabel("Name:");
			JLabel idLabel = new JLabel("ID:");
			JLabel valueLabel = new JLabel("Value:");
			reacParamName = new JTextField();
			reacParamID = new JTextField();
			reacParamValue = new JTextField();
			reacAddSaveParams = new JButton(option);
			reacCancelParams = new JButton("Cancel");
			if (option.equals("Save")) {
				Parameter paramet = changedParameters.get(reacParameters.getSelectedIndex());
				reacParamID.setText(paramet.getId());
				reacParamName.setText(paramet.getName());
				reacParamValue.setText("" + paramet.getValue());
			}
			reacAddSaveParams.addActionListener(this);
			reacCancelParams.addActionListener(this);
			parametersPanel.add(nameLabel);
			parametersPanel.add(reacParamName);
			parametersPanel.add(idLabel);
			parametersPanel.add(reacParamID);
			parametersPanel.add(valueLabel);
			parametersPanel.add(reacParamValue);
			parametersPanel.add(reacAddSaveParams);
			parametersPanel.add(reacCancelParams);
			reacParameterFrame.setContentPane(parametersPanel);
			reacParameterFrame.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = reacParameterFrame.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			reacParameterFrame.setLocation(x, y);
			reacParameterFrame.setVisible(true);
		}
	}

	/**
	 * Creates a frame used to edit products or create new ones.
	 */
	public void productsEditor(String option) {
		if (option.equals("Save") && products.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(reactionFrame, "No product selected!",
					"Must Select A Product", JOptionPane.ERROR_MESSAGE);
		} else {
			productFrame = new JFrame("Products");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					productFrame.dispose();
				}

				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			productFrame.addWindowListener(w);
			JPanel productsPanel = new JPanel(new GridLayout(3, 2));
			JLabel speciesLabel = new JLabel("Species:");
			JLabel stoiciLabel = new JLabel("Stoiciometry:");
			ListOf listOfSpecies = document.getModel().getListOfSpecies();
			ArrayList<String> species = new ArrayList<String>();
			amount = false;
			for (int i = 0; i < listOfSpecies.getNumItems(); i++) {
				species.add(((Species) listOfSpecies.get(i)).getName());
			}
			Object[] choices = species.toArray();
			productSpecies = new JComboBox(choices);
			productStoiciometry = new JTextField();
			addSaveProducts = new JButton(option);
			cancelProducts = new JButton("Cancel");
			if (option.equals("Save")) {
				SpeciesReference product = changedProducts.get(products.getSelectedIndex());
				productSpecies.setSelectedItem(product.getSpecies());
				productStoiciometry.setText("" + product.getStoichiometry());
			}
			addSaveProducts.addActionListener(this);
			cancelProducts.addActionListener(this);
			productsPanel.add(speciesLabel);
			productsPanel.add(productSpecies);
			productsPanel.add(stoiciLabel);
			productsPanel.add(productStoiciometry);
			productsPanel.add(addSaveProducts);
			productsPanel.add(cancelProducts);
			productFrame.setContentPane(productsPanel);
			productFrame.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = productFrame.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			productFrame.setLocation(x, y);
			productFrame.setVisible(true);
			if (choices.length == 0) {
				JOptionPane.showMessageDialog(productFrame,
						"There are no species availiable to be products."
								+ "\nAdd species to this sbml file first.", "No Species",
						JOptionPane.ERROR_MESSAGE);
				productFrame.dispose();
			}
		}
	}

	/**
	 * Creates a frame used to edit reactants or create new ones.
	 */
	public void reactantsEditor(String option) {
		if (option.equals("Save") && reactants.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(reactionFrame, "No reactant selected!",
					"Must Select A Reactant", JOptionPane.ERROR_MESSAGE);
		} else {
			reactantFrame = new JFrame("Reactants");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					reactantFrame.dispose();
				}

				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			reactantFrame.addWindowListener(w);
			JPanel reactantsPanel = new JPanel(new GridLayout(3, 2));
			JLabel speciesLabel = new JLabel("Species:");
			JLabel stoiciLabel = new JLabel("Stoiciometry:");
			ListOf listOfSpecies = document.getModel().getListOfSpecies();
			ArrayList<String> species = new ArrayList<String>();
			amount = false;
			for (int i = 0; i < listOfSpecies.getNumItems(); i++) {
				species.add(((Species) listOfSpecies.get(i)).getName());
			}
			Object[] choices = species.toArray();
			reactantSpecies = new JComboBox(choices);
			reactantStoiciometry = new JTextField();
			addSaveReactants = new JButton(option);
			cancelReactants = new JButton("Cancel");
			if (option.equals("Save")) {
				SpeciesReference reactant = changedReactants.get(reactants.getSelectedIndex());
				reactantSpecies.setSelectedItem(reactant.getSpecies());
				reactantStoiciometry.setText("" + reactant.getStoichiometry());
			}
			addSaveReactants.addActionListener(this);
			cancelReactants.addActionListener(this);
			reactantsPanel.add(speciesLabel);
			reactantsPanel.add(reactantSpecies);
			reactantsPanel.add(stoiciLabel);
			reactantsPanel.add(reactantStoiciometry);
			reactantsPanel.add(addSaveReactants);
			reactantsPanel.add(cancelReactants);
			reactantFrame.setContentPane(reactantsPanel);
			reactantFrame.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = reactantFrame.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			reactantFrame.setLocation(x, y);
			reactantFrame.setVisible(true);
			if (choices.length == 0) {
				JOptionPane.showMessageDialog(productFrame,
						"There are no species availiable to be reactants."
								+ "\nAdd species to this sbml file first.", "No Species",
						JOptionPane.ERROR_MESSAGE);
				reactantFrame.dispose();
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
				document.getModel().getListOfCompartments().remove(compartments.getSelectedIndex());
				Buttons.remove(compartments, comps);
				change = true;
			} else if (e.getSource() == species) {
				speciesEditor("Save");
			} else if (e.getSource() == reactions) {
				reactionsEditor("Save");
			} else if (e.getSource() == parameters) {
				parametersEditor("Save");
			} else if (e.getSource() == reacParameters) {
				reacParametersEditor("Save");
			} else if (e.getSource() == reactants) {
				reactantsEditor("Save");
			} else if (e.getSource() == products) {
				productsEditor("Save");
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

	/**
	 * This method currently does nothing.
	 */
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void keyPressed(KeyEvent e) {
	}

	/**
	 * Invoked when a key has been released in the model's id field.
	 */
	public void keyReleased(KeyEvent e) {
		Model model = document.getModel();
		if (this.model) {
			model.setId(modelID.getText());
		} else {
			model.setName(modelID.getText());
		}
		change = true;
	}

	/**
	 * Saves the sbml file.
	 */
	public void save() {
		try {
			FileOutputStream out = new FileOutputStream(new File(file));
			SBMLWriter writer = new SBMLWriter();
			String doc = writer.writeToString(document);
			byte[] output = doc.getBytes();
			out.write(output);
			out.close();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this, "Unable to save sbml file!", "Error Saving File",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}