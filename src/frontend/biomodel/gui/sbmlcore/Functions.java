/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package frontend.biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.text.parser.FormulaParserLL3;
import org.sbml.jsbml.text.parser.IFormulaParser;
import org.sbml.jsbml.text.parser.ParseException;

import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.util.GlobalConstants;
import dataModels.util.exceptions.BioSimException;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.biomodel.gui.schematic.Utils;
import frontend.main.Gui;
import frontend.main.util.SpringUtilities;
import frontend.main.util.Utility;

import org.sbml.jsbml.JSBML;


/**
 * This is a class for creating SBML functions
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Functions extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addFunction, removeFunction, editFunction;

	private JList functions; // JList of functions

	private BioModel bioModel;

	private ModelEditor modelEditor;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;
	
	private JCheckBox onPort;
	
	private JComboBox SBOTerms;
	
	/* Create initial assignment panel */
	public Functions(BioModel bioModel, ModelEditor modelEditor) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		this.modelEditor = modelEditor;
		Model model = bioModel.getSBMLDocument().getModel();
		addFunction = new JButton("Add Function");
		removeFunction = new JButton("Remove Function");
		editFunction = new JButton("Edit Function");
		functions = new JList();
		ListOf<FunctionDefinition> listOfFunctions = model.getListOfFunctionDefinitions();
		int count = 0;
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			FunctionDefinition function = listOfFunctions.get(i);
			if (!Utils.isSpecialFunction(model,function.getId())) count++;
		}
		String[] funcs = new String[count];
		count = 0;
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			FunctionDefinition function = listOfFunctions.get(i);
			if (Utils.isSpecialFunction(model,function.getId())) continue;
			funcs[count] = function.getId() + " ( ";
			for (int j = 0; j < function.getArgumentCount(); j++) {
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
		dataModels.biomodel.util.Utility.sort(funcs);
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
	private static String[] sortFunctions(String[] funcs) {
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
		JPanel funcPanel = new JPanel(new SpringLayout());
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel argLabel = new JLabel("Arguments:");
		JLabel eqnLabel = new JLabel("Definition:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		JLabel sboTermLabel = new JLabel(GlobalConstants.SBOTERM);
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_MATHEMATICAL_EXPRESSION));
		JTextField funcID = new JTextField(12);
		JTextField funcName = new JTextField(12);
		JTextField args = new JTextField(12);
		JTextField eqn = new JTextField(12);
		onPort = new JCheckBox();
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				FunctionDefinition function = bioModel.getSBMLDocument().getModel().getFunctionDefinition((((String) functions.getSelectedValue()).split(" ")[0]));
				funcID.setText(function.getId());
				selectedID = function.getId();
				funcName.setText(function.getName());
				if (bioModel.getPortByIdRef(function.getId())!=null) {
					onPort.setSelected(true);
				} else {
					onPort.setSelected(false);
				}
				if (function.isSetSBOTerm()) {
					SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(function.getSBOTermID()));
				}
				String argStr = "";
				for (int j = 0; j < function.getArgumentCount(); j++) {
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
		funcPanel.add(onPortLabel);
		funcPanel.add(onPort);
		funcPanel.add(sboTermLabel);
		funcPanel.add(SBOTerms);
		SpringUtilities.makeCompactGrid(funcPanel,
                6, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
		functionPanel.add(funcPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, functionPanel, "Function Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = Utils.checkID(bioModel.getSBMLDocument(), funcID.getText().trim(), selectedID, false);
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
				} else
					try {
						IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
						if (args.getText().trim().equals("") && ASTNode.parseFormula("lambda(" + eqn.getText().trim() + ")", parser) == null) {
							JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else if (!args.getText().trim().equals("")
								&& JSBML.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")") == null) {
							JOptionPane.showMessageDialog(Gui.frame, "Formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						else {
							/*
							String [] dimID = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), funcID.getText(), false);
							String [] dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
							*/
							error = Utils.displayinvalidVariables("Function", bioModel.getSBMLDocument(), null, 
										eqn.getText().trim(), args.getText().trim(), true);
						}
					} catch (HeadlessException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
			}
			if (!error) {
				error = Utils.checkNumFunctionArguments(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(eqn.getText().trim()));
			}
			if (!error) {
				error = Utils.checkFunctionArgumentTypes(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(eqn.getText().trim()));
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
					FunctionDefinition f = bioModel.getSBMLDocument().getModel().getFunctionDefinition(val);
					f.setId(funcID.getText().trim());
					f.setName(funcName.getText().trim());
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						f.unsetSBOTerm();
					} else {
						f.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					if (args.getText().trim().equals("")) {
						try {
							f.setMath(JSBML.parseFormula("lambda(" + eqn.getText().trim() + ")"));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					else {
						try {
							f.setMath(JSBML.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")"));
						} catch (ParseException e) {
							e.printStackTrace();
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
					Port port = bioModel.getPortByIdRef(val);
					if (port!=null) {
						if (onPort.isSelected()) {
							port.setId(GlobalConstants.FUNCTION+"__"+f.getId());
							port.setIdRef(f.getId());
						} else {
							bioModel.getSBMLCompModel().removePort(port);
						}
					} else {
						if (onPort.isSelected()) {
							port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.FUNCTION+"__"+f.getId());
							port.setIdRef(f.getId());
						}
					}
					functions.setListData(funcs);
					functions.setSelectedIndex(index);
					try {
            SBMLutilities.updateVarId(bioModel.getSBMLDocument(), false, val, funcID.getText().trim());
          } catch (BioSimException e) {
            JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
          }
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
					adding = Utility.add(funcs, functions, add);
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
						FunctionDefinition f = bioModel.getSBMLDocument().getModel().createFunctionDefinition();
						f.setId(funcID.getText().trim());
						f.setName(funcName.getText().trim());
						if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
							f.unsetSBOTerm();
						} else {
							f.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
						}
						if (args.getText().trim().equals("")) {
							try {
								f.setMath(JSBML.parseFormula("lambda(" + eqn.getText().trim() + ")"));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						else {
							try {
								f.setMath(JSBML.parseFormula("lambda(" + args.getText().trim() + "," + eqn.getText().trim() + ")"));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						if (onPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.FUNCTION+"__"+f.getId());
							port.setIdRef(f.getId());
						}
					}
					functions.setListData(funcs);
					functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (bioModel.getSBMLDocument().getModel().getFunctionDefinitionCount() == 1) {
						functions.setSelectedIndex(0);
					}
					else {
						functions.setSelectedIndex(index);
					}
				}
				modelEditor.setDirty(true);
				modelEditor.makeUndoPoint();
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
			if (!SBMLutilities.variableInUse(bioModel.getSBMLDocument(), ((String) functions.getSelectedValue()).split(" ")[0], false, true, true)) {
				FunctionDefinition tempFunc = bioModel.getSBMLDocument().getModel().getFunctionDefinition(((String) functions.getSelectedValue()).split(" ")[0]);
				ListOf<FunctionDefinition> f = bioModel.getSBMLDocument().getModel().getListOfFunctionDefinitions();
				for (int i = 0; i < bioModel.getSBMLDocument().getModel().getFunctionDefinitionCount(); i++) {
					if (f.get(i).getId().equals(tempFunc.getId())) {
						f.remove(i);
					}
				}
				for (int i = 0; i < bioModel.getSBMLCompModel().getListOfPorts().size(); i++) {
					Port port = bioModel.getSBMLCompModel().getListOfPorts().get(i);
					if (port.isSetIdRef() && port.getIdRef().equals(tempFunc.getId())) {
						bioModel.getSBMLCompModel().getListOfPorts().remove(i);
						break;
					}
				}
				functions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				Utility.remove(functions);
				functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (index < functions.getModel().getSize()) {
					functions.setSelectedIndex(index);
				}
				else {
					functions.setSelectedIndex(index - 1);
				}
				modelEditor.setDirty(true);
				modelEditor.makeUndoPoint();
			}
		}
	}

	/**
	 * Refresh functions panel
	 */
	public void refreshFunctionsPanel() {
		Model model = bioModel.getSBMLDocument().getModel();
		ListOf<FunctionDefinition> listOfFunctions = model.getListOfFunctionDefinitions();
		int count = 0;
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			FunctionDefinition function = listOfFunctions.get(i);
			if (!Utils.isSpecialFunction(model,function.getId())) count++;
		}
		String[] funcs = new String[count];
		count = 0;
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			FunctionDefinition function = listOfFunctions.get(i);
			if (Utils.isSpecialFunction(model,function.getId())) continue;
			funcs[count] = function.getId() + " ( ";
			for (int j = 0; j < function.getArgumentCount(); j++) {
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
		dataModels.biomodel.util.Utility.sort(funcs);
		functions.setListData(funcs);
		functions.setSelectedIndex(0);
	}
	
	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addFunction) {
			functionEditor("Add");
		}
		// if the edit event button is clicked
		else if (e.getSource() == editFunction) {
			functionEditor("OK");
			initialsPanel.refreshInitialAssignmentPanel(bioModel);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeFunction) {
			removeFunction();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == functions) {
				functionEditor("OK");
				initialsPanel.refreshInitialAssignmentPanel(bioModel);
				rulesPanel.refreshRulesPanel();
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