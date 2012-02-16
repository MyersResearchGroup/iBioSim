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
import javax.swing.JCheckBox;
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

import org.sbml.libsbml.*;


/**
 * This is a class for creating SBML events.
 * 
 * @author Chris Myers
 * 
 */
public class Events extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addEvent, removeEvent, editEvent;

	private JList events; // JList of events

	private JList eventAssign; // JList of event assignments

	private SBMLDocument document;

	private ArrayList<String> usedIDs;

	private MutableBoolean dirty;

	private Gui biosim;

	/* Create event panel */
	public Events(Gui biosim, SBMLDocument document, ArrayList<String> usedIDs, MutableBoolean dirty) {
		super(new BorderLayout());
		this.document = document;
		this.usedIDs = usedIDs;
		this.biosim = biosim;
		this.dirty = dirty;
		Model model = document.getModel();
		addEvent = new JButton("Add Event");
		removeEvent = new JButton("Remove Event");
		editEvent = new JButton("Edit Event");
		events = new JList();
		eventAssign = new JList();
		ListOf listOfEvents = model.getListOfEvents();
		String[] ev = new String[(int) model.getNumEvents()];
		for (int i = 0; i < model.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) listOfEvents.get(i);
			if (!event.isSetId()) {
				String eventId = "event0";
				int en = 0;
				while (usedIDs.contains(eventId)) {
					en++;
					eventId = "event" + en;
				}
				usedIDs.add(eventId);
				event.setId(eventId);
			}
			ev[i] = event.getId();
		}
		JPanel addRem = new JPanel();
		addRem.add(addEvent);
		addRem.add(removeEvent);
		addRem.add(editEvent);
		addEvent.addActionListener(this);
		removeEvent.addActionListener(this);
		editEvent.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Events:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(events);
		Utility.sort(ev);
		events.setListData(ev);
		events.setSelectedIndex(0);
		events.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	};

	/**
	 * Creates a frame used to edit events or create new ones.
	 */
	private void eventEditor(Gui biosim, String option, JList events, SBMLDocument document, ArrayList<String> usedIDs, JList eventAssign) {
		String[] origAssign = null;
		String[] assign = new String[0];
		if (option.equals("OK") && events.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No event selected.", "Must Select an Event", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int index = events.getSelectedIndex();
		JPanel eventPanel = new JPanel(new BorderLayout());
		// JPanel evPanel = new JPanel(new GridLayout(2, 2));
		JPanel evPanel = new JPanel(new GridLayout(9, 2));
		JLabel IDLabel = new JLabel("ID:");
		JLabel NameLabel = new JLabel("Name:");
		JLabel triggerLabel = new JLabel("Trigger:");
		JLabel delayLabel = new JLabel("Delay:");
		JLabel priorityLabel = new JLabel("Priority:");
		JLabel assignTimeLabel = new JLabel("Use values at trigger time:");
		JLabel persistentTriggerLabel = new JLabel("Trigger is persistent:");
		JLabel initialTriggerLabel = new JLabel("Trigger initially true:");
		JLabel dynamicProcessLabel = new JLabel("Dynamic Process:");
		
		JTextField eventID = new JTextField(12);
		JTextField eventName = new JTextField(12);
		JTextField eventTrigger = new JTextField(12);
		JTextField eventDelay = new JTextField(12);
		JTextField eventPriority = new JTextField(12);
		JCheckBox assignTime = new JCheckBox("");
		JCheckBox persistentTrigger = new JCheckBox("");
		JCheckBox initialTrigger = new JCheckBox("");
		JComboBox dynamicProcess = new JComboBox(new String[] {"none","Division","Death"});
		
		JPanel eventAssignPanel = new JPanel(new BorderLayout());
		JPanel addEventAssign = new JPanel();
		JButton addAssignment = new JButton("Add Assignment");
		JButton removeAssignment = new JButton("Remove Assignment");
		JButton editAssignment = new JButton("Edit Assignment");
		addEventAssign.add(addAssignment);
		addEventAssign.add(removeAssignment);
		addEventAssign.add(editAssignment);
		addAssignment.addActionListener(this);
		removeAssignment.addActionListener(this);
		editAssignment.addActionListener(this);
		JLabel eventAssignLabel = new JLabel("List of Event Assignments:");
		eventAssign.removeAll();
		eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(eventAssign);
		int Eindex = -1;
		String selectedID = "";
		if (option.equals("OK")) {
			String selected = ((String) events.getSelectedValue());
			ListOf e = document.getModel().getListOfEvents();
			for (int i = 0; i < document.getModel().getNumEvents(); i++) {
				org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) e.get(i);
				if (event.getId().equals(selected)) {
					Eindex = i;
					eventID.setText(event.getId());
					selectedID = event.getId();
					eventName.setText(event.getName());
					eventTrigger.setText(SBMLutilities.myFormulaToString(event.getTrigger().getMath()));
					if (event.isSetDelay()) {
						ASTNode delay = event.getDelay().getMath();
						if ((delay.getType() == libsbml.AST_FUNCTION) && (delay.getName().equals("priority"))) {
							eventDelay.setText(SBMLutilities.myFormulaToString(delay.getLeftChild()));
							eventPriority.setText(SBMLutilities.myFormulaToString(delay.getRightChild()));
						}
						else {
							eventDelay.setText(SBMLutilities.myFormulaToString(delay));
						}
					}
					if (event.getUseValuesFromTriggerTime()) {
						assignTime.setSelected(true);
					}
					if (event.getTrigger().getAnnotationString().contains("<TriggerCanBeDisabled/>")) {
						persistentTrigger.setSelected(false);
					}
					else {
						persistentTrigger.setSelected(true);
					}
					if (event.getTrigger().getAnnotationString().contains("<TriggerInitiallyFalse/>")) {
						initialTrigger.setSelected(false);
					}
					else {
						initialTrigger.setSelected(true);
					}

					/* new libsbml */
					if (event.isSetPriority()) {
						eventPriority.setText(SBMLutilities.myFormulaToString(event.getPriority().getMath()));
					}
					if (event.getTrigger().isSetPersistent()) {
						persistentTrigger.setSelected(event.getTrigger().getPersistent());
					}
					if (event.getTrigger().isSetInitialValue()) {
						initialTrigger.setSelected(event.getTrigger().getInitialValue());
					}

					assign = new String[(int) event.getNumEventAssignments()];
					origAssign = new String[(int) event.getNumEventAssignments()];
					for (int j = 0; j < event.getNumEventAssignments(); j++) {
						assign[j] = event.getEventAssignment(j).getVariable() + " = "
								+ SBMLutilities.myFormulaToString(event.getEventAssignment(j).getMath());
						origAssign[j] = event.getEventAssignment(j).getVariable() + " = "
								+ SBMLutilities.myFormulaToString(event.getEventAssignment(j).getMath());
					}
				}
			}
		}
		else {
			String eventId = "event0";
			int en = 0;
			while (usedIDs.contains(eventId)) {
				en++;
				eventId = "event" + en;
			}
			eventID.setText(eventId);
		}
		Utility.sort(assign);
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
		evPanel.add(triggerLabel);
		evPanel.add(eventTrigger);
		evPanel.add(delayLabel);
		evPanel.add(eventDelay);
		evPanel.add(priorityLabel);
		evPanel.add(eventPriority);
		evPanel.add(assignTimeLabel);
		evPanel.add(assignTime);
		evPanel.add(persistentTriggerLabel);
		evPanel.add(persistentTrigger);
		evPanel.add(initialTriggerLabel);
		evPanel.add(initialTrigger);
		evPanel.add(dynamicProcessLabel);
		evPanel.add(dynamicProcess);
		eventPanel.add(evPanel, "North");
		eventPanel.add(eventAssignPanel, "South");
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, "Event Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			assign = new String[eventAssign.getModel().getSize()];
			for (int i = 0; i < eventAssign.getModel().getSize(); i++) {
				assign[i] = eventAssign.getModel().getElementAt(i).toString();
			}
			error = SBMLutilities.checkID(document, usedIDs, eventID.getText().trim(), selectedID, false);
			if (eventTrigger.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(Gui.frame, "Event must have a trigger formula.", "Enter Trigger Formula", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else if (SBMLutilities.myParseFormula(eventTrigger.getText().trim()) == null) {
				JOptionPane.showMessageDialog(Gui.frame, "Trigger formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else if (!SBMLutilities.myParseFormula(eventTrigger.getText().trim()).isBoolean()) {
				JOptionPane
						.showMessageDialog(Gui.frame, "Trigger formula must be of type Boolean.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
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
			else if (assign.length == 0) {
				JOptionPane.showMessageDialog(Gui.frame, "Event must have at least one event assignment.", "Event Assignment Needed",
						JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else {
				ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(document, eventTrigger.getText().trim(), "", false);
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
					message = "Event trigger contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(300, 300));
					scrolls.setPreferredSize(new Dimension(300, 300));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (!error) {
					invalidVars = SBMLutilities.getInvalidVariables(document, eventDelay.getText().trim(), "", false);
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
						message = "Event delay contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
						JTextArea messageArea = new JTextArea(message);
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(300, 300));
						scrolls.setPreferredSize(new Dimension(300, 300));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error) {
					invalidVars = SBMLutilities.getInvalidVariables(document, eventPriority.getText().trim(), "", false);
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
						message = "Event priority contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
						JTextArea messageArea = new JTextArea(message);
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(300, 300));
						scrolls.setPreferredSize(new Dimension(300, 300));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error) {
					error = SBMLutilities.checkNumFunctionArguments(document, SBMLutilities.myParseFormula(eventTrigger.getText().trim()));
				}
				if ((!error) && (!eventDelay.getText().trim().equals(""))) {
					error = SBMLutilities.checkNumFunctionArguments(document, SBMLutilities.myParseFormula(eventDelay.getText().trim()));
					if (!error) {
						if (SBMLutilities.myParseFormula(eventDelay.getText().trim()).isBoolean()) {
							JOptionPane.showMessageDialog(Gui.frame, "Event delay must evaluate to a number.", "Number Expected",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
				}
				if ((!error) && (!eventPriority.getText().trim().equals(""))) {
					error = SBMLutilities.checkNumFunctionArguments(document, SBMLutilities.myParseFormula(eventPriority.getText().trim()));
					if (!error) {
						if (SBMLutilities.myParseFormula(eventPriority.getText().trim()).isBoolean()) {
							JOptionPane.showMessageDialog(Gui.frame, "Event priority must evaluate to a number.", "Number Expected",
									JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
				}
			}
			if (!error) {
				//edit event
				if (option.equals("OK")) {
					events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					String[] ev = new String[events.getModel().getSize()];
					for (int i = 0; i < events.getModel().getSize(); i++) {
						ev[i] = events.getModel().getElementAt(i).toString();
					}
					events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					org.sbml.libsbml.Event e = (org.sbml.libsbml.Event) (document.getModel().getListOfEvents()).get(Eindex);
					e.setUseValuesFromTriggerTime(assignTime.isSelected());
					while (e.getNumEventAssignments() > 0) {
						e.getListOfEventAssignments().remove(0);
					}
					for (int i = 0; i < assign.length; i++) {
						EventAssignment ea = e.createEventAssignment();
						ea.setVariable(assign[i].split(" ")[0]);
						ea.setMath(SBMLutilities.myParseFormula(assign[i].split("=")[1].trim()));
						error = checkEventAssignmentUnits(biosim, document, ea);
						if (error)
							break;
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
							e.getDelay().setMath(SBMLutilities.myParseFormula(eventDelay.getText().trim()));
							error = checkEventDelayUnits(biosim, document, e.getDelay());
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
							e.getPriority().setMath(SBMLutilities.myParseFormula(eventPriority.getText().trim()));
						}
					}
					if (!error) {
						e.createTrigger();
						if (!persistentTrigger.isSelected()) {
							e.getTrigger().setPersistent(false);
						}
						else {
							e.getTrigger().setPersistent(true);
						}
						if (!initialTrigger.isSelected()) {
							e.getTrigger().setInitialValue(false);
						}
						else {
							e.getTrigger().setInitialValue(true);
						}
						e.getTrigger().setMath(SBMLutilities.myParseFormula(eventTrigger.getText().trim()));
						if (eventID.getText().trim().equals("")) {
							e.unsetId();
						}
						else {
							e.setId(eventID.getText().trim());
						}
						if (eventName.getText().trim().equals("")) {
							e.unsetName();
						}
						else {
							e.setName(eventName.getText().trim());
						}
						for (int i = 0; i < usedIDs.size(); i++) {
							if (usedIDs.get(i).equals(selectedID)) {
								usedIDs.set(i, eventID.getText().trim());
							}
						}
						ev[index] = e.getId();
						Utility.sort(ev);
						events.setListData(ev);
						events.setSelectedIndex(index);
					}
					else {
						while (e.getNumEventAssignments() > 0) {
							e.getListOfEventAssignments().remove(0);
						}
						for (int i = 0; i < origAssign.length; i++) {
							EventAssignment ea = e.createEventAssignment();
							ea.setVariable(origAssign[i].split(" ")[0]);
							ea.setMath(SBMLutilities.myParseFormula(origAssign[i].split("=")[1].trim()));
						}
					}
				}
				//add event
				else {
					JList add = new JList();
					org.sbml.libsbml.Event e = document.getModel().createEvent();
					e.setUseValuesFromTriggerTime(assignTime.isSelected());
					e.createTrigger();
					if (!eventID.getText().trim().equals("")) {
						e.setId(eventID.getText().trim());
					}
					if (!eventName.getText().trim().equals("")) {
						e.setName(eventName.getText().trim());
					}
					if (!persistentTrigger.isSelected()) {
						e.getTrigger().setPersistent(false);
					}
					else {
						e.getTrigger().setPersistent(true);
					}
					if (!initialTrigger.isSelected()) {
						e.getTrigger().setInitialValue(false);
					}
					else {
						e.getTrigger().setInitialValue(true);
					}
					e.getTrigger().setMath(SBMLutilities.myParseFormula(eventTrigger.getText().trim()));
					if (!eventPriority.getText().trim().equals("")) {
						e.createPriority();
						e.getPriority().setMath(SBMLutilities.myParseFormula(eventPriority.getText().trim()));
					}
					if (!eventDelay.getText().trim().equals("")) {
						e.createDelay();
						e.getDelay().setMath(SBMLutilities.myParseFormula(eventDelay.getText().trim()));
						error = checkEventDelayUnits(biosim, document, e.getDelay());
					}
					if (!error) {
						for (int i = 0; i < assign.length; i++) {
							EventAssignment ea = e.createEventAssignment();
							ea.setVariable(assign[i].split(" ")[0]);
							ea.setMath(SBMLutilities.myParseFormula(assign[i].split("=")[1].trim()));
							error = checkEventAssignmentUnits(biosim, document, ea);
							if (error)
								break;
						}
					}
					//check for dynamic process
					if (!error) {
						
						if (((String)dynamicProcess.getSelectedItem()).equals("Division")) {
						
							e.setAnnotation("Division");
						}
						else if (((String)dynamicProcess.getSelectedItem()).equals("Death")) {
							
							e.setAnnotation("Death");
						}
					}
					
					Object[] adding = { e.getId() };
					// Object[] adding = {
					// myFormulaToString(e.getTrigger().getMath()) };
					add.setListData(adding);
					add.setSelectedIndex(0);
					events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					String[] ev = new String[events.getModel().getSize()];
					for (int i = 0; i < events.getModel().getSize(); i++) {
						ev[i] = events.getModel().getElementAt(i).toString();
					}
					adding = Utility.add(ev, events, add, null, null, null, null, null, Gui.frame);
					usedIDs.add(eventID.getText().trim());
					ev = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						ev[i] = (String) adding[i];
					}
					Utility.sort(ev);
					events.setListData(ev);
					events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (document.getModel().getNumEvents() == 1) {
						events.setSelectedIndex(0);
					}
					else {
						events.setSelectedIndex(index);
					}
					if (error) {
						removeTheEvent(document, SBMLutilities.myFormulaToString(e.getTrigger().getMath()));
					}
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, "Event Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
		dirty.setValue(true);
	}

	/**
	 * Check the units of an event delay
	 */
	private boolean checkEventDelayUnits(Gui biosim, SBMLDocument document, Delay delay) {
		document.getModel().populateListFormulaUnitsData();
		if (delay.containsUndeclaredUnits()) {
			if (biosim.checkUndeclared) {
				JOptionPane.showMessageDialog(Gui.frame, "Event assignment delay contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = delay.getDerivedUnitDefinition();
			if (unitDef != null && !(unitDef.isVariantOfTime())) {
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
	private boolean checkEventAssignmentUnits(Gui biosim, SBMLDocument document, EventAssignment assign) {
		document.getModel().populateListFormulaUnitsData();
		if (assign.containsUndeclaredUnits()) {
			if (biosim.checkUndeclared) {
				JOptionPane.showMessageDialog(Gui.frame, "Event assignment to " + assign.getVariable()
						+ " contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = assign.getDerivedUnitDefinition();
			UnitDefinition unitDefVar;
			Species species = document.getModel().getSpecies(assign.getVariable());
			Compartment compartment = document.getModel().getCompartment(assign.getVariable());
			Parameter parameter = document.getModel().getParameter(assign.getVariable());
			if (species != null) {
				unitDefVar = species.getDerivedUnitDefinition();
			}
			else if (compartment != null) {
				unitDefVar = compartment.getDerivedUnitDefinition();
			}
			else {
				unitDefVar = parameter.getDerivedUnitDefinition();
			}
			if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
				JOptionPane.showMessageDialog(Gui.frame, "Units on the left and right-hand side for the event assignment " + assign.getVariable()
						+ " do not agree.", "Units Do Not Match", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove an event from a list and SBML document
	 * 
	 * @param events
	 *            a list of events
	 * @param document
	 *            an SBML document from which to remove the event
	 * @param usedIDs
	 *            a list of all IDs current in use
	 * @param ev
	 *            an array of all events
	 */
	private void removeEvent(JList events, SBMLDocument document, ArrayList<String> usedIDs) {
		int index = events.getSelectedIndex();
		if (index != -1) {
			String selected = ((String) events.getSelectedValue());
			removeTheEvent(document, selected);
			usedIDs.remove(((String) events.getSelectedValue()).split(" ")[0]);
			events.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			Utility.remove(events);
			events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < events.getModel().getSize()) {
				events.setSelectedIndex(index);
			}
			else {
				events.setSelectedIndex(index - 1);
			}
			dirty.setValue(true);
		}
	}

	/**
	 * Remove an event from an SBML document
	 * 
	 * @param document
	 *            the SBML document from which to remove the event
	 * @param selected
	 *            the event Id to remove
	 */
	private void removeTheEvent(SBMLDocument document, String selected) {
		ListOf EL = document.getModel().getListOfEvents();
		for (int i = 0; i < document.getModel().getNumEvents(); i++) {
			org.sbml.libsbml.Event E = (org.sbml.libsbml.Event) EL.get(i);
			if (E.getId().equals(selected)) {
				EL.remove(i);
			}
		}
	}

	/**
	 * Creates a frame used to edit event assignments or create new ones.
	 * 
	 */
	private void eventAssignEditor(SBMLDocument document, JList eventAssign, String option) {
		if (option.equals("OK") && eventAssign.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No event assignment selected.", "Must Select an Event Assignment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel eventAssignPanel = new JPanel();
		JPanel EAPanel = new JPanel();
		JLabel idLabel = new JLabel("Variable:");
		JLabel eqnLabel = new JLabel("Assignment:");
		JComboBox eaID = new JComboBox();
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
		Model model = document.getModel();
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			String id = ((Compartment) ids.get(i)).getId();
			if (!((Compartment) ids.get(i)).getConstant()) {
				if (keepVarEvent(document, assign, selected, id)) {
					eaID.addItem(id);
				}
			}
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < model.getNumParameters(); i++) {
			String id = ((Parameter) ids.get(i)).getId();
			if (!((Parameter) ids.get(i)).getConstant()) {
				if (keepVarEvent(document, assign, selected, id)) {
					eaID.addItem(id);
				}
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			String id = ((Species) ids.get(i)).getId();
			if (!((Species) ids.get(i)).getConstant()) {
				if (keepVarEvent(document, assign, selected, id)) {
					eaID.addItem(id);
				}
			}
		}
		ids = model.getListOfReactions();
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) ids.get(i);
			ListOf ids2 = reaction.getListOfReactants();
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				SpeciesReference reactant = (SpeciesReference) ids2.get(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals("")) && !(reactant.getConstant())) {
					String id = reactant.getId();
					if (keepVarEvent(document, assign, selected, id)) {
						eaID.addItem(id);
					}
				}
			}
			ids2 = reaction.getListOfProducts();
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				SpeciesReference product = (SpeciesReference) ids2.get(j);
				if ((product.isSetId()) && (!product.getId().equals("")) && !(product.getConstant())) {
					String id = product.getId();
					if (keepVarEvent(document, assign, selected, id)) {
						eaID.addItem(id);
					}
				}
			}
		}
		JTextField eqn = new JTextField(30);
		if (option.equals("OK")) {
			String selectAssign = ((String) eventAssign.getSelectedValue());
			eaID.setSelectedItem(selectAssign.split(" ")[0]);
			eqn.setText(selectAssign.split("=")[1].trim());
		}
		EAPanel.add(idLabel);
		EAPanel.add(eaID);
		EAPanel.add(eqnLabel);
		EAPanel.add(eqn);
		eventAssignPanel.add(EAPanel);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, eventAssignPanel, "Event Asssignment Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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
				ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(document, eqn.getText().trim(), "", false);
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
					message = "Event assignment contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(300, 300));
					scrolls.setPreferredSize(new Dimension(300, 300));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (!error) {
					error = SBMLutilities.checkNumFunctionArguments(document, SBMLutilities.myParseFormula(eqn.getText().trim()));
				}
				if (!error) {
					if (SBMLutilities.myParseFormula(eqn.getText().trim()).isBoolean()) {
						JOptionPane.showMessageDialog(Gui.frame, "Event assignment must evaluate to a number.", "Number Expected",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = eventAssign.getSelectedIndex();
					eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					assign = Utility.getList(assign, eventAssign);
					eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					assign[index] = eaID.getSelectedItem() + " = " + eqn.getText().trim();
					Utility.sort(assign);
					eventAssign.setListData(assign);
					eventAssign.setSelectedIndex(index);
				}
				else {
					JList add = new JList();
					int index = eventAssign.getSelectedIndex();
					Object[] adding = { eaID.getSelectedItem() + " = " + eqn.getText().trim() };
					add.setListData(adding);
					add.setSelectedIndex(0);
					eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(assign, eventAssign, add, null, null, null, null, null, Gui.frame);
					assign = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						assign[i] = (String) adding[i];
					}
					Utility.sort(assign);
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
	private boolean keepVarEvent(SBMLDocument document, String[] assign, String selected, String id) {
		if (!selected.equals(id)) {
			for (int j = 0; j < assign.length; j++) {
				if (id.equals(assign[j].split(" ")[0])) {
					return false;
				}
			}
			ListOf r = document.getModel().getListOfRules();
			for (int i = 0; i < document.getModel().getNumRules(); i++) {
				Rule rule = (Rule) r.get(i);
				if (rule.isAssignment() && rule.getVariable().equals(id))
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
	private void removeAssignment(JList eventAssign) {
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

	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addEvent) {
			eventEditor(biosim, "Add", events, document, usedIDs, eventAssign);
		}
		// if the edit event button is clicked
		else if (e.getSource() == editEvent) {
			eventEditor(biosim, "OK", events, document, usedIDs, eventAssign);
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeEvent) {
			removeEvent(events, document, usedIDs);
		}
		// if the add event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Add Assignment")) {
			eventAssignEditor(document, eventAssign, "Add");
		}
		// if the edit event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Edit Assignment")) {
			eventAssignEditor(document, eventAssign, "OK");
		}
		// if the remove event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Remove Assignment")) {
			removeAssignment(eventAssign);
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == events) {
				eventEditor(biosim, "OK", events, document, usedIDs, eventAssign);
			}
			else if (e.getSource() == eventAssign) {
				eventAssignEditor(document, eventAssign, "OK");
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
