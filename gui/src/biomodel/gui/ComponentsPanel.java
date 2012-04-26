package biomodel.gui;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sbml.libsbml.CompSBasePlugin;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Port;
import org.sbml.libsbml.ReplacedElement;
import org.sbml.libsbml.Replacing;
import org.sbml.libsbml.Species;

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
	
	private ArrayList<String> ports = null;

	private ArrayList<JComboBox> portmapBox = null;

	private ArrayList<JComboBox> directionBox = null;

	private BioModel gcm = null;

	private PropertyList componentsList = null;

	private HashMap<String, PropertyField> fields = null;

	private String selectedComponent, oldPort;
	
	private ModelEditor gcmEditor;

	public ComponentsPanel(String selected, PropertyList componentsList, BioModel gcm, 
			ArrayList<String> inputs, ArrayList<String> outputs, ArrayList<String> compartmentPorts,
			String selectedComponent, String oldPort, boolean paramsOnly, ModelEditor gcmEditor) {
		
		super(new GridLayout(inputs.size() + outputs.size() + compartmentPorts.size() + 2, 1));
		this.selected = selected;
		this.componentsList = componentsList;
		this.gcm = gcm;
		this.gcmEditor = gcmEditor;
		this.selectedComponent = selectedComponent;
		this.oldPort = oldPort;

		fields = new HashMap<String, PropertyField>();
		ports = new ArrayList<String>();
		portmapBox = new ArrayList<JComboBox>();
		directionBox = new ArrayList<JComboBox>();
		
		ArrayList <String> speciesList = gcm.getSpecies();
		Collections.sort(speciesList);
		String[] specsWithNone = new String[speciesList.size() + 1];
		specsWithNone[0] = "--none--";
		String[] directions = new String[2];
		directions[0] = "<--";
		directions[1] = "-->";
 		for (int l = 1; l < specsWithNone.length; l++) {
			specsWithNone[l] = speciesList.get(l - 1);
		}
		for (int i = 0; i < inputs.size(); i++) {
			ports.add(GlobalConstants.INPUT+"__"+inputs.get(i));
			JComboBox port = new JComboBox(specsWithNone);
			portmapBox.add(port);
			JComboBox dirport = new JComboBox(directions);
			directionBox.add(dirport);
		}
		for (int i = 0; i < outputs.size(); i++) {
			ports.add(GlobalConstants.OUTPUT+"__"+outputs.get(i));
			JComboBox port = new JComboBox(specsWithNone);
			portmapBox.add(port);
			JComboBox dirport = new JComboBox(directions);
			directionBox.add(dirport);
		}
		
		ArrayList <String> compartmentList = gcm.getCompartments();
		Collections.sort(compartmentList);
		String[] compsWithNone = new String[compartmentList.size() + 1];
		compsWithNone[0] = "--none--";
		for (int l = 1; l < compsWithNone.length; l++) {
			compsWithNone[l] = compartmentList.get(l - 1);
		}
		for (int i = 0; i < compartmentPorts.size(); i++) {
			ports.add(GlobalConstants.COMPARTMENT+"__"+compartmentPorts.get(i));
			JComboBox port = new JComboBox(compsWithNone);
			portmapBox.add(port);
			JComboBox dirport = new JComboBox(directions);
			directionBox.add(dirport);
		}
		
		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.IDstring, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		add(field);
		
		// Port Map field
		int i = 0;
		add(new JLabel("Ports"));
		for (String s : ports) {
			String type = s.substring(0,s.indexOf("__"));
			String id = s.substring(s.indexOf("__")+2);
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(id);
			JLabel tempLabel2 = new JLabel(type);
			tempPanel.setLayout(new GridLayout(1, 4));
			tempPanel.add(tempLabel2);
			tempPanel.add(tempLabel);
			tempPanel.add(directionBox.get(i));
			tempPanel.add(portmapBox.get(i));
			i++;
			add(tempPanel);
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumSpecies(); j++) {
			Species species = gcm.getSBMLDocument().getModel().getSpecies(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
			for (long k = 0; k < sbmlSBase.getNumReplacedElements(); k++) {
				ReplacedElement replacement = sbmlSBase.getReplacedElement(k);
				if (replacement.getSubmodelRef().equals(selected)) {
					if (replacement.isSetPortRef()) {
						int l = ports.indexOf(replacement.getPortRef());
						if (l >= 0) {
							portmapBox.get(l).setSelectedItem(species.getId());
							directionBox.get(l).setSelectedIndex(0);
						} 
					}
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				Replacing replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(selected)) {
					if (replacement.isSetPortRef()) {
						int l = ports.indexOf(replacement.getPortRef());
						if (l >= 0) {
							portmapBox.get(l).setSelectedItem(species.getId());
							directionBox.get(l).setSelectedIndex(1);
						}
					} 
				}
			}
		}
		for (long j = 0; j < gcm.getSBMLDocument().getModel().getNumCompartments(); j++) {
			Compartment compartment = gcm.getSBMLDocument().getModel().getCompartment(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)compartment.getPlugin("comp");
			for (long k = 0; k < sbmlSBase.getNumReplacedElements(); k++) {
				ReplacedElement replacement = sbmlSBase.getReplacedElement(k);
				if (replacement.getSubmodelRef().equals(selected)) {
					if (replacement.isSetPortRef()) {
						int l = ports.indexOf(replacement.getPortRef());
						if (l >= 0) {
							portmapBox.get(l).setSelectedItem(compartment.getId());
							directionBox.get(l).setSelectedIndex(0);
						}
					} 
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				Replacing replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(selected)) {
					if (replacement.isSetPortRef()) {
						int l = ports.indexOf(replacement.getPortRef());
						if (l >= 0) {
							portmapBox.get(l).setSelectedItem(compartment.getId());
							directionBox.get(l).setSelectedIndex(1);
						}
					} 
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

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue()) {
				return false;
			}
		}
		return true;
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
				if (gcm.getUsedIDs().contains((String)fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (gcm.getUsedIDs().contains((String)fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();
			if (selected != null && !oldName.equals(id)) {
				while (gcm.getUsedIDs().contains(selected)) {
					gcm.getUsedIDs().remove(selected);
				}
				gcm.changeComponentName(oldName, id);
			}
			if (!gcm.getUsedIDs().contains(id)) {
				gcm.getUsedIDs().add(id);
			}

			for (long i = 0; i < gcm.getSBMLDocument().getModel().getNumCompartments(); i++) {
				Compartment compartment = gcm.getSBMLDocument().getModel().getCompartment(i);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)compartment.getPlugin("comp");
				long j = 0;
				while (j < sbmlSBase.getNumReplacedElements()) {
					ReplacedElement replacement = sbmlSBase.getReplacedElement(j);
					if (replacement.getSubmodelRef().equals(selected) && (replacement.isSetPortRef()) &&
							(replacement.getPortRef().startsWith(GlobalConstants.COMPARTMENT+"__"))) {
						replacement.removeFromParentAndDelete();
					} else {
						j++;
					}
				}
				if (sbmlSBase.isSetReplacedBy()) {
					Replacing replacement = sbmlSBase.getReplacedBy();
					if (replacement.getSubmodelRef().equals(selected) && (replacement.isSetPortRef()) &&
							(replacement.getPortRef().startsWith(GlobalConstants.COMPARTMENT+"__"))) {
						replacement.removeFromParentAndDelete();
					}
				}
			}
			for (long i = 0; i < gcm.getSBMLDocument().getModel().getNumSpecies(); i++) {
				Species species = gcm.getSBMLDocument().getModel().getSpecies(i);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
				long j = 0;
				while (j < sbmlSBase.getNumReplacedElements()) {
					ReplacedElement replacement = sbmlSBase.getReplacedElement(j);
					if (replacement.getSubmodelRef().equals(selected) && (replacement.isSetPortRef()) &&
							((replacement.getPortRef().startsWith(GlobalConstants.INPUT+"__"))||
							 (replacement.getPortRef().startsWith(GlobalConstants.OUTPUT+"__")))) {
						replacement.removeFromParentAndDelete();
					} else {
						j++;
					}
				}
				if (sbmlSBase.isSetReplacedBy()) {
					Replacing replacement = sbmlSBase.getReplacedBy();
					if (replacement.getSubmodelRef().equals(selected) && (replacement.isSetPortRef()) &&
							((replacement.getPortRef().startsWith(GlobalConstants.INPUT+"__"))||
							 (replacement.getPortRef().startsWith(GlobalConstants.OUTPUT+"__")))) {
						replacement.removeFromParentAndDelete();
					}
				}
			}
			for (int i = 0; i < ports.size(); i++) {
				String port = ports.get(i);
				String portmapId = (String)portmapBox.get(i).getSelectedItem();
				if (!portmapId.equals("--none--")) {
					CompSBasePlugin sbmlSBase = null;
					if (port.startsWith(GlobalConstants.COMPARTMENT+"__")) {
						Compartment compartment = gcm.getSBMLDocument().getModel().getCompartment(portmapId);
						sbmlSBase = (CompSBasePlugin)compartment.getPlugin("comp");
					} else if ((port.startsWith(GlobalConstants.INPUT+"__")||
							   (port.startsWith(GlobalConstants.OUTPUT+"__")))) {
						Species species = gcm.getSBMLDocument().getModel().getSpecies(portmapId);
						sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
					}
					if (sbmlSBase != null) {
						if (directionBox.get(i).getSelectedIndex()==0) {
							ReplacedElement replacement = sbmlSBase.createReplacedElement();
							replacement.setSubmodelRef(id);
							replacement.setPortRef(port);
						} else {
							Replacing replacement = sbmlSBase.createReplacedBy();
							replacement.setSubmodelRef(id);
							replacement.setPortRef(port);
						}
					}
				}
			}
			String newPort = gcm.getComponentPortMap(id);
			componentsList.removeItem(oldName + " " + selectedComponent.replace(".gcm", "") + " " + oldPort);
			componentsList.addItem(id + " " + selectedComponent.replace(".gcm", "") + " " + newPort);
			componentsList.setSelectedValue(id + " " + selectedComponent.replace(".gcm", "") + " " + newPort, true);
			gcmEditor.setDirty(true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

}
