package sbmleditor.core.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.sbml.libsbml.*;
import biomodelsim.core.gui.*;
import reb2sac.core.gui.*;
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
public class SBML_Editor extends JPanel implements ActionListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8236967001410906807L;

	private SBMLDocument document; // sbml document

	private String file; // SBML file

	/*
	 * compartment buttons
	 */
	private JButton addCompart, removeCompart, editCompart;

	private String[] comps; // array of compartments

	private JList compartments; // JList of compartments

	private JTextField compID;// , compName;

	private JButton addSpec, removeSpec, editSpec; // species buttons

	private String[] specs; // array of species

	private JList species; // JList of species

	private JTextField ID, init;// , name; // species text fields

	private JComboBox comp; // compartment combo box

	private boolean amount; // determines if the species have amount set

	private boolean change; // determines if any changes were made

	private JTextField modelID; // the model's ID

	private JList reactions; // JList of reactions

	private String[] reacts; // array of reactions

	private JButton addReac, removeReac, editReac; // reactions buttons

	private JList parameters; // JList of parameters

	private String[] params; // array of parameters

	private JButton addParam, removeParam, editParam; // parameters buttons

	/*
	 * parameters text fields
	 */
	private JTextField paramID, paramValue;// , paramName;

	private JList reacParameters; // JList of reaction parameters

	private String[] reacParams; // array of reaction parameters

	/*
	 * reaction parameters buttons
	 */
	private JButton reacAddParam, reacRemoveParam, reacEditParam;

	/*
	 * reaction parameters text fields
	 */
	private JTextField reacParamID, reacParamValue;// , reacParamName;

	private ArrayList<Parameter> changedParameters; // ArrayList of parameters

	private JTextField reacID;// , reacName; // reaction name and id text

	// fields

	private JComboBox reacReverse; // reaction reversible combo box

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

	private JButton saveNoRun, run, saveAs; // save and run buttons

	private Log log;

	private ArrayList<String> usedIDs;

	private ArrayList<String> thisReactionParams;

	private BioSim biosim;

	private JButton useMassAction, clearKineticLaw;

	/**
	 * Creates a new SBML_Editor and sets up the frame where the user can edit a
	 * new sbml file.
	 */
	public SBML_Editor(Reb2Sac reb2sac, Log log, BioSim biosim) {
		this.reb2sac = reb2sac;
		this.log = log;
		this.biosim = biosim;
		createSbmlFrame("");
	}

	/**
	 * Creates a new SBML_Editor and sets up the frame where the user can edit
	 * the sbml file given to this constructor.
	 */
	public SBML_Editor(String file, Reb2Sac reb2sac, Log log, BioSim biosim) {
		this.reb2sac = reb2sac;
		this.log = log;
		this.biosim = biosim;
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

		usedIDs = new ArrayList<String>();
		if (model.isSetId()) {
			usedIDs.add(model.getId());
		}
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < ids.getNumItems(); i++) {
			usedIDs.add(((Compartment) ids.get(i)).getId());
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < ids.getNumItems(); i++) {
			usedIDs.add(((Parameter) ids.get(i)).getId());
		}
		ids = model.getListOfReactions();
		for (int i = 0; i < ids.getNumItems(); i++) {
			usedIDs.add(((Reaction) ids.get(i)).getId());
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < ids.getNumItems(); i++) {
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
		JLabel compartmentsLabel = new JLabel("List Of Compartments:");
		compartments = new JList();
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(compartments);
		ListOf listOfCompartments = model.getListOfCompartments();
		comps = new String[(int) model.getNumCompartments()];
		for (int i = 0; i < model.getNumCompartments(); i++) {
			Compartment compartment = (Compartment) listOfCompartments.get(i);
			comps[i] = compartment.getId();
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
		JLabel speciesLabel = new JLabel("List Of Species:");
		species = new JList();
		species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll1 = new JScrollPane();
		scroll1.setMinimumSize(new Dimension(260, 220));
		scroll1.setPreferredSize(new Dimension(276, 152));
		scroll1.setViewportView(species);
		ListOf listOfSpecies = model.getListOfSpecies();
		specs = new String[(int) model.getNumSpecies()];
		amount = false;
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = (Species) listOfSpecies.get(i);
			amount = species.isSetInitialAmount();
			specs[i] = species.getId() + " " + species.getCompartment() + " "
					+ species.getInitialAmount();
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
		scroll2.setMinimumSize(new Dimension(400, 220));
		scroll2.setPreferredSize(new Dimension(436, 152));
		scroll2.setViewportView(reactions);
		ListOf listOfReactions = model.getListOfReactions();
		reacts = new String[(int) model.getNumReactions()];
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) listOfReactions.get(i);
			reacts[i] = reaction.getId();
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
		JLabel parametersLabel = new JLabel("List Of Parameters Defined Outside The Kinetic Law:");
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
			params[i] = parameter.getId() + " " + parameter.getValue();
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
		modelID = new JTextField(model.getId(), 50);
		JLabel modelIDLabel = new JLabel("Model ID:");
		modelID.setEditable(false);
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
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, saveRun, null);
			splitPane.setDividerSize(0);
			this.add(splitPane, "South");
		} else {
			saveNoRun = new JButton("Save");
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
			if (reb2sac != null) {
				reb2sac.getSaveButton().doClick();
			} else {
				save();
			}
		}
		// if the save as button is clicked
		else if (e.getSource() == saveAs) {
			String simName = JOptionPane.showInputDialog(biosim.frame(), "Enter Model ID:",
					"Model ID", JOptionPane.PLAIN_MESSAGE);
			if (simName != null && !simName.equals("")) {
				if (simName.length() > 4) {
					if (!simName.substring(simName.length() - 5).equals(".sbml")
							&& !simName.substring(simName.length() - 4).equals(".xml")) {
						simName += ".sbml";
					}
				} else {
					simName += ".sbml";
				}
				String modelID = "";
				if (simName.length() > 4) {
					if (simName.substring(simName.length() - 5).equals(".sbml")) {
						modelID = simName.substring(0, simName.length() - 5);
					} else {
						modelID = simName.substring(0, simName.length() - 4);
					}
				}
				String oldId = document.getModel().getId();
				document.getModel().setId(modelID);
				String newFile = file;
				newFile = newFile.substring(0, newFile.length()
						- newFile.split(File.separator)[newFile.split(File.separator).length - 1]
								.length())
						+ simName;
				try {
					log.addText("Saving sbml file as:\n" + newFile + "\n");
					FileOutputStream out = new FileOutputStream(new File(newFile));
					SBMLWriter writer = new SBMLWriter();
					String doc = writer.writeToString(document);
					byte[] output = doc.getBytes();
					out.write(output);
					out.close();
					biosim.refreshTree();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(biosim.frame(), "Unable to save sbml file!",
							"Error Saving File", JOptionPane.ERROR_MESSAGE);
				} finally {
					document.getModel().setId(oldId);
				}
			}
		}
		// if the add comparment button is clicked
		else if (e.getSource() == addCompart) {
			compartEditor("Add");
		} else if (e.getSource() == editCompart) {
			compartEditor("Save");
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
									(String) compartments.getSelectedValue())) {
								remove = false;
								speciesUsing.add(species.getId());
							}
						}
					}
					if (remove) {
						Compartment tempComp = document.getModel().getCompartment(
								(String) compartments.getSelectedValue());
						ListOf c = document.getModel().getListOfCompartments();
						for (int i = 0; i < c.getNumItems(); i++) {
							if (((Compartment) c.get(i)).getId().equals(tempComp.getId())) {
								c.remove(i);
							}
						}
						usedIDs.remove(compartments.getSelectedValue());
						compartments
								.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						Buttons.remove(compartments, comps);
						compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						compartments.setSelectedIndex(0);
					} else {
						String species = "";
						String[] specs = speciesUsing.toArray(new String[0]);
						sort(specs);
						for (int i = 0; i < specs.length; i++) {
							if (i == specs.length - 1) {
								species += specs[i];
							} else {
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
						JOptionPane.showMessageDialog(biosim.frame(), scroll,
								"Unable To Remove Compartment", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Each model must contain at least one compartment.",
							"Unable To Remove Compartment", JOptionPane.ERROR_MESSAGE);
				}
			}
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
							if (model.getSpecies(
									((String) species.getSelectedValue()).split(" ")[0]).getId()
									.equals(specRef)) {
								remove = false;
								productsUsing.add(reaction.getId());
							}
						}
					}
					for (int j = 0; j < reaction.getNumReactants(); j++) {
						if (reaction.getReactant(j).isSetSpecies()) {
							String specRef = reaction.getReactant(j).getSpecies();
							if (model.getSpecies(
									((String) species.getSelectedValue()).split(" ")[0]).getId()
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
					for (int i = 0; i < s.getNumItems(); i++) {
						if (((Species) s.get(i)).getId().equals(tempSpecies.getId())) {
							s.remove(i);
						}
					}
					usedIDs.remove(tempSpecies.getId());
					species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					Buttons.remove(species, specs);
					species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					species.setSelectedIndex(0);
				} else {
					String reactants = "";
					String products = "";
					String[] reacts = reactantsUsing.toArray(new String[0]);
					sort(reacts);
					String[] prods = productsUsing.toArray(new String[0]);
					sort(prods);
					for (int i = 0; i < reacts.length; i++) {
						if (i == reacts.length - 1) {
							reactants += reacts[i];
						} else {
							reactants += reacts[i] + "\n";
						}
					}
					for (int i = 0; i < prods.length; i++) {
						if (i == prods.length - 1) {
							products += prods[i];
						} else {
							products += prods[i] + "\n";
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
					JOptionPane.showMessageDialog(biosim.frame(), scroll,
							"Unable To Remove Species", JOptionPane.ERROR_MESSAGE);
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
				Reaction tempReaction = document.getModel().getReaction(
						(String) reactions.getSelectedValue());
				ListOf r = document.getModel().getListOfReactions();
				for (int i = 0; i < r.getNumItems(); i++) {
					if (((Reaction) r.get(i)).getId().equals(tempReaction.getId())) {
						r.remove(i);
					}
				}
				usedIDs.remove(reactions.getSelectedValue());
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
				Parameter tempParameter = document.getModel().getParameter(
						((String) parameters.getSelectedValue()).split(" ")[0]);
				ListOf p = document.getModel().getListOfParameters();
				for (int i = 0; i < p.getNumItems(); i++) {
					if (((Parameter) p.get(i)).getId().equals(tempParameter.getId())) {
						p.remove(i);
					}
				}
				usedIDs.remove(tempParameter.getId());
				parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Buttons.remove(parameters, params);
				parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				parameters.setSelectedIndex(0);
			}
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
				String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < changedParameters.size(); i++) {
					if (changedParameters.get(i).getId().equals(v)) {
						changedParameters.remove(i);
					}
				}
				thisReactionParams.remove(v);
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
			if (reactants.getSelectedIndex() != -1) {
				String v = ((String) reactants.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < changedReactants.size(); i++) {
					if (changedReactants.get(i).getSpecies().equals(v)) {
						changedReactants.remove(i);
					}
				}
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
			if (products.getSelectedIndex() != -1) {
				String v = ((String) products.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < changedProducts.size(); i++) {
					if (changedProducts.get(i).getSpecies().equals(v)) {
						changedProducts.remove(i);
					}
				}
				products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Buttons.remove(products, product);
				products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				products.setSelectedIndex(0);
			}
		}
		// if the clear button is clicked
		else if (e.getSource() == clearKineticLaw) {
			kineticLaw.setText("");
		}
		// if the use mass action button is clicked
		else if (e.getSource() == useMassAction) {
			String kf;
			String kr;
			if (changedParameters.size() == 0) {
				kf = "kf";
				kr = "kr";
			} else if (changedParameters.size() == 1) {
				kf = changedParameters.get(0).getId();
				kr = changedParameters.get(0).getId();
			} else {
				kf = changedParameters.get(0).getId();
				kr = changedParameters.get(1).getId();
			}
			String kinetic = kf;
			for (SpeciesReference s : changedReactants) {
				if (s.getStoichiometry() == 1) {
					kinetic += " * " + s.getSpecies();
				} else {
					kinetic += " * pow(" + s.getSpecies() + ", " + s.getStoichiometry() + ")";
				}
			}
			if (reacReverse.getSelectedItem().equals("true")) {
				kinetic += " - " + kr;
				for (SpeciesReference s : changedProducts) {
					if (s.getStoichiometry() == 1) {
						kinetic += " * " + s.getSpecies();
					} else {
						kinetic += " * pow(" + s.getSpecies() + ", " + s.getStoichiometry() + ")";
					}
				}
			}
			kineticLaw.setText(kinetic);
		}
	}

	/**
	 * Creates a frame used to edit compartments or create new ones.
	 */
	private void compartEditor(String option) {
		if (option.equals("Save") && compartments.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No compartments selected!",
					"Must Select A Compartment", JOptionPane.ERROR_MESSAGE);
		} else {
			JPanel compartPanel = new JPanel();
			JPanel compPanel = new JPanel(new GridLayout(1, 2));
			JLabel idLabel = new JLabel("ID:");
			compID = new JTextField(12);
			if (option.equals("Save")) {
				try {
					Compartment compartment = document.getModel().getCompartment(
							((String) compartments.getSelectedValue()));
					compID.setText(compartment.getId());
				} catch (Exception e) {
				}
			}
			compPanel.add(idLabel);
			compPanel.add(compID);
			compartPanel.add(compPanel);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), compartPanel,
					"Compartment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (compID.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter an id into the id field!", "Enter An ID",
								JOptionPane.ERROR_MESSAGE);
						error = true;
						value = JOptionPane.showOptionDialog(biosim.frame(), compartPanel,
								"Compartment Editor", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					} else {
						String addComp = "";
						addComp = compID.getText().trim();
						if (usedIDs.contains(addComp)) {
							if (option.equals("Save")
									&& !addComp.equals((String) compartments.getSelectedValue())) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique id into the id field!",
										"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
								error = true;
							} else if (option.equals("Add")) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique id into the id field!",
										"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						if (!error) {
							if (option.equals("Save")) {
								int index = compartments.getSelectedIndex();
								String val = (String) compartments.getSelectedValue();
								compartments
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								comps = Buttons.getList(comps, compartments);
								compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								Compartment c = document.getModel().getCompartment(val);
								c.setId(compID.getText().trim());
								for (int i = 0; i < usedIDs.size(); i++) {
									if (usedIDs.get(i).equals(val)) {
										usedIDs.set(i, addComp);
									}
								}
								comps[index] = addComp;
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
								species
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								specs = Buttons.getList(specs, species);
								species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								for (int i = 0; i < specs.length; i++) {
									if (specs[i].split(" ")[1].equals(val)) {
										specs[i] = specs[i].split(" ")[0] + " " + addComp + " "
												+ specs[i].split(" ")[2];
									}
								}
								sort(specs);
								species.setListData(specs);
								species.setSelectedIndex(index1);
							} else {
								int index = compartments.getSelectedIndex();
								Compartment c = document.getModel().createCompartment();
								c.setId(compID.getText().trim());
								usedIDs.add(addComp);
								JList add = new JList();
								Object[] adding = { addComp };
								add.setListData(adding);
								add.setSelectedIndex(0);
								compartments
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(comps, compartments, add, false, null, null,
										null, null, null, null, null, biosim.frame());
								comps = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									comps[i] = (String) adding[i];
								}
								sort(comps);
								compartments.setListData(comps);
								compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (document.getModel().getNumCompartments() == 1) {
									compartments.setSelectedIndex(0);
								} else {
									compartments.setSelectedIndex(index);
								}
							}
							change = true;
						}
						if (error) {
							value = JOptionPane.showOptionDialog(biosim.frame(), compartPanel,
									"Compartment Editor", JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
	 * Creates a frame used to edit species or create new ones.
	 */
	private void speciesEditor(String option) {
		if (option.equals("Save") && species.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No species selected!",
					"Must Select A Species", JOptionPane.ERROR_MESSAGE);
		} else {
			JPanel speciesPanel = new JPanel(new GridLayout(3, 2));
			JLabel idLabel = new JLabel("ID:");
			JLabel compLabel = new JLabel("Compartment:");
			JLabel initLabel;
			if (amount) {
				initLabel = new JLabel("Initial Amount:");
			} else {
				initLabel = new JLabel("Initial Concentration:");
			}
			ID = new JTextField();
			init = new JTextField("0.0");
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
			if (option.equals("Save")) {
				try {
					Species specie = document.getModel().getSpecies(
							((String) species.getSelectedValue()).split(" ")[0]);
					ID.setText(specie.getId());
					init.setText("" + specie.getInitialAmount());
					comp.setSelectedItem(specie.getCompartment());
				} catch (Exception e) {
				}
			}
			speciesPanel.add(idLabel);
			speciesPanel.add(ID);
			speciesPanel.add(compLabel);
			speciesPanel.add(comp);
			speciesPanel.add(initLabel);
			speciesPanel.add(init);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), speciesPanel,
					"Species Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (ID.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter an id into the id field!", "Enter An ID",
								JOptionPane.ERROR_MESSAGE);
						error = true;
						value = JOptionPane.showOptionDialog(biosim.frame(), speciesPanel,
								"Species Editor", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					} else {
						try {
							double initial = Double.parseDouble(init.getText().trim());
							String addSpec = ID.getText().trim() + " " + comp.getSelectedItem()
									+ " " + initial;
							if (usedIDs.contains(ID.getText().trim())) {
								if (option.equals("Save")
										&& !ID.getText().trim()
												.equals(
														((String) species.getSelectedValue())
																.split(" ")[0])) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique id into the id field!",
											"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
									error = true;
								} else if (option.equals("Add")) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique id into the id field!",
											"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
									error = true;
								}
							}
							if (!error) {
								if (option.equals("Save")) {
									int index1 = species.getSelectedIndex();
									String val = ((String) species.getSelectedValue()).split(" ")[0];
									species
											.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									specs = Buttons.getList(specs, species);
									species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									Species specie = document.getModel().getSpecies(val);
									String speciesName = specie.getId();
									specie.setCompartment((String) comp.getSelectedItem());
									specie.setId(ID.getText().trim());
									for (int i = 0; i < usedIDs.size(); i++) {
										if (usedIDs.get(i).equals(val)) {
											usedIDs.set(i, ID.getText().trim());
										}
									}
									Model model = document.getModel();
									for (int i = 0; i < model.getNumReactions(); i++) {
										Reaction reaction = (Reaction) document.getModel()
												.getListOfReactions().get(i);
										for (int j = 0; j < reaction.getNumProducts(); j++) {
											if (reaction.getProduct(j).isSetSpecies()) {
												SpeciesReference specRef = reaction.getProduct(j);
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
									}
									if (amount) {
										specie.setInitialAmount(initial);
									} else {
										specie.setInitialConcentration(initial);
									}
									specs[index1] = addSpec;
									sort(specs);
									species.setListData(specs);
									species.setSelectedIndex(index1);
								} else {
									int index1 = species.getSelectedIndex();
									Species specie = document.getModel().createSpecies();
									specie.setCompartment((String) comp.getSelectedItem());
									specie.setId(ID.getText().trim());
									usedIDs.add(ID.getText().trim());
									if (amount) {
										specie.setInitialAmount(initial);
									} else {
										specie.setInitialConcentration(initial);
									}
									JList addIt = new JList();
									Object[] adding = { addSpec };
									addIt.setListData(adding);
									addIt.setSelectedIndex(0);
									species
											.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									adding = Buttons.add(specs, species, addIt, false, null, null,
											null, null, null, null, null, biosim.frame());
									specs = new String[adding.length];
									for (int i = 0; i < adding.length; i++) {
										specs[i] = (String) adding[i];
									}
									sort(specs);
									species.setListData(specs);
									species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									if (document.getModel().getNumSpecies() == 1) {
										species.setSelectedIndex(0);
									} else {
										species.setSelectedIndex(index1);
									}
								}
								change = true;
							}
						} catch (Exception e1) {
							error = true;
							if (amount) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a double into the initial"
												+ " amount field!",
										"Enter A Valid Initial Concentration",
										JOptionPane.ERROR_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a double into the initial"
												+ " concentration field!",
										"Enter A Valid Initial Concentration",
										JOptionPane.ERROR_MESSAGE);
							}
						}
						if (error) {
							value = JOptionPane.showOptionDialog(biosim.frame(), speciesPanel,
									"Species Editor", JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
		if (option.equals("Save") && reactions.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No reaction selected!",
					"Must Select A Reaction", JOptionPane.ERROR_MESSAGE);
		} else {
			JPanel reactionPanelNorth = new JPanel();
			JPanel reactionPanelNorth1 = new JPanel(new GridLayout(2, 2));
			JPanel reactionPanelNorth2 = new JPanel();
			JPanel reactionPanelCentral = new JPanel(new GridLayout(2, 2));
			JPanel reactionPanel = new JPanel(new BorderLayout());
			JLabel id = new JLabel("ID:");
			reacID = new JTextField(15);
			JLabel reverse = new JLabel("Reversible:");
			String[] options = { "true", "false" };
			reacReverse = new JComboBox(options);
			reacReverse.setSelectedItem("false");
			if (option.equals("Save")) {
				Reaction reac = document.getModel().getReaction(
						((String) reactions.getSelectedValue()));
				reacID.setText(reac.getId());
				if (reac.getReversible()) {
					reacReverse.setSelectedItem("true");
				} else {
					reacReverse.setSelectedItem("false");
				}
			}
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
			reacParams = new String[0];
			changedParameters = new ArrayList<Parameter>();
			thisReactionParams = new ArrayList<String>();
			if (option.equals("Save")) {
				ListOf listOfParameters = document.getModel().getReaction(
						((String) reactions.getSelectedValue())).getKineticLaw()
						.getListOfParameters();
				reacParams = new String[(int) listOfParameters.getNumItems()];
				for (int i = 0; i < listOfParameters.getNumItems(); i++) {
					Parameter parameter = (Parameter) listOfParameters.get(i);
					changedParameters.add(parameter);
					thisReactionParams.add(parameter.getId());
					reacParams[i] = parameter.getId() + " " + parameter.getValue();
				}
			} else {
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
			reacta = new String[0];
			changedReactants = new ArrayList<SpeciesReference>();
			kineticL = "";
			if (option.equals("Save")) {
				ListOf listOfReactants = document.getModel().getReaction(
						((String) reactions.getSelectedValue())).getListOfReactants();
				reacta = new String[(int) listOfReactants.getNumItems()];
				for (int i = 0; i < listOfReactants.getNumItems(); i++) {
					SpeciesReference reactant = (SpeciesReference) listOfReactants.get(i);
					changedReactants.add(reactant);
					reacta[i] = reactant.getSpecies() + " " + reactant.getStoichiometry();
				}
				kineticL = document.getModel().getReaction(((String) reactions.getSelectedValue()))
						.getKineticLaw().getFormula();
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
			product = new String[0];
			changedProducts = new ArrayList<SpeciesReference>();
			if (option.equals("Save")) {
				ListOf listOfProducts = document.getModel().getReaction(
						((String) reactions.getSelectedValue())).getListOfProducts();
				product = new String[(int) listOfProducts.getNumItems()];
				for (int i = 0; i < listOfProducts.getNumItems(); i++) {
					SpeciesReference product = (SpeciesReference) listOfProducts.get(i);
					changedProducts.add(product);
					this.product[i] = product.getSpecies() + " " + product.getStoichiometry();
				}
			}
			sort(product);
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
			if (option.equals("Save")) {
				kineticLaw.setText(document.getModel().getReaction(
						((String) reactions.getSelectedValue())).getKineticLaw().getFormula());
			}
			JPanel kineticPanel = new JPanel(new BorderLayout());
			kineticPanel.add(kineticLabel, "North");
			kineticPanel.add(scroll4, "Center");
			kineticPanel.add(kineticButtons, "South");
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
			reactionPanel.add(reactionPanelNorth, "North");
			reactionPanel.add(reactionPanelCentral, "Center");
			Object[] options1 = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), reactionPanel,
					"Reaction Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options1, options1[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (reacID.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter an ID into the ID field!", "Enter An ID",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					} else {
						String reac;
						reac = reacID.getText().trim();
						if (usedIDs.contains(reacID.getText().trim())) {
							if (option.equals("Save")
									&& !reacID.getText().trim().equals(
											(String) reactions.getSelectedValue())) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique id into the id field!",
										"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
								error = true;
							} else if (option.equals("Add")) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique id into the id field!",
										"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
						int kineticCheck;
						if (!error) {
							if (option.equals("Save")) {
								int index = reactions.getSelectedIndex();
								String val = (String) reactions.getSelectedValue();
								kineticCheck = index;
								reactions
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacts = Buttons.getList(reacts, reactions);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								Reaction react = document.getModel().getReaction(val);
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
								if (reacReverse.getSelectedItem().equals("true")) {
									react.setReversible(true);
								} else {
									react.setReversible(false);
								}
								react.setId(reacID.getText().trim());
								for (int i = 0; i < usedIDs.size(); i++) {
									if (usedIDs.get(i).equals(val)) {
										usedIDs.set(i, reacID.getText().trim());
									}
								}
								react.getKineticLaw().setFormula(kineticLaw.getText().trim());
								reacts[index] = reac;
								sort(reacts);
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
								if (reacReverse.getSelectedItem().equals("true")) {
									react.setReversible(true);
								} else {
									react.setReversible(false);
								}
								react.setId(reacID.getText().trim());
								usedIDs.add(reacID.getText().trim());
								react.getKineticLaw().setFormula(kineticLaw.getText().trim());
								JList add = new JList();
								Object[] adding = { reac };
								add.setListData(adding);
								add.setSelectedIndex(0);
								reactions
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(reacts, reactions, add, false, null, null,
										null, null, null, null, null, biosim.frame());
								reacts = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									reacts[i] = (String) adding[i];
								}
								sort(reacts);
								reactions.setListData(reacts);
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
								if (option.equals("Save")) {
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
									validKineticVars.add(((Parameter) sbml.get(i)).getId());
								}
								sbml = r.getListOfReactants();
								for (int i = 0; i < sbml.getNumItems(); i++) {
									validKineticVars.add(((SpeciesReference) sbml.get(i))
											.getSpecies());
								}
								sbml = r.getListOfProducts();
								for (int i = 0; i < sbml.getNumItems(); i++) {
									validKineticVars.add(((SpeciesReference) sbml.get(i))
											.getSpecies());
								}
								sbml = document.getModel().getListOfParameters();
								for (int i = 0; i < sbml.getNumItems(); i++) {
									validKineticVars.add(((Parameter) sbml.get(i)).getId());
								}
								if (!docu.getModel().getReaction(0).getKineticLaw().isSetFormula()) {
									if (option.equals("Save")) {
										document.getModel().getReaction(kineticCheck)
												.getKineticLaw().setFormula(kineticL);
									} else if (kineticCheck != -1) {
										document.getModel().getReaction(0).getKineticLaw()
												.setFormula(kineticL);
									} else {
										document.getModel().getReaction(reacts.length - 1)
												.getKineticLaw().setFormula(kineticL);
									}
									if (option.equals("Save")) {
										JOptionPane.showMessageDialog(biosim.frame(),
												"Unable to parse kinetic law!",
												"Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
									} else {
										JOptionPane
												.showMessageDialog(
														biosim.frame(),
														"Unable to parse kinetic law!"
																+ "\nAll others parts of the reaction have been saved.",
														"Kinetic Law Error",
														JOptionPane.ERROR_MESSAGE);
									}
								} else {
									String[] splitLaw = docu.getModel().getReaction(0)
											.getKineticLaw().getFormula().split(" ");
									boolean pass = true;
									for (int i = 0; i < splitLaw.length; i++) {
										if (splitLaw[i].equals("+") || splitLaw[i].equals("-")
												|| splitLaw[i].equals("*")
												|| splitLaw[i].equals("/")
												|| splitLaw[i].equals("INF")) {

										} else if ((splitLaw[i].contains("(") && splitLaw[i]
												.contains(")"))) {
											String subString = (String) splitLaw[i].substring(
													splitLaw[i].indexOf('(') + 1, splitLaw[i]
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
										} else if (splitLaw[i].contains("(")
												&& splitLaw[i].contains(",")) {
											String subString = (String) splitLaw[i].substring(
													splitLaw[i].indexOf('(') + 1, splitLaw[i]
															.indexOf(','));
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
											String subString = (String) splitLaw[i].substring(0,
													splitLaw[i].indexOf(')'));
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
											String subString = (String) splitLaw[i]
													.substring(splitLaw[i].indexOf('(') + 1);
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
									if (!pass || validKineticVars.size() == 0
											|| invalidKineticVars.size() != 0) {
										String invalid = "";
										for (int i = 0; i < invalidKineticVars.size(); i++) {
											if (i == invalidKineticVars.size() - 1) {
												invalid += invalidKineticVars.get(i);
											} else {
												invalid += invalidKineticVars.get(i) + "\n";
											}
										}
										String message;
										if (!option.equals("Save")) {
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
										JScrollPane scrolls = new JScrollPane();
										scrolls.setMinimumSize(new Dimension(300, 300));
										scrolls.setPreferredSize(new Dimension(300, 300));
										scrolls.setViewportView(messageArea);
										JOptionPane.showMessageDialog(biosim.frame(), scrolls,
												"Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
										if (!option.equals("Save")) {
											reactions
													.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
											Buttons.remove(reactions, reacts);
											reactions
													.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
											reactions.setSelectedIndex(0);
										}
									}
								}
							}
						}
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), reactionPanel,
								"Reaction Editor", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
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
		if (option.equals("Save") && parameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No parameter selected!",
					"Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
		} else {
			JPanel parametersPanel = new JPanel(new GridLayout(2, 2));
			JLabel idLabel = new JLabel("ID:");
			JLabel valueLabel = new JLabel("Value:");
			paramID = new JTextField();
			paramValue = new JTextField();
			if (option.equals("Save")) {
				try {
					Parameter paramet = document.getModel().getParameter(
							((String) parameters.getSelectedValue()).split(" ")[0]);
					paramID.setText(paramet.getId());
					paramValue.setText("" + paramet.getValue());
				} catch (Exception e) {

				}
			}
			parametersPanel.add(idLabel);
			parametersPanel.add(paramID);
			parametersPanel.add(valueLabel);
			parametersPanel.add(paramValue);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel,
					"Parameter Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (paramID.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter an ID into the ID field!", "Enter An ID",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					} else {
						try {
							double val = Double.parseDouble(paramValue.getText().trim());
							String param = paramID.getText().trim() + " " + val;
							if (usedIDs.contains(paramID.getText().trim())) {
								if (option.equals("Save")
										&& !paramID.getText().trim()
												.equals(
														((String) parameters.getSelectedValue())
																.split(" ")[0])) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique id into the id field!",
											"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
									error = true;
								} else if (option.equals("Add")) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique id into the id field!",
											"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
									error = true;
								}
							}
							if (!error) {
								if (option.equals("Save")) {
									int index = parameters.getSelectedIndex();
									String v = ((String) parameters.getSelectedValue()).split(" ")[0];
									Parameter paramet = document.getModel().getParameter(v);
									parameters
											.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									params = Buttons.getList(params, parameters);
									parameters
											.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									paramet.setId(paramID.getText().trim());
									for (int i = 0; i < usedIDs.size(); i++) {
										if (usedIDs.get(i).equals(v)) {
											usedIDs.set(i, paramID.getText().trim());
										}
									}
									paramet.setValue(val);
									params[index] = param;
									sort(params);
									parameters.setListData(params);
									parameters.setSelectedIndex(index);
								} else {
									int index = parameters.getSelectedIndex();
									Parameter paramet = document.getModel().createParameter();
									paramet.setId(paramID.getText().trim());
									usedIDs.add(paramID.getText().trim());
									paramet.setValue(val);
									JList add = new JList();
									Object[] adding = { param };
									add.setListData(adding);
									add.setSelectedIndex(0);
									parameters
											.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									adding = Buttons.add(params, parameters, add, false, null,
											null, null, null, null, null, null, biosim.frame());
									params = new String[adding.length];
									for (int i = 0; i < adding.length; i++) {
										params[i] = (String) adding[i];
									}
									sort(params);
									parameters.setListData(params);
									parameters
											.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									if (document.getModel().getNumParameters() == 1) {
										parameters.setSelectedIndex(0);
									} else {
										parameters.setSelectedIndex(index);
									}
								}
								change = true;
							}
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"You must enter a double into the value" + " field!",
									"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel,
								"Parameter Editor", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
		if (option.equals("Save") && reacParameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No parameter selected!",
					"Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
		} else {
			JPanel parametersPanel = new JPanel(new GridLayout(2, 2));
			JLabel idLabel = new JLabel("ID:");
			JLabel valueLabel = new JLabel("Value:");
			reacParamID = new JTextField();
			reacParamValue = new JTextField();
			if (option.equals("Save")) {
				String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
				Parameter paramet = null;
				for (Parameter p : changedParameters) {
					if (p.getId().equals(v)) {
						paramet = p;
					}
				}
				reacParamID.setText(paramet.getId());
				reacParamValue.setText("" + paramet.getValue());
			}
			parametersPanel.add(idLabel);
			parametersPanel.add(reacParamID);
			parametersPanel.add(valueLabel);
			parametersPanel.add(reacParamValue);
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel,
					"Parameter Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					if (reacParamID.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter an ID into the ID field!", "Enter An ID",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					} else {
						try {
							double val = Double.parseDouble(reacParamValue.getText().trim());
							String param = reacParamID.getText().trim() + " " + val;
							if (thisReactionParams.contains(reacParamID.getText().trim())) {
								if (option.equals("Save")
										&& !reacParamID.getText().trim().equals(
												((String) reacParameters.getSelectedValue())
														.split(" ")[0])) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique id into the id field!",
											"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
									error = true;
								} else if (option.equals("Add")) {
									JOptionPane.showMessageDialog(biosim.frame(),
											"You must enter a unique id into the id field!",
											"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
									error = true;
								}
							} else if (usedIDs.contains(reacParamID.getText().trim())) {
								JOptionPane.showMessageDialog(biosim.frame(),
										"You must enter a unique id into the id field!",
										"Enter A Unique ID", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
							if (!error) {
								if (option.equals("Save")) {
									int index = reacParameters.getSelectedIndex();
									String v = ((String) reacParameters.getSelectedValue())
											.split(" ")[0];
									Parameter paramet = null;
									for (Parameter p : changedParameters) {
										if (p.getId().equals(v)) {
											paramet = p;
										}
									}
									reacParameters
											.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									reacParams = Buttons.getList(reacParams, reacParameters);
									reacParameters
											.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									paramet.setId(reacParamID.getText().trim());
									for (int i = 0; i < thisReactionParams.size(); i++) {
										if (thisReactionParams.get(i).equals(v)) {
											thisReactionParams.set(i, reacParamID.getText().trim());
										}
									}
									paramet.setValue(val);
									reacParams[index] = param;
									sort(reacParams);
									reacParameters.setListData(reacParams);
									reacParameters.setSelectedIndex(index);
								} else {
									int index = reacParameters.getSelectedIndex();
									Parameter paramet = new Parameter();
									changedParameters.add(paramet);
									paramet.setId(reacParamID.getText().trim());
									thisReactionParams.add(reacParamID.getText().trim());
									paramet.setValue(val);
									JList add = new JList();
									Object[] adding = { param };
									add.setListData(adding);
									add.setSelectedIndex(0);
									reacParameters
											.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									adding = Buttons.add(reacParams, reacParameters, add, false,
											null, null, null, null, null, null, null, biosim
													.frame());
									reacParams = new String[adding.length];
									for (int i = 0; i < adding.length; i++) {
										reacParams[i] = (String) adding[i];
									}
									sort(reacParams);
									reacParameters.setListData(reacParams);
									reacParameters
											.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									try {
										if (document.getModel().getReaction(
												((String) reactions.getSelectedValue()))
												.getKineticLaw().getNumParameters() == 1) {
											reacParameters.setSelectedIndex(0);
										} else {
											reacParameters.setSelectedIndex(index);
										}
									} catch (Exception e2) {
										reacParameters.setSelectedIndex(0);
									}
								}
								change = true;
							}
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(biosim.frame(),
									"You must enter a double into the value" + " field!",
									"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), parametersPanel,
								"Parameter Editor", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
		if (option.equals("Save") && products.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No product selected!",
					"Must Select A Product", JOptionPane.ERROR_MESSAGE);
		} else {
			JPanel productsPanel = new JPanel(new GridLayout(2, 2));
			JLabel speciesLabel = new JLabel("Species:");
			JLabel stoiciLabel = new JLabel("Stoiciometry:");
			ListOf listOfSpecies = document.getModel().getListOfSpecies();
			amount = false;
			String[] speciesList = new String[(int) listOfSpecies.getNumItems()];
			for (int i = 0; i < listOfSpecies.getNumItems(); i++) {
				speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
			}
			sort(speciesList);
			Object[] choices = speciesList;
			productSpecies = new JComboBox(choices);
			productStoiciometry = new JTextField("1");
			if (option.equals("Save")) {
				String v = ((String) products.getSelectedValue()).split(" ")[0];
				SpeciesReference product = null;
				for (SpeciesReference p : changedProducts) {
					if (p.getSpecies().equals(v)) {
						product = p;
					}
				}
				productSpecies.setSelectedItem(product.getSpecies());
				productStoiciometry.setText("" + product.getStoichiometry());
			}
			productsPanel.add(speciesLabel);
			productsPanel.add(productSpecies);
			productsPanel.add(stoiciLabel);
			productsPanel.add(productStoiciometry);
			if (choices.length == 0) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"There are no species availiable to be products."
								+ "\nAdd species to this sbml file first.", "No Species",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), productsPanel,
					"Products Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					try {
						double val = Double.parseDouble(productStoiciometry.getText().trim());
						String prod = productSpecies.getSelectedItem() + " " + val;
						if (option.equals("Save")) {
							int index = products.getSelectedIndex();
							products
									.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							product = Buttons.getList(product, products);
							products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							products.setSelectedIndex(index);
							for (int i = 0; i < product.length; i++) {
								if (i != index) {
									if (product[i].split(" ")[0].equals(productSpecies
											.getSelectedItem())) {
										error = true;
										JOptionPane
												.showMessageDialog(
														biosim.frame(),
														"Unable to add species as a product.\n"
																+ "Each species can only be used as a product once.",
														"Species Can Only Be Used Once",
														JOptionPane.ERROR_MESSAGE);
									}
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
								products
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								product = Buttons.getList(product, products);
								products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								produ.setSpecies((String) productSpecies.getSelectedItem());
								produ.setStoichiometry(val);
								product[index] = prod;
								sort(product);
								products.setListData(product);
								products.setSelectedIndex(index);
							}
						} else {
							int index = products.getSelectedIndex();
							products
									.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							product = Buttons.getList(product, products);
							products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							products.setSelectedIndex(index);
							for (int i = 0; i < product.length; i++) {
								if (product[i].split(" ")[0].equals(productSpecies
										.getSelectedItem())) {
									error = true;
									JOptionPane
											.showMessageDialog(
													biosim.frame(),
													"Unable to add species as a product.\n"
															+ "Each species can only be used as a product once.",
													"Species Can Only Be Used Once",
													JOptionPane.ERROR_MESSAGE);
								}
							}
							if (!error) {
								SpeciesReference produ = new SpeciesReference();
								changedProducts.add(produ);
								produ.setSpecies((String) productSpecies.getSelectedItem());
								produ.setStoichiometry(val);
								JList add = new JList();
								Object[] adding = { prod };
								add.setListData(adding);
								add.setSelectedIndex(0);
								products
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(product, products, add, false, null, null,
										null, null, null, null, null, biosim.frame());
								product = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									product[i] = (String) adding[i];
								}
								sort(product);
								products.setListData(product);
								products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								try {
									if (document.getModel().getReaction(
											((String) reactions.getSelectedValue()))
											.getNumProducts() == 1) {
										products.setSelectedIndex(0);
									} else {
										products.setSelectedIndex(index);
									}
								} catch (Exception e2) {
									products.setSelectedIndex(0);
								}
							}
						}
						change = true;
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter a double into the stoiciometry" + " field!",
								"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), productsPanel,
								"Products Editor", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
		if (option.equals("Save") && reactants.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(biosim.frame(), "No reactant selected!",
					"Must Select A Reactant", JOptionPane.ERROR_MESSAGE);
		} else {
			JPanel reactantsPanel = new JPanel(new GridLayout(2, 2));
			JLabel speciesLabel = new JLabel("Species:");
			JLabel stoiciLabel = new JLabel("Stoiciometry:");
			ListOf listOfSpecies = document.getModel().getListOfSpecies();
			amount = false;
			String[] speciesList = new String[(int) listOfSpecies.getNumItems()];
			for (int i = 0; i < listOfSpecies.getNumItems(); i++) {
				speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
			}
			sort(speciesList);
			Object[] choices = speciesList;
			reactantSpecies = new JComboBox(choices);
			reactantStoiciometry = new JTextField("1");
			if (option.equals("Save")) {
				String v = ((String) reactants.getSelectedValue()).split(" ")[0];
				SpeciesReference reactant = null;
				for (SpeciesReference r : changedReactants) {
					if (r.getSpecies().equals(v)) {
						reactant = r;
					}
				}
				reactantSpecies.setSelectedItem(reactant.getSpecies());
				reactantStoiciometry.setText("" + reactant.getStoichiometry());
			}
			reactantsPanel.add(speciesLabel);
			reactantsPanel.add(reactantSpecies);
			reactantsPanel.add(stoiciLabel);
			reactantsPanel.add(reactantStoiciometry);
			if (choices.length == 0) {
				JOptionPane.showMessageDialog(biosim.frame(),
						"There are no species availiable to be reactants."
								+ "\nAdd species to this sbml file first.", "No Species",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object[] options = { option, "Cancel" };
			int value = JOptionPane.showOptionDialog(biosim.frame(), reactantsPanel,
					"Reactants Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				boolean error = true;
				while (error && value == JOptionPane.YES_OPTION) {
					error = false;
					try {
						double val = Double.parseDouble(reactantStoiciometry.getText().trim());
						String react = reactantSpecies.getSelectedItem() + " " + val;
						if (option.equals("Save")) {
							int index = reactants.getSelectedIndex();
							reactants
									.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							reacta = Buttons.getList(reacta, reactants);
							reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							reactants.setSelectedIndex(index);
							for (int i = 0; i < reacta.length; i++) {
								if (i != index) {
									if (reacta[i].split(" ")[0].equals(reactantSpecies
											.getSelectedItem())) {
										error = true;
										JOptionPane
												.showMessageDialog(
														biosim.frame(),
														"Unable to add species as a reactant.\n"
																+ "Each species can only be used as a reactant once.",
														"Species Can Only Be Used Once",
														JOptionPane.ERROR_MESSAGE);
									}
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
								reactants
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacta = Buttons.getList(reacta, reactants);
								reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reactan.setSpecies((String) reactantSpecies.getSelectedItem());
								reactan.setStoichiometry(val);
								reacta[index] = react;
								sort(reacta);
								reactants.setListData(reacta);
								reactants.setSelectedIndex(index);
							}
						} else {
							int index = reactants.getSelectedIndex();
							reactants
									.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							reacta = Buttons.getList(reacta, reactants);
							reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							reactants.setSelectedIndex(index);
							for (int i = 0; i < reacta.length; i++) {
								if (reacta[i].split(" ")[0].equals(reactantSpecies
										.getSelectedItem())) {
									error = true;
									JOptionPane
											.showMessageDialog(
													biosim.frame(),
													"Unable to add species as a reactant.\n"
															+ "Each species can only be used as a reactant once.",
													"Species Can Only Be Used Once",
													JOptionPane.ERROR_MESSAGE);
								}
							}
							if (!error) {
								SpeciesReference reactan = new SpeciesReference();
								changedReactants.add(reactan);
								reactan.setSpecies((String) reactantSpecies.getSelectedItem());
								reactan.setStoichiometry(val);
								JList add = new JList();
								Object[] adding = { react };
								add.setListData(adding);
								add.setSelectedIndex(0);
								reactants
										.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Buttons.add(reacta, reactants, add, false, null, null,
										null, null, null, null, null, biosim.frame());
								reacta = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									reacta[i] = (String) adding[i];
								}
								sort(reacta);
								reactants.setListData(reacta);
								reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								try {
									if (document.getModel().getReaction(
											((String) reactions.getSelectedValue()))
											.getNumReactants() == 1) {
										reactants.setSelectedIndex(0);
									} else {
										reactants.setSelectedIndex(index);
									}
								} catch (Exception e2) {
									reactants.setSelectedIndex(0);
								}
							}
						}
						change = true;
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(biosim.frame(),
								"You must enter a double into the stoiciometry" + " field!",
								"Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (error) {
						value = JOptionPane.showOptionDialog(biosim.frame(), reactantsPanel,
								"Reactants Editor", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
				compartEditor("Save");
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

	/**
	 * Saves the sbml file.
	 */
	public void save() {
		try {
			log.addText("Saving sbml file:\n" + file + "\n");
			FileOutputStream out = new FileOutputStream(new File(file));
			SBMLWriter writer = new SBMLWriter();
			String doc = writer.writeToString(document);
			byte[] output = doc.getBytes();
			out.write(output);
			out.close();
			change = false;
			if (reb2sac != null) {
				reb2sac.updateSpeciesList();
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to save sbml file!",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
	}
}