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
package edu.utah.ece.async.biosim.gui.modelEditor.sbmlcore;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jlibsedml.Change;
import org.jlibsedml.RemoveXML;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.modelsupport.SBMLSupport;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;

import edu.utah.ece.async.biosim.dataModels.biomodel.util.SBMLutilities;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ElementsPanel extends JPanel { 

	private static final long serialVersionUID = 1L;
	private ArrayList<String> elementChanges;
	
	public ElementsPanel(SBMLDocument document,SEDMLDocument sedmlDoc,String simName/*String paramFile*/) {
		super(new GridLayout(1, 4));
		elementChanges = new ArrayList<String>();
		ArrayList<String> usedIDs = SBMLutilities.CreateListOfUsedIDs(document);
		SBMLSupport sbmlSupport = new SBMLSupport();
		SedML sedml = sedmlDoc.getSedMLModel();
		org.jlibsedml.Model model = sedml.getModelWithId(simName+"_model");
		if (model!=null) {
			for (Change change : model.getListOfChanges()) {
				if (change instanceof RemoveXML) {
					RemoveXML removeXML = (RemoveXML)change;
					String target = sbmlSupport.getIdFromXPathIdentifer(removeXML.getTargetXPath().getTargetAsString());
					if (target!=null) {
						elementChanges.add(target);
					}
				}
			}
		}
		Model m = document.getModel();
		int consNum = m.getConstraintCount();
		String[] cons = new String[consNum];
		for (int i = 0; i < consNum; i++) {
			Constraint constraint = m.getConstraint(i);
			if (!constraint.isSetMetaId()) {
				String constraintId = "constraint0";
				int cn = 0;
				while (usedIDs.contains(constraintId)) {
					cn++;
					constraintId = "constraint" + cn;
				}
				usedIDs.add(constraintId);
				SBMLutilities.setMetaId(constraint, constraintId);
			}
			cons[i] = constraint.getMetaId();
		}
		int rulNum = m.getRuleCount();
		String[] rul = new String[rulNum];
		for (int i = 0; i < rulNum; i++) {
			Rule rule = m.getRule(i);
			if (rule.isAlgebraic()) {
				rul[i] = "0 = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else if (rule.isAssignment()) {
				rul[i] = SBMLutilities.getVariable(rule) + " = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else {
				rul[i] = "d( " + SBMLutilities.getVariable(rule) + " )/dt = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			if (elementChanges.contains(rul[i])) {
				elementChanges.remove(rul[i]);
				elementChanges.add(rule.getMetaId());
			}
		}
		int initsNum = m.getInitialAssignmentCount();
		String[] inits = new String[initsNum];
		for (int i = 0; i < initsNum; i++) {
			inits[i] = m.getInitialAssignment(i).getVariable() + " = "
					+ SBMLutilities.myFormulaToString(m.getInitialAssignment(i).getMath());
		}
		int evNum = m.getEventCount();
		String[] ev = new String[evNum];
		for (int i = 0; i < evNum; i++) {
			if (m.getEvent(i).isSetId()) {
				ev[i] = m.getEvent(i).getId();
			}
		}
		if (initsNum > 0) {
			JPanel initsPanel = new JPanel(new GridLayout(initsNum + 1, 1));
			initsPanel.add(new JLabel("Initial Assignments:"));
			for (int i = 0; i < inits.length; i++) {
				JCheckBox temp = new JCheckBox(inits[i]);
				if (!elementChanges.contains(inits[i])) {
					temp.setSelected(true);
				}
				temp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox clicked = ((JCheckBox) e.getSource());
						if (clicked.isSelected()) {
							elementChanges.remove(clicked.getText());
						}
						else {
							elementChanges.add(clicked.getText());
						}
					}
				});
				initsPanel.add(temp);
			}
			JPanel initial = new JPanel();
			((FlowLayout) initial.getLayout()).setAlignment(FlowLayout.LEFT);
			initial.add(initsPanel);
			this.add(initial);
		}
		if (rulNum > 0) {
			JPanel rulPanel = new JPanel(new GridLayout(rulNum + 1, 1));
			rulPanel.add(new JLabel("Rules:"));
			for (int i = 0; i < rul.length; i++) {
				JCheckBox temp = new JCheckBox(rul[i]);
				if (!elementChanges.contains(rul[i])) {
					temp.setSelected(true);
				}
				temp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox clicked = ((JCheckBox) e.getSource());
						if (clicked.isSelected()) {
							elementChanges.remove(clicked.getText());
						}
						else {
							elementChanges.add(clicked.getText());
						}
					}
				});
				rulPanel.add(temp);
			}
			JPanel rules = new JPanel();
			((FlowLayout) rules.getLayout()).setAlignment(FlowLayout.LEFT);
			rules.add(rulPanel);
			this.add(rules);
		}
		if (consNum > 0) {
			JPanel consPanel = new JPanel(new GridLayout(consNum + 1, 1));
			consPanel.add(new JLabel("Constaints:"));
			for (int i = 0; i < cons.length; i++) {
				JCheckBox temp = new JCheckBox(cons[i]);
				if (!elementChanges.contains(cons[i])) {
					temp.setSelected(true);
				}
				temp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox clicked = ((JCheckBox) e.getSource());
						if (clicked.isSelected()) {
							elementChanges.remove(clicked.getText());
						}
						else {
							elementChanges.add(clicked.getText());
						}
					}
				});
				consPanel.add(temp);
			}
			JPanel constaints = new JPanel();
			((FlowLayout) constaints.getLayout()).setAlignment(FlowLayout.LEFT);
			constaints.add(consPanel);
			this.add(constaints);
		}
		if (evNum > 0) {
			JPanel evPanel = new JPanel(new GridLayout(evNum + 1, 1));
			evPanel.add(new JLabel("Events:"));
			for (int i = 0; i < ev.length; i++) {
				JCheckBox temp = new JCheckBox(ev[i]);
				if (!elementChanges.contains(ev[i])) {
					temp.setSelected(true);
				}
				temp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox clicked = ((JCheckBox) e.getSource());
						if (clicked.isSelected()) {
							elementChanges.remove(clicked.getText());
						}
						else {
							elementChanges.add(clicked.getText());
						}
					}
				});
				evPanel.add(temp);
			}
			JPanel events = new JPanel();
			((FlowLayout) events.getLayout()).setAlignment(FlowLayout.LEFT);
			events.add(evPanel);
			this.add(events);
		}
	}

	public ArrayList<String> getElementChanges() {
		return elementChanges;
	}

}
