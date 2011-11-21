package biomodel.gui;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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

	private ArrayList<JComboBox> portmapBox = null;
	
	private ArrayList<String> types = null;

	private BioModel gcm = null;

	private PropertyList componentsList = null;

	//private PropertyList influences = null;

	private HashMap<String, PropertyField> fields = null;

	private String[] species;

	private String selectedComponent, oldPort;
	
	private ModelEditor gcmEditor;

	public ComponentsPanel(String selected, PropertyList componentsList, PropertyList influences,
			BioModel gcm, String[] inputs, String[] outputs, String selectedComponent, String oldPort,
			boolean paramsOnly, ModelEditor gcmEditor) {
		super(new GridLayout(inputs.length + outputs.length + 2, 1));
		this.selected = selected;
		this.componentsList = componentsList;
		//this.influences = influences;
		this.gcm = gcm;
		this.gcmEditor = gcmEditor;
		species = new String[inputs.length + outputs.length];
		for(int i = 0; i < inputs.length; i++) {
			species[i] = inputs[i];
		}
		for(int i = 0; i < outputs.length; i++) {
			species[inputs.length + i] = outputs[i];
		}
		this.selectedComponent = selectedComponent;
		this.oldPort = oldPort;

		fields = new HashMap<String, PropertyField>();
		portmapBox = new ArrayList<JComboBox>();
		types = new ArrayList<String>();
		String[] specs = gcm.getSpecies().toArray(new String[0]);
		int j, k;
		String index;
		for (j = 1; j < specs.length; j++) {
			index = specs[j];
			k = j;
			while ((k > 0) && specs[k - 1].compareToIgnoreCase(index) > 0) {
				specs[k] = specs[k - 1];
				k = k - 1;
			}
			specs[k] = index;
		}
		String[] specsWithNone = new String[specs.length + 1];
		specsWithNone[0] = "--none--";
		for (int l = 1; l < specsWithNone.length; l++) {
			specsWithNone[l] = specs[l - 1];
		}
		for (int i = 0; i < inputs.length; i++) {
			JComboBox port = new JComboBox(specsWithNone);
			port.addActionListener(this);
			portmapBox.add(port);
		}
		for (int i = 0; i < outputs.length; i++) {
			JComboBox port = new JComboBox(specsWithNone);
			port.addActionListener(this);
			portmapBox.add(port);
		}
		
		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.IDstring, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		add(field);
		
		// Port Map field
		add(new JLabel("Ports"));
		for (String s : inputs) {
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(s);
			JLabel tempLabel2 = new JLabel("Input");
			types.add("Input");
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(tempLabel2);
			add(tempPanel);
		}
		for (String s : outputs) {
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(s);
			JLabel tempLabel2 = new JLabel("Output");
			types.add("Output");
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(tempLabel2);
			add(tempPanel);
		}
		
		String oldName = null;
		if (selected != null) {
			oldName = selected;
			fields.get(GlobalConstants.ID).setValue(selected);
			/*
			Properties prop = gcm.getComponents().get(selected);
			i = 0;
			for (String s : species) {
				if (prop.containsKey(s)) {
					portmapBox.get(i).setSelectedItem(prop.getProperty(s));
				}
				else {
					portmapBox.get(i).setSelectedIndex(0);
				}
				i++;
			}
			// typeBox.setSelectedItem(prop.getProperty(GlobalConstants.TYPE));
			loadProperties(prop);
			*/
		}

		// setType(types[0]);
		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue() /*|| f.getValue().equals("RNAP") || 
					f.getValue().endsWith("_RNAP") || f.getValue().endsWith("_bound")*/) {
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

			// Check to see if we need to add or edit
			Properties property = new Properties();
			
			// copy the old positioning values.
			/*
			if(oldName != null){
				for(Object s:gcm.getComponents().get(oldName).keySet()){
					String k = s.toString();
					String v = (gcm.getComponents().get(oldName).getProperty(k)).toString();
					if (k.contains("graph") || k.contains("row") || k.contains("col")) {
						property.put(k, v);
					}	
				}
			}
			*/
			
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			int i = 0;
			for (String s : species) {
				if (!portmapBox.get(i).getSelectedItem().toString().equals("--none--")) {
					property.put(s, portmapBox.get(i).getSelectedItem().toString());
					property.put("type_" + s, types.get(i));
				}
				i++;
			}
			property.put("gcm", selectedComponent);
			if (selected != null && !oldName.equals(id)) {
				while (gcm.getUsedIDs().contains(selected)) {
					gcm.getUsedIDs().remove(selected);
				}
				gcm.changeComponentName(oldName, id);
			}
			if (!gcm.getUsedIDs().contains(id)) {
				gcm.getUsedIDs().add(id);
			}
			String newPort = "(";
			boolean added = false;
			for (int j = 0; j < species.length; j++) {
				if (!portmapBox.get(j).getSelectedItem().toString().equals("--none--")) {
					newPort += species[j] + "->" + portmapBox.get(j).getSelectedItem() + ", ";
					added = true;
				}
			}
			if (added) {
				newPort = newPort.substring(0, newPort.length() - 2);
			}
			newPort += ")";
			componentsList.removeItem(oldName + " " + selectedComponent.replace(".gcm", "") + " "
					+ oldPort);
			componentsList
					.addItem(id + " " + selectedComponent.replace(".gcm", "") + " " + newPort);
			componentsList.setSelectedValue(id + " " + selectedComponent.replace(".gcm", "") + " "
					+ newPort, true);
			gcmEditor.setDirty(true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			// setType(typeBox.getSelectedItem().toString());
		}
	}
	
}
