package biomodel.gui;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sbml.libsbml.CompSBasePlugin;
import org.sbml.libsbml.Deletion;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ReplacedElement;
import org.sbml.libsbml.Replacing;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.SBaseList;
import org.sbml.libsbml.Submodel;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
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
	
	private ModelEditor gcmEditor;
	
	private String subModelId;
	
	private JComboBox timeConvFactorBox;
	
	private JComboBox extentConvFactorBox;
	
	private boolean paramsOnly;

	public ComponentsPanel(String selected, PropertyList componentsList, BioModel bioModel, ArrayList<String> ports, 
			String selectedComponent, String oldPort, boolean paramsOnly, ModelEditor gcmEditor) {
		
		super(new GridLayout(ports.size() + 5, 1));
		this.selected = selected;
		this.componentsList = componentsList;
		this.bioModel = bioModel;
		this.gcmEditor = gcmEditor;
		this.selectedComponent = selectedComponent;
		this.oldPort = oldPort;
		this.paramsOnly = paramsOnly;

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
		
		String[] Choices = new String[2];
		Choices[0] = "--include--";
		Choices[1] = "--delete--";
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (!type.equals(GlobalConstants.COMPARTMENT) && !type.equals(GlobalConstants.PARAMETER) &&
				!type.equals(GlobalConstants.LOCALPARAMETER) &&
				!type.equals(GlobalConstants.SBMLSPECIES) && !type.equals(GlobalConstants.SBMLREACTION) && 
				!type.equals(GlobalConstants.FUNCTION) && !type.equals(GlobalConstants.UNIT)) {
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
		
		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.IDstring, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		add(field);
		
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
		add(headingPanel);
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
		SBaseList elements = bioModel.getSBMLDocument().getModel().getListOfAllElements();
		for (long j = 0; j < elements.getSize(); j++) {
			SBase sbase = elements.get(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getPlugin("comp");
			if (sbmlSBase!=null) {
				getPortMap(sbmlSBase,sbase.getId());
			}
		}
		Submodel instance = bioModel.getSBMLCompModel().getSubmodel(subModelId);
		if (instance!=null) {
			for (long j = 0; j < instance.getNumDeletions(); j++) {
				Deletion deletion = instance.getDeletion(j);
				int l = portIds.indexOf(deletion.getPortRef());
				if (l >= 0) {
					portmapBox.get(l).setSelectedItem("--delete--");
				}
			}
		}
		updateComboBoxEnabling();
		timeConvFactorBox.setSelectedItem(instance.getTimeConversionFactor());
		extentConvFactorBox.setSelectedItem(instance.getExtentConversionFactor());
		
		String oldName = null;
		if (selected != null) {
			oldName = selected;
			fields.get(GlobalConstants.ID).setValue(selected);
		}
		// Parse out SBOL annotations and add to SBOL field
		if(!paramsOnly) {
			// Field for annotating submodel with SBOL DNA components
			sbolField = new SBOLField(GlobalConstants.SBOL_DNA_COMPONENT, gcmEditor, 2);
			add(sbolField);
			LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(instance);
			if (sbolURIs.size() > 0)
				sbolField.setSBOLURIs(sbolURIs);
		}
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
		add(timePanel);
		add(extentPanel);
		
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
			} else if (directionBox.get(i).getSelectedIndex()==1 &&
					convBox.get(i).getSelectedIndex()!=0) {
				directionBox.get(i).setEnabled(true);
				convBox.get(i).setSelectedIndex(0);
				convBox.get(i).setEnabled(false);
			} else {
				directionBox.get(i).setEnabled(true);
				convBox.get(i).setEnabled(true);
			}
		}
	}
	
	private void getPortMap(CompSBasePlugin sbmlSBase,String id) {
		for (long k = 0; k < sbmlSBase.getNumReplacedElements(); k++) {
			ReplacedElement replacement = sbmlSBase.getReplacedElement(k);
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
				} 
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			Replacing replacement = sbmlSBase.getReplacedBy();
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
		long j = 0;
		while (j < sbmlSBase.getNumReplacedElements()) {
			ReplacedElement replacement = sbmlSBase.getReplacedElement(j);
			if (replacement.getSubmodelRef().equals(subModelId) && (replacement.isSetPortRef())) { 
				replacement.removeFromParentAndDelete();
				return true;
			} else {
				j++;
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			Replacing replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId) && (replacement.isSetPortRef())) {
				replacement.removeFromParentAndDelete();
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
			String id = fields.get(GlobalConstants.ID).getValue();
			if (selected != null && !oldName.equals(id)) {
				bioModel.changeComponentName(oldName, id);
			}

			Submodel instance = bioModel.getSBMLCompModel().getSubmodel(subModelId);
			if (instance != null) {
				long k = 0;
				while (k < instance.getNumDeletions()) {
					Deletion deletion = instance.getDeletion(k);
					if (deletion.isSetPortRef() && portIds.contains(deletion.getPortRef())) {
						deletion.removeFromParentAndDelete();
					} else {
						k++;
					}
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
			SBaseList elements = bioModel.getSBMLDocument().getModel().getListOfAllElements();
			for (long j = 0; j < elements.getSize(); j++) {
				SBase sbase = elements.get(j);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getPlugin("comp");
				if (sbmlSBase!=null) {
					if (removePortMaps(sbmlSBase)) {
						elements = bioModel.getSBMLDocument().getModel().getListOfAllElements();
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
					SBase sbase = bioModel.getSBMLDocument().getModel().getElementBySId(portmapId);
					sbmlSBase = (CompSBasePlugin)sbase.getPlugin("comp");
					if (sbmlSBase != null) {
						if (directionBox.get(i).getSelectedIndex()==0) {
							ReplacedElement replacement = sbmlSBase.createReplacedElement();
							replacement.setSubmodelRef(subId);
							replacement.setPortRef(portId);
							if (!convBox.get(i).getSelectedItem().equals("(none)")) {
								replacement.setConversionFactor((String)convBox.get(i).getSelectedItem());
							}
						} else {
							Replacing replacement = sbmlSBase.createReplacedBy();
							replacement.setSubmodelRef(subId);
							replacement.setPortRef(portId);
						}
					}
				} else if (portmapId.equals("--delete--")) {
					Submodel submodel = bioModel.getSBMLCompModel().getSubmodel(subId);
					Deletion deletion = submodel.createDeletion();
					deletion.setPortRef(portId);
				}
			}
			String newPort = bioModel.getComponentPortMap(id);
			componentsList.removeItem(oldName + " " + selectedComponent.replace(".xml", "") + " " + oldPort);
			componentsList.addItem(id + " " + selectedComponent.replace(".xml", "") + " " + newPort);
			componentsList.setSelectedValue(id + " " + selectedComponent.replace(".xml", "") + " " + newPort, true);
			if (!paramsOnly) {
				// Add SBOL annotation to submodel
				LinkedList<URI> sbolURIs = sbolField.getSBOLURIs();
				if (sbolURIs.size() > 0) {
					SBOLAnnotation sbolAnnot = new SBOLAnnotation(instance.getMetaId(), sbolURIs);
					AnnotationUtility.setSBOLAnnotation(instance, sbolAnnot);
				} else
					AnnotationUtility.removeSBOLAnnotation(instance);
			}
			gcmEditor.setDirty(true);
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
