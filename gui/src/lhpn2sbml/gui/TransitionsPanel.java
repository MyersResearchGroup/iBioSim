package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
import gcm2sbml.gui.Runnable;
import biomodelsim.BioSim;
import biomodelsim.Log;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;
import lhpn2sbml.parser.ExprTree;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
//import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.swing.DefaultListModel;
//import javax.swing.JComboBox;
//import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;;

//import javax.swing.Icon;

public class TransitionsPanel extends JPanel implements ActionListener {

	private String selected = "";

	// private TransitionsPanel frame;

	private PropertyList transitionsList, controlList, boolAssignments, varAssignments,
			intAssignments, rateAssignments, assignments;

	private JPanel fieldPanel;

	private String[] options = { "Ok", "Cancel" };
	
	private String[] allVariables;
	
	private JCheckBox fail;
	
	private JRadioButton rateButton, delayButton;

	// private Object[] types = { "Boolean", "Continuous", "Integer", "Rate" };

	private LHPNFile lhpn;

	// private Log log;

	private HashMap<String, PropertyField> fields = null;
	
	private BioSim biosim;

	//private ExprTree delayTree, rateTree, enablingTree;

	public TransitionsPanel(String selected, PropertyList transitionsList,
			PropertyList controlList, LHPNFile lhpn, Log log, BioSim biosim) {
		super(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		this.selected = selected;
		this.transitionsList = transitionsList;
		this.controlList = controlList;
		this.lhpn = lhpn;
		this.biosim = biosim;
		// this.log = log;

		fields = new HashMap<String, PropertyField>();
		fieldPanel = new JPanel(new GridLayout(5, 2));

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.ATACSIDstring);
		// field.setMaximumSize(new Dimension(5, 5));
		// field.setPreferredSize(new Dimension(5, 5));
		fields.put(GlobalConstants.ID, field);
		fieldPanel.add(field);

		// Delay field
		field = new PropertyField("Delay/Transition Rate", "", null, null, Utility.NAMEstring);
		fields.put("delay/rate", field);
		fieldPanel.add(field);

		// fieldPanel.setMaximumSize(new Dimension(50,50));
		// fieldPanel.setPreferredSize(new Dimension(50,50));
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(fieldPanel, constraints);
		
		// Fail Transition check box
		JPanel failPanel = new JPanel(new GridLayout(1, 2));
		//failPanel.setMinimumSize(new Dimension(200, 20));
		JLabel failLabel = new JLabel("Fail Transition");
		//JLabel blankLabel1 = new JLabel("  ");
		JLabel blankLabel2 = new JLabel("                                     ");
		fail = new JCheckBox();
		failPanel.add(failLabel);
		//failPanel.add(blankLabel1);
		failPanel.add(fail);
		failPanel.add(blankLabel2);
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(failPanel,constraints);
		
		//Delay or Transition Rate Selection
		JPanel rateSelectPanel = new JPanel(new GridLayout(1,2));
		delayButton = new JRadioButton("Use Delay Bounds");
		rateButton = new JRadioButton("Use Transition Rate");
		ButtonGroup selectGroup = new ButtonGroup();
		selectGroup.add(delayButton);
		selectGroup.add(rateButton);
		delayButton.setSelected(true);
		rateSelectPanel.add(delayButton);
		rateSelectPanel.add(rateButton);
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(rateSelectPanel,constraints);

		// Assignment panel
		assignments = new PropertyList("Assignment List");
		boolAssignments = new PropertyList("Boolean Assignment List");
		varAssignments = new PropertyList("Continuous Assignment List");
		intAssignments = new PropertyList("Integer Assignment List");
		rateAssignments = new PropertyList("Rate Assignment List");
		EditButton addAssign = new EditButton("Add Assignment", assignments);
		RemoveButton removeAssign = new RemoveButton("Remove Assignment", assignments);
		EditButton editAssign = new EditButton("Edit Assignment", assignments);
		if (selected != null) {
			if (lhpn.getBooleanVars(selected) != null) {
				for (String s : lhpn.getBooleanVars(selected)) {
					if (s != null && lhpn.getBoolAssign(selected, s) != null) {
						boolAssignments.addItem(s + ":="
								+ lhpn.getBoolAssign(selected, s).toString());
						assignments.addItem(s + ":=" + lhpn.getBoolAssign(selected, s).toString());
					}
				}
			}
			if (lhpn.getContAssignVars(selected) != null) {
				for (String s : lhpn.getContAssignVars(selected)) {
					if (s != null) {
						if (!s.equals(null)) {
							varAssignments.addItem(s + ":=" + lhpn.getContAssign(selected, s));
							assignments.addItem(s + ":=" + lhpn.getContAssign(selected, s));
						}
					}
				}
			}
			if (lhpn.getIntVars(selected) != null) {
				for (Object obj : lhpn.getIntVars(selected).keySet()) {
					if (obj != null) {
						String s = (String) obj;
						if (!s.equals(null)) {
							intAssignments.addItem(s + ":=" + lhpn.getIntAssign(selected, s));
							assignments.addItem(s + ":=" + lhpn.getIntAssign(selected, s));
						}
					}
				}
			}
			if (lhpn.getRateVars(selected) != null) {
				for (String s : lhpn.getRateVars(selected)) {
					// log.addText(selected + " " + s);
					if (s != null) {
						if (!s.equals(null) && !lhpn.getRateAssign(selected, s).equals("true")
								&& !lhpn.getRateAssign(selected, s).equals("false")) {
							rateAssignments.addItem(s + "':=" + lhpn.getRateAssign(selected, s));
							assignments.addItem(s + "':=" + lhpn.getRateAssign(selected, s));
						}
					}
				}
			}
		}

		JPanel assignPanel = Utility.createPanel(this, "Assignments", assignments, addAssign,
				removeAssign, editAssign);
		constraints.gridx = 0;
		constraints.gridy = 3;
		add(assignPanel, constraints);

		/*
		 * // Boolean Assignment panel boolAssignments = new
		 * PropertyList("Boolean Assignment List"); EditButton addBoolAssign =
		 * new EditButton("Add Boolean Assignment", boolAssignments);
		 * RemoveButton removeBoolAssign = new RemoveButton("Remove Boolean
		 * Assignment", boolAssignments); EditButton editBoolAssign = new
		 * EditButton("Edit Boolean Assignment", boolAssignments); if (selected !=
		 * null) { if (lhpn.getBooleanVars(selected) != null) { for (String s :
		 * lhpn.getBooleanVars(selected)) { boolAssignments.addItem(s + ":=" +
		 * lhpn.getBoolAssign(selected, s)); } } }
		 * 
		 * JPanel boolAssignPanel = Utility.createPanel(this, "Boolean
		 * Assignments", boolAssignments, addBoolAssign, removeBoolAssign,
		 * editBoolAssign); constraints.gridx = 0; constraints.gridy = 1;
		 * add(boolAssignPanel, constraints); // Variable Assignment panel
		 * varAssignments = new PropertyList("Variable Assignment List");
		 * EditButton addVarAssign = new EditButton("Add Variable Assignment",
		 * varAssignments); RemoveButton removeVarAssign = new
		 * RemoveButton("Remove Variable Assignment", varAssignments);
		 * EditButton editVarAssign = new EditButton("Edit Variable Assignment",
		 * varAssignments); if (selected != null) { if
		 * (lhpn.getContAssignVars(selected) != null) { for (String s :
		 * lhpn.getContAssignVars(selected)) { varAssignments.addItem(s + ":=" +
		 * lhpn.getContAssign(selected, s)); } } }
		 * 
		 * JPanel varAssignPanel = Utility.createPanel(this, "Variable
		 * Assignments", varAssignments, addVarAssign, removeVarAssign,
		 * editVarAssign); constraints.gridx = 0; constraints.gridy = 2;
		 * add(varAssignPanel, constraints); // Rate Assignment panel
		 * rateAssignments = new PropertyList("Rate Assignment List");
		 * EditButton addRateAssign = new EditButton("Add Rate Assignment",
		 * rateAssignments); RemoveButton removeRateAssign = new
		 * RemoveButton("Remove Rate Assignment", rateAssignments); EditButton
		 * editRateAssign = new EditButton("Edit Rate Assignment",
		 * rateAssignments); if (selected != null) { if
		 * (lhpn.getRateVars(selected) != null) { for (String s :
		 * lhpn.getRateVars(selected)) { // log.addText(selected + " " + s);
		 * rateAssignments.addItem(s + ":=" + lhpn.getRateAssign(selected, s)); } } }
		 * 
		 * JPanel rateAssignPanel = Utility.createPanel(this, "Rate
		 * Assignments", rateAssignments, addRateAssign, removeRateAssign,
		 * editRateAssign); constraints.gridx = 0; constraints.gridy = 3;
		 * constraints.fill = GridBagConstraints.BOTH; add(rateAssignPanel,
		 * constraints);
		 */

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			// Properties prop = lhpn.getVariables().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			// log.addText(lhpn.getDelay(selected));
			String delay = lhpn.getDelay(selected);
			if (lhpn.isFail(oldName)) {
				fail.setSelected(true);
			}
			if (delay != null) {
				fields.get("delay/rate").setValue(delay);
			}
			else if (lhpn.getTransitionRate(selected) != null) {
				fields.get("delay/rate").setValue(delay);
				rateButton.setSelected(true);
			}
			//String temp = lhpn.getEnabling(selected);
			fields.get("Enabling Condition").setValue(lhpn.getEnabling(selected));
			fields.get("Transition rate").setValue(lhpn.getTransitionRate(selected));
			// log.addText(selected + lhpn.getEnabling(selected));
			// loadProperties(prop);
		}

