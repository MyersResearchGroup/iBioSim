package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.MutableBoolean;
import main.util.Utility;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.SpeciesType;
import org.sbml.libsbml.UnitDefinition;

import biomodel.parser.BioModel;

/**
 * This is a class for creating SBML species
 * 
 * @author Chris Myers
 * 
 */
public class MySpecies extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private BioModel gcm;

	private ArrayList<String> usedIDs;

	private MutableBoolean dirty;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private Gui biosim;

	private JButton addSpec, removeSpec, editSpec; // species buttons

	private JList species; // JList of species

	private JTextField ID, init, Name; // species text fields

	private JComboBox specTypeBox, specBoundary, specConstant, specHasOnly; // species

	private JComboBox specUnits, initLabel, specConv;

	private JComboBox comp; // compartment combo box

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;

	public MySpecies(Gui biosim, BioModel gcm, ArrayList<String> usedIDs, MutableBoolean dirty, Boolean paramsOnly,
			ArrayList<String> getParams, String file, ArrayList<String> parameterChanges, Boolean editOnly) {
		super(new BorderLayout());
		this.gcm = gcm;
		this.usedIDs = usedIDs;
		this.biosim = biosim;
		this.dirty = dirty;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
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
		if (paramsOnly || editOnly) {
			addSpec.setEnabled(false);
			removeSpec.setEnabled(false);
		}
		JLabel speciesLabel = new JLabel("List of Species:");
		species = new JList();
		species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll1 = new JScrollPane();
		scroll1.setViewportView(species);
		Model model = gcm.getSBMLDocument().getModel();
		ListOf listOfSpecies = model.getListOfSpecies();
		String[] specs = new String[(int) model.getNumSpecies()];
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = (Species) listOfSpecies.get(i);
			/*
			 * if (species.isSetSpeciesType()) { specs[i] = species.getId() +
			 * " " + species.getSpeciesType() + " " + species.getCompartment();
			 * } else {
			 */
			specs[i] = species.getId(); // + " " + species.getCompartment();
			// }
			if (species.isSetInitialAmount()) {
				specs[i] += " " + species.getInitialAmount();
			}
			else {
				specs[i] += " " + species.getInitialConcentration();
			}
			/*
			 * if (species.isSetUnits()) { specs[i] += " " + species.getUnits();
			 * }
			 */
			if (paramsOnly) {
				for (int j = 0; j < getParams.size(); j++) {
					if (getParams.get(j).split(" ")[0].equals(species.getId())) {
						parameterChanges.add(getParams.get(j));
						String[] splits = getParams.get(j).split(" ");
						if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
							String value = splits[splits.length - 1];
							if (species.isSetInitialAmount()) {
								species.setInitialAmount(Double.parseDouble(value));
							}
							else {
								species.setInitialConcentration(Double.parseDouble(value));
							}
							specs[i] += " Modified " + splits[splits.length - 1];
						}
						else if (splits[splits.length - 2].equals("Sweep")) {
							String value = splits[splits.length - 1];
							if (species.isSetInitialAmount()) {
								species.setInitialAmount(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							}
							else {
								species.setInitialConcentration(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							}
							specs[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
						}
					}
				}
			}
		}
		Utility.sort(specs);
		species.setListData(specs);
		species.setSelectedIndex(0);
		species.addMouseListener(this);
		this.add(speciesLabel, "North");
		this.add(scroll1, "Center");
		this.add(addSpecs, "South");
	}

	/**
	 * Refresh species panel
	 */
	public void refreshSpeciesPanel(BioModel gcm) {
		String selectedSpecies = "";
		if (!species.isSelectionEmpty()) {
			selectedSpecies = ((String) species.getSelectedValue()).split(" ")[0];
		}
		this.gcm = gcm;
		Model model = gcm.getSBMLDocument().getModel();
		ListOf listOfSpecies = model.getListOfSpecies();
		String[] specs = new String[(int) model.getNumSpecies()];
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = (Species) listOfSpecies.get(i);
			specs[i] = species.getId();
			if (species.isSetInitialAmount()) {
				specs[i] += " " + species.getInitialAmount();
			}
			else {
				specs[i] += " " + species.getInitialConcentration();
			}
			if (paramsOnly) {
				for (int j = 0; j < parameterChanges.size(); j++) {
					if (parameterChanges.get(j).split(" ")[0].equals(specs[i].split(" ")[0])) {
						parameterChanges.set(j, specs[i] + " " + parameterChanges.get(j).split(" ")[2] + " " + parameterChanges.get(j).split(" ")[3]);
						specs[i] = parameterChanges.get(j);
					}
				}
			}
		}
		Utility.sort(specs);
		int selected = 0;
		for (int i = 0; i < specs.length; i++) {
			if (specs[i].split(" ")[0].equals(selectedSpecies)) {
				selected = i;
			}
		}
		species.setListData(specs);
		species.setSelectedIndex(selected);
	}

	/**
	 * Creates a frame used to edit species or create new ones.
	 */
	private void speciesEditor(String option) {
		if (option.equals("OK") && species.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No species selected.", "Must Select A Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel speciesPanel;
		if (paramsOnly) {
			speciesPanel = new JPanel(new GridLayout(11, 2));
		}
		else {
			speciesPanel = new JPanel(new GridLayout(9, 2));
		}
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel specTypeLabel = new JLabel("Type:");
		JLabel compLabel = new JLabel("Compartment:");
		String[] initLabels = { "Initial Amount", "Initial Concentration", "Initial Assignment" };
		initLabel = new JComboBox(initLabels);
		JLabel unitLabel = new JLabel("Units:");
		JLabel boundLabel = new JLabel("Boundary Condition:");
		JLabel constLabel = new JLabel("Constant:");
		JLabel hasOnlyLabel = new JLabel("Has Only Substance Units:");
		JLabel convLabel = new JLabel("Conversion Factor:");
		ID = new JTextField();
		String selectedID = "";
		Name = new JTextField();
		init = new JTextField("0.0");
		String origAssign = "";
		specUnits = createUnitsChoices(gcm);
		if (gcm.getSBMLDocument().getLevel() > 2) {
			specConv = createConversionFactorChoices(gcm);
		}
		String[] optionsTF = { "true", "false" };
		specBoundary = new JComboBox(optionsTF);
		specBoundary.setSelectedItem("false");
		specConstant = new JComboBox(optionsTF);
		specConstant.setSelectedItem("false");
		specHasOnly = new JComboBox(optionsTF);
		specHasOnly.setSelectedItem("false");
		ListOf listOfSpecTypes = gcm.getSBMLDocument().getModel().getListOfSpeciesTypes();
		String[] specTypeList = new String[(int) gcm.getSBMLDocument().getModel().getNumSpeciesTypes() + 1];
		specTypeList[0] = "( none )";
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumSpeciesTypes(); i++) {
			specTypeList[i + 1] = ((SpeciesType) listOfSpecTypes.get(i)).getId();
		}
		Utility.sort(specTypeList);
		Object[] choices = specTypeList;
		specTypeBox = new JComboBox(choices);
		comp = createCompartmentChoices(gcm);
		String[] list = { "Original", "Modified" };
		String[] list1 = { "1", "2" };
		final JComboBox type = new JComboBox(list);
		final JTextField start = new JTextField();
		final JTextField stop = new JTextField();
		final JTextField step = new JTextField();
		final JComboBox level = new JComboBox(list1);
		final JButton sweep = new JButton("Sweep");
		sweep.addActionListener(new ActionListener() {
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
					init.setText("(" + startVal + "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ")");
				}
			}
		});
		if (paramsOnly) {
			ID.setEditable(false);
			Name.setEditable(false);
			specTypeBox.setEnabled(false);
			specBoundary.setEnabled(false);
			specConstant.setEnabled(false);
			specHasOnly.setEnabled(false);
			comp.setEnabled(false);
			init.setEnabled(false);
			initLabel.setEnabled(false);
			specUnits.setEnabled(false);
			specConv.setEnabled(false);
			sweep.setEnabled(false);
		}
		if (option.equals("OK")) {
			try {
				Species specie = gcm.getSBMLDocument().getModel().getSpecies(((String) species.getSelectedValue()).split(" ")[0]);
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
				if (specie.getHasOnlySubstanceUnits()) {
					specHasOnly.setSelectedItem("true");
				}
				else {
					specHasOnly.setSelectedItem("false");
				}
				if (gcm.getSBMLDocument().getModel().getInitialAssignment(specie.getId()) != null) {
					origAssign = SBMLutilities.myFormulaToString(gcm.getSBMLDocument().getModel().getInitialAssignment(specie.getId()).getMath());
					init.setText(origAssign);
					initLabel.setSelectedItem("Initial Assignment");
				}
				else if (specie.isSetInitialAmount()) {
					init.setText("" + specie.getInitialAmount());
					initLabel.setSelectedItem("Initial Amount");
				}
				else {
					init.setText("" + specie.getInitialConcentration());
					initLabel.setSelectedItem("Initial Concentration");
				}
				if (specie.isSetUnits()) {
					specUnits.setSelectedItem(specie.getUnits());
				}
				comp.setSelectedItem(specie.getCompartment());
				if (specie.isSetConversionFactor()) {
					specConv.setSelectedItem(specie.getConversionFactor());
				}
				if (paramsOnly
						&& (((String) species.getSelectedValue()).contains("Modified") || ((String) species.getSelectedValue()).contains("Custom") || ((String) species
								.getSelectedValue()).contains("Sweep"))) {
					type.setSelectedItem("Modified");
					sweep.setEnabled(true);
					init.setText(((String) species.getSelectedValue()).split(" ")[((String) species.getSelectedValue()).split(" ").length - 1]);
					init.setEnabled(true);
					initLabel.setEnabled(false);
					if (init.getText().trim().startsWith("(")) {
						try {
							start.setText((init.getText().trim()).split(",")[0].substring(1).trim());
							stop.setText((init.getText().trim()).split(",")[1].trim());
							step.setText((init.getText().trim()).split(",")[2].trim());
							int lev = Integer.parseInt((init.getText().trim()).split(",")[3].replace(")", "").trim());
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
			catch (Exception e) {
			}
		}
		speciesPanel.add(idLabel);
		speciesPanel.add(ID);
		speciesPanel.add(nameLabel);
		speciesPanel.add(Name);
		if (gcm.getSBMLDocument().getLevel() < 3) {
			speciesPanel.add(specTypeLabel);
			speciesPanel.add(specTypeBox);
		}
		speciesPanel.add(compLabel);
		speciesPanel.add(comp);
		speciesPanel.add(boundLabel);
		speciesPanel.add(specBoundary);
		speciesPanel.add(constLabel);
		speciesPanel.add(specConstant);
		speciesPanel.add(hasOnlyLabel);
		speciesPanel.add(specHasOnly);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Value Type:");
			type.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						sweep.setEnabled(true);
						init.setEnabled(true);
						initLabel.setEnabled(false);
					}
					else {
						sweep.setEnabled(false);
						init.setEnabled(false);
						initLabel.setEnabled(false);
						SBMLDocument d = Gui.readSBML(file);
						if (d.getModel().getSpecies(((String) species.getSelectedValue()).split(" ")[0]).isSetInitialAmount()) {
							init.setText(d.getModel().getSpecies(((String) species.getSelectedValue()).split(" ")[0]).getInitialAmount() + "");
							initLabel.setSelectedItem("Initial Amount");
						}
						else {
							init.setText(d.getModel().getSpecies(((String) species.getSelectedValue()).split(" ")[0]).getInitialConcentration() + "");
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
		if (paramsOnly) {
			speciesPanel.add(new JLabel());
			speciesPanel.add(sweep);
		}
		speciesPanel.add(unitLabel);
		speciesPanel.add(specUnits);
		if (gcm.getSBMLDocument().getLevel() > 2) {
			speciesPanel.add(convLabel);
			speciesPanel.add(specConv);
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, speciesPanel, "Species Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(gcm.getSBMLDocument(), usedIDs, ID.getText().trim(), selectedID, false);
			double initial = 0;
			if (!error) {
				if (init.getText().trim().startsWith("(") && init.getText().trim().endsWith(")")) {
					try {
						Double.parseDouble((init.getText().trim()).split(",")[0].substring(1).trim());
						Double.parseDouble((init.getText().trim()).split(",")[1].trim());
						Double.parseDouble((init.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((init.getText().trim()).split(",")[3].replace(")", "").trim());
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
					if (initLabel.getSelectedItem().equals("Initial Assignment")) {
						InitialAssignments.removeInitialAssignment(gcm, selectedID);
						error = InitialAssignments.addInitialAssignment(biosim, gcm, ID.getText().trim(), init.getText().trim());
						initial = 0.0;
					}
					else {
						if (!selectedID.equals("")) {
							InitialAssignments.removeInitialAssignment(gcm, selectedID);
						}
						try {
							initial = Double.parseDouble(init.getText().trim());
						}
						catch (Exception e1) {
							error = true;
							JOptionPane.showMessageDialog(Gui.frame, "The initial value field must be a real number.", "Enter a Valid Initial Value",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				String unit = (String) specUnits.getSelectedItem();
				String convFactor = null;
				if (gcm.getSBMLDocument().getLevel() > 2) {
					convFactor = (String) specConv.getSelectedItem();
				}
				String addSpec = "";
				String selSpecType = (String) specTypeBox.getSelectedItem();
				if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
					String[] specs = new String[species.getModel().getSize()];
					for (int i = 0; i < species.getModel().getSize(); i++) {
						specs[i] = species.getModel().getElementAt(i).toString();
					}
					int index = species.getSelectedIndex();
					String[] splits = specs[index].split(" ");
					for (int i = 0; i < splits.length - 2; i++) {
						addSpec += splits[i] + " ";
					}
					if (!splits[splits.length - 2].equals("Modified") && !splits[splits.length - 2].equals("Custom")
							&& !splits[splits.length - 2].equals("Sweep")) {
						addSpec += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
					}
					if (init.getText().trim().startsWith("(") && init.getText().trim().endsWith(")")) {
						double startVal = Double.parseDouble((init.getText().trim()).split(",")[0].substring(1).trim());
						double stopVal = Double.parseDouble((init.getText().trim()).split(",")[1].trim());
						double stepVal = Double.parseDouble((init.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((init.getText().trim()).split(",")[3].replace(")", "").trim());
						addSpec += "Sweep (" + startVal + "," + stopVal + "," + stepVal + "," + lev + ")";
					}
					else {
						addSpec += "Modified " + initial;
					}
				}
				else {
					addSpec = ID.getText().trim();
					/*
					 * if (!selSpecType.equals("( none )")) { addSpec += " " +
					 * selSpecType; }
					 */
					addSpec += " " /* + comp.getSelectedItem() + " " */+ initial;
					/*
					 * if (!unit.equals("( none )")) { addSpec += " " + unit; }
					 */
				}
				if (!error) {
					ListOf listOfSpecies = gcm.getSBMLDocument().getModel().getListOfSpecies();
					String selected = "";
					if (option.equals("OK")) {
						selected = ((String) species.getSelectedValue()).split(" ")[0];
					}
					for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumSpecies(); i++) {
						if (!((Species) listOfSpecies.get(i)).getId().equals(selected)) {
							if (((Species) listOfSpecies.get(i)).getCompartment().equals((String) comp.getSelectedItem())
									&& ((Species) listOfSpecies.get(i)).getSpeciesType().equals(selSpecType)) {
								JOptionPane.showMessageDialog(Gui.frame, "Compartment already contains another species of this type.",
										"Species Type Not Unique", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
				if (!error) {
					Compartment compartment = gcm.getSBMLDocument().getModel().getCompartment((String) comp.getSelectedItem());
					if (initLabel.getSelectedItem().equals("Initial Concentration") && gcm.getSBMLDocument().getLevel() < 3
							&& compartment.getSpatialDimensions() == 0) {
						JOptionPane.showMessageDialog(Gui.frame, "Species in a 0 dimensional compartment cannot have an initial concentration.",
								"Concentration Not Possible", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error && option.equals("OK") && specConstant.getSelectedItem().equals("true")) {
					String val = ((String) species.getSelectedValue()).split(" ")[0];
					error = SBMLutilities.checkConstant(gcm.getSBMLDocument(), "Species", val);
				}
				if (!error && option.equals("OK") && specBoundary.getSelectedItem().equals("false")) {
					String val = ((String) species.getSelectedValue()).split(" ")[0];
					error = checkBoundary(val, specConstant.getSelectedItem().equals("false"));
				}
				if (!error) {
					if (option.equals("OK")) {
						String[] specs = new String[species.getModel().getSize()];
						for (int i = 0; i < species.getModel().getSize(); i++) {
							specs[i] = species.getModel().getElementAt(i).toString();
						}
						int index1 = species.getSelectedIndex();
						String val = ((String) species.getSelectedValue()).split(" ")[0];
						species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						specs = Utility.getList(specs, species);
						species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						Species specie = gcm.getSBMLDocument().getModel().getSpecies(val);
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
						if (specHasOnly.getSelectedItem().equals("true")) {
							specie.setHasOnlySubstanceUnits(true);
						}
						else {
							specie.setHasOnlySubstanceUnits(false);
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
							specie.unsetInitialConcentration();
							specie.setInitialAmount(initial);
						}
						else if (initLabel.getSelectedItem().equals("Initial Concentration")) {
							specie.unsetInitialAmount();
							specie.setInitialConcentration(initial);
						}
						else {
							specie.unsetInitialConcentration();
							specie.setInitialAmount(initial);
						}
						if (unit.equals("( none )")) {
							specie.unsetUnits();
						}
						else {
							specie.setUnits(unit);
						}
						if (gcm.getSBMLDocument().getLevel() > 2) {
							if (convFactor.equals("( none )")) {
								specie.unsetConversionFactor();
							}
							else {
								specie.setConversionFactor(convFactor);
							}
						}
						specs[index1] = addSpec;
						Utility.sort(specs);
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
							SBMLutilities.updateVarId(gcm.getSBMLDocument(), true, speciesName, specie.getId());
						}
					}
					else {
						String[] specs = new String[species.getModel().getSize()];
						for (int i = 0; i < species.getModel().getSize(); i++) {
							specs[i] = species.getModel().getElementAt(i).toString();
						}
						int index1 = species.getSelectedIndex();
						Species specie = gcm.getSBMLDocument().getModel().createSpecies();
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
						if (specHasOnly.getSelectedItem().equals("true")) {
							specie.setHasOnlySubstanceUnits(true);
						}
						else {
							specie.setHasOnlySubstanceUnits(false);
						}
						usedIDs.add(ID.getText().trim());
						if (!selSpecType.equals("( none )")) {
							specie.setSpeciesType(selSpecType);
						}
						if (initLabel.getSelectedItem().equals("Initial Amount")) {
							specie.setInitialAmount(initial);
						}
						else if (initLabel.getSelectedItem().equals("Initial Concentration")) {
							specie.setInitialConcentration(initial);
						}
						else {
							specie.setInitialAmount(initial);
						}
						if (!unit.equals("( none )")) {
							specie.setUnits(unit);
						}
						if (gcm.getSBMLDocument().getLevel() > 2) {
							if (convFactor.equals("( none )")) {
								specie.unsetConversionFactor();
							}
							else {
								specie.setConversionFactor(convFactor);
							}
						}
						JList addIt = new JList();
						Object[] adding = { addSpec };
						addIt.setListData(adding);
						addIt.setSelectedIndex(0);
						species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(specs, species, addIt, null, null, null, null, null, Gui.frame);
						specs = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							specs[i] = (String) adding[i];
						}
						Utility.sort(specs);
						species.setListData(specs);
						species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (gcm.getSBMLDocument().getModel().getNumSpecies() == 1) {
							species.setSelectedIndex(0);
						}
						else {
							species.setSelectedIndex(index1);
						}
					}
					dirty.setValue(true);
					gcm.makeUndoPoint();
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, speciesPanel, "Species Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			if (!origAssign.equals("")) {
				InitialAssignments.removeInitialAssignment(gcm, selectedID);
				error = InitialAssignments.addInitialAssignment(biosim, gcm, selectedID, origAssign);
			}
			return;
		}
	}

	/**
	 * creates a frame for editing grid species
	 */
	private void openGridSpeciesEditor() {
		
		JPanel speciesPanel = new JPanel(new GridLayout(8, 2));
		
		String[] initLabels = {"Initial Amount", "Initial Concentration", "Initial Assignment"};
		initLabel = new JComboBox(initLabels);
		
		ID = new JTextField();
		ID.setEditable(false);
		
		Name = new JTextField();
		
		init = new JTextField("0.0");
		init.setEditable(false);
		
		JTextField kecdiff = new JTextField("0.0");
		JTextField kecd = new JTextField("0.0");		
		
		String[] optionsTF = { "true", "false" };
		
		specBoundary = new JComboBox(optionsTF);
		specBoundary.setSelectedItem("false");
		
		specConstant = new JComboBox(optionsTF);
		specConstant.setSelectedItem("false");
		
		specHasOnly = new JComboBox(optionsTF);
		specHasOnly.setSelectedItem("true");
		
		speciesPanel.add(new JLabel("ID:"));
		speciesPanel.add(ID);
		speciesPanel.add(new JLabel("Name:"));
		speciesPanel.add(Name);
		speciesPanel.add(new JLabel("Boundary Condition:"));
		speciesPanel.add(specBoundary);
		speciesPanel.add(new JLabel("Constant:"));
		speciesPanel.add(specConstant);
		speciesPanel.add(new JLabel("Has Only Substance Units:"));
		speciesPanel.add(specHasOnly);
		speciesPanel.add(initLabel);
		speciesPanel.add(init);
		speciesPanel.add(new JLabel("Extracellular diffusion rate (kecdiff)"));
		speciesPanel.add(kecdiff);
		speciesPanel.add(new JLabel("Extracellular degradation rate (kecd)"));
		speciesPanel.add(kecd);
		
		//set the values of the species fields using the sbml gcm.getSBMLDocument()
		try {
			
			Species selectedSpecies = gcm.getSBMLDocument().getModel().getSpecies(((String)species.getSelectedValue()).split(" ")[0]);
			
			ID.setText(selectedSpecies.getId());
			ID.setEditable(false);
			Name.setText(selectedSpecies.getName());
			
			if (selectedSpecies.getBoundaryCondition())
				specBoundary.setSelectedItem("true");
			else
				specBoundary.setSelectedItem("false");
			
			if (selectedSpecies.getConstant())
				specConstant.setSelectedItem("true");
			else
				specConstant.setSelectedItem("false");
			
			if (selectedSpecies.getHasOnlySubstanceUnits())
				specHasOnly.setSelectedItem("true");
			else
				specHasOnly.setSelectedItem("false");
			
			if (gcm.getSBMLDocument().getModel().getInitialAssignment(selectedSpecies.getId()) != null) {
				
				init.setText(SBMLutilities.myFormulaToString(
						gcm.getSBMLDocument().getModel().getInitialAssignment(selectedSpecies.getId()).getMath()));
				initLabel.setSelectedItem("Initial Assignment");
			}
			else if (selectedSpecies.isSetInitialAmount()) {
				
				init.setText("" + selectedSpecies.getInitialAmount());
				initLabel.setSelectedItem("Initial Amount");
			}
			else {
				
				init.setText("" + selectedSpecies.getInitialConcentration());
				initLabel.setSelectedItem("Initial Concentration");
			}
			
			kecdiff.setText(String.valueOf(gcm.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Above")
					.getKineticLaw().getParameter("kecdiff").getValue()));
			kecd.setText(String.valueOf(gcm.getSBMLDocument().getModel().getReaction("Degradation_" + selectedSpecies.getId())
					.getKineticLaw().getParameter("kecd").getValue()));			
		}
		catch (Exception e) {			
			e.printStackTrace();
		}

		//show the frame
		Object[] options = { "OK", "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, speciesPanel, "Species Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		
		if (value == JOptionPane.YES_OPTION) {
			
			Species selectedSpecies = gcm.getSBMLDocument().getModel().getSpecies(((String)species.getSelectedValue()).split(" ")[0]);
			
			//set the species settings in the sbml model
			selectedSpecies.setBoundaryCondition(Boolean.valueOf((String) specBoundary.getModel().getSelectedItem()));
			selectedSpecies.setHasOnlySubstanceUnits(Boolean.valueOf((String) specHasOnly.getModel().getSelectedItem()));
			selectedSpecies.setConstant(Boolean.valueOf((String) specConstant.getModel().getSelectedItem()));
			
			//set the local parameters in the diffusion/degredation reactions
			double kecdiffRate = Double.parseDouble(kecdiff.getText());
			double kecdRate = Double.parseDouble(kecd.getText());
			
			gcm.getSBMLDocument().getModel().getReaction("Degradation_" + selectedSpecies.getId())
				.getKineticLaw().getParameter("kecd").setValue(kecdRate);			
			gcm.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Above")
				.getKineticLaw().getParameter("kecdiff").setValue(kecdiffRate);			
			gcm.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Below")
				.getKineticLaw().getParameter("kecdiff").setValue(kecdiffRate);
			gcm.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Left")
				.getKineticLaw().getParameter("kecdiff").setValue(kecdiffRate);
			gcm.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Right")
				.getKineticLaw().getParameter("kecdiff").setValue(kecdiffRate);		
		}
	}
	
	public static JComboBox createCompartmentChoices(BioModel gcm) {
		ListOf listOfCompartments = gcm.getSBMLDocument().getModel().getListOfCompartments();
		String[] add = new String[(int) gcm.getSBMLDocument().getModel().getNumCompartments()];
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumCompartments(); i++) {
			add[i] = ((Compartment) listOfCompartments.get(i)).getId();
		}
		try {
			add[0].getBytes();
		}
		catch (Exception e) {
			add = new String[1];
			add[0] = "default";
		}
		JComboBox comp = new JComboBox(add);
		return comp;
	}

	public static JComboBox createUnitsChoices(BioModel gcm) {
		JComboBox specUnits = new JComboBox();
		specUnits.addItem("( none )");
		ListOf listOfUnits = gcm.getSBMLDocument().getModel().getListOfUnitDefinitions();
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumUnitDefinitions(); i++) {
			UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
			if ((unit.getNumUnits() == 1)
					&& (unit.getUnit(0).isMole() || unit.getUnit(0).isItem() || unit.getUnit(0).isGram() || unit.getUnit(0).isKilogram())
					&& (unit.getUnit(0).getExponentAsDouble() == 1)) {
				if (!(gcm.getSBMLDocument().getLevel() < 3 && unit.getId().equals("substance"))) {
					specUnits.addItem(unit.getId());
				}
			}
		}
		if (gcm.getSBMLDocument().getLevel() < 3) {
			specUnits.addItem("substance");
		}
		String[] unitIds = { "dimensionless", "gram", "item", "kilogram", "mole" };
		for (int i = 0; i < unitIds.length; i++) {
			specUnits.addItem(unitIds[i]);
		}
		return specUnits;
	}

	public static JComboBox createConversionFactorChoices(BioModel gcm) {
		JComboBox specConv;
		specConv = new JComboBox();
		specConv.addItem("( none )");
		ListOf listOfParameters = gcm.getSBMLDocument().getModel().getListOfParameters();
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumParameters(); i++) {
			Parameter param = (Parameter) listOfParameters.get(i);
			if (param.getConstant()) {
				specConv.addItem(param.getId());
			}
		}
		return specConv;
	}

	/**
	 * Species that is a reactant or product cannot be constant unless it is a
	 * boundary condition
	 */
	private boolean checkBoundary(String val, boolean checkRule) {
		Model model = gcm.getSBMLDocument().getModel();
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
							JOptionPane.showMessageDialog(Gui.frame, "Boundary condition cannot be false if a species is used\n"
									+ "in a rule and as a reactant or product in a reaction.", "Boundary Condition Cannot be False",
									JOptionPane.ERROR_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(Gui.frame, "Species cannot be reactant if constant and not a boundary condition.",
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
							JOptionPane.showMessageDialog(Gui.frame, "Boundary condition cannot be false if a species is used\n"
									+ "in a rule and as a reactant or product in a reaction.", "Boundary Condition Cannot be False",
									JOptionPane.ERROR_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(Gui.frame, "Species cannot be product if constant and not a boundary condition.",
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
	 * Remove a species
	 */
	private void removeSpecies() {
		int index = species.getSelectedIndex();
		if (index != -1) {
			if (!SBMLutilities.variableInUse(gcm.getSBMLDocument(), ((String) species.getSelectedValue()).split(" ")[0], false, true, true)) {
				Species tempSpecies = gcm.getSBMLDocument().getModel().getSpecies(((String) species.getSelectedValue()).split(" ")[0]);
				ListOf s = gcm.getSBMLDocument().getModel().getListOfSpecies();
				for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumSpecies(); i++) {
					if (((Species) s.get(i)).getId().equals(tempSpecies.getId())) {
						s.remove(i);
					}
				}
				usedIDs.remove(tempSpecies.getId());
				species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Utility.remove(species);
				species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < species.getModel().getSize()) {
					species.setSelectedIndex(index);
				}
				else {
					species.setSelectedIndex(index - 1);
				}
				dirty.setValue(true);
				gcm.makeUndoPoint();
			}
		}
	}

	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	public void actionPerformed(ActionEvent e) {
		// if the add species button is clicked
		if (e.getSource() == addSpec) {
			speciesEditor("Add");
		}
		// if the edit species button is clicked
		else if (e.getSource() == editSpec) {
			
			
			if (species.getModel().getSize() > 0) {
								
				//if we're dealing with grid species, use a different species editor
				if (gcm.getSBMLDocument().getModel().getSpecies(((String)species.getModel().getElementAt(0)).split(" ")[0])
						.getAnnotation() != null &&						
						gcm.getSBMLDocument().getModel().getSpecies(((String)species.getModel().getElementAt(0)).split(" ")[0])
						.getAnnotationString().contains("type=\"grid\"")) {
					
					openGridSpeciesEditor();
				}
				else {
					
					speciesEditor("OK");
					initialsPanel.refreshInitialAssignmentPanel(gcm);
					rulesPanel.refreshRulesPanel();
				}
			}
			else {
				
				JOptionPane.showMessageDialog(Gui.frame, "No species selected.", 
						"Must Select A Species", JOptionPane.ERROR_MESSAGE);
			}
		}
		// if the remove species button is clicked
		else if (e.getSource() == removeSpec) {
			removeSpecies();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == species) {
				speciesEditor("OK");
				initialsPanel.refreshInitialAssignmentPanel(gcm);
				rulesPanel.refreshRulesPanel();
			}
		}
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
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}
}
