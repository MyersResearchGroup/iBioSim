package frontend.biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.comp.Port;

import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.util.GlobalConstants;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.main.Gui;
import frontend.main.util.Utility;

import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SpeciesReference;


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

	private BioModel bioModel;

	private ModelEditor modelEditor;
	
	private JComboBox dimensionType, dimensionX, dimensionY;
	
	private JTextField iIndex, jIndex;

	/* Create initial assignment panel */
	public InitialAssignments(BioModel bioModel, ModelEditor modelEditor) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		this.modelEditor = modelEditor;
		Model model = bioModel.getSBMLDocument().getModel();
		/* Create initial assignment panel */
		addInit = new JButton("Add Initial");
		removeInit = new JButton("Remove Initial");
		editInit = new JButton("Edit Initial");
		initAssigns = new JList();
		dimensionType = new JComboBox();
		dimensionType.addItem("Scalar");
		dimensionType.addItem("1-D Array");
		dimensionType.addItem("2-D Array");
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
		iIndex = new JTextField(10);
		jIndex = new JTextField(10);
		iIndex.setEnabled(false);
		jIndex.setEnabled(false);
		String[] inits = new String[model.getInitialAssignmentCount()];
		for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
			InitialAssignment init = model.getInitialAssignment(i);
			inits[i] = init.getVariable() + " = " + SBMLutilities.myFormulaToString(init.getMath());
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
		if (model.getInitialAssignmentCount() > 0) {
			String[] inits = new String[model.getInitialAssignmentCount()];
			for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
				InitialAssignment init = model.getListOfInitialAssignments().get(i);
				inits[i] = init.getVariable() + " = " + SBMLutilities.myFormulaToString(init.getMath());
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
		ListOf<InitialAssignment> r = gcm.getSBMLDocument().getModel().getListOfInitialAssignments();
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getInitialAssignmentCount(); i++) {
			if (r.get(i).getVariable().equals(variable)) {
				for (int j = 0; j < gcm.getSBMLCompModel().getListOfPorts().size(); j++) {
					Port port = gcm.getSBMLCompModel().getListOfPorts().get(j);
					if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(r.get(i).getMetaId())) {
						gcm.getSBMLCompModel().getListOfPorts().remove(j);
						break;
					}
				}
				r.remove(i);
			}
		}
	}

	public static boolean addInitialAssignment(BioModel bioModel, String assignment, String[] dimensions) {
		String variable = dimensions[0].trim();
		return addInitialAssignment(bioModel,assignment,dimensions,variable);
	}

	public static boolean addInitialRateAssignment(BioModel bioModel, String assignment, String[] dimensions) {
		String variable = dimensions[0].trim() + "_" + GlobalConstants.RATE;
		return addInitialAssignment(bioModel,assignment,dimensions,variable);
	}
	
	/**
	 * Try to add or edit initial assignments
	 */
	public static boolean addInitialAssignment(BioModel bioModel, String assignment, String[] dimensions, String variable) {
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
		String [] dimIds = SBMLutilities.getDimensionIds("",dimensions.length-1);
		if(SBMLutilities.displayinvalidVariables("Rule", bioModel.getSBMLDocument(), dimIds, assignment.trim(), "", false)){
			return true;
		}
		if (SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(assignment.trim()))) {
			return true;
		}
		if (SBMLutilities.checkFunctionArgumentTypes(bioModel.getSBMLDocument(), bioModel.addBooleans(assignment.trim()))) {
			return true;
		}
		if (SBMLutilities.returnsBoolean(bioModel.addBooleans(assignment.trim()), bioModel.getSBMLDocument().getModel())) {
			JOptionPane.showMessageDialog(Gui.frame, "Initial assignment must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		boolean error = false;
		InitialAssignment ia = bioModel.getSBMLDocument().getModel().createInitialAssignment();
		String initialId = "init__"+variable;
		SBMLutilities.setMetaId(ia, initialId);
		ia.setVariable(variable);
		ia.setMath(bioModel.addBooleans(assignment.trim()));
		SBMLutilities.createDimensions(ia, dimIds, dimensions);
		SBMLutilities.addIndices(ia, "symbol", dimIds, 0);
		if (checkInitialAssignmentUnits(bioModel, ia)) {
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
				SBMLutilities.cloneDimensionAddIndex(ia, port, "comp:metaIdRef");
			} 
		}
		return error;
	}

	/**
	 * Check the units of an initial assignment
	 */
	public static boolean checkInitialAssignmentUnits(BioModel bioModel, InitialAssignment init) {
		if (Gui.getCheckUndeclared()) {
			if (init.containsUndeclaredUnits()) {
				JOptionPane.showMessageDialog(Gui.frame, "Initial assignment contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (Gui.getCheckUnits()) {
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
	private static String[] sortInitRules(String[] initRules) {
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
		if(SBMLutilities.displayinvalidVariables("Rule", bioModel.getSBMLDocument(), null, assignment, "", false)){
			return true;
		}
		if (SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(assignment))) {
			return true;
		}
		if (SBMLutilities.checkFunctionArgumentTypes(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(assignment))) {
			return true;
		}
		if (SBMLutilities.returnsBoolean(SBMLutilities.myParseFormula(assignment), bioModel.getSBMLDocument().getModel())) {
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
			InitialAssignment r = (bioModel.getSBMLDocument().getModel().getListOfInitialAssignments()).get(Rindex);
			String oldSymbol = r.getVariable();
			String oldInit = SBMLutilities.myFormulaToString(r.getMath());
			String oldVal = inits[index];
			r.setVariable(variable);
			r.setMath(SBMLutilities.myParseFormula(assignment));
			inits[index] = variable + " = " + SBMLutilities.myFormulaToString(r.getMath());
			if (InitialAssignments.checkInitialAssignmentUnits(bioModel, r)) {
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
				if (SBMLutilities.checkCycles(bioModel.getSBMLDocument())) {
					JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
							"Cycle Detected", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (error) {
				r.setVariable(oldSymbol);
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
			InitialAssignment r = bioModel.getSBMLDocument().getModel().createInitialAssignment();
			r.setVariable(variable);
			r.setMath(SBMLutilities.myParseFormula(assignment));
			addStr = variable + " = " + SBMLutilities.myFormulaToString(r.getMath());
			Object[] adding = { addStr };
			add.setListData(adding);
			add.setSelectedIndex(0);
			initAssigns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			adding = Utility.add(inits, initAssigns, add);
			String[] oldInits = inits;
			inits = new String[adding.length];
			for (int i = 0; i < adding.length; i++) {
				inits[i] = (String) adding[i];
			}
			if (InitialAssignments.checkInitialAssignmentUnits(bioModel, r)) {
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
			if (!error && SBMLutilities.checkCycles(bioModel.getSBMLDocument())) {
				JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
						"Cycle Detected", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			if (error) {
				inits = oldInits;
				ListOf<InitialAssignment> ia = bioModel.getSBMLDocument().getModel().getListOfInitialAssignments();
				for (int i = 0; i < bioModel.getSBMLDocument().getModel().getInitialAssignmentCount(); i++) {
					if (SBMLutilities.myFormulaToString(ia.get(i).getMath()).equals(
							SBMLutilities.myFormulaToString(r.getMath()))
							&& ia.get(i).getVariable().equals(r.getVariable())) {
						ia.remove(i);
					}
				}
			}
			initAssigns.setListData(inits);
			initAssigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (bioModel.getSBMLDocument().getModel().getInitialAssignmentCount() == 1) {
				initAssigns.setSelectedIndex(0);
			}
			else {
				initAssigns.setSelectedIndex(index);
			}
		}
		if (!error) {
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
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
		Model model = bioModel.getSBMLDocument().getModel();
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			String id = model.getCompartment(i).getId();
			if (keepVarInit(bioModel, selected.split(" ")[0], id)
					&& (bioModel.getSBMLDocument().getLevel() > 2 || model.getCompartment(i).getSpatialDimensions() != 0)) {
				initVar.addItem(id);
			}
		}
		for (int i = 0; i < model.getParameterCount(); i++) {
			String id = model.getParameter(i).getId();
			if (keepVarInit(bioModel, selected.split(" ")[0], id)) {
				initVar.addItem(id);
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			String id = model.getSpecies(i).getId();
			if (keepVarInit(bioModel, selected.split(" ")[0], id)) {
				initVar.addItem(id);
			}
		}
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			for (int j = 0; j < reaction.getReactantCount(); j++) {
				SpeciesReference reactant = reaction.getReactant(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals(""))) {
					String id = reactant.getId();
					if (keepVarInit(bioModel, selected.split(" ")[0], id)) {
						initVar.addItem(id);
					}
				}
			}
			for (int j = 0; j < reaction.getProductCount(); j++) {
				SpeciesReference product = reaction.getProduct(j);
				if ((product.isSetId()) && (!product.getId().equals(""))) {
					String id = product.getId();
					if (keepVarInit(bioModel, selected.split(" ")[0], id)) {
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
			ListOf<InitialAssignment> r = bioModel.getSBMLDocument().getModel().getListOfInitialAssignments();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getInitialAssignmentCount(); i++) {
				if (SBMLutilities.myFormulaToString(r.get(i).getMath()).equals(initMath.getText())
						&& r.get(i).getVariable().equals(initVar.getSelectedItem())) {
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
			ListOf<InitialAssignment> r = bioModel.getSBMLDocument().getModel().getListOfInitialAssignments();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getInitialAssignmentCount(); i++) {
				if (SBMLutilities.myFormulaToString(r.get(i).getMath()).equals(tempMath)
						&& r.get(i).getVariable().equals(tempVar)) {
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
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
		}
	}

	/**
	 * Determines if a variable is already in an initial or assignment rule
	 */
	public static boolean keepVarInit(BioModel gcm, String selected, String id) {
		if (!selected.equals(id)) {
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getInitialAssignmentCount(); i++) {
				InitialAssignment init = gcm.getSBMLDocument().getModel().getInitialAssignment(i);
				if (init.getVariable().equals(id))
					return false;
			}
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getRuleCount(); i++) {
				Rule rule = gcm.getSBMLDocument().getModel().getRule(i);
				if (rule.isAssignment() && SBMLutilities.getVariable(rule).equals(id))
					return false;
			}
		}
		return true;
	}

	@Override
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

	@Override
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
