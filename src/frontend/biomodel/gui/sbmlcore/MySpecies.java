package frontend.biomodel.gui.sbmlcore;

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

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
//SpeciesType not supported in Level 3
//import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.UnitDefinition;

import backend.biomodel.annotation.AnnotationUtility;
import backend.biomodel.parser.BioModel;
import backend.biomodel.util.SBMLutilities;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.main.Gui;
import frontend.main.util.Utility;

/**
 * This is a class for creating SBML species
 * 
 * @author Chris Myers
 * 
 */
public class MySpecies extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private BioModel bioModel;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private JButton addSpec, addComplex, addPromoter, removeSpec, editSpec; // species buttons

	private JList species; // JList of species

	private JTextField ID, init, Name; // species text fields
	
	private JComboBox dimensionType, dimensionX, dimensionY;
	
//	SpeciesType not supported in Level 3
//	private JComboBox specTypeBox; // species
	
	private JComboBox specBoundary, specConstant, specHasOnly; // species

	private JComboBox specUnits, initLabel, specConv;

	private JComboBox comp; // compartment combo box

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;
	
	private Parameters parametersPanel;
	
	private Reactions reactionsPanel;
	
	private ModelEditor modelEditor;

	public MySpecies(BioModel bioModel, Boolean paramsOnly, ArrayList<String> getParams, String file, ArrayList<String> parameterChanges, 
			Boolean editOnly, ModelEditor modelEditor) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
		this.modelEditor = modelEditor;
		JPanel addSpecs = new JPanel();
		addSpec = new JButton("Add Species");
		addComplex = new JButton("Add Complex");
		addPromoter = new JButton("Add Promoter");
		removeSpec = new JButton("Remove Species/Promoter");
		editSpec = new JButton("Edit Species/Promoter");
		addSpecs.add(addSpec);
		addSpecs.add(addComplex);
		addSpecs.add(addPromoter);
		addSpecs.add(removeSpec);
		addSpecs.add(editSpec);
		addSpec.addActionListener(this);
		addComplex.addActionListener(this);
		addPromoter.addActionListener(this);
		removeSpec.addActionListener(this);
		editSpec.addActionListener(this);
		if (paramsOnly || editOnly) {
			addSpec.setEnabled(false);
			addComplex.setEnabled(false);
			addPromoter.setEnabled(false);
			removeSpec.setEnabled(false);
		}
		JLabel speciesLabel = new JLabel("List of Species:");
		species = new JList();
		species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll1 = new JScrollPane();
		scroll1.setViewportView(species);
		Model model = bioModel.getSBMLDocument().getModel();
		String[] specs = new String[model.getSpeciesCount()];
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			Species species = model.getSpecies(i);
			specs[i] = species.getId(); 
			specs[i] += SBMLutilities.getDimensionString(species);
			if (species.isSetInitialAmount()) {
				specs[i] += " " + species.getInitialAmount();
			}
			else {
				specs[i] += " " + species.getInitialConcentration();
			}
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
		this.bioModel = gcm;
		Model model = gcm.getSBMLDocument().getModel();
		String[] specs = new String[model.getSpeciesCount()];
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			Species species = model.getSpecies(i);
			specs[i] = species.getId() + SBMLutilities.getDimensionString(species);
			if (species.isSetInitialAmount()) {
				specs[i] += " " + species.getInitialAmount();
			}
			else {
				specs[i] += " " + species.getInitialConcentration();
			}
			if (paramsOnly) {
				for (int j = 0; j < parameterChanges.size(); j++) {
					String[] splits = parameterChanges.get(j).split(" ");
					if (splits[0].equals(specs[i].split("\\[| ")[0])) {
						parameterChanges.set(j,	specs[i] + " " + splits[splits.length-2] + " " + 
								splits[splits.length-1]);
						specs[i] = parameterChanges.get(j);
					}
				}
			}
		}
		Utility.sort(specs);
		int selected = 0;
		for (int i = 0; i < specs.length; i++) {
			if (specs[i].split("\\[| ")[0].equals(selectedSpecies)) {
				selected = i;
			}
		}
		species.setListData(specs);
		species.setSelectedIndex(selected);
	}

	/**
	 * Creates a frame used to edit species or create new ones.
	 */
	/**
	 * @param option
	 */
	@SuppressWarnings("unused")
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
		specUnits = createUnitsChoices(bioModel);
		if (bioModel.getSBMLDocument().getLevel() > 2) {
			specConv = createConversionFactorChoices(bioModel);
		}
		String[] optionsTF = { "true", "false" };
		specBoundary = new JComboBox(optionsTF);
		specBoundary.setSelectedItem("false");
		specConstant = new JComboBox(optionsTF);
		specConstant.setSelectedItem("false");
		specHasOnly = new JComboBox(optionsTF);
		specHasOnly.setSelectedItem("false");
		
		dimensionType = new JComboBox();
		dimensionType.addItem("Scalar");
		dimensionType.addItem("Vector");
		dimensionType.addItem("Matrix");
		dimensionType.addActionListener(this);
		dimensionX = new JComboBox();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
			Parameter param = bioModel.getSBMLDocument().getModel().getParameter(i);
			if (param.getConstant() && !BioModel.IsDefaultParameter(param.getId())) {
				dimensionX.addItem(param.getId());
			}
		}
		dimensionY = new JComboBox();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
			Parameter param = bioModel.getSBMLDocument().getModel().getParameter(i);
			if (param.getConstant() && !BioModel.IsDefaultParameter(param.getId())) {
				dimensionY.addItem(param.getId());
			}
		}
		dimensionX.setEnabled(false);
		dimensionY.setEnabled(false);
		
		comp = createCompartmentChoices(bioModel);
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
					init.setText("(" + startVal + "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ")");
				}
			}
		});
		if (paramsOnly) {
			ID.setEditable(false);
			Name.setEditable(false);
//			SpeciesType not supported in Level 3
//			specTypeBox.setEnabled(false);
			specBoundary.setEnabled(false);
			specConstant.setEnabled(false);
			specHasOnly.setEnabled(false);
			comp.setEnabled(false);
			init.setEnabled(false);
			initLabel.setEnabled(false);
			specUnits.setEnabled(false);
			specConv.setEnabled(false);
			sweep.setEnabled(false);
			dimensionType.setEnabled(false);
		}
		if (option.equals("OK")) {
			try {
				Species specie = bioModel.getSBMLDocument().getModel().getSpecies(((String) species.getSelectedValue()).split("\\[| ")[0]);
				ID.setText(specie.getId());
				selectedID = specie.getId();
				Name.setText(specie.getName());
//				SpeciesType not supported in Level 3
//				specTypeBox.setSelectedItem(specie.getSpeciesType());
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
				if (bioModel.getSBMLDocument().getModel().getInitialAssignment(specie.getId()) != null) {
					origAssign = bioModel.removeBooleans(bioModel.getSBMLDocument().getModel().getInitialAssignment(specie.getId()).getMath());
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
					init.setText(((String) species.getSelectedValue()).split("\\[| ")[((String) species.getSelectedValue()).split("\\[| ").length - 1]);
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
				@Override
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
						SBMLDocument d = SBMLutilities.readSBML(file);
						if (d.getModel().getSpecies(((String) species.getSelectedValue()).split("\\[| ")[0]).isSetInitialAmount()) {
							init.setText(d.getModel().getSpecies(((String) species.getSelectedValue()).split("\\[| ")[0]).getInitialAmount() + "");
							initLabel.setSelectedItem("Initial Amount");
						}
						else {
							init.setText(d.getModel().getSpecies(((String) species.getSelectedValue()).split("\\[| ")[0]).getInitialConcentration() + "");
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
		if (bioModel.getSBMLDocument().getLevel() > 2) {
			speciesPanel.add(convLabel);
			speciesPanel.add(specConv);
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, speciesPanel, "Species Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(bioModel.getSBMLDocument(), ID.getText().trim(), selectedID, false);
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
						InitialAssignments.removeInitialAssignment(bioModel, selectedID);
						String [] dimID = new String[1];
						dimID[0] = ID.getText().trim();
						error = InitialAssignments.addInitialAssignment(bioModel, init.getText().trim(), dimID);
						initial = 0.0;
					}
					else {
						if (!selectedID.equals("")) {
							InitialAssignments.removeInitialAssignment(bioModel, selectedID);
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
				convFactor = (String) specConv.getSelectedItem();
				String addSpec = "";
//				SpeciesType not supported in Level 3
//				String selSpecType = (String) specTypeBox.getSelectedItem();
				if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
					String[] specs = new String[species.getModel().getSize()];
					for (int i = 0; i < species.getModel().getSize(); i++) {
						specs[i] = species.getModel().getElementAt(i).toString();
					}
					int index = species.getSelectedIndex();
					String[] splits = specs[index].split("\\[| ");
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
					Compartment compartment = bioModel.getSBMLDocument().getModel().getCompartment((String) comp.getSelectedItem());
					if (initLabel.getSelectedItem().equals("Initial Concentration") && bioModel.getSBMLDocument().getLevel() < 3
							&& compartment.getSpatialDimensions() == 0) {
						JOptionPane.showMessageDialog(Gui.frame, "Species in a 0 dimensional compartment cannot have an initial concentration.",
								"Concentration Not Possible", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error && option.equals("OK") && specConstant.getSelectedItem().equals("true")) {
					String val = ((String) species.getSelectedValue()).split("\\[| ")[0];
					error = SBMLutilities.checkConstant(bioModel.getSBMLDocument(), "Species", val);
				}
				if (!error && option.equals("OK") && specBoundary.getSelectedItem().equals("false")) {
					String val = ((String) species.getSelectedValue()).split("\\[| ")[0];
					error = checkBoundary(val, specConstant.getSelectedItem().equals("false"));
				}
				if (!error) {
					if (option.equals("OK")) {
						String[] specs = new String[species.getModel().getSize()];
						for (int i = 0; i < species.getModel().getSize(); i++) {
							specs[i] = species.getModel().getElementAt(i).toString();
						}
						int index1 = species.getSelectedIndex();
						String val = ((String) species.getSelectedValue()).split("\\[| ")[0];
						species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						specs = Utility.getList(specs, species);
						species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						Species specie = bioModel.getSBMLDocument().getModel().getSpecies(val);
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
						if (convFactor.equals("( none )")) {
							specie.unsetConversionFactor();
						}
						else {
							specie.setConversionFactor(convFactor);
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
							SBMLutilities.updateVarId(bioModel.getSBMLDocument(), true, speciesName, specie.getId());
						}
					}
					else {
						String[] specs = new String[species.getModel().getSize()];
						for (int i = 0; i < species.getModel().getSize(); i++) {
							specs[i] = species.getModel().getElementAt(i).toString();
						}
						int index1 = species.getSelectedIndex();
						Species specie = bioModel.getSBMLDocument().getModel().createSpecies();
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
						if (convFactor.equals("( none )")) {
							specie.unsetConversionFactor();
						}
						else {
							specie.setConversionFactor(convFactor);
						}
						JList addIt = new JList();
						Object[] adding = { addSpec };
						addIt.setListData(adding);
						addIt.setSelectedIndex(0);
						species.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(specs, species, addIt);
						specs = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							specs[i] = (String) adding[i];
						}
						Utility.sort(specs);
						species.setListData(specs);
						species.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (bioModel.getSBMLDocument().getModel().getSpeciesCount() == 1) {
							species.setSelectedIndex(0);
						}
						else {
							species.setSelectedIndex(index1);
						}
					}
					modelEditor.setDirty(true);
					bioModel.makeUndoPoint();
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, speciesPanel, "Species Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			if (!origAssign.equals("")) {
				InitialAssignments.removeInitialAssignment(bioModel, selectedID);
				String [] dimID = new String[1];
				dimID[0] = selectedID;
				error = InitialAssignments.addInitialAssignment(bioModel, origAssign, dimID);
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
			
		Species selectedSpecies = bioModel.getSBMLDocument().getModel().getSpecies(((String)species.getSelectedValue()).split("\\[| ")[0]);

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

		if (bioModel.getSBMLDocument().getModel().getInitialAssignment(selectedSpecies.getId()) != null) {

			init.setText(bioModel.removeBooleans(
					bioModel.getSBMLDocument().getModel().getInitialAssignment(selectedSpecies.getId()).getMath()));
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

		Reaction diffusion = bioModel.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Above");
		if (diffusion!=null) {
			kecdiff.setText(String.valueOf(diffusion.getKineticLaw().getLocalParameter("kecdiff").getValue()));
		}
		Reaction degradation = bioModel.getDegradationReaction(selectedSpecies.getId());
		if (degradation!=null) {
			kecd.setText(String.valueOf(degradation.getKineticLaw().getLocalParameter("kecd").getValue()));			
		}
		
		//show the frame
		Object[] options = { "OK", "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, speciesPanel, "Species Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		
		if (value == JOptionPane.YES_OPTION) {
			
			selectedSpecies = bioModel.getSBMLDocument().getModel().getSpecies(((String)species.getSelectedValue()).split("\\[| ")[0]);
			
			//set the species settings in the sbml model
			selectedSpecies.setBoundaryCondition(Boolean.valueOf((String) specBoundary.getModel().getSelectedItem()));
			selectedSpecies.setHasOnlySubstanceUnits(Boolean.valueOf((String) specHasOnly.getModel().getSelectedItem()));
			selectedSpecies.setConstant(Boolean.valueOf((String) specConstant.getModel().getSelectedItem()));
			
			//initial amount
			if (initLabel.getSelectedIndex() == 0) {
				
				double amount = Double.parseDouble(init.getText());				
				selectedSpecies.setInitialAmount(amount);
			}
			//initial concentration
			else if (initLabel.getSelectedIndex() == 1) {
				
				double conc = Double.parseDouble(init.getText());
				selectedSpecies.setInitialConcentration(conc);
			}
			//initial assignment
			else {
				
				Model model = bioModel.getSBMLDocument().getModel();
				String formula = init.getText();
				InitialAssignment initialAssignment = new InitialAssignment(model.getLevel(), model.getVersion());
				initialAssignment.setVariable(selectedSpecies.getId());
				initialAssignment.setMath(bioModel.addBooleans(formula));				
				
				model.addInitialAssignment(initialAssignment);
			}
			
			//set the local parameters in the diffusion/degredation reactions
			double kecdiffRate = Double.parseDouble(kecdiff.getText());
			double kecdRate = Double.parseDouble(kecd.getText());
			
			if (degradation!=null) {
				degradation.getKineticLaw().getLocalParameter("kecd").setValue(kecdRate);		
			}
			if (diffusion!=null) {
				AnnotationUtility.parseArraySizeAnnotation(bioModel.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Above").getKineticLaw().getLocalParameter("i"));
				bioModel.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Above")
					.getKineticLaw().getLocalParameter("kecdiff").setValue(kecdiffRate);			
				bioModel.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Below")
					.getKineticLaw().getLocalParameter("kecdiff").setValue(kecdiffRate);
				bioModel.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Left")
					.getKineticLaw().getLocalParameter("kecdiff").setValue(kecdiffRate);
				bioModel.getSBMLDocument().getModel().getReaction("Diffusion_" + selectedSpecies.getId() + "_Right")
					.getKineticLaw().getLocalParameter("kecdiff").setValue(kecdiffRate);	
			}
		}
	}
	
	public static JComboBox createCompartmentChoices(BioModel bioModel) {
		String[] add = new String[bioModel.getSBMLDocument().getModel().getCompartmentCount()];
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getCompartmentCount(); i++) {
			add[i] = bioModel.getSBMLDocument().getModel().getCompartment(i).getId();
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

	public static JComboBox createUnitsChoices(BioModel bioModel) {
		JComboBox specUnits = new JComboBox();
		specUnits.addItem("( none )");
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getUnitDefinitionCount(); i++) {
			UnitDefinition unit = bioModel.getSBMLDocument().getModel().getUnitDefinition(i);
			if ((unit.getUnitCount() == 1)
					&& (unit.getUnit(0).isMole() || unit.getUnit(0).isItem() || unit.getUnit(0).isGram() || unit.getUnit(0).isKilogram())
					&& (unit.getUnit(0).getExponent() == 1)) {
				if (!(bioModel.getSBMLDocument().getLevel() < 3 && unit.getId().equals("substance"))) {
					specUnits.addItem(unit.getId());
				}
			}
		}
		if (bioModel.getSBMLDocument().getLevel() < 3) {
			specUnits.addItem("substance");
		}
		String[] unitIds = { "dimensionless", "gram", "item", "kilogram", "mole" };
		for (int i = 0; i < unitIds.length; i++) {
			specUnits.addItem(unitIds[i]);
		}
		return specUnits;
	}

	public static JComboBox createConversionFactorChoices(BioModel bioModel) {
		JComboBox specConv;
		specConv = new JComboBox();
		specConv.addItem("( none )");
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
			Parameter param = bioModel.getSBMLDocument().getModel().getParameter(i);
			if (param.getConstant() && !BioModel.IsDefaultParameter(param.getId())) {
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
		Model model = bioModel.getSBMLDocument().getModel();
		boolean inRule = false;
		if (checkRule) {
			if (model.getRuleCount() > 0) {
				for (int i = 0; i < model.getRuleCount(); i++) {
					Rule rule = model.getListOfRules().get(i);
					if (SBMLutilities.isSetVariable(rule) && val.equals(SBMLutilities.getVariable(rule))) {
						inRule = true;
						break;
					}
				}
			}
			if (!inRule)
				return false;
		}
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getProductCount(); j++) {
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
			for (int j = 0; j < reaction.getReactantCount(); j++) {
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
			String id = ((String) species.getSelectedValue()).split("\\[| ")[0];
			if (BioModel.isPromoterSpecies(bioModel.getSBMLDocument().getModel().getSpecies(id))) {
				bioModel.removePromoter(id);
				modelEditor.setDirty(true);
				bioModel.makeUndoPoint();
			} else {
				if (!SBMLutilities.variableInUse(bioModel.getSBMLDocument(), id, false, true, true)) {
					bioModel.removeSpecies(id);
					modelEditor.setDirty(true);
					bioModel.makeUndoPoint();
				}
			}
		}
	}

	public void setPanels(Reactions reactionsPanel,InitialAssignments initialsPanel, Rules rulesPanel, Parameters parametersPanel) {
		this.reactionsPanel = reactionsPanel;
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
		this.parametersPanel = parametersPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add species button is clicked
		if (e.getSource() == addSpec) {
			String id;
			if ((id = bioModel.createSpecies(null, -1, -1)) != null) {
				modelEditor.launchSpeciesPanel(id, false);
			}
			//speciesEditor("Add");
			reactionsPanel.refreshReactionPanel(bioModel);
			parametersPanel.refreshParameterPanel(bioModel);
		}
		// if the add complex button is clicked
		else if (e.getSource() == addComplex) {
			String id;
			if ((id = bioModel.createSpecies(null, -1, -1)) != null) {
				bioModel.createComplexReaction(id, null, false);
				modelEditor.launchSpeciesPanel(id, false);
			}
			//speciesEditor("Add");
			reactionsPanel.refreshReactionPanel(bioModel);
			parametersPanel.refreshParameterPanel(bioModel);
		}
		// if the add promoter button is clicked
		else if (e.getSource() == addPromoter) {
			String id;
			if ((id = bioModel.createPromoter(null, 10, 10, true)) != null) {
				modelEditor.launchPromoterPanel(id);
			}
			//speciesEditor("Add");
			reactionsPanel.refreshReactionPanel(bioModel);
			parametersPanel.refreshParameterPanel(bioModel);
		}
		// if the edit species button is clicked
		else if (e.getSource() == editSpec) {
			if (species.getModel().getSize() > 0) {
				//if we're dealing with grid species, use a different species editor
				if (AnnotationUtility.parseGridAnnotation(bioModel.getSBMLDocument().getModel().
								getSpecies(((String)species.getModel().getElementAt(0)).split("\\[| ")[0]))!=null) {
					openGridSpeciesEditor();
				}
				else {
					//speciesEditor("OK");
					if (species.getSelectedIndex() == -1) {
						JOptionPane.showMessageDialog(Gui.frame, "No species selected.", "Must Select A Species", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String id = ((String) species.getSelectedValue()).split("\\[| ")[0];
					if (BioModel.isPromoterSpecies(bioModel.getSBMLDocument().getModel().getSpecies(id))) {
						modelEditor.launchPromoterPanel(id);
					} else {
						modelEditor.launchSpeciesPanel(id, false);
					}
					reactionsPanel.refreshReactionPanel(bioModel);
					initialsPanel.refreshInitialAssignmentPanel(bioModel);
					rulesPanel.refreshRulesPanel();
					parametersPanel.refreshParameterPanel(bioModel);
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

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == species) {
				if (AnnotationUtility.parseGridAnnotation(bioModel.getSBMLDocument().getModel().
								getSpecies(((String)species.getModel().getElementAt(0)).split("\\[| ")[0]))!=null) {
					openGridSpeciesEditor();
				}
				else {
					if (species.getSelectedIndex() == -1) {
						JOptionPane.showMessageDialog(Gui.frame, "No species selected.", "Must Select A Species", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String id = ((String) species.getSelectedValue()).split("\\[| ")[0];
					if (BioModel.isPromoterSpecies(bioModel.getSBMLDocument().getModel().getSpecies(id))) {
						modelEditor.launchPromoterPanel(id);
					} else {
						modelEditor.launchSpeciesPanel(id, false);
					}
					reactionsPanel.refreshReactionPanel(bioModel);
					initialsPanel.refreshInitialAssignmentPanel(bioModel);
					rulesPanel.refreshRulesPanel();
					parametersPanel.refreshParameterPanel(bioModel);
				}
			}
		}
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
