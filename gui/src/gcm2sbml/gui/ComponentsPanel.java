package gcm2sbml.gui;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ComponentsPanel extends JPanel implements ActionListener {
	private String selected = "";

	private String[] options = { "Ok", "Cancel" };

	private ArrayList<JComboBox> portmapBox = null;

	private GCMFile gcm = null;

	private PropertyList componentsList = null;

	private PropertyList influences = null;

	private HashMap<String, PropertyField> fields = null;

	private String[] species;

	private String selectedComponent, oldPort;

	public ComponentsPanel(String selected, PropertyList componentsList, PropertyList influences,
			GCMFile gcm, String[] species, String selectedComponent, String oldPort, boolean paramsOnly) {
		super(new GridLayout(species.length + 2, 1));
		this.selected = selected;
		this.componentsList = componentsList;
		this.influences = influences;
		this.gcm = gcm;
		this.species = species;
		this.selectedComponent = selectedComponent;
		this.oldPort = oldPort;

		fields = new HashMap<String, PropertyField>();
		portmapBox = new ArrayList<JComboBox>();
		for (int i = 0; i < species.length; i++) {
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
			JComboBox port = new JComboBox(specsWithNone);
			port.addActionListener(this);
			portmapBox.add(port);
		}

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null, Utility.IDstring,
				paramsOnly);
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Port Map field
		add(new JLabel(GlobalConstants.PORTMAP));
		int i = 0;
		for (String s : species) {
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(s);
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(portmapBox.get(i));
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
			if (!f.isValid()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(new JFrame(), this, "Component Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (gcm.getComponents().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Components id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (gcm.getComponents().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Components id already exists.");
					return false;
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			int i = 0;
			for (String s : species) {
				if (!portmapBox.get(i).getSelectedItem().toString().equals("--none--")) {
					property.put(s, portmapBox.get(i).getSelectedItem().toString());
				}
				i++;
			}
			property.put("gcm", selectedComponent);

			if (selected != null && !oldName.equals(id)) {
				gcm.changeComponentName(oldName, id);
				((DefaultListModel) influences.getModel()).clear();
				influences.addAllItem(gcm.getInfluences().keySet());
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
			componentsList.addItem(id + " " + selectedComponent.replace(".gcm", "") + " " + newPort);
			componentsList.setSelectedValue(id + " " + selectedComponent.replace(".gcm", "") + " "
					+ newPort, true);

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
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}
}
