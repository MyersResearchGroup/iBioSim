package biomodel.gui;


import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sbml.libsbml.CompSBasePlugin;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Deletion;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.ReplacedElement;
import org.sbml.libsbml.Replacing;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.SBaseList;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.Submodel;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import main.Gui;


public class ComponentsPanel extends JPanel {
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

	private BioModel bioModel = null;

	private PropertyList componentsList = null;

	private HashMap<String, PropertyField> fields = null;

	private String selectedComponent, oldPort;
	
	private ModelEditor gcmEditor;
	
	private String subModelId;

	public ComponentsPanel(String selected, PropertyList componentsList, BioModel bioModel, ArrayList<String> ports, 
			String selectedComponent, String oldPort, boolean paramsOnly, ModelEditor gcmEditor) {
		
		super(new GridLayout(ports.size() + 2, 1));
		this.selected = selected;
		this.componentsList = componentsList;
		this.bioModel = bioModel;
		this.gcmEditor = gcmEditor;
		this.selectedComponent = selectedComponent;
		this.oldPort = oldPort;

		fields = new HashMap<String, PropertyField>();
		portIds = new ArrayList<String>();
		idRefs = new ArrayList<String>();
		types = new ArrayList<String>();
		portmapBox = new ArrayList<JComboBox>();
		directionBox = new ArrayList<JComboBox>();

		String[] directions = new String[2];
		directions[0] = "<--";
		directions[1] = "-->";
		
		
		if (bioModel.isGridEnabled()) {
			subModelId = "GRID__" + selectedComponent.replace(".xml","");
		} else {
			subModelId = selected;
		}
		
		ArrayList <String> compartmentList = bioModel.getCompartments();
		Collections.sort(compartmentList);
		String[] compsWithNone = new String[compartmentList.size() + 2];
		compsWithNone[0] = "--none--";
		compsWithNone[1] = "--delete--";
		for (int l = 2; l < compsWithNone.length; l++) {
			compsWithNone[l] = compartmentList.get(l - 2);
		}
		for (int i = 0; i < ports.size(); i++) {
			String portId = ports.get(i).split(":")[0];
			String idRef = ports.get(i).split(":")[1];
			String type = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.COMPARTMENT)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(compsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
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
			String portId = ports.get(i).split(":")[0];
			String idRef = ports.get(i).split(":")[1];
			String type = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.PARAMETER)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(paramsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
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
			String portId = ports.get(i).split(":")[0];
			String idRef = ports.get(i).split(":")[1];
			String type = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.SBMLSPECIES)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(specsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
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
			String portId = ports.get(i).split(":")[0];
			String idRef = ports.get(i).split(":")[1];
			String type = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.SBMLREACTION)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(reactionsWithNone);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
			}
		}
		
		String[] Choices = new String[2];
		Choices[0] = "--include--";
		Choices[1] = "--delete--";
		for (int i = 0; i < ports.size(); i++) {
			String portId = ports.get(i).split(":")[0];
			String idRef = ports.get(i).split(":")[1];
			String type = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.INITIAL_ASSIGNMENT)) {
				portIds.add(portId);
				idRefs.add(idRef.replace("init__",""));
				types.add(type);
				JComboBox port = new JComboBox(Choices);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				dirport.setEnabled(false);
				directionBox.add(dirport);
			}
		}
		for (int i = 0; i < ports.size(); i++) {
			String portId = ports.get(i).split(":")[0];
			String idRef = ports.get(i).split(":")[1];
			String type = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.ASSIGNMENT_RULE) ||
					type.equals(GlobalConstants.RATE_RULE) ||
					type.equals(GlobalConstants.ALGEBRAIC_RULE)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(Choices);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				dirport.setEnabled(false);
				directionBox.add(dirport);
			}
		}
		for (int i = 0; i < ports.size(); i++) {
			String portId = ports.get(i).split(":")[0];
			String idRef = ports.get(i).split(":")[1];
			String type = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.CONSTRAINT)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(Choices);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				dirport.setEnabled(false);
				directionBox.add(dirport);
			}
		}
		for (int i = 0; i < ports.size(); i++) {
			String portId = ports.get(i).split(":")[0];
			String idRef = ports.get(i).split(":")[1];
			String type = ports.get(i).split(":")[2];
			if (type.equals(GlobalConstants.EVENT)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(Choices);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				dirport.setEnabled(false);
				directionBox.add(dirport);
			}
		}
		
		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.IDstring, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		add(field);
		
		// Port Map field
		add(new JLabel("Ports"));
		for (int i = 0; i < portIds.size(); i++) {
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(idRefs.get(i));
			JLabel tempLabel2 = new JLabel(types.get(i));
			tempPanel.setLayout(new GridLayout(1, 4));
			tempPanel.add(tempLabel2);
			tempPanel.add(tempLabel);
			tempPanel.add(directionBox.get(i));
			tempPanel.add(portmapBox.get(i));
			add(tempPanel);
		}
		SBaseList elements = bioModel.getSBMLDocument().getModel().getListOfAllElements();
		for (long j = 0; j < elements.getSize(); j++) {
			SBase sbase = elements.get(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getPlugin("comp");
			if (sbmlSBase!=null) {
				getPortMap(sbmlSBase,sbase.getId());
			}
		}
		/*
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumCompartments(); j++) {
			Compartment compartment = gcm.getSBMLDocument().getModel().getCompartment(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)compartment.getPlugin("comp");
			getPortMap(sbmlSBase,compartment.getId());
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumParameters(); j++) {
			Parameter parameter = gcm.getSBMLDocument().getModel().getParameter(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)parameter.getPlugin("comp");
			getPortMap(sbmlSBase,parameter.getId());
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumSpecies(); j++) {
			Species species = gcm.getSBMLDocument().getModel().getSpecies(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
			getPortMap(sbmlSBase,species.getId());
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumReactions(); j++) {
			Reaction reaction = gcm.getSBMLDocument().getModel().getReaction(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)reaction.getPlugin("comp");
			getPortMap(sbmlSBase,reaction.getId());
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumInitialAssignments(); j++) {
			InitialAssignment initial = gcm.getSBMLDocument().getModel().getInitialAssignment(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)initial.getPlugin("comp");
			getPortMap(sbmlSBase,initial.getMetaId());
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumRules(); j++) {
			Rule rule = gcm.getSBMLDocument().getModel().getRule(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)rule.getPlugin("comp");
			getPortMap(sbmlSBase,rule.getMetaId());
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumConstraints(); j++) {
			Constraint constraint = gcm.getSBMLDocument().getModel().getConstraint(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)constraint.getPlugin("comp");
			getPortMap(sbmlSBase,constraint.getMetaId());
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumEvents(); j++) {
			Event event = gcm.getSBMLDocument().getModel().getEvent(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)event.getPlugin("comp");
			getPortMap(sbmlSBase,event.getId());
		}
		*/
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
	
	private void getPortMap(CompSBasePlugin sbmlSBase,String id) {
		for (long k = 0; k < sbmlSBase.getNumReplacedElements(); k++) {
			ReplacedElement replacement = sbmlSBase.getReplacedElement(k);
			if (replacement.getSubmodelRef().equals(subModelId)) {
				if (replacement.isSetPortRef()) {
					int l = portIds.indexOf(replacement.getPortRef());
					if (l >= 0) {
						portmapBox.get(l).setSelectedItem(id);
						directionBox.get(l).setSelectedIndex(0);
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
						directionBox.get(l).setSelectedIndex(1);
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
	
	private void removePortMaps(CompSBasePlugin sbmlSBase) {
		long j = 0;
		while (j < sbmlSBase.getNumReplacedElements()) {
			ReplacedElement replacement = sbmlSBase.getReplacedElement(j);
			if (replacement.getSubmodelRef().equals(subModelId) && (replacement.isSetPortRef())) { 
				replacement.removeFromParentAndDelete();
			} else {
				j++;
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			Replacing replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId) && (replacement.isSetPortRef())) {
				replacement.removeFromParentAndDelete();
			}
		}
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
			for (long i = 0; i < bioModel.getSBMLDocument().getModel().getNumCompartments(); i++) {
				Compartment compartment = bioModel.getSBMLDocument().getModel().getCompartment(i);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)compartment.getPlugin("comp");
				removePortMaps(sbmlSBase);
			}
			for (long i = 0; i < bioModel.getSBMLDocument().getModel().getNumParameters(); i++) {
				Parameter parameter = bioModel.getSBMLDocument().getModel().getParameter(i);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)parameter.getPlugin("comp");
				removePortMaps(sbmlSBase);
			}
			for (long i = 0; i < bioModel.getSBMLDocument().getModel().getNumSpecies(); i++) {
				Species species = bioModel.getSBMLDocument().getModel().getSpecies(i);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
				removePortMaps(sbmlSBase);
			}
			for (long i = 0; i < bioModel.getSBMLDocument().getModel().getNumReactions(); i++) {
				Reaction reaction = bioModel.getSBMLDocument().getModel().getReaction(i);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)reaction.getPlugin("comp");
				removePortMaps(sbmlSBase);
			}
			for (int i = 0; i < portIds.size(); i++) {
				String subId = id;
				if (subModelId.startsWith("GRID__")) subId = subModelId;
				String portId = portIds.get(i);
				String type = types.get(i);
				String portmapId = (String)portmapBox.get(i).getSelectedItem();
				if (!portmapId.equals("--none--")&&!portmapId.equals("--delete--")&&!portmapId.equals("--include--")) {
					CompSBasePlugin sbmlSBase = null;
					if (type.equals(GlobalConstants.COMPARTMENT)) {
						Compartment compartment = bioModel.getSBMLDocument().getModel().getCompartment(portmapId);
						sbmlSBase = (CompSBasePlugin)compartment.getPlugin("comp");
					} else if (type.equals(GlobalConstants.PARAMETER)) {
						Parameter parameter = bioModel.getSBMLDocument().getModel().getParameter(portmapId);
						sbmlSBase = (CompSBasePlugin)parameter.getPlugin("comp");
					} else if (type.equals(GlobalConstants.SBMLSPECIES)) {
						Species species = bioModel.getSBMLDocument().getModel().getSpecies(portmapId);
						sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
					} else if (type.equals(GlobalConstants.SBMLREACTION)) {
						Reaction reaction = bioModel.getSBMLDocument().getModel().getReaction(portmapId);
						sbmlSBase = (CompSBasePlugin)reaction.getPlugin("comp");
					}
					if (sbmlSBase != null) {
						if (directionBox.get(i).getSelectedIndex()==0) {
							ReplacedElement replacement = sbmlSBase.createReplacedElement();
							replacement.setSubmodelRef(subId);
							replacement.setPortRef(portId);
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
			gcmEditor.setDirty(true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

}
