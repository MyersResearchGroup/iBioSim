package biomodel.gui;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.CompConstant;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.comp.Submodel;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;
import main.Gui;


public class ComponentsPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String selected = "";

	private String[] options = { "Ok", "Cancel" };
	
	private ArrayList<String> portIds = null;
	
	private ArrayList<String> idRefs = null;
	
	private ArrayList<String> types = null;

	private ArrayList<JComboBox> portmapBox = null;

	private ArrayList<JComboBox> directionBox = null;

	private ArrayList<JComboBox> convBox = null;

	private BioModel bioModel = null;

	private PropertyList componentsList = null;

	private HashMap<String, PropertyField> fields = null;
	
	private SBOLField sbolField;

	private String selectedComponent, oldPort;
	
	private ModelEditor modelEditor;
	
	private String subModelId;
	
	private JComboBox timeConvFactorBox;
	
	private JComboBox extentConvFactorBox;
	
	private boolean paramsOnly;
	
	private BioModel subBioModel;

	public ComponentsPanel(String selected, PropertyList componentsList, BioModel bioModel, BioModel subBioModel,
			ArrayList<String> ports, String selectedComponent, String oldPort, boolean paramsOnly, ModelEditor gcmEditor) {
		
		super(new GridLayout(ports.size() + 6, 1));
		this.selected = selected;
		this.componentsList = componentsList;
		this.bioModel = bioModel;
		this.modelEditor = gcmEditor;
		this.selectedComponent = selectedComponent;
		this.oldPort = oldPort;
		this.paramsOnly = paramsOnly;
		this.subBioModel = subBioModel;

		fields = new HashMap<String, PropertyField>();
		portIds = new ArrayList<String>();
		idRefs = new ArrayList<String>();
		types = new ArrayList<String>();
		portmapBox = new ArrayList<JComboBox>();
		directionBox = new ArrayList<JComboBox>();
		convBox = new ArrayList<JComboBox>();

		String[] directions = new String[2];
		directions[0] = "<--";
		directions[1] = "-->";
		
		if (bioModel.isGridEnabled()) {
			subModelId = "GRID__" + selectedComponent.replace(".xml","");
		} else {
			subModelId = selected;
		}
		
		ArrayList <String> constParameterList = bioModel.getConstantUserParameters();
		Collections.sort(constParameterList);
		String[] parameters = new String[constParameterList.size()+1];
		parameters[0] = "(none)";
		for (int l = 1; l < parameters.length; l++) {
			parameters[l] = constParameterList.get(l-1);
		}
		timeConvFactorBox = new JComboBox(parameters);
		extentConvFactorBox = new JComboBox(parameters);

		ArrayList <String> compartmentList = bioModel.getCompartments();
		Collections.sort(compartmentList);
		String[] compsWithNone = new String[compartmentList.size() + 2];
		compsWithNone[0] = "--none--";
		compsWithNone[1] = "--delete--";
		for (int l = 2; l < compsWithNone.length; l++) {
			compsWithNone[l] = compartmentList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.COMPARTMENT)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(compsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> parameterList = bioModel.getParameters();
		Collections.sort(parameterList);
		String[] paramsWithNone = new String[parameterList.size() + 2];
		paramsWithNone[0] = "--none--";
		paramsWithNone[1] = "--delete--";
		for (int l = 2; l < paramsWithNone.length; l++) {
			paramsWithNone[l] = parameterList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.PARAMETER) || type.equals(GlobalConstants.LOCALPARAMETER)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(paramsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> booleanList = bioModel.getBooleans();
		Collections.sort(booleanList);
		String[] boolsWithNone = new String[booleanList.size() + 2];
		boolsWithNone[0] = "--none--";
		boolsWithNone[1] = "--delete--";
		for (int l = 2; l < boolsWithNone.length; l++) {
			boolsWithNone[l] = booleanList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.BOOLEAN)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(boolsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> placeList = bioModel.getPlaces();
		Collections.sort(placeList);
		String[] placesWithNone = new String[placeList.size() + 2];
		placesWithNone[0] = "--none--";
		placesWithNone[1] = "--delete--";
		for (int l = 2; l < placesWithNone.length; l++) {
			placesWithNone[l] = placeList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.PLACE)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(placesWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> speciesList = bioModel.getSpecies();
		Collections.sort(speciesList);
		String[] specsWithNone = new String[speciesList.size() + 2];
		specsWithNone[0] = "--none--";
		specsWithNone[1] = "--delete--";
 		for (int l = 2; l < specsWithNone.length; l++) {
			specsWithNone[l] = speciesList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.SBMLSPECIES)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(specsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> promoterList = bioModel.getPromoters();
		Collections.sort(promoterList);
		String[] promsWithNone = new String[promoterList.size() + 2];
		promsWithNone[0] = "--none--";
		promsWithNone[1] = "--delete--";
 		for (int l = 2; l < promsWithNone.length; l++) {
			promsWithNone[l] = promoterList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.PROMOTER)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(promsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> reactionList = bioModel.getReactions();
		Collections.sort(reactionList);
		String[] reactionsWithNone = new String[reactionList.size() + 2];
		reactionsWithNone[0] = "--none--";
		reactionsWithNone[1] = "--delete--";
 		for (int l = 2; l < reactionsWithNone.length; l++) {
			reactionsWithNone[l] = reactionList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.SBMLREACTION)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(reactionsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> functionList = bioModel.getFunctions();
		Collections.sort(functionList);
		String[] functionsWithNone = new String[functionList.size() + 2];
		functionsWithNone[0] = "--none--";
		functionsWithNone[1] = "--delete--";
 		for (int l = 2; l < functionsWithNone.length; l++) {
			functionsWithNone[l] = functionList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.FUNCTION)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(functionsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> unitList = bioModel.getUnits();
		Collections.sort(unitList);
		String[] unitsWithNone = new String[unitList.size() + 2];
		unitsWithNone[0] = "--none--";
		unitsWithNone[1] = "--delete--";
 		for (int l = 2; l < unitsWithNone.length; l++) {
			unitsWithNone[l] = unitList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.UNIT)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(unitsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> ruleList = bioModel.getAlgebraicRules();
		Collections.sort(ruleList);
		String[] rulesWithNone = new String[ruleList.size() + 2];
		rulesWithNone[0] = "--none--";
		rulesWithNone[1] = "--delete--";
 		for (int l = 2; l < rulesWithNone.length; l++) {
			rulesWithNone[l] = ruleList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.ALGEBRAIC_RULE)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(rulesWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convFactor.setEnabled(false);
				convBox.add(convFactor);
			}
		}
		
		ruleList = bioModel.getAssignmentRules();
		Collections.sort(ruleList);
		rulesWithNone = new String[ruleList.size() + 2];
		rulesWithNone[0] = "--none--";
		rulesWithNone[1] = "--delete--";
 		for (int l = 2; l < rulesWithNone.length; l++) {
			rulesWithNone[l] = ruleList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.ASSIGNMENT_RULE)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(rulesWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convFactor.setEnabled(false);
				convBox.add(convFactor);
			}
		}	
		
		ruleList = bioModel.getRateRules();
		Collections.sort(ruleList);
		rulesWithNone = new String[ruleList.size() + 2];
		rulesWithNone[0] = "--none--";
		rulesWithNone[1] = "--delete--";
 		for (int l = 2; l < rulesWithNone.length; l++) {
			rulesWithNone[l] = ruleList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.RATE_RULE)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(rulesWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convFactor.setEnabled(false);
				convBox.add(convFactor);
			}
		}	
		
		ArrayList <String> constraintList = bioModel.getConstraints();
		Collections.sort(constraintList);
		String[] constraintsWithNone = new String[constraintList.size() + 2];
		constraintsWithNone[0] = "--none--";
		constraintsWithNone[1] = "--delete--";
 		for (int l = 2; l < constraintsWithNone.length; l++) {
			constraintsWithNone[l] = constraintList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.CONSTRAINT)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(constraintsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convFactor.setEnabled(false);
				convBox.add(convFactor);
			}
		}
				
		ArrayList <String> eventList = bioModel.getEvents();
		Collections.sort(eventList);
		String[] eventsWithNone = new String[eventList.size() + 2];
		eventsWithNone[0] = "--none--";
		eventsWithNone[1] = "--delete--";
 		for (int l = 2; l < eventsWithNone.length; l++) {
			eventsWithNone[l] = eventList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.EVENT)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(eventsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convFactor.setEnabled(false);
				convBox.add(convFactor);
			}
		}
		
		ArrayList <String> transitionList = bioModel.getTransitions();
		Collections.sort(transitionList);
		String[] transWithNone = new String[transitionList.size() + 2];
		transWithNone[0] = "--none--";
		transWithNone[1] = "--delete--";
		for (int l = 2; l < transWithNone.length; l++) {
			transWithNone[l] = transitionList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.TRANSITION)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(transWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convFactor.setEnabled(false);
				convBox.add(convFactor);
			}
		}

		String[] Choices = new String[2];
		Choices[0] = "--include--";
		Choices[1] = "--delete--";
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (!type.equals(GlobalConstants.COMPARTMENT) && !type.equals(GlobalConstants.PARAMETER) &&
				!type.equals(GlobalConstants.LOCALPARAMETER) && !type.equals(GlobalConstants.EVENT) &&
				!type.equals(GlobalConstants.SBMLSPECIES) && !type.equals(GlobalConstants.SBMLREACTION) && 
				!type.equals(GlobalConstants.FUNCTION) && !type.equals(GlobalConstants.UNIT) &&
				!type.equals(GlobalConstants.ASSIGNMENT_RULE) && !type.equals(GlobalConstants.RATE_RULE) &&
				!type.equals(GlobalConstants.ALGEBRAIC_RULE) && !type.equals(GlobalConstants.CONSTRAINT)  && 
				!type.equals(GlobalConstants.PROMOTER) && !type.equals(GlobalConstants.BOOLEAN) && 
				!type.equals(GlobalConstants.PLACE) && !type.equals(GlobalConstants.TRANSITION)) {
				portIds.add(portId);
				idRefs.add(idRef.replace("init__",""));
				types.add(type);
				JComboBox port = new JComboBox(Choices);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				dirport.setEnabled(false);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
			}
		}
		

		Submodel instance = bioModel.getSBMLCompModel().getListOfSubmodels().get(subModelId);
		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.IDstring, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Name field
		if (instance != null) {
			field = new PropertyField(GlobalConstants.NAME, instance.getName(), null, null, Utility.NAMEstring, paramsOnly, 
					"default", false);
		} else {
			field = new PropertyField(GlobalConstants.NAME, "", null, null, Utility.NAMEstring, paramsOnly, 
					"default", false);
		}
		fields.put(GlobalConstants.NAME, field);
		add(field);

		JLabel timeConvFactorLabel = new JLabel("Time Conversion Factor");
		JLabel extentConvFactorLabel = new JLabel("Extent Conversion Factor");
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new GridLayout(1, 2));
		JPanel extentPanel = new JPanel();
		extentPanel.setLayout(new GridLayout(1, 2));
		timePanel.add(timeConvFactorLabel);
		timePanel.add(timeConvFactorBox);
		extentPanel.add(extentConvFactorLabel);
		extentPanel.add(extentConvFactorBox);
		if (instance.isSetTimeConversionFactor()) {
			timeConvFactorBox.setSelectedItem(instance.getTimeConversionFactor());
		}
		if (instance.isSetExtentConversionFactor()) {
			extentConvFactorBox.setSelectedItem(instance.getExtentConversionFactor());
		}
		add(timePanel);
		add(extentPanel);
		// Parse out SBOL annotations and add to SBOL field
		if(!paramsOnly) {
			// Field for annotating submodel with SBOL DNA components
			List<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(instance);
			sbolField = new SBOLField(sbolURIs, GlobalConstants.SBOL_DNA_COMPONENT, gcmEditor, 2, false);
			add(sbolField);
		}
		
		// Port Map field
		JPanel headingPanel = new JPanel();
		JLabel typeLabel = new JLabel("Type");
		JLabel portLabel = new JLabel("Port");
		JLabel dirLabel = new JLabel("Direction");
		JLabel replLabel = new JLabel("Replacement");
		JLabel convLabel = new JLabel("Conversion");
		headingPanel.setLayout(new GridLayout(1, 5));
		headingPanel.add(typeLabel);
		headingPanel.add(portLabel);
		headingPanel.add(dirLabel);
		headingPanel.add(replLabel);
		headingPanel.add(convLabel);
		if (portIds.size()>0) {
			add(headingPanel);
		}
		//add(new JLabel("Ports"));
		for (int i = 0; i < portIds.size(); i++) {
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(idRefs.get(i));
			JLabel tempLabel2 = new JLabel(types.get(i));
			tempPanel.setLayout(new GridLayout(1, 5));
			tempPanel.add(tempLabel2);
			tempPanel.add(tempLabel);
			tempPanel.add(directionBox.get(i));
			tempPanel.add(portmapBox.get(i));
			tempPanel.add(convBox.get(i));
			add(tempPanel);
			directionBox.get(i).addActionListener(this);
			portmapBox.get(i).addActionListener(this);
		}
		if (instance!=null) {
			for (int j = 0; j < instance.getListOfDeletions().size(); j++) {
				Deletion deletion = instance.getListOfDeletions().get(j);
				int l = portIds.indexOf(deletion.getPortRef());
				if (l >= 0) {
					portmapBox.get(l).setSelectedItem("--delete--");
				}
			}
		}
		ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(bioModel.getSBMLDocument().getModel());
		for (int j = 0; j < elements.size(); j++) {
			SBase sbase = elements.get(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)SBMLutilities.getPlugin(CompConstant.namespaceURI, sbase, false);
			if (sbase.getElementName().equals(GlobalConstants.ASSIGNMENT_RULE)||
				sbase.getElementName().equals(GlobalConstants.RATE_RULE)||
				sbase.getElementName().equals(GlobalConstants.ALGEBRAIC_RULE)||
				sbase.getElementName().equals(GlobalConstants.CONSTRAINT)) {
				if (sbmlSBase!=null) {
					getPortMap(sbmlSBase,sbase.getMetaId());
				}
			} else {
				if (sbmlSBase!=null) {
					getPortMap(sbmlSBase,SBMLutilities.getId(sbase));
				}
			}
		}
		updateComboBoxEnabling();
		String oldName = null;
		if (selected != null) {
			oldName = selected;
			fields.get(GlobalConstants.ID).setValue(selected);
		}
		
		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}
	
	private void updateComboBoxEnabling() {
		for (int i = 0; i < portmapBox.size(); i++) {
			if (portmapBox.get(i).getSelectedIndex()<2 &&
				(directionBox.get(i).getSelectedIndex()!=0 ||
				 convBox.get(i).getSelectedIndex()!=0)) {
				directionBox.get(i).setSelectedIndex(0);
				directionBox.get(i).setEnabled(false);
				convBox.get(i).setSelectedIndex(0);
				convBox.get(i).setEnabled(false);
			} else if (portmapBox.get(i).getSelectedIndex()<2) {
				directionBox.get(i).setEnabled(false);
				convBox.get(i).setEnabled(false);
			} else if (directionBox.get(i).getSelectedIndex()==1 &&
					convBox.get(i).getSelectedIndex()!=0) {
					directionBox.get(i).setEnabled(true);
					convBox.get(i).setSelectedIndex(0);
					convBox.get(i).setEnabled(false);
			} else if (directionBox.get(i).getSelectedIndex()==1) {
					directionBox.get(i).setEnabled(true);
					convBox.get(i).setEnabled(false);
			} else {
				directionBox.get(i).setEnabled(true);
				convBox.get(i).setEnabled(true);
			}
		}
	}
	
	private void getPortMap(CompSBasePlugin sbmlSBase,String id) {
		for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) {
			ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
			if (replacement.getSubmodelRef().equals(subModelId)) {
				if (replacement.isSetPortRef()) {
					int l = portIds.indexOf(replacement.getPortRef());
					if (l >= 0) {
						portmapBox.get(l).setSelectedItem(id);
						if (!portmapBox.get(l).getSelectedItem().equals(id)) {
							portmapBox.get(l).addItem(id);
							portmapBox.get(l).setSelectedItem(id);
						}
						directionBox.get(l).setSelectedIndex(0);
						convBox.get(l).setSelectedIndex(0);
						if (replacement.isSetConversionFactor()) {
							convBox.get(l).setSelectedItem(replacement.getConversionFactor());
						}
					}
				} else if (replacement.isSetDeletion()) {
					Deletion deletion = bioModel.getSBMLCompModel().getListOfSubmodels().get(subModelId).getListOfDeletions().get(replacement.getDeletion());
					if (deletion!=null) {
						int l = portIds.indexOf(deletion.getPortRef());
						if (l >= 0) {
							portmapBox.get(l).setSelectedItem(id);
							if (!portmapBox.get(l).getSelectedItem().equals(id)) {
								portmapBox.get(l).addItem(id);
								portmapBox.get(l).setSelectedItem(id);
							}
							directionBox.get(l).setSelectedIndex(0);
							convBox.get(l).setSelectedIndex(0);
						}
					}
				}
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			ReplacedBy replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId)) {
				if (replacement.isSetPortRef()) {
					int l = portIds.indexOf(replacement.getPortRef());
					if (l >= 0) {
						portmapBox.get(l).setSelectedItem(id);
						if (!portmapBox.get(l).getSelectedItem().equals(id)) {
							portmapBox.get(l).addItem(id);
							portmapBox.get(l).setSelectedItem(id);
						}
						directionBox.get(l).setSelectedIndex(1);
						convBox.get(l).setSelectedIndex(0);
					}
				} 
			}
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
	
	private boolean removePortMaps(CompSBasePlugin sbmlSBase) {
		int j = 0;
		while (j < sbmlSBase.getListOfReplacedElements().size()) {
			ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j);
			if (replacement.getSubmodelRef().equals(subModelId) && 
					((replacement.isSetPortRef())||(replacement.isSetDeletion()))) { 
				SBMLutilities.removeFromParentAndDelete(replacement);
				return true;
			} else {
				j++;
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			ReplacedBy replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId) && (replacement.isSetPortRef())) {
				SBMLutilities.removeFromParentAndDelete(replacement);
				return true;
			}
		}
		return false;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Component Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (bioModel.isSIdInUse((String)fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (bioModel.isSIdInUse((String)fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			
			// Checks whether SBOL annotation on model needs to be deleted later when annotating component with SBOL
//			boolean removeModelSBOLAnnotationFlag = false;
//			if (!paramsOnly && sbolField.getSBOLURIs().size() > 0 && 
//					bioModel.getElementSBOLCount() == 0 && bioModel.getModelSBOLAnnotationFlag()) {
//				Object[] options = { "OK", "Cancel" };
//				int choice = JOptionPane.showOptionDialog(null, 
//						"SBOL associated to model elements can't coexist with SBOL associated to model itself unless" +
//						" the latter was previously generated from the former.  Remove SBOL associated to model?", 
//						"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
//				if (choice == JOptionPane.OK_OPTION)
//					removeModelSBOLAnnotationFlag = true;
//				else
//					return false;
//			}
			
			String id = fields.get(GlobalConstants.ID).getValue();
			Submodel instance = bioModel.getSBMLCompModel().getListOfSubmodels().get(subModelId);
			instance.setName(fields.get(GlobalConstants.NAME).getValue());
			if (instance != null) {
				//long k = 0;
				while (instance.getListOfDeletions().size()>0) {
					Deletion deletion = instance.getListOfDeletions().get(0);
					SBMLutilities.removeFromParentAndDelete(deletion);
					/*
					if (deletion.isSetPortRef() && portIds.contains(deletion.getPortRef())) {
					} else {
						k++;
					}
					*/
				}
			}
			if (timeConvFactorBox.getSelectedItem().equals("(none)")) {
				instance.unsetTimeConversionFactor();
			} else {
				instance.setTimeConversionFactor((String)timeConvFactorBox.getSelectedItem());
			}
			if (extentConvFactorBox.getSelectedItem().equals("(none)")) {
				instance.unsetExtentConversionFactor();
			} else {
				instance.setExtentConversionFactor((String)extentConvFactorBox.getSelectedItem());
			}
			ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(bioModel.getSBMLDocument().getModel());
			for (int j = 0; j < elements.size(); j++) {
				SBase sbase = elements.get(j);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)SBMLutilities.getPlugin(CompConstant.namespaceURI, sbase, false);
				if (sbmlSBase!=null) {
					if (removePortMaps(sbmlSBase)) {
						elements = SBMLutilities.getListOfAllElements(bioModel.getSBMLDocument().getModel());
					}
				}
			}
			for (int i = 0; i < portIds.size(); i++) {
				String subId = id;
				if (subModelId.startsWith("GRID__")) subId = subModelId;
				String portId = portIds.get(i);
				//String type = types.get(i);
				String portmapId = (String)portmapBox.get(i).getSelectedItem();
				if (!portmapId.equals("--none--")&&!portmapId.equals("--delete--")&&!portmapId.equals("--include--")) {
					CompSBasePlugin sbmlSBase = null;
					SBase sbase = SBMLutilities.getElementBySId(bioModel.getSBMLDocument().getModel(), portmapId);
					if (sbase!=null) {
						sbmlSBase = (CompSBasePlugin)SBMLutilities.getPlugin(CompConstant.namespaceURI, sbase, false);
						if (sbmlSBase != null) {
							if (directionBox.get(i).getSelectedIndex()==0) {
								ReplacedElement replacement = sbmlSBase.createReplacedElement();
								replacement.setSubmodelRef(subId);
								replacement.setPortRef(portId);
								if (!convBox.get(i).getSelectedItem().equals("(none)")) {
									replacement.setConversionFactor((String)convBox.get(i).getSelectedItem());
								}
							} else {
								ReplacedBy replacement = sbmlSBase.createReplacedBy();
								replacement.setSubmodelRef(subId);
								replacement.setPortRef(portId);
							}
						}
					} else {
						sbase = SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument().getModel(), portmapId);
						sbmlSBase = (CompSBasePlugin)SBMLutilities.getPlugin(CompConstant.namespaceURI, sbase, false);
						if (sbmlSBase != null) {
							if (directionBox.get(i).getSelectedIndex()==0) {
								/* TODO: Code below uses just a replacement */
								ReplacedElement replacement = sbmlSBase.createReplacedElement();
								replacement.setSubmodelRef(subId);
								replacement.setPortRef(portId);
								if (!convBox.get(i).getSelectedItem().equals("(none)")) {
									replacement.setConversionFactor((String)convBox.get(i).getSelectedItem());
								}
								String speciesId = portId.replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "");
								CompModelPlugin subCompModel = subBioModel.getSBMLCompModel();
								Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(subId);
								bioModel.addImplicitDeletions(subCompModel, submodel, speciesId);
								/* Code below using replacement and deletion */
								/*
								ReplacedElement replacement = sbmlSBase.createReplacedElement();
								replacement.setSubmodelRef(subId);
								Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(subId);
								Deletion deletion = submodel.createDeletion();
								deletion.setPortRef(portId);
								deletion.setId("delete_"+portId);
								replacement.setDeletion("delete_"+portId);
								*/
							} else {
								ReplacedBy replacement = sbmlSBase.createReplacedBy();
								replacement.setSubmodelRef(subId);
								replacement.setPortRef(portId);
								String speciesId = portId.replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "");
								CompModelPlugin subCompModel = subBioModel.getSBMLCompModel();
								Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(subId);
								bioModel.addImplicitReplacedBys(subCompModel,submodel,speciesId,SBMLutilities.getId(sbase));
							}
						}
					}
				} else if (portmapId.equals("--delete--")) {
					Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(subId);
					Deletion deletion = submodel.createDeletion();
					deletion.setPortRef(portId);
					String speciesId = portId.replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "");
					CompModelPlugin subCompModel = subBioModel.getSBMLCompModel();
					bioModel.addImplicitDeletions(subCompModel, submodel, speciesId);
				}
			}
			if (selected != null && !oldName.equals(id)) {
				bioModel.changeComponentName(oldName, id);
			}
			String newPort = bioModel.getComponentPortMap(id);
			componentsList.removeItem(oldName + " " + selectedComponent.replace(".xml", "") + " " + oldPort);
			componentsList.addItem(id + " " + selectedComponent.replace(".xml", "") + " " + newPort);
			componentsList.setSelectedValue(id + " " + selectedComponent.replace(".xml", "") + " " + newPort, true);
			if (!paramsOnly) {
				// Add SBOL annotation to submodel
				List<URI> sbolURIs = sbolField.getSBOLURIs();
				if (sbolURIs.size() > 0) {
					SBOLAnnotation sbolAnnot = new SBOLAnnotation(instance.getMetaId(), sbolURIs);
					AnnotationUtility.setSBOLAnnotation(instance, sbolAnnot);
					if (sbolField.wasInitiallyBlank())
						bioModel.setElementSBOLCount(bioModel.getElementSBOLCount() + 1);
//					if (removeModelSBOLAnnotationFlag) {
//						AnnotationUtility.removeSBOLAnnotation(bioModel.getSBMLDocument().getModel());
//						bioModel.setModelSBOLAnnotationFlag(false);
//						modelEditor.getSchematic().getSBOLDescriptorsButton().setEnabled(true);
//					}
				} else {
					AnnotationUtility.removeSBOLAnnotation(instance);
					if (!sbolField.wasInitiallyBlank())
						bioModel.setElementSBOLCount(bioModel.getElementSBOLCount() - 1);
				}
			}
			modelEditor.setDirty(true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			updateComboBoxEnabling();
		}
	}

}
