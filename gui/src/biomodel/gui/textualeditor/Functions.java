package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
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
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.libsbml;

import biomodel.parser.BioModel;


/**
 * This is a class for creating SBML functions
 * 
 * @author Chris Myers
 * 
 */
public class Functions extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addFunction, removeFunction, editFunction;

	private JList functions; // JList of functions

	private BioModel gcm;

	private ArrayList<String> usedIDs;

	private MutableBoolean dirty;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;

	/* Create initial assignment panel */
	public Functions(BioModel gcm, ArrayList<String> usedIDs, MutableBoolean dirty) {
		super(new BorderLayout());
		this.gcm = gcm;
		this.usedIDs = usedIDs;
		this.dirty = dirty;
		Model model = gcm.getSBMLDocument().getModel();
		addFunction = new JButton("Add Function");
		removeFunction = new JButton("Remove Function");
		editFunction = new JButton("Edit Function");
		functions = new JList();
		ListOf listOfFunctions = model.getListOfFunctionDefinitions();
		int count = 0;
		for (int i = 0; i < model.getNumFunctionDefinitions(); i++) {
			FunctionDefinition function = (FunctionDefinition) listOfFunctions.get(i);
			if (!SBMLutilities.isSpecialFunction(function.getId())) count++;
		}
		String[] funcs = new String[count];
		count = 0;
		for (int i = 0; i < model.getNumFunctionDefinitions(); i++) {
			FunctionDefinition function = (FunctionDefinition) listOfFunctions.get(i);
			if (SBMLutilities.isSpecialFunction(function.getId())) continue;
			funcs[count] = function.getId() + " ( ";
			for (long j = 0; j < function.getNumArguments(); j++) {
				if (j != 0) {
					funcs[count] += ", ";
				}
				funcs[count] += SBMLutilities.myFormulaToString(function.getArgument(j));
			}
			if (function.isSetMath()) {
				funcs[count] += " ) = " + SBMLutilities.myFormulaToString(function.getBody());
			}
			count++;
		}
		String[] oldFuncs = funcs;
		try {
			funcs = sortFunctions(funcs);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in function definitions.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
			funcs = oldFuncs;
		}
		JPanel addRem = new JPanel();
		addRem.add(addFunction);
		addRem.add(removeFunction);
		addRem.add(editFunction);
		addFunction.addActionListener(this);
		removeFunction.addActionListener(this);
		editFunction.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Functions:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(functions);
		Utility.sort(funcs);
		functions.setListData(funcs);
		functions.setSelectedIndex(0);
		functions.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
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
	 * Creates a frame used to edit functions or create new ones.
	 */
	private void functionEditor(String option) {
		if (option.equals("OK") && functions.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No function selected.", "Must Select a Function", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel functionPanel = new JPanel();
		JPanel funcPanel = new JPanel(new GridLayout(4, 2));
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel argLabel = new JLabel("Arguments:");
		JLabel eqnLabel = new JLabel("Definition:");
		JTextField funcID = new JTextField(12);
		JTextField funcName = new JTextField(12);
		JTextField args = new JTextField(12);
		JTextField eqn = new JTextField(12);
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				FunctionDefinition function = gcm.getSBMLDocument().getModel().getFunctionDefinition((((String) functions.getSelectedValue()).split(" ")[0]));
				funcID.setText(function.getId());
				selectedID = function.getId();
				funcName.setText(function.getName());
				String argStr = "";
				for (long j = 0; j < function.getNumArguments(); j++) {
					if (j != 0) {
						argStr += ", ";
					}
					argStr += SBMLutilities.myFormulaToString(function.getArgument(j));
				}
				args.setText(argStr);
				if (function.isSetMath()) {
					eqn.setText("" + SBMLutilities.myFormulaToString(function.getBody()));
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
		int value = JOptionPane.showOptionDialog(Gui.frame, functionPanel, "Function Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(gcm.getSBMLDocument(), usedIDs, funcID.getText().trim(), selectedID, false);
			if (!error) {
				String[] vars = eqn.getText().trim().split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
				for (int i = 0; i < vars.length; i++) {
					if (vars[i].equals(funcID.getText().trim())) {
						JOptionPane.showMessageDialog(Gui.frame, "Recursive functions are not allowed.", "Recursion Illegal",
								JOptionPane.ERROR_MESSAGE);
						error = true;
						break;
					}
				}
			}
			if (!error) {
				if (eqn.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (args.getText().trim().equals("") && libsbml.parseFormula("lambda(" + eqn.getText().trim() + ")") == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!args.getText().trim().equals("")
						&& libsbml.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")") == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else {
					ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(gcm.getSBMLDocument(), eqn.getText().trim(), args.getText().trim(), true);
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
						message = "Function can only contain the arguments or other function calls.\n\n" + "Illegal variables:\n" + invalid;
						JTextArea messageArea = new JTextArea(message);
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(300, 300));
						scrolls.setPreferredSize(new Dimension(300, 300));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(Gui.frame, scrolls, "Illegal Variables", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
			}
			if (!error) {
				error = SBMLutilities.checkNumFunctionArguments(gcm.getSBMLDocument(), SBMLutilities.myParseFormula(eqn.getText().trim()));
			}
			if (!error) {
				if (option.equals("OK")) {
					String[] funcs = new String[functions.getModel().getSize()];
					for (int i = 0; i < functions.getModel().getSize(); i++) {
						funcs[i] = functions.getModel().getElementAt(i).toString();
					}
					int index = functions.getSelectedIndex();
					String val = ((String) functions.getSelectedValue()).split(" ")[0];
					functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					funcs = Utility.getList(funcs, functions);
					functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					FunctionDefinition f = gcm.getSBMLDocument().getModel().getFunctionDefinition(val);
					f.setId(funcID.getText().trim());
					f.setName(funcName.getText().trim());
					if (args.getText().trim().equals("")) {
						f.setMath(libsbml.parseFormula("lambda(" + eqn.getText().trim() + ")"));
					}
					else {
						f.setMath(libsbml.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")"));
					}
					for (int i = 0; i < usedIDs.size(); i++) {
						if (usedIDs.get(i).equals(val)) {
							usedIDs.set(i, funcID.getText().trim());
						}
					}
					String oldVal = funcs[index];
					funcs[index] = funcID.getText().trim() + " ( " + args.getText().trim() + " ) = " + eqn.getText().trim();
					try {
						funcs = sortFunctions(funcs);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in functions.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
						funcs[index] = oldVal;
					}
					functions.setListData(funcs);
					functions.setSelectedIndex(index);
					SBMLutilities.updateVarId(gcm.getSBMLDocument(), false, val, funcID.getText().trim());
				}
				else {
					String[] funcs = new String[functions.getModel().getSize()];
					for (int i = 0; i < functions.getModel().getSize(); i++) {
						funcs[i] = functions.getModel().getElementAt(i).toString();
					}
					int index = functions.getSelectedIndex();
					JList add = new JList();
					String addStr;
					addStr = funcID.getText().trim() + " ( " + args.getText().trim() + " ) = " + eqn.getText().trim();
					Object[] adding = { addStr };
					add.setListData(adding);
					add.setSelectedIndex(0);
					functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(funcs, functions, add, null, null, null, null, null, Gui.frame);
					String[] oldVal = funcs;
					funcs = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						funcs[i] = (String) adding[i];
					}
					try {
						funcs = sortFunctions(funcs);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in functions.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
						funcs = oldVal;
					}
					if (!error) {
						FunctionDefinition f = gcm.getSBMLDocument().getModel().createFunctionDefinition();
						f.setId(funcID.getText().trim());
						f.setName(funcName.getText().trim());
						if (args.getText().trim().equals("")) {
							f.setMath(libsbml.parseFormula("lambda(" + eqn.getText().trim() + ")"));
						}
						else {
							f.setMath(libsbml.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")"));
						}
						usedIDs.add(funcID.getText().trim());
					}
					functions.setListData(funcs);
					functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (gcm.getSBMLDocument().getModel().getNumFunctionDefinitions() == 1) {
						functions.setSelectedIndex(0);
					}
					else {
						functions.setSelectedIndex(index);
					}
				}
				dirty.setValue(true);
				gcm.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, functionPanel, "Function Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Remove a function if not in use
	 */
	private void removeFunction() {
		int index = functions.getSelectedIndex();
		if (index != -1) {
			if (!SBMLutilities.variableInUse(gcm.getSBMLDocument(), ((String) functions.getSelectedValue()).split(" ")[0], false, true, true)) {
				FunctionDefinition tempFunc = gcm.getSBMLDocument().getModel().getFunctionDefinition(((String) functions.getSelectedValue()).split(" ")[0]);
				ListOf f = gcm.getSBMLDocument().getModel().getListOfFunctionDefinitions();
				for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumFunctionDefinitions(); i++) {
					if (((FunctionDefinition) f.get(i)).getId().equals(tempFunc.getId())) {
						f.remove(i);
					}
				}
				usedIDs.remove(((String) functions.getSelectedValue()).split(" ")[0]);
				functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Utility.remove(functions);
				functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < functions.getModel().getSize()) {
					functions.setSelectedIndex(index);
				}
				else {
					functions.setSelectedIndex(index - 1);
				}
				dirty.setValue(true);
				gcm.makeUndoPoint();
			}
		}
	}

	/**
	 * Refresh functions panel
	 */
	public void refreshFunctionsPanel() {
		Model model = gcm.getSBMLDocument().getModel();
		ListOf listOfFunctions = model.getListOfFunctionDefinitions();
		int count = 0;
		for (int i = 0; i < model.getNumFunctionDefinitions(); i++) {
			FunctionDefinition function = (FunctionDefinition) listOfFunctions.get(i);
			if (!SBMLutilities.isSpecialFunction(function.getId())) count++;
		}
		String[] funcs = new String[count];
		count = 0;
		for (int i = 0; i < model.getNumFunctionDefinitions(); i++) {
			FunctionDefinition function = (FunctionDefinition) listOfFunctions.get(i);
			if (SBMLutilities.isSpecialFunction(function.getId())) continue;
			funcs[count] = function.getId() + " ( ";
			for (long j = 0; j < function.getNumArguments(); j++) {
				if (j != 0) {
					funcs[count] += ", ";
				}
				funcs[count] += SBMLutilities.myFormulaToString(function.getArgument(j));
			}
			if (function.isSetMath()) {
				funcs[count] += " ) = " + SBMLutilities.myFormulaToString(function.getBody());
			}
			count++;
		}
		Utility.sort(funcs);
		functions.setListData(funcs);
		functions.setSelectedIndex(0);
	}
	
	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addFunction) {
			functionEditor("Add");
		}
		// if the edit event button is clicked
		else if (e.getSource() == editFunction) {
			functionEditor("OK");
			initialsPanel.refreshInitialAssignmentPanel(gcm);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeFunction) {
			removeFunction();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == functions) {
				functionEditor("OK");
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