		// setType(types[0]);
		boolean display = false;
		allVariables = lhpn.getAllIDs();
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(biosim.frame(), this, "Transition Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null && allVariables != null) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i] != null) {
						if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
							Utility.createErrorMessage("Error", "Transition id already exists.");
							return false;
						}
					}
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
						Utility.createErrorMessage("Error", "Transition id already exists.");
						return false;
					}
				}
			}
			else if (fields.get(GlobalConstants.ID).getValue() == null) {
				Utility.createErrorMessage("Error", "Enter transition ID.");
				return false;
			}
			String id = fields.get(GlobalConstants.ID).getValue();
			if (!save(id)) {
				//Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			
			if (selected != null && !oldName.equals(id)) {
				lhpn.changeTransitionName(oldName, id);
			}
			else if (selected == null) {
				lhpn.addTransition(id);
			}
			transitionsList.removeItem(oldName);
			transitionsList.addItem(id);
			transitionsList.setSelectedValue(id, true);
			for (String s : controlList.getItems()) {
				if (oldName != null) {
					String[] array = s.split("\\s");
					for (String t : array) {
						if (t.equals(oldName)) {
							controlList.removeItem(s);
							s = s.replace(oldName, id);
							controlList.addItem(s);
						}
					}
				}
			}
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public boolean save(String transition) {
		// log.addText("saving...");
		lhpn.removeAllAssign(transition);
		if (boolAssignments.getItems() != null) {
			for (String s : boolAssignments.getItems()) {
				// System.out.println("bool" + s);
				String[] tempArray = s.split(":=");
				if (!lhpn.addBoolAssign(transition, tempArray[0], tempArray[1])) return false;
			}
		}
		if (varAssignments.getItems() != null) {
			for (String s : varAssignments.getItems()) {
				// System.out.println("var " + s);
				String[] tempArray = s.split(":=");
				// System.out.println(transition + " " + tempArray[0] + " " +
				// tempArray[1]);
				lhpn.addContAssign(transition, tempArray[0], tempArray[1]);
				// log.addText("continuous "+ tempArray[0]);
			}
		}
		if (intAssignments.getItems() != null) {
			for (String s : intAssignments.getItems()) {
				// System.out.println("int" + s);
				String[] tempArray = s.split(":=");
				// System.out.println(transition + " " + tempArray[0] + " " +
				// tempArray[1]);
				if (!lhpn.addIntAssign(transition, tempArray[0], tempArray[1])) return false;
				// log.addText("integer " + tempArray[0]);
			}
		}
		if (rateAssignments.getItems() != null) {
			for (String s : rateAssignments.getItems()) {
				// System.out.println("rate " + s);
				String[] tempArray = s.split("':=");
				// System.out.println(tempArray[1]);
				if (!lhpn.addRateAssign(transition, tempArray[0], tempArray[1])) return false;
			}
		}
		String delay;
		if (rateButton.isSelected()) {
			if (!lhpn.changeDelay(transition, fields.get("delay/rate").getValue()))
				return false;
		}
		else if (delayButton.isSelected()) {
			if (!lhpn.changeTransitionRate(transition, fields.get("delay/rate").getValue()))
				return false;
		}
		if (fail.isSelected()) {
			lhpn.addFail(transition);
		}
		else {
			lhpn.removeFail(transition);
		}
		if (!lhpn.changeEnabling(transition, fields.get("Enabling Condition").getValue()))
			return false;
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {

		}
		Object o = e.getSource();
		if (o instanceof Runnable) {
			((Runnable) o).run();
		}
	}

	public class RemoveButton extends AbstractRunnableNamedButton {
		public RemoveButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		public void run() {
			if (list.getSelectedValue() != null) {
				String assignment = list.getSelectedValue().toString();
				if (isBoolean(assignment)) {
					list.removeItem(assignment);
					boolAssignments.removeItem(assignment);
					lhpn.removeBoolAssign(selected, assignment);
				}
				else if (isContinuous(assignment)) {
					list.removeItem(assignment);
					varAssignments.removeItem(assignment);
					lhpn.removeContAssign(selected, assignment);
				}
				else if (isRate(assignment)) {
					list.removeItem(assignment);
					rateAssignments.removeItem(assignment);
					lhpn.removeRateAssign(selected, assignment);
				}
				else if (isInteger(assignment)) {
					list.removeItem(assignment);
					intAssignments.removeItem(assignment);
					lhpn.removeIntAssign(selected, assignment);
				}
			}
		}

		private PropertyList list = null;
	}

	public class EditButton extends AbstractRunnableNamedButton {
		public EditButton(String name, PropertyList list) {
			super(name);
			this.list = list;
		}

		public void run() {
			new EditCommand(getName(), list).run();
		}

		private PropertyList list = null;
	}

	private class EditCommand implements Runnable {
		public EditCommand(String name, PropertyList list) {
			this.name = name;
			this.list = list;
		}

		public void run() {
			if (name == null || name.equals("")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			if (list.getSelectedValue() == null && getName().contains("Edit")) {
				Utility.createErrorMessage("Error", "Nothing selected to edit");
				return;
			}
			// String type = "", assignment = "";
			String variable = null;
			if (list.getSelectedValue() != null && getName().contains("Edit")) {
				variable = list.getSelectedValue().toString();
			}
			if ((lhpn.getContVars().length == 0) && (lhpn.getBooleanVars().length == 0)
					&& (lhpn.getIntVars().length == 0)) {
				Utility.createErrorMessage("Error", "Add variables first");
			}
			else if (getName().contains("Add")
					&& (list.getItems().length == lhpn.getContVars().length
							+ lhpn.getBooleanVars().length + lhpn.getIntVars().length)) {
				Utility.createErrorMessage("Error",
						"All variables have already been assigned in this transition");
			}
			else {
				// System.out.println("transition " + selected);
				AssignmentPanel panel = new AssignmentPanel(selected, variable, list,
						varAssignments, rateAssignments, boolAssignments, intAssignments, lhpn, biosim);
			}
			/*
			 * if (list.getSelectedValue() == null) { type = (String)
			 * JOptionPane.showInputDialog(fieldPanel, "Which type of variable
			 * assignment do you want to add?", "Assignment Type",
			 * JOptionPane.PLAIN_MESSAGE, null, types, types[0]); } else {
			 * assignment = list.getSelectedValue().toString(); } if
			 * (getName().contains("Boolean") || type.equals("Boolean") ||
			 * isBoolean(assignment)) { String variable = null; if
			 * (list.getSelectedValue() != null && getName().contains("Edit")) {
			 * variable = list.getSelectedValue().toString(); } if
			 * (lhpn.getBooleanVars().length == 0) {
			 * Utility.createErrorMessage("Error", "Add boolean variables
			 * first"); } else { BoolAssignPanel panel = new
			 * BoolAssignPanel(selected, variable, list, boolAssignments, lhpn,
			 * log); } } else if (getName().contains("Variable") ||
			 * type.equals("Continuous") || isContinuous(assignment)) { String
			 * variable = null; if (list.getSelectedValue() != null &&
			 * getName().contains("Edit")) { variable =
			 * list.getSelectedValue().toString(); } if
			 * (lhpn.getContVars().length == 0) {
			 * Utility.createErrorMessage("Error", "Add continuous variables
			 * first"); } else { // System.out.println("transition " +
			 * selected); VarAssignPanel panel = new VarAssignPanel(selected,
			 * variable, list, varAssignments, lhpn); } } else if
			 * (getName().contains("Integer") || type.equals("Integer") ||
			 * isInteger(assignment)) { String variable = null; if
			 * (list.getSelectedValue() != null && getName().contains("Edit")) {
			 * variable = list.getSelectedValue().toString(); } if
			 * (lhpn.getIntVars().length == 0) {
			 * Utility.createErrorMessage("Error", "Add integers first"); } else { //
			 * System.out.println("transition " + // lhpn.getIntVars().length);
			 * IntAssignPanel panel = new IntAssignPanel(selected, variable,
			 * list, intAssignments, lhpn); } } else if
			 * (getName().contains("Rate") || type.equals("Rate") ||
			 * isRate(assignment)) { String variable = null; if
			 * (list.getSelectedValue() != null && getName().contains("Edit")) {
			 * variable = list.getSelectedValue().toString(); } if
			 * (lhpn.getContVars().length == 0) {
			 * Utility.createErrorMessage("Error", "Add continuous variables
			 * first"); } else { RateAssignPanel panel = new
			 * RateAssignPanel(selected, variable, list, rateAssignments, lhpn,
			 * log); } }
			 */
		}

		public String getName() {
			return name;
		}

		private String name = null;

		private PropertyList list = null;
	}

	private boolean isBoolean(String assignment) {
		for (String s : boolAssignments.getItems()) {
			if (s.equals(assignment)) {
				return true;
			}
		}
		return false;
	}

	private boolean isContinuous(String assignment) {
		for (String s : varAssignments.getItems()) {
			if (s.equals(assignment)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInteger(String assignment) {
		for (String s : intAssignments.getItems()) {
			if (s.equals(assignment)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRate(String assignment) {
		for (String s : rateAssignments.getItems()) {
			if (s.equals(assignment)) {
				return true;
			}
		}
		return false;
	}

}
