package biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.xml.stream.XMLStreamException;

import main.Gui;
import main.util.Utility;

import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.layout.Layout;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.sbol.SBOLField;
import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;


/**
 * This is a class for creating SBML events.
 * 
 * @author Chris Myers
 * 
 */
public class Events extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addEvent, addTrans, removeEvent, editEvent, addAssignment, editAssignment, removeAssignment;

	private JList events; // JList of events

	private JList eventAssign; // JList of event assignments
	
	private JComboBox dimensionType, dimensionX, dimensionY;
	
	private JTextField iIndex, jIndex;

	private BioModel bioModel;

	private ModelEditor modelEditor;
	
	private SBOLField sbolField;
	
	private boolean isTextual;

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
		Utility.sort(ev);
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
		String[] origAssign = new String[0];
		String[] assign = new String[0];
		String[] placeAssign = new String[0];
		ArrayList<String> presetPlaces = new ArrayList<String>();
		JPanel eventPanel = new JPanel(new BorderLayout());
		// JPanel evPanel = new JPanel(new GridLayout(2, 2));
		JPanel evPanel = new JPanel(new GridLayout(12, 2));
		if (isTransition) {
			evPanel.setLayout(new GridLayout(10, 2));
		}
		JLabel IDLabel = new JLabel("ID:");
		JLabel NameLabel = new JLabel("Name:");
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
		
		JTextField eventID = new JTextField(12);
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
		iIndex = new JTextField(10);
		jIndex = new JTextField(10);
		iIndex.setEnabled(false);
		jIndex.setEnabled(false);
		
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
						evPanel.setLayout(new GridLayout(10, 2));
					}
					Eindex = i;
					eventID.setText(event.getId());
					selectedID = event.getId();
					eventName.setText(event.getName());
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
					String[] indecies = new String[2];
					for (int j = 0; j < event.getEventAssignmentCount(); j++) {
						Parameter parameter = 
								bioModel.getSBMLDocument().getModel().getParameter(event.getListOfEventAssignments().get(j).getVariable());
						EventAssignment ea = event.getListOfEventAssignments().get(j);
						// TODO: add the index in the field
						String assignIndex = "";
						indecies[0] = AnnotationUtility.parseRowIndexAnnotation(ea);
						if(indecies[0]!=null){
							indecies[1] = AnnotationUtility.parseColIndexAnnotation(ea);
							if(indecies[1]==null){
								assignIndex = " [" + indecies[0] + "]";
							}
							else{
								assignIndex = "[" + indecies[0] + ",";
								assignIndex += (indecies[1] + "]");
							}
						}
						// TODO: update assign with index in the string
						if (parameter!=null && SBMLutilities.isPlace(parameter)) {
							if (isTextual) {
								assign[l] = ea.getVariable() + assignIndex + " := " + SBMLutilities.myFormulaToString(ea.getMath());
								l++;
							} else {
								placeAssign[k] = ea.getVariable() + assignIndex + " := " + SBMLutilities.myFormulaToString(ea.getMath());
								k++;
							}
						} else if (ea.getVariable().equals(GlobalConstants.FAIL)){
							failTransition.setSelected(true);
						} else {
							String assignMath = SBMLutilities.myFormulaToString(ea.getMath());
							if (parameter!=null && SBMLutilities.isBoolean(parameter)) {
								assignMath = bioModel.removeBooleanAssign(event.getListOfEventAssignments().get(j).getMath());
							} 
							assign[l] = ea.getVariable() + assignIndex + " := " + assignMath;
							l++;
						}
						origAssign[j] = ea.getVariable() + assignIndex + " := " + SBMLutilities.myFormulaToString(ea.getMath());
					}
					if (!modelEditor.isParamsOnly()) {
						//Parse out SBOL annotations and add to SBOL field
						List<URI> sbolURIs = new LinkedList<URI>();
						String sbolStrand = AnnotationUtility.parseSBOLAnnotation(event, sbolURIs);
						// Field for annotating event with SBOL DNA components
						sbolField = new SBOLField(sbolURIs, sbolStrand, GlobalConstants.SBOL_DNA_COMPONENT, modelEditor, 
								2, false);
					}
				}
				String[] sizes = new String[2];
				sizes[0] = AnnotationUtility.parseVectorSizeAnnotation(event);
				if(sizes[0]==null){
					sizes = AnnotationUtility.parseMatrixSizeAnnotation(event);
					if(sizes==null){
						dimensionType.setSelectedIndex(0);
						dimensionX.setEnabled(false);
						dimensionY.setEnabled(false);
					}
					else{
						dimensionType.setSelectedIndex(2);
						dimensionX.setEnabled(true);
						dimensionY.setEnabled(true);
						dimensionX.setSelectedItem(sizes[0]);
						dimensionY.setSelectedItem(sizes[1]);
					}
				}
				else{
					dimensionType.setSelectedIndex(1);
					dimensionX.setEnabled(true);
					dimensionX.setSelectedItem(sizes[0]);
					dimensionY.setEnabled(false);
				}
			}
		}
		else {
			// Field for annotating event with SBOL DNA components
			sbolField = new SBOLField(new LinkedList<URI>(), GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND, 
					GlobalConstants.SBOL_DNA_COMPONENT, modelEditor, 2, false);
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
		Utility.sort(assign);
		eventAssign.setListData(assign);
		if(eventAssign.getModel().getSize()==0){
			dimensionType.setEnabled(true);
		}
		else{
			dimensionType.setEnabled(false);					
		}
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
		if (!isTransition) {
			evPanel.add(assignTimeLabel);
			evPanel.add(assignTime);
		}
		evPanel.add(persistentTriggerLabel);
		evPanel.add(persistentTrigger);
		if (!isTransition) {
			evPanel.add(initialTriggerLabel);
			evPanel.add(initialTrigger);
			evPanel.add(dynamicProcessLabel);
			evPanel.add(dynamicProcess);
		} else {
			evPanel.add(failTransitionLabel);
			evPanel.add(failTransition);
		}
		
		evPanel.add(dimensionType);
		evPanel.add(dimensionX);
		evPanel.add(new JLabel());
		evPanel.add(dimensionY);
		evPanel.add(onPortLabel);
		evPanel.add(onPort);
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
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			assign = new String[eventAssign.getModel().getSize()];
			for (int i = 0; i < eventAssign.getModel().getSize(); i++) {
				assign[i] = eventAssign.getModel().getElementAt(i).toString();
			}
			error = SBMLutilities.checkID(bioModel.getSBMLDocument(), eventID.getText().trim(), selectedID, false);
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
				ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(bioModel.getSBMLDocument(), eventTrigger.getText().trim(), "", false);
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
					invalidVars = SBMLutilities.getInvalidVariables(bioModel.getSBMLDocument(), eventDelay.getText().trim(), "", false);
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
					invalidVars = SBMLutilities.getInvalidVariables(bioModel.getSBMLDocument(), eventPriority.getText().trim(), "", false);
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
					error = SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), 
							bioModel.addBooleans(eventTrigger.getText().trim()));
				}
				if ((!error) && (!eventDelay.getText().trim().equals(""))) {
					error = SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), 
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
					error = SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), 
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
					for (int i = 0; i < assign.length; i++) {
						EventAssignment ea = e.createEventAssignment();
						String var = assign[i].split(" ")[0];
						ea.setVariable(var);
						// TODO: extract the index, if one exists and add annotations for them
						// look at assign[i]
						String left = assign[i].split(":=")[0].trim();
						if(left.contains("[")){
							String ind = left.split("\\[")[1].trim();
							if(!ind.contains(",")){
								AnnotationUtility.removeColIndexAnnotation(ea);
								AnnotationUtility.setRowIndexAnnotation(ea, ind.replace("]", ""));
							}
							else{
								AnnotationUtility.setRowIndexAnnotation(ea,ind.split(",")[0]);
								AnnotationUtility.setColIndexAnnotation(ea,ind.split(",")[1].replace("]", ""));
							}
						}
						else{
							AnnotationUtility.removeRowIndexAnnotation(ea);
							AnnotationUtility.removeColIndexAnnotation(ea);
						}
						Parameter p = bioModel.getSBMLDocument().getModel().getParameter(var);
						if (p != null && SBMLutilities.isBoolean(p)) {
							ea.setMath(bioModel.addBooleanAssign(assign[i].split(":=")[1].trim()));
						} else {
							ea.setMath(SBMLutilities.myParseFormula(assign[i].split(":=")[1].trim()));
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
					for (int i = 0; i < placeAssign.length; i++) {
						EventAssignment ea = e.createEventAssignment();
						ea.setVariable(placeAssign[i].split(" ")[0]);
						String left = placeAssign[i].split(":=")[0].trim();
						if(left.contains("[")){
							String ind = left.split("\\[")[1].trim();
							if(!ind.contains(",")){
								AnnotationUtility.setRowIndexAnnotation(ea, ind.replace("]", ""));
							}
							else{
								AnnotationUtility.setRowIndexAnnotation(ea,ind.split(",")[0]);
								AnnotationUtility.setColIndexAnnotation(ea, ind.split(",")[1].replace("]", ""));
							}
						}
						ea.setMath(SBMLutilities.myParseFormula(placeAssign[i].split(":=")[1].trim()));
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
									SBMLutilities.removeFromParentAndDelete(r);
								}
								Parameter p = bioModel.getSBMLDocument().getModel().getParameter(GlobalConstants.TRIGGER + "_" + e.getId());
								if (p != null) {
									SBMLutilities.removeFromParentAndDelete(p);
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
								SBMLutilities.createFunction(bioModel.getSBMLDocument().getModel(), "G", "Globally Property", 
										"lambda(t,x,or(not(t),x))");
								c.setMath(SBMLutilities.myParseFormula("G(true,!(eq(fail,1)))"));
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
								SBMLutilities.removeFromParentAndDelete(ea);
							}
						}
						Port port = bioModel.getPortByIdRef(selectedID);
						if (port!=null) {
							if (onPort.isSelected()) {
								port.setId(GlobalConstants.EVENT+"__"+e.getId());
								port.setIdRef(e.getId());
							} else {
								SBMLutilities.removeFromParentAndDelete(port);
							}
						} else {
							if (onPort.isSelected()) {
								port = bioModel.getSBMLCompModel().createPort();
								port.setId(GlobalConstants.EVENT+"__"+e.getId());
								port.setIdRef(e.getId());
							}
						}
						int index = events.getSelectedIndex();
						ev[index] = e.getId();
						Utility.sort(ev);
						events.setListData(ev);
						events.setSelectedIndex(index);
						bioModel.makeUndoPoint();
					}
					//edit dynamic process
					if (!error) {
						if (!((String)dynamicProcess.getSelectedItem()).equals("none")) {
							AnnotationUtility.setDynamicAnnotation(e, (String)dynamicProcess.getSelectedItem());
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
							ea.setMath(SBMLutilities.myParseFormula(origAssign[i].split(":=")[1].trim()));
						}
					}
					if (dimensionType.getSelectedIndex() == 1){
						AnnotationUtility.removeMatrixSizeAnnotation(e);
						AnnotationUtility.setVectorSizeAnnotation(e,(String) dimensionX.getSelectedItem());
					}
					else if (dimensionType.getSelectedIndex() == 2){
						AnnotationUtility.removeVectorSizeAnnotation(e);
						AnnotationUtility.setMatrixSizeAnnotation(e,(String) dimensionX.getSelectedItem(), 
								(String) dimensionY.getSelectedItem());
					}
					else{
						AnnotationUtility.removeVectorSizeAnnotation(e);
						AnnotationUtility.removeMatrixSizeAnnotation(e);
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
					if (!eventID.getText().trim().equals("")) {
						e.setId(eventID.getText().trim());
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
						for (int i = 0; i < assign.length; i++) {
							EventAssignment ea = e.createEventAssignment();
							String var = assign[i].split(" ")[0];
							// TODO: extract the index, if one exists and add annotations for them
							// look at assign[i]
							String left = assign[i].split(":=")[0].trim();
							if(left.contains("[")){
								String ind = left.split("\\[")[1].trim();
								if(!ind.contains(",")){
									AnnotationUtility.removeColIndexAnnotation(ea);
									AnnotationUtility.setRowIndexAnnotation(ea, ind.replace("]", ""));
									AnnotationUtility.removeColIndexAnnotation(ea);
								}
								else{
									AnnotationUtility.setRowIndexAnnotation(ea,ind.split(",")[0]);
									AnnotationUtility.setColIndexAnnotation(ea,ind.split(",")[1].replace("]", ""));
								}
							}
							else{
								AnnotationUtility.removeRowIndexAnnotation(ea);
								AnnotationUtility.removeColIndexAnnotation(ea);
							}
							if (var.endsWith("\'")) {
								var = "rate_" + var.replace("\'","");
							}
							ea.setVariable(var);
							Parameter p = bioModel.getSBMLDocument().getModel().getParameter(var);
							if (p != null && SBMLutilities.isBoolean(p)) {
								ea.setMath(bioModel.addBooleanAssign(assign[i].split(":=")[1].trim()));
							} else {
								ea.setMath(SBMLutilities.myParseFormula(assign[i].split(":=")[1].trim()));
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
								SBMLutilities.createFunction(bioModel.getSBMLDocument().getModel(), "G", "Globally Property", 
										"lambda(t,x,or(not(t),x))");
								c.setMath(SBMLutilities.myParseFormula("G(true,!(eq(fail,1)))"));
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
								SBMLutilities.removeFromParentAndDelete(ea);
							}
						}
						if (onPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.EVENT+"__"+e.getId());
							port.setIdRef(e.getId());
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
					adding = Utility.add(ev, events, add);
					ev = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						ev[i] = (String) adding[i];
					}
					Utility.sort(ev);
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
					if (dimensionType.getSelectedIndex() == 1){
						AnnotationUtility.removeMatrixSizeAnnotation(e);
						AnnotationUtility.setVectorSizeAnnotation(e,(String) dimensionX.getSelectedItem());
						
					}
					else if (dimensionType.getSelectedIndex() == 2){
						AnnotationUtility.removeVectorSizeAnnotation(e);
						AnnotationUtility.setMatrixSizeAnnotation(e,(String) dimensionX.getSelectedItem(), 
								(String) dimensionY.getSelectedItem());
					}
					else{
						AnnotationUtility.removeVectorSizeAnnotation(e);
						AnnotationUtility.removeMatrixSizeAnnotation(e);
					}
				}
				if (!error && !modelEditor.isParamsOnly()) {
					// Add SBOL annotation to event
					if (sbolField.getSBOLURIs().size() > 0) {
						SBOLAnnotation sbolAnnot = new SBOLAnnotation(e.getMetaId(), sbolField.getSBOLURIs(), 
								sbolField.getSBOLStrand());
						AnnotationUtility.setSBOLAnnotation(e, sbolAnnot);
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
		return eventID.getText().trim();
	}

	/**
	 * Check the units of an event delay
	 */
	private static boolean checkEventDelayUnits(Delay delay) {
//		TODO:  Is this necessary?
//		bioModel.getSBMLDocument().getModel().populateListFormulaUnitsData();
		if (delay.containsUndeclaredUnits()) {
			if (Gui.getCheckUndeclared()) {
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
//		TODO:  Is this necessary?
//		bioModel.getSBMLDocument().getModel().populateListFormulaUnitsData();
		if (assign.containsUndeclaredUnits()) {
			if (Gui.getCheckUndeclared()) {
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
			ev[i] = event.getId();
		}
		Utility.sort(ev);
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
			String selected = ((String) events.getSelectedValue());
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
			gcm.makeUndoPoint();
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
	public static void removeTheEvent(BioModel gcm, String selected) {
		ListOf<Event> EL = gcm.getSBMLDocument().getModel().getListOfEvents();
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getEventCount(); i++) {
			org.sbml.jsbml.Event E = EL.get(i);
			if (E.getId().equals(selected)) {
				EL.remove(i);
			}
		}
		for (int i = 0; i < gcm.getSBMLCompModel().getListOfPorts().size(); i++) {
			Port port = gcm.getSBMLCompModel().getListOfPorts().get(i);
			if (port.isSetIdRef() && port.getIdRef().equals(selected)) {
				gcm.getSBMLCompModel().getListOfPorts().remove(i);
				break;
			}
		}
		if (gcm.getSBMLLayout().getListOfLayouts().get("iBioSim") != null) {
			Layout layout = gcm.getSBMLLayout().getListOfLayouts().get("iBioSim"); 
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+selected)!=null) {
				layout.getListOfAdditionalGraphicalObjects().remove(GlobalConstants.GLYPH+"__"+selected);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
				layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+selected);
			}
		}
	}

	/**
	 * Creates a frame used to edit event assignments or create new ones.
	 * 
	 */
	private void eventAssignEditor(BioModel gcm, JList eventAssign, String option) {
		if (option.equals("OK") && eventAssign.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No event assignment selected.", "Must Select an Event Assignment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel eventAssignPanel = new JPanel(new GridLayout(2,1));
		JPanel northEAPanel = new JPanel();
		JPanel southEAPanel = new JPanel();
		JLabel idLabel = new JLabel("Variable:");
		JLabel indexLabel = new JLabel("Indecies:");
		JLabel eqnLabel = new JLabel("Assignment:");
		JComboBox eaID = new JComboBox();
		iIndex = new JTextField(10);
		jIndex = new JTextField(10);
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
		Model model = gcm.getSBMLDocument().getModel();
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			String id = model.getCompartment(i).getId();
			if (!(model.getCompartment(i).getConstant())) {
				if (keepVarEvent(gcm, assign, selected, id)) {
					eaID.addItem(id);
				}
			}
		}
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter p = model.getParameter(i);
			if ((!isTextual && SBMLutilities.isPlace(p))||p.getId().endsWith("_"+GlobalConstants.RATE)) continue;
			String id = p.getId();
			if (!p.getConstant()) {
				if (keepVarEvent(gcm, assign, selected, id)) {
					eaID.addItem(id);
				}
				if (!SBMLutilities.isBoolean(p) && !SBMLutilities.isPlace(p)) {
					if (keepVarEvent(gcm, assign, selected, id+"_"+GlobalConstants.RATE)) {
						eaID.addItem(id+"_"+GlobalConstants.RATE);
					}
				}
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			String id = model.getSpecies(i).getId();
			if (!(model.getSpecies(i).getConstant())) {
				if (keepVarEvent(gcm, assign, selected, id)) {
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
					if (keepVarEvent(gcm, assign, selected, id)) {
						eaID.addItem(id);
					}
				}
			}
			for (int j = 0; j < reaction.getProductCount(); j++) {
				SpeciesReference product = reaction.getProduct(j);
				if ((product.isSetId()) && (!product.getId().equals("")) && !(product.getConstant())) {
					String id = product.getId();
					if (keepVarEvent(gcm, assign, selected, id)) {
						eaID.addItem(id);
					}
				}
			}
		}
		JTextField eqn = new JTextField(30);
		if (option.equals("OK")) {
			String selectAssign = ((String) eventAssign.getSelectedValue());
			eaID.setSelectedItem(selectAssign.split(" ")[0]);
			String left = selectAssign.split(":=")[0].trim();
			if(left.contains("[")){
				String ind = left.split("\\[")[1].trim();
				if(!ind.contains(",")){
					iIndex.setText(ind.replace("]", ""));
				}
				else{
					iIndex.setText(ind.split("\\,")[0]);
					jIndex.setText(ind.split("\\,")[1].replace("]", ""));
				}
			}
			eqn.setText(selectAssign.split(":=")[1].trim());
		}
		northEAPanel.add(idLabel);
		northEAPanel.add(eaID);
		northEAPanel.add(indexLabel);
		northEAPanel.add(iIndex);
		northEAPanel.add(jIndex);
		southEAPanel.add(eqnLabel);
		southEAPanel.add(eqn);
		eventAssignPanel.add(northEAPanel,"North");
		eventAssignPanel.add(southEAPanel,"South");
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
				ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(gcm.getSBMLDocument(), eqn.getText().trim(), "", false);
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
					Parameter p = bioModel.getSBMLDocument().getModel().getParameter((String)eaID.getSelectedItem());
					ASTNode assignMath = SBMLutilities.myParseFormula(eqn.getText().trim());
					if (p != null && SBMLutilities.isBoolean(p)) {
						assignMath = bioModel.addBooleanAssign(eqn.getText().trim());
					} 
					error = SBMLutilities.checkNumFunctionArguments(gcm.getSBMLDocument(), assignMath);
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
					// TODO: add the index into the assign[index] string
					String assignIndex = " [";
					if(iIndex.getText().equals("")) {
						assignIndex = "";
					}
					else if (!iIndex.getText().equals("") && !jIndex.getText().equals("")) {
						assignIndex += (iIndex.getText() + ",");
						assignIndex += (jIndex.getText() + "]");
					}
					else {
						assignIndex += (iIndex.getText() + "]");
					}
					assign[index] = eaID.getSelectedItem() + assignIndex + " := " + eqn.getText().trim();
					Utility.sort(assign);
					eventAssign.setListData(assign);
					eventAssign.setSelectedIndex(index);
				}
				else {
					JList add = new JList();
					int index = eventAssign.getSelectedIndex();
					// TODO: add the index into the assign[index] string
					String assignIndex = " [";
					if(iIndex.getText().equals("")) {
						assignIndex = "";
					}
					else if (!iIndex.getText().equals("") && !jIndex.getText().equals("")) {
						assignIndex += (iIndex.getText() + ",");
						assignIndex += (jIndex.getText() + "]");
					}
					else {
						assignIndex += (iIndex.getText() + "]");
					}
					Object[] adding = { eaID.getSelectedItem() + assignIndex + " := " + eqn.getText().trim() };
					add.setListData(adding);
					add.setSelectedIndex(0);
					eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(assign, eventAssign, add);
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
			bioModel.makeUndoPoint();
		}else if (e.getSource() == addTrans) {
			eventEditor("Add","",true);
			bioModel.makeUndoPoint();
		}
		// if the edit event button is clicked
		else if (e.getSource() == editEvent) {
			if (events.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No event selected.", "Must Select an Event", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String selected = ((String) events.getSelectedValue());
			eventEditor("OK",selected,false);
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeEvent) {
			removeEvent(events, bioModel);
		}
		// if the dimension type is changed
		else if (e.getSource() == dimensionType) {
			int index = dimensionType.getSelectedIndex();
			if (index == 0) {
				dimensionX.setEnabled(false);
				dimensionY.setEnabled(false);
				iIndex.setEnabled(false);
				jIndex.setEnabled(false);
			}
			else if (index == 1) {
				dimensionX.setEnabled(true);
				dimensionY.setEnabled(false);
				iIndex.setEnabled(true);
				jIndex.setEnabled(false);
			}
			else if (index == 2) {
				dimensionX.setEnabled(true);
				dimensionY.setEnabled(true);
				iIndex.setEnabled(true);
				jIndex.setEnabled(true);
			}
		}
		// if the add event assignment button is clicked
		else if (e.getSource() == addAssignment) {
		//else if (((JButton) e.getSource()).getText().equals("Add Assignment")) {
			eventAssignEditor(bioModel, eventAssign, "Add");
			if(eventAssign.getModel().getSize()==0){
				dimensionType.setEnabled(true);
				int index = dimensionType.getSelectedIndex();
				if (index == 0) {
					dimensionX.setEnabled(false);
					dimensionY.setEnabled(false);
					iIndex.setEnabled(false);
					jIndex.setEnabled(false);
				}
				else if (index == 1) {
					dimensionX.setEnabled(true);
					dimensionY.setEnabled(false);
					iIndex.setEnabled(true);
					jIndex.setEnabled(false);
				}
				else if (index == 2) {
					dimensionX.setEnabled(true);
					dimensionY.setEnabled(true);
					iIndex.setEnabled(true);
					jIndex.setEnabled(true);
				}
			}
			else{
				dimensionType.setEnabled(false);
				dimensionX.setEnabled(false);
				dimensionY.setEnabled(false);
			}
		}
		// if the edit event assignment button is clicked
		else if (e.getSource() == editAssignment) {
			eventAssignEditor(bioModel, eventAssign, "OK");
			if(eventAssign.getModel().getSize()==0){
				dimensionType.setEnabled(true);	
				int index = dimensionType.getSelectedIndex();
				if (index == 0) {
					dimensionX.setEnabled(false);
					dimensionY.setEnabled(false);
					iIndex.setEnabled(false);
					jIndex.setEnabled(false);
				}
				else if (index == 1) {
					dimensionX.setEnabled(true);
					dimensionY.setEnabled(false);
					iIndex.setEnabled(true);
					jIndex.setEnabled(false);
				}
				else if (index == 2) {
					dimensionX.setEnabled(true);
					dimensionY.setEnabled(true);
					iIndex.setEnabled(true);
					jIndex.setEnabled(true);
				}
			}
			else{
				dimensionType.setEnabled(false);
				dimensionX.setEnabled(false);
				dimensionY.setEnabled(false);
			}
		}
		// if the remove event assignment button is clicked
		else if (e.getSource() == removeAssignment) {
			removeAssignment(eventAssign);
			if(eventAssign.getModel().getSize()==0){
				dimensionType.setEnabled(true);
				int index = dimensionType.getSelectedIndex();
				if (index == 0) {
					dimensionX.setEnabled(false);
					dimensionY.setEnabled(false);
					iIndex.setEnabled(false);
					jIndex.setEnabled(false);
				}
				else if (index == 1) {
					dimensionX.setEnabled(true);
					dimensionY.setEnabled(false);
					iIndex.setEnabled(true);
					jIndex.setEnabled(false);
				}
				else if (index == 2) {
					dimensionX.setEnabled(true);
					dimensionY.setEnabled(true);
					iIndex.setEnabled(true);
					jIndex.setEnabled(true);
				}
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
				String selected = ((String) events.getSelectedValue());
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
