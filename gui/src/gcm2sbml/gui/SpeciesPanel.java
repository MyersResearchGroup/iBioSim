package gcm2sbml.gui;

import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class SpeciesPanel extends JPanel implements ActionListener {
	public SpeciesPanel(String selected, PropertyList speciesList, PropertyList influencesList,
			PropertyList conditionsList, PropertyList componentsList, GCMFile gcm, boolean paramsOnly, GCMFile refGCM) {
		super(new GridLayout(7, 1));
		this.selected = selected;
		this.speciesList = speciesList;
		this.influences = influencesList;
		this.conditions = conditionsList;
		this.components = componentsList;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null, Utility.IDstring,
				paramsOnly);
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Name field
		field = new PropertyField(GlobalConstants.NAME, "", null, null, Utility.NAMEstring, paramsOnly);
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.NAME, field);
		add(field);

		// Type field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel(GlobalConstants.TYPE);
		typeBox = new JComboBox(types);
		typeBox.setSelectedItem(types[1]);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			typeBox.setEnabled(false);
		}
		add(tempPanel);

		// Initial field
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.INITIAL_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.INITIAL_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(GlobalConstants.INITIAL_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.INITIAL_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.INITIAL_STRING);
			}
			field = new PropertyField(GlobalConstants.INITIAL_STRING, gcm
					.getParameter(GlobalConstants.INITIAL_STRING), PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		}
		else {
			field = new PropertyField(GlobalConstants.INITIAL_STRING, gcm
					.getParameter(GlobalConstants.INITIAL_STRING), PropertyField.states[0], gcm
					.getParameter(GlobalConstants.INITIAL_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.INITIAL_STRING, field);
		add(field);

		// Max dimer field
		// field = new PropertyField(GlobalConstants.MAX_DIMER_STRING, gcm
		// .getParameter(GlobalConstants.MAX_DIMER_STRING),
		// PropertyField.states[0], gcm
		// .getParameter(GlobalConstants.MAX_DIMER_STRING),
		// Utility.NUMstring);
		// fields.put(GlobalConstants.MAX_DIMER_STRING, field);
		// add(field);

		// Dimerization field
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KASSOCIATION_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KASSOCIATION_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(GlobalConstants.KASSOCIATION_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KASSOCIATION_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KASSOCIATION_STRING);
			}
			field = new PropertyField(GlobalConstants.KASSOCIATION_STRING, gcm
					.getParameter(GlobalConstants.KASSOCIATION_STRING), PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		}
		else {
			field = new PropertyField(GlobalConstants.KASSOCIATION_STRING, gcm
					.getParameter(GlobalConstants.KASSOCIATION_STRING), PropertyField.states[0], gcm
					.getParameter(GlobalConstants.KASSOCIATION_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.KASSOCIATION_STRING, field);
		add(field);

		// Decay field
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KDECAY_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KDECAY_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(GlobalConstants.KDECAY_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KDECAY_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KDECAY_STRING);
			}
			field = new PropertyField(GlobalConstants.KDECAY_STRING, gcm
					.getParameter(GlobalConstants.KDECAY_STRING), PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		}
		else {
			field = new PropertyField(GlobalConstants.KDECAY_STRING, gcm
					.getParameter(GlobalConstants.KDECAY_STRING), PropertyField.states[0], gcm
					.getParameter(GlobalConstants.KDECAY_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.KDECAY_STRING, field);
		add(field);

		// Complex Equilibrium Constant Field
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KCOMPLEX_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KCOMPLEX_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(GlobalConstants.KCOMPLEX_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KCOMPLEX_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KCOMPLEX_STRING);
			}
			field = new PropertyField(GlobalConstants.KCOMPLEX_STRING, gcm
					.getParameter(GlobalConstants.KCOMPLEX_STRING), PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		}
		else {
			field = new PropertyField(GlobalConstants.KCOMPLEX_STRING, gcm
					.getParameter(GlobalConstants.KCOMPLEX_STRING), PropertyField.states[0], gcm
					.getParameter(GlobalConstants.KCOMPLEX_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.KCOMPLEX_STRING, field);
		add(field);
		
		
		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getSpecies().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			typeBox.setSelectedItem(prop.getProperty(GlobalConstants.TYPE));
			setType(prop.getProperty(GlobalConstants.TYPE));
			loadProperties(prop);
		}
		else {
			setType(types[1]);
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
		int value = JOptionPane.showOptionDialog(BioSim.frame, this, "Species Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (gcm.getComponents().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Species id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (gcm.getComponents().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Species id already exists.");
					return false;
				}
			}
			if (oldName != null
					&& !gcm.editSpeciesCheck(oldName, typeBox.getSelectedItem().toString())) {
				Utility.createErrorMessage("Error", "Cannot change species type.  "
						+ "Species is used as a port in a component.");
				return false;
			}
			String id = fields.get(GlobalConstants.ID).getValue();

			
			Properties property = new Properties();
			
			// copy the old values into the new property. Some will then be overwritten , 
			// but others (such as positioning info) will not and need to be preserved.
			if(oldName != null){
				for(Object s:gcm.getSpecies().get(oldName).keySet()){
					String k = s.toString();
					String v = (gcm.getSpecies().get(oldName).getProperty(k)).toString();
					if (!k.equals("label")) {
						property.put(k, v);
					}	
				}
			}
			
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1]) || f.getState().equals(PropertyField.paramStates[1])) {
					property.put(f.getKey(), f.getValue());
				}
				else {
					property.remove(f.getKey());
				}
			}
			property.put(GlobalConstants.TYPE, typeBox.getSelectedItem().toString());

			if (selected != null && !oldName.equals(id)) {
				gcm.changeSpeciesName(oldName, id);
				((DefaultListModel) influences.getModel()).clear();
				influences.addAllItem(gcm.getInfluences().keySet());
				((DefaultListModel) conditions.getModel()).clear();
				conditions.addAllItem(gcm.getConditions());
				((DefaultListModel) components.getModel()).clear();
				for (String c : gcm.getComponents().keySet()) {
					components.addItem(c + " "
							+ gcm.getComponents().get(c).getProperty("gcm").replace(".gcm", "")
							+ " " + gcm.getComponentPortMap(c));
				}
			}
			gcm.addSpecies(id, property);
			if (paramsOnly) {
				if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(PropertyField.paramStates[1])
						|| fields.get(GlobalConstants.KASSOCIATION_STRING).getState().equals(
								PropertyField.paramStates[1])
						|| fields.get(GlobalConstants.KDECAY_STRING).getState().equals(PropertyField.paramStates[1])) {
					id += " Modified";
				}
			}
			speciesList.removeItem(oldName);
			speciesList.removeItem(oldName + " Modified");
			speciesList.addItem(id);
			speciesList.setSelectedValue(id, true);

		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public String updates() {
		String updates = "";
		if (paramsOnly) {
			if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(PropertyField.paramStates[1])) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ CompatibilityFixer.getSBMLName(GlobalConstants.INITIAL_STRING) + " "
						+ fields.get(GlobalConstants.INITIAL_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KASSOCIATION_STRING).getState()
					.equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KASSOCIATION_STRING) + " "
				+ fields.get(GlobalConstants.KASSOCIATION_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KDECAY_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KDECAY_STRING) + " "
				+ fields.get(GlobalConstants.KDECAY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KCOMPLEX_STRING) + " "
				+ fields.get(GlobalConstants.KCOMPLEX_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/";
			}
		}
		return updates;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			setType(typeBox.getSelectedItem().toString());
		}
	}

	private void setType(String type) {
		if (type.equals(types[0])) {
			// fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(false);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
		}
		else if (type.equals(types[1])) {
			// fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
		}
		else if (type.equals(types[2])) {
			// fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
		} else {
			// fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(false);
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

	private String selected = "";

	private PropertyList speciesList = null;

	private PropertyList influences = null;
	
	private PropertyList conditions = null;
	
	private PropertyList components = null;

	private String[] options = { "Ok", "Cancel" };

	private GCMFile gcm = null;

	private JComboBox typeBox = null;

	private static final String[] types = new String[] { "input", "internal", "output", "unconstrained"};

	private HashMap<String, PropertyField> fields = null;

	private boolean paramsOnly;
}
