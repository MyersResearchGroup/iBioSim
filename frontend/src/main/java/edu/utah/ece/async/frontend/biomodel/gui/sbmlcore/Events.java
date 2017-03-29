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
package edu.utah.ece.async.frontend.biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.layout.Layout;

import edu.utah.ece.async.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.dataModels.biomodel.annotation.SBOLAnnotation;
import edu.utah.ece.async.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.dataModels.util.GlobalConstants;
import edu.utah.ece.async.frontend.biomodel.gui.sbol.SBOLField2;
import edu.utah.ece.async.frontend.biomodel.gui.schematic.ModelEditor;
import edu.utah.ece.async.frontend.biomodel.gui.schematic.Utils;
import edu.utah.ece.async.frontend.main.Gui;
import edu.utah.ece.async.frontend.main.util.Utility;


/**
 * This is a class for creating SBML events.
 * 
 * @author Chris Myers
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Events extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addEvent, addTrans, removeEvent, editEvent, addAssignment, editAssignment, removeAssignment;

	private JList events; // JList of events

	private JList eventAssign; // JList of event assignments
	
	private JTextField iIndex, EAdimensions;
	
	private JComboBox eaID;

	private BioModel bioModel;

	private ModelEditor modelEditor;
	
	private SBOLField2 sbolField;
	
	private boolean isTextual;
	
	private JTextField eventID;
	
	private boolean isTransition;
	
	private JComboBox SBOTerms;
	
	/* Create event panel */
	public Events(Gui biosim, BioModel bioModel, ModelEditor modelEditor, boolean isTextual) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		this.isTextual = isTextual;
		this.modelEditor = modelEditor;
		Model model = bioModel.getSBMLDocument().getModel();
		addEvent = new JButton("Add Event");
		addTrans = new JButton("Add Transition");
		if (biosim.lema) {
			removeEvent = new JButton("Remove Transition");
			editEvent = new JButton("Edit Transition");
		} else {
			removeEvent = new JButton("Remove Event");
			editEvent = new JButton("Edit Event");
		}
		events = new JList();
		eventAssign = new JList();
		ListOf<Event> listOfEvents = model.getListOfEvents();
		String[] ev = new String[model.getEventCount()];
		for (int i = 0; i < model.getEventCount(); i++) {
			org.sbml.jsbml.Event event = listOfEvents.get(i);
			if (!event.isSetId()) {
				String eventId = "event0";
				int en = 0;
				while (bioModel.isSIdInUse(eventId)) {
					en++;
					eventId = "event" + en;
				}
				event.setId(eventId);
			}
			ev[i] = event.getId();
			ev[i] += SBMLutilities.getDimensionString(event);
		}
		JPanel addRem = new JPanel();
		if (!biosim.lema) {
			addRem.add(addEvent);
		}
		if (isTextual) {
			addRem.add(addTrans);
			addTrans.addActionListener(this);
		}
		addRem.add(removeEvent);
		addRem.add(editEvent);
		addEvent.addActionListener(this);
		removeEvent.addActionListener(this);
		editEvent.addActionListener(this);
		JLabel panelLabel;
		if (biosim.lema) {
			panelLabel = new JLabel("List of Transitions:");
		} else {
			panelLabel = new JLabel("List of Events:");
		}
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(events);
		edu.utah.ece.async.dataModels.biomodel.util.Utility.sort(ev);
		events.setListData(ev);
		events.setSelectedIndex(0);
		events.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Creates a frame used to edit events or create new ones.
	 */
	public String eventEditor(String option,String selected,boolean isTransition) {
		this.isTransition = isTransition;
		String[] origAssign = new String[0];
		String[] assign = new String[0];
		String[] placeAssign = new String[0];
		ArrayList<String> presetPlaces = new ArrayList<String>();
		JPanel eventPanel = new JPanel(new BorderLayout());
		// JPanel evPanel = new JPanel(new GridLayout(2, 2));
		JPanel evPanel = new JPanel(new GridLayout(10, 2));
		if (isTransition) {
			evPanel.setLayout(new GridLayout(8, 2));
		}
		JLabel IDLabel = new JLabel("ID:");
		JLabel NameLabel = new JLabel("Name:");
		JLabel sboTermLabel = new JLabel(GlobalConstants.SBOTERM);
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_INTERACTION));
		if (isTransition) {
			SBOTerms.setSelectedItem("petri net transition");
		}
		JLabel triggerLabel;
		if (isTransition) {
			triggerLabel = new JLabel("Enabling condition:");
		} else {
			triggerLabel = new JLabel("Trigger:");
		}
		JLabel delayLabel = new JLabel("Delay:");
		JLabel priorityLabel = new JLabel("Priority:");
		JLabel assignTimeLabel = new JLabel("Use values at trigger time:");
		JLabel persistentTriggerLabel;
		if (isTransition) {
			persistentTriggerLabel = new JLabel("Enabling is persistent:");
		} else {
			persistentTriggerLabel = new JLabel("Trigger is persistent:");
		}
		JLabel initialTriggerLabel = new JLabel("Trigger initially true:");
		JLabel dynamicProcessLabel = new JLabel("Dynamic Process:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		JLabel failTransitionLabel = new JLabel("Fail transition:");
		
		eventID = new JTextField(12);
		JTextField eventName = new JTextField(12);
		JTextField eventTrigger = new JTextField(12);
		JTextField eventDelay = new JTextField(12);
		JTextField eventPriority = new JTextField(12);
		JCheckBox assignTime = new JCheckBox("");
		JCheckBox persistentTrigger = new JCheckBox("");
		JCheckBox initialTrigger = new JCheckBox("");
		JCheckBox failTransition = new JCheckBox("");
		JComboBox dynamicProcess = new JComboBox(new String[] {"none",
				"Symmetric Division","Asymmetric Division","Death", "Move Random", "Move Left", "Move Right", "Move Above", "Move Below"});
		JCheckBox onPort = new JCheckBox();
		iIndex = new JTextField(20);
		EAdimensions = new JTextField(20);
		if (bioModel != null && bioModel.IsWithinCompartment() == false) {
			dynamicProcess.setEnabled(false);
			dynamicProcess.setSelectedItem("none");
		}
		
		JPanel eventAssignPanel = new JPanel(new BorderLayout());
		JPanel addEventAssign = new JPanel();
		addAssignment = new JButton("Add Assignment");
		removeAssignment = new JButton("Remove Assignment");
		editAssignment = new JButton("Edit Assignment");
		addEventAssign.add(addAssignment);
		addEventAssign.add(removeAssignment);
		addEventAssign.add(editAssignment);
		addAssignment.addActionListener(this);
		removeAssignment.addActionListener(this);
		editAssignment.addActionListener(this);
		JLabel eventAssignLabel = new JLabel("List of Assignments:");
		eventAssign.removeAll();
		eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(eventAssign);
		int Eindex = -1;
		String selectedID = "";
		if (option.equals("OK")) {
			ListOf<Event> e = bioModel.getSBMLDocument().getModel().getListOfEvents();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getEventCount(); i++) {
				org.sbml.jsbml.Event event = e.get(i);
				if (event.getId().equals(selected)) {
					isTransition = SBMLutilities.isTransition(event);
					if (isTransition) {
						evPanel.setLayout(new GridLayout(8, 2));
					}
					Eindex = i;
					eventID.setText(event.getId());
					selectedID = event.getId();
					eventName.setText(event.getName());
					if (event.isSetSBOTerm()) {
						SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(event.getSBOTermID()));
					}
					String trigger = SBMLutilities.myFormulaToString(event.getTrigger().getMath());
					ASTNode triggerMath = event.getTrigger().getMath();
					for (int j = 0; j < bioModel.getSBMLDocument().getModel().getParameterCount(); j++) {
						Parameter parameter = bioModel.getSBMLDocument().getModel().getParameter(j);
						if (parameter!=null && SBMLutilities.isPlace(parameter)) {
							if (!isTextual && (trigger.contains("eq("+parameter.getId()+", 1)")||
									trigger.contains("("+parameter.getId()+" == 1)"))) {
								triggerMath = SBMLutilities.removePreset(triggerMath, parameter.getId());
								presetPlaces.add(parameter.getId());
							}
						} 
					}
					eventTrigger.setText(bioModel.removeBooleans(triggerMath));
					
					String dynamic = AnnotationUtility.parseDynamicAnnotation(event);
					if (dynamic!=null) {
						dynamicProcess.setSelectedItem(dynamic);
					}
					
					if (event.isSetDelay() && event.getDelay().isSetMath()) {
						ASTNode delay = event.getDelay().getMath();	
						if ((delay.getType() == ASTNode.Type.FUNCTION) && (delay.getName().equals("priority"))) {
							eventDelay.setText(SBMLutilities.myFormulaToString(delay.getLeftChild()));
							eventPriority.setText(SBMLutilities.myFormulaToString(delay.getRightChild()));
						}
						else {
							eventDelay.setText(bioModel.removeBooleans(delay));
						}
					}
					if (event.getUseValuesFromTriggerTime()) {
						assignTime.setSelected(true);
					}
					if (AnnotationUtility.checkObsoleteAnnotation(event.getTrigger(),"<TriggerCanBeDisabled/>")) {
						persistentTrigger.setSelected(false);
					}
					else {
						persistentTrigger.setSelected(true);
					}
					if (AnnotationUtility.checkObsoleteAnnotation(event.getTrigger(),"<TriggerInitiallyFalse/>")) {
						initialTrigger.setSelected(false);
					}
					else {
						initialTrigger.setSelected(true);
					}
					if (event.isSetPriority()) {
						eventPriority.setText(bioModel.removeBooleans(event.getPriority().getMath()));
					}
					if (event.getTrigger().isSetPersistent()) {
						persistentTrigger.setSelected(event.getTrigger().getPersistent());
						if (isTransition) {
							Rule r = bioModel.getSBMLDocument().getModel().getRule(GlobalConstants.TRIGGER + "_" + event.getId());
							if (r != null) {
								persistentTrigger.setSelected(true);
								triggerMath = r.getMath();
								if (triggerMath.getType()==ASTNode.Type.FUNCTION_PIECEWISE && triggerMath.getChildCount() > 2) {
									triggerMath = triggerMath.getChild(1);
									if (triggerMath.getType()==ASTNode.Type.LOGICAL_OR) {
										triggerMath = triggerMath.getLeftChild();
										trigger = SBMLutilities.myFormulaToString(triggerMath);
										for (int j = 0; j < bioModel.getSBMLDocument().getModel().getParameterCount(); j++) {
											Parameter parameter = bioModel.getSBMLDocument().getModel().getParameter(j);
											if (parameter!=null && SBMLutilities.isPlace(parameter)) {
												if (!isTextual && (trigger.contains("eq("+parameter.getId()+", 1)")||
														trigger.contains("("+parameter.getId()+" == 1)"))) {
													triggerMath = SBMLutilities.removePreset(triggerMath, parameter.getId());
													//presetPlaces.add(parameter.getId());
												}
											} 
										}
										eventTrigger.setText(bioModel.removeBooleans(triggerMath));
									}
								}
							}
						} else {
							persistentTrigger.setSelected(false);
						}
					}
					if (event.getTrigger().isSetInitialValue()) {
						initialTrigger.setSelected(event.getTrigger().getInitialValue());
					}
					if (bioModel.getPortByIdRef(event.getId())!=null) {
						onPort.setSelected(true);
					} else {
						onPort.setSelected(false);
					}
					int numPlaces=0;
					int numFail=0;
					for (int j = 0; j < event.getEventAssignmentCount(); j++) {
						EventAssignment ea = event.getListOfEventAssignments().get(j);
						Parameter parameter = bioModel.getSBMLDocument().getModel().getParameter(ea.getVariable());
						if (parameter!=null && SBMLutilities.isPlace(parameter)) {
							numPlaces++;
						} else if (ea.getVariable().equals(GlobalConstants.FAIL)) {
							numFail++;
						}
					}
					if (isTextual) {
						assign = new String[event.getEventAssignmentCount()-(numFail)];
					} else {
						assign = new String[event.getEventAssignmentCount()-(numPlaces+numFail)];
					}
					if (isTextual) {
						placeAssign = new String[0];
					} else {
						placeAssign = new String[numPlaces];
					}
					origAssign = new String[event.getEventAssignmentCount()];
					int k=0;
					int l=0;
					for (int j = 0; j < event.getEventAssignmentCount(); j++) {
						Parameter parameter = 
								bioModel.getSBMLDocument().getModel().getParameter(event.getListOfEventAssignments().get(j).getVariable());
						EventAssignment ea = event.getListOfEventAssignments().get(j);
						String freshIndex = "; " + SBMLutilities.getIndicesString(ea, "variable");
						String dimens = " " + SBMLutilities.getDimensionString(ea);
						if (parameter!=null && SBMLutilities.isPlace(parameter)) {
							if (isTextual) {
								assign[l] = ea.getVariable() + dimens + " := " 
										+ SBMLutilities.myFormulaToString(ea.getMath()) + freshIndex;
								l++;
							} else {
								placeAssign[k] = ea.getVariable() + dimens + " := " 
										+ SBMLutilities.myFormulaToString(ea.getMath())	+ freshIndex;
								k++;
							}
						} else if (ea.getVariable().equals(GlobalConstants.FAIL)){
							failTransition.setSelected(true);
						} else {
							String assignMath = SBMLutilities.myFormulaToString(ea.getMath());
							if (parameter!=null && SBMLutilities.isBoolean(parameter)) {
								assignMath = bioModel.removeBooleanAssign(event.getListOfEventAssignments().get(j).getMath());
							} 
							assign[l] = ea.getVariable() + dimens + " := " + assignMath + freshIndex;
							l++;
						}
						origAssign[j] = ea.getVariable() + dimens + " := " 
								+ SBMLutilities.myFormulaToString(ea.getMath()) + freshIndex;
					}
					if (!modelEditor.isParamsOnly()) {
						//Parse out SBOL annotations and add to SBOL field
						List<URI> sbolURIs = new LinkedList<URI>();
						String sbolStrand = AnnotationUtility.parseSBOLAnnotation(event, sbolURIs);
						// Field for annotating event with SBOL DNA components
						sbolField = new SBOLField2(sbolURIs, sbolStrand, GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 
								2, false);
					}
					String dimInID = SBMLutilities.getDimensionString(event);
					eventID.setText(eventID.getText()+dimInID);
				}
			}
		}
		else {
			// Field for annotating event with SBOL DNA components
			sbolField = new SBOLField2(new LinkedList<URI>(), GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND, 
					GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 2, false);
			String eventId = "event0";
			if (isTransition) eventId = "t0";
			int en = 0;
			while (bioModel.isSIdInUse(eventId)) {
				en++;
				if (isTransition) eventId = "t" + en;
				else eventId = "event" + en;
			}
			eventID.setText(eventId);
		}
		edu.utah.ece.async.dataModels.biomodel.util.Utility.sort(assign);
		eventAssign.setListData(assign);
		eventAssign.setSelectedIndex(0);
		eventAssign.addMouseListener(this);
		eventAssignPanel.add(eventAssignLabel, "North");
		eventAssignPanel.add(scroll, "Center");
		eventAssignPanel.add(addEventAssign, "South");
		evPanel.add(IDLabel);
		evPanel.add(eventID);
		evPanel.add(NameLabel);
		evPanel.add(eventName);
		evPanel.add(onPortLabel);
		evPanel.add(onPort);
		evPanel.add(sboTermLabel);
		evPanel.add(SBOTerms);
		evPanel.add(triggerLabel);
		evPanel.add(eventTrigger);
		JPanel persistPanel = new JPanel();
		persistPanel.add(persistentTriggerLabel);
		persistPanel.add(persistentTrigger);
		evPanel.add(persistPanel);
		if (!isTransition) {
			JPanel initTrigPanel = new JPanel();
			initTrigPanel.add(initialTriggerLabel);
			initTrigPanel.add(initialTrigger);
			evPanel.add(initTrigPanel);
			JPanel assignTimePanel = new JPanel();
			assignTimePanel.add(assignTimeLabel);
			assignTimePanel.add(assignTime);
			evPanel.add(new JLabel(""));
			evPanel.add(assignTimePanel);
		} else {
			JPanel failTransPanel = new JPanel();
			failTransPanel.add(failTransitionLabel);
			failTransPanel.add(failTransition);
			evPanel.add(failTransPanel);
		}
		evPanel.add(delayLabel);
		evPanel.add(eventDelay);
		evPanel.add(priorityLabel);
		evPanel.add(eventPriority);
		if (!isTransition) {
			evPanel.add(dynamicProcessLabel);
			evPanel.add(dynamicProcess);
		}
		eventPanel.add(evPanel, "North");
		if (!modelEditor.isParamsOnly())
			eventPanel.add(sbolField, "Center");
		eventPanel.add(eventAssignPanel, "South");
		Object[] options = { option, "Cancel" };
		String title = "Event Editor";
		if (isTransition) {
			title = "Transition Editor";
		}
		int value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, options[0]);
		String[] dimID = new String[]{""};
		String[] dimensionIds = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			assign = new String[eventAssign.getModel().getSize()];
			for (int i = 0; i < eventAssign.getModel().getSize(); i++) {
				assign[i] = eventAssign.getModel().getElementAt(i).toString();
			}
			dimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), eventID.getText(), false);
			if(dimID!=null){
				dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
				error = Utils.checkID(bioModel.getSBMLDocument(), dimID[0].trim(), selected, false);
			}
			else{
				error = true;
			}
			if(!error){
				if (eventTrigger.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(Gui.frame, "Event must have a trigger formula.", "Enter Trigger Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (SBMLutilities.myParseFormula(eventTrigger.getText().trim()) == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Trigger formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!SBMLutilities.returnsBoolean(bioModel.addBooleans(eventTrigger.getText().trim()), bioModel.getSBMLDocument().getModel())) {
					JOptionPane.showMessageDialog(Gui.frame, "Trigger formula must be of type Boolean.", 
							"Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!eventDelay.getText().trim().equals("") && SBMLutilities.myParseFormula(eventDelay.getText().trim()) == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Delay formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (!eventPriority.getText().trim().equals("") && SBMLutilities.myParseFormula(eventPriority.getText().trim()) == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Priority formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (bioModel.getSBMLDocument().getLevel() < 3 && assign.length == 0) {
					JOptionPane.showMessageDialog(Gui.frame, "Event must have at least one event assignment.", "Event Assignment Needed",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else {
					error = Utils.displayinvalidVariables("Event trigger", bioModel.getSBMLDocument(), dimensionIds, eventTrigger.getText().trim(), "", false);
					if (!error) {
						error = Utils.displayinvalidVariables("Event delay", bioModel.getSBMLDocument(), dimensionIds, eventDelay.getText().trim(), "", false);
					}
					if (!error) {
						error = Utils.displayinvalidVariables("Event priority", bioModel.getSBMLDocument(), dimensionIds, eventPriority.getText().trim(), "", false);
					}
					if (!error) {
						error = Utils.checkNumFunctionArguments(bioModel.getSBMLDocument(), 
								SBMLutilities.myParseFormula(eventTrigger.getText().trim()));
					}
					if (!error) {
						error = Utils.checkFunctionArgumentTypes(bioModel.getSBMLDocument(), 
								bioModel.addBooleans(eventTrigger.getText().trim()));
					}
					if ((!error) && (!eventDelay.getText().trim().equals(""))) {
						error = Utils.checkNumFunctionArguments(bioModel.getSBMLDocument(), 
								bioModel.addBooleans(eventDelay.getText().trim()));
						if (!error) {
							if (SBMLutilities.returnsBoolean(bioModel.addBooleans(eventDelay.getText().trim()), bioModel.getSBMLDocument().getModel())) {
								JOptionPane.showMessageDialog(Gui.frame, "Event delay must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
					if ((!error) && (!eventPriority.getText().trim().equals(""))) {
						error = Utils.checkNumFunctionArguments(bioModel.getSBMLDocument(), 
								bioModel.addBooleans(eventPriority.getText().trim()));
						if (!error) {
							if (SBMLutilities.returnsBoolean(bioModel.addBooleans(eventPriority.getText().trim()), bioModel.getSBMLDocument().getModel())) {
								JOptionPane.showMessageDialog(Gui.frame, "Event priority must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error) {
				String[] EAdimID = new String[]{""};
				String[] EAdex = new String[]{""};
				String[] EAdimensionIds = new String[]{""};
				for (int i = 0; i < assign.length; i++) {
					String left = assign[i].split(":=")[0].trim();
					String rightSide = assign[i].split(":=")[1].split(";")[1].trim();
					EAdimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), left, false);
					if(EAdimID!=null){
						EAdimensionIds = SBMLutilities.getDimensionIds("e",EAdimID.length-1);
						String variableId = EAdimID[0].trim();
						if (variableId.endsWith("_"+GlobalConstants.RATE)) {
							variableId = variableId.replace("_"+GlobalConstants.RATE, "");
						}
						SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), variableId);
						EAdex = Utils.checkIndices(rightSide, variable, bioModel.getSBMLDocument(), EAdimensionIds, "variable", EAdimID, dimensionIds, dimID);
						error = (EAdex==null);
					}
					else{
						error = true;
					}
				}
			}
			if (!error) {
				//edit event
				org.sbml.jsbml.Event e = (bioModel.getSBMLDocument().getModel().getListOfEvents()).get(Eindex);
				if (option.equals("OK")) {
					events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					String[] ev = new String[events.getModel().getSize()];
					for (int i = 0; i < events.getModel().getSize(); i++) {
						ev[i] = events.getModel().getElementAt(i).toString();
					}
					events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					e.setUseValuesFromTriggerTime(assignTime.isSelected());
					while (e.getEventAssignmentCount() > 0) {
						e.getListOfEventAssignments().remove(0);
					}
					String[] EAdimID = new String[]{""};
					String[] EAdex = new String[]{""};
					String[] EAdimensionIds = new String[]{""};
					for (int i = 0; i < assign.length; i++) {
						EventAssignment ea = e.createEventAssignment();
						String var = assign[i].split(" ")[0];
						ea.setVariable(var);
						String left = assign[i].split(":=")[0].trim();
						String rightSide = assign[i].split(":=")[1].split(";")[1].trim();
						EAdimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), left, false);
						if(EAdimID!=null){
							EAdimensionIds = SBMLutilities.getDimensionIds("e",EAdimID.length-1);
							String variableId = EAdimID[0].trim();
							if (variableId.endsWith("_"+GlobalConstants.RATE)) {
								variableId = variableId.replace("_"+GlobalConstants.RATE, "");
							}
							SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), variableId);
							EAdex = Utils.checkIndices(rightSide, variable, bioModel.getSBMLDocument(), EAdimensionIds, "variable", EAdimID, dimensionIds, dimID);
						}
						SBMLutilities.createDimensions(ea, EAdimensionIds, EAdimID);
						SBMLutilities.addIndices(ea, "variable", EAdex, 1);
						Parameter p = bioModel.getSBMLDocument().getModel().getParameter(var);
						if (p != null && SBMLutilities.isBoolean(p)) {
							ea.setMath(bioModel.addBooleanAssign(assign[i].split(":=")[1].split(";")[0].trim()));
						} else {
							ea.setMath(SBMLutilities.myParseFormula(assign[i].split(":=")[1].split(";")[0].trim()));
						}
						if (p == null && var.endsWith("_"+GlobalConstants.RATE)) {
							p = bioModel.getSBMLDocument().getModel().createParameter();
							p.setId(var);
							p.setConstant(false);
							p.setValue(0);
							RateRule r = bioModel.getSBMLDocument().getModel().createRateRule();
							SBMLutilities.setMetaId(r, GlobalConstants.RULE+"_" + var);
							r.setVariable(var.replace("_" + GlobalConstants.RATE,""));
							r.setMath(SBMLutilities.myParseFormula(var));
						}
						error = checkEventAssignmentUnits(ea);
						if (error) break;
					}
					EAdimID = new String[]{""};
					EAdex = new String[]{""};
					EAdimensionIds = new String[]{""};
					for (int i = 0; i < placeAssign.length; i++) {
						EventAssignment ea = e.createEventAssignment();
						ea.setVariable(placeAssign[i].split(" ")[0]);
						String left = placeAssign[i].split(":=")[0].trim();
						String rightSide = placeAssign[i].split(":=")[1].split(";")[1];
						EAdimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), left, false);
						if(EAdimID!=null){
							EAdimensionIds = SBMLutilities.getDimensionIds("e",EAdimID.length-1);
							String variableId = EAdimID[0].trim();
							if (variableId.endsWith("_"+GlobalConstants.RATE)) {
								variableId = variableId.replace("_"+GlobalConstants.RATE, "");
							}
							SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), variableId);
							EAdex = Utils.checkIndices(rightSide, variable, bioModel.getSBMLDocument(), dimensionIds, "variable", EAdimID, dimensionIds, dimID);
							error = (EAdex==null);
						}
						else{
							error = true;
						}
						if (error) break;
						SBMLutilities.createDimensions(ea, EAdimensionIds, EAdimID);
						SBMLutilities.addIndices(ea, "variable", EAdex, 1);
						ea.setMath(SBMLutilities.myParseFormula(placeAssign[i].split(":=")[1].split(";")[0].trim()));
						error = checkEventAssignmentUnits(ea);
						if (error) break;
					}
					if (!error) {
						if (eventDelay.getText().trim().equals("")) {
							e.unsetDelay();
						}
						else {
							String oldDelayStr = "";
							if (e.isSetDelay()) {
								oldDelayStr = SBMLutilities.myFormulaToString(e.getDelay().getMath());
							}
							e.createDelay();
							e.getDelay().setMath(bioModel.addBooleans(eventDelay.getText().trim()));
							error = checkEventDelayUnits(e.getDelay());
							if (error) {
								if (oldDelayStr.equals("")) {
									e.unsetDelay();
								}
								else {
									e.createDelay();
									e.getDelay().setMath(SBMLutilities.myParseFormula(oldDelayStr));
								}
							}
						}
					}
					if (!error) {
						if (eventPriority.getText().trim().equals("")) {
							e.unsetPriority();
						}
						else {
							e.createPriority();
							e.getPriority().setMath(bioModel.addBooleans(eventPriority.getText().trim()));
						}
					}
					if (!error) {
						e.createTrigger();
						if (!persistentTrigger.isSelected()) {
							e.getTrigger().setPersistent(false);
							ASTNode triggerMath = bioModel.addBooleans(eventTrigger.getText().trim());
							if (!isTextual) {
								for (int j = 0; j < presetPlaces.size(); j++) {
									triggerMath = SBMLutilities.addPreset(triggerMath, presetPlaces.get(j));
								}
							}
							e.getTrigger().setMath(triggerMath);
							if (isTransition) {
								Rule r = bioModel.getSBMLDocument().getModel().getRule(GlobalConstants.TRIGGER + "_" + e.getId());
								if (r != null) {
									r.removeFromParent();
								}
								Parameter p = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.TRIGGER + "_" + e.getId());
								if (p != null) {
									p.removeFromParent();
								}
							}
						}
						else {
							if (isTransition) {
								e.getTrigger().setPersistent(false);
								ASTNode leftChild = bioModel.addBooleans(eventTrigger.getText().trim());
								if (!isTextual) {
									for (int j = 0; j < presetPlaces.size(); j++) {
										leftChild = SBMLutilities.addPreset(leftChild, presetPlaces.get(j));
									}
								}
								ASTNode rightChild = SBMLutilities.myParseFormula("eq(" + GlobalConstants.TRIGGER + "_" + e.getId() + ",1)");
								if (!isTextual) {
									for (int j = 0; j < presetPlaces.size(); j++) {
										rightChild = SBMLutilities.addPreset(rightChild, presetPlaces.get(j));
									}
								}
								ASTNode ruleMath = SBMLutilities.myParseFormula("piecewise(1,or(" + SBMLutilities.myFormulaToString(leftChild) + "," + 
										SBMLutilities.myFormulaToString(rightChild) + "),0)");
								Parameter p = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.TRIGGER + "_" + e.getId());
								if (p == null) {
									p = bioModel.getSBMLDocument().getModel().createParameter();
									p.setId(GlobalConstants.TRIGGER + "_" + e.getId());
									p.setConstant(false);
									p.setValue(0);
								}
								Rule r = bioModel.getSBMLDocument().getModel().getRule(GlobalConstants.TRIGGER + "_" + e.getId());
								if (r == null) {
									r = bioModel.getSBMLDocument().getModel().createAssignmentRule();
									SBMLutilities.setVariable(r, GlobalConstants.TRIGGER + "_" + e.getId());
								}
								SBMLutilities.setMetaId(r, GlobalConstants.TRIGGER + "_" + GlobalConstants.RULE+"_"+e.getId());
								r.setMath(ruleMath);
								ASTNode triggerMath = SBMLutilities.myParseFormula(GlobalConstants.TRIGGER + "_" + e.getId());
								if (!isTextual) {
									for (int j = 0; j < presetPlaces.size(); j++) {
										triggerMath = SBMLutilities.addPreset(triggerMath, presetPlaces.get(j));
									}
								}
								e.getTrigger().setMath(triggerMath);
							} else {
								e.getTrigger().setPersistent(true);
								ASTNode triggerMath = bioModel.addBooleans(eventTrigger.getText().trim());
								if (!isTextual) {
									for (int j = 0; j < presetPlaces.size(); j++) {
										triggerMath = SBMLutilities.addPreset(triggerMath, presetPlaces.get(j));
									}
								}
								e.getTrigger().setMath(triggerMath);
							}
						}
						if (!initialTrigger.isSelected()) {
							e.getTrigger().setInitialValue(false);
						}
						else {
							e.getTrigger().setInitialValue(true);
						}
						if (dimID==null || dimID[0].trim().equals("")) {
							e.unsetId();
						}
						else {
							e.setId(dimID[0].trim());
						}
						if (eventName.getText().trim().equals("")) {
							e.unsetName();
						}
						else {
							e.setName(eventName.getText().trim());
						}
						if (failTransition.isSelected()) {
							Parameter p = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.FAIL);
							if (p==null) {
								p = bioModel.getSBMLDocument().getModel().createParameter();
								p.setId(GlobalConstants.FAIL);
								p.setSBOTerm(GlobalConstants.SBO_BOOLEAN);
								p.setConstant(false);
								p.setValue(0);
								Constraint c = bioModel.getSBMLDocument().getModel().createConstraint();
								SBMLutilities.setMetaId(c, GlobalConstants.FAIL_TRANSITION);
								/*
								SBMLutilities.createFunction(bioModel.getSBMLDocument().getModel(), "G", "Globally Property", 
										"lambda(t,x,or(not(t),x))");
										*/
								c.setMath(SBMLutilities.myParseFormula("eq("+GlobalConstants.FAIL+",0)"));
							}
							EventAssignment ea = e.getListOfEventAssignments().get(GlobalConstants.FAIL);
							if (ea==null) {
								ea = e.createEventAssignment();
								ea.setVariable(GlobalConstants.FAIL);
								ea.setMath(SBMLutilities.myParseFormula("piecewise(1,true,0)"));
							}
						} else {
							EventAssignment ea = e.getListOfEventAssignments().get(GlobalConstants.FAIL);
							if (ea != null) {
								ea.removeFromParent();
							}
						}
						SBMLutilities.createDimensions(e, dimensionIds, dimID);
						Port port = bioModel.getPortByIdRef(selectedID);
						if (port!=null) {
							if (onPort.isSelected()) {
								port.setId(GlobalConstants.EVENT+"__"+e.getId());
								port.setIdRef(e.getId());
								SBMLutilities.cloneDimensionAddIndex(e, port, "comp:idRef");
							} else {
								bioModel.getSBMLCompModel().removePort(port);
							}
						} else {
							if (onPort.isSelected()) {
								port = bioModel.getSBMLCompModel().createPort();
								port.setId(GlobalConstants.EVENT+"__"+e.getId());
								port.setIdRef(e.getId());
								SBMLutilities.cloneDimensionAddIndex(e, port, "comp:idRef");
							}
						}
						if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
							e.unsetSBOTerm();
						} else {
							e.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
						}
						int index = events.getSelectedIndex();
						ev[index] = e.getId();
						for (int i = 1; dimID!=null && i < dimID.length; i++) {
							ev[index] += "[" + dimID[i] + "]";
						}
						edu.utah.ece.async.dataModels.biomodel.util.Utility.sort(ev);
						events.setListData(ev);
						events.setSelectedIndex(index);
						modelEditor.makeUndoPoint();
					}
					//edit dynamic process
					if (!error) {
						if (!((String)dynamicProcess.getSelectedItem()).equals("none")) {
							if(!AnnotationUtility.setDynamicAnnotation(e, (String)dynamicProcess.getSelectedItem()))
					     {
				        JOptionPane.showMessageDialog(Gui.frame, "Invalid XML Operation", "Error occurred while annotating SBML element " 
				            + SBMLutilities.getId(e), JOptionPane.ERROR_MESSAGE); 
				        
				      }
						}
						else {
							AnnotationUtility.removeDynamicAnnotation(e);
						}
					}
					else {
						while (e.getEventAssignmentCount() > 0) {
							e.getListOfEventAssignments().remove(0);
						}
						for (int i = 0; i < origAssign.length; i++) {
							EventAssignment ea = e.createEventAssignment();
							ea.setVariable(origAssign[i].split(" ")[0]);
							String[] rightSide = origAssign[i].split(":=")[1].split(",");
							ea.setMath(SBMLutilities.myParseFormula(rightSide[0].trim()));
						}
					}
				} //end if option is "ok"
				//add event
				else {
					JList add = new JList();
					e = bioModel.getSBMLDocument().getModel().createEvent();
					if (isTransition) {
						e.setSBOTerm(GlobalConstants.SBO_PETRI_NET_TRANSITION);
					}
					e.setUseValuesFromTriggerTime(assignTime.isSelected());
					e.createTrigger();
					if (dimID!=null && !dimID[0].trim().equals("")) {
						e.setId(dimID[0].trim());
					}
					bioModel.setMetaIDIndex(
							SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), e, bioModel.getMetaIDIndex()));
					if (!eventName.getText().trim().equals("")) {
						e.setName(eventName.getText().trim());
					}
					if (!persistentTrigger.isSelected()) {
						e.getTrigger().setPersistent(false);
						e.getTrigger().setMath(bioModel.addBooleans(eventTrigger.getText().trim()));
					}
					else {
						if (isTransition) {
							e.getTrigger().setPersistent(false);
							ASTNode leftChild = bioModel.addBooleans(eventTrigger.getText().trim());
							if (!isTextual) {
								for (int j = 0; j < presetPlaces.size(); j++) {
									leftChild = SBMLutilities.addPreset(leftChild, presetPlaces.get(j));
								}
							}
							ASTNode rightChild = SBMLutilities.myParseFormula("eq(" + GlobalConstants.TRIGGER + "_" + e.getId() + ",1)");
							if (!isTextual) {
								for (int j = 0; j < presetPlaces.size(); j++) {
									rightChild = SBMLutilities.addPreset(rightChild, presetPlaces.get(j));
								}
							}
							ASTNode ruleMath = SBMLutilities.myParseFormula("piecewise(1,or(" + SBMLutilities.myFormulaToString(leftChild) + "," + 
									SBMLutilities.myFormulaToString(rightChild) + "),0)");
							Parameter p = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.TRIGGER + "_" + e.getId());
							if (p == null) {
								p = bioModel.getSBMLDocument().getModel().createParameter();
								p.setId(GlobalConstants.TRIGGER + "_" + e.getId());
								p.setConstant(false);
								p.setValue(0);
							}
							Rule r = bioModel.getSBMLDocument().getModel().getRule(GlobalConstants.TRIGGER + "_" + e.getId());
							if (r == null) {
								r = bioModel.getSBMLDocument().getModel().createAssignmentRule();
								SBMLutilities.setVariable(r, GlobalConstants.TRIGGER + "_" + e.getId());
							}
							SBMLutilities.setMetaId(r, GlobalConstants.TRIGGER + "_" + GlobalConstants.RULE+"_"+e.getId());
							r.setMath(ruleMath);
							ASTNode triggerMath = SBMLutilities.myParseFormula(GlobalConstants.TRIGGER + "_" + e.getId());
							if (!isTextual) {
								for (int j = 0; j < presetPlaces.size(); j++) {
									triggerMath = SBMLutilities.addPreset(triggerMath, presetPlaces.get(j));
								}
							}
							e.getTrigger().setMath(triggerMath);
						} else {
							e.getTrigger().setPersistent(true);
							e.getTrigger().setMath(bioModel.addBooleans(eventTrigger.getText().trim()));
						}
					}
					if (!initialTrigger.isSelected()) {
						e.getTrigger().setInitialValue(false);
					}
					else {
						e.getTrigger().setInitialValue(true);
					}
					if (!eventPriority.getText().trim().equals("")) {
						e.createPriority();
						e.getPriority().setMath(bioModel.addBooleans(eventPriority.getText().trim()));
					}
					if (!eventDelay.getText().trim().equals("")) {
						e.createDelay();
						e.getDelay().setMath(bioModel.addBooleans(eventDelay.getText().trim()));
						error = checkEventDelayUnits(e.getDelay());
					}
					if (!error) {
						String[] EAdimID = new String[]{""};
						String[] EAdex = new String[]{""};
						String[] EAdimensionIds = new String[]{""};
						for (int i = 0; i < assign.length; i++) {
							EventAssignment ea = e.createEventAssignment();
							String var = assign[i].split(" ")[0];
							ea.setVariable(var);
							String left = assign[i].split(":=")[0].trim();
							String rightSide = assign[i].split(":=")[1].split(";")[1].trim();
							EAdimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), left, false);
							if(EAdimID!=null){
								EAdimensionIds = SBMLutilities.getDimensionIds("e",EAdimID.length-1);
								String variableId = EAdimID[0].trim();
								if (variableId.endsWith("_"+GlobalConstants.RATE)) {
									variableId = variableId.replace("_"+GlobalConstants.RATE, "");
								}
								SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), variableId);
								EAdex = Utils.checkIndices(rightSide, variable, bioModel.getSBMLDocument(), EAdimensionIds, "variable", EAdimID, dimensionIds, dimID);
							}
							if(!error){
								SBMLutilities.createDimensions(ea, EAdimensionIds, EAdimID);
								SBMLutilities.addIndices(ea, "variable", EAdex, 1);
								if (var.endsWith("\'")) {
									var = "rate_" + var.replace("\'","");
								}
								ea.setVariable(var);
								Parameter p = bioModel.getSBMLDocument().getModel().getParameter(var);
								if (p != null && SBMLutilities.isBoolean(p)) {
									ea.setMath(bioModel.addBooleanAssign(assign[i].split(":=")[1].split(";")[0].trim()));
								} else {
									ea.setMath(SBMLutilities.myParseFormula(assign[i].split(":=")[1].split(";")[0].trim()));
								}
								if (p == null && var.endsWith("_" + GlobalConstants.RATE)) {
									p = bioModel.getSBMLDocument().getModel().createParameter();
									p.setId(var);
									p.setConstant(false);
									p.setValue(0);
									RateRule r = bioModel.getSBMLDocument().getModel().createRateRule();
									SBMLutilities.setMetaId(r, GlobalConstants.RULE+"_" + var);
									r.setVariable(var.replace("_"+GlobalConstants.RATE,""));
									r.setMath(SBMLutilities.myParseFormula(var));
								}
								error = checkEventAssignmentUnits(ea);
								if (error)
									break;
							}
						}
					}
					//add dynamic process
					if (!error) {
						if (!((String)dynamicProcess.getSelectedItem()).equals("none")) {
							AnnotationUtility.setDynamicAnnotation(e, (String)dynamicProcess.getSelectedItem());
						}
						if (failTransition.isSelected()) {
							Parameter p = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.FAIL);
							if (p==null) {
								p = bioModel.getSBMLDocument().getModel().createParameter();
								p.setId(GlobalConstants.FAIL);
								p.setSBOTerm(GlobalConstants.SBO_BOOLEAN);
								p.setConstant(false);
								p.setValue(0);
								Constraint c = bioModel.getSBMLDocument().getModel().createConstraint();
								SBMLutilities.setMetaId(c, GlobalConstants.FAIL_TRANSITION);
								/*
								SBMLutilities.createFunction(bioModel.getSBMLDocument().getModel(), "G", "Globally Property", 
										"lambda(t,x,or(not(t),x))");
										*/
								c.setMath(SBMLutilities.myParseFormula("eq("+GlobalConstants.FAIL+",0)"));
							}
							EventAssignment ea = e.getListOfEventAssignments().get(GlobalConstants.FAIL);
							if (ea==null) {
								ea = e.createEventAssignment();
								ea.setVariable(GlobalConstants.FAIL);
								ea.setMath(SBMLutilities.myParseFormula("piecewise(1,true,0)"));
							}
						} else {
							EventAssignment ea = e.getListOfEventAssignments().get(GlobalConstants.FAIL);
							if (ea != null) {
								ea.removeFromParent();
							}
						}
						SBMLutilities.createDimensions(e, dimensionIds, dimID);
						if (onPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.EVENT+"__"+e.getId());
							port.setIdRef(e.getId());
							SBMLutilities.cloneDimensionAddIndex(e, port, "comp:idRef");
						}
					}
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						e.unsetSBOTerm();
					} else {
						e.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}

					String eventEntry = e.getId();
					for (int i = 1; dimID!=null && i < dimID.length; i++) {
						eventEntry += "[" + dimID[i] + "]";
					}
					Object[] adding = { eventEntry };
					// Object[] adding = {
					// myFormulaToString(e.getTrigger().getMath()) };
					add.setListData(adding);
					add.setSelectedIndex(0);
					events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					String[] ev = new String[events.getModel().getSize()];
					for (int i = 0; i < events.getModel().getSize(); i++) {
						ev[i] = events.getModel().getElementAt(i).toString();
					}
					adding = Utility.add(ev, events, add);
					ev = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						ev[i] = (String) adding[i];
					}
					edu.utah.ece.async.dataModels.biomodel.util.Utility.sort(ev);
					int index = events.getSelectedIndex();
					events.setListData(ev);
					events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (bioModel.getSBMLDocument().getModel().getEventCount() == 1) {
						events.setSelectedIndex(0);
					}
					else {
						events.setSelectedIndex(index);
					}
					if (error) {
						removeTheEvent(bioModel, SBMLutilities.myFormulaToString(e.getTrigger().getMath()));
					}
				}
				if (!error && !modelEditor.isParamsOnly()) {
					// Add SBOL annotation to event
					if (sbolField.getSBOLURIs().size() > 0) {
						if (!e.isSetMetaId() || e.getMetaId().equals(""))
							SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), e, 
									bioModel.getMetaIDIndex());
						SBOLAnnotation sbolAnnot = new SBOLAnnotation(e.getMetaId(), sbolField.getSBOLURIs(), 
								sbolField.getSBOLStrand());
						if(!AnnotationUtility.setSBOLAnnotation(e, sbolAnnot))
						{
						  JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error occurred while annotating SBML element "  + SBMLutilities.getId(e) + " with SBOL.", JOptionPane.ERROR_MESSAGE);}
					} else
						AnnotationUtility.removeSBOLAnnotation(e);
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return selected;
		}
		modelEditor.setDirty(true);
		if (dimID!=null && !dimID[0].equals("")) 
			return dimID[0].trim();
		return selected;
	}

	/**
	 * Check the units of an event delay
	 */
	private static boolean checkEventDelayUnits(Delay delay) {
		if (Gui.getCheckUndeclared()) {
			if (delay.containsUndeclaredUnits()) {
				JOptionPane.showMessageDialog(Gui.frame, "Event assignment delay contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (Gui.getCheckUnits()) {
			if (SBMLutilities.checkUnitsInEventDelay(delay)) {
				JOptionPane.showMessageDialog(Gui.frame, "Event delay should be units of time.", "Event Delay Not Time Units",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the units of an event assignment
	 */
	private boolean checkEventAssignmentUnits(EventAssignment assign) {
		if (Gui.getCheckUndeclared()) {
			if (assign.containsUndeclaredUnits()) {
				JOptionPane.showMessageDialog(Gui.frame, "Event assignment to " + assign.getVariable()
						+ " contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (Gui.getCheckUnits()) {
			if (SBMLutilities.checkUnitsInEventAssignment(bioModel.getSBMLDocument(), assign)) {
				JOptionPane.showMessageDialog(Gui.frame, "Units on the left and right-hand side for the event assignment " + assign.getVariable()
						+ " do not agree.", "Units Do Not Match", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Refresh events panel
	 */
	public void refreshEventsPanel() {
		Model model = bioModel.getSBMLDocument().getModel();
		ListOf<Event> listOfEvents = model.getListOfEvents();
		String[] ev = new String[model.getEventCount()];
		for (int i = 0; i < model.getEventCount(); i++) {
			org.sbml.jsbml.Event event = listOfEvents.get(i);
			if (!event.isSetId()) {
				String eventId = "event0";
				int en = 0;
				while (bioModel.isSIdInUse(eventId)) {
					en++;
					eventId = "event" + en;
				}
				event.setId(eventId);
			}
			ev[i] = event.getId() + SBMLutilities.getDimensionString(event);
		}
		edu.utah.ece.async.dataModels.biomodel.util.Utility.sort(ev);
		events.setListData(ev);
		events.setSelectedIndex(0);
	}
	
	/**
	 * Remove an event from a list and SBML gcm.getSBMLDocument()
	 * 
	 * @param events
	 *            a list of events
	 * @param gcm.getSBMLDocument()
	 *            an SBML gcm.getSBMLDocument() from which to remove the event
	 * @param usedIDs
	 *            a list of all IDs current in use
	 * @param ev
	 *            an array of all events
	 */
	private void removeEvent(JList events, BioModel gcm) {
		int index = events.getSelectedIndex();
		if (index != -1) {
			String selected = ((String) events.getSelectedValue()).split("\\[")[0];
			removeTheEvent(gcm, selected);
			events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			Utility.remove(events);
			events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < events.getModel().getSize()) {
				events.setSelectedIndex(index);
			}
			else {
				events.setSelectedIndex(index - 1);
			}
			modelEditor.setDirty(true);
			modelEditor.makeUndoPoint();
		}
	}

	/**
	 * Remove an event from an SBML gcm.getSBMLDocument()
	 * 
	 * @param gcm.getSBMLDocument()
	 *            the SBML gcm.getSBMLDocument() from which to remove the event
	 * @param selected
	 *            the event Id to remove
	 */
	public static void removeTheEvent(BioModel bioModel, String selected) {
		ListOf<Event> EL = bioModel.getSBMLDocument().getModel().getListOfEvents();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getEventCount(); i++) {
			org.sbml.jsbml.Event E = EL.get(i);
			if (E.getId().equals(selected)) {
				EL.remove(i);
			}
		}
		for (int i = 0; i < bioModel.getSBMLCompModel().getListOfPorts().size(); i++) {
			Port port = bioModel.getSBMLCompModel().getListOfPorts().get(i);
			if (port.isSetIdRef() && port.getIdRef().equals(selected)) {
				bioModel.getSBMLCompModel().getListOfPorts().remove(i);
				break;
			}
		}
		Layout layout = bioModel.getLayout();
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+selected)!=null) {
			layout.getListOfAdditionalGraphicalObjects().remove(GlobalConstants.GLYPH+"__"+selected);
		}
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
			layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+selected);
		}
	}

	/**
	 * Creates a frame used to edit event assignments or create new ones.
	 * 
	 */
	private void eventAssignEditor(BioModel bioModel, JList eventAssign, String option) {
		if (option.equals("OK") && eventAssign.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No event assignment selected.", "Must Select an Event Assignment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel eventAssignPanel = new JPanel(new GridLayout(3,1));
		JPanel topEAPanel = new JPanel();
		JPanel northEAPanel = new JPanel();
		JPanel southEAPanel = new JPanel();
		JLabel idLabel = new JLabel("Variable:");
		JLabel indexLabel = new JLabel("Indices:");
		JLabel eqnLabel = new JLabel("Assignment:");
		eaID = new JComboBox();
		eaID.addActionListener(this);
		iIndex = new JTextField(20);
		
		String selected;
		String[] assign = new String[eventAssign.getModel().getSize()];
		for (int i = 0; i < eventAssign.getModel().getSize(); i++) {
			assign[i] = eventAssign.getModel().getElementAt(i).toString();
		}
		if (option.equals("OK")) {
			selected = ((String) eventAssign.getSelectedValue()).split(" ")[0];
		}
		else {
			selected = "";
		}
		Model model = bioModel.getSBMLDocument().getModel();
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			String id = model.getCompartment(i).getId();
			if (!(model.getCompartment(i).getConstant())) {
				if (keepVarEvent(bioModel, assign, selected, id)) {
					eaID.addItem(id);
				}
			}
		}
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter p = model.getParameter(i);
			if ((!isTextual && SBMLutilities.isPlace(p))||p.getId().endsWith("_"+GlobalConstants.RATE)) continue;
			if (p.getId().equals(GlobalConstants.FAIL)) continue;
			String id = p.getId();
			if (!p.getConstant()) {
				if (keepVarEvent(bioModel, assign, selected, id)) {
					eaID.addItem(id);
				}
				if (isTransition && !SBMLutilities.isBoolean(p) && !SBMLutilities.isPlace(p)) {
					if (keepVarEvent(bioModel, assign, selected, id+"_"+GlobalConstants.RATE)) {
						eaID.addItem(id+"_"+GlobalConstants.RATE);
					}
				}
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			String id = model.getSpecies(i).getId();
			if (!(model.getSpecies(i).getConstant())) {
				if (keepVarEvent(bioModel, assign, selected, id)) {
					eaID.addItem(id);
				}
			}
		}
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			for (int j = 0; j < reaction.getReactantCount(); j++) {
				SpeciesReference reactant = reaction.getReactant(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals("")) && !(reactant.getConstant())) {
					String id = reactant.getId();
					if (keepVarEvent(bioModel, assign, selected, id)) {
						eaID.addItem(id);
					}
				}
			}
			for (int j = 0; j < reaction.getProductCount(); j++) {
				SpeciesReference product = reaction.getProduct(j);
				if ((product.isSetId()) && (!product.getId().equals("")) && !(product.getConstant())) {
					String id = product.getId();
					if (keepVarEvent(bioModel, assign, selected, id)) {
						eaID.addItem(id);
					}
				}
			}
		}
		JTextField eqn = new JTextField(30);
		// From the event assignments list to the event assignment window
		// The listed string is: X[<first index>][<second index>] := <math>, Dim0 = <first dimension>, Dim1 = 
		// <second dimension>
		if (option.equals("OK")) {
			String selectAssign = ((String) eventAssign.getSelectedValue());
			eaID.setSelectedItem(selectAssign.split(" ")[0]);
			String left = selectAssign.split(":=")[0].trim();
			EAdimensions.setText(left.substring(selectAssign.split(" ")[0].length()).trim());
			String rightSide = selectAssign.split(":=")[1].split(";")[1];
			eqn.setText(selectAssign.split(":=")[1].split(";")[0].trim());
			iIndex.setText(rightSide.trim());
		}
		topEAPanel.add(new JLabel("Dimension Size Ids:"));
		topEAPanel.add(EAdimensions);
		northEAPanel.add(idLabel);
		northEAPanel.add(eaID);
		northEAPanel.add(indexLabel);
		northEAPanel.add(iIndex);
		southEAPanel.add(eqnLabel);
		southEAPanel.add(eqn);
		eventAssignPanel.add(topEAPanel);
		eventAssignPanel.add(northEAPanel);
		eventAssignPanel.add(southEAPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, eventAssignPanel, "Event Asssignment Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		String[] EAdimID = new String[]{""};
		String[] EAdimensionIds = new String[]{""};
		String[] EAdex = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if (eqn.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(Gui.frame, "Event assignment is missing.", "Enter Assignment", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else if (SBMLutilities.myParseFormula(eqn.getText().trim()) == null) {
				JOptionPane.showMessageDialog(Gui.frame, "Event assignment is not valid.", "Enter Valid Assignment", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else {
				String[] dimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), eventID.getText(), false);
				String[] dimensionIds = null;
				if(dimID!=null){
					dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
				}
				EAdimID = Utils.checkSizeParameters(bioModel.getSBMLDocument(), EAdimensions.getText(), true);
				if(EAdimID!=null){
					EAdimensionIds = SBMLutilities.getDimensionIds("e",EAdimID.length-1);
					String variableId = (String)eaID.getSelectedItem();
					if (variableId.endsWith("_"+GlobalConstants.RATE)) {
						variableId = variableId.replace("_"+GlobalConstants.RATE, "");
					}
					SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), variableId);
					EAdex = Utils.checkIndices(iIndex.getText(), variable, bioModel.getSBMLDocument(), EAdimensionIds, "variable", EAdimID, dimensionIds, dimID);
					error = (EAdex==null);
					if (!error) {	
						ArrayList<String> meshDimensionIds = new ArrayList<String>();
						if (dimensionIds!=null) {
							meshDimensionIds.addAll(Arrays.asList(dimensionIds));
						}
						if (EAdimensionIds!=null) {
							meshDimensionIds.addAll(Arrays.asList(EAdimensionIds));
						}
						error = Utils.displayinvalidVariables("Event assignment", bioModel.getSBMLDocument(), 
								meshDimensionIds.toArray(new String[meshDimensionIds.size()]), eqn.getText().trim(), "", false);
					}
				}
				else{
					error = true;
				}
				if (!error) {
					Parameter p = bioModel.getSBMLDocument().getModel().getParameter((String)eaID.getSelectedItem());
					ASTNode assignMath = SBMLutilities.myParseFormula(eqn.getText().trim());
					if (p != null && SBMLutilities.isBoolean(p)) {
						assignMath = bioModel.addBooleanAssign(eqn.getText().trim());
					} 
					error = Utils.checkNumFunctionArguments(bioModel.getSBMLDocument(), 
							SBMLutilities.myParseFormula(eqn.getText().trim()));
					if (!error) {
						error = Utils.checkFunctionArgumentTypes(bioModel.getSBMLDocument(), assignMath);
					}
					if (!error) {
						if (p != null && SBMLutilities.isBoolean(p)) {
							if (!SBMLutilities.returnsBoolean(SBMLutilities.myParseFormula(eqn.getText().trim()), bioModel.getSBMLDocument().getModel())) {
								JOptionPane.showMessageDialog(Gui.frame, "Event assignment must evaluate to a Boolean.", "Boolean Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						} else {
							if (SBMLutilities.returnsBoolean(SBMLutilities.myParseFormula(eqn.getText().trim()), bioModel.getSBMLDocument().getModel())) {
								JOptionPane.showMessageDialog(Gui.frame, "Event assignment must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = eventAssign.getSelectedIndex();
					eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					assign = Utility.getList(assign, eventAssign);
					eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					// String for the indices
					String assignIndex = "; " + iIndex.getText();
					// String for the dimensions
					String dimens = " " + EAdimensions.getText();
					assign[index] = eaID.getSelectedItem() + dimens + " := " + eqn.getText().trim() 
							+ assignIndex;
					edu.utah.ece.async.dataModels.biomodel.util.Utility.sort(assign);
					eventAssign.setListData(assign);
					eventAssign.setSelectedIndex(index);
				}
				else {
					JList add = new JList();
					int index = eventAssign.getSelectedIndex();
					// String for the indices
					String assignIndex = "; " + iIndex.getText();
					// String for the dimensions
					String dimens = " " + EAdimensions.getText();
					Object[] adding = { eaID.getSelectedItem() + dimens + " := " + eqn.getText().trim()
							+ assignIndex };
					add.setListData(adding);
					add.setSelectedIndex(0);
					eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(assign, eventAssign, add);
					assign = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						assign[i] = (String) adding[i];
					}
					edu.utah.ece.async.dataModels.biomodel.util.Utility.sort(assign);
					eventAssign.setListData(assign);
					eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (adding.length == 1) {
						eventAssign.setSelectedIndex(0);
					}
					else {
						eventAssign.setSelectedIndex(index);
					}
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, eventAssignPanel, "Event Assignment Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Determines if a variable is already used in assignment rule or another
	 * event assignment
	 */
	private static boolean keepVarEvent(BioModel gcm, String[] assign, String selected, String id) {
		if (!selected.equals(id)) {
			for (int j = 0; j < assign.length; j++) {
				if (id.equals(assign[j].split(" ")[0])) {
					return false;
				}
			}
			ListOf<Rule> r = gcm.getSBMLDocument().getModel().getListOfRules();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getRuleCount(); i++) {
				Rule rule = r.get(i);
				if (rule.isAssignment() && SBMLutilities.getVariable(rule).equals(id))
					return false;
			}
		}
		return true;
	}

	/**
	 * Remove an event assignment
	 * 
	 * @param eventAssign
	 *            Jlist of event assignments for selected event
	 * @param assign
	 *            String array of event assignments for selected event
	 */
	private static void removeAssignment(JList eventAssign) {
		int index = eventAssign.getSelectedIndex();
		if (index != -1) {
			eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			Utility.remove(eventAssign);
			eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < eventAssign.getModel().getSize()) {
				eventAssign.setSelectedIndex(index);
			}
			else {
				eventAssign.setSelectedIndex(index - 1);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addEvent) {
			eventEditor("Add","",false);
			modelEditor.makeUndoPoint();
		}else if (e.getSource() == addTrans) {
			eventEditor("Add","",true);
			modelEditor.makeUndoPoint();
		}
		// if the edit event button is clicked
		else if (e.getSource() == editEvent) {
			if (events.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No event selected.", "Must Select an Event", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String selected = ((String) events.getSelectedValue()).split("\\[")[0];
			eventEditor("OK",selected,false);
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeEvent) {
			removeEvent(events, bioModel);
		}
		// if the add event assignment button is clicked
		else if (e.getSource() == addAssignment) {
		//else if (((JButton) e.getSource()).getText().equals("Add Assignment")) {
			eventAssignEditor(bioModel, eventAssign, "Add");
		}
		// if the edit event assignment button is clicked
		else if (e.getSource() == editAssignment) {
			eventAssignEditor(bioModel, eventAssign, "OK");
		}
		// if the remove event assignment button is clicked
		else if (e.getSource() == removeAssignment) {
			removeAssignment(eventAssign);
		}
		else if (e.getSource() == eaID){
			if (bioModel.isArray((String)eaID.getSelectedItem())) {
				iIndex.setEnabled(true);
			} else {
				iIndex.setText("");
				iIndex.setEnabled(false);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == events) {
				if (events.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No event selected.", "Must Select an Event", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String selected = ((String) events.getSelectedValue()).split("\\[")[0];
				eventEditor("OK",selected,false);
			}
			else if (e.getSource() == eventAssign) {
				eventAssignEditor(bioModel, eventAssign, "OK");
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
