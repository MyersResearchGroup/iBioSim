package gcm.gui;

import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.Gui;


public class ComponentsPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String selected = "";

	private String[] options = { "Ok", "Cancel" };

	private ArrayList<JComboBox> portmapBox = null;
	
	private JCheckBox compartmentBox = null;
	
	private ArrayList<String> types = null;

	private GCMFile gcm = null;

	private PropertyList componentsList = null;

	private PropertyList influences = null;

	private HashMap<String, PropertyField> fields = null;

	private String[] species;

	private String selectedComponent, oldPort;
	
	private GCM2SBMLEditor gcmEditor;

	public ComponentsPanel(String selected, PropertyList componentsList, PropertyList influences,
			GCMFile gcm, String[] inputs, String[] outputs, String selectedComponent, String oldPort,
			boolean paramsOnly, GCM2SBMLEditor gcmEditor) {
		super(new GridLayout(inputs.length + outputs.length + 2, 1));
		this.selected = selected;
		this.componentsList = componentsList;
		this.influences = influences;
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
		String[] specs = gcm.getSpecies().keySet().toArray(new String[0]);
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
				Utility.IDstring, paramsOnly, "default");
		fields.put(GlobalConstants.ID, field);
		add(field);
		
		//Compartment check box
		/*
		compartmentBox = new JCheckBox();
		compartmentBox.addActionListener(this);
		compartmentBox.setEnabled(true);
		JPanel compartmentPanel = new JPanel();
		JLabel compartmentLabel = new JLabel("Compartment");
		compartmentPanel.setLayout(new GridLayout(1, 2));
		compartmentPanel.add(compartmentLabel);
		compartmentPanel.add(compartmentBox);
		add(compartmentPanel);
		*/
		
		// Port Map field
		add(new JLabel("Ports" /*GlobalConstants.PORTMAP*/));
		int i = 0;
		for (String s : inputs) {
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(s);
			JLabel tempLabel2 = new JLabel("Input");
			types.add("Input");
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(tempLabel2);
			//tempPanel.add(portmapBox.get(i));
			add(tempPanel);
			i++;
		}
		for (String s : outputs) {
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(s);
			JLabel tempLabel2 = new JLabel("Output");
			types.add("Output");
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(tempLabel2);
			//tempPanel.add(portmapBox.get(i));
			add(tempPanel);
			i++;
		}
		
		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getComponents().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
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
			if(oldName != null){
				for(Object s:gcm.getComponents().get(oldName).keySet()){
					String k = s.toString();
					String v = (gcm.getComponents().get(oldName).getProperty(k)).toString();
					if (k.contains("graph") || k.contains("row") || k.contains("col")) {
						property.put(k, v);
					}	
				}
			}
			
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
			/*
			if (compartmentBox.getSelectedObjects() != null)
				property.put("compartment", "true");
			else
				property.put("compartment", "false");
				*/
			if (selected != null && !oldName.equals(id)) {
				while (gcm.getUsedIDs().contains(selected)) {
					gcm.getUsedIDs().remove(selected);
				}
				gcm.changeComponentName(oldName, id);
				((DefaultListModel) influences.getModel()).clear();
				influences.addAllItem(gcm.getInfluences().keySet());
			}
			if (!gcm.getUsedIDs().contains(id)) {
				gcm.getUsedIDs().add(id);
			}
			gcm.addComponent(id, property);
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
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			// setType(typeBox.getSelectedItem().toString());
		}
	}

	private void loadProperties(Properties property) {
		/*
		if (property.containsKey("compartment"))
			compartmentBox.setSelected(Boolean.parseBoolean(property.get("compartment").toString()));
			*/
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}
	
}
