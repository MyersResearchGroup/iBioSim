package sbmleditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentType;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.UnitDefinition;

import util.MutableBoolean;
import util.Utility;

/**
 * This is a class for creating SBML comparments
 * 
 * @author Chris Myers
 * 
 */
public class Compartments extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addCompart, removeCompart, editCompart;

	private JList compartments; // JList of compartments

	private JTextField compID, compSize, compName; // compartment fields;

	private JComboBox compUnits, compOutside, compConstant; // compartment units

	private JComboBox compTypeBox, dimBox; // compartment type combo box

	private JTextField dimText;

	private boolean editComp = false;

	private SBMLDocument document;

	private ArrayList<String> usedIDs;

	private MutableBoolean dirty;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;
	
	private JComboBox compartmentList;

	public Compartments(SBMLDocument document, ArrayList<String> usedIDs, MutableBoolean dirty, Boolean paramsOnly, ArrayList<String> getParams,
			String file, ArrayList<String> parameterChanges, Boolean editOnly, JComboBox compartmentList) {
		super(new BorderLayout());
		this.document = document;
		this.usedIDs = usedIDs;
		this.dirty = dirty;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
		this.compartmentList = compartmentList;
		Model model = document.getModel();
		addCompart = new JButton("Add Compartment");
		removeCompart = new JButton("Remove Compartment");
		editCompart = new JButton("Edit Compartment");
		if (paramsOnly | editOnly) {
			addCompart.setEnabled(false);
			removeCompart.setEnabled(false);
		}
		compartments = new JList();
		ListOf listOfCompartments = model.getListOfCompartments();
		String[] comps = new String[(int) model.getNumCompartments()];
		for (int i = 0; i < model.getNumCompartments(); i++) {
			Compartment compartment = (Compartment) listOfCompartments.get(i);
			/*
			 * if (compartment.isSetCompartmentType()) { comps[i] =
			 * compartment.getId() + " " + compartment.getCompartmentType(); }
			 * else {
			 */
			comps[i] = compartment.getId();
			// }
			if (compartment.isSetSize()) {
				comps[i] += " " + compartment.getSize();
			}
			/*
			 * if (compartment.isSetUnits()) { comps[i] += " " +
			 * compartment.getUnits(); }
			 */
			if (paramsOnly) {
				for (int j = 0; j < getParams.size(); j++) {
					if (getParams.get(j).split(" ")[0].equals(compartment.getId())) {
						parameterChanges.add(getParams.get(j));
						String[] splits = getParams.get(j).split(" ");
						if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
							String value = splits[splits.length - 1];
							compartment.setSize(Double.parseDouble(value));
							comps[i] += " Modified " + splits[splits.length - 1];
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
		JPanel addRem = new JPanel();
		addRem.add(addCompart);
		addRem.add(removeCompart);
		addRem.add(editCompart);
		addCompart.addActionListener(this);
		removeCompart.addActionListener(this);
		editCompart.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Compartments:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(compartments);
		Utility.sort(comps);
		compartments.setListData(comps);
		compartments.setSelectedIndex(0);
		compartments.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Creates a frame used to edit compartments or create new ones.
	 */
	public void compartEditor(String option) {
		if (option.equals("OK") && compartments.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No compartment selected.", "Must Select a Compartment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel compartPanel = new JPanel();
		JPanel compPanel;
		if (paramsOnly) {
			if (document.getLevel() < 3) {
				compPanel = new JPanel(new GridLayout(10, 2));
			}
			else {
				compPanel = new JPanel(new GridLayout(8, 2));
			}
		}
		else {
			if (document.getLevel() < 3) {
				compPanel = new JPanel(new GridLayout(8, 2));
			}
			else {
				compPanel = new JPanel(new GridLayout(6, 2));
			}
		}
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel compTypeLabel = new JLabel("Type:");
		JLabel dimLabel = new JLabel("Dimensions:");
		JLabel outsideLabel = new JLabel("Outside:");
		JLabel constLabel = new JLabel("Constant:");
		JLabel sizeLabel = new JLabel("Size:");
		JLabel compUnitsLabel = new JLabel("Units:");
		compID = new JTextField(12);
		compName = new JTextField(12);
		ListOf listOfCompTypes = document.getModel().getListOfCompartmentTypes();
		String[] compTypeList = new String[(int) document.getModel().getNumCompartmentTypes() + 1];
		compTypeList[0] = "( none )";
		for (int i = 0; i < document.getModel().getNumCompartmentTypes(); i++) {
			compTypeList[i + 1] = ((CompartmentType) listOfCompTypes.get(i)).getId();
		}
		Utility.sort(compTypeList);
		Object[] choices = compTypeList;
		compTypeBox = new JComboBox(choices);
		Object[] dims = { "0", "1", "2", "3" };
		dimBox = new JComboBox(dims);
		dimBox.setSelectedItem("3");
		dimText = new JTextField(12);
		dimText.setText("3.0");
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
					setCompartOptions(((String) compartments.getSelectedValue()).split(" ")[0], (String) dimBox.getSelectedItem());
				}
				else {
					setCompartOptions("", (String) dimBox.getSelectedItem());
				}
			}
		});
		dimText.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (editComp) {
					setCompartOptions(((String) compartments.getSelectedValue()).split(" ")[0], dimText.getText());
				}
				else {
					setCompartOptions("", (String) dimText.getText());
				}
			}
		});
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
					compSize.setText("(" + startVal + "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ")");
				}
			}
		});
		if (paramsOnly) {
			compID.setEnabled(false);
			compName.setEnabled(false);
			compTypeBox.setEnabled(false);
			dimBox.setEnabled(false);
			dimText.setEnabled(false);
			compUnits.setEnabled(false);
			compOutside.setEnabled(false);
			compConstant.setEnabled(false);
			compSize.setEnabled(false);
			sweep.setEnabled(false);
		}
		if (option.equals("OK")) {
			try {
				Compartment compartment = document.getModel().getCompartment((((String) compartments.getSelectedValue()).split(" ")[0]));
				compID.setText(compartment.getId());
				selectedID = compartment.getId();
				compName.setText(compartment.getName());
				if (compartment.isSetCompartmentType()) {
					compTypeBox.setSelectedItem(compartment.getCompartmentType());
				}
				dimBox.setSelectedItem(String.valueOf(compartment.getSpatialDimensions()));
				dimText.setText(String.valueOf(compartment.getSpatialDimensionsAsDouble()));
				setCompartOptions(((String) compartments.getSelectedValue()).split(" ")[0],
						String.valueOf(compartment.getSpatialDimensionsAsDouble()));
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
						&& (((String) compartments.getSelectedValue()).contains("Modified")
								|| ((String) compartments.getSelectedValue()).contains("Custom") || ((String) compartments.getSelectedValue())
								.contains("Sweep"))) {
					type.setSelectedItem("Modified");
					sweep.setEnabled(true);
					compSize.setText(((String) compartments.getSelectedValue()).split(" ")[((String) compartments.getSelectedValue()).split(" ").length - 1]);
					compSize.setEnabled(true);
					if (compSize.getText().trim().startsWith("(")) {
						try {
							start.setText((compSize.getText().trim()).split(",")[0].substring(1).trim());
							stop.setText((compSize.getText().trim()).split(",")[1].trim());
							step.setText((compSize.getText().trim()).split(",")[2].trim());
							int lev = Integer.parseInt((compSize.getText().trim()).split(",")[3].replace(")", "").trim());
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
		compPanel.add(idLabel);
		compPanel.add(compID);
		compPanel.add(nameLabel);
		compPanel.add(compName);
		if (document.getLevel() < 3) {
			compPanel.add(compTypeLabel);
			compPanel.add(compTypeBox);
		}
		compPanel.add(dimLabel);
		if (document.getLevel() < 3) {
			compPanel.add(dimBox);
		}
		else {
			compPanel.add(dimText);
		}
		if (document.getLevel() < 3) {
			compPanel.add(outsideLabel);
			compPanel.add(compOutside);
		}
		compPanel.add(constLabel);
		compPanel.add(compConstant);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Value Type:");
			type.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						sweep.setEnabled(true);
						compSize.setEnabled(true);
					}
					else {
						sweep.setEnabled(false);
						compSize.setEnabled(false);
						SBMLDocument d = Gui.readSBML(file);
						if (d.getModel().getCompartment(((String) compartments.getSelectedValue()).split(" ")[0]).isSetSize()) {
							compSize.setText(d.getModel().getCompartment(((String) compartments.getSelectedValue()).split(" ")[0]).getSize() + "");
						}
					}
				}
			});
			compPanel.add(typeLabel);
			compPanel.add(type);
		}
		compPanel.add(sizeLabel);
		compPanel.add(compSize);
		if (paramsOnly) {
			compPanel.add(new JLabel());
			compPanel.add(sweep);
		}
		compPanel.add(compUnitsLabel);
		compPanel.add(compUnits);
		compartPanel.add(compPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, compartPanel, "Compartment Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(document, usedIDs, compID.getText().trim(), selectedID, false);
			if (!error && option.equals("OK") && compConstant.getSelectedItem().equals("true")) {
				String val = ((String) compartments.getSelectedValue()).split(" ")[0];
				error = SBMLutilities.checkConstant(document, "Compartment", val);
			}
			if (!error && document.getLevel() < 3 && option.equals("OK") && dimBox.getSelectedItem().equals("0")) {
				String val = ((String) compartments.getSelectedValue()).split(" ")[0];
				for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
					Species species = document.getModel().getSpecies(i);
					if ((species.getCompartment().equals(val)) && (species.isSetInitialConcentration())) {
						JOptionPane.showMessageDialog(Gui.frame,
								"Compartment with 0-dimensions cannot contain species with an initial concentration.", "Cannot be 0 Dimensions",
								JOptionPane.ERROR_MESSAGE);
						error = true;
						break;
					}
				}
				if (!error) {
					for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
						Compartment compartment = document.getModel().getCompartment(i);
						if (compartment.getOutside().equals(val)) {
							JOptionPane.showMessageDialog(Gui.frame, "Compartment with 0-dimensions cannot be outside another compartment.",
									"Cannot be 0 Dimensions", JOptionPane.ERROR_MESSAGE);
							error = true;
							break;
						}
					}
				}
			}
			Double addCompSize = 1.0;
			if ((!error) && (Integer.parseInt((String) dimBox.getSelectedItem()) != 0)) {
				if (compSize.getText().trim().startsWith("(") && compSize.getText().trim().endsWith(")")) {
					try {
						Double.parseDouble((compSize.getText().trim()).split(",")[0].substring(1).trim());
						Double.parseDouble((compSize.getText().trim()).split(",")[1].trim());
						Double.parseDouble((compSize.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((compSize.getText().trim()).split(",")[3].replace(")", "").trim());
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
						addCompSize = Double.parseDouble(compSize.getText().trim());
					}
					catch (Exception e1) {
						error = true;
						JOptionPane.showMessageDialog(Gui.frame, "The compartment size must be a real number.", "Enter a Valid Size",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			if (!error) {
				if (document.getLevel() < 3) {
					if (!compOutside.getSelectedItem().equals("( none )")) {
						if (checkOutsideCycle(compID.getText().trim(), (String) compOutside.getSelectedItem(), 0)) {
							JOptionPane.showMessageDialog(Gui.frame, "Compartment contains itself through outside references.",
									"Cycle in Outside References", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
				}
			}
			if (!error) {
				if (document.getLevel() < 3) {
					if (((String) dimBox.getSelectedItem()).equals("0") && (SBMLutilities.variableInUse(document, selected, true))) {
						error = true;
					}
				}
			}
			double dim = 3;
			if (!error) {
				try {
					if (document.getLevel() < 3) {
						dim = Integer.parseInt((String) dimBox.getSelectedItem());
					}
					else {
						dim = Double.parseDouble((String) dimText.getText());
					}
				}
				catch (Exception e1) {
					JOptionPane.showMessageDialog(Gui.frame, "Compartment spatial dimensions must be a real number.", "Invalid Spatial Dimensions",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				String addComp = "";
				String selCompType = (String) compTypeBox.getSelectedItem();
				// String unit = (String) compUnits.getSelectedItem();
				if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
					String[] comps = new String[compartments.getModel().getSize()];
					for (int i = 0; i < compartments.getModel().getSize(); i++) {
						comps[i] = compartments.getModel().getElementAt(i).toString();
					}
					int index = compartments.getSelectedIndex();
					String[] splits = comps[index].split(" ");
					if (splits[0].length() == 1) {
						addComp += splits[0] + " ";
					}
					else {
						for (int i = 0; i < splits.length - 2; i++) {
							addComp += splits[i] + " ";
						}
					}
					if (splits.length > 1 && !splits[splits.length - 2].equals("Modified") && !splits[splits.length - 2].equals("Custom")
							&& !splits[splits.length - 2].equals("Sweep")) {
						addComp += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
					}
					if (compSize.getText().trim().startsWith("(") && compSize.getText().trim().endsWith(")")) {
						double startVal = Double.parseDouble((compSize.getText().trim()).split(",")[0].substring(1).trim());
						double stopVal = Double.parseDouble((compSize.getText().trim()).split(",")[1].trim());
						double stepVal = Double.parseDouble((compSize.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((compSize.getText().trim()).split(",")[3].replace(")", "").trim());
						addComp += "Sweep (" + startVal + "," + stopVal + "," + stepVal + "," + lev + ")";
					}
					else {
						addComp += "Modified " + addCompSize;
					}
				}
				else {
					addComp = compID.getText().trim();
					/*
					 * if (!selCompType.equals("( none )")) { addComp += " " +
					 * selCompType; } if (!unit.equals("( none )")) { addComp +=
					 * " " + addCompSize + " " + unit; } else {
					 */
					addComp += " " + addCompSize;
					// }
				}
				if (!error) {
					if (option.equals("OK")) {
						String[] comps = new String[compartments.getModel().getSize()];
						for (int i = 0; i < compartments.getModel().getSize(); i++) {
							comps[i] = compartments.getModel().getElementAt(i).toString();
						}
						int index = compartments.getSelectedIndex();
						String val = ((String) compartments.getSelectedValue()).split(" ")[0];
						compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						comps = Utility.getList(comps, compartments);
						compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						Compartment c = document.getModel().getCompartment(val);
						c.setId(compID.getText().trim());
						compartmentList.removeItem(val);
						compartmentList.addItem(c.getId());
						c.setName(compName.getText().trim());
						if (!selCompType.equals("( none )")) {
							c.setCompartmentType(selCompType);
						}
						else {
							c.unsetCompartmentType();
						}
						if (document.getLevel() < 3) {
							c.setSpatialDimensions(dim);
						}
						else {
							c.setSpatialDimensions(dim);
						}
						if (compSize.getText().trim().equals("") || compSize.getText().trim().startsWith("(")) {
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
						Utility.sort(comps);
						compartments.setListData(comps);
						compartments.setSelectedIndex(index);
						for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
							Species species = document.getModel().getSpecies(i);
							if (species.getCompartment().equals(val)) {
								species.setCompartment(compID.getText().trim());
							}
						}
						for (int i = 0; i < document.getModel().getNumReactions(); i++) {
							Reaction reaction = document.getModel().getReaction(i);
							if (reaction.getCompartment().equals(val)) {
								reaction.setCompartment(compID.getText().trim());
							}
						}
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
							SBMLutilities.updateVarId(document, false, val, compID.getText().trim());
							for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
								Compartment compartment = document.getModel().getCompartment(i);
								if (compartment.getOutside().equals(val)) {
									compartment.setOutside(compID.getText().trim());
								}
							}
						}
					}
					else {
						String[] comps = new String[compartments.getModel().getSize()];
						for (int i = 0; i < compartments.getModel().getSize(); i++) {
							comps[i] = compartments.getModel().getElementAt(i).toString();
						}
						int index = compartments.getSelectedIndex();
						Compartment c = document.getModel().createCompartment();
						c.setId(compID.getText().trim());
						compartmentList.addItem(c.getId());
						c.setName(compName.getText().trim());
						if (!selCompType.equals("( none )")) {
							c.setCompartmentType(selCompType);
						}
						if (document.getLevel() < 3) {
							c.setSpatialDimensions(dim);
						}
						else {
							c.setSpatialDimensions(dim);
						}
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
						/*
						 * if (!selCompType.equals("( none )")) { addStr =
						 * compID.getText().trim() + " " + selCompType + " " +
						 * compSize.getText().trim(); } else {
						 */
						addStr = compID.getText().trim() + " " + compSize.getText().trim();
						/*
						 * } if
						 * (!compUnits.getSelectedItem().equals("( none )")) {
						 * addStr += " " + compUnits.getSelectedItem(); }
						 */
						Object[] adding = { addStr };
						add.setListData(adding);
						add.setSelectedIndex(0);
						compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(comps, compartments, add, false, null, null, null, null, null, null, Gui.frame);
						comps = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							comps[i] = (String) adding[i];
						}
						Utility.sort(comps);
						compartments.setListData(comps);
						compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (document.getModel().getNumCompartments() == 1) {
							compartments.setSelectedIndex(0);
						}
						else {
							compartments.setSelectedIndex(index);
						}
					}
					dirty.setValue(true);
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, compartPanel, "Compartment Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
	 * Set compartment options based on number of dimensions
	 */
	private void setCompartOptions(String selected, String dimStr) {
		double dim = 3;
		try {
			dim = Double.parseDouble(dimStr);
		}
		catch (Exception e1) {
			return;
		}
		if (dim == 3) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isLitre() && unit.getUnit(0).getExponentAsDouble() == 1)
						|| (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 3)) {
					if (!(document.getLevel() < 3 && unit.getId().equals("volume"))) {
						compUnits.addItem(unit.getId());
					}
				}
			}
			if (document.getLevel() < 3) {
				compUnits.addItem("volume");
			}
			compUnits.addItem("litre");
			compUnits.addItem("dimensionless");
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
		else if (dim == 2) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 2)) {
					if (!(document.getLevel() < 3 && unit.getId().equals("area"))) {
						compUnits.addItem(unit.getId());
					}
				}
			}
			if (document.getLevel() < 3) {
				compUnits.addItem("area");
			}
			compUnits.addItem("dimensionless");
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
		else if (dim == 1) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 1)) {
					if (!(document.getLevel() < 3 && unit.getId().equals("length"))) {
						compUnits.addItem(unit.getId());
					}
				}
			}
			if (document.getLevel() < 3) {
				compUnits.addItem("length");
			}
			compUnits.addItem("metre");
			compUnits.addItem("dimensionless");
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
		else if (dim == 0) {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			if (document.getLevel() < 3) {
				compSize.setText("");
				compConstant.setEnabled(false);
				compSize.setEnabled(false);
				compOutside.removeAllItems();
				compOutside.addItem("( none )");
				ListOf listOfComps = document.getModel().getListOfCompartments();
				for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
					Compartment compartment = (Compartment) listOfComps.get(i);
					if (!compartment.getId().equals(selected)) {
						compOutside.addItem(compartment.getId());
					}
				}
			}
		}
		else {
			compUnits.removeAllItems();
			compUnits.addItem("( none )");
			ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
			for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
				if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == dim)) {
					compUnits.addItem(unit.getId());
				}
			}
			compUnits.addItem("dimensionless");
			if (!paramsOnly) {
				compConstant.setEnabled(true);
				compSize.setEnabled(true);
			}
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
	 * Refresh compartment panel
	 */
	public void refreshCompartmentPanel(SBMLDocument document) {
		String selectedCompartment = "";
		if (!compartments.isSelectionEmpty()) {
			selectedCompartment = ((String) compartments.getSelectedValue()).split(" ")[0];
		}
		this.document = document;
		Model model = document.getModel();
		ListOf listOfCompartments = model.getListOfCompartments();
		String[] comparts = new String[(int) model.getNumCompartments()];
		for (int i = 0; i < model.getNumCompartments(); i++) {
			Compartment compartment = (Compartment) listOfCompartments.get(i);
			comparts[i] = compartment.getId();
			comparts[i] += " " + compartment.getSize();
			for (int j = 0; j < parameterChanges.size(); j++) {
				if (parameterChanges.get(j).split(" ")[0].equals(comparts[i].split(" ")[0])) {
					parameterChanges.set(j, comparts[i] + " " + parameterChanges.get(j).split(" ")[2] + " "
							+ parameterChanges.get(j).split(" ")[3]);
					comparts[i] = parameterChanges.get(j);
				}
			}
		}
		Utility.sort(comparts);
		int selected = 0;
		for (int i = 0; i < comparts.length; i++) {
			if (comparts[i].split(" ")[0].equals(selectedCompartment)) {
				selected = i;
			}
		}
		compartments.setListData(comparts);
		compartments.setSelectedIndex(selected);
	}

	/**
	 * Remove a compartment
	 */
	private void removeCompartment() {
		int index = compartments.getSelectedIndex();
		if (index != -1) {
			if (document.getModel().getNumCompartments() != 1) {
				if (((String)compartmentList.getSelectedItem()).equals(((String) compartments.getSelectedValue()).split(" ")[0])) {
					JOptionPane.showMessageDialog(Gui.frame,
							"Enclosing compartment cannot be removed", 
							"Cannot remove enclosing compartment",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				boolean remove = true;
				ArrayList<String> speciesUsing = new ArrayList<String>();
				for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
					Species species = (Species) document.getModel().getListOfSpecies().get(i);
					if (species.isSetCompartment()) {
						if (species.getCompartment().equals(((String) compartments.getSelectedValue()).split(" ")[0])) {
							remove = false;
							speciesUsing.add(species.getId());
						}
					}
				}
				ArrayList<String> outsideUsing = new ArrayList<String>();
				for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
					Compartment compartment = document.getModel().getCompartment(i);
					if (compartment.isSetOutside()) {
						if (compartment.getOutside().equals(((String) compartments.getSelectedValue()).split(" ")[0])) {
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
						Utility.sort(vars);
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
						Utility.sort(vars);
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
					JOptionPane.showMessageDialog(Gui.frame, scroll, "Unable To Remove Compartment", JOptionPane.ERROR_MESSAGE);
				}
				else if (!SBMLutilities.variableInUse(document, ((String) compartments.getSelectedValue()).split(" ")[0], false)) {
					compartmentList.removeItem(((String) compartments.getSelectedValue()).split(" ")[0]);
					Compartment tempComp = document.getModel().getCompartment(((String) compartments.getSelectedValue()).split(" ")[0]);
					ListOf c = document.getModel().getListOfCompartments();
					for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
						if (((Compartment) c.get(i)).getId().equals(tempComp.getId())) {
							c.remove(i);
						}
					}
					usedIDs.remove(((String) compartments.getSelectedValue()).split(" ")[0]);
					compartments.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					Utility.remove(compartments);
					compartments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index < compartments.getModel().getSize()) {
						compartments.setSelectedIndex(index);
					}
					else {
						compartments.setSelectedIndex(index - 1);
					}
					dirty.setValue(true);
				}
			}
			else {
				JOptionPane.showMessageDialog(Gui.frame, "Each model must contain at least one compartment.", "Unable To Remove Compartment",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	public void actionPerformed(ActionEvent e) {
		// if the add compartment type button is clicked
		// if the add species type button is clicked
		// if the add compartment button is clicked
		if (e.getSource() == addCompart) {
			compartEditor("Add");
		}
		// if the edit compartment button is clicked
		else if (e.getSource() == editCompart) {
			compartEditor("OK");
			initialsPanel.refreshInitialAssignmentPanel(document);
			rulesPanel.refreshRulesPanel(document);
		}
		// if the remove compartment button is clicked
		else if (e.getSource() == removeCompart) {
			removeCompartment();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == compartments) {
				compartEditor("OK");
				initialsPanel.refreshInitialAssignmentPanel(document);
				rulesPanel.refreshRulesPanel(document);
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