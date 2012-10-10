package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.MutableBoolean;
import main.util.Utility;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Port;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.UnitDefinition;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;


/**
 * This is a class for creating SBML initial assignments
 * 
 * @author Chris Myers
 * 
 */
public class InitialAssignments extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addInit, removeInit, editInit;

	private JList initAssigns; // JList of initial assignments

	private BioModel gcm;

	private MutableBoolean dirty;

	private Gui biosim;

	/* Create initial assignment panel */
	public InitialAssignments(Gui biosim, BioModel gcm, MutableBoolean dirty) {
		super(new BorderLayout());
		this.gcm = gcm;
		this.biosim = biosim;
		this.dirty = dirty;
		Model model = gcm.getSBMLDocument().getModel();
		/* Create initial assignment panel */
		addInit = new JButton("Add Initial");
		removeInit = new JButton("Remove Initial");
		editInit = new JButton("Edit Initial");
		initAssigns = new JList();
		ListOf listOfInits = model.getListOfInitialAssignments();
		String[] inits = new String[(int) model.getNumInitialAssignments()];
		for (int i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment init = (InitialAssignment) listOfInits.get(i);
			inits[i] = init.getSymbol() + " = " + SBMLutilities.myFormulaToString(init.getMath());
		}
		String[] oldInits = inits;
		try {
			inits = sortInitRules(inits);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in initial assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
			inits = oldInits;
		}
		JPanel addRem = new JPanel();
		addRem.add(addInit);
		addRem.add(removeInit);
		addRem.add(editInit);
		addInit.addActionListener(this);
		removeInit.addActionListener(this);
		editInit.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Initial Assignments:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(initAssigns);
		Utility.sort(inits);
		initAssigns.setListData(inits);
		initAssigns.setSelectedIndex(0);
		initAssigns.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Refresh initial assingment panel
	 */
	public void refreshInitialAssignmentPanel(BioModel gcm) {
		Model model = gcm.getSBMLDocument().getModel();
		if (model.getNumInitialAssignments() > 0) {
			String[] inits = new String[(int) model.getNumInitialAssignments()];
			for (int i = 0; i < model.getNumInitialAssignments(); i++) {
				InitialAssignment init = (InitialAssignment) model.getListOfInitialAssignments().get(i);
				inits[i] = init.getSymbol() + " = " + SBMLutilities.myFormulaToString(init.getMath());
			}
			try {
				inits = sortInitRules(inits);
				if (SBMLutilities.checkCycles(gcm.getSBMLDocument())) {
					JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
							"Cycle Detected", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
			}
			initAssigns.setListData(inits);
			initAssigns.setSelectedIndex(0);
		}
	}

	/**
	 * Remove an initial assignment
	 */
	public static void removeInitialAssignment(BioModel gcm, String variable) {
		ListOf r = gcm.getSBMLDocument().getModel().getListOfInitialAssignments();
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumInitialAssignments(); i++) {
			if (((InitialAssignment) r.get(i)).getSymbol().equals(variable)) {
				for (long j = 0; j < gcm.getSBMLCompModel().getNumPorts(); j++) {
					Port port = gcm.getSBMLCompModel().getPort(j);
					if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(r.get(i).getMetaId())) {
						gcm.getSBMLCompModel().removePort(j);
						break;
					}
				}
				r.remove(i);
			}
		}
	}

	/**
	 * Try to add or edit initial assignments
	 */
	public static boolean addInitialAssignment(Gui biosim, BioModel bioModel, String variable, String assignment) {
		if (assignment.trim().equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Initial assignment is empty.", "Enter Assignment", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (SBMLutilities.myParseFormula(assignment.trim()) == null) {
			JOptionPane.showMessageDialog(Gui.frame, "Initial assignment is not valid.", "Enter Valid Assignment", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		Rule rule = bioModel.getSBMLDocument().getModel().getRule(variable);
		if (rule != null && rule.isAssignment()) {
			JOptionPane.showMessageDialog(Gui.frame, 
					"Cannot have both an assignment rule and an initial assignment on the same variable.", 
					"Multiple Assignment", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(bioModel.getSBMLDocument(), assignment.trim(), "", false);
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
			JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), bioModel.addBooleans(assignment.trim()))) {
			return true;
		}
		if (bioModel.addBooleans(assignment.trim()).isBoolean()) {
			JOptionPane.showMessageDialog(Gui.frame, "Initial assignment must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		boolean error = false;
		InitialAssignment r = bioModel.getSBMLDocument().getModel().createInitialAssignment();
		String initialId = "init__"+variable;
		r.setMetaId(initialId);
		r.setSymbol(variable);
		r.setMath(bioModel.addBooleans(assignment.trim()));
		if (checkInitialAssignmentUnits(biosim, bioModel, r)) {
			error = true;
		}
		if (!error && SBMLutilities.checkCycles(bioModel.getSBMLDocument())) {
			JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.", "Cycle Detected",
					JOptionPane.ERROR_MESSAGE);
			error = true;
		}
		if (error) {
			removeInitialAssignment(bioModel, variable);
		} else {
			if (bioModel.getPortByIdRef(variable)!=null) {
				Port port = bioModel.getSBMLCompModel().createPort();
				port.setId(GlobalConstants.INITIAL_ASSIGNMENT+"__"+variable);
				port.setMetaIdRef(initialId);
			} 
		}
		return error;
	}

	/**
	 * Check the units of an initial assignment
	 */
	public static boolean checkInitialAssignmentUnits(Gui biosim, BioModel bioModel, InitialAssignment init) {
		bioModel.getSBMLDocument().getModel().populateListFormulaUnitsData();
		if (init.containsUndeclaredUnits()) {
			if (biosim.getCheckUndeclared()) {
				JOptionPane.showMessageDialog(Gui.frame, "Initial assignment contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (biosim.getCheckUnits()) {
			if (SBMLutilities.checkUnitsInInitialAssignment(bioModel.getSBMLDocument(), init)) {
				JOptionPane.showMessageDialog(Gui.frame, "Units on the left and right-hand side of the initial assignment do not agree.",
						"Units Do Not Match", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
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
	 * Try to add or edit initial assignments
	 */
	private boolean addUpdateInitialAssignment(String option, int Rindex, String variable, String assignment) {
		if (assignment.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Initial assignment is empty.", "Enter Assignment", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (SBMLutilities.myParseFormula(assignment) == null) {
			JOptionPane.showMessageDialog(Gui.frame, "Initial assignment is not valid.", "Enter Valid Assignment", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(gcm.getSBMLDocument(), assignment, "", false);
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
			JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (SBMLutilities.checkNumFunctionArguments(gcm.getSBMLDocument(), SBMLutilities.myParseFormula(assignment))) {
			return true;
		}
		if (SBMLutilities.myParseFormula(assignment).isBoolean()) {
			JOptionPane.showMessageDialog(Gui.frame, "Initial assignment must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		boolean error = false;
		if (option.equals("OK")) {
			String[] inits = new String[initAssigns.getModel().getSize()];
			for (int i = 0; i < initAssigns.getModel().getSize(); i++) {
				inits[i] = initAssigns.getModel().getElementAt(i).toString();
			}
			int index = initAssigns.getSelectedIndex();
			initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			inits = Utility.getList(inits, initAssigns);
			initAssigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			InitialAssignment r = (InitialAssignment) (gcm.getSBMLDocument().getModel().getListOfInitialAssignments()).get(Rindex);
			String oldSymbol = r.getSymbol();
			String oldInit = SBMLutilities.myFormulaToString(r.getMath());
			String oldVal = inits[index];
			r.setSymbol(variable);
			r.setMath(SBMLutilities.myParseFormula(assignment));
			inits[index] = variable + " = " + SBMLutilities.myFormulaToString(r.getMath());
			if (InitialAssignments.checkInitialAssignmentUnits(biosim, gcm, r)) {
				error = true;
			}
			if (!error) {
				try {
					inits = sortInitRules(inits);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in initial assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				if (SBMLutilities.checkCycles(gcm.getSBMLDocument())) {
					JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
							"Cycle Detected", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (error) {
				r.setSymbol(oldSymbol);
				r.setMath(SBMLutilities.myParseFormula(oldInit));
				inits[index] = oldVal;
			}
			initAssigns.setListData(inits);
			initAssigns.setSelectedIndex(index);
		}
		else {
			String[] inits = new String[initAssigns.getModel().getSize()];
			for (int i = 0; i < initAssigns.getModel().getSize(); i++) {
				inits[i] = initAssigns.getModel().getElementAt(i).toString();
			}
			JList add = new JList();
			int index = initAssigns.getSelectedIndex();
			String addStr;
			InitialAssignment r = gcm.getSBMLDocument().getModel().createInitialAssignment();
			r.setSymbol(variable);
			r.setMath(SBMLutilities.myParseFormula(assignment));
			addStr = variable + " = " + SBMLutilities.myFormulaToString(r.getMath());
			Object[] adding = { addStr };
			add.setListData(adding);
			add.setSelectedIndex(0);
			initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			adding = Utility.add(inits, initAssigns, add, null, null, null, null, null, Gui.frame);
			String[] oldInits = inits;
			inits = new String[adding.length];
			for (int i = 0; i < adding.length; i++) {
				inits[i] = (String) adding[i];
			}
			if (InitialAssignments.checkInitialAssignmentUnits(biosim, gcm, r)) {
				error = true;
			}
			if (!error) {
				try {
					inits = sortInitRules(inits);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in initial assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error && SBMLutilities.checkCycles(gcm.getSBMLDocument())) {
				JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
						"Cycle Detected", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			if (error) {
				inits = oldInits;
				ListOf ia = gcm.getSBMLDocument().getModel().getListOfInitialAssignments();
				for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumInitialAssignments(); i++) {
					if (SBMLutilities.myFormulaToString(((InitialAssignment) ia.get(i)).getMath()).equals(
							SBMLutilities.myFormulaToString(r.getMath()))
							&& ((InitialAssignment) ia.get(i)).getSymbol().equals(r.getSymbol())) {
						ia.remove(i);
					}
				}
			}
			initAssigns.setListData(inits);
			initAssigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (gcm.getSBMLDocument().getModel().getNumInitialAssignments() == 1) {
				initAssigns.setSelectedIndex(0);
			}
			else {
				initAssigns.setSelectedIndex(index);
			}
		}
		if (!error) {
			dirty.setValue(true);
			gcm.makeUndoPoint();
		}
		return error;
	}

	/**
	 * Creates a frame used to edit initial assignments or create new ones.
	 */
	private void initEditor(String option) {
		if (option.equals("OK") && initAssigns.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No initial assignment selected.", "Must Select an Initial Assignment",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel initAssignPanel = new JPanel();
		JPanel initPanel = new JPanel();
		JLabel varLabel = new JLabel("Symbol:");
		JLabel assignLabel = new JLabel("Assignment:");
		JComboBox initVar = new JComboBox();
		String selected;
		if (option.equals("OK")) {
			selected = ((String) initAssigns.getSelectedValue());
		}
		else {
			selected = new String("");
		}
		Model model = gcm.getSBMLDocument().getModel();
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			String id = ((Compartment) ids.get(i)).getId();
			if (keepVarInit(gcm, selected.split(" ")[0], id)
					&& (gcm.getSBMLDocument().getLevel() > 2 || ((Compartment) ids.get(i)).getSpatialDimensions() != 0)) {
				initVar.addItem(id);
			}
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < model.getNumParameters(); i++) {
			String id = ((Parameter) ids.get(i)).getId();
			if (keepVarInit(gcm, selected.split(" ")[0], id)) {
				initVar.addItem(id);
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			String id = ((Species) ids.get(i)).getId();
			if (keepVarInit(gcm, selected.split(" ")[0], id)) {
				initVar.addItem(id);
			}
		}
		ids = model.getListOfReactions();
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) ids.get(i);
			ListOf ids2 = reaction.getListOfReactants();
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				SpeciesReference reactant = (SpeciesReference) ids2.get(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals(""))) {
					String id = reactant.getId();
					if (keepVarInit(gcm, selected.split(" ")[0], id)) {
						initVar.addItem(id);
					}
				}
			}
			ids2 = reaction.getListOfProducts();
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				SpeciesReference product = (SpeciesReference) ids2.get(j);
				if ((product.isSetId()) && (!product.getId().equals(""))) {
					String id = product.getId();
					if (keepVarInit(gcm, selected.split(" ")[0], id)) {
						initVar.addItem(id);
					}
				}
			}
		}
		JTextField initMath = new JTextField(30);
		int Rindex = -1;
		if (option.equals("OK")) {
			initVar.setSelectedItem(selected.split(" ")[0]);
			initMath.setText(selected.substring(selected.indexOf('=') + 2));
			ListOf r = gcm.getSBMLDocument().getModel().getListOfInitialAssignments();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumInitialAssignments(); i++) {
				if (SBMLutilities.myFormulaToString(((InitialAssignment) r.get(i)).getMath()).equals(initMath.getText())
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
		int value = JOptionPane.showOptionDialog(Gui.frame, initAssignPanel, "Initial Assignment Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = addUpdateInitialAssignment(option, Rindex, (String) initVar.getSelectedItem(), initMath.getText().trim());
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, initAssignPanel, "Initial Assignment Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Remove an initial assignment
	 */
	private void removeInit() {
		int index = initAssigns.getSelectedIndex();
		if (index != -1) {
			String selected = ((String) initAssigns.getSelectedValue());
			String tempVar = selected.split(" ")[0];
			String tempMath = selected.substring(selected.indexOf('=') + 2);
			ListOf r = gcm.getSBMLDocument().getModel().getListOfInitialAssignments();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumInitialAssignments(); i++) {
				if (SBMLutilities.myFormulaToString(((InitialAssignment) r.get(i)).getMath()).equals(tempMath)
						&& ((InitialAssignment) r.get(i)).getSymbol().equals(tempVar)) {
					r.remove(i);
				}
			}
			initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			Utility.remove(initAssigns);
			initAssigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < initAssigns.getModel().getSize()) {
				initAssigns.setSelectedIndex(index);
			}
			else {
				initAssigns.setSelectedIndex(index - 1);
			}
			dirty.setValue(true);
			gcm.makeUndoPoint();
		}
	}

	/**
	 * Determines if a variable is already in an initial or assignment rule
	 */
	public static boolean keepVarInit(BioModel gcm, String selected, String id) {
		if (!selected.equals(id)) {
			ListOf ia = gcm.getSBMLDocument().getModel().getListOfInitialAssignments();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumInitialAssignments(); i++) {
				InitialAssignment init = (InitialAssignment) ia.get(i);
				if (init.getSymbol().equals(id))
					return false;
			}
			ListOf r = gcm.getSBMLDocument().getModel().getListOfRules();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumRules(); i++) {
				Rule rule = (Rule) r.get(i);
				if (rule.isAssignment() && rule.getVariable().equals(id))
					return false;
			}
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addInit) {
			initEditor("Add");
		}
		// if the edit event button is clicked
		else if (e.getSource() == editInit) {
			initEditor("OK");
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeInit) {
			removeInit();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == initAssigns) {
				initEditor("OK");
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